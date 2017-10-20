package fr.jedistar.commands;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.vdurmont.emoji.EmojiManager;

import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.embed.EmbedBuilder;
import de.btobastian.javacord.entities.message.impl.ImplReaction;
import fr.jedistar.JediStarBotCommand;
import fr.jedistar.StaticVars;
import fr.jedistar.formats.CommandAnswer;
import fr.jedistar.formats.PendingAction;
import fr.jedistar.listener.JediStarBotReactionAddListener;

public class EquilibrageCommand implements JediStarBotCommand {
	
	private final String COMMAND;
	private final String COMMAND_UPDATE;
	private final String LAUNCH_RAID_COMMAND;
	private final String END_RAID_COMMAND;
	private final String REPORT_COMMAND;
	private final String COMMAND_DELETE;
	private final String COMMAND_ADD;
	private final String COMMAND_ADJUST;
		
	private final String PODIUM = "podium";
	private final Integer PODIUM_VALUE = -100;
	
	private final String EMBED_TITLE;
	private final Color EMBED_COLOR = Color.BLUE;
	private final String MESSAGE_LINE;
	private final String MESSAGE_CURRENT_RAIDS_TITLE;
	private final String MESSAGE_CURRENT_RAIDS_RANGE;
	private final String MESSAGE_CURRENT_RAIDS_PODIUM;
	private final String CONFIRM_DELETE;
	
	private final String PODIUM_TEXT;
	private final String PODIUM_END;
		
	private final String HELP;
	private final String ERROR_MESSAGE;
	private final String FORBIDDEN;
	private final String WRITE_ERROR;
	private final String WRITE_HISTORY_ERROR;
	private final String READ_ERROR;
	private final String NUMBER_PROBLEM;


	private static final String DB_FILE = "balancingMembersDB.json";
	private static final String HISTORY_DIRECTORY = "History/balancingMembersDB";

	private final String RANCOR = "rancor";
	private final String TANK = "tank";
		
	//Des Map pour repr�senter les tableaux...
	private Map<String,List<Ranking>> rankingsPerRaid;
	
	private HashMap<String,HashMap<Integer,HashMap<String,List<Integer>>>> valuesPerUserPerRaid = null ;
	
	
	private final static String KEY_VALUES = "values";
	private final static String KEY_TARGET_RANK = "targetRank";
	private final static String KEY_PODIUMS = "podiums";
	private final static String KEY_WITHOUT_PODIUM = "withoutPodium";
	private static final Object COMMAND_AUTO_PODIUM = "podium-auto";
	private static final Integer MIN_RAIDS_FOR_PODIUM = 10;
	
	private Map<String,String> rulesPerRaid;
	
	//Nom des champs dans le json de param�tres
	private final static String JSON_ERROR_MESSAGE = "errorMessage";
	private final static String JSON_BALANCING="balancingCommandParameters";
	
	private final static String JSON_BALANCING_COMMANDS="commands";
	private final static String JSON_BALANCING_COMMANDS_COMMAND="command";
	private final static String JSON_BALANCING_COMMANDS_UPDATE="update";
	private final static String JSON_BALANCING_COMMANDS_LAUNCH_RAID="launchRaid";
	private final static String JSON_BALANCING_COMMANDS_END_RAID="endRaid";
	private final static String JSON_BALANCING_COMMANDS_REPORT="report";
	private final static String JSON_BALANCING_COMMANDS_DELETE="delete";
	private final static String JSON_BALANCING_COMMANDS_ADD="add";
	private final static String JSON_BALANCING_COMMANDS_ADJUST="adjust";
	
	private final static String JSON_BALANCING_HELP="help";
	
	private final static String JSON_BALANCING_MESSAGES="messages";
	private final static String JSON_BALANCING_MESSAGES_EMBED_TITLE="embedTitle";
	private final static String JSON_BALANCING_MESSAGES_EMBED_LINE="embedLine";
	private final static String JSON_BALANCING_MESSAGES_CURRENT_RAIDS_TITLE="currentRaidsTitle";
	private final static String JSON_BALANCING_MESSAGES_CURRENT_RAID_RANGE="currentRaidRange";
	private final static String JSON_BALANCING_MESSAGES_CURRENT_RAID_PODIUM="currentRaidPodium";
	private final static String JSON_BALANCING_MESSAGES_PODIUM_TEXT="podiumText";
	private final static String JSON_BALANCING_MESSAGES_PODIUM_END="podiumEnd";
	private final static String JSON_BALANCING_MESSAGES_CONFIRM_DELETE="confirmDelete";
	
	private final static String JSON_BALANCING_ERRORS="errorMessages";
	private final static String JSON_BALANCING_ERRORS_FORBIDDEN="forbidden";
	private final static String JSON_BALANCING_ERRORS_WRITE_JSON="writeJson";
	private final static String JSON_BALANCING_ERRORS_ARCHIVE_JSON="archiveJson";
	private final static String JSON_BALANCING_ERRORS_READ_JSON="readJson";
	private final static String JSON_BALANCING_ERRORS_NUMBER="numberProblem";

	/**
	 * Constructeur
	 */
	public EquilibrageCommand() {
		super();

				
		//AJOUTER DE NOUVEAUX RAIDS ICI
		rankingsPerRaid = new HashMap<String,List<Ranking>>();
		rankingsPerRaid.put(RANCOR,Arrays.asList(new Ranking("1-10",1,7,400000,600000),new Ranking("11-30",2,20,100000,300000),new Ranking("31+",2,20,0,0)));
		rankingsPerRaid.put(TANK,Arrays.asList(new Ranking("1-10",1,7,1100000,1300000),new Ranking("11-30",2,20,800000,1000000),new Ranking("31+",2,20,500000,700000)));
		
		rulesPerRaid = new HashMap<String,String>();
		rulesPerRaid.put(RANCOR, "@everyone \r\n"
				+ ":round_pushpin: Raid **RANCOR** Lancé :round_pushpin: \r\n" + 
				":white_small_square: Podium à 800k pour se placer\r\n" + 
				":white_small_square: Tranche 4-10 entre 400K et 600K\r\n" + 
				":white_small_square: Tranche 11-30 entre 100K et 300K\r\n" + 
				":white_small_square: Tranche 31+ à 0\r\n" + 
				":clock2: Au bout de **24h**, le podium finit le raid\r\n" + 
				":warning: Un podium sera comptabilisé pour non respect de la tranche de dégâts. :warning:");
		rulesPerRaid.put(TANK, "@everyone \r\n"
				+ ":round_pushpin: Raid **TANK** Lancé :round_pushpin: \r\n" + 
				":white_small_square: Podium à 1M5 pour se placer\r\n" + 
				":white_small_square: Tranche 4-10 entre 1,1M et 1,3M\r\n" + 
				":white_small_square: Tranche 11-30 entre 800K et 1M\r\n" + 
				":white_small_square: Tranche 31+ entre 500K et 700K\r\n" + 
				":clock2: Au bout de **36h**, le podium finit le raid\r\n" +
				":warning: Un podium sera comptabilisé pour non respect de la tranche de dégâts :warning:");
		
		//Lire le Json
		JSONObject parameters = StaticVars.jsonSettings;

		ERROR_MESSAGE = parameters.getString(JSON_ERROR_MESSAGE);

		JSONObject balancingParams = parameters.getJSONObject(JSON_BALANCING);
		
		JSONObject commands = balancingParams.getJSONObject(JSON_BALANCING_COMMANDS);
		COMMAND = commands.getString(JSON_BALANCING_COMMANDS_COMMAND);
		COMMAND_UPDATE = commands.getString(JSON_BALANCING_COMMANDS_UPDATE);
		LAUNCH_RAID_COMMAND = commands.getString(JSON_BALANCING_COMMANDS_LAUNCH_RAID);
		END_RAID_COMMAND = commands.getString(JSON_BALANCING_COMMANDS_END_RAID);
		REPORT_COMMAND = commands.getString(JSON_BALANCING_COMMANDS_REPORT);
		COMMAND_DELETE = commands.getString(JSON_BALANCING_COMMANDS_DELETE);
		COMMAND_ADD = commands.getString(JSON_BALANCING_COMMANDS_ADD);
		COMMAND_ADJUST = commands.getString(JSON_BALANCING_COMMANDS_ADJUST);
		
		HELP = balancingParams.getString(JSON_BALANCING_HELP);
		
		JSONObject messages = balancingParams.getJSONObject(JSON_BALANCING_MESSAGES);
		EMBED_TITLE = messages.getString(JSON_BALANCING_MESSAGES_EMBED_TITLE);
		MESSAGE_LINE = messages.getString(JSON_BALANCING_MESSAGES_EMBED_LINE);
		MESSAGE_CURRENT_RAIDS_TITLE = messages.getString(JSON_BALANCING_MESSAGES_CURRENT_RAIDS_TITLE);
		MESSAGE_CURRENT_RAIDS_RANGE = messages.getString(JSON_BALANCING_MESSAGES_CURRENT_RAID_RANGE);
		MESSAGE_CURRENT_RAIDS_PODIUM = messages.getString(JSON_BALANCING_MESSAGES_CURRENT_RAID_PODIUM);
		PODIUM_TEXT = messages.getString(JSON_BALANCING_MESSAGES_PODIUM_TEXT);
		PODIUM_END = messages.getString(JSON_BALANCING_MESSAGES_PODIUM_END);
		CONFIRM_DELETE = messages.getString(JSON_BALANCING_MESSAGES_CONFIRM_DELETE);

		JSONObject errorMessages = balancingParams.getJSONObject(JSON_BALANCING_ERRORS);
		FORBIDDEN = errorMessages.getString(JSON_BALANCING_ERRORS_FORBIDDEN);
		WRITE_ERROR = errorMessages.getString(JSON_BALANCING_ERRORS_WRITE_JSON);
		WRITE_HISTORY_ERROR = errorMessages.getString(JSON_BALANCING_ERRORS_ARCHIVE_JSON);
		READ_ERROR = errorMessages.getString(JSON_BALANCING_ERRORS_READ_JSON);
		NUMBER_PROBLEM = errorMessages.getString(JSON_BALANCING_ERRORS_NUMBER);
	}
	
	
	
	@Override
	public CommandAnswer answer(List<String> params,Message receivedMessage,boolean isAdmin) {
		
		//Si les tableaux n'ont pas �t� charg�s, les charger maintenant...
		if(valuesPerUserPerRaid == null) {
			String readReturn = readFromJson();
			if(readReturn != null) {
				return new CommandAnswer(readReturn,null);
			}
		}
		
		User author = receivedMessage.getAuthor();
		Channel chan = receivedMessage.getChannelReceiver();

		if(params == null || params.size() == 0) {
			//Appel sans param�tres : retourner l'�quilibrage sur tous les raids			
			Set<String> raids = rankingsPerRaid.keySet();
			
			EmbedBuilder embed = new EmbedBuilder();
			embed.setTitle(String.format(EMBED_TITLE, author.getName()));
			embed.setColor(EMBED_COLOR);
			
			String currentRaidTarget = findCurrentTargetRankings(author, raids);
			
			if(!currentRaidTarget.isEmpty())
			{
				embed.addField(MESSAGE_CURRENT_RAIDS_TITLE, currentRaidTarget, false);	
			}
			
			for(String raidName : raids) {
				embed.addField(raidName, returnUserValues(raidName, author.getDiscriminator()), true);
				
			}
			
			
			return new CommandAnswer(null,embed);
		}
		else if(params.size() == 1) {
			//Appel avec un param�tre
			String param = params.get(0);
			
			if(COMMAND_UPDATE.equals(param)) {
				if(isAdmin) {
					return new CommandAnswer(readFromJson(),null);
				}
				else {
					return new CommandAnswer(FORBIDDEN,null);
				}
			}
			
			if(REPORT_COMMAND.equals(param)) {
				if(isAdmin) {
					return new CommandAnswer(generateExcelReport(receivedMessage),null);
				}
				else {
					return new CommandAnswer(FORBIDDEN,null);
				}
			}
			
			//Accepter des noms alternatifs
			param = param.replaceAll("haat", "tank");
			param = param.replaceAll("aat", "tank");
			
			Set<String> raids = rankingsPerRaid.keySet();
			
			if(raids.contains(param)) {
				EmbedBuilder embed = new EmbedBuilder();
				embed.setTitle(String.format(EMBED_TITLE, author.getName()));		
				embed.setColor(EMBED_COLOR);

				embed.addField(param, returnUserValues(param, author.getDiscriminator()), true);
				
				return new CommandAnswer(null,embed);
			}
			
			else {
				return new CommandAnswer(error("Nom du raid non trouvé"),null);
			}
		}
		else if(params.size() >= 2 && END_RAID_COMMAND.equals(params.get(0))) {
			if(!isAdmin) {
				return new CommandAnswer(FORBIDDEN,null);
			}
			
			String raidName = params.get(1);
			
			Map<Integer,Integer> punished = new HashMap<Integer,Integer>();
			Set<Integer> notParticipated = new HashSet<Integer>();
			
			for(int i=2;i<params.size();i++) {
				String[] param = params.get(i).split("-");
				
				if(param.length != 2) {
					return new CommandAnswer("Merci d'utiliser la forme @user-X pour désigner les membres dans cette commande",null);
				}
				
				String userId = param[0];
				String rank = param[1];
				
				if(!userId.startsWith("<@") || ! userId.endsWith(">")) {
					return new CommandAnswer("Merci d'utiliser la forme @user-X pour désigner les membres dans cette commande",null);
				}
				
				Integer userDiscriminator = getUserDiscriminator(chan, userId);
				
				if("0".equals(rank)) {
					notParticipated.add(userDiscriminator);
				}
				
				if(PODIUM.equals(rank)) {
					punished.put(userDiscriminator, PODIUM_VALUE);
				}
				else {
					try {
						Integer rankInt = Integer.valueOf(rank);
						
						punished.put(userDiscriminator, rankInt);
					}
					catch(NumberFormatException e) {
						return new CommandAnswer("Merci d'utiliser la forme @user-X pour désigner les membres dans cette commande",null);
					}
				}		
			}
			return finishRaid(raidName,punished,notParticipated);

		}
		else if(params.size() == 2) {
			String command = params.get(0);
			String param = params.get(1);

			if(COMMAND_DELETE.equals(command)) {
				if(!isAdmin) {
					return new CommandAnswer(FORBIDDEN,null);				
				}
				else {
					Integer userId = new Integer(0);
					if(param.startsWith("<@") && param.endsWith(">")) {
						userId = getUserDiscriminator(chan, param);
					}
					else {
						try {
							userId = Integer.valueOf(param);
						}
						catch(NumberFormatException e) {
							return new CommandAnswer(NUMBER_PROBLEM,null);
						}
					}
					
					return beforeDeleteUser(receivedMessage, userId);
				}
			}
			else if(COMMAND_ADD.equals(command)) {
				if(!isAdmin) {
					return new CommandAnswer(FORBIDDEN,null);				
				}
				else {
					if(!param.startsWith("<@") || !param.endsWith(">")) {
						return new CommandAnswer("Merci d'utiliser les tags «@user» pour désigner l'utilisateur",null);
					}
					return new CommandAnswer(addUser(getUserDiscriminator(chan, param)),null);
				}
			}
		}
		else if(params.size() >= 3 && LAUNCH_RAID_COMMAND.equals(params.get(0))) {
			
			if(!isAdmin) {
				return new CommandAnswer(FORBIDDEN,null);
			}
			
			String raidName = params.get(1);
			
			Set<Integer> podium;
			int firstExcluded = 0;
			
			//D�tecter la fonction podium auto
			if(COMMAND_AUTO_PODIUM.equals(params.get(2))) {

				podium = generatePodium(raidName);
				
				if(podium == null) {
					return new CommandAnswer("Une erreur s'est produite lors de la génération du podium",null);
				}
				firstExcluded = 3;
			}
			else {
				if(!params.get(2).startsWith("<@") || !params.get(3).startsWith("<@") || !params.get(4).startsWith("<@")
						|| !params.get(2).endsWith(">") || !params.get(3).endsWith(">") || !params.get(4).endsWith(">")) {
					return new CommandAnswer("Merci d'utiliser les tags «@user» pour désigner le podium",null);
				}
				podium = new HashSet<Integer>();
				podium.add(getUserDiscriminator(chan, params.get(2)));
				podium.add(getUserDiscriminator(chan, params.get(3)));
				podium.add(getUserDiscriminator(chan, params.get(4)));

				firstExcluded = 5;
			}
			
			Set<Integer> excludedFromFirstRank = new HashSet<Integer>();
			for(int i = firstExcluded; i<params.size();i++) {
				String excluded = params.get(i);
				
				if(!excluded.startsWith("<@") || ! excluded.endsWith(">")) {
					return new CommandAnswer("Merci d'utiliser les tags «@user» pour désigner les joueurs exclus",null);
				}

				excludedFromFirstRank.add(getUserDiscriminator(chan, excluded));
			}
			
			return launchRaid(raidName,podium,excludedFromFirstRank,chan);
		}
		
		
		else if(params.size()>=5 && COMMAND_ADJUST.equals(params.get(0))) {
			if(!isAdmin) {
				return new CommandAnswer(FORBIDDEN,null);
			}

			try {

				String raidName = params.get(1);
				String rangeIndex = params.get(2);
				Integer adjustmentValue = Integer.parseInt(params.get(3));
								
				String returnMessage = "";
				
				for(int i=4;i<params.size();i++) {
					String param = params.get(i);
					
					if(!param.startsWith("<@") || ! param.endsWith(">")) {
						return new CommandAnswer("Merci d'utiliser les tags «@user» pour désigner les joueurs", null);
					}
					
					Integer userId = getUserDiscriminator(chan, param);
					
					String errorMessage = adjustUserValue(userId,raidName,rangeIndex,adjustmentValue);
					
					if(StringUtils.isNotBlank(errorMessage)) {
						returnMessage += "**Erreur lors de l'ajustement pour l'utilisateur "+param+"**\r\n"+errorMessage+"\r\n";
					}
				}

				if(StringUtils.isNotBlank(returnMessage)) {
					returnMessage = "**DES ERREURS SE SONT PRODUITES POUR CERTAINS UTILISATEURS :**\r\n"+returnMessage;
				}
				
				String write = writeToJson();
				
				if(write == null) {
					returnMessage = "**AJUSTEMENT OK**\r\n" + returnMessage;
				}
				else {
					returnMessage = "**PROBLEME LORS DE L'ECRITURE DU FICHIER**\r\n";
				}
				
				return new CommandAnswer(returnMessage,null);
			}
			catch(NumberFormatException e) {
				return new CommandAnswer("Un nombre entré n'a pas été reconnu", null);
			}

		}
		
		return new CommandAnswer(error("Commande incorrecte"),null);
	}



	/**
	 * Adjusts a user's value
	 * 
	 * DOESN'T write to JSON file
	 * @param userId
	 * @param raidName
	 * @param rangeIndex
	 * @param adjustmentValue
	 * @return an error message if anything went wrong, null if adjustment went OK
	 * @throws NumberFormatException
	 */
	private String adjustUserValue(Integer userId, String raidName, String rangeIndex, Integer adjustmentValue) throws NumberFormatException {

		HashMap<Integer,HashMap<String,List<Integer>>> valuesPerUser = valuesPerUserPerRaid.get(raidName);
		
		if(valuesPerUser == null) {
			return "raid non trouvé";
		}
		
		HashMap<String,List<Integer>> valuesMapForThisUser = valuesPerUser.get(userId);
		
		if(valuesMapForThisUser == null) {
			return "utilisateur non trouvé";
		}
		
		Integer listIndex = null;
		
		if(PODIUM.equals(rangeIndex)) {
			listIndex = 0;
			
			List<Integer> newValuesList = new ArrayList<Integer>();
			Integer currentValue = valuesMapForThisUser.get(KEY_PODIUMS).get(0);
			
			newValuesList.add(currentValue + adjustmentValue);
			
			valuesMapForThisUser.put(KEY_PODIUMS, newValuesList);
		}
		else {
			listIndex = Integer.parseInt(rangeIndex) -1;
		}
		
		List<Integer> valuesListForThisUser = valuesMapForThisUser.get(KEY_VALUES);
		
		if(listIndex +1 > valuesListForThisUser.size()) {
			return "Cette tranche n'a pas été trouvée";
		}
		
		List<Integer> newValuesList = new ArrayList<Integer>();
		
		for(int i=0;i<valuesListForThisUser.size();i++) {
			Integer currValue = valuesListForThisUser.get(i);
			
			if(i == listIndex) {
				currValue += adjustmentValue;
			}
			
			newValuesList.add(currValue);
		}
		
		valuesMapForThisUser.put(KEY_VALUES, newValuesList);
			
		return null;
	}



	private Set<Integer> generatePodium(String raidName) {
		
		Map<Integer,HashMap<String,List<Integer>>> valuesPerUser = valuesPerUserPerRaid.get(raidName);
		
		if(valuesPerUser == null) {
			return null;
		}
		
		Set<Integer> podium = new HashSet<Integer>();
		
		//Cr�er une liste de membres
		List<PodiumUserScore> usersList = new ArrayList<PodiumUserScore>();
		for(Integer userId : valuesPerUser.keySet()) {
			HashMap<String,List<Integer>> valuesForThisUser = valuesPerUser.get(userId);
			
			//R�cup�rer les valeurs relatives aux podiums
			Integer nbPodiums = valuesForThisUser.get(KEY_PODIUMS).get(0);
			Integer nbRaidsWithoutPodium = valuesForThisUser.get(KEY_WITHOUT_PODIUM).get(0);
			
			//calculer le nombre total de participations
			Integer totalNbRaids = 0;
			for(Integer nbRaids : valuesForThisUser.get(KEY_VALUES)) {
				totalNbRaids += nbRaids;
			}
			
			//Les utilisateurs ayant moins du minimum de participation sont ignor�s
			if(totalNbRaids > MIN_RAIDS_FOR_PODIUM) {
				PodiumUserScore userScore = new PodiumUserScore(userId,nbPodiums, totalNbRaids, nbRaidsWithoutPodium);
				usersList.add(userScore);
			}
		}
		
		//Trier la liste, puis prendre les trois premiers
		Collections.sort(usersList);
		
		for(int i=0;i<3;i++) {
			podium.add(usersList.get(i).userId);
		}
		
		return podium;
	}



	private String findCurrentTargetRankings(User author, Set<String> raids) {
		String currentRaidTarget = "";
		for(String raidName : raids) {
			
			HashMap<Integer, HashMap<String, List<Integer>>> valuesPerUser = valuesPerUserPerRaid.get(raidName);
			if (valuesPerUser!=null)
			{
				HashMap<String, List<Integer>> userInfos =  valuesPerUser.get(Integer.parseInt(author.getDiscriminator()));
				if(userInfos !=null)
				{
					List<Integer> userTargetRanks = userInfos.get(KEY_TARGET_RANK);
					if(userTargetRanks != null)
					{
						Integer userTargetRank = userTargetRanks.get(0)-1;
						List<Ranking> possibleRankings = rankingsPerRaid.get(raidName);
						
						if(userTargetRank == PODIUM_VALUE - 1) {
							currentRaidTarget += String.format(MESSAGE_CURRENT_RAIDS_PODIUM, raidName);
						}
						if(userTargetRank>=0 && userTargetRank<possibleRankings.size())
						{
							Ranking ranking = possibleRankings.get(userTargetRank);
							
							currentRaidTarget += String.format(MESSAGE_CURRENT_RAIDS_RANGE,raidName,ranking.name,ranking.getDamageRange());
							
						}
						
					}
				}
			}	
		}
		return currentRaidTarget;
	}


	

	private CommandAnswer finishRaid(String raidName, Map<Integer, Integer> punished, Set<Integer> notParticipated) {

		HashMap<Integer, HashMap<String, List<Integer>>> valuesPerUser = valuesPerUserPerRaid.get(raidName);
		
		if(valuesPerUser == null) {
			return new CommandAnswer("Raid non trouvé",null);
		}
		
		//Gestion des membres punis
		for(Map.Entry<Integer, Integer> punishedUser : punished.entrySet()) {
			
			Integer userId = punishedUser.getKey();
			Integer rankCursor = punishedUser.getValue() - 1;
			
			HashMap<String, List<Integer>> valuesMapForThisUser = valuesPerUser.get(userId);

			//Si la punition est un podium�
			if(rankCursor == PODIUM_VALUE -1) {
				rankCursor = 0;
				Integer nbPodiums = valuesMapForThisUser.get(KEY_PODIUMS).get(0);
				valuesMapForThisUser.put(KEY_PODIUMS, Arrays.asList(nbPodiums +1));
				valuesMapForThisUser.put(KEY_WITHOUT_PODIUM, Arrays.asList(0));
			}
			
			//On r�cup�re la liste de valeurs de l'utilisateur
			List<Integer> valuesForThisUser = valuesMapForThisUser.get(KEY_VALUES);

			//On incr�mente le classement dans la tranche correspondante.	
			List<Integer> newValues = new ArrayList<Integer>();
			
			for(int i=0;i<valuesForThisUser.size();i++) {
				if(i == rankCursor) {
					newValues.add(valuesForThisUser.get(i) + 1);
				}
				else {
					newValues.add(valuesForThisUser.get(i));
				}
			}
			valuesMapForThisUser.put(KEY_VALUES, newValues);
		}
		
		//Gestion des membres non punis
		for(Map.Entry<Integer, HashMap<String, List<Integer>>> user : valuesPerUser.entrySet()) {
			

			HashMap<String, List<Integer>> valuesMapForThisUser = user.getValue();
			if(notParticipated.contains(user.getKey())) {
				valuesMapForThisUser.put(KEY_TARGET_RANK, null);
				continue;
			}
			
			if(valuesMapForThisUser.get(KEY_TARGET_RANK) == null) {
				
				//Vérifier le total de raids de l'user
				Integer totalNbRaids = 0;
				List<Integer> valuesForThisUser = valuesMapForThisUser.get(KEY_VALUES);
				for(Integer nb : valuesForThisUser) {
					totalNbRaids += nb;
				}
				
				//Si total à zéro, c'est que l'user a été ajouté après la fin du raid
				if(totalNbRaids == 0) {
					continue;
				}
				
				return new CommandAnswer("Ce raid n'est pas en cours, ou il y a eu un problème avec le fichier stockant les donn�es",null);
			}
			
			//On prend le numéro de rang stocké dans la grosse Map
			Integer rankCursor = valuesMapForThisUser.get(KEY_TARGET_RANK).get(0) - 1;
			
			//Si l'utilisateur est sur le podium�
			if(rankCursor == PODIUM_VALUE -1) {
				rankCursor = 0;
				Integer nbPodiums = valuesMapForThisUser.get(KEY_PODIUMS).get(0);
				valuesMapForThisUser.put(KEY_PODIUMS, Arrays.asList(nbPodiums +1));
				valuesMapForThisUser.put(KEY_WITHOUT_PODIUM, Arrays.asList(0));			
			}
			else {
				//S'il n'est pas sur le podium, incr�menter son nombre de raids sans podium
				Integer nbRaidsSansPodium = valuesMapForThisUser.get(KEY_WITHOUT_PODIUM).get(0);
				valuesMapForThisUser.put(KEY_WITHOUT_PODIUM, Arrays.asList(nbRaidsSansPodium +1));
			}
			
			//On récupère la liste de valeurs de l'utilisateur
			List<Integer> valuesForThisUser = valuesMapForThisUser.get(KEY_VALUES);

			//On incrémente le classement dans la tranche correspondante.	
			List<Integer> newValues = new ArrayList<Integer>();
			
			for(int i=0;i<valuesForThisUser.size();i++) {
				if(i == rankCursor) {
					newValues.add(valuesForThisUser.get(i) + 1);
				}
				else {
					newValues.add(valuesForThisUser.get(i));
				}
			}
			valuesMapForThisUser.put(KEY_VALUES, newValues);
			valuesMapForThisUser.put(KEY_TARGET_RANK, null);
		}
		
		String write = writeToJson();
		
		if(write == null) {
			return new CommandAnswer("Raid terminé avec succès",null);
		}
		else {
			return new CommandAnswer(write,null);
		}
	}



	private String returnUserValues(String raidName,String userId) {
		Integer userID = Integer.parseInt(userId);
		
		List<Ranking> possibleRankings = rankingsPerRaid.get(raidName);
		
		Map<Integer, HashMap<String, List<Integer>>> valuesPerUser = valuesPerUserPerRaid.get(raidName);
		
		if(possibleRankings == null || valuesPerUser == null) {
			return error("Nom du raid non trouvé");
		}
		
		if(valuesPerUser.get(userID) == null) {
			return error("Problème dans le fichier json, l'avez-vous modifié à la main ?");
		}
		
		List<Integer> values = valuesPerUser.get(userID).get(KEY_VALUES);
		
		if(values == null) {
			return error("Votre numéro d'utilisateur n'a pas été trouvé dans le tableau d'équilibrage");
		}
		
		String returnMessage = "";
		
		for(int i = 0 ; i < possibleRankings.size() ; i++) {
			
			try {
				Ranking ranking = possibleRankings.get(i);
				Integer value = values.get(i);
				
				returnMessage += String.format(MESSAGE_LINE,ranking.name,value);
			}
			catch(IndexOutOfBoundsException e){
				//S'il manque une valeur, on saute cette ligne
				continue;
			}
		}
		
		return returnMessage;
	}

	private String readFromJson() {
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(DB_FILE));
			String json = new String(encoded, "utf-8");
			
			Gson gson = new GsonBuilder().create();
			
			Type ValuesMap = new TypeToken<HashMap<String,HashMap<Integer,HashMap<String,List<Integer>>>>>() {}.getType();
			
			valuesPerUserPerRaid = gson.fromJson(json, ValuesMap);
			
			return null;
		}
		catch(Exception e){
			e.printStackTrace();
			return READ_ERROR;
		}
	}
	
	private String archivePreviousDatabase() {
		
		try {
			Path fileToArchive = Paths.get(DB_FILE);

			String archiveFilename= new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".json";
			Path archiveDirectory = Paths.get(HISTORY_DIRECTORY);

			Files.createDirectories(archiveDirectory);

			Files.move(fileToArchive, archiveDirectory.resolve(archiveFilename));

			return null;
		}
		catch(Exception e) {
			e.printStackTrace();
			return WRITE_HISTORY_ERROR;
		}
		
	}
	
	private String writeToJson() {
		
		try {
			archivePreviousDatabase();
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			
			String json = gson.toJson(valuesPerUserPerRaid);
			
			Files.write(Paths.get(DB_FILE), json.getBytes());
		}
		catch(Exception e) {
			e.printStackTrace();
			return WRITE_ERROR;
		}
		
		return null;
	}
	
	private CommandAnswer launchRaid(String raidName, Set<Integer> podium, Set<Integer> excludedFromFirstRank, Channel chan) {
		
		List<Ranking> rankings = rankingsPerRaid.get(raidName);
		Map<Integer,HashMap<String,List<Integer>>> valuesPerUser = valuesPerUserPerRaid.get(raidName);
		
		if(rankings == null || valuesPerUser == null) {
			return new CommandAnswer(error("Nom du raid non trouvé"), null);
		}
		
		//cr�er une liste d'utilisateurs
		List<UserScore> usersList = new ArrayList<UserScore>();
		for(Integer userId : valuesPerUser.keySet()) {
			//Ignorer le podium
			if(!podium.contains(userId)) {
				usersList.add(new UserScore(userId));
			}
		}
		
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(EMBED_COLOR);
		boolean firstRank = true;
		
		//It�r�r sur les tranches
		for(int rankCur = 0;rankCur<rankings.size();rankCur++) {
	
			Ranking currentRanking = rankings.get(rankCur);
			String returnTextForThisRank = new String();
			
			List<String> usersForThisRank = new ArrayList<String>();
			
			
			//Calculer tous les scores
			for(UserScore user : usersList) {
				//exclure les joueurs de la premi�re tranche
				if(firstRank && excludedFromFirstRank.contains(user.userId)) {
					user.score = -10000.;
				}
				else {
					if(valuesPerUser.get(user.userId) == null) {
						return new CommandAnswer(error("Problème dans le fichier json, l'avez-vous modifié à la main ?"),null);
					}
					user.score = computeScore(rankings, valuesPerUser.get(user.userId).get(KEY_VALUES), rankCur);
				}
			}
			
			//Trier la liste
			Collections.sort(usersList);
			
			//Mettre le podium dans le retour
			if(firstRank) {
				returnTextForThisRank += PODIUM_TEXT;
				for(Integer userId : podium) {
					returnTextForThisRank += getUserName(userId,chan) + "\r\n";
					valuesPerUser.get(userId).put(KEY_TARGET_RANK, Arrays.asList(PODIUM_VALUE));
				}
				returnTextForThisRank += PODIUM_END;
				firstRank = false;
			}
			
			//Prendre les n premiers de la liste
			for(int userCur=0;userCur<currentRanking.width;userCur++) {
				UserScore user = usersList.get(0);
				valuesPerUser.get(user.userId).put(KEY_TARGET_RANK, Arrays.asList(rankCur+1));
				usersList.remove(0);
				usersForThisRank.add(getUserName(user.userId,chan));
			}
			
			usersForThisRank.sort(String::compareToIgnoreCase);
			
			for(String user : usersForThisRank) {
				returnTextForThisRank += user + "\r\n";
			}
			embed.addField("---- "+currentRanking.name+" ----", returnTextForThisRank, true);
		}
		
		String write = writeToJson();

		if(write == null) {
			return new CommandAnswer(rulesPerRaid.get(raidName), embed);
		}
		else {
			return new CommandAnswer(write,null);
		}
		
	}
	
	private String getUserName(Integer userId, Channel chan) {
		
		Collection<User> usersList = chan.getServer().getMembers();
		
		for(User user : usersList) {
			Integer discriminator = Integer.valueOf(user.getDiscriminator());
			if(discriminator.equals(userId)) {
				String userName = user.getName();
				String userNick = user.getNickname(chan.getServer());
				
				if(userNick != null) {
					userName = userNick;
				}
				
				return userName;
			}
		}
		return "Utilisateur non trouvé sur Discord";

	}
	
	private User getUser(Integer userId, Channel chan) {
		
		Collection<User> usersList = chan.getServer().getMembers();
		
		for(User user : usersList) {
			Integer discriminator = Integer.valueOf(user.getDiscriminator());
			if(discriminator.equals(userId)) {
				return user;			
			}
		}
		return null;

	}

	private Integer getUserDiscriminator(Channel chan,String discordUserId) {
		
		discordUserId = discordUserId.replaceAll("^[^0-9]+", "");
		discordUserId = discordUserId.replaceAll("[^0-9]+$", "");

		User user = chan.getServer().getMemberById(discordUserId);
		
		if(user == null) {
			return null;
		}
		
		return Integer.valueOf(user.getDiscriminator());
	}
	/**
	 * Calcule le score comme la moyenne des �carts entre la phase en question et les autres phases, chaque phase �tant pond�r�e par son poids.
	 * @param rankings
	 * @param values
	 * @param currentIndex
	 * @return
	 */
	private Double computeScore(List<Ranking> rankings,List<Integer>values,int currentIndex) {
		
		Double sum = 0.;
		Integer divider = 0;
		
		for(int i=currentIndex +1;i<rankings.size();i++) {			
			sum -= values.get(currentIndex) / rankings.get(currentIndex).weight;
			sum += values.get(i) / rankings.get(i).weight;
			divider ++;
		}
		
		if(divider == 0) {
			return 0.;
		}
		
		return sum / divider;
	}
	
	private CommandAnswer beforeDeleteUser(Message message,Integer userToDelete) {
				
		JediStarBotReactionAddListener.addPendingAction(new PendingAction(message.getAuthor(),"deleteUser",this, message,1,userToDelete));
		
		String emojiX = EmojiManager.getForAlias("x").getUnicode();
		String emojiV = EmojiManager.getForAlias("white_check_mark").getUnicode();

		return new CommandAnswer(String.format(CONFIRM_DELETE,userToDelete),null,emojiV,emojiX);
	}
	
	public String deleteUser(ImplReaction reaction, Integer userToDelete) {
		String emojiX = EmojiManager.getForAlias("x").getUnicode();
		String emojiV = EmojiManager.getForAlias("white_check_mark").getUnicode();
		
		if(emojiX.equals(reaction.getUnicodeEmoji())) {
			return "Ok, j'annule la demande.";
		}
		
		//Si les tableaux n'ont pas �t� charg�s, les charger maintenant...
		if(valuesPerUserPerRaid == null) {
			String readReturn = readFromJson();
			if(readReturn != null) {
				return readReturn;
			}
		}
		
		if(emojiV.equals(reaction.getUnicodeEmoji())) {
			
			for(String raidName : valuesPerUserPerRaid.keySet()) {			
				Object remove = valuesPerUserPerRaid.get(raidName).remove(userToDelete);
				
				if(remove == null) {
					return "Utilisateur non trouvé";
				}
			}
			String write = writeToJson();

			if(write == null) {
				return "Supression OK !";
			}
			else {
				return write;
			}
		}
		
		return null;
	}
	
	private String addUser(Integer userId) {

		//Si les tableaux n'ont pas �t� charg�s, les charger maintenant...
		if(valuesPerUserPerRaid == null) {
			String readReturn = readFromJson();
			if(readReturn != null) {
				return readReturn;
			}
		}

		
		for(String raidName : valuesPerUserPerRaid.keySet()) {			

			if(valuesPerUserPerRaid.get(raidName).get(userId) != null) {
				return "L'utilisateur existe déjà !";
			}
			List<Integer> newValuesList = new ArrayList<Integer>();

			for(Ranking rank:rankingsPerRaid.get(raidName)) {
				newValuesList.add(0);
			}

			HashMap<String,List<Integer>> newValuesMap = new HashMap<String,List<Integer>>();
			
			newValuesMap.put(KEY_VALUES, newValuesList);
			newValuesMap.put(KEY_PODIUMS, Arrays.asList(0));
			newValuesMap.put(KEY_WITHOUT_PODIUM, Arrays.asList(0));
			
			valuesPerUserPerRaid.get(raidName).put(userId, newValuesMap);	
		}
		
		String write = writeToJson();

		if(write != null) {
			return WRITE_ERROR;
		}
		
		return "Ajout réussi";
	}
	
	/**
	 * G�n�re un rapport au format excel et l'envoie sur le chan Discord
	 * @param receivedMessage
	 */
	private String generateExcelReport(Message receivedMessage) {
		
		Channel chan = receivedMessage.getChannelReceiver();
		
		XSSFWorkbook wb = new XSSFWorkbook();
		
		for(String raidName : valuesPerUserPerRaid.keySet()) {
			Sheet sheet = wb.createSheet(raidName);
			
			short rowCursor = 0;
			
			Row header = sheet.createRow(rowCursor);
			
			header.createCell(0).setCellValue("Id Discord");
			header.createCell(1).setCellValue("Nom");
			header.createCell(2).setCellValue("Podiums");
			header.createCell(3).setCellValue("Raids Sans Podium");
			
			short colCursor = 4;
			for(Ranking rank : rankingsPerRaid.get(raidName)) {
				header.createCell(colCursor).setCellValue(rank.name);
				colCursor ++;
			}
			
			rowCursor ++;
			
			HashMap<Integer,HashMap<String,List<Integer>>> valuesPerUser = valuesPerUserPerRaid.get(raidName);
			
			for(Map.Entry<Integer, HashMap<String,List<Integer>>> userValues : valuesPerUser.entrySet()) {
				Row curRow = sheet.createRow(rowCursor);
				
				curRow.createCell(0).setCellValue(userValues.getKey());
				curRow.createCell(1).setCellValue(getUserName(userValues.getKey(),chan));
				curRow.createCell(2).setCellValue(userValues.getValue().get(KEY_PODIUMS).get(0));
				curRow.createCell(3).setCellValue(userValues.getValue().get(KEY_WITHOUT_PODIUM).get(0));

				colCursor = 4;
				
				for(Integer nbTimesInThisRank : userValues.getValue().get(KEY_VALUES)) {
					curRow.createCell(colCursor).setCellValue(nbTimesInThisRank);
					colCursor ++;
				}
				rowCursor++;
			}
		}
		
		try {
			
			String filename = "swgohGuildReport_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".xlsx";
			FileOutputStream fileOut = new FileOutputStream(filename);
			
			wb.write(fileOut);
			fileOut.close();
			wb.close();

			chan.sendFile(new File(filename)).get();
			
			Files.delete(Paths.get(filename));
				
		} catch (IOException | InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return "Une erreur est survenue lors de la génération du rapport";
		}
		
		return null;
	}
	
	private String error(String message) {
		return ERROR_MESSAGE +"**"+ message + "**\r\n\r\n"+ HELP;
	}
	
	private class UserScore implements Comparable<UserScore>{

		public Integer userId;
		public Double score;
		
		public UserScore(Integer userId) {
			this.userId = userId;
		}
		
		@Override
		public int compareTo(UserScore o) {
			
			return (int)( 100000 * (o.score - this.score));
		}
		
	}
	
	
	
	private class Ranking{
		
		public String name;
		public Integer weight;
		public Integer width;
		public Integer lowDamage;
		public Integer highDamage;
		
		public Ranking(String name, Integer weight, Integer width, Integer lowDamage, Integer highDamage) {
			this.name = name;
			this.weight = weight;
			this.width = width;
			this.lowDamage = lowDamage;
			this.highDamage = highDamage;
		}
		
		private String formatNumber(Integer value)
		{
			String result;
			if(value>999999) {
				 result = String.format("%.1fM", value/1000000.);
			}
			else if(value>999) {
				 result = String.format("%dK", value/1000);
			}
			else {
				 result = String.format("%d", value);
			}
			return result;
			
			
		}
		
		public String getDamageRange()
		{
			if(lowDamage == highDamage)
			{
				return "à "+formatNumber(lowDamage);
			}
			else
			{
				return "entre "+formatNumber(lowDamage)+" et "+formatNumber(highDamage);
			}
		}
	}
	
	private class PodiumUserScore implements Comparable<PodiumUserScore>{
		
		public Integer userId;
		private Double score;
		private Integer nbRaidsWithoutPodium;
		
		public PodiumUserScore(Integer userId,Integer nbPodiums, Integer totalNbRaids, Integer nbRaidsWithoutPodium) {
			this.userId = userId;
			this.score = nbPodiums.doubleValue() / totalNbRaids;
			this.nbRaidsWithoutPodium = nbRaidsWithoutPodium;
		}

		@Override
		public int compareTo(PodiumUserScore o) {

			if(this.score.equals(o.score)) {
				return o.nbRaidsWithoutPodium - this.nbRaidsWithoutPodium;
			}
			
			return (int) (10000 * (this.score - o.score));
		}
	}

	@Override
	public String getCommand() {
		return COMMAND;
	}
}
