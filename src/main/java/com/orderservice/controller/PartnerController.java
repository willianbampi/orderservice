package com.orderservice.controller;

import com.orderservice.dto.PartnerRequestDTO;
import com.orderservice.dto.PartnerResponseDTO;
import com.orderservice.service.PartnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/partners")
@RequiredArgsConstructor
@Tag(name = "Partners", description = "Partners Management")
public class PartnerController implements GenericController {

    private static final String CREATE_LOG_INFO = "PARTNERCONTROLLER - create method with dto: {}";
    private static final String GET_BY_ID_LOG_INFO = "PARTNERCONTROLLER - getById method with id: " +
                                                                        "{}";
    private static final String UPDATE_LOG_INFO = "PARTNERCONTROLLER - update method with id: " +
                                                                        "{} and with dto: {}";

    private final PartnerService partnerService;

    @Operation(summary = "Create a new partner")
    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody PartnerRequestDTO dto) {
        log.info(CREATE_LOG_INFO, dto);
        PartnerResponseDTO partnerResponseDTO = partnerService.create(dto);
        URI uri = generateHeaderLocation(partnerResponseDTO.id());
        return ResponseEntity.created(uri).build();
    }

    @Operation(summary = "Get a partner by id")
    @GetMapping("/{id}")
    public ResponseEntity<PartnerResponseDTO> getById(@PathVariable UUID id) {
        log.info(GET_BY_ID_LOG_INFO, id);
        return ResponseEntity.ok(partnerService.getById(id));
    }

    @Operation(summary = "Update a partner")
    @PostMapping("/{id}")
    public ResponseEntity<PartnerResponseDTO> update(@PathVariable UUID id, @Valid @RequestBody PartnerRequestDTO dto) {
        log.info(UPDATE_LOG_INFO, id, dto);
        return ResponseEntity.ok(partnerService.update(id, dto));
    }

}