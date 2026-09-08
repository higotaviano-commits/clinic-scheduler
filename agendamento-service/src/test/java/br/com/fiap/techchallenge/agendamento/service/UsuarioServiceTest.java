package br.com.fiap.techchallenge.agendamento.service;

import br.com.fiap.techchallenge.agendamento.dto.request.CreateUsuarioRequest;
import br.com.fiap.techchallenge.agendamento.dto.response.UsuarioResponse;
import br.com.fiap.techchallenge.agendamento.model.Enfermeiro;
import br.com.fiap.techchallenge.agendamento.model.Medico;
import br.com.fiap.techchallenge.agendamento.model.Paciente;
import br.com.fiap.techchallenge.agendamento.model.Usuario;
import br.com.fiap.techchallenge.agendamento.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private CreateUsuarioRequest request;

    @BeforeEach
    void setUp() {
        request = new CreateUsuarioRequest(
                "João da Silva",
                "joao@email.com",
                "joao",
                "123456",
                "MEDICO"
        );
    }

    @Test
    void deveCriarUsuarioMedicoComSucesso() {
        when(usuarioRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(usuarioRepository.existsByLogin(request.login()))
                .thenReturn(false);

        when(passwordEncoder.encode(request.password()))
                .thenReturn("senha-criptografada");

        UsuarioResponse response = usuarioService.create(request);

        assertNotNull(response);

        verify(usuarioRepository).save(any(Medico.class));

        verify(passwordEncoder)
                .encode(request.password());
    }

    @Test
    void deveCriarUsuarioEnfermeiroComSucesso() {
        request = new CreateUsuarioRequest(
                "Maria",
                "maria@email.com",
                "maria",
                "123456",
                "ENFERMEIRO"
        );

        when(usuarioRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(usuarioRepository.existsByLogin(request.login()))
                .thenReturn(false);

        when(passwordEncoder.encode(request.password()))
                .thenReturn("senha-criptografada");

        UsuarioResponse response = usuarioService.create(request);

        assertNotNull(response);

        verify(usuarioRepository).save(any(Enfermeiro.class));
        verify(passwordEncoder).encode(request.password());
    }

    @Test
    void deveCriarUsuarioPacienteComSucesso() {
        request = new CreateUsuarioRequest(
                "Pedro",
                "pedro@email.com",
                "pedro",
                "123456",
                "PACIENTE"
        );

        when(usuarioRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(usuarioRepository.existsByLogin(request.login()))
                .thenReturn(false);

        when(passwordEncoder.encode(request.password()))
                .thenReturn("senha-criptografada");

        UsuarioResponse response = usuarioService.create(request);

        assertNotNull(response);

        verify(usuarioRepository).save(any(Paciente.class));
        verify(passwordEncoder).encode(request.password());
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaEstiverCadastrado() {
        when(usuarioRepository.existsByEmail(request.email()))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.create(request)
        );

        assertEquals("E-mail já cadastrado", exception.getMessage());

        verify(usuarioRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void deveLancarExcecaoQuandoLoginJaEstiverCadastrado() {
        when(usuarioRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(usuarioRepository.existsByLogin(request.login()))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.create(request)
        );

        assertEquals("Login já cadastrado", exception.getMessage());

        verify(usuarioRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void deveLancarExcecaoQuandoTipoUsuarioForInvalido() {
        request = new CreateUsuarioRequest(
                "João",
                "joao@email.com",
                "joao",
                "123456",
                "DENTISTA"
        );

        when(usuarioRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(usuarioRepository.existsByLogin(request.login()))
                .thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.create(request)
        );

        assertEquals(
                "Tipo inválido. Use MEDICO, ENFERMEIRO ou PACIENTE",
                exception.getMessage()
        );

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveCodificarSenhaAntesDeSalvarUsuario() {
        when(usuarioRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(usuarioRepository.existsByLogin(request.login()))
                .thenReturn(false);

        when(passwordEncoder.encode("123456"))
                .thenReturn("senha-criptografada");

        usuarioService.create(request);

        ArgumentCaptor<Usuario> captor =
                ArgumentCaptor.forClass(Usuario.class);

        verify(usuarioRepository).save(captor.capture());

        Usuario usuarioSalvo = captor.getValue();

        assertEquals("senha-criptografada", usuarioSalvo.getSenha());

        verify(passwordEncoder).encode("123456");
    }

    @Test
    void devePreencherDadosDoUsuarioAntesDeSalvar() {
        when(usuarioRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(usuarioRepository.existsByLogin(request.login()))
                .thenReturn(false);

        when(passwordEncoder.encode(request.password()))
                .thenReturn("senha-criptografada");

        usuarioService.create(request);

        ArgumentCaptor<Usuario> captor =
                ArgumentCaptor.forClass(Usuario.class);

        verify(usuarioRepository).save(captor.capture());

        Usuario usuarioSalvo = captor.getValue();

        assertEquals("João da Silva", usuarioSalvo.getNome());
        assertEquals("joao@email.com", usuarioSalvo.getEmail());
        assertEquals("joao", usuarioSalvo.getLogin());
        assertEquals("senha-criptografada", usuarioSalvo.getSenha());

        assertNotNull(usuarioSalvo.getLastModifiedAt());
    }

    @Test
    void deveAceitarTipoUsuarioEmMinusculo() {
        request = new CreateUsuarioRequest(
                "João",
                "joao@email.com",
                "joao",
                "123456",
                "medico"
        );

        when(usuarioRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(usuarioRepository.existsByLogin(request.login()))
                .thenReturn(false);

        when(passwordEncoder.encode(request.password()))
                .thenReturn("senha-criptografada");

        usuarioService.create(request);

        verify(usuarioRepository).save(any(Medico.class));
    }
}