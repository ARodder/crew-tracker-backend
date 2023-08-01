package net.aroder.TripTracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import net.aroder.TripTracker.config.FileStorageProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({
	FileStorageProperties.class
})
public class TripTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TripTrackerApplication.class, args);
	}

}
