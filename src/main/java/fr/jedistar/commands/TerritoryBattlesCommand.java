package fr.jedistar.commands;

import java.awt.Color;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.embed.EmbedBuilder;
import fr.jedistar.JediStarBotCommand;
import fr.jedistar.StaticVars;
import fr.jedistar.commands.helper.GalaticPowerToStars;
import fr.jedistar.commands.helper.StringFormating;
import fr.jedistar.commands.helper.StringMatcher;
import fr.jedistar.formats.CommandAnswer;
import fr.jedistar.utils.GuildUnitsSWGOHGGDataParser;

public class TerritoryBattlesCommand implements JediStarBotCommand {

	final static Logger logger = LoggerFactory.getLogger(TerritoryBattlesCommand.class);

	private final String COMMAND;
	private final String COMMAND_PLATOON;
	private final String COMMAND_CHARS;
	private final String COMMAND_SHIPS;
	private final String COMMAND_STRATEGY;
	private final String COMMAND_MIN_STRATEGY;

	private final String HELP;

	private final String DISPLAYED_RESULTS;
	private final String NO_UNIT_FOUND;
	private final String MAX_STARS_FROM_GP_TITLE;
	private final String MIN_STARS_FROM_GP_TITLE;
	private final String MAX_STARS_FROM_GP;
	private final String OUTDATED_DATA;
	
	private final String ERROR_MESSAGE;
	private final String ERROR_MESSAGE_SQL;
	private final String ERROR_MESSAGE_NO_CHANNEL;
	private final String ERROR_MESSAGE_NO_GUILD_NUMBER;
	private final String ERROR_MESSAGE_PARAMS_NUMBER;
	private final String ERROR_COMMAND;
	private final String ERROR_INCORRECT_NUMBER;
	private final String ERROR_DB_UPDATE;
	private final String TOO_MUCH_RESULTS;
	private final String ERROR_SWGOHGG_BLOCKER;
	private final String ERROR_SWGOHGG_BUG;


	private final static String SQL_GUILD_ID = "SELECT guildID FROM guild WHERE channelID=?;";
	private final static String SQL_FIND_CHARS = "SELECT * FROM %s WHERE";
	private final static String SQL_FIND_GUILD_UNITS = "SELECT * FROM guildUnits WHERE guildID=? AND charID=? AND rarity>=? ORDER BY power LIMIT 15";
	private final static String SQL_COUNT_GUILD_UNITS = "SELECT COUNT(*) as count FROM guildUnits WHERE guildID=? AND charID=? AND rarity>=?";
	private final static String SQL_SUM_GUILD_UNITS_GP ="SELECT SUM(u.power) as sumGP FROM guildUnits u INNER JOIN characters c ON (c.baseID=u.charID) WHERE guildID=?";
	private final static String SQL_SUM_GUILD_SHIPS_GP = "SELECT SUM(u.power) as sumGP FROM guildUnits u INNER JOIN ships s ON (s.baseID=u.charID) WHERE guildID=?";
	private final static String SQL_FIND_EXPIRATION_DATE = "SELECT MAX(expiration) as expiration FROM guildUnits WHERE guildID=?";
	
	private final static String CHAR_MODE = "characters";
	private final static String SHIP_MODE = "ships";
	
	private final static Color EMBED_COLOR = Color.MAGENTA;
	
	private final static Integer MAX_RESULTS = 4;

	//Nom des champs dans le JSON
	private final static String JSON_ERROR_MESSAGE = "errorMessage";

	private final static String JSON_TB = "territoryBattlesCommandParams";

	private final static String JSON_TB_HELP = "help";

	private final static String JSON_TB_COMMANDS = "commands";
	private final static String JSON_TB_COMMANDS_BASE = "base";
	private final static String JSON_TB_COMMANDS_PLATOON = "platoon";
	private final static String JSON_TB_COMMANDS_CHARS = "characters";
	private final static String JSON_TB_COMMANDS_SHIPS = "ships";
	private final static String JSON_TB_COMMANDS_STRATEGY = "strategy";
	private final static String JSON_TB_COMMANDS_MIN_STRATEGY = "strategyMin";
	
	private final static String JSON_TB_MESSAGES = "messages";
	private final static String JSON_TB_MESSAGES_DISPLAYED_RESULTS = "displayedResults";
	private final static String JSON_TB_MESSAGES_NO_UNTI_FOUND = "noUnitFound";
	private final static String JSON_TB_MESSAGES_MAX_STARS_FROM_GP = "maxStarResult";
	private final static String JSON_TB_MESSAGES_MAX_STARS_FROM_GP_TITLE = "maxStarTitle";
	private final static String JSON_TB_MESSAGES_MIN_STARS_FROM_GP_TITLE = "minStarTitle";
	private final static String JSON_TB_MESSAGES_OUTDATED_DATA = "outdatedData";

	private final static String JSON_TB_ERROR_MESSAGES = "errorMessages";
	private final static String JSON_TB_ERROR_MESSAGES_SQL = "sqlError";
	private final static String JSON_TB_ERROR_MESSAGES_NO_CHANNEL = "noChannel";
	private final static String JSON_TB_ERROR_MESSAGES_NO_GUILD = "noGuildNumber";
	private final static String JSON_TB_ERROR_MESSAGES_PARAMS_NUMBER = "paramsNumber";
	private final static String JSON_TB_ERROR_MESSAGES_COMMAND = "commandError";
	private final static String JSON_TB_ERROR_MESSAGES_INCORRECT_NUMBER = "incorrectNumber";
	private final static String JSON_TB_ERROR_MESSAGES_DB_UPDATE = "dbUpdateError";
	private final static String JSON_TB_TOO_MUCH_RESULTS = "tooMuchResults";
	private final static String JSON_TB_SWGOHGG_BLOCKER = "swgohGGblocker";
	private final static String JSON_TB_SWGOHGG_BUG = "swgohGGbug";

	public TerritoryBattlesCommand() {

		JSONObject parameters = StaticVars.jsonMessages;

		ERROR_MESSAGE = parameters.getString(JSON_ERROR_MESSAGE);

		JSONObject tbParams = parameters.getJSONObject(JSON_TB);

		HELP = tbParams.getString(JSON_TB_HELP);

		JSONObject commands = tbParams.getJSONObject(JSON_TB_COMMANDS);
		COMMAND = commands.getString(JSON_TB_COMMANDS_BASE);
		COMMAND_PLATOON = commands.getString(JSON_TB_COMMANDS_PLATOON);
		COMMAND_CHARS = commands.getString(JSON_TB_COMMANDS_CHARS);
		COMMAND_SHIPS = commands.getString(JSON_TB_COMMANDS_SHIPS);
		COMMAND_STRATEGY = commands.getString(JSON_TB_COMMANDS_STRATEGY);
		COMMAND_MIN_STRATEGY = commands.getString(JSON_TB_COMMANDS_MIN_STRATEGY);

		JSONObject messages = tbParams.getJSONObject(JSON_TB_MESSAGES);
		DISPLAYED_RESULTS = messages.getString(JSON_TB_MESSAGES_DISPLAYED_RESULTS);
		NO_UNIT_FOUND = messages.getString(JSON_TB_MESSAGES_NO_UNTI_FOUND);
		MAX_STARS_FROM_GP = messages.getString(JSON_TB_MESSAGES_MAX_STARS_FROM_GP);
		MAX_STARS_FROM_GP_TITLE = messages.getString(JSON_TB_MESSAGES_MAX_STARS_FROM_GP_TITLE);
		MIN_STARS_FROM_GP_TITLE = messages.getString(JSON_TB_MESSAGES_MIN_STARS_FROM_GP_TITLE);
		OUTDATED_DATA = messages.getString(JSON_TB_MESSAGES_OUTDATED_DATA);
		
		JSONObject errorMessages = tbParams.getJSONObject(JSON_TB_ERROR_MESSAGES);
		ERROR_MESSAGE_SQL = errorMessages.getString(JSON_TB_ERROR_MESSAGES_SQL);
		ERROR_MESSAGE_NO_CHANNEL = errorMessages.getString(JSON_TB_ERROR_MESSAGES_NO_CHANNEL);
		ERROR_MESSAGE_NO_GUILD_NUMBER = errorMessages.getString(JSON_TB_ERROR_MESSAGES_NO_GUILD);
		ERROR_MESSAGE_PARAMS_NUMBER = errorMessages.getString(JSON_TB_ERROR_MESSAGES_PARAMS_NUMBER);
		ERROR_COMMAND = errorMessages.getString(JSON_TB_ERROR_MESSAGES_COMMAND);
		ERROR_INCORRECT_NUMBER = errorMessages.getString(JSON_TB_ERROR_MESSAGES_INCORRECT_NUMBER);
		ERROR_DB_UPDATE = errorMessages.getString(JSON_TB_ERROR_MESSAGES_DB_UPDATE);
		TOO_MUCH_RESULTS = errorMessages.getString(JSON_TB_TOO_MUCH_RESULTS);
		ERROR_SWGOHGG_BLOCKER = errorMessages.getString(JSON_TB_SWGOHGG_BLOCKER);
		ERROR_SWGOHGG_BUG = errorMessages.getString(JSON_TB_SWGOHGG_BUG);
	}

	@Override
	public String getCommand() {
		return COMMAND;
	}

	@Override
	public CommandAnswer answer(List<String> params, Message receivedMessage, boolean isAdmin) {

		
		if(params.size() == 0) {
			return new CommandAnswer(ERROR_MESSAGE_PARAMS_NUMBER,null);
		}
		
		if(COMMAND_STRATEGY.equals(params.get(0))) {
			
			if(params.size() >  2 ) {
				return new CommandAnswer(ERROR_COMMAND,null);
			}
			
			try {
				
				Integer guildID = getGuildIDFromDB(receivedMessage);

				if(guildID == null) {
					return new CommandAnswer(ERROR_MESSAGE_SQL, null);
				}

				if(guildID == -1) {
					return new CommandAnswer(ERROR_MESSAGE_NO_GUILD_NUMBER,null);
				}
				
				EmbedBuilder embed = new EmbedBuilder();
				embed.setColor(EMBED_COLOR);
				
				Integer characterGP = getGPSUM(guildID,SHIP_MODE,true);
				Integer shipGP = getGPSUM(guildID,CHAR_MODE,true);
				
				String errorMessage = "";
				
				if(characterGP == -1 || shipGP == -1) {
					errorMessage = ERROR_MESSAGE_SQL;
				}
				else if(characterGP == -2 || shipGP == -2) {
					errorMessage = ERROR_SWGOHGG_BLOCKER;
				}
				else if(characterGP == -3 || shipGP == -3) {
					errorMessage = ERROR_SWGOHGG_BUG;
				}
				
				if(!StringUtils.isBlank(errorMessage)) {
					Calendar lastUpdate = findLastUpdateDate(guildID);
					
					if(lastUpdate == null) {
						return new CommandAnswer(errorMessage,null);
					}
					
					String message = receivedMessage.getAuthor().getMentionTag();
					
					message += "\r\n"+errorMessage+ "\r\n" + String.format(OUTDATED_DATA, lastUpdate);
					
					receivedMessage.reply(message);
					
					Thread.sleep(50);
				}
				
				characterGP = getGPSUM(guildID,SHIP_MODE,false);
				shipGP = getGPSUM(guildID,CHAR_MODE,false);
				
				GalaticPowerToStars strat = new GalaticPowerToStars(characterGP,shipGP);
				Integer starFromAir = strat.starFromShip;
				Integer starFromGround = strat.starFromCharacter;
				String 	strategyText = strat.strategy;
				String 	title = MAX_STARS_FROM_GP_TITLE;
				
				if(params.size() == 2 && COMMAND_MIN_STRATEGY.equals(params.get(1)))
				{
					starFromAir = strat.minStarFromShip;
					starFromGround =strat.minStarFromCharacter;
					strategyText =strat.minStrategy;
					title =MIN_STARS_FROM_GP_TITLE;
				}
				String result = String.format(MAX_STARS_FROM_GP,StringFormating.formatNumber(characterGP), StringFormating.formatNumber(shipGP),StringFormating.formatNumber(shipGP+characterGP),starFromAir,starFromGround,starFromAir+starFromGround)+strategyText;
				embed.addField(title, result, true);
				return new CommandAnswer(null,embed);
				
			}
			catch(NumberFormatException e) {
				return new CommandAnswer("Invalid number",null);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return null;
			}
		
			

		}
		
			
		if(params.size() < 4) {
			return new CommandAnswer(error(ERROR_MESSAGE_PARAMS_NUMBER),null);
		}

		if(COMMAND_PLATOON.equals(params.get(0))) {
			if(receivedMessage.getChannelReceiver() == null) {
				return new CommandAnswer(ERROR_MESSAGE_NO_CHANNEL,null);
			}

			Integer guildID = getGuildIDFromDB(receivedMessage);

			if(guildID == null) {
				return new CommandAnswer(ERROR_MESSAGE_SQL, null);
			}

			if(guildID == -1) {
				return new CommandAnswer(ERROR_MESSAGE_NO_GUILD_NUMBER,null);
			}
			
			Integer rarity = 0;
			try {
				String rarityAsString = params.get(params.size()-1).replace("*","");
				rarity = Integer.parseInt(rarityAsString);
			}
			catch(NumberFormatException e) {
				logger.warn(e.getMessage());
				return new CommandAnswer(error(ERROR_INCORRECT_NUMBER),null);
			}
			
			//récupérer le nom du perso si celui-ci contient des espaces
			String unitName = params.get(2);
			for(int i=3;i<params.size()-1;i++) {
				unitName += " "+params.get(i);
			}
			
			String retour = error(ERROR_COMMAND);
			if(COMMAND_SHIPS.equals(params.get(1))) {
				retour = findUnits(guildID, SHIP_MODE, unitName, rarity,receivedMessage);
			}
			else if(COMMAND_CHARS.equals(params.get(1))) {
				retour = findUnits(guildID, CHAR_MODE, unitName, rarity,receivedMessage);
			}
			
			return new CommandAnswer(retour,null);
		}

		return new CommandAnswer(error(ERROR_COMMAND),null);
	}

	/**
	 * Gets the guild ID associated with this Discord server from the DB
	 * @param message
	 * @return
	 */
	private Integer getGuildIDFromDB(Message message) {

		String channelID = message.getChannelReceiver().getId();

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			conn = StaticVars.getJdbcConnection();

			stmt = conn.prepareStatement(SQL_GUILD_ID);

			stmt.setString(1,channelID);

			logger.debug("Executing query : "+stmt.toString());

			rs = stmt.executeQuery();

			if(rs.next()) {
				return rs.getInt("guildID");
			}
			return -1;
		}
		catch(SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return null;
		}
		finally {
			try {
				if(rs != null) {
					rs.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				logger.error(e.getMessage());
			}
		}
	}
	
	private Integer getGPSUM(Integer guildID,String mode,boolean refresh) 
	{

		Integer result = -1;
		boolean updateOK = true;
		String request = "";

		if(refresh) {
			try {
				updateOK = updateOK && GuildUnitsSWGOHGGDataParser.parseGuildUnits(guildID);

				if(SHIP_MODE.equals(mode)) {
					updateOK = updateOK && GuildUnitsSWGOHGGDataParser.parseShips();
					request=SQL_SUM_GUILD_UNITS_GP;
				}

				if(CHAR_MODE.equals(mode)) {
					updateOK = updateOK && GuildUnitsSWGOHGGDataParser.parseCharacters();
					request=SQL_SUM_GUILD_SHIPS_GP;
				}
			}

			catch(IOException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
				if(e.getMessage().contains("Server returned HTTP response code: 402")) {
					return -2;
				}

				return -3;
			}

			if(!updateOK) {
				return -1;
			}
		}

			
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			conn = StaticVars.getJdbcConnection();

			stmt = conn.prepareStatement(request);
			
			stmt.setInt(1,guildID);
			
			logger.debug("Executing query : "+stmt.toString());
			rs = stmt.executeQuery();
			
			rs.next();
			
			result = rs.getInt("sumGP");
				
		}
		catch(SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return -1;
		}
		finally {
			try {
				if(rs != null) {
					rs.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				logger.error(e.getMessage());
			}
		}
		
		return result;
	}

	private String findUnits(Integer guildID,String mode,String charName,Integer rarity,Message receivedMessage) {
		
		boolean updateOK = true;
		String errorMessage = "";
		
		try {
			updateOK = updateOK && GuildUnitsSWGOHGGDataParser.parseGuildUnits(guildID);

			if(SHIP_MODE.equals(mode)) {
				updateOK = updateOK && GuildUnitsSWGOHGGDataParser.parseShips();
			}

			if(CHAR_MODE.equals(mode)) {
				updateOK = updateOK && GuildUnitsSWGOHGGDataParser.parseCharacters();
			}
		}
		catch(IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			if(e.getMessage().contains("Server returned HTTP response code: 402")) {
				errorMessage = ERROR_SWGOHGG_BLOCKER;
			}
			else {
				errorMessage= ERROR_SWGOHGG_BUG;
			}
			
			updateOK = false;
		}
		
		if(!updateOK) {
			Calendar lastUpdate = findLastUpdateDate(guildID);
			
			if(lastUpdate == null) {
				return errorMessage;
			}
			
			String message = receivedMessage.getAuthor().getMentionTag();
			
			message += "\r\n"+errorMessage+ "\r\n" + String.format(OUTDATED_DATA, lastUpdate);
			
			receivedMessage.reply(message);
		}
		
		List<Character> charsList = findMatchingCharacters(charName,mode);
		
		if(charsList == null) {
			return ERROR_MESSAGE_SQL;
		}
		
		if(charsList.isEmpty()) {
			return "No character found with this name, approximate matching will come in a future update";
		}
		
		if(charsList.size() > MAX_RESULTS) {
			String returnStr = TOO_MUCH_RESULTS;
			
			for(Character chara : charsList) {
				returnStr += chara.name + "\r\n";
			}
			
			return returnStr;
		}
		
		for(Character chara : charsList) {
			EmbedBuilder embed = new EmbedBuilder();
			embed.setAuthor(chara.name,chara.url,chara.image);
			embed.setThumbnail(chara.image);
			embed.setColor(EMBED_COLOR);
			
			Connection conn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;

			try {
				conn = StaticVars.getJdbcConnection();

				stmt = conn.prepareStatement(SQL_COUNT_GUILD_UNITS);
				
				stmt.setInt(1,guildID);
				stmt.setString(2, chara.baseID);
				stmt.setInt(3, rarity);
				
				rs = stmt.executeQuery();
				
				rs.next();
				
				Integer totalMatchNumber = rs.getInt("count");
				if(totalMatchNumber == 0) {
					embed.setTitle(NO_UNIT_FOUND);
				}
				if(totalMatchNumber > 15) {
					embed.setTitle(String.format(DISPLAYED_RESULTS, totalMatchNumber));
				}
				
				rs.close();
				stmt.close();
				
				if(totalMatchNumber > 0) {
					stmt = conn.prepareStatement(SQL_FIND_GUILD_UNITS);

					stmt.setInt(1,guildID);
					stmt.setString(2, chara.baseID);
					stmt.setInt(3, rarity);

					logger.debug("Executing query : "+stmt.toString());

					rs = stmt.executeQuery();

					Map<Integer,String> contentPerRarity = new HashMap<Integer,String>();

					for(int i=rarity;i<=7;i++) {
						contentPerRarity.put(i, "");
					}

					while(rs.next()) {

						Integer currRarity = rs.getInt("rarity");

						String currentContent = contentPerRarity.get(currRarity);
						Integer intPower = rs.getInt("power");
						String power = NumberFormat.getIntegerInstance().format(intPower);
						currentContent += power +" GP - "+rs.getString("player")+" \r\n";
						contentPerRarity.put(currRarity,currentContent);
					}

					for(Map.Entry<Integer, String> entry : contentPerRarity.entrySet()) {
						if(!StringUtils.isEmpty(entry.getValue())) {
							embed.addField(entry.getKey()+"*",entry.getValue(),true);
						}
					}
				}
				embed.addField("-", "Data from [swgoh.gg](https://swgoh.gg)\r\nBot designed by [JediStar](https://jedistar.jimdo.com)", false);
				receivedMessage.reply(null, embed);
			}
			catch(SQLException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
				return ERROR_MESSAGE_SQL;
			}
			finally {
				try {
					if(rs != null) {
						rs.close();
					}
					if(stmt != null) {
						stmt.close();
					}
				} catch (SQLException e) {
					logger.error(e.getMessage());
				}
			}
		}
		return null;
	}
	
	private List<Character> findMatchingCharacters(String charName,String mode){
		
		List<Character> charList = new ArrayList<Character>();
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<String> potentialMatches = null;
		if(mode.equals(SHIP_MODE))
		{
			potentialMatches =GuildUnitsSWGOHGGDataParser.shipsNames;
		}
		else if (mode.equals(CHAR_MODE))
		{
			potentialMatches =GuildUnitsSWGOHGGDataParser.charactersNames;
		}
		String query = String.format(SQL_FIND_CHARS,mode);
		List<StringMatcher.Match> potentialNames = StringMatcher.getMatch(charName,potentialMatches);
		if(potentialNames.isEmpty())
		{
			return charList;
		}
		for (StringMatcher.Match match : potentialNames)
		{
			query += " name=? OR";
		}
		query = query.substring(0, query.length() -2);

		try {
			conn = StaticVars.getJdbcConnection();

			stmt = conn.prepareStatement(query);

			int i =1;
			for (StringMatcher.Match match : potentialNames)
			{
				stmt.setString(i, match.potentialMatch);
				i++;
			}
			
			logger.debug("Executing query : "+stmt.toString());

			rs = stmt.executeQuery();

			while(rs.next()) {
				charList.add(new Character(rs.getString("name"), rs.getString("baseID"),rs.getString("image"),rs.getString("url")));
			}
			
			return charList;
		}
		catch(SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return null;
		}
		finally {
			try {
				if(rs != null) {
					rs.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				logger.error(e.getMessage());
			}
		}
	}
	
	private Calendar findLastUpdateDate(Integer guildID) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = StaticVars.getJdbcConnection();

			stmt = conn.prepareStatement(SQL_FIND_EXPIRATION_DATE);
	
			stmt.setInt(1, guildID);
			
			logger.debug("Executing query : "+stmt.toString());

			rs = stmt.executeQuery();

			Calendar result = Calendar.getInstance();
			
			while(rs.next()) {
				result.setTime(rs.getDate("expiration"));
			}
			
			result.add(Calendar.HOUR, -24);
			
			return result;
		}
		catch(SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return null;
		}
		finally {
			try {
				if(rs != null) {
					rs.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				logger.error(e.getMessage());
			}
		}
	}
	
	private String error(String message) {
		return ERROR_MESSAGE +"**"+ message + "**\r\n\r\n"+ HELP;
	}
	
	private class Character{
		public String name;
		public String baseID;
		public String image;
		public String url;
		
		public Character(String name,String baseID,String image,String url) {
			this.name= name;
			this.baseID = baseID;
			this.image= image;
			this.url = url;
		}
	}
}
