package net.aroder.TripTracker.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import net.aroder.TripTracker.models.DTOs.AuthDTOs.*;
import net.aroder.TripTracker.models.DTOs.UserDTOs.PasswordUpdateRequest;
import net.aroder.TripTracker.models.DTOs.UserDTOs.UserUpdateRequest;
import net.aroder.TripTracker.models.Email;
import net.aroder.TripTracker.models.User;
import net.aroder.TripTracker.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The AuthService class handles operations related to user authentication
 */
@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserService userService;
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final EmailSender emailSender;
    private final List<String> allRoles = List.of("DRIVER", "ADMIN", "ORGANIZER", "DISPATCHER","MANAGER");
    private final String baseUrl;
    private final String realm;
    private final String clientId;
    private final String clientSecret;
    public AuthService(final UserService userService,
                       final RestTemplate restTemplate,
                       final UserRepository userRepository,
                       final EmailSender emailSender,
                       @Value("${keycloak.base-url}") final String baseUrl,
                       @Value("${keycloak.realm}") final String realm,
                       @Value("${keycloak.client-id}")final String clientId,
                       @Value("${keycloak.client-secret}")final String clientSecret) {
        this.userService = userService;
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
        this.emailSender = emailSender;
        this.baseUrl = baseUrl;
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    private static final String CLIENT_ID_KEY = "client_id";
    private static final String CLIENT_SECRET_KEY = "client_secret";
    private static final String REFRESH_TOKEN_KEY = "refresh_token";
    private static final String GRANT_TYPE_KEY = "grant_type";
    private static final String AUTHORIZATION_KEY = "Authorization";


    /**
     * Logs in a user using the provided login request.
     *
     * @param loginRequest The login request object containing the user's email and password.
     * @return The login response object containing the authentication token and other relevant information.
     */
    public LoginResponse login(LoginRequest loginRequest) {
        ResponseEntity<LoginResponse> response = sendLoginRequest(loginRequest);

        return response.getBody();
    }

    private ResponseEntity<LoginResponse> sendLoginRequest(LoginRequest loginRequest){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", this.clientId);
        map.add("client_secret", this.clientSecret);
        map.add("grant_type", "password");
        map.add("username", loginRequest.getEmail());
        map.add("password", loginRequest.getPassword());

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, headers);
        String loginUrl = this.baseUrl + "realms/" + this.realm + "/protocol/openid-connect/token";
        return restTemplate.postForEntity(loginUrl, httpEntity, LoginResponse.class);
    }

    /**
     * Signs a user out using refresh_token
     *
     * @param request TokenRequest containing refresh_token
     * @return message received on sign out.
     */
    public boolean signOut(TokenRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", this.clientId);
        map.add("client_secret", this.clientSecret);
        map.add(REFRESH_TOKEN_KEY, request.getToken());

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, headers);

        var signOutUrl = baseUrl + "/auth/realms/" + realm + "/protocol/openid-connect/logout";
        var response = restTemplate.postForEntity(signOutUrl, httpEntity, SignoutResponse.class);

        return response.getStatusCode().is2xxSuccessful();
    }

    /**
     * Refreshes authentication token
     *
     * @param token refresh token to use for refresh
     * @return LoginResponse containing new access_token and refresh_token.
     */
    public LoginResponse refresh(TokenRequest token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", this.clientId);
        map.add("client_secret", this.clientSecret);
        map.add("grant_type", REFRESH_TOKEN_KEY);
        map.add(REFRESH_TOKEN_KEY, token.getToken());

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, headers);
        String loginUrl = this.baseUrl + "realms/" + this.realm + "/protocol/openid-connect/token";
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(loginUrl, httpEntity, LoginResponse.class);

        return response.getBody();
    }

    public User registerNewUser(User request) throws JsonProcessingException {
        if (!request.isValid()) throw new IllegalArgumentException("One or more fields are not valid");

        HttpHeaders headers = this.getAdminHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String initialPassword = this.generateRandomString();

        SignupKeycloakRequest body = new SignupKeycloakRequest(
                request.getFirstName(),
                request.getSurname(),
                request.getEmail(),
                true,
                false,
                List.of(new KeycloakCredentials(
                        "password",
                        initialPassword,
                        false)));

        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = mapper.writeValueAsString(body);

        HttpEntity<String> httpEntity = new HttpEntity<>(jsonBody, headers);

        var signupUrl = baseUrl + "admin/realms/" + realm + "/users";
        var response = restTemplate.postForEntity(signupUrl, httpEntity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            String uid = this.getUserId(request.getEmail());

            User user = new User();
            user.setId(uid);
            user.setFirstName(request.getFirstName());
            user.setSurname(request.getSurname());
            user.setEmail(request.getEmail());
            this.sendVerificationEmail(uid);
            this.sendInitialPasswordEmail(initialPassword, request.getEmail());
            return user;
        } else {
            throw new IllegalAccessError();
        }
    }

    public User registerUser(User signUpRequest) throws JsonProcessingException {
        User newUser = this.registerNewUser(signUpRequest);
        User currentUser = userService.getCurrentUser();
        String role = null;

        if(userService.userIsAdmin()){
            newUser.setDispatcherCompany(signUpRequest.getDispatcherCompany());
            newUser.setOrganizerCompany(signUpRequest.getOrganizerCompany());
            newUser.setPhoneNumber(signUpRequest.getPhoneNumber());
            if(signUpRequest.getRoles().isEmpty()){
                throw new IllegalArgumentException("User must have at least one role");
            }else {
                role = signUpRequest.getRoles().get(0);
            }
        }else if(userService.userIsDispatcher()){
            newUser.setDispatcherCompany(currentUser.getDispatcherCompany());
            role = "DRIVER";
        }else if(userService.userIsManager()){
            newUser.setOrganizerCompany(currentUser.getOrganizerCompany());
            role = "ORGANIZER";
        }
        newUser = userService.saveUser(newUser);
        this.tryAddRole(newUser.getId(),role);
        return newUser;

    }

    public String getUserId(String username) throws JsonProcessingException {
        // Admin access
        HttpHeaders headers = this.getAdminHeaders();

        // Search for user
        var userDetailUrl = baseUrl + "admin/realms/" + realm + "/users?email=" + username;
        var userDetailResponse = restTemplate.exchange(userDetailUrl, HttpMethod.GET, new HttpEntity<>(headers),
                String.class);
        var body = userDetailResponse.getBody();

        // Check if user was found or not
        if (body == null) {
            throw new EntityNotFoundException("Did not find any users with username: " + username);
        }

        // Map response to object
        ObjectMapper mapper = new ObjectMapper();
        var userDetails = mapper.readValue(body, new TypeReference<List<UserDetails>>() {
        });

        return userDetails.get(0).getId();
    }


    /**
     * Returns a string for the authorization field in a http request in the format
     * "Bearer token"
     *
     * @param token the token to be used in the authorization field
     * @return a complete string for the value of the authorization field
     */
    private String getAuthorizationValue(String token) {
        return "Bearer " + token;
    }


    /**
     * Generates headers for http request with admin access
     *
     * @return http headers that can be used in a http request with admin access
     */
    private HttpHeaders getAdminHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(CLIENT_ID_KEY, this.clientId);
        map.add(CLIENT_SECRET_KEY, this.clientSecret);
        map.add(GRANT_TYPE_KEY, "client_credentials");

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, headers);

        var loginUrl = baseUrl + "realms/" + this.realm + "/protocol/openid-connect/token";
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(loginUrl, httpEntity,
                LoginResponse.class);
        var body = response.getBody();

        if (body == null) {
            throw new EntityNotFoundException("Failed to find the admin user");
        }

        headers.set(AUTHORIZATION_KEY, this.getAuthorizationValue(body.getAccess_token()));

        return headers;
    }

    /**
     * Adds a role to a user. Role is only applied if both users are in the same warehouse
     *
     * @param promotingUuid of the user to add the role to
     * @param role the role to be added
     * @throws JsonProcessingException if the role cannot be added.
     * @return the user that was promoted.
     */
    public User tryAddRole(String promotingUuid, String role) throws JsonProcessingException {
        User currentUser = userService.getCurrentUser();
        User promotedUser = userService.findById(promotingUuid);
        if(currentUser.getRoles().contains("ADMIN")){
            addRole(promotingUuid,role);
        }else if(currentUser.getRoles().contains("MANAGER") && (role.equals("MANAGER") || role.equals("ORGANIZER"))){
            addRole(promotingUuid, role);
        } else if(currentUser.getRoles().contains("DISPATCHER") && (role.equals("DISPATCHER") || role.equals("DRIVER"))){
            addRole(promotingUuid, role);
        }

        return promotedUser;
    }

    /**
     * Removes a role from a user. Role is only removed if both users are in the same warehouse.
     *
     * @param demotingUuid uuid of the user to remove role from.
     * @param role the role to remove.
     * @throws JsonProcessingException if the role cannot be added.
     */
    public User tryRemoveRole(String demotingUuid, String role) throws JsonProcessingException {

        User currentUser = userService.getCurrentUser();
        User demotedUser = userService.findById(demotingUuid);
        if(currentUser.getRoles().contains("ADMIN")){
            removeRole(demotingUuid,role);
        }else if(currentUser.getRoles().contains("MANAGER") && (role.equals("MANAGER") || role.equals("ORGANIZER"))){
            removeRole(demotingUuid, role);
        } else if(currentUser.getRoles().contains("DISPATCHER") && (role.equals("DISPATCHER") || role.equals("DRIVER"))){
            removeRole(demotingUuid, role);
        }
        return demotedUser;
    }

    /**
     * Adds a role to a user. Role is only applied if both users are in the same warehouse
     *
     * @param promotingUuid of the user to add the role to
     * @param role the role to be added
     */
    public void addRole(String promotingUuid, String role) throws JsonProcessingException {
        // Add admin token to headers
        HttpHeaders headers = this.getAdminHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Get role
        var roleUrl = baseUrl + "admin/realms/" + realm + "/roles/" + role;
        var response = restTemplate.exchange(roleUrl, HttpMethod.GET, new HttpEntity<>(headers), KeycloakRoleResponse.class);

        var body = new ObjectMapper().writeValueAsString(Collections.singletonList(response.getBody()));

        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);

        var url = baseUrl + "admin/realms/" + realm + "/users/" + promotingUuid + "/role-mappings/realm";
        var updateResponse = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);

        if (updateResponse.getStatusCode().is2xxSuccessful()) {
            this.userService.addRole(promotingUuid, role);
        }
    }

    public void removeRole(String demotingUuid, String role) throws JsonProcessingException {
        HttpHeaders headers = this.getAdminHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        var roleUrl = baseUrl + "admin/realms/" + realm + "/roles/" + role;
        var response = restTemplate.exchange(roleUrl, HttpMethod.GET, new HttpEntity<>(headers), KeycloakRoleResponse.class);

        var body = new ObjectMapper().writeValueAsString(Collections.singletonList(response.getBody()));

        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);

        var url = baseUrl + "admin/realms/" + realm + "/users/" + demotingUuid + "/role-mappings/realm";
        var updateResponse = restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, String.class);

        if (updateResponse.getStatusCode().is2xxSuccessful()) {
            this.userService.removeRole(demotingUuid, role);
        }
    }

    public User promoteUser(String userId) throws JsonProcessingException {
        User foundUser = userService.findById(userId);
        String newRole = this.determineNewRolePromotion(foundUser);
        if(!userService.userIsAdmin() && !userService.userIsManager() && !userService.userIsDispatcher()) throw new AccessDeniedException("You cannot access this user");
        if(userService.userIsManager() && foundUser.getOrganizerCompany().equals(userService.getCurrentUser().getOrganizerCompany()) && !newRole.equals("ADMIN")) {
            removeOldRoles(foundUser);
            return tryAddRole(userId,newRole);
        }else if(userService.userIsDispatcher() && foundUser.getDispatcherCompany().equals(userService.getCurrentUser().getDispatcherCompany()) && !newRole.equals("ADMIN")){
            removeOldRoles(foundUser);
            return tryAddRole(userId,newRole);
        }else if(userService.userIsAdmin()){
            removeOldRoles(foundUser);
            return tryAddRole(userId,newRole);
        }else throw new AccessDeniedException("You cannot modify this user");
    }

    private void removeOldRoles(User user) throws JsonProcessingException {
        for(String role : user.getRoles()){
            if(role.matches("[A-Z]+")){
                tryRemoveRole(user.getId(),role);
            }
        }
    }

    public String determineNewRolePromotion(User user){
        if(user.getRoles().contains("ADMIN")) throw new IllegalArgumentException("User cannot be promoted further");
        if(user.getRoles().contains("MANAGER") || user.getRoles().contains("DISPATCHER")) return "ADMIN";
        if(user.getRoles().contains("ORGANIZER")) return "MANAGER";
        if(user.getRoles().contains("DRIVER")) return "DISPATCHER";
        else throw new IllegalArgumentException("User does not have a valid role to be promoted");
    }

    public String determineNewRoleDemotion(User user){
        if(user.getRoles().contains("ADMIN")) throw new IllegalArgumentException("New role cannot be determined");
        if(user.getRoles().contains("MANAGER")) return "ORGANIZER";
        if(user.getRoles().contains("DISPATCHER")) return "DRIVER";
        else throw new IllegalArgumentException("User does not have a valid role to be demoted");
    }

    public User demoteUser(String userId) throws JsonProcessingException {
        User foundUser = userService.findById(userId);
        String newRole = determineNewRoleDemotion(foundUser);

        if(!userService.userIsAdmin() && !userService.userIsManager() && !userService.userIsDispatcher()) throw new AccessDeniedException("You cannot access this user");
        if(userService.userIsManager() && foundUser.getOrganizerCompany().equals(userService.getCurrentUser().getOrganizerCompany()) && !foundUser.getRoles().contains("ADMIN")) {
            removeOldRoles(foundUser);
            return tryAddRole(userId,newRole);
        }else if(userService.userIsDispatcher() && foundUser.getDispatcherCompany().equals(userService.getCurrentUser().getDispatcherCompany()) && !foundUser.getRoles().contains("ADMIN")){
            removeOldRoles(foundUser);
            return tryAddRole(userId,newRole);
        }else if(userService.userIsAdmin()){
            removeOldRoles(foundUser);
            return tryAddRole(userId,newRole);
        }else throw new AccessDeniedException("You cannot modify this user");
    }

    /**
     * Deletes a user
     *
     * @throws EntityNotFoundException if a user could not be found with the given
     *                                 email
     */
    public void deleteUser(String userId) {
        User userToDelete = userService.findById(userId);
        User currentUser = userService.getCurrentUser();
        if(userId.equals(userService.getCurrentUser().getId())) throw new AccessDeniedException("You cannot demote yourself");
        if((userService.userIsManager() && currentUser.getOrganizerCompany().equals(userToDelete.getOrganizerCompany()) && userToDelete.getRoles().contains("ORGANIZER"))||
                (userService.userIsDispatcher() && currentUser.getDispatcherCompany().equals(userToDelete.getDispatcherCompany()) && userToDelete.getRoles().contains("DRIVER"))|| userService.userIsAdmin()){

            HttpHeaders headers = this.getAdminHeaders();

            String url = baseUrl + "admin/realms/" + realm + "/users/" + userId;
            var response = restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                this.userService.deleteUser(userId);
            }
        }


    }


    public void sendVerificationEmail(String userId){
        HttpHeaders headers = this.getAdminHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String url = baseUrl + "admin/realms/"+realm+"/users/"+userId+"/send-verify-email";
        restTemplate.exchange(url,HttpMethod.PUT,new HttpEntity<>(headers),String.class);


    }

    public void sendForgotPasswordEmail(String emailAddress){
        User foundUser = userService.findUserByEmail(emailAddress).orElseThrow(EntityNotFoundException::new);

        HttpHeaders headers = this.getAdminHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String url = baseUrl + "admin/realms/"+realm+"/users/"+foundUser.getId()+"/execute-actions-email";
        restTemplate.exchange(url,HttpMethod.PUT,new HttpEntity<>(List.of("UPDATE_PASSWORD"),headers),String.class);

    }

    public String generateRandomString(){
        final var CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        final var LENGTH = 12;
        var random = new SecureRandom();

        var newPassword = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            newPassword.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return newPassword.toString();
    }

    public void sendInitialPasswordEmail(String initialPassword,String recipient){
        Email email = new Email(recipient, Email.Subject.INITIAL_PASSWORD,initialPassword);
        emailSender.sendMail(email);
    }

    public List<String> getAllRoles() throws IllegalAccessException {
        if(userService.userIsAdmin()) return allRoles;
        if(userService.userIsManager()) return List.of("ORGANIZER","MANAGER");
        if(userService.userIsDispatcher()) return List.of("DRIVER","DISPATCHER");
        else throw new IllegalAccessException("You do not have access to this resource");
    }

    public void updateCurrentUser(UserUpdateRequest updatedUser){
        User currentUser = userService.getCurrentUser();
        this.updateKeycloakUser(currentUser.getId(),updatedUser);
        if(updatedUser.getFirstName() != null && !updatedUser.getFirstName().equalsIgnoreCase(currentUser.getFirstName())){
            currentUser.setFirstName(updatedUser.getFirstName());
        }
        if(updatedUser.getSurname() != null && !updatedUser.getSurname().equalsIgnoreCase(currentUser.getSurname())){
            currentUser.setSurname(updatedUser.getSurname());
        }
        if(updatedUser.getPhoneNumber() != null && !updatedUser.getPhoneNumber().equalsIgnoreCase(currentUser.getPhoneNumber())){
            currentUser.setPhoneNumber(updatedUser.getPhoneNumber());
        }
        if(updatedUser.getEmail() != null && !updatedUser.getEmail().equalsIgnoreCase(currentUser.getEmail())){
            currentUser.setEmail(updatedUser.getEmail());
        }
        userRepository.save(currentUser);
    }

    public void updateKeycloakUser(String userId, UserUpdateRequest updatedUser){
        HttpHeaders adminHeaders = this.getAdminHeaders();
        adminHeaders.setContentType(MediaType.APPLICATION_JSON);

        Map<String,String> body = this.generateUserUpdateMap(updatedUser);
        if (body.size() == 0) throw new IllegalArgumentException("Nothing to update");

        String url = baseUrl + "admin/realms/"+realm+"/users/"+userId;
        ResponseEntity response = restTemplate.exchange(url,HttpMethod.PUT,new HttpEntity(body ,adminHeaders),String.class);

        if(response.getStatusCode().isError()) throw new HttpClientErrorException(response.getStatusCode());
    }


    private Map<String,String> generateUserUpdateMap(UserUpdateRequest updatedUser){
        Map<String,String> map = new HashMap<>();
        User currentUser = userService.getCurrentUser();

        if(updatedUser.getFirstName() != null && !updatedUser.getFirstName().equalsIgnoreCase(currentUser.getFirstName())){
            map.put("firstName", updatedUser.getFirstName());
        }
        if(updatedUser.getSurname() != null && !updatedUser.getSurname().equalsIgnoreCase(currentUser.getSurname())){
            map.put("lastName", updatedUser.getSurname());
        }
        if(updatedUser.getEmail() != null && !updatedUser.getEmail().equalsIgnoreCase(currentUser.getEmail())){
            map.put("email", updatedUser.getEmail());
        }
        return map;
    }

    public void changeUserPassword(PasswordUpdateRequest passwordUpdate){
        User currentUser = userService.getCurrentUser();
        ResponseEntity<LoginResponse> loginAttempt = this.sendLoginRequest(new LoginRequest(currentUser.getEmail(), passwordUpdate.getCurrentPassword()));
        if(!loginAttempt.getStatusCode().is2xxSuccessful()) throw new IllegalArgumentException("Wrong current password");

        HttpHeaders adminHeaders = this.getAdminHeaders();
        adminHeaders.setContentType(MediaType.APPLICATION_JSON);

        String url = baseUrl + "admin/realms/"+realm+"/users/"+currentUser.getId()+"/reset-password";
        ResponseEntity<String> response = restTemplate.exchange(url,HttpMethod.PUT,new HttpEntity<>(new KeycloakCredentials(
                "password",
                passwordUpdate.getNewPassword(),
                false),adminHeaders),String.class);

        if(response.getStatusCode().isError()) throw new HttpClientErrorException(response.getStatusCode());

    }
}
