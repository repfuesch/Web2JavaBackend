package ch.uzh.ifi.web2.howler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.Place;
import twitter4j.RateLimitStatusEvent;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

/**
 * 
 *StatusListener that receives Status updates from twitter. Puts tweets that belong to a trending topic
 *and are located in switzerland into the database.
 */
public class TweetListener implements StatusListener, twitter4j.util.function.Consumer<RateLimitStatusEvent>
{
	private List<String> topics;
	private MongoClient mongoClient;
	private List<Location> locations;
	private double latitude;
	private double longitude; 
	
	private final Comparator<GeoLocation> latitudeComp = (g1, g2) -> Double.compare(g1.getLatitude(), g2.getLatitude());
	private final Comparator<GeoLocation> longitudeComp = (g1, g2) -> Double.compare(g1.getLongitude(), g2.getLongitude());
	
	private final Comparator<Location> locationComp = (l1, l2) -> 
	   {
		   double dist1 = Math.sqrt(Math.pow(l1.getLatitude() - latitude, 2) + Math.pow(l1.getLongitude() - longitude, 2));
		   double dist2 = Math.sqrt(Math.pow(l2.getLatitude() - latitude, 2) + Math.pow(l2.getLongitude() - longitude, 2));
		   if(dist1 < dist2)
			   return -1;
		   if (dist1 == dist2)
			   return 0;
		   return 1;
	   };
	
	public TweetListener(MongoClient client, List<Location> locations, List<String> topics)
	{
		mongoClient = client;
		this.topics = topics;
		this.locations = locations;
	}
	
	public void UpdateTopics(List<String> topics)
	{
		this.topics = topics;
	}
	
	@Override
	public void accept(RateLimitStatusEvent arg0) {
	}

	@Override
	public void onException(Exception arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDeletionNotice(StatusDeletionNotice arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onScrubGeo(long arg0, long arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStallWarning(StallWarning arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatus(Status status) {

		String topic = GetTopic(status);
		if(topic == null)
			return;
		
		Location nearestLocation = GetNearestLocation(status);
		if(nearestLocation == null)
			return;
		
        MongoDatabase db = mongoClient.getDatabase("howlrdb");
	   
        db.getCollection("tweets").insertOne(
		   new Document()
			  .append("topic", topic)
			  .append("createdAt", status.getCreatedAt())
			  .append("text", status.getText())
			  .append("city", nearestLocation.getName())
			  .append("canton", nearestLocation.getCanton())
			  .append("longitude", longitude)
			  .append("latitude", latitude)
			  .append("language", status.getLang())
			  .append("id", status.getId()));
	}

	private List<GeoLocation> GetGeoLocations(GeoLocation[][] geometryCoordinates) {
		List<GeoLocation> locations = new ArrayList<>();
		for(GeoLocation[] locArr : geometryCoordinates)
		{
			for(GeoLocation loc : locArr)
			{
				locations.add(loc);
			}
		}
		return locations;
	}
	
	private String GetTopic(Status status)
	{
	   HashtagEntity[] hashtags = status.getHashtagEntities();
	   String topic = null;
	   for(HashtagEntity entity : hashtags)
	   {
		   if(topics.contains("#" + entity.getText()))
			   topic = entity.getText();
	   }
	   return topic;
	}
	
	private Location GetNearestLocation(Status status)
	{
	   GeoLocation location = status.getGeoLocation();
	   Place place = status.getPlace();

	   if(location != null)
	   {
		   latitude = location.getLatitude();
		   longitude = location.getLongitude();
	   }else{
		   if(!place.getCountryCode().equalsIgnoreCase("ch"))
			   return null;
		   
		   List<GeoLocation> geoLocations = GetGeoLocations(place.getBoundingBoxCoordinates());
		   latitude = (geoLocations.stream().min(latitudeComp).map(g -> g.getLatitude()).get() + geoLocations.stream().max(latitudeComp).map(g -> g.getLatitude()).get()) / 2;
		   longitude = (geoLocations.stream().min(longitudeComp).map(g -> g.getLongitude()).get() + geoLocations.stream().max(longitudeComp).map(g -> g.getLongitude()).get()) / 2;
	   }
	   
	   Location nearestLocation = locations.stream().min(locationComp).get();
	   return nearestLocation;
	}

	@Override
	public void onTrackLimitationNotice(int arg0) {
		// TODO Auto-generated method stub
		
	}

}
