package net.aroder.TripTracker.repositories;

import net.aroder.TripTracker.models.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
/**
 * Repository interface for managing Location entities.
 */
@Repository
public interface LocationRepository extends JpaRepository<Location,Long> {

    /**
     * Find a location by its name.
     *
     * @param name the name of the location
     * @return an optional containing the location if found, or empty if not found
     */
    List<Location> findByNameIgnoreCase(String name);

    /**
     * Retrieves a page of locations with a name 'like' the given parameter.
     *
     * @param name name to search for.
     * @param pageable information regarding the page such as page size.
     * @return a page with locations like the given name.
     */
    Page<Location> findAllByNameLikeIgnoreCaseOrderByName(String name, Pageable pageable);
}
