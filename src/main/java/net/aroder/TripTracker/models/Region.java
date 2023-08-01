package net.aroder.TripTracker.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * An entity class representing a Region.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"id", "name", "regionLocation"})
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String name;
    @ManyToOne
    @JoinColumn(name = "dispatcher_company_id")
    @JsonBackReference
    private DispatcherCompany dispatcherCompany;
    @ManyToOne
    @JoinColumn(name = "region_location_id")
    private Location regionLocation;

    @OneToMany(mappedBy = "region")
    private List<Trip> trips;

    public Long getId(){
        return this.id;
    }

    public void setId(Long id){
        this.id = id;
    }

}
