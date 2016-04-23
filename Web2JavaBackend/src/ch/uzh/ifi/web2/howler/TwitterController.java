package ch.uzh.ifi.web2.howler;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.bson.Document;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import ch.uzh.ifi.feedback.library.rest.Controller;
import ch.uzh.ifi.feedback.library.rest.RestController;
import com.twitter.*;

@Controller(Route = "/posts")
public class TwitterController extends RestController<Location> {

	private TweetManager tweetManager;
	
	public TwitterController()
	{
		tweetManager = new TweetManager();
	}
	
	@Override
	public Location Get(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		response.setStatus(404);
		return null;
	}
	
	@Override
	public void Put(HttpServletRequest request, HttpServletResponse response, Location location) throws Exception {
		
		Extractor extractor = new Extractor();
		List<Tweet> tweets = tweetManager.getTweets(location.getLongitude(), location.getLatitude());

		MongoClient mongoClient = new MongoClient();
		MongoDatabase db = mongoClient.getDatabase("test");
		db.getCollection("tweets").drop();
		
		tweets.stream().forEach(t -> {
			
			//Todos:
			//Predict category of tweet
			//Train predictor for text -> topic mapping
			t.setCategory("Category");
			t.setCity(location.getCity());
			
			db.getCollection("tweets").insertOne(
					new Document()
							.append("topics", t.getTopics())
							.append("message", t.getMessage())
							.append("category", t.getCategory())
							.append("category", t.getCategory())
							.append("city", t.getCity())
					);
		});
	}
}
