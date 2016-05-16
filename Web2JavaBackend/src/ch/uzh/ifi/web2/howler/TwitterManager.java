package ch.uzh.ifi.web2.howler;

import java.util.ArrayList;
import java.util.List;

import com.twitter.Extractor;
import com.twitter.Extractor.Entity;

import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.Query.Unit;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class TwitterManager {
	
	private Twitter twitter;
	private Extractor extractor;
	private int resultSize;

	public TwitterManager(){
		twitter = new TwitterFactory().getInstance();
		extractor = new Extractor();
		setResultSize(10);
	}
	
	public List<Tweet> getTweets(double longitude, double latitude) throws TwitterException {
	
	   List<Tweet> tweetList = new ArrayList<Tweet>();
	   int count = 0;
	   
	   try {
		   
	       Query query = new Query();
	       query.setCount(10);
	       query.setGeoCode(new GeoLocation(latitude, longitude), 100.0, Unit.km);
	       
	       do{
			   QueryResult queryResult = twitter.search(query);
	           List<Status> tweets = queryResult.getTweets();
	           for (Status status : tweets) {
	        	   Tweet tweet = new Tweet();
	        	   tweet.setCreatedAt(status.getCreatedAt());
	        	   tweet.setLanguage(status.getLang());
	        	   tweet.setTopics(extractor.extractHashtags(status.getText()));
	        	   String text = status.getText();
	        	   List<Entity> entities = extractor.extractEntitiesWithIndices(text);
	        	   text = cleanTweet(text, entities);
	        	   tweet.setMessage(text);
	        	   tweet.setTweetId(status.getId());
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
