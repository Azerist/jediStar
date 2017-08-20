package fr.jedistar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;
import fr.jedistar.commands.EquilibrageCommand;
import fr.jedistar.commands.ModsCommand;
import fr.jedistar.commands.RaidCommand;

public class JediStarBotMessageListener implements MessageCreateListener {

	public static final String PREFIXE_COMMANDES = "!";

	Map<String,JediStarBotCommand> commandsMap;
	
	private static String MESSAGE = "Bonjour %s,\r%s";
	
	private static String MESSAGE_TROP_LONG = "La réponse générée par l'application est trop longue pour être affichée sur Discord.\r\nEssaie de faire une recherche plus précise :wink: ";
	
	public JediStarBotMessageListener() {
		super();
		
		commandsMap = new HashMap<String,JediStarBotCommand>();
		
		//AJOUTER ICI DE NOUVELLES COMMANDES
		commandsMap.put(RaidCommand.COMMAND, new RaidCommand());
		commandsMap.put(EquilibrageCommand.COMMAND,new EquilibrageCommand());
		commandsMap.put(ModsCommand.COMMAND,new ModsCommand());
	}
	
	public void onMessageCreate(DiscordAPI api, Message messageRecu) {
		
		String messageAsString = messageRecu.getContent().toLowerCase();
		
		//Si le message est vide ou ne commence pas par ! : Ne rien faire.
		if(messageAsString == null
				|| !messageAsString.startsWith(PREFIXE_COMMANDES)) {
			return;
		}
		
		//On retire le !
		messageAsString = messageAsString.substring(1);
		
		//On éclate les différentes parties du message
		String[] messagePartsArray = messageAsString.split(" ");	
		
		if(messagePartsArray.length == 0) {
			return;
		}
		
		ArrayList<String> messageParts = new ArrayList<String>(Arrays.asList(messagePartsArray));
		messageParts.remove(0);
		
		String command = messagePartsArray[0];
		
		JediStarBotCommand botCommand = commandsMap.get(command);
		
		if(botCommand == null) {
			return;
		}
		
		String answer = botCommand.answer(messageParts,messageRecu.getAuthor());
		
		if(answer == null || answer == "") {
			return;
		}
		
		String discordAnswer = String.format(MESSAGE, messageRecu.getAuthor().getMentionTag(),answer);
		
		if(discordAnswer.length() < 2000) {
			messageRecu.reply(discordAnswer);
		}
		else {
			messageRecu.reply(String.format(MESSAGE, messageRecu.getAuthor().getMentionTag(),MESSAGE_TROP_LONG));
		}
		
	}

}
