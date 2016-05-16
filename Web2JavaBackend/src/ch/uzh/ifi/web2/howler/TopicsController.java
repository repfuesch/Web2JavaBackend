package ch.uzh.ifi.web2.howler;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.*;

import ch.uzh.ifi.feedback.library.rest.Controller;
import ch.uzh.ifi.feedback.library.rest.RestController;
import twitter4j.GeoLocation;
import twitter4j.ResponseList;
import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

@Controller(Route = "/topics")
public class TopicsController extends RestController<Object> {

	private MongoClient mongoClient; 
	private TwitterManager twitterManager;
	
	public TopicsController()
	{
		mongoClient = getMongoClient();
		twitterManager = new TwitterManager();
	}
	
	@Override
	public void Put (HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {

		//Get trending topic for switzerland and update DB
		List<Trend> trendingTopics = twitterManager.GetTrendingTopics(23424957);
		UpdateTrends(trendingTopics);
		//get all topics in the DB
		List<String> topics = getAllTopics();
		//Retrieve Locations
		List<Location> locations = GetLocations();
		//register new topics in twitterManager
		//twitterManager.RegisterTopics(locations, topics, mongoClient);
		UpdateTweets(locations, topics);
		//UpdateTestTweets(locations, topics);
		
		response.getWriter().append(" \n Topics and Tweets updated! \n");
	}
	
	private void UpdateTrends(List<Trend> trendingTopics)
	{
		MongoDatabase db = mongoClient.getDatabase("howlrdb");
		boolean duplicate = false;
		for(Trend trend : trendingTopics)
		{
			for(Document doc : db.getCollection("topics").find())
			{
				for(Trend topic : trendingTopics)
				{
					if(doc.getString("name").equalsIgnoreCase(topic.getName()))
						duplicate = true;
				}
			}
			
			if(!duplicate)
			{
				db.getCollection("topics").insertOne(
						new Document()
								.append("name", trend.getName())
								.append("categories", new ArrayList<String>())
						);
			}
		}
	}
	
	private void UpdateTweets(List<Location> locations, List<String> topics) throws Exception
	{
		MongoDatabase db = mongoClient.getDatabase("howlrdb");
		twitterManager.setResultSize(1);
		for(Location location : locations)
		{
			for(String topic : topics)
			{
				List<Tweet> tweets = twitterManager.getTweets(location, topic);
				tweets.forEach(tweet -> {
					db.getCollection("tweets").insertOne(
							new Document()
									.append("topic", tweet.getTopic())
									.append("createdAt", tweet.getCreatedAt())
									.append("text", tweet.getMessage())
									.append("city", tweet.getCity())
									.append("canton", tweet.getCanton())
									.append("language", tweet.getLanguage())
									.append("longitude", tweet.getLongitude())
									.append("latitude", tweet.getLatitude())
									.append("id", tweet.getTweetId())
							);
				});
			}
		}
	}
	
	private void UpdateTestTweets(List<Location> locations, List<String> topics) throws Exception
	{
		MongoDatabase db = mongoClient.getDatabase("howlrdb");
		db.getCollection("test_tweets").drop();
		for(Location location : locations)
		{
			for(String topic : topics)
			{
				db.getCollection("test_tweets").insertOne(
						new Document()
								.append("topic", topic)
								.append("createdAt", Date.from(Instant.now()))
								.append("text", topic)
								.append("city", location.getName())
								.append("canton", location.getCanton())
								.append("language", "en")
								.append("longitude", location.getLongitude())
								.append("latitude", location.getLatitude())
								.append("id", (long) (Math.random() * 100000))
						);
			}
		}
	}
	
	private List<String> getAllTopics()
	{
		MongoDatabase db = mongoClient.getDatabase("howlrdb");
		FindIterable<Document> dbTopics = db.getCollection("topics").find();
		List<String> topics = new ArrayList<>();
		for(Document doc : dbTopics)
		{
			topics.add(doc.getString("name"));
		}
		
		return topics;
	}
	
	private List<Location> GetLocations(){
		
		MongoDatabase db = mongoClient.getDatabase("howlrdb");
		List<Location> locations = new ArrayList<>();
		FindIterable<Document> dbLocations = db.getCollection("locations").find();
		for(Document doc : dbLocations)
		{
			locations.add(new Location(doc.getString("name"), doc.getString("canton"), doc.getDouble("long"), doc.getDouble("lat")));
		}
		return locations;
	}
	
	
	private MongoClient getMongoClient()
	{
		ServerAddress address = new ServerAddress("ds031812.mlab.com", 31812);
		MongoCredential credential = MongoCredential.createCredential("admin", "howlrdb", "admin".toCharArray());
		List<MongoCredential> credentials = new ArrayList<>();
		credentials.add(credential);
		MongoClient mongoClient = new MongoClient(address, credentials);
		return mongoClient;
	}

	@Override
	public Object Get(HttpServletRequest request, HttpServletResponse response) throws Exception {

		response.setStatus(404);
		response.getWriter().append("Resource not found");
		return null;
	}
}
