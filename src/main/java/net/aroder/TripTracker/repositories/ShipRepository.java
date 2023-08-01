package net.aroder.TripTracker.repositories;

import net.aroder.TripTracker.models.OrganizerCompany;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.aroder.TripTracker.models.Ship;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Ship entities.
 */
@Repository
public interface ShipRepository extends JpaRepository<Ship, Long> {

    /**
     * Retrieves a ship by its name.
     *
     * @param name the name of the ship
     * @return an optional ship if found, otherwise empty
     */
    Optional<Ship> findByName(String name);

    /**
     * Retrieves a page of ships with a name 'like' the given parameter.
     *
     * @param name name to search for.
     * @param pageable information regarding the page such as page size.
     * @return a page with ships like the given name.
     */
    Page<Ship> findAllByNameLikeIgnoreCaseOrderByName(String name, Pageable pageable);

    List<Ship> findByOrganizerCompanyOrderByName(OrganizerCompany organizerCompany);

}
