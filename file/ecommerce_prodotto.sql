-- MySQL dump 10.13  Distrib 8.0.29, for Win64 (x86_64)
--
-- Host: localhost    Database: ecommerce
-- ------------------------------------------------------
-- Server version	8.0.29

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `prodotto`
--

DROP TABLE IF EXISTS `prodotto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `prodotto` (
  `ID_Prodotto` int NOT NULL AUTO_INCREMENT,
  `Nome` varchar(20) NOT NULL,
  `Descrizione` varchar(255) NOT NULL,
  `Prezzo` double(10,2) NOT NULL,
  `Quantità_Disponibile` int NOT NULL DEFAULT '0',
  `Sconto` int NOT NULL DEFAULT '0',
  `Immagine` varchar(255) NOT NULL,
  `Categoria` varchar(20) NOT NULL,
  PRIMARY KEY (`ID_Prodotto`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `prodotto`
--

LOCK TABLES `prodotto` WRITE;
/*!40000 ALTER TABLE `prodotto` DISABLE KEYS */;
INSERT INTO `prodotto` VALUES (1,'AMD Ryzen 5 5600X','Processore AMD Ryzen 5 5600X 6 Core 3.7GHz 32MB skAM4 Box',305.00,12,30,'./img/products/B08166SLDF.png','Processore'),(2,'WD Black SN850','WD BLACK SN850 500 GB NVMe SSD Internal Gaming, Tecnologia PCIe Gen4, velocità di lettura fino a 7.000 MB/s, M.2 2280, 3D NAND',121.90,0,0,'./img/products/B08KFN1KT1.png','SSD'),(3,'Inno3D RTX 3060','INNO3D GeForce RTX 3060 Twin X2 LHR 12G',492.99,1,0,'./img/products/B09Z8CVJF3.png','Scheda Video'),(4,'MSI MAG B660M','MSI MAG B660M BAZOOKA DDR4, Scheda Madre Micro-ATX - Supporta i processori Intel 12th Gen Core, LGA 1700 - DDR4 Memory Boost 4800+MHz/OC, PCIe 4.0 x16 Slot, 2 x M.2 Gen4 Slot, 2.5G LAN',178.90,0,0,'./img/products/B09NTPPSBP.png','Scheda Madre'),(5,'Kingston Beast 32GB','Kingston FURY Beast RGB 32GB (2x16GB) 3200MHz DDR4 CL16 Memoria Gaming Kit per Computer Fissi Kit da 2, KF432C16BB1AK2/32',149.90,0,0,'./img/products/B097HQPZRG.png','RAM'),(6,'Corsair Vengeance 32','Corsair Vengeance RGB PRO TUF Edition 32 GB (2x16 GB) DDR4 3200 (PC4-25600) C16 - Nero',147.50,0,0,'./img/products/B08HVL3DVJ.png','RAM'),(7,'Intel i5-12600KF','Processore Intel Core i5-12600KF – Alder Lake, 10 core, 4.90 GHz Turbo, Socket LGA1700, 20 MB Cache, UHD 770, Box',349.91,1,12,'./img/products/B09FXFJW2F.png','Processore'),(8,'DeepCool CC560','Case DeepCool CC560 – Mid-Tower ATX, finestra a battente in vetro temperato, 4 ventole integrate A-RGB',77.99,0,0,'./img/products/B09PMJX88Y.png','Case'),(9,'Kingston A400 240GB','Kingston A400 SSD Unità a stato solido interne 2.5\" SATA Rev 3.0, 480GB - SA400S37/480G',33.90,18,0,'./img/products/B01N5IB20Q.png','SSD Sata'),(10,'MSI Modern AM241','PC Desktop All in One MSI Modern AM241 11M-012EU – 23.8″ (60cm) IPS Non-touch, Core i5-1135G7, 2.4GHz, 2.8GHz, Quad Core, 8GB(8GB*1), SSD 512GB*1, Grafica Integrata, Windows 10 Home',840.55,0,0,'./img/products/B08YFJ2DMF.png','All In One'),(11,'WD Blue 1TB 3.5\"','Hard disk WD Blue 1TB 3,5″ Sata 6 GB/s 7200rpm 64 Mb Cache',41.89,38,13,'./img/products/B0088PUEPK.png','HDD'),(12,'MSI X570-A Pro','Scheda Madre MSI X570-A Pro – Chipset AMD X570, Socket AM4, ATX',159.91,5,0,'./img/products/B07TDST84Q.png','Scheda Madre'),(13,'MSI K240','Dissipatore a liquido MSI MPG CORELIQUID K240 – Ventole 2x120mm, 1x60mm, Display LCD 2,4″, LED personalizzabili',224.99,0,30,'./img/products/B08WJCKVZ4.png','Dissipatore liquido'),(14,'DeepCool 360EX','Dissipatore a liquido DeepCool CASTLE 360EX tre ventole A-RGB da 120mm',149.99,0,10,'./img/products/B07TYM5T17.png','Dissipatore liquido'),(15,'BenQ EX2780Q','Benq Ex2780Q Monitor Gaming 27 Pollici, Ips, 2K, 144 Hz, Hdr 400, ‎Bronzo',399.99,0,20,'./img/products/B07X8HBHJP.png','Monitor'),(16,'Cooler Master MWE','Cooler Master Alimentatore MWE 650 White 230V V2, Spina UE - 80 PLUS 230V Certificato UE, Ventola 120 HDB Silenziosa, Circuito DC-to-DC + LLC con Singola Linea +12V - 3 Anni di Garanzia',77.90,18,20,'./img/products/B07VHFDVK2.png','Alimentatore ');
/*!40000 ALTER TABLE `prodotto` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-07-24  9:51:25
