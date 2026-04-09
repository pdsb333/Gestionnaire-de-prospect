package com.GDP.GDP.service.professional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.GDP.GDP.components.VerifyBusinessForUser;
import com.GDP.GDP.dto.professional.ProfessionalRequest;
import com.GDP.GDP.dto.professional.ProfessionalResponse;
import com.GDP.GDP.entity.Business;
import com.GDP.GDP.entity.Professional;
import com.GDP.GDP.entity.User;
import com.GDP.GDP.repository.ProfessionalRepository;
import com.GDP.GDP.exception.ResourceNotFoundException;


@Service
@Transactional
public class ProfessionalServiceImpl implements ProfessionalService {
    private final VerifyBusinessForUser verifyBusinessForUser;
    private final ProfessionalRepository professionalRepository;

    public ProfessionalServiceImpl(ProfessionalRepository professionalRepository, VerifyBusinessForUser verifyBusinessForUser){
        this.professionalRepository = professionalRepository;
        this.verifyBusinessForUser = verifyBusinessForUser;
    }

    private Professional verifyProfessional(Long id, User user){
        return professionalRepository.findByIdAndBusiness_UserId(id, user.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Professional", id));
    }

    @Override
    public ProfessionalResponse create(ProfessionalRequest request, Long id, User user){
        Business business = verifyBusinessForUser.verifyBusiness(id, user);
        Professional professional = new Professional(request.lastName(), request.firstName(), request.job(), request.contact(), business);
        return ProfessionalResponse.fromEntity(professionalRepository.save(professional));
    }

    @Override
    public ProfessionalResponse updateProfessional(ProfessionalRequest request, Long id, User user){
        Professional professional = verifyProfessional(id, user);
        professional.setFirstName(request.firstName());
        professional.setLastName(request.lastName());
        professional.setJob(request.job());
        professional.setContact(request.contact());
        return ProfessionalResponse.fromEntity(professional);
    }

}
