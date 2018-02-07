package fr.jedistar.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import de.btobastian.javacord.entities.message.Message;
import fr.jedistar.JediStarBotCommand;
import fr.jedistar.StaticVars;
import fr.jedistar.formats.CommandAnswer;
import fr.jedistar.utils.DbUtils;

public class GuildReportCommand implements JediStarBotCommand{

	private final String COMMAND;
	
	private final String ERROR_FORBIDDEN;
	private final String ERROR_GUILD_NOT_FOUND;
	private final String ERROR_NO_CHANNEL;

	
	private final static String SQL_FIND_GUILD_UNITS = "SELECT player,power,rarity,level,charID FROM guildUnits WHERE guildID=?";
	private final static String SQL_FIND_CHAR_NAME = "SELECT name FROM characters WHERE baseID=?";
	private final static String SQL_FIND_SHIP_NAME = "SELECT name FROM ships WHERE baseID=?";
	
	private Map<String,String> unitNamesCache = new HashMap<String,String>();
	
	//Nom des champs JSON
	private final static String JSON_GUILD_REPORT = "guildReportCommandParameters";
	
	private final static String JSON_GUILD_REPORT_COMMANDS = "commands";
	private final static String JSON_GUILD_REPORT_COMMAND_MAIN = "main";
	
	private final static String JSON_GUILD_REPORT_ERRORS = "errorMessages";
	private final static String JSON_GUILD_REPORT_ERROR_FORBIDDEN = "forbidden";
	private final static String JSON_GUILD_REPORT_ERROR_NO_GUILD_NUMBER = "noGuildNumber";
	private final static String JSON_GUILD_REPORT_ERROR_NO_CHANNEL = "noChannel";

	public GuildReportCommand() {
		super();
		
		//Lire le Json
		JSONObject parameters = StaticVars.jsonMessages;
		
		JSONObject guildReportParams = parameters.getJSONObject(JSON_GUILD_REPORT);
		
		JSONObject commandsParams = guildReportParams.getJSONObject(JSON_GUILD_REPORT_COMMANDS);
		COMMAND = commandsParams.getString(JSON_GUILD_REPORT_COMMAND_MAIN);
		
		JSONObject errorMessages = guildReportParams.getJSONObject(JSON_GUILD_REPORT_ERRORS);
		ERROR_FORBIDDEN = errorMessages.getString(JSON_GUILD_REPORT_ERROR_FORBIDDEN);
		ERROR_GUILD_NOT_FOUND = errorMessages.getString(JSON_GUILD_REPORT_ERROR_NO_GUILD_NUMBER);
		ERROR_NO_CHANNEL = errorMessages.getString(JSON_GUILD_REPORT_ERROR_NO_CHANNEL);
	}
	
	@Override
	public String getCommand() {
		return COMMAND;
	}

	@Override
	public CommandAnswer answer(List<String> params, Message receivedMessage, boolean isAdmin) {
		
		if(!isAdmin) {
			return new CommandAnswer(ERROR_FORBIDDEN, null);
		}
		
		if(receivedMessage.getChannelReceiver() == null) {
			return new CommandAnswer(ERROR_NO_CHANNEL,null);
		}
		
		Integer guildID = DbUtils.getGuildIDFromDB(receivedMessage);
		
		generateGuildReport (guildID,receivedMessage);
		
		return null;
	}

	private void generateGuildReport(Integer guildID, Message receivedMessage) {
		// TODO Auto-generated method stub
		
	}

	
}
