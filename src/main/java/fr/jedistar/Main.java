package fr.jedistar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.jedistar.commands.ModsCommand;

public class Main {

	final static Logger logger = LoggerFactory.getLogger(Main.class);

	// Noms des éléments dans le fichier de paramètres
	private static final String PARAM_MODS_JSON_URI = "modsJsonURI";
	private static final String PARAM_TOKEN = "discordToken";
	private static final String PARAM_DB = "database";
	private static final String PARAM_DB_URL = "url";
	private static final String PARAM_DB_USER = "user";
	private static final String PARAM_DB_PWD = "pwd";
	private static final String PARAM_MESSAGES = "messages";

	private static final String DEFAULT_PARAMETERS_FILE = "settings.json";
	private static final String DEFAULT_MESSAGE_FILE_NAME = "messages_";
	private static final String DEFAULT_MESSAGE_FILE_EXT = ".json";
	
	private static String url;
	private static String user;
	private static String passwd;

	public static void main(String... args) {

		String parametersFilePath = "";
		String messagesFilePath =DEFAULT_MESSAGE_FILE_NAME ;

		// Si un argument, on l'utilise comme chemin au fichier de param�tres
		if (args.length != 0) {
			parametersFilePath = args[0];
		}
		// Sinon, on utilise le chemin par d�faut
		else {
			parametersFilePath = DEFAULT_PARAMETERS_FILE;
		}

		String token = "";

		// Lecture du fichier Json et récupération des paramètres
		try {
			// Lecture du fichier
			byte[] encoded = Files.readAllBytes(Paths.get(parametersFilePath));
			String parametersJson = new String(encoded, "utf-8");

			// D�codage du json
			JSONObject parameters = new JSONObject(parametersJson);

			StaticVars.jsonSettings = parameters;

			// METTRE LA LECTURE DES PARAMETRES DU PLUS IMPORTANT AU MOINS IMPORTANT
			// Lecture du token Discord
			token = parameters.getString(PARAM_TOKEN);

			// URI et encodage du JSON des mods conseill�s
			String modsJsonUri = parameters.getString(PARAM_MODS_JSON_URI);
			ModsCommand.setJsonUri(modsJsonUri);

			JSONObject dbParams = parameters.getJSONObject(PARAM_DB);
			url = dbParams.getString(PARAM_DB_URL);
			user = dbParams.getString(PARAM_DB_USER);
			passwd = dbParams.getString(PARAM_DB_PWD);
			
			messagesFilePath+=parameters.getString(PARAM_MESSAGES)+DEFAULT_MESSAGE_FILE_EXT;
			

		} catch (IOException e) {
			logger.error("Cannot read the parameters file " + parametersFilePath);
			e.printStackTrace();
			return;
		} catch (JSONException e) {
			logger.error("JSON parameters file is incorrectly formatted");
			e.printStackTrace();
		}
		
		try {
			// Lecture du fichier
			
			byte[] encoded = Files.readAllBytes(Paths.get(messagesFilePath));
			String messagesJson = new String(encoded, "utf-8");

			// D�codage du json
			JSONObject messages = new JSONObject(messagesJson);

			StaticVars.jsonMessages = messages;

		} catch (IOException e) {
			logger.error("Cannot read the parameters file " + parametersFilePath);
			e.printStackTrace();
			return;
		} catch (JSONException e) {
			logger.error("JSON parameters file is incorrectly formatted");
			e.printStackTrace();
		}

		// Initialisation bdd
		try {
			StaticVars.setJdbcParams(url, user, passwd);
			logger.info("testing database connection");
			StaticVars.getJdbcConnection();
			logger.info("database connection OK");
		} catch (SQLException e) {
			logger.error("Error connecting to mysql database");
			e.printStackTrace();
		}

		logger.info("Launching bot with token -" + token + "-");

		JediStarBot bot = new JediStarBot(token);

		bot.connect();

	}
}
