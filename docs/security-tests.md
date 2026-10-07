# Security Test Suite - HardwareZone

**Progetto:** HardwareZone - E-commerce di componenti hardware  
**Corso:** Software Engineering for Secure Cloud Systems  
**Data:** 07/10/2026

---

## Indice

1. [Panoramica](#1-panoramica)
2. [Perché due categorie di test?](#2-perché-due-categorie-di-test)
3. [Organizzazione della documentazione](#3-organizzazione-della-documentazione)
4. [Metriche complessive](#4-metriche-complessive)
5. [Copertura per area OWASP](#5-copertura-per-area-owasp)
6. [Finding documentati](#6-finding-documentati)
7. [Link ai documenti di dettaglio](#7-link-ai-documenti-di-dettaglio)

---

## 1. Panoramica

La suite di test di sicurezza di **HardwareZone** è composta da **~450 test** distribuiti su **2 categorie complementari** e **5 aree OWASP** (come da OWASP Testing Guide v4.2).

| Categoria | Approccio | Contributo Coverage |
|:---|:---|:-------------------:|
| **Audit Statico** (SAST manuale) | Legge i file `.java` come testo, cerca pattern di sicurezza |         0%          |
| **Test Funzionali** | Esegue il codice con Mockito + H2, verifica comportamento runtime |    Contribuisce     |

Le due categorie sono **complementari** e non ridondanti:
- L'**audit statico** documenta finding **strutturali** (SQL Injection, NPE, CSRF) in modo ripetibile e prevedibile.
- I **test funzionali** verificano il **comportamento reale** del codice e alimentano la coverage SonarCloud.

---

## 2. Perché due categorie di test?

La scelta di utilizzare **due approcci paralleli** non è casuale, ma risponde a una precisa strategia di sicurezza.

### 2.1 Limiti dell'audit statico

L'audit statico (SAST manuale) è potente per trovare **pattern noti** ma ha limiti:

-  Non esegue il codice → non rileva bug runtime
-  Può produrre falsi positivi (analisi testuale, non AST)
-  Non contribuisce alla coverage SonarCloud
-  Non verifica il comportamento effettivo del sistema

### 2.2 Limiti dei test funzionali

I test funzionali verificano il comportamento ma non coprono tutto:

-  Non rilevano finding strutturali (es. CSRF su azioni GET)
-  Richiedono setup (Mockito + H2) più complesso
-  Non documentano il "perché" di un finding, solo il comportamento

### 2.3 La strategia ibrida

Combinando i due approcci:

| Obiettivo | Strumento | Vantaggio |
|:---|:---|:---|
| Trovare SQL Injection | Audit statico | Analisi di ogni query |
| Verificare SQL Injection a runtime | Test funzionale | Esegue query con payload |
| Documentare CSRF | Audit statico | Ispeziona pattern `doGet` |
| Alimentare coverage | Test funzionale | JaCoCo conta le righe |
| Prevenire regressioni | Entrambi | Se il codice cambia, i test falliscono |



---

## 3. Organizzazione della documentazione

Per mantenere la documentazione **leggibile e navigabile**, i dettagli sono separati in **3 file**:

```
docs/
├── security-tests.md          ← QUESTO FILE (panoramica + hub)
├── audit-tests.md             ← Dettaglio dei 275 test di audit statico
└── functional-tests.md        ← Dettaglio dei 175 test funzionali
```

### 3.1 Struttura dei package

La separazione si riflette anche nel **codice sorgente**:

```
src/test/java/security/
├── BaseFunctionalTest.java       ← Configurazione H2 comune
├── ServletTestSupport.java       ← Mock di Servlet API
│
├── audit/                         ← 275 test di analisi statica
│   ├── authorization/             ← A01 — 93 test
│   ├── businesslogic/             ← A04 — 38 test
│   ├── dataprotection/            ← A02 — 33 test
│   ├── daointegration/            ← A03/A05 — 18 test
│   └── inputvalidation/           ← A03 — 51 test
│
└── functional/                    ← 175 test funzionali
    ├── authorization/             ← A01/A07
    ├── businesslogic/             ← A04
    ├── dataprotection/            ← A02
    ├── daointegration/            ← A03
    └── inputvalidation/           ← A03
```

**Convenzione di naming:**
- File di **audit**: `*Test.java` (es. `AuthorizationTest.java`)
- File **funzionali**: `*FunctionalTest.java` (es. `ProductsFunctionalTest.java`)

Questa convenzione permette di distinguere i due approcci già dal nome.

---

## 4. Metriche complessive

| Metrica |        Valore        |
|:---|:--------------------:|
| **Test totali** |       **~450**       |
| Test di audit statico |         ~275         |
| Test funzionali |         ~175         |
| Classi di test |         40+          |
| **Coverage SonarCloud** | **87.5%** (New Code) |
| **Success rate** |       **100%**       |
| Durata media esecuzione |      ~5 secondi      |
| Quality Gate SonarCloud |      **Passed**      |

---

## 5. Copertura per area OWASP

| Area OWASP | Test Audit | Test Funzionali | Totale |
|:---|:---:|:---:|:---:|
| **A01** — Broken Access Control | ~93 | ~45 | **~138** |
| **A02** — Cryptographic Failures | ~40 | ~14 | **~54** |
| **A03** — Injection | ~51 | ~11 | **~62** |
| **A04** — Insecure Design | ~76 | ~20 | **~96** |
| **A05** — Security Misconfiguration | ~18 | ~6 | **~24** |
| **A07** — Authentication Failures | ~15 | ~10 | **~25** |
| **GDPR / Business Logic / DAO** | — | ~75 | **~75** |
| **TOTALE** | **~275** | **~175** | **~450** |

---

## 6. Finding documentati

La suite ha identificato **40+ finding**, di cui **12 critici**. La seguente tabella riassume i **finding di sicurezza più rilevanti** emersi a runtime:

| # | Finding | CWE | Severità | Test |
|:--|:---|:---:|:---:|:---|
| 1 | Privilege escalation (`SetStateUser`) | CWE-269 |  Critica | `SetStateUserFunctionalTest` |
| 2 | Self-lockout admin | CWE-269 |  Critica | `SetStateUserFunctionalTest` |
| 3 | CSRF su azioni GET | CWE-352 |  Critica | `AdminUserManagementTest` |
| 4 | SQL Injection in `UserDAO` | CWE-89 |  Critica | `DaoIntegrationTest` |
| 5 | SQL Injection in `ProductDAO` | CWE-89 |  Critica | `DaoIntegrationTest` |
| 6 | Unrestricted File Upload | CWE-434 |  Critica | `AdminFunctionsTest` |
| 7 | Race condition su stock | CWE-362 |  Critica | `PaymentSecurityTest` |
| 8 | Loop infinito `ProductsHomepage` | CWE-835 |  Critica | `ProductViewTest` |
| 9 | PCI-DSS: numero carta in chiaro | CWE-312 |  Critica | `PaymentDAOFunctionalTest` |
| 10 | NPE su `user.isAdmin()` (12+ Servlet) | CWE-476 |  Alta | `AuthorizationTest` |
| 11 | SHA-1 per password | CWE-916 |  Alta | `UserBeanFunctionalTest` |
| 12 | No verifica password attuale | CWE-620 |  Alta | `ProfileAndCartTest` |
| 13 | No session-timeout in `web.xml` | CWE-613 |  Alta | `AuthenticationTest` |
| 14 | Quantità negative `AddToCart` | CWE-20 |  Alta | `AddToCartFunctionalTest` |
| 15 | CVV getter esposto | CWE-200 |  Alta | `PaymentBeanFunctionalTest` |

**Per il dettaglio completo** di ogni finding (con descrizione, test, scenario, patch), consulta i due file di dettaglio nella prossima sezione.

---

## 7. Link ai documenti di dettaglio

Per una trattazione completa dei test, consulta:

###  [Audit Statico — `audit-tests.md`](audit-tests.md)

**Contenuto:**
- 275 test di analisi statica del codice sorgente
- 5 sotto-sezioni OWASP (A01, A02, A03, A04, A05, A07)
- Sezione **"Dettaglio dei Finding"** con struttura `Vulnerabilità → Test → Scenario → Patch` per ogni finding critico
- 22 finding documentati in dettaglio

---

###  [Test funzionali — `functional-tests.md`](functional-tests.md)

**Contenuto:**
- 175 test funzionali che eseguono il codice con Mockito + H2
- 40 classi di test distribuite su 5 sottocartelle OWASP
- Documentazione dettagliata delle **classi base** (`BaseFunctionalTest`, `ServletTestSupport`)
- Sezione **"Dettaglio dei Finding Documentati"** (8 finding a runtime)

---



