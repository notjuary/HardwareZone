<p align="center">
  <a href="https://www.unisa.it">
    <img src="https://www.unisa.it/rescue/img/logo_standard.png" alt="Unisa" width="180">
  </a>
</p>

<h1 align="center">Hardware Zone</h1>
<p align="center"><em>E-commerce di componenti hardware</em></p>

<p align="center">
  <strong>Università degli Studi di Salerno</strong><br>
  Corso di Laurea in Sicurezza Informatica per Tecnologie Cloud<br>
  Software Engineering for Secure Cloud Systems · A.A. 2025/2026
</p>

<p align="center">
  <a href="https://github.com/notjuary/HardwareZone/actions/workflows/ci.yml">
    <img src="https://github.com/notjuary/HardwareZone/actions/workflows/ci.yml/badge.svg?branch=master" alt="CI Pipeline">
  </a>
  <a href="https://hub.docker.com/r/francescobarlotti/hardwarezone">
    <img src="https://img.shields.io/badge/Docker%20Hub-francescobarlotti%2Fhardwarezone-2496ED?logo=docker&logoColor=white" alt="Docker Hub">
  </a>
  <a href="https://sonarcloud.io/project/overview?id=notjuary_HardwareZone">
    <img src="https://img.shields.io/badge/SonarCloud-Quality%20Gate%20Passed-brightgreen?logo=sonarcloud&logoColor=white" alt="SonarCloud">
  </a>
  <a href="https://app.snyk.io">
    <img src="https://img.shields.io/badge/Snyk-0%20High%20Vulnerabilities-4C4A73?logo=snyk&logoColor=white" alt="Snyk">
  </a>
  <a href="https://dashboard.gitguardian.com">
    <img src="https://img.shields.io/badge/GitGuardian-0%20Secrets%20Leaked-FF6B6B?logo=gitguardian&logoColor=white" alt="GitGuardian">
  </a>
</p>

| Risorsa | Link |
|:---|:---|
| **Docker Hub** | [hub.docker.com/r/francescobarlotti/hardwarezone](https://hub.docker.com/r/francescobarlotti/hardwarezone) |
| **SonarCloud** | [https://sonarcloud.io/project/overview?id=notjuary_HardwareZone](https://sonarcloud.io/project/overview?id=notjuary_HardwareZone) |

| Studente | Matricola |
|:---|:---|
| Francesco Alfonso Barlotti | *05222700022* |

---

## Indice

1. [Descrizione dell'applicazione, stack tecnologico e system design](#1-descrizione-dellapplicazione-stack-tecnologico-e-system-design)
    - [1.1 Descrizione dell'applicazione](#11-descrizione-dellapplicazione)
    - [1.2 Stack tecnologico](#12-stack-tecnologico)
    - [1.3 System Design — Class Diagram](#13-system-design--class-diagram)
2. [Architettura e ruoli](#2-architettura-e-ruoli)
    - [2.1 Architettura](#21-architettura)
    - [2.2 Ruoli](#22-ruoli)
    - [2.3 Matrice di Accesso RBAC](#23-matrice-di-accesso-rbac)
3. [API Reference](#3-api-reference)
4. [Test e Qualità del Codice](#4-test-e-qualità-del-codice)
    - [4.1 Test di Audit Statistico](#41-test-di-audit-statistico)
    - [4.2 Test Funzionali](#42-test-funzionali)
    - [4.3 Report SonarCloud](#43-report-sonarcloud)
    - [4.4 Report Snyk](#44-report-snyk)
    - [4.5 Report GitGuardian](#45-report-gitguardian)
5. [Deployment](#5-deployment)
    - [5.1 Docker](#51-docker)
    - [5.2 Docker Compose](#52-docker-compose)
    - [5.3 Configurazione](#53-configurazione)
6. [CI/CD Pipeline](#6-cicd-pipeline)


---

## 1. Descrizione dell'applicazione, stack tecnologico e system design

### 1.1 Descrizione dell'applicazione

**HardwareZone** è un'applicazione web di e-commerce per la vendita di componenti hardware (CPU, GPU, schede madri, SSD, RAM, ecc.). L'applicazione implementa le funzionalità tipiche di un negozio online:

- **Catalogo prodotti** con filtri per prezzo e categoria
- **Ricerca testuale** case-insensitive su nome e descrizione
- **Carrello** persistente (sessione + database)
- **Autenticazione e registrazione** utenti
- **Gestione profilo** utente
- **Checkout e pagamento** (simulato)
- **Area amministratore** per la gestione di prodotti, utenti e ordini

L'applicazione è stata progettata seguendo i principi **DevSecOps**, integrando tool di analisi statica (SonarCloud, Snyk, GitGuardian) direttamente nella pipeline CI/CD.

### 1.2 Stack Tecnologico

| Livello | Tecnologia | Versione                 |
|:---|:---|:-------------------------|
| **Linguaggio** | Java | 21 (build target: 8)     |
| **Web** | Jakarta Servlet | 5.0.0                    |
| **View** | JSP + JSTL | 3.0                      |
| **Database** | MySQL | 8                        |
| **Connection Pool** | Tomcat JDBC Pool | 10.0.20                  |
| **Build** | Maven | 3.9+                     |
| **Testing** | JUnit 5, Mockito, AssertJ | 5.10.2 / 4.11.0 / 3.27.7 |
| **DB di test** | H2 (in-memory, modalità MySQL) | 2.2.224                  |
| **Coverage** | JaCoCo | 0.8.14                   |
| **Container** | Docker, Docker Compose | 24+                      |
| **CI/CD** | GitHub Actions | -                        |
| **SAST** | SonarCloud, Snyk Code, GitGuardian | -                        |

### 1.3 System Design — Class Diagram

Il progetto segue il pattern **MVC (Model-View-Controller)**:

- **Model**: `UserBean`, `ProductBean`, `CartBean`, `OrderBean`, `PaymentBean`, `CategoryBean`, e i rispettivi DAO (`UserDAO`, `ProductDAO`, `CartDAO`, `OrderDAO`, `PaymentDAO`, `CategoryDAO`).
- **View**: pagine JSP sotto `WEB-INF/` (protette da accesso diretto).
- **Controller**: Servlet mappate su URL specifici (es. `/login-servlet`, `/add-to-cart-servlet`, ecc.).

Diagramma delle classi principali (semplificato):

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│  UserBean   │────▶│   UserDAO    │────▶│   ConPool   │
└─────────────┘     └──────────────┘     └─────────────┘
       ▲                                         │
       │                                         ▼
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│  CartBean   │────▶│   CartDAO    │────▶│   MySQL     │
└─────────────┘     └──────────────┘     └─────────────┘
       ▲                                         ▲
       │                                         │
┌─────────────┐     ┌──────────────┐             │
│ProductBean  │────▶│ ProductDAO   │─────────────┘
└─────────────┘     └──────────────┘
```

---

## 2. Architettura e ruoli

### 2.1 Architettura

L'architettura è **a tre livelli**:

1. **Presentation Layer** (JSP + Servlet): gestisce la UI e le richieste HTTP.
2. **Business Logic Layer** (Bean + DAO): incapsula la logica di business e l'accesso ai dati.
3. **Data Layer** (MySQL): persistenza dei dati.

L'applicazione è deployata su **Tomcat 10.1** (Jakarta EE 9+) e containerizzata con Docker.

### 2.2 Ruoli

| Ruolo | Descrizione |
|:---|:---|
| **Anonimo** | Può visualizzare catalogo, ricerca, dettagli prodotto. Non può accedere al carrello né al checkout. |
| **Utente** | Può aggiungere/rimuovere prodotti dal carrello, effettuare ordini, gestire il proprio profilo. |
| **Admin** | Può gestire prodotti (CRUD), utenti (abilitazione/disabilitazione, promozione), visualizzare tutti gli ordini. |

### 2.3 Matrice di Accesso RBAC

| Endpoint / Risorsa | Anonimo | Utente | Admin |
|:---|:---:|:---:|:---:|
| Catalogo, Ricerca, Dettaglio prodotto | ✅ | ✅ | ✅ |
| Aggiungi al carrello, Rimuovi dal carrello | ❌ | ✅ | ✅ |
| Checkout, Pagamento | ❌ | ✅ | ✅ |
| Profilo utente | ❌ | ✅ | ✅ |
| Gestione prodotti (CRUD) | ❌ | ❌ | ✅ |
| Gestione utenti (lista, stato, promozione) | ❌ | ❌ | ✅ |
| Visualizzazione tutti gli ordini | ❌ | ❌ | ✅ |

---

## 3. API Reference

Elenco delle Servlet principali:

| Metodo | Endpoint | Descrizione | Auth |
|:---|:---|:---|:---:|
| GET | `/products-homepage-servlet` | Restituisce 12 prodotti casuali in JSON | Pubblico |
| GET | `/show-catalog-servlet` | Mostra il catalogo completo | Pubblico |
| GET | `/show-product-servlet` | Dettaglio di un prodotto | Pubblico |
| GET | `/search-product-servlet` | Ricerca testuale (JSON) | Pubblico |
| GET | `/filter-product-servlet` | Filtro per prezzo e categoria | Pubblico |
| GET/POST | `/login-servlet` | Login utente | Pubblico |
| GET | `/logout-servlet` | Logout e sync carrello | Utente |
| POST | `/register-servlet` | Registrazione nuovo utente | Pubblico |
| GET | `/add-to-cart-servlet` | Aggiungi prodotto al carrello | Utente |
| GET | `/remove-from-cart-servlet` | Rimuovi prodotto dal carrello | Utente |
| GET | `/show-cart-servlet` | Visualizza carrello (JSON) | Utente |
| GET/POST | `/payment-servlet` | Checkout e pagamento | Utente |
| GET | `/user-profile-servlet` | Profilo utente | Utente |
| GET | `/edit-profile-servlet` | Modifica profilo | Utente |
| GET/POST | `/add-product-servlet` | Aggiungi prodotto | Admin |
| GET/POST | `/edit-product-servlet` | Modifica prodotto | Admin |
| GET | `/users-servlet` | Lista utenti | Admin |
| GET | `/set-admin-servlet` | Promuovi utente ad admin | Admin |
| GET | `/set-state-user-servlet` | Abilita/disabilita utente | Admin |
| GET | `/orders-servlet` | Lista ordini (tutti per admin, propri per utente) | Utente/Admin |

---

## 4. Test e qualità del codice

Il progetto adotta una strategia di test **a più livelli**:

- **Test di audit statico** (SAST manuale): analizzano il codice sorgente come testo per documentare pattern di sicurezza (SQL Injection, NPE, CSRF, privilege escalation).
- **Test funzionali**: eseguono il codice di produzione con Mockito (Servlet API) e H2 (database in-memory), verificando il comportamento a runtime.
- **Tool automatici**: SonarCloud (qualità + coverage), Snyk (SAST + SCA), GitGuardian (secret scanning).

### 4.1 Test di audit statistico

**File di report completo:** [`docs/TestCase/audit-test.md`](docs/audit-tests.md)

I test di audit sono **~275** e coprono:

| Area OWASP                      | Test |
|:--------------------------------|:---:|
| A01 - Broken Access Control     | ~93 |
| A02 - Cryptographic Failures    | ~40 |
| A03 - Injection                 | ~51 |
| A04 - Insecure Design           | ~76 |
| A05 - Security Misconfiguration | ~18 |
| A07 - Authentication Failures   | ~15 |

### 4.2 Test funzionali

**File di report completo:** [`docs/TestCase/functional.md`](docs/functional-tests.md)

I test funzionali sono **~179** e utilizzano:

- **Mockito** per simulare `HttpServletRequest`, `HttpServletResponse`, `HttpSession`, `RequestDispatcher`.
- **H2 in-memory** (modalità MySQL) per testare i DAO contro un database reale.
- **JaCoCo** per la misurazione della coverage.

**Classi di test:** 40, suddivise in:

| Categoria | File | Test |
|:---|:---:|:---:|
| Authorization (Servlet) | 15 | ~45 |
| Business Logic (Bean) | 14 | ~20 |
| DAO Integration | 7 | ~30 |
| Data Protection | 2 | ~14 |
| Input Validation | 2 | ~11 |

**Coverage sul New Code:** **≥ 84%** (sopra la soglia dell'80%).

### 4.3 Report SonarCloud

**File di report completo:** [`docs/SonarCloud/README.md`](docs/SonarCloud/README.md)

SonarCloud è integrato nella pipeline CI/CD e valuta:

- **Security** (vulnerabilità, security hotspot)
- **Reliability** (bug, resource leak)
- **Maintainability** (code smell, debito tecnico)
- **Coverage** (JaCoCo)
- **Duplications**

**Stato finale (New Code):**

| Metrica | Valore | Soglia | Stato |
|:---|:---:|:---:|:---:|
| Quality Gate | **PASSED** | — | ✅ |
| New Issues | **0** | 0 | ✅ |
| Coverage | **≥ 84%** | ≥ 80% | ✅ |
| Duplications | **0%** | ≤ 3% | ✅ |
| Security Rating | **A** | A | ✅ |
| Reliability Rating | **A** | A | ✅ |
| Maintainability Rating | **A** | A | ✅ |

**Overall Code (dopo remediation):**

| Categoria | Prima | Dopo | Δ |
|:---|:---:|:---:|:---:|
| Reliability | 25+ (E) | **0** (A) | **-25+ (E→A)** |
| Maintainability | 15+ (A) | 0 (A) | -15+ |
| Security | 1 (B) | 1 (B) | 0 |

**Blocker risolti:** 18 Blocker S2095 (try-with-resources su `PreparedStatement`) in tutti i DAO.

**Finding residuo:** SHA-1 in `UserBean.java` (documentato come Won't Fix per compatibilità DB).

### 4.4 Report Snyk

**File di report completo:** [`docs/Snyk/README.md`](docs/Snyk/README.md)

Snyk è configurato in 3 job:

1. **Snyk SCA** - analizza le dipendenze Maven (`pom.xml`).
2. **Snyk Code (SAST)** - analisi statica del codice Java/JSP.
3. **Snyk Container** - analizza l'immagine Docker.

**Configurazione:** `--severity-threshold=medium` (i problemi LOW non bloccano la build).

**Finding principali:**

| Finding | Severità |    Stato    |
|:---|:--------:|:-----------:|
| SHA-1 weak hash in `UserBean.java` |   High   | Documentato |
| SQL Injection (fixato con `PreparedStatement`) |   High   |    FIXED    |
| Hardcoded passwords nei test |   Low    | Documentato |
| CVV getter esposto |   Low    | Documentato |
| SELECT * (fixato) |    -     |    FIXED    |

### 4.5 Report GitGuardian

**File di report completo:** [`docs/GitGuardian/README.md`](docs/GitGuardian/README.md)

GitGuardian scansiona i commit per rilevare **secret hardcoded** (API key, token, password).

**Configurazione:** il workflow è impostato per eseguire lo scan su ogni push/PR.

---

## 5. Deployment

### 5.1 Docker

L'immagine Docker è pubblica su DockerHub:

```bash
docker pull francescobarlotti/hardwarezone:latest
```

**Tag disponibili:**
- `latest` - ultima build del branch `master`
- `master` - alias del branch
- `sha-<commit>` - build specifica
- `1.0.0`, `1.0` - release semantiche (quando pushi un tag `v*`)

**Base image:** `tomcat:10.1-jdk21-temurin`  


### 5.2 Docker compose

Il file `docker-compose.yml` orchestra l'applicazione e un database MySQL 8:

```yaml
services:
  app:
    image: francescobarlotti/hardwarezone:latest
    container_name: hardwarezone-app
    restart: unless-stopped
    ports:
      - "8080:8080"
    depends_on:
      db:
        condition: service_healthy
    environment:
      - MYSQL_HOST=db
      - MYSQL_PORT=3306
      - MYSQL_DATABASE=${MYSQL_DATABASE:-ecommerce}
      - MYSQL_USER=${MYSQL_USER:-root}
      - MYSQL_PASSWORD=${MYSQL_PASSWORD:-rootpassword}
    networks:
      - ecommerce-net

  db:
    image: mysql:8
    container_name: hardwarezone-db
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_PASSWORD:-rootpassword}
      MYSQL_DATABASE: ${MYSQL_DATABASE:-ecommerce}
    ports:
      - "3306:3306"
    volumes:
      - db_data:/var/lib/mysql
      - ./database/createDB.sql:/docker-entrypoint-initdb.d/createDB.sql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-p${MYSQL_PASSWORD:-rootpassword}"]
      interval: 5s
      timeout: 5s
      retries: 10
    networks:
      - ecommerce-net

networks:
  ecommerce-net:
    driver: bridge

volumes:
  db_data:
```

**Avvio:**

```bash
docker compose up -d
```

**Verifica:**

```bash
docker compose ps
```

L'applicazione sarà disponibile su `http://localhost:8080/`.

**Reset completo (cancella anche i dati):**

```bash
docker compose down -v
docker compose up -d
```

### 5.3 Configurazione

Le credenziali del database possono essere sovrascritte tramite variabili d'ambiente o file `.env`:

```env
MYSQL_DATABASE=ecommerce
MYSQL_USER=root
MYSQL_PASSWORD=tuapassword
```

Il file `database/createDB.sql` viene eseguito automaticamente alla prima inizializzazione del container MySQL (grazie al mount in `/docker-entrypoint-initdb.d/`).

---

## 6. CI/CD Pipeline

La pipeline è orchestrata da **GitHub Actions** e si compone di 5 job:

```
Push / PR
    │
    ▼
┌─────────────────────────────────────────────────────┐
│                   CI Pipeline                       │
│  ┌─────────┐  ┌─────────┐  ┌───────────────┐        │
│  │  Test   │  │  Snyk   │  │  GitGuardian  │        │
│  │ (Maven) │  │ (SAST)  │  │  (Secret      │        │
│  │         │  │         │  │   Scanning)   │        │
│  └────┬────┘  └─────────┘  └───────────────┘        │
│       │                                             │
│       ▼                                             │
│  ┌─────────────┐                                    │
│  │ SonarCloud  │                                    │
│  │ (Quality)   │                                    │
│  └──────┬──────┘                                    │
│         │                                           │
│         ▼                                           │
│  ┌─────────────────────┐                            │
│  │ Docker Build & Push │                            │
│  └─────────────────────┘                            │
└─────────────────────────────────────────────────────┘
```

**Dettaglio job:**

| Job | Workflow | Dipende da           | Descrizione |
|:---|:---|:---------------------|:---|
| **Test** | `test.yml` | -                    | Esegue `mvn -B clean verify` (481 test) |
| **Snyk** | `snyk.yml` | -                    | SCA, SAST e Container scan |
| **GitGuardian** | `gitguardian.yml` | -                    | Secret scanning |
| **SonarCloud** | `sonar.yml` | `test`               | Analisi qualità + coverage |
| **Docker** | `docker-publish.yml` | `test`, `sonarcloud` | Build e push su DockerHub |

**Trigger:**

- Push su `main`/`master`
- Push di tag `v*` (release)
- Pull request verso `main`/`master`

**Secrets richiesti:**

| Secret | Usato da |
|:---|:---|
| `SONAR_TOKEN` | SonarCloud |
| `SNYK_API_KEY` | Snyk |
| `GITGUARDIAN_API_KEY` | GitGuardian |
| `DOCKERHUB_USERNAME` | Docker |
| `DOCKERHUB_TOKEN` | Docker |

---


*Ultimo aggiornamento: 10/10/2026*