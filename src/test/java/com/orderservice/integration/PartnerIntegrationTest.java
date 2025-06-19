package com.orderservice.integration;

import com.orderservice.entity.Partner;
import com.orderservice.repository.PartnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class PartnerIntegrationTest {

    private static final String PARTNER_A_NAME = "Partner A";
    private static final String PARTNER_B_NAME = "Partner B";
    private static final String PARTNER_UPDATE_NAME = "Partner Update";
    private static final BigDecimal CREDIT_LIMIT_INITIAL = new BigDecimal("1000.00");
    private static final BigDecimal CREDIT_LIMIT_UPDATED = new BigDecimal("2000.00");

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.3")
            .withDatabaseName("orderdb")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private PartnerRepository partnerRepository;

    private Partner partner;

    @BeforeEach
    void setup() {
        partner = Partner.builder()
                                 .name(PARTNER_A_NAME)
                                 .creditLimit(CREDIT_LIMIT_INITIAL)
                                 .build();
        partner = partnerRepository.save(partner);
    }

    @Test
    void shouldCreatPartener() {
        Partner newPartner = Partner.builder()
                                 .name(PARTNER_B_NAME)
                                 .creditLimit(CREDIT_LIMIT_UPDATED)
                                 .build();

        Partner savedNewPartner = partnerRepository.save(newPartner);

        assertNotNull(savedNewPartner.getId());
        assertEquals(PARTNER_B_NAME, savedNewPartner.getName());
        assertEquals(CREDIT_LIMIT_UPDATED, savedNewPartner.getCreditLimit());
    }

    @Test
    void shoulGetPartnerById() {
        Optional<Partner> searchedPartner = partnerRepository.findById(partner.getId());

        assertTrue(searchedPartner.isPresent());
        assertEquals(partner.getId(), searchedPartner.get().getId());
        assertEquals(PARTNER_A_NAME, searchedPartner.get().getName());
        assertEquals(CREDIT_LIMIT_INITIAL, searchedPartner.get().getCreditLimit());
    }

    @Test
    void shouldReturnEmpty_whenPartnerNotFound() {
        Optional<Partner> searchedPartner = partnerRepository.findById(UUID.randomUUID());

        assertTrue(searchedPartner.isEmpty());
    }

    @Test
    void shouldUpdatePartner() {
        partner.setName(PARTNER_UPDATE_NAME);
        partner.setCreditLimit(CREDIT_LIMIT_UPDATED);

        Partner updatedPartner = partnerRepository.save(partner);

        Optional<Partner> searchedAfterUpdatedPartner = partnerRepository.findById(updatedPartner.getId());

        assertTrue(searchedAfterUpdatedPartner.isPresent());
        assertEquals(PARTNER_UPDATE_NAME, searchedAfterUpdatedPartner.get().getName());
        assertEquals(CREDIT_LIMIT_UPDATED, searchedAfterUpdatedPartner.get().getCreditLimit());
    }

//    @Test
//    void updateNotPresentPartnerShouldCreateNew() {
//        UUID newPartnerId = UUID.randomUUID();
//        Partner notPresentePartner = Partner.builder()
//                                            .id(newPartnerId)
//                                            .name("Partner Already Not Present")
//                                            .creditLimit(new BigDecimal("5000.00"))
//                                            .build();
//
//        Partner savedNotPresentePartner = partnerRepository.save(notPresentePartner);
//
//        assertNotNull(savedNotPresentePartner.getId());
//        assertNotEquals(newPartnerId, savedNotPresentePartner.getId());
//        assertEquals("Partner Already Not Present", savedNotPresentePartner.getName());
//    }

}