package net.aroder.TripTracker.services;

import jakarta.persistence.EntityNotFoundException;
import net.aroder.TripTracker.models.Location;
import net.aroder.TripTracker.models.Region;
import net.aroder.TripTracker.models.Trip;
import net.aroder.TripTracker.repositories.LocationRepository;
import net.aroder.TripTracker.repositories.RegionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The LocationService takes care of any action pertaining to locations, such as modifying,
 * adding, retrieving and deleting locations.
 */
@Service
public class LocationService {
    private final LocationRepository locationRepository;
    private final RestTemplate restTemplate;
    private final RegionRepository regionRepository;
    private final String countryCode;
    private final String baseLocationSearchUrl;

    public LocationService(final LocationRepository locationRepository,
                           final RestTemplate restTemplate,
                           final RegionRepository regionRepository,
                           @Value("${location.country-code}") final String countryCode,
                           @Value("${location.search.url}") final String baseLocationSearchUrl) {
        this.locationRepository = locationRepository;
        this.restTemplate = restTemplate;
        this.regionRepository = regionRepository;
        this.countryCode = countryCode;
        this.baseLocationSearchUrl = baseLocationSearchUrl;
    }



    /**
     * Finds a location based on the name of the location
     *
     * @param name name of the location to find
     * @return found location.
     */
    public Location determineLocation(String name) {
        if(name == null) return null;
        name = name.toLowerCase();
        List<Location> foundLocation = locationRepository.findByNameIgnoreCase(name);
        if (foundLocation.size() > 0) {
            Location loc = foundLocation.get(0);
            if (loc.getLatitude() != null && loc.getLongitude() != null) {
                return loc;
            }
            return searchLocation(loc);
        }
        return searchNewLocation(name);
    }


    /**
     * Searches for a location based on the name
     * if it does not exist in the internal repository.
     * If the location is not found in search it will create a new
     * location without coordinates.
     *
     * @param name name of the location to search for
     * @return location found
     */
    private Location searchNewLocation(String name) {
        name = name.trim().toLowerCase();
        String loginUrl = baseLocationSearchUrl + "search?q=" + name + "&countrycodes=" + countryCode + "&format=json";
        ResponseEntity<List> response = restTemplate.getForEntity(loginUrl, List.class);
        HashMap<String, Object> responseLocation;
        if (response.getBody() != null && !response.getBody().isEmpty()  && response.getBody().get(0) != null && response.getBody().get(0) instanceof Map) {
            responseLocation = new HashMap<String, Object>((Map) response.getBody().get(0));
            Location newLocation = new Location(name, Double.parseDouble((String) responseLocation.get("lon")), Double.parseDouble((String) responseLocation.get("lat")));
            newLocation.setRegion(findClosestRegion(newLocation));
            return locationRepository.save(newLocation);
        }
        Location newLocation = new Location(name);
        newLocation.setError("Missing region");
        return locationRepository.save(newLocation);
    }

    /**
     * Searches for a location by name if a given
     * location does not contain coordinates.
     *
     * @param location location to search for coordinates
     * @return returns the location, with or without updated coordinates.
     */
    private Location searchLocation(Location location) {

        String loginUrl = baseLocationSearchUrl + "search?q=" + location.getName() + "&countrycodes=" + countryCode + "&format=json";
        ResponseEntity<List> response = restTemplate.getForEntity(loginUrl, List.class);
        HashMap<String, Object> responseLocation;
        if (response.getBody() != null && response.getBody().size() > 0 && response.getBody().get(0) instanceof Map) {
            responseLocation = new HashMap<String, Object>((Map) response.getBody().get(0));
            location.setLongitude(Double.parseDouble((String) responseLocation.get("lon")));
            location.setLatitude(Double.parseDouble((String) responseLocation.get("lat")));

            return locationRepository.save(location);
        }

        return location;
    }


    /**
     * Determines the closest region for a given location usually a harbour
     *
     * @param trip the trip to use for the distance search.
     * @return Returns the Region closest to the parameter
     */
    public Region determineRegion(Trip trip) {
        if(trip == null) return null;
        if(trip.getPickUpLocation() != null && trip.getPickUpLocation().getLatitude() != null){
            return trip.getPickUpLocation().getRegion();
        }else if(trip.getDestination() != null && trip.getDestination().getLatitude() != null){
            return trip.getDestination().getRegion();
        }else if(trip.getHarbour() != null && trip.getHarbour().getLatitude() != null){
            return trip.getHarbour().getRegion();
        }else {
            throw new IllegalArgumentException("Trip does not contain a location with coordinates");
        }
    }
    private Region findClosestRegion(Location location){
        List<Region> regions = regionRepository.findAll();

        Region closestRegion = null;
        double closestRegionDistance = 0.0;
        for (Region region : regions) {
            double currentRegionDistance = calculateDirectDistance(region.getRegionLocation(), location);
            if (closestRegion == null) {
                closestRegion = region;
                closestRegionDistance = currentRegionDistance;
            } else if (currentRegionDistance < closestRegionDistance) {
                closestRegion = region;
                closestRegionDistance = currentRegionDistance;
            }
        }
        return closestRegion;
    }

    /**
     * Calculates the kilometer distance between two given locations.
     *
     * @param locationA first location for the distance calculation
     * @param locationB second location for the distance calculation
     * @return the distance in kilometers between the two given points
     */
    private Double calculateDirectDistance(Location locationA, Location locationB) {
        double latA = Math.toRadians(locationA.getLatitude());
        double longA = Math.toRadians(locationA.getLongitude());

        double latB = Math.toRadians(locationB.getLatitude());
        double longB = Math.toRadians(locationB.getLongitude());

        double earthRadius = 6371.0;

        //deltas
        double dLat = latB - latA;
        double dLong = longB - longA;

        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(latA) * Math.cos(latB) * Math.pow(Math.sin(dLong / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }


    /**
     * Search for locations with a name like a given parameter.
     *
     * @param name name to search for.
     * @return list of location names like the given name.
     */
    public List<String> searchLocationsLikeName(String name) {
        Pageable pageRequest = PageRequest.of(0, 5);

        return locationRepository.findAllByNameLikeIgnoreCaseOrderByName("%" + name + "%", pageRequest).toList().stream().map(Location::getName).toList();
    }

    public List<Location> findAll() {
        return locationRepository.findAll();
    }

    public void deleteLocation(Long locationId){
        locationRepository.deleteById(locationId);
    }

    public Location createLocation(Location location){
        if(location.getName() == null) throw new IllegalArgumentException();
        location.setName(location.getName().trim().toLowerCase());
        location.setRegion(findClosestRegion(location));
        return locationRepository.save(location);
    }

    public void updateLocation(Location location){
        if(location.getName() == null && location.getId() == null) throw new IllegalArgumentException();
        location.setRegion(findClosestRegion(location));
        locationRepository.save(location);
    }

    public Location findLocationById(Long id) {
        return locationRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Location not found"));
    }


}
