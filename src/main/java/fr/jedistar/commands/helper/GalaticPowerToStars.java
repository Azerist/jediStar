package fr.jedistar.commands.helper;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;



public class GalaticPowerToStars {
	
	public class StarInfo
	{		
			public Integer starCount;
			public Integer GPMargin;
			public Integer GPToNextStar;
			public StarInfo(TerritoryBattleStrategy strategy)
			{
				this.starCount = strategy.starCount;
				this.GPMargin = strategy.GPMargin;
				this.GPToNextStar = strategy.GPToNextStar;
			}
			public String FormatToString(String margin,String Next)
			{
				String sResult = String.format(margin,starCount,StringFormating.formatNumber(GPMargin));
				if(GPToNextStar != 0)
				{
					sResult += String.format(Next,StringFormating.formatNumber(GPToNextStar));
				}
				return sResult;
			}
	}
	
	private Integer characterGP;
	private Integer shipGP;
	public StarInfo starFromShip;
	public StarInfo starFromCharacter;
	public StarInfo minStarFromShip;
	public StarInfo minStarFromCharacter;
	public String 	strategy ="";
	public String 	minStrategy ="";
	public Boolean isDarkSide = false;
	
	private TerritoryBattleStrategy groundStrategy;
	private TerritoryBattleStrategy airStrategy;
	private TerritoryBattleStrategy minGroundStrategy;
	private TerritoryBattleStrategy minAirStrategy;
	
	private static class TerritoryBattleStrategy
	{
				
			private Integer starCount;
			private String[] strategies;
			private Integer GPMargin = 0;
			private Integer GPToNextStar = 0;
			
			public TerritoryBattleStrategy(Integer starCount,String[] strategies)
			{
				this.starCount=starCount;
				this.strategies=strategies;
			}
	}
	
	
	private static final SortedMap<Integer, TerritoryBattleStrategy> groundGPMap = Collections.unmodifiableSortedMap(
		    new TreeMap<Integer, TerritoryBattleStrategy>() {/**
				 * 
				 */
				private static final long serialVersionUID = 8633415667127566560L;

			{
		    	put(0		, 	new TerritoryBattleStrategy(19,new String[] {"2","2/2","2/2","2/2","1/2","1/1"}));
		    	put(6350000	, 	new TerritoryBattleStrategy(20,new String[] {"2","2/2","2/2","2/2","1/2","1/2"}));
		    	put(12570000, 	new TerritoryBattleStrategy(21,new String[] {"2","2/3","2/2","2/2","1/2","1/2"}));
		    	put(12680000, 	new TerritoryBattleStrategy(22,new String[] {"2","2/3","2/2","2/2","1/3","1/2"}));
		    	put(15800000, 	new TerritoryBattleStrategy(24,new String[] {"3","2/3","2/2","2/2","1/3","1/2"}));
		    	put(22040000, 	new TerritoryBattleStrategy(23,new String[] {"3","2/3","2/3","2/2","1/3","1/2"}));
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
		    new TreeMap<Integer, TerritoryBattleStrategy>() {/**
				 * 
				 */
				private static final long serialVersionUID = 8926699849073076427L;

			{
		    	put(0		, 	new TerritoryBattleStrategy(6,new String[] {"","","2/","2/","1/","1/"}));
		    	put(4940000		, 	new TerritoryBattleStrategy(7,new String[] {"","","2/","3/","1/","1/"}));
		    	put(6120000		, 	new TerritoryBattleStrategy(8,new String[] {"","","2/","3/","2/","1/"}));
		    	put(6910000		, 	new TerritoryBattleStrategy(9,new String[] {"","","3/","3/","2/","1/"}));
		    	put(8900000		, 	new TerritoryBattleStrategy(10,new String[] {"","","3/","3/","2/","2/"}));
		    	put(22120000	, 	new TerritoryBattleStrategy(11,new String[] {"","","3/","3/","3/","2/"}));
		    	put(28100000	, 	new TerritoryBattleStrategy(12,new String[] {"","","3/","3/","3/","3/"}));	    		     		       
		    }});
	
	private static final SortedMap<Integer, TerritoryBattleStrategy> groundGPMinMap = Collections.unmodifiableSortedMap(
		    new TreeMap<Integer, TerritoryBattleStrategy>() {{
		    	put(0			, 	new TerritoryBattleStrategy(0 ,new String[] {"0","0/0","0/0","0/0","0/0","0/0"}));
		    	put(885000		, 	new TerritoryBattleStrategy(1 ,new String[] {"1","0/0","0/0","0/0","0/0","0/0"}));
		    	put(1900000		, 	new TerritoryBattleStrategy(2 ,new String[] {"1","1/0","0/0","0/0","0/0","0/0"}));
		    	put(3510000		, 	new TerritoryBattleStrategy(3 ,new String[] {"1","1/0","1/0","0/0","0/0","0/0"}));
		    	put(3800000		, 	new TerritoryBattleStrategy(4 ,new String[] {"1","1/1","1/0","0/0","0/0","0/0"}));
		    	put(5220000		, 	new TerritoryBattleStrategy(5 ,new String[] {"1","1/1","1/0","1/0","0/0","0/0"}));
		    	put(6580000		, 	new TerritoryBattleStrategy(6 ,new String[] {"2","1/1","1/0","1/0","0/0","0/0"}));
		    	put(7020000		, 	new TerritoryBattleStrategy(7 ,new String[] {"2","1/1","1/1","1/0","0/0","0/0"}));
		    	put(10440000	, 	new TerritoryBattleStrategy(8 ,new String[] {"2","1/1","1/1","1/1","0/0","0/0"}));
		    	put(11100000	, 	new TerritoryBattleStrategy(9 ,new String[] {"2","1/1","1/1","1/1","0/1","0/0"}));
		    	put(17300000	, 	new TerritoryBattleStrategy(10,new String[] {"2","1/2","1/1","1/1","0/1","0/0"}));
		    	put(25200000	, 	new TerritoryBattleStrategy(11,new String[] {"2","1/2","1/1","1/1","1/1","0/0"}));
		    	put(25910000	, 	new TerritoryBattleStrategy(12,new String[] {"2","1/2","1/2","1/1","1/1","0/0"}));
		    	put(26400000	, 	new TerritoryBattleStrategy(13,new String[] {"2","1/2","1/2","1/1","1/1","0/1"}));
		    	put(33520000	, 	new TerritoryBattleStrategy(14,new String[] {"2","1/2","1/2","1/2","1/1","0/1"}));
		    	put(35200000	, 	new TerritoryBattleStrategy(15,new String[] {"2","2/2","1/2","1/2","1/1","0/1"}));
		    	put(45600000	, 	new TerritoryBattleStrategy(16,new String[] {"3","2/2","1/2","1/2","1/1","0/1"}));
		    	put(50000000	, 	new TerritoryBattleStrategy(17,new String[] {"3","2/2","2/2","1/2","1/1","0/1"}));
		    	put(55100000	, 	new TerritoryBattleStrategy(18,new String[] {"3","2/2","2/2","1/2","1/2","0/1"}));
		    	put(57400000	, 	new TerritoryBattleStrategy(19,new String[] {"3","2/2","2/2","1/2","1/2","1/1"}));
		    	put(63000000	, 	new TerritoryBattleStrategy(20,new String[] {"3","2/2","2/2","2/2","1/2","1/1"}));
		    	put(63600000	, 	new TerritoryBattleStrategy(21,new String[] {"3","2/3","2/2","2/2","1/2","1/1"}));
		    	put(79800000	, 	new TerritoryBattleStrategy(22,new String[] {"3","2/3","2/3","2/2","1/2","1/1"}));
		    	put(85700000	, 	new TerritoryBattleStrategy(23,new String[] {"3","2/3","2/3","2/2","1/3","1/1"}));
		    	put(90300000	, 	new TerritoryBattleStrategy(24,new String[] {"3","2/3","2/3","2/2","1/3","1/2"}));
		    	put(97300000	, 	new TerritoryBattleStrategy(25,new String[] {"3","2/3","2/3","2/3","1/3","1/2"}));
		    	put(98800000	, 	new TerritoryBattleStrategy(26,new String[] {"3","3/3","2/3","2/3","1/3","1/2"}));
		    	put(112500000	, 	new TerritoryBattleStrategy(27,new String[] {"3","3/3","2/3","2/3","1/3","1/3"}));
		    	put(117000000	, 	new TerritoryBattleStrategy(28,new String[] {"3","3/3","3/3","2/3","1/3","1/3"}));
		    	put(120900000	, 	new TerritoryBattleStrategy(29,new String[] {"3","3/3","3/3","2/3","2/3","1/3"}));
		    	put(140700000	, 	new TerritoryBattleStrategy(30,new String[] {"3","3/3","3/3","3/3","2/3","1/3"}));
		    	put(153500000	, 	new TerritoryBattleStrategy(31,new String[] {"3","3/3","3/3","3/3","2/3","2/3"}));
		    	put(161400000	, 	new TerritoryBattleStrategy(32,new String[] {"3","3/3","3/3","3/3","3/3","2/3"}));
		    	put(181500000	, 	new TerritoryBattleStrategy(33,new String[] {"3","3/3","3/3","3/3","3/3","3/3"}));
			 	    		       
		    }});
	
	private static final SortedMap<Integer, TerritoryBattleStrategy> airGPMinMap = Collections.unmodifiableSortedMap(
		    new TreeMap<Integer, TerritoryBattleStrategy>() {{
		    	put(0			, 	new TerritoryBattleStrategy(0 ,new String[] {"","","0/","0/","0/","0/"}));
		    	put(1920000		, 	new TerritoryBattleStrategy(1 ,new String[] {"","","1/","0/","0/","0/"}));
		    	put(2176000		, 	new TerritoryBattleStrategy(2 ,new String[] {"","","1/","1/","0/","0/"}));
		    	put(16500000	, 	new TerritoryBattleStrategy(3 ,new String[] {"","","2/","1/","0/","0/"}));
		    	put(18000000	, 	new TerritoryBattleStrategy(4 ,new String[] {"","","2/","1/","1/","0/"}));
		    	put(18700000	, 	new TerritoryBattleStrategy(5 ,new String[] {"","","2/","2/","1/","0/"}));
		    	put(21600000	, 	new TerritoryBattleStrategy(6 ,new String[] {"","","2/","2/","1/","1/"}));
		    	put(26300000	, 	new TerritoryBattleStrategy(7 ,new String[] {"","","3/","2/","1/","1/"}));
		    	put(29800000	, 	new TerritoryBattleStrategy(8 ,new String[] {"","","3/","3/","1/","1/"}));
		    	put(34000000	, 	new TerritoryBattleStrategy(9 ,new String[] {"","","3/","3/","2/","1/"}));
		    	put(40800000	, 	new TerritoryBattleStrategy(10,new String[] {"","","3/","3/","2/","2/"}));
		    	put(50000000	, 	new TerritoryBattleStrategy(11,new String[] {"","","3/","3/","3/","2/"}));
		    	put(60000000	, 	new TerritoryBattleStrategy(12,new String[] {"","","3/","3/","3/","3/"}));
			 	     		       
		    }});
	
	private static final SortedMap<Integer, TerritoryBattleStrategy> groundGPMapDS = Collections.unmodifiableSortedMap(
		    new TreeMap<Integer, TerritoryBattleStrategy>() {/**
				 * 
				 */
				private static final long serialVersionUID = 8633415667127566560L;

			{
				put(0			, 	new TerritoryBattleStrategy(21,new String[] {"2/2","2/2","2/2","1/2","1/2","1/2"}));
				put(858000		, 	new TerritoryBattleStrategy(22,new String[] {"2/2","2/2","2/2","2/2","1/2","1/2"}));
				put(12806000	, 	new TerritoryBattleStrategy(23,new String[] {"2/2","2/2","2/2","2/2","1/3","1/2"}));
				put(13266000	, 	new TerritoryBattleStrategy(24,new String[] {"2/2","2/3","2/2","2/2","1/3","1/2"}));
				put(14706000	, 	new TerritoryBattleStrategy(25,new String[] {"2/3","2/3","2/2","2/2","1/3","1/2"}));
				put(21678000	, 	new TerritoryBattleStrategy(26,new String[] {"2/3","2/3","2/3","2/2","1/3","1/2"}));
				put(28380000	, 	new TerritoryBattleStrategy(27,new String[] {"2/3","2/3","2/3","2/2","1/3","1/3"}));
				put(33283000	, 	new TerritoryBattleStrategy(28,new String[] {"2/3","2/3","2/3","2/3","1/3","1/3"}));
				put(50226000	, 	new TerritoryBattleStrategy(29,new String[] {"2/3","3/3","2/3","2/3","1/3","1/3"}));
				put(50471000	, 	new TerritoryBattleStrategy(30,new String[] {"2/3","3/3","2/3","2/3","2/3","1/3"}));
				put(55677000	, 	new TerritoryBattleStrategy(31,new String[] {"3/3","3/3","2/3","2/3","2/3","1/3"}));
				put(63978000	, 	new TerritoryBattleStrategy(32,new String[] {"3/3","3/3","3/3","2/3","2/3","1/3"}));
				put(76580000	, 	new TerritoryBattleStrategy(33,new String[] {"3/3","3/3","3/3","2/3","2/3","2/3"}));
				put(82758000	, 	new TerritoryBattleStrategy(34,new String[] {"3/3","3/3","3/3","3/3","2/3","2/3"}));
				put(101976000	, 	new TerritoryBattleStrategy(35,new String[] {"3/3","3/3","3/3","3/3","3/3","2/3"}));
				put(127380000	, 	new TerritoryBattleStrategy(35,new String[] {"3/3","3/3","3/3","3/3","3/3","3/3"}));   	     		       
		    }});
	
	private static final SortedMap<Integer, TerritoryBattleStrategy> airGPMapDS = Collections.unmodifiableSortedMap(
		    new TreeMap<Integer, TerritoryBattleStrategy>() {/**
				 * 
				 */
				private static final long serialVersionUID = 8926699849073076427L;

			{
		    	put(0			, 	new TerritoryBattleStrategy(6,new String[] {"","","2/","2/","1/","1/"}));
		    	put(3160000		, 	new TerritoryBattleStrategy(7,new String[] {"","","2/","2/","1/","2/"}));
		    	put(3469000		, 	new TerritoryBattleStrategy(8,new String[] {"","","2/","2/","2/","2/"}));
		    	put(5248000		, 	new TerritoryBattleStrategy(9,new String[] {"","","2/","3/","2/","2/"}));
		    	put(7974000		, 	new TerritoryBattleStrategy(10,new String[] {"","","3/","3/","2/","2/"}));
		    	put(25269000	, 	new TerritoryBattleStrategy(11,new String[] {"","","3/","3/","3/","2/"}));
		    	put(36280000	, 	new TerritoryBattleStrategy(12,new String[] {"","","3/","3/","3/","3/"}));	    		     		       
		    }});
	
	private static final SortedMap<Integer, TerritoryBattleStrategy> groundGPMinMapDS = Collections.unmodifiableSortedMap(
		    new TreeMap<Integer, TerritoryBattleStrategy>() {{
		    	put(0			, 	new TerritoryBattleStrategy(0 ,new String[] {"0/0","0/0","0/0","0/0","0/0","0/0"}));
		    	put(885000		, 	new TerritoryBattleStrategy(1 ,new String[] {"1/0","0/0","0/0","0/0","0/0","0/0"}));
		    	put(1330000		, 	new TerritoryBattleStrategy(2 ,new String[] {"1/1","0/0","0/0","0/0","0/0","0/0"}));
		    	put(1900000		, 	new TerritoryBattleStrategy(3 ,new String[] {"1/1","1/0","0/0","0/0","0/0","0/0"}));
		    	put(3510000		, 	new TerritoryBattleStrategy(4 ,new String[] {"1/1","1/0","1/0","0/0","0/0","0/0"}));
		    	put(3800000		, 	new TerritoryBattleStrategy(5 ,new String[] {"1/1","1/1","1/0","0/0","0/0","0/0"}));
		    	put(3919000		, 	new TerritoryBattleStrategy(6 ,new String[] {"1/2","1/1","1/0","0/0","0/0","0/0"}));
		    	put(5220000		, 	new TerritoryBattleStrategy(7 ,new String[] {"1/2","1/1","1/0","1/0","0/0","0/0"}));
		    	put(7020000		, 	new TerritoryBattleStrategy(8 ,new String[] {"1/2","1/1","1/1","1/0","0/0","0/0"}));
		    	put(10383000	, 	new TerritoryBattleStrategy(9 ,new String[] {"2/2","1/1","1/1","1/0","0/0","0/0"}));
		    	put(10440000	, 	new TerritoryBattleStrategy(10,new String[] {"2/2","1/1","1/1","1/1","0/0","0/0"}));
		    	put(11100000	, 	new TerritoryBattleStrategy(11,new String[] {"2/2","1/1","1/1","1/1","0/1","0/0"}));
		    	put(18070000	, 	new TerritoryBattleStrategy(12,new String[] {"2/2","1/2","1/1","1/1","0/1","0/0"}));
		    	put(25200000	, 	new TerritoryBattleStrategy(13,new String[] {"2/2","1/2","1/1","1/1","1/1","0/0"}));
		    	put(26400000	, 	new TerritoryBattleStrategy(14,new String[] {"2/2","1/2","1/1","1/1","1/1","0/1"}));
		    	put(27030000	, 	new TerritoryBattleStrategy(15,new String[] {"2/2","1/2","1/2","1/1","1/1","0/1"}));
		    	put(30984000	, 	new TerritoryBattleStrategy(16,new String[] {"2/3","1/2","1/2","1/1","1/1","0/1"}));
		    	put(36960000	, 	new TerritoryBattleStrategy(17,new String[] {"2/3","2/2","1/2","1/1","1/1","0/1"}));
		    	put(41655000	, 	new TerritoryBattleStrategy(18,new String[] {"2/3","2/2","1/2","1/2","1/1","0/1"}));
		    	put(52500000	, 	new TerritoryBattleStrategy(19,new String[] {"2/3","2/2","2/2","1/2","1/1","0/1"}));
		    	put(57150000	, 	new TerritoryBattleStrategy(20,new String[] {"2/3","2/2","2/2","1/2","1/2","0/1"}));
		    	put(57400000	, 	new TerritoryBattleStrategy(21,new String[] {"2/3","2/2","2/2","1/2","1/2","1/1"}));
		    	put(66780000	, 	new TerritoryBattleStrategy(22,new String[] {"2/3","2/3","2/2","1/2","1/2","1/1"}));
		    	put(71955000	, 	new TerritoryBattleStrategy(23,new String[] {"3/3","2/3","2/2","1/2","1/2","1/1"}));
		    	put(72870000	, 	new TerritoryBattleStrategy(24,new String[] {"3/3","2/3","2/2","2/2","1/2","1/1"}));
		    	put(83790000	, 	new TerritoryBattleStrategy(25,new String[] {"3/3","2/3","2/3","2/2","1/2","1/1"}));
		    	put(96230000	, 	new TerritoryBattleStrategy(26,new String[] {"3/3","2/3","2/3","2/2","1/2","1/2"}));
		    	put(96440000	, 	new TerritoryBattleStrategy(27,new String[] {"3/3","2/3","2/3","2/2","1/3","1/2"}));
		    	put(103740000	, 	new TerritoryBattleStrategy(28,new String[] {"3/3","3/3","2/3","2/2","1/3","1/2"}));
		    	put(105295000	, 	new TerritoryBattleStrategy(29,new String[] {"3/3","3/3","2/3","2/3","1/3","1/2"}));
		    	put(126090000	, 	new TerritoryBattleStrategy(30,new String[] {"3/3","3/3","3/3","2/3","1/3","1/2"}));
		    	put(134105000	, 	new TerritoryBattleStrategy(31,new String[] {"3/3","3/3","3/3","2/3","2/3","1/2"}));
		    	put(136950000	, 	new TerritoryBattleStrategy(32,new String[] {"3/3","3/3","3/3","2/3","2/3","1/3"}));
		    	put(154770000	, 	new TerritoryBattleStrategy(33,new String[] {"3/3","3/3","3/3","3/3","2/3","1/3"}));
		    	put(185150000	, 	new TerritoryBattleStrategy(34,new String[] {"3/3","3/3","3/3","3/3","2/3","2/3"}));
		    	put(185610000	, 	new TerritoryBattleStrategy(35,new String[] {"3/3","3/3","3/3","3/3","3/3","2/3"}));
		    	put(235950000	, 	new TerritoryBattleStrategy(36,new String[] {"3/3","3/3","3/3","3/3","3/3","3/3"}));
			 	    		       
		    }});
	
	private static final SortedMap<Integer, TerritoryBattleStrategy> airGPMinMapDS = Collections.unmodifiableSortedMap(
		    new TreeMap<Integer, TerritoryBattleStrategy>() {{
		    	put(0			, 	new TerritoryBattleStrategy(0 ,new String[] {"","","0/","0/","0/","0/"}));
		    	put(1920000		, 	new TerritoryBattleStrategy(1 ,new String[] {"","","1/","0/","0/","0/"}));
		    	put(2176000		, 	new TerritoryBattleStrategy(2 ,new String[] {"","","1/","1/","0/","0/"}));
		    	put(17325000	, 	new TerritoryBattleStrategy(3 ,new String[] {"","","2/","1/","0/","0/"}));
		    	put(18000000	, 	new TerritoryBattleStrategy(4 ,new String[] {"","","2/","1/","1/","0/"}));
		    	put(19635000	, 	new TerritoryBattleStrategy(5 ,new String[] {"","","2/","2/","1/","0/"}));
		    	put(21600000	, 	new TerritoryBattleStrategy(6 ,new String[] {"","","2/","2/","1/","1/"}));
		    	put(28930000	, 	new TerritoryBattleStrategy(7 ,new String[] {"","","3/","2/","1/","1/"}));
		    	put(32780000	, 	new TerritoryBattleStrategy(8 ,new String[] {"","","3/","3/","1/","1/"}));
		    	put(35700000	, 	new TerritoryBattleStrategy(9 ,new String[] {"","","3/","3/","2/","1/"}));
		    	put(44880000	, 	new TerritoryBattleStrategy(10,new String[] {"","","3/","3/","2/","2/"}));
		    	put(57500000	, 	new TerritoryBattleStrategy(11,new String[] {"","","3/","3/","3/","2/"}));
		    	put(78000000	, 	new TerritoryBattleStrategy(12,new String[] {"","","3/","3/","3/","3/"}));
			 	     		       
		    }});
	
	public GalaticPowerToStars(Integer CharacterGP,Integer ShipGP, Boolean isDark)
	{
		this.characterGP =CharacterGP;
		this.shipGP =ShipGP;
		this.isDarkSide = isDark;
		
		this.minGroundStrategy = getStrategyFromGP(this.characterGP,isDark?groundGPMinMapDS:groundGPMinMap);
		this.minAirStrategy = getStrategyFromGP(this.shipGP,isDark?airGPMinMapDS:airGPMinMap);
		this.minStarFromShip = new StarInfo(minAirStrategy);
		this.minStarFromCharacter=new StarInfo(minGroundStrategy);
		this.minStrategy = FormatGroundAndAirStrategy(this.minAirStrategy,this.minGroundStrategy);
		
		this.groundStrategy = getStrategyFromGP(this.characterGP,isDark?groundGPMapDS:groundGPMap);
		this.airStrategy = getStrategyFromGP(this.shipGP,isDark?airGPMapDS:airGPMap);
		this.starFromShip =new StarInfo(airStrategy);
		this.starFromCharacter=new StarInfo(groundStrategy);
		this.strategy = FormatGroundAndAirStrategy(this.airStrategy,this.groundStrategy);
		
	}
	
	private TerritoryBattleStrategy getStrategyFromGP(Integer GP,SortedMap<Integer, TerritoryBattleStrategy> possibleStrategy)
	{
		TerritoryBattleStrategy bestStrategy;
		SortedMap<Integer, TerritoryBattleStrategy> reachableGroundStars = possibleStrategy.headMap(GP);
		SortedMap<Integer, TerritoryBattleStrategy> unreachableGroundStars = possibleStrategy.tailMap(GP);
		if(reachableGroundStars.isEmpty())
		{
			bestStrategy = possibleStrategy.get(possibleStrategy.firstKey());
			bestStrategy.GPMargin =GP - possibleStrategy.firstKey()  ;
			bestStrategy.GPToNextStar = 0;
		}
		else
		{
			bestStrategy = reachableGroundStars.get(reachableGroundStars.lastKey());
			bestStrategy.GPMargin =GP - reachableGroundStars.lastKey();
			if(unreachableGroundStars.isEmpty())
			{
				bestStrategy.GPToNextStar = 0;
			}
			else
			{
				bestStrategy.GPToNextStar =unreachableGroundStars.firstKey()-GP ;
			}
		}
		return bestStrategy;
	}
	
	private String FormatGroundAndAirStrategy(TerritoryBattleStrategy airStrat,TerritoryBattleStrategy groundStrat)
	{
		String strategyReturned = "";
		for(int i =0;i<groundStrat.strategies.length;i++)
		{
			strategyReturned +="P"+ (i+1)+" "+airStrat.strategies[i]+groundStrat.strategies[i]+"\r\n";
		}
		return strategyReturned;
	}

}
