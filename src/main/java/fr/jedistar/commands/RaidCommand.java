package fr.jedistar.commands;

import java.text.NumberFormat;
import java.util.List;

import fr.jedistar.JediStarBotCommand;

public class RaidCommand implements JediStarBotCommand {

	public final static String COMMANDE = "raid";
	
	private final static String COMMANDE_RANCOR = "rancor";
	private final static String COMMANDE_TANK = "aat";
	
	private final static String COMMANDE_P1 = "p1";
	private final static String COMMANDE_P2 = "p2";
	private final static String COMMANDE_P3 = "p3";
	private final static String COMMANDE_P4 = "p4";
			
	//Représente 1% de HP pour les différentes phases du Rancor
	private final static Integer RANCOR_P1 = 18730;
	private final static Integer RANCOR_P2 = 30550;
	private final static Integer RANCOR_P3 = 35098;
	private final static Integer RANCOR_P4 = 21080;
	
	//Représente 1% de HP pour les différentes phases du Tank
		private final static Integer TANK_P1 = 43000;
		private final static Integer TANK_P2 = 192000;
		private final static Integer TANK_P3 = 120000;
		private final static Integer TANK_P4 = 120000;
	
	private final static String HELP = "Commandes possibles :\rraid rancor p1 5.5%\rraid rancor p1 10% 5%\rraid rancor p1 100000"
										+ "\rraid aat p1 5.5%\rraid aat p1 10% 5%\rraid aat p1 100000";
	
	public String answer(List<String> params) {
		
		if(params.size() != 3 && params.size() != 4) {
			return HELP;
		}
		
		if(COMMANDE_RANCOR.equalsIgnoreCase(params.get(0))) {
			return doRancor(params);
		}
		else if(COMMANDE_TANK.equalsIgnoreCase(params.get(0))) {
			return doTank(params);
		}
		
		return HELP;
	}


	private String doTank(List<String> params) {
String command = params.get(1);
		
		String value = params.get(2);
		String secondValue = params.size() == 4 ? params.get(3) : null;
		
		if(COMMANDE_P1.equalsIgnoreCase(command)){
			return doPhase(value, secondValue,TANK_P1,COMMANDE_TANK,1);
		}
		else if(COMMANDE_P2.equalsIgnoreCase(command)){
			return doPhase(value, secondValue,TANK_P2,COMMANDE_TANK,2);
		}
		else if(COMMANDE_P3.equalsIgnoreCase(command)){
			return doPhase(value, secondValue,TANK_P3,COMMANDE_TANK,3);
		}
		else if(COMMANDE_P4.equalsIgnoreCase(command)){
			return doPhase(value, secondValue,TANK_P4,COMMANDE_TANK,4);
		}
		return HELP;
	}


	private String doRancor(List<String> params) {
		String command = params.get(1);
		
		String value = params.get(2);
		String secondValue = params.size() == 4 ? params.get(3) : null;
		
		if(COMMANDE_P1.equalsIgnoreCase(command)){
			return doPhase(value, secondValue,RANCOR_P1,COMMANDE_RANCOR,1);
		}
		else if(COMMANDE_P2.equalsIgnoreCase(command)){
			return doPhase(value, secondValue,RANCOR_P2,COMMANDE_RANCOR,2);
		}
		else if(COMMANDE_P3.equalsIgnoreCase(command)){
			return doPhase(value, secondValue,RANCOR_P3,COMMANDE_RANCOR,3);
		}
		else if(COMMANDE_P4.equalsIgnoreCase(command)){
			return doPhase(value, secondValue,RANCOR_P4,COMMANDE_RANCOR,4);
		}
		return HELP;
	}


	private String doPhase(String value, String secondValue, Integer phaseHP1percent,String raidName,Integer phaseNumber) {
		if(secondValue != null) {		
			return doPhaseWithTwoParameters(value, secondValue, phaseHP1percent, raidName, phaseNumber);
		}
		else {
			return doPhaseWithOneParameter(value,phaseHP1percent,raidName,phaseNumber);
		}
	}


	private String doPhaseWithOneParameter(String value, Integer phaseHP1percent, String raidName,
			Integer phaseNumber) {
		
		value = value.replace("%", "");
		value = value.replace(",", ".");

		try {
			Float valueAsFloat = Float.parseFloat(value);
			
			if(valueAsFloat < 0) {
				valueAsFloat = -1 * valueAsFloat;
			}
			
			if(valueAsFloat < 100) {
				//Il s'agit d'un pourcentage
				Integer responseValue = (int) (valueAsFloat * phaseHP1percent);
				
				String formattedValue = NumberFormat.getIntegerInstance().format(responseValue);

				return String.format("Sur le %s en phase %d, *%s%%* correspondent à **%s** dégâts", 
						raidName,phaseNumber,value,formattedValue);
			}
			else {
				//Il s'agit d'une valeur en dégâts
				Float responseValue = valueAsFloat / phaseHP1percent;
				
				String formattedValue = NumberFormat.getIntegerInstance().format(Integer.parseInt(value));
				
				return String.format("Sur le %s en phase %d, *%s* dégâts correspondent à **%.1f%%**", 
									raidName,phaseNumber,formattedValue,responseValue);
			}
		}

		catch(NumberFormatException e) {
			return HELP;
		}
	}


	private String doPhaseWithTwoParameters(String value, String secondValue, Integer phaseHP1percent, String raidName,Integer phaseNumber) {
		value = value.replace("%", "");
		value = value.replace(",", ".");
		secondValue = secondValue.replace("%", "");
		secondValue = secondValue.replace(",", ".");
		
		try {
			Float secondValueAsFloat = Math.min(100,Float.valueOf(secondValue));
			Float valueAsFloat = Math.min(100,Float.valueOf(value));
			
			
			Float paramValue = valueAsFloat - secondValueAsFloat;
			
			if(paramValue < 0) {
				Float temp = secondValueAsFloat;
				secondValueAsFloat = valueAsFloat;
				valueAsFloat = temp;
				
				String temp2 = secondValue;
				secondValue = value;
				value = temp2;
				
				paramValue = valueAsFloat - secondValueAsFloat;
			}
			
			Integer responseValue = (int) (paramValue * phaseHP1percent);
			String formattedValue = NumberFormat.getIntegerInstance().format(responseValue);
			
			return String.format("De *%s%%* à *%s%%* sur le %s en phase %d, votre équipe a fait **%s** dégâts.",
									value,
									secondValue,
									raidName,
									phaseNumber,
									formattedValue);
		}
		catch(NumberFormatException e) {
			return HELP;
		}
	}

}
