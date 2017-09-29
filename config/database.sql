CREATE DATABASE IF NOT EXISTS jedistar;

CREATE USER IF NOT EXISTS 'jedistar'@'localhost' IDENTIFIED BY 'JeDiStArBoT';

GRANT ALL ON jedistar.* TO 'jedistar'@'localhost';

USE jedistar;

CREATE TABLE IF NOT EXISTS guild
(
	serverID BIGINT PRIMARY KEY,
	guildID INT
);	