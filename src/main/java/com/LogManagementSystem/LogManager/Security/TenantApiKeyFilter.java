package com.LogManagementSystem.LogManager.Security;

import com.LogManagementSystem.LogManager.Entity.Role;
import com.LogManagementSystem.LogManager.Entity.Tenant;
import com.LogManagementSystem.LogManager.Repository.TenantRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TenantApiKeyFilter extends OncePerRequestFilter {

    private final TenantRepository tenantRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String apiKey = request.getHeader("X-Tenant-Api-Key");
        if(apiKey == null){
            filterChain.doFilter(request, response);
            return;
        }
        Optional<Tenant> tenantOptional = tenantRepository.findByApiTokenHash(apiKey);
        if(tenantOptional.isEmpty()){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid Tenant API Key");
            return;
        }
        Tenant tenant = tenantOptional.get();

        UsernamePasswordAuthenticationToken tenantAuth =
                new UsernamePasswordAuthenticationToken(
                        tenant.getCompanyName(),
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority(Role.TENANT.name()))
                );
        tenantAuth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(tenantAuth);

        request.setAttribute("tenantId", tenant.getId());

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){
        String path = request.getServletPath();
        if(path.startsWith("/websocket-connect") ||
                path.startsWith("/actuator") ||
                path.equals("/") ||
                path.endsWith(".html") ||
                path.endsWith(".css") ||
                path.endsWith(".js") ||
                path.endsWith(".ico")){
            return true;
        }
        return !path.startsWith("/api/v1/ingest");
    }
}
