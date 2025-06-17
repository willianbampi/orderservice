package com.orderservice.service;

import com.orderservice.dto.PartnerRequestDTO;
import com.orderservice.dto.PartnerResponseDTO;
import com.orderservice.entity.Partner;
import com.orderservice.exception.PartnerNotFoundException;
import com.orderservice.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PartnerService {

    private final PartnerRepository partnerRepository;

    public PartnerResponseDTO create(PartnerRequestDTO dto) {
        Partner partner = Partner.builder()
                .name(dto.name())
                .creditLimit(dto.creditLimit())
                .build();

        partner = partnerRepository.save(partner);

        return toResponseDTO(partner);
    }

    public PartnerResponseDTO getById(UUID id) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new PartnerNotFoundException("Partner not found!"));
        return toResponseDTO(partner);
    }

    private PartnerResponseDTO toResponseDTO(Partner partner) {
        return new PartnerResponseDTO(
                partner.getId(),
                partner.getName(),
                partner.getCreditLimit()
        );
    }

}
