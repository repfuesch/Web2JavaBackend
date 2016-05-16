package ch.uzh.ifi.web2.howler;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.RateLimitStatusEvent;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

public class TweetListener implements StatusListener, twitter4j.util.function.Consumer<RateLimitStatusEvent>
{
	private List<String> topics;
	private MongoClient mongoClient;
	private List<Location> locations;
	
	public TweetListener(MongoClient client, List<Location> locations, List<String> topics)
	{
		mongoClient = client;
		this.topics = topics;
		this.locations = locations;
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
	   final GeoLocation location = status.getGeoLocation();
	   if(location == null)
		   return;
	   
	   final Comparator<Location> comp = (l1, l2) -> 
	   {
		   double dist1 = Math.sqrt(Math.pow(l1.getLatitude() - location.getLatitude(), 2) + Math.pow(l1.getLongitude() - location.getLongitude(), 2));
		   double dist2 = Math.sqrt(Math.pow(l2.getLatitude() - location.getLatitude(), 2) + Math.pow(l2.getLongitude() - location.getLongitude(), 2));
		   if(dist1 <= dist2)
			   return -1;
		   if (dist1 == dist2)
			   return 0;
		   return 1;
	   };
	   
	   Location nearestLocation = locations.stream().min(comp).get();
	   HashtagEntity[] hashtags = status.getHashtagEntities();
	   String topic = null;
	   for(HashtagEntity entity : hashtags)
	   {
		   if(topics.contains("#" + entity.getText()))
			   topic = entity.getText();
	   }
	   
	   if(topic == null)
		   return;
	   
	   MongoDatabase db = mongoClient.getDatabase("howlrdb");
	   
	   db.getCollection("tweets").insertOne(
				new Document()
					.append("topic", topic)
					.append("createdAt", status.getCreatedAt())
					.append("text", status.getText())
					.append("city", nearestLocation.getName())
					.append("canton", nearestLocation.getCanton())
					.append("longitude", status.getGeoLocation().getLongitude())
					.append("latitude", status.getGeoLocation().getLatitude())
					.append("language", status.getLang())
					.append("id", status.getId()));
		
	}

	@Override
	public void onTrackLimitationNotice(int arg0) {
		// TODO Auto-generated method stub
		
	}

}
