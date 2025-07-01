# Diagrama de Arquitetura - MS Email

```mermaid
graph TD
    subgraph "MS-Email"
        A[Kafka Consumer] -->|Consome mensagens| B[EmailService]
        B -->|Envia e-mail| C[SMTP Server]
        D[Template Engine] -->|Gera conteúdo| B
    end

    subgraph "Sistema Externo"
        E[Kafka] -->|Publica mensagens| A
    end

    subgraph "Destino"
        C -->|Entrega| F[Cliente Email]
    end

    style MS-Email fill:#f9f,stroke:#333,stroke-width:2px
    style Sistema_Externo fill:#bbf,stroke:#333,stroke-width:2px
    style Destino fill:#bfb,stroke:#333,stroke-width:2px
```

## Descrição dos Componentes

### MS-Email (Microserviço)
- **Kafka Consumer**: Consome mensagens de um tópico Kafka
- **EmailService**: Processa as mensagens e gerencia o envio de e-mails
- **Template Engine**: Gera o conteúdo HTML dos e-mails usando templates

### Sistema Externo
- **Kafka**: Broker de mensagens que publica eventos para o microserviço

### Destino
- **SMTP Server**: Servidor de e-mail responsável pelo envio das mensagens
- **Cliente Email**: Destinatário final dos e-mails

## Fluxo de Dados
1. Um evento é publicado no Kafka
2. O Kafka Consumer do MS-Email consome a mensagem
3. O EmailService processa a mensagem e gera o e-mail usando o Template Engine
4. O e-mail é enviado através do servidor SMTP configurado
5. O destinatário final recebe o e-mail

## Dependências Principais
- Spring Boot 3.3.2
- Spring Kafka
- Spring Mail
- Thymeleaf (Template Engine)
- Lombok
- Spring Web
- Spring AMQP (RabbitMQ)
- Jackson (JSON processing)
