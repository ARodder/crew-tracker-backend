package net.aroder.TripTracker.models;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * An entity class representing a domain file.
 */
@Entity
@Data
public class DomainFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String name;
    private Long size;
    private String location;
    private Long poNumber;
    private String type;
    private Timestamp createdAt;
    private String classification;
    @OneToMany(mappedBy = "adminReport")
    private List<PAX> adminFileContent = new ArrayList<>();
    @OneToMany(mappedBy = "dispatchReport")
    private List<PAX> dispatchFileContent = new ArrayList<>();
    @OneToMany(mappedBy = "organizerReport")
    private List<PAX> organizerFileContent = new ArrayList<>();
    @ManyToOne
    private OrganizerCompany organizerCompany;
    @ManyToOne
    private User createdBy;
    @ManyToOne
    private DispatcherCompany dispatcherCompany;


}
