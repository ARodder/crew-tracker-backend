package net.aroder.TripTracker.services;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import net.aroder.TripTracker.models.DomainFile;
import net.aroder.TripTracker.models.PAX;
import net.aroder.TripTracker.repositories.DomainFileRepository;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.WritableResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import org.springframework.beans.factory.annotation.Autowired;
import net.aroder.TripTracker.config.FileStorageProperties;
import net.aroder.TripTracker.exceptions.FileStorageException;
import net.aroder.TripTracker.exceptions.MyFileNotFoundException;
import org.springframework.util.StringUtils;

import java.nio.file.StandardCopyOption;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.net.MalformedURLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * The FileStorageService handles the saving of uploaded files
 */
@Service
public class FileStorageService {

    @Autowired
    private DomainFileRepository domainFileRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private BlobServiceClient blobServiceClient;
    @Autowired
    private BlobContainerClient blobContainerClient;

    /**
     * Stores a file locally
     *
     * @param file file to save.
     * @return name of the saved file.
     */
    public DomainFile storeFile(File file,Long poNumber, String fileType) throws IllegalAccessException {
        // Normalize file name
        String targetLocation = determineDirectory(fileType);
        String fileName = StringUtils.cleanPath(file.getName());

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }
            InputStream fileInputStream = new FileInputStream(file);
            writeResourceToBlob(targetLocation+fileName,fileInputStream);
            fileInputStream.close();

            file.delete();
            return createDomainFileReference(fileName,poNumber, Path.of(targetLocation), file.length(), fileType);
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }



    public DomainFile storeMultipartFile(MultipartFile file,Long poNumber,String newFileName,String type) throws IllegalAccessException {
        // Normalize file name
        String targetLocation = determineDirectory(type);
        String fileName = (newFileName != null ? newFileName : StringUtils.cleanPath(file.getOriginalFilename()));

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }
            writeResourceToBlob(targetLocation+fileName,file.getInputStream());
            return createDomainFileReference(fileName, poNumber,Path.of(targetLocation), file.getSize(), "Order");

        } catch (IOException | IllegalAccessException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }


    public DomainFile createDomainFileReference(String fileName, Long poNumber, Path targetLocation, Long size, String type) throws IllegalAccessException {
        DomainFile domainFile = new DomainFile();
        domainFile.setName(fileName);
        domainFile.setLocation(targetLocation.toString());
        domainFile.setSize(size);
        domainFile.setType(type);
        domainFile.setPoNumber(poNumber);
        Timestamp createdAt = new Timestamp(System.currentTimeMillis());
        domainFile.setCreatedAt(createdAt);
        domainFile.setCreatedBy(userService.getCurrentUser());

        if (userService.userIsAdmin()) {
            domainFile.setClassification("ADMIN");
        } else if (userService.userIsManagerOrOrganizer()) {
            domainFile.setClassification("MANAGER");
            domainFile.setOrganizerCompany(userService.getCurrentUser().getOrganizerCompany());
        } else if (userService.userIsDispatcher()) {
            domainFile.setClassification("DISPATCHER");
            domainFile.setDispatcherCompany(userService.getCurrentUser().getDispatcherCompany());
        } else throw new IllegalAccessException("User is not authorized to generate files");
        return domainFileRepository.save(domainFile);
    }

    public File convertWorkbookToFile(XSSFWorkbook workbook, String fileName) throws IOException {
        File tempFile = new File(fileName + ".xlsx");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            workbook.write(fos);
        }

        return tempFile;
    }

    public void writeResourceToBlob(String fileName, InputStream is) throws IOException {
        BlobClient blobClient = blobContainerClient.getBlobClient(fileName);
        blobClient.upload(is,true);
    }

    public byte[] readResourceFromBlob(String fileName,String location) throws IOException {
        BlobClient blobClient = blobContainerClient.getBlobClient(location+"/"+fileName);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        blobClient.downloadStream(os);
        final byte[] bytes = os.toByteArray();
        return bytes;
    }

    public String determineDirectory(String type) throws IllegalAccessException {
        if(userService.userIsAdmin()){
            return "admin/"+type+"/";
        }else if(userService.userIsDispatcher()){
            return userService.getCurrentUser().getDispatcherCompany().getName()+"/"+type+"/";
        }else if (userService.userIsManagerOrOrganizer()){
            return userService.getCurrentUser().getOrganizerCompany().getName()+"/"+type+"/";
        }else throw new IllegalAccessException();
    }

    public void deleteBlobFile(String fileName){
        BlobClient blobClient = blobContainerClient.getBlobClient(fileName);
        blobClient.delete();
    }
}