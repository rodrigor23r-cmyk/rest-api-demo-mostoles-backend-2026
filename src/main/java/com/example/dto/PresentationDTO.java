package com.example.dto;

/**
 * DTO (Data Transfer Object) para la entidad Presentation.
 *
 * Al tratarse de un record, es un objeto inmutable, de solo lectura, pensado
 * unicamente para viajar en las respuestas de la capa de presentacion
 * (los controladores), sin exponer la entidad JPA (com.example.entities.Presentation)
 * ni sus anotaciones de persistencia (@Entity, @OneToMany, etc.) al cliente de la API.
 */
public record PresentationDTO(
        int id,
        String name,
        String description) {
}
