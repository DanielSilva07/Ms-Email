# 📧 MS Email Service

Microsserviço responsável pelo processamento assíncrono de mensagens Kafka e envio de e-mails com informações de clientes. Desenvolvido com Spring Boot, oferece alta disponibilidade, resiliência e monitoramento detalhado.

## 🚀 Funcionalidades

- **Consumo de Mensagens**
  - Integração com Apache Kafka
  - Processamento assíncrono de mensagens
  - Suporte a múltiplos tópicos e partições

- **Envio de E-mails**
  - Configuração SMTP flexível
  - Templates HTML personalizáveis com Thymeleaf
  - Suporte a anexos e formatação rica

- **Resiliência**
  - Circuit Breaker com Resilience4j
  - Retry com backoff exponencial
  - Timeout configurável
  - Fallbacks para falhas

- **Monitoramento**
  - Métricas detalhadas com Micrometer
  - Integração com Prometheus e Grafana
  - Health checks personalizados
  - Logs estruturados

- **Segurança**
  - Autenticação SMTP segura
  - Configuração sensível via variáveis de ambiente
  - Proteção contra injeção de templates

## 🛠️ Tecnologias Utilizadas

- **Backend**: Java 21, Spring Boot 3.x, Spring Kafka
- **Resiliência**: Resilience4j, Retry, Circuit Breaker
- **Monitoramento**: Micrometer, Prometheus, Grafana
- **Email**: JavaMail, Thymeleaf
- **Containerização**: Docker, Docker Compose
- **CI/CD**: GitHub Actions (opcional)
- **Documentação**: OpenAPI 3.0 (Swagger UI)

## 📁 Estrutura do Projeto

```
ms-email/
├── src/
│   ├── main/
│   │   ├── java/br/com/danielsilva/ms_email/
│   │   │   ├── MsEmailApplication.java       # Aplicação principal
│   │   │   ├── config/                       # Configurações
│   │   │   │   ├── KafkaConfig.java          # Configuração do Kafka
│   │   │   │   ├── MonitoringConfig.java     # Configuração de métricas
│   │   │   │   ├── OpenApiConfig.java        # Documentação da API
│   │   │   │   ├── ResilienceConfig.java     # Configuração de resiliência
│   │   │   │   └── ThymeleafConfig.java      # Configuração de templates
│   │   │   ├── controller/                   # Controladores REST
│   │   │   │   └── MetricsController.java    # Endpoints de métricas
│   │   │   ├── domain/                       # Modelos de domínio
│   │   │   │   ├── Cliente.java              # Entidade Cliente
│   │   │   │   └── EmailRetry.java           # Estratégias de retentativa
│   │   │   ├── dto/                          # Objetos de Transferência de Dados
│   │   │   ├── exceptions/                   # Tratamento de erros
│   │   │   │   ├── EmailException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── listener/kafka/               # Consumidores Kafka
│   │   │   │   ├── KafkaConsumer.java
│   │   │   │   └── KafkaErrorHandler.java
│   │   │   └── service/                      # Lógica de negócios
│   │   │       ├── EmailService.java         # Serviço de e-mail
│   │   │       ├── MetricsService.java       # Métricas personalizadas
│   │   │       └── EmailRetryService.java    # Lógica de retentativas
│   │   └── resources/                        # Recursos da aplicação
│   │       ├── application.yml               # Configurações
│   │       └── templates/                    # Templates de e-mail
│   └── test/                                 # Testes automatizados
└── docker/                                   # Configurações Docker
    ├── prometheus/
    │   └── prometheus.yml                   # Configuração do Prometheus
    └── grafana/
        └── dashboards/                      # Dashboards do Grafana
```

## 🚀 Começando

### 📋 Pré-requisitos

- **JDK 21**
- **Maven 3.6+** ou **Gradle 7.6+**
- **Docker** e **Docker Compose** (para ambiente de desenvolvimento)
- **Kafka** (executado via Docker Compose)
- **SMTP Server** (para testes locais, pode usar MailHog)
- **Prometheus** (opcional, para monitoramento)
- **Grafana** (opcional, para visualização de métricas)

## ⚙️ Configuração

 **Acessar ferramentas**
   - **Aplicação**: http://localhost:8080
   - **Actuator**: http://localhost:8080/actuator
   - **Prometheus**: http://localhost:9090
   - **Grafana**: http://localhost:3000 (usuário: admin, senha: admin)
   - **MailHog**: http://localhost:8025 (para visualizar e-mails em desenvolvimento)



## 📊 Monitoramento

A aplicação expõe métricas detalhadas através do Spring Boot Actuator e Prometheus.

### Endpoints de Monitoramento

| Endpoint                     | Descrição                                      |
|------------------------------|-----------------------------------------------|
| `GET /actuator/health`       | Status de saúde da aplicação                   |
| `GET /actuator/metrics`      | Lista todas as métricas disponíveis            |
| `GET /actuator/prometheus`   | Métricas no formato Prometheus                 |
| `GET /actuator/info`         | Informações da aplicação                       |
| `GET /api/metrics/health`    | Health check personalizado                     |

### Métricas Personalizadas

| Métrica                          | Tipo      | Descrição                                   |
|----------------------------------|-----------|--------------------------------------------|
| `email_sent_total`               | Counter   | Total de e-mails enviados com sucesso      |
| `email_errors_total`             | Counter   | Total de falhas no envio de e-mails        |
| `email_processing_duration`      | Timer     | Tempo de processamento de e-mails          |
| `kafka_consumer_records_consumed`| Counter   | Mensagens consumidas do Kafka              |
| `kafka_consumer_errors`          | Counter   | Erros no consumidor Kafka                  |
| `retry_attempts`                 | Counter   | Tentativas de retentativa de envio         |
| `circuit_breaker_state`          | Gauge     | Estado atual do circuit breaker            |

### Grafana

Um dashboard de exemplo está disponível em `docker/grafana/dashboards/email-dashboard.json` que pode ser importado para visualizar as métricas de forma gráfica.

## 🔄 Resiliência

O serviço implementa padrões de resiliência usando Resilience4j para garantir operação estável mesmo em condições adversas.

### Circuit Breaker

- Abre o circuito após falhas consecutivas
- Estado atual exposto nas métricas

```yaml
resilience4j.circuitbreaker:
  configs:
    default:
      registerHealthIndicator: true
      slidingWindowSize: 10
      permittedNumberOfCallsInHalfOpenState: 3
      slidingWindowType: COUNT_BASED
      minimumNumberOfCalls: 5
      waitDurationInOpenState: 5s
      failureRateThreshold: 50
```

### Retry

- Tentativas de reenvio com backoff exponencial
- Número máximo de tentativas configurável
- Filtro de exceções para retentativas

```yaml
resilience4j.retry:
  configs:
    default:
      maxAttempts: 3
      waitDuration: 1s
      enableExponentialBackoff: true
      exponentialBackoffMultiplier: 2
```


## 📨 Formato da Mensagem

O serviço consome mensagens no formato JSON do tópico Kafka configurado. O formato esperado é:

```json
{
  "id": 1,
  "nome": "João da Silva",
  "email": "joao.silva@example.com",
  "peso": 70.5,
  "altura": 1.75
}
```

### Campos Obrigatórios

| Campo  | Tipo    | Descrição                     |
|--------|---------|--------------------------------|
| id     | Long    | Identificador único do cliente |
| nome   | String  | Nome completo do cliente       |
| email  | String  | E-mail válido do cliente       |
| peso   | Double  | Peso em quilogramas           |
| altura | Double  | Altura em metros              |

## 🛠️ Desenvolvimento

### Estrutura de Código

- **`config/`**: Classes de configuração do Spring
- **`controller/`**: Controladores REST
- **`domain/`**: Entidades do domínio
- **`dto/`**: Objetos de transferência de dados
- **`exceptions/`**: Tratamento de exceções
- **`listener/kafka/`**: Consumidores Kafka
- **`service/`**: Lógica de negócios
- **`resources/`**: Arquivos de configuração e templates


## 📄 Licença

Este projeto está licenciado sob a licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

## 🤝 Contribuição

1. Faça um Fork do projeto
2. Crie uma Branch para sua Feature (`git checkout -b feature/AmazingFeature`)
3. Adicione suas mudanças (`git add .`)
4. Comite suas mudanças (`git commit -m 'Add some AmazingFeature'`)
5. Faça o Push da Branch (`git push origin feature/AmazingFeature`)
6. Abra um Pull Request

## 📧 Contato

Daniel Silva - [@seuemail@example.com](mailto:seuemail@example.com)

Link do Projeto: [https://github.com/seu-usuario/ms-email](https://github.com/seu-usuario/ms-email)
   
   Cole o JSON com os dados do cliente.

2. O serviço irá processar a mensagem e enviar um e-mail para o cliente com o cálculo do IMC.

## Logs

O serviço gera logs detalhados para facilitar a depuração, incluindo:
- Recebimento de mensagens
- Processamento do JSON
- Erros de validação
- Confirmação de envio de e-mail

## Tratamento de Erros

- Mensagens inválidas são registradas em log e ignoradas
- Erros no envio de e-mail são registrados para posterior análise
- O serviço continua processando novas mensagens mesmo em caso de falha

## Melhorias Futuras

- Implementar DLQ (Dead Letter Queue) para mensagens com falha
- Adicionar métricas de monitoramento
- Implementar testes unitários e de integração
- Adicionar suporte a templates de e-mail mais elaborados
