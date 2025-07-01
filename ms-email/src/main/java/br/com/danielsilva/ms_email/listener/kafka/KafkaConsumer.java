package br.com.danielsilva.ms_email.listener.kafka;

import br.com.danielsilva.ms_email.dto.Cliente;
import br.com.danielsilva.ms_email.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class KafkaConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:email-service-group}")
    private String groupId;


    @Autowired
    public KafkaConsumer(EmailService emailService, ObjectMapper objectMapper) {
        this.emailService = emailService;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        // Configurações adicionais para melhor resiliência
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 10000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        
        // Configurações específicas para conexão com o Kafka no Docker
        props.put("security.protocol", "PLAINTEXT");
        props.put("metadata.max.age.ms", 5000);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(KafkaErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setCommonErrorHandler(errorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.setConcurrency(3);
        return factory;
    }

    @KafkaListener(topics = "imc", groupId = "${spring.kafka.consumer.group-id}")
    public void listenToImc(@Payload String message) {

        if (message == null || message.trim().isEmpty()) {
            logger.error("Mensagem vazia recebida do tópico IMC");
            return;
        }

        try {
            // Tenta remover possíveis aspas extras ou caracteres não visíveis
            String cleanMessage = message.trim();
            if (cleanMessage.startsWith("\"") && cleanMessage.endsWith("\"")) {
                cleanMessage = cleanMessage.substring(1, cleanMessage.length() - 1);
                // Substitui aspas escapadas
                cleanMessage = cleanMessage.replace("\\\"", "\"");
            }

            objectMapper.enable(com.fasterxml.jackson.core.JsonParser.Feature
                    .INCLUDE_SOURCE_IN_LOCATION);

            Cliente cliente;
            try {
                cliente = objectMapper.readValue(cleanMessage, Cliente.class);
            } catch (com.fasterxml.jackson.core.JsonParseException e) {
                logger.error("Erro ao fazer parse do JSON. Verifique se a mensagem está no formato correto. Mensagem: {}", cleanMessage, e);
                throw new IllegalArgumentException("Formato de mensagem inválido. Esperado um JSON válido.", e);
            } catch (com.fasterxml.jackson.databind.JsonMappingException e) {
                logger.error("Erro ao mapear JSON para objeto Cliente. Verifique os campos da mensagem: {}", cleanMessage, e);
                throw new IllegalArgumentException("Falha ao mapear os dados do cliente. Verifique os campos fornecidos.", e);
            }

            emailService.sendEmailAsync(cliente);
            logger.info("E-mail de IMC enviado para: {}", cliente.getEmail());
            
        } catch (Exception e) {
            logger.error("Erro ao processar mensagem do tópico IMC. Mensagem: {}", message, e);
            //adicionar lógica para lidar com erros, como enviar para uma DLQ
        }
    }
}
