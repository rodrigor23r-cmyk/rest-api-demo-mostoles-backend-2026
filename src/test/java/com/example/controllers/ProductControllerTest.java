package com.example.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.example.dto.ProductRequestDTO;
import com.example.entities.Presentation;
import com.example.entities.Product;
import com.example.services.ProductService;
import com.example.spring_security_jwt.payload.request.LoginRequest;
import com.example.utilities.FileDownloadUtil;
import com.example.utilities.FileUploadUtil;
import com.example.utilities.FileUtil;


import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

// @WebMvcTest(ProductController.class)
@SpringBootTest 
/**
 * La anotacion anterior es la recomendada para implementar test de Integracion,
 * a la capa de controladores que conlleva la realizacion de peticiones HTTP.
 * 
 * Esta anotacion no serviria si tuviesemos implementada la seguridad con Spring
 * Security porque no carga todo el contexto de Spring. Cuando se implemente la
 * seguridad, comentaremos esta anotacion y utilizaremos @SpringBootTest
 */

/*
 * La siguiente anotacion se utiliza cuando queremos utilizar una base de datos
 * real, que no sea H2 Database, MySQL por ejemplo, y que al terminar la prueba
 * se deje la base de datos tal y como estaba
 */
@AutoConfigureTestDatabase(replace = Replace.NONE)

/**
 * Se necesita MockMvc para realizar peticiones a los end points, lo cual
 * suministra y configura la anotacion siguiente
 */
@AutoConfigureMockMvc
class ProductControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	ProductService productService;

	@MockitoBean
	FileUploadUtil fileUploadUtil;

	@MockitoBean
	FileDownloadUtil fileDownloadUtil;
	
	@MockitoBean
	FileUtil fileUtil;
	
	@Autowired
	ObjectMapper objectMapper;
	
	List<Product> products = new ArrayList<>();
	Presentation presentation1, presentation2;
	Product product1, product2;
	

	// no hace falta inicializar el token, ya que se inicializa en el metodo setUp() antes de cada test. De momento su valor es null.
	String token;


	@BeforeEach
	void setUp() throws Exception {

		/**
		 * Necesitamos obtener un token válido para poder realizar las peticiones a los end points de ProductController, ya que
		 * estos end points están protegidos por Spring Security y requieren autenticación.
		 */
		LoginRequest loginRequest = LoginRequest.builder()
				.username("admin")
				.password("Temp2026$$")
				.build();
		/**
		 *  Para convertir el objeto loginRequest a formato JSON, es decir, una cadena (String) en formato de JSON, lo cual hace el objectMapper que hemos inyectado como dependencia al principio de la clase bajo Test
		 *  Esto es necesario para poder enviar el objeto loginRequest en el cuerpo de la petición HTTP, ya que el end point de autenticación espera recibir un objeto JSON con las credenciales del usuario (username y password) para poder generar un token JWT válido.
		 *  Una vez que tengamos el token JWT válido, podremos utilizarlo en las cabeceras de las peticiones HTTP
		 */
		String jsonStringLoginRequest = objectMapper.writeValueAsString(loginRequest);


		ResultActions resultActions = this.mockMvc.perform(post("/api/auth/signin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonStringLoginRequest));

		// Obtenemos el token JWT válido de la respuesta del end point de autenticación
		MvcResult mvcResult = resultActions.andDo(print()).andReturn();

		String contentAsString = mvcResult.getResponse().getContentAsString();

		// Convertimos la respuesta a un objeto JSON para poder obtener el token JWT válido
		
		JSONObject jsonObject = new JSONObject(contentAsString);

		this.token = "Bearer " + jsonObject.getString("token");

		// =================================================================


		presentation1 = Presentation.builder()
				.name("decenas")
				.description("Por decenas")
				.build();
		
		presentation2 = Presentation.builder()
				.name("unidades")
				.description("Por unidades")
				.build();
			
		product1 = Product.builder()
				.name("Camara")
				.description("HP Camara")
				.price(new BigDecimal(500))
				.stock(1900)
				.productImage(null)
				.presentation(presentation1)
				.build();
		
			
		product2 = Product.builder()
				.name("Frigorifico")
				.description("General Electric")
				.price(new BigDecimal(2500))
				.stock(3900)
				.productImage(null)
				.presentation(presentation2)
				.build();
		
		products.add(product1);
		products.add(product2);
	}

	@Test
	@DisplayName("Controller Test que recupera todos los productos")
	void testFindAll() throws Exception {

		// given
		
		given(productService.findAll(Sort.by("name")))
			.willReturn(products);

		// when => Realizar la peticion (request) HTTP, mediante el metodo GET
		// al end point de products ("/products"). Aqui se utiliza MockMvc

		ResultActions response = mockMvc
				.perform(get("/products")
				.accept(MediaType.APPLICATION_JSON)
				.header("Authorization", this.token));	
		// then

		// NOTA: desde que se implemento HATEOAS, la coleccion se serializa en
		// formato HAL por defecto, es decir, dentro de "_embedded.productList"
		// (el nombre "productList" lo asigna Spring HATEOAS por defecto al no
		// tener la entidad Product una anotacion @Relation), en vez de bajo la
		// clave plana "products" que se usaba antes.
		// NOTA: desde que el endpoint devuelve DTOs (ProductDTO) en vez de la
		// entidad Product, el nombre que Spring HATEOAS asigna a la coleccion
		// embebida pasa de "productList" a "productDTOList", porque lo deriva
		// del nombre de la clase que envuelve cada EntityModel.
		response.andExpect(status().isOk()).andDo(print())
				.andExpect(jsonPath("$._embedded.productDTOList.size()",
						is(products.size())));

	}

	@Test
	@DisplayName("Controller Test para Persistir un Producto")
	void testSaveProduct()  {
		
		// given
		given(productService.save(any(Product.class)))
			.willAnswer(invocation -> invocation.getArgument(0));
		
		// when
		
		/* Convertir el producto a formato JSON, es decir, una cadena (String)
		 * en formato de JSON, lo cual hace el objectMapper que hemos inyectado como 
		 * dependencia al principio de la clase bajo Test */
		
		// Desde que saveProduct recibe ProductRequestDTO en vez de la entidad
		// Product, hay que enviar ese DTO (con presentationId en vez del objeto
		// Presentation completo) como JSON de la peticion
		ProductRequestDTO productRequestDTO = new ProductRequestDTO(
				product1.getName(),
				product1.getDescription(),
				product1.getStock(),
				product1.getPrice(),
				presentation1.getId());

		String jsonStringProduct = objectMapper.writeValueAsString(productRequestDTO);

		MockMultipartFile bytesArrayProduct = new MockMultipartFile(
				    "product",
				    null,
				    "application/json",
				    jsonStringProduct.getBytes());

		try {
				mockMvc
				    .perform(multipart("/products")
					.file(bytesArrayProduct)
					.file("file", null)
					.header("Authorization", this.token))			    
				    	.andDo(print())
				    	.andExpect(status().isCreated())
				    	.andExpect(jsonPath("$.product.name",
		  			is(product1.getName())));
		  	
		  
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// then
		
	}

	@Test
	@DisplayName("Controller Test para recuperar un producto por su ID")
	void testRecuperarProductoPorSuID() throws Exception {
		
		// given
		
		int productId = 1;
		
		given(productService.findById(productId))
			.willReturn(product1);
		
		// when
		mockMvc.perform(get("/products/{id}",
				productId)
				.header("Authorization", this.token))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$['producto encontrado: '].name",
						is(product1.getName())));
	}

	@Test
	@DisplayName("Controller Test Producto no encontrado")
	void testProductoNoEncontrado() throws Exception {
		
		// given
		given(productService.findById(20)).willReturn(null);
		
		// when
		
		mockMvc.perform(get("/products/{id}", 20)
			.header("Authorization", this.token))
			.andDo(print())
			.andExpect(status().isNotFound());
	}
	
    @Test 
    @DisplayName("Controller Test que actualiza un producto con su imagen")
    void testUpdateProduct() throws Exception{

        //given
        int id = 1;
        given(productService.findById(id)).willReturn(product1);
        given(productService.save(any(Product.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        //when

        // Desde que updateProduct recibe ProductRequestDTO en vez de la entidad
        // Product, hay que enviar ese DTO (con presentationId en vez del objeto
        // Presentation completo) como JSON de la peticion
        ProductRequestDTO productRequestDTO = new ProductRequestDTO(
                product1.getName(),
                product1.getDescription(),
                product1.getStock(),
                product1.getPrice(),
                presentation1.getId());

        String jsonStringProduct = objectMapper.writeValueAsString(productRequestDTO);

        MockMultipartFile bytesArrayProduct = new MockMultipartFile("product",
                            null,
                            "application/json",
                            jsonStringProduct.getBytes());

        ResultActions response = this.mockMvc
        		.perform(multipart("/products/{id}", id)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .file("image", null)
                        .file(bytesArrayProduct)
						.header("Authorization", this.token));

        //then
        response.andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$['producto actualizado: '].name",
            		is(product1.getName())))
            .andExpect(jsonPath("$['producto actualizado: '].description",
            		is(product1.getDescription())));
        

    }
    
    @Test 
    @DisplayName("Controller Test que elimina un producto")
    void testDeleteProduct() throws Exception{

        //given
        int ProductId = 1;

        given(productService.findById(ProductId)).willReturn(product1);
        doNothing().when(productService).delete(product1);

        //when
        mockMvc.perform(delete("/products/{id}", ProductId)
				.header("Authorization", this.token))
                .andExpect(status().isOk());

    }
	
}











