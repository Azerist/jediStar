package fr.jedistar;

import com.google.common.util.concurrent.FutureCallback;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;

public class JediStarBotCallback implements FutureCallback<DiscordAPI> {

	JediStarBotMessageListener listener;
	
	public JediStarBotCallback() {
		super();
		
		listener = new JediStarBotMessageListener();
	}
	
	public void onFailure(Throwable t) {
		t.printStackTrace();
		
	}

	public void onSuccess(DiscordAPI api) {

		api.registerListener(listener);
		
	}

}
