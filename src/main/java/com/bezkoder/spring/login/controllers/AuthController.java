package com.bezkoder.spring.login.controllers;

import java.util.Optional;

import javax.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bezkoder.spring.login.models.User;
import com.bezkoder.spring.login.payload.request.LoginRequest;
import com.bezkoder.spring.login.payload.request.SignupRequest;
import com.bezkoder.spring.login.payload.request.UpdateUserRequest;
import com.bezkoder.spring.login.payload.response.UserInfoResponse;
import com.bezkoder.spring.login.payload.response.MessageResponse;
import com.bezkoder.spring.login.repository.UserRepository;
import com.bezkoder.spring.login.security.jwt.JwtUtils;
import com.bezkoder.spring.login.security.services.UserDetailsImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @Autowired
  AuthenticationManager authenticationManager;

  @Autowired
  UserRepository userRepository;

  @Autowired
  PasswordEncoder encoder;

  @Autowired
  JwtUtils jwtUtils;

  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    Authentication authentication = authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
    );

    SecurityContextHolder.getContext().setAuthentication(authentication);

    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

    ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

    UserInfoResponse userInfoResponse = new UserInfoResponse(
      userDetails.getId(),
      userDetails.getUsername(),
      userDetails.getEmail(),
      userDetails.getFiliere(),
      userDetails.getPhone()
    );

    return ResponseEntity.ok()
      .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
      .body(userInfoResponse);
  }

  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
    }

    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
    }

    // Create new user's account
    User user = new User(
      signUpRequest.getUsername(),
      signUpRequest.getEmail(),
      encoder.encode(signUpRequest.getPassword())
    );

    // Set additional fields (filiere and phone)
    user.setFiliere(signUpRequest.getFiliere());
    user.setPhone(signUpRequest.getPhone());

    userRepository.save(user);

    return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
  }


  @GetMapping("/user/{userId}")
  public ResponseEntity<?> getUserInfo(@PathVariable Long userId) {
    Optional<User> userOptional = userRepository.findById(userId);

    if (userOptional.isPresent()) {
      User user = userOptional.get();
      UserInfoResponse userInfoResponse = new UserInfoResponse(user.getId(), user.getUsername(), user.getEmail(), user.getFiliere(), user.getPhone());
      return ResponseEntity.ok().body(userInfoResponse);
    } else {
      return ResponseEntity.notFound().build();
    }
  }
  @PutMapping("/user/{userId}")
  public ResponseEntity<?> updateUserInfo(
    @PathVariable Long userId,
    @Valid @RequestBody UpdateUserRequest updateUserRequest
  ) {
    Optional<User> userOptional = userRepository.findById(userId);

    if (userOptional.isPresent()) {
      User user = userOptional.get();
      
      // Update user information
      user.setUsername(updateUserRequest.getUsername());
      user.setEmail(updateUserRequest.getEmail());
      user.setFiliere(updateUserRequest.getFiliere());
      user.setPhone(updateUserRequest.getPhone());
      
      userRepository.save(user);

      UserInfoResponse userInfoResponse = new UserInfoResponse(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getFiliere(),
        user.getPhone()
      );
      
      return ResponseEntity.ok().body(userInfoResponse);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/signout")
  public ResponseEntity<?> logoutUser() {
    ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(new MessageResponse("You've been signed out!"));
  }
}
