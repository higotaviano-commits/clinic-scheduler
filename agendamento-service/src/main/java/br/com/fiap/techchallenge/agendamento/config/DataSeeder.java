package br.com.fiap.techchallenge.agendamento.config;

import br.com.fiap.techchallenge.agendamento.model.Enfermeiro;
import br.com.fiap.techchallenge.agendamento.model.Medico;
import br.com.fiap.techchallenge.agendamento.model.Paciente;
import br.com.fiap.techchallenge.agendamento.model.Usuario;
import br.com.fiap.techchallenge.agendamento.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.function.Supplier;

/** Popula o banco com um usuário de cada perfil (login/senha "senha123") para testes imediatos. */
@Configuration
@Profile("!test")
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String SENHA_DEMO = "senha123";

    @Bean
    public CommandLineRunner seedUsuariosDemo(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            criarSeNaoExistir(usuarioRepository, passwordEncoder, "medico", "Dra. Ana Souza", "medico@hospital.com", Medico::new);
            criarSeNaoExistir(usuarioRepository, passwordEncoder, "enfermeiro", "Enf. Bruno Lima", "enfermeiro@hospital.com", Enfermeiro::new);
            criarSeNaoExistir(usuarioRepository, passwordEncoder, "paciente", "Carlos Pereira", "paciente@hospital.com", Paciente::new);
        };
    }

    private void criarSeNaoExistir(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                                    String login, String nome, String email, Supplier<Usuario> fabrica) {
        if (usuarioRepository.existsByLogin(login)) {
            return;
        }
        Usuario usuario = fabrica.get();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setLogin(login);
        usuario.setSenha(passwordEncoder.encode(SENHA_DEMO));
        usuario.setLastModifiedAt(LocalDateTime.now());
        usuarioRepository.save(usuario);

        log.info("Usuário demo criado: login={} email={} senha={}", login, email, SENHA_DEMO);
    }
}
