package com.marriagehall.notification_service.listener;

import com.marriagehall.notification_service.config.RabbitMQConfig;
import com.marriagehall.notification_service.dto.BookingCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BookingEventListener {

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void handleBookingCreated(BookingCreatedEvent event) {
        log.info("Notification -> Booking {} created for user {} (amount: {}). " +
                        "Sending confirmation email/SMS...",
                event.getBookingId(), event.getUserId(), event.getTotalAmount());
        // real implementation: send email / SMS / push notification here
    }
}
