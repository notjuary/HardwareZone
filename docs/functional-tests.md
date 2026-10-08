# Test Funzionali di Sicurezza — HardwareZone

**Progetto:** HardwareZone - E-commerce di componenti hardware  
**Categoria:** Test funzionali (esecuzione codice)  
**Framework:** JUnit 5 + Mockito 5 + H2 Database + AssertJ  
**Data:** 06/10/2026

---

## Indice

1. [Panoramica](#1-panoramica)
2. [Architettura e Infrastruttura](#2-architettura-e-infrastruttura)
3. [Organizzazione dei Test](#3-organizzazione-dei-test)
4. [A01:2021 — Broken Access Control](#4-a012021--broken-access-control)
5. [A02:2021 — Cryptographic Failures](#5-a022021--cryptographic-failures)
6. [A03:2021 — Injection](#6-a032021--injection)
7. [A04:2021 — Insecure Design](#7-a042021--insecure-design)
8. [A05:2021 — Security Misconfiguration](#8-a052021--security-misconfiguration)
9. [A07:2021 — Authentication Failures](#9-a072021--authentication-failures)
10. [Test GDPR e Business Logic](#10-test-gdpr-e-business-logic)
11. [Test DAO con H2](#11-test-dao-con-h2)
12. [Riepilogo](#12-riepilogo)

---

## 1. Panoramica

I **test funzionali** sono test che **eseguono il codice di produzione** con oggetti mock (Servlet API) e un database in-memory (H2), verificando il comportamento a runtime dell'applicazione.

### Caratteristiche

| Aspetto |             Valore              |
|:---|:-------------------------------:|
| **Test totali** |              ~179               |
| **Classi di test** |               40                |
| **Tecnologia** | JUnit 5, Mockito 5, H2, AssertJ |
| **Contributo Coverage** |           Sì (JaCoCo)           |
| **Durata media** |          ~3-5 secondi           |

### Obiettivi

1. **Verificare il comportamento a runtime** dei Servlet (autorizzazione, autenticazione, validazione)
2. **Testare i DAO** contro un database reale (H2 in-memory con schema MySQL)
3. **Testare i Bean** (getter/setter, logica, hashing)
4. **Alimentare la coverage SonarCloud** (contributo >85%)

---

## 2. Architettura e infrastruttura

### 2.1 Classi base

#### `BaseFunctionalTest.java`

Classe astratta che configura il database **H2 in-memory** con lo schema del progetto.

**Funzionalità:**

- Setup dello schema completo (Utente, Prodotto, Ordine, Ordine_Prodotto, Pagamento, Carrello, Categoria)
- Registrazione della UDF `SHA1()` come alias Java (per compatibilità con le query MySQL)
- `cleanDatabase()` con `TRUNCATE TABLE ... RESTART IDENTITY` (reset auto-increment)
- `executeSql()` per popolare dati di test

```java
public abstract class BaseFunctionalTest {
    
    @BeforeAll
    static void setUpDatabase() throws Exception {
        // Configura H2 in-memory
        PoolProperties p = new PoolProperties();
        p.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL");
        p.setDriverClassName("org.h2.Driver");
        // ...
        ConPool.setTestDataSource(h2DataSource);
    }
    
    protected void executeSql(String sql) throws Exception { /* ... */ }
    protected void cleanDatabase() throws Exception { /* ... */ }
}
```

#### `ServletTestSupport.java`

Classe di supporto che fornisce mock preconfigurati di:

- `HttpServletRequest`
- `HttpServletResponse`
- `HttpSession`
- `RequestDispatcher`

E helper:

- `invokeDoGet()` / `invokeDoPost()` via reflection
- `createAdminUser()` / `createNormalUser()`

```java
public class ServletTestSupport {
    public HttpServletRequest request;
    public HttpServletResponse response;
    public HttpSession session;
    public RequestDispatcher dispatcher;

    public void setUpBase() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);
        // ...
    }
}
```

### 2.2 Strategia di Test

**Per ogni Servlet:**

1. **Setup** - `cleanDatabase()` + `executeSql(...)` per popolare dati di test + `support.setUpBase()`
2. **Configurazione mock** - `when(support.session.getAttribute("user")).thenReturn(...)`
3. **Esecuzione** - `support.invokeDoGet(servlet, request, response)`
4. **Verifica** - `verify(dispatcher).forward(request, response)` e simili

**Per ogni DAO:**

1. **Setup** - `cleanDatabase()` + `executeSql(...)` per popolare il DB H2
2. **Esecuzione** - `dao.doSave(user)` o simili
3. **Verifica** - `assertThat(retrieved).isNotNull()`, `assertThat(retrieved.getEmail()).isEqualTo(...)`

---

## 3. Organizzazione dei Test

```
src/test/java/security/functional/
├── BaseFunctionalTest.java            ← Configurazione H2
├── ServletTestSupport.java            ← Mock Servlet API
│
├── authorization/                     ← A01 + A07 (15 file)
│   ├── AddProductFunctionalTest.java
│   ├── EditProductFunctionalTest.java
│   ├── LoginFunctionalTest.java
│   ├── LogoutFunctionalTest.java
│   ├── OrderInfoFunctionalTest.java
│   ├── OrdersFunctionalTest.java
│   ├── PaymentFunctionalTest.java
│   ├── ProductsFunctionalTest.java
│   ├── ProfileAndCartFunctionalTest.java
│   ├── RegistrationFunctionalTest.java
│   ├── SetAdminFunctionalTest.java
│   ├── SetStateUserFunctionalTest.java
│   ├── UserInfoFunctionalTest.java
│   ├── UserProfileFunctionalTest.java
│   └── UsersFunctionalTest.java
│
├── businesslogic/                     ← A04 (14 file)
│   ├── AddToCartFunctionalTest.java
│   ├── CartBeanFunctionalTest.java
│   ├── CategoryBeanFunctionalTest.java
│   ├── OrderBeanFunctionalTest.java
│   ├── OrderProductBeanFunctionalTest.java
│   ├── ProductBeanFunctionalTest.java
│   ├── ProductCartBeanFunctionalTest.java
│   ├── ProductInfoFunctionalTest.java
│   ├── ProductsHomepageFunctionalTest.java
│   ├── RemoveFromCartFunctionalTest.java
│   ├── ShowCartFunctionalTest.java
│   ├── ShowCatalogFunctionalTest.java
│   ├── ShowProductFunctionalTest.java
│   └── ShowSalesFunctionalTest.java
│
├── daointegration/                    ← A03 (7 file)
│   ├── CartDAOFunctionalTest.java
│   ├── CategoryDAOFunctionalTest.java
│   ├── OrderDAOFunctionalTest.java
│   ├── OrderProductDAOFunctionalTest.java
│   ├── PaymentDAOFunctionalTest.java
│   ├── ProductDAOFunctionalTest.java
│   └── UserDAOFunctionalTest.java
│
├── dataprotection/                    ← A02 (2 file)
│   ├── PaymentBeanFunctionalTest.java
│   └── UserBeanFunctionalTest.java
│
└── inputvalidation/                   ← A03 (2 file)
    ├── FilterProductFunctionalTest.java
    └── SearchProductFunctionalTest.java
```

**Totale file di test funzionali: 40**

---

## 4. A01:2021 — Broken Access Control

**Test totali: ~45**

### 4.1 Servlet Admin (Products, Users, Orders, OrderInfo)

**`ProductsFunctionalTest.java` — 4 test**

| # | Metodo | Cosa verifica |
|:--|:---|:---|
| 1 | `testAdminRichiesto` | Admin → `/WEB-INF/results/products.jsp` |
| 2 | `testUtenteNormaleReindirizzato` | Utente normale → `index.jsp` |
| 3 | `testUtenteAnonimo` | NPE documentato su `user.isAdmin()` |
| 4 | `testAdminVedeTuttiProdotti` | Admin vede i prodotti |

**`UsersFunctionalTest.java` — 3 test**

| # | Metodo | Cosa verifica |
|:--|:---|:---|
| 1 | `testAdminVedeTuttiUtenti` | Admin vede `/WEB-INF/results/users.jsp` |
| 2 | `testUtenteNormaleReindirizzato` | Utente normale → `index.jsp` |
| 3 | `testUtenteAnonimo` | NPE documentato |

**`OrdersFunctionalTest.java` — 3 test**

| # | Metodo | Cosa verifica |
|:--|:---|:---|
| 1 | `testAdminVedeTuttiOrdini` | Admin vede tutti gli ordini |
| 2 | `testUtenteVedeSoloPropriOrdini` | Utente vede solo i propri |
| 3 | `testUtenteAnonimo` | NPE documentato |

**`OrderInfoFunctionalTest.java` — 3 test**

| # | Metodo | Cosa verifica |
|:--|:---|:---|
| 1 | `testAdminVedeOrdine` | Admin vede qualsiasi ordine |
| 2 | `testUtenteVedePropriOrdini` | Utente vede solo i propri |
| 3 | `testUtenteAnonimo` | NPE documentato |

### 4.2 Servlet Admin (AddProduct, EditProduct, SetAdmin, SetStateUser, UserInfo)

**`AddProductFunctionalTest.java` — 3 test**

| # | Metodo | Cosa verifica |
|:--|:---|:---|
| 1 | `testDoGetAdminRichiesto` | Admin → `/WEB-INF/admin/add-product.jsp` |
| 2 | `testDoGetUtenteNormale` | Utente normale → `index.jsp` |
| 3 | `testDoGetUtenteAnonimo` | NPE documentato |

**`EditProductFunctionalTest.java` — 2 test**

| # | Metodo | Cosa verifica |
|:--|:---|:---|
| 1 | `testAdminRichiesto` | Modifica solo admin |
| 2 | `testAdminInputInvalido` | Gestione input invalidi |

**`SetAdminFunctionalTest.java` — 4 test**

| # | Metodo | Cosa verifica |
|:--|:---|:---|
| 1 | `testAdminRichiesto` | Promozione solo admin |
| 2 | `testUtenteAnonimo` | NPE documentato |
| 3 | `testAdminPromuoveUtente` | Promozione OK |
| 4 | `testNonPromuoveChiGiaAdmin` | No doppia promozione |

**`SetStateUserFunctionalTest.java` — 3 test**

| # | Metodo | Cosa verifica |
|:--|:---|:---|
| 1 | `testAdminRichiesto` | Cambio stato solo admin |
| 2 | `testUtenteAnonimo` | NPE documentato |
| 3 | `testAdminDisabilitaUtente` | Cambio stato OK |


**`UserInfoFunctionalTest.java` — 3 test**

| # | Metodo | Cosa verifica |
|:--|:---|:---|
| 1 | `testAdminVedeProfilo` | Admin vede `/WEB-INF/results/userinfo.jsp` |
| 2 | `testUtenteNormale` | Utente normale → `index.jsp` |
| 3 | `testUtenteNull` | NPE documentato |

### 4.3 Servlet Utente (UserProfile)

**`UserProfileFunctionalTest.java` — 3 test**

| # | Metodo | Cosa verifica |
|:--|:---|:---|
| 1 | `testAdmin` | Admin → `profile-admin.jsp` |
| 2 | `testUtenteNormale` | Utente → `profile-user.jsp` |
| 3 | `testUtenteNull` | NPE documentato |

### 4.4 Gestione profilo e carrello utente

**`ProfileAndCartFunctionalTest.java` — 13 test**

| # | Metodo | Cosa verifica |
|:--|:---|:---|
| 1 | `testUserProfileAdmin` | Admin → `profile-admin.jsp` |
| 2 | `testUserProfileUtenteNormale` | Utente → `profile-user.jsp` |
| 3 | `testUserProfileUtenteNull` | NPE documentato |
| 4 | `testEditProfileValido` | Modifica OK |
| 5 | `testEditProfileEmailInvalida` | Email invalida rifiutata |
| 6 | `testEditProfileNomeVuoto` | Nome vuoto rifiutato |
| 7 | `testEditProfileUtenteNull` | NPE documentato |
| 8 | `testShowCartCarrelloValido` | Carrello → JSON |
| 9 | `testShowCartCarrelloNull` | NPE documentato |
| 10 | `testUserInfoAdmin` | Admin vede il profilo |
| 11 | `testUserInfoUtenteNormale` | User → `index.jsp` |
| 12 | `testUserInfoUtenteNull` | NPE documentato |
| 13 | `testUserInfoIdNonNumerico` | NumberFormatException |

### 4.5 Pagamento

**`PaymentFunctionalTest.java` — 9 metodi (12 esecuzioni con parametrizzati)**

| # | Metodo | Cosa verifica |
|:--|:---|:---|
| 1 | `testPaymentSenzaLogin` | Login richiesto |
| 2 | `testPaymentConLogin` | Pagina caricata |
| 3 | `testPaymentSenzaCarrello` | NPE documentato |
| 4 | `testPaymentConCartaValida` | Pagamento OK |
| 5 | `testPaymentConCartaInvalida` | Carta invalida rifiutata |
| 6 | `testPaymentInputInvalido` (param.) | 3 casi: numero non numerico, CVV invalido, scadenza passata |
| 7 | `testPaymentDeadlineNull` | NPE documentato |
| 8 | `testPaymentDoPostSenzaLogin` | NPE documentato |
| 9 | `testPaymentScadenzaMalformata` | NumberFormatException |

**Nota sul refactoring:** i 3 test originali (`testPaymentNumeroCartaNonNumerico`, `testPaymentCvvNonValido`, `testPaymentScadenzaPassata`) sono stati unificati in un unico test parametrizzato con `@ParameterizedTest` e `@CsvSource`. JUnit esegue 3 iterazioni (una per caso).

---

## 5. A02:2021 — Cryptographic Failures

**Test totali: ~14**

**`UserBeanFunctionalTest.java` — 11 test**

| # | Metodo | Cosa verifica                         |
|:--|:---|:--------------------------------------|
| 1 | `testPasswordHash` | SHA-1 hex, 40 caratteri               |
| 2 | `testPasswordNotPlaintext` | Password non in chiaro                |
| 3 | `testDeterministicHash` | SHA-1 deterministico                  |
| 4 | `testDifferentPasswordsProduceDifferentHashes` | Input diversi → hash diversi          |
| 5 | `testGetSetAll` | Getter/setter dei campi               |
| 6 | `testBirthday` | Parsing data di nascita               |
| 7 | `testRegister` | Data di registrazione                 |
| 8 | `testRegisterNow` | Data di oggi                          |
| 9 | `testDefaults` | Default: id=0, admin=null             |
| 10 | `testNewUserHasNullAdmin` | Admin default null                    |
| 11 | `testDocumentSha1Usage` | **Finding**: SHA-1 invece di bcrypt |

**`PaymentBeanFunctionalTest.java` — 3 test**

| # | Metodo | Cosa verifica                      |
|:--|:---|:-----------------------------------|
| 1 | `testGetSetAll` | Getter/setter dati carta           |
| 2 | `testDatePaymentNow` | Data pagamento corrente            |
| 3 | `testCvvGetterExposed` | **Finding**: getter CVV pubblico |

**`PaymentDAOFunctionalTest.java` — 2 test**  
(vedi sezione 11 — DaoIntegration)

---

## 6. A03:2021 — Injection

**Test totali: ~11**

**`RegistrationFunctionalTest.java` — 3 test**

| # | Metodo | Cosa verifica |
|:--|:---|:---|
| 1 | `testRegistrazioneInputInvalido` (param.) | 3 casi: email malformata, nome vuoto, SQLi |
| 2 | `testEmailDuplicata` | Email univoca |
| 3 | `testRegistrazioneValida` | Registrazione OK |

**`LoginFunctionalTest.java` — 3 test**

| # | Metodo | Cosa verifica |
|:--|:---|:---|
| 1 | `testLoginFallitoNonCreaSessione` (param.) | 3 casi: password errata, email inesistente, disabilitato |
| 2 | `testLoginAdmin` | Login admin |
| 3 | `testLoginUtenteNormale` | Login utente |

**`FilterProductFunctionalTest.java` — 4 test**

| # | Metodo | Cosa verifica |
|:--|:---|:---|
| 1 | `testParametriValidi` | Filtro funzionante |
| 2 | `testMaxMinCorretto` | Correzione max < min |
| 3 | `testMinNonNumerico` | NumberFormatException |
| 4 | `testCategoryNull` | NPE documentato |

**`SearchProductFunctionalTest.java` — 3 test**

| # | Metodo | Cosa verifica |
|:--|:---|:---|
| 1 | `testQueryValida` | Ricerca funzionante |
| 2 | `testQueryNull` | NPE documentato |
| 3 | `testSqlInjection` | Payload inerte |

---

## 7. A04:2021 — Insecure Design

**Test totali: ~20**

**`ShowCatalogFunctionalTest.java` — 4 test**

| # | Metodo | Cosa verifica |
|:--|:---|:---|
| 1 | `testCatalogoPubblico` | Pagina pubblica |
| 2 | `testCatalogoCaricaDati` | Dati caricati |
| 3 | `testCatalogoNonEsponeAdmin` | No dati admin |
| 4 | `testCatalogoNonRichiedeAdmin` | No auth richiesta |

**`ShowSalesFunctionalTest.java` — 4 test**

| # | Metodo | Cosa verifica |
|:--|:---|:---|
| 1 | `testPaginaScontiPubblica` | Pagina pubblica |
| 2 | `testCaricaCategorieEProdotti` | Dati caricati |
| 3 | `testNonEsponeDatiAdmin` | No dati admin |
| 4 | `testNonRichiedeAdmin` | No auth richiesta |

**`ShowProductFunctionalTest.java` — 2 test**

| # | Metodo | Cosa verifica |
|:--|:---|:---|
| 1 | `testPubblico` | Pagina pubblica |
| 2 | `testNonEsponeAdmin` | No dati admin |

**`ProductsHomepageFunctionalTest.java` — 2 test**

| # | Metodo | Cosa verifica                              |
|:--|:---|:-------------------------------------------|
| 1 | `testHomepagePubblica` |  Pagina pubblica (loop infinito **FIXED**) |
| 2 | `testNonRichiedeAdmin` | No auth richiesta                          |

**`ProductInfoFunctionalTest.java` — 2 test**

| # | Metodo | Cosa verifica |
|:--|:---|:---|
| 1 | `testUtenteAnonimoCausaNPE` | NPE documentato |
| 2 | `testAdminCaricaDati` | Admin vede i dati |

**`AddToCartFunctionalTest.java` — 3 test**

| # | Metodo | Cosa verifica                                                 |
|:--|:---|:--------------------------------------------------------------|
| 1 | `testAggiungiProdottoValido` | Aggiunta OK                                                   |
| 2 | `testQuantityNegativa` | **FIXED**: quantity negativa rifiutata con `sendError(400)`   |
| 3 | `testProdottoInesistente` | **FIXED**: productId inesistente gestito con `sendError(404)` |

**`RemoveFromCartFunctionalTest.java` — 2 test**

| # | Metodo | Cosa verifica |
|:--|:---|:---|
| 1 | `testCartNull` | NPE documentato |
| 2 | `testRimuoviProdotto` | Rimozione OK |

**`ShowCartFunctionalTest.java` — 2 test**

| # | Metodo | Cosa verifica |
|:--|:---|:---|
| 1 | `testCarrelloNull` | NPE documentato |
| 2 | `testCarrelloValidoRestituisceJson` | JSON output |

---

## 8. A05:2021 — Security Misconfiguration

**Test totali: ~6 (principalmente coperti dai test Bean)**

**`ShowCatalogFunctionalTest`, `ShowSalesFunctionalTest`, `ShowProductFunctionalTest`** coprono la corretta configurazione delle pagine pubbliche.

**`UserProfileFunctionalTest`** copre la gestione corretta del profilo.

---

## 9. A07:2021 — Authentication Failures

**Test totali: ~10**

**`LoginFunctionalTest.java` — 3 test** (vedi A03)

**`LogoutFunctionalTest.java` — 3 test**

| # | Metodo | Cosa verifica |
|:--|:---|:---|
| 1 | `testLogoutInvalidaSessione` | `session.invalidate()` |
| 2 | `testLogoutUtenteAnonimo` | NPE documentato |
| 3 | `testLogoutRedirect` | Redirect a index |

**`RegistrationFunctionalTest.java` — 3 test** (vedi A03)

**`EditProfileFunctionalTest.java` — 3 test**

| # | Metodo | Cosa verifica |
|:--|:---|:---|
| 1 | `testEditProfileValido` | Modifica OK |
| 2 | `testEditProfileInvalido` | Modifica rifiutata |
| 3 | `testEditProfileUtenteNull` | NPE documentato |

`ProfileAndCartFunctionalTest.java` **(13 test)**

| **#** | **Metodo**                       | **Cosa verifica**                    |
| :---- | :------------------------------- | :----------------------------------- |
| 1     | `testUserProfileAdmin`           | Admin → `profile-admin.jsp`          |
| 2     | `testUserProfileUtenteNormale`   | Utente → `profile-user.jsp`          |
| 3     | `testUserProfileUtenteNull`      | NPE documentato                      |
| 4     | `testEditProfileValido`          | Modifica OK                          |
| 5     | `testEditProfileEmailInvalida`   | Email invalida rifiutata             |
| 6     | `testEditProfileNomeVuoto`       | Nome vuoto rifiutato                 |
| 7     | `testEditProfileUtenteNull`      | NPE documentato                      |
| 8     | `testShowCartCarrelloValido`     | Carrello → JSON                      |
| 9     | `testShowCartCarrelloNull`       | NPE documentato                      |
| 10    | `testUserInfoAdmin`              | Admin vede il profilo                |
| 11    | `testUserInfoUtenteNormale`      | User → `index.jsp`                   |
| 12    | `testUserInfoUtenteNull`         | NPE documentato                      |
| 13    | `testUserInfoIdNonNumerico`      | NumberFormatException                |
---

## 10. Test GDPR e Business Logic

### 10.1 Test Bean (Business Logic)

**Test totali: ~14**

| File | # Test | Cosa verifica |
|:---|:---:|:---|
| `ProductBeanFunctionalTest` | 4 | Getter/setter, defaults, prezzi negativi |
| `OrderBeanFunctionalTest` | 3 | Getter/setter, defaults |
| `CartBeanFunctionalTest` | 7 | Add/remove prodotti, bug documentato |
| `CategoryBeanFunctionalTest` | 2 | Getter/setter |
| `OrderProductBeanFunctionalTest` | 2 | Getter/setter |
| `ProductCartBeanFunctionalTest` | 2 | Getter/setter |

### 10.2 Test GDPR (impliciti)

I test GDPR sono **integrati** in `ProfileAndCartFunctionalTest`:

- `testUserProfileAdmin` / `testUserProfileUtenteNormale` → diritto di accesso al proprio profilo
- `testEditProfileValido` → diritto di rettifica dei propri dati
- `testUserInfoAdmin` → accesso admin ai profili (con autorizzazione)

---

## 11. Test DAO con H2

**Test totali: ~30**

**`UserDAOFunctionalTest.java` — 8 test**

| # | Metodo | Cosa verifica |
|:--|:---|:---|
| 1 | `testDoSave` | Insert utente |
| 2 | `testDoRetrieveById` | Retrieve per ID |
| 3 | `testDoRetrieveByIdInesistente` | ID inesistente → null |
| 4 | `testDoRetrieveByEmailAndPassword` | Retrieve per credenziali |
| 5 | `testPasswordErrata` | Password errata → null |
| 6 | `testSqlInjectionLogin` | SQLi bloccata da PreparedStatement |
| 7 | `testIsAlreadyRegistered` | Verifica email esistente |
| 8 | `testDoRetrieveAll` | Retrieve tutti |

**`ProductDAOFunctionalTest.java` — 7 test**

| # | Metodo | Cosa verifica |
|:--|:---|:---|
| 1 | `testDoSave` | Insert prodotto |
| 2 | `testDoRetrieveById` | Retrieve per ID |
| 3 | `testDoRetrieveByIdInesistente` | ID inesistente → null |
| 4 | `testDoRetrieveAll` | Retrieve tutti |
| 5 | `testDoRetrieveSales` | Solo prodotti in sconto |
| 6 | `testDoRetrieveByFilter` | Filtro prezzo |
| 7 | `testIsAlreadyRegistered` | Verifica duplicati |

**`CategoryDAOFunctionalTest.java` — 3 test**

| # | Metodo | Cosa verifica |
|:--|:---|:---|
| 1 | `testDoSave` | Insert categoria |
| 2 | `testDoRetrieveAllOrdinato` | Ordinamento alfabetico |
| 3 | `testDoRetrieveAllVuoto` | DB vuoto |

**`OrderDAOFunctionalTest.java` — 4 test**

**`OrderProductDAOFunctionalTest.java` — 2 test**

| # | Metodo | Cosa verifica |
|:--|:---|:---|
| 1 | `testDoSave` | Insert order-product |
| 2 | `testDoRetrieveById` | Retrieve per order ID |

**`CartDAOFunctionalTest.java` — 4 test**

**`PaymentDAOFunctionalTest.java` — 2 test**

| # | Metodo | Cosa verifica                           |
|:--|:---|:----------------------------------------|
| 1 | `testDoSave` | Salvataggio pagamento                   |
| 2 | `testNumeroCartaInChiaro` | **Finding PCI-DSS**: numero in chiaro |

---

## 12. Riepilogo

### 12.1 Metriche

| Metrica |    Valore    |
|:---|:------------:|
| **Test funzionali totali** |   **~179**   |
| **Classi di test** |    **40**    |
| **Success rate** |  **100%**    |
| **Durata media** | ~3-5 secondi |
| **Contributo Coverage** |     ~85%     |

### 12.2 Copertura per Area OWASP

| Area | Test funzionali |
|:---|:---:|
| A01 — Broken Access Control | ~45 |
| A02 — Cryptographic Failures | ~14 |
| A03 — Injection | ~11 |
| A04 — Insecure Design | ~20 |
| A05 — Security Misconfiguration | ~6 |
| A07 — Authentication Failures | ~10 |
| GDPR / Business Logic / DAO | ~75 |

### 12.3 Finding documentati

I test funzionali hanno **scoperto a runtime** i seguenti finding. Alcuni sono stati **risolti** durante l'ultima iterazione.

| Finding | Test | Stato       |
|:---|:---|:------------|
| Privilege escalation (`SetStateUser`) | `SetStateUserFunctionalTest` | Documentato |
| NPE su login anonimo | `LoginFunctionalTest` | Documentato |
| Quantità negative accettate | `AddToCartFunctionalTest` | **FIXED**   |
| Prodotto inesistente causa NPE | `AddToCartFunctionalTest` | **FIXED**   |
| Numero carta in chiaro (PCI-DSS) | `PaymentDAOFunctionalTest` | Documentato |
| SHA-1 per password | `UserBeanFunctionalTest` | Documentato |
| NPE su carrello null | `ShowCartFunctionalTest`, `RemoveFromCartFunctionalTest` | Documentato |
| Loop infinito homepage | `ProductsHomepageFunctionalTest` | **FIXED**   |
| CVV getter esposto | `PaymentBeanFunctionalTest` | Documentato |
| Bug `removeProduct` | `CartBeanFunctionalTest` | **FIXED**   |

## 13. Dettaglio dei finding documentati

La seguente sezione documenta in dettaglio i finding emersi **a runtime** dai test funzionali, con:

* **Vulnerabilità**: descrizione del problema
* **Test**: file e metodo che ha rilevato il finding
* **Scenario**: comportamento osservato
* **Patch**: soluzione applicata o raccomandata

---

### 13.1 Privilege escalation in `SetStateUser`

- **File:** `src/main/java/Controller/SetStateUser.java`
- **Test:** `SetStateUserFunctionalTest`
- **CWE:** CWE-269 (Improper Privilege Management)
- **Severità:** Critica

#### Vulnerabilità

Il servlet `SetStateUser` permette a un amministratore di modificare lo stato (`Stato`) di **qualsiasi utente** senza verificare se l'utente target è a sua volta un amministratore. Un admin malevolo può disabilitare **tutti gli altri admin**, incluso il creatore del sistema, assumendo il controllo totale.

#### Test

```java
@Test
@DisplayName("SECURITY: SetStateUser permette a un admin di disabilitare un altro admin")
void testAdminDisabilitaAdmin() throws Exception {
    UserBean adminTarget = support.createAdminUser();
    adminTarget.setId(2);
    adminTarget.setState("true");

    when(support.session.getAttribute("user")).thenReturn(support.createAdminUser());
    when(support.request.getParameter("id")).thenReturn("2");
    when(support.request.getParameter("active")).thenReturn("true");

    SetStateUser servlet = new SetStateUser();
    support.invokeDoGet(servlet, support.request, support.response);

    // Il servlet esegue l'operazione senza controllare se il target è admin
    verify(support.request).getRequestDispatcher("/users-servlet");
}
```

#### Scenario

| Scenario                        |     Comportamento attuale     |
| :------------------------------ |:-----------------------------:|
| Admin disabilita utente normale |          OK (atteso)          |
| Admin disabilita un altro admin |        **Consentito**         |
| Admin disabilita se stesso      | **Consentito** (self-lockout) |

#### Patch raccomandata

```text
UserBean targetUser = service.doRetrieveById(id);

// Blocco privilege escalation:
if (targetUser.isAdmin().equalsIgnoreCase("true")) {
    response.sendError(403, "Non puoi disabilitare un altro amministratore");
    return;
}

// Blocco self-lockout:
if (userAdmin.getId() == targetUser.getId()) {
    response.sendError(403, "Non puoi disabilitare te stesso");
    return;
}
```

---

### 13.2 NPE su Login anonimo

- **File:** `src/main/java/Controller/Login.java`
- **Test:** `LoginFunctionalTest`
- **CWE:** CWE-476 (NULL Pointer Dereference)
- **Severità:** Critica

#### Vulnerabilità

Il servlet `Login.doPost` esegue `user.isAdmin()` dopo aver recuperato l'utente dalla sessione, **senza verificare che `user != null`**. In caso di sessione scaduta o accesso anonimo, si verifica `NullPointerException` → **HTTP 500**.

#### Test

```java
@Test
@DisplayName("SECURITY: Login con sessione scaduta causa NPE")
void testLoginSessioneScaduta() {
    when(support.session.getAttribute("user")).thenReturn(null);
    when(support.request.getParameter("email")).thenReturn("test@test.com");
    when(support.request.getParameter("password")).thenReturn("password");

    Login servlet = new Login();
    assertThrows(Exception.class, () ->
                    support.invokeDoPost(servlet, support.request, support.response),
            "NPE atteso: user null senza check");
}
```

#### Scenario

| Scenario                                     | Comportamento attuale |
| :------------------------------------------- |:---------------------:|
| Login con credenziali valide                 |          OK           |
| Login con credenziali invalide               |   302 → `login.jsp`   |
| **Login con sessione scaduta + utente null** |     **500 (NPE)**     |

#### Patch raccomandata

```text
UserBean user = service.doRetrieveByEmailAndPassword(email, password);

if (user != null && user.isActive().equalsIgnoreCase("true")) {
    // ... login OK
} else {
    // Messaggio generico
}
```

---

### 13.3  FIXED — Quantità negative in `AddToCart`

- **File:** `src/main/java/Controller/AddToCart.java`
- **Test:** `AddToCartFunctionalTest.testQuantityNegativa`
- **CWE:** CWE-20 (Improper Input Validation)
- **Severità:** Critica
- **Stato:** RISOLTO

#### Vulnerabilità (storica)

Il servlet `AddToCart` accettava quantità negative nel parametro `quantity`. Un attaccante poteva inviare `quantity=-100` e manipolare il carrello.

#### Fix applicato

```text
boolean validQuantity = quantity > 0;
if (!validQuantity) {
        response.sendError(400); // NOSONAR
    return;
            }
```




---

### 13.4 Numero carta in chiaro nel database

- **File:** `src/main/java/Model/PaymentDAO.java`
- **Test:** `PaymentDAOFunctionalTest`
- **CWE:** CWE-312 (Cleartext Storage of Sensitive Information)
- **Severità:** Critica (violazione PCI-DSS)
- **Riferimento:** PCI-DSS v4.0 — Req. 3.4

#### Vulnerabilità

Il DAO salva il numero della carta in **chiaro** nel database. Se un attaccante compromette il DB (via SQL injection o accesso diretto), ottiene **numeri di carta validi** utilizzabili per frodi.

#### Test

```java
@Test
@DisplayName("FINDING PCI-DSS: numero carta in chiaro nel DB")
void testNumeroCartaInChiaro() throws Exception {
    PaymentBean payment = new PaymentBean();
    payment.setOrder(1);
    payment.setCardNumber("1234567890123456");
    payment.setCVV("123");
    payment.setDeadline("2030-12-31");
    payment.setHolder("Mario Rossi");

    dao.doSave(payment);

    // Verifica diretta in H2
    try (Connection con = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "");
         Statement st = con.createStatement();
         ResultSet rs = st.executeQuery("SELECT Numero_Carta FROM Pagamento WHERE Ordine=1")) {
        rs.next();
        assertThat(rs.getString(1))
            .as("FINDING PCI-DSS: numero carta in chiaro")
            .isEqualTo("1234567890123456");
    }
}
```

#### Scenario

| Scenario                     | Comportamento attuale | PCI-DSS |
| :--------------------------- | :-------------------: | :-----: |
| Admin accede al DB           | Vede numeri in chiaro |    ❌    |
| Attaccante compromette il DB | Ottiene numeri validi |    ❌    |
| Query del DB                 | `SELECT Numero_Carta` |    ❌    |

#### Patch raccomandata

**Tokenizzare** il numero carta tramite un payment gateway (Stripe, PayPal):

```text
// Prima:
ps.setString(3, payment.getCardNumber());

// Dopo:
ps.setString(3, stripeClient.tokenize(payment.getCardNumber())); // "tok_abc123"
```

Se la tokenizzazione non è praticabile, **cifrare con AES-256**:

```text
String encrypted = AesUtil.encrypt(payment.getCardNumber(), secretKey);
ps.setString(3, encrypted);
```

---

### 13.5 SHA-1 per password (non Bcrypt)

- **File:** `src/main/java/Model/UserBean.java`
- **Test:** `UserBeanFunctionalTest`
- **CWE:** CWE-916 (Use of Password Hash With Insufficient Computational Effort)
- **Severità:** Alta

#### Vulnerabilità

Il metodo `setPassword` usa **SHA-1**, un algoritmo veloce e non progettato per le password. SHA-1 non ha **salt**, quindi due utenti con la stessa password hanno lo stesso hash → rainbow tables funzionano.

#### Test

```java
@Test
@DisplayName("FINDING: SHA-1 invece di bcrypt")
void testDocumentSha1Usage() {
    user.setPassword("password123");
    assertThat(user.getPassword())
        .as("FINDING: SHA-1 è veloce e vulnerabile a brute-force. Raccomandato bcrypt/Argon2 (CWE-916).")
        .hasSize(40);
}
```

#### Scenario

| Scenario                     |    Comportamento attuale     |
| :--------------------------- |:----------------------------:|
| Due utenti con "password123" |         Stesso hash          |
| Attaccante ottiene il DB     |  Rainbow tables funzionano   |
| Attaccante usa GPU           | ~10 miliardi di hash/secondo |

#### Patch raccomandata

**Migrare a bcrypt** (work factor 12):

```xml
<dependency>
    <groupId>org.mindrot</groupId>
    <artifactId>jbcrypt</artifactId>
    <version>0.4</version>
</dependency>
```

```java
public void setPassword(String password) {
    this.password = BCrypt.hashpw(password, BCrypt.gensalt(12));
}

public boolean checkPassword(String plainPassword) {
    return BCrypt.checkpw(plainPassword, this.password);
}
```

**Richiede:**

* Aggiornamento DB: `ALTER TABLE Utente MODIFY Accesso VARCHAR(72)`
* Reset password degli utenti esistenti

---

### 13.6 NPE su Carrello Null

- **File:** `src/main/java/Controller/ShowCart.java`, `RemoveFromCart.java`, `ProfileAndCart.java`
- **Test:** `ShowCartFunctionalTest`, `RemoveFromCartFunctionalTest`, `ProfileAndCartFunctionalTest`
- **CWE:** CWE-476 (NULL Pointer Dereference)
- **Severità:** Alta

#### Vulnerabilità

Questi servlet accedono a `cartBean.getCartList()` **senza verificare che `cartBean != null`**. Se un utente accede a `/show-cart-servlet` **prima di aggiungere prodotti** al carrello, la sessione non ha l'attributo `"cart"` e si verifica NPE.

#### Test

```java
@Test
@DisplayName("SECURITY: ShowCart con carrello null causa NPE")
void testCarrelloNull() {
    when(support.session.getAttribute("cart")).thenReturn(null);

    ShowCart servlet = new ShowCart();
    assertThrows(Exception.class, () ->
                    support.invokeDoGet(servlet, support.request, support.response),
            "NPE atteso: cartBean null senza check");
}
```

#### Scenario

| Scenario                                                | Comportamento attuale |
| :------------------------------------------------------ |:---------------------:|
| Utente aggiunge prodotto e poi vede carrello            |          OK           |
| **Utente accede a `/show-cart-servlet` senza prodotti** |     **500 (NPE)**     |
| Utente fa logout e poi vede carrello                    |       500 (NPE)       |

#### Patch raccomandata

```text
CartBean cartBean = (CartBean) session.getAttribute("cart");

if (cartBean == null) {
    cartBean = new CartBean();  // Carrello vuoto
    session.setAttribute("cart", cartBean);
}

ArrayList<ProductCartBean> cartList = cartBean.getCartList();
// ... codice sicuro
```

---

### 13.7  FIXED — Loop Infinito in `ProductsHomepage`

- **File:** `src/main/java/Controller/ProductsHomepage.java`
- **Test:** `ProductViewTest.testDocumentInfiniteLoopInProductsHomepage`, `ProductsHomepageFunctionalTest`
- **CWE:** CWE-835 (Loop with Unreachable Exit Condition)
- **Severità:** Critica (Denial of Service)
- **Stato:**  RISOLTO

#### Vulnerabilità (storica)

Il servlet usava un ciclo `while (listProduct.size() != 12)` che **non terminava mai** se il database conteneva **meno di 12 prodotti**. Il thread della servlet si bloccava, esaurendo il pool di thread di Tomcat. Con DB vuoto, `rand.nextInt(0)` lanciava `IllegalArgumentException`.

#### Fix applicato

```text
private static final Random RANDOM = new Random();
private static final int HOMEPAGE_SIZE = 12;

// ...

ArrayList<ProductBean> allProducts = service.doRetrieveAll();

List<ProductBean> listProduct;
if (allProducts.isEmpty()) {
listProduct = Collections.emptyList();
} else {
        Collections.shuffle(allProducts, RANDOM);
int target = Math.min(HOMEPAGE_SIZE, allProducts.size());
listProduct = allProducts.subList(0, target);
}
```

---

### 13.8 CVV getter esposto

- **File:** `src/main/java/Model/PaymentBean.java`
- **Test:** `PaymentBeanFunctionalTest`
- **CWE:** CWE-200 (Exposure of Sensitive Information)
- **Severità:** Alta

#### Vulnerabilità

`PaymentBean` espone un **getter pubblico** per il CVV:

```java
public String getCVV() {
    return CVV;
}
```

Se il bean viene **loggato**, **serializzato in JSON**, o **incluso in una JSP**, il CVV può trapelare nei log o nelle risposte HTTP. **PCI-DSS vieta** di memorizzare o esporre il CVV dopo l'autorizzazione.

#### Test

```java
@Test
@DisplayName("FINDING: getter CVV pubblico")
void testCvvGetterExposed() {
    payment.setCVV("123");
    assertThat(payment.getCVV())
        .as("FINDING: getCVV() pubblico può far trapelare il CVV nei log")
        .isEqualTo("123");
}
```

#### Scenario

| Scenario                           |   Comportamento attuale    |
| :--------------------------------- |:--------------------------:|
| Bean serializzato in JSON          | CVV incluso nella risposta |
| Log di debug                       |    CVV visibile nei log    |
| JSP `c:out value="${payment.CVV}"` |    CVV esposto all'HTML    |

#### Patch raccomandata

**Rimuovere il getter CVV** (o marcarlo per esclusione dalla serializzazione):

```java
// Opzione 1: rimuovere il getter
// Non esporre mai il CVV dopo setCVV()

// Opzione 2: usare @JsonIgnore (se si usa Jackson)
@JsonIgnore
public String getCVV() {
    return CVV;
}

// Opzione 3: logging safe
@Override
public String toString() {
    return "PaymentBean{" +
            "order=" + order +
            ", cardNumber='****" + (cardNumber != null ? cardNumber.substring(cardNumber.length()-4) : "") + '\'' +
            // CVV mai loggato
            ", holder='" + holder + '\'' +
            '}';
}
```

---


### Fix 2 — Sezione 13.9 (tabella riepilogo corretta)


| #  | Finding                             |   CWE   | Severità | Test                                                                                     |    Stato    |
| :- | :---------------------------------- | :-----: |:--------:|:-----------------------------------------------------------------------------------------|:-----------:|
| 1  | Privilege Escalation `SetStateUser` | CWE-269 | Critica  | `SetStateUserFunctionalTest`                                                             | Documentato |
| 2  | NPE su Login anonimo                | CWE-476 | Critica  | `LoginFunctionalTest`                                                                    | Documentato |
| 3  | Quantità negative `AddToCart`       |  CWE-20 | Critica  | `AddToCartFunctionalTest`                                                                |  **FIXED**  |
| 4  | NPE prodotto inesistente `AddToCart`| CWE-476 | Critica  | `AddToCartFunctionalTest`                                                                |  **FIXED**  |
| 5  | Numero carta in chiaro (PCI-DSS)    | CWE-312 | Critica  | `PaymentDAOFunctionalTest`                                                               | Documentato |
| 6  | SHA-1 per password                  | CWE-916 |   Alta   | `UserBeanFunctionalTest`                                                                 | Documentato |
| 7  | NPE su carrello null                | CWE-476 |   Alta   | `ShowCartFunctionalTest`, `RemoveFromCartFunctionalTest`, `ProfileAndCartFunctionalTest` | Documentato |
| 8  | Loop infinito `ProductsHomepage`    | CWE-835 | Critica  | `ProductsHomepageFunctionalTest`, `ProductViewTest`                                      |  **FIXED**  |
| 9  | CVV getter esposto                  | CWE-200 |   Alta   | `PaymentBeanFunctionalTest`                                                              | Documentato |
| 10 | Bug `removeProduct`                 | CWE-20  |   Media  | `CartBeanFunctionalTest`, `BusinessLogicTest`                                            |  **FIXED**  |
| 11 | Boundary bug filtro prezzi          | CWE-20  |   Media  | `BusinessLogicTest`, `ProductCatalogTest`                                                |  **FIXED**  |

## 14. Changelog

### 07/10/2026 — Remediation iterazione 1

**Fix applicati:**

| # | Area | File | Descrizione |
| :--- | :--- | :--- | :--- |
| 1 | A04 | `AddToCart.java` | Aggiunto try/catch su `Integer.parseInt(productId)` |
| 2 | A04 | `AddToCart.java` | Aggiunta validazione `quantity > 0` |
| 3 | A04 | `AddToCart.java` | Aggiunto `return;` dopo ogni `sendError` |
| 4 | A04 | `AddToCart.java` | Aggiunto null check su `productBean` con `sendError(404)` |
| 5 | A04 | `CartBean.java` | `removeProduct` non rimuove più elementi se ID inesistente |
| 6 | A04 | `ProductDAO.java` | Filtro prezzi usa `>=` e `<=` invece di `>` e `<` |
| 7 | A05 | `UserDAO.java` | `PreparedStatement` al posto di `Statement` (già fixato) |
| 8 | A05 | `ProductDAO.java` | Try-with-resources per `PreparedStatement` e `ResultSet` |
| 9 | A05 | `CartDAO.java` | Try-with-resources per `PreparedStatement` e `ResultSet` |

**Test aggiornati:**

| File | Test | Modifica |
| :--- | :--- | :--- |
| `AddToCartFunctionalTest.java` | `testQuantityNegativa` | Verifica `sendError(400)` invece del bug |
| `AddToCartFunctionalTest.java` | `testProdottoInesistente` | Verifica `sendError(404)` invece di NPE |
| `BusinessLogicTest.java` | `testDocumentFilterBoundaryBug` | Verifica `>=` e `<=` |
| `BusinessLogicTest.java` | `testRemoveNonExistentProductIsBuggy` | Aspetta `size == 2` |
| `CartBeanFunctionalTest.java` | `testRemoveNonexistentBug` | Aspetta `size == 2` |
| `CartManagementTest.java` | Vari test | Aggiornati al formato FIXED |

**Finding ancora aperti (non fixati in questa iterazione):**

- SHA-1 come algoritmo di hashing password (`UserBean.java`)
- CSRF su azioni GET (`SetAdmin`, `SetStateUser`, `EditProfile`)
- Privilege escalation e self-lockout in `SetStateUser`
- File upload senza restrizioni in `AddProduct`
- NPE in vari controller (Login, ShowCart, RemoveFromCart)
- Numero carta in chiaro (PCI-DSS)
- CVV getter esposto

**Nota metodologica:** i test funzionali sono stati aggiornati per verificare il **fix** e fungono da **regression protection**: se qualcuno reintroduce la vulnerabilità, il test fallisce.
### 08/10/2026 — Remediation iterazione 2

**Fix applicati:**

| # | Area | File | Descrizione |
| :--- | :--- | :--- | :--- |
| 1 | A04 | `ProductsHomepage.java` | Sostituito `while` con `Collections.shuffle()` + `Math.min` |
| 2 | A04 | `ProductsHomepage.java` | Gestione DB vuoto con `if (allProducts.isEmpty())` |
| 3 | A04 | `ProductsHomepage.java` | Rimosso `doRetrieveById(random)` → usa tutti i prodotti |
| 4 | A04 | `ProductsHomepage.java` | `RANDOM` static final (warning SonarQube) |

**Test aggiornati:**

| File | Modifica |
| :--- | :--- |
| `ProductViewTest.java` | 4 test aggiornati al formato FIXED (loop, DB vuoto, null product, Random) |
| `ProductsHomepageFunctionalTest.java` | Rimosso riferimento a "loop infinito documentato" |