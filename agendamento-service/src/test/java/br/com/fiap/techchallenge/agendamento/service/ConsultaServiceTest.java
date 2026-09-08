package br.com.fiap.techchallenge.agendamento.service;

import br.com.fiap.techchallenge.agendamento.dto.request.ConsultaRequest;
import br.com.fiap.techchallenge.agendamento.dto.request.ConsultaUpdateRequest;
import br.com.fiap.techchallenge.agendamento.dto.response.ConsultaResponse;
import br.com.fiap.techchallenge.agendamento.exception.EntidadeNaoEncontradaException;
import br.com.fiap.techchallenge.agendamento.exception.ForbiddenOperationException;
import br.com.fiap.techchallenge.agendamento.messaging.ConsultaEventoDTO;
import br.com.fiap.techchallenge.agendamento.model.Consulta;
import br.com.fiap.techchallenge.agendamento.model.Enfermeiro;
import br.com.fiap.techchallenge.agendamento.model.Medico;
import br.com.fiap.techchallenge.agendamento.model.Paciente;
import br.com.fiap.techchallenge.agendamento.model.StatusConsulta;
import br.com.fiap.techchallenge.agendamento.model.Usuario;
import br.com.fiap.techchallenge.agendamento.repository.ConsultaRepository;
import br.com.fiap.techchallenge.agendamento.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultaServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    private ConsultaService consultaService;

    private Paciente paciente;
    private Medico medico;
    private Consulta consulta;

    @BeforeEach
    void setUp() {

        paciente = new Paciente();
        paciente.setId(1L);
        paciente.setNome("João");

        medico = new Medico();
        medico.setId(2L);
        medico.setNome("Dr. Carlos");

        consulta = new Consulta();
        consulta.setId(10L);
        consulta.setPacienteId(1L);
        consulta.setMedicoId(2L);
        consulta.setRegistradoPorId(1L);
        consulta.setDataHora(LocalDateTime.of(2026, 9, 10, 10, 0));
        consulta.setStatus(StatusConsulta.AGENDADA);
        consulta.setObservacoes("Consulta de rotina");
        consulta.setCriadoEm(LocalDateTime.now());
        consulta.setLastModifiedAt(LocalDateTime.now());

        consultaService = new ConsultaService(consultaRepository, usuarioRepository, rabbitTemplate);

        ReflectionTestUtils.setField(consultaService, "exchange", "consulta.exchange");
        ReflectionTestUtils.setField(consultaService, "routingKeyCriada", "consulta.criada");
        ReflectionTestUtils.setField(consultaService, "routingKeyAtualizada", "consulta.atualizada");

        autenticar("joao");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // =========================================================
    // CREATE
    // =========================================================

    @Test
    void deveCriarConsultaComSucesso() {

        ConsultaRequest request = new ConsultaRequest(
                1L,
                2L,
                LocalDateTime.of(2026, 9, 15, 14, 0),
                "Consulta de rotina"
        );

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(paciente));

        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(medico));

        when(usuarioRepository.findByLogin("joao"))
                .thenReturn(Optional.of(paciente));

        ConsultaResponse response = consultaService.create(request);

        assertNotNull(response);

        ArgumentCaptor<Consulta> captor =
                ArgumentCaptor.forClass(Consulta.class);

        verify(consultaRepository).save(captor.capture());

        Consulta consultaSalva = captor.getValue();

        assertEquals(1L, consultaSalva.getPacienteId());
        assertEquals(2L, consultaSalva.getMedicoId());
        assertEquals(1L, consultaSalva.getRegistradoPorId());
        assertEquals(
                LocalDateTime.of(2026, 9, 15, 14, 0),
                consultaSalva.getDataHora()
        );
        assertEquals(StatusConsulta.AGENDADA, consultaSalva.getStatus());
        assertEquals(
                "Consulta de rotina",
                consultaSalva.getObservacoes()
        );

        assertNotNull(consultaSalva.getCriadoEm());
        assertNotNull(consultaSalva.getLastModifiedAt());

        verify(rabbitTemplate).convertAndSend(
                anyString(),
                anyString(),
                any(ConsultaEventoDTO.class)
        );
    }

    @Test
    void deveLancarExcecaoQuandoPacienteNaoExistir() {

        ConsultaRequest request = new ConsultaRequest(
                99L,
                2L,
                LocalDateTime.now(),
                "Consulta"
        );

        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> consultaService.create(request)
        );

        assertEquals(
                "pacienteId informado não corresponde a um paciente válido",
                exception.getMessage()
        );

        verify(consultaRepository, never()).save(any());
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioPacienteNaoForPaciente() {

        Enfermeiro enfermeiro = new Enfermeiro();
        enfermeiro.setId(1L);

        ConsultaRequest request = new ConsultaRequest(
                1L,
                2L,
                LocalDateTime.now(),
                "Consulta"
        );

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(enfermeiro));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> consultaService.create(request)
        );

        assertEquals(
                "pacienteId informado não corresponde a um paciente válido",
                exception.getMessage()
        );

        verify(consultaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoMedicoNaoExistir() {

        ConsultaRequest request = new ConsultaRequest(
                1L,
                99L,
                LocalDateTime.now(),
                "Consulta"
        );

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(paciente));

        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> consultaService.create(request)
        );

        assertEquals(
                "medicoId informado não corresponde a um médico válido",
                exception.getMessage()
        );

        verify(consultaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioMedicoNaoForMedico() {

        Enfermeiro enfermeiro = new Enfermeiro();
        enfermeiro.setId(2L);

        ConsultaRequest request = new ConsultaRequest(
                1L,
                2L,
                LocalDateTime.now(),
                "Consulta"
        );

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(paciente));

        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(enfermeiro));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> consultaService.create(request)
        );

        assertEquals(
                "medicoId informado não corresponde a um médico válido",
                exception.getMessage()
        );

        verify(consultaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoPacienteEMedicoForemOMesmoUsuario() {

        paciente.setId(1L);

        medico.setId(1L);

        ConsultaRequest request = new ConsultaRequest(
                1L,
                1L,
                LocalDateTime.now(),
                "Consulta"
        );

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(paciente))
                .thenReturn(Optional.of(medico));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> consultaService.create(request)
        );

        assertEquals(
                "Paciente e médico não podem ser o mesmo usuário",
                exception.getMessage()
        );

        verify(consultaRepository, never()).save(any());
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioAutenticadoNaoForEncontrado() {

        ConsultaRequest request = new ConsultaRequest(
                1L,
                2L,
                LocalDateTime.now(),
                "Consulta"
        );

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(paciente));

        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(medico));

        when(usuarioRepository.findByLogin("joao"))
                .thenReturn(Optional.empty());

        EntidadeNaoEncontradaException exception = assertThrows(
                EntidadeNaoEncontradaException.class,
                () -> consultaService.create(request)
        );

        assertEquals(
                "Usuário autenticado não encontrado: joao",
                exception.getMessage()
        );

        verify(consultaRepository, never()).save(any());
        verifyNoInteractions(rabbitTemplate);
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @Test
    void deveAtualizarConsultaComSucesso() {

        ConsultaUpdateRequest request = new ConsultaUpdateRequest(
                LocalDateTime.of(2026, 9, 20, 15, 0),
                StatusConsulta.AGENDADA,
                "Nova observação"
        );

        when(consultaRepository.findById(10L))
                .thenReturn(Optional.of(consulta));

        ConsultaResponse response =
                consultaService.update(10L, request);

        assertNotNull(response);

        verify(consultaRepository).save(consulta);

        assertEquals(
                LocalDateTime.of(2026, 9, 20, 15, 0),
                consulta.getDataHora()
        );

        assertEquals(
                StatusConsulta.AGENDADA,
                consulta.getStatus()
        );

        assertEquals(
                "Nova observação",
                consulta.getObservacoes()
        );

        assertNotNull(consulta.getLastModifiedAt());

        verify(rabbitTemplate).convertAndSend(
                anyString(),
                anyString(),
                any(ConsultaEventoDTO.class)
        );
    }

    @Test
    void deveAtualizarSomenteCamposInformados() {

        LocalDateTime dataOriginal = consulta.getDataHora();
        String observacaoOriginal = consulta.getObservacoes();

        ConsultaUpdateRequest request =
                new ConsultaUpdateRequest(
                        null,
                        null,
                        null
                );

        when(consultaRepository.findById(10L))
                .thenReturn(Optional.of(consulta));

        consultaService.update(10L, request);

        assertEquals(dataOriginal, consulta.getDataHora());
        assertEquals(StatusConsulta.AGENDADA, consulta.getStatus());
        assertEquals(observacaoOriginal, consulta.getObservacoes());

        assertNotNull(consulta.getLastModifiedAt());

        verify(consultaRepository).save(consulta);
    }

    @Test
    void naoDeveAtualizarConsultaCancelada() {

        consulta.setStatus(StatusConsulta.CANCELADA);

        ConsultaUpdateRequest request =
                new ConsultaUpdateRequest(
                        LocalDateTime.now(),
                        StatusConsulta.AGENDADA,
                        "Alteração"
                );

        when(consultaRepository.findById(10L))
                .thenReturn(Optional.of(consulta));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> consultaService.update(10L, request)
        );

        assertEquals(
                "Não é possível atualizar uma consulta cancelada",
                exception.getMessage()
        );

        verify(consultaRepository, never()).save(any());
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void deveLancarExcecaoAoAtualizarConsultaInexistente() {

        when(consultaRepository.findById(99L))
                .thenReturn(Optional.empty());

        EntidadeNaoEncontradaException exception =
                assertThrows(
                        EntidadeNaoEncontradaException.class,
                        () -> consultaService.update(
                                99L,
                                new ConsultaUpdateRequest(
                                        null,
                                        null,
                                        null
                                )
                        )
                );

        assertEquals(
                "Consulta com id 99 não encontrada",
                exception.getMessage()
        );

        verify(consultaRepository, never()).save(any());
    }

    // =========================================================
    // FIND BY ID
    // =========================================================

    @Test
    void deveBuscarConsultaPorIdComSucesso() {

        when(consultaRepository.findById(10L))
                .thenReturn(Optional.of(consulta));

        when(usuarioRepository.findByLogin("joao"))
                .thenReturn(Optional.of(paciente));

        ConsultaResponse response =
                consultaService.findById(10L);

        assertNotNull(response);

        verify(consultaRepository).findById(10L);
        verify(usuarioRepository).findByLogin("joao");
    }

    @Test
    void deveLancarExcecaoQuandoConsultaNaoExistir() {

        when(consultaRepository.findById(99L))
                .thenReturn(Optional.empty());

        EntidadeNaoEncontradaException exception =
                assertThrows(
                        EntidadeNaoEncontradaException.class,
                        () -> consultaService.findById(99L)
                );

        assertEquals(
                "Consulta com id 99 não encontrada",
                exception.getMessage()
        );

        verify(usuarioRepository, never()).findByLogin(anyString());
    }

    @Test
    void pacienteNaoPodeVisualizarConsultaDeOutroPaciente() {

        Paciente outroPaciente = new Paciente();
        outroPaciente.setId(50L);

        when(consultaRepository.findById(10L))
                .thenReturn(Optional.of(consulta));

        when(usuarioRepository.findByLogin("joao"))
                .thenReturn(Optional.of(outroPaciente));

        ForbiddenOperationException exception =
                assertThrows(
                        ForbiddenOperationException.class,
                        () -> consultaService.findById(10L)
                );

        assertEquals(
                "Paciente só pode visualizar as próprias consultas",
                exception.getMessage()
        );
    }

    @Test
    void medicoPodeVisualizarConsultaDeQualquerPaciente() {

        when(consultaRepository.findById(10L))
                .thenReturn(Optional.of(consulta));

        when(usuarioRepository.findByLogin("joao"))
                .thenReturn(Optional.of(medico));

        ConsultaResponse response =
                consultaService.findById(10L);

        assertNotNull(response);
    }

    // =========================================================
    // FIND ALL
    // =========================================================

    @Test
    void deveRetornarTodasAsConsultas() {

        Consulta consulta2 = new Consulta();
        consulta2.setId(20L);
        consulta2.setPacienteId(3L);
        consulta2.setMedicoId(4L);
        consulta2.setStatus(StatusConsulta.AGENDADA);
        consulta2.setDataHora(LocalDateTime.now());

        when(consultaRepository.findAll())
                .thenReturn(List.of(consulta, consulta2));

        List<ConsultaResponse> response =
                consultaService.findAll();

        assertNotNull(response);
        assertEquals(2, response.size());

        verify(consultaRepository).findAll();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremConsultas() {

        when(consultaRepository.findAll())
                .thenReturn(List.of());

        List<ConsultaResponse> response =
                consultaService.findAll();

        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(consultaRepository).findAll();
    }

    // =========================================================
    // FIND BY PACIENTE
    // =========================================================

    @Test
    void deveBuscarConsultasDoPaciente() {

        when(usuarioRepository.findByLogin("joao"))
                .thenReturn(Optional.of(paciente));

        when(consultaRepository.findByPacienteId(1L))
                .thenReturn(List.of(consulta));

        List<ConsultaResponse> response =
                consultaService.findByPaciente(1L);

        assertNotNull(response);
        assertEquals(1, response.size());

        verify(consultaRepository)
                .findByPacienteId(1L);
    }

    @Test
    void pacienteNaoPodeBuscarConsultasDeOutroPaciente() {

        Paciente outroPaciente = new Paciente();
        outroPaciente.setId(50L);

        when(usuarioRepository.findByLogin("joao"))
                .thenReturn(Optional.of(outroPaciente));

        ForbiddenOperationException exception =
                assertThrows(
                        ForbiddenOperationException.class,
                        () -> consultaService.findByPaciente(1L)
                );

        assertEquals(
                "Paciente só pode visualizar as próprias consultas",
                exception.getMessage()
        );

        verify(consultaRepository, never())
                .findByPacienteId(anyLong());
    }

    @Test
    void medicoPodeBuscarConsultasDeQualquerPaciente() {

        when(usuarioRepository.findByLogin("joao"))
                .thenReturn(Optional.of(medico));

        when(consultaRepository.findByPacienteId(1L))
                .thenReturn(List.of(consulta));

        List<ConsultaResponse> response =
                consultaService.findByPaciente(1L);

        assertEquals(1, response.size());

        verify(consultaRepository)
                .findByPacienteId(1L);
    }

    // =========================================================
    // AUXILIAR
    // =========================================================

    private void autenticar(String login) {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        login,
                        null
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }
}