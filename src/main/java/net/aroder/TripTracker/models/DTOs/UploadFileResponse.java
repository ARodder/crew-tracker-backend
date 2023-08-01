package net.aroder.TripTracker.models.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A data class representing a response for uploading a file.
 */
@Data
@AllArgsConstructor
public class UploadFileResponse {
   private String fileName;
   private String fileDownloadUri;
   private String fileType;
   private long size;
}
