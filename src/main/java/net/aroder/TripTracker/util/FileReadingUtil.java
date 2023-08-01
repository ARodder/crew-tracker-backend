package net.aroder.TripTracker.util;


import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.aroder.TripTracker.controllers.AuthController;
import net.aroder.TripTracker.exceptions.ExcelInformationException;
import net.aroder.TripTracker.models.Location;
import net.aroder.TripTracker.models.OrganizerCompany;
import net.aroder.TripTracker.models.PAX;
import net.aroder.TripTracker.models.Ship;
import net.aroder.TripTracker.repositories.ShipRepository;
import net.aroder.TripTracker.services.LocationService;
import net.aroder.TripTracker.services.PAXService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.InvalidNameException;

/**
 * Utility class specifically for reading a
 * specifically formatted file.
 */
@Service
public class FileReadingUtil {

    private Logger logger = LoggerFactory.getLogger(FileReadingUtil.class);
    private final List<String> firstNameSynonyms = Arrays.asList("firstname", "onsigners", "ofsigners");
    private Set<String> headerContentSet;
    private final List<String> surNameSynonyms = List.of("surname", "lastname", "onsigners", "ofsigners");
    private final List<String> flightSynonyms = List.of("flight");
    private final List<String> dateSynonyms = List.of("dato", "date");
    private final List<String> timeSynonyms = List.of("time");
    private final List<String> pickUpLocationSynonyms = List.of("pickuplocation", "from");
    private final List<String> destinationSynonyms = List.of("to", "destination");
    private final List<String> immigrationSynonyms = List.of("immigration", "immigr", "immig");
    private final List<String> hotelSynonyms = List.of("hotel");
    private final List<String> organizationSynonyms = List.of("organization", "job", "department");
    private final List<String> remarksSynonyms = List.of("remarks", "comments", "customernote");
    private final List<String> poSynonyms = List.of("po");
    private Integer firstNameIndex;
    private Integer surNameIndex;
    private Integer flightIndex;
    private Integer dateIndex;
    private Integer timeIndex;
    private Integer pickUpLocationIndex;
    private Integer destinationIndex;
    private Integer immigrationIndex;
    private Integer hotelIndex;
    private Integer organizationIndex;
    private Integer remarksIndex;
    private Integer poIndex;
    private Location harbour;
    private Ship ship;
    @Autowired
    private ShipRepository shipRepository;
    @Autowired
    private LocationService locationService;
    @Autowired
    private PAXService paxService;


    /**
     * Constructor for FileReadingUtil
     * initializing headerContentSet variable.
     */
    public FileReadingUtil() {
        this.headerContentSet = Stream.of(firstNameSynonyms, surNameSynonyms,
                flightSynonyms, dateSynonyms, timeSynonyms, pickUpLocationSynonyms,
                destinationSynonyms, immigrationSynonyms, hotelSynonyms, organizationSynonyms,
                remarksSynonyms, poSynonyms).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    /**
     * Resets all fields specific for each file.
     */
    public void reset() {
        this.harbour = null;
        this.ship = null;
        this.firstNameIndex = null;
        this.surNameIndex = null;
        this.flightIndex = null;
        this.dateIndex = null;
        this.timeIndex = null;
        this.pickUpLocationIndex = null;
        this.destinationIndex = null;
        this.immigrationIndex = null;
        this.hotelIndex = null;
        this.organizationIndex = null;
        this.remarksIndex = null;
        this.poIndex = null;
    }

    public Ship getShip(){
        return this.ship;
    }
    public Location getHarbour(){
        return this.harbour;
    }

    /**
     * Determines if a given row is a header row
     *
     * @param row row to check for headers
     * @return boolean based on if the header row was found.
     */
    public boolean isHeaderRow(Row row) {
        for (Cell cell : row) {
            if (cell.getCellType() != CellType.STRING) continue;
            if (headerContentSet.contains(cell.getStringCellValue().replace(" ", "").toLowerCase())) {
                this.assignIndexes(row);
                return true;
            }
        }
        return false;
    }

    /**
     * Assigns each index for the different header fields.
     *
     * @param row Row containing headers.
     */
    private void assignIndexes(Row row) {
        for (Cell cell : row) {
            if (cell.getCellType() != CellType.STRING) continue;

            String cellText = cell.getStringCellValue().replace(" ", "").toLowerCase();
            cellText = cellText.replaceAll("[^a-z]", "");

            if (!headerContentSet.contains(cellText)) continue;

            if (firstNameSynonyms.contains(cellText)) {
                this.firstNameIndex = cell.getColumnIndex();
            } else if (surNameSynonyms.contains(cellText)) {
                this.surNameIndex = cell.getColumnIndex();
            } else if (flightSynonyms.contains(cellText)) {
                this.flightIndex = cell.getColumnIndex();
            } else if (dateSynonyms.contains(cellText)) {
                this.dateIndex = cell.getColumnIndex();
            } else if (timeSynonyms.contains(cellText)) {
                this.timeIndex = cell.getColumnIndex();
            } else if (pickUpLocationSynonyms.contains(cellText)) {
                this.pickUpLocationIndex = cell.getColumnIndex();
            } else if (destinationSynonyms.contains(cellText)) {
                this.destinationIndex = cell.getColumnIndex();
            } else if (immigrationSynonyms.contains(cellText)) {
                this.immigrationIndex = cell.getColumnIndex();
            } else if (hotelSynonyms.contains(cellText)) {
                this.hotelIndex = cell.getColumnIndex();
            } else if (organizationSynonyms.contains(cellText)) {
                this.organizationIndex = cell.getColumnIndex();
            } else if (remarksSynonyms.contains(cellText)) {
                this.remarksIndex = cell.getColumnIndex();
            } else if (poSynonyms.contains(cellText)) {
                this.poIndex = cell.getColumnIndex();
            }
        }
    }

    /**
     * Finds the ship and the harbour from predefined fields in the Excel sheet.
     *
     * @param sheet            Excel sheet to find the ship and harbour from.
     * @param organizerCompany organizercompany behind the order.
     * @throws InvalidNameException the name of the sheet is not valid.
     */
    public void findHarbourAndShip(Sheet sheet, OrganizerCompany organizerCompany) throws InvalidNameException, ExcelInformationException {

        String sheetName = sheet.getSheetName();

        for (Cell cell : sheet.getRow(0)) {
            if (cell.getCellType() != CellType.STRING) continue;
            String cellStringValue = cell.getStringCellValue();
            if (cellStringValue.toLowerCase().contains(sheetName.toLowerCase())) {

                String extractedShipName = cellStringValue.substring(cellStringValue.indexOf("\"") + 1, cellStringValue.lastIndexOf("\""));
                Optional<Ship> foundShip = shipRepository.findByName(extractedShipName);

                if (foundShip.isPresent()) {
                    Ship tempShip = foundShip.get();
                    //TODO: What to do if the organizerCompany is wrong.
                    if (tempShip.getOrganizerCompany().equals(organizerCompany)) {
                        this.ship = tempShip;
                    }


                } else {
                    if (extractedShipName.equalsIgnoreCase("ships name"))
                        throw new InvalidNameException("Missing name of the ship");
                    this.ship = new Ship(extractedShipName);
                    ship.setOrganizerCompany(organizerCompany);
                    shipRepository.save(ship);
                }

            } else if (cellStringValue.toLowerCase().contains("harbour") || cellStringValue.toLowerCase().contains("havn")|| cellStringValue.toLowerCase().contains("port")) {
                Cell neighbouringCell = sheet.getRow(0).getCell(cell.getColumnIndex() + 1);
                if (neighbouringCell.getCellType() == CellType.STRING) {
                    this.harbour = locationService.determineLocation(neighbouringCell.getStringCellValue());
                }
                break;
            }
        }
        if( this.harbour == null || this.ship == null) throw new ExcelInformationException("Harbour or Ship could not be found");
    }

    /**
     * Reads Passenger information from a given row
     *
     * @param row      Row to read the passenger from
     * @param workbook Workbook contained in the Excel sheet
     * @return The found passenger.
     */
    public PAX readRow(Row row, Workbook workbook) {
        DataFormatter dataFormatter = new DataFormatter();
        dataFormatter.setUse4DigitYearsInAllDateFormats(true);

        PAX pax = new PAX();
        pax.setPaxValid(true);
        Cell timeCell = row.getCell(timeIndex);
        Cell dateCell = row.getCell(dateIndex);
        if (timeCell != null) {
            timeCell.getCellStyle().setDataFormat((short) 21);
        }

        if (dateCell != null) {
            dateCell.getCellStyle()
                    .setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("yyyy-mm-dd"));
        }

        try {
            Color color = row.getCell(firstNameIndex).getCellStyle().getFillForegroundColorColor();
            XSSFColor red = new XSSFColor(IndexedColors.RED, new DefaultIndexedColorMap());
            XSSFColor yellow = new XSSFColor(IndexedColors.YELLOW, new DefaultIndexedColorMap());

            if (Arrays.equals(((XSSFColor) color).getRGB(), red.getRGB())) {
                pax.setStatus("cancel");
            } else if (Arrays.equals(((XSSFColor) color).getRGB(), yellow.getRGB())) {
                pax.setStatus("change");
            } else {
                pax.setStatus("add");
            }

        } catch (Exception e) {
            pax.setStatus("add");
        }

        try {
            pax.setFirstName(row.getCell(firstNameIndex).getStringCellValue());
        } catch (Exception e) {
            pax.setError("Error in first name field");
        }
        try {
            pax.setSurname(row.getCell(surNameIndex).getStringCellValue());
        } catch (Exception e) {
            pax.setError("Error in surname field");
        }
        try {
            pax.setFlight(row.getCell(flightIndex).getStringCellValue());
        } catch (Exception e) {
            logger.error("Could not read flight: "+e.getMessage());
        }
        try {
            String date = dataFormatter.formatCellValue(row.getCell(dateIndex));
            String time = dataFormatter.formatCellValue(row.getCell(timeIndex));
            if(DateUtil.isAmPmTime(time)){
                time = DateUtil.convertAmPmTime(time);
            }


            String dateTime = date + " " + time;

            String dateTimeFormat = DateUtil.determineDateFormat(dateTime);

            if(dateTimeFormat != null){
                pax.setPickUpTime(formatDateTime(dateTime,dateTimeFormat,"yyyy-MM-dd HH:mm:ss"));
            }else{
                pax.setPickUpTime(Timestamp.valueOf(dateTime));
            }

        } catch (Exception e) {
            pax.setError("Error in pick up time");
            pax.setPaxValid(false);
        }

        try {
            Cell immigrationCell = row.getCell(immigrationIndex);
            if (immigrationCell.getCellType() == CellType.STRING) {
                String immigrationCellContent = immigrationCell.getStringCellValue();
                immigrationCellContent = immigrationCellContent.replace(" ", "");
                pax.setImmigration(immigrationCellContent.equalsIgnoreCase("yes"));
            } else {
                pax.setImmigration(false);
            }

        } catch (Exception e) {
            pax.setError("Error in immigration");
        }
        try {
            pax.setOrganization(row.getCell(organizationIndex).getStringCellValue());
        } catch (Exception e) {
            logger.error("Could not read organization: "+ e.getMessage());
        }
        try {
            pax.setRemarks(row.getCell(remarksIndex).getStringCellValue());
        } catch (Exception e) {
            //System.out.println("No remarks found");
        }
        try {
            pax.setPoNumber((long) row.getCell(poIndex).getNumericCellValue());
        } catch (Exception e) {
            pax.setError("Error in PO field");
        }

        try {
            String pickUpLocation = row.getCell(pickUpLocationIndex).getStringCellValue().trim();
            if (pickUpLocation.equalsIgnoreCase("hotel") && hotelIndex != null) {
                pax.setPickUpLocation(row.getCell(hotelIndex).getStringCellValue().trim().toLowerCase());
            } else if (pickUpLocation.equalsIgnoreCase("ship") && harbour != null) {
                pax.setPickUpLocation(this.harbour.getName().trim().toLowerCase());
            } else {
                pax.setPickUpLocation(pickUpLocation.toLowerCase());
            }

        } catch (Exception e) {
            pax.setError("Error in pick up location");
            pax.setPaxValid(false);
        }
        try {
            String destination = row.getCell(destinationIndex).getStringCellValue().trim();
            if (destination.equalsIgnoreCase("hotel") && hotelIndex != null) {
                pax.setDestination(row.getCell(hotelIndex).getStringCellValue().trim().toLowerCase());
            } else if (destination.replace(" ", "").equalsIgnoreCase("ship") && harbour != null) {
                pax.setDestination(this.harbour.getName().trim().toLowerCase());
            } else {
                pax.setDestination(destination.toLowerCase());
            }
        } catch (Exception e) {
            pax.setError("Error in destination");
            pax.setPaxValid(false);
        }
        if (ship != null) {
            pax.setShip(this.ship);
        }
        if (harbour != null) {
            pax.setHarbour(this.harbour);
        }
        if (pax.getStatus().equals("cancel")) {
            if (!paxService.checkPassengerExactExist(pax)) {
                pax.setError("Passenger does not exist");
                pax.setPaxValid(false);
            }
        }
        if (pax.getStatus().equals("change")) {
            Boolean checkIfExistByLocation = paxService.checkPassengerByLocationsExist(pax);
            Boolean checkIfExistByTime = paxService.checkPassengerExistByTime(pax);
            if (!checkIfExistByLocation && !checkIfExistByTime) {
                pax.setError("Passenger does not exist");
            }
        }
        Date expirationDate = new Date();
        expirationDate = DateUtil.addMonths(expirationDate,30);
        pax.setExpirationDate(new Timestamp(expirationDate.getTime()));
        return pax;
    }


    private boolean isDateValid(String inputDate, String dateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        sdf.setLenient(false); // Set parsing to be strict

        try {
            sdf.parse(inputDate);
            return true; // Parsing successful, date is valid
        } catch (ParseException e) {
            return false; // Parsing failed, date is not valid or doesn't match the format
        }
    }

    private Timestamp formatDateTime(String inputDateTime,String inputFormat,String outputFormat) throws ParseException {

            // Parse input string using input format
            SimpleDateFormat inputDateFormat = new SimpleDateFormat(inputFormat);
            java.util.Date parsedDate = inputDateFormat.parse(inputDateTime);

            // Format parsed date to the desired output format
            SimpleDateFormat outputDateFormat = new SimpleDateFormat(outputFormat);
            String formattedTimestamp = outputDateFormat.format(parsedDate);

            // Create a Timestamp object using the formatted timestamp string
            return Timestamp.valueOf(formattedTimestamp);
    }

}
