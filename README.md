# 🎬 CinePosts — Backend

Plataforma editorial interna para gestão e geração de conteúdo para redes sociais (Twitter/X e Instagram), focada em cinema, séries, TV e entretenimento.

---

## 📋 Sobre o Projeto

O **CinePosts** é uma ferramenta interna utilizada por equipes de conteúdo para centralizar informações editoriais e gerar sugestões de posts otimizadas para diferentes plataformas sociais. O sistema **não é uma rede social pública** — é um painel de gestão colaborativa para times de criação.

### Funcionalidades principais

- Autenticação com JWT
- Sistema de usuários com controle de papéis (ADMIN / USER)
- CRUD completo de conteúdos editoriais
- Geração automática de sugestões de posts para Twitter e Instagram
- Feed colaborativo onde todos os membros da equipe podem visualizar conteúdos e sugestões
- Sistema de solicitações de edição com aprovação por administrador
- Documentação interativa via Swagger/OpenAPI

---

## 🛠️ Tecnologias

| Tecnologia | Versão | Função |
|---|---|---|
| Java | 21 | Linguagem principal |
| Spring Boot | 3.3.0 | Framework base |
| Spring Security | 6.x | Autenticação e autorização |
| Spring Data MongoDB | 4.x | Persistência de dados |
| MongoDB | 7.x | Banco de dados |
| JWT (jjwt) | 0.11.5 | Geração e validação de tokens |
| Springdoc OpenAPI | 2.5.0 | Documentação Swagger |
| Lombok | latest | Redução de boilerplate |
| Maven | 3.x | Gerenciamento de dependências |

---

## 📁 Estrutura do Projeto

```
src/main/java/com/cineposts/
│
├── config/                  # Configurações gerais
│   ├── SecurityConfig.java  # Configuração do Spring Security
│   ├── SwaggerConfig.java   # Configuração do OpenAPI/Swagger
│   ├── MongoConfig.java     # Habilita auditoria MongoDB
│   ├── CorsConfig.java      # Configuração de CORS
│   └── DataInitializer.java # Seed do usuário admin padrão
│
├── security/                # Infraestrutura de segurança JWT
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── UserDetailsServiceImpl.java
│
├── model/                   # Documentos MongoDB
│   ├── User.java
│   ├── Content.java
│   ├── PostSuggestion.java
│   ├── EditRequest.java
│   └── enums/               # Role, ContentType, ContentStatus, Platform, etc.
│
├── repository/              # Interfaces Spring Data MongoDB
│   ├── UserRepository.java
│   ├── ContentRepository.java
│   ├── PostSuggestionRepository.java
│   └── EditRequestRepository.java
│
├── dto/                     # Objetos de transferência de dados
│   ├── request/             # DTOs de entrada
│   └── response/            # DTOs de saída
│
├── service/                 # Regras de negócio
│   ├── AuthService.java
│   ├── AdminService.java
│   ├── ContentService.java
│   ├── PostGeneratorService.java   # Gerador rule-based de posts
│   ├── PostSuggestionService.java
│   ├── EditRequestService.java
│   └── FeedService.java
│
├── controller/              # Endpoints REST
│   ├── AuthController.java
│   ├── AdminController.java
│   ├── ContentController.java
│   ├── PostSuggestionController.java
│   ├── EditRequestController.java
│   └── FeedController.java
│
└── exception/               # Tratamento centralizado de erros
    ├── GlobalExceptionHandler.java
    ├── ResourceNotFoundException.java
    ├── UnauthorizedActionException.java
    └── BusinessRuleException.java
```

---

## 🗄️ Coleções MongoDB

### `users`
Armazena os usuários da plataforma.

| Campo | Tipo | Descrição |
|---|---|---|
| `_id` | ObjectId | Identificador único |
| `username` | String | Nome de usuário (único) |
| `password` | String | Senha criptografada com BCrypt |
| `role` | Enum | `ADMIN` ou `USER` |
| `active` | Boolean | Se o usuário está ativo |
| `createdAt` | DateTime | Data de criação |

### `contents`
Conteúdos editoriais base — a matéria-prima para geração de posts.

| Campo | Tipo | Descrição |
|---|---|---|
| `type` | Enum | Tipo do conteúdo (ver abaixo) |
| `title` | String | Título do conteúdo |
| `description` | String | Descrição editorial |
| `relatedTitle` | String | Título de obra relacionada |
| `relatedPerson` | String | Pessoa relacionada (ator, diretor) |
| `eventDate` | Date | Data do evento (aniversário, lançamento) |
| `tags` | String[] | Tags para categorização |
| `status` | Enum | `PENDING`, `APPROVED`, `REJECTED` |
| `createdBy` | ObjectId | Referência ao usuário criador |
| `createdByUsername` | String | Username desnormalizado para performance |

**Tipos de conteúdo disponíveis:**
`TRIVIA` · `PERSON_BIRTHDAY` · `MOVIE_ANNIVERSARY` · `SERIES_ANNIVERSARY` · `TV_SHOW_ANNIVERSARY` · `RELEASE_REMINDER` · `BEHIND_THE_SCENES` · `COMPARISON` · `RECOMMENDATION`

### `post_suggestions`
Sugestões de posts geradas a partir de um conteúdo.

| Campo | Tipo | Descrição |
|---|---|---|
| `contentId` | ObjectId | Referência ao conteúdo base |
| `platform` | Enum | `TWITTER` ou `INSTAGRAM` |
| `hook` | String | Frase de abertura do post |
| `caption` | String | Corpo do post |
| `hashtags` | String[] | Hashtags sugeridas |
| `cta` | String | Call-to-action de engajamento |
| `status` | Enum | `DRAFT`, `READY`, `ARCHIVED` |

### `edit_requests`
Solicitações de alteração em conteúdos já aprovados.

| Campo | Tipo | Descrição |
|---|---|---|
| `contentId` | ObjectId | Conteúdo alvo da solicitação |
| `requestedBy` | ObjectId | Usuário solicitante |
| `originalSnapshot` | Map | Cópia completa do conteúdo no momento da solicitação |
| `proposedChanges` | Map | Apenas os campos que se deseja alterar |
| `status` | Enum | `PENDING`, `APPROVED`, `REJECTED` |
| `reviewedBy` | ObjectId | Admin que revisou |
| `reviewedAt` | DateTime | Data da revisão |

---

## 🔐 Segurança

### Fluxo de autenticação

```
1. POST /auth/login  →  valida credenciais
2. Retorna JWT (HS256, validade 24h)
3. Todas as requisições protegidas exigem: Authorization: Bearer <token>
4. JwtAuthenticationFilter valida o token e popula o SecurityContext
```

### Papéis e permissões

| Ação | USER | ADMIN |
|---|:---:|:---:|
| Login | ✅ | ✅ |
| Criar conteúdo | ✅ | ✅ |
| Editar próprio conteúdo (PENDING) | ✅ | ✅ |
| Editar conteúdo APROVADO | ❌ (via EditRequest) | ✅ |
| Gerar sugestões de post | ✅ | ✅ |
| Editar própria sugestão | ✅ | ✅ |
| Editar sugestão de outro usuário | ❌ | ✅ |
| Ver feed da equipe | ✅ | ✅ |
| Criar usuários | ❌ | ✅ |
| Gerenciar usuários | ❌ | ✅ |
| Aprovar/rejeitar EditRequests | ❌ | ✅ |

---

## ▶️ Como Executar

### Pré-requisitos

- Java 21+
- Maven 3.8+
- MongoDB rodando em `localhost:27017`

### Passo a passo

**1. Clone o repositório**
```bash
git clone https://github.com/msciriaco81/Cineposts-backend.git
cd Cineposts-backend
```

**2. Verifique se o MongoDB está rodando**

No Windows:
```powershell
Get-Service -Name MongoDB
# Se estiver parado:
net start MongoDB
```

No Mac/Linux:
```bash
brew services start mongodb-community
# ou
sudo systemctl start mongod
```

**3. Execute a aplicação**
```bash
# Linux/Mac
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run

# Com Maven instalado globalmente
mvn spring-boot:run
```

**4. Acesse**

| Recurso | URL |
|---|---|
| API Base | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |

### Usuário padrão

Na primeira execução, um usuário administrador é criado automaticamente:

```
Usuário: admin
Senha:   admin123
```

> ⚠️ **Altere a senha padrão imediatamente em produção.**

---

## 🌐 Endpoints da API

### Autenticação
| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| POST | `/auth/login` | Autenticar e receber JWT | Público |

### Admin — Usuários
| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| POST | `/admin/users` | Criar novo usuário | ADMIN |
| GET | `/admin/users` | Listar todos os usuários | ADMIN |
| PUT | `/admin/users/{id}` | Atualizar usuário | ADMIN |
| DELETE | `/admin/users/{id}` | Desativar usuário | ADMIN |

### Admin — Solicitações de Edição
| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/admin/edit-requests` | Listar todas as solicitações | ADMIN |
| POST | `/admin/edit-requests/{id}/approve` | Aprovar solicitação | ADMIN |
| POST | `/admin/edit-requests/{id}/reject` | Rejeitar solicitação | ADMIN |

### Conteúdos
| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| POST | `/contents` | Criar conteúdo | Autenticado |
| GET | `/contents` | Listar conteúdos | Autenticado |
| GET | `/contents/{id}` | Buscar conteúdo por ID | Autenticado |
| PUT | `/contents/{id}` | Atualizar conteúdo | Dono ou ADMIN |
| DELETE | `/contents/{id}` | Deletar conteúdo | Dono ou ADMIN |
| POST | `/contents/{id}/edit-request` | Solicitar edição em conteúdo aprovado | Autenticado |

### Sugestões de Posts
| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| POST | `/contents/{id}/suggestions/twitter` | Gerar sugestão para Twitter | Autenticado |
| POST | `/contents/{id}/suggestions/instagram` | Gerar sugestão para Instagram | Autenticado |
| GET | `/post-suggestions` | Listar sugestões | Autenticado |
| GET | `/post-suggestions/{id}` | Buscar sugestão por ID | Autenticado |
| PUT | `/post-suggestions/{id}` | Editar sugestão | Dono ou ADMIN |

### Feed da Equipe
| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/team/contents` | Feed de conteúdos da equipe | Autenticado |
| GET | `/team/post-suggestions` | Feed de sugestões da equipe | Autenticado |

---

## ⚙️ Configuração

As configurações da aplicação ficam em `src/main/resources/application.properties`:

```properties
# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/cineposts

# JWT
jwt.secret=sua-chave-secreta-aqui
jwt.expiration=86400000   # 24 horas em milissegundos

# Servidor
server.port=8080
```

### Usando MongoDB Atlas (nuvem)

Substitua a URI do MongoDB:
```properties
spring.data.mongodb.uri=mongodb+srv://usuario:senha@cluster.mongodb.net/cineposts
```

---

## 🧩 Gerador de Posts

O MVP utiliza um gerador **baseado em regras** — sem IA. Para cada tipo de conteúdo e plataforma, o `PostGeneratorService` aplica templates específicos.

**Exemplo — Twitter (MOVIE_ANNIVERSARY):**
```
Hook:    🎬 Hoje faz 30 anos de "Pulp Fiction"!
Caption: Uma das obras que redefiniu o cinema moderno...
CTA:     Qual sua cena favorita? 👇
Hashtags: #cinema #quentintarantino
```

**Exemplo — Instagram (MOVIE_ANNIVERSARY):**
```
Hook:    🎬 Hoje faz 30 anos de "Pulp Fiction" — um marco do cinema!
Caption: Uma das obras que redefiniu o cinema moderno...
         Um dos títulos que marcaram a história do entretenimento.
CTA:     💬 Qual momento desse título mais te marcou? Conta nos comentários!
Hashtags: #cinema #filmes #entretenimento #cultura #classicodocinema
          #aniversariodofilme #setearte #pulpfiction
```

> 💡 O `PostGeneratorService` foi isolado intencionalmente para facilitar a substituição futura por um gerador com IA (ex: Claude API, OpenAI).

---

## 🔄 Fluxo de EditRequest

Quando um conteúdo está com status `APPROVED`, ele não pode ser editado diretamente por usuários comuns. O fluxo é:

```
1. Usuário envia POST /contents/{id}/edit-request
   com os campos que deseja alterar em "proposedChanges"

2. Sistema salva um snapshot completo do conteúdo atual

3. Admin visualiza em GET /admin/edit-requests
   com o diff entre originalSnapshot e proposedChanges

4a. POST /admin/edit-requests/{id}/approve
    → aplica as mudanças no conteúdo original

4b. POST /admin/edit-requests/{id}/reject
    → rejeita sem alterar o conteúdo
```

---

## 📝 Licença

Uso interno. Todos os direitos reservados.