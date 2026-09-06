package br.com.fiap.techchallenge.agendamento.service;

import br.com.fiap.techchallenge.agendamento.dto.request.CreateUsuarioRequest;
import br.com.fiap.techchallenge.agendamento.dto.response.UsuarioResponse;
import br.com.fiap.techchallenge.agendamento.model.Enfermeiro;
import br.com.fiap.techchallenge.agendamento.model.Medico;
import br.com.fiap.techchallenge.agendamento.model.Paciente;
import br.com.fiap.techchallenge.agendamento.model.Usuario;
import br.com.fiap.techchallenge.agendamento.repository.UsuarioRepository;
import br.com.fiap.techchallenge.agendamento.usecase.UsuarioUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UsuarioService implements UsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UsuarioResponse create(CreateUsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }
        if (usuarioRepository.existsByLogin(request.login())) {
            throw new IllegalArgumentException("Login já cadastrado");
        }

        Usuario usuario = buildUsuario(request);
        usuario.setLastModifiedAt(LocalDateTime.now());
        usuarioRepository.save(usuario);

        return UsuarioResponse.from(usuario);
    }

    private Usuario buildUsuario(CreateUsuarioRequest request) {
        Usuario usuario = switch (request.tipoUsuario().toUpperCase()) {
            case "MEDICO" -> new Medico();
            case "ENFERMEIRO" -> new Enfermeiro();
            case "PACIENTE" -> new Paciente();
            default -> throw new IllegalArgumentException(
                    "Tipo inválido. Use MEDICO, ENFERMEIRO ou PACIENTE"
            );
        };

        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setLogin(request.login());
        usuario.setSenha(passwordEncoder.encode(request.password()));

        return usuario;
    }
}
