package com.example.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.example.dto.PresentationDTO;
import com.example.dto.ProductDTO;
import com.example.dto.ProductRequestDTO;
import com.example.entities.Presentation;
import com.example.entities.Product;

/**
 * Mapper de MapStruct encargado de traducir entre la capa de persistencia
 * (entidades JPA, Product y Presentation) y la capa de presentacion
 * (DTOs en forma de record, ProductDTO y PresentationDTO).
 *
 * componentModel = "spring" le indica a MapStruct que genere la
 * implementacion (ProductMapperImpl) anotada con @Component, para que
 * Spring pueda inyectarla como cualquier otro bean (por ejemplo, en
 * ProductController).
 *
 * No hace falta anotar los campos uno a uno con @Mapping porque Product y
 * ProductDTO (y Presentation y PresentationDTO) tienen exactamente los mismos
 * nombres de campo; MapStruct los empareja automaticamente por nombre.
 *
 * MapStruct detecta que, al mapear Product -> ProductDTO, el campo
 * "presentation" necesita a su vez convertir un Presentation en un
 * PresentationDTO, y usa automaticamente el metodo toDto(Presentation)
 * de este mismo mapper para ello (no hace falta declararlo de forma
 * explicita con "uses = ...").
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductDTO toDto(Product product);

    PresentationDTO toDto(Presentation presentation);

    /**
     * Convierte el DTO de entrada (ProductRequestDTO) en la entidad Product que
     * hay que persistir.
     *
     * "id" se ignora porque nunca viene en el cuerpo de la peticion: en el alta
     * (POST) lo genera la base de datos, y en la actualizacion (PUT) lo fija el
     * controlador a partir del @PathVariable, despues de llamar a este metodo.
     *
     * "productImage" se ignora porque esa informacion no llega en este DTO, sino
     * en el @RequestPart del fichero, y el controlador la asigna aparte con
     * product.setProductImage(...).
     *
     * "presentation" no tiene en el DTO un campo con ese mismo nombre, sino
     * "presentationId" (un simple int), asi que hay que indicarle a MapStruct,
     * con @Mapping, de donde sale el valor y que metodo usar para convertirlo.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productImage", ignore = true)
    @Mapping(target = "presentation", source = "presentationId", qualifiedByName = "presentationIdToPresentation")
    Product toEntity(ProductRequestDTO productRequestDTO);

    /**
     * Construye una Presentation "de referencia", es decir, con unicamente el id
     * rellenado. Es suficiente para que Hibernate, al guardar el Product, fije la
     * clave foranea (presentation_id) sin necesidad de cargar la Presentation
     * completa desde la base de datos.
     */
    @Named("presentationIdToPresentation")
    default Presentation presentationIdToPresentation(Integer presentationId) {

        if (presentationId == null) {
            return null;
        }

        return Presentation.builder().id(presentationId).build();
    }
}
