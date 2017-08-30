package fr.jedistar;

import java.util.List;

import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.User;
import fr.jedistar.formats.CommandAnswer;

public interface JediStarBotCommand {
	
	public CommandAnswer answer(List<String> params,User author,Channel chan);
}
