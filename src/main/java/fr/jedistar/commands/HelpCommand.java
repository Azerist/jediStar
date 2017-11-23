package fr.jedistar.commands;

import de.btobastian.javacord.entities.message.Message;
import fr.jedistar.JediStarBotCommand;
import fr.jedistar.StaticVars;
import fr.jedistar.exception.HelpParamException;
import fr.jedistar.formats.CommandAnswer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class HelpCommand implements JediStarBotCommand {

	private static final Logger LOGGER = LoggerFactory.getLogger(JediStarBotCommand.class);

	private final static String JSON_ERROR_MESSAGE = "errorMessage";
	private final static String JSON_HELP_COMMAND = "helpCommandParameters";
	private final static String JSON_HELP_COMMAND_COMMAND = "command";
	private static final String MODS=StaticVars.jsonMessages.getJSONObject("modsCommandParameters").getString(JSON_HELP_COMMAND_COMMAND);
	private static final String RAID=StaticVars.jsonMessages.getJSONObject("raidCommandParameters").getString(JSON_HELP_COMMAND_COMMAND);
	private static final String ARENE=StaticVars.jsonMessages.getJSONObject("arenaCommandParameters").getString(JSON_HELP_COMMAND_COMMAND);
	private static final String EQUILIBRAGE=StaticVars.jsonMessages.getJSONObject("balancingCommandParameters").getJSONObject("commands").getString(JSON_HELP_COMMAND_COMMAND);
	private static final String TB=StaticVars.jsonMessages.getJSONObject("territoryBattlesCommandParams").getJSONObject("commands").getString("base");
	private static final String PAYOUTS=StaticVars.jsonMessages.getJSONObject("payoutCommandParameters").getJSONObject("commands").getString("main");

	private final static String JSON_HELP_MESSAGES = "messages";
	private static final String JSON_HELP_MESSAGE_INTRO_MESSAGE = "introMessage";
	private static final String JSON_HELP_MESSAGE_MODS_MESSAGE = "modsMessage";
	private static final String JSON_HELP_MESSAGE_RAID_MESSAGE = "raidMessage";
	private static final String JSON_HELP_MESSAGE_EQUILIBRAGE_MESSAGE = "equilibrageMessage";
	private static final String JSON_HELP_MESSAGE_ARENA_MESSAGE = "arenaMessage";
	private static final String JSON_HELP_MESSAGE_TB_MESSAGE = "tbMessage";
	private static final String JSON_HELP_MESSAGE_PAYOUTS_MESSAGE = "payoutsMessage";
	private static final String JSON_HELP_MESSAGE_SMALL_MODS_MESSAGE = "smallModsMessage";
	private static final String JSON_HELP_MESSAGE_SMALL_RAID_MESSAGE = "smallRaidMessage";
	private static final String JSON_HELP_MESSAGE_SMALL_EQUILIBRAGE_MESSAGE = "smallEquilibrageMessage";
	private static final String JSON_HELP_MESSAGE_SMALL_ARENA_MESSAGE = "smallArenaMessage";
	private static final String JSON_HELP_MESSAGE_SMALL_TB_MESSAGE = "smallTbMessage";
	private static final String JSON_HELP_MESSAGE_SMALL_PAYOUTS_MESSAGE = "smallPayoutsMessage";

	private final static String JSON_HELP_ERROR_MESSAGES = "errorMessages";
	private final static String JSON_HELP_ERROR_MESSAGES_PARAMS_ERROR = "paramsError";
	private final static String JSON_HELP_ERROR_MESSAGES_TECHNICAL_ERROR = "technicalError";

	private static String COMMAND;
	private static String ERROR_MESSAGE;
	private static String INTRO_MESSAGE;
	private static String MODS_MESSAGE;
	private static String RAID_MESSAGE;
	private static String ARENA_MESSAGE;
	private static String EQUILIBRAGE_MESSAGE;
	private static String TB_MESSAGE;
	private static String PAYOUTS_MESSAGE;
	private static String SMALL_MODS_MESSAGE;
	private static String SMALL_RAID_MESSAGE;
	private static String SMALL_ARENA_MESSAGE;
	private static String SMALL_EQUILIBRAGE_MESSAGE;
	private static String SMALL_TB_MESSAGE;
	private static String SMALL_PAYOUTS_MESSAGE;
	private static String PARAMS_ERROR;
	private static String TECHNICAL_ERROR;

	public HelpCommand() {
		super();

		JSONObject parameters = StaticVars.jsonMessages;

		//messages de base
		ERROR_MESSAGE = parameters.getString(JSON_ERROR_MESSAGE);

		JSONObject modsParams = parameters.getJSONObject(JSON_HELP_COMMAND);

		COMMAND = modsParams.getString(JSON_HELP_COMMAND_COMMAND);

		//Messages
		JSONObject messages = modsParams.getJSONObject(JSON_HELP_MESSAGES);
		INTRO_MESSAGE = messages.getString(JSON_HELP_MESSAGE_INTRO_MESSAGE);
		MODS_MESSAGE = messages.getString(JSON_HELP_MESSAGE_MODS_MESSAGE);
		RAID_MESSAGE = messages.getString(JSON_HELP_MESSAGE_RAID_MESSAGE);
		EQUILIBRAGE_MESSAGE = messages.getString(JSON_HELP_MESSAGE_EQUILIBRAGE_MESSAGE);
		ARENA_MESSAGE = messages.getString(JSON_HELP_MESSAGE_ARENA_MESSAGE);
		TB_MESSAGE = messages.getString(JSON_HELP_MESSAGE_TB_MESSAGE);
		PAYOUTS_MESSAGE = messages.getString(JSON_HELP_MESSAGE_PAYOUTS_MESSAGE);
		SMALL_MODS_MESSAGE = messages.getString(JSON_HELP_MESSAGE_SMALL_MODS_MESSAGE);
		SMALL_RAID_MESSAGE = messages.getString(JSON_HELP_MESSAGE_SMALL_RAID_MESSAGE);
		SMALL_ARENA_MESSAGE = messages.getString(JSON_HELP_MESSAGE_SMALL_ARENA_MESSAGE);
		SMALL_EQUILIBRAGE_MESSAGE = messages.getString(JSON_HELP_MESSAGE_SMALL_EQUILIBRAGE_MESSAGE);
		SMALL_TB_MESSAGE = messages.getString(JSON_HELP_MESSAGE_SMALL_TB_MESSAGE);
		SMALL_PAYOUTS_MESSAGE = messages.getString(JSON_HELP_MESSAGE_SMALL_PAYOUTS_MESSAGE);

		//Messages d'erreur
		JSONObject errorMessages = modsParams.getJSONObject(JSON_HELP_ERROR_MESSAGES);
		PARAMS_ERROR = errorMessages.getString(JSON_HELP_ERROR_MESSAGES_PARAMS_ERROR);
		TECHNICAL_ERROR = errorMessages.getString(JSON_HELP_ERROR_MESSAGES_TECHNICAL_ERROR);
	}


	@Override
	public CommandAnswer answer(List<String> params, Message messageRecu, boolean isAdmin) {
		CommandAnswer response = new CommandAnswer("", null);
		try {
			if(params.size() == 0){
				response.setMessage(constructFullMessage());
			}else{
				response.setMessage(constructAppropriateMessage(params.get(0)));
			}
		} catch (HelpParamException e) {
			response.setMessage(PARAMS_ERROR);
		}
		return response;
	}

	private String constructAppropriateMessage(String s) throws HelpParamException {
		StringBuilder sb = new StringBuilder();
		sb.append(INTRO_MESSAGE);
		sb.append("\r\n");
		if (s.equals(MODS)) {
			sb.append(MODS_MESSAGE);
		} else if (s.equals(ARENE)) {
			sb.append(ARENA_MESSAGE);
		} else if (s.equals(EQUILIBRAGE)) {
			sb.append(EQUILIBRAGE_MESSAGE);
		} else if (s.equals(RAID)) {
			sb.append(RAID_MESSAGE);
		} else if (s.equals(TB)) {
			sb.append(TB_MESSAGE);
		} else if (s.equals(PAYOUTS)) {
			sb.append(PAYOUTS_MESSAGE);
		}else {
			throw new HelpParamException();
		}

		return sb.toString();
	}

	private String constructFullMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append(INTRO_MESSAGE);
		sb.append("\r\n");
		sb.append(SMALL_MODS_MESSAGE);
		sb.append("\r\n\n\n");
		sb.append(SMALL_RAID_MESSAGE);
		sb.append("\r\n\n\n");
		//sb.append(SMALL_EQUILIBRAGE_MESSAGE);
		//sb.append("\r\n\n\n");
		sb.append(SMALL_ARENA_MESSAGE);
		sb.append("\r\n\n\n");
		sb.append(SMALL_TB_MESSAGE);
		sb.append("\r\n\n\n");
		sb.append(SMALL_PAYOUTS_MESSAGE);

		return sb.toString();
	}

	@Override
	public String getCommand() {
		return COMMAND;
	}
}
