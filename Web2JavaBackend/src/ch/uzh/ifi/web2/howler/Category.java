package ch.uzh.ifi.web2.howler;

public enum Category {

 hot("Hot"), 
 news("News"), 
 sport("Sport"), 
 politik("Politik"), 
 events("Events"), 
 people("People"), 
 digital("Digital"), 
 lifestyle("Lifestyle"), 
 entertainment("Entertainment"), 
 technik("Technik"), 
 sightseeing("Sightseeing");
	
	
	private final  String category;
	
	Category(String category ){
		this.category = category;
	}
	
	public String getCategory(){
		return category;
	}
}
