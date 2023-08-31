package net.aroder.TripTracker.Scheduling;

import net.aroder.TripTracker.repositories.PAXRepository;
import net.aroder.TripTracker.repositories.TripRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.sql.Timestamp;

@Configuration
@EnableScheduling
public class SchedulingConfig {

    private final Logger logger = LoggerFactory.getLogger(SchedulingConfig.class);
    private final PAXRepository paxRepository;
    private final TripRepository tripRepository;

    public SchedulingConfig(final PAXRepository paxRepository, final TripRepository tripRepository){
        this.paxRepository = paxRepository;
        this.tripRepository = tripRepository;
    }


    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Oslo")
    public void cleanExpiredPax(){
        logger.info("Running scheduled PAX cleaner");
        Timestamp now = new Timestamp(System.currentTimeMillis());
        paxRepository.deleteAll(paxRepository.findByExpirationDateBefore(now));
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Oslo")
    public void cleanExpiredTrip(){
        logger.info("Running scheduled Trip cleaner");
        Timestamp now = new Timestamp(System.currentTimeMillis());
        tripRepository.deleteAll(tripRepository.findByExpirationDateBefore(now));
    }
}
