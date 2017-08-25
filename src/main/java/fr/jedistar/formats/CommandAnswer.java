package fr.jedistar.formats;

import de.btobastian.javacord.entities.message.embed.EmbedBuilder;

public class CommandAnswer {

	private String message = "";
	private EmbedBuilder embed;


	public CommandAnswer(String message, EmbedBuilder embed) {
		if(message !=null) {
			this.message = message;
		}
		this.embed = embed;
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
