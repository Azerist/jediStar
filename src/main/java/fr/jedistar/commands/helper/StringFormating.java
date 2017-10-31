package fr.jedistar.commands.helper;

public class StringFormating {
	
	public static String formatNumber(Integer value)
	{
		String result;
		if(value>999999) {
			 result = String.format("%.1fM", value/1000000.);
		}
		else if(value>999) {
			 result = String.format("%dK", value/1000);
		}
		else {
			 result = String.format("%d", value);
		}
		return result;
		
		
	}
}
