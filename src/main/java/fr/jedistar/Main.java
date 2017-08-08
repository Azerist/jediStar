package fr.jedistar;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import fr.jedistar.usedapis.SheetsAPIBuilder;

public class Main {

	public static void main(String ... args) {
		
		if(args.length != 0) {
			System.out.println("Lancement du bot avec le token -"+args[0]+"-");
			JediStarBot bot = new JediStarBot(args[0]);
			bot.connect();
		}
		else {
			System.out.println("Aucun token passé en paramètre");
		}

	}
}
