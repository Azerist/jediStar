package fr.jedistar;

import java.util.List;

import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.Message;
import fr.jedistar.formats.CommandAnswer;

public interface JediStarBotCommand {

	public String getCommand();

	public CommandAnswer answer(List<String> params, Message receivedMessage, boolean isAdmin);
}
