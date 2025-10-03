package com.sathwikhbhat.orderservice.service;

import com.sathwikhbhat.bookingservice.event.BookingEvent;
import com.sathwikhbhat.orderservice.client.InventoryServiceClient;
import com.sathwikhbhat.orderservice.entity.Order;
import com.sathwikhbhat.orderservice.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryServiceClient inventoryServiceClient;

    @Autowired
    public OrderService(final OrderRepository orderRepository,
                        final InventoryServiceClient inventoryServiceClient) {
        this.orderRepository = orderRepository;
        this.inventoryServiceClient = inventoryServiceClient;
    }

    @KafkaListener(topics = "booking", groupId = "order-service")
    public void orderEvent(BookingEvent bookingEvent) {
        log.info("Received booking event: {}", bookingEvent);

        Order order = createOrder(bookingEvent);

        orderRepository.saveAndFlush(order);
        log.info("Order created for user ID: {}", bookingEvent.getUserId());

        inventoryServiceClient.updateInventory(bookingEvent.getEventId(), bookingEvent.getTicketCount());
        log.info("Inventory updated for event ID: {}", bookingEvent.getEventId());
    }

    private Order createOrder(BookingEvent bookingEvent) {
        return Order.builder()
                .customerId(bookingEvent.getUserId())
                .eventId(bookingEvent.getEventId())
                .ticketCount(bookingEvent.getTicketCount())
                .totalPrice(bookingEvent.getTotalPrice())
                .build();
    }

}