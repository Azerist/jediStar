package fr.jedistar.commands;

import java.util.Collections;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.btobastian.javacord.entities.message.Message;
import fr.jedistar.JediStarBotCommand;
import fr.jedistar.StaticVars;
import fr.jedistar.formats.CommandAnswer;

public class AreneCommand implements JediStarBotCommand {

	final static Logger logger = LoggerFactory.getLogger(JediStarBotCommand.class);
	
	private static final SortedMap<Integer, Integer> attackRangeMap = Collections.unmodifiableSortedMap(
		    new TreeMap<Integer, Integer>() {{
		        put(12, 4);
		        put(18, 5);
		        put(24, 6);
		        put(33, 7);
		    }});
	
	public static final String COMMAND = "arene";
	
	
	private static String HELP;
	private static String ERROR_MESSAGE ;
	private static String ERROR_MESSAGE_INCOHERENT_PARAM;
	private static String ERROR_MESSAGE_INCORRECT_NUMBER;
	private static String MESSAGES_FASTEST_PATH;
	
	
	
	private final static String JSON_MESSAGE_HELP = "help";
	private final static String JSON_ERROR_MESSAGE = "errorMessage";
	private final static String JSON_ARENE_COMMAND = "areneCommandParameters";
	private final static String JSON_MESSAGES = "messages";
	private final static String JSON_MESSAGES_FASTEST_PATH = "fastestPath";
	private final static String JSON_ERROR_MESSAGES = "errorMessages";
	private final static String JSON_ERROR_MESSAGE_INCOHERENT_PARAM = "incoherentParams";
	private final static String JSON_ERROR_MESSAGE_INCORRECT_NUMBER = "incorrectNumber";

	
	
	public AreneCommand() {
		super();
		
		//Lecture du Json
		try {
			JSONObject parameters = StaticVars.jsonSettings;
			ERROR_MESSAGE = parameters.getString(JSON_ERROR_MESSAGE);

			//Paramètres propres à l'équilibrage
			JSONObject AreneParams = parameters.getJSONObject(JSON_ARENE_COMMAND);
			
			//Messages
			JSONObject messages = AreneParams.getJSONObject(JSON_MESSAGES);
			HELP = messages.getString(JSON_MESSAGE_HELP);
			MESSAGES_FASTEST_PATH = messages.getString(JSON_MESSAGES_FASTEST_PATH);
			
			JSONObject errorMessages = AreneParams.getJSONObject(JSON_ERROR_MESSAGES);
			ERROR_MESSAGE_INCOHERENT_PARAM = errorMessages.getString(JSON_ERROR_MESSAGE_INCOHERENT_PARAM);
			ERROR_MESSAGE_INCORRECT_NUMBER = errorMessages.getString(JSON_ERROR_MESSAGE_INCORRECT_NUMBER);
		}
		catch(JSONException e) {
			logger.error("JSON parameters file is incorrectly formatted");
			e.printStackTrace();
		}
		
	}

	public CommandAnswer answer(List<String> params, Message messageRecu, boolean isAdmin) {
		
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
		while(currentRank > 1 && remainingFight >0)
		{
			
			currentRank = GetMaxAccessibleRank(currentRank);
			path += " > " + currentRank;
			remainingFight--;
		}
		String message = String.format(MESSAGES_FASTEST_PATH,path);
		return new CommandAnswer(message,null);
	}
	
	private Integer GetMaxAccessibleRank(Integer rank)
	{
		Integer newRank =0;
		SortedMap<Integer, Integer> reachableRank =attackRangeMap.tailMap(rank);
		if(reachableRank.isEmpty())
		{
			newRank = rank-(attackRangeMap.get(attackRangeMap.lastKey())+1);
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

}
