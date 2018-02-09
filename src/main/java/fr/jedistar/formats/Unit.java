package fr.jedistar.formats;

import java.util.Calendar;

public class Unit {

	private String name;
	private String baseID;
	private String url;
	private String image;
	private Integer power;
	private String description;
	private Integer combatType;
	private Calendar expiration;
	
	public final static int UNIT_TYPE_TOON = 1;
	public final static int UNIT_TYPE_SHIP = 2;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getBaseID() {
		return baseID;
	}
	public void setBaseID(String baseID) {
		this.baseID = baseID;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public Integer getPower() {
		return power;
	}
	public void setPower(Integer power) {
		this.power = power;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Integer getCombatType() {
		return combatType;
	}
	public void setCombatType(Integer combatType) {
		this.combatType = combatType;
	}
	public Calendar getExpiration() {
		return expiration;
	}
	public void setExpiration(Calendar expiration) {
		this.expiration = expiration;
	}	
	
}
