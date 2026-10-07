# Test di Audit Statistico di Sicurezza — HardwareZone

**Progetto:** HardwareZone — E-commerce di componenti hardware  
**Categoria:** Test di audit statistico (SAST manuale)  
**Framework:** JUnit 5 + AssertJ (analisi sorgenti)  
**Data:** 06/10/2026

---

## Indice

1. [Panoramica](#1-panoramica)
2. [Metodologia](#2-metodologia)
3. [Organizzazione dei Test](#3-organizzazione-dei-test)
4. [A01:2021 — Broken Access Control](#4-a012021--broken-access-control)
5. [A02:2021 — Cryptographic Failures](#5-a022021--cryptographic-failures)
6. [A03:2021 — Injection](#6-a032021--injection)
7. [A04:2021 — Insecure Design](#7-a042021--insecure-design)
8. [A05:2021 — Security Misconfiguration](#8-a052021--security-misconfiguration)
9. [A07:2021 — Authentication Failures](#9-a072021--authentication-failures)
10. [Riepilogo](#10-riepilogo)

---

## 1. Panoramica

I **test di audit statistico** sono test che **leggono il codice sorgente come testo** e verificano pattern di sicurezza tramite `source.contains(...)` e regex. **Non eseguono il codice**, ma **documentano finding strutturali** in modo ripetibile.

### Caratteristiche

| Aspetto | Valore |
|:---|:---:|
| **Test totali** | ~275 |
| **Classi di test** | 15+ |
| **Tecnologia** | JUnit 5, AssertJ, `Files.readAllBytes` |
| **Ruolo** | **SAST manuale** — audit del codice sorgente |

### Obiettivi

1. **Documentare finding strutturali**: SQL Injection, NPE, CSRF, privilege escalation
2. **Prevenire regressioni**: se un finding viene risolto, il test fallisce (alert)
3. **Verificare compliance**: OWASP, PCI-DSS, GDPR, CWE
4. **Complementare i tool automatici** (SonarQube, Snyk)

---

## 2. Metodologia

### 2.1 Pattern generale

Ogni test segue questo schema:

```java
@Test
@DisplayName("FINDING: <descrizione del problema>")
void testDocumentXXX() throws Exception {
    // 1. Leggi il file
    String source = readSource("src/main/java/Controller/XXX.java");

    // 2. Verifica la presenza del pattern problematico
    boolean hasIssue =
            source.contains("<pattern 1>") &&
            source.contains("<pattern 2>") &&
            !source.contains("<fix pattern>");

    // 3. Asserzione (con documentazione)
    assertThat(hasIssue)
            .as("FINDING: <descrizione estesa>. " +
                "CWE-XXX. Fix: <soluzione>.")
            .isTrue();
}
```

### 2.2 Utility Comune

java

```
private String readSource(String path) throws Exception {
    return new String(Files.readAllBytes(Paths.get(path)));
}
```

### 2.3 Vantaggi e limiti

**Vantaggi:**

- Documentazione **ripetibile** e **versionata**
- **Alert** se il codice cambia (regression protection)
- **Non richiede** database o mock
- **Tracciabile** nel report

**Limiti:**

- **Non esegue** il codice (0% coverage)
- **Potenziali falsi positivi** (regex su testo, non su AST)
- **Manutenzione** se il codice si evolve
- **Sostituibile** con SonarQube per molti finding

---

## 3. Organizzazione dei test

text

```
src/test/java/security/audit/
├── authorization/                     ← A01 + A07
│   ├── AuthorizationTest.java         
│   ├── AdminFunctionsTest.java        
│   ├── AdminUserManagementTest.java   
│   ├── ProfileAndCartTest.java        
│   └── AuthenticationTest.java        
│   
├── businesslogic/                     ← A04
│   ├── BusinessLogicTest.java         
│   ├── ProductCatalogTest.java        
│   ├── ProductViewTest.java           
│   └── CartManagementTest.java        
│
├── dataprotection/                    ← A02
│   ├── DataProtectionTest.java        
│   ├── ConPoolTest.java               
│   └── PaymentSecurityTest.java      
│
├── daointegration/                    ← A03 + A05
│   └── DaoIntegrationTest.java       
│
└── inputvalidation/                   ← A03
    ├── InputValidationTest.java       
    └── RegistrationFlowTest.java      
```

---

## 4. A01:2021 — Broken Access Control

**Test totali: \~93**

### 4.1 `AuthorizationTest.java` (15 test)

**Test sui ruoli:**

| **#** | **Metodo**                     | **Cosa verifica**                          |
| :---- | :----------------------------- | :----------------------------------------- |
| 1     | `testAdminUserHasCorrectRole`  | Admin ha `isAdmin()==true`                 |
| 2     | `testNormalUserHasCorrectRole` | Utente normale ha `isAdmin()==false`       |
| 3     | `testActiveUserIsActive`       | Utente attivo ha `isActive()==true`        |
| 4     | `testDisabledUserState`        | Utente disabilitato ha `isActive()==false` |

**Finding NPE:**

| **#** | **Metodo**                                | **Finding**                                |
| :---- | :---------------------------------------- | :----------------------------------------- |
| 5     | `testDocumentMissingNullCheckInProducts`  | NPE su `user.isAdmin()` in `Products.java` |
| 6     | `testDocumentMissingNullCheckInOrders`    | NPE in `Orders.java`                       |
| 7     | `testDocumentMissingNullCheckInOrderInfo` | NPE in `OrderInfo.java`                    |
| 8     | `testDocumentOrderInfoLoopBug`            | Bug `else` dentro `for`                    |
| 9     | `testDocumentMissingAuthCheckInAddToCart` | No guard auth in `AddToCart`               |

**Test su ruoli combinati:**

| **#** | **Metodo**                     | **Cosa verifica**                |
| :---- | :----------------------------- | :------------------------------- |
| 10    | `testDisabledAdminLosesAccess` | Admin disabilitato perde accesso |
| 11    | `testActiveAdminHasAccess`     | Admin attivo ha accesso          |
| 12    | `testIsAdminIsCaseInsensitive` | Case-insensitive                 |
| 13    | `testNewUserHasNullAdmin`      | Default `admin=null`             |
| 14    | `testNullAdminIsNotAdmin`      | `null` → non admin               |
| 15    | `testListOfAdminServlets`      | Censimento servlet admin         |

### 4.2 `AdminFunctionsTest.java` (23 test)

**Autorizzazione:**

| **#** | **Metodo**                        | **Cosa verifica**                  |
| :---- | :-------------------------------- | :--------------------------------- |
| 1     | `testAddProductGetRequiresAdmin`  | `AddProduct.doGet` richiede admin  |
| 2     | `testAddProductPostRequiresAdmin` | `AddProduct.doPost` richiede admin |
| 3     | `testEditProductRequiresAdmin`    | `EditProduct` richiede admin       |
| 4     | `testUsersRequiresAdmin`          | `Users` richiede admin             |

**Finding NPE:**

| **#** | **Metodo**                                  | **Finding**               |
| :---- | :------------------------------------------ | :------------------------ |
| 5     | `testDocumentMissingNullCheckInAddProduct`  | NPE in `AddProduct.java`  |
| 6     | `testDocumentMissingNullCheckInEditProduct` | NPE in `EditProduct.java` |
| 7     | `testDocumentMissingNullCheckInUsers`       | NPE in `Users.java`       |

**Validazione input:**

| **#** | **Metodo**                                  | **Cosa verifica**      |
| :---- | :------------------------------------------ | :--------------------- |
| 8     | `testAddProductPriceAcceptsDecimal`         | Regex prezzo           |
| 9     | `testAddProductQuantityAcceptsOnlyIntegers` | Regex quantità         |
| 10    | `testAddProductReplacesCommaInPrice`        | Sostituzione `,` → `.` |
| 11    | `testAddProductRequiresAllSixFieldsValid`   | `level == 6`           |
| 12    | `testAddProductRejectsDuplicates`           | No duplicati           |

**File upload:**

| **#** | **Metodo**                                       | **Finding**        |
| :---- | :----------------------------------------------- |:-------------------|
| 13    | `testDocumentUnrestrictedFileUploadInAddProduct` | **CWE-434**      |
| 14    | `testAddProductHasMultipartConfig`               | `@MultipartConfig` |
| 15    | `testEditProductHasMultipartConfig`              | `@MultipartConfig` |

**Logica:**

| **#** | **Metodo**                                | **Cosa verifica** |
| :---- | :---------------------------------------- | :---------------- |
| 16    | `testEditProductPreservesImageIfNoUpload` | Preserva immagine |
| 17    | `testEditProductRedirectsAfterUpdate`     | Redirect          |
| 18-23 | altri                                     | Vari              |

### 4.3 `AdminUserManagementTest.java` (20 test)

**SetAdmin:**

| **#** | **Metodo**                               | **Cosa verifica**          |
| :---- | :--------------------------------------- |:---------------------------|
| 1     | `testSetAdminRequiresAdmin`              | Richiede admin             |
| 2     | `testSetAdminRedirectsNonAdmin`          | Utente normale → index     |
| 3     | `testSetAdminPreventsAlreadyAdmin`       | No doppia promozione       |
| 4     | `testSetAdminPreventsInactiveUser`       | No promozione disabilitato |
| 5     | `testDocumentMissingNullCheckInSetAdmin` | NPE                        |
| 6     | `testDocumentCsrfInSetAdmin`             | **CSRF su GET**            |
| 7     | `testDocumentUnrestrictedPromotion`      | **Privilege escalation** |

**SetStateUser:**

| **#** | **Metodo**                                      | **Cosa verifica**                      |
| :---- | :---------------------------------------------- |:---------------------------------------|
| 8     | `testSetStateUserRequiresAdmin`                 | Richiede admin                         |
| 9     | `testSetStateUserInvertsState`                  | Inversione stato                       |
| 10    | `testSetStateUserRedirectsAfterOperation`       | Redirect                               |
| 11    | `testDocumentMissingNullCheckInSetStateUser`    | NPE                                    |
| 12    | `testDocumentPrivilegeEscalationInSetStateUser` | **Admin può disabilitare altri admin** |
| 13    | `testDocumentSelfLockoutInSetStateUser`         | **Self-lockout**                       |
| 14    | `testDocumentCsrfInSetStateUser`                | **CSRF su GET**                      |
| 15    | `testDocumentMissingIdValidationInSetStateUser` | NumberFormatException                  |

**UserBean:**

| **#** | **Metodo** | **Cosa verifica**     |
| :---- | :--------- | :-------------------- |
| 16-20 | altri      | Verifiche ruolo/stato |

### 4.4 `ProfileAndCartTest.java` (20 test)

**EditProfile:**

| **#** | **Metodo**                                  | **Cosa verifica**                 |
| :---- | :------------------------------------------ |:----------------------------------|
| 1     | `testEditProfileRequiresAllFields`          | `level == 10`                     |
| 2     | `testEditProfilePreservesAdminRole`         | Preserva ruolo admin              |
| 3     | `testDocumentNoCurrentPasswordVerification` | **No verifica password attuale**  |
| 4     | `testDocumentCsrfInEditProfile`             | **No CSRF token**               |
| 5     | `testDocumentNullUserInEditProfile`         | NPE                               |
| 6     | `testDocumentRegisterOverwriteBug`          | Bug overwrite data registrazione  |

**UserProfile:**

| **#** | **Metodo**                                  | **Cosa verifica**           |
| :---- | :------------------------------------------ | :-------------------------- |
| 7     | `testUserProfileShowsCurrentUser`           | Profilo utente              |
| 8     | `testUserProfileRedirectsAdmin`             | Admin → `profile-admin.jsp` |
| 9     | `testUserProfileRedirectsUser`              | User → `profile-user.jsp`   |
| 10    | `testDocumentMissingNullCheckInUserProfile` | NPE                         |

**UserInfo:**

| **#** | **Metodo**                                    | **Cosa verifica**     |
| :---- | :-------------------------------------------- | :-------------------- |
| 11    | `testUserInfoRequiresAdmin`                   | Richiede admin        |
| 12    | `testDocumentMissingNullCheckInUserInfo`      | NPE                   |
| 13    | `testDocumentNumberFormatExceptionInUserInfo` | NumberFormatException |

**ShowCart:**

| **#** | **Metodo**                               | **Cosa verifica** |
| :---- | :--------------------------------------- | :---------------- |
| 14    | `testShowCartReadsCartFromSession`       | Legge da sessione |
| 15    | `testShowCartReturnsJson`                | JSON output       |
| 16    | `testDocumentMissingNullCheckInShowCart` | NPE               |

**GDPR / Bean:**

| **#** | **Metodo** | **Cosa verifica** |
| :---- | :--------- | :---------------- |
| 17-20 | altri      | Diritti GDPR      |

### 4.5 `AuthenticationTest.java` (15 test)

**Login:**

| **#** | **Metodo**                               | **Cosa verifica**         |
| :---- | :--------------------------------------- | :------------------------ |
| 1     | `testLoginUsesCorrectDAO`                | Uso corretto DAO          |
| 2     | `testLoginChecksUserActive`              | Check user attivo         |
| 3     | `testLoginCreatesSessionAfterValidation` | Sessione dopo validazione |
| 4     | `testLoginRedirectsAdminToAdminPanel`    | Admin → admin.jsp         |
| 5     | `testLoginRedirectsNormalUserToHome`     | User → index.jsp          |
| 6     | `testLoginShowsGenericError`             | No user enumeration       |
| 7     | `testLoginHandlesDisabledAccount`        | Account disabilitato      |

**Logout:**

| **#** | **Metodo**                             | **Cosa verifica**      |
| :---- | :------------------------------------- | :--------------------- |
| 8     | `testLogoutInvalidatesSession`         | `session.invalidate()` |
| 9     | `testDocumentMissingNullCheckInLogout` | NPE                    |
| 10    | `testDocumentNullCartBeanInLogout`     | NPE                    |

**Session:**

| **#** | **Metodo**                          | **Cosa verifica**                     |
| :---- | :---------------------------------- |:--------------------------------------|
| 11    | `testActiveUserState`               | Utente attivo                         |
| 12    | `testDisabledUserState`             | Utente disabilitato                   |
| 13    | `testAdminRole`                     | Admin                                 |
| 14    | `testDocumentMissingSessionTimeout` | **No session-timeout in** `web.xml` |
| 15    | `testLoginDoesNotLogPassword`       | Password mai loggata                  |

---

## 5. A02:2021 — Cryptographic Failures

**Test totali: \~40**

### 5.1 `DataProtectionTest.java` (11 test)

| **#** | **Metodo**                                     | **Cosa verifica**            |
| :---- | :--------------------------------------------- | :--------------------------- |
| 1     | `testPasswordIsNotStoredInPlaintext`           | Password non in chiaro       |
| 2     | `testPasswordHashHasExpectedLength`            | SHA-1 = 40 char              |
| 3     | `testPasswordHashIsHexadecimal`                | Solo `[0-9a-f]`              |
| 4     | `testSamePasswordProducesSameHash`             | SHA-1 deterministico         |
| 5     | `testPasswordHashMatchesManualSha1Computation` | Correttezza SHA-1            |
| 6     | `testDifferentPasswordsProduceDifferentHashes` | Diversi input → diversi hash |
| 7     | `testSetPasswordHandlesUtf8Characters`         | Supporto UTF-8               |
| 8     | `testConPoolHasNoHardcodedPassword`            | No password hardcoded        |
| 9     | `testConPoolReadsCredentialsFromEnvironment`   | Uso `System.getenv`          |
| 10    | `testConPoolFailsFastOnMissingPassword`        | Fail-fast                    |
| 11    | `testNoHardcodedCredentialsInAnyJavaFile`      | Scansione globale            |

### 5.2 `ConPoolTest.java` (7 test)

| **#** | **Metodo**                         | **Cosa verifica**          |
| :---- | :--------------------------------- | :------------------------- |
| 1     | `testLeggeMysqlPasswordDaEnv`      | Legge `MYSQL_PASSWORD`     |
| 2     | `testLeggeTutteLeCredenzialiDaEnv` | Legge tutte le credenziali |
| 3     | `testNessunaPasswordHardcoded`     | No password hardcoded      |
| 4     | `testFailFastSenzaPassword`        | Fail-fast                  |
| 5     | `testUsaDriverMySQLCorretto`       | Driver corretto            |
| 6     | `testUrlJdbcCorretto`              | URL JDBC sicuro            |
| 7     | `testParametriPoolConfigurati`     | Pool configurato           |

### 5.3 `PaymentSecurityTest.java` (22 test)

**Validazione carta:**

| **#** | **Metodo**                  | **Cosa verifica** |
| :---- | :-------------------------- | :---------------- |
| 1     | `testValidCardNumber`       | 16 cifre valide   |
| 2     | `testCardNumberTooShort`    | <16 rifiutato     |
| 3     | `testCardNumberTooLong`     | >16 rifiutato     |
| 4     | `testCardNumberWithLetters` | Lettere rifiutate |
| 5     | `testDocumentNoLuhnCheck`   |  **No Luhn check**  |

**CVV:**

| **#** | **Metodo**        | **Cosa verifica** |
| :---- | :---------------- | :---------------- |
| 6     | `testValidCvv`    | 3 cifre           |
| 7     | `testCvvTooShort` | <3 rifiutato      |
| 8     | `testCvvTooLong`  | >3 rifiutato      |

**Scadenza:**

| **#** | **Metodo**                                | **Cosa verifica** |
| :---- | :---------------------------------------- | :---------------- |
| 9     | `testFutureDeadlineIsValid`               | Futuro valido     |
| 10    | `testPastDeadlineIsInvalid`               | Passato invalido  |
| 11    | `testDocumentNullDeadline`                | NPE su null       |
| 12    | `testDocumentNumberFormatExceptionOnDate` | No try/catch      |

**PCI-DSS:**

| **#** | **Metodo**                               | **Finding**        |
| :---- | :--------------------------------------- |:-------------------|
| 13    | `testDocumentPlaintextCardNumberStorage` | **PCI-DSS 3.4**    |
| 14    | `testDocumentCvvStorage`                 | **PCI-DSS 3.2**  |
| 15    | `testDocumentNoEncryptionColumn`         | No cifratura       |
| 16    | `testDocumentCvvGetterExposure`          | Getter CVV esposto |

**Login/Auth:**

| **#** | **Metodo**                            | **Cosa verifica**           |
| :---- | :------------------------------------ | :-------------------------- |
| 17    | `testPaymentRequiresLogin`            | Login richiesto             |
| 18    | `testDocumentMissingLoginCheckInPost` | NPE su login non verificato |

**Race condition:**

| **#** | **Metodo**                         | **Finding**                   |
| :---- | :--------------------------------- |:------------------------------|
| 19    | `testDocumentRaceConditionOnStock` | **Race condition su stock** |

**Bean:**

| **#** | **Metodo** | **Cosa verifica**         |
| :---- | :--------- | :------------------------ |
| 20-22 | altri      | Getter/setter PaymentBean |

---

## 6. A03:2021 — Injection

**Test totali: \~51**

### 6.1 `InputValidationTest.java` (17 test)

**Robustezza setter:**

| **#** | **Metodo**                                     | **Cosa verifica**       |
| :---- | :--------------------------------------------- | :---------------------- |
| 1     | `testSetNameHandlesNull`                       | Null-safe               |
| 2     | `testSetNameAcceptsSqlPayloadWithoutExecution` | SQLi memorizzata inerte |
| 3     | `testSetNameAcceptsXssPayload`                 | XSS memorizzata inerte  |
| 4     | `testSetEmailAcceptsValidEmail`                | Email valida            |
| 5     | `testSetPasswordHandlesEmptyString`            | Password vuota          |

**Regex validation:**

| **#** | **Metodo**                             | **Cosa verifica**      |
| :---- | :------------------------------------- | :--------------------- |
| 6     | `testValidEmailPassesRegex`            | Email valida           |
| 7     | `testSqlInjectionInEmailIsRejected`    | SQLi rifiutata         |
| 8     | `testXssInEmailIsRejected`             | XSS rifiutata          |
| 9     | `testValidPasswordPassesRegex`         | Password valida        |
| 10    | `testSqlInjectionInPasswordIsRejected` | SQLi rifiutata         |
| 11    | `testValidPhonePassesRegex`            | Telefono valido        |
| 12    | `testNonNumericPhoneIsRejected`        | Lettere rifiutate      |
| 13    | `testValidPostalCodePassesRegex`       | CAP valido             |
| 14    | `testAlphanumericPostalCodeIsRejected` | Alfanumerico rifiutato |
| 15    | `testValidProvincePassesRegex`         | Provincia valida       |
| 16    | `testInvalidProvinceIsRejected`        | Provincia invalida     |
| 17    | `testNameWithApostropheIsValid`        | Apostrofo valido       |

### 6.2 `RegistrationFlowTest.java` (34 test)

Validazione completa di tutti i campi del form di registrazione:

- Nome (5 test)
- Cognome (2 test)
- Email (4 test)
- Password (3 test)
- Telefono (4 test)
- Città (2 test)
- Provincia (4 test)
- CAP (3 test)
- Indirizzo (3 test)
- Data (2 test)
- Finding flusso (2 test)

---

## 7. A04:2021 — Insecure Design

**Test totali: \~76**

### 7.1 `BusinessLogicTest.java` (20 test)

| **Categoria**          | **# Test** | **Cosa verifica**                |
| :--------------------- | :--------- | :------------------------------- |
| CartBean.addProduct    | 4          | Aggiunta, duplicati, quantità    |
| CartBean.removeProduct | 3          | Rimozione, **bug documentato**   |
| CartBean.setCartList   | 1          | Calcolo `numberObject`           |
| ProductBean            | 4          | Prezzi/quantità (anche negativi) |
| OrderBean              | 3          | Totali (anche negativi)          |
| Integrità              | 2          | Coerenza carrello                |
| **Finding**            | **2**      | SQL Injection + bug filtro       |

### 7.2 `ProductCatalogTest.java` (18 test)

| **#** | **Metodo**                                  | **Cosa verifica**   |
| :---- | :------------------------------------------ |:--------------------|
| 1     | `testMaxLessThanMinIsCorrected`             | Correzione max\<min |
| 2     | `testFilterUsesCorrectDAO`                  | DAO corretto        |
| 3     | `testDocumentFilterBoundaryBug`             | **FIXED** — filtro usa `>=` e `<=`   |
| 4     | `testDocumentNumberFormatExceptionInFilter` | No try/catch        |
| 5     | `testDocumentNullCategoryInFilter`          | NPE                 |
| 6     | `testSearchIsCaseInsensitive`               | Case-insensitive    |
| 7     | `testSearchMatchesNameAndDescription`       | Nome + descrizione  |
| 8     | `testDocumentNullSearchQuery`               | NPE                 |
| 9     | `testSearchReturnsJsonOutput`               | JSON                |
| 10-18 | altri                                       | Vari                |

**Finding risolti:** `testDocumentFilterBoundaryBug` — la query ora usa `Prezzo >= ?` e `Prezzo <= ?`.

### 7.3 `ProductViewTest.java` (22 test)

| **Categoria**    | **# Test** |
| :--------------- | :--------- |
| ProductInfo      | 4          |
| ShowProduct      | 3          |
| ShowCatalog      | 4          |
| ShowSales        | 3          |
| ProductsHomepage | 6          |
| ProductBean      | 2          |

**Finding critici:**

- `testDocumentInfiniteLoopInProductsHomepage` - **DoS loop infinito**
-  `testDocumentEmptyDatabaseRisk` - `IllegalArgumentException`

### 7.4 `CartManagementTest.java` (16 test)

| **Categoria**  | **# Test** |
| :------------- |:-----------|
| AddToCart      | 7 FIXED    |
| RemoveFromCart | 4 FIXED    |
| CartBean       | 5 FIXED    |

**Finding risolti:**

- `testDocumentNegativeQuantityAcceptance` — **FIXED**: `AddToCart` ora valida `quantity > 0`
-  `testDocumentMissingReturnAfterSendError` — **FIXED**: aggiunto `return;` dopo ogni `sendError`
-  `testDocumentNumberFormatExceptionOnProductId` — **FIXED**: aggiunto try/catch su `Integer.parseInt`
-  `testCartRemoveNonExistentBug` — **FIXED**: `CartBean.removeProduct` ora rimuove solo se l'ID esiste
---

## 8. A05:2021 — Security Misconfiguration

**Test totali: \~18**

### `DaoIntegrationTest.java` (18 test)

**PreparedStatement:**

| **#** | **Metodo**                                  | **Cosa verifica**  |
| :---- | :------------------------------------------ | :----------------- |
| 1     | `testOrderDAO_UsesPreparedStatement`        | OrderDAO OK        |
| 2     | `testOrderProductDAO_UsesPreparedStatement` | OrderProductDAO OK |
| 3     | `testCartDAO_UsesPreparedStatement`         | CartDAO OK         |

****Regression test SQL Injection:****

| **#** | **Metodo**                                          | **Finding**                  |
| :---- |:----------------------------------------------------|:-----------------------------|
| 4     | `testUserDAOUpdateUsesPreparedStatement`            | `UserDAO.doUpdate` fixato    |
| 5     | `testUserDAOUpdateStateUsesPreparedStatement`       | `doUpdateState`    fixato    |
| 6     | `testUserDAOUpdateAdminUsesPreparedStatement`       | `doUpdateAdmin`    fixato    |
| 7     | `testProductDAOUpdateUsesPreparedStatement`         | `ProductDAO.doUpdate` fixato |

**Schema DB:**

| **#** | **Metodo**                               | **Cosa verifica**     |
| :---- | :--------------------------------------- | :-------------------- |
| 8     | `testSchemaPasswordColumnIsHashSized`    | `VARCHAR(40)`         |
| 9     | `testSchemaHasNoPlaintextPasswordColumn` | No password in chiaro |
| 10    | `testSchemaEmailIsUnique`                | UNIQUE su email       |
| 11    | `testSchemaTablesHavePrimaryKeys`        | AUTO_INCREMENT        |

**Coerenza DAO ↔ Schema:**

| **#** | **Metodo**                              | **Cosa verifica** |
| :---- | :-------------------------------------- | :---------------- |
| 12    | `testOrderDAOColumnsMatchSchema`        | Coerenza          |
| 13    | `testOrderProductDAOColumnsMatchSchema` | Coerenza          |

**Bean ↔ DAO:**

| **#** | **Metodo**                                  | **Cosa verifica** |
| :---- | :------------------------------------------ | :---------------- |
| 14    | `testOrderBeanFieldsMatchDAOColumns`        | Getter usati      |
| 15    | `testOrderProductBeanFieldsMatchDAOColumns` | Getter usati      |

**ConPool:**

| **#** | **Metodo**                         | **Cosa verifica**      |
| :---- | :--------------------------------- | :--------------------- |
| 16    | `testConPoolIsSingleton`           | Static DataSource      |
| 17    | `testConPoolGetConnectionIsStatic` | Static method          |
| 18    | `testConPoolUrlHasSecurityParams`  | `serverTimezone` + SSL |

---

## 9. A07:2021 — Authentication Failures

**Test totali: \~15 (parte in authorization)**

Coperti da `AuthenticationTest.java` — vedi sezione 4.5.

---

## 10. Riepilogo

### 10.1 Metriche

| **Metrica**              | **Valore**   |
| :----------------------- |:-------------|
| **Test di audit totali** | **\~275**    |
| **Classi di test**       | 15+          |
| **Success rate**         | **100%**     |
| **Ruolo**                | SAST manuale |

### 10.2 Copertura per Area OWASP

| **Area**                        | **Test di audit** |
| :------------------------------ | :---------------- |
| A01 — Broken Access Control     | \~93              |
| A02 — Cryptographic Failures    | \~40              |
| A03 — Injection                 | \~51              |
| A04 — Insecure Design           | \~76              |
| A05 — Security Misconfiguration | \~18              |
| A07 — Authentication Failures   | \~15 (in A01)     |

### 10.3 Finding documentati

I test di audit hanno **scoperto 40+ finding**, di cui **5 risolti** durante l'ultimo ciclo di remediation.

| **Categoria** | **# Finding totali** | **# Risolti** | **# Aperti** | **Esempi** |
|:--------------| :------------------- | :------------ | :----------- | :--------- |
| **Critici**   | 12                   | 0             | 12           | Privilege escalation, CSRF, PCI-DSS, race condition |
| **Alti**      | 8                    | 0             | 8            | SQL Injection, DoS, file upload |
| **Medi**      | 20+                  | 5             | 15+          | NPE, bug logici, NumberFormatException |

**Finding risolti in questa iterazione:**

| # | Finding | File | Test |
| :--- | :--- | :--- | :--- |
| 1 | Validazione `quantity > 0` in AddToCart | `AddToCart.java` | `CartManagementTest.testDocumentNegativeQuantityAcceptance` |
| 2 | `return;` dopo `sendError` | `AddToCart.java` | `CartManagementTest.testDocumentMissingReturnAfterSendError` |
| 3 | Gestione `NumberFormatException` | `AddToCart.java` | `CartManagementTest.testDocumentNumberFormatExceptionOnProductId` |
| 4 | `removeProduct` rimuoveva ID inesistente | `CartBean.java` | `CartManagementTest.testCartRemoveNonExistentBug` |
| 5 | Filtro prezzi usava `>` e `<` | `ProductDAO.java` | `ProductCatalogTest.testDocumentFilterBoundaryBug` |

## 11. Dettaglio dei Finding

Questa sezione documenta in dettaglio i finding più critici emersi dai test di audit, con:

* **Vulnerabilità**: descrizione del problema
* **Test**: come è stato verificato
* **Scenario**: comportamento osservato
* **Patch**: soluzione (applicata o raccomandata)

---

### 11.1 A01 — Privilege Escalation in `SetStateUser`

- **File:** `Controller/SetStateUser.java`
- **Test:** `AdminUserManagementTest.testDocumentPrivilegeEscalationInSetStateUser`
- **CWE:** CWE-269 (Improper Privilege Management)
- **Severità:** Critica

#### Vulnerabilità

Il servlet `SetStateUser` permette a un amministratore di cambiare lo stato (`Stato`) di **qualsiasi utente**, senza verificare se l'utente target è a sua volta un amministratore. Un admin malevolo (o un admin compromesso) può quindi disabilitare **tutti gli altri admin** del sistema, incluso il creatore della piattaforma, assumendo il controllo totale.

#### Test

Il test analizza staticamente il codice di `SetStateUser.java` e verifica che **non esista** una condizione come `if (user.isAdmin())` o `if (target.isAdmin())` prima della chiamata `user.setState("false")`.

```text
boolean noAdminCheck =
        !source.contains("user.isAdmin()") &&
        !source.contains("target.isAdmin()");
assertThat(noAdminCheck).isTrue();
```

#### Scenario

| Scenario                          | Comportamento attuale                    |
| :-------------------------------- |:-----------------------------------------|
| Admin A disabilita utente normale | OK (comportamento atteso)                |
| Admin A disabilita Admin B        | **Consentito** (privilege escalation)    |
| Admin A disabilita se stesso      | **Consentito** (self-lockout, vedi 11.2) |

#### Patch raccomandata

```text
if (user.isAdmin().equalsIgnoreCase("true")) {
    // Aggiungi blocco privilege escalation:
    if (targetUser.isAdmin().equalsIgnoreCase("true")) {
        throw new IllegalArgumentException("Non puoi disabilitare un altro amministratore");
    }

    // ... logica esistente
}
```

---

### 11.2 A01 — Self-Lockout in `SetStateUser`

- **File:** `Controller/SetStateUser.java`
- **Test:** `AdminUserManagementTest.testDocumentSelfLockoutInSetStateUser`
- **CWE:** CWE-269 (Improper Privilege Management)
- **Severità:** Critica

#### Vulnerabilità

Un amministratore può **disabilitare se stesso** cliccando il pulsante "disabilita" sul proprio profilo. Dopo questa azione, perde immediatamente l'accesso al sistema e **nessuno può riabilitarlo** (se è l'unico admin). Questo causa un **denial of service permanente** della piattaforma.

#### Test

```text
boolean noSelfCheck =
        !source.contains("userAdmin.getId() != user.getId()") &&
        !source.contains("userAdmin.getId() == user.getId()") &&
        !source.contains("self");
assertThat(noSelfCheck).isTrue();
```

#### Scenario

| Scenario                        | Comportamento attuale |
| :------------------------------ |:----------------------|
| Admin disabilita utente normale | OK                    |
| Admin disabilita un altro admin | Privilege escalation  |
| **Admin disabilita se stesso**  | **Self-lockout**      |

#### Patch raccomandata

```text
if (userAdmin.getId() == targetUser.getId()) {
    throw new IllegalArgumentException("Non puoi disabilitare te stesso");
}
```

---

### 11.3 A01 — CSRF su azioni GET (`SetAdmin`, `SetStateUser`, `EditProfile`)

- **File:** `Controller/SetAdmin.java`, `Controller/SetStateUser.java`, `Controller/EditProfile.java`
- **Test:** `AdminUserManagementTest.testDocumentCsrfInSetAdmin`, `testDocumentCsrfInSetStateUser`, `ProfileAndCartTest.testDocumentCsrfInEditProfile`
- **CWE:** CWE-352 (Cross-Site Request Forgery)
- **Severità:**  Critica

#### Vulnerabilità

Le azioni di **modifica dello stato applicativo** (promozione admin, disabilitazione utente, modifica profilo) sono esposte tramite metodo **GET**, senza token CSRF. Un attaccante può costruire un link malevolo che, se cliccato da un utente autenticato, esegue l'azione senza il suo consenso.

**Esempio di attacco:**

```html
<img src="https://hardwarezone.com/set-admin-servlet?id=5" width="0" height="0">
```

Se un admin visualizza la pagina contenente questo `<img>`, l'utente con ID 5 viene **promosso ad admin** senza che nessuno se ne accorga.

#### Test

```text
assertThat(source).as("FINDING: azione esposta via GET senza token CSRF").contains("doGet(HttpServletRequest");
```

#### Scenario

| Scenario                                                    | Comportamento attuale        |
| :---------------------------------------------------------- |:-----------------------------|
| Admin promuove utente via form POST                         | Il form attuale usa GET      |
| Attaccante invia link `<img src="/set-admin-servlet?id=5">` | **Eseguito automaticamente** |
| Admin visualizza il link                                    | Promozione non voluta        |

#### Patch raccomandata

1. **Cambiare metodo HTTP** da GET a **POST**

2. **Aggiungere token CSRF** in tutte le form:

   ```html
   <input type="hidden" name="csrf_token" value="${sessionScope.csrf_token}">
   ```

3. **Validare il token** nel servlet:

   ```text
   String token = request.getParameter("csrf_token");
   if (!token.equals(session.getAttribute("csrf_token"))) {
       response.sendError(403, "CSRF token non valido");
       return;
   }
   ```

---

### 11.4 A03 — SQL Injection in `UserDAO.doUpdate` / `doUpdateState` / `doUpdateAdmin`

- **File:** `Model/UserDAO.java`
- **Test:** `DaoIntegrationTest.testFindingSqlInjectionInUserDAO`, `testFindingSqlInjectionInUserDAOUpdateState`, `testFindingSqlInjectionInUserDAOUpdateAdmin`
- **CWE:** CWE-89 (SQL Injection)
- **Severità:** Critica

#### Vulnerabilità

I tre metodi di aggiornamento di `UserDAO` utilizzano `Statement` con **concatenazione di stringhe** anziché `PreparedStatement` parametrizzato. Questo permette a un attaccante di iniettare SQL arbitrario.

**Esempio di payload:**

```text
name = "', Amministratore='true' WHERE Id_Utente=1; --"
```

Risultato: l'utente con ID 1 diventa automaticamente admin.

#### Test

```text
boolean hasRawStatement =
    source.contains("con.createStatement()") &&
    source.contains("UPDATE Utente SET Nome = '\" +");
assertThat(hasRawStatement).isTrue();
```

#### Scenario

| Scenario                                             | Comportamento attuale             |
| :--------------------------------------------------- |:----------------------------------|
| Utente aggiorna proprio profilo (input normale)      | OK                                |
| Attaccante invia `name="', Amministratore='true'--"` | **Privilege escalation via SQLi** |
| Attaccante invia `id=1 OR 1=1`                       | **Modifica tutti gli utenti**     |

#### Patch raccomandata

**Sostituire `Statement` con `PreparedStatement`:**

```java
public void doUpdate(UserBean utente) {
    try (Connection con = ConPool.getConnection()) {
        PreparedStatement ps = con.prepareStatement(
            "UPDATE Utente SET Nome=?, Cognome=?, Data_Nascita=?, Email=?, " +
            "Accesso=?, Telefono=?, Citta=?, Provincia=?, Codice_Postale=?, " +
            "Indirizzo=?, Stato=?, Amministratore=? WHERE Id_Utente=?");

        ps.setString(1, utente.getName());
        ps.setString(2, utente.getSurname());
        // ...
        ps.setInt(13, utente.getId());
        ps.executeUpdate();
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
}
```
---

### 11.5 Finding risolti — Riepilogo remediation

I seguenti finding sono stati **corretti** e i test ora verificano la presenza del fix (non più la vulnerabilità).

#### 11.5.1 A04 — Validazione `quantity` in `AddToCart`

- **File:** `Controller/AddToCart.java`
- **Test:** `CartManagementTest.testDocumentNegativeQuantityAcceptance`
- **Fix applicato:**

```text
  boolean validQuantity = quantity > 0;
  if (!validQuantity) {
      response.sendError(400);
      return;
  }
```
- **Stato**: FIXED

#### 11.5.2 A04 — `return;` dopo `sendError` in `AddToCart`
- **File:** `Controller/AddToCart.java`
- **Test:** `CartManagementTest.testDocumentMissingReturnAfterSendError`
- **Fix applicato:** aggiunto return; dopo ogni response.sendError(400) per interrompere l'esecuzione.
- **Stato**:  FIXED

#### 11.5.3 A04 — Gestione `NumberFormatException` in `AddToCart`
- **File:** `Controller/AddToCart.java`
- **Test:** `CartManagementTest.testDocumentNumberFormatExceptionOnProductId`
- **Fix applicato:**

```text
     try {
        productId = Integer.parseInt(request.getParameter("productId"));
     }catch (NumberFormatException e) {
        response.sendError(400);
    return;
}
```
- **Stato**: FIXED

#### 11.5.4 A04 — Bug `CartBean.removeProduct`
- **File:** `Model/CartBean.java`
- **Test:** `CartManagementTest.testCartRemoveNonExistentBug`
- Fix applicato:

```java
public void removeProduct(int id) {
 for (int i = 0; i < cartList.size(); i++) {
  if (cartList.get(i).getId() == id) {
   numberObject -= cartList.get(i).getQuantity();
   cartList.remove(i);
   return; // esci dopo aver rimosso l'elemento corretto
  }
 }
}
```
- **Stato:**  FIXED

#### 11.5.5 A04 — Boundary bug filtro prezzi
- **File:** `Model/ProductDAO.java`
- **Test:** `ProductCatalogTest.testDocumentFilterBoundaryBug`
- **Fix applicato:** query SQL cambiata da Prezzo > ? a Prezzo >= ? e da Prezzo < ? a Prezzo <= ?.
- **Stato:**  FIXED
