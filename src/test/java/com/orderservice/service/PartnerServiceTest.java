package com.orderservice.service;

import com.orderservice.dto.PartnerResponseDTO;
import com.orderservice.entity.Partner;
import com.orderservice.exception.PartnerNotFoundException;
import com.orderservice.repository.PartnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PartnerServiceTest {

    @Mock
    private PartnerRepository partnerRepository;

    @InjectMocks
    private PartnerService partnerService;

    private Partner partner;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Partner partner = new Partner(UUID.randomUUID(), "Partner A", new BigDecimal("1000.00"));
    }

    @Test
    void getById_withValidId_shouldReturnPartner() {
        when(partnerRepository.findById(partner.getId())).thenReturn(Optional.of(partner));

        PartnerResponseDTO partnerResponseDTO = partnerService.getById(partner.getId());
        assertNotNull(partnerResponseDTO);
        assertEquals("Partner A", partnerResponseDTO.name());
        assertEquals(new BigDecimal("1000.00"), partner.getCreditLimit());
        verify(partnerRepository, times(1)).findById(partner.getId());
    }

    @Test
    void getById_withInvalidId_shouldReturnException() {
        UUID partnerId = UUID.randomUUID();
        when(partnerRepository.findById(UUID.randomUUID())).thenReturn(Optional.empty());

        assertThrows(PartnerNotFoundException.class, () -> partnerService.getById(partnerId));

        verify(partnerRepository, times(1)).findById(partnerId);
    }

}
