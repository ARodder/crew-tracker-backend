package net.aroder.TripTracker.util;

import net.aroder.TripTracker.models.Location;
import net.aroder.TripTracker.models.PAX;
import net.aroder.TripTracker.models.Ship;
import net.aroder.TripTracker.models.Trip;
import net.aroder.TripTracker.repositories.ShipRepository;
import net.aroder.TripTracker.services.LocationService;
import net.aroder.TripTracker.services.PAXService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileWritingUtil {
    private final List<String> firstNameSynonyms = List.of("firstname", "onsigners", "ofsigners");
    private final Set<String> headerContentSet;
    private final List<String> surNameSynonyms = List.of("surname", "lastname", "onsigners", "ofsigners");
    private final List<String> flightSynonyms = List.of("flight");
    private final List<String> dateSynonyms = List.of("dato", "date");
    private final List<String> timeSynonyms = List.of("time");
    private final List<String> pickUpLocationSynonyms = List.of("pickuplocation", "from");
    private final List<String> destinationSynonyms = List.of("to", "destination");
    private final List<String> immigrationSynonyms = List.of("immigration", "immigr", "immig");
    private final List<String> hotelSynonyms = List.of("hotel");
    private final List<String> organizationSynonyms = List.of("organization", "job", "department");
    private final List<String> remarksSynonyms = List.of("remarks", "comments", "customernote", "remarksinvoiceinfo");
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
    @Autowired
    private ResourceLoader resourceLoader;


    /**
     * Constructor for FileReadingUtil
     * initializing headerContentSet variable.
     */
    public FileWritingUtil() {
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


    public XSSFWorkbook generateReportAdmin(List<Trip> trips) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(resourceLoader.getResource("classpath:data/Transport_report_template_admin.xlsx").getInputStream());
        XSSFCellStyle style = getBorderCellStyle(workbook);
        boolean headerRowFound = false;

        for (Sheet sheet : workbook) {
            sheet.getRow(0).getCell(1).setCellValue(trips.get(0).getShip().getName());
            int rowIndex = 0;
            for (rowIndex = 0; rowIndex < sheet.getLastRowNum(); rowIndex++) {
                if (headerRowFound) {
                    break;
                }
                headerRowFound = isHeaderRow(sheet.getRow(rowIndex));
            }
            for (Trip trip : trips) {
                for (PAX passenger : trip.getPassengers()) {
                    Row row = sheet.createRow(rowIndex);

                    row.createCell(firstNameIndex).setCellValue(passenger.getFirstName());
                    row.getCell(firstNameIndex).setCellStyle(style);

                    row.createCell(surNameIndex).setCellValue(passenger.getSurname());
                    row.getCell(surNameIndex).setCellStyle(style);

                    row.createCell(flightIndex).setCellValue(passenger.getFlight() != null ? passenger.getFlight() : "");
                    row.getCell(flightIndex).setCellStyle(style);

                    row.createCell(timeIndex).setCellValue(trip.getPickUpTime().toLocalDateTime().toLocalTime().toString());
                    row.getCell(timeIndex).setCellStyle(style);

                    row.createCell(dateIndex).setCellValue(trip.getPickUpTime().toLocalDateTime().toLocalDate().toString());
                    row.getCell(dateIndex).setCellStyle(style);

                    row.createCell(pickUpLocationIndex).setCellValue(trip.getPickUpLocation().getName());
                    row.getCell(pickUpLocationIndex).setCellStyle(style);

                    row.createCell(destinationIndex).setCellValue(trip.getDestination().getName());
                    row.getCell(destinationIndex).setCellStyle(style);

                    row.createCell(immigrationIndex).setCellValue(passenger.isImmigration() ? "yes" : "no");
                    row.getCell(immigrationIndex).setCellStyle(style);

                    row.createCell(organizationIndex).setCellValue(passenger.getOrganization() != null ? passenger.getOrganization() : "");
                    row.getCell(organizationIndex).setCellStyle(style);
                    if (remarksIndex != null) {
                        String remarks = "";
                        if(passenger.getRemarks() != null) {
                            remarks = passenger.getRemarks()+";";
                        }
                        if (trip.getDriverRemarks() != null){
                            remarks += trip.getDriverRemarks();
                        }

                        row.createCell(remarksIndex).setCellValue(remarks);
                        row.getCell(remarksIndex).setCellStyle(style);
                    }
                    row.createCell(poIndex).setCellValue(passenger.getPoNumber() != null && passenger.getPoNumber() != 0 ? passenger.getPoNumber().toString() : "");
                    row.getCell(poIndex).setCellStyle(style);

                    row.createCell(11).setCellValue(roundLong(trip.getSubContractorPrice() / trip.getActivePassengers(), 2));
                    row.getCell(11).setCellStyle(style);

                    row.createCell(12).setCellValue(roundLong(trip.getExternalPrice() / trip.getActivePassengers(), 2));
                    row.getCell(12).setCellStyle(style);


                    rowIndex++;
                }
            }
        }

        return workbook;
    }

    public XSSFWorkbook generateReportDispatch(List<Trip> trips) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(resourceLoader.getResource("classpath:data/Transport_report_template_standard.xlsx").getInputStream());
        XSSFCellStyle style = getBorderCellStyle(workbook);
        boolean headerRowFound = false;

        for (Sheet sheet : workbook) {
            sheet.getRow(0).getCell(1).setCellValue(trips.get(0).getShip().getName());
            int rowIndex = 0;
            for (rowIndex = 0; rowIndex < sheet.getLastRowNum(); rowIndex++) {
                if (headerRowFound) {
                    break;
                }
                headerRowFound = isHeaderRow(sheet.getRow(rowIndex));
            }
            for (Trip trip : trips) {
                for (PAX passenger : trip.getPassengers()) {
                    Row row = sheet.createRow(rowIndex);

                    row.createCell(firstNameIndex).setCellValue(passenger.getFirstName());
                    row.getCell(firstNameIndex).setCellStyle(style);

                    row.createCell(surNameIndex).setCellValue(passenger.getSurname());
                    row.getCell(surNameIndex).setCellStyle(style);

                    row.createCell(flightIndex).setCellValue(passenger.getFlight() != null ? passenger.getFlight() : "");
                    row.getCell(flightIndex).setCellStyle(style);

                    row.createCell(timeIndex).setCellValue(trip.getPickUpTime().toLocalDateTime().toLocalTime().toString());
                    row.getCell(timeIndex).setCellStyle(style);

                    row.createCell(dateIndex).setCellValue(trip.getPickUpTime().toLocalDateTime().toLocalDate().toString());
                    row.getCell(dateIndex).setCellStyle(style);

                    row.createCell(pickUpLocationIndex).setCellValue(trip.getPickUpLocation().getName());
                    row.getCell(pickUpLocationIndex).setCellStyle(style);

                    row.createCell(destinationIndex).setCellValue(trip.getDestination().getName());
                    row.getCell(destinationIndex).setCellStyle(style);

                    row.createCell(immigrationIndex).setCellValue(passenger.isImmigration() ? "yes" : "no");
                    row.getCell(immigrationIndex).setCellStyle(style);

                    row.createCell(organizationIndex).setCellValue(passenger.getOrganization() != null ? passenger.getOrganization() : "");
                    row.getCell(organizationIndex).setCellStyle(style);
                    if (remarksIndex != null) {
                        row.createCell(remarksIndex).setCellValue(passenger.getRemarks() != null ? passenger.getRemarks() : "");
                        row.getCell(remarksIndex).setCellStyle(style);
                    }
                    row.createCell(poIndex).setCellValue(passenger.getPoNumber() != null && passenger.getPoNumber() != 0 ? passenger.getPoNumber().toString() : "");
                    row.getCell(poIndex).setCellStyle(style);

                    row.createCell(11).setCellValue(roundLong(trip.getSubContractorPrice() / trip.getActivePassengers(), 2));
                    row.getCell(11).setCellStyle(style);

                    rowIndex++;
                }
            }
        }
        return workbook;
    }

    public XSSFWorkbook generateReportOrganizer(List<Trip> trips) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(resourceLoader.getResource("classpath:data/Transport_report_template_standard.xlsx").getInputStream());
        XSSFCellStyle style = getBorderCellStyle(workbook);
        boolean headerRowFound = false;

        for (Sheet sheet : workbook) {
            sheet.getRow(0).getCell(1).setCellValue(trips.get(0).getShip().getName());
            int rowIndex = 0;
            for (rowIndex = 0; rowIndex < sheet.getLastRowNum(); rowIndex++) {
                if (headerRowFound) {
                    break;
                }
                headerRowFound = isHeaderRow(sheet.getRow(rowIndex));
            }
            for (Trip trip : trips) {
                for (PAX passenger : trip.getPassengers()) {
                    Row row = sheet.createRow(rowIndex);

                    row.createCell(firstNameIndex).setCellValue(passenger.getFirstName());
                    row.getCell(firstNameIndex).setCellStyle(style);

                    row.createCell(surNameIndex).setCellValue(passenger.getSurname());
                    row.getCell(surNameIndex).setCellStyle(style);

                    row.createCell(flightIndex).setCellValue(passenger.getFlight() != null ? passenger.getFlight() : "");
                    row.getCell(flightIndex).setCellStyle(style);

                    row.createCell(timeIndex).setCellValue(trip.getPickUpTime().toLocalDateTime().toLocalTime().toString());
                    row.getCell(timeIndex).setCellStyle(style);

                    row.createCell(dateIndex).setCellValue(trip.getPickUpTime().toLocalDateTime().toLocalDate().toString());
                    row.getCell(dateIndex).setCellStyle(style);

                    row.createCell(pickUpLocationIndex).setCellValue(trip.getPickUpLocation().getName());
                    row.getCell(pickUpLocationIndex).setCellStyle(style);

                    row.createCell(destinationIndex).setCellValue(trip.getDestination().getName());
                    row.getCell(destinationIndex).setCellStyle(style);

                    row.createCell(immigrationIndex).setCellValue(passenger.isImmigration() ? "yes" : "no");
                    row.getCell(immigrationIndex).setCellStyle(style);

                    row.createCell(organizationIndex).setCellValue(passenger.getOrganization() != null ? passenger.getOrganization() : "");
                    row.getCell(organizationIndex).setCellStyle(style);
                    if (remarksIndex != null) {
                        row.createCell(remarksIndex).setCellValue(passenger.getRemarks() != null ? passenger.getRemarks() : "");
                        row.getCell(remarksIndex).setCellStyle(style);
                    }
                    row.createCell(poIndex).setCellValue(passenger.getPoNumber() != null && passenger.getPoNumber() != 0 ? passenger.getPoNumber().toString() : "");
                    row.getCell(poIndex).setCellStyle(style);

                    row.createCell(11).setCellValue(roundLong(trip.getExternalPrice() / trip.getActivePassengers(), 2));
                    row.getCell(11).setCellStyle(style);

                    rowIndex++;
                }
            }
        }
        return workbook;
    }


    public XSSFCellStyle getBorderCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());

        return style;
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

    public Double roundLong(Double value, Integer decimalPlaces) {
        Double roundedValue = Math.pow(10, decimalPlaces) * value;
        roundedValue = (double) Math.round(roundedValue);
        roundedValue = roundedValue / Math.pow(10, decimalPlaces);

        return roundedValue;
    }



    public XSSFWorkbook generateTripExport(List<Trip> trips) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(resourceLoader.getResource("classpath:data/Transport_report_template_standard.xlsx").getInputStream());
        XSSFCellStyle style = getBorderCellStyle(workbook);
        boolean headerRowFound = false;
        for (Sheet sheet : workbook) {
            sheet.getRow(0).getCell(1).setCellValue(trips.get(0).getShip().getName());
            int rowIndex = 0;
            for (rowIndex = 0; rowIndex < sheet.getLastRowNum(); rowIndex++) {
                if (headerRowFound) {
                    break;
                }
                headerRowFound = isHeaderRow(sheet.getRow(rowIndex));
            }
            for (Trip trip : trips) {
                for (PAX passenger : trip.getPassengers()) {
                    Row row = sheet.createRow(rowIndex);

                    row.createCell(firstNameIndex).setCellValue(passenger.getFirstName());
                    row.getCell(firstNameIndex).setCellStyle(style);

                    row.createCell(surNameIndex).setCellValue(passenger.getSurname());
                    row.getCell(surNameIndex).setCellStyle(style);

                    row.createCell(flightIndex).setCellValue(passenger.getFlight() != null ? passenger.getFlight() : "");
                    row.getCell(flightIndex).setCellStyle(style);

                    row.createCell(timeIndex).setCellValue(trip.getPickUpTime().toLocalDateTime().toLocalTime().toString());
                    row.getCell(timeIndex).setCellStyle(style);

                    row.createCell(dateIndex).setCellValue(trip.getPickUpTime().toLocalDateTime().toLocalDate().toString());
                    row.getCell(dateIndex).setCellStyle(style);

                    row.createCell(pickUpLocationIndex).setCellValue(trip.getPickUpLocation().getName());
                    row.getCell(pickUpLocationIndex).setCellStyle(style);

                    row.createCell(destinationIndex).setCellValue(trip.getDestination().getName());
                    row.getCell(destinationIndex).setCellStyle(style);

                    row.createCell(immigrationIndex).setCellValue(passenger.isImmigration() ? "yes" : "no");
                    row.getCell(immigrationIndex).setCellStyle(style);

                    row.createCell(organizationIndex).setCellValue(passenger.getOrganization() != null ? passenger.getOrganization() : "");
                    row.getCell(organizationIndex).setCellStyle(style);
                    if (remarksIndex != null) {
                        row.createCell(remarksIndex).setCellValue(passenger.getRemarks() != null ? passenger.getRemarks() : "");
                        row.getCell(remarksIndex).setCellStyle(style);
                    }
                    row.createCell(poIndex).setCellValue(passenger.getPoNumber() != null && passenger.getPoNumber() != 0 ? passenger.getPoNumber().toString() : "");
                    row.getCell(poIndex).setCellStyle(style);

                    rowIndex++;
                }
                rowIndex++;
            }
        }
        return workbook;
    }
}
