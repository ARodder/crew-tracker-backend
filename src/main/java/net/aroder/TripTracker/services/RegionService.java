package net.aroder.TripTracker.services;

import jakarta.persistence.EntityNotFoundException;
import net.aroder.TripTracker.models.DTOs.RegionDTOs.NewRegionDTO;
import net.aroder.TripTracker.models.DispatcherCompany;
import net.aroder.TripTracker.models.Location;
import net.aroder.TripTracker.models.Region;
import net.aroder.TripTracker.repositories.RegionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegionService {

    @Autowired
    private RegionRepository regionRepository;
    @Autowired
    private LocationService locationService;
    @Autowired
    private DispatcherCompanyService dispatcherCompanyService;

    public List<Region> findAllRegions(){
        return regionRepository.findAll();
    }

    public Region findRegionById(Long regionId){
        return regionRepository.findById(regionId).orElseThrow(()-> new EntityNotFoundException("Could not find company"));
    }

    public void deleteRegion(Long regionId){
        regionRepository.deleteById(regionId);
    }

    public Region updateRegion(Region region){
        if(region.getId() == null || region.getName() == null || region.getRegionLocation() == null) throw new IllegalArgumentException("id, location or name cant be null");
        Region foundRegion = regionRepository.findById(region.getId()).orElseThrow(()-> new EntityNotFoundException("Cant find region"));
        return regionRepository.save(region);
    }

    public Region updateRegionName(Long regionId,String newName){
        if(regionId == null || newName == null) throw new IllegalArgumentException("Name or id cant be null");
        Region foundRegion = regionRepository.findById(regionId).orElseThrow(()->new EntityNotFoundException("Could not find region"));
        foundRegion.setName(newName);
        return regionRepository.save(foundRegion);
    }
    public Region createRegion(NewRegionDTO newRegion){
        if(newRegion == null ) throw new IllegalArgumentException("Company needs a name");
        Region region = new Region();
        region.setName(newRegion.getName());

        Location regionLocation = locationService.determineLocation(newRegion.getRegionLocationName());
        region.setRegionLocation(regionLocation);

        DispatcherCompany dispatcherCompany = dispatcherCompanyService.findDispatcherCompanyByName(newRegion.getDispatcherCompanyName());
        region.setDispatcherCompany(dispatcherCompany);

        return regionRepository.save(region);
    }

    public void updateLocation(Long regionId, String locationName){
        if(regionId == null || locationName == null) throw new IllegalArgumentException("id or locationName cant be null");
        Region foundRegion = regionRepository.findById(regionId).orElseThrow(()->new EntityNotFoundException("Could not find region"));
        Location location = locationService.determineLocation(locationName);
        if(location.getLatitude() == null || location.getLongitude() == null) throw new IllegalArgumentException("Could not find location coordinates");
        foundRegion.setRegionLocation(location);
        regionRepository.save(foundRegion);
    }

    public void updateDispatcherCompany(Long regionId, String dispatcherCompanyName){
        if(regionId == null || dispatcherCompanyName == null) throw new IllegalArgumentException("id or dispatcherCompanyName cant be null");
        Region foundRegion = regionRepository.findById(regionId).orElseThrow(()->new EntityNotFoundException("Could not find region"));
        DispatcherCompany dispatcherCompany = dispatcherCompanyService.findDispatcherCompanyByName(dispatcherCompanyName);
        foundRegion.setDispatcherCompany(dispatcherCompany);
        regionRepository.save(foundRegion);
    }

    public Region findRegionByName(String name){
        return regionRepository.findByNameIgnoreCase(name).orElseThrow(()->new EntityNotFoundException("Could not find region by this name"));
    }
    public List<String> searchRegionsLikeName(String name) {
        Pageable pageRequest = PageRequest.of(0, 5);

        return regionRepository.findAllByNameLikeIgnoreCaseOrderByName("%" + name + "%", pageRequest).toList().stream().map(Region::getName).toList();
    }

    public List<String> getAllRegionNames(){
        return regionRepository.findAll().stream().map(Region::getName).toList();
    }
}
