package com.example.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import com.example.controllers.ProductController;
import com.example.dto.ProductDTO;
import com.example.entities.Product;
import com.example.mappers.ProductMapper;

/**
 * Unico sitio del proyecto donde se construyen los enlaces HATEOAS de
 * Product. Antes, cada metodo de ProductController repetia su propio
 * linkTo(methodOn(...)) para el self link y para el link a "all products";
 * ahora toda esa logica vive aqui, en un solo componente de Spring
 * (RepresentationModelAssemblerSupport ya es la clase pensada por Spring
 * HATEOAS exactamente para esto).
 *
 * RepresentationModelAssemblerSupport<Product, ProductDTO> necesita que
 * ProductDTO sea, el mismo, un RepresentationModel (por eso ProductDTO ahora
 * extiende de RepresentationModel<ProductDTO> en vez de ser un record), ya
 * que es el propio ProductDTO quien lleva encima la coleccion de enlaces
 * (_links), sin necesidad de envolverlo aparte en un EntityModel<ProductDTO>.
 */
@Component
public class ProductModelAssembler extends RepresentationModelAssemblerSupport<Product, ProductDTO> {

    private final ProductMapper productMapper;

    public ProductModelAssembler(ProductMapper productMapper) {
        // El primer argumento es el controlador contra el que se resuelven
        // los enlaces relativos (por ejemplo, el "self" que se calcula en
        // createModelWithId); el segundo es el tipo de recurso que este
        // assembler produce.
        super(ProductController.class, ProductDTO.class);
        this.productMapper = productMapper;
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
     * Convierte un Product en su ProductDTO, ya con el enlace "self" (gracias
     * a createModelWithId, heredado de RepresentationModelAssemblerSupport,
     * que internamente llama a instantiateModel(entity) y le añade
     * linkTo(ProductController.class).slash(id).withSelfRel()) y con el enlace
     * "all products" añadido a mano, una sola vez, aqui.
     */
    @Override
    public ProductDTO toModel(Product entity) {

        ProductDTO productDTO = createModelWithId(entity.getId(), entity);
        productDTO.add(allProductsLink());

        return productDTO;
    }

    /**
     * Version sin paginar de la coleccion: añade, ademas del enlace de cada
     * producto (heredado, reutiliza toModel(Product) para cada elemento), el
     * enlace "self" de la propia coleccion.
     */
    @Override
    public CollectionModel<ProductDTO> toCollectionModel(Iterable<? extends Product> entities) {
        return toCollectionModel(entities, null, null);
    }

    /**
     * Version paginada: igual que la anterior, pero el enlace "self" de la
     * coleccion refleja los parametros page/size realmente usados en la
     * peticion.
     */
    public CollectionModel<ProductDTO> toCollectionModel(Iterable<? extends Product> entities, Integer page,
            Integer size) {

        CollectionModel<ProductDTO> collectionModel = super.toCollectionModel(entities);

        Link collectionSelfLink = linkTo(methodOn(ProductController.class).dameProductos(page, size))
                .withSelfRel();

        collectionModel.add(collectionSelfLink);

        return collectionModel;
    }

    /**
     * Enlace a la coleccion completa de productos. Se expone como metodo
     * publico porque el controlador tambien lo necesita en el metodo de
     * borrado (deleteProducto), donde no hay ningun Product que convertir a
     * ProductDTO (ya se ha eliminado), pero si conviene indicarle al cliente a
     * donde dirigirse a continuacion.
     */
    public Link allProductsLink() {
        return linkTo(methodOn(ProductController.class).dameProductos(null, null)).withRel("all products");
    }
}
