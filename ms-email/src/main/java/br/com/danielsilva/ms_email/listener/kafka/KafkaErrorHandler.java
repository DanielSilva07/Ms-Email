package br.com.danielsilva.ms_email.listener.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.RecordDeserializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
public class KafkaErrorHandler implements CommonErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(KafkaErrorHandler.class);


    public void handleRecord(Exception thrownException, ConsumerRecord<?, ?> record, Consumer<?, ?> consumer, MessageListenerContainer container) {
        String message = String.format("Erro ao processar mensagem do tópico %s, partição %d, offset %d",
                record.topic(), record.partition(), record.offset());
        
        if (thrownException instanceof RecordDeserializationException ex) {
            logger.error("{} - Erro de desserialização: {}", message, ex.getMessage());
            consumer.seek(ex.topicPartition(), ex.offset() + 1);
            consumer.commitSync();
        } else {
            logger.error("{} - Erro inesperado: {}", message, thrownException.getMessage(), thrownException);
        }
    }

    @Override
    public void handleOtherException(Exception thrownException, Consumer<?, ?> consumer, MessageListenerContainer container, boolean batchListener) {
        logger.error("Erro no consumidor Kafka: {}", thrownException.getMessage(), thrownException);
    }
}
