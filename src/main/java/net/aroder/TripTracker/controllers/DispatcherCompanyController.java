package net.aroder.TripTracker.controllers;

import jakarta.persistence.EntityNotFoundException;
import net.aroder.TripTracker.mappers.DispatcherCompanyMapper;
import net.aroder.TripTracker.services.DispatcherCompanyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/dispatcher-companies")
public class DispatcherCompanyController {

    private final Logger logger = LoggerFactory.getLogger(DispatcherCompanyController.class);

    private final DispatcherCompanyService dispatcherCompanyService;

    private final DispatcherCompanyMapper dispatcherCompanyMapper;

    public DispatcherCompanyController(final DispatcherCompanyService dispatcherCompanyService, final DispatcherCompanyMapper dispatcherCompanyMapper){
        this.dispatcherCompanyService = dispatcherCompanyService;
        this.dispatcherCompanyMapper = dispatcherCompanyMapper;
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity findAllDispatcherCompanies(@RequestParam(value = "withDetails", required = false) Boolean withDetails) {
        try {
            if(withDetails != null && withDetails){
                return ResponseEntity.ok(dispatcherCompanyMapper.toDispatcherCompanyDto(dispatcherCompanyService.findAllDispatcherCompanies()));
            }else{
                return ResponseEntity.ok(dispatcherCompanyMapper.toCompanyDTO(dispatcherCompanyService.findAllDispatcherCompanies()));
            }
        } catch (Exception e) {
            logger.error("Error while retrieving all dispatcher companies "+ e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{companyId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity findDispatcherCompanyById(@PathVariable("companyId") Long companyId) {
        try {
            return ResponseEntity.ok(dispatcherCompanyMapper.toCompanyDTO(dispatcherCompanyService.findDispatcherCompanyById(companyId)));
        } catch (EntityNotFoundException e) {
            logger.error("Error while retrieving dispatcher company with id " + companyId + " " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error while retrieving dispatcher company with id " + companyId + " " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity createDispatcherCompany(@RequestParam("companyName") String newCompanyName) {
        try {
            return ResponseEntity.created(URI.create("api/v1/organizer-company/" + dispatcherCompanyService.createDispatcherCompany(newCompanyName).getId())).build();
        } catch (IllegalArgumentException e) {
            logger.error("Error while creating dispatcher company " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error while creating dispatcher company " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/{companyId}/re-name")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity updateName(@PathVariable("companyId") Long companyId, @RequestParam("name") String newName) {
        try {
            dispatcherCompanyService.updateDispatcherCompanyName(companyId, newName);
            return ResponseEntity.accepted().build();
        } catch (EntityNotFoundException e) {
            logger.error("Error while updating dispatcher company name " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error while updating dispatcher company name " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{companyId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity deleteDispatcherCompany(@PathVariable("companyId") Long companyId){
        try{
            dispatcherCompanyService.deleteDispatcherCompany(companyId);
            return ResponseEntity.accepted().build();
        }catch (IllegalArgumentException e){
            logger.error("Error while deleting dispatcher company " + e.getMessage());
            return ResponseEntity.badRequest().body("Could not find company to delete");
        }catch (Exception e){
            logger.error("Error while deleting dispatcher company " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity searchDispatcherCompany(@RequestParam("companyName") String companyName){
        try{
            return ResponseEntity.ok(dispatcherCompanyService.searchDispatcherCompanyByName(companyName));
        }catch (IllegalArgumentException e){
            logger.error("Error while searching for dispatcher company " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (Exception e){
            logger.error("Error while searching for dispatcher company " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
