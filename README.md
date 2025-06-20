# order-service

Microserviço de gestão de pedidos para sistema B2B, com alta concorrência, escalável e performático.

API REST para gerenciamento de pedidos com os seguintes endpoints:
- Cadastro de pedidos
- Consulta de pedidos (por ID, por período, por status)
- Atualização de status de pedidos
- Cancelamento de pedidos

Cada pedido contem:
- ID único
- ID do parceiro
- Lista de itens (produto, quantidade, preço unitário)
- Valor total
- Status (PENDENTE, APROVADO, EM_PROCESSAMENTO, ENVIADO, ENTREGUE, CANCELADO)
- Data de criação
- Data de última atualização

Sistema de créditos para parceiros:
- Cada parceiro possui um limite de crédito
- Ao criar um pedido, o sistema deve verificar se o parceiro possui crédito suficiente
- Ao aprovar um pedido, o sistema deve debitar o valor do crédito do parceiro

Mecanismo de notificação para mudanças de status de pedidos (sistema de mensageria)

---

## Tecnologias
- Java 21, Spring Boot 3.5.0
- PostgreSQL 16.3 + Flyway
- RabbitMQ (mensageria)
- Testcontainers (testes de integração)
- Docker + docker-compose
---

## Requisitos
- Docker e Docker Compose instalados
- Java 21
- Maven 3.8+
---

# Rodando o ambiente completo (Docker)
Execute os comando estando na raiz do projeto

## Comandos
### Build da aplicação
./mvnw clean package -DskipTests
### Executar ambiente Docker Compose
docker-compose up -d
### Para ambiente Docker Compose
docker-compose down

## Acessos Importantes
### API REST - Docs
http://localhost:8080/api-docs
### API REST - Swagger
http://localhost:8080/api/swagger-ui.html
### PGAdmin
http://localhost:5050 (login configurado no docker-compose)
### RabbitMQ Management
http://localhost:15672 (login configurado no docker-compose)