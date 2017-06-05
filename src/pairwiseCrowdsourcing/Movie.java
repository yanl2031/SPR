package pairwiseCrowdsourcing;

import java.util.*;


class MeanComparator implements Comparator<Object> {
    @Override
    public int compare(Object a, Object b) {
		if (a.getMovie().getMean() > b.getMovie().getMean()){//a won, 
			return -1;
		}
		else if (a.getMovie().getMean() < b.getMovie().getMean()){
			return 1;
		}
    	return 0;
    }
}

public class Movie {
	private double mean; // mean value of movie in the real data
	private double current_mean; // the expected mean value of our converted distribution
	private double delta_mean; // correction, the difference between the real mean and our mean
	
	
	private String title;
	private double rank;
	private int vote;
	private int year;
	// C = the mean vote across the whole report (currently 6.90)
	private static double default_rank = 6.9;
	// k = minimum votes required to be listed in the top 250 (currently 25000)
	private static double minimum_vote = 25000;
	
	public static Random rand = new Random(20310);
	
	private ArrayList<Integer> distribution = new ArrayList<Integer>();
	private ArrayList<Double> current_distribution = new ArrayList<Double>();
	
	public double getMean() {
		return mean;
	}
	public void setMean(double mean) {
		this.mean = mean;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public double getRank() {
		return rank;
	}
	public void setRank(double rank) {
		this.rank = rank;
	}
	public int getVote() {
		return vote;
	}
	public void setVote(int vote) {
		this.vote = vote;
	}
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public ArrayList<Integer> getDistribution() {
		return distribution;
	}
	public void setDistribution(ArrayList<Integer> distribution) {
		this.distribution = distribution;
	}
	public Movie clone(){
		Movie movie = new Movie();
		movie.rank = rank;
		movie.vote = vote;
		movie.title = title;
		movie.year = year;
		movie.mean = mean;
		movie.current_mean = current_mean;
		movie.delta_mean = delta_mean;
		movie.distribution.addAll(distribution);
		movie.current_distribution.addAll(current_distribution);
		return movie;
	}
	
	public void setDistributionValue(String str){
		char [] disArray = str.toCharArray();
		//System.out.println(str);
		for(int i = 0; i < disArray.length; i++){
			if( Character.getNumericValue(disArray[i]) <0 && Character.getNumericValue(disArray[i]) >9 ){
				disArray[i] = '0';	
			}
			int value = Character.getNumericValue(disArray[i]);
			distribution.add(value);
			//System.out.print(value+"\t");
		}
		//System.out.print("\n");
	}
	
	public void displayCurrentDistribution(){
		for(int j=0;j<current_distribution.size();j++){
			System.out.print(j + " " + current_distribution.get(j) + "\t");
		}
		System.out.print("\n");
	}
	
	public void retrieveMean(){
		mean = rank + (rank - default_rank) * minimum_vote/vote;
	}
	
	public void convertDistribution(){
		double sum = 0;
		
		for(int i = 0; i < getDistribution().size() ; i++ ){
			if(getDistribution().get(i)!=0){
				sum += getDistribution().get(i)*10+5;
			}
		}
		//System.out.println("sum "+sum);
		
		double value = 0;
		double preValue = value;
		
		retrieveMean();
		current_mean = 0;
		
		for(int i = 0; i < getDistribution().size() ; i++ ){
			if(getDistribution().get(i) != 0){
				value = getDistribution().get(i)*10 +5;
			}
			value /= sum;
			current_mean += (i + 0.5) * value; // compute expected mean of converted distribution
			//System.out.print("before value "+ value);
			value += preValue;
			//System.out.println("after value "+ value);
			current_distribution.add(value);
			preValue = value;
		}
		
		delta_mean = mean - current_mean;
	}
	
	public double sample(){
		double score = 0;
		double probability = rand.nextDouble(); // from 0.0 (inclusive) to 1 (exclusive)
//		System.out.println("probability "+probability);

		for(int i = 0; i < current_distribution.size() ; i++ ){
			if(probability < current_distribution.get(i)){
				double scorePro = rand.nextDouble();
				score = scorePro + i;
//				System.out.print("scorePro "+scorePro+"\t");
//				System.out.print("i "+i+"\t");
				break;
			}
		}
		
//		System.out.println();
		return score + delta_mean;
	}
}