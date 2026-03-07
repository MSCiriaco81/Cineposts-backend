# 🎬 CinePosts — Estrutura do Backend

Guia explicativo de cada pasta e arquivo do projeto.

---

## 📁 Visão Geral

```
cineposts/
├── src/main/java/com/cineposts/
│   ├── config/
│   ├── security/
│   ├── model/
│   ├── repository/
│   ├── dto/
│   ├── service/
│   ├── controller/
│   ├── exception/
│   └── CinePostsApplication.java
├── src/main/resources/
│   └── application.properties
├── pom.xml
├── .gitignore
└── README.md
```

---

## 📂 `config/`

Contém todas as classes de configuração da aplicação. Cada classe é responsável por configurar um aspecto específico do sistema.

```
config/
├── SecurityConfig.java
├── SwaggerConfig.java
├── MongoConfig.java
├── CorsConfig.java
└── DataInitializer.java
```

| Arquivo | Responsabilidade |
|---|---|
| `SecurityConfig.java` | Configura o Spring Security: quais rotas são públicas, quais exigem autenticação, qual papel acessa cada endpoint, e registra o filtro JWT na cadeia de filtros |
| `SwaggerConfig.java` | Configura a documentação interativa OpenAPI/Swagger, incluindo o suporte a autenticação via Bearer Token diretamente pelo Swagger UI |
| `MongoConfig.java` | Habilita a auditoria automática do MongoDB, permitindo que os campos `createdAt` e `updatedAt` sejam preenchidos automaticamente pelo Spring Data |
| `CorsConfig.java` | Libera o acesso cross-origin para o frontend (localhost:5173), necessário para que o browser permita requisições entre origens diferentes |
| `DataInitializer.java` | Executado na inicialização da aplicação — verifica se o usuário `admin` já existe e, caso não exista, cria automaticamente com senha padrão |

---

## 📂 `security/`

Responsável por toda a infraestrutura de autenticação e segurança baseada em JWT.

```
security/
├── JwtTokenProvider.java
├── JwtAuthenticationFilter.java
└── UserDetailsServiceImpl.java
```

| Arquivo | Responsabilidade |
|---|---|
| `JwtTokenProvider.java` | Gera tokens JWT após login bem-sucedido, valida tokens recebidos nas requisições, e extrai o username do token para identificar o usuário |
| `JwtAuthenticationFilter.java` | Intercepta todas as requisições HTTP antes de chegarem aos controllers. Extrai o token do header `Authorization`, valida com o `JwtTokenProvider` e popula o `SecurityContext` com o usuário autenticado |
| `UserDetailsServiceImpl.java` | Implementação do `UserDetailsService` do Spring Security. Busca o usuário no MongoDB pelo username e constrói o objeto de autenticação com suas permissões (`ROLE_ADMIN` ou `ROLE_USER`) |

---

## 📂 `model/`

Contém as entidades que representam os documentos armazenados no MongoDB, e os enums que definem os valores permitidos para cada campo.

```
model/
├── User.java
├── Content.java
├── PostSuggestion.java
├── EditRequest.java
└── enums/
    ├── Role.java
    ├── ContentType.java
    ├── ContentStatus.java
    ├── Platform.java
    ├── SuggestionStatus.java
    └── EditRequestStatus.java
```

### Documentos

| Arquivo | Coleção MongoDB | Descrição |
|---|---|---|
| `User.java` | `users` | Representa um membro da equipe com username, senha criptografada, papel e status ativo/inativo |
| `Content.java` | `contents` | Registro editorial base — trivia, aniversário de filme, bastidores, etc. É a matéria-prima para geração de posts |
| `PostSuggestion.java` | `post_suggestions` | Sugestão de post gerada a partir de um conteúdo, com hook, caption, hashtags e CTA específicos para Twitter ou Instagram |
| `EditRequest.java` | `edit_requests` | Solicitação de alteração em um conteúdo já aprovado. Armazena um snapshot do conteúdo original e as mudanças propostas para revisão do admin |

### Enums

| Arquivo | Valores | Usado em |
|---|---|---|
| `Role.java` | `ADMIN`, `USER` | `User` |
| `ContentType.java` | `TRIVIA`, `PERSON_BIRTHDAY`, `MOVIE_ANNIVERSARY`, `SERIES_ANNIVERSARY`, `TV_SHOW_ANNIVERSARY`, `RELEASE_REMINDER`, `BEHIND_THE_SCENES`, `COMPARISON`, `RECOMMENDATION` | `Content` |
| `ContentStatus.java` | `PENDING`, `APPROVED`, `REJECTED` | `Content` |
| `Platform.java` | `TWITTER`, `INSTAGRAM` | `PostSuggestion` |
| `SuggestionStatus.java` | `DRAFT`, `READY`, `ARCHIVED` | `PostSuggestion` |
| `EditRequestStatus.java` | `PENDING`, `APPROVED`, `REJECTED` | `EditRequest` |

---

## 📂 `repository/`

Interfaces que estendem `MongoRepository`, fornecendo acesso ao banco de dados sem necessidade de escrever queries manualmente. O Spring Data MongoDB implementa os métodos automaticamente em tempo de execução.

```
repository/
├── UserRepository.java
├── ContentRepository.java
├── PostSuggestionRepository.java
└── EditRequestRepository.java
```

| Arquivo | Métodos principais |
|---|---|
| `UserRepository.java` | `findByUsername`, `existsByUsername` |
| `ContentRepository.java` | `findByCreatedBy`, `findByStatus`, `findAllByOrderByCreatedAtDesc` |
| `PostSuggestionRepository.java` | `findByContentId`, `findByCreatedBy`, `findByContentIdAndPlatform`, `findAllByOrderByCreatedAtDesc` |
| `EditRequestRepository.java` | `findByContentId`, `findByStatus`, `findByRequestedBy` |

---

## 📂 `dto/`

Data Transfer Objects — objetos simples usados para receber dados das requisições (`request`) e enviar respostas ao cliente (`response`). Separam a camada de API dos modelos internos do banco de dados.

```
dto/
├── request/
│   ├── LoginRequest.java
│   ├── CreateUserRequest.java
│   ├── UpdateUserRequest.java
│   ├── CreateContentRequest.java
│   ├── UpdateContentRequest.java
│   ├── UpdateSuggestionRequest.java
│   └── EditRequestPayload.java
└── response/
    ├── AuthResponse.java
    ├── UserResponse.java
    ├── ContentResponse.java
    ├── PostSuggestionResponse.java
    └── EditRequestResponse.java
```

### Request DTOs

| Arquivo | Usado em | Descrição |
|---|---|---|
| `LoginRequest.java` | `POST /auth/login` | Recebe username e senha |
| `CreateUserRequest.java` | `POST /admin/users` | Dados para criação de novo usuário |
| `UpdateUserRequest.java` | `PUT /admin/users/{id}` | Campos opcionais para atualização de usuário |
| `CreateContentRequest.java` | `POST /contents` | Dados completos para criação de conteúdo editorial |
| `UpdateContentRequest.java` | `PUT /contents/{id}` | Campos opcionais para atualização de conteúdo |
| `UpdateSuggestionRequest.java` | `PUT /post-suggestions/{id}` | Campos opcionais para edição de sugestão |
| `EditRequestPayload.java` | `POST /contents/{id}/edit-request` | Mapa com os campos que o usuário deseja alterar |

### Response DTOs

| Arquivo | Descrição |
|---|---|
| `AuthResponse.java` | Retorna o token JWT, username e papel após login |
| `UserResponse.java` | Dados do usuário sem expor a senha |
| `ContentResponse.java` | Dados completos do conteúdo editorial |
| `PostSuggestionResponse.java` | Dados completos da sugestão de post |
| `EditRequestResponse.java` | Dados da solicitação com snapshot e mudanças propostas |

---

## 📂 `service/`

Camada de negócio da aplicação. Toda regra de negócio, validação e orquestração de operações vive aqui. Os controllers apenas recebem requisições e delegam para os services.

```
service/
├── AuthService.java
├── AdminService.java
├── ContentService.java
├── PostGeneratorService.java
├── PostSuggestionService.java
├── EditRequestService.java
└── FeedService.java
```

| Arquivo | Responsabilidade |
|---|---|
| `AuthService.java` | Autentica o usuário via `AuthenticationManager`, gera o JWT e retorna os dados do usuário logado |
| `AdminService.java` | Gerenciamento de usuários pelo admin: criação, listagem, atualização de papel/senha/status e soft-delete (desativação) |
| `ContentService.java` | CRUD completo de conteúdos com validação de ownership — apenas o dono ou admin pode editar/deletar. Bloqueia edição direta de conteúdos aprovados por usuários comuns |
| `PostGeneratorService.java` | Gerador de posts baseado em regras. Para cada tipo de conteúdo e plataforma, aplica templates diferentes de hook, caption, hashtags e CTA. Isolado para facilitar substituição futura por IA |
| `PostSuggestionService.java` | Orquestra a geração de sugestões chamando o `PostGeneratorService`, e gerencia listagem e edição com validação de ownership |
| `EditRequestService.java` | Gerencia o fluxo completo de solicitações de edição: criação com snapshot do conteúdo original, aprovação (aplica mudanças no conteúdo) e rejeição |
| `FeedService.java` | Agrega conteúdos e sugestões de toda a equipe ordenados por data para o feed colaborativo |

---

## 📂 `controller/`

Camada de entrada da API. Os controllers recebem as requisições HTTP, extraem os dados necessários e delegam para os services. Não contém regras de negócio.

```
controller/
├── AuthController.java
├── AdminController.java
├── ContentController.java
├── PostSuggestionController.java
├── EditRequestController.java
└── FeedController.java
```

| Arquivo | Base URL | Descrição |
|---|---|---|
| `AuthController.java` | `/auth` | Endpoint de login público |
| `AdminController.java` | `/admin` | Gerenciamento de usuários e revisão de solicitações de edição. Todos os endpoints exigem papel ADMIN |
| `ContentController.java` | `/contents` | CRUD de conteúdos editoriais |
| `PostSuggestionController.java` | `/contents/{id}/suggestions`, `/post-suggestions` | Geração e gerenciamento de sugestões de posts |
| `EditRequestController.java` | `/contents/{id}/edit-request` | Submissão de solicitações de edição por usuários |
| `FeedController.java` | `/team` | Feed colaborativo com conteúdos e sugestões de toda a equipe |

---

## 📂 `exception/`

Tratamento centralizado de erros. O `GlobalExceptionHandler` intercepta todas as exceções lançadas na aplicação e retorna respostas HTTP padronizadas com mensagem e status code adequados.

```
exception/
├── GlobalExceptionHandler.java
├── ResourceNotFoundException.java
├── UnauthorizedActionException.java
└── BusinessRuleException.java
```

| Arquivo | HTTP Status | Quando é lançada |
|---|---|---|
| `ResourceNotFoundException.java` | `404 Not Found` | Quando um documento não é encontrado no banco pelo ID fornecido |
| `UnauthorizedActionException.java` | `403 Forbidden` | Quando um usuário tenta realizar uma ação em um recurso que não é seu |
| `BusinessRuleException.java` | `422 Unprocessable Entity` | Quando uma regra de negócio é violada (ex: editar conteúdo aprovado diretamente) |
| `GlobalExceptionHandler.java` | — | Captura todas as exceções acima, além de `AccessDeniedException`, `BadCredentialsException` e erros de validação de campos |

---

## 📄 `CinePostsApplication.java`

Classe principal da aplicação. Contém o método `main` que inicializa o Spring Boot.

---

## 📄 `application.properties`

Arquivo de configuração central da aplicação.

```properties
# Conexão com MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/cineposts

# Segredo e expiração do JWT
jwt.secret=sua-chave-secreta
jwt.expiration=86400000

# Porta do servidor
server.port=8080
```

> ⚠️ Nunca versione este arquivo com credenciais reais. Use variáveis de ambiente em produção.

---

## 📄 `pom.xml`

Arquivo de configuração do Maven. Define as dependências do projeto (Spring Boot, MongoDB, JWT, Lombok, Swagger), a versão do Java e o plugin de build.