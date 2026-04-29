package com.cookiesstore.pos.config;

import com.cookiesstore.pos.auth.PosPrincipal;
import com.cookiesstore.pos.auth.PosSessionKeys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class PosSessionAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        Long userId = (Long) request.getSession().getAttribute(PosSessionKeys.POS_AUTH_USER_ID);
        Long sourceId = (Long) request.getSession().getAttribute(PosSessionKeys.POS_AUTH_SOURCE_ID);
        String email = (String) request.getSession().getAttribute(PosSessionKeys.POS_AUTH_EMAIL);

        if (userId != null && sourceId != null) {
            PosPrincipal principal = new PosPrincipal(userId, sourceId, email == null ? "" : email);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_POS_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
