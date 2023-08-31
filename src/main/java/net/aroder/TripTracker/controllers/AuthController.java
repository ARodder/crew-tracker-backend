package net.aroder.TripTracker.controllers;


import net.aroder.TripTracker.mappers.UserMapper;
import net.aroder.TripTracker.models.DTOs.AuthDTOs.*;
import net.aroder.TripTracker.models.DTOs.UserDTOs.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import net.aroder.TripTracker.services.AuthService;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URI;

@RestController
@RequestMapping(path = "/api/v1/authenticate")
public class AuthController {

    private final Logger logger = LoggerFactory.getLogger(AuthController.class);


    private final AuthService authService;
    private final UserMapper userMapper;

    public AuthController(final AuthService authService, final UserMapper userMapper) {
        this.authService = authService;
        this.userMapper = userMapper;

    }

    @PostMapping
    @Operation(summary = "Login to keycloak and retrieve access_token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
            }),
    })
    public ResponseEntity login(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping(path = "/refresh")
    @Operation(summary = "Refresh access_token using refresh_token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
            }),
    })
    public ResponseEntity refreshToken(@RequestBody TokenRequest token) {
        try {
            return ResponseEntity.ok(authService.refresh(token));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(path = "/signout")
    @Operation(summary = "Refresh access_token using refresh_token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = SignOutResponse.class))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
            }),
    })
    public ResponseEntity signout(@RequestBody TokenRequest tokenRequest) {
        try {
            return ResponseEntity.ok(authService.signOut(tokenRequest));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body("Logout unavailable");
        }
    }

    @PostMapping(path = "/new")
    @Operation(summary = "Register a new user ")
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_DISPATCHER')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = SignOutResponse.class))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
            }),
            @ApiResponse(responseCode = "409", description = "Conflict", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
            }),
    })
    public ResponseEntity registerUser(@RequestBody UserDTO signUpRequest) {
        try {
            return ResponseEntity.created(URI.create("/users/" + authService.registerUser(userMapper.userDtoToUser(signUpRequest)).getId())).build();
        } catch (HttpClientErrorException e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body("register unavailable");
        }
    }


    @GetMapping(path = "/promote")
    @Operation(summary = "Promotes a user to dispatcher")
    @PreAuthorize("hasRole('ROLE_DISPATCHER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = SignOutResponse.class))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
            }),
    })
    public ResponseEntity promoteUser(@RequestParam("userId") String userId) {
        try {
            return ResponseEntity.ok(userMapper.toUserDTO(authService.promoteUser(userId)));
        } catch (HttpClientErrorException e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body("promotion unavailable");
        }
    }
    @GetMapping(path = "/demote")
    @Operation(summary = "Demotes a user if possible")
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_DISPATCHER') ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = SignOutResponse.class))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
            }),
    })
    public ResponseEntity demoteUser(@RequestParam("userId") String userId) {
        try {
            return ResponseEntity.ok(userMapper.toUserDTO(authService.demoteUser(userId)));
        } catch (HttpClientErrorException e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().body("demotion unavailable");
        }
    }


    @DeleteMapping
    @Operation(summary = "Delete a user based on user ID")
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_DISPATCHER')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Accepted", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = SignOutResponse.class))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
            }),
    })
    public ResponseEntity deleteUser(@RequestParam("userId") String userId) {
        try {
            authService.deleteUser(userId);
            return ResponseEntity.accepted().build();
        } catch (HttpClientErrorException e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/forgot-password")
    public ResponseEntity forgotPassword(@RequestParam("email") String emailAddress){
        try{
            authService.sendForgotPasswordEmail(emailAddress);
            return ResponseEntity.noContent().build();
        }catch(Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

}
