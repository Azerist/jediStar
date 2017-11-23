package fr.jedistar.listener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.embed.EmbedBuilder;
import de.btobastian.javacord.entities.permissions.Role;
import de.btobastian.javacord.listener.message.MessageCreateListener;
import fr.jedistar.JediStarBotCommand;
import fr.jedistar.StaticVars;
import fr.jedistar.commands.AreneCommand;
import fr.jedistar.commands.HelpCommand;
import fr.jedistar.commands.ModsCommand;
import fr.jedistar.commands.PayoutCommand;
import fr.jedistar.commands.RaidCommand;
import fr.jedistar.commands.SetUpCommand;
import fr.jedistar.commands.TerritoryBattlesCommand;
import fr.jedistar.formats.CommandAnswer;

public class JediStarBotMessageListener implements MessageCreateListener {

	private static final Logger logger = LoggerFactory.getLogger(JediStarBotMessageListener.class);

	public static final String PREFIXE_COMMANDES = "%";

	Map<String,JediStarBotCommand> commandsMap;
	
	private static String MESSAGE;
	
	//Noms des champs JSON
	private static final String JSON_ADMINS = "botAdmins";
	private static final String JSON_GROUPS = "groups";
	private static final String JSON_USERS = "users";
	private static final String JSON_BASE_MESSAGE = "baseMessage";
	
	private Set<String> adminGroups;
	private Set<Integer> adminUsers;
	
	private static final String SQL_INSERT_HISTORY = "INSERT INTO commandHistory(command,ts,userID,userName,serverID,serverName,serverRegion) VALUES (?,?,?,?,?,?,?);";
	
	public JediStarBotMessageListener() {
		super();
		

		commandsMap = new HashMap<String,JediStarBotCommand>();
		
		//AJOUTER ICI DE NOUVELLES COMMANDES
		RaidCommand raid = new RaidCommand();
		ModsCommand mods = new ModsCommand();
		AreneCommand arene = new AreneCommand();
		SetUpCommand setup = new SetUpCommand();
		TerritoryBattlesCommand tb = new TerritoryBattlesCommand();
        HelpCommand help = new HelpCommand();
        PayoutCommand payout = new PayoutCommand();

		commandsMap.put(raid.getCommand(), raid);
		commandsMap.put(mods.getCommand(), mods);
		commandsMap.put(arene.getCommand(), arene);
		commandsMap.put(setup.getCommand(),setup);
		commandsMap.put(tb.getCommand(), tb);
        commandsMap.put(help.getCommand(), help);
        commandsMap.put(payout.getCommand(), payout);

		//Lecture du Json
		try {
			JSONObject parameters = StaticVars.jsonSettings;

			//message de base
			MESSAGE = StaticVars.jsonMessages.getString(JSON_BASE_MESSAGE);
			
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
	
	public void onMessageCreate(DiscordAPI api, Message receivedMessage) {
		
		String messageAsString = receivedMessage.getContent().toLowerCase();
		
		//Si le message est vide ou ne commence pas par % : Ne rien faire.
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
		
		insertCommandHistory(command, receivedMessage);
		
		
		if(receivedMessage.getChannelReceiver() != null) {
			receivedMessage.getChannelReceiver().type();
		}

		boolean isAdmin = isAdmin(receivedMessage);
		
		CommandAnswer answer = botCommand.answer(messageParts,receivedMessage,isAdmin);
		
		if(answer == null) {
			return;
		}
		
		String message ="";		
		if(!"".equals(answer.getMessage())) {
			message = String.format(MESSAGE, receivedMessage.getAuthor().getMentionTag(),answer.getMessage());
		}
		
		EmbedBuilder embed = answer.getEmbed();
		
		if(embed != null) {
			embed.addField("-","Bot designed by [JediStar](https://jedistar.jimdo.com)", false);
		}
		
		Future<Message> future = receivedMessage.reply(message, embed);
		
		if(answer.getReactions() != null && !answer.getReactions().isEmpty()) {
			Message sentMessage = null;
			try {
				sentMessage = future.get(1, TimeUnit.MINUTES);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				e.printStackTrace();
				return;
			}
							
			for(String reaction : answer.getReactions()) {
				try {
					sentMessage.addUnicodeReaction(reaction).get(1, TimeUnit.MINUTES);
					Thread.sleep(250);
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					e.printStackTrace();
					return;
				}
			}
			
		}
	}

	private boolean isAdmin(Message messageRecu) {
		
		User author = messageRecu.getAuthor();
		
		if(adminUsers.contains(Integer.parseInt(author.getDiscriminator()))){
			return true;
		}
		
		if(messageRecu.isPrivateMessage()) {
			return false;
		}
		
		for(Role role : author.getRoles(messageRecu.getChannelReceiver().getServer())) {
			if(adminGroups.contains(role.getName())) {
				return true;
			}
		}
		
		return false;
	}

	private void insertCommandHistory(String command,Message receivedMessage) {
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			conn = StaticVars.getJdbcConnection();
			
			stmt = conn.prepareStatement(SQL_INSERT_HISTORY);
			
			stmt.setString(1,command);
			
			java.sql.Timestamp ts = new Timestamp(new Date().getTime());
			stmt.setTimestamp(2, ts);
			
			stmt.setString(3, receivedMessage.getAuthor().getId());
			stmt.setString(4, receivedMessage.getAuthor().getName());
			
			if(receivedMessage.isPrivateMessage()) {
				stmt.setString(5, null);
				stmt.setString(6, null);
				stmt.setString(7, null);
			}
			else {
				Server server = receivedMessage.getChannelReceiver().getServer();
				stmt.setString(5, server.getId());
				stmt.setString(6, server.getName());
				stmt.setString(7, server.getRegion().getName());
			}
			
			logger.debug("executing query "+stmt.toString());
			stmt.executeUpdate();
		}
		catch(SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
				}
			}
		}
	}
}
