package com.promineotech.jeep.controller;

import static org.assertj.core.api.Assertions.assertThat;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import com.promineotech.jeep.entity.JeepModel;
import com.promineotech.jeep.entity.Order;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(scripts = {"classpath:flyway/migrations/V1.0__Jeep_Schema.sql",
		"classpath:flyway/migrations/V1.1__Jeep_Data.sql"},
		config = @SqlConfig(encoding = "utf-8"))
class CreateOrderTest {
	
	@LocalServerPort
	private int serverPort;
	
	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void testCreateOrderReturnsSuccess201() {
		String body = createOrderBody();
		String uri = String.format("http://localhost:%d/orders", serverPort);
		
		// use import org.springframework.http.HttpHeaders;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		HttpEntity<String> bodyEntity = new HttpEntity<String>(body, headers);
		
		// When:
		//use import com.promineotech.jeep.entity.Order;
		ResponseEntity<Order> response = 
				restTemplate.exchange(uri, HttpMethod.POST, bodyEntity, Order.class);
		// Then:
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotNull();
		
		Order order = response.getBody();
		assertThat(order.getCustomer().getCustomerId()).isEqualTo("MORISON_LINA");
		assertThat(order.getModel().getModel()).isEqualTo(JeepModel.WRANGLER);
		assertThat(order.getModel().getTrimLevel()).isEqualTo("Sport Altitude");
		assertThat(order.getModel().getNumDoors()).isEqualTo(4);
		assertThat(order.getColor().getColorId()).isEqualTo("EXT_NACHO");
		assertThat(order.getEngine().getEngineId()).isEqualTo("2_0_TURBO");
		assertThat(order.getTire().getTireId()).isEqualTo("35_TOYO");
		assertThat(order.getOptions()).hasSize(6);
	}
	
	private String createOrderBody() {
		// @formatter:off
		return "{"
			+ "  \"customer\":\"MORISON_LINA\","
			+ "  \"model\":\"WRANGLER\","
			+ "  \"trim\":\"Sport Altitude\","
			+ "  \"doors\":4,"
			+ "  \"color\":\"EXT_NACHO\","
			+ "  \"engine\":\"2_0_TURBO\","
			+ "  \"tire\":\"35_TOYO\","
			+ "  \"options\":["
			+ "    \"DOOR_QUAD_4\","
			+ "    \"EXT_AEV_LIFT\","
			+ "    \"EXT_WARN_WINCH\","
			+ "    \"EXT_WARN_BUMPER_FRONT\","
			+ "    \"EXT_WARN_BUMPER_REAR\","
			+ "    \"EXT_ARB_COMPRESSOR\""
			+ " ]"
			+ "}";
		// @formatter:on
	}

}
