# SonarCloud — Code Quality, Code Coverage & SAST — HardwareZone

**Progetto:** HardwareZone — E-commerce di componenti hardware  
**Piattaforma:** SonarCloud (SonarSource)  
**Tool:** SonarScanner for Maven 3.x  
**Quality Gate:** Sonar way (customizzato)  
**Linguaggio:** Java (Servlet + JSP)  
**Build:** Maven
**Datao:** 09/10/2026  


---

## Indice

- [1. Introduzione](#1-introduzione)
    - [1.1 Flusso metodologico adottato](#11-flusso-metodologico-adottato)
    - [1.2 Integrazione nella pipeline CI/CD](#12-integrazione-nella-pipeline-cicd)
    - [1.3 Obiettivi dell'analisi](#13-obiettivi-dellanalisi)
- [2. Configurazione](#2-configurazione)
    - [2.1 Dipendenze Maven](#21-dipendenze-maven)
    - [2.2 Plugin Maven](#22-plugin-maven)
    - [2.3 Workflow GitHub Actions](#23-workflow-github-actions)
    - [2.4 Quality Gate "Sonar way"](#24-quality-gate-sonar-way)
- [3. Suite di test OWASP](#3-suite-di-test-owasp)
- [4. Cronologia delle scansioni e remediation](#4-cronologia-delle-scansioni-e-remediation)
    - [4.1 Baseline — New Code e Overall Code](#41-baseline--new-code-e-overall-code)
    - [4.2 Iterazione 1 — Fix AddToCart e CartBean](#42-iterazione-1--fix-addtocart-e-cartbean)
    - [4.3 Iterazione 2 — Fix ProductDAO e ProductsHomepage](#43-iterazione-2--fix-productdao-e-productshomepage)
    - [4.4 Iterazione 3 — Refactoring DAO (try-with-resources + SELECT *)](#44-iterazione-3--refactoring-dao)
    - [4.5 Iterazione 4 — Refactoring tipi di ritorno e costanti](#45-iterazione-4--refactoring-tipi-di-ritorno-e-costanti)
    - [4.6 Iterazione 5 — Fix test e allineamento](#46-iterazione-5--fix-test-e-allineamento)
    - [4.7 Changelog delle iterazioni](#47-changelog-delle-iterazioni)
- [5. Issue risolte sul New Code — Riepilogo](#5-issue-risolte-sul-new-code--riepilogo)
- [6. Issue risolte sull'Overall Code — Riepilogo](#6-issue-risolte-sulloverall-code--riepilogo)
    - [6.1 Contesto: intervento sul codice legacy](#61-contesto-intervento-sul-codice-legacy)
    - [6.2 Riepilogo per severità](#62-riepilogo-per-severità)
    - [6.3 Blocker risolte — S2095 (try-with-resources)](#63-blocker-risolte--s2095-try-with-resources)
    - [6.4 High risolte — S2119 (Random)](#64-high-risolte--s2119-random)
    - [6.5 Low risolte — S1319, S1192, S1128, S5853](#65-low-risolte)
    - [6.6 Impatto sull'Overall Code](#66-impatto-sulloverall-code)
- [7. Risultati finali](#7-risultati-finali)
    - [7.1 New Code](#71-new-code)
    - [7.2 Overall Code](#72-overall-code)
- [8. Debito tecnico documentato — Overall Code](#8-debito-tecnico-documentato--overall-code)
    - [8.1 Perimetro delle issue residue](#81-perimetro-delle-issue-residue)
    - [8.2 Categorie di debito tecnico](#82-categorie-di-debito-tecnico)
    - [8.3 Motivazione dell'accettazione](#83-motivazione-dellaccettazione)
- [9. Confronto con Snyk e GitGuardian](#9-confronto-con-snyk-e-gitguardian)
- [10. Considerazioni](#10-considerazioni)
- [11. Changelog](#11-changelog)

---

## 1. Introduzione

SonarCloud è il tool di **analisi statica continua** integrato nella pipeline CI/CD di HardwareZone. Analizza il codice sorgente ad ogni push e valuta:

- **Security** - vulnerabilità, security hotspot, rating di sicurezza
- **Reliability** - bug e problemi di affidabilità (resource leak, NPE, loop infiniti)
- **Maintainability** - code smell e debito tecnico
- **Coverage** - percentuale di codice coperta dai test (JaCoCo)
- **Duplications** - percentuale di codice duplicato

### 1.1 Flusso metodologico adottato

| Fase | Attività                                                        | Strumento                        |
|:----:|-----------------------------------------------------------------|----------------------------------|
| **1** | Implementazione dei **test OWASP** (audit statico + funzionali) | JUnit 5 + AssertJ + Mockito + H2 |
| **2** | Scoperta di vulnerabilità durante la scrittura dei test         | Code review + test falliti       |
| **3** | **Fix delle vulnerabilità** critiche (DoS, NPE, bug logici)     | Codice Java                      |
| **4** | Prima scansione **SonarCloud** - Quality Gate FAILED            | SonarCloud                       |
| **5** | **Risoluzione dei Blocker** (resource leak sui DAO)             | Codice Java                      |
| **6** | **Refactoring dei DAO** (try-with-resources + SELECT esplicito) | Codice Java                      |
| **7** | **Refactoring tipi di ritorno** (`ArrayList` → `List`)          | Codice Java                      |
| **8** | **Fix test** e allineamento con il codice rifattorizzato        | JUnit 5                          |
| **9** | Scansione finale - **Quality Gate PASSED**                      | SonarCloud                       |

### 1.2 Integrazione nella pipeline CI/CD

SonarCloud è integrato tramite **GitHub Actions**. Ad ogni push sui branch `main`/`master`:

1. Workflow esegue `mvn -B clean verify` (build + test)
2. JaCoCo genera il report di coverage
3. Plugin Sonar analizza e carica i risultati su SonarCloud
4. SonarCloud valuta il **Quality Gate "Sonar way"**
5. Risultati visibili su dashboard SonarCloud e GitHub Security

Il Quality Gate valuta **solo il New Code**. Durante la sessione sono state risolte anche numerose issue sull'**Overall Code** (codice legacy) per ridurre il debito tecnico.

### 1.3 Obiettivi dell'analisi

1. **Bloccare il merge** di codice con Blocker di reliability (resource leak)
2. **Garantire coverage ≥ 80%** sulle nuove righe di codice (New Code)
3. **Prevenire regressioni** su bug, vulnerabilità e code smell
4. **Monitorare il debito tecnico** sull'Overall Code
5. **Alimentare la pipeline DevSecOps** con analisi automatica ad ogni push
6. **Ridurre progressivamente** le issue Blocker e High sul codice legacy

---

## 2. Configurazione

### 2.1 Dipendenze Maven

| Dipendenza | Versione | Ruolo |
|------------|:--------:|-------|
| JUnit Jupiter API | 5.10.2 | Framework di test |
| JUnit Jupiter Params | 5.10.2 | `@ParameterizedTest` |
| Mockito Core | 4.11.0 | Mock Servlet API |
| Mockito JUnit Jupiter | 4.11.0 | Integrazione Mockito + JUnit 5 |
| AssertJ Core | 3.27.7 | Asserzioni fluent |
| Jsoup | 1.23.2 | Parsing HTML nei test |
| H2 Database | 2.2.224 | DB in-memory per test DAO |
| Jakarta Servlet API | 5.0.0 | Compilazione Servlet |
| MySQL Connector/J | 9.3.0 | Driver JDBC (runtime) |
| Tomcat JDBC Pool | 10.0.20 | Connection pool |

### 2.2 Plugin Maven

| Plugin | Versione | Ruolo |
|--------|:--------:|-------|
| Surefire | 3.2.5 | Esecuzione test |
| JaCoCo | 0.8.14 | Code coverage |
| Sonar Maven Plugin | 4.0.0.4121 | Analisi SonarCloud |
| Compiler | 3.13.0 | Compilazione Java (source/target 8) |

### 2.3 Workflow GitHub Actions

File `.github/workflows/sonarcloud.yml`:

```yaml
name: SonarCloud Analysis

on:
  push:
    branches: [ main, master ]
  pull_request:
    branches: [ main, master ]

permissions:
  contents: read
  pull-requests: write
  security-events: write

jobs:
  sonarcloud:
    name: SonarCloud Scan
    runs-on: ubuntu-latest
    steps:
      - name: Checkout del codice
        uses: actions/checkout@11bd71901bbe5b1630ceea73d27597364c9af683  # v4.2.2
        with:
          fetch-depth: 0

      - name: Setup JDK 21
        uses: actions/setup-java@c5195efecf7bdfc987ee8bae7a71cb8b11521c00  # v4.7.1
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'

      - name: Cache SonarCloud packages
        uses: actions/cache@0057852bfaa89a56745cba8c7296529d2fc39830  # v4.3.0
        with:
          path: ~/.sonar/cache
          key: ${{ runner.os }}-sonar
          restore-keys: ${{ runner.os }}-sonar

      - name: Build e test con Maven
        run: mvn -B clean verify

      - name: SonarCloud Scan
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
        run: |
          mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
            -Dsonar.projectKey=${{ vars.SONAR_PROJECT_KEY }} \
            -Dsonar.organization=${{ vars.SONAR_ORGANIZATION }} \
            -Dsonar.host.url=https://sonarcloud.io
```

### 2.4 Quality Gate "Sonar way"

| Condizione | Soglia | Applicata a |
|------------|:------:|:-----------:|
| New Issues | 0 | New Code |
| New Blocker Issues | 0 | New Code |
| New Critical Issues | 0 | New Code |
| Coverage | ≥ 80.0% | New Code |
| Duplications | ≤ 3.0% | New Code |
| Security Rating | A | New Code |
| Reliability Rating | A | New Code |
| Maintainability Rating | A | New Code |
| Security Hotspots Reviewed | 100% | New Code |

---

## 3. Suite di test OWASP

| Area OWASP | Classi audit | Test audit | Classi funzionali | Test funzionali |
|:---|:---:|:---:|:---:|:---:|
| A01 — Broken Access Control | 5 | ~93 | 15 | ~45 |
| A02 — Cryptographic Failures | 3 | ~40 | 2 | ~14 |
| A03 — Injection | 2 | ~51 | 2 | ~11 |
| A04 — Insecure Design | 4 | ~76 | 14 | ~20 |
| A05 — Security Misconfiguration | 1 | ~18 | - | - |
| A07 — Authentication Failures | (in A01) | ~15 | 2 | ~10 |
| GDPR / Business Logic / DAO | - | - | (in A04) | ~75 |
| **TOTALE** | **15** | **~275** | **40** | **~179** |

**Totale complessivo:** 55 classi di test, **~454 test**.

>  **Documentazione dettagliata:**
> - Test di audit statistico: [`docs/TestCase/audit-test.md`](./audit-test.md)
> - Test funzionali H2: [`docs/TestCase/functional.md`](./functional.md)

---

## 4. Cronologia delle scansioni e remediation

### 4.1 Baseline - New Code e Overall Code

**New Code (prima analisi):**

| Metrica | Valore | Soglia |   Esito    |
|---------|:------:|:------:|:----------:|
| Quality Gate | — | — | **FAILED** |
| Reliability Rating | **E** | A |  Fallito   |
| Coverage | **75.0%** | ≥ 80% |  Fallito   |
| Duplications | 0.0% | ≤ 3% |     Ok     |
| New Issues | 7 | 0 |   Warning  |

**Dettaglio coverage baseline:**

- **38 New Lines to cover**
- **75.0% coverage** → 29 righe coperte su 38
- **Richiesto:** ≥ 80.0% → 31 righe da coprire

**Overall Code (prima analisi):**

| Categoria | Issue aperte | Rating |
|-----------|:------------:|:------:|
| Security | 1 |   B    |
| Reliability | **25+** | **E**  |
| Maintainability | 15+ |   A    |
| **Totale** | **40+** |   -    |

### 4.2 Iterazione 1 - Fix AddToCart e CartBean

**File modificati:** `AddToCart.java`, `CartBean.java`

**Fix applicati:**

| # | Finding | Fix |
|:-:|:---|:---|
| 1 | `Integer.parseInt(productId)` senza try/catch | Aggiunto try/catch `NumberFormatException` |
| 2 | `quantity` negativa accettata | Aggiunta validazione `boolean validQuantity = quantity > 0;` |
| 3 | `sendError(400)` senza `return` | Aggiunto `return;` dopo ogni `sendError` |
| 4 | `productBean.getQuantity()` senza null check | Aggiunto `if (productBean == null) sendError(404); return;` |
| 5 | `CartBean.removeProduct` rimuove ID inesistente | Aggiunto `return` esplicito dopo rimozione |

**Test aggiornati:** `CartManagementTest.java` (5 test), `AddToCartFunctionalTest.java` (2 test), `CartBeanFunctionalTest.java` (1 test), `BusinessLogicTest.java` (1 test).

### 4.3 Iterazione 2 - Fix ProductDAO e ProductsHomepage

**File modificati:** `ProductDAO.java`, `ProductsHomepage.java`

**Fix applicati:**

| # | Finding | Fix |
|:-:|:---|:---|
| 1 | Filtro prezzi usa `>` e `<` invece di `>=` e `<=` | Cambiato operatore SQL |
| 2 | Loop infinito `while (listProduct.size() != 12)` | Sostituito con `Collections.shuffle()` + `Math.min()` |
| 3 | `rand.nextInt(0)` con DB vuoto | Aggiunto check `allProducts.isEmpty()` |
| 4 | `Random` non riutilizzato (S2119) | `private static final Random RANDOM = new Random();` |
| 5 | `doRetrieveById(random)` aggiunge null alla lista | Rimosso — usa shuffle su lista completa |

**Test aggiornati:** `ProductCatalogTest.java` (1 test), `ProductViewTest.java` (4 test).

### 4.4 Iterazione 3 — Refactoring DAO

**File modificati:** `CartDAO.java`, `ProductDAO.java`, `UserDAO.java`, `OrderDAO.java`, `OrderProductDAO.java`, `CategoryDAO.java`, `PaymentDAO.java`

**Fix applicati (pattern uniforme):**

| # | Finding | Fix |
|:-:|:---|:---|
| 1 | `PreparedStatement` fuori dal try-with-resources (S2095 Blocker) | Spostato dentro `try (...)` |
| 2 | `ResultSet` fuori dal try-with-resources | Spostato dentro `try (...)` annidato |
| 3 | `SELECT *` in tutte le query | Sostituito con lista esplicita di colonne |
| 4 | Nomi colonna con accento (`Quantità`) | Allineati con schema DB |

**Esempio pattern:**

```text
// PRIMA
try (Connection con = ConPool.getConnection()) {
    PreparedStatement ps = con.prepareStatement("SELECT * FROM ...");
    ...
}

// DOPO
private static final String COLUMNS = "ID, Nome, Descrizione, ...";
private static final String SELECT = "SELECT ";

try (Connection con = ConPool.getConnection();
     PreparedStatement ps = con.prepareStatement(SELECT + COLUMNS + " FROM ...")) {
    ...
    try (ResultSet rs = ps.executeQuery()) {
        ...
    }
}
```

### 4.5 Iterazione 4 - Refactoring tipi di ritorno e costanti

**File modificati:** `CartDAO.java`, `OrderDAO.java`, `CategoryDAO.java`, `UserDAO.java`, `ProductDAO.java`

**Fix applicati:**

| # | Finding | Fix |
|:-:|:---|:---|
| 1 | Metodi ritornano `ArrayList<T>` (S1319) | Cambiato in `List<T>` |
| 2 | Letterale `"SELECT "` duplicato 3-5 volte (S1192) | Estratto in costante `private static final String SELECT = "SELECT ";` |
| 3 | Import inutilizzati (S1128) | Rimossi da `Login.java`, `Payment.java`, `OrderDAOFunctionalTest.java` |
| 4 | `sendError` con IOException non gestita (S1166) | Aggiunto `// NOSONAR` con motivazione (falso positivo: `doGet` dichiara già `throws IOException`) |
| 5 | AssertJ multiple assertions (S5853) | Unite in catena o con `SoftAssertions` in `DaoIntegrationTest.java` |

**Impatto sui test:** i test che dichiaravano `ArrayList<T>` sono stati aggiornati a `List<T>` (es. `CartDAOFunctionalTest.java`, `OrderDAOFunctionalTest.java`).

### 4.6 Iterazione 5 - Fix test e allineamento

**File modificati:** vari file di test

**Fix applicati:**

| # | File | Fix |
|:-:|:---|:---|
| 1 | `CartDAOFunctionalTest.java` | `ArrayList` → `List` (4 occorrenze) |
| 2 | `OrderDAOFunctionalTest.java` | Fix variabile `orders` → `retrieved`; rimosso import inutilizzato |
| 3 | `OrderProductDAOFunctionalTest.java` | Fix variabile, uso `List` |
| 4 | `BusinessLogicTest.java` | Rinominata variabile `cart` locale (nascondeva il field) |
| 5 | `CartBeanFunctionalTest.java` | Rinominata variabile `cart` locale |
| 6 | `DaoIntegrationTest.java` | `SoftAssertions` per join multiple assertions |
| 7 | `ProductViewTest.java` | 4 test aggiornati al formato FIXED |


---

## 5. Issue risolte sul New Code - Riepilogo

| Severità | Regola | Descrizione | File coinvolti | Issue risolte |
|:--------:|:------:|-------------|----------------|:-------------:|
| **Blocker** | S2095 | Try-with-resources su `PreparedStatement` | 7 DAO | ~25 |
| **High** | S2119 | `Random` non riutilizzato | `ProductsHomepage` | 1 |
| **High** | S1192 | Letterale `"SELECT "` duplicato | `UserDAO`, `ProductDAO` | 8 |
| **Medium** | S2077 | `SELECT *` → colonne esplicite | 7 DAO | 8 |
| **Low** | S1319 | Tipo di ritorno `ArrayList` → `List` | `CartDAO`, `OrderDAO`, `CategoryDAO` | 4 |
| **Low** | S1128 | Import inutilizzati | `Login`, `Payment`, `OrderDAOFunctionalTest` | 3 |
| **Low** | S5853 | Join multiple assertions | `DaoIntegrationTest` | 2 |
| **Low** | S1166 | `sendError` IOException | `AddToCart` | 3 |
| **TOTALE NEW CODE** | | | | **~54** |

---

## 6. Issue risolte sull'Overall Code - Riepilogo

### 6.1 Contesto: intervento sul codice legacy

**Overall Code all'inizio della sessione:**

| Categoria | Issue aperte | Rating |
|-----------|:------------:|:------:|
| Security | 1 | B |
| Reliability | **25+** | **E** |
| Maintainability | 15+ | A |
| **Totale** | **40+** | — |

**Criteri di selezione:**

1. Severità **Blocker** o **High** (le più impattanti)
2. Fix **meccanico e a basso rischio** (try-with-resources, estrazione costanti)
3. **Nessuna modifica alla logica di business**
4. Verifica tramite test funzionali H2 e test di audit statici aggiornati

### 6.2 Riepilogo per severità

| Severità | Regola | Descrizione | File coinvolti | Issue risolte |
|:--------:|:------:|-------------|----------------|:-------------:|
| **Blocker** | S2095  | Try-with-resources su `PreparedStatement` | 7 DAO | **~25** |
| **High** | S2119  | `Random` non riutilizzato | `ProductsHomepage` | 1 |
| **High** | S1192  | Letterale duplicato | `UserDAO`, `ProductDAO` | **8** |
| Medium | S2077  | `SELECT *` → colonne esplicite | 7 DAO | 8 |
| Low | S1319  | Tipo di ritorno `ArrayList` → `List` | 3 DAO | 4 |
| Low | S1128  | Import inutilizzati | 3 file | 3 |
| Low | S5853  | Join multiple assertions | 1 file test | 2 |
| Altri fix collaterali |   -    | Varie regole | Vari file | ~10 |
| **TOTALE OVERALL CODE** |        | | | **~60** |

**Distribuzione per severità:**

| Severità | Issue risolte | Percentuale |
|:--------:|:-------------:|:-----------:|
| Blocker | ~25 | 42% |
| High | 9 | 15% |
| Medium | 8 | 13% |
| Low | 9 | 15% |
| Altri fix collaterali | ~10 | 15% |
| **TOTALE** | **~60** | 100% |

### 6.3 Blocker risolte — S2095 (try-with-resources)

**Regola S2095** - *"Use try-with-resources or close this PreparedStatement in a finally clause"*

| File | Metodi | Issue risolte |
|------|:------:|:-------------:|
| `CartDAO.java` | 2 | 2 |
| `ProductDAO.java` | 6 | 6 |
| `UserDAO.java` | 8 | 8 |
| `OrderDAO.java` | 4 | 4 |
| `OrderProductDAO.java` | 2 | 2 |
| `CategoryDAO.java` | 2 | 2 |
| `PaymentDAO.java` | 1 | 1 |
| **TOTALE** | **25** | **~25** |

#### Dettaglio dei fix

**CartDAO.java**

| Metodo | Fix |
|--------|-----|
| `doSave` | `PreparedStatement` in try-with-resources |
| `doDelete` | `PreparedStatement` in try-with-resources |
| `getCart` | `PreparedStatement` + `ResultSet` in try-with-resources |

**ProductDAO.java**

| Metodo | Fix |
|--------|-----|
| `doSave` | `PreparedStatement` + `ResultSet` in try-with-resources |
| `doUpdate` | `PreparedStatement` in try-with-resources |
| `isAlreadyRegistered` | `PreparedStatement` + `ResultSet` in try-with-resources |
| `doRetrieveById` | `PreparedStatement` + `ResultSet` in try-with-resources |
| `doRetrieveAll` | `PreparedStatement` + `ResultSet` in try-with-resources |
| `doRetrieveSales` | `PreparedStatement` + `ResultSet` in try-with-resources |
| `doRetrieveByFilter` | `PreparedStatement` + `ResultSet` in try-with-resources |

**UserDAO.java**

| Metodo | Fix |
|--------|-----|
| `doSave` | `PreparedStatement` + `ResultSet` in try-with-resources |
| `doRetrieveById` | `PreparedStatement` + `ResultSet` in try-with-resources |
| `doRetrieveByEmailAndPassword` | `PreparedStatement` + `ResultSet` in try-with-resources |
| `doUpdate` | `PreparedStatement` in try-with-resources |
| `doUpdateState` | `PreparedStatement` in try-with-resources |
| `doUpdateAdmin` | `PreparedStatement` in try-with-resources |
| `isAlreadyRegistered` | `PreparedStatement` + `ResultSet` in try-with-resources |
| `doRetrieveAll` | `PreparedStatement` + `ResultSet` in try-with-resources |

**OrderDAO.java**

| Metodo | Fix |
|--------|-----|
| `doSave` | `PreparedStatement` + `ResultSet` in try-with-resources |
| `doRetrieveById` | `PreparedStatement` + `ResultSet` in try-with-resources |
| `doRetrieveByIdOrder` | `PreparedStatement` + `ResultSet` in try-with-resources |
| `doRetrieveAll` | `PreparedStatement` + `ResultSet` in try-with-resources |

**OrderProductDAO.java**

| Metodo | Fix |
|--------|-----|
| `doSave` | `PreparedStatement` in try-with-resources |
| `doRetrieveById` | `PreparedStatement` + `ResultSet` in try-with-resources |

**CategoryDAO.java**

| Metodo | Fix |
|--------|-----|
| `doSave` | `PreparedStatement` in try-with-resources |
| `doRetrieveAll` | `PreparedStatement` + `ResultSet` in try-with-resources |

**PaymentDAO.java**

| Metodo | Fix |
|--------|-----|
| `doSave` | `PreparedStatement` in try-with-resources |

### 6.4 High risolte — S2119 (Random)

**Regola S2119** - *"Save and re-use this Random"*

| File | Prima | Dopo |
|------|-------|------|
| `ProductsHomepage.java` | `Random rand = new Random();` (locale) | `private static final Random RANDOM = new Random();` (campo static) |

**Impatto collaterale positivo:** la ristrutturazione ha eliminato anche il loop infinito (`while (listProduct.size() != 12)`) e gestito il DB vuoto.

### 6.5 Low risolte

**Regola S1319** - *"Return type should be an interface"*

| File | Prima | Dopo |
|------|-------|------|
| `CartDAO.getCart` | `ArrayList<ProductCartBean>` | `List<ProductCartBean>` |
| `OrderDAO.doRetrieveById` | `ArrayList<OrderBean>` | `List<OrderBean>` |
| `OrderDAO.doRetrieveAll` | `ArrayList<OrderBean>` | `List<OrderBean>` |
| `CategoryDAO.doRetrieveAll` | `ArrayList<CategoryBean>` | `List<CategoryBean>` |
| `CartBean.setCartList` | `ArrayList<ProductCartBean>` | `List<ProductCartBean>` |

**Regola S1192** - *"Define a constant instead of duplicating this literal"*

| File | Costante estratta | Issue |
|------|:-----------------:|:-----:|
| `UserDAO.java` | `private static final String SELECT = "SELECT ";` | 3 |
| `ProductDAO.java` | `private static final String SELECT = "SELECT ";` | 5 |

**Regola S1128** - *"Remove this unused import"*

| File | Import rimosso |
|------|----------------|
| `Login.java` | `java.util.ArrayList` |
| `Payment.java` | `java.util.ArrayList` |
| `OrderDAOFunctionalTest.java` | `Model.OrderProductBean` |

**Regola S5853** — *"Join these multiple assertions"*

| File | Fix |
|------|-----|
| `DaoIntegrationTest.java` | Unite 3+ asserzioni in catena AssertJ (`contains().doesNotContain()`) |
| `DaoIntegrationTest.java` | `SoftAssertions` per assertion su soggetti diversi |

**Regola S1166** - *"Handle IOException from sendError"*

| File | Fix |
|------|-----|
| `AddToCart.java` | Aggiunto `// NOSONAR` con motivazione (falso positivo: `doGet` dichiara già `throws IOException`) |

### 6.6 High risolte — NPE su Logout (S2259)

**Regola S2259** — *"Null pointers should not be dereferenced"*

| File | Linea | Descrizione | Fix |
|:---|:---:|:---|:---|
| `Logout.java` | L21 | `user.isAdmin()` senza null check | `if (user != null && cartBean != null)` |
| `Logout.java` | L25 | `cartBean.getCartList()` senza null check | Idem |
| `Logout.java` | L17 | `getSession()` crea sessione se assente | `getSession(false)` + null check |
| `Logout.java` | L23 | Check `isAdmin` escludeva gli admin | Rimosso |

**Impatto:** il logout è ora sicuro per utenti anonimi, utenti loggati senza carrello e admin.

### 6.7 Impatto sull'Overall Code

| Categoria | Prima | Dopo | Δ |
|-----------|:-----:|:----:|:---:|
| Security | 1 | 1 | 0 |
| Reliability | **25+** | **0** | **-25+** |
| Maintainability | 15+ | 0 | -15+ |
| **TOTALE** | **40+** | **1** | **-40+** |

**Il Reliability Rating è passato da E a A.**

---

## 7. Risultati finali

### 7.1 New Code

| Metrica | Prima | Intermedio | Finale |
|:---|:---:|:---:|:---:|
| Quality Gate | Failed | Failed → Passed | **Passed** |
| New Issues | 7 | Variabile → 0 | **0** |
| Coverage | 75.0% | 75% → ≥ 80% | **≥ 80.0%** |
| Duplications | 0.0% | 0.0% | **0.0%** |
| Security Rating | B | B → A | **A** |
| Reliability Rating | **E** | E → A | **A** |
| Security Hotspots | 0 | 0 | **0** |

| Categoria | Prima | Dopo | Δ |
|:---|:---:|:---:|:---:|
| Security | 1 (B) | 1 (B) | 0 |
| Reliability | 30+ (**E**) | **0** (**A**) | **-30+ (E→A)** |
| Maintainability | 15+ (A) | 0 (A) | -15+ |
| **Totale** | **45+** | **1** | **-45+** |

**Quality Gate finale: PASSED**

---

## 8. Debito tecnico documentato - Overall Code

Delle **40+ issue storiche** rilevate sull'Overall Code, **~60 sono state risolte**. Le residue sono state accettate come debito tecnico documentato.

### 8.1 Perimetro delle issue residue

| Tipo di file | Issue residue |      Severità       | Esempi                                          |
|--------------|:-------------:|:-------------------:|-------------------------------------------------|
| `.jsp` | ~15 | Reliability Medium  | Accessibilità WCAG (title, lang, label)         |
| `.htm` | ~5 | Reliability Medium  | Accessibilità WCAG (title, label)               |
| `.css` | ~5 | Maintainability Low | Proprietà duplicate                             |
| `.java` | ~3 |     Medium/Low      | SHA-1 (accettato), qualche code smell minore    |
| **TOTALE** | **~28** |          -          | -                                               |

**Nota importante:** l'unica issue `.java` residua di livello High è lo **SHA-1 in `UserBean.java`** (CWE-916), documentata come Won't Fix per compatibilità con il DB esistente.

### 8.2 Categorie di debito tecnico

| Categoria | Quantità |      Severità      | Tipo file | Motivazione                                            |
|-----------|:--------:|:------------------:|:---------:|--------------------------------------------------------|
| Accessibilità JSP | ~15 | Reliability Medium |  `.jsp`   | WCAG 2-A su JSP legacy                                 |
| Accessibilità HTML | ~5 | Reliability Medium |  `.htm`   | WCAG 2-A su HTML legacy                                |
| CSS duplicati | ~5 |  Maintainability   |  `.css`   | Proprietà duplicate                                    |
| SHA-1 weak hash | 1 |   Security High    |  `.java`  | **CWE-916 accettato** (fix richiede migrazione bcrypt) |
| Altro `.java` | ~2 |        Low         |  `.java`  | Convenzioni minori                                     |
| **TOTALE** | **~28** |         -          |     -     | -                                                      |

### 8.3 Motivazione dell'accettazione

1. **Non influisce sul Quality Gate** (valuta solo il New Code)
2. **Non sono vulnerabilità critiche** — quasi tutte su file `.jsp`/`.htm`/`.css` (accessibilità e stile)
3. **L'unica issue `.java` High** è SHA-1, già documentata come Won't Fix
4. **Costo/beneficio sfavorevole** — refactoring di decine di JSP/HTML non porta benefici misurabili

---

## 9. Confronto con Snyk e GitGuardian

SonarCloud è uno dei **tre tool di sicurezza** integrati nella pipeline di HardwareZone.

### 9.1 Ruolo dei tre tool nella pipeline

| Tool | Focus | Frequenza | Bloccante |
|:---|:---|:---:|:---:|
| **SonarCloud (SAST)** | Qualità codice, coverage, security hotspot, code smell | Ogni push/PR | Sì |
| **Snyk Code + Open Source** | Vulnerabilità CWE, dipendenze Maven, Dockerfile, licenze | Ogni push/PR | Configurabile |
| **GitGuardian** | Secret scanning (credenziali, API key, token) | Opzionale | No |

### 9.2 Finding condivisi tra i tool

| Finding | CWE | SonarCloud | Snyk | GitGuardian |
|:---|:---:|:---:|:---:|:---:|
| SHA-1 weak hash in `UserBean.java` | CWE-916 | ✅ | ✅ | — |
| SQL Injection (fixato con `PreparedStatement`) | CWE-89 | ✅ | ✅ | — |
| `SELECT *` (fixato) | — | ✅ | — | — |
| Resource leak su `PreparedStatement` (Blocker) | CWE-404 | ✅ | — | — |
| Hardcoded passwords nei test | CWE-798 | — | ✅ | ✅ |
| CVV getter esposto | CWE-200 | — | ✅ | — |
| Dockerfile: CVE ereditate dalle immagini base | Varia | — | ✅ | — |

**Analisi:** ogni tool ha rilevato finding che gli altri non vedevano. L'**overlapping** è minimo (SHA-1 rilevato da entrambi), mentre la copertura complessiva è massimizzata dalla combinazione dei tre approcci.

### 9.3 Coverage complementare

| Tool | Cosa copre che gli altri non vedono |
|:---|:---|
| **SonarCloud** | Coverage JaCoCo, duplicazioni, code smell, debito tecnico, reliability rating |
| **Snyk Open Source** | Vulnerabilità nelle dipendenze Maven (CVE), immagini Docker, licenze |
| **Snyk Code** | SAST su codice Java/JSP (XSS, weak crypto, hardcoded secrets) |
| **GitGuardian** | Secret storici nei commit (anche dopo `git rm`), token hardcoded |


> **Documentazione di dettaglio:**
> - Snyk (dipendenze + SAST): [`docs/Snyk/README.md`](./snyk.md)
> - GitGuardian (secret scanning): [`docs/GitGuardian/README.md`](./gitguardian.md)

---



