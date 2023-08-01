package net.aroder.TripTracker.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.aroder.TripTracker.models.PAX;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing PAX entities.
 */
@Repository
public interface PAXRepository extends JpaRepository<PAX, Long> {

    /**
     * Find a PAX entity by the given first name, surname, pick-up location, destination, and pick-up time.
     *
     * @param firstName      the first name of the PAX
     * @param surname        the surname of the PAX
     * @param pickUpLocation the pick-up location of the PAX
     * @param destination    the destination of the PAX
     * @param pickUpTime     the pick-up time of the PAX
     * @return an optional containing the PAX if found, or empty if not found
     */
    Optional<PAX> findByFirstNameAndSurnameAndPickUpLocationAndDestinationAndPickUpTime(String firstName, String surname, String pickUpLocation, String destination, Timestamp pickUpTime);

    /**
     * Find a PAX entity by the given first name, surname, and pick-up time.
     *
     * @param firstName  the first name of the PAX
     * @param surname    the surname of the PAX
     * @param pickUpTime the pick-up time of the PAX
     * @return an optional containing the PAX if found, or empty if not found
     */
    Optional<PAX> findByFirstNameAndSurnameAndPickUpTime(String firstName, String surname,Timestamp pickUpTime);


    Optional<PAX> findByFirstNameAndSurnameAndPickUpLocationAndDestinationAndPickUpTimeAfter(String firstName, String surname, String pickUpLocation, String destination, Timestamp pickUpTime);

    Optional<PAX> findByFirstNameAndSurnameAndPickUpTimeAndPickUpTimeAfter(String firstName, String surname,Timestamp pickUpTime, Timestamp currentTime);


    List<PAX> findByExpirationDateBefore(Timestamp timestamp);

}
