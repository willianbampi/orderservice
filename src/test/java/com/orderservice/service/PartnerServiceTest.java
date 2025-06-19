package com.orderservice.service;

import com.orderservice.dto.PartnerRequestDTO;
import com.orderservice.dto.PartnerResponseDTO;
import com.orderservice.entity.Partner;
import com.orderservice.exception.PartnerNotFoundException;
import com.orderservice.repository.PartnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PartnerServiceTest {

    private static final UUID PARTNER_ID = UUID.randomUUID();
    private static final String PARTNER_A_NAME = "Partner A";
    private static final String PARTNER_UPDATE_NAME = "Partner Update";
    private static final String PARTNER_NOT_FOUND = "Partner not found!";
    private static final BigDecimal CREDIT_LIMIT_INITIAL = new BigDecimal("1000.00");
    private static final BigDecimal CREDIT_LIMIT_UPDATED = new BigDecimal("2000.00");
    private static final Partner PARTNER = new Partner(PARTNER_ID, PARTNER_A_NAME, CREDIT_LIMIT_INITIAL,
                                                       LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay(),
                                                       LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay());

    @InjectMocks
    private PartnerService partnerService;

    @Mock
    private PartnerRepository partnerRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void creatPartner_shouldReturnPartnerCreated() {
        // Arrange
        PartnerRequestDTO partnerRequestDTO = new PartnerRequestDTO(PARTNER_A_NAME, CREDIT_LIMIT_INITIAL);
        when(partnerRepository.save(any(Partner.class))).thenReturn(PARTNER);

        // Act
        PartnerResponseDTO partnerResponseDTO = partnerService.create(partnerRequestDTO);

        // Assert
        assertNotNull(partnerResponseDTO);
        assertEquals(PARTNER_ID, partnerResponseDTO.id());
        assertEquals(PARTNER_A_NAME, partnerResponseDTO.name());
        assertEquals(CREDIT_LIMIT_INITIAL, partnerResponseDTO.creditLimit());

        verify(partnerRepository, times(1)).save(any(Partner.class));
    }

    @Test
    void getPartnerById_shouldReturnPartnerWhenExist() {
        // Arrange
        when(partnerRepository.findById(PARTNER_ID)).thenReturn(Optional.of(PARTNER));

        // Act
        PartnerResponseDTO partnerResponseDTO = partnerService.getById(PARTNER_ID);

        // Assert
        assertNotNull(partnerResponseDTO);
        assertEquals(PARTNER_ID, partnerResponseDTO.id());
        assertEquals(PARTNER_A_NAME, partnerResponseDTO.name());

        verify(partnerRepository, times(1)).findById(PARTNER_ID);
    }

    @Test
    void getPartnerById_shouldThorwsExceptionWhenNotFound() {
        // Arrange
        UUID anotherPartnerId = UUID.randomUUID();
        when(partnerRepository.findById(anotherPartnerId)).thenReturn(Optional.empty());

        // Act & Assert
        PartnerNotFoundException partnerNotFoundException = assertThrows(PartnerNotFoundException.class, () ->
                partnerService.getById(anotherPartnerId)
        );
        assertEquals(PARTNER_NOT_FOUND, partnerNotFoundException.getMessage());

        verify(partnerRepository, times(1)).findById(anotherPartnerId);
    }

    @Test
    void updatePartner_shouldUpdate() {
        // Arrange
        PartnerRequestDTO partnerRequestDTO = new PartnerRequestDTO(PARTNER_UPDATE_NAME, CREDIT_LIMIT_UPDATED);

        when(partnerRepository.findById(PARTNER_ID)).thenReturn(Optional.of(PARTNER));
        when(partnerRepository.save(any(Partner.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        PartnerResponseDTO partnerResponseDTO = partnerService.update(PARTNER_ID, partnerRequestDTO);

        // Assert
        assertNotNull(partnerResponseDTO);
        assertEquals(PARTNER_ID, partnerResponseDTO.id());
        assertEquals(PARTNER_UPDATE_NAME, partnerResponseDTO.name());
        assertEquals(CREDIT_LIMIT_UPDATED, partnerResponseDTO.creditLimit());

        verify(partnerRepository, times(1)).findById(PARTNER_ID);
        verify(partnerRepository, times(1)).save(any(Partner.class));
    }

    @Test
    void updatePartner_shouldThrowsExceptionWhenNotFound() {
        // Arrange
        PartnerRequestDTO partnerRequestDTO = new PartnerRequestDTO(PARTNER_UPDATE_NAME, CREDIT_LIMIT_UPDATED);

        when(partnerRepository.findById(PARTNER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        PartnerNotFoundException partnerNotFoundException = assertThrows(PartnerNotFoundException.class, () ->
                partnerService.update(PARTNER_ID, partnerRequestDTO)
        );

        assertEquals(PARTNER_NOT_FOUND, partnerNotFoundException.getMessage());

        verify(partnerRepository, times(1)).findById(PARTNER_ID);
        verify(partnerRepository, never()).save(any(Partner.class));
    }

}