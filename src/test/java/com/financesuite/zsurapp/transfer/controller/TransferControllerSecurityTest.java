
package com.financesuite.zsurapp.transfer.controller;

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
import com.financesuite.zsurapp.transfer.entity.Transfer;
import com.financesuite.zsurapp.transfer.service.TransferService;

@WebMvcTest(TransferController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = { "SUPABASE_URL=https://auth.test.invalid" })
class TransferControllerSecurityTest {

	private static final UUID USER_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");

	private static final UUID WORKSPACE_ID = UUID.fromString("22222222-2222-4222-8222-222222222222");

	private static final UUID FROM_ACCOUNT_ID = UUID.fromString("33333333-3333-4333-8333-333333333333");

	private static final UUID TO_ACCOUNT_ID = UUID.fromString("44444444-4444-4444-8444-444444444444");

	private static final Instant OCCURRED_AT = Instant.parse("2026-11-23T16:00:00Z");

	private static final String URL = "/api/v1/workspaces/" + WORKSPACE_ID + "/transfers";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TransferService service;

	@MockitoBean
	private JwtDecoder jwtDecoder;

	private String validBody() {
		return """
				{
				  "fromAccountId": "%s",
				  "toAccountId": "%s",
				  "amount": 20000.0000,
				  "description": "Transferencia al banco",
				  "occurredAt": "2026-11-23T16:00:00Z"
				}
				""".formatted(FROM_ACCOUNT_ID, TO_ACCOUNT_ID);
	}

	private Transfer sampleTransfer() {
		return new Transfer(WORKSPACE_ID, FROM_ACCOUNT_ID, TO_ACCOUNT_ID, new BigDecimal("20000.0000"),
				"Transferencia al banco", OCCURRED_AT);
	}

	@Test
	void shouldRejectUnauthenticatedRequest() throws Exception {

		mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());

		verifyNoInteractions(service);
	}

	@Test
	void shouldListTransfersForAuthenticatedUser() throws Exception {

		when(service.findAllByWorkspace(WORKSPACE_ID, USER_ID)).thenReturn(List.of());

		mockMvc.perform(get(URL).with(jwt().jwt(token -> token.subject(USER_ID.toString())))).andExpect(status().isOk())
				.andExpect(content().json("[]"));

		verify(service).findAllByWorkspace(WORKSPACE_ID, USER_ID);
	}

	@Test
	void shouldFindTransferById() throws Exception {

		Transfer transfer = sampleTransfer();

		when(service.findById(WORKSPACE_ID, transfer.getId(), USER_ID)).thenReturn(transfer);

		mockMvc.perform(get(URL + "/" + transfer.getId()).with(jwt().jwt(token -> token.subject(USER_ID.toString()))))
				.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(transfer.getId().toString()))
				.andExpect(jsonPath("$.fromAccountId").value(FROM_ACCOUNT_ID.toString()))
				.andExpect(jsonPath("$.toAccountId").value(TO_ACCOUNT_ID.toString()));
	}

	@Test
	void shouldCreateTransfer() throws Exception {

		Transfer transfer = sampleTransfer();

		when(service.create(eq(WORKSPACE_ID), eq(FROM_ACCOUNT_ID), eq(TO_ACCOUNT_ID), any(BigDecimal.class),
				eq("Transferencia al banco"), eq(OCCURRED_AT), eq(USER_ID))).thenReturn(transfer);

		mockMvc.perform(post(URL).with(jwt().jwt(token -> token.subject(USER_ID.toString())))
				.contentType(MediaType.APPLICATION_JSON).content(validBody())).andExpect(status().isCreated())
				.andExpect(header().string("Location", URL + "/" + transfer.getId()))
				.andExpect(jsonPath("$.id").value(transfer.getId().toString()))
				.andExpect(jsonPath("$.amount").value(20000.0));

		verify(service).create(eq(WORKSPACE_ID), eq(FROM_ACCOUNT_ID), eq(TO_ACCOUNT_ID), any(BigDecimal.class),
				eq("Transferencia al banco"), eq(OCCURRED_AT), eq(USER_ID));
	}

	@Test
	void shouldRejectNegativeAmount() throws Exception {

		String body = validBody().replace("20000.0000", "-100.0000");

		mockMvc.perform(post(URL).with(jwt().jwt(token -> token.subject(USER_ID.toString())))
				.contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest());

		verifyNoInteractions(service);
	}

	@Test
	void shouldRejectMissingDestination() throws Exception {

		String body = validBody().replace("\"" + TO_ACCOUNT_ID + "\"", "null");

		mockMvc.perform(post(URL).with(jwt().jwt(token -> token.subject(USER_ID.toString())))
				.contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest());

		verifyNoInteractions(service);
	}

	@Test
	void shouldRejectSameAccount() throws Exception {

		String body = validBody().replace(TO_ACCOUNT_ID.toString(), FROM_ACCOUNT_ID.toString());

		when(service.create(eq(WORKSPACE_ID), eq(FROM_ACCOUNT_ID), eq(FROM_ACCOUNT_ID), any(BigDecimal.class),
				anyString(), eq(OCCURRED_AT), eq(USER_ID)))
				.thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las cuentas deben ser diferentes"));

		mockMvc.perform(post(URL).with(jwt().jwt(token -> token.subject(USER_ID.toString())))
				.contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest());
	}

	@Test
	void shouldRejectForeignWorkspace() throws Exception {

		when(service.findAllByWorkspace(WORKSPACE_ID, USER_ID))
				.thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

		mockMvc.perform(get(URL).with(jwt().jwt(token -> token.subject(USER_ID.toString()))))
				.andExpect(status().isNotFound());
	}
}
