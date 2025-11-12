package com.example.mivoto.config;

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // Le da prioridad máxima a este filtro
public class CorsFilter implements Filter {

    private static final String FRONTEND_ORIGIN = "https://mi-voto-theta.vercel.app";
    private static final String ALLOWED_METHODS = "GET, POST, OPTIONS";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

        // Aquí ponemos los permisos explícitos
        response.setHeader("Access-Control-Allow-Origin", FRONTEND_ORIGIN);
        response.setHeader("Access-Control-Allow-Methods", ALLOWED_METHODS);
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        // Si es una petición OPTIONS (pre-vuelo), respondemos OK
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            // Si no, dejamos que la petición continúe
            chain.doFilter(req, res);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // No es necesario implementar esto
    }

    @Override
    public void destroy() {
        // No es necesario implementar esto
    }
}
