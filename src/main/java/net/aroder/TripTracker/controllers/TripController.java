package net.aroder.TripTracker.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.EntityNotFoundException;
import net.aroder.TripTracker.mappers.TripMapper;
import net.aroder.TripTracker.models.DTOs.*;
import net.aroder.TripTracker.models.DTOs.TripDTOs.TripDTO;
import net.aroder.TripTracker.models.Trip;
import net.aroder.TripTracker.services.TripService;
import net.aroder.TripTracker.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
/**
 * Controller registering the required endpoints for trips.
 */
@RestController
@RequestMapping(path = "/api/v1/trips")
public class TripController {
    private static final Logger logger = LoggerFactory.getLogger(TripController.class);


    @Autowired
    private TripMapper tripMapper;

    @Autowired
    private TripService tripService;
    @Autowired
    private UserService userService;

    @GetMapping("/page/{pageNum}")
    @Operation(summary = "Retrieves a paginated list of trips")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TripDTO.class)))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
            }),
    })
    public ResponseEntity getTrips(@PathVariable(value = "pageNum", required = false) Integer pageNum) {
        try {
            List<Trip> foundTrips = tripService.getTripPage(pageNum != null ? pageNum : 0);
            if(userService.userIsDispatcher() || userService.userIsDriver()){
                return ResponseEntity.ok(tripMapper.toTripDTODispatch(foundTrips));
            }else if(userService.userIsManagerOrOrganizer()){
                return ResponseEntity.ok(tripMapper.toTripDTOOrganize(foundTrips));
            }else if(userService.userIsAdmin()){
                //TODO: add admin trip dto.
                return ResponseEntity.ok(tripMapper.toTripDTODispatch(foundTrips));
            }else throw new IllegalAccessException();
        } catch (Exception e) {
            logger.error("Error getting trips: ", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity getTrip(@PathVariable("id") Long id) {
        try {
            Trip foundTrip = tripService.getTrip(id);
            return ResponseEntity.ok(tripMapper.toTripDTOAdmin(foundTrip));
        } catch (EntityNotFoundException e) {
            logger.error("Error getting trip: ", e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error getting trip: ", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/page")
    @Operation(summary = "Retrieves a paginated list of trips based on search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TripDTO.class)))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
            }),
    })
    public ResponseEntity getTrips(
            @RequestParam(value = "pageNum", required = false,defaultValue = "0") Integer pageNum,
            @RequestBody SearchObject searchObject) {
        try {
            List<Trip> foundTrips = tripService.searchTrips(searchObject, pageNum);

            if(userService.userIsDispatcher() || userService.userIsDriver()){
                return ResponseEntity.ok(tripMapper.toTripDTODispatch(foundTrips));
            }else if(userService.userIsManagerOrOrganizer()){
                return ResponseEntity.ok(tripMapper.toTripDTOOrganize(foundTrips));
            }else if(userService.userIsAdmin()){
                //TODO: add admin trip dto.
                return ResponseEntity.ok(tripMapper.toTripDTODispatch(foundTrips));
            }else throw new IllegalAccessException();

        } catch (Exception e) {
            logger.error("Failed getting trips: ", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/cancel/{tripId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER') or hasRole('ROLE_ORGANIZER')")
    @Operation(summary = "Cancels a trip based on trip id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = TripDTO.class))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
            }),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
            }),
            @ApiResponse(responseCode = "404", description = "Not found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
            }),
    })
    public ResponseEntity cancelTrip(@PathVariable("tripId") Long tripId) {
        try {
            Trip foundTrip = tripService.cancelTrip(tripId);

            if(userService.userIsDispatcher() || userService.userIsDriver()){
                return ResponseEntity.ok(tripMapper.toTripDTODispatch(foundTrip));
            }else if(userService.userIsManagerOrOrganizer()){
                return ResponseEntity.ok(tripMapper.toTripDTOOrganize(foundTrip));
            }else if(userService.userIsAdmin()){
                //TODO: add admin trip dto.
                return ResponseEntity.ok(tripMapper.toTripDTODispatch(foundTrip));
            }else throw new IllegalAccessException();
        } catch (EntityNotFoundException e) {
            logger.error("Failed to cancel trip: ", e);
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            logger.error("Failed to cancel trip: ", e);
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            logger.error("Failed to cancel trip: ", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER') or hasRole('ROLE_ORGANIZER')")
    @Operation(summary = "Edits a trip based on id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = TripDTO.class))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
            }),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
            }),
            @ApiResponse(responseCode = "404", description = "Not found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
            }),
    })
    public ResponseEntity editTrip(@RequestBody TripDTO trip) {
        try {
            tripService.editTrip(tripMapper.toTrip(trip));
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            logger.error("Failed to edit trip: ", e);
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            logger.error("Failed to edit trip: ", e);
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            logger.error("Failed to edit trip: ", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/transferable/{paxId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER') or hasRole('ROLE_ORGANIZER')")
    @Operation(summary = "Retrieves a list of trips a passenger can be transferred to")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = TripDTO.class))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
            }),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
            }),
            @ApiResponse(responseCode = "404", description = "Not found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
            }),
    })

    public ResponseEntity findTransferableTrips(@PathVariable("paxId") Long paxId) {
        try {
            List<Trip> foundTrips = tripService.findTransferableTrips(paxId);


            if(userService.userIsDispatcher() || userService.userIsDriver()){
                return ResponseEntity.ok(tripMapper.toTripDTODispatch(foundTrips));
            }else if(userService.userIsManagerOrOrganizer()){
                return ResponseEntity.ok(tripMapper.toTripDTOOrganize(foundTrips));
            }else if(userService.userIsAdmin()){
                //TODO: add admin trip dto.
                return ResponseEntity.ok(tripMapper.toTripDTODispatch(foundTrips));
            }else throw new IllegalAccessException();
        } catch (EntityNotFoundException e) {
            logger.error("Failed to find transferable trips: ", e);
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            logger.error("Failed to find transferable trips: ", e);
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            logger.error("Failed to find transferable trips: ", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/transfer/{tripId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER') or hasRole('ROLE_ORGANIZER')")
    @Operation(summary = "Transfers a passenger from one trip to another")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = TripDTO.class))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
            }),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
            }),
            @ApiResponse(responseCode = "404", description = "Not found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
            }),
    })
    public ResponseEntity transferPax(@PathVariable("tripId") Long tripId, @RequestParam("paxId") Long paxId) {
        try {
            tripService.transferPax(paxId, tripId);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            logger.error("Failed to transfer passenger: ", e);
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            logger.error("Failed to transfer passenger: ", e);
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            logger.error("Failed to transfer passenger: ", e);
            return ResponseEntity.badRequest().build();
        }
    }


    @PostMapping("/dispatch")
    @PreAuthorize("hasRole('ROLE_DISPATCHER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "Retrieves a paginated list of trips based on search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TripDTO.class)))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
            }),
    })
    public ResponseEntity getDispatchTrips(@RequestBody DispatchSearchObject searchObject) {
        try {
            return ResponseEntity.ok(tripMapper.toTripDTODispatch(
                    tripService.getDispatchTrips(searchObject)
            ));
        } catch (Exception e) {
            logger.error("Failed to get dispatch trips: ", e);
            return ResponseEntity.badRequest().build();
        }
    }


    @GetMapping("/{tripId}/set-driver")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_DISPATCHER')")
    public ResponseEntity assignDriver(@PathVariable("tripId") Long tripId,@RequestParam("driverId") String driverId){
        try{
            tripService.assignDriver(tripId,driverId);
            return ResponseEntity.accepted().build();
        }catch(EntityNotFoundException | IllegalArgumentException e){
            logger.error("Failed to assign driver: ", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e){
            logger.error("Failed to assign driver: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/set-driver")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_DISPATCHER')")
    public ResponseEntity assignMultipleDrivers(@RequestParam("driverId") String driverId,@RequestBody List<Long> tripIds){
        try{
            tripService.assignMultipleDrivers(tripIds,driverId);
            return ResponseEntity.accepted().build();
        }catch(EntityNotFoundException | IllegalArgumentException e){
            logger.error("Failed to assign driver: ", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e){
            logger.error("Failed to assign driver: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{tripId}/set-price")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_DISPATCHER')")
    public ResponseEntity setTripPrice(@PathVariable("tripId") Long tripId,@RequestBody NewPriceRequest priceRequest){
        try{
            return ResponseEntity.ok(tripMapper.toTripDTODispatch(tripService.setPrice(tripId,priceRequest.getPrice())));
        }catch (EntityNotFoundException e){
            logger.error("Failed to set trip price: ", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (Exception e){
            logger.error("Failed to set trip price: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    @PatchMapping("/set-price")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_DISPATCHER')")
    public ResponseEntity setMultipleTripPrice(@RequestParam("newPrice") Double newPrice,@RequestBody List<Long> tripIds){
        try{
            return ResponseEntity.ok(tripMapper.toTripDTODispatch(tripService.setMultipleTripPrice(tripIds,newPrice)));
        }catch (EntityNotFoundException e){
            logger.error("Failed to set trip price: ", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (Exception e){
            logger.error("Failed to set trip price: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @PostMapping("/{tripId}/add-driver-remark")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_DISPATCHER') or hasRole('ROLE_DRIVER')")
    public ResponseEntity addDriverRemark(@PathVariable("tripId")Long tripId, @RequestBody DriverRemarkRequest driverRemark){
        try{
            tripService.addDriverRemark(tripId,driverRemark.getDriverRemark());
            return ResponseEntity.accepted().build();
        }catch(Exception e){
            logger.error("Failed to add driver remark: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/{tripId}/set-status")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_DISPATCHER')")
    public ResponseEntity setTripStatus(@PathVariable("tripId") Long tripId,@RequestParam("tripStatus") String tripStatus){
        try{
            return ResponseEntity.ok(tripMapper.toTripDTODispatch(tripService.setTripStatus(tripId,tripStatus)));
        }catch (EntityNotFoundException e){
            logger.error("Failed to set trip status: ", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (Exception e){
            logger.error("Failed to set trip status: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/set-status")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_DISPATCHER')")
    public ResponseEntity setMultipleTripStatus(@RequestParam("tripStatus") String tripStatus,@RequestBody List<Long> tripIds){
        try{
            return ResponseEntity.ok(tripMapper.toTripDTODispatch(tripService.setMultipleTripStatus(tripIds,tripStatus)));
        }catch (EntityNotFoundException e){
            logger.error("Failed to set trip status: ", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (Exception e){
            logger.error("Failed to set trip status: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/unreported")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_DISPATCHER') or hasRole('ROLE_ORGANIZER') or hasRole('ROLE_MANAGER')")
    public ResponseEntity getUnreportedTrips(@RequestParam("pageNum") Integer pageNumber,@RequestBody SearchObject searchObject){
        try{
            return ResponseEntity.ok(tripMapper.toTripDTOOrganize(tripService.getUnreportedTrips(pageNumber,searchObject)));
        }catch (Exception e){
            logger.error("Failed to get unreported trips: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/find-report-trips")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_DISPATCHER') or hasRole('ROLE_ORGANIZER') or hasRole('ROLE_MANAGER')")
    public ResponseEntity findReportTrips(@RequestBody UnreportedTripSearchObject tripReportRequest){
        try{
            return ResponseEntity.ok(tripMapper.toTripDTOOrganize(tripService.getTripsForReport(tripReportRequest)));
        }catch (Exception e){
            logger.error("Failed to get unreported trips: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/sheet-list/{poNumber}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_DISPATCHER') or hasRole('ROLE_ORGANIZER') or hasRole('ROLE_MANAGER')")
    public ResponseEntity getTripsAsFile(@PathVariable("poNumber") Long poNumber){
        try{
            FileDownloadObject fdo = tripService.generateTripsExport(poNumber);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", fdo.getFilename());


            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .headers(headers)
                    .body(fdo.getByteArrayResource());
        }catch (Exception e){
            logger.error("Failed to get trips as file: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{tripId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity deleteTrip(@PathVariable("tripId") Long tripId){
        try{
            tripService.deleteTrip(tripId);
            return ResponseEntity.accepted().build();
        }catch (EntityNotFoundException e){
            logger.error("Failed to delete trip: ", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (Exception e){
            logger.error("Failed to delete trip: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
