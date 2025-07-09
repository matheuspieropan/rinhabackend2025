# Rinha de Backend 2025 - Solução em Java com Spring + Threads Virtuais

Esta é a minha solução para o desafio [Rinha de Backend 2025](https://github.com/luizalabs/rinha-de-backend-2025),
implementada em **Java 21** utilizando o **Spring Web MVC** tradicional com **threads virtuais (Project Loom)**.

Optei por **não utilizar WebFlux**, tanto pela complexidade adicional que ele impõe quanto pelo fato de que o modelo
baseado em **threads virtuais já entrega uma abordagem leve e não bloqueante**, compatível com os requisitos de alta
concorrência do desafio.

---

## Considerações Técnicas

### Arquitetura e Processamento Assíncrono

A aplicação foi projetada para oferecer o melhor desempenho possível no cenário da **Rinha de Backend 2025**, utilizando **Java 21**, **Threads Virtuais (Project Loom)** e **MongoDB** como banco de dados principal.

#### Processamento de Pagamentos

Quando uma requisição de pagamento é recebida, ela é tratada de forma **assíncrona**, retornando `200 OK` imediatamente para o cliente. Enquanto isso, uma **Thread Virtual** continua o processamento real em segundo plano. Isso garante alta responsividade da API mesmo em cenários de alta concorrência.

As requisições de pagamento são submetidas a um **serviço processador de pagamentos externo**, e o sistema escolhe o melhor processador com base em uma estratégia dinâmica explicada a seguir.

#### Escolha Inteligente do Processador de Pagamentos

A Rinha disponibiliza um endpoint `GET /payments/service-health` em ambos os processadores (default e fallback), que retorna:

```json
{
  "failing": false,
  "minResponseTime": 100
}
```

Esse endpoint só pode ser chamado **uma vez a cada 5 segundos**, sob risco de retornar `HTTP 429 Too Many Requests`.

Para contornar isso, implementamos um **job agendado a cada 5100ms** que faz chamadas de health-check a ambos os processadores. Com base na resposta, um objeto `MelhorOpcao` é atualizado:

```java
public record MelhorOpcao(boolean processadorDefault, int timeoutIndicado) {}
```

Esse objeto representa qual processador está saudável e qual possui o menor tempo de resposta (`minResponseTime`), com um fallback interno caso ambos falhem.

#### Código: Seleção do Melhor Processador

```java
@Scheduled(initialDelay = 5000, fixedDelay = 5100)
public void checaProcessadorDefault() {
  HealthResponse healthResponseDefault = null;
  HealthResponse healthResponseFallback = null;

  try {
    healthResponseDefault = pagamentoProcessorDefaultClient.healthCheck();
  } catch (Exception ignored) {}

  try {
    healthResponseFallback = pagamentoProcessorFallbackClient.healthCheck();
  } catch (Exception ignored) {}

  if (healthResponseDefault == null && healthResponseFallback == null) {
    MELHOR_OPCAO = null;
    return;
  }

  // Lógica de decisão entre default e fallback
    [...]
}
```

Essa opção é usada em tempo real pela aplicação no momento de decidir para qual processador enviar a requisição.

---
### Consulta de Pagamentos (MongoDB Aggregation)

Para o endpoint de consulta (`/payments-summary?from=...&to=...`), foi utilizada uma **pipeline de agregação no MongoDB** para filtrar, agrupar e sumarizar os pagamentos registrados.

Essa abordagem evita a sobrecarga da aplicação e permite que a base NoSQL realize a maior parte do trabalho computacional, sendo altamente eficiente para operações de leitura intensiva.

---
## 🧪 Testando localmente

Este repositório já contém **tudo o que você precisa para rodar e testar a aplicação localmente**.

### Pré-requisitos

- Docker + Docker Compose
- Java 21 (caso deseje rodar fora do container)
- [k6](https://grafana.com/docs/k6/latest/set-up/install-k6/) instalado globalmente (para executar o teste de carga)

---

## 🗂 Estrutura do Projeto

```
.
├── rinha-test/                 # Pasta com o teste de carga (fornecido pela organização)
├── script/
│   └── nginx.conf              # Configuração do nginx reverse proxy
├── src/                        # Código fonte Java (Spring)
├── init.sql                    # Script de criação de schema no PostgreSQL (fornecido pela organização)
├── docker-compose-rinha.yml    # Infraestrutura base (nginx, postgres) fornecida pela organização
├── docker-compose.yml          # Minha aplicação (Java Spring Boot + MongoDB + Nginx)
```

---

## 🚀 Como executar

Siga os passos abaixo para rodar o sistema completo localmente:

### 1. Suba a infraestrutura base:

```bash
docker-compose -f docker-compose-rinha.yml up -d
```

> ⚠️ Este passo é **obrigatório**, pois ele cria uma rede docker (`payment-processor`) que será utilizada no próximo passo.

### 2. Suba infraestrutura da minha solução

```bash
docker-compose up --build -d
```

### 3. Execute o teste de carga:

```bash
cd rinha-test
k6 run rinha.js
```
---

## Build Nativo com GraalVM

Para gerar uma imagem nativa com GraalVM (Java 21), a solução utiliza o **Native Image Agent** para coletar as configurações de reflexão necessárias, seguido da construção da imagem nativa via Maven.

### Passo a passo para gerar arquivos de reflexão

1. Entre no ambiente WSL na pasta raiz do projeto.

2. Execute o comando abaixo para iniciar a aplicação com o agente de GraalVM, que irá coletar as configurações de acesso reflexivo usadas durante a execução:

```bash
$JAVA_HOME/bin/java \
  -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image \
  -jar target/rinha2025spring-0.0.1-SNAPSHOT.jar
```

> Esse comando executa a aplicação normalmente, gerando os arquivos necessários na pasta `src/main/resources/META-INF/native-image` para o build nativo.

### Construção da imagem nativa

3. Com os arquivos de configuração gerados, execute o build nativo usando Maven:

```bash
mvn package -Pnative -DskipTests -Dspring.native.remove-unused-autoconfig=true
```

> O perfil `native` já está configurado no `pom.xml` para usar o plugin do GraalVM Native Image e gerar o executável nativo.

### Dockerfile para execução do binário nativo

4. Para rodar a aplicação compilada nativamente, utilize o seguinte Dockerfile baseado em Alpine Linux:

```dockerfile
FROM alpine:3.19

RUN apk add --no-cache libc6-compat

WORKDIR /app

COPY target/rinha2025 /app/rinha2025

RUN chmod +x /app/rinha2025

EXPOSE 8080

ENTRYPOINT ["./rinha2025"]
```

> Este Dockerfile copia o binário nativo gerado para dentro do container leve Alpine, configura permissões, expõe a porta 8080 e define o entrypoint.

---

## ✍️ Autor e créditos

**Matheus Pieropan** — Desenvolvedor e autor da solução.

Esta documentação foi auxiliada pela inteligência artificial **ChatGPT (OpenAI)**, que ajudou na formatação e esclarecimento das partes técnicas.