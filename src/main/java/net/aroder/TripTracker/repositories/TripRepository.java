package net.aroder.TripTracker.repositories;

import net.aroder.TripTracker.models.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Trip entities.
 */
@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    /**
     * Retrieves a page of trips ordered by pick-up time in descending order.
     *
     * @param pageable the pageable information
     * @return a page of trips ordered by pick-up time
     */
    Page<Trip> findByOrderByPickUpTimeAsc(Pageable pageable);

    List<Trip> findByExpirationDateBefore(Timestamp timestamp);

    /**
     * Retrieves a page of trips from the specified regions ordered by pick-up time in descending order.
     *
     * @param region   the list of regions
     * @param pageable the pageable information
     * @return a page of trips from the specified regions ordered by pick-up time
     */
    Page<Trip> findByRegionInOrderByPickUpTimeAsc(List<Region> region, Pageable pageable);

    /**
     * Retrieves a page of trips belonging to the specified organizer company ordered by pick-up time in descending order.
     *
     * @param organizerCompany the organizer company
     * @param pageable         the pageable information
     * @return a page of trips belonging to the specified organizer company ordered by pick-up time
     */
    Page<Trip> findByOrganizerCompanyOrderByPickUpTimeAsc(OrganizerCompany organizerCompany, Pageable pageable);

    /**
     * Retrieves a trip based on certain properties.
     *
     * @param organizerCompany organizer company that owns the trip.
     * @param ship             ship related to the trip.
     * @param pickUpTime       time for pickup.
     * @param destination      destination for the trip.
     * @param pickUpLocation   pickup location for the trip.
     * @return An optional containing the trip or not.
     */
    Optional<Trip> findByOrganizerCompanyAndShipAndPickUpTimeAndDestinationAndPickUpLocationAndPoNumber(OrganizerCompany organizerCompany, Ship ship, Timestamp pickUpTime, Location destination, Location pickUpLocation,Long poNumber);

    @Query("SELECT t FROM Trip t " +
            "WHERE t.poNumber = :poNumber " +
            "AND (:organizerCompany IS NULL OR t.organizerCompany = :organizerCompany) " +
            "AND (:regionIds IS NULL OR t.region.id IN :regionIds) " +
            "AND t.status != 'cancelled' " +
            "ORDER BY t.pickUpTime ASC")
    List<Trip> findTripsForExport(
            @Param("poNumber") Long poNumber,
            @Param("organizerCompany") OrganizerCompany organizerCompany,
            @Param("regionIds") List<Long> regionIds);


    /**
     * Enables the use of specially formatted queries for pages.
     *
     * @return page of trips
     */

    @Query("SELECT t FROM Trip t " +
            "WHERE (:ship is null or t.ship = :ship) " +
            "AND (:poNumber is null or t.poNumber = :poNumber) " +
            "AND (:harbour is null or t.harbour = :harbour) " +
            "AND ((:hideCanceledTrips  = false) OR (:hideCanceledTrips = true AND t.status != 'canceled')) " +
            "AND (:organizerCompany is null or t.organizerCompany = :organizerCompany) " +
            "AND (:region is null or t.region IN :region) " +
            "AND (cast(:startDate as date) is null or t.pickUpTime > :startDate) " +
            "AND (cast(:endDate as date) is null or t.pickUpTime < :endDate) " +
            "ORDER BY t.pickUpTime " +
            "LIMIT :pageSize " +
            "OFFSET :offset")
    List<Trip> searchTripsPage(@Param("ship") Ship ship,
                               @Param("poNumber") Long poNumber,
                               @Param("harbour") Location harbour,
                               @Param("hideCanceledTrips") boolean hideCanceledTrips,
                               @Param("startDate") Timestamp startDate,
                               @Param("endDate") Timestamp endDate,
                               @Param("organizerCompany") OrganizerCompany organizerCompany,
                               @Param("region") List<Region> region,
                               @Param("pageSize") Integer pageSize,
                               @Param("offset") Integer offset
    );

    @Query("SELECT t FROM Trip t " +
            "WHERE (:ship is null or t.ship = :ship) " +
            "AND (:poNumber is null or t.poNumber = :poNumber) " +
            "AND (:harbour is null or t.harbour = :harbour) " +
            "AND ((:hideCanceledTrips  = false) OR (:hideCanceledTrips = true AND t.status != 'canceled')) " +
            "AND (:organizerCompany is null or t.organizerCompany = :organizerCompany) " +
            "AND (:region is null or t.region IN :region) " +
            "AND (cast(:startDate as date) is null or t.pickUpTime > :startDate) " +
            "AND (cast(:endDate as date) is null or t.pickUpTime < :endDate) " +
            "ORDER BY t.pickUpTime desc " +
            "LIMIT :pageSize " +
            "OFFSET :offset")
    List<Trip> searchTripsPageArchive(@Param("ship") Ship ship,
                                      @Param("poNumber") Long poNumber,
                                      @Param("harbour") Location harbour,
                                      @Param("hideCanceledTrips") boolean hideCanceledTrips,
                                      @Param("startDate") Timestamp startDate,
                                      @Param("endDate") Timestamp endDate,
                                      @Param("organizerCompany") OrganizerCompany organizerCompany,
                                      @Param("region") List<Region> region,
                                      @Param("pageSize") Integer pageSize,
                                      @Param("offset") Integer offset
    );


    @Query("SELECT t FROM Trip t " +
            "WHERE (:ship is null or t.ship = :ship) " +
            "AND (:poNumber is null or t.poNumber = :poNumber) " +
            "AND (:harbour is null or t.harbour = :harbour) " +
            "AND (:status is null or (t.status != 'canceled' AND t.status != 'completed')) " +
            "AND (:organizerCompany is null or t.organizerCompany = :organizerCompany) " +
            "AND (cast(:today as date) is null or t.pickUpTime > :today) " +
            "ORDER BY t.pickUpTime ")
    List<Trip> findTransferableTrips(@Param("ship") Ship ship,
                                     @Param("poNumber") Long poNumber,
                                     @Param("harbour") Location harbour,
                                     @Param("status") String status,
                                     @Param("today") Timestamp today,
                                     @Param("organizerCompany") OrganizerCompany organizerCompany
    );


    @Query("SELECT t FROM Trip t " +
            "WHERE (:ship is not null AND t.ship = :ship) " +
            "AND (:poNumber is null or t.poNumber = :poNumber) " +
            "AND (:harbour is not null AND t.harbour = :harbour) " +
            "AND (:pickUpLocation is not null AND t.pickUpLocation = :pickUpLocation) " +
            "AND (:destination is not null AND t.destination = :destination) " +
            "AND (t.status = 'created') " +
            "AND (:organizerCompany is not null AND t.organizerCompany = :organizerCompany) " +
            "AND (cast(:pickUpTime as date) is not null AND t.pickUpTime = :pickUpTime) ")
    Optional<Trip> findCompatibleTrip(@Param("ship") Ship ship,
                                      @Param("poNumber") Long poNumber,
                                      @Param("harbour") Location harbour,
                                      @Param("destination") Location destination,
                                      @Param("pickUpLocation") Location pickUpLocation,
                                      @Param("pickUpTime") Timestamp pickUpTime,
                                      @Param("organizerCompany") OrganizerCompany organizerCompany
    );


    @Query("SELECT t FROM Trip t " +
            "WHERE (t.region.id IN :regions) " +
            "AND ((:hideAssignedTrips = true AND t.driver is null) OR (:hideAssignedTrips = false)) " +
            "AND ((:hideCanceledTrips = true AND t.status != 'canceled') OR (:hideCanceledTrips = false)) " +
            "AND ((:hidePricedTrips = true AND (t.subContractorPrice = null AND t.externalPrice = null)) OR (:hidePricedTrips = false)) " +
            "AND ((:today is null) OR (:showPastTrips = true AND t.pickUpTime < :today) OR (:showPastTrips = false AND t.pickUpTime > :today)) " +
            "AND (:poNumber is null OR t.poNumber = :poNumber) " +
            "ORDER by t.pickUpTime " +
            "LIMIT :pageSize " +
            "OFFSET :offset")
    List<Trip> findDispatcherTrips(@Param("regions") List<Long> regions,
                                   @Param("hideAssignedTrips") Boolean hideAssignedTrips,
                                   @Param("hideCanceledTrips") Boolean hideCanceledTrips,
                                   @Param("showPastTrips") Boolean showPastTrips,
                                   @Param("today") Timestamp today,
                                   @Param("hidePricedTrips") Boolean hidePricedTrips,
                                   @Param("poNumber") Long poNumber,
                                   @Param("pageSize") Integer pageSize,
                                   @Param("offset") Integer offset);

    @Query("SELECT t FROM Trip t " +
            "WHERE ((:hideAssignedTrips = true AND t.driver is null) OR (:hideAssignedTrips = false)) " +
            "AND ((:hideCanceledTrips = true AND t.status != 'canceled') OR (:hideCanceledTrips = false)) " +
            "AND ((:showPastTrips = true AND t.pickUpTime < :today) OR (:showPastTrips = false AND t.pickUpTime > :today)) " +
            "AND ((:hidePricedTrips = true AND (t.subContractorPrice = null AND t.externalPrice = null)) OR (:hidePricedTrips = false)) " +
            "AND (:poNumber is null OR t.poNumber = :poNumber) " +
            "AND (:regionId is null OR t.region.id = :regionId) " +
            "ORDER by t.pickUpTime " +
            "LIMIT :pageSize " +
            "OFFSET :offset")
    List<Trip> findDispatcherTripsAdmin(@Param("hideAssignedTrips") Boolean hideAssignedTrips,
                                        @Param("hideCanceledTrips") Boolean hideCanceledTrips,
                                        @Param("showPastTrips") Boolean showPastTrips,
                                        @Param("today") Timestamp today,
                                        @Param("hidePricedTrips") Boolean hidePricedTrips,
                                        @Param("poNumber") Long poNumber,
                                        @Param("regionId") Long regionId,
                                        @Param("pageSize") Integer pageSize,
                                        @Param("offset") Integer offset);


    @Query("SELECT t FROM Trip t " +
            "WHERE ((:userType = 'admin' AND (t.adminReported = false OR t.adminReported is null)) " +
            "OR (:userType = 'organizer' AND (t.organizerReported = false OR t.organizerReported is null)) " +
            "OR (:userType = 'dispatch' AND (t.dispatchReported = false OR t.dispatchReported is null))) " +
            "AND ((t.status = 'completed') OR (t.status = 'canceled' AND t.cancelFee = true)) " +
            "AND (:ship is null OR t.ship = :ship) " +
            "AND (:poNumber is null OR t.poNumber = :poNumber) " +
            "AND (:harbour is null OR t.harbour = :harbour) " +
            "AND (:organizerCompany is null OR t.organizerCompany = :organizerCompany) " +
            "AND (cast(:startDate as date) is null or t.pickUpTime > :startDate) " +
            "AND (cast(:endDate as date) is null or t.pickUpTime < :endDate) " +
            "AND (:region is null or t.region.id in :region) " +
            "ORDER by t.pickUpTime " +
            "LIMIT :pageSize " +
            "OFFSET :offset")
    List<Trip> findUnreportedTrips(@Param("ship") Ship ship,
                                   @Param("userType") String userType,
                                   @Param("poNumber") Long poNumber,
                                   @Param("startDate") Timestamp startDate,
                                   @Param("endDate") Timestamp endDate,
                                   @Param("harbour") Location harbour,
                                   @Param("organizerCompany") OrganizerCompany organizerCompany,
                                   @Param("region") List<Long> region,
                                   @Param("pageSize") Integer pageSize,
                                   @Param("offset") Integer offset);

    @Query("SELECT t FROM Trip t " +
            "WHERE ((:userType = 'admin' AND (t.adminReported = false OR t.adminReported is null)) " +
            "OR (:userType = 'organizer' AND (t.organizerReported = false OR t.organizerReported is null)) " +
            "OR (:userType = 'dispatch' AND (t.dispatchReported = false OR t.dispatchReported is null))) " +
            "AND ((t.status = 'completed') OR (t.status = 'canceled' AND t.cancelFee = true)) " +
            "AND ((:ship is null) OR (t.ship = :ship)) " +
            "AND ((:organizerCompany is null) OR (t.organizerCompany = :organizerCompany)) " +
            "AND ((:regions is null) OR (t.region.id in :regions)) " +
            "AND ((:harbour is null) OR (t.harbour = :harbour)) " +
            "AND ((:poNumber is null) OR t.poNumber = :poNumber)" +
            "AND (cast(:startDate as date) is null or t.pickUpTime > :startDate) " +
            "AND (cast(:endDate as date) is null or t.pickUpTime < :endDate) " +
            "ORDER by t.pickUpTime")
    List<Trip> findUnreportedTripsFiltered(@Param("poNumber") Integer poNumber,
                                           @Param("userType") String userType,
                                           @Param("ship") Ship ship,
                                           @Param("harbour") Location harbour,
                                           @Param("organizerCompany") OrganizerCompany organizerCompany,
                                           @Param("regions") List<Long> regions,
                                           @Param("startDate") Timestamp startDate,
                                           @Param("endDate") Timestamp endDate);

}