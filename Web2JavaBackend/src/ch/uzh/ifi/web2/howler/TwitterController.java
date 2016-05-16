package ch.uzh.ifi.web2.howler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.bson.Document;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.MongoURI;
import com.mongodb.MongoWriteConcernException;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import ch.uzh.ifi.feedback.library.rest.Controller;
import ch.uzh.ifi.feedback.library.rest.RestController;
import twitter4j.TwitterException;

@Controller(Route = "/posts")
public class TwitterController extends RestController<Location> {

	private TwitterManager tweetManager;
	
	public TwitterController()
	{
		tweetManager = new TwitterManager();
	}
	
	@Override
	public Location Get(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		response.setStatus(404);
		return null;
	}
	
	@Override
	public void Put(HttpServletRequest request, HttpServletResponse response, Location location) throws Exception {
		
		tweetManager.setResultSize(10);
		List<Tweet> tweets = tweetManager.getTweets(location.getLongitude(), location.getLatitude());
		MongoClient client = getMongoClient();
		MongoDatabase db = client.getDatabase("howlrdb");
		db.getCollection("test_tweets").drop();
		
		tweets.stream().forEach(t -> {
			
			//Todos:
			//Predict category of tweet
			//Train predictor for text -> topic mapping
			t.setCategory("Category");
			t.setCity(location.getCity());
			
			db.getCollection("test_tweets").insertOne(
					new Document()
							.append("topics", t.getTopics())
							.append("message", t.getMessage())
							.append("category", t.getCategory())
							.append("id", t.getTweetId())
							.append("city", t.getCity())
					);
		});
		
		client.close();
		
		response.getWriter().append("Tweets updated");
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
}
