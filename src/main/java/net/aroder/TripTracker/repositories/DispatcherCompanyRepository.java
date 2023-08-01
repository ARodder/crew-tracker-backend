package net.aroder.TripTracker.repositories;

import net.aroder.TripTracker.models.DispatcherCompany;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DispatcherCompanyRepository extends JpaRepository<DispatcherCompany, Long> {


    Page<DispatcherCompany> findAllByNameLikeIgnoreCaseOrderByName(String name, Pageable pageable);

    Optional<DispatcherCompany> findByName(String name);
}
