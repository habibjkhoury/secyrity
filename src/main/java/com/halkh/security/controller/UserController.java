package com.halkh.security.controller;

import com.halkh.security.dto.request.UserLoginRequest;
import com.halkh.security.dto.request.UserRegisterRequest;
import com.halkh.security.entity.User;
import com.halkh.security.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@Slf4j
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterRequest userRegisterRequest) {

        log.debug(" ============= NEW USER REGISTRATION =============");

        User newUser = userService.register(userRegisterRequest);

        if(newUser == null){
            log.error("Error Creating New User");
            return new ResponseEntity<>("Error Creating New User", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(newUser, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserLoginRequest userLoginRequest) {

        log.debug(String.format(" ============= %s Login =============", userLoginRequest.getUsername()));
        String token = userService.login(userLoginRequest);

        if(token == null){
            return new ResponseEntity<>("Unauthorized access", HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    @GetMapping("/secured")
    public ResponseEntity<String> secured() {
        return new ResponseEntity<>("Welcome to Secured App", HttpStatus.OK);
    }

}