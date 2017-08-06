package fr.jedistar.commands;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.jedistar.JediStarBotCommand;

public class RaidCommand implements JediStarBotCommand {

	private static final String MESSAGE_PERCENTS_DIFFERENCE = "De *%.1f%%* à *%.1f%%* sur le **%s** en *phase %d*, votre équipe a fait **%s** dégâts.";
	private static final String MESSAGE_PERCENTS_DIFFERENCE_PHASECHANGE = "De *%.1f%%* à *%.1f%%* sur le **%s** en *phase %d*, si mon interprétation est correcte, vous avez changé de phase.\r\nSi c'est bien cela, votre équipe a fait **%s** dégâts.";
	private static final String MESSAGE_DAMAGES = "Sur le **%s** en *phase %d*, *%s* dégâts correspondent à **%.1f%%**";
	private static final String MESSAGE_PERCENT = "Sur le **%s** en *phase %d*, *%s%%* correspondent à **%s** dégâts";
	private static final String MESSAGE_TARGET = "Sur le **%s**, en commençant en *phase %d à %.1f%%* :\r\nPour atteindre votre objectif de *%s* dégâts, vous devez vous arrêter en **phase %d à %.1f%%**";
	public final static String COMMANDE = "raid";
	
	private final static String COMMANDE_RANCOR = "rancor";
	private final static String COMMANDE_TANK = "tank";
			
	//Représente 1% de HP pour les différentes phases des différents raids
	private Map<String,Map<Integer,Integer>> phaseHPmap;
	
	private final static String HELP = "Voici des exemples de commandes disponibles pour déterminer vos résultats de raid :\r\n\r\n" + 
			"- **!raid rancor p1 5.5%** ==> Donne les dégâts correspondant à 5.5% réalisés en P1 sur le rancor\r\n" + 
			"- **!raid rancor p2 10% 4%** ==> Donne les dégâts correspondant à 6% réalisés sur la P2 du rancor\r\n" + 
			"- **!raid aat p3 40000** ==> Donne le % correspondant à 40K de dégâts sur la p3 du tank\r\n" + 
			"- **!raid tank p4 35% 100000** ==> Donne le % cible à atteindre pour réaliser 100K dégâts en commençant le combat à 35% sur la P4 du tank";
	
	private final static String ERROR_MESSAGE = "Merci de faire appel à moi, mais je ne comprends pas votre commande pour la raison suivante :\r\n";
	
	public RaidCommand() {
		super();
		
		//AJOUTER ICI DES NOUVEAUX RAIDS
		Map<Integer,Integer> rancorPhaseHPmap = new HashMap<Integer,Integer>();
		rancorPhaseHPmap.put(1,18730);
		rancorPhaseHPmap.put(2,30550);
		rancorPhaseHPmap.put(3,35098);
		rancorPhaseHPmap.put(4,21080);
		
		Map<Integer,Integer> tankPhaseHPmap = new HashMap<Integer,Integer>();
		tankPhaseHPmap.put(1,43000);
		tankPhaseHPmap.put(2,192000);
		tankPhaseHPmap.put(3,120000);
		tankPhaseHPmap.put(4,120000);
		
		phaseHPmap = new HashMap<String, Map<Integer,Integer>>();
		phaseHPmap.put(COMMANDE_RANCOR, rancorPhaseHPmap);
		phaseHPmap.put(COMMANDE_TANK, tankPhaseHPmap);

	}
	
	public String answer(List<String> params) {

		if(params.size() == 0) {
			return HELP;
		}
		String raidName = params.get(0);
		
		//Accepter des noms alternatifs
		raidName = raidName.replaceAll("haat", "tank");
		raidName = raidName.replaceAll("aat", "tank");
		
		if(!COMMANDE_RANCOR.equals(raidName) && !COMMANDE_TANK.equals(raidName)) {
			return error("Nom du raid non reconnu");
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
				return error("Nombre de paramètres incorrect");
			}
		}
		catch(NumberFormatException | IndexOutOfBoundsException e) {
			return error("Nom de la phase non reconnu");
		}
		
		
	}

	private String doPhaseWithOneParameter(String value, String raidName,Integer phaseNumber) {
		
		Integer phaseHP1percent = phaseHPmap.get(raidName).get(phaseNumber);		
		if(phaseHP1percent == null) {
			return error("Numéro de phase non trouvé pour ce raid");
		}
		
		//Retirer le signe "%"
		value = value.replace("%", "");
		
		//Accepter , ou .
		value = value.replace(",", ".");
		
		Integer multiplier = 1;
		//Accepter "k" à la fin d'un nombre
		if(value.endsWith("k")) {
			value = value.replace("k","");
			multiplier = 1000;
		}		
		
		//Accepter "M" à la fin d'un nombre
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

				return String.format(MESSAGE_PERCENT, 
						raidName,phaseNumber,value,formattedValue);
			}
			else {
				//Il s'agit d'une valeur en dégâts
				Float responseValue = valueAsFloat / phaseHP1percent;
				
				String formattedValue = NumberFormat.getIntegerInstance().format(valueAsFloat.intValue());
				
				return String.format(MESSAGE_DAMAGES, 
									raidName,phaseNumber,formattedValue,responseValue);
			}
		}

		catch(NumberFormatException e) {
			return error("Un nombre entré n'a pas été reconnu correctement");
		}
	}


	private String doPhaseWithTwoParameters(String value, String secondValue, String raidName,Integer phaseNumber) {

		//Retirer le signe "%"
		value = value.replace("%", "");
		secondValue = secondValue.replace("%", "");

		//Accepter , ou .
		value = value.replace(",", ".");
		secondValue = secondValue.replace(",", ".");

		Integer multiplier = 1;
		Integer secondMultiplier = 1;
		//Accepter "k" à la fin d'un nombre
		if(value.endsWith("k")) {
			value = value.replace("k","");
			multiplier = 1000;
		}
		if(secondValue.endsWith("k")) {
			secondValue = secondValue.replace("k","");
			secondMultiplier = 1000;
		}	
		
		//Accepter "M" à la fin d'un nombre
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

			//Si valeurs négatives, on les repasse en positif…
			if(valueAsFloat < 0) {
				valueAsFloat = -1 * valueAsFloat;
			}
			if(secondValueAsFloat < 0) {
				secondValueAsFloat = -1 * secondValueAsFloat;
			}
			
			if(valueAsFloat <= 100 && secondValueAsFloat < 100) {
				//Il s'agit de deux pourcentages…
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
			
			return error("Les deux derniers paramètres entrés ne sont pas cohérents");
		}
		catch(NumberFormatException e) {
			return error("Un nombre entré n'a pas été reconnu correctement");
		}
	}

	private String doPhaseWithTwoPercentages(Float valueAsFloat, Float secondValueAsFloat, String raidName,
			Integer phaseNumber) {
		
		Integer phaseHP1percent = phaseHPmap.get(raidName).get(phaseNumber);		
		if(phaseHP1percent == null) {
			return error("Numéro de phase non trouvé pour ce raid");
		}
		
		Float paramValue = valueAsFloat - secondValueAsFloat;

		if(paramValue < 0) {
			//Il y a eu changement de phase
			
			Integer nextPhaseHP1percent = phaseHPmap.get(raidName).get(phaseNumber);		
			if(nextPhaseHP1percent == null) {
				return error("Vous êtes à la dernière phase, mais le second pourcentage est plus grand que le premier ?");
			}
			
			//Dégâts faits à la fin de la phase annoncée
			Integer responseValue = (int) (valueAsFloat * phaseHP1percent);
			
			//On ajoute les dégâts faits au début de la phase suivante
			responseValue += (int) (100 - secondValueAsFloat) * nextPhaseHP1percent;
			
			String formattedValue = NumberFormat.getIntegerInstance().format(responseValue);
			return String.format(MESSAGE_PERCENTS_DIFFERENCE_PHASECHANGE,
					valueAsFloat,
					secondValueAsFloat,
					raidName,
					phaseNumber,
					formattedValue);
		}
		else {
			Integer responseValue = (int) (paramValue * phaseHP1percent);
			String formattedValue = NumberFormat.getIntegerInstance().format(responseValue);

			return String.format(MESSAGE_PERCENTS_DIFFERENCE,
					valueAsFloat,
					secondValueAsFloat,
					raidName,
					phaseNumber,
					formattedValue);
		}
	}

	private String doPhaseWithPercentageAndValue(Float initialPercentage, Float targetValue,String raidName, Integer phaseNumber) {
		
		Map<Integer,Integer> phaseHPmapForCurrentRaid = phaseHPmap.get(raidName);
		
		Float resultPercentage = (float)0;
		
		Integer phaseNumberCursor = phaseNumber;
		
		Float residualDamage = targetValue;
		
		Float currentPhasePercentage = initialPercentage;
		
		while(residualDamage > 0) {
			
			Integer HP1percent = phaseHPmapForCurrentRaid.get(phaseNumberCursor);
			
			if(HP1percent == null) {
				return "Votre objectif dépasse la fin du raid";
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
		return String.format(MESSAGE_TARGET, raidName,phaseNumber,initialPercentage,
								formattedValue,phaseNumberCursor,resultPercentage) ;
	}
	
	private String error(String message) {
		return ERROR_MESSAGE +"**"+ message + "**\r\n\r\n"+ HELP;
	}
}
