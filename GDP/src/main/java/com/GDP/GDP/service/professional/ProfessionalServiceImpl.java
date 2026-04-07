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

@Service
@Transactional
public class ProfessionalServiceImpl implements ProfessionalService {
    private final VerifyBusinessForUser verifyBusinessForUser;
    private final ProfessionalRepository professionalRepository;

    public ProfessionalServiceImpl(ProfessionalRepository professionalRepository, VerifyBusinessForUser verifyBusinessForUser){
        this.professionalRepository = professionalRepository;
        this.verifyBusinessForUser = verifyBusinessForUser;
    }

    @Override
    public ProfessionalResponse create(ProfessionalRequest request, Long id, User user){
        Business business = verifyBusinessForUser.verifyBusiness(id, user);
        Professional professional = new Professional(request.lastName(), request.firstName(), request.job(), request.contact(), business);
        return ProfessionalResponse.fromEntity(professionalRepository.save(professional));
    }

}
