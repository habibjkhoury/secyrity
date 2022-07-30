package com.halkh.security.service;

import com.halkh.security.dto.request.UserLoginRequest;
import com.halkh.security.dto.request.UserRegisterRequest;
import com.halkh.security.entity.Role;
import com.halkh.security.entity.User;
import com.halkh.security.exception.UserNotFoundException;
import com.halkh.security.repository.RoleRepository;
import com.halkh.security.repository.UserRepository;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServer;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final AuthenticationManager authenticationManager;

    private final RoleRepository roleRepository;

    public User register(UserRegisterRequest userRegisterRequest){
        Role userRole = roleRepository.findByName("ROLE_USER");
        User user = new User();

        user.setUsername(userRegisterRequest.getUsername());
        user.setPassword(userRegisterRequest.getPassword());
        user.setFullName(userRegisterRequest.getFullName());
        user.setRoles(Collections.singletonList(userRole));
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        User newUser = userRepository.save(user);
        newUser.setPassword(null);

        return newUser;
    }

    public String login(UserLoginRequest userLoginRequest){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginRequest.getUsername(), userLoginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User fetchedUser = userRepository.findByUsername(userLoginRequest.getUsername()).orElseThrow(() -> new UserNotFoundException("Customer with email {%s} does not exist"));

        if (!bCryptPasswordEncoder.matches(userLoginRequest.getPassword(), fetchedUser.getPassword())) {
            return null;
        }
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        Map<String, Object> claims = new HashMap<>();

        claims.put( "username", userLoginRequest.getUsername());

        JwtBuilder jwt = Jwts.builder()
                .setSubject(userLoginRequest.getUsername())
                .setExpiration(Date.from(ZonedDateTime.now().plusMinutes(5).toInstant()))
                .setId(String.valueOf(fetchedUser.getId()))
                .setIssuedAt(new Date())
                .setClaims(claims)
                .signWith(signatureAlgorithm, "secret");

        return jwt!=null? jwt.compact() : null;
    }
}
