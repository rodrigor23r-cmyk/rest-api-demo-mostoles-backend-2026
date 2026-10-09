package com.example.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.io.IOException;

import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.example.controllers.ProductController;
import com.example.dto.ProductDTO;
import com.example.entities.Product;
import com.example.mappers.ProductMapper;
import com.example.spring_security_jwt.entities.ERole;

/**
 * Unico sitio del proyecto donde se construyen los enlaces HATEOAS de
 * Product: tanto los de navegacion (self, paginacion) como los de mutacion
 * (create, update, delete). Antes, cada metodo de ProductController repetia
 * su propio linkTo(methodOn(...)); ahora toda esa logica vive aqui, en un
 * solo componente de Spring.
 */
@Component
public class ProductModelAssembler extends RepresentationModelAssemblerSupport<Product, ProductDTO> {

    private final ProductMapper productMapper;

    /**
     * Bean (org.springframework.data.web.PagedResourcesAssembler, del modulo
     * spring-data-commons) que Spring auto-configura en cuanto detecta en el
     * classpath Spring HATEOAS junto con el soporte de paginacion de Spring
     * Data (Pageable/Page), ambos ya presentes en este proyecto. Sabe
     * construir, a partir de un Page<T> y un RepresentationModelAssembler<T, D>,
     * un PagedModel<D> con los enlaces de navegacion (self, first, prev, next,
     * last) ya calculados.
     */
    private final PagedResourcesAssembler<Product> pagedResourcesAssembler;

    public ProductModelAssembler(ProductMapper productMapper,
            PagedResourcesAssembler<Product> pagedResourcesAssembler) {
        super(ProductController.class, ProductDTO.class);
        this.productMapper = productMapper;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    /**
     * RepresentationModelAssemblerSupport usa este metodo, por defecto, para
     * crear una instancia "en blanco" del DTO mediante reflexion (su
     * constructor vacio) y no sabe como rellenar sus campos. Lo sobreescribimos
     * para que la conversion de verdad (Product -> ProductDTO) la haga,
     * como en el resto del proyecto, el mapper de MapStruct.
     */
    @Override
    protected ProductDTO instantiateModel(Product entity) {
        return productMapper.toDto(entity);
    }

    /**
     * Convierte un Product en su ProductDTO, con sus enlaces:
     *
     * - self (de navegacion, vía createModelWithId) y all products (de
     *   navegacion, a la coleccion): se añaden siempre, porque los endpoints
     *   GET los permite tanto ROLE_USER como ROLE_ADMIN
     * - update y delete (de mutacion): solo se añaden si quien hace la
     *   peticion tiene ROLE_ADMIN, porque esos son los unicos que sus
     *   endpoints (PUT y DELETE) autorizan de verdad (@PreAuthorize
     *   ("hasRole('ADMIN')") en ProductController). Asi, el cliente nunca ve
     *   anunciado un enlace que, si lo sigue, le va a devolver un 403.
     */
    @Override
    public ProductDTO toModel(Product entity) {

        ProductDTO productDTO = createModelWithId(entity.getId(), entity);

        productDTO.add(allProductsLink());

        if (currentUserIsAdmin()) {
            productDTO.add(updateProductLink(entity.getId()));
            productDTO.add(deleteProductLink(entity.getId()));
        }

        return productDTO;
    }

    /**
     * Version SIN paginar de la coleccion (cuando no se recibe page/size en la
     * peticion): reutiliza toModel(Product) para cada elemento (heredado de
     * RepresentationModelAssemblerSupport) y añade el enlace "self" de la
     * propia coleccion y, solo si quien pregunta es ROLE_ADMIN, el enlace de
     * mutacion "create" (el POST solo lo autoriza ROLE_ADMIN).
     */
    @Override
    public CollectionModel<ProductDTO> toCollectionModel(Iterable<? extends Product> entities) {

        CollectionModel<ProductDTO> collectionModel = super.toCollectionModel(entities);

        collectionModel.add(linkTo(methodOn(ProductController.class).dameProductos(null, null)).withSelfRel());

        if (currentUserIsAdmin()) {
            collectionModel.add(createProductLink());
        }

        return collectionModel;
    }

    /**
     * Version PAGINADA de la coleccion (cuando si se recibe page/size): usa el
     * PagedResourcesAssembler de Spring HATEOAS, que ya sabe calcular, a partir
     * de la peticion HTTP actual y del Page<Product> recibido de la capa de
     * persistencia, los enlaces first/prev/self/next/last (omitiendo "prev" en
     * la primera pagina y "next" en la ultima, automaticamente). Aqui solo
     * añadimos, encima y solo si quien pregunta es ROLE_ADMIN, el enlace de
     * mutacion "create".
     */
    public PagedModel<ProductDTO> toPagedModel(Page<Product> productPage) {

        PagedModel<ProductDTO> pagedModel = pagedResourcesAssembler.toModel(productPage, this);

        if (currentUserIsAdmin()) {
            pagedModel.add(createProductLink());
        }

        return pagedModel;
    }

    /**
     * Comprueba si quien ha hecho la peticion HTTP actual tiene ROLE_ADMIN,
     * exactamente el mismo criterio que usan los @PreAuthorize("hasRole('ADMIN')")
     * de saveProduct, updateProduct y deleteProducto en ProductController. Por
     * eso los enlaces de mutacion (create/update/delete) solo se generan para
     * quien, de verdad, puede usarlos: nunca se anuncia un enlace que
     * terminaria en un 403.
     */
    private boolean currentUserIsAdmin() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(ERole.ROLE_ADMIN.name()));
    }

    /**
     * Enlace de navegacion a la coleccion completa de productos (sin
     * paginar). Publico porque el controlador tambien lo necesita en el
     * metodo de borrado (deleteProducto), donde no hay ningun Product que
     * convertir a ProductDTO (ya se ha eliminado), pero si conviene indicarle
     * al cliente a donde dirigirse a continuacion.
     */
    public Link allProductsLink() {
        return linkTo(methodOn(ProductController.class).dameProductos(null, null)).withRel("all products");
    }

    /**
     * Enlace de MUTACION a nivel de coleccion: le dice al cliente que en esa
     * misma URI (la raiz de la coleccion) se admite una peticion POST para
     * crear un nuevo producto.
     *
     * Los argumentos que se le pasan a saveProduct(...) son irrelevantes (se
     * usan null): methodOn(...) no ejecuta el metodo real, solo intercepta la
     * llamada para leer sus anotaciones (@PostMapping) y construir la URI.
     */
    public Link createProductLink() {
        try {
            return linkTo(methodOn(ProductController.class).saveProduct(null, null, null)).withRel("create");
        } catch (IOException e) {
            // saveProduct() declara throws IOException, pero methodOn(...) nunca
            // llega a ejecutar el metodo real: solo intercepta la invocacion para
            // leer sus anotaciones y construir la URI. Esta excepcion jamas salta
            // en tiempo de ejecucion; este catch es solo para satisfacer al
            // compilador.
            throw new IllegalStateException(e);
        }
    }

    /**
     * Enlace de MUTACION a nivel de producto: le dice al cliente que esa
     * misma URI (/products/{id}) admite una peticion PUT para actualizarlo.
     */
    public Link updateProductLink(int productId) {
        try {
            return linkTo(methodOn(ProductController.class).updateProduct(null, null, null, productId))
                    .withRel("update");
        } catch (IOException e) {
            // Mismo motivo que en createProductLink(): updateProduct() declara
            // throws IOException, pero nunca se ejecuta de verdad a traves de
            // methodOn(...).
            throw new IllegalStateException(e);
        }
    }

    /**
     * Enlace de MUTACION a nivel de producto: le dice al cliente que esa
     * misma URI (/products/{id}) admite una peticion DELETE para eliminarlo.
     */
    public Link deleteProductLink(int productId) {
        return linkTo(methodOn(ProductController.class).deleteProducto(productId)).withRel("delete");
    }
}
