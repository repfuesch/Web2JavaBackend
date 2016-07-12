package ch.uzh.ifi.web2.howler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mongodb.MongoClient;
import com.twitter.Extractor;
import com.twitter.Extractor.Entity;

import twitter4j.FilterQuery;
import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.Query.ResultType;
import twitter4j.Query.Unit;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

/**
 * Class that is responsible for the communication with the twitter API. Provides
 * functionality for the registration of topics, as well as for the retrieval of
 * tweets and trending topics.
 *
 */
public class TwitterManager {
	
	private Twitter twitter;
	private TwitterStream stream;
	private Extractor extractor;
	private int resultSize;

	public TwitterManager(){
		twitter = new TwitterFactory().getInstance();
		extractor = new Extractor();
		setResultSize(10);
	}
	
	public void RegisterTopics(List<Location> locations, List<String> topics, MongoClient client)
	{
		if(stream == null){
	        stream = new TwitterStreamFactory().getInstance();
		}else{
			stream.clearListeners();
		}

        TweetListener listener = new TweetListener(client, locations, topics);
        stream.addListener(listener);
        FilterQuery fq = new FilterQuery();
        fq.locations(new double[][]{ {5.95587, 45.81802}, {10.49203, 47.80838} });

        stream.filter(fq);
	}
	
	public List<Tweet> getTweets(Location location, String topic) throws TwitterException {
	
	   List<Tweet> tweetList = new ArrayList<Tweet>();
	   int count = 0;
	   
	   try {
	       Query query = new Query();
	       query.setQuery(topic);
	       query.setCount(1);
	       query.setGeoCode(new GeoLocation(location.getLatitude(), location.getLongitude()), 20.0, Unit.km);
	       query.setResultType(ResultType.popular);
	       
	       do{
			   QueryResult queryResult = twitter.search(query);
	           List<Status> tweets = queryResult.getTweets();
	           for (Status status : tweets) {
	        	   Tweet tweet = new Tweet();
	        	   tweet.setCreatedAt(status.getCreatedAt());
	        	   tweet.setLanguage(status.getLang());
	        	   tweet.setTopic(topic);
	        	   tweet.setCanton(location.getCanton());
	        	   String text = status.getText();
	        	   tweet.setMessage(text);
	        	   tweet.setTweetId(status.getId());
	        	   tweet.setLongitude(status.getGeoLocation().getLongitude());
	        	   tweet.setLatitude(status.getGeoLocation().getLatitude());
	        	   tweetList.add(tweet);
	        	   
		           count ++;
	           }
	       }
	       while(count < resultSize);
	       
	   } catch (TwitterException te) {
	       te.printStackTrace();
	       System.out.println("Failed to search tweets: " + te.getMessage());
	       throw te;
	   }
	   
	   return tweetList;
	}
	
	public List<Trend> GetTrendingTopics(int woeid) throws TwitterException
	{
		List<Trend> trendingTopics = new ArrayList<>();
		Trends trends = twitter.getPlaceTrends(23424957);
		for(Trend trend : trends.getTrends())
		{
			if(trend.getName().startsWith("#")){
				trendingTopics.add(trend);
			}
		}
		
		return trendingTopics;
	}
	
	 public String cleanTweet(String text, List<Entity> entities) {
		 StringBuilder builder = new StringBuilder(text.length());
		 
		 int beginIndex = 0;
		 for (Entity entity : entities) {
			 builder.append(text.subSequence(beginIndex, entity.getStart()));
			 beginIndex = entity.getEnd();
		 }
		 builder.append(text.subSequence(beginIndex, text.length()));
		 return builder.toString();
		 }

	public int getResultSize() {
		return resultSize;
	}

	public void setResultSize(int resultSize) {
		this.resultSize = resultSize;
	}
}
