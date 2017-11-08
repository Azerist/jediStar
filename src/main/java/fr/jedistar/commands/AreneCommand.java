package fr.jedistar.commands;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.embed.EmbedBuilder;
import fr.jedistar.JediStarBotCommand;
import fr.jedistar.StaticVars;
import fr.jedistar.formats.CommandAnswer;

public class AreneCommand implements JediStarBotCommand {

	final static Logger logger = LoggerFactory.getLogger(JediStarBotCommand.class);
	
	private static final SortedMap<Integer, Integer> attackRangeMap = Collections.unmodifiableSortedMap(
		    new TreeMap<Integer, Integer>() {

				private static final long serialVersionUID = 4607362140163522483L;

			{
		        put(12, 4);
		        put(18, 5);
		        put(24, 6);
		        put(32, 7);
		        put(40, 8);
		        put(55, 9);
		    }});
	
	private final static Color EMBED_COLOR = Color.WHITE;
	
	private final String COMMAND;
	
	
	private static String HELP;
	private static String ERROR_MESSAGE ;
	private static String ERROR_MESSAGE_INCOHERENT_PARAM;
	private static String ERROR_MESSAGE_INVALID_RANK;
	private static String ERROR_MESSAGE_INCORRECT_NUMBER;
	private static String MESSAGES_FASTEST_PATH;
	private static String MESSAGES_HELP_US;
	private static String MESSAGES_FASTEST_PATH_TITLE;
	private static String MESSAGES_HELP_US_TITLE;
	
	
	
	
	private final static String JSON_MESSAGE_HELP = "help";
	private final static String JSON_ERROR_MESSAGE = "errorMessage";
	private final static String JSON_ARENE = "arenaCommandParameters";
	private final static String JSON_ARENE_COMMAND = "command";
	private final static String JSON_MESSAGES = "messages";
	private final static String JSON_MESSAGES_FASTEST_PATH = "fastestPath";
	private final static String JSON_MESSAGES_HELP_US = "helpUs";
	private final static String JSON_MESSAGES_FASTEST_PATH_TITLE = "fastestPathTitle";
	private final static String JSON_MESSAGES_HELP_US_TITLE = "helpUsTitle";
	private final static String JSON_ERROR_MESSAGES = "errorMessages";
	private final static String JSON_ERROR_MESSAGE_INCOHERENT_PARAM = "incoherentParams";
	private final static String JSON_ERROR_MESSAGE_INVALID_RANK = "invalidRank";
	private final static String JSON_ERROR_MESSAGE_INCORRECT_NUMBER = "incorrectNumber";

	private boolean isApproximation = false;
	
	
	public AreneCommand() {
		super();

		JSONObject parameters = StaticVars.jsonMessages;
		ERROR_MESSAGE = parameters.getString(JSON_ERROR_MESSAGE);

		//Param�tres propres � l'�quilibrage
		JSONObject AreneParams = parameters.getJSONObject(JSON_ARENE);

		COMMAND = AreneParams.getString(JSON_ARENE_COMMAND);

		//Messages
		JSONObject messages = AreneParams.getJSONObject(JSON_MESSAGES);
		HELP = messages.getString(JSON_MESSAGE_HELP);
		MESSAGES_FASTEST_PATH = messages.getString(JSON_MESSAGES_FASTEST_PATH);
		MESSAGES_HELP_US = messages.getString(JSON_MESSAGES_HELP_US);
		MESSAGES_FASTEST_PATH_TITLE = messages.getString(JSON_MESSAGES_FASTEST_PATH_TITLE);
		MESSAGES_HELP_US_TITLE = messages.getString(JSON_MESSAGES_HELP_US_TITLE);

		JSONObject errorMessages = AreneParams.getJSONObject(JSON_ERROR_MESSAGES);
		ERROR_MESSAGE_INCOHERENT_PARAM = errorMessages.getString(JSON_ERROR_MESSAGE_INCOHERENT_PARAM);
		ERROR_MESSAGE_INVALID_RANK = errorMessages.getString(JSON_ERROR_MESSAGE_INVALID_RANK);
		ERROR_MESSAGE_INCORRECT_NUMBER = errorMessages.getString(JSON_ERROR_MESSAGE_INCORRECT_NUMBER);

	}

	public CommandAnswer answer(List<String> params, Message messageRecu, boolean isAdmin) {
		
		isApproximation = false;
		if(params.size() == 0) {
			return new CommandAnswer(HELP,null);
		}
		if(params.size() == 1) {
			try {
				
				String arenaRankString = params.get(0);
				Integer arenaRank = Integer.parseInt(arenaRankString);
				
				return ArenaRoadToFirst(arenaRank);
				
			}
			catch(NumberFormatException e) {
				return error(ERROR_MESSAGE_INCORRECT_NUMBER);
			}
		}
		return error(ERROR_MESSAGE_INCOHERENT_PARAM);
	}
	
	private CommandAnswer ArenaRoadToFirst(Integer rank)
	{
		Integer currentRank = rank;
		Integer remainingFight = 5;
		String path =currentRank.toString();
		
		if(currentRank<1)
			return error(ERROR_MESSAGE_INVALID_RANK);
		
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(EMBED_COLOR);
		
		while(currentRank > 1 && remainingFight >0)
		{
			
			currentRank = GetMaxAccessibleRank(currentRank);
			path += " > " + currentRank;
			remainingFight--;
		}
		String message = String.format(MESSAGES_FASTEST_PATH,path);
		String messageTitle = String.format(MESSAGES_FASTEST_PATH_TITLE,rank);
		embed.addField(messageTitle, message, true);
		if(isApproximation)
		{
			embed.addField(MESSAGES_HELP_US_TITLE, MESSAGES_HELP_US, true);
		}
		return new CommandAnswer(null,embed);
	}
	
	private Integer GetMaxAccessibleRank(Integer rank)
	{
		Integer newRank =0;
		SortedMap<Integer, Integer> reachableRank = attackRangeMap.tailMap(rank);
		if(reachableRank.isEmpty())
		{
			newRank = (int) Math.round(rank*0.85) -1 ;
			if(newRank < attackRangeMap.get(attackRangeMap.lastKey()))
			{
				newRank =attackRangeMap.get(attackRangeMap.lastKey())+1;
			}
			isApproximation = true;
		}
		else
		{
			newRank = rank-reachableRank.get(reachableRank.firstKey());
		}
		if(newRank <=0)
		{
			newRank =1;
		}
		return newRank;
	}
	
	private CommandAnswer error(String errorMessage) {
		String message = ERROR_MESSAGE +"**"+ errorMessage + "**\r\n\r\n"+ HELP;
		
		return new CommandAnswer(message, null);
	}

	@Override
	public String getCommand() {
		return COMMAND;
	}

}
