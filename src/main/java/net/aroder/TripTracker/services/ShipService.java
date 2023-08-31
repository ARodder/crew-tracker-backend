package net.aroder.TripTracker.services;

import jakarta.persistence.EntityNotFoundException;
import net.aroder.TripTracker.models.OrganizerCompany;
import net.aroder.TripTracker.models.Ship;
import net.aroder.TripTracker.repositories.OrganizerCompanyRepository;
import net.aroder.TripTracker.repositories.ShipRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
public class ShipService {
    private final ShipRepository shipRepository;
    private final UserService userService;
    private final OrganizerCompanyRepository organizerCompanyRepository;

    public ShipService(final ShipRepository shipRepository, final UserService userService, final OrganizerCompanyRepository organizerCompanyRepository) {
        this.shipRepository = shipRepository;
        this.userService = userService;
        this.organizerCompanyRepository = organizerCompanyRepository;
    }


    /**
     * Retrieves five ships with a name like the given parameter
     * and maps them to a list of the names of the ships.
     *
     * @param name name to search for.
     * @return List of ship names.
     */
    public List<String> searchShipNamesLike(String name){
        Pageable pageRequest = PageRequest.of(0,5);

        return shipRepository.findAllByNameLikeIgnoreCaseOrderByName("%"+name+"%",pageRequest).toList().stream().map(Ship::getName).toList();
    }

    public List<Ship> findAllShips() throws AccessDeniedException {
        if(userService.userIsAdmin()){
            return shipRepository.findAll();
        } else if (userService.userIsManager()) {
            return shipRepository.findByOrganizerCompanyOrderByName(userService.getCurrentUser().getOrganizerCompany());
        }else{
            throw new AccessDeniedException("No access to ships");
        }
    }

    public void deleteShip(Long shipId) throws AccessDeniedException {
        Ship foundShip = shipRepository.findById(shipId).orElseThrow(EntityNotFoundException::new);
        if(userService.userIsAdmin() || (userService.userIsManager() && userService.getCurrentUser().getOrganizerCompany().equals(foundShip.getOrganizerCompany()))){
            shipRepository.delete(foundShip);
        }else throw new AccessDeniedException("You cannot delete this ship");
    }

    public Ship createShip(Ship ship,Long organizerCompanyId){
        if(organizerCompanyId != null && userService.userIsAdmin()){
            OrganizerCompany foundCompany = organizerCompanyRepository.findById(organizerCompanyId).orElseThrow(EntityNotFoundException::new);
            ship.setOrganizerCompany(foundCompany);
        } else if(userService.getCurrentUser().getOrganizerCompany() != null){
            ship.setOrganizerCompany(userService.getCurrentUser().getOrganizerCompany());
        }
        return shipRepository.save(ship);
    }

    public void updateShip(Ship ship) throws AccessDeniedException {
        if(ship.getId() == null) throw new IllegalArgumentException();
        if(userService.userIsAdmin() || (userService.userIsManager() && userService.getCurrentUser().getOrganizerCompany().equals(ship.getOrganizerCompany()))) {
            shipRepository.save(ship);
        } else throw new AccessDeniedException("You cannot edit this ship");

    }
}
