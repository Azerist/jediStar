package fr.jedistar.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.btobastian.javacord.entities.message.Message;
import fr.jedistar.JediStarBotCommand;
import fr.jedistar.StaticVars;
import fr.jedistar.formats.CommandAnswer;

public class TerritoryBattlesCommand implements JediStarBotCommand {

	final static Logger logger = LoggerFactory.getLogger(TerritoryBattlesCommand.class);

	private final String COMMAND;
	private final String COMMAND_PLATOON;
	private final String COMMAND_CHARS;
	private final String COMMAND_SHIPS;

	private final String HELP;

	private final String ERROR_MESSAGE;
	private final String ERROR_MESSAGE_SQL;
	private final String ERROR_MESSAGE_NO_CHANNEL;
	private final String ERROR_MESSAGE_NO_GUILD_NUMBER;
	private final String ERROR_MESSAGE_PARAMS_NUMBER;

	private final static String SQL_GUILD_ID = "SELECT guildID FROM guild WHERE serverID=?;";

	//Nom des champs dans le JSON
	private final static String JSON_ERROR_MESSAGE = "errorMessage";

	private final static String JSON_TB = "territoryBattlesCommandParams";

	private final static String JSON_TB_HELP = "help";

	private final static String JSON_TB_COMMANDS = "commands";
	private final static String JSON_TB_COMMANDS_BASE = "base";
	private final static String JSON_TB_COMMANDS_PLATOON = "platoon";
	private final static String JSON_TB_COMMANDS_CHARS = "characters";
	private final static String JSON_TB_COMMANDS_SHIPS = "ships";

	private final static String JSON_TB_ERROR_MESSAGES = "errorMessages";
	private final static String JSON_TB_ERROR_MESSAGES_SQL = "errorMessages";
	private final static String JSON_TB_ERROR_MESSAGES_NO_CHANNEL = "noChannel";
	private final static String JSON_TB_ERROR_MESSAGES_NO_GUILD = "noGuildNumber";
	private final static String JSON_TB_ERROR_MESSAGES_PARAMS_NUMBER = "paramsNumber";

	public TerritoryBattlesCommand() {

		JSONObject parameters = StaticVars.jsonSettings;

		ERROR_MESSAGE = parameters.getString(JSON_ERROR_MESSAGE);

		JSONObject tbParams = parameters.getJSONObject(JSON_TB);

		HELP = tbParams.getString(JSON_TB_HELP);

		JSONObject commands = tbParams.getJSONObject(JSON_TB_COMMANDS);
		COMMAND = commands.getString(JSON_TB_COMMANDS_BASE);
		COMMAND_PLATOON = commands.getString(JSON_TB_COMMANDS_PLATOON);
		COMMAND_CHARS = commands.getString(JSON_TB_COMMANDS_CHARS);
		COMMAND_SHIPS = commands.getString(JSON_TB_COMMANDS_SHIPS);

		JSONObject errorMessages = tbParams.getJSONObject(JSON_TB_ERROR_MESSAGES);
		ERROR_MESSAGE_SQL = errorMessages.getString(JSON_TB_ERROR_MESSAGES_SQL);
		ERROR_MESSAGE_NO_CHANNEL = errorMessages.getString(JSON_TB_ERROR_MESSAGES_NO_CHANNEL);
		ERROR_MESSAGE_NO_GUILD_NUMBER = errorMessages.getString(JSON_TB_ERROR_MESSAGES_NO_GUILD);
		ERROR_MESSAGE_PARAMS_NUMBER = errorMessages.getString(JSON_TB_ERROR_MESSAGES_PARAMS_NUMBER);
	}

	@Override
	public String getCommand() {
		return COMMAND;
	}

	@Override
	public CommandAnswer answer(List<String> params, Message receivedMessage, boolean isAdmin) {

		if(params.size() < 5) {
			return new CommandAnswer(ERROR_MESSAGE_PARAMS_NUMBER,null);
		}

		if(COMMAND_PLATOON.equals(params.get(0)) && COMMAND_PLATOON.equalsIgnoreCase(params.get(1))) {
			if(receivedMessage.getChannelReceiver() == null) {
				return new CommandAnswer(ERROR_MESSAGE_NO_CHANNEL,null);
			}

			Integer guildID = getGuildIDFromDB(receivedMessage);

			if(guildID == null) {
				return new CommandAnswer(ERROR_MESSAGE_SQL, null);
			}

			if(guildID == -1) {
				return new CommandAnswer(ERROR_MESSAGE_NO_GUILD_NUMBER,null);
			}
		}

		return null;
	}

	/**
	 * Gets the guild ID associated with this Discord server from the DB
	 * @param message
	 * @return
	 */
	private Integer getGuildIDFromDB(Message message) {

		String serverID = message.getChannelReceiver().getServer().getId();

		Connection conn = StaticVars.jdbcConnection;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = conn.prepareStatement(SQL_GUILD_ID);

			stmt.setString(1,serverID);

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

	private String error(String message) {
		return ERROR_MESSAGE +"**"+ message + "**\r\n\r\n"+ HELP;
	}
}
