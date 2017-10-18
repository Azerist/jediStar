package fr.jedistar.formats;

import java.util.Arrays;
import java.util.List;

import de.btobastian.javacord.entities.message.embed.EmbedBuilder;

public class CommandAnswer {

	private String message = "";
	private EmbedBuilder embed;
	private List<String> reactions;

	public CommandAnswer(String message, EmbedBuilder embed,String...reactions) {
		if(message !=null) {
			this.message = message;
		}
		this.embed = embed;
		
		this.reactions = Arrays.asList(reactions);
	}

	public List<String> getReactions() {
		return reactions;
	}

	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {

		if(message != null) {
			this.message = message;
		}
	}
	public EmbedBuilder getEmbed() {
		return embed;
	}
	public void setEmbed(EmbedBuilder embed) {
		this.embed = embed;
	}


}
