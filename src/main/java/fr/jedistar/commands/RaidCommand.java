package fr.jedistar.commands;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.Message;
import fr.jedistar.JediStarBotCommand;
import fr.jedistar.Main;
import fr.jedistar.StaticVars;
import fr.jedistar.formats.CommandAnswer;

public class RaidCommand implements JediStarBotCommand {
	
	final static Logger logger = LoggerFactory.getLogger(JediStarBotCommand.class);

	public static String COMMAND;

	private static String MESSAGE_PERCENTS_DIFFERENCE;
	private static String MESSAGE_PERCENTS_DIFFERENCE_PHASECHANGE;
	private static String MESSAGE_DAMAGES;
	private static String MESSAGE_PERCENT;
	private static String MESSAGE_TARGET;
	
	private static String ERROR_MESSAGE ;
	private static String HELP;
	
	private static String OBJECTIVE_OVER_RAID_END;
	private static String INCOHERENT_PARAMETERS;
	private static String INCORRECT_NUMBER;
	private static String PHASE_NOT_FOUND;
	private static String INCORRECT_PARAMS_NUMBER;
	private static String RAID_NOT_FOUND;
			
	//Repr�sente 1% de HP pour les diff�rentes phases des diff�rents raids
	private Map<String,Map<Integer,Integer>> phaseHPmap;
	private Map<String,List<String>> aliasesMap;
	
	//Variables JSON
	private final static String JSON_ERROR_MESSAGE = "errorMessage";
	private final static String JSON_RAID_COMMAND = "raidCommandParameters";
	private final static String JSON_RAID_COMMAND_COMMAND = "command";
	private final static String JSON_MESSAGES = "messages";
	private final static String JSON_MESSAGE_PERCENTS_DIFFERENCE = "percentDifference";
	private final static String JSON_MESSAGE_PERCENTS_DIFFERENCE_PHASE_CHANGE = "percentDifferencePhaseChange";
	private final static String JSON_MESSAGE_DAMAGES = "damages";
	private final static String JSON_MESSAGE_PERCENT = "percent";
	private final static String JSON_MESSAGE_TARGET = "target";
	private final static String JSON_MESSAGE_HELP = "help";
	
	private final static String JSON_ERROR_MESSAGES = "errorMessages";
	private final static String JSON_ERROR_MESSAGE_OVER_RAID_END = "overRaidEnd";
	private final static String JSON_ERROR_MESSAGE_INCOHERENT_PARAMS = "incoherentParams";
	private final static String JSON_ERROR_MESSAGE_INCORRECT_NUMBER = "incorrectNumber";
	private final static String JSON_ERROR_MESSAGE_PHASE_NOT_FOUND = "phaseNotFound";
	private final static String JSON_ERROR_MESSAGE_INCORRECT_PARAMS_NUMBER = "incorrectParamsNumber";
	private final static String JSON_ERROR_MESSAGE_RAID_NOT_FOUND = "raidNotFound";

	private final static String JSON_RAIDS = "raids";
	private final static String JSON_RAID_NAME = "name";
	private final static String JSON_RAID_ALIASES = "aliases";
	private final static String JSON_RAID_PHASES = "phases";
	private final static String JSON_RAID_PHASE_NUMBER = "number";
	private final static String JSON_RAID_PHASE_DAMAGE = "damage1percent";

	
	public RaidCommand() {
		super();
		
		//Lecture du Json
		try {
			JSONObject parameters = StaticVars.jsonSettings;

			//messages de base
			ERROR_MESSAGE = parameters.getString(JSON_ERROR_MESSAGE);

			//Param�tres propres � l'�quilibrage
			JSONObject raidParams = parameters.getJSONObject(JSON_RAID_COMMAND);
			
			COMMAND = raidParams.getString(JSON_RAID_COMMAND_COMMAND);
			
			//Messages
			JSONObject messages = raidParams.getJSONObject(JSON_MESSAGES);
			MESSAGE_PERCENTS_DIFFERENCE = messages.getString(JSON_MESSAGE_PERCENTS_DIFFERENCE);
			MESSAGE_PERCENTS_DIFFERENCE_PHASECHANGE = messages.getString(JSON_MESSAGE_PERCENTS_DIFFERENCE_PHASE_CHANGE);
			MESSAGE_DAMAGES = messages.getString(JSON_MESSAGE_DAMAGES);
			MESSAGE_PERCENT = messages.getString(JSON_MESSAGE_PERCENT);
			MESSAGE_TARGET = messages.getString(JSON_MESSAGE_TARGET);
			HELP = messages.getString(JSON_MESSAGE_HELP);
			
			//Messages d'erreur
			JSONObject errorMessages = raidParams.getJSONObject(JSON_ERROR_MESSAGES);
			OBJECTIVE_OVER_RAID_END = errorMessages.getString(JSON_ERROR_MESSAGE_OVER_RAID_END);
			INCOHERENT_PARAMETERS = errorMessages.getString(JSON_ERROR_MESSAGE_INCOHERENT_PARAMS);
			INCORRECT_NUMBER = errorMessages.getString(JSON_ERROR_MESSAGE_INCORRECT_NUMBER);
			PHASE_NOT_FOUND = errorMessages.getString(JSON_ERROR_MESSAGE_PHASE_NOT_FOUND);
			INCORRECT_PARAMS_NUMBER = errorMessages.getString(JSON_ERROR_MESSAGE_INCORRECT_PARAMS_NUMBER);
			RAID_NOT_FOUND = errorMessages.getString(JSON_ERROR_MESSAGE_RAID_NOT_FOUND);

			//gestion des raids
			JSONArray raids = raidParams.getJSONArray(JSON_RAIDS);
			phaseHPmap = new HashMap<String, Map<Integer,Integer>>();
			aliasesMap = new HashMap<String,List<String>>();

			for(int r=0 ; r<raids.length() ; r++) {
				Map<Integer,Integer> phasesHPmapForThisRaid = new HashMap<Integer,Integer>();
				
				JSONObject raid = raids.getJSONObject(r);
				
				String raidName = raid.getString(JSON_RAID_NAME);
				JSONArray phases = raid.getJSONArray(JSON_RAID_PHASES);
				
				for(int p=0 ; p<phases.length() ; p++) {
					JSONObject phase = phases.getJSONObject(p);
					
					Integer phaseNumber = phase.getInt(JSON_RAID_PHASE_NUMBER);
					Integer phaseDamage = phase.getInt(JSON_RAID_PHASE_DAMAGE);
					
					phasesHPmapForThisRaid.put(phaseNumber, phaseDamage);
				}
				
				phaseHPmap.put(raidName, phasesHPmapForThisRaid);
				
				JSONArray aliases = raid.getJSONArray(JSON_RAID_ALIASES);
				List<String> aliasesForThisRaid = new ArrayList<String>();
				
				for(int a=0 ; a<aliases.length() ; a++) {
					aliasesForThisRaid.add(aliases.getString(a));
				}
				
				aliasesMap.put(raidName, aliasesForThisRaid);
			}
		}
		catch(JSONException e) {
			logger.error("JSON parameters file is incorrectly formatted");
			e.printStackTrace();
		}
		
	}
	
	public CommandAnswer answer(List<String> params,Message messageRecu,boolean isAdmin) {

		if(params.size() == 0) {
			return new CommandAnswer(HELP,null);
		}
		String raidName = params.get(0);
		
		//Accepter des noms alternatifs
		for(Entry<String, List<String>> raidAliases : aliasesMap.entrySet()) {
			String currentRaidName = raidAliases.getKey();
			for(String alias : raidAliases.getValue()) {
				raidName = raidName.replaceAll(alias, currentRaidName);
			}
		}
		
		if(phaseHPmap.get(raidName) == null) {
			return error(RAID_NOT_FOUND);
		}
		
		try {		
			String phaseName = params.get(1).replace("p","");
			Integer phaseNumber = Integer.parseInt(phaseName);

			if(params.size() == 3) {			
				return doPhaseWithOneParameter(params.get(2), raidName, phaseNumber);		
			}
			else if(params.size() == 4) {
				return doPhaseWithTwoParameters(params.get(2),params.get(3), raidName, phaseNumber);		
			}
			else {
				return error(INCORRECT_PARAMS_NUMBER);
			}
		}
		catch(NumberFormatException | IndexOutOfBoundsException e) {
			return error(PHASE_NOT_FOUND);
		}
		
		
	}

	private CommandAnswer doPhaseWithOneParameter(String value, String raidName,Integer phaseNumber) {
		
		Integer phaseHP1percent = phaseHPmap.get(raidName).get(phaseNumber);		
		if(phaseHP1percent == null) {
			return error(PHASE_NOT_FOUND);
		}
		
		//Retirer le signe "%"
		value = value.replace("%", "");
		
		//Accepter , ou .
		value = value.replace(",", ".");
		
		Integer multiplier = 1;
		//Accepter "k" � la fin d'un nombre
		if(value.endsWith("k")) {
			value = value.replace("k","");
			multiplier = 1000;
		}		
		
		//Accepter "M" � la fin d'un nombre
		if(value.endsWith("m")) {
			value = value.replace("m","");
			multiplier = 1000000;
		}

		try {
			
			Float valueAsFloat = Float.parseFloat(value) * multiplier;
			
			if(valueAsFloat < 0) {
				valueAsFloat = -1 * valueAsFloat;
			}
			
			if(valueAsFloat <= 100) {
				//Il s'agit d'un pourcentage
				Integer responseValue = (int) (valueAsFloat * phaseHP1percent);
				
				String formattedValue = NumberFormat.getIntegerInstance().format(responseValue);

				String message = String.format(MESSAGE_PERCENT, raidName,phaseNumber,value,formattedValue);
				return new CommandAnswer(message, null);
			}
			else {
				//Il s'agit d'une valeur en d�g�ts
				Float responseValue = valueAsFloat / phaseHP1percent;
				
				String formattedValue = NumberFormat.getIntegerInstance().format(valueAsFloat.intValue());
				
				String message = String.format(MESSAGE_DAMAGES,raidName,phaseNumber,formattedValue,responseValue);
				return new CommandAnswer(message,null);
			}
		}

		catch(NumberFormatException e) {
			return error(INCORRECT_NUMBER);
		}
	}


	private CommandAnswer doPhaseWithTwoParameters(String value, String secondValue, String raidName,Integer phaseNumber) {

		//Retirer le signe "%"
		value = value.replace("%", "");
		secondValue = secondValue.replace("%", "");

		//Accepter , ou .
		value = value.replace(",", ".");
		secondValue = secondValue.replace(",", ".");

		Integer multiplier = 1;
		Integer secondMultiplier = 1;
		//Accepter "k" � la fin d'un nombre
		if(value.endsWith("k")) {
			value = value.replace("k","");
			multiplier = 1000;
		}
		if(secondValue.endsWith("k")) {
			secondValue = secondValue.replace("k","");
			secondMultiplier = 1000;
		}	
		
		//Accepter "M" � la fin d'un nombre
		if(value.endsWith("m")) {
			value = value.replace("m","");
			multiplier = 1000000;
		}
		if(secondValue.endsWith("m")) {
			secondValue = secondValue.replace("m","");
			secondMultiplier = 1000000;
		}
		
		try {
			Float valueAsFloat = Float.valueOf(value) * multiplier;
			Float secondValueAsFloat = Float.valueOf(secondValue) * secondMultiplier;

			//Si valeurs n�gatives, on les repasse en positif�
			if(valueAsFloat < 0) {
				valueAsFloat = -1 * valueAsFloat;
			}
			if(secondValueAsFloat < 0) {
				secondValueAsFloat = -1 * secondValueAsFloat;
			}
			
			if(valueAsFloat <= 100 && secondValueAsFloat < 100) {
				//Il s'agit de deux pourcentages�
				return doPhaseWithTwoPercentages(valueAsFloat, secondValueAsFloat, raidName, phaseNumber);
			}
			
			if(valueAsFloat <= 100 && secondValueAsFloat > 100) {
				//Il s'agit d'un pourcentage et d'une valeur
				return doPhaseWithPercentageAndValue(valueAsFloat,secondValueAsFloat,raidName,phaseNumber);
			}
			if(secondValueAsFloat <= 100 && valueAsFloat > 100) {
				//Il s'agit d'un pourcentage et d'une valeur
				return doPhaseWithPercentageAndValue(secondValueAsFloat,valueAsFloat,raidName,phaseNumber);
			}
			
			return error(INCOHERENT_PARAMETERS);
		}
		catch(NumberFormatException e) {
			return error(INCORRECT_NUMBER);
		}
	}

	private CommandAnswer doPhaseWithTwoPercentages(Float valueAsFloat, Float secondValueAsFloat, String raidName,
			Integer phaseNumber) {
		
		Integer phaseHP1percent = phaseHPmap.get(raidName).get(phaseNumber);		
		if(phaseHP1percent == null) {
			return error(PHASE_NOT_FOUND);
		}
		
		Float paramValue = valueAsFloat - secondValueAsFloat;

		if(paramValue < 0) {
			//Il y a eu changement de phase
			
			Integer nextPhaseHP1percent = phaseHPmap.get(raidName).get(phaseNumber);		
			if(nextPhaseHP1percent == null) {
				return error(INCOHERENT_PARAMETERS);
			}
			
			//D�g�ts faits � la fin de la phase annonc�e
			Integer responseValue = (int) (valueAsFloat * phaseHP1percent);
			
			//On ajoute les d�g�ts faits au d�but de la phase suivante
			responseValue += (int) (100 - secondValueAsFloat) * nextPhaseHP1percent;
			
			String formattedValue = NumberFormat.getIntegerInstance().format(responseValue);
			String message = String.format(MESSAGE_PERCENTS_DIFFERENCE_PHASECHANGE,
					valueAsFloat,
					secondValueAsFloat,
					raidName,
					phaseNumber,
					formattedValue);
			return new CommandAnswer(message,null);
		}
		else {
			Integer responseValue = (int) (paramValue * phaseHP1percent);
			String formattedValue = NumberFormat.getIntegerInstance().format(responseValue);

			String message = String.format(MESSAGE_PERCENTS_DIFFERENCE,
					valueAsFloat,
					secondValueAsFloat,
					raidName,
					phaseNumber,
					formattedValue);
			return new CommandAnswer(message,null);
		}
	}

	private CommandAnswer doPhaseWithPercentageAndValue(Float initialPercentage, Float targetValue,String raidName, Integer phaseNumber) {
		
		Map<Integer,Integer> phaseHPmapForCurrentRaid = phaseHPmap.get(raidName);
		
		Float resultPercentage = (float)0;
		
		Integer phaseNumberCursor = phaseNumber;
		
		Float residualDamage = targetValue;
		
		Float currentPhasePercentage = initialPercentage;
		
		while(residualDamage > 0) {
			
			Integer HP1percent = phaseHPmapForCurrentRaid.get(phaseNumberCursor);
			
			if(HP1percent == null) {
				return new CommandAnswer(OBJECTIVE_OVER_RAID_END,null);
			}
			
			Float requiredPercentage = residualDamage / HP1percent;
			
			if(requiredPercentage < currentPhasePercentage) {
				resultPercentage = currentPhasePercentage - requiredPercentage;
				residualDamage = (float) 0;
			}
			else {
				residualDamage -= currentPhasePercentage * HP1percent;
				phaseNumberCursor ++;
				currentPhasePercentage = (float) 100;
			}
		}
		
		String formattedValue = NumberFormat.getIntegerInstance().format(targetValue);
		String message = String.format(MESSAGE_TARGET, raidName,phaseNumber,initialPercentage,
								formattedValue,phaseNumberCursor,resultPercentage) ;
		return new CommandAnswer(message,null);
	}
	
	private CommandAnswer error(String errorMessage) {
		String message = ERROR_MESSAGE +"**"+ errorMessage + "**\r\n\r\n"+ HELP;
		
		return new CommandAnswer(message, null);
	}
}
