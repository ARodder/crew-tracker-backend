package net.aroder.TripTracker.services;

import jakarta.persistence.EntityNotFoundException;
import net.aroder.TripTracker.models.DTOs.FileDownloadObject;
import net.aroder.TripTracker.models.DomainFile;
import net.aroder.TripTracker.models.PAX;
import net.aroder.TripTracker.models.Trip;
import net.aroder.TripTracker.models.User;
import net.aroder.TripTracker.repositories.DomainFileRepository;
import net.aroder.TripTracker.repositories.PAXRepository;
import net.aroder.TripTracker.repositories.TripRepository;
import net.aroder.TripTracker.util.FileWritingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private Logger logger = LoggerFactory.getLogger(ReportService.class);
    @Autowired
    private DomainFileRepository domainFileRepository;
    @Autowired
    private PAXRepository paxRepository;
    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private FileWritingUtil fileWritingUtil;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private UserService userService;
    @Autowired
    private PAXService paxService;
    private final Integer PAGE_SIZE = 15;


    private final List<String> allowedFileTypes = List.of("report", "booking");

    public List<DomainFile> findAllReports(Integer pageNum) throws IllegalAccessException {
        PageRequest pageRequest = PageRequest.of(pageNum, PAGE_SIZE);

        if(userService.userIsAdmin()){
            List<DomainFile> foundReports = domainFileRepository.findAllByType("report", pageRequest).toList();
            return foundReports;
        }else if(userService.userIsManagerOrOrganizer()){
            return domainFileRepository.findPageForOrganizerCompany("report", userService.getCurrentUser().getOrganizerCompany(), PAGE_SIZE, PAGE_SIZE*pageNum);
        }else if(userService.userIsDispatcher()){
            return domainFileRepository.findPageForDispatcherCompany("report", userService.getCurrentUser().getDispatcherCompany(), PAGE_SIZE, PAGE_SIZE*pageNum);
        }else throw new IllegalAccessException("User is not authorized to view reports");
    }

    public DomainFile generateReport(List<Trip> trips) throws IOException, IllegalAccessException {
        trips = trips.stream().sorted(Comparator.comparing(Trip::getPickUpTime)).toList();
        LocalDate startLocalDate = trips.get(0).getPickUpTime().toLocalDateTime().toLocalDate();
        LocalDate endLocalDate = trips.get(trips.size() - 1).getPickUpTime().toLocalDateTime().toLocalDate();
        String fileName = "";
        File newFile = null;
        Long poNumber = null;
        String fileType = "report";
        try {
            if (userService.userIsAdmin()) {
                trips = trips.stream().filter(trip -> trip.getAdminReported() == null || !trip.getAdminReported()).toList();
                if(trips.size() == 0) throw new IllegalArgumentException("No trips to report");
                fileName = trips.get(0).getShip().getName() + "_" + trips.get(0).getHarbour().getName() + "_" + startLocalDate.toString() + "_" + endLocalDate.toString() + "_ADMIN";
                newFile = fileStorageService.convertWorkbookToFile(fileWritingUtil.generateReportAdmin(trips), fileName);
                trips.forEach(trip -> {
                    trip.setAdminReported(true);
                    tripRepository.save(trip);
                });
            } else if (userService.userIsManagerOrOrganizer()) {
                trips = trips.stream().filter(trip -> trip.getOrganizerCompany().equals(userService.getCurrentUser().getOrganizerCompany()) && (trip.getOrganizerReported() == null || !trip.getOrganizerReported())).toList();
                if(trips.size() == 0) throw new IllegalArgumentException("No trips to report");
                fileName = trips.get(0).getShip().getName() + "_" + trips.get(0).getHarbour().getName() + "_" + startLocalDate.toString() + "_" + endLocalDate.toString() + "_MANAGER";
                newFile = fileStorageService.convertWorkbookToFile(fileWritingUtil.generateReportOrganizer(trips), fileName);
                trips.forEach(trip -> {
                    trip.setOrganizerReported(true);
                    tripRepository.save(trip);
                });
            } else if (userService.userIsDispatcher()) {
                trips = trips.stream().filter(trip -> userService.getCurrentUser().getDispatcherCompany().getRegions().contains(trip.getRegion()) && (trip.getOrganizerReported() == null || !trip.getOrganizerReported())).toList();
                if(trips.size() == 0) throw new IllegalArgumentException("No trips to report");
                fileName = trips.get(0).getShip().getName() + "_" + trips.get(0).getHarbour().getName() + "_" + startLocalDate.toString() + "_" + endLocalDate.toString() + "_DISPATCH";
                newFile = fileStorageService.convertWorkbookToFile(fileWritingUtil.generateReportDispatch(trips), fileName);
                trips.forEach(trip -> {
                    trip.setDispatchReported(true);
                    tripRepository.save(trip);
                });
            }
        } catch (Exception e) {
            logger.error("Error generating report: " + e.getMessage());
        } finally {
            fileWritingUtil.reset();
        }
        List<PAX> paxList = new ArrayList<>();
        for (Trip trip : trips) {
            paxList.addAll(trip.getPassengers());
            if(trip.getPoNumber() != null && trip.getPoNumber() != 0){
                poNumber = trip.getPoNumber();
            }
        }
        DomainFile newDomainFile = fileStorageService.storeFile(newFile, poNumber, fileType);
        if(userService.userIsAdmin()){
            paxList.forEach(pax -> {
                pax.setAdminReport(newDomainFile);
                paxService.savePAX(pax);
            });
        }else if(userService.userIsManagerOrOrganizer()){
            paxList.forEach(pax -> {
                pax.setOrganizerReport(newDomainFile);
                paxService.savePAX(pax);
            });
        } else if(userService.userIsDispatcher()){
            paxList.forEach(pax -> {
                pax.setDispatchReport(newDomainFile);
                paxService.savePAX(pax);
            });
        }
        return newDomainFile;
    }

    public FileDownloadObject findResourceRemote(Long domainFileId) throws IOException {
        DomainFile df = domainFileRepository.findById(domainFileId).orElseThrow(()-> new EntityNotFoundException("Could not find file"));
        FileDownloadObject fdo = new FileDownloadObject();
        fdo.setFilename(df.getName());
        fdo.setByteArrayResource(new ByteArrayResource(fileStorageService.readResourceFromBlob(df.getName(),df.getLocation())));
        return fdo;
    }

    public void deleteAndResetReport(Long reportId) throws IllegalAccessException {
        DomainFile currentDomainFile = domainFileRepository.findById(reportId).orElseThrow(()-> new EntityNotFoundException("Could not find file with this id: "+reportId));
        User currentUser = userService.getCurrentUser();
        if(!currentDomainFile.getType().equals("report")) throw new IllegalArgumentException("Wrong fileType");
        if((userService.userIsManagerOrOrganizer() && !currentDomainFile.getOrganizerCompany().equals(currentUser.getOrganizerCompany()))
        || (userService.userIsDispatcher() && !currentDomainFile.getDispatcherCompany().equals(currentUser.getDispatcherCompany()))
        || !userService.userIsAdmin()) throw new IllegalAccessException("This user cannot access this file. Companies does not match and user is not admin");


        if(currentDomainFile.getClassification().equals("ADMIN")){
            List<PAX> associatedPax = currentDomainFile.getAdminFileContent();
            Set<Trip> associatedTrips = associatedPax.stream().map(PAX::getTrip).collect(Collectors.toSet());
            associatedPax.forEach(pax -> {
                pax.setAdminReport(null);
                paxRepository.save(pax);
            });
            associatedTrips.forEach(trip ->{
                trip.setAdminReported(false);
                tripRepository.save(trip);
            });
        } else if (currentDomainFile.getClassification().equals("MANAGER")) {
            List<PAX> associatedPax = currentDomainFile.getOrganizerFileContent();
            Set<Trip> associatedTrips = associatedPax.stream().map(PAX::getTrip).collect(Collectors.toSet());
            associatedPax.forEach(pax -> {
                pax.setOrganizerReport(null);
                paxRepository.save(pax);
            });
            associatedTrips.forEach(trip ->{
                trip.setAdminReported(false);
                tripRepository.save(trip);
            });
        } else if (currentDomainFile.getClassification().equals("DISPATCHER")) {
            List<PAX> associatedPax = currentDomainFile.getOrganizerFileContent();
            Set<Trip> associatedTrips = associatedPax.stream().map(PAX::getTrip).collect(Collectors.toSet());
            associatedPax.forEach(pax -> {
                pax.setDispatchReport(null);
                paxRepository.save(pax);
            });
            associatedTrips.forEach(trip ->{
                trip.setAdminReported(false);
                tripRepository.save(trip);
            });
        }
        fileStorageService.deleteBlobFile(currentDomainFile.getLocation()+"/"+currentDomainFile.getName());
        domainFileRepository.delete(currentDomainFile);
    }
}
