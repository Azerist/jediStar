package fr.jedistar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.embed.EmbedBuilder;
import de.btobastian.javacord.listener.message.MessageCreateListener;
import fr.jedistar.commands.EquilibrageCommand;
import fr.jedistar.commands.ModsCommand;
import fr.jedistar.commands.RaidCommand;
import fr.jedistar.formats.CommandAnswer;

public class JediStarBotMessageListener implements MessageCreateListener {

	public static final String PREFIXE_COMMANDES = "!";

	Map<String,JediStarBotCommand> commandsMap;
	
	private static String MESSAGE = "Bonjour %s,\r%s";
		
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
		
		CommandAnswer answer = botCommand.answer(messageParts,messageRecu.getAuthor());
		
		if(answer == null) {
			return;
		}
		
		String message ="";		
		if(!"".equals(answer.getMessage())) {
			message = String.format(MESSAGE, messageRecu.getAuthor().getMentionTag(),answer.getMessage());
		}
		
		EmbedBuilder embed = answer.getEmbed();
				
		messageRecu.reply(message, embed);
	}

}
