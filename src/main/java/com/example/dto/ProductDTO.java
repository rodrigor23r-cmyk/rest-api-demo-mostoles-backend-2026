package com.example.dto;

import java.math.BigDecimal;

import org.springframework.hateoas.RepresentationModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO (Data Transfer Object) para la entidad Product.
 *
 * Esta es la representacion que ve la capa de presentacion (ProductController)
 * hacia el cliente de la API, separada de la entidad JPA (com.example.entities.Product),
 * que pertenece exclusivamente a la capa de persistencia.
 *
 * A diferencia de la version anterior (un record), esta clase hereda de
 * RepresentationModel<ProductDTO>, que es la clase base de Spring HATEOAS que
 * ya trae de serie la coleccion de enlaces (_links) y los metodos add(Link...),
 * getLinks(), etc. Gracias a esto, el propio ProductDTO ES el recurso
 * hipermedial: ya no hace falta envolverlo en un EntityModel<ProductDTO> aparte.
 *
 * No puede ser un record porque un record en Java solo puede implementar
 * interfaces, nunca heredar de una clase (y RepresentationModel es una clase).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO extends RepresentationModel<ProductDTO> {

    private int id;
    private String name;
    private String description;
    private int stock;
    private BigDecimal price;
    private PresentationDTO presentation;
    private String productImage;
}
