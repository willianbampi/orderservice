package com.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderservice.dto.PartnerRequestDTO;
import com.orderservice.dto.PartnerResponseDTO;
import com.orderservice.entity.Partner;
import com.orderservice.exception.GlobalExceptionHandler;
import com.orderservice.exception.PartnerNotFoundException;
import com.orderservice.service.PartnerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PartnerController.class)
public class PartnerControllerTest {

    private static final UUID partnerId = UUID.randomUUID();
    private static final Partner partner = new Partner(partnerId, "Partner A", new BigDecimal("1000.00"),
                                                       LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay(),
                                                       LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay());

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PartnerService partnerService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        partnerService = Mockito.mock(PartnerService.class);
        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new PartnerController(partnerService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createPartner_shouldReturnCreated() throws Exception {
        PartnerRequestDTO partnerRequestDTO = new PartnerRequestDTO("Partner A", new BigDecimal("1000"));
        PartnerResponseDTO partnerResponseDTO = new PartnerResponseDTO(partnerId, "Partner A", new BigDecimal("1000"));

        when(partnerService.create(any())).thenReturn(partnerResponseDTO);

        mockMvc.perform(post("/api/partners")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(partnerRequestDTO)))
               .andExpect(status().isCreated());
    }

    @Test
    void getPartnerById_shouldReturnPartner() throws Exception {
        PartnerResponseDTO partnerResponseDTO = new PartnerResponseDTO(partnerId, "Partner A", new BigDecimal("1000"));

        when(partnerService.getById(any())).thenReturn(partnerResponseDTO);

        mockMvc.perform(get("/api/partners/{id}", partnerId))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(partnerId.toString()))
               .andExpect(jsonPath("$.name").value("Partner A"))
               .andExpect(jsonPath("$.creditLimit").value(1000.00));
    }

    @Test
    void getPartnerById_shouldReturnExceptionWhenNotFound() throws Exception {
        UUID anotherPartnerId = UUID.randomUUID();
        when(partnerService.getById(any())).thenThrow(new PartnerNotFoundException("Partner not found!"));

        mockMvc.perform(get("/api/partners/{id}", anotherPartnerId))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.status").value(404))
               .andExpect(jsonPath("$.message").value("Partner not found!"));
    }

    @Test
    void updatePartner_shouldReturnPartner() throws Exception {
        PartnerRequestDTO partnerRequestDTO = new PartnerRequestDTO("Partner Update", new BigDecimal("2000"));
        PartnerResponseDTO partnerResponseDTO = new PartnerResponseDTO(partnerId, "Partner A", new BigDecimal("1000"));

        when(partnerService.update(any(), any())).thenReturn(partnerResponseDTO);

        mockMvc.perform(post("/api/partners/{id}", partnerId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(partnerRequestDTO)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(partnerId.toString()))
               .andExpect(jsonPath("$.name").value("Partner A"))
               .andExpect(jsonPath("$.creditLimit").value(1000.00));
    }

    @Test
    void updatePartner_shouldReturnExceptionWhenNotFound() throws Exception {
        UUID anotherPartnerId = UUID.randomUUID();
        PartnerRequestDTO partnerRequestDTO = new PartnerRequestDTO("Partner Update", new BigDecimal("2000"));

        when(partnerService.update(any(), any())).thenThrow(new PartnerNotFoundException("Partner not found!"));

        mockMvc.perform(post("/api/partners/{id}", anotherPartnerId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(partnerRequestDTO)))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.status").value(404))
               .andExpect(jsonPath("$.message").value("Partner not found!"));
    }

}