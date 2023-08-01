package net.aroder.TripTracker.services;

import jakarta.persistence.EntityNotFoundException;
import net.aroder.TripTracker.models.OrganizerCompany;
import net.aroder.TripTracker.repositories.OrganizerCompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizerCompanyService {

    @Autowired
    private OrganizerCompanyRepository organizerCompanyRepository;

    public List<OrganizerCompany> findAllOrganizerCompanies(){
        return organizerCompanyRepository.findAll();
    }

    public OrganizerCompany findOrganizerCompanyById(Long companyId){
        return organizerCompanyRepository.findById(companyId).orElseThrow(()-> new EntityNotFoundException("Could not find company"));
    }

    public void deleteOrganizerCompany(Long companyId){
        organizerCompanyRepository.deleteById(companyId);
    }

    public OrganizerCompany updateOrganizerCompanyName(Long companyId,String name){
        if(companyId == null || name == null) throw new IllegalArgumentException("id cant be null");
        OrganizerCompany organizerCompany = organizerCompanyRepository.findById(companyId).orElseThrow(()->new EntityNotFoundException("Could not find company by id"));
        organizerCompany.setName(name);
        return organizerCompanyRepository.save(organizerCompany);
    }
    public OrganizerCompany createOrganizerCompany(String newCompanyName){
        if(newCompanyName == null ) throw new IllegalArgumentException("Company needs a name");

        OrganizerCompany newCompany = new OrganizerCompany();
        newCompany.setName(newCompanyName);
        return organizerCompanyRepository.save(newCompany);
    }

    public OrganizerCompany findOrganizerCompanyByName(String companyName){
        return organizerCompanyRepository.findByName(companyName).orElseThrow(()->new EntityNotFoundException("Could not find company by name"));
    }
}
