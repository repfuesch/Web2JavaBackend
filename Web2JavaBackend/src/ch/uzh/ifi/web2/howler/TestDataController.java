package ch.uzh.ifi.web2.howler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.Document;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

import ch.uzh.ifi.feedback.library.rest.Controller;
import ch.uzh.ifi.feedback.library.rest.RestController;

@Controller(Route = "/TestData")
public class TestDataController extends RestController<Object> {

	@Override
	public String Serialize(Object result)
	{
		return "Test";
	}
	
	@Override
	public Object Get(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		MongoClient client = getMongoClient();
		MongoDatabase db = client.getDatabase("howlrdb");
		
		List<String> topics = new ArrayList<>();
		FindIterable<Document> iterable = db.getCollection("test_tweets").find();
		iterable.forEach(new Block<Document>() {
		    @Override
		    public void apply(final Document document) {
		    	List<String> tweet_topics = (List<String>) document.get("topics");
		        for(String topic : tweet_topics)
		        {
		        	if(!topics.contains(topic))
		        		topics.add(topic);
		        }
		    }
		});
		
		WriteToFile(topics);
		
		return null;
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
	
	private void WriteToFile(List<String> topics)
	{
		String path = "/home/flo/topics";
		// Use relative path for Unix systems
		File f = new File(path);
		try {
			FileWriter writer = new FileWriter(f);
			for(String topic : topics)
			{
				writer.write(topic + "\n");
			}
			
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
