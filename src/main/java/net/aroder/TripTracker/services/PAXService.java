package net.aroder.TripTracker.services;

import jakarta.persistence.EntityNotFoundException;
import net.aroder.TripTracker.exceptions.InvalidPassengerException;
import net.aroder.TripTracker.models.OrganizerCompany;
import net.aroder.TripTracker.models.PAX;
import net.aroder.TripTracker.models.Trip;
import net.aroder.TripTracker.repositories.PAXRepository;
import net.aroder.TripTracker.repositories.TripRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class PAXService {

    private final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private final TripRepository tripRepository;
    private final PAXRepository paxRepository;
    private final TripService tripService;
    private final UserService userService;

    public PAXService(final PAXRepository paxRepository,
                      final TripRepository tripRepository,
                      final TripService tripService,
                      final UserService userService) {
        this.paxRepository = paxRepository;
        this.tripRepository = tripRepository;
        this.tripService = tripService;
        this.userService = userService;
    }

    /**
     * Checks if a passenger with the exact details (name, pickup location, destination, pickup time) exists and is not canceled.
     *
     * @param pax The PAX object representing the passenger details to check.
     * @return true if a matching passenger exists and is not canceled, false otherwise.
     */
    public boolean checkPassengerExactExist(PAX pax) {
        Optional<PAX> foundPax = paxRepository.findByFirstNameAndSurnameAndPickUpLocationAndDestinationAndPickUpTime(pax.getFirstName(), pax.getSurname(), pax.getPickUpLocation(), pax.getDestination(), pax.getPickUpTime());
        return foundPax.isPresent() && !foundPax.get().getStatus().equals("cancel");
    }

    /**
     * Checks if a passenger with the same pickup time exists and is not canceled.
     *
     * @param pax The PAX object representing the passenger details to check.
     * @return true if a matching passenger exists and is not canceled, false otherwise.
     */
    public boolean checkPassengerByLocationsExist(PAX pax) {
        Optional<PAX> foundPax = paxRepository.findByFirstNameAndSurnameAndPickUpLocationAndDestination(pax.getFirstName(), pax.getSurname(), pax.getPickUpLocation(), pax.getDestination());
        return foundPax.isPresent() && !foundPax.get().getStatus().equals("cancel");
    }

    /**
     * Checks if a passenger with the same pickup time exists and is not canceled.
     *
     * @param pax The PAX object representing the passenger details to check.
     * @return true if a matching passenger exists and is not canceled, false otherwise.
     */
    public boolean checkPassengerExistByTime(PAX pax) {

        Optional<PAX> foundPax = paxRepository.findByFirstNameAndSurnameAndPickUpTime(pax.getFirstName(), pax.getSurname(), pax.getPickUpTime());
        return foundPax.isPresent() && !foundPax.get().getStatus().equals("cancel");
    }


    /**
     * Cancels multiple passenger bookings based on the provided list of PAX objects.
     *
     * @param paxList The list of PAX objects representing the bookings to cancel.
     */
    public void cancelMultiplePax(List<PAX> paxList, OrganizerCompany organizerCompany) {
        for (PAX pax : paxList) {
            try {
                cancelSinglePax(pax, organizerCompany);
            } catch (NoSuchElementException e) {
                logger.error("PAX not found, delete failed for: " + pax.getFirstName() + " " + pax.getSurname() + ", with message: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                logger.error("Pax cannot be canceled: ", e);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }

    }

    /**
     * Cancels a single passenger booking.
     *
     * @param pax The PAX object representing the booking to cancel.
     */
    public void cancelSinglePax(PAX pax, OrganizerCompany organizerCompany) throws AccessDeniedException {
        PAX storedPax = paxRepository.findByFirstNameAndSurnameAndPickUpLocationAndDestinationAndPickUpTime(pax.getFirstName(), pax.getSurname(), pax.getPickUpLocation(), pax.getDestination(), pax.getPickUpTime()).orElseThrow(NoSuchElementException::new);
        Trip trip = storedPax.getTrip();
        if (trip.getStatus().equals("completed") || trip.getStatus().equals("in_progress")) {
            throw new IllegalArgumentException("Cannot cancel an in progress or completed trip");
        }
        if (!trip.getOrganizerCompany().equals(organizerCompany)) {
            throw new AccessDeniedException("You are not allowed to cancel this booking");
        }
        storedPax.setStatus("cancel");
        paxRepository.save(storedPax);
        if (trip.allPassengersCanceled()) {
            trip.setStatus("cancel");
            if (timeFromTimeStamp(trip.getPickUpTime()) < 1) {
                trip.addDriverRemark("Cancellation fee");
            }
            tripRepository.save(trip);
        }
    }

    /**
     * Calculates the time difference in hours between the provided timestamp and the current time.
     *
     * @param timestamp The timestamp to calculate the time difference from.
     * @return The time difference in hours.
     */
    private long timeFromTimeStamp(Timestamp timestamp) {
        Timestamp now = new Timestamp(new Date().getTime());
        long difference = Math.abs(timestamp.getTime() - now.getTime());
        return difference / (60 * 60 * 1000);

    }

    /**
     * Change passengers already listed, either change the time or destination/pickUpLocation.
     *
     * @param paxList list of pax to change
     */
    public void changePax(List<PAX> paxList) {
        //TODO: Might need to add time limitation, to only change pax within the same day
        //when changing time
        for (PAX pax : paxList) {
            Optional<PAX> passengerByTime = paxRepository.findByFirstNameAndSurnameAndPickUpTime(pax.getFirstName(), pax.getSurname(), pax.getPickUpTime());
            Optional<PAX> passengerByLocations = paxRepository.findByFirstNameAndSurnameAndPickUpLocationAndDestination(pax.getFirstName(), pax.getSurname(), pax.getPickUpLocation(), pax.getDestination());

            passengerByTime.ifPresent(value -> changePaxByTime(pax, value));
            passengerByLocations.ifPresent(value -> changePaxByLocations(pax, value));
        }
    }

    /**
     * Change a pax, find the pax to change based on the locations
     *
     * @param newPax newPax details.
     * @param oldPax old pax details
     */
    private void changePaxByLocations(PAX newPax, PAX oldPax) {
        Trip oldTrip = oldPax.getTrip();
        oldPax.setPickUpTime(newPax.getPickUpTime());
        oldPax = tripService.determineNewTrip(oldTrip, oldPax);
        paxRepository.save(oldPax);
    }

    /**
     * Change a pax, find the pax to change based on the time
     *
     * @param newPax newPax details.
     * @param oldPax old pax details
     */
    private void changePaxByTime(PAX newPax, PAX oldPax) {
        Trip oldTrip = oldPax.getTrip();
        oldPax.setDestination(newPax.getDestination());
        oldPax.setPickUpLocation(newPax.getPickUpLocation());
        oldPax = tripService.determineNewTrip(oldTrip, oldPax);
        paxRepository.save(oldPax);
    }


    /**
     * Cancel a Passenger based on a  given id.
     *
     * @param passengerId id of the passenger to cancel.
     * @return passenger eit h updated status;
     * @throws AccessDeniedException if the user is not allowed to cancel the passenger.
     */
    public PAX toggleCancelPassenger(Long passengerId) throws AccessDeniedException {
        PAX passenger = paxRepository.findById(passengerId).orElseThrow(EntityNotFoundException::new);
        String newStatus = passenger.getStatus().equals("cancel") ? "created" : "cancel";
        Trip trip = passenger.getTrip();
        if ((userService.userIsManagerOrOrganizer() && trip.getOrganizerCompany().equals(userService.getCurrentUser().getOrganizerCompany()))
                || userService.userIsAdmin()) {
            passenger.setStatus(newStatus);

            paxRepository.save(passenger);
            if (trip.allPassengersCanceled() || newStatus.equals("created")) {
                trip.setStatus(newStatus);
                //TODO: improve cancel fee logic
                if (newStatus.equals("cancel") && Duration.between(trip.getPickUpTime().toLocalDateTime(), LocalDateTime.now()).toHours() < 2) {
                    trip.setDriverRemarks("Cancellation fee");
                    trip.setCancelFee(true);
                }
                tripRepository.save(trip);
            }
        } else throw new AccessDeniedException("Not accessible by user");
        return passenger;
    }

    public void paxToNewTrip(Long paxId) throws AccessDeniedException, InvalidPassengerException {
        PAX passenger = paxRepository.findById(paxId).orElseThrow(EntityNotFoundException::new);
        Trip oldTrip = passenger.getTrip();
        if (oldTrip.getPassengers().size() < 2)
            throw new InvalidPassengerException("Passenger will not be moved, REASON: only passenger in trip");
        if (!(userService.userIsAdmin() || (userService.userIsManagerOrOrganizer() && userService.getCurrentUser().getOrganizerCompany().equals(passenger.getTrip().getOrganizerCompany()))))
            throw new AccessDeniedException("Not accessible by user");
        Trip newTrip = tripService.createTripFromPax(passenger, passenger.getTrip().getOrganizerCompany());
        passenger.setTrip(newTrip);
        paxRepository.save(passenger);
    }

    public PAX savePAX(PAX pax) {
        return paxRepository.save(pax);
    }
}
