
package com.financesuite.zsurapp.category.controller;

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

import com.financesuite.zsurapp.category.entity.CategoryType;
import com.financesuite.zsurapp.category.service.CategoryService;
import com.financesuite.zsurapp.security.SecurityConfig;

@WebMvcTest(CategoryController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
    "SUPABASE_URL=https://auth.test.invalid"
})
class CategoryControllerSecurityTest {

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
        + "/categories";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService service;

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
    void shouldListCategoriesForAuthenticatedUser()
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
    void shouldRejectEmptyCategoryName()
            throws Exception {

        String body = """
            {
              "name": "",
              "type": "EXPENSE"
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
    void shouldRejectMissingCategoryType()
            throws Exception {

        String body = """
            {
              "name": "Alimentación"
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
    void shouldRejectListingForeignWorkspace()
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

    @Test
    void shouldReturn409ForDuplicateCategory()
            throws Exception {

        String body = """
            {
              "name": "Alimentación",
              "type": "EXPENSE"
            }
            """;

        when(service.create(
                WORKSPACE_ID,
                "Alimentación",
                CategoryType.EXPENSE,
                USER_ID
            ))
            .thenThrow(new ResponseStatusException(
                HttpStatus.CONFLICT,
                "La categoria ya existe"
            ));

        mockMvc.perform(
                post(URL)
                    .with(jwt().jwt(token ->
                        token.subject(USER_ID.toString())
                    ))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isConflict());

        verify(service).create(
            WORKSPACE_ID,
            "Alimentación",
            CategoryType.EXPENSE,
            USER_ID
        );
    }

    @Test
    void shouldReturn404ForMissingCategory()
            throws Exception {

        UUID categoryId = UUID.randomUUID();

        when(service.findById(
                WORKSPACE_ID,
                categoryId,
                USER_ID
            ))
            .thenThrow(new ResponseStatusException(
                HttpStatus.NOT_FOUND
            ));

        mockMvc.perform(
                get(URL + "/" + categoryId)
                    .with(jwt().jwt(token ->
                        token.subject(USER_ID.toString())
                    ))
            )
            .andExpect(status().isNotFound());
    }
}
