package com.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderservice.dto.PartnerRequestDTO;
import com.orderservice.dto.PartnerResponseDTO;
import com.orderservice.service.PartnerService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PartnerController.class)
public class PartnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PartnerService partnerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createPartner_shouldReturnOk() throws Exception {
        UUID partnerId = UUID.randomUUID();
        PartnerRequestDTO partnerRequestDTO = new PartnerRequestDTO("Partner A", new BigDecimal("1000.00"));
        PartnerResponseDTO partnerResponseDTO = new PartnerResponseDTO(partnerId, "Partner A", new BigDecimal("1000.00"));

        Mockito.when(partnerService.create(Mockito.any())).thenReturn(partnerResponseDTO);

        mockMvc.perform(post("/api/partners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partnerRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(partnerId)))
                .andExpect(jsonPath("$.nome", is("Partner A")))
                .andExpect(jsonPath("$.creditLimit", is(1000.00)));
    }

    @Test
    void getById_shouldReturnPartner() throws Exception {
        UUID partnerId = UUID.randomUUID();
        PartnerResponseDTO partnerResponseDTO = new PartnerResponseDTO(partnerId, "Partner A", new BigDecimal("1000.00"));

        Mockito.when(partnerService.getById(partnerId)).thenReturn(partnerResponseDTO);

        mockMvc.perform(get("/api/partners/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(partnerId)))
                .andExpect(jsonPath("$.nome", is("Partner A")))
                .andExpect(jsonPath("$.limitCredit", is(1000.00)));
    }

}
