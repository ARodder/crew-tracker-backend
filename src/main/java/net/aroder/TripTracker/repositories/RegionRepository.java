package net.aroder.TripTracker.repositories;

import net.aroder.TripTracker.models.Region;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing Region entities.
 */
@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {

    Optional<Region> findByNameIgnoreCase(String name);

    Page<Region> findAllByNameLikeIgnoreCaseOrderByName(String name, Pageable pageable);
}
