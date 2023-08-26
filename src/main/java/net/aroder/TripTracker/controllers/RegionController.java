package net.aroder.TripTracker.controllers;

import jakarta.persistence.EntityNotFoundException;
import net.aroder.TripTracker.mappers.RegionMapper;
import net.aroder.TripTracker.models.DTOs.RegionDTOs.NewRegionDTO;
import net.aroder.TripTracker.models.DTOs.RegionDTOs.RegionDTO;
import net.aroder.TripTracker.services.RegionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/regions")
public class RegionController {

    private final Logger logger = LoggerFactory.getLogger(RegionController.class);

    @Autowired
    private RegionService regionService;
    @Autowired
    private RegionMapper regionMapper;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity findAllRegions() {
        try {
            return ResponseEntity.ok(regionMapper.toRegionDto(regionService.findAllRegions()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{regionId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity findRegionById(@PathVariable("regionId") Long regionId) {
        try {
            return ResponseEntity.ok(regionMapper.toRegionDto(regionService.findRegionById(regionId)));
        } catch (EntityNotFoundException e) {
            logger.error("Could not fund region by id "+ regionId,e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Could not fund region by id "+ regionId,e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity createRegion(@RequestBody NewRegionDTO newRegion) {
        try {
            return ResponseEntity.created(URI.create("api/v1/organizer-company/" + regionService.createRegion(newRegion).getId())).build();
        } catch (IllegalArgumentException e) {
            logger.error("Could not create region", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Could not create region", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/{regionId}/re-name")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity updateName(@PathVariable("regionId") Long regionId, @RequestParam("name") String newName) {
        try {
            regionService.updateRegionName(regionId, newName);
            return ResponseEntity.accepted().build();
        } catch (EntityNotFoundException e) {
            logger.error("Could not update region name", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Could not update region name", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{regionId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity deleteRegion(@PathVariable("regionId") Long regionId){
        try{
            regionService.deleteRegion(regionId);
            return ResponseEntity.accepted().build();
        }catch (IllegalArgumentException e){
            logger.error("Could not delete region", e);
            return ResponseEntity.badRequest().body("Could not find company to delete");
        }catch (Exception e){
            logger.error("Could not delete region", e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @PatchMapping("/{regionId}/re-locate")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity updateLocation(@PathVariable("regionId") Long regionId, @RequestParam("locationName") String locationName) {
        try {
            regionService.updateLocation(regionId, locationName);
            return ResponseEntity.accepted().build();
        } catch (EntityNotFoundException e) {
            logger.error("Could not update region location", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Could not update region location", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/{regionId}/change-dispatch-company")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity changeDispatchCompany(@PathVariable("regionId") Long regionId, @RequestParam("dispatchCompanyName") String dispatchCompanyName) {
        try {
            regionService.updateDispatcherCompany(regionId, dispatchCompanyName);
            return ResponseEntity.accepted().build();
        } catch (EntityNotFoundException e) {
            logger.error("Could not update region dispatch company", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Could not update region dispatch company", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/search")
    public ResponseEntity searchRegionNames(@RequestParam("searchWord") String searchWord){
        try{
            return ResponseEntity.ok(regionService.searchRegionsLikeName(searchWord));
        } catch(Exception e){
            logger.error("Error searching for locations " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/names")
    public ResponseEntity getAllRegionNames(){
        try{
            return ResponseEntity.ok(regionService.getAllRegionNames());
        } catch(Exception e){
            logger.error("Error searching for locations " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }


}
