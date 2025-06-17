package com.orderservice.integration;

import com.orderservice.entity.Partner;
import com.orderservice.repository.PartnerRepository;
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

    @Test
    void shouldSaveAndGetPartner() {
        Partner partner = new Partner(UUID.randomUUID(), "Partner A", new BigDecimal("1000.00"));

        Partner savedPartner = partnerRepository.save(partner);

        Optional<Partner> findedPartner = partnerRepository.findById(savedPartner.getId());

        assertTrue(findedPartner.isPresent());
        assertEquals("Partner A", findedPartner.get().getName());
        assertEquals(new BigDecimal("1000.00"), findedPartner.get().getCreditLimit());
    }

}
