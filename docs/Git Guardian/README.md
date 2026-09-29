# GitGuardian — Secret Scanning

**Strumento:** GitGuardian (ggshield)
**Tipo di analisi:** Rilevamento di segreti hardcoded (password) nel codice sorgente e nella cronologia Git.
**Data di integrazione:** 28/09/2026


---

## 1. Configurazione e integrazione

La repository `notjuary/HardwareZone` è stata collegata al tool GitGuardian e sottoposta a una scansione iniziale completa. Successivamente, l'integrazione è stata resa automatica tramite GitHub Actions.

- **Workflow:** `.github/workflows/gitguardian.yml`
- **Azione:** `GitGuardian/ggshield-action@v1`
- **Trigger:** Push sui branch `main`/`master` e apertura/sincronizzazione di Pull Request.
- **Autenticazione:** GitHub Actions Secret `GITGUARDIAN_API_KEY` (token con scope minimo `scan`).
- **Permessi:** `contents: read` (permessi minimi per ridurre la superficie d'attacco della pipeline).

Il workflow esegue una scansione ad ogni push, analizzando i nuovi commit e segnalando eventuali segreti introdotti.

---

## 2. Processo di scansione e rilevamento

Il processo di identificazione della vulnerabilità si è svolto in due fasi distinte:

### Fase 1 — Scansione iniziale (incidente rilevato)

La repository è stata collegata a GitGuardian e scansionata per la prima volta. La scansione ha **immediatamente generato un incidente critico**: GitGuardian ha rilevato una **password in chiaro** all'interno del file `src/main/java/Model/ConPool.java`, nel commit `8fcd1fd`.

**Dettagli dell'alert:**
- **File coinvolto:** `src/main/java/Model/ConPool.java`
- **Commit:** `8fcd1fd`
- **Segreto rilevato:** password dell'utente `root` del database MySQL 
- **Stato:** Open → successivamente Resolved

L'analisi manuale del codice ha poi confermato che la stessa password era replicata anche in `docker-compose.yaml`, in due punti distinti (variabile `MYSQL_PASSWORD` del servizio `tomcat` e `MYSQL_ROOT_PASSWORD` del servizio `db`).

### Fase 2 — Remediation e ripristino

Dopo la conferma del rilevamento, si è proceduto al **fix definitivo**:
- Rimozione della password da `ConPool.java`.
- Rimozione della password da `docker-compose.yaml` (sostituita con la sintassi `${VARIABILE}`).
- Esternalizzazione della configurazione tramite variabili d'ambiente.
- Rotazione della password compromessa sul database.
- Chiusura dell'incidente sulla dashboard GitGuardian.

---

## 3. Vulnerabilità rilevate e prioritizzazione

La scansione ha prodotto due alert distinti, entrambi classificati come **critici**:

| ID | Strumento | File | Categoria (CWE) | Severità (CVSS) | Priorità |
|:---|:---|:---|:---|:---|:---|
| SEC-01 | GitGuardian | `src/main/java/Model/ConPool.java` | CWE-798: Use of Hard-coded Credentials | 9.8 (Critica) | P0 — Immediata | 
| SEC-02 | GitGuardian | `docker-compose.yaml` | CWE-798: Use of Hard-coded Credentials (2 occorrenze) | 9.8 (Critica) | P0 — Immediata | 

**Prioritizzazione:** entrambe le vulnerabilità sono state classificate come **P0** perché una credenziale in chiaro fornisce accesso diretto al database, con impatto completo su riservatezza, integrità e disponibilità dei dati. Essendo entrambe di severità critica, sono state risolte contestualmente, senza dilazione.

---

## 4. Dettaglio delle vulnerabilità

### SEC-01 — Credenziale hardcoded in `ConPool.java`

**Descrizione.** La password dell'utente `root` del database MySQL era inserita in chiaro nel metodo `getConnection()`, alla riga `p.setPassword("teograuso01")`.

**Categoria.** CWE-798: Use of Hard-coded Credentials (sotto-categoria CWE-259: Use of Hard-coded Password).

**Impatto.** Chiunque avesse accesso alla repository (o alla sua cronologia Git) avrebbe potuto leggere la password e ottenere accesso completo al database, con possibilità di lettura, modifica e cancellazione dei dati.

### SEC-02 — Credenziale hardcoded in `docker-compose.yaml`

**Descrizione.** La stessa password era replicata in due punti del file `docker-compose.yaml`:
- `MYSQL_PASSWORD` (servizio `tomcat`)
- `MYSQL_ROOT_PASSWORD` (servizio `db`)

**Categoria.** CWE-798: Use of Hard-coded Credentials.

**Impatto.** Analogo a SEC-01, con l'aggravante che la password era visibile anche nella configurazione dell'infrastruttura, non solo nel codice applicativo.

---

## 5. Processo di remediation

1. **Rimozione dal codice sorgente.** La password è stata eliminata da `ConPool.java` e da `docker-compose.yaml`.
2. **Esternalizzazione della configurazione.** Il codice Java è stato modificato per leggere host, porta, database, utente e password tramite `System.getenv()` (`MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`), applicando il principio **fail-fast**: se `MYSQL_PASSWORD` non è impostata, l'applicazione lancia un'eccezione e non parte.
3. **Configurazione locale sicura.** Le variabili sono fornite tramite un file `.env`, aggiunto a `.gitignore` per impedirne il commit.
4. **Configurazione CI/CD sicura.** I valori sono stati salvati come GitHub Actions Secrets (`MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`, `MYSQL_ROOT_PASSWORD`) e iniettati nei job tramite un file `.env` generato al volo sul runner.
5. **Rotazione della credenziale.** La password compromessa è stata cambiata sul database, invalidando il valore esposto nella cronologia Git.
6. **Chiusura dell'incidente.** Gli alert su GitGuardian sono stati marcati come `Resolved` con motivazione *"Credential rotated and removed from source code"*.

---

## 6. Verifica

- **GitHub Actions:** Il workflow viene eseguito automaticamente ad ogni push; i log mostrano il messaggio `No secrets have been found` sull'ultimo commit.
- **Dashboard GitGuardian:** La repository risulta `Monitored`, con `Open incidents: 0` e `Last scan: Successful`.
- **Test funzionale:** L'applicazione si avvia correttamente con le variabili d'ambiente (in locale tramite `.env`, in CI/CD tramite i GitHub Secrets), confermando che la connessione al database avviene tramite la configurazione esternalizzata.
- **Docker Compose:** `docker compose up -d --build` completa correttamente; il container Tomcat si connette al container MySQL usando le credenziali passate via variabili d'ambiente.



