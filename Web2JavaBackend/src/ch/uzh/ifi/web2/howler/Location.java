package ch.uzh.ifi.web2.howler;

public class Location {
	
	private String name;
	private double longitude;
	private double latitude;
	private String canton;
	
	public Location(String name, String canton, double lat, double lon)
	{
		this.name = name;
		this.longitude = lon;
		this.latitude = lat;
		this.canton = canton;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCanton() {
		return canton;
	}
	public void setCanton(String canton) {
		this.canton = canton;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

}
