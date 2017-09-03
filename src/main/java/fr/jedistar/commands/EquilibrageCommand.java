package fr.jedistar.commands;

import java.awt.Color;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.embed.EmbedBuilder;
import fr.jedistar.JediStarBotCommand;
import fr.jedistar.formats.CommandAnswer;
import fr.jedistar.usedapis.SheetsAPIBuilder;

public class EquilibrageCommand implements JediStarBotCommand {

	private static final String ERROR_GOOGLE_SHEETS = "La connexion � Google Sheets n'est pas bien configur�e. Impossible d'utiliser cette fonction";

	private SheetsAPIBuilder sheetsAPI;
	
	public static final String COMMAND = "equilibrage";
	private final String COMMAND_UPDATE = "maj";
	private final String LAUNCH_RAID_COMMAND = "lancer";
	
	private static String SHEET_ID = null;
	
	private final String GOOGLE_API_ERROR = "Une erreur s'est produite lors de la connexion � Google Drive";
	
	private final String EMBED_TITLE = "�quilibrage de %s";
	private final Color EMBED_COLOR = Color.BLUE;
	private final String MESSAGE_LINE = "**Tranche %s** : %d\r\n";
	private final String PODIUM = "**+-- Podium --+**\r\n";
	private final String PODIUM_END = "**+--------------+**\r\n\r\n";
		
	private final String HELP = "Cette commande vous permet de conna�tre votre �quilibrage sur un raid.\r\n\r\n**Exemple d'appel**\r\n!equilibrage rancor\r\n**Commandes pour les officiers :**\r\n!equilibrage maj\r\n!equilibrage lancer rancor @podium1 @podium2 @podium3 @exclus1 @exclus2";
	private final static String ERROR_MESSAGE = "Merci de faire appel � moi, mais je ne peux pas te r�pondre pour la raison suivante :\r\n";

	private final String RANCOR = "rancor";
	private final String TANK = "tank";
	
	private Map<String,String> tableSheetRangesPerRaid;
	
	//Des Map pour repr�senter les tableaux...
	private Map<String,List<Ranking>> rankingsPerRaid;
	private Map<String,Map<Integer,List<Integer>>> valuesPerUserPerRaid;
	
	private Map<String,Map<Integer,String>> currentTargetRankingPerUserPerRaid = new HashMap<String,Map<Integer,String>>();
	
	private Map<String,String> rulesPerRaid;
	
	/**
	 * Constructeur
	 */
	public EquilibrageCommand() {
		super();
		
		//AJOUTER DE NOUVEAUX RAIDS ICI
		rankingsPerRaid = new HashMap<String,List<Ranking>>();
		rankingsPerRaid.put(RANCOR,Arrays.asList(new Ranking("1-10",1,7),new Ranking("11-30",2,20),new Ranking("31+",2,20)));
		rankingsPerRaid.put(TANK,Arrays.asList(new Ranking("1-10",1,7),new Ranking("11-30",2,20),new Ranking("31+",2,20)));
		
		tableSheetRangesPerRaid = new HashMap<String,String>();
		tableSheetRangesPerRaid.put(RANCOR, "Rancor H�roique!B61:G110");
		tableSheetRangesPerRaid.put(TANK, "Tank H�roique!B61:G110");
		
		rulesPerRaid = new HashMap<String,String>();
		rulesPerRaid.put(RANCOR, "@everyone \r\n"
				+ ":round_pushpin: Raid **RANCOR** Lanc� :round_pushpin: \r\n" + 
				":white_small_square: Podium � 1M pour se placer\r\n" + 
				":white_small_square: Tranche 3-10 entre 500K et 800K\r\n" + 
				":white_small_square: Tranche 11-30 entre 100K et 400K\r\n" + 
				":white_small_square: Tranche 31+ � 0\r\n" + 
				":warning: Un podium sera comptabilis� pour non respect de la tranche de d�g�ts. :warning:");
		rulesPerRaid.put(TANK, "@everyone \r\n"
				+ ":round_pushpin: Raid **TANK** Lanc� :round_pushpin: \r\n" + 
				":white_small_square: Podium � fond\r\n" + 
				":white_small_square: Tranche 3-10 entre 1M et 1,2M\r\n" + 
				":white_small_square: Tranche 11-30 entre 800K et 1M\r\n" + 
				":white_small_square: Tranche 31+ entre 600K et 800K\r\n" + 
				":warning: Un podium sera comptabilis� pour non respect de la tranche de d�g�ts :warning:");
	}
	
	/**
	 * Sert � renseigner la variable statique SHEET_ID qui repr�sente l'identifiant de la feuille Google Sheets � utiliser.
	 * @param sheetId
	 */
	public static void setSheetId(String sheetId) {
		SHEET_ID = sheetId;
	}
	
	@Override
	public CommandAnswer answer(List<String> params,User author,Channel chan) {
		

		if(params == null || params.size() == 0) {
			//Appel sans param�tres : retourner l'�quilibrage sur tous les raids

			//Si les tableaux n'ont pas �t� charg�s, les charger maintenant...
			if(valuesPerUserPerRaid == null) {
				updateTables();
			}
			
			Set<String> raids = rankingsPerRaid.keySet();
			
			EmbedBuilder embed = new EmbedBuilder();
			embed.setTitle(String.format(EMBED_TITLE, author.getName()));
			embed.setColor(EMBED_COLOR);
			
			for(String raidName : raids) {
				embed.addField(raidName, returnUserValues(raidName, author.getDiscriminator()), true);
			}
			
			return new CommandAnswer(null,embed);
		}
		else if(params.size() == 1) {
			//Appel avec un param�tre
			String param = params.get(0);
			
			if(COMMAND_UPDATE.equals(param)) {
				return new CommandAnswer(updateTables(),null);
			}
			
			//Si les tableaux n'ont pas �t� charg�s, les charger maintenant...
			if(valuesPerUserPerRaid == null) {
				updateTables();
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
				return new CommandAnswer(error("Nom du raid non trouv�"),null);
			}
		}
		else if(params.size() > 5 && LAUNCH_RAID_COMMAND.equals(params.get(0))) {
			
			//Si les tableaux n'ont pas �t� charg�s, les charger maintenant...
			if(valuesPerUserPerRaid == null) {
				updateTables();
			}
			
			String raidName = params.get(1);
			
			if(!params.get(2).startsWith("<@") || !params.get(3).startsWith("<@") || !params.get(4).startsWith("<@")
					|| !params.get(2).endsWith(">") || !params.get(3).endsWith(">") || !params.get(4).endsWith(">")) {
				return new CommandAnswer("Merci d'utiliser les tags �@user� pour d�signer le podium",null);
			}
			Set<Integer> podium = new HashSet<Integer>();
			podium.add(getUserDiscriminator(chan, params.get(2)));
			podium.add(getUserDiscriminator(chan, params.get(3)));
			podium.add(getUserDiscriminator(chan, params.get(4)));
			
			Set<Integer> excludedFromFirstRank = new HashSet<Integer>();
			for(int i = 5; i<params.size();i++) {
				String excluded = params.get(i);
				
				if(!excluded.startsWith("<@") || ! excluded.endsWith(">")) {
					return new CommandAnswer("Merci d'utiliser les tags �@user� pour d�signer les joueurs exclus",null);
				}

				excludedFromFirstRank.add(getUserDiscriminator(chan, excluded));
			}
			
			return launchRaid(raidName,podium,excludedFromFirstRank,chan);
		}
		
		return new CommandAnswer(error("Commande incorrecte"),null);
	}


	

	private String returnUserValues(String raidName,String userId) {
		Integer userID = Integer.parseInt(userId);
		
		List<Ranking> possibleRankings = rankingsPerRaid.get(raidName);
		
		Map<Integer,List<Integer>> valuesPerUser = valuesPerUserPerRaid.get(raidName);
		
		if(possibleRankings == null || valuesPerUser == null) {
			return error("Nom du raid non trouv�");
		}
		
		List<Integer> values = valuesPerUser.get(userID);
		
		if(values == null) {
			return error("Votre num�ro d'utilisateur n'a pas �t� trouv� dans le tableau d'�quilibrage");
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


	private String updateTables() {
		
		if(SHEET_ID == null) {
			return ERROR_GOOGLE_SHEETS;
		}
		String returnMessage = "";
		
		try {
			
			//Connexion � Google API
			sheetsAPI = new SheetsAPIBuilder(SHEET_ID,true);	
			
			valuesPerUserPerRaid = new HashMap<String,Map<Integer,List<Integer>>>();
			//Mise � jour des tables pour chaque raid
			for(Entry<String,String> raid : tableSheetRangesPerRaid.entrySet()) {
				List<List<Object>> readTable = sheetsAPI.getRange(raid.getValue());
				
				String updateMessage = updateTable(raid.getKey(),readTable);
				
				returnMessage += updateMessage + "\r\n";
			}
			
			
		} catch (GeneralSecurityException|IOException e) {
			e.printStackTrace();
			return GOOGLE_API_ERROR;
		}
		
		return returnMessage;
	}


	private String updateTable(String raidName, List<List<Object>> readTable) {

		if(readTable == null || readTable.isEmpty()) {
			return "Erreur lors de la m�j pour le raid" + raidName + " : Impossible de lire le tableau sur Google Drive";
		}
		
		//Initialisation de la Map pour stocker les valeurs lues...
		Map<Integer,List<Integer>> valuesPerUser = new HashMap<Integer,List<Integer>>();
		
		//le nombre de tranches pour ce raid
		Integer numberOfRangesForThisRaid = rankingsPerRaid.get(raidName).size();
		
		//On it�re sur les lignes du tableau lues...
		for(List<Object> tableLine : readTable) {
			
			//Si ligne vide, passer � la suivante...
			if(tableLine == null || tableLine.isEmpty()) {
				continue;
			}
			
			try {
				//R�cup�ration du userID...
				Integer userID = sheetsAPI.readInteger(tableLine.get(0));
				
				//Initialisation de la List contenant les valeurs...
				List<Integer> values = new ArrayList<Integer>();
				
				//Ajout des valeurs � la liste
				for(int i=2;i-1<=numberOfRangesForThisRaid;i++) {
					values.add(sheetsAPI.readInteger(tableLine.get(i)));
				}
				
				//Ajout de la liste de valeurs dans la Map
				valuesPerUser.put(userID, values);
			}
			catch(Throwable e) {
				//Il y a un probl�me avec cette ligne du tableau, on passe � la suivante
				continue;
			}
		}
		
		//On ajoute la Map que l'on vient de remplir � la Map globale
		valuesPerUserPerRaid.put(raidName, valuesPerUser);
		return "Mise � jour du tableau OK pour le raid "+raidName;
	}
	
	private CommandAnswer launchRaid(String raidName, Set<Integer> podium, Set<Integer> excludedFromFirstRank, Channel chan) {
		
		List<Ranking> rankings = rankingsPerRaid.get(raidName);
		Map<Integer,List<Integer>> valuesPerUser = valuesPerUserPerRaid.get(raidName);
		
		if(rankings == null || valuesPerUser == null) {
			return new CommandAnswer(error("Nom du raid non trouv�"), null);
		}
		
		//Initialiser une Map pour stocker les valeurs calcul�es ici
		Map<Integer,String> targetRankingPerUser = new HashMap<Integer,String>();
		
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
					user.score = -100.;
				}
				else {
					user.score = computeScore(rankings, valuesPerUser.get(user.userId), rankCur);
				}
			}
			
			//Trier la liste
			Collections.sort(usersList);
			
			//Mettre le podium dans le retour
			if(firstRank) {
				returnTextForThisRank += PODIUM;
				for(Integer userId : podium) {
					returnTextForThisRank += getUserName(userId,chan) + "\r\n";
				}
				returnTextForThisRank += PODIUM_END;
				firstRank = false;
			}
			
			//Prendre les n premiers de la liste
			for(int userCur=0;userCur<currentRanking.width;userCur++) {
				UserScore user = usersList.get(0);
				targetRankingPerUser.put(user.userId, currentRanking.name);
				usersList.remove(0);
				usersForThisRank.add(getUserName(user.userId,chan));
			}
			
			Collections.sort(usersForThisRank);
			
			for(String user : usersForThisRank) {
				returnTextForThisRank += user + "\r\n";
			}
			embed.addField("--- "+currentRanking.name+" ---", returnTextForThisRank, true);
		}
		currentTargetRankingPerUserPerRaid.put(raidName, targetRankingPerUser);
		
		return new CommandAnswer(rulesPerRaid.get(raidName), embed);
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
		return "Utilisateur non trouv� sur Discord";

	}

	private Integer getUserDiscriminator(Channel chan,String discordUserId) {
		
		discordUserId = discordUserId.replace("<@", "");
		discordUserId = discordUserId.replace(">", "");

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
			
			return ( 100000 * (int)(o.score - this.score));
		}
		
	}
	
	private class Ranking{
		
		public String name;
		public Integer weight;
		public Integer width;
		
		public Ranking(String name, Integer weight, Integer width) {
			this.name = name;
			this.weight = weight;
			this.width = width;
		}
	}
}
