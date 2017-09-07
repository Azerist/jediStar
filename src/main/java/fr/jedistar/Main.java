package fr.jedistar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONException;
import org.json.JSONObject;

import fr.jedistar.commands.EquilibrageCommand;
import fr.jedistar.commands.ModsCommand;

public class Main {
	
	//Noms des éléments dans le fichier de paramètres
	private static final String PARAM_MODS_JSON_URI = "modsJsonURI";
	private static final String PARAM_SHEET_ID = "equilibrageSheetID";
	private static final String PARAM_AUTH_FILE = "authFile";
	private static final String PARAM_TOKEN = "discordToken";
	private static final String PARAM_GOOGLE_API = "googleAPI";

	private static final String DEFAULT_PARAMETERS_FILE = "settings.json";
	
	public static void main(String ... args) {
		
		String parametersFilePath = "";
			
		//Si un argument, on l'utilise comme chemin au fichier de paramètres
		if(args.length != 0) {		
			parametersFilePath = args[0];	
		}
		//Sinon, on utilise le chemin par défaut
		else {
			parametersFilePath = DEFAULT_PARAMETERS_FILE;	
		}

		String token = "";
		
		//Lecture du fichier Json et récupération des paramètres
		try {
			//Lecture du fichier
			byte[] encoded = Files.readAllBytes(Paths.get(parametersFilePath));
			String parametersJson = new String(encoded, "utf-8");
			
			//Décodage du json
			JSONObject parameters = new JSONObject(parametersJson);
			
			StaticVars.jsonSettings = parameters;
			
			//METTRE LA LECTURE DES PARAMETRES DU PLUS IMPORTANT AU MOINS IMPORTANT
			//Lecture du token Discord
			token = parameters.getString(PARAM_TOKEN);
			
			//URI et encodage du JSON des mods conseillés
			String modsJsonUri = parameters.getString(PARAM_MODS_JSON_URI);
			ModsCommand.setJsonUri(modsJsonUri);
			
		}
		catch(IOException e) {
			System.out.println("Cannot read the parameters file "+parametersFilePath);
			e.printStackTrace();
			return;
		}
		catch(JSONException e) {
			System.out.println("JSON parameters file is incorrectly formatted");
			e.printStackTrace();
		}
		
		System.out.println("Launching bot with token -"+token+"-");
		JediStarBot bot = new JediStarBot(token);
		bot.connect();
		
	}
}
