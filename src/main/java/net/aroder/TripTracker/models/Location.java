package net.aroder.TripTracker.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An entity class representing a location.
 */
@Entity
@Data
@NoArgsConstructor
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;
    private Double longitude;
    private Double latitude;

    /**
     * Constructs a location with the given name.
     *
     * @param name The name of the location.
     */
    public Location(String name){
        this.name = name;
    }
    /**
     * Constructs a location with the given name, longitude, and latitude.
     *
     * @param name      The name of the location.
     * @param longitude The longitude of the location.
     * @param latitude  The latitude of the location.
     */
    public Location(String name,Double longitude,Double latitude){
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
    }

}
