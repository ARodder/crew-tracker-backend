package net.aroder.TripTracker.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import net.aroder.TripTracker.exceptions.ExcelInformationException;
import net.aroder.TripTracker.models.DTOs.UploadFileResponse;
import net.aroder.TripTracker.models.DomainFile;
import net.aroder.TripTracker.models.OrganizerCompany;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.aroder.TripTracker.models.PAX;
import net.aroder.TripTracker.util.FileReadingUtil;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.naming.InvalidNameException;

/**
 * The BookingService class handles operations related to bookings and trips.
 * It provides methods for reading an Excel file, confirming orders, canceling passengers,
 * creating trips, and checking passenger existence.
 */
@Service
public class BookingService {
    Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    private FileReadingUtil frUtil;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private UserService userService;
    @Autowired
    private TripService tripService;
    @Autowired
    private PAXService paxService;
    @Autowired
    private OrganizerCompanyService organizerCompanyService;


    /**
     * Reads an Excel file and extracts PAX (passenger) information.
     *
     * @param file The Excel file to be read.
     * @return A list of PAX objects containing the extracted passenger information.
     * @throws IOException If an I/O error occurs during file reading.
     */
    public List<PAX> readExcelFile(MultipartFile file, String companyName) throws IOException, ExcelInformationException, IllegalAccessException {
        if (file == null) throw new FileNotFoundException();
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        List<PAX> newPax = new ArrayList<>();
        OrganizerCompany organizerCompany;

        if (userService.userIsAdmin() && companyName != null) {
            organizerCompany = organizerCompanyService.findOrganizerCompanyByName(companyName);
        } else if (userService.userIsManagerOrOrganizer() && userService.getCurrentUser().getOrganizerCompany() != null) {
            organizerCompany = userService.getCurrentUser().getOrganizerCompany();
        } else throw new IllegalAccessException("User does not meet the requirements to upload a file");
        Long poNumber = null;
        for (Sheet sheet : workbook) {
            if (sheet.getSheetName().toLowerCase().contains("hotel")) {
                continue;
            }
            try {
                frUtil.findHarbourAndShip(sheet, organizerCompany);
            } catch (InvalidNameException e) {
                logger.error(e.getMessage());
                continue;
            }

            boolean headerRowFound = false;
            for (Row row : sheet) {
                if (headerRowFound) {
                    PAX pax = null;
                    try {
                        pax = frUtil.readRow(row, workbook);
                    } catch (Exception e) {
                        logger.error("Failed to read PAX from file");
                    }
                    if (pax == null) continue;
                    if (pax.getFirstName() == null
                            || pax.getFirstName().trim().length() == 0
                            || pax.getSurname() == null
                            || pax.getSurname().trim().length() == 0) continue;
                    if (paxService.checkPassengerExistByTime(pax) && !(pax.getStatus().equals("change") || pax.getStatus().equals("cancel"))) {
                        pax.setError("Passenger already exist with this name and time");
                    }
                    newPax.add(pax);
                    if(pax.getPoNumber() != null && pax.getPoNumber() != 0){
                        poNumber = pax.getPoNumber();
                    }
                }
                if (!headerRowFound) {
                    headerRowFound = frUtil.isHeaderRow(row);
                }
            }
        }

        newPax = this.checkPaxConflicts(newPax);

        this.uploadFileLocal(file,poNumber, frUtil.getShip().getName() + "_" + frUtil.getHarbour().getName() + "_" + new Timestamp(System.currentTimeMillis()) + "_BOOKING.xlsx", "booking");
        frUtil.reset();
        return newPax;
    }

    /**
     * Confirms the booking orders based on the provided list of PAX objects.
     *
     * @param paxList The list of PAX objects representing the booking orders.
     */
    public void confirmOrder(List<PAX> paxList, String companyName) throws IllegalAccessException {
        OrganizerCompany foundOrganizerCompany = null;
        if (userService.userIsAdmin() && companyName != null && !companyName.trim().equals("")) {
            foundOrganizerCompany = organizerCompanyService.findOrganizerCompanyByName(companyName);
        } else if (userService.userIsManagerOrOrganizer() && userService.getCurrentUser().getOrganizerCompany() != null) {
            foundOrganizerCompany = userService.getCurrentUser().getOrganizerCompany();
        } else throw new IllegalAccessException("User does not meet the requirements to confirm orders");
        paxList = paxList.stream().filter(pax -> pax.getPickUpTime() != null && pax.getPaxValid()).toList();


        List<PAX> newPax = paxList.stream().filter(pax -> pax.getStatus().equals("add") && !paxService.checkPassengerExactExist(pax)).toList();

        List<PAX> canceledPax = paxList.stream().filter(pax -> pax.getStatus().equals("cancel") && paxService.checkPassengerExistByTime(pax)).toList();

        List<PAX> changedPax = paxList.stream().filter(pax -> pax.getStatus().equals("change")).toList();

        tripService.createTrips(newPax, foundOrganizerCompany);
        paxService.cancelMultiplePax(canceledPax, foundOrganizerCompany);
        paxService.changePax(changedPax);
    }


    /**
     * Saves a file locally when it is uploaded.
     *
     * @param file the file to save.
     * @return location of the saved file.
     */
    public UploadFileResponse uploadFileLocal(MultipartFile file, Long poNumber,String newFileName, String type) throws IllegalAccessException {
        DomainFile domainFile = fileStorageService.storeMultipartFile(file, poNumber,newFileName, type);


        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(domainFile.getLocation())
                .path(domainFile.getName())
                .toUriString();

        return new UploadFileResponse(domainFile.getName(), fileDownloadUri, file.getContentType(), file.getSize());
    }

    public List<PAX> checkPaxConflicts(List<PAX> paxList) {
        List<PAX> searchList = new ArrayList<>(paxList);
        List<PAX> completedList = new ArrayList<>();
        while (searchList.size() > 0) {
            PAX currentPax = searchList.remove(0);
            completedList.add(currentPax);
            if (searchList.size() == 0) break;
            List<PAX> conflictingPax = searchList
                    .stream()
                    .filter(pax ->
                            pax.getFirstName().equals(currentPax.getFirstName())
                                    && pax.getSurname().equals(currentPax.getSurname())
                                    && pax.getPickUpTime() != null
                                    && currentPax.getPickUpTime() != null
                                    && pax.getPickUpTime().compareTo(currentPax.getPickUpTime()) == 0).toList();
            searchList.removeAll(conflictingPax);
            conflictingPax.forEach(pax ->{
                pax.setPaxValid(false);
                pax.setError("Conflict, passenger cant have multiple trips at one time");
            });

            completedList.addAll(conflictingPax);
        }

        return completedList;
    }


}
