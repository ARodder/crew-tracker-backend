package net.aroder.TripTracker.mappers;

import net.aroder.TripTracker.models.DTOs.CompanyDTOs.DispatcherCompanyDTO;
import net.aroder.TripTracker.models.DTOs.CompanyDTOs.OrganizerCompanyDTO;
import net.aroder.TripTracker.models.DTOs.UserDTOs.DriverDTO;
import net.aroder.TripTracker.models.DTOs.UserDTOs.SimpleUserDTO;
import net.aroder.TripTracker.models.DTOs.UserDTOs.UserDTO;
import net.aroder.TripTracker.models.DispatcherCompany;
import net.aroder.TripTracker.models.OrganizerCompany;
import net.aroder.TripTracker.models.User;
import net.aroder.TripTracker.services.DispatcherCompanyService;
import net.aroder.TripTracker.services.OrganizerCompanyService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;

/**
 * Mapper class for converting between User entities and DTOs.
 * Uses MapStruct library for mapping operations.
 */
@Mapper(componentModel = "spring")
public abstract class UserMapper {
    @Autowired
    private DispatcherCompanyMapper dispatcherCompanyMapper;
    @Autowired
    private OrganizerCompanyMapper organizerCompanyMapper;
    @Autowired
    private DispatcherCompanyService dispatcherCompanyService;
    @Autowired
    private OrganizerCompanyService organizerCompanyService;


    /**
     * Converts a User entity to a UserDTO.
     *
     * @param user The User entity to be converted.
     * @return The corresponding UserDTO.
     */

    @Mapping(source="dispatcherCompany",target = "dispatcherCompany",qualifiedByName = "dispatcherCompanyToDispatcherCompanyDto")
    @Mapping(source="organizerCompany",target = "organizerCompany",qualifiedByName = "organizerCompanyToOrganizerCompanyDto")
    public abstract UserDTO toUserDTO(User user);

    public abstract DriverDTO toDriverDTO(User user);

    @Mapping(source="dispatcherCompany",target = "dispatcherCompany",qualifiedByName = "dispatcherCompanyDtoToDispatcherCompany")
    @Mapping(source="organizerCompany",target = "organizerCompany",qualifiedByName = "organizerCompanyDtoToOrganizerCompany")
    public abstract User userDtoToUser(UserDTO userDTO);

    public abstract SimpleUserDTO toSimpleUserDTO(User user);
    public abstract List<SimpleUserDTO> toSimpleUserDTO(List<User> user);

    @Named("dispatcherCompanyDtoToDispatcherCompany")
    public DispatcherCompany dispatcherCompanyDtoToDispatcherCompany(DispatcherCompanyDTO dispatcherCompanyDTO){
        return dispatcherCompanyDTO != null ? dispatcherCompanyService.findDispatcherCompanyById(dispatcherCompanyDTO.getId()):null;
    }
    @Named("organizerCompanyDtoToOrganizerCompany")
    public OrganizerCompany organizerCompanyDtoToOrganizerCompany(OrganizerCompanyDTO organizerCompanyDTO){
        return organizerCompanyDTO != null ? organizerCompanyService.findOrganizerCompanyById(organizerCompanyDTO.getId()):null;
    }


    @Named("organizerCompanyToOrganizerCompanyDto")
    public OrganizerCompanyDTO organizerCompanyToOrganizerCompanyDto(OrganizerCompany organizerCompany){
        return organizerCompany != null ? organizerCompanyMapper.toOrganizerCompanyDTO(organizerCompany):null;
    }



    /**
     * Converts a collection of User entities to a collection of UserDTOs.
     *
     * @param user The collection of User entities to be converted.
     * @return The corresponding collection of UserDTOs.
     */
    public abstract Collection<UserDTO> toUserDTO(Collection<User> user);


    /**
     * Converts an OrganizerCompany object to its corresponding ID (organizer company ID).
     *
     * @param organizerCompany The OrganizerCompany object to be converted.
     * @return The ID of the organizer company.
     */
    @Named("organizerCompanyToOrganizerCompanyId")
    public Long organizerCompanyToOrganizerCompanyId(OrganizerCompany organizerCompany){
        return organizerCompany != null ? organizerCompany.getId() : null;
    }

    /**
     * Converts a DispatcherCompany object to its corresponding ID (dispatcher company ID).
     *
     * @param dispatcherCompany The DispatcherCompany object to be converted.
     * @return The ID of the dispatcher company.
     */
    @Named("dispatcherCompanyToDispatcherCompanyDto")
    public DispatcherCompanyDTO dispatcherCompanyToDispatcherCompanyDto(DispatcherCompany dispatcherCompany){
        return dispatcherCompany != null ? dispatcherCompanyMapper.toDispatcherCompanyDto(dispatcherCompany) : null;
    }
}
