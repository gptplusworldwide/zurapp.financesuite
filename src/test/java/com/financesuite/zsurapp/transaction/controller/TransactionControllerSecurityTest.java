
package com.financesuite.zsurapp.transaction.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.financesuite.zsurapp.security.SecurityConfig;
import com.financesuite.zsurapp.transaction.entity.Transaction;
import com.financesuite.zsurapp.transaction.entity.TransactionType;
import com.financesuite.zsurapp.transaction.service.TransactionService;

@WebMvcTest(TransactionController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = { "SUPABASE_URL=https://auth.test.invalid" })
class TransactionControllerSecurityTest {

	private static final UUID USER_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");

	private static final UUID WORKSPACE_ID = UUID.fromString("22222222-2222-4222-8222-222222222222");

	private static final UUID ACCOUNT_ID = UUID.fromString("33333333-3333-4333-8333-333333333333");

	private static final UUID CATEGORY_ID = UUID.fromString("44444444-4444-4444-8444-444444444444");

	private static final Instant OCCURRED_AT = Instant.parse("2026-11-23T12:00:00Z");

	private static final String URL = "/api/v1/workspaces/" + WORKSPACE_ID + "/transactions";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TransactionService service;

	@MockitoBean
	private JwtDecoder jwtDecoder;

	private String validBody() {
		return """
				{
				  "accountId": "%s",
				  "categoryId": "%s",
				  "type": "EXPENSE",
				  "amount": 15000.0000,
				  "description": "Compra del supermercado",
				  "occurredAt": "2026-11-23T12:00:00Z"
				}
				""".formatted(ACCOUNT_ID, CATEGORY_ID);
	}

	@Test
	void shouldRejectUnauthenticatedRequest() throws Exception {

		mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());

		verifyNoInteractions(service);
	}

	@Test
	void shouldListTransactionsForAuthenticatedUser() throws Exception {

		when(service.findAllByWorkspace(WORKSPACE_ID, USER_ID)).thenReturn(List.of());

		mockMvc.perform(get(URL).with(jwt().jwt(token -> token.subject(USER_ID.toString())))).andExpect(status().isOk())
				.andExpect(content().json("[]"));

		verify(service).findAllByWorkspace(WORKSPACE_ID, USER_ID);
	}

	@Test
	void shouldRejectNegativeAmount() throws Exception {

		String body = validBody().replace("15000.0000", "-100.0000");

		mockMvc.perform(post(URL).with(jwt().jwt(token -> token.subject(USER_ID.toString())))
				.contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest());

		verifyNoInteractions(service);
	}

	@Test
	void shouldRejectMissingAccount() throws Exception {

		String body = validBody().replace("\"" + ACCOUNT_ID + "\"", "null");

		mockMvc.perform(post(URL).with(jwt().jwt(token -> token.subject(USER_ID.toString())))
				.contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest());

		verifyNoInteractions(service);
	}

	@Test
	void shouldCreateTransaction() throws Exception {

		Transaction transaction = new Transaction(WORKSPACE_ID, ACCOUNT_ID, CATEGORY_ID, TransactionType.EXPENSE,
				new BigDecimal("15000.0000"), "Compra del supermercado", OCCURRED_AT);

		when(service.create(eq(WORKSPACE_ID), eq(ACCOUNT_ID), eq(CATEGORY_ID), eq(TransactionType.EXPENSE),
				any(BigDecimal.class), eq("Compra del supermercado"), eq(OCCURRED_AT), eq(USER_ID)))
				.thenReturn(transaction);

		mockMvc.perform(post(URL).with(jwt().jwt(token -> token.subject(USER_ID.toString())))
				.contentType(MediaType.APPLICATION_JSON).content(validBody())).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(transaction.getId().toString()))
				.andExpect(jsonPath("$.type").value("EXPENSE")).andExpect(jsonPath("$.amount").value(15000.0));

		verify(service).create(eq(WORKSPACE_ID), eq(ACCOUNT_ID), eq(CATEGORY_ID), eq(TransactionType.EXPENSE),
				any(BigDecimal.class), eq("Compra del supermercado"), eq(OCCURRED_AT), eq(USER_ID));
	}

	@Test
	void shouldRejectCategoryTypeMismatch() throws Exception {

		when(service.create(eq(WORKSPACE_ID), eq(ACCOUNT_ID), eq(CATEGORY_ID), eq(TransactionType.EXPENSE),
				any(BigDecimal.class), anyString(), eq(OCCURRED_AT), eq(USER_ID)))
				.thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo no coincide con la categoria"));

		mockMvc.perform(post(URL).with(jwt().jwt(token -> token.subject(USER_ID.toString())))
				.contentType(MediaType.APPLICATION_JSON).content(validBody())).andExpect(status().isBadRequest());
	}

	@Test
	void shouldRejectForeignWorkspace() throws Exception {

		when(service.findAllByWorkspace(WORKSPACE_ID, USER_ID))
				.thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

		mockMvc.perform(get(URL).with(jwt().jwt(token -> token.subject(USER_ID.toString()))))
				.andExpect(status().isNotFound());
	}
}
