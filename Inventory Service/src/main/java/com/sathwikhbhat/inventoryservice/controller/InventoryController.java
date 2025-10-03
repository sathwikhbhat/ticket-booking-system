package com.sathwikhbhat.inventoryservice.controller;

import com.sathwikhbhat.inventoryservice.response.EventInventoryResponse;
import com.sathwikhbhat.inventoryservice.response.VenueInventoryResponse;
import com.sathwikhbhat.inventoryservice.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(final InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/events")
    public List<EventInventoryResponse> inventoryGetAllEvents() {
        return inventoryService.getAllEvents();
    }

    @GetMapping("/venue/{venueId}")
    public VenueInventoryResponse inventoryByVenueId(@PathVariable final Long venueId) {
        return inventoryService.getVenueInformation(venueId);
    }

    @GetMapping("/event/{eventId}")
    public EventInventoryResponse inventoryForEvent(@PathVariable final Long eventId) {
        return inventoryService.getEventInventory(eventId);
    }

}