package br.com.fiap.techchallenge.agendamento.service;

import br.com.fiap.techchallenge.agendamento.dto.request.LoginRequest;
import br.com.fiap.techchallenge.agendamento.dto.response.AuthResponse;
import br.com.fiap.techchallenge.agendamento.exception.InvalidCredentialsException;
import br.com.fiap.techchallenge.agendamento.model.Medico;
import br.com.fiap.techchallenge.agendamento.model.Paciente;
import br.com.fiap.techchallenge.agendamento.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private Medico medico;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        medico = new Medico();
        medico.setId(1L);
        medico.setLogin("dr_carlos");
        medico.setNome("Dr. Carlos");
        medico.setSenha("senha-criptografada");

        loginRequest = new LoginRequest("dr_carlos", "123456");
    }

    @Test
    void deveAutenticarUsuarioComSucesso() {
        when(usuarioRepository.findByLogin("dr_carlos"))
                .thenReturn(Optional.of(medico));

        when(passwordEncoder.matches("123456", "senha-criptografada"))
                .thenReturn(true);

        when(jwtService.generateToken("dr_carlos", "MEDICO"))
                .thenReturn("token-valido");

        AuthResponse response = authService.authenticate(loginRequest);

        assertNotNull(response);
        assertEquals("token-valido", response.token());

        verify(usuarioRepository).findByLogin("dr_carlos");
        verify(passwordEncoder).matches("123456", "senha-criptografada");
        verify(jwtService).generateToken("dr_carlos", "MEDICO");
    }

    @Test
    void deveAutenticarPacienteComSucesso() {
        Paciente paciente = new Paciente();
        paciente.setId(2L);
        paciente.setLogin("joao_paciente");
        paciente.setNome("João");
        paciente.setSenha("senha-criptografada");

        LoginRequest request = new LoginRequest("joao_paciente", "123456");

        when(usuarioRepository.findByLogin("joao_paciente"))
                .thenReturn(Optional.of(paciente));

        when(passwordEncoder.matches("123456", "senha-criptografada"))
                .thenReturn(true);

        when(jwtService.generateToken("joao_paciente", "PACIENTE"))
                .thenReturn("token-paciente");

        AuthResponse response = authService.authenticate(request);

        assertNotNull(response);
        assertEquals("token-paciente", response.token());

        verify(jwtService).generateToken("joao_paciente", "PACIENTE");
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExistir() {
        when(usuarioRepository.findByLogin("usuario_inexistente"))
                .thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("usuario_inexistente", "123456");

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.authenticate(request)
        );

        verify(usuarioRepository).findByLogin("usuario_inexistente");
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    void deveLancarExcecaoQuandoSenhaNaoCorresponder() {
        when(usuarioRepository.findByLogin("dr_carlos"))
                .thenReturn(Optional.of(medico));

        when(passwordEncoder.matches("senha_incorreta", "senha-criptografada"))
                .thenReturn(false);

        LoginRequest request = new LoginRequest("dr_carlos", "senha_incorreta");

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.authenticate(request)
        );

        verify(usuarioRepository).findByLogin("dr_carlos");
        verify(passwordEncoder).matches("senha_incorreta", "senha-criptografada");
        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    void deveLancarExcecaoQuandoSenhaForNula() {
        medico.setSenha(null);

        when(usuarioRepository.findByLogin("dr_carlos"))
                .thenReturn(Optional.of(medico));

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.authenticate(loginRequest)
        );

        verify(usuarioRepository).findByLogin("dr_carlos");
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    void deveGerarTokenComPerfilCorreto() {
        when(usuarioRepository.findByLogin("dr_carlos"))
                .thenReturn(Optional.of(medico));

        when(passwordEncoder.matches("123456", "senha-criptografada"))
                .thenReturn(true);

        when(jwtService.generateToken("dr_carlos", "MEDICO"))
                .thenReturn("token-com-perfil");

        authService.authenticate(loginRequest);

        verify(jwtService).generateToken("dr_carlos", "MEDICO");
    }

    @Test
    void deveGerarTokenComPerfilMaiusculo() {
        Paciente paciente = new Paciente();
        paciente.setId(2L);
        paciente.setLogin("joao");
        paciente.setSenha("senha-criptografada");

        when(usuarioRepository.findByLogin("joao"))
                .thenReturn(Optional.of(paciente));

        when(passwordEncoder.matches("123456", "senha-criptografada"))
                .thenReturn(true);

        when(jwtService.generateToken("joao", "PACIENTE"))
                .thenReturn("token");

        authService.authenticate(new LoginRequest("joao", "123456"));

        verify(jwtService).generateToken("joao", "PACIENTE");
    }

    @Test
    void deveRetornarTokenNoAuthResponse() {
        String tokenEsperado = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.token";

        when(usuarioRepository.findByLogin("dr_carlos"))
                .thenReturn(Optional.of(medico));

        when(passwordEncoder.matches("123456", "senha-criptografada"))
                .thenReturn(true);

        when(jwtService.generateToken("dr_carlos", "MEDICO"))
                .thenReturn(tokenEsperado);

        AuthResponse response = authService.authenticate(loginRequest);

        assertEquals(tokenEsperado, response.token());
    }

}
