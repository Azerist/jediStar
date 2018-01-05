CREATE DATABASE IF NOT EXISTS jedistar CHARACTER SET UTF8;

CREATE USER IF NOT EXISTS 'jedistar'@'localhost' IDENTIFIED BY 'JeDiStArBoT';

GRANT ALL ON jedistar.* TO 'jedistar'@'localhost';

USE jedistar;

CREATE TABLE IF NOT EXISTS guild
(
	channelID VARCHAR(64) PRIMARY KEY,
	guildID INT NOT NULL
);	

CREATE TABLE IF NOT EXISTS characters
(
	name VARCHAR(64) PRIMARY KEY,
	baseID VARCHAR(64),
	url VARCHAR(128),
	image VARCHAR(128),
	power INTEGER,
	description VARCHAR(512),
	combatType INTEGER,
	expiration TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ships
(
	name VARCHAR(64) PRIMARY KEY,
	baseID VARCHAR(64),
	url VARCHAR(128),
	image VARCHAR(128),
	power INTEGER,
	description VARCHAR(512),
	combatType INTEGER,
	expiration TIMESTAMP
);

CREATE TABLE IF NOT EXISTS guildUnits
(
	guildID INTEGER NOT NULL,
	player VARCHAR(32) NOT NULL,
	charID VARCHAR(64) NOT NULL,
	rarity INTEGER,
	combatType INTEGER,
	power INTEGER,
	level INTEGER,
	expiration TIMESTAMP,
	PRIMARY KEY (guildID,player,charID)
);

CREATE TABLE IF NOT EXISTS commandHistory
(
	command VARCHAR(32),
	ts TIMESTAMP,
	userID VARCHAR(64),
	userName VARCHAR(32) NOT NULL,
	serverID VARCHAR(128),
	serverName VARCHAR(128),
	serverRegion VARCHAR(64),
	PRIMARY KEY (command,ts,userID)
);

CREATE TABLE IF NOT EXISTS payoutTime
(
	channelID varchar(64),
	userName varchar(64),
	payoutTime TIME,
	flag varchar(32),
	swgohggLink varchar(256),
	PRIMARY KEY (channelID,userName)
);

DELIMITER //

CREATE PROCEDURE copyPayouts (IN source VARCHAR(64),IN dest VARCHAR(64))
BEGIN
	DROP TABLE IF EXISTS temp;
	CREATE TEMPORARY TABLE temp AS SELECT * FROM payoutTime WHERE channelID=source;
	UPDATE temp set channelID=dest;
	REPLACE INTO payoutTime SELECT * FROM temp;
	DROP TABLE temp;
END//

DELIMITER ;