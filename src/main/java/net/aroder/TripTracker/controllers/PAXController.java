package net.aroder.TripTracker.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.EntityNotFoundException;
import net.aroder.TripTracker.mappers.PaxMapper;
import net.aroder.TripTracker.models.DTOs.PaxDTOs.PaxDTO;
import net.aroder.TripTracker.models.DTOs.TripDTOs.TripDTO;
import net.aroder.TripTracker.services.PAXService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/passengers")
public class PAXController {

    private final Logger logger = LoggerFactory.getLogger(PAXController.class);
    @Autowired
    private PAXService paxService;
    @Autowired
    private PaxMapper paxMapper;

    @GetMapping("/toggleCancel/{passengerId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER') or hasRole('ROLE_ORGANIZER')")
    @Operation(summary="Cancels a passenger based on the id of the passenger")
    @ApiResponses(value={
            @ApiResponse(responseCode = "200",description = "Success",content={
                    @Content(mediaType="application/json", schema=@Schema(implementation= PaxDTO.class))
            }),
            @ApiResponse(responseCode = "400",description = "Bad Request",content={
                    @Content(mediaType="application/json", schema=@Schema(implementation= ProblemDetail.class))
            }),
            @ApiResponse(responseCode = "403",description = "Forbidden",content={
                    @Content(mediaType="application/json", schema=@Schema(implementation= ProblemDetail.class))
            }),
            @ApiResponse(responseCode = "404",description = "Not found",content={
                    @Content(mediaType="application/json", schema=@Schema(implementation= ProblemDetail.class))
            }),
    })
    public ResponseEntity toggleCancelTrip(@PathVariable("passengerId") Long passengerId){
        try{

            return ResponseEntity.ok(paxMapper.toPaxDTO(paxService.toggleCancelPassenger(passengerId)));
        }catch(EntityNotFoundException e){
            logger.error("Toggle cancel failed ",e);
            return ResponseEntity.notFound().build();
        }catch(AccessDeniedException e){
            logger.error("Toggle cancel failed ",e);
            return ResponseEntity.status(401).build();
        }catch(Exception e){
            logger.error("Toggle cancel failed ",e);
            return ResponseEntity.badRequest().build();
        }
    }


    @PutMapping("/transfer/new/{passengerId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER') or hasRole('ROLE_ORGANIZER')")
    @Operation(summary="Transfers an existing passenger to a new trip")
    @ApiResponses(value={
            @ApiResponse(responseCode = "200",description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = TripDTO.class))
            }),
            @ApiResponse(responseCode = "400",description = "Bad Request",content={
                    @Content(mediaType="application/json", schema=@Schema(implementation= ProblemDetail.class))
            }),
            @ApiResponse(responseCode = "403",description = "Forbidden",content={
                    @Content(mediaType="application/json", schema=@Schema(implementation= ProblemDetail.class))
            }),
            @ApiResponse(responseCode = "404",description = "Not found",content={
                    @Content(mediaType="application/json", schema=@Schema(implementation= ProblemDetail.class))
            }),
    })
    public ResponseEntity transferPaxToNewTrip(@PathVariable("passengerId") Long passengerId){
        try{
            paxService.paxToNewTrip(passengerId);
            return ResponseEntity.ok().build();
        }catch(EntityNotFoundException e){
            logger.error("Transfer failed ",e);
            return ResponseEntity.notFound().build();
        }catch(AccessDeniedException e){
            logger.error("Transfer failed ",e);
            return ResponseEntity.status(403).build();
        }catch(Exception e){
            logger.error("Transfer failed ",e);
            return ResponseEntity.badRequest().build();
        }
    }

}
