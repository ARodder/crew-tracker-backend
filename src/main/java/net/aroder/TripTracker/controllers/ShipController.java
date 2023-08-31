package net.aroder.TripTracker.controllers;

import net.aroder.TripTracker.mappers.ShipMapper;
import net.aroder.TripTracker.models.DTOs.ShipDTOs.ShipDTO;
import net.aroder.TripTracker.services.ShipService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping(path = "/api/v1/ships")
public class ShipController {

    private final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final ShipService shipService;
    private final ShipMapper shipMapper;

    public ShipController(final ShipService shipService, final ShipMapper shipMapper){
        this.shipService = shipService;
        this.shipMapper = shipMapper;
    }


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/search")
    public ResponseEntity searchShipNames(@RequestParam("searchWord") String searchWord){
        try{
            return ResponseEntity.ok(shipService.searchShipNamesLike(searchWord));
        } catch (Exception e){
            logger.error("Error searching for ships",e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    @GetMapping
    public ResponseEntity findAllShips(){
        try{
            return ResponseEntity.ok(shipMapper.toShipDto(shipService.findAllShips()));
        }catch(AccessDeniedException e){
            logger.error("Access denied",e);
            return ResponseEntity.status(403).build();
        }catch (Exception e) {
            logger.error("Error finding all ships",e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    @DeleteMapping
    public ResponseEntity deleteShip(@RequestParam("shipId") Long shipId){
        try{
            shipService.deleteShip(shipId);
            return ResponseEntity.accepted().build();
        }catch(Exception e){
            logger.error("Error deleting ship",e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    @PostMapping
    public ResponseEntity createShip(@RequestBody ShipDTO shipDTO,@RequestParam(value = "organizerCompanyId",required = false) Long organizerCompanyId){
        try{
            return ResponseEntity.created(URI.create("/ships/"+shipService.createShip(shipMapper.toShip(shipDTO),organizerCompanyId).getId())).build();
        }catch(Exception e){
            logger.error("Error creating ship",e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    @PatchMapping
    public ResponseEntity updateShip(@RequestBody ShipDTO shipDTO){
        try{
            shipService.updateShip(shipMapper.toShip(shipDTO));
            return ResponseEntity.accepted().build();
        }catch(Exception e){
            logger.error("Error updating ship",e);
            return ResponseEntity.internalServerError().build();
        }
    }


}
