package com.promineotech.jeep.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import com.promineotech.jeep.Constants;
import com.promineotech.jeep.entity.Jeep;
import com.promineotech.jeep.entity.JeepModel;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(scripts = {"classpath:flyway/migrations/V1.0__Jeep_Schema.sql",
		"classpath:flyway/migrations/V1.1__Jeep_Data.sql"},
		config = @SqlConfig(encoding = "utf-8"))
class FetchJeepTest {
	
	@Autowired
	private TestRestTemplate restTemplate;
	
	@LocalServerPort 
	private int serverPort;

	@Test
	void testThatJeepsAreReturnedWhenAValidModelAndTrimAreSupplied() {
		// Given: a model and trim and REST URI
		JeepModel model = JeepModel.WRANGLER;
		String trim = "Sport";
		String uri = String.format(
				"http://localhost:%d/jeeps?model=%s&trim=%s", serverPort, model, trim);
		
		// When: an HTTP (REST) request is sent to the service
		ResponseEntity<List<Jeep>> response = 
				restTemplate.exchange(
						uri, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
		
		// Then: the response status is 200 (OK)
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		
		// And: the returned Jeep objects are what I expected
		assertThat(response.getBody()).isEqualTo(buildExpected());
		
	}
	
	@Test
	void testThatAnErrorMessageIsReturnedWhenAnUnknownTrimIsSupplied() {
		// Given: a model and trim and REST URI
		JeepModel model = JeepModel.WRANGLER;
		String trim = "Unknown trim name";
		String uri = String.format(
				"http://localhost:%d/jeeps?model=%s&trim=%s", serverPort, model, trim);
		
		// When: an HTTP (REST) request is sent to the service
		ResponseEntity<Map<String, Object>> response = 
				restTemplate.exchange(
						uri, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
		
		// Then: a not found (404) status code is returned
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		
		// And: an error message is returned
		Map<String, Object> error = response.getBody();
		
		assertErrorMessageValid(error, HttpStatus.NOT_FOUND);
		
		
	}
	
	@ParameterizedTest
	@MethodSource("com.promineotech.jeep.controller.FetchJeepTest#parametersForInvalidInput")
	void testThatAnErrorMessageIsReturnedWhenAnInvalidValueIsSupplied(
			String model, String trim, String reason) {
		
		// Given: a model and trim and REST URI
		String uri = 
				String.format("http://localhost:%d/jeeps?model=%s&trim=%s", 
						serverPort, model, trim);
		
		// When: a connection is made to the URI
		ResponseEntity<Map<String, Object>> response = 
				restTemplate.exchange(
						uri, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
		
		// Then: a bad request (400) status code is returned
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		
		// And: an error message is returned
		Map<String, Object> error = response.getBody();
		
		assertErrorMessageValid(error, HttpStatus.BAD_REQUEST);
		
		
	}
	

	static Stream<Arguments> parametersForInvalidInput() {
		// @formatter: off
		return Stream.of(
				arguments("WRANGLER", "$%#$#@", "Trim contains non-alphanumeric characters"),
				arguments("WRANGLER", "C".repeat(Constants.TRIM_MAX_LENGTH + 1), "Trim length too long"),
				arguments("INVALID", "Sport", "Model is not enum value")
		// @formatter:on
		);
	}
	
	protected void assertErrorMessageValid(Map<String, Object> error,
			HttpStatus status) {
		// @formatter:off
		assertThat(error)
			.containsKey("message")
			.containsEntry("status code", status.value())
			.containsEntry("uri", "/jeeps")
			.containsKey("timestamp")
			.containsEntry("reason", status.getReasonPhrase());
		// @formatter:on
	}

	private List<Jeep> buildExpected() {
		List<Jeep> jeeps = new LinkedList<Jeep>();
		
		// @formatter:off
		jeeps.add(Jeep.builder()
				.model(JeepModel.WRANGLER)
				.trimLevel("Sport")
				.numDoors(2)
				.wheelSize(17)
				.basePrice(new BigDecimal("28475.00"))
				.build());
		
		jeeps.add(Jeep.builder()
				.model(JeepModel.WRANGLER)
				.trimLevel("Sport")
				.numDoors(4)
				.wheelSize(17)
				.basePrice(new BigDecimal("31975.00"))
				.build());
		// @formatter:on
		
		return jeeps;
		
	}

}
