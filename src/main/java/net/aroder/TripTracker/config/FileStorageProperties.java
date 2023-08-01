package net.aroder.TripTracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configures properties for storing uploaded files
 */
@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {
    private String uploadDir;

    /**
     * Retrieves the directory for saving the uploaded files.
     *
     * @return Directory for saving uploaded files.
     */
    public String getUploadDir() {
        return uploadDir;
    }

    /**
     * Sets the directory for the uploaded files.
     *
     * @param uploadDir string representing the path to the uploaded files.
     */
    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }
}
