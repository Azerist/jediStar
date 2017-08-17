package fr.jedistar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONException;
import org.json.JSONObject;

import fr.jedistar.commands.EquilibrageCommand;
import fr.jedistar.usedapis.SheetsAPIBuilder;

public class Main {
	
	//Noms des éléments dans le fichier de paramètres
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
			
			//Lecture du token Discord
			token = parameters.getString(PARAM_TOKEN);
			
			//Lecture des paramètres pour Google API
			JSONObject googleParams = parameters.getJSONObject(PARAM_GOOGLE_API);
			String googleAuthFile = googleParams.getString("authFile");
			SheetsAPIBuilder.setAuthFilePath(googleAuthFile);
			
			//Id de la Google Sheet pour l'équilibrage
			String googleSheetID = googleParams.getString("equilibrageSheetID");
			EquilibrageCommand.setSheetId(googleSheetID);
			
			
		}
		catch(IOException e) {
			System.out.println("Erreur lors de la lecture du fichier de paramètres "+parametersFilePath);
			e.printStackTrace();
			return;
		}
		catch(JSONException e) {
			System.out.println("Le fichier json de paramètres est mal formaté");
			e.printStackTrace();
		}
		
		System.out.println("Lancement du bot avec le token -"+token+"-");
		JediStarBot bot = new JediStarBot(token);
		bot.connect();
		
	}
}
