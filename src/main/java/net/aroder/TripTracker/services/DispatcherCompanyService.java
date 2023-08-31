package net.aroder.TripTracker.services;

import jakarta.persistence.EntityNotFoundException;
import net.aroder.TripTracker.models.DispatcherCompany;
import net.aroder.TripTracker.repositories.DispatcherCompanyRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DispatcherCompanyService {

    private final DispatcherCompanyRepository dispatcherCompanyRepository;

    public DispatcherCompanyService(final DispatcherCompanyRepository dispatcherCompanyRepository){
        this.dispatcherCompanyRepository = dispatcherCompanyRepository;
    }

    public List<DispatcherCompany> findAllDispatcherCompanies(){
        return dispatcherCompanyRepository.findAll();
    }

    public DispatcherCompany findDispatcherCompanyById(Long companyId){
        return dispatcherCompanyRepository.findById(companyId).orElseThrow(()-> new EntityNotFoundException("Could not find company"));
    }

    public void deleteDispatcherCompany(Long companyId){
        dispatcherCompanyRepository.deleteById(companyId);
    }

    public DispatcherCompany updateDispatcherCompanyName(Long companyId, String name){
        if(companyId == null || name == null) throw new IllegalArgumentException("id or name cant be null");
        DispatcherCompany dispatcherCompany = dispatcherCompanyRepository.findById(companyId).orElseThrow(()->new EntityNotFoundException("Could not find company by id"));
        dispatcherCompany.setName(name);
        return dispatcherCompanyRepository.save(dispatcherCompany);
    }
    public DispatcherCompany createDispatcherCompany(String newCompanyName){
        if(newCompanyName == null ) throw new IllegalArgumentException("Company needs a name");
        DispatcherCompany newCompany = new DispatcherCompany();
        newCompany.setName(newCompanyName);
        return dispatcherCompanyRepository.save(newCompany);
    }

    public List<String> searchDispatcherCompanyByName(String name){
        Pageable page = PageRequest.of(0, 10);
        return dispatcherCompanyRepository.findAllByNameLikeIgnoreCaseOrderByName("%"+name+"%",page).map(DispatcherCompany::getName).toList();
    }

    public DispatcherCompany findDispatcherCompanyByName(String name){
        return dispatcherCompanyRepository.findByName(name).orElseThrow(()->new EntityNotFoundException("Could not find company by name"));
    }
}
