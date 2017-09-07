package fr.jedistar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.embed.EmbedBuilder;
import de.btobastian.javacord.entities.permissions.Role;
import de.btobastian.javacord.listener.message.MessageCreateListener;
import fr.jedistar.commands.EquilibrageCommand;
import fr.jedistar.commands.ModsCommand;
import fr.jedistar.commands.RaidCommand;
import fr.jedistar.formats.CommandAnswer;

public class JediStarBotMessageListener implements MessageCreateListener {

	public static final String PREFIXE_COMMANDES = "!";

	Map<String,JediStarBotCommand> commandsMap;
	
	private static String MESSAGE;
	
	//Noms des champs JSON
	private static final String JSON_ADMINS = "botAdmins";
	private static final String JSON_GROUPS = "groups";
	private static final String JSON_USERS = "users";
	private static final String JSON_BASE_MESSAGE = "baseMessage";
	
	private Set<String> adminGroups;
	private Set<Integer> adminUsers;
		
	public JediStarBotMessageListener() {
		super();
		
		commandsMap = new HashMap<String,JediStarBotCommand>();
		
		//AJOUTER ICI DE NOUVELLES COMMANDES
		commandsMap.put(RaidCommand.COMMAND, new RaidCommand());
		commandsMap.put(ModsCommand.COMMAND,new ModsCommand());
		
		//Lecture du Json
		try {
			JSONObject parameters = StaticVars.jsonSettings;

			//message de base
			MESSAGE = parameters.getString(JSON_BASE_MESSAGE);
			
			//admins
			JSONObject jsonAdmins = parameters.getJSONObject(JSON_ADMINS);	
			JSONArray jsonAdminGroups = jsonAdmins.getJSONArray(JSON_GROUPS);	
			adminGroups = new HashSet<String>();

			for(int i=0 ; i<jsonAdminGroups.length() ; i++) {
				adminGroups.add(jsonAdminGroups.getString(i));
			}

			JSONArray jsonAdminUsers = jsonAdmins.getJSONArray(JSON_USERS);
			adminUsers = new HashSet<Integer>();

			for(int i=0 ; i<jsonAdminUsers.length() ; i++) {
				adminUsers.add(jsonAdminUsers.getInt(i));
			}
		}
		catch(JSONException e) {
			System.out.println("JSON parameters file is incorrectly formatted");
			e.printStackTrace();
		}
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
		
		ArrayList<String> messageParts = new ArrayList<String>();
		for(String param : messagePartsArray) {
			if(param != null && !"".equals(param)) {
				messageParts.add(param.trim());
			}
		}
		
		messageParts.remove(0);
		
		String command = messagePartsArray[0];
		
		JediStarBotCommand botCommand = commandsMap.get(command);
		
		if(botCommand == null) {
			return;
		}
		
		messageRecu.getChannelReceiver().type();

		boolean isAdmin = isAdmin(messageRecu);
		
		CommandAnswer answer = botCommand.answer(messageParts,messageRecu,isAdmin);
		
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

	private boolean isAdmin(Message messageRecu) {
		
		User author = messageRecu.getAuthor();
		
		if(adminUsers.contains(Integer.parseInt(author.getDiscriminator()))){
			return true;
		}
		
		for(Role role : author.getRoles(messageRecu.getChannelReceiver().getServer())) {
			if(adminGroups.contains(role.getName())) {
				return true;
			}
		}
		
		return false;
	}

}
