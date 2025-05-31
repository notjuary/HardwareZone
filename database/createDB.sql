DROP DATABASE ecommerce;
CREATE DATABASE ecommerce;

USE ecommerce;

CREATE TABLE Utente (
	ID_Utente INT AUTO_INCREMENT PRIMARY KEY,
    Nome VARCHAR(20) NOT NULL,
    Cognome VARCHAR(20) NOT NULL,
    Data_Nascita DATE NOT NULL,
    Email VARCHAR(20) UNIQUE NOT NULL,
    Accesso VARCHAR(40) NOT NULL,
    Telefono CHAR(10) NOT NULL,
    Citta VARCHAR(20) NOT NULL,
    Provincia CHAR(2) NOT NULL,
    Codice_Postale CHAR(5) NOT NULL,
    Indirizzo VARCHAR(20) NOT NULL,
    Data_Registrazione DATE NOT NULL,
    Stato VARCHAR(5) DEFAULT "true",
    Amministratore VARCHAR(5) DEFAULT "false"
);

CREATE TABLE Prodotto (
	ID_Prodotto INT AUTO_INCREMENT PRIMARY KEY,
    Nome VARCHAR(20) NOT NULL,
    Descrizione VARCHAR(255) NOT NULL,
    Prezzo DOUBLE(10, 2) NOT NULL,
    Quantita_Disponibile INT NOT NULL DEFAULT 0,
    Sconto INT NOT NULL DEFAULT 0,
    Immagine VARCHAR(255) NOT NULL,
    Categoria VARCHAR(20) NOT NULL
);

CREATE TABLE Ordine (
	ID_Ordine INT AUTO_INCREMENT PRIMARY KEY,
    Utente INT REFERENCES Utente(ID_Utente),
    Totale DOUBLE(10, 2) NOT NULL
);

CREATE TABLE Ordine_Prodotto (
	Prodotto INT NOT NULL,
    Quantità INT NOT NULL,
    Prezzo DOUBLE(10, 2),
    Ordine INT REFERENCES Ordine(ID_Ordine)
);

CREATE TABLE Pagamento (
	ID_Pagamento INT AUTO_INCREMENT PRIMARY KEY,
    Ordine INT REFERENCES Ordine(ID_Ordine),
    Data_Pagamento DATE NOT NULL,
    Numero_Carta CHAR(16) NOT NULL,
    CVV CHAR(3) NOT NULL,
    Scadenza DATE NOT NULL,
    Titolare_Carta VARCHAR(60) NOT NULL
);

CREATE TABLE Carrello (
	Prodotto INT NOT NULL REFERENCES Prodotto(ID_Prodotto),
    Quantità INT NOT NULL,
    Utente INT NOT NULL REFERENCES Utente(ID_Utente)
);

CREATE TABLE Categoria (
    Nome_Categoria VARCHAR(20) PRIMARY KEY
);

-- Inserimento utenti
INSERT INTO Utente (Nome, Cognome, Data_Nascita, Email, Accesso, Telefono, Citta, Provincia, Codice_Postale, Indirizzo, Data_Registrazione, Stato, Amministratore)
VALUES
    ("Teodoro","Grauso","2001-10-12","teo@ecommerce.com","5cec175b165e3d5e62c9e13ce848ef6feac81bff","1234567890","Fisciano","SA","84040","Via 1","2022-01-01","true","true"),
    ("Michele","Spinelli","2000-07-06","mike@ecommerce.com","5cec175b165e3d5e62c9e13ce848ef6feac81bff","1234567890","Fisciano","SA","84040","Via 2","2022-01-01","true","true"),
    ("Junhuang","Chen","2001-02-11","chen@ecommerce.com","5cec175b165e3d5e62c9e13ce848ef6feac81bff","1234567890","Fisciano","SA","84040","Via 3","2022-01-01","true","true");

-- Inserimento categorie corrette (senza spazi extra)
INSERT INTO Categoria (Nome_Categoria) VALUES
                                           ('Alimentatore'), ('All In One'), ('Case'), ('Dissipatore liquido'),
                                           ('HDD'), ('Monitor'), ('Processore'), ('RAM'), ('Scheda Madre'),
                                           ('Scheda Video'), ('SSD'), ('SSD Sata');

-- Inserimento prodotti con dati coerenti
INSERT INTO Prodotto (Nome, Descrizione, Prezzo, Quantita_Disponibile, Sconto, Immagine, Categoria) VALUES
                                                                                                        ('AMD Ryzen 5 5600X', 'Processore AMD Ryzen 5 5600X 6 Core 3.7GHz 32MB skAM4 Box', 305.00, 12, 30, './img/products/B08166SLDF.png', 'Processore'),
                                                                                                        ('WD Black SN850', 'WD BLACK SN850 500 GB NVMe SSD, Tecnologia PCIe Gen4', 121.90, 10, 0, './img/products/B08KFN1KT1.png', 'SSD'),
                                                                                                        ('Inno3D RTX 3060', 'INNO3D GeForce RTX 3060 Twin X2 LHR 12G', 492.99, 1, 0, './img/products/B09Z8CVJF3.png', 'Scheda Video'),
                                                                                                        ('MSI MAG B660M', 'Scheda Madre Micro-ATX per Intel 12th Gen Core', 178.90, 5, 0, './img/products/B09NTPPSBP.png', 'Scheda Madre');

-- Inserimento ordini con riferimenti validi agli utenti
INSERT INTO Ordine (Utente, Totale) VALUES
                                        (1, 134.99),
                                        (2, 259.99);

-- Inserimento prodotti negli ordini
INSERT INTO Ordine_Prodotto (Prodotto, Quantità, Prezzo, Ordine) VALUES
                                                                     (3, 1, 492.99, 1),
                                                                     (4, 2, 178.90, 2);

-- Inserimento pagamenti
INSERT INTO Pagamento (Ordine, Data_Pagamento, Numero_Carta, CVV, Scadenza, Titolare_Carta) VALUES
                                                                                                (1, '2022-07-23', '1234567890123456', '123', '2025-07-24', 'Teodoro Grauso'),
                                                                                                (2, '2022-07-23', '9876543210987654', '321', '2026-05-30', 'Michele Spinelli');

