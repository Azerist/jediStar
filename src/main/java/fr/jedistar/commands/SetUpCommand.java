/**
 * 
 */
package fr.jedistar.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiManager;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.impl.ImplReaction;
import fr.jedistar.JediStarBotCommand;
import fr.jedistar.StaticVars;
import fr.jedistar.formats.CommandAnswer;
import fr.jedistar.formats.PendingAction;
import fr.jedistar.listener.JediStarBotReactionAddListener;

/**
 * @author Jerem
 *
 */
public class SetUpCommand implements JediStarBotCommand {

	final static Logger logger = LoggerFactory.getLogger(SetUpCommand.class);

	private final String COMMAND;
	private final String COMMAND_GUILD_NUMBER;
	
	private final String CONFIRM_UPDATE_GUILD;
	private final String SETUP_GUILD_OK;	
	private final String CANCEL_MESSAGE;
	
	private final String ERROR_MESSAGE;
	private final String HELP;
	private final String FORBIDDEN;
	private final String PARAMS_NUMBER;
	private final String NUMBER_PROBLEM;
	private final String ERROR_NO_CHAN;
	private final String SQL_ERROR;	
	private final String NO_COMMAND_FOUND;
	
	//Requ�tes SQL
	private static final String SELECT_GUILD_REQUEST = "SELECT * FROM guild WHERE channelID=?";
	private static final String INSERT_GUILD_REQUEST = "INSERT INTO guild VALUES (?,?);";
	private static final String UPDATE_GUILD_REQUEST = "UPDATE guild SET guildID=? WHERE channelID=?;";


	//Nom des champs JSON
	private final static String JSON_ERROR_MESSAGE = "errorMessage";

	private static final String JSON_SETUP = "setUpCommandParameters";
	
	private static final String JSON_SETUP_HELP = "help";
	
	private static final String JSON_SETUP_COMMANDS = "commands";
	private static final String JSON_SETUP_COMMANDS_BASE = "base";
	private static final String JSON_SETUP_COMMANDS_GUILD_NUMBER = "guildNumber";
	
	private static final String JSON_SETUP_MESSAGES = "messages";
	private static final String JSON_SETUP_MESSAGES_CONFIRM_UPDATE_GUILD = "confirmUpdateGuild";
	private static final String JSON_SETUP_MESSAGES_GUILD_SETUP_OK = "guildSetupOK";
	private static final String JSON_SETUP_MESSAGES_CANCEL = "cancelAction";
	
	private static final String JSON_SETUP_ERROR_MESSAGES = "errorMessages";
	private static final String JSON_SETUP_ERROR_MESSAGES_FORBIDDEN = "forbidden";
	private static final String JSON_SETUP_ERROR_MESSAGES_PARAMS_NUMBER = "paramsNummber";
	private static final String JSON_SETUP_ERROR_MESSAGES_INCORRECT_NUMBER = "incorrectNumber";
	private static final String JSON_SETUP_ERROR_NO_CHAN = "noChannel";
	private static final String JSON_SETUP_ERROR_SQL = "sqlError";
	private static final String JSON_SETUP_ERROR_NO_COMMAND = "noCommandFound";


	public SetUpCommand() {
		//Lecture du JSON
		JSONObject params = StaticVars.jsonSettings;

		ERROR_MESSAGE = params.getString(JSON_ERROR_MESSAGE);

		JSONObject setupParams = params.getJSONObject(JSON_SETUP);

		HELP = setupParams.getString(JSON_SETUP_HELP);

		JSONObject commands = setupParams.getJSONObject(JSON_SETUP_COMMANDS);		
		COMMAND = commands.getString(JSON_SETUP_COMMANDS_BASE);
		COMMAND_GUILD_NUMBER = commands.getString(JSON_SETUP_COMMANDS_GUILD_NUMBER);
		
		JSONObject messages = setupParams.getJSONObject(JSON_SETUP_MESSAGES);
		CONFIRM_UPDATE_GUILD = messages.getString(JSON_SETUP_MESSAGES_CONFIRM_UPDATE_GUILD);
		SETUP_GUILD_OK = messages.getString(JSON_SETUP_MESSAGES_GUILD_SETUP_OK);
		CANCEL_MESSAGE = messages.getString(JSON_SETUP_MESSAGES_CANCEL);
		
		JSONObject errorMessages = setupParams.getJSONObject(JSON_SETUP_ERROR_MESSAGES);
		FORBIDDEN = errorMessages.getString(JSON_SETUP_ERROR_MESSAGES_FORBIDDEN);
		PARAMS_NUMBER = errorMessages.getString(JSON_SETUP_ERROR_MESSAGES_PARAMS_NUMBER);
		NUMBER_PROBLEM = errorMessages.getString(JSON_SETUP_ERROR_MESSAGES_INCORRECT_NUMBER);
		ERROR_NO_CHAN = errorMessages.getString(JSON_SETUP_ERROR_NO_CHAN);
		SQL_ERROR = errorMessages.getString(JSON_SETUP_ERROR_SQL);
		NO_COMMAND_FOUND = errorMessages.getString(JSON_SETUP_ERROR_NO_COMMAND);
	}

	@Override
	public String getCommand() {
		return COMMAND;
	}


	@Override
	public CommandAnswer answer(List<String> params, Message receivedMessage, boolean isAdmin) {

		if(!isAdmin) {
			return new CommandAnswer(FORBIDDEN, null);
		}
		
		if(params.size() < 2) {
			return new CommandAnswer(PARAMS_NUMBER,null);
		}
		
		if(COMMAND_GUILD_NUMBER.equalsIgnoreCase(params.get(0))) {
			
			if(receivedMessage.getChannelReceiver() == null) {
				return new CommandAnswer(ERROR_NO_CHAN, null);
			}
			try {
				Integer guildID = Integer.parseInt(params.get(1));
				
				return registerNewGuild(receivedMessage,guildID);
			}
			catch(NumberFormatException e) {
				logger.warn(e.getMessage());
				e.printStackTrace();
				return new CommandAnswer(error(NUMBER_PROBLEM), null);
			}
		}
		
		
		return new CommandAnswer(error(NO_COMMAND_FOUND),null);
		
	}

	/**
	 * Registers a new guild inside the database.
	 * @param serverID
	 * @param guildID
	 * @param receivedMessage
	 * @return
	 */
	private CommandAnswer registerNewGuild(Message receivedMessage,Integer guildID) {

		String channelID = receivedMessage.getChannelReceiver().getId();
		Integer existingGuildID = checkIfChannelExists(channelID);
		
		//Erreur SQL
		if(existingGuildID != null && existingGuildID == -1) {
			return new CommandAnswer(SQL_ERROR, null);
		}
		
		//Guilde existante, on affiche un avertissement
		if(existingGuildID != null && existingGuildID > 0) {
			
			if(existingGuildID.equals(guildID)) {
				return new CommandAnswer(SETUP_GUILD_OK,null);
			}
			
			JediStarBotReactionAddListener.addPendingAction(new PendingAction(receivedMessage.getAuthor(),"executeUpdate",this,receivedMessage,1,channelID,guildID));
			String emojiX = EmojiManager.getForAlias("x").getUnicode();
			String emojiV = EmojiManager.getForAlias("white_check_mark").getUnicode();

			return new CommandAnswer(String.format(CONFIRM_UPDATE_GUILD,existingGuildID),null,emojiV,emojiX);
		}
		
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			//Guilde inexistante, on l'ins�re
			if(existingGuildID == null) {
				conn = StaticVars.getJdbcConnection();
				stmt = conn.prepareStatement(INSERT_GUILD_REQUEST);
				
				stmt.setString(1, channelID);
				stmt.setInt(2, guildID);
				
				logger.debug("Executing query : "+stmt.toString());
				stmt.executeUpdate();
				
				return new CommandAnswer(SETUP_GUILD_OK,null);
			}
		}
		catch(SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return new CommandAnswer(SQL_ERROR,null);
		}
		finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
				}
			}
		}
		
		return null;
	}

	/**
	 * Checks in the databse if data already exist for this server
	 * @param serverID
	 * @return
	 */
	private Integer checkIfChannelExists(String channelID) {
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = StaticVars.getJdbcConnection();

			stmt = conn.prepareStatement(SELECT_GUILD_REQUEST);
			
			stmt.setString(1,channelID);
			
			logger.debug("Executing query : "+stmt.toString());

			rs = stmt.executeQuery();
			
			if(rs.next()) {
				return rs.getInt("guildID");
			}
			else {
				return null;
			}
		}
		catch(SQLException e) {
			logger.error(e.getMessage());
			return -1;
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
				e.printStackTrace();
				logger.error(e.getMessage());
			}

		}
	}
	
	/**
	 * Executes an update query on the database
	 * @param serverID
	 * @param guildID
	 * @return
	 */
	public String executeUpdate(ImplReaction reaction,String channelID,Integer guildID) {

		String emojiX = EmojiManager.getForAlias("x").getUnicode();
		String emojiV = EmojiManager.getForAlias("white_check_mark").getUnicode();

		if(emojiX.equals(reaction.getUnicodeEmoji())) {
			return CANCEL_MESSAGE;
		}

		if(emojiV.equals(reaction.getUnicodeEmoji())) {
			Connection conn = null;
			PreparedStatement stmt = null;

			try {
				conn = StaticVars.getJdbcConnection();

				stmt = conn.prepareStatement(UPDATE_GUILD_REQUEST);

				stmt.setInt(1, guildID);
				stmt.setString(2, channelID);

				logger.debug("Executing query : "+stmt.toString());

				stmt.executeUpdate();

				return SETUP_GUILD_OK;
			}
			catch(SQLException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
				return SQL_ERROR;
			}
			finally {
				if(stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						logger.error(e.getMessage());
					}
				}
			}
		}
		return null;
	}
	
	private String error(String message) {
		return ERROR_MESSAGE +"**"+ message + "**\r\n\r\n"+ HELP;
	}
}
