package net.aroder.TripTracker.repositories;

import java.util.List;
import java.util.Optional;

import net.aroder.TripTracker.models.DispatcherCompany;
import net.aroder.TripTracker.models.OrganizerCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.aroder.TripTracker.models.User;

/**
 * Repository interface for managing User entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

   /**
    * Retrieves a user by email.
    *
    * @param email the email of the user
    * @return an Optional containing the user with the specified email, or an empty Optional if not found
    */
   Optional<User> findByEmail(String email);

   List<User> findAllByOrganizerCompany(OrganizerCompany organizerCompany);

   List<User> findAllByDispatcherCompany(DispatcherCompany dispatcherCompany);
}
