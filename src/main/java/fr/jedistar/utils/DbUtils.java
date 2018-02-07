package fr.jedistar.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.btobastian.javacord.entities.message.Message;
import fr.jedistar.StaticVars;
import fr.jedistar.commands.TerritoryBattlesCommand;

public class DbUtils {

	final static Logger logger = LoggerFactory.getLogger(DbUtils.class);

	private final static String SQL_GUILD_ID = "SELECT guildID FROM guild WHERE channelID=?;";

	/**
	 * Gets the guild ID associated with this Discord server from the DB
	 * @param message
	 * @return
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
}
