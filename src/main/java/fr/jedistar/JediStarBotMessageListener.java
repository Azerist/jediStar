package fr.jedistar;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;

public class JediStarBotMessageListener implements MessageCreateListener {

	public void onMessageCreate(DiscordAPI api, Message messageRecu) {
		
		if (messageRecu.getContent().equalsIgnoreCase("!ta m�re")) {
			messageRecu.reply("Sale connard "+messageRecu.getAuthor().getMentionTag());
		}
	}

}
