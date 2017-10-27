package fr.jedistar;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.Channel;

public class JediStarBot {

	String token;
	DiscordAPI api = Javacord.getApi(token, true);
	JediStarBotCallback botCallback;

	public JediStarBot(String inToken) {

		token = inToken;

		api = Javacord.getApi(token, true);

		botCallback = new JediStarBotCallback();

	}

	public void connect() {
		api.connect(botCallback);
		
	}

}