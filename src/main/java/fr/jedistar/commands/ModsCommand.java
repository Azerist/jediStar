package fr.jedistar.commands;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.embed.EmbedBuilder;
import fr.jedistar.JediStarBotCommand;
import fr.jedistar.formats.CommandAnswer;
import fr.jedistar.usedapis.JaroWinklerDistance;


public class ModsCommand implements JediStarBotCommand {

	private static final int MAX_LENGTH = 1950;
	private static final int MAX_ANSWERS = 4;

	public final static String COMMAND = "mods";
	
	private final static String APPROX_MATCHES_MESSAGES = "\r\n**Voici des personnages qui ressemblent à votre recherche :**\r\n\r\n";
	private final static String CHAR_MESSAGE = "**  set1** : %s\r\n  **set2** : %s\r\n  **set3** : %s\r\n\r\n  **☐** : %s\r\n  **➚** : %s\r\n  **◆** : %s\r\n  **Δ** : %s\r\n  **O** : %s\r\n  **✙** : %s\r\n  ";

	private final static String HELP = "Cette commande vous permet de connaître les mods recommandés pour un personnage.\r\n\r\n**Exemple d'appel**\r\n!mods anakin";
	private final static String ERROR_MESSAGE = "Merci de faire appel à moi, mais je ne peux pas te répondre pour la raison suivante :\r\n";
	private final static String PARAMS_ERROR = "L'API de mods n'est pas correctement configurée. Impossible d'utiliser cette fonction.";
	private final static String ACCESS_ERROR = "Impossible d'accéder à l'API de mods. Impossible d'utiliser cette fonction.";
	private final static String JSON_ERROR = "L'API de mods a renvoyé une réponse mal formatée. Impossible d'utiliser cette fonction.";
	private final static String MESSAGE_TOO_LONG = "**La réponse détaillée est trop longue pour être affichée sur Discord.\r\nVoici la liste des personnages correspondant à votre recherche :**\r\n";
	
	private final static String EMBED_TITLE = "Recherche de mods pour «%s»";
	private final static Color EMBED_COLOR = Color.GREEN;
	//Nom des éléments dans le JSON
	private final static String JSON_DATA = "data";
	private final static String JSON_NAME = "name";
	private final static String JSON_SHORT = "short";
	private final static String JSON_SET1 = "set1";
	private final static String JSON_SET2 = "set2";
	private final static String JSON_SET3 = "set3";
	private final static String JSON_SQUARE = "square";
	private final static String JSON_ARROW = "arrow";
	private final static String JSON_DIAMOND = "diamond";
	private final static String JSON_TRIANGLE = "triangle";
	private final static String JSON_CIRCLE = "circle";
	private final static String JSON_CROSS = "cross";

	private static String JSON_URI = null;

	public static void setJsonUri(String uri) {
		JSON_URI = uri;
	}

	@Override
	public CommandAnswer answer(List<String> params, User author,Channel chan) {

		if(params.size() == 0) {
			return error(HELP);
		}
		
		String requestedCharacterName = String.join(" ",params);
		
		try {
			JSONObject modsJsonRoot = getHttpJsonFile();
			
			JSONArray dataArray = modsJsonRoot.getJSONArray(JSON_DATA);
			
			List<Match> exactMatches = new ArrayList<Match>();
			List<Match> approxMatches = new ArrayList<Match>();
						
			//Itérer sur les personnages présents dans le json
			for(int i=0;i<dataArray.length();i++) {
				
				JSONObject charData = dataArray.getJSONObject(i);
				
				//Comparer le nom du personnage avec la recherche
				String charName = charData.getString(JSON_NAME).toLowerCase();			
				String charShortName = charData.getString(JSON_SHORT);
				Double jaroWinkler = new JaroWinklerDistance().apply(requestedCharacterName, charName);			
				
				Match match = new Match();
				match.score = jaroWinkler;
				match.value = formatMessageForChar(charData);
				match.charName = charData.getString(JSON_NAME) + "\r\n";
				
				//Correspondances exactes
				if(requestedCharacterName.length() > 2 && (charName.contains(requestedCharacterName) || requestedCharacterName.contains(charName))) {
					exactMatches.add(match);
				}
				else if(charShortName.contains(requestedCharacterName)) {
					exactMatches.add(match);
				}
				//Correspondance approximative				
				else if(jaroWinkler > 0) {
					approxMatches.add(match);
				}
			}
			
			String message = "";
			EmbedBuilder embed = new EmbedBuilder();
			embed.setTitle(String.format(EMBED_TITLE, requestedCharacterName));
			embed.setColor(EMBED_COLOR);
			
			boolean embedEmpty = true;
			
			Collections.sort(exactMatches);
			
			//Si trop de réponses, on renvoie simplement la liste de noms
			if(exactMatches.size() > MAX_ANSWERS) {
				message = MESSAGE_TOO_LONG;
				for(Match match : exactMatches) {
					message += match.charName;
				}
			}
			else {
				//sinon, on renvoi la réponse détaillée
				for(Match match : exactMatches) {
					embed.addField(match.charName, match.value, true);
					embedEmpty = false;
				}
			}
			
			
			//Si pas de corresp. exactes, on renvoie les correspondances approx., en baissant progressivement le niveau de tolérance
			if(exactMatches.isEmpty() && !approxMatches.isEmpty()) {
				message += APPROX_MATCHES_MESSAGES;
				
				Collections.sort(approxMatches);
					
				boolean nothingFound = true;
				
				for(Double currentThreshold = 0.7 ; currentThreshold > 0 && nothingFound; currentThreshold -= 0.2) {
					for(Match approx : approxMatches) {
						
						if(message.length() + approx.charName.length() > MAX_LENGTH) {
							break;
						}
						
						if(approx.score < currentThreshold) {
							break;
						}
						
						message += approx.charName;			
						nothingFound = false;
						
					}
				}
				
			}

			if(embedEmpty) {
				embed = null;
			}
			
			return new CommandAnswer(message,embed);
		}
		catch (MalformedURLException|UnsupportedEncodingException e) {
			e.printStackTrace();
			return error(PARAMS_ERROR);
		} 
		catch (IOException e) {
			e.printStackTrace();
			return error(ACCESS_ERROR);
		}
		catch (JSONException e) {
			e.printStackTrace();
			return error(JSON_ERROR);
		}
	}

	/**
	 * Lit le fichier JSON via HTTP
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private JSONObject getHttpJsonFile() throws MalformedURLException, IOException, UnsupportedEncodingException {
		
		Date now = new Date();
		
		URL url = new URL(JSON_URI);
		URLConnection connection = url.openConnection();
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
		connection.connect();

		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			String json = in.readLine();
			
			String timeMillis = ((Long)(new Date().getTime() - now.getTime())).toString() + "\r\n";
			Files.write(Paths.get("httpLatencyMonitor"), timeMillis.getBytes(), StandardOpenOption.APPEND);
			return new JSONObject(json);
		}
		finally {
			if(in != null) {
				in.close();
			}
		}
	}
	
	/**
	 * Remplit le message avec les données contenues dans le JsonObject
	 * @param charData
	 * @return le message formaté
	 */
	private String formatMessageForChar(JSONObject charData) {
		return String.format(CHAR_MESSAGE,
								charData.get(JSON_SET1),
								charData.get(JSON_SET2),
								charData.get(JSON_SET3),
								charData.get(JSON_SQUARE),
								charData.get(JSON_ARROW),
								charData.get(JSON_DIAMOND),
								charData.get(JSON_TRIANGLE),
								charData.get(JSON_CIRCLE),
								charData.get(JSON_CROSS)
							);
	}

	private CommandAnswer error(String errorMessage) {
		String message = ERROR_MESSAGE +"**"+ errorMessage + "**\r\n\r\n"+ HELP;
		
		return new CommandAnswer(message, null);
	}
	
	private class Match implements Comparable<Match>{

		public String value;
		public String charName;
		public Double score;
		
		@Override
		public int compareTo(Match other) {			
			return -1 * (int) ((this.score - other.score)*1000);
		}
		
	}
}
