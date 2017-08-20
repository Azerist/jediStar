package fr.jedistar.commands;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.btobastian.javacord.entities.User;
import fr.jedistar.JediStarBotCommand;
import fr.jedistar.usedapis.SheetsAPIBuilder;

public class EquilibrageCommand implements JediStarBotCommand {

	private static final String ERROR_GOOGLE_SHEETS = "La connexion � Google Sheets n'est pas bien configur�e. Impossible d'utiliser cette fonction";

	private SheetsAPIBuilder sheetsAPI;
	
	public static final String COMMAND = "equilibrage";
	private final String COMMAND_UPDATE = "maj";
	
	private static String SHEET_ID = null;
	
	private final String GOOGLE_API_ERROR = "Une erreur s'est produite lors de la connexion � Google Drive";
	
	private final String ANSWER_MESSAGE = "Votre �quilibrage sur le raid **%s** est le suivant :\r\n";
	private final String MESSAGE_LINE = "**Tranche %s** : %d\r\n";
		
	private final String HELP = "Cette commande vous permet de conna�tre votre �quilibrage sur un raid.\r\n\r\n**Exemple d'appel**\r\n!equilibrage rancor";
	private final static String ERROR_MESSAGE = "Merci de faire appel � moi, mais je ne peux pas te r�pondre pour la raison suivante :\r\n";

	private final String RANCOR = "rancor";
	private final String TANK = "tank";
	
	private Map<String,String> tableSheetRangesPerRaid;
	
	//Des Map pour repr�senter les tableaux...
	private Map<String,List<String>> rankingsPerRaid;
	private Map<String,Map<Integer,List<Integer>>> valuesPerUserPerRaid;
	
	
	/**
	 * Constructeur
	 */
	public EquilibrageCommand() {
		super();
		
		//AJOUTER DE NOUVEAUX RAIDS ICI
		rankingsPerRaid = new HashMap<String,List<String>>();
		rankingsPerRaid.put(RANCOR,Arrays.asList("1-10","11-30","31+"));
		rankingsPerRaid.put(TANK,Arrays.asList("1-10","11-30","31+"));
		
		tableSheetRangesPerRaid = new HashMap<String,String>();
		tableSheetRangesPerRaid.put(RANCOR, "Rancor H�roique!B61:G110");
		tableSheetRangesPerRaid.put(TANK, "Tank H�roique!B61:G110");
	}
	
	/**
	 * Sert � renseigner la variable statique SHEET_ID qui repr�sente l'identifiant de la feuille Google Sheets � utiliser.
	 * @param sheetId
	 */
	public static void setSheetId(String sheetId) {
		SHEET_ID = sheetId;
	}
	
	@Override
	public String answer(List<String> params,User author) {
		

		if(params == null || params.size() == 0) {
			//Appel sans param�tres : retourner l'�quilibrage sur tous les raids

			//Si les tableaux n'ont pas �t� charg�s, les charger maintenant...
			if(valuesPerUserPerRaid == null) {
				updateTables();
			}
			
			Set<String> raids = rankingsPerRaid.keySet();
			
			String answer = "";
			
			for(String raidName : raids) {
				answer += returnUserValues(raidName, author.getDiscriminator());
			}
			
			return answer;
		}
		else if(params.size() == 1) {
			//Appel avec un param�tre
			String param = params.get(0);
			
			if(COMMAND_UPDATE.equals(param)) {
				return updateTables();
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
				return returnUserValues(param,author.getDiscriminator());
			}
			else {
				return error("Nom du raid non reconnu");
			}
		}
		
		return null;
	}


	private String returnUserValues(String raidName,String userId) {
		Integer userID = Integer.parseInt(userId);
		
		List<String> possibleRankings = rankingsPerRaid.get(raidName);
		
		Map<Integer,List<Integer>> valuesPerUser = valuesPerUserPerRaid.get(raidName);
		
		if(possibleRankings == null || valuesPerUser == null) {
			return error("Nom du raid non trouv�");
		}
		
		List<Integer> values = valuesPerUser.get(userID);
		
		if(values == null) {
			return error("Votre num�ro d'utilisateur n'a pas �t� trouv� dans le tableau d'�quilibrage");
		}
		
		String returnMessage = String.format(ANSWER_MESSAGE,raidName);
		
		for(int i = 0 ; i < possibleRankings.size() ; i++) {
			
			try {
				String ranking = possibleRankings.get(i);
				Integer value = values.get(i);
				
				returnMessage += String.format(MESSAGE_LINE,ranking,value);
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
	
	private String error(String message) {
		return ERROR_MESSAGE +"**"+ message + "**\r\n\r\n"+ HELP;
	}
}
