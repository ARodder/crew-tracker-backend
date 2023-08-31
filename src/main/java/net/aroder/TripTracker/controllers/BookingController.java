package net.aroder.TripTracker.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import net.aroder.TripTracker.exceptions.ExcelInformationException;
import net.aroder.TripTracker.mappers.PaxMapper;
import net.aroder.TripTracker.models.DTOs.PaxDTOs.PaxDTO;
import net.aroder.TripTracker.models.PAX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

import java.util.List;
import org.springframework.http.ResponseEntity;

import net.aroder.TripTracker.services.BookingService;

/**
 * Controller registering the required endpoints for booking.
 */
@RestController
@RequestMapping("/api/v1/booking")
public class BookingController {
    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    private final PaxMapper paxMapper;

    private final BookingService bookingService;

    public BookingController(final PaxMapper paxMapper,final BookingService bookingService){
        this.paxMapper = paxMapper;
        this.bookingService = bookingService;
    }


    @PostMapping("/uploadBooking")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER') or hasRole('ROLE_ORGANIZER')")
    @Operation(summary="Upload a single file")
    @ApiResponses(value={
            @ApiResponse(responseCode = "200",description = "Success",content={
                    @Content(mediaType="application/json",array = @ArraySchema(schema = @Schema(implementation= PaxDTO.class)))
            }),
            @ApiResponse(responseCode = "400",description = "Bad Request",content={
                    @Content(mediaType="application/json", schema=@Schema(implementation= ProblemDetail.class))
            }),
    })
    public ResponseEntity uploadFile(@RequestParam("file") MultipartFile file, @RequestParam(value = "companyName", required = false) String companyName) {
        try {
            return ResponseEntity.ok(paxMapper.toPaxDTO(bookingService.readExcelFile(file,companyName)));
        } catch (IOException e) {
            logger.error("FileUpload error "+ e.getMessage());
            return ResponseEntity.badRequest().build();
        }catch (ExcelInformationException e) {
            logger.error("FileUpload error "+ e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (Exception e) {
            logger.error("FileUpload error "+ e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/order")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER') or hasRole('ROLE_ORGANIZER')")
    @Operation(summary="Confirm a formatted order")
    @ApiResponses(value={
            @ApiResponse(responseCode = "200",description = "Success"),
            @ApiResponse(responseCode = "400",description = "Bad Request",content={
                    @Content(mediaType="application/json", schema=@Schema(implementation= ProblemDetail.class))
            }),
    })
    public ResponseEntity confirmOrder(@RequestBody List<PaxDTO> paxList,@RequestParam(value = "companyName", required = false) String companyName){
        try {
            bookingService.confirmOrder((List<PAX>) paxMapper.toPax(paxList),companyName);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("ConfirmOrder error"+ e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
