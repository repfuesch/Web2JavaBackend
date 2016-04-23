package ch.uzh.ifi.web2.howler;

import java.util.ArrayList;
import java.util.List;

import com.twitter.Extractor;

import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.Query.Unit;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class TweetManager {
	
	private Twitter twitter;
	private Extractor extractor;
	private int resultSize;
	private int batchSize;
	
	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public TweetManager(){
		twitter = new TwitterFactory().getInstance();
		extractor = new Extractor();
		setResultSize(100);
		setBatchSize(20);
	}
	
	public List<Tweet> getTweets(double longitude, double latitude) {
	
	   List<Tweet> tweetList = new ArrayList<Tweet>();
	   int count = 0;
	   
	   try {
		   
	       Query query = new Query();
	       query.setCount(batchSize);
	       query.setGeoCode(new GeoLocation(latitude, longitude), 20.0, Unit.km);
	       
	       do{
			   QueryResult queryResult = twitter.search(query);
	           List<Status> tweets = queryResult.getTweets();
	           for (Status status : tweets) {
	        	   Tweet tweet = new Tweet();
	        	   tweet.setCreatedAt(status.getCreatedAt());
	        	   tweet.setLanguage(status.getLang());
	        	   tweet.setTopics(extractor.extractHashtags(status.getText()));
	        	   tweet.setMessage(status.getText());
	        	   tweetList.add(tweet);
	           }
	           count += batchSize;
	       }
	       while(count < resultSize);
	       
	   } catch (TwitterException te) {
	       te.printStackTrace();
	       System.out.println("Failed to search tweets: " + te.getMessage());
	   }
	   
	   return tweetList;
	}

	public int getResultSize() {
		return resultSize;
	}

	public void setResultSize(int resultSize) {
		this.resultSize = resultSize;
	}
}
