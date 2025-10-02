package com.matekoncz.task_manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matekoncz.task_manager.exceptions.auth.WrongUsernameOrPasswordException;
import com.matekoncz.task_manager.exceptions.user.UserNotFoundException;
import com.matekoncz.task_manager.model.Credentials;
import com.matekoncz.task_manager.model.User;
import com.matekoncz.task_manager.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final String COOKIE_NAME = "JSESSIONID";

    private final UserService userService;
    private final ObjectMapper objectMapper;
    public AuthController(UserService userService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }
    @PostMapping("/login")
    public ResponseEntity<User> login(HttpServletRequest request, @RequestBody String credentialsJson) throws IOException, UserNotFoundException, WrongUsernameOrPasswordException {
        Credentials credentials = objectMapper.readValue(credentialsJson, Credentials.class);
        User user = userService.authenticate(credentials.getUsername(), credentials.getPassword());

        Authentication authentication =
            new UsernamePasswordAuthenticationToken(
                user,
                credentials.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        request.getSession(true).setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) throws IOException, UserNotFoundException, WrongUsernameOrPasswordException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            new CookieClearingLogoutHandler(COOKIE_NAME).logout(request, response, authentication);
        }
        request.getSession().invalidate(); 
        return ResponseEntity.ok().build();
    }
}