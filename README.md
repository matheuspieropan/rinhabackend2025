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

# 🚀 Build Nativo com GraalVM

Esta aplicação foi preparada para rodar como **imagem nativa compilada com GraalVM** usando **Java 21**. A geração do binário é feita de forma automatizada através de **buildpacks**, sem a necessidade de executar manualmente o agente ou mexer no WSL.

---

## 🧠 Por que GraalVM exige configuração extra?

A **GraalVM** realiza **compilação estática** para gerar binários nativos, e não consegue identificar automaticamente classes acessadas via:

- Reflection
- Proxies dinâmicos
- Inicializações em tempo de execução

Por isso, o uso de bibliotecas como Spring exige configuração explícita dessas classes — ou o uso de ferramentas que abstraem isso.

---

## 🛠️ Geração da imagem nativa com buildpacks (recomendado)

### ✅ Pré-requisitos

- Docker instalado e em execução
- Maven 3.9+
- Java 21 (de preferência da distribuição GraalVM)
- Conta no Docker Hub (opcional, se quiser publicar a imagem)

### 📦 Build nativo com um único comando

Execute:

```bash
./mvnw spring-boot:build-image -Pnative -DskipTests
```

Esse comando:

- Usa o plugin `spring-boot-maven-plugin` com buildpacks
- Ativa a variável de ambiente `BP_NATIVE_IMAGE=true`, instruindo o buildpack a gerar uma imagem nativa com GraalVM
- Empacota a imagem com o nome configurado no `pom.xml` (ex: `matheuspieropan/rinhabackend2025:graalvm`)
- (Opcional) Realiza `docker push` automático para o Docker Hub se você estiver autenticado

---

## 🐳 Dockerfile (execução manual opcional)

Caso deseje controlar manualmente o build e a execução do binário nativo, use o Dockerfile abaixo:

```dockerfile
FROM alpine:3.19

RUN apk add --no-cache libc6-compat

WORKDIR /app

COPY target/rinha2025 /app/rinha2025
RUN chmod +x /app/rinha2025

EXPOSE 8080

ENTRYPOINT ["./rinha2025"]
```

---

## 🔁 Alternativa manual (com agente da GraalVM)

Se desejar gerar os arquivos de configuração reflexiva manualmente, execute:

```bash
$JAVA_HOME/bin/java \
  -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image \
  -jar target/rinha2025spring-0.0.1-SNAPSHOT.jar
```

Depois disso, rode:

```bash
mvn package -Pnative -DskipTests
```

Os arquivos gerados conterão as instruções para manter classes e métodos usados via reflection.

---

## 🔧 Plugins utilizados no `pom.xml`

```xml
<plugin>
  <groupId>org.graalvm.buildtools</groupId>
  <artifactId>native-maven-plugin</artifactId>
</plugin>

<plugin>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-maven-plugin</artifactId>
  <configuration>
    <image>
      <name>matheuspieropan/rinhabackend2025:graalvm</name>
      <builder>paketobuildpacks/builder-jammy-java-tiny:latest</builder>
      <env>
        <BP_NATIVE_IMAGE>true</BP_NATIVE_IMAGE>
        <BP_JVM_VERSION>21</BP_JVM_VERSION>
      </env>
    </image>
  </configuration>
</plugin>
```

---

✅ Com esse setup, você consegue gerar, empacotar e publicar sua aplicação nativa com apenas um comando — simples, performático e sem complicações com reflection!

> Este Dockerfile copia o binário nativo gerado para dentro do container leve Alpine, configura permissões, expõe a porta 8080 e define o entrypoint.

---

## ✍️ Autor e créditos

**Matheus Pieropan** — Desenvolvedor e autor da solução.

Esta documentação foi auxiliada pela inteligência artificial **ChatGPT (OpenAI)**, que ajudou na formatação e esclarecimento das partes técnicas.