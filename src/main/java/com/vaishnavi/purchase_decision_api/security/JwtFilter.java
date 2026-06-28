package com.vaishnavi.purchase_decision_api.security;


import com.vaishnavi.purchase_decision_api.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor

public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // step 1 — get the Authorization header
        String authHeader = request.getHeader("Authorization");

        // step 2 — if header is missing or doesn't start with "Bearer ", skip
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // pass to next filter
            return;
        }

        // step 3 — extract just the token (strip "Bearer ")
        String token = authHeader.substring(7);

        // step 4 — validate token
        if (!jwtUtil.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // step 5 — extract email from token
        String email = jwtUtil.extractEmail(token);

        // step 6 — load user from database
        // hint: userRepository.findByEmail(email)
        // if user not found, just skip (doFilter and return)

        // step 6 — load user from database
        var user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // step 7 — tell Spring Security who this user is
        // (code below — read and understand it)
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user,        // ← replace with your actual user variable
                        null,
                        List.of()    // no roles for now
                );
        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // step 8 — continue to the next filter / controller
        filterChain.doFilter(request, response);
    }
}