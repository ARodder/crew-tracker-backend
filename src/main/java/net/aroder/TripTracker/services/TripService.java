package net.aroder.TripTracker.services;

import jakarta.persistence.EntityNotFoundException;
import net.aroder.TripTracker.models.*;
import net.aroder.TripTracker.models.DTOs.DispatchSearchObject;
import net.aroder.TripTracker.models.DTOs.FileDownloadObject;
import net.aroder.TripTracker.models.DTOs.SearchObject;
import net.aroder.TripTracker.models.DTOs.TripDTOs.ManualTripDTO;
import net.aroder.TripTracker.models.DTOs.UnreportedTripSearchObject;
import net.aroder.TripTracker.repositories.*;
import net.aroder.TripTracker.util.DateUtil;
import net.aroder.TripTracker.util.FileWritingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TripService handles any action applied to a trip, such as
 * change, adding, removing and retrieving trips
 */
@Service
public class TripService {
    private final Integer PAGE_SIZE = 15;
    private final List<String> tripStages = List.of("created", "assigned", "in_progress", "completed", "cancelled");
    private static final Logger logger = LoggerFactory.getLogger(TripService.class);
    private final Double margin;
    private final RegionRepository regionRepository;
    private final FileStorageService fileStorageService;
    private final FileWritingUtil fileWritingUtil;
    private final TripRepository tripRepository;
    private final UserService userService;
    private final LocationService locationService;
    private final PAXRepository paxRepository;
    private final ShipRepository shipRepository;
    private final OrganizerCompanyService organizerCompanyService;
    private final ShipService shipService;

    public TripService(final TripRepository tripRepository,
                       final UserService userService,
                       final LocationService locationService,
                       final PAXRepository paxRepository,
                       final ShipRepository shipRepository,
                       @Value("${margin}") final Double margin,
                       final RegionRepository regionRepository,
                       final FileStorageService fileStorageService,
                       final FileWritingUtil fileWritingUtil,
                       final ShipService shipService,
                       final OrganizerCompanyService organizerCompanyService) {
        this.tripRepository = tripRepository;
        this.userService = userService;
        this.locationService = locationService;
        this.paxRepository = paxRepository;
        this.shipRepository = shipRepository;
        this.margin = margin;
        this.regionRepository = regionRepository;
        this.fileStorageService = fileStorageService;
        this.fileWritingUtil = fileWritingUtil;
        this.shipService = shipService;
        this.organizerCompanyService = organizerCompanyService;
    }

    /**
     * Retrieves a page of trips, filtering trips
     * based on roles.
     *
     * @param pageNum number of the page of trips.
     * @return List of trips containing the given page.
     */
    public List<Trip> getTripPage(Integer pageNum) {
        List<String> roles = userService.getRoles();
        User currentUser = userService.getCurrentUser();
        Pageable pageRequest = PageRequest.of(pageNum, PAGE_SIZE);

        String ADMIN_ROLE_KEY = "ROLE_ADMIN";
        String MANAGER_ROLE_KEY = "ROLE_MANAGER";
        String ORGANIZER_ROLE_KEY = "ROLE_ORGANIZER";
        String DISPATCHER_ROLE_KEY = "ROLE_DISPATCHER";
        if (roles.contains(ADMIN_ROLE_KEY)) {
            return tripRepository.findByOrderByPickUpTimeAsc(pageRequest).toList();
        } else if (roles.contains(MANAGER_ROLE_KEY) || roles.contains(ORGANIZER_ROLE_KEY)) {
            return tripRepository.findByOrganizerCompanyOrderByPickUpTimeAsc(currentUser.getOrganizerCompany(), pageRequest).toList();
        } else if (roles.contains(DISPATCHER_ROLE_KEY)) {
            return tripRepository.findByRegionInOrderByPickUpTimeAsc(currentUser.getDispatcherCompany().getRegions(), pageRequest).toList();
        } else {
            throw new IllegalArgumentException("User missing role or company");
        }
    }

    public Trip findTripById(Long id) {
        return tripRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Trip not found"));
    }

    /**
     * Creates trips based on the provided list of PAX objects.
     *
     * @param paxList The list of PAX objects representing the bookings to create trips for.
     */
    public void createTrips(List<PAX> paxList, OrganizerCompany organizerCompany) {
        paxList = new ArrayList<>(paxList);
        int paxSkipped = 0;
        while (paxList.size() > 0) {
            PAX passenger = paxList.get(0);
            paxList.remove(0);
            Trip trip;
            try {
                trip = this.findOrCreateTrip(passenger, organizerCompany);
            } catch (Exception e) {
                paxSkipped++;
                logger.error("Failed generating trip ", e);
                continue;
            }

            List<PAX> filteredPax = paxList.stream()
                    .filter((pax) -> pax.getDestination().equalsIgnoreCase(trip.getDestination().getName())
                            && pax.getShip().equals(trip.getShip())
                            && pax.getPickUpLocation().equalsIgnoreCase(trip.getPickUpLocation().getName())
                            && pax.getPickUpTime().compareTo(trip.getPickUpTime()) == 0).toList();

            passenger.setTrip(trip);
            paxRepository.save(passenger);

            if (filteredPax.size() > 0) {
                trip.addPassengers(filteredPax);
                filteredPax.forEach(p -> {
                    p.setTrip(trip);
                    paxRepository.save(p);
                });
                paxList.removeAll(filteredPax);
            }
        }
    }

    public Trip getTrip(Long id) {
        return tripRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Trip not found"));
    }

    public Trip findOrCreateTrip(PAX passenger, OrganizerCompany organizerCompany) {
        return tripRepository.findCompatibleTrip(
                passenger.getShip(),
                passenger.getPoNumber(),
                passenger.getHarbour(),
                locationService.determineLocation(passenger.getDestination()),
                locationService.determineLocation(passenger.getPickUpLocation()),
                passenger.getPickUpTime(),
                organizerCompany
        ).orElse(createTripFromPax(passenger, organizerCompany));
    }

    public PAX determineNewTrip(Trip oldTrip, PAX pax) {
        Optional<Trip> matchingTrip = tripRepository
                .findByOrganizerCompanyAndShipAndPickUpTimeAndDestinationAndPickUpLocationAndPoNumber(
                        oldTrip.getOrganizerCompany(),
                        pax.getShip(),
                        pax.getPickUpTime(),
                        locationService.determineLocation(pax.getDestination()),
                        locationService.determineLocation(pax.getPickUpLocation()),
                        pax.getPoNumber());
        if (matchingTrip.isPresent()) {
            pax.setTrip(matchingTrip.get());
        } else {
            Trip newTrip = oldTrip.clone();
            newTrip.setStatus("created");
            newTrip.setDestination(locationService.determineLocation(pax.getDestination()));
            newTrip.setPickUpLocation(locationService.determineLocation(pax.getPickUpLocation()));
            newTrip.setPickUpTime(pax.getPickUpTime());
            newTrip.addPassengerRemark(pax.getRemarks());

            tripRepository.save(newTrip);
            pax.setTrip(newTrip);
        }
        return pax;
    }

    public List<Trip> searchTrips(SearchObject searchObject, Integer pageNum) throws AccessDeniedException {
        User currentUser = userService.getCurrentUser();
        if (currentUser.getOrganizerCompany() == null
                && currentUser.getDispatcherCompany() == null
                && !userService.userIsAdmin())
            throw new AccessDeniedException("User not allowed to search");
        Calendar startDate = null;
        Calendar endDate = null;
        if (searchObject.getDate() != null) {
            startDate = DateUtil.toCalendar(searchObject.getDate());
            endDate = DateUtil.toCalendar(searchObject.getDate());
            startDate.set(Calendar.HOUR_OF_DAY, 0);
            startDate.set(Calendar.MINUTE, 0);

            endDate.set(Calendar.HOUR_OF_DAY, 23);
            endDate.set(Calendar.MINUTE, 59);
        }
        Ship foundShip = shipRepository.findByName(searchObject.getShipName()).orElse(null);

        Location foundLocation = locationService.determineLocation(searchObject.getHarbour());

        List<Trip> searchResult;
        DispatcherCompany dispatcherCompany = currentUser.getDispatcherCompany();

        // List<Long> regionIds = dispatcherCompany != null && dispatcherCompany.getRegions().size() > 0 ? dispatcherCompany.getRegions().stream().map(Region::getId).toList() : null;
        List<Region> regions = null;
        if (dispatcherCompany != null && dispatcherCompany.getRegions().size() > 0) {
            regions = new ArrayList<>(dispatcherCompany.getRegions());
        }
        System.out.println("regionIds = " + (regions != null && regions.size() > 0 ? regions.getClass() :""));
        if (searchObject.getArchiveMode() == null || !searchObject.getArchiveMode()) {
            searchResult = tripRepository.searchTripsPage(foundShip,
                    searchObject.getPoNumber(),
                    foundLocation,
                    searchObject.getHideCanceledTrips() != null ? searchObject.getHideCanceledTrips() : false,
                    startDate != null ? new Timestamp(startDate.getTimeInMillis()) : new Timestamp(System.currentTimeMillis()),
                    endDate != null ? new Timestamp(endDate.getTimeInMillis()) : null,
                    currentUser.getOrganizerCompany(),
                    regions,
                    PAGE_SIZE, PAGE_SIZE * pageNum);
        } else {
            searchResult = tripRepository.searchTripsPageArchive(foundShip,
                    searchObject.getPoNumber(),
                    foundLocation,
                    searchObject.getHideCanceledTrips() != null ? searchObject.getHideCanceledTrips() : false,
                    startDate != null ? new Timestamp(startDate.getTimeInMillis()) : null,
                    endDate != null ? new Timestamp(endDate.getTimeInMillis()) : new Timestamp(System.currentTimeMillis()),
                    currentUser.getOrganizerCompany(),
                    regions,
                    PAGE_SIZE, PAGE_SIZE * pageNum);

        }
        return searchResult;
    }

    public Trip cancelTrip(Long tripId) throws AccessDeniedException {
        Trip trip = tripRepository.findById(tripId).orElseThrow(EntityNotFoundException::new);
        if (!((userService.userIsManagerOrOrganizer() && trip.getOrganizerCompany().equals(userService.getCurrentUser().getOrganizerCompany()))
                || userService.userIsAdmin())) throw new AccessDeniedException("Not accessible by user");

        trip.setStatus("canceled");

        trip.getPassengers().forEach((passenger) -> {
            passenger.setStatus("canceled");
            paxRepository.save(passenger);
        });
        long timeUntilTrip = Duration.between(LocalDateTime.now(), trip.getPickUpTime().toLocalDateTime()).toHours();
        if (timeUntilTrip < 2) {
            trip.setDriverRemarks("Cancellation fee");
            trip.setCancelFee(true);
        }
        return tripRepository.save(trip);
    }

    public void editTrip(Trip newTrip) throws AccessDeniedException {
        Trip oldTrip = tripRepository.findById(newTrip.getId()).orElseThrow(EntityNotFoundException::new);
        if (!(userService.userIsAdmin() || (userService.userIsManagerOrOrganizer() && userService.getCurrentUser().getOrganizerCompany().equals(oldTrip.getOrganizerCompany()))))
            throw new AccessDeniedException("Not accessible by user");
        oldTrip.setPickUpLocation(locationService.determineLocation(newTrip.getPickUpLocation().getName()));
        oldTrip.setDestination(locationService.determineLocation(newTrip.getDestination().getName()));
        oldTrip.setPickUpTime(newTrip.getPickUpTime());
        List<PAX> newPax = newTrip.getPassengers();
        newPax.forEach((passenger) -> {
            if (passenger.getId() == null) {
                passenger.setStatus(oldTrip.getStatus());
            }
            passenger.setPickUpTime(oldTrip.getPickUpTime());
            passenger.setPickUpLocation(oldTrip.getPickUpLocation().getName());
            passenger.setDestination(oldTrip.getDestination().getName());
            passenger.setPoNumber(oldTrip.getPoNumber());
            passenger.setHarbour(oldTrip.getHarbour());
            passenger.setShip(oldTrip.getShip());
            passenger.setTrip(oldTrip);

            paxRepository.save(passenger);
        });

        tripRepository.save(oldTrip);


    }

    public Trip createTripFromPax(PAX passenger, OrganizerCompany organizerCompany) {
        Date expirationDate = new Date();
        expirationDate = DateUtil.addMonths(expirationDate, 30);

        Trip trip = new Trip();
        trip.setStatus("created");
        trip.setOrganizerCompany(organizerCompany);
        trip.setDestination(locationService.determineLocation(passenger.getDestination()));
        trip.setPickUpLocation(locationService.determineLocation(passenger.getPickUpLocation()));
        trip.setPickUpTime(passenger.getPickUpTime());
        trip.setShip(passenger.getShip());
        trip.setPoNumber(passenger.getPoNumber());
        trip.setHarbour(passenger.getHarbour());
        trip.setExpirationDate(new Timestamp(expirationDate.getTime()));
        try {
            trip.setRegion(locationService.determineRegion(passenger.getHarbour()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        trip.setImmigration(passenger.isImmigration());

        return tripRepository.save(trip);
    }

    public List<Trip> findTransferableTrips(Long paxId) throws AccessDeniedException {
        PAX passenger = paxRepository.findById(paxId).orElseThrow(EntityNotFoundException::new);
        Trip oldTrip = passenger.getTrip();
        if (oldTrip == null) throw new EntityNotFoundException();
        if (!(userService.userIsAdmin() || (userService.userIsManagerOrOrganizer() && userService.getCurrentUser().getOrganizerCompany().equals(oldTrip.getOrganizerCompany()))))
            throw new AccessDeniedException("Not accessible by user");

        List<Trip> transferableTrips = tripRepository.findTransferableTrips(passenger.getShip(), passenger.getPoNumber(), passenger.getHarbour(), passenger.getStatus(), new Timestamp(System.currentTimeMillis()), passenger.getTrip().getOrganizerCompany());
        transferableTrips.remove(oldTrip);
        return transferableTrips;
    }

    public void transferPax(Long paxId, Long tripId) throws AccessDeniedException {
        PAX passenger = paxRepository.findById(paxId).orElseThrow(() -> new EntityNotFoundException("Passenger not found"));
        Trip oldTrip = passenger.getTrip();
        Trip newTrip = tripRepository.findById(tripId).orElseThrow(() -> new EntityNotFoundException("Trip not found"));
        if (oldTrip == null) throw new EntityNotFoundException();
        if (!(userService.userIsAdmin() || (userService.userIsManagerOrOrganizer() && userService.getCurrentUser().getOrganizerCompany().equals(oldTrip.getOrganizerCompany()) && userService.getCurrentUser().getOrganizerCompany().equals(newTrip.getOrganizerCompany()))))
            throw new AccessDeniedException("Not accessible by user");

        passenger.setStatus(newTrip.getStatus());
        passenger.setPickUpTime(newTrip.getPickUpTime());
        passenger.setPickUpLocation(newTrip.getPickUpLocation().getName());
        passenger.setDestination(newTrip.getDestination().getName());
        passenger.setPoNumber(newTrip.getPoNumber());
        passenger.setHarbour(newTrip.getHarbour());
        passenger.setShip(newTrip.getShip());
        passenger.setTrip(newTrip);

        paxRepository.save(passenger);

        if (oldTrip.getPassengers().size() < 1) {
            if (Duration.between(oldTrip.getPickUpTime().toLocalDateTime(), LocalDateTime.now()).toHours() > 2) {
                tripRepository.delete(oldTrip);
            } else {
                oldTrip.setStatus("canceled");
                oldTrip.setDriverRemarks("Cancellation fee");
                tripRepository.save(oldTrip);
            }
        }
    }

    public List<Trip> getDispatchTrips(DispatchSearchObject searchObject) throws AccessDeniedException {
        User currentUser = userService.getCurrentUser();
        Region foundRegion = regionRepository.findByNameIgnoreCase(searchObject.getRegion()).orElse(null);
        Integer pageSize = 20;
        Integer offset = pageSize * (searchObject.getPageNum() != null && searchObject.getPageNum() != 0 ? searchObject.getPageNum() : 0);
        if (userService.userIsAdmin()) {
            return tripRepository.findDispatcherTripsAdmin(searchObject.getHideAssignedTrips(),
                    searchObject.getHideCanceledTrips(),
                    searchObject.getShowPastTrips(),
                    new Timestamp(System.currentTimeMillis()),
                    searchObject.getHidePricedTrips(),
                    searchObject.getPoNumber() != null && searchObject.getPoNumber() != 0 ? searchObject.getPoNumber() : null,
                    foundRegion != null ? foundRegion.getId() : null,
                    pageSize,
                    offset);
        } else if (userService.userIsDispatcher()) {
            return tripRepository.findDispatcherTrips(currentUser.getDispatcherCompany().getRegions().stream().map(Region::getId).toList(),
                    searchObject.getHideAssignedTrips(),
                    searchObject.getHideCanceledTrips(),
                    searchObject.getShowPastTrips(),
                    new Timestamp(System.currentTimeMillis()),
                    searchObject.getHidePricedTrips(),
                    searchObject.getPoNumber() != null && searchObject.getPoNumber() != 0 ? searchObject.getPoNumber() : null,
                    pageSize,
                    offset);
        } else throw new AccessDeniedException("Not accessible by user");
    }

    public void assignDriver(Long tripId, String driverId) {
        Trip trip = tripRepository.findById(tripId).orElseThrow(() -> new EntityNotFoundException("Trip could not be found"));
        User driver = userService.findById(driverId);
        Timestamp twoHoursAgo = Timestamp.valueOf(LocalDateTime.now().minusHours(2));
        if (trip.getPickUpTime().before(twoHoursAgo))
            throw new IllegalArgumentException("can no longer assign a driver to this trip");

        //TODO:Add email notification
        if (userService.userIsAdmin() && driver.getDispatcherCompany().getRegions().contains(trip.getRegion())) {
            trip.setDriver(driver);
        } else if (userService.userIsDispatcher() && userService.getCurrentUser().getDispatcherCompany().equals(driver.getDispatcherCompany())
                && userService.getCurrentUser().getDispatcherCompany().getRegions().contains(trip.getRegion())) {
            trip.setDriver(driver);
        } else throw new IllegalArgumentException("You cannot assign this driver to this trip");
        if (trip.getStatus().equals("created")) {
            trip.setStatus("assigned");
        }
        tripRepository.save(trip);
    }

    public void assignMultipleDrivers(List<Long> tripIds, String driverId) {
        tripIds.forEach(tripId -> assignDriver(tripId, driverId));
    }

    public Trip setPrice(Long tripId, Double price) throws IllegalAccessException {
        Trip foundTrip = tripRepository.findById(tripId).orElseThrow(() -> new EntityNotFoundException("Could not find trip"));
        if (!userService.userIsAdmin() && !userService.userIsDispatcher())
            throw new IllegalAccessException("You cannot access this function");
        if (!userService.userIsAdmin() && !userService.getCurrentUser().getDispatcherCompany().getRegions().contains(foundTrip.getRegion()))
            throw new IllegalAccessException("Trip is not in your region");
        foundTrip.setSubContractorPrice(price);
        if (margin != null) {
            foundTrip.setExternalPrice(price * (1 + margin));
        } else {
            foundTrip.setExternalPrice(price * 1.05);
        }
        return tripRepository.save(foundTrip);
    }

    public List<Trip> setMultipleTripPrice(List<Long> tripIds, Double price) {
        List<Trip> trips = new ArrayList<>();
        tripIds.forEach(tripId -> {
            try {
                trips.add(setPrice(tripId, price));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        return trips;
    }

    public void addDriverRemark(Long tripId, String driverRemark) throws IllegalAccessException {
        Trip foundTrip = tripRepository.findById(tripId).orElseThrow(() -> new EntityNotFoundException("Could not find trip"));
        if (!userService.userIsAdmin() && !userService.userIsDispatcher() && !userService.userIsDriver())
            throw new IllegalAccessException("You cannot access this function");
        if (!userService.userIsAdmin() && !userService.getCurrentUser().getDispatcherCompany().getRegions().contains(foundTrip.getRegion()))
            throw new IllegalAccessException("Trip is not in your region");
        foundTrip.addDriverRemark(driverRemark);
        tripRepository.save(foundTrip);
    }

    public Trip setTripStatus(Long tripId, String status) throws IllegalAccessException {
        Trip foundTrip = tripRepository.findById(tripId).orElseThrow(() -> new EntityNotFoundException("Could not find trip"));
        if (!tripStages.contains(status)) throw new IllegalArgumentException("Status is not valid");
        if (!userService.userIsAdmin() && !userService.getCurrentUser().getDispatcherCompany().getRegions().contains(foundTrip.getRegion()))
            throw new IllegalAccessException("Trip is not in your region");
        foundTrip.setStatus(status);
        return tripRepository.save(foundTrip);
    }

    public List<Trip> setMultipleTripStatus(List<Long> tripIds, String status) throws IllegalAccessException {
        List<Trip> list = new ArrayList<>();
        for (Long tripId : tripIds) {
            Trip trip = setTripStatus(tripId, status);
            list.add(trip);
        }
        return list;
    }

    public List<Trip> getUnreportedTrips(Integer pageNum, SearchObject searchObject) throws AccessDeniedException {
        String userType = userService.getCurrentUserType();
        User currentUser = userService.getCurrentUser();
        if (currentUser.getOrganizerCompany() == null
                && currentUser.getDispatcherCompany() == null
                && !userService.userIsAdmin())
            throw new AccessDeniedException("User not allowed to search");
        Calendar startDate = null;
        Calendar endDate = null;
        if (searchObject.getDate() != null) {
            startDate = DateUtil.toCalendar(searchObject.getDate());
            endDate = DateUtil.toCalendar(searchObject.getDate());
            startDate.set(Calendar.HOUR_OF_DAY, 0);
            startDate.set(Calendar.MINUTE, 0);

            endDate.set(Calendar.HOUR_OF_DAY, 23);
            endDate.set(Calendar.MINUTE, 59);
        }
        Ship foundShip = shipRepository.findByName(searchObject.getShipName()).orElse(null);
        Location foundLocation = locationService.determineLocation(searchObject.getHarbour());
        List<Trip> searchResult;
        DispatcherCompany dispatcherCompany = currentUser.getDispatcherCompany();
        //TODO: Add search parameters
        if (searchObject.getArchiveMode() == null || !searchObject.getArchiveMode()) {
            searchResult = tripRepository.findUnreportedTrips(
                    foundShip,
                    userType,
                    searchObject.getPoNumber(),
                    startDate != null ? new Timestamp(startDate.getTimeInMillis()) : new Timestamp(System.currentTimeMillis()),
                    endDate != null ? new Timestamp(endDate.getTimeInMillis()) : null,
                    foundLocation,
                    userService.getCurrentUser().getOrganizerCompany(),
                    dispatcherCompany != null ? dispatcherCompany.getRegions().stream().map(Region::getId).toList() : null,
                    PAGE_SIZE,
                    pageNum * PAGE_SIZE);
        } else {
            searchResult = tripRepository.findUnreportedTrips(
                    foundShip,
                    userType,
                    searchObject.getPoNumber(),
                    startDate != null ? new Timestamp(startDate.getTimeInMillis()) : null,
                    endDate != null ? new Timestamp(endDate.getTimeInMillis()) : new Timestamp(System.currentTimeMillis()),
                    foundLocation,
                    userService.getCurrentUser().getOrganizerCompany(),
                    dispatcherCompany != null ? dispatcherCompany.getRegions().stream().map(Region::getId).toList() : null,
                    PAGE_SIZE,
                    pageNum * PAGE_SIZE);
        }
        return searchResult;
    }

    public List<Trip> getTripsForReport(UnreportedTripSearchObject searchObject) {
        Ship foundShip = null;
        Location harbour = null;

        if(searchObject.getShip() != null && !searchObject.getShip().trim().isEmpty() && searchObject.getPoNumber() == null && searchObject.getHarbor() != null && !searchObject.getHarbor().trim().isEmpty()){
            foundShip = shipRepository.findByName(searchObject.getShip()).orElseThrow(()->new EntityNotFoundException("Could not find ship"));
            harbour = locationService.determineLocation(searchObject.getHarbor());
        }
        if (foundShip == null && searchObject.getPoNumber() == null && harbour == null)
            throw new IllegalArgumentException("You must provide a ship and a harbour or a po number");


        return tripRepository.findUnreportedTripsFiltered(searchObject.getPoNumber(),
                userService.getCurrentUserType(),
                foundShip,
                harbour,
                userService.getCurrentUser().getOrganizerCompany(),
                userService.getCurrentUser().getDispatcherCompany() != null ? userService.getCurrentUser().getDispatcherCompany().getRegions().stream().map(Region::getId).toList() : null,
                searchObject.getStartDate(), searchObject.getEndDate());
    }


    public FileDownloadObject generateTripsExport(Long poNumber) throws IOException {
        User currentUser = userService.getCurrentUser();
        if (currentUser.getOrganizerCompany() == null && currentUser.getDispatcherCompany() == null && !userService.userIsAdmin())
            throw new AccessDeniedException("User not allowed to get trips");
        List<Trip> trips = tripRepository.findTripsForExport(poNumber, currentUser.getOrganizerCompany(), currentUser.getDispatcherCompany() != null ? currentUser.getDispatcherCompany().getRegions().stream().map(Region::getId).toList() : null);
        if (trips.isEmpty()) throw new EntityNotFoundException("Could not find trips with po number: " + poNumber);
        FileDownloadObject fdo = new FileDownloadObject();
        fdo.setFilename("Trips_" + poNumber + ".xlsx");


        File tripsFile = fileStorageService.convertWorkbookToFile(fileWritingUtil.generateTripExport(trips), "Trips_" + poNumber);
        Path pathToTripFile = tripsFile.toPath();
        byte[] fileBytes = Files.readAllBytes(pathToTripFile);
        fdo.setByteArrayResource(new ByteArrayResource(fileBytes));
        tripsFile.delete();

        return fdo;
    }

    public void deleteTrip(Long tripId) throws EntityNotFoundException {
        Trip foundTrip = tripRepository.findById(tripId).orElseThrow(() -> new EntityNotFoundException("Could not find trip"));
        foundTrip.getPassengers().forEach(passenger -> {
            passenger.setTrip(null);
            paxRepository.delete(passenger);
        });
        tripRepository.delete(foundTrip);
    }

    public Trip tripFromManualOrder(ManualTripDTO tripOrder){
        Date expirationDate = new Date();
        expirationDate = DateUtil.addMonths(expirationDate, 30);
        OrganizerCompany organizerCompany;
        if(userService.userIsAdmin()) {
            organizerCompany = organizerCompanyService.findOrganizerCompanyByName(tripOrder.getOrganizerCompany());
        }else{
            organizerCompany = userService.getCurrentUser().getOrganizerCompany();
        }
        Ship foundShip = shipService.findShipByName(tripOrder.getShip(), organizerCompany);


        Trip trip = new Trip();
        trip.setPickUpTime(tripOrder.getPickUpTime());
        trip.setStatus("created");
        trip.setOrganizerCompany(organizerCompany);
        trip.setExpirationDate(new Timestamp(expirationDate.getTime()));
        trip.setPickUpLocation(locationService.determineLocation(tripOrder.getPickUpLocation()));
        trip.setDestination(locationService.determineLocation(tripOrder.getDestination()));
        trip.setCancelFee(false);
        trip.setPoNumber(Long.parseLong(tripOrder.getPoNumber()));
        trip.setPassengerRemarks(tripOrder.getPassengerRemarks());
        trip.setHarbour(locationService.determineLocation(tripOrder.getHarbour()));
        trip.setRegion(locationService.determineRegion(trip.getHarbour()));
        trip.setShip(foundShip);

        return tripRepository.save(trip);
    }

    public Trip saveTrip(Trip trip) {
        return tripRepository.save(trip);
    }
}
