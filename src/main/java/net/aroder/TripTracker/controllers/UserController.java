package net.aroder.TripTracker.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import net.aroder.TripTracker.mappers.UserMapper;
import net.aroder.TripTracker.models.DTOs.UserDTOs.PasswordUpdateRequest;
import net.aroder.TripTracker.models.DTOs.UserDTOs.UserDTO;
import net.aroder.TripTracker.models.DTOs.UserDTOs.UserUpdateRequest;
import net.aroder.TripTracker.services.AuthService;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import net.aroder.TripTracker.services.UserService;
import org.springframework.web.client.HttpClientErrorException;

@RestController
@RequestMapping(path = "/api/v1/users")
public class UserController {

    private Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AuthService authService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upload a single file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
            }),
    })
    public ResponseEntity getUserInfo() {
        try {
            return ResponseEntity.ok(userMapper.toUserDTO(userService.getUserInfo()));
        } catch (Exception e) {
            logger.error("Unable to get user info", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping(path = "/all")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_DISPATCHER') or hasRole('ROLE_MANAGER')")
    @Operation(summary = "Upload a single file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserDTO.class)))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
            }),
    })
    public ResponseEntity getAllUsers() {
        try {
            return ResponseEntity.ok(userMapper.toUserDTO(userService.getAllUsers()));
        } catch (Exception e) {
            logger.error("Unable to get all users", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping(path = "/roles")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_DISPATCHER') or hasRole('ROLE_MANAGER')")
    public ResponseEntity getAllRoles() {
        try {
            return ResponseEntity.ok(authService.getAllRoles());
        } catch (Exception e) {
            logger.error("Unable to get all roles", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping(path = "/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity updateUser(@PathVariable("userId") String userId, @RequestBody UserDTO userDTO) {
        try {
            userService.updateUser(userId, userMapper.userDtoToUser(userDTO));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Unable to update user", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(path = "/tos/accept")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity acceptTos(){
        try{
            userService.acceptTos();
            return ResponseEntity.accepted().build();
        }catch(Exception e){
            logger.error("Could not accept TOS ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping(path = "/update-account")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity updateThisUser(@RequestBody UserUpdateRequest updatedUser){
        try{
            authService.updateCurrentUser(updatedUser);
            return ResponseEntity.accepted().build();
        }catch(Exception e){
            logger.error("Update current user: ",e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PutMapping(path = "/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity changePassword(@RequestBody PasswordUpdateRequest passwordUpdate){
        try{
            authService.changeUserPassword(passwordUpdate);
            return ResponseEntity.accepted().build();
        }catch (IllegalArgumentException e){
            logger.error("Could not update password: ",e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (HttpClientErrorException e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getMessage());
        }catch(Exception e){
            logger.error("Could not update password: ",e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
