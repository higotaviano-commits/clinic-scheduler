package br.com.fiap.techchallenge.agendamento.controller;

import br.com.fiap.techchallenge.agendamento.dto.request.ConsultaRequest;
import br.com.fiap.techchallenge.agendamento.dto.request.ConsultaUpdateRequest;
import br.com.fiap.techchallenge.agendamento.dto.response.ConsultaResponse;
import br.com.fiap.techchallenge.agendamento.exception.EntidadeNaoEncontradaException;
import br.com.fiap.techchallenge.agendamento.model.StatusConsulta;
import br.com.fiap.techchallenge.agendamento.service.JwtService;
import br.com.fiap.techchallenge.agendamento.usecase.ConsultaUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import br.com.fiap.techchallenge.agendamento.config.SecurityConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes para ConsultaController
 * 
 * Testa:
 * - POST /api/v1/consultas - criar consulta
 * - PUT /api/v1/consultas/{id} - atualizar consulta
 * - GET /api/v1/consultas/{id} - buscar por ID
 * - GET /api/v1/consultas - listar todas
 * - GET /api/v1/consultas/paciente/{pacienteId} - buscar por paciente
 * 
 * Controles de acesso:
 * - MEDICO: pode criar, atualizar, visualizar tudo
 * - ENFERMEIRO: pode criar, visualizar tudo
 * - PACIENTE: pode visualizar apenas suas consultas
 */
@WebMvcTest(ConsultaController.class)
@Import(SecurityConfig.class)
class ConsultaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private ConsultaUseCase consultaUseCase;

    private static final String BASE_URL = "/api/v1/consultas";

    // ========================================================================
    // POST - Criar Consulta
    // ========================================================================

    @Test
    @WithMockUser(roles = "ENFERMEIRO")
    void POST_criarConsulta_com_enfermeiro_retorna_201() throws Exception {
        // Arrange
        LocalDateTime dataHora = LocalDateTime.of(2026, 10, 15, 14, 0);
        ConsultaRequest request = new ConsultaRequest(
            1L, 2L, dataHora, "Consulta de rotina"
        );

        ConsultaResponse response = new ConsultaResponse(
            10L, 1L, 2L, 1L, dataHora,
            StatusConsulta.AGENDADA, "Consulta de rotina",
            LocalDateTime.now(), LocalDateTime.now()
        );

        when(consultaUseCase.create(any(ConsultaRequest.class)))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(10L))
            .andExpect(jsonPath("$.status").value("AGENDADA"))
            .andExpect(jsonPath("$.observacoes").value("Consulta de rotina"));

        verify(consultaUseCase).create(any(ConsultaRequest.class));
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    void POST_criarConsulta_com_medico_retorna_201() throws Exception {
        // Arrange
        LocalDateTime dataHora = LocalDateTime.of(2026, 10, 15, 14, 0);
        ConsultaRequest request = new ConsultaRequest(
            1L, 2L, dataHora, "Consulta urgente"
        );

        ConsultaResponse response = new ConsultaResponse(
            10L, 1L, 2L, 1L, dataHora,
            StatusConsulta.AGENDADA, "Consulta urgente",
            LocalDateTime.now(), LocalDateTime.now()
        );

        when(consultaUseCase.create(any(ConsultaRequest.class)))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        verify(consultaUseCase).create(any(ConsultaRequest.class));
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    void POST_criarConsulta_com_paciente_retorna_403() throws Exception {
        // Arrange
        LocalDateTime dataHora = LocalDateTime.of(2026, 10, 15, 14, 0);
        ConsultaRequest request = new ConsultaRequest(
            1L, 2L, dataHora, "Teste"
        );

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());

        verifyNoInteractions(consultaUseCase);
    }

    @Test
    void POST_criarConsulta_sem_autenticacao_retorna_401() throws Exception {
        // Arrange
        LocalDateTime dataHora = LocalDateTime.of(2026, 10, 15, 14, 0);
        ConsultaRequest request = new ConsultaRequest(
            1L, 2L, dataHora, "Teste"
        );

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(consultaUseCase);
    }

    @Test
    @WithMockUser(roles = "ENFERMEIRO")
    void POST_criarConsulta_sem_pacienteId_retorna_400() throws Exception {
        // Arrange
        String invalidRequest = """
            {
                "medicoId": 2,
                "dataHora": "2026-10-15T14:00:00",
                "observacoes": "Teste"
            }
            """;

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(consultaUseCase);
    }

    @Test
    @WithMockUser(roles = "ENFERMEIRO")
    void POST_criarConsulta_com_pacienteId_nulo_retorna_400() throws Exception {
        // Arrange
        String invalidRequest = """
            {
                "pacienteId": null,
                "medicoId": 2,
                "dataHora": "2026-10-15T14:00:00",
                "observacoes": "Teste"
            }
            """;

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(consultaUseCase);
    }

    @Test
    @WithMockUser(roles = "ENFERMEIRO")
    void POST_criarConsulta_sem_medicoId_retorna_400() throws Exception {
        // Arrange
        String invalidRequest = """
            {
                "pacienteId": 1,
                "dataHora": "2026-10-15T14:00:00",
                "observacoes": "Teste"
            }
            """;

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(consultaUseCase);
    }

    @Test
    @WithMockUser(roles = "ENFERMEIRO")
    void POST_criarConsulta_sem_dataHora_retorna_400() throws Exception {
        // Arrange
        String invalidRequest = """
            {
                "pacienteId": 1,
                "medicoId": 2,
                "observacoes": "Teste"
            }
            """;

        // Act & Assert
        mockMvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(consultaUseCase);
    }

    // ========================================================================
    // PUT - Atualizar Consulta
    // ========================================================================

    @Test
    @WithMockUser(roles = "MEDICO")
    void PUT_atualizarConsulta_com_medico_retorna_200() throws Exception {
        // Arrange
        ConsultaUpdateRequest request = new ConsultaUpdateRequest(
            LocalDateTime.of(2026, 10, 20, 14, 0),
            StatusConsulta.AGENDADA,
            "Atualizada"
        );

        ConsultaResponse response = new ConsultaResponse(
            10L, 1L, 2L, 1L, LocalDateTime.of(2026, 10, 20, 14, 0),
            StatusConsulta.AGENDADA, "Atualizada",
            LocalDateTime.now(), LocalDateTime.now()
        );

        when(consultaUseCase.update(eq(10L), any(ConsultaUpdateRequest.class)))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(
            put(BASE_URL + "/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(10L))
            .andExpect(jsonPath("$.observacoes").value("Atualizada"));

        verify(consultaUseCase).update(eq(10L), any(ConsultaUpdateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ENFERMEIRO")
    void PUT_atualizarConsulta_com_enfermeiro_retorna_403() throws Exception {
        // Arrange - apenas MEDICO pode atualizar
        String updateRequest = """
            {
                "status": "CANCELADA"
            }
            """;

        // Act & Assert
        mockMvc.perform(
            put(BASE_URL + "/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest))
            .andExpect(status().isForbidden());

        verifyNoInteractions(consultaUseCase);
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    void PUT_atualizarConsulta_com_paciente_retorna_403() throws Exception {
        // Arrange
        String updateRequest = """
            {
                "status": "CANCELADA"
            }
            """;

        // Act & Assert
        mockMvc.perform(
            put(BASE_URL + "/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest))
            .andExpect(status().isForbidden());

        verifyNoInteractions(consultaUseCase);
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    void PUT_atualizarConsultaInexistente_retorna_404() throws Exception {
        // Arrange
        ConsultaUpdateRequest request = new ConsultaUpdateRequest(
            null,
            StatusConsulta.CANCELADA,
            null
        );

        when(consultaUseCase.update(eq(99L), any(ConsultaUpdateRequest.class)))
            .thenThrow(new EntidadeNaoEncontradaException("Consulta com id 99 não encontrada"));

        // Act & Assert
        mockMvc.perform(
            put(BASE_URL + "/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    void PUT_atualizarConsulta_sem_autenticacao_retorna_401() throws Exception {
        // Arrange
        String updateRequest = """
            {
                "status": "CANCELADA"
            }
            """;

        // Act & Assert
        mockMvc.perform(
            put(BASE_URL + "/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(consultaUseCase);
    }

    // ========================================================================
    // GET - Buscar por ID
    // ========================================================================

    @Test
    @WithMockUser(roles = "MEDICO")
    void GET_buscarConsultaPorId_com_medico_retorna_200() throws Exception {
        // Arrange
        ConsultaResponse response = new ConsultaResponse(
            10L, 1L, 2L, 1L, LocalDateTime.of(2026, 10, 15, 14, 0),
            StatusConsulta.AGENDADA, "Teste",
            LocalDateTime.now(), LocalDateTime.now()
        );

        when(consultaUseCase.findById(10L))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(
            get(BASE_URL + "/10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(10L))
            .andExpect(jsonPath("$.pacienteId").value(1L));

        verify(consultaUseCase).findById(10L);
    }

    @Test
    @WithMockUser(roles = "ENFERMEIRO")
    void GET_buscarConsultaPorId_com_enfermeiro_retorna_200() throws Exception {
        // Arrange
        ConsultaResponse response = new ConsultaResponse(
            10L, 1L, 2L, 1L, LocalDateTime.of(2026, 10, 15, 14, 0),
            StatusConsulta.AGENDADA, "Teste",
            LocalDateTime.now(), LocalDateTime.now()
        );

        when(consultaUseCase.findById(10L))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(
            get(BASE_URL + "/10"))
            .andExpect(status().isOk());

        verify(consultaUseCase).findById(10L);
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    void GET_buscarConsultaPorId_com_paciente_retorna_200() throws Exception {
        // Arrange
        ConsultaResponse response = new ConsultaResponse(
            10L, 1L, 2L, 1L, LocalDateTime.of(2026, 10, 15, 14, 0),
            StatusConsulta.AGENDADA, "Teste",
            LocalDateTime.now(), LocalDateTime.now()
        );

        when(consultaUseCase.findById(10L))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(
            get(BASE_URL + "/10"))
            .andExpect(status().isOk());

        verify(consultaUseCase).findById(10L);
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    void GET_buscarConsultaInexistente_retorna_404() throws Exception {
        // Arrange
        when(consultaUseCase.findById(99L))
            .thenThrow(new EntidadeNaoEncontradaException("Consulta com id 99 não encontrada"));

        // Act & Assert
        mockMvc.perform(
            get(BASE_URL + "/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void GET_buscarConsultaPorId_sem_autenticacao_retorna_401() throws Exception {
        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/10"))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(consultaUseCase);
    }

    // ========================================================================
    // GET - Listar Todas
    // ========================================================================

    @Test
    @WithMockUser(roles = "MEDICO")
    void GET_listarTodasAsConsultas_com_medico_retorna_200() throws Exception {
        // Arrange
        ConsultaResponse consulta1 = new ConsultaResponse(
            10L, 1L, 2L, 1L, LocalDateTime.now().plusDays(1),
            StatusConsulta.AGENDADA, "Teste 1",
            LocalDateTime.now(), LocalDateTime.now()
        );

        ConsultaResponse consulta2 = new ConsultaResponse(
            11L, 3L, 2L, 1L, LocalDateTime.now().plusDays(2),
            StatusConsulta.AGENDADA, "Teste 2",
            LocalDateTime.now(), LocalDateTime.now()
        );

        when(consultaUseCase.findAll())
            .thenReturn(List.of(consulta1, consulta2));

        // Act & Assert
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(10L))
            .andExpect(jsonPath("$[1].id").value(11L));

        verify(consultaUseCase).findAll();
    }

    @Test
    @WithMockUser(roles = "ENFERMEIRO")
    void GET_listarTodasAsConsultas_com_enfermeiro_retorna_200() throws Exception {
        // Arrange
        ConsultaResponse consulta1 = new ConsultaResponse(
            10L, 1L, 2L, 1L, LocalDateTime.now().plusDays(1),
            StatusConsulta.AGENDADA, "Teste 1",
            LocalDateTime.now(), LocalDateTime.now()
        );

        when(consultaUseCase.findAll())
            .thenReturn(List.of(consulta1));

        // Act & Assert
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    void GET_listarTodasAsConsultas_com_paciente_retorna_403() throws Exception {
        // Pacientes NÃO podem listar todas
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isForbidden());

        verifyNoInteractions(consultaUseCase);
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    void GET_listarTodasAsConsultas_retorna_lista_vazia() throws Exception {
        // Arrange
        when(consultaUseCase.findAll())
            .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void GET_listarTodasAsConsultas_sem_autenticacao_retorna_401() throws Exception {
        // Act & Assert
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(consultaUseCase);
    }

    // ========================================================================
    // GET - Por Paciente
    // ========================================================================

    @Test
    @WithMockUser(roles = "MEDICO")
    void GET_buscarConsultasPorPaciente_com_medico_retorna_200() throws Exception {
        // Arrange
        ConsultaResponse response = new ConsultaResponse(
            10L, 1L, 2L, 1L, LocalDateTime.now().plusDays(1),
            StatusConsulta.AGENDADA, "Teste",
            LocalDateTime.now(), LocalDateTime.now()
        );

        when(consultaUseCase.findByPaciente(1L))
            .thenReturn(List.of(response));

        // Act & Assert
        mockMvc.perform(
            get(BASE_URL + "/paciente/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].pacienteId").value(1L));

        verify(consultaUseCase).findByPaciente(1L);
    }

    @Test
    @WithMockUser(roles = "ENFERMEIRO")
    void GET_buscarConsultasPorPaciente_com_enfermeiro_retorna_200() throws Exception {
        // Arrange
        ConsultaResponse response = new ConsultaResponse(
            10L, 1L, 2L, 1L, LocalDateTime.now().plusDays(1),
            StatusConsulta.AGENDADA, "Teste",
            LocalDateTime.now(), LocalDateTime.now()
        );

        when(consultaUseCase.findByPaciente(1L))
            .thenReturn(List.of(response));

        // Act & Assert
        mockMvc.perform(
            get(BASE_URL + "/paciente/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    void GET_buscarConsultasPorPaciente_com_paciente_retorna_200() throws Exception {
        // Arrange
        ConsultaResponse response = new ConsultaResponse(
            10L, 1L, 2L, 1L, LocalDateTime.now().plusDays(1),
            StatusConsulta.AGENDADA, "Teste",
            LocalDateTime.now(), LocalDateTime.now()
        );

        when(consultaUseCase.findByPaciente(1L))
            .thenReturn(List.of(response));

        // Act & Assert
        mockMvc.perform(
            get(BASE_URL + "/paciente/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    void GET_buscarConsultasPorPaciente_retorna_lista_vazia() throws Exception {
        // Arrange
        when(consultaUseCase.findByPaciente(1L))
            .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(
            get(BASE_URL + "/paciente/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void GET_buscarConsultasPorPaciente_sem_autenticacao_retorna_401() throws Exception {
        // Act & Assert
        mockMvc.perform(
            get(BASE_URL + "/paciente/1"))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(consultaUseCase);
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    void GET_buscarConsultasPorPaciente_com_paciente_inexistente_retorna_lista_vazia() throws Exception {
        // Arrange
        when(consultaUseCase.findByPaciente(99L))
            .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(
            get(BASE_URL + "/paciente/99"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    // ========================================================================
    // Métodos HTTP Inválidos
    // ========================================================================

    @Test
    @WithMockUser(roles = "MEDICO")
    void DELETE_removerConsulta_retorna_405() throws Exception {
        // Act & Assert
        mockMvc.perform(
            delete(BASE_URL + "/10"))
            .andExpect(status().isMethodNotAllowed());

        verifyNoInteractions(consultaUseCase);
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    void PATCH_modificarConsulta_retorna_405() throws Exception {
        // Act & Assert
        mockMvc.perform(
            patch(BASE_URL + "/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isMethodNotAllowed());

        verifyNoInteractions(consultaUseCase);
    }
}
