package com.ecommerce.order_management.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Send a simple text email
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }


    // Send order confirmation
    public void sendOrderConfirmation(String to, Long orderId,
                                      BigDecimal totalAmount) {
        String subject = "Order Confirmation #" + orderId;
        String body = """
                Thank you for your order!
                
                Order ID: %d
                Total amount: %s
                """.formatted(orderId, totalAmount);

        sendEmail(to, subject, body);
    }

}