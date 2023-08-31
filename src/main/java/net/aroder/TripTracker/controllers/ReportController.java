package net.aroder.TripTracker.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import net.aroder.TripTracker.mappers.DomainFileMapper;
import net.aroder.TripTracker.mappers.TripMapper;
import net.aroder.TripTracker.models.DTOs.DomainFileDTOs.DomainFileDTO;
import net.aroder.TripTracker.models.DTOs.FileDownloadObject;
import net.aroder.TripTracker.models.DTOs.TripDTOs.TripDTO;
import net.aroder.TripTracker.models.DTOs.TripDTOs.TripOrganizeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import net.aroder.TripTracker.services.ReportService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {
    private final ReportService reportService;
    private final DomainFileMapper domainFileMapper;
    private final Logger logger  = LoggerFactory.getLogger(ReportController.class);
    private final TripMapper tripMapper;

    public ReportController(final ReportService reportService, final DomainFileMapper domainFileMapper, final TripMapper tripMapper){
        this.reportService = reportService;
        this.domainFileMapper = domainFileMapper;
        this.tripMapper = tripMapper;
    }


    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_DISPATCHER') or hasRole('ROLE_ORGANIZER') or hasRole('ROLE_MANAGER')")
    public ResponseEntity getReportService(@RequestParam("pageNum") Integer pageNum) {
        try{
            List<DomainFileDTO> foundReports = domainFileMapper.toDomainFileDTO(reportService.findAllReports(pageNum));
            return ResponseEntity.ok().body(foundReports);
        }catch(Exception e){
            logger.error("Could not find reports",e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_DISPATCHER') or hasRole('ROLE_ORGANIZER') or hasRole('ROLE_MANAGER')")
    public ResponseEntity generateReport(@RequestBody List<TripOrganizeDTO> trips) {
        try{
            reportService.generateReport(tripMapper.toTripById(trips.stream().map(TripDTO::getId).toList()));
            return ResponseEntity.ok().body("");
        }catch(Exception e){
            logger.error("Could not generate report",e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{reportId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_DISPATCHER') or hasRole('ROLE_ORGANIZER') or hasRole('ROLE_MANAGER')")
    public ResponseEntity deleteAndResetReport(@PathVariable("reportId") Long reportId){
        try{
            reportService.deleteAndResetReport(reportId);
            return ResponseEntity.accepted().build();
        }catch(Exception e){
            logger.error("Failed to delete report ",e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/{domainFileId}/download")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_DISPATCHER') or hasRole('ROLE_ORGANIZER') or hasRole('ROLE_MANAGER')")
    @Operation(summary="Download file")
    @ApiResponses(value={
            @ApiResponse(responseCode = "200",description = "Success",content={
                    @Content(mediaType="application/json",schema = @Schema(allOf = Resource.class))
            }),
            @ApiResponse(responseCode = "400",description = "Bad Request",content={
                    @Content(mediaType="application/json", schema=@Schema(implementation= ProblemDetail.class))
            }),
    })
    public ResponseEntity<Resource> downloadFile(@PathVariable("domainFileId") Long domainFileId) {

        try {
            FileDownloadObject fdo = reportService.findResourceRemote(domainFileId);


            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fdo.getFilename() + "\"")
                    .body(fdo.getByteArrayResource());
        } catch (Exception e){
            logger.error("Could not download file",e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
