
package com.financesuite.zsurapp.account.controller;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

import com.financesuite.zsurapp.account.service.AccountService;
import com.financesuite.zsurapp.security.SecurityConfig;

@WebMvcTest(AccountController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
    "SUPABASE_URL=https://auth.test.invalid"
})
class AccountControllerSecurityTest {

    private static final UUID USER_ID =
        UUID.fromString(
            "11111111-1111-4111-8111-111111111111"
        );

    private static final UUID WORKSPACE_ID =
        UUID.fromString(
            "22222222-2222-4222-8222-222222222222"
        );

    private static final String URL =
        "/api/v1/workspaces/" + WORKSPACE_ID
        + "/accounts";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldRejectUnauthenticatedRequest()
            throws Exception {

        mockMvc.perform(get(URL))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    @Test
    void shouldListAccountsForAuthenticatedUser()
            throws Exception {

        when(service.findAllByWorkspace(
                WORKSPACE_ID, USER_ID))
            .thenReturn(List.of());

        mockMvc.perform(
                get(URL)
                    .with(jwt().jwt(token ->
                        token.subject(USER_ID.toString())
                    ))
            )
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));

        verify(service).findAllByWorkspace(
            WORKSPACE_ID, USER_ID
        );
    }

    @Test
    void shouldRejectInvalidAccountName()
            throws Exception {

        String body = """
            {
              "name": "",
              "type": "CASH",
              "currency": "ARS",
              "initialBalance": 1000.00
            }
            """;

        mockMvc.perform(
                post(URL)
                    .with(jwt().jwt(token ->
                        token.subject(USER_ID.toString())
                    ))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void shouldRejectInvalidCurrency()
            throws Exception {

        String body = """
            {
              "name": "Efectivo",
              "type": "CASH",
              "currency": "PESOS",
              "initialBalance": 1000.00
            }
            """;

        mockMvc.perform(
                post(URL)
                    .with(jwt().jwt(token ->
                        token.subject(USER_ID.toString())
                    ))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void shouldRejectCreationInForeignWorkspace()
            throws Exception {

        String body = """
            {
              "name": "Cuenta privada",
              "type": "BANK",
              "currency": "ARS",
              "initialBalance": 0
            }
            """;

        when(service.create(
                eq(WORKSPACE_ID),
                eq("Cuenta privada"),
                eq(com.financesuite.zsurapp.account.entity.AccountType.BANK),
                eq("ARS"),
                eq(java.math.BigDecimal.ZERO),
                eq(USER_ID)
            ))
            .thenThrow(new ResponseStatusException(
                HttpStatus.NOT_FOUND
            ));

        mockMvc.perform(
                post(URL)
                    .with(jwt().jwt(token ->
                        token.subject(USER_ID.toString())
                    ))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectForeignWorkspaceListing()
            throws Exception {

        when(service.findAllByWorkspace(
                WORKSPACE_ID, USER_ID))
            .thenThrow(new ResponseStatusException(
                HttpStatus.NOT_FOUND
            ));

        mockMvc.perform(
                get(URL)
                    .with(jwt().jwt(token ->
                        token.subject(USER_ID.toString())
                    ))
            )
            .andExpect(status().isNotFound());
    }
}
