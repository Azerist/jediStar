package fr.jedistar;

import java.util.List;

import de.btobastian.javacord.entities.User;

public interface JediStarBotCommand {
	
	public String answer(List<String> params,User author);
}
