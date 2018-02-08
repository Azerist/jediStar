package fr.jedistar.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.btobastian.javacord.entities.message.Message;
import fr.jedistar.StaticVars;
import fr.jedistar.commands.TerritoryBattlesCommand;
import fr.jedistar.formats.Unit;

public class DbUtils {

	final static Logger logger = LoggerFactory.getLogger(DbUtils.class);

	private final static String SQL_GUILD_ID = "SELECT guildID FROM guild WHERE channelID=?;";
	private final static String SQL_FIND_UNIT = "SELECT * FROM characters WHERE baseID=? UNION ALL SELECT * FROM ships WHERE baseID=?";
	private final static String SQL_FIND_EXPIRATION_DATE = "SELECT MAX(expiration) as expiration FROM guildUnits WHERE guildID=?";

	private final static Map<String,Unit> unitsCache = new HashMap<String,Unit>();
	


	/**
	 * Gets the guild ID associated with this Discord server from the DB
	 * @param message
	 * @return The guild ID if found, -1 if not found, null if error
	 */
	public static Integer getGuildIDFromDB(Message message) {

		String channelID = message.getChannelReceiver().getId();

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			conn = StaticVars.getJdbcConnection();

			stmt = conn.prepareStatement(SQL_GUILD_ID);

			stmt.setString(1,channelID);

			logger.debug("Executing query : "+stmt.toString());

			rs = stmt.executeQuery();

			if(rs.next()) {
				return rs.getInt("guildID");
			}
			return -1;
		}
		catch(SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return null;
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
			}
		}
	}
	
	/**
	 * Finds a unit in database given its ID
	 * @param unitID The id of the unit we're looking for
	 * @return the unit if found, null if sql error
	 */
	public static Unit findUnitByID(String unitID) {
		
		//Check cache
		Unit unitFromCache = unitsCache.get(unitID);
		if(unitFromCache != null && unitFromCache.getExpiration().after(Calendar.getInstance())) {
			return unitFromCache;
		}
		
		//If not found in cache, go look in database
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			conn = StaticVars.getJdbcConnection();

			stmt = conn.prepareStatement(SQL_FIND_UNIT);
			
			stmt.setString(1,unitID);
			stmt.setString(2,unitID);

			logger.debug("Executing query : "+stmt.toString());
			rs = stmt.executeQuery();
			
			//If unit name found, put it into cache and return it
			if(rs != null && rs.next()) {

				Unit unit = new Unit();
				
				unit.setName(rs.getString("name"));
				unit.setBaseID(rs.getString("baseID"));
				unit.setUrl(rs.getString("url"));
				unit.setImage(rs.getString("image"));
				unit.setPower(rs.getInt("power"));
				unit.setDescription(rs.getString("description"));
				unit.setCombatType(rs.getInt("combatType"));
				
				Calendar expiration = Calendar.getInstance();
				expiration.setTime(rs.getTimestamp("expiration"));
				
				unit.setExpiration(expiration);
				
				unitsCache.put(unitID, unit);
				
				return unit;
			}
				
			return null;
		}
		catch(SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return null;
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
			}
		}
	}
	
	/**
	 * Finds the last update date for guild data
	 * @param guildID
	 * @return the date, null if error
	 */
	public static Calendar findLastUpdateDateForGuild(Integer guildID) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = StaticVars.getJdbcConnection();

			stmt = conn.prepareStatement(SQL_FIND_EXPIRATION_DATE);
	
			stmt.setInt(1, guildID);
			
			logger.debug("Executing query : "+stmt.toString());

			rs = stmt.executeQuery();

			Calendar result = Calendar.getInstance();
			
			if(rs == null || !rs.next() || rs.getDate("expiration")==null ) {
				return null;
			}
			
			result.setTime(rs.getDate("expiration"));
			
			result.add(Calendar.HOUR, -24);
			
			return result;
		}
		catch(SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return null;
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
			}
		}
	}
}
