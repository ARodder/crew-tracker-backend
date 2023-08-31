package net.aroder.TripTracker.controllers;

import net.aroder.TripTracker.models.Location;
import net.aroder.TripTracker.services.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping(path = "/api/v1/locations")
public class LocationController {

    private final Logger logger = LoggerFactory.getLogger(LocationController.class);

    private final LocationService locationService;

    public LocationController(final LocationService locationservice){
        this.locationService = locationservice;
    }


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/search")
    public ResponseEntity searchLocationNames(@RequestParam("searchWord") String searchWord){
        try{
            return ResponseEntity.ok(locationService.searchLocationsLikeName(searchWord));
        } catch(Exception e){
            logger.error("Error searching for locations " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }



    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER') or hasRole('ROLE_DISPATCHER')")
    @GetMapping
    public ResponseEntity findAllLocations(){
        try{
            return ResponseEntity.ok(locationService.findAll());
        }catch (Exception e) {
            logger.error("Error finding all locations " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER') or hasRole('ROLE_DISPATCHER')")
    @DeleteMapping
    public ResponseEntity deleteShip(@RequestParam("locationId") Long shipId){
        try{
            locationService.deleteLocation(shipId);
            return ResponseEntity.accepted().build();
        }catch(Exception e){
            logger.error("Error deleting location " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER') or hasRole('ROLE_DISPATCHER')")
    @PostMapping
    public ResponseEntity createLocation(@RequestBody Location location){
        try{
            return ResponseEntity.created(URI.create("/locations/"+ locationService.createLocation(location).getId())).build();
        }catch(Exception e){
            logger.error("Error creating location " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER') or hasRole('ROLE_DISPATCHER')")
    @PatchMapping
    public ResponseEntity updateLocation(@RequestBody Location location){
        try{
            locationService.updateLocation(location);
            return ResponseEntity.accepted().build();
        }catch(Exception e){
            logger.error("Error updating location " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
