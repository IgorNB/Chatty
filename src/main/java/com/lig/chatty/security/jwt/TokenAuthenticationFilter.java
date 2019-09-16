package com.lig.chatty.security.jwt;

import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;


import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TokenAuthenticationFilter extends OncePerRequestFilter {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORIZATION_HEADER_VALUE_PREFIX = "Bearer ";
    private final TokenProviderService tokenProviderService;

    public TokenAuthenticationFilter(TokenProviderService tokenProviderService) {
        this.tokenProviderService = tokenProviderService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String httpJwt = getJwtFromHttpRequest(request);
            String websocketJwt = getJwtFromWebSocketRequest(request);
            String jwt = StringUtils.hasText(httpJwt) ? httpJwt : websocketJwt;

            if (StringUtils.hasText(jwt) && tokenProviderService.validateToken(jwt)) {
                Authentication authentication = tokenProviderService.getAuthentication(jwt);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {

            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private @Nullable String getJwtFromHttpRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(AUTHORIZATION_HEADER_VALUE_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private @Nullable String getJwtFromWebSocketRequest(HttpServletRequest request) {

        String bearerToken = request.getParameterMap().getOrDefault("access_token", new String[1])[0];
        if (StringUtils.hasText(bearerToken)) {
            return bearerToken;
        }

        return null;
    }
}
