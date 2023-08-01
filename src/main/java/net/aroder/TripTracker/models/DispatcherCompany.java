package net.aroder.TripTracker.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Fetch;

import java.util.ArrayList;
import java.util.List;

/**
 * An entity class representing a Dispatcher Company.
 */
@Entity
@Data
public class DispatcherCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String name;
    @OneToMany(mappedBy="dispatcherCompany")
    private List<User> drivers = new ArrayList<>();
    @OneToMany(mappedBy = "dispatcherCompany", fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Region> regions = new ArrayList<>();
}
