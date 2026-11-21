
package com.financesuite.zsurapp.workspace.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import com.financesuite.zsurapp.security.SecurityConfig;
import com.financesuite.zsurapp.workspace.service.WorkspaceService;

@WebMvcTest(WorkspaceController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
    "SUPABASE_URL=https://auth.test.invalid"
})
class WorkspaceControllerSecurityTest {

    private static final String URL =
        "/api/v1/workspaces";

    private static final UUID USER_ID =
        UUID.fromString(
            "11111111-1111-4111-8111-111111111111"
        );

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkspaceService service;

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
    void shouldListWorkspacesForAuthenticatedUser()
            throws Exception {

        when(service.findAllByOwner(USER_ID))
            .thenReturn(java.util.List.of());

        mockMvc.perform(
                get(URL)
                    .with(jwt().jwt(token ->
                        token.subject(USER_ID.toString())
                    ))
            )
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));

        verify(service).findAllByOwner(USER_ID);
    }

    @Test
    void shouldRejectBlankWorkspaceName()
            throws Exception {

        String body = """
            {
              "name": "",
              "type": "PERSONAL"
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
    void shouldNotExposeAnotherUsersWorkspace()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();

        when(service.findByIdAndOwner(
                workspaceId, USER_ID))
            .thenThrow(new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Espacio financiero no encontrado"
            ));

        mockMvc.perform(
                get(URL + "/" + workspaceId)
                    .with(jwt().jwt(token ->
                        token.subject(USER_ID.toString())
                    ))
            )
            .andExpect(status().isNotFound());

        verify(service).findByIdAndOwner(
            workspaceId, USER_ID
        );
    }
}
