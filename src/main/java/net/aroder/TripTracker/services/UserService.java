package net.aroder.TripTracker.services;

import java.util.*;

import jakarta.persistence.EntityNotFoundException;
import net.aroder.TripTracker.exceptions.UserNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

import net.aroder.TripTracker.models.User;
import net.aroder.TripTracker.repositories.UserRepository;

/**
 * UserService handling any action affecting users.
 */
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(final UserRepository userRepository){
        this.userRepository = userRepository;
    }

    /**
     * Retrieves the information of a user
     *
     * @return found userinfo
     */
    public User getUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> foundUser = userRepository.findById(auth.getName());
        if (foundUser.isPresent()) {
            return foundUser.get();
        } else {
            Jwt credentials = (Jwt) auth.getCredentials();

            User user = new User();
            user.setId(auth.getName());
            user.setFirstName(credentials.getClaimAsString("given_name"));
            user.setSurname(credentials.getClaimAsString("family_name"));
            user.setEmail(credentials.getClaimAsString("email"));
            user.setRoles(new ArrayList<String>(credentials.getClaim("roles")).stream().filter(role -> role.matches("[A-Z]+")).collect(Collectors.toList()));
            userRepository.save(user);
            return user;
        }
    }

    /**
     * Retrieves the current authenticated user.
     *
     * @return currently authenticated user.
     */
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findById(auth.getName()).orElseThrow(() -> new UserNotFoundException(""));
    }

    /**
     * Retrieves the roles of the currently authenticated user.
     *
     * @return List of roles for the currently authenticated user.
     */
    public List<String> getRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
    }

    public boolean userIsAdmin() {
        return getRoles().contains("ROLE_ADMIN");
    }

    public boolean userIsManagerOrOrganizer() {
        return getRoles().contains("ROLE_ORGANIZER") || getRoles().contains("ROLE_MANAGER");
    }

    public boolean userIsManager() {
        return getRoles().contains("ROLE_MANAGER");
    }

    public boolean userIsDispatcher() {
        return getRoles().contains("ROLE_DISPATCHER");
    }

    public boolean userIsDriver() {
        return getRoles().contains("ROLE_DRIVER");
    }

    /**
     * Retrieves a list of all users in the database
     *
     * @return List of all found users.
     */
    public List<User> getAllUsers() {
        if (userIsAdmin()) {
            return userRepository.findAll();
        } else if (userIsManager() && getCurrentUser().getOrganizerCompany() != null) {
            return userRepository.findAllByOrganizerCompany(getCurrentUser().getOrganizerCompany());
        } else if (userIsDispatcher() && getCurrentUser().getDispatcherCompany() != null) {
            return userRepository.findAllByDispatcherCompany(getCurrentUser().getDispatcherCompany());
        } else {
            throw new AccessDeniedException("Does not have required role");
        }

    }

    public User saveUser(User newUser) {
        return userRepository.save(newUser);
    }

    public void addRole(String userId, String role) {
        User foundUser = userRepository.findById(userId).orElseThrow(EntityNotFoundException::new);
        List<String> roles = foundUser.getRoles();
        roles.add(role);
        foundUser.setRoles(roles);
        userRepository.save(foundUser);
    }

    public void removeRole(String userId, String role) {
        User foundUser = userRepository.findById(userId).orElseThrow(EntityNotFoundException::new);
        List<String> roles = foundUser.getRoles();
        roles.remove(role);
        foundUser.setRoles(roles);
        userRepository.save(foundUser);
    }

    public User findById(String userId) {
        return userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User could not be found"));
    }

    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User updateUser(String userId,User updatedUser) {
        if(!userId.equals(updatedUser.getId())) {
            throw new IllegalArgumentException("User id does not match");
        }
        User foundUser = userRepository.findById(updatedUser.getId()).orElseThrow(EntityNotFoundException::new);
        foundUser.setFirstName(updatedUser.getFirstName());
        foundUser.setSurname(updatedUser.getSurname());
        foundUser.setEmail(updatedUser.getEmail());
        foundUser.setRoles(updatedUser.getRoles());
        foundUser.setOrganizerCompany(updatedUser.getOrganizerCompany());
        foundUser.setDispatcherCompany(updatedUser.getDispatcherCompany());
        foundUser.setPhoneNumber(updatedUser.getPhoneNumber());


        return userRepository.save(foundUser);
    }

    public String getCurrentUserType(){
        if(userIsAdmin()){
            return "admin";
        }else if(userIsManagerOrOrganizer()){
            return "organizer";
        }else if(userIsDispatcher()){
            return "dispatch";
        }else throw new IllegalArgumentException("User type cannot be determined");
    }

    public void acceptTos(){
        User currentUser = getCurrentUser();
        currentUser.setTosAccepted(true);
        userRepository.save(currentUser);
    }




}
