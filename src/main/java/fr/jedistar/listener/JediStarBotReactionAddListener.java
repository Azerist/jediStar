package fr.jedistar.listener;

import java.util.ArrayList;
import java.util.List;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.Reaction;
import de.btobastian.javacord.listener.message.ReactionAddListener;
import fr.jedistar.formats.PendingAction;

public class JediStarBotReactionAddListener implements ReactionAddListener {

	private static List<PendingAction> pendingActions = new ArrayList<PendingAction>();
	
	@Override
	public void onReactionAdd(DiscordAPI api, Reaction reaction, User user) {
		
		if(!reaction.isUsedByYou()) {
			return;
		}
		
		for(PendingAction action:pendingActions) {
			
			if(action.isExpired()) {
				pendingActions.remove(action);
			}
			
			if(action.getUser().getId().equals(user.getId())
					&& action.getMessage().getId().equals(reaction.getMessage().getId())) {
				
				action.doAction(reaction);
				pendingActions.remove(action);
				return;
			}
		}
	}

	public static void addPendingAction(PendingAction action) {
		pendingActions.add(action);
	}
}
