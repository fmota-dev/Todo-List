package br.dev.fmota.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.BCrypt.Result;
import br.dev.fmota.todolist.users.User;
import br.dev.fmota.todolist.users.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String servletPath = request.getServletPath();

        if (servletPath.startsWith("/tasks")) {

            // Pegar a autenticação (usuario e senha)
            String authorization = request.getHeader("Authorization");
            if (authorization == null || !authorization.startsWith("Basic ")) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "Cabeçalho de autorização ausente ou malformado");
                return;
            }

            String authEncoded = authorization.substring("Basic".length()).trim();
            byte[] authDecode;
            try {
                authDecode = Base64.getDecoder().decode(authEncoded);
            } catch (IllegalArgumentException e) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Credenciais Base64 inválidas");
                return;
            }

            String[] credentials = new String(authDecode).split(":");
            if (credentials.length != 2) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Formato de credenciais inválido");
                return;
            }

            String username = credentials[0];
            String password = credentials[1];

            // Validar usuário
            User user = this.userRepository.findByUsername(username);
            if (user == null) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "Usuário não encontrado");
                return;
            }

            // Validar senha
            Result passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
            if (!passwordVerify.verified) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "Senha incorreta");
                return;
            }

            request.setAttribute("idUser", user.getId());
        }

        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        FilterResponseError errorResponse = new FilterResponseError(status, "Error", message, "");
        errorResponse.sendErrorResponse(response);
    }
}
