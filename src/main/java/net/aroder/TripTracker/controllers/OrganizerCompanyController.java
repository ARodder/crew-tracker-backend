package net.aroder.TripTracker.controllers;

import jakarta.persistence.EntityNotFoundException;
import net.aroder.TripTracker.mappers.OrganizerCompanyMapper;
import net.aroder.TripTracker.services.OrganizerCompanyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/organizer-companies")
public class OrganizerCompanyController {

    private final Logger logger = LoggerFactory.getLogger(OrganizerCompanyController.class);
    @Autowired
    private OrganizerCompanyService organizerCompanyService;
    @Autowired
    private OrganizerCompanyMapper organizerCompanyMapper;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity findAllOrganizerCompanies(@RequestParam(value = "withDetails", required = false) Boolean withDetails) {
        try {
            if(withDetails != null && withDetails) {
                return ResponseEntity.ok(organizerCompanyMapper.toOrganizerCompanyDTO(organizerCompanyService.findAllOrganizerCompanies()));
            }else{
                return ResponseEntity.ok(organizerCompanyMapper.toCompanyDTO(organizerCompanyService.findAllOrganizerCompanies()));
            }
        } catch (Exception e) {
            logger.error("Error while fetching all organizer companies", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{companyId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity findOrganizerCompanyById(@PathVariable("companyId") Long companyId) {
        try {
            return ResponseEntity.ok(organizerCompanyMapper.toCompanyDTO(organizerCompanyService.findOrganizerCompanyById(companyId)));
        } catch (EntityNotFoundException e) {
            logger.error("Could not find organizer company with id "+companyId, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error while fetching organizer company with id "+companyId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity createOrganizerCompany(@RequestParam("companyName") String newCompanyName) {
        try {
            return ResponseEntity.created(URI.create("api/v1/organizer-company/" + organizerCompanyService.createOrganizerCompany(newCompanyName).getId())).build();
        } catch (IllegalArgumentException e) {
            logger.error("Could not create organizer company with name "+newCompanyName, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error while creating organizer company with name "+newCompanyName, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/{companyId}/re-name")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity updateName(@PathVariable("companyId") Long companyId, @RequestParam("name") String newName) {
        try {
            organizerCompanyService.updateOrganizerCompanyName(companyId, newName);
            return ResponseEntity.accepted().build();
        } catch (EntityNotFoundException e) {
            logger.error("Could not find organizer company with id "+companyId, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error while updating organizer company with id "+companyId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{companyId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity deleteOrganizerCompany(@PathVariable("companyId") Long companyId){
        try{
            organizerCompanyService.deleteOrganizerCompany(companyId);
            return ResponseEntity.accepted().build();
        }catch (IllegalArgumentException e){
            logger.error("Could not delete organizer company with id "+companyId, e);
            return ResponseEntity.badRequest().body("Could not find company to delete");
        }catch (Exception e){
            logger.error("Error while deleting organizer company with id "+companyId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
