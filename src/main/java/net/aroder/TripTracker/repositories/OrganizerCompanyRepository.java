package net.aroder.TripTracker.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.aroder.TripTracker.models.OrganizerCompany;

import java.util.Optional;

/**
 * Repository interface for managing OrganizerCompany entities.
 */
@Repository
public interface OrganizerCompanyRepository extends JpaRepository<OrganizerCompany, Long> {

    Optional<OrganizerCompany> findByName(String name);
}
