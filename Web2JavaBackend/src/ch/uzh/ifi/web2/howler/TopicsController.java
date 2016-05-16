package ch.uzh.ifi.web2.howler;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
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
	
	public TopicsController() {
		mongoClient = getMongoClient();
		twitterManager = new TwitterManager();
	}
	
	@Override
	public void Put (HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {

		MongoDatabase db = mongoClient.getDatabase("howlrdb");
		
		//Get trending topic for switzerland and update db
		List<Trend> trendingTopics = twitterManager.GetTrendingTopics(23424957);
		
		for(Trend trend : trendingTopics)
		{
			Document result = db.getCollection("topics").find(eq("name", trend.getName())).first();		
			if(result == null)
			{
				db.getCollection("topics").insertOne(
						new Document()
								.append("name", trend.getName())
								.append("categories", new ArrayList<String>())
						);
			}
		}
		
		//Retrieve Locations and get tweets for each location and topic
		
	}
	
	private MongoClient getMongoClient()
	{
		ServerAddress address = new ServerAddress("http://ec2-54-229-137-71.eu-west-1.compute.amazonaws.com", 27017);
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
