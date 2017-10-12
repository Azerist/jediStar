package fr.jedistar.commands.helper;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;



public class GalaticPowerToStars {
	private Integer CharacterGP = 0;
	private Integer ShipGP = 0;
	public Integer StarFromShip =0;
	public Integer StarFromCharacter=0;
	public String 	Strategy ="";
	private TerritoryBattleStrategy GroundStrategy;
	private TerritoryBattleStrategy AirStrategy;
	
	private static class TerritoryBattleStrategy
	{
				
			private Integer starCount;
			private String[] strategies;
			
			public TerritoryBattleStrategy(Integer starCount,String[] strategies)
			{
				this.starCount=starCount;
				this.strategies=strategies;
			}
	}
	
	
	private static final SortedMap<Integer, TerritoryBattleStrategy> groundGPMap = Collections.unmodifiableSortedMap(
		    new TreeMap<Integer, TerritoryBattleStrategy>() {{
		    	put(0		, 	new TerritoryBattleStrategy(19,new String[] {"2","2/2","2/2","2/2","1/2","1/1"}));
		    	put(6350000	, 	new TerritoryBattleStrategy(20,new String[] {"2","2/2","2/2","2/2","1/2","1/2"}));
		    	put(12570000, 	new TerritoryBattleStrategy(21,new String[] {"2","2/3","2/2","2/2","1/2","1/2"}));
		    	put(12680000, 	new TerritoryBattleStrategy(22,new String[] {"2","2/3","2/2","2/2","1/3","1/2"}));
		    	put(22040000, 	new TerritoryBattleStrategy(23,new String[] {"2","2/3","2/3","2/2","1/3","1/2"}));
		    	put(26300000, 	new TerritoryBattleStrategy(24,new String[] {"3","2/3","2/3","2/2","1/3","1/2"}));
		    	put(28550000, 	new TerritoryBattleStrategy(25,new String[] {"3","2/3","2/3","2/2","1/3","1/3"}));
		    	put(31610000, 	new TerritoryBattleStrategy(26,new String[] {"3","2/3","2/3","2/3","1/3","1/3"}));
		    	put(47770000, 	new TerritoryBattleStrategy(27,new String[] {"3","3/3","2/3","2/3","1/3","1/3"}));
		    	put(47880000, 	new TerritoryBattleStrategy(28,new String[] {"3","3/3","2/3","2/3","2/3","1/3"}));
		    	put(59240000, 	new TerritoryBattleStrategy(29,new String[] {"3","3/3","3/3","2/3","2/3","1/3"}));
		    	put(69550000, 	new TerritoryBattleStrategy(30,new String[] {"3","3/3","3/3","2/3","2/3","2/3"}));
		    	put(75010000, 	new TerritoryBattleStrategy(31,new String[] {"3","3/3","3/3","3/3","2/3","2/3"}));
		    	put(88380000, 	new TerritoryBattleStrategy(32,new String[] {"3","3/3","3/3","3/3","3/3","2/3"}));
		    	put(97550000, 	new TerritoryBattleStrategy(33,new String[] {"3","3/3","3/3","3/3","3/3","3/3"}));
				     		       
		    }});
	
	private static final SortedMap<Integer, TerritoryBattleStrategy> airGPMap = Collections.unmodifiableSortedMap(
		    new TreeMap<Integer, TerritoryBattleStrategy>() {{
		    	put(0		, 	new TerritoryBattleStrategy(6,new String[] {"","","2/","2/","1/","1/"}));
		    	put(4940000		, 	new TerritoryBattleStrategy(7,new String[] {"","","2/","3/","1/","1/"}));
		    	put(6120000		, 	new TerritoryBattleStrategy(8,new String[] {"","","2/","3/","2/","1/"}));
		    	put(6910000		, 	new TerritoryBattleStrategy(9,new String[] {"","","3/","3/","2/","1/"}));
		    	put(8900000		, 	new TerritoryBattleStrategy(10,new String[] {"","","3/","3/","2/","2/"}));
		    	put(22120000	, 	new TerritoryBattleStrategy(11,new String[] {"","","3/","3/","3/","2/"}));
		    	put(28100000	, 	new TerritoryBattleStrategy(12,new String[] {"","","3/","3/","3/","3/"}));
		    		     		       
		    }});
	
	public GalaticPowerToStars(Integer CharacterGP,Integer ShipGP)
	{
		this.CharacterGP =CharacterGP;
		this.ShipGP =ShipGP;
		this.GroundStrategy = getStrategyFromGP(CharacterGP,groundGPMap);
		this.AirStrategy = getStrategyFromGP(ShipGP,airGPMap);
		this.StarFromShip =AirStrategy.starCount;
		this.StarFromCharacter=GroundStrategy.starCount;
		FormatGroundAndAirStrategy();
		
	}
	
	private TerritoryBattleStrategy getStrategyFromGP(Integer GP,SortedMap<Integer, TerritoryBattleStrategy> possibleStrategy)
	{
		TerritoryBattleStrategy bestStrategy;
		SortedMap<Integer, TerritoryBattleStrategy> reachableGroundStars = possibleStrategy.headMap(GP);
		if(reachableGroundStars.isEmpty())
		{
			bestStrategy = possibleStrategy.get(possibleStrategy.firstKey());
		}
		else
		{
			bestStrategy = reachableGroundStars.get(reachableGroundStars.lastKey());
		}
		return bestStrategy;
	}
	
	private void FormatGroundAndAirStrategy()
	{
		Strategy = "";
		for(int i =0;i<GroundStrategy.strategies.length;i++)
		{
			Strategy +="P"+ (i+1)+" "+AirStrategy.strategies[i]+GroundStrategy.strategies[i]+"\r\n";
		}
	}

}
