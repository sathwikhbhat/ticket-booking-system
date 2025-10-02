package com.sathwikhbhat.inventoryservice.service;

import com.sathwikhbhat.inventoryservice.entity.Event;
import com.sathwikhbhat.inventoryservice.entity.Venue;
import com.sathwikhbhat.inventoryservice.repository.EventRepository;
import com.sathwikhbhat.inventoryservice.repository.VenueRepository;
import com.sathwikhbhat.inventoryservice.response.EventInventoryResponse;
import com.sathwikhbhat.inventoryservice.response.VenueInventoryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;

    @Autowired
    public InventoryService(final EventRepository eventRepository, final VenueRepository venueRepository) {
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
    }

    public List<EventInventoryResponse> getAllEvents() {
        final List<Event> events = eventRepository.findAll();
        return events.stream().map(event -> EventInventoryResponse.builder()
                .event(event.getName())
                .capacity(event.getLeftCapacity())
                .venue(event.getVenue())
                .build()).toList();
    }

    public VenueInventoryResponse getVenueInformation(final Long venueId) {
        final Venue venue = venueRepository.findById(venueId).orElse(null);
        assert venue != null;
        return VenueInventoryResponse.builder()
                .venueId(venue.getId())
                .venueName(venue.getName())
                .totalCapacity(venue.getTotalCapacity())
                .build();
    }

}