package fr.jedistar;

public class Main {

	public static void main(String ... args) {

		if(args.length != 0) {
			JediStarBot bot = new JediStarBot(args[0]);
			bot.connect();
		}

	}
}
