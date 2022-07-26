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
-- Table structure for table `utente`
--

DROP TABLE IF EXISTS `utente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `utente` (
  `ID_Utente` int NOT NULL AUTO_INCREMENT,
  `Nome` varchar(20) NOT NULL,
  `Cognome` varchar(20) NOT NULL,
  `Data_Nascita` date NOT NULL,
  `Email` varchar(20) NOT NULL,
  `Accesso` varchar(40) NOT NULL,
  `Telefono` char(10) NOT NULL,
  `Citta` varchar(20) NOT NULL,
  `Provincia` char(2) NOT NULL,
  `Codice_Postale` char(5) NOT NULL,
  `Indirizzo` varchar(20) NOT NULL,
  `Data_Registrazione` date NOT NULL,
  `Stato` varchar(5) DEFAULT 'true',
  `Amministratore` varchar(5) DEFAULT 'false',
  PRIMARY KEY (`ID_Utente`),
  UNIQUE KEY `Email` (`Email`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `utente`
--

LOCK TABLES `utente` WRITE;
/*!40000 ALTER TABLE `utente` DISABLE KEYS */;
INSERT INTO `utente` VALUES (1,'Teodoro','Grauso','2001-10-12','teo@ecommerce.com','5cec175b165e3d5e62c9e13ce848ef6feac81bff','1234567890','Fisciano','SA','84040','Via, 1','2022-01-01','true','true'),(2,'Michele','Spinelli','2000-07-06','mike@ecommerce.com','5cec175b165e3d5e62c9e13ce848ef6feac81bff','1234567890','Fisciano','SA','84040','Via, 2','2022-01-01','true','true'),(3,'Junhuang','Chen','2001-02-11','chen@ecommerce.com','5cec175b165e3d5e62c9e13ce848ef6feac81bff','1234567890','Fisciano','SA','84040','Via, 3','2022-01-01','true','true'),(4,'Nome','Cognome','2022-07-07','utente@ecommerce.com','5cec175b165e3d5e62c9e13ce848ef6feac81bff','1234567890','Citta','SA','12345','Via, 5','2022-07-05','true','false');
/*!40000 ALTER TABLE `utente` ENABLE KEYS */;
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
