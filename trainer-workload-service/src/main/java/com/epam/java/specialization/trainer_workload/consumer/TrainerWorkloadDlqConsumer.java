package com.epam.java.specialization.trainer_workload.consumer;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TrainerWorkloadDlqConsumer {

    @JmsListener(
            destination = "${app.jms.queue.dlq:ActiveMQ.DLQ}",
            containerFactory = "jmsListenerContainerFactory"
    )
    public void processDlqMessage(Message message) {
        try {
            String messageBody = "";
            if (message instanceof TextMessage textMessage) {
                messageBody = textMessage.getText();
            }

            String jmsMessageId = message.getJMSMessageID();
            String originalDestination = message.getStringProperty("dlqDeliveryFailureCause");

            log.error("CRITICAL: Message routed to DLQ! MessageId: {}, FailureCause: {}, Payload: {}",
                    jmsMessageId, originalDestination, messageBody);

        } catch (JMSException ex) {
            log.error("Failed to process dead letter message: {}", ex.getMessage(), ex);
        }
    }
}