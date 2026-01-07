-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: bistrorestaurant
-- ------------------------------------------------------
-- Server version	8.0.44

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
-- Table structure for table `bills`
--

DROP TABLE IF EXISTS `bills`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bills` (
  `bill_number` int NOT NULL,
  `total_price` decimal(10,2) NOT NULL,
  `discount_value` decimal(10,2) DEFAULT '0.00',
  `payment_date` date NOT NULL,
  `table_number` int NOT NULL,
  `subscriber_number` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`bill_number`),
  KEY `table_number` (`table_number`),
  KEY `subscriber_number` (`subscriber_number`),
  CONSTRAINT `bills_ibfk_1` FOREIGN KEY (`table_number`) REFERENCES `tables_info` (`table_number`) ON DELETE CASCADE,
  CONSTRAINT `bills_ibfk_2` FOREIGN KEY (`subscriber_number`) REFERENCES `subscribers` (`subscriber_number`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `monthly_reports`
--

DROP TABLE IF EXISTS `monthly_reports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `monthly_reports` (
  `report_id` int NOT NULL AUTO_INCREMENT,
  `period_start` date NOT NULL,
  `period_end` date NOT NULL,
  PRIMARY KEY (`report_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `opening_hours`
--

DROP TABLE IF EXISTS `opening_hours`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `opening_hours` (
  `id` int NOT NULL AUTO_INCREMENT,
  `weekday` enum('SUNDAY','MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY') NOT NULL,
  `opening_time` time NOT NULL,
  `closing_time` time NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `representatives`
--

DROP TABLE IF EXISTS `representatives`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `representatives` (
  `user_id` int NOT NULL,
  `representative_number` varchar(50) NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `representative_number` (`representative_number`),
  CONSTRAINT `representatives_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `reservations`
--

DROP TABLE IF EXISTS `reservations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservations` (
  `reservation_id` int NOT NULL AUTO_INCREMENT,
  `booking_date` date NOT NULL,
  `booking_time` time NOT NULL,
  `guest_count` int NOT NULL,
  `confirmation_code` varchar(20) NOT NULL,
  `reservation_status` enum('ACTIVE','CANCELLED','COMPLETED','NO_SHOW') DEFAULT 'ACTIVE',
  `table_number` int DEFAULT NULL,
  `subscriber_number` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`reservation_id`),
  UNIQUE KEY `confirmation_code` (`confirmation_code`),
  KEY `table_number` (`table_number`),
  KEY `subscriber_number` (`subscriber_number`),
  CONSTRAINT `reservations_ibfk_1` FOREIGN KEY (`table_number`) REFERENCES `tables_info` (`table_number`) ON DELETE SET NULL,
  CONSTRAINT `reservations_ibfk_2` FOREIGN KEY (`subscriber_number`) REFERENCES `subscribers` (`subscriber_number`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `special_hours`
--

DROP TABLE IF EXISTS `special_hours`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `special_hours` (
  `special_id` int NOT NULL AUTO_INCREMENT,
  `special_date` date NOT NULL,
  `opening_time` time DEFAULT NULL,
  `closing_time` time DEFAULT NULL,
  `closed_flag` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`special_id`),
  UNIQUE KEY `special_date` (`special_date`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `subscribers`
--

DROP TABLE IF EXISTS `subscribers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subscribers` (
  `user_id` int NOT NULL,
  `subscriber_number` varchar(50) NOT NULL,
  `membership_card` varchar(100) NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `subscriber_number` (`subscriber_number`),
  CONSTRAINT `subscribers_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tables_info`
--

DROP TABLE IF EXISTS `tables_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tables_info` (
  `table_number` int NOT NULL,
  `seat_capacity` int NOT NULL,
  `table_location` varchar(50) DEFAULT NULL,
  `table_status` enum('AVAILABLE','OCCUPIED') DEFAULT 'AVAILABLE',
  `reservation_start` timestamp NULL DEFAULT NULL,
  `reservation_end` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`table_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tags`
--

DROP TABLE IF EXISTS `tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tags` (
  `log_id` int NOT NULL AUTO_INCREMENT,
  `subscriber_number` varchar(50) NOT NULL,
  `activity_details` text NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`),
  KEY `subscriber_number` (`subscriber_number`),
  CONSTRAINT `tags_ibfk_1` FOREIGN KEY (`subscriber_number`) REFERENCES `subscribers` (`subscriber_number`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `email_address` varchar(120) NOT NULL,
  `phone_number` varchar(30) DEFAULT NULL,
  `user_password` varchar(100) NOT NULL,
  `user_role` enum('SUBSCRIBER','REPRESENTATIVE','MANAGER') NOT NULL,
  `account_status` tinyint(1) DEFAULT '1',
  `registration_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `email_address` (`email_address`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `waiting_list`
--

DROP TABLE IF EXISTS `waiting_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `waiting_list` (
  `entry_id` int NOT NULL AUTO_INCREMENT,
  `request_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `number_of_diners` int NOT NULL,
  `entry_code` varchar(20) NOT NULL,
  `subscriber_number` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`entry_id`),
  UNIQUE KEY `entry_code` (`entry_code`),
  KEY `subscriber_number` (`subscriber_number`),
  CONSTRAINT `waiting_list_ibfk_1` FOREIGN KEY (`subscriber_number`) REFERENCES `subscribers` (`subscriber_number`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-07 22:53:11
