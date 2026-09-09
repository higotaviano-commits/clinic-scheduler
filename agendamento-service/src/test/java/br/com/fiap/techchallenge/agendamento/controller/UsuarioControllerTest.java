package br.com.fiap.techchallenge.agendamento.controller;

import br.com.fiap.techchallenge.agendamento.dto.request.CreateUsuarioRequest;
import br.com.fiap.techchallenge.agendamento.dto.response.UsuarioResponse;
import br.com.fiap.techchallenge.agendamento.service.JwtService;
import br.com.fiap.techchallenge.agendamento.usecase.UsuarioUseCase;
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
 * Testes para UsuarioController
 * 
 * Testa:
 * - POST /api/v1/usuarios - criar usuário bem-sucedido (MEDICO, ENFERMEIRO, PACIENTE)
 * - Request inválido (missing fields, email inválido, tipo inválido)
 * 
 * Notas:
 * - Endpoint é público (sem autenticação necessária)
 */
@WebMvcTest(UsuarioController.class)
@Import(SecurityConfig.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioUseCase usuarioUseCase;

    private static final String BASE_URL = "/api/v1/usuarios";

    // ========================================================================
    // POST - Criar Usuário
    // ========================================================================

    @Test
    void POST_criarUsuario_com_tipo_MEDICO_retorna_201() throws Exception {
        // Arrange
        CreateUsuarioRequest request = new CreateUsuarioRequest(
            "Dr. Carlos Silva",
            "carlos@hospital.com",
            "dr_carlos",
            "senha_segura_123",
            "MEDICO"
        );

        UsuarioResponse response = new UsuarioResponse(
            1L,
            "Dr. Carlos Silva",
            "carlos@hospital.com",
            "dr_carlos",
            "MEDICO"
        );

        when(usuarioUseCase.create(any(CreateUsuarioRequest.class)))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.nome").value("Dr. Carlos Silva"))
            .andExpect(jsonPath("$.email").value("carlos@hospital.com"))
            .andExpect(jsonPath("$.login").value("dr_carlos"))
            .andExpect(jsonPath("$.tipoUsuario").value("MEDICO"));

        verify(usuarioUseCase).create(any(CreateUsuarioRequest.class));
    }

    @Test
    void POST_criarUsuario_com_tipo_ENFERMEIRO_retorna_201() throws Exception {
        // Arrange
        CreateUsuarioRequest request = new CreateUsuarioRequest(
            "Maria Santos",
            "maria@hospital.com",
            "maria_santos",
            "senha_123",
            "ENFERMEIRO"
        );

        UsuarioResponse response = new UsuarioResponse(
            2L,
            "Maria Santos",
            "maria@hospital.com",
            "maria_santos",
            "ENFERMEIRO"
        );

        when(usuarioUseCase.create(any(CreateUsuarioRequest.class)))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.tipoUsuario").value("ENFERMEIRO"));

        verify(usuarioUseCase).create(any(CreateUsuarioRequest.class));
    }

    @Test
    void POST_criarUsuario_com_tipo_PACIENTE_retorna_201() throws Exception {
        // Arrange
        CreateUsuarioRequest request = new CreateUsuarioRequest(
            "João Silva",
            "joao@email.com",
            "joao_silva",
            "minha_senha_123",
            "PACIENTE"
        );

        UsuarioResponse response = new UsuarioResponse(
            3L,
            "João Silva",
            "joao@email.com",
            "joao_silva",
            "PACIENTE"
        );

        when(usuarioUseCase.create(any(CreateUsuarioRequest.class)))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.tipoUsuario").value("PACIENTE"));

        verify(usuarioUseCase).create(any(CreateUsuarioRequest.class));
    }

    @Test
    void POST_criarUsuario_sem_nome_retorna_400() throws Exception {
        // Arrange - nome vazio
        String invalidRequest = """
            {
                "nome": "",
                "email": "carlos@hospital.com",
                "login": "dr_carlos",
                "password": "senha123",
                "tipoUsuario": "MEDICO"
            }
            """;

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioUseCase);
    }

    @Test
    void POST_criarUsuario_sem_email_retorna_400() throws Exception {
        // Arrange - email vazio
        String invalidRequest = """
            {
                "nome": "Dr. Carlos",
                "email": "",
                "login": "dr_carlos",
                "password": "senha123",
                "tipoUsuario": "MEDICO"
            }
            """;

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioUseCase);
    }

    @Test
    void POST_criarUsuario_com_email_invalido_retorna_400() throws Exception {
        // Arrange - email sem @
        String invalidRequest = """
            {
                "nome": "Dr. Carlos",
                "email": "carlos_hospital.com",
                "login": "dr_carlos",
                "password": "senha123",
                "tipoUsuario": "MEDICO"
            }
            """;

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioUseCase);
    }

    @Test
    void POST_criarUsuario_com_email_com_espaco_invalido_retorna_400() throws Exception {
        // Arrange - email com espaço
        String invalidRequest = """
            {
                "nome": "Dr. Carlos",
                "email": "carlos @hospital.com",
                "login": "dr_carlos",
                "password": "senha123",
                "tipoUsuario": "MEDICO"
            }
            """;

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioUseCase);
    }

    @Test
    void POST_criarUsuario_sem_login_retorna_400() throws Exception {
        // Arrange - login vazio
        String invalidRequest = """
            {
                "nome": "Dr. Carlos",
                "email": "carlos@hospital.com",
                "login": "",
                "password": "senha123",
                "tipoUsuario": "MEDICO"
            }
            """;

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioUseCase);
    }

    @Test
    void POST_criarUsuario_sem_password_retorna_400() throws Exception {
        // Arrange - password vazio
        String invalidRequest = """
            {
                "nome": "Dr. Carlos",
                "email": "carlos@hospital.com",
                "login": "dr_carlos",
                "password": "",
                "tipoUsuario": "MEDICO"
            }
            """;

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioUseCase);
    }

    @Test
    void POST_criarUsuario_sem_tipoUsuario_retorna_400() throws Exception {
        // Arrange - tipoUsuario vazio
        String invalidRequest = """
            {
                "nome": "Dr. Carlos",
                "email": "carlos@hospital.com",
                "login": "dr_carlos",
                "password": "senha123",
                "tipoUsuario": ""
            }
            """;

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioUseCase);
    }

    @Test
    void POST_criarUsuario_faltam_campos_obrigatorios_retorna_400() throws Exception {
        // Arrange - faltam vários campos
        String invalidRequest = """
            {
                "nome": "Dr. Carlos"
            }
            """;

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioUseCase);
    }

    @Test
    void POST_criarUsuario_com_nome_com_espacos_retorna_400() throws Exception {
        // Arrange - nome com apenas espaços
        String invalidRequest = """
            {
                "nome": "   ",
                "email": "carlos@hospital.com",
                "login": "dr_carlos",
                "password": "senha123",
                "tipoUsuario": "MEDICO"
            }
            """;

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioUseCase);
    }

    @Test
    void POST_criarUsuario_com_json_malformado_retorna_400() throws Exception {
        // Arrange - JSON inválido
        String malformedJson = """
            {
                "nome": "Dr. Carlos"
                "email": "carlos@hospital.com"
            }
            """;

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioUseCase);
    }

    @Test
    void POST_criarUsuario_com_content_type_invalido_retorna_415() throws Exception {
        // Arrange
        String invalidRequest = "nome=Dr.Carlos&email=carlos@hospital.com";

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(invalidRequest))
            .andExpect(status().isUnsupportedMediaType());

        verifyNoInteractions(usuarioUseCase);
    }

    @Test
    void POST_criarUsuario_com_metodo_get_retorna_405() throws Exception {
        // Act & Assert
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isMethodNotAllowed());

        verifyNoInteractions(usuarioUseCase);
    }

    @Test
    void POST_criarUsuario_sem_content_type_retorna_erro() throws Exception {
        // Arrange
        CreateUsuarioRequest request = new CreateUsuarioRequest(
            "Dr. Carlos",
            "carlos@hospital.com",
            "dr_carlos",
            "senha123",
            "MEDICO"
        );

        // Act & Assert - sem Content-Type
        mockMvc.perform(
            post(BASE_URL)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnsupportedMediaType());

        verifyNoInteractions(usuarioUseCase);
    }

    @Test
    void POST_criarUsuario_response_contem_id_gerado() throws Exception {
        // Arrange
        CreateUsuarioRequest request = new CreateUsuarioRequest(
            "Ana Costa",
            "ana@hospital.com",
            "ana_costa",
            "senha123",
            "ENFERMEIRO"
        );

        UsuarioResponse response = new UsuarioResponse(
            42L,
            "Ana Costa",
            "ana@hospital.com",
            "ana_costa",
            "ENFERMEIRO"
        );

        when(usuarioUseCase.create(any(CreateUsuarioRequest.class)))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.id").value(42L));

        verify(usuarioUseCase).create(any(CreateUsuarioRequest.class));
    }

    @Test
    void POST_criarUsuario_nao_retorna_senha_na_resposta() throws Exception {
        // Arrange
        CreateUsuarioRequest request = new CreateUsuarioRequest(
            "Dr. Carlos",
            "carlos@hospital.com",
            "dr_carlos",
            "minha_senha_secreta",
            "MEDICO"
        );

        UsuarioResponse response = new UsuarioResponse(
            1L,
            "Dr. Carlos",
            "carlos@hospital.com",
            "dr_carlos",
            "MEDICO"
        );

        when(usuarioUseCase.create(any(CreateUsuarioRequest.class)))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.senha").doesNotExist());
    }
}
