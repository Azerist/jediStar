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

CREATE TABLE IF NOT EXISTS raid
(
	raidName varchar(16) PRIMARY KEY,
	raidAliases varchar(64)
);

CREATE TABLE IF NOT EXISTS balancingGuildRules
(
	guildID INT NOT NULL,
	raidName varchar(16),
	textualRules varchar(2000),
	minRaidsForPodium INT NOT NULL DEFAULT 15,
	PRIMARY KEY (guildID,raidName),
	FOREIGN KEY (raidName) REFERENCES raid(raidName)
);

CREATE TABLE IF NOT EXISTS balancingGuildRanks
(
	guildID INT,
	raidName varchar(16),
	rankID INT NOT NULL,
	rankName varchar(32) NOT NULL,
	rankWidth INT NOT NULL,
	PRIMARY KEY (guildID,raidName,rankID),
	FOREIGN KEY (guildID,raidName) REFERENCES balancingGuildRules(guildID,raidName)
);

CREATE TABLE IF NOT EXISTS balancingUserValues
(
	userID varchar(64) NOT NULL,
	raidName varchar(16),
	guildID INT,
	podiums INT NOT NULL DEFAULT 0,
	withoutPodium INT NOT NULL DEFAULT 0,
	targetRank INT,
	PRIMARY KEY (userID,raidName,guildID),
	FOREIGN KEY (guildID,raidName) REFERENCES balancingGuildRules(guildID,raidName)
);

CREATE TABLE IF NOT EXISTS balancingUserValuesPerRank
(
	userID varchar(64),
	raidName varchar(16),
	guildID INT,
	rankID INT,
	userValue INT NOT NULL DEFAULT 0,
	PRIMARY KEY (userID,raidName,guildID,rankID),
	FOREIGN KEY (userID,raidName,guildID) REFERENCES balancingUserValues(userID,raidName,guildID),
	FOREIGN KEY (guildID,raidName,rankID) REFERENCES balancingGuildRanks(guildID,raidName,rankID)
);

CREATE TABLE IF NOT EXISTS balancingActionHistory
(
	guildID INT NOT NULL,
	userID varchar(64) NOT NULL,
	raidName varchar(16) NOT NULL,
	ts TIMESTAMP,
	actionName varchar(64),
	payload varchar(128)
);