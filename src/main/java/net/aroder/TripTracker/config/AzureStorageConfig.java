package net.aroder.TripTracker.config;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureStorageConfig {
    private final String connectionString;
    private final String containerName;

    public AzureStorageConfig(@Value("${azure.storage.connection.string}")final String connectionString, @Value("${azure.storage.container.name}")final String containerName) {
        this.connectionString = connectionString;
        this.containerName = containerName;
    }

    @Bean
    public BlobServiceClient blobServiceClient() {
        return new BlobServiceClientBuilder()
                .connectionString(this.connectionString)
                .buildClient();
    }

    @Bean
    public BlobContainerClient blobContainerClient() {
        return blobServiceClient()
                        .getBlobContainerClient(this.containerName);

    }

}
