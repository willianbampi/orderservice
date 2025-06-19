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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PartnerServiceTest {

    private static final UUID partnerId = UUID.randomUUID();
    private static final Partner partner = new Partner(partnerId, "Partner A", new BigDecimal("1000.00"),
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
        PartnerRequestDTO partnerRequestDTO = new PartnerRequestDTO("Partner A", new BigDecimal("1000"));
        when(partnerRepository.save(any(Partner.class))).thenReturn(partner);

        // Act
        PartnerResponseDTO partnerResponseDTO = partnerService.create(partnerRequestDTO);

        // Assert
        assertNotNull(partnerResponseDTO);
        assertEquals(partnerId, partnerResponseDTO.id());
        assertEquals("Partner A", partnerResponseDTO.name());
        assertEquals(new BigDecimal("1000.00"), partnerResponseDTO.creditLimit());

        verify(partnerRepository, times(1)).save(any(Partner.class));
    }

    @Test
    void getPartnerById_shouldReturnPartnerWhenExist() {
        // Arrange
        when(partnerRepository.findById(partnerId)).thenReturn(Optional.of(partner));

        // Act
        PartnerResponseDTO partnerResponseDTO = partnerService.getById(partnerId);

        // Assert
        assertNotNull(partnerResponseDTO);
        assertEquals(partnerId, partnerResponseDTO.id());
        assertEquals("Partner A", partnerResponseDTO.name());

        verify(partnerRepository, times(1)).findById(partnerId);
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
        assertEquals("Partner not found!", partnerNotFoundException.getMessage());

        verify(partnerRepository, times(1)).findById(anotherPartnerId);
    }

    @Test
    void updatePartner_shouldUpdate() {
        // Arrange
        PartnerRequestDTO partnerRequestDTO = new PartnerRequestDTO("Partner Update", new BigDecimal("2000"));

        when(partnerRepository.findById(partnerId)).thenReturn(Optional.of(partner));
        when(partnerRepository.save(any(Partner.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        PartnerResponseDTO partnerResponseDTO = partnerService.update(partnerId, partnerRequestDTO);

        // Assert
        assertNotNull(partnerResponseDTO);
        assertEquals(partnerId, partnerResponseDTO.id());
        assertEquals("Partner Update", partnerResponseDTO.name());
        assertEquals(new BigDecimal("2000"), partnerResponseDTO.creditLimit());

        verify(partnerRepository, times(1)).findById(partnerId);
        verify(partnerRepository, times(1)).save(any(Partner.class));
    }

    @Test
    void updatePartner_shouldThrowsExceptionWhenNotFound() {
        // Arrange
        PartnerRequestDTO partnerRequestDTO = new PartnerRequestDTO("Partner Update", new BigDecimal("2000"));

        when(partnerRepository.findById(partnerId)).thenReturn(Optional.empty());

        // Act & Assert
        PartnerNotFoundException partnerNotFoundException = assertThrows(PartnerNotFoundException.class, () ->
                partnerService.update(partnerId, partnerRequestDTO)
        );

        assertEquals("Partner not found!", partnerNotFoundException.getMessage());

        verify(partnerRepository, times(1)).findById(partnerId);
        verify(partnerRepository, never()).save(any(Partner.class));
    }

}