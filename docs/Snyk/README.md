# Snyk — Analisi di Sicurezza Completa

Questo documento raccoglie i risultati di due analisi Snyk:

1. **Software Composition Analysis (SCA)** — dipendenze Java (`pom.xml`)
2. **Container Scanning** — base image Docker (`Dockerfile`)

---
## Indice generale

- [** Parte 1 — Snyk — Software Composition Analysis (SCA)**](#parte-1--snyk--software-composition-analysis-sca)
    - [1. Configurazione e Integrazione](#1-configurazione-e-integrazione)
    - [2. Processo di scansione e rilevamento](#2-processo-di-scansione-e-rilevamento)
    - [3. Prioritizzazione](#3-prioritizzazione)
    - [4. Dettaglio delle vulnerabilità](#4-dettaglio-delle-vulnerabilità)
    - [5. Processo di remediation](#5-processo-di-remediation)
        - [5.1 Aggiornamento di `mysql-connector-j`](#51-aggiornamento-di-mysql-connector-j)
        - [5.1.1 Scelta critica della versione target](#511-scelta-critica-della-versione-target)
        - [5.2 Aggiornamento di `json:json`](#52-aggiornamento-di-jsonjson)

- [**Parte 2 — Snyk Container — Analisi e remediation della base image Docker**](#parte-2--snyk-container--analisi-e-remediation-della-base-image-docker)
    - [1. Configurazione e Integrazione](#1-configurazione-e-integrazione-1)
    - [2. Confronto Before / After](#2-confronto-before--after)
        - [### 2.1 Prima scansione — `tomcat:10.1-jdk17`](#21-prima-scansione--tomcat101-jdk17)
        - [2.2 Seconda Scansione — `tomcat:10.1-jre17-temurin-jammy`](#22-seconda-scansione--tomcat101-jre17-temurin-jammy)
        - [2.3 Confronto Diretto](#23-confronto-diretto)
    - [3. Vulnerabilità risolte (15 CVE)](#3-vulnerabilità-risolte-15-cve)
        - [3.1 Perché `binutils` e `perl` sono spariti](#31-perché-binutils-e-perl-sono-spariti)
        - [3.2 Perché le altre 4 CVE sono cambiate](#32-perché-le-altre-4-cve-sono-cambiate)
    - [4. Analisi della CVE High Residua: OpenSSL](#4-analisi-della-cve-high-residua-openssl)
        - [4.1 Dettagli della Vulnerabilità](#41-dettagli-della-vulnerabilità)
        - [4.2 Perché la CVE è rimasta irrisolta](#42-perché-la-cve-è-rimasta-irrisolta)
        - [4.3 Mitigazioni applicate](#43-mitigazioni-applicate)

---

# Parte 1 — Snyk — Software Composition Analysis (SCA)

**Strumento:** Snyk (Software Composition Analysis)
**Tipo di analisi:** Rilevamento di vulnerabilità note (CVE) nelle dipendenze dichiarate nel `pom.xml`.
**Data di integrazione:** 01/10/2026

##  Indice parte 1

- [1. Configurazione e integrazione](#1-configurazione-e-integrazione)
- [2. Processo di scansione e rilevamento](#2-processo-di-scansione-e-rilevamento)
- [3. Prioritizzazione](#3-prioritizzazione)
- [4. Dettaglio delle vulnerabilità](#4-dettaglio-delle-vulnerabilità)
- [5. Processo di remediation](#5-processo-di-remediation)
    - [5.1 Aggiornamento di `mysql-connector-j`](#51-aggiornamento-di-mysql-connector-j)
    - [5.1.1 Scelta critica della versione target](#511-scelta-critica-della-versione-target)
    - [5.2 Aggiornamento di `json:json`](#52-aggiornamento-di-jsonjson)

---

## 1. Configurazione e integrazione

La repository `notjuary/HardwareZone` è stata collegata alla dashboard di Snyk tramite l'integrazione GitHub. Snyk analizza automaticamente il file `pom.xml` ad ogni push, confrontando le versioni delle dipendenze con il database CVE.

- **Integrazione CI/CD:** pianificata tramite GitHub Action `snyk/actions/maven@master`
- **Autenticazione:** GitHub Actions Secret `SNYK_TOKEN` (token personale con permessi di sola lettura)
- **Soglia di severità:** `high`

---

## 2. Processo di scansione e rilevamento

La scansione iniziale del `pom.xml` ha prodotto **4 vulnerabilità distinte** in **2 dipendenze dirette**, tutte di severità **alta** secondo il CVSS di Snyk. Il Priority Score massimo rilevato è stato **696/1000**.

### Vulnerabilità rilevate

| ID | Dipendenza | CWE | CVSS | Priority Score | Tipo |
|:---|:---|:---|:---|:---|:---|
| DEP-01 | `com.mysql:mysql-connector-j@8.0.33` | CWE-284 (Access Control Bypass) | 8.3 | 629 | Diretta |
| DEP-02 | `com.mysql:mysql-connector-j@8.0.33` → `com.google.protobuf:protobuf-java@3.21.9` | CWE-121 (Stack-based Buffer Overflow) | 8.7 | 649 | Transitive |
| DEP-03 | `org.json:json@20220320` | CWE-400 (Denial of Service) | 7.5 | 696 | Diretta |
| DEP-04 | `org.json:json@20220320` | CWE-770 (Allocation of Resources Without Limits) | 7.5 | 696 | Diretta |

**Nota:** Le vulnerabilità DEP-01 e DEP-02 sono legate alla stessa dipendenza (`mysql-connector-j@8.0.33`), ma sono state conteggiate separatamente perché riguardano CWE diverse e richiedono azioni di remediation differenti (aggiornamento della dipendenza diretta vs. della dipendenza transitiva).

---

## 3. Prioritizzazione

Tutte e quattro le vulnerabilità sono state classificate con priorità **P1 - Alta**, per i seguenti motivi:

1. **Severità CVSS ≥ 7.5** su tutte le voci.
2. **Exploit maturity:** alcune CVE hanno exploit pubblici noti.
3. **Impatto diretto sull'applicazione:** `mysql-connector-j` è il driver JDBC usato per connettersi al database di produzione; `org.json` è usato per il parsing di JSON in input/output. Entrambe sono dipendenze critiche.
4. **Fix disponibile:** Snyk ha indicato chiaramente le versioni sicure, quindi la remediation è immediata.


---

## 4. Dettaglio delle vulnerabilità

### DEP-01 / DEP-02 - `com.mysql:mysql-connector-j@8.0.33`

**Descrizione.** Il driver JDBC MySQL Connector/J versione 8.0.33 introduce due vulnerabilità:

- **CWE-284 (Access Control Bypass, CVE-2023-22102, CVSS 8.3)**: un utente con privilegi limitati potrebbe bypassare i controlli di accesso al database, ottenendo privilegi non autorizzati.
- **CWE-121 (Stack-based Buffer Overflow, CVE-2024-7254, CVSS 8.7)**: vulnerabilità transitiva nella libreria `com.google.protobuf:protobuf-java@3.21.9`, che può causare un buffer overflow nello stack, con potenziale esecuzione di codice arbitrario.



### DEP-03 / DEP-04 -`org.json:json@20220320`

**Descrizione.** La libreria `org.json` versione 20220320 introduce due vulnerabilità:

- **CWE-400 (Denial of Service)**: un input JSON malevolo può causare un consumo eccessivo di memoria, portando al crash dell'applicazione.
- **CWE-770 (Allocation of Resources Without Limits or Throttling)**: manca un meccanismo di limitazione delle risorse durante il parsing JSON, permettendo a un attaccante di saturare la memoria del server.

---

## 5. Processo di remediation

### 5.1 Aggiornamento di `mysql-connector-j`

**Azione.** La dipendenza è stata aggiornata da `8.0.33` a `9.3.0` nel `pom.xml`:

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>9.3.0</version>
</dependency>
```
### 5.1.1 Scelta critica della versione target

Snyk raccomandava inizialmente l'aggiornamento a `mysql-connector-j@9.2.0`.
Tuttavia, l'analisi statica di IntelliJ IDEA (powered by Mend.io) ha segnalato
che la versione 9.2.0 era a sua volta vulnerabile alla **CVE-2025-30706**
(CVSS 7.5), una vulnerabilità critica del connettore MySQL che colpisce le
versioni dalla 9.0.0 alla 9.2.0 inclusa.

**Decisione:** si è scelto di aggiornare direttamente a `9.3.0`, la prima
versione che risolve sia la CWE-284 (DEP-01) sia la CVE-2025-30706, e che
aggiorna automaticamente la dipendenza transitiva `protobuf-java` a una
versione non vulnerabile (risolvendo anche la CWE-121 / DEP-02).



### 5.2 Aggiornamento di `json:json`

**Azione.** La dipendenza è stata aggiornata da `20220320` a `20231013` nel `pom.xml`:

```xml
<dependency>
    <groupId>org.json</groupId>
    <artifactId>json</artifactId>
    <version>20231013</version>
</dependency>
```

# Parte 2 — Snyk Container — Analisi e remediation della base image Docker

**Strumento:** Snyk Container  
**Tipo di analisi:** Vulnerabilità note (CVE) nei pacchetti del sistema operativo della base image Docker  
**Data di integrazione:** 01/10/2026

##  Indice parte 2

- [1. Configurazione e integrazione](#1-configurazione-e-integrazione)
- [2. Confronto before / after](#2-confronto-before--after)
   - [2.1 Prima scansione — `tomcat:10.1-jdk17`](#21-prima-scansione--tomcat101-jdk17)
   - [2.2 Seconda scansione — `tomcat:10.1-jre17-temurin-jammy`](#22-seconda-scansione--tomcat101-jre17-temurin-jammy)
   - [2.3 Confronto diretto](#23-confronto-diretto)
- [3. Vulnerabilità risolte (15 CVE)](#3-vulnerabilità-risolte-15-cve)
   - [3.1 Perché binutils e perl sono spariti](#31-perché-binutils-e-perl-sono-spariti)
   - [3.2 Perché le altre 4 CVE sono cambiate](#32-perché-le-altre-4-cve-sono-cambiate)
- [4. Analisi della CVE high residua: OpenSSL](#4-analisi-della-cve-high-residua-openssl)
   - [4.1 Dettagli della vulnerabilità](#41-dettagli-della-vulnerabilità)
   - [4.2 Perché la CVE è rimasta irrisolta](#42-perché-la-cve-è-rimasta-irrisolta)
   - [4.3 Mitigazioni applicate](#43-mitigazioni-applicate)
---

## 1. Configurazione e integrazione

L'immagine Docker di `HardwareZone` è stata collegata a Snyk tramite l'integrazione GitHub. Snyk analizza il `Dockerfile` ad ogni push e valuta la base image e i layer costruiti.

- **Manifest analizzato:** `Dockerfile`
- **Repository Snyk:** `notjuary/HardwareZone`
- **Immagine target:** `HardwareZone`
- **Base image iniziale:** `tomcat:10.1-jdk17` (Ubuntu 24.04)
- **Base image finale:** `tomcat:10.1-jre17-temurin-jammy` (Ubuntu 22.04)

---

## 2. Confronto before / after

Il progetto è stato sottoposto a due scansioni Snyk Container: la prima con la base image `tomcat:10.1-jdk17` e la seconda dopo il passaggio a `tomcat:10.1-jre17-temurin-jammy`.

### 2.1 Prima scansione — `tomcat:10.1-jdk17`

**Totale: 103 issue**

| Severità    | Conteggio |
|:------------| :---: |
| **High**    | **1** |
| **Medium**  | **83** |
| **Low**   | **19** |

### 2.2 Seconda scansione — `tomcat:10.1-jre17-temurin-jammy`

**Totale: 89 issue**

| Severità    | Conteggio |
|:------------| :---: |
| **High**    | **1** |
| **Medium**  | **68** |
| **Low**   | **20** |

### 2.3 Confronto Diretto

| Severità    | Prima (`jdk17`) | Dopo (`jre17-temurin-jammy`) |    Δ     |
|:------------| :---: | :---: |:--------:|
| **High**    | 1 | 1 |  **0**   |
| **Medium**  | 83 | 68 | **−15**  |
| **Low**   | 19 | 20 |  **+1**  |
| **TOTALE**  | **103** | **89** | **−14**  |

**Risultato:** il passaggio da JDK a JRE ha permesso di rimuovere **15 vulnerabilità di severità media** e ha introdotto **1 nuova vulnerabilità di severità bassa** (`libpng1.6/libpng16-16`, CVE-2026-40930), per un saldo netto di **−14 voci**. La vulnerabilità **High è rimasta invariata**, perché appartiene a `openssl`, un pacchetto di sistema presente in entrambe le varianti (JRE e JDK).

---

## 3. Vulnerabilità risolte (15 CVE)

Il passaggio alla variante JRE ha eliminato **15 CVE di severità media**. La seguente tabella elenca in dettaglio le vulnerabilità risolte, raggruppate per pacchetto:

| # | CVE | Pacchetto | Score | Motivo della risoluzione                             |
| :-- | :--- | :--- | :---: |:-----------------------------------------------------|
| 1–7 | CVE-2025-69644, 69645, 69646, 69647, 69648, 69651, 69652 | `binutils` | 300 | Pacchetto non incluso nella JRE (toolchain di build) |
| 8–11 | CVE-2025-15649, 2026-48959, 2026-48962, 2026-7017 | `perl/perl-base` | 514 | Tool di automazione non necessari a runtime          |
| 12 | CVE-2026-13608 | `curl` | 586 | Versione aggiornata nel tag JRE                      |
| 13 | CVE-2026-19499 | `glibc/locales` | 586 | Versione aggiornata nel tag JRE                      |
| 14 | CVE-2026-53612 | `util-linux` | 514 | Sottoinsieme JRE non include questa CVE              |
| 15 | CVE-2026-53614 | `util-linux` | 514 | Sottoinsieme JRE non include questa CVE              |

**Totale:** 7 (binutils) + 4 (perl) + 1 (curl) + 1 (glibc) + 2 (util-linux) = **15 CVE** 

### 3.1 Perché `binutils` e `perl` sono spariti

- **`binutils`**: pacchetto di tool binari (assembler, linker) usato **solo per compilare**. La JRE non compila nulla, quindi non lo include.
- **`perl/perl-base`**: usato da alcuni script di automazione di sistema. La JRE ufficiale di Tomcat non lo richiede.

### 3.2 Perché le altre 4 CVE sono cambiate

Non sono "sparite" come pacchetti (`curl`, `glibc`, `util-linux` esistono ancora nel "dopo"), ma **la loro versione è cambiata** nel passaggio a `jre17-temurin-jammy`. Il tag JRE puntualizza versioni più recenti di questi pacchetti, risolvendo alcune CVE specifiche.

---

## 4. Analisi della CVE High residua: OpenSSL

La vulnerabilità di severità **High** identificata da Snyk è **CVE-2026-84782** nel pacchetto `openssl`.

### 4.1 Dettagli della vulnerabilità

| Campo | Valore                                                                |
| :--- |:----------------------------------------------------------------------|
| **CVE** | CVE-2026-84782                                                        |
| **SNYK ID** | SNYK-UBUNTU2204-OPENSSL-20266990                                      |
| **Pacchetto** | `openssl@3.0.2-0ubuntu1.26`, `openssl/libssl3@3.0.2-0ubuntu1.26`      |
| **Severità** | High (Snyk: High, Ubuntu Security Rating: High, Red Hat: Important) |
| **CVSS v3** | 7.4 (Red Hat), 8.2 (cve.org)                                          |
| **CWE** | CWE-125: Out-of-bounds Read                                           |
| **Exploit maturity** | No known exploit                                                      |
| **Fixed in** | `openssl@3.0.2-0ubuntu1.30` (per Ubuntu 22.04)                        |

**Descrizione tecnica:** la vulnerabilità risiede nella logica di ritrasmissione del protocollo **DTLS** (Datagram Transport Layer Security). Quando un messaggio di handshake viene scritto in modo frammentato e la scrittura viene sospesa (a causa di un buffer di trasporto momentaneamente pieno), il timer di ritrasmissione DTLS può erroneamente riutilizzare lo stesso buffer e la stessa posizione di tracking della scrittura sospesa, senza resettare la posizione all'inizio del messaggio. Ciò può causare una lettura oltre i limiti del buffer (out-of-bounds read), con potenziale **divulgazione di memoria heap** al peer come dati di handshake in chiaro, oppure un **crash del processo** (Denial of Service) se la lettura raggiunge una regione di memoria non mappata.

### 4.2 Perché la CVE è rimasta irrisolta

La vulnerabilità è stata **accettata come rischio residuo** per le seguenti motivazioni tecniche:

1. **Dipendenza dalla base image upstream:** la CVE è ereditata dalla base image `tomcat:10.1-jre17-temurin-jammy`. Snyk stesso conferma che **"The base image tomcat:10.1-jre17-temurin-jammy is up to date"**, ovvero non esiste una versione più recente di questa immagine che risolva la vulnerabilità.

2. **Patch non disponibile nei repository Ubuntu 22.04:** sebbene OpenSSL abbia rilasciato le patch nelle versioni 4.0.3, 3.6.5, 3.5.9 e 3.4.8 per i branch mantenuti, la versione 3.0 (utilizzata in Ubuntu 22.04) **ha raggiunto il suo end-of-life il 7 settembre 2026** e non riceve più aggiornamenti di sicurezza pubblici. Le correzioni per il branch 3.0 (versione 3.0.23) sono disponibili **solo per i clienti con supporto premium** di OpenSSL. Di conseguenza, la versione `openssl@3.0.2-0ubuntu1.30` (indicata come "Fixed in" da Snyk) non è ancora stata rilasciata nei repository pubblici di Ubuntu 22.04.

3. **Impatto limitato al protocollo DTLS:** la vulnerabilità **riguarda esclusivamente il protocollo DTLS su UDP**. Le connessioni TLS standard su TCP, che rappresentano il modello di deployment tipico per la maggior parte delle applicazioni web (incluso Tomcat), **non sono interessate**. L'applicazione `HardwareZone` utilizza Tomcat come servlet container e non fa uso di DTLS, il che riduce drasticamente la probabilità di sfruttamento.

4. **Exploit maturity "No known exploit":** Snyk classifica la vulnerabilità con **"No known exploit"**, indicando che non esiste un exploit pubblico funzionante al momento dell'analisi.

### 4.3 Mitigazioni applicate

Anche se la CVE non è stata risolta direttamente, il rischio associato è stato significativamente mitigato tramite l'applicazione di difese in profondità:

| Mitigazione | Riferimento | Effetto |
| :--- | :--- | :--- |
| **Esecuzione come utente non privilegiato** (`USER tomcat`) | CIS Docker Benchmark 4.1 | Anche in caso di exploit riuscito di una CVE dell'OS, l'attaccante non otterrebbe privilegi di root nel container, limitando drasticamente il potenziale danno. |
| **Multi-stage build** | Best Practice | L'immagine finale non contiene Maven, JDK di build, né tool di sviluppo, riducendo la superficie d'attacco complessiva. |
| **Rimozione delle webapp di default** | Hardening | Eliminazione di vettori d'attacco inutili. |
| **`HEALTHCHECK`** | Best Practice | L'orchestratore può rilevare e riavviare automaticamente il container se compromesso. |
| **`.dockerignore`** | Best Practice | `.env` e altri file sensibili non vengono copiati nell'immagine. |

---
## 5. Vulnerabilità Medium Residue (68 CVE)

### 5.1 Sintesi

Dopo il passaggio alla base image `tomcat:10.1-jre17-temurin-jammy`, sono rimaste **68 vulnerabilità di severità media**, tutte ereditate dalla base image stessa. Queste CVE **non sono state risolte** e sono state **accettate come rischio residuo** con motivazioni tecniche precise.

### 5.2 Distribuzione per pacchetto

| Pacchetto | # CVE | Esempi                                                                                                                                                                                                                                                                                                                                                                         |       Fixabile?        |
| :--- | :---: |:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:----------------------:|
| `expat/libexpat1` | ~24 | CVE-2026-56408, CVE-2026-56411, CVE-2026-56403, CVE-2026-56405, CVE-2026-56406, CVE-2026-56131, CVE-2026-56132, CVE-2026-72522, CVE-2026-45186, CVE-2025-59375, CVE-2026-76957, CVE-2026-76641, CVE-2026-66046, CVE-2026-32776, CVE-2026-32777, CVE-2026-32778, CVE-2026-41080, CVE-2026-56404, CVE-2026-56407, CVE-2026-56409, CVE-2026-56410, CVE-2026-50219, CVE-2026-56412 |  In attesa di upstream |
| `glibc/locales` | ~11 | CVE-2026-19499, CVE-2026-19542, CVE-2026-6368, CVE-2026-6791, CVE-2026-77117, CVE-2026-80489, CVE-2026-89092, CVE-2026-18374                                                                                                                                                                                                                                                   |           No           |
| `perl/perl-base` | ~11 | CVE-2026-13221, CVE-2026-57432, CVE-2025-15649, CVE-2026-12087, CVE-2026-15534, CVE-2026-19487, CVE-2026-48959, CVE-2026-48962, CVE-2026-57433, CVE-2026-7017, CVE-2026-9538                                                                                                                                                                                                   |     Sì (in attesa)     |
| `curl` | ~6 | CVE-2026-13608, CVE-2026-18924, CVE-2026-80230, CVE-2026-82209, CVE-2026-19931, CVE-2026-11856                                                                                                                                                                                                                                                                                 |           No           |
| `util-linux` | ~6 | CVE-2026-13595, CVE-2026-53612, CVE-2026-53613, CVE-2026-53614, CVE-2026-53615, CVE-2026-27456                                                                                                                                                                                                                                                                                 |     Sì (in attesa)     |
| `libssh/libssh-4` | ~6 | CVE-2026-5946, CVE-2026-59843, CVE-2026-59845, CVE-2026-59847, CVE-2026-59848, CVE-2026-59850                                                                                                                                                                                                                                                                                  |     Sì (in attesa)     |
| `openssl` | ~3 | CVE-2026-63072, CVE-2026-63076, CVE-2026-84782 (High)                                                                                                                                                                                                                                                                                                                          |      No (EOL 3.0)      |
| Altri (`pcre2`, `zlib`, `binutils`, `sqlite3`, `systemd`, `p11-kit`, `tar`, `wget`, `diffutils`, `attr`, `libpng`) | ~11 | -                                                                                                                                                                                                                                                                                                                                                                              |         Misto          |

**Nota sulle duplicazioni:** Snyk conta separatamente ogni occorrenza della stessa CVE in pacchetti correlati (es. `glibc/locales` e `glibc/libc`). Il numero reale di CVE uniche è quindi inferiore alle 68 "voci" mostrate.

### 5.3 Motivazioni del "rischio accettato"

La decisione di **non tentare di risolvere manualmente** queste vulnerabilità si basa su solide motivazioni tecniche:

#### 5.3.1 Snyk conferma che non ci sono fix disponibili

Il messaggio **"The base image tomcat:10.1-jre17-temurin-jammy is up to date"** indica che non esiste una versione più recente di questa base image che risolva le CVE. Eventuali tentativi di `apt-get upgrade` nel Dockerfile non porterebbero benefici (come verificato empiricamente) perché:
- Snyk analizza **tutti i layer** dell'immagine, non solo il layer finale.
- Le vulnerabilità dei layer originali rimangono visibili anche dopo un aggiornamento nei layer successivi.

#### 5.3.2 Patch non disponibili nei repository pubblici

Per molte delle CVE (es. `curl`, `glibc`, `openssl`), la versione "Fixed in" indicata da Snyk **non è ancora stata rilasciata nei repository pubblici di Ubuntu 22.04** al momento dell'analisi. Si tratta di una condizione transitoria che dipende dai maintainer upstream (Ubuntu Security Team, Debian, OpenSSL).

#### 5.3.3 Exploit maturity: "No known exploit"

Snyk classifica **tutte le 68 CVE Medium** con la label **"No known exploit"**. Questo indica che:
- Non esiste un exploit pubblico funzionante al momento dell'analisi.
- Il rischio di sfruttamento è **teorico**, non pratico.
- Gli attacchi richiederebbero competenze avanzate e condizioni specifiche non presenti nel deployment.

#### 5.3.4 Superficie d'attacco limitata

La maggior parte dei pacchetti vulnerabili (`binutils`, `perl`, `diffutils`, `attr`, `tar`, `wget`) **non è raggiungibile dall'esterno**:
- Non sono esposti da Tomcat.
- Non sono invocati dall'applicazione web.
- Non fanno parte del flusso applicativo (richieste HTTP, connessione DB, template rendering).

Un attaccante esterno non può sfruttarli senza **già avere accesso al container**.








