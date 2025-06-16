package com.orderservice.controller;

import com.orderservice.dto.PartnerRequestDTO;
import com.orderservice.dto.PartnerResponseDTO;
import com.orderservice.service.PartnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/partners")
@RequiredArgsConstructor
@Tag(name = "Partners", description = "Partners Management")
public class PartnerController {

    private final PartnerService partnerService;

    @Operation(summary = "Create a new partner")
    @PostMapping
    public ResponseEntity<PartnerResponseDTO> create(@Valid @RequestBody PartnerRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(partnerService.create(dto));
    }

    @Operation(summary = "Get a partner by id")
    @GetMapping("/{id}")
    public ResponseEntity<PartnerResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(partnerService.getById(id));
    }

}
