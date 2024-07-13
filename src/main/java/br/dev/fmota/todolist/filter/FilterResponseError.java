package br.dev.fmota.todolist.filter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

@Getter
@Setter
@AllArgsConstructor
public class FilterResponseError {
    private int status;
    private String error;
    private String message;
    private String path;

    public void sendErrorResponse(HttpServletResponse response) throws IOException {
        response.setStatus(this.status);
        response.setContentType("application/json");
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(this));
    }
}
