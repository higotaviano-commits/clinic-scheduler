package br.com.fiap.techchallenge.agendamento.filter;

import br.com.fiap.techchallenge.agendamento.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtFilter jwtFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveProsseguirQuandoNaoHouverHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void deveProsseguirQuandoHeaderNaoComecaComBearer() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void deveExtrairTokenEAutenticarComTokenValido() throws ServletException, IOException {
        String token = "token-valido-jwt";
        String username = "usuario_teste";
        String role = "MEDICO";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isValid(token)).thenReturn(true);
        when(jwtService.extractUsername(token)).thenReturn(username);
        when(jwtService.extractRole(token)).thenReturn(role);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(username, SecurityContextHolder.getContext().getAuthentication().getName());
        assertTrue(SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_MEDICO")));

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void naoDeveAutenticarComTokenInvalido() throws ServletException, IOException {
        String token = "token-invalido-jwt";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isValid(token)).thenReturn(false);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(any());
        verify(jwtService, never()).extractRole(any());
    }

    @Test
    void deveExtrairTokenCorretamenteRemovendoBearerPrefix() throws ServletException, IOException {
        String token = "token-teste";
        String authHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.isValid(token)).thenReturn(true);
        when(jwtService.extractUsername(token)).thenReturn("usuario");
        when(jwtService.extractRole(token)).thenReturn("PACIENTE");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService).isValid(token);
        verify(jwtService).extractUsername(token);
        verify(jwtService).extractRole(token);
    }

    @Test
    void deveMapearRoleCorretamenteComPrefixoROLE() throws ServletException, IOException {
        String token = "token-teste";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isValid(token)).thenReturn(true);
        when(jwtService.extractUsername(token)).thenReturn("user");
        when(jwtService.extractRole(token)).thenReturn("ENFERMEIRO");

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertTrue(SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ENFERMEIRO")));
    }

    @Test
    void deveHandlearRoleNula() throws ServletException, IOException {
        String token = "token-sem-role";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isValid(token)).thenReturn(true);
        when(jwtService.extractUsername(token)).thenReturn("usuario");
        when(jwtService.extractRole(token)).thenReturn(null);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("usuario", SecurityContextHolder.getContext().getAuthentication().getName());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().isEmpty());
    }

    @Test
    void deveProsseguirFiltroApenasComBearerValido() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).isValid(any());
    }

    @Test
    void deveChamarFilterChainSempreAoFinal() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

}
