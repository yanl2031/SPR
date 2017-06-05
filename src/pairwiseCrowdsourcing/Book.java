package pairwiseCrowdsourcing;

import java.util.*;

public class Book {
	private String bookName;
	private int count = 0; // count of frequency
	private int[] distribution = new int[11]; // from 0 to 10 
	public static Random rand = new Random(20310);
	
	public String getBookName() {
		return bookName;
	}
	public void setBookName(String bookName) {
		this.bookName = bookName;
	}
	public int[] getDistribution() {
		return distribution;
	}
	public void setDistribution(int[] distribution) {
		this.distribution = distribution;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public void setDisValue(int value){
		if(value > 0 && value <=10){
			distribution[value] += 1;
			count++;
		}
	}
	public Book clone(){
		Book book = new Book();
		book.bookName = bookName;
		book.count = count;
		book.distribution = distribution;
		return book;
	}
	public double sample(){
		int sum = 0;
		int score = 0;
		// calculate sum of distribution
		for(int i = 1 ; i < distribution.length ; i++){
			sum += distribution[i];
		}
		
		// transfer distribution into percent distribution and keep upper bound
		ArrayList<Double> percentDis = new ArrayList<Double>();
		double temp = 0;
		double preValue = 0;
		for(int j = 1 ; j < distribution.length ; j++){
			temp = 1.0 * distribution[j] / sum;
			preValue += temp;
			percentDis.add(preValue);
		}
		
		// sample from percent distribution 
		double probability = rand.nextDouble(); // from 0.0 (inclusive) to 1 (exclusive)
		
		for(int i = 1 ; i < percentDis.size(); i++){
			if(probability < percentDis.get(i)){
				score  = i;
				break;
			}
		}
		return score;
	}
}
