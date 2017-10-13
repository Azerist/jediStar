package fr.jedistar.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import fr.jedistar.JediStarBotCommand;
import fr.jedistar.StaticVars;

public abstract class GuildUnitsSWGOHGGDataParser {

	private final static Logger logger = LoggerFactory.getLogger(GuildUnitsSWGOHGGDataParser.class);

	private final static String SQL_SELECT_CHARS_EXPIRATION = "SELECT expiration FROM characters LIMIT 1;";
	private final static String SQL_INSERT_CHARS = "INSERT INTO characters (name,baseID,url,image,power,description,combatType,expiration) VALUES (?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE baseID=VALUES(baseID),url=VALUES(url),image=VALUES(image),power=VALUES(power),description=VALUES(description),combatType=VALUES(combatType),expiration=VALUES(expiration);";
	
	private final static String SQL_SELECT_SHIPS_EXPIRATION = "SELECT expiration FROM ships LIMIT 1;";
	private final static String SQL_INSERT_SHIPS = "INSERT INTO ships (name,baseID,url,image,power,description,combatType,expiration) VALUES (?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE baseID=VALUES(baseID),url=VALUES(url),image=VALUES(image),power=VALUES(power),description=VALUES(description),combatType=VALUES(combatType),expiration=VALUES(expiration);";
	
	private final static String SQL_SELECT_GUILD_UNITS_EXPIRATION = "SELECT expiration FROM guildUnits WHERE guildID=? LIMIT 1";
	private final static String SQL_INSERT_GUILD_UNITS = "INSERT INTO guildUnits (guildID,player,charID,rarity,combatType,power,level,expiration) VALUES (?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE rarity=VALUES(rarity),combatType=VALUES(combatType),power=VALUES(power),level=VALUES(level),expiration=VALUES(expiration);";
	private final static String SQL_DELETE_EXPIRED_GUILD_UNITS = "DELETE FROM guildUnits WHERE guildID=? AND expiration<CURRENT_TIMESTAMP";
	
	private final static String CHARS_URI = "https://swgoh.gg/api/characters/?format=json";
	private final static String SHIPS_URI = "https://swgoh.gg/api/ships/?format=json";
	private final static String GUILD_UNITS_URI = "https://swgoh.gg/api/guilds/%d/units/";

	public static boolean parseCharacters() {
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		BufferedReader in = null;
		
		try {
			//Vérifier si une màj est nécessaire
			conn = StaticVars.getJdbcConnection();

			stmt = conn.prepareStatement(SQL_SELECT_CHARS_EXPIRATION);
			
			logger.debug("Executing query :"+stmt.toString());
			rs = stmt.executeQuery();
			
			boolean updateNeeded = true;
			
			if(rs.next()) {
				Date expiration = rs.getTimestamp("expiration");
				if(expiration.after(new Date())) {
					updateNeeded = false;
				}
			}
			
			if(!updateNeeded) {
				return true;
			}
			
			rs.close();
			stmt.close();
			
			//Charger l'API swgoh.gg
			URL url = new URL(CHARS_URI);
			URLConnection connection = url.openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			connection.connect();

			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			String json = in.readLine();

			JSONArray charsJson = new JSONArray(json);
			
			//Insérer les donn�es
			conn.setAutoCommit(false);
			stmt = conn.prepareStatement(SQL_INSERT_CHARS);
			
			Calendar expirationCal = Calendar.getInstance();
			expirationCal.add(Calendar.DAY_OF_MONTH, 1);
			java.sql.Timestamp expiration = new Timestamp(expirationCal.getTimeInMillis());
			
			for(int i=0;i<charsJson.length();i++) {
				JSONObject character = charsJson.getJSONObject(i);
				
				stmt.setString(1,character.getString("name"));
				stmt.setString(2, character.getString("base_id"));
				stmt.setString(3, character.getString("url"));
				stmt.setString(4, character.getString("image"));
				stmt.setInt(5, character.getInt("power"));
				stmt.setString(6, character.getString("description"));
				stmt.setInt(7, character.getInt("combat_type"));
				stmt.setTimestamp(8, expiration);
				
				stmt.addBatch();
			}
			
			logger.debug("Executing query :"+stmt.toString());
			stmt.executeBatch();
			conn.commit();
			conn.setAutoCommit(true);
		}
		catch(Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return false;
		}
		finally {

			try {
				if(rs != null) {
					rs.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}

		}
		
		return true;
	}
	
public static boolean parseShips() {
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		BufferedReader in = null;
		
		try {
			conn = StaticVars.getJdbcConnection();

			//Vérifier si une màj est nécessaire
			stmt = conn.prepareStatement(SQL_SELECT_SHIPS_EXPIRATION);
			
			logger.debug("Executing query :"+stmt.toString());
			rs = stmt.executeQuery();
			
			boolean updateNeeded = true;
			
			if(rs.next()) {
				Date expiration = rs.getTimestamp("expiration");
				if(expiration.after(new Date())) {
					updateNeeded = false;
				}
			}
			
			if(!updateNeeded) {
				return true;
			}
			
			rs.close();
			stmt.close();
			
			//Charger l'API swgoh.gg
			URL url = new URL(SHIPS_URI);
			URLConnection connection = url.openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			connection.connect();

			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			String json = in.readLine();

			JSONArray charsJson = new JSONArray(json);
			
			//Insérer les donn�es
			conn.setAutoCommit(false);
			stmt = conn.prepareStatement(SQL_INSERT_SHIPS);
			
			Calendar expirationCal = Calendar.getInstance();
			expirationCal.add(Calendar.DAY_OF_MONTH, 1);
			java.sql.Timestamp expiration = new Timestamp(expirationCal.getTimeInMillis());
			
			for(int i=0;i<charsJson.length();i++) {
				JSONObject character = charsJson.getJSONObject(i);
				
				stmt.setString(1,character.getString("name"));
				stmt.setString(2, character.getString("base_id"));
				stmt.setString(3, character.getString("url"));
				stmt.setString(4, character.getString("image"));
				stmt.setInt(5, character.getInt("power"));
				stmt.setString(6, character.getString("description"));
				stmt.setInt(7, character.getInt("combat_type"));
				stmt.setTimestamp(8, expiration);
				
				stmt.addBatch();
			}
			
			logger.debug("Executing query :"+stmt.toString());
			stmt.executeBatch();
			conn.commit();
			conn.setAutoCommit(true);
		}
		catch(Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return false;
		}
		finally {

			try {
				if(rs != null) {
					rs.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}

		}
		
		return true;
	}

public static boolean parseGuildUnits(Integer guildID) {
	
	if(guildID == null) {
		return false;
	}
	
	Connection conn = null;
	PreparedStatement stmt = null;
	ResultSet rs = null;
	BufferedReader in = null;
	
	try {
		conn = StaticVars.getJdbcConnection();

		//Vérifier si une màj est nécessaire
		stmt = conn.prepareStatement(SQL_SELECT_GUILD_UNITS_EXPIRATION);
		stmt.setInt(1, guildID);
		
		logger.debug("Executing query :"+stmt.toString());
		rs = stmt.executeQuery();
		
		boolean updateNeeded = true;
		
		if(rs.next()) {
			Date expiration = rs.getTimestamp("expiration");
			if(expiration.after(new Date())) {
				updateNeeded = false;
			}
		}
		
		if(!updateNeeded) {
			return true;
		}
		
		rs.close();
		stmt.close();
		
		//Charger l'API swgoh.gg
		String uri = String.format(GUILD_UNITS_URI, guildID);
		URL url = new URL(uri);
		URLConnection connection = url.openConnection();
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
		connection.connect();

		in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

		String json = in.readLine();

		JSONObject unitsJson = new JSONObject(json);
		
		//Insérer les donn�es
		conn.setAutoCommit(false);
		stmt = conn.prepareStatement(SQL_INSERT_GUILD_UNITS);
		
		Calendar expirationCal = Calendar.getInstance();
		expirationCal.add(Calendar.DAY_OF_MONTH, 1);
		java.sql.Timestamp expiration = new Timestamp(expirationCal.getTimeInMillis());
		
		Iterator<String> unitIDs = unitsJson.keys();
		
		while(unitIDs.hasNext()) {
			String unitID = unitIDs.next();
			JSONArray unitData = unitsJson.getJSONArray(unitID);
			
			for(int i=0;i<unitData.length();i++) {
				JSONObject playerData = unitData.getJSONObject(i);
				
				stmt.setInt(1,guildID);
				stmt.setString(2, playerData.getString("player"));
				stmt.setString(3, unitID);
				stmt.setInt(4, playerData.getInt("rarity"));
				stmt.setInt(5,  playerData.getInt("combat_type"));
				stmt.setInt(6, playerData.getInt("power"));
				stmt.setInt(7, playerData.getInt("level"));
				stmt.setTimestamp(8, expiration);
				
				stmt.addBatch();

			}
		}
		
		logger.debug("Executing query :"+stmt.toString());
		stmt.executeBatch();
		conn.commit();
		conn.setAutoCommit(true);
		
		rs.close();
		stmt.close();
		
		stmt = conn.prepareStatement(SQL_DELETE_EXPIRED_GUILD_UNITS);
		
		stmt.setInt(1,guildID);
		
		logger.debug("Executing query :"+stmt.toString());
		stmt.executeUpdate();
	}
	catch(Exception e) {
		logger.error(e.getMessage());
		e.printStackTrace();
		return false;
	}
	finally {

		try {
			if(rs != null) {
				rs.close();
			}
			if(stmt != null) {
				stmt.close();
			}
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

	}
	
	return true;
}
}