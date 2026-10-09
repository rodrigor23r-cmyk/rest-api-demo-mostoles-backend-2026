package com.example.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO (Data Transfer Object) de ENTRADA para crear o actualizar un Product.
 *
 * Es el "espejo" de ProductDTO pero para las peticiones (POST/PUT), y
 * tambien pertenece exclusivamente a la capa de presentacion, nunca a la de
 * persistencia.
 *
 * Diferencias deliberadas respecto a la entidad Product:
 *
 * - No lleva "id": en el alta (POST) lo genera la base de datos, y en la
 *   actualizacion (PUT) ya viene en la ruta (@PathVariable), no en el cuerpo.
 * - No lleva "productImage": esa parte de la peticion llega por separado,
 *   en otro @RequestPart (el fichero), no en el JSON del producto.
 * - En vez de recibir el objeto Presentation completo, solo se pide su id
 *   (presentationId). El cliente de la API referencia asi una presentacion
 *   que ya existe, sin tener que enviar (ni poder enviar) su nombre o su
 *   descripcion.
 *
 * Las anotaciones de validacion son las mismas que tenia la entidad Product,
 * trasladadas aqui porque ahora es este DTO, y no la entidad, el que recibe
 * directamente el JSON de la peticion.
 */
public record ProductRequestDTO(

        @NotNull(message = "El producto tiene que tener un nombre")
        @NotEmpty(message = "El nombre del producto no puede estar vacio")
        @Size(min = 4, max = 25, message = "El nombre del producto no puede tener menos de 4 caracteres ni mas de 25")
        String name,

        @NotNull(message = "La description del producto es requerida")
        @NotBlank(message = "La description del producto no puede tener espacios vacios solamente")
        @Size(max = 45, message = "La description no puede superar los 45 caracteres")
        String description,

        @Min(value = 0, message = "El stock del producto no puede ser negativo")
        int stock,

        @Min(value = 0, message = "El precio no puede estar en valores negativos")
        BigDecimal price,

        @NotNull(message = "La presentación del producto es requerida")
        Integer presentationId) {
}
