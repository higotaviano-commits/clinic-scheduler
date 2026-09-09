package br.com.fiap.techchallenge.agendamento.controller;

import br.com.fiap.techchallenge.agendamento.dto.request.LoginRequest;
import br.com.fiap.techchallenge.agendamento.dto.response.AuthResponse;
import br.com.fiap.techchallenge.agendamento.exception.AutorizacaoInvalidaException;
import br.com.fiap.techchallenge.agendamento.service.JwtService;
import br.com.fiap.techchallenge.agendamento.usecase.AuthUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import br.com.fiap.techchallenge.agendamento.config.SecurityConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes para AuthController
 *
 * Testa:
 * - POST /api/v1/auth/login - autenticação bem-sucedida
 * - Credenciais inválidas
 * - Request inválido (missing fields)
 */
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthUseCase authUseCase;

    private static final String BASE_URL = "/api/v1/auth";

    // ========================================================================
    // POST - Login
    // ========================================================================

    @Test
    void POST_login_com_credenciais_validas_retorna_200_com_token() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("dr_carlos", "senha123");
        AuthResponse response = new AuthResponse("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.token");

        when(authUseCase.authenticate(any(LoginRequest.class)))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.token").value("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.token"));

        verify(authUseCase).authenticate(any(LoginRequest.class));
    }

    @Test
    void POST_login_com_usuario_medico_retorna_token() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("medico_joao", "senha_segura");
        AuthResponse response = new AuthResponse("token-medico");

        when(authUseCase.authenticate(any(LoginRequest.class)))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("token-medico"));

        verify(authUseCase).authenticate(any(LoginRequest.class));
    }

    @Test
    void POST_login_com_usuario_paciente_retorna_token() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("paciente_maria", "senha123");
        AuthResponse response = new AuthResponse("token-paciente");

        when(authUseCase.authenticate(any(LoginRequest.class)))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("token-paciente"));
    }

    @Test
    void POST_login_com_credenciais_invalidas_retorna_401() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("usuario_inexistente", "senha_errada");

        when(authUseCase.authenticate(any(LoginRequest.class)))
            .thenThrow(new AutorizacaoInvalidaException());

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());

        verify(authUseCase).authenticate(any(LoginRequest.class));
    }

    @Test
    void POST_login_com_senha_incorreta_retorna_401() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("dr_carlos", "senha_errada");

        when(authUseCase.authenticate(any(LoginRequest.class)))
            .thenThrow(new AutorizacaoInvalidaException());

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void POST_login_sem_login_retorna_400() throws Exception {
        // Arrange - login vazio
        String invalidRequest = """
            {
                "login": "",
                "password": "senha123"
            }
            """;

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(authUseCase);
    }

    @Test
    void POST_login_sem_senha_retorna_400() throws Exception {
        // Arrange - password vazio
        String invalidRequest = """
            {
                "login": "dr_carlos",
                "password": ""
            }
            """;

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(authUseCase);
    }

    @Test
    void POST_login_sem_campos_obrigatorios_retorna_400() throws Exception {
        // Arrange - ambos os campos faltam
        String invalidRequest = """
            {
            }
            """;

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(authUseCase);
    }

    @Test
    void POST_login_com_content_type_invalido_retorna_415() throws Exception {
        // Arrange
        String invalidRequest = "dr_carlos:senha123";

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL + "/login")
                .contentType(MediaType.TEXT_PLAIN)
                .content(invalidRequest))
            .andExpect(status().isUnsupportedMediaType());

        verifyNoInteractions(authUseCase);
    }

    @Test
    void POST_login_com_json_malformado_retorna_400() throws Exception {
        // Arrange - JSON inválido
        String malformedJson = """
            {
                "login": "dr_carlos"
                "password": "senha123"
            }
            """;

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(authUseCase);
    }

    @Test
    void POST_login_endpoint_nao_existe_retorna_404() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("dr_carlos", "senha123");

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL + "/authenticate")  // Endpoint errado
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());

        verifyNoInteractions(authUseCase);
    }

    @Test
    void POST_login_com_metodo_get_retorna_405() throws Exception {
        // Act & Assert
        mockMvc.perform(
            get(BASE_URL + "/login"))
            .andExpect(status().isMethodNotAllowed());

        verifyNoInteractions(authUseCase);
    }

    @Test
    void POST_login_retorna_token_no_corpo_da_resposta() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("dr_carlos", "senha123");
        String tokenEsperado = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJkcl9jYXJsb3MiLCJyb2xlIjoiTUVESUNPIn0.signature";
        AuthResponse response = new AuthResponse(tokenEsperado);

        when(authUseCase.authenticate(any(LoginRequest.class)))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value(tokenEsperado));
    }

    @Test
    void POST_login_com_login_com_espacos_retorna_400() throws Exception {
        // Arrange - login só com espaços
        String invalidRequest = """
            {
                "login": "   ",
                "password": "senha123"
            }
            """;

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(authUseCase);
    }
}
