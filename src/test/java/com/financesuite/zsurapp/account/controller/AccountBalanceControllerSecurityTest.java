
package com.financesuite.zsurapp.account.controller;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.financesuite.zsurapp.account.dto.AccountBalanceResponse;
import com.financesuite.zsurapp.account.service.AccountBalanceService;
import com.financesuite.zsurapp.security.SecurityConfig;

@WebMvcTest(AccountBalanceController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = { "SUPABASE_URL=https://auth.test.invalid" })
class AccountBalanceControllerSecurityTest {

	private static final UUID USER_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");

	private static final UUID WORKSPACE_ID = UUID.fromString("22222222-2222-4222-8222-222222222222");

	private static final UUID ACCOUNT_ID = UUID.fromString("33333333-3333-4333-8333-333333333333");

	private static final String URL = "/api/v1/workspaces/" + WORKSPACE_ID + "/accounts/" + ACCOUNT_ID + "/balance";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AccountBalanceService service;

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@Test
	void shouldRejectUnauthenticatedRequest() throws Exception {

		mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());

		verifyNoInteractions(service);
	}

	@Test
	void shouldReturnBalanceForAuthenticatedUser() throws Exception {

		AccountBalanceResponse response = new AccountBalanceResponse(ACCOUNT_ID, WORKSPACE_ID, "Efectivo personal",
				"ARS", new BigDecimal("150000.0000"), BigDecimal.ZERO, new BigDecimal("15000.0000"),
				new BigDecimal("135000.0000"));

		when(service.getBalance(WORKSPACE_ID, ACCOUNT_ID, USER_ID)).thenReturn(response);

		mockMvc.perform(get(URL).with(jwt().jwt(token -> token.subject(USER_ID.toString())))).andExpect(status().isOk())
				.andExpect(jsonPath("$.accountId").value(ACCOUNT_ID.toString()))
				.andExpect(jsonPath("$.currency").value("ARS")).andExpect(jsonPath("$.currentBalance").value(135000.0));

		verify(service).getBalance(WORKSPACE_ID, ACCOUNT_ID, USER_ID);
	}

	@Test
	void shouldRejectForeignWorkspace() throws Exception {

		when(service.getBalance(WORKSPACE_ID, ACCOUNT_ID, USER_ID))
				.thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

		mockMvc.perform(get(URL).with(jwt().jwt(token -> token.subject(USER_ID.toString()))))
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldReturn404ForMissingAccount() throws Exception {

		when(service.getBalance(WORKSPACE_ID, ACCOUNT_ID, USER_ID))
				.thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada"));

		mockMvc.perform(get(URL).with(jwt().jwt(token -> token.subject(USER_ID.toString()))))
				.andExpect(status().isNotFound());
	}
}
