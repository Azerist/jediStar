package fr.jedistar.commands.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import fr.jedistar.utils.JaroWinklerDistance;

public class StringMatcher {

	public static class Match implements Comparable<Match>{

		public String potentialMatch;
		public Double score;
		
		@Override
		public int compareTo(Match other) {			
			return -1 * (int) ((this.score - other.score)*1000);
		}
		
		public int compareTo(Double other) {			
			return -1 * (int) ((this.score - other)*1000);
		}
		
	}
	
	public static List<Match> getMatch (String textToMatch, List<String> potentialMatchs)
	{
		List<Match> returnedMatches = new ArrayList<Match>();
		Double currentMaxScore = 0.8;
		for(String potentialMatch : potentialMatchs) 
		{
			Double jaroWinkler = new JaroWinklerDistance().applyWithSubString(textToMatch, potentialMatch);	
			Match match = new Match();
			match.score = jaroWinkler;
			match.potentialMatch = potentialMatch;
			if(match.compareTo(currentMaxScore) < 0)
			{
				returnedMatches.clear();
				returnedMatches.add(match);
				currentMaxScore = jaroWinkler;
			} 
			else if(match.compareTo(currentMaxScore) == 0)
			{
				returnedMatches.add(match);
			}
		}
	
		return returnedMatches;
	}
	
	
	
	
}
