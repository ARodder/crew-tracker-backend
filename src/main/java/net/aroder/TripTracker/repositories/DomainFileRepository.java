package net.aroder.TripTracker.repositories;

import net.aroder.TripTracker.models.DispatcherCompany;
import net.aroder.TripTracker.models.DomainFile;
import net.aroder.TripTracker.models.OrganizerCompany;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DomainFileRepository extends JpaRepository<DomainFile, Long> {

    Page<DomainFile> findAllByTypeOrderByCreatedAtDesc(String type, Pageable pageable);

    @Query("SELECT df FROM DomainFile df " +
            "WHERE df.type = :type " +
            "AND df.organizerCompany = :organizerCompany " +
            "AND df.classification = 'MANAGER' " +
            "ORDER BY df.createdAt " +
            "LIMIT :pageSize " +
            "OFFSET :offset")
    List<DomainFile> findPageForOrganizerCompany(@Param("type") String type,
                                                 @Param("organizerCompany") OrganizerCompany organizerCompany,
                                                 @Param("pageSize") Integer pageSize,
                                                 @Param("offset") Integer offset
    );

    @Query("SELECT df FROM DomainFile df " +
            "WHERE df.type = :type " +
            "AND df.dispatcherCompany = :dispatcherCompany " +
            "AND df.classification = 'DISPATCHER' " +
            "ORDER BY df.createdAt " +
            "LIMIT :pageSize " +
            "OFFSET :offset")
    List<DomainFile> findPageForDispatcherCompany(@Param("type") String type,
                                                  @Param("dispatcherCompany") DispatcherCompany dispatcherCompany,
                                                  @Param("pageSize") Integer pageSize,
                                                  @Param("offset") Integer offset
    );
}
