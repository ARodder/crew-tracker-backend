package net.aroder.TripTracker.models.DTOs;

import lombok.Data;
import org.springframework.core.io.ByteArrayResource;

@Data
public class FileDownloadObject {
    private String filename;
    private ByteArrayResource byteArrayResource;
}
