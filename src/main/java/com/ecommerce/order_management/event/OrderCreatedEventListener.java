package com.ecommerce.order_management.event;

import com.ecommerce.order_management.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderCreatedEventListener {

    private final EmailService emailService;

    // Send the email only after the transaction commits successfully.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) {
        // Extract order details from the event and send confirmation.
        emailService.sendOrderConfirmation(
                event.email(),
                event.orderId(),
                event.totalAmount()
        );
    }
}