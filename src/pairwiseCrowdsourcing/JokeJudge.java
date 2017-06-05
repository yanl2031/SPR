package pairwiseCrowdsourcing;

import java.util.*;

public class JokeJudge {
	private int id;
	private List <Double> jokeRatings = new ArrayList <Double> ();
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public List<Double> getJokeRatings() {
		return jokeRatings;
	}
	public void setJokeRatings(List<Double> jokeRatings) {
		this.jokeRatings = jokeRatings;
	}
	
	
}
