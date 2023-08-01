package net.aroder.TripTracker.controllers;

import net.aroder.TripTracker.models.Location;
import net.aroder.TripTracker.services.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping(path = "/api/v1/locations")
public class LocationController {

    private Logger logger = LoggerFactory.getLogger(LocationController.class);
    @Autowired
    private LocationService locationservice;


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/search")
    public ResponseEntity searchLocationNames(@RequestParam("searchWord") String searchWord){
        try{
            return ResponseEntity.ok(locationservice.searchLocationsLikeName(searchWord));
        } catch(Exception e){
            logger.error("Error searching for locations " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }



    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER') or hasRole('ROLE_DISPATCHER')")
    @GetMapping
    public ResponseEntity findAllLocations(){
        try{
            return ResponseEntity.ok(locationservice.findAll());
        }catch (Exception e) {
            logger.error("Error finding all locations " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER') or hasRole('ROLE_DISPATCHER')")
    @DeleteMapping
    public ResponseEntity deleteShip(@RequestParam("locationId") Long shipId){
        try{
            locationservice.deleteLocation(shipId);
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
            return ResponseEntity.created(URI.create("/locations/"+locationservice.createLocation(location).getId())).build();
        }catch(Exception e){
            logger.error("Error creating location " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER') or hasRole('ROLE_DISPATCHER')")
    @PatchMapping
    public ResponseEntity updateLocation(@RequestBody Location location){
        try{
            locationservice.updateLocation(location);
            return ResponseEntity.accepted().build();
        }catch(Exception e){
            logger.error("Error updating location " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
