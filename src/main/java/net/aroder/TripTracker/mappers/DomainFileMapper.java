package net.aroder.TripTracker.mappers;

import net.aroder.TripTracker.models.DTOs.DomainFileDTOs.DomainFileDTO;
import net.aroder.TripTracker.models.DTOs.UserDTOs.SimpleUserDTO;
import net.aroder.TripTracker.models.DomainFile;
import net.aroder.TripTracker.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class DomainFileMapper {
    @Autowired
    private  UserMapper userMapper;

    @Mapping(source="createdBy",target = "createdBy",qualifiedByName = "toSimpleUserDTO")
    public abstract DomainFileDTO toDomainFileDTO(DomainFile domainFile);

    public abstract List<DomainFileDTO> toDomainFileDTO(List<DomainFile> domainFile);

    @Named("toSimpleUserDTO")
    public SimpleUserDTO toSimpleUserDTO(User user){
        return user != null ? userMapper.toSimpleUserDTO(user):null;
    }
}