package pairwiseCrowdsourcing;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.TDistribution;

import pairwiseCrowdsourcing.Object;

//import evaluation.RandomSelection.Comparison;

class CompRecord{
	private int questNum = 0;// quest number
	private double sum = 0;
	private double squareSum = 0;
	private int winnerID = -1;
	private int questIncrement = 0;
	private double lowerbound = -1;
	private double upperbound = 1;
	
	public double getSum() {
		return sum;
	}
	public void setSum(double sum) {
		this.sum = sum;
	}
	public double getSquareSum() {
		return squareSum;
	}
	public void setSquareSum(double squareSum) {
		this.squareSum = squareSum;
	}
	public int getQuestNum() {
		return questNum;
	}
	public void setQuestNum(int questNum) {
		this.questNum = questNum;
	}
	public int getWinnerID() {
		return winnerID;
	}
	public void setWinnerID(int winnerID) {
		this.winnerID = winnerID;
	}
	public int getQuestIncrement() {
		return questIncrement;
	}
	public void setQuestIncrement(int questIncrement) {
		this.questIncrement = questIncrement;
	}
	public double getLowerbound() {
		return lowerbound;
	}
	public void setLowerbound(double lowerbound) {
		this.lowerbound = lowerbound;
	}
	public double getUpperbound() {
		return upperbound;
	}
	public void setUpperbound(double upperbound) {
		this.upperbound = upperbound;
	}
	
	public void clear() {
		questNum = 0;
		sum = 0;
		squareSum = 0;
		winnerID = -1;
		questIncrement = 0;
		lowerbound = -1;
		upperbound = 1;
	}
}

// compare object by postion
class PositionComparator implements Comparator<Object> {
    @Override
    public int compare(Object a, Object b) {
		if (a.getPosition() < b.getPosition()){//a won, 
			return -1;
		}
		else if (a.getPosition() > b.getPosition()){
			return 1;
		}
    	return 0;
    }
}

// compare object by expected #quest
class ExpectedQuestComparator implements Comparator<Object> {
    @Override
    public int compare(Object a, Object b) {
		if (a.getExpectedQuest() < b.getExpectedQuest()){//a won, 
			return -1;
		}
		else if (a.getExpectedQuest() > b.getExpectedQuest()){
			return 1;
		}
    	return 0;
    }
}

// jokeData sort by score 
class ScoreComparator implements Comparator<Object> {
    @Override
    public int compare(Object a, Object b) {
		if (a.getScore() > b.getScore()){//a won, 
			return -1;
		}
		else if (a.getScore() < b.getScore()){
			return 1;
		}
    	return 0;
    }
}

//sort by graded sampling score 
class GradeComparator implements Comparator<Object> {
	@Override
	// ascending order
	public int compare(Object a, Object b) {
		if (a.getGradedScore() > b.getGradedScore()){//a won, 
			return 1;
		}
		else if (a.getGradedScore() < b.getGradedScore()){
			return -1;
		}
		return 0;
 	}
}

class ItemPair {
	ArrayList<Double> quest = new ArrayList<Double>();
	int idA;
	int idB;
	int nextQuest;
	
	public ArrayList<Double> getQuest() {
		return quest;
	}
	public void setQuest(ArrayList<Double> quest) {
		this.quest = quest;
	}
	public int getIdA() {
		return idA;
	}
	public void setIdA(int idA) {
		this.idA = idA;
	}
	public int getIdB() {
		return idB;
	}
	public void setIdB(int idB) {
		this.idB = idB;
	}
	public int getNextQuest() {
		return nextQuest;
	}
	public void setNextQuest(int nextQuest) {
		this.nextQuest = nextQuest;
	}
	public double getSample(){
		double score = -1;
//		if (nextQuest < quest.size()){
//			score = quest.get(nextQuest);
//			nextQuest++;
//		}
//		else {
//			System.err.format("No enough quests A: %d and B: %d%n", idA, idB);
//			System.exit(-1);
//		}
		
		Random rand = new Random();
		nextQuest = rand.nextInt(quest.size());
		score = quest.get(nextQuest);
		
		return score;
	}

}


public class Object {
	// const for different states
	public static final int UNVISITED = 0;
	public static final int IN_HEAP = 1;
	public static final int NOT_TOPK = 2;
	public static final int CONFUSE = 2;
	public static double confidence = 0.95;
	public static double sita = 5;
	public static double alpha = 1;
	public static double beta = 1;
	public static int minSampleNumCI = 30;
	//public static int minSampleNumCI = 10;
//	public static ResultStore store = new ResultStore();
	// debug setting
//	public static int minSampleNumCI = 100;
//	public static double beta = 0.01;
	
	public static int CountOfSample = 0;
	
	// constants for selecting dataset and model
	public enum Dataset {SYNTHETIC_DATA, SOCCER_DATA, MOVIE_DATA, JOKE_DATA, BOOK_DATA, PHOTO_DATA, AGE_ONLINE, ONLINE, LOCAL_SERVER};
	public enum Model {CI_MODEL, STEIN_MODEL, HOEFFDING_MODEL, HOEFFDING_BINARY_MODEL, GRADED_MODEL, GRADED_MODEL_LIKERT};
	public static Dataset dataset = Dataset.MOVIE_DATA;
	public static Model model = Model.CI_MODEL;
	
	// keep joke information
	public static ArrayList <JokeJudge> JokeInfo = new ArrayList <JokeJudge> ();
	
	// keep parameter table information
	public static ArrayList<ParameterElement> ParameterTable = new ArrayList<ParameterElement> ();
	
	// keep photo judgments
	// key is a string "id-id"
	public static Hashtable<String, ArrayList<Double>> PhotoJudgments = new Hashtable<String, ArrayList<Double>>();
		
	// the score calculated by Graded Judge
	private int gradedSampleNum = 0;
	private double gradedSampleSum = 0;
	private double gradedScore = 0;

	private int ID;
	private double Score;
	private int Position;
	private int expectedQuest = -1;
	private int state = UNVISITED;
	private ArrayList<Object> Children = new ArrayList<Object>();
	
	private Hashtable<Integer, CompRecord> RecordHash = new Hashtable<Integer, CompRecord>();
	private Hashtable<Integer, ItemPair> ItemPairs = new Hashtable<Integer, ItemPair>(); 

	// each object contains a Movie
	private Movie movie = new Movie();
	private Book book = new Book();
	
	// compute average workload for monitor, workloadsum: total number of microtasks of completed comparisons, workloadcount: count number of completed comparisons-yan
	public static int workLoadSum = 0;
	public static int workLoadCount = 0;
	
	public Hashtable<Integer, CompRecord> getRecordHash() {
		return RecordHash;
	}
	public void setRecordHash(Hashtable<Integer, CompRecord> recordHash) {
		RecordHash = recordHash;
	}
	public Hashtable<Integer, ItemPair> getItemPairs() {
		return ItemPairs;
	}
	public void setItemPairs(Hashtable<Integer, ItemPair> itemPairs) {
		Iterator it = itemPairs.entrySet().iterator();
		while (it.hasNext()){
			Map.Entry<Integer, ItemPair> entry = (Entry<Integer, ItemPair>) it.next();
			ItemPair pair = entry.getValue();
			Integer key = entry.getKey();
			ItemPair copy = new ItemPair();
			copy.setIdA(pair.idA);
			copy.setIdB(pair.idB);
			copy.setQuest(pair.quest);
			copy.setNextQuest(0);
			ItemPairs.put(key, copy);
		}
	}
	public int getChildrenNumber() {
		int number = Children.size();
		for (int i = 0; i < Children.size(); i++){
			number += Children.get(i).getChildrenNumber();
		}
		return number;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public void setChildrenState(int state) {
		Stack<Object> objs = new Stack<Object>();
	
		objs.addAll(Children);
		
		while (!objs.isEmpty()){
			objs.peek().setState(state);
			objs.addAll(objs.pop().Children);
		}
	}
	
	public ArrayList<Object> getChildren() {
		return Children;
	}
	public void setChildren(Object child) {
		Children.add(child);
	}
	public void setChildrenInfo(ArrayList<Object> children){
		Children = children;
	}
	
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public double getScore() {
		return Score;
	}
	public void setScore(double score) {
		Score = score;
	}
	public int getPosition() {
		return Position;
	}
	public void setPosition(int position) {
		Position = position;
	}
	
	public int getExpectedQuest() {
		return expectedQuest;
	}
	public void setExpectedQuest(int expectedQuest) {
		this.expectedQuest = expectedQuest;
	}
	public Movie getMovie() {
		return movie;
	}
	public void setMovie(Movie movie) {
		this.movie = movie;
	}
	
	public Book getBook() {
		return book;
	}
	public void setBook(Book book) {
		this.book = book;
	}
	public double getGradedScore() {
		return gradedScore;
	}
	public void setGradedScore(double gradedScore) {
		this.gradedScore = gradedScore;
	}
	// to read parameter data m, x, cost, input: 
	public static void ReadParameterTable() throws IOException{
		String filename = "./input/parameter_table.csv";
		Scanner  cin = new Scanner(new File(filename));
		String line;
		ArrayList<Double> probability = new ArrayList <Double> ();
		line = cin.nextLine();
		//System.out.println(line);
		// first line
		String firstLine [] = line.split(",");
		for(int i = 2 ; i < firstLine.length ; i+=3){
			double p = Double.parseDouble(firstLine[i]);
			probability.add(p);
		}
		// second line
		line = cin.nextLine();
		// 
		while(cin.hasNext()){
	        line = cin.nextLine();
	        String ss [] = line.split(",");
	        double s = Double.parseDouble(ss[0]);
	        for(int i = 0 ,j; i < probability.size() ; i++ ){
	        	ParameterElement parameter = new ParameterElement();
	        	double proba = probability.get(i);
	        	j = (3*i+1);
	        	int m = Integer.parseInt(ss[j]);
	        	int x = Integer.parseInt(ss[j+1]);
	        	int cost = Integer.parseInt(ss[j+2]);
	        	parameter.setN_over_k(s);
	        	parameter.setProbability(proba);
	        	parameter.setM(m);
	        	parameter.setX(x);
	        	parameter.setCost(cost);
	        	Object.ParameterTable.add(parameter);
	        }
		}
		
		cin.close();
//		System.out.println(Object.ParameterTable.size());
//		for(int i = 0 ; i < Object.ParameterTable.size() ; i++ ){
//			System.out.println(Object.ParameterTable.get(i).getN_over_k()+"\t"+Object.ParameterTable.get(i).getProbability()+"\t"
//					+Object.ParameterTable.get(i).getM()+"\t"+Object.ParameterTable.get(i).getX()+"\t"+Object.ParameterTable.get(i).getCost());
//		}
	}
	
	public static ParameterElement getParameters(double N_over_k, double probability){
		if (Object.ParameterTable.size() == 0){
			try {
				ReadParameterTable();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		ParameterElement parameter;
		int size = Object.ParameterTable.size();
		double left , right;
		double pleft, pright;
		for(int i = 0 ; i < size ; i++){
			parameter = Object.ParameterTable.get(i);
			left = parameter.getN_over_k() - 0.5;
			right = parameter.getN_over_k() + 0.5;
			if(N_over_k >= left && N_over_k <= right){
				pleft = parameter.getProbability() - 0.05 ;
				pright = parameter.getProbability() + 0.05 ;
				if(probability >= pleft && probability <= pright){
					return parameter;
				}
			}
		}
		throw new IllegalArgumentException("the parameters cannot be found");
		//return null;
	}
		
	public static ParameterElement getParameters(double N_over_k, int n){
		if (Object.ParameterTable.size() == 0){
			try {
				ReadParameterTable();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(N_over_k < 2){
			N_over_k = 2;
		}else if(N_over_k > 20){
			N_over_k = 20;
		}
		
		ParameterElement parameter, bestParameter = null;
		int size = Object.ParameterTable.size();
		double left , right;
		for(int i = 0 ; i < size ; i++){
			parameter = Object.ParameterTable.get(i);
			left = parameter.getN_over_k() - 0.5;
			right = parameter.getN_over_k() + 0.5;
			if(N_over_k >= left && N_over_k <= right){
				if(parameter.getCost() <= n){
					bestParameter = parameter;
				}
			}
		}
		
		if (bestParameter == null){
			throw new IllegalArgumentException("the parameters cannot be found");
		}
		else {
			return bestParameter;
		}
	}
	
	public boolean isParentOf(int id){
		Stack<Object> objs = new Stack<Object>();
		
		// circle detection
		HashSet<Integer> visited = new HashSet<Integer>();
		
		objs.addAll(Children);
		visited.add(ID);
		
		Object peek;
		
		while (!objs.isEmpty()){
			peek = objs.pop();
			if (peek.getID() == id){
				return true;
			}
			else {
				// circle detection
				if (visited.contains(peek.ID)){
					// avoid the expansion of this object
					//System.out.println("Circle detected: " + ID);
				}
				else {
					visited.add(peek.getID());
					objs.addAll(peek.Children);
				}
				// debug
//				if (peek.getID() == ID){
//					System.out.println("ERROR!" + ID);
//					break;
//				}
			}
		}
		
		return false;
	}
	
	public Object clone(){
		Object copy = new Object();
		
		copy.setID(ID);
		copy.setPosition(Position);
		copy.setScore(Score);
		copy.setItemPairs(ItemPairs);
		copy.setMovie(movie.clone());
		copy.setBook(book.clone());
		
		
		return copy;
	}
	
	private void WithoutCI(Object o, int winnerID){
		if(ID > o.getID()){
			CompRecord record;
			
			if(RecordHash.containsKey(o.getID())){
				record = RecordHash.get(o.getID());
				record.setQuestIncrement(0);
				record.setWinnerID(winnerID);
			}
			else {
				record = new CompRecord();
				record.setQuestIncrement(0);
				record.setWinnerID(winnerID);
				RecordHash.put(o.getID(), record);
			}
		}else{
			o.WithoutCI(this, winnerID);
		}
	}

	public int Compare(Object o, int Threshold){
		return Compare(o, Threshold, true);
	}
	
	public int Compare(Object o, int Threshold, boolean checkParent){
		int winnerID = -1;
		
		// compare with itself!
		if (ID == o.getID()){
			return -1;
		}
		
		if (isParentOf(o.getID()) && checkParent){
			winnerID = ID;
			WithoutCI(o, winnerID);
		}
		else if (o.isParentOf(ID) && checkParent){
			winnerID = o.getID();
			WithoutCI(o, winnerID);
		}
		else if(ID > o.getID()){
			switch (dataset){
			case SYNTHETIC_DATA:
				switch (model){
				case CI_MODEL:
					winnerID = CI(o, Threshold);
					break;
				case STEIN_MODEL:
					break;
				case HOEFFDING_MODEL:
					break;
				case HOEFFDING_BINARY_MODEL:
					break;
				case GRADED_MODEL:
					break;
				}
				break;
			case SOCCER_DATA:
				switch (model){
				case CI_MODEL:
					winnerID = CIReal(o, Threshold);
					break;
				case STEIN_MODEL:
					break;
				case HOEFFDING_MODEL:
					break;
				case HOEFFDING_BINARY_MODEL:
					break;
				case GRADED_MODEL:
					break;
				}
				break;
			case MOVIE_DATA:
				switch (model){
				case CI_MODEL:
					winnerID = CIMovie(o, Threshold);
					break;
				case STEIN_MODEL:
					winnerID = SteinMovie(o, Threshold);
					break;
				case HOEFFDING_MODEL:
					winnerID = HoeffdingMovie(o, Threshold);
					break;
				case HOEFFDING_BINARY_MODEL:
					winnerID = HoeffdingBinaryMovie(o, Threshold);
					break;
				case GRADED_MODEL:
					winnerID = GradedJudgeMovie(o, Threshold);
					break;
				case GRADED_MODEL_LIKERT:
					winnerID = GradedLikertJudgeMovie(o, Threshold);
					break;
				}
				break;
			case JOKE_DATA:
				switch (model){
				case CI_MODEL:
					winnerID = CIJoke(o, Threshold);
					break;
//				case STEIN_MODEL:
//					winnerID = SteinMovie(o, Threshold);
//					break;
//				case HOEFFDING_MODEL:
//					winnerID = HoeffdingMovie(o, Threshold);
//					break;
//				case HOEFFDING_BINARY_MODEL:
//					winnerID = HoeffdingBinaryMovie(o, Threshold);
//					break;
//				case GRADED_MODEL:
//					winnerID = GradedJudgeMovie(o, Threshold);
//					break; 
				}
				break;
			case BOOK_DATA:
				switch(model){
				case CI_MODEL:
					winnerID = CIBook(o, Threshold);
					break;
//				case STEIN_MODEL:
//					winnerID = SteinMovie(o, Threshold);
//					break;
//				case HOEFFDING_MODEL:
//					winnerID = HoeffdingMovie(o, Threshold);
//					break;
//				case HOEFFDING_BINARY_MODEL:
//					winnerID = HoeffdingBinaryMovie(o, Threshold);
//					break;
//				case GRADED_MODEL:
//					winnerID = GradedJudgeMovie(o, Threshold);
//					break; 
				}
				break;
			case ONLINE:
				switch(model){
				case CI_MODEL:
					winnerID = CIOnline(o, Threshold);
					break;
//				case STEIN_MODEL:
//					winnerID = SteinMovie(o, Threshold);
//					break;
//				case HOEFFDING_MODEL:
//					winnerID = HoeffdingMovie(o, Threshold);
//					break;
//				case HOEFFDING_BINARY_MODEL:
//					winnerID = HoeffdingBinaryMovie(o, Threshold);
//					break;
//				case GRADED_MODEL:
//					winnerID = GradedJudgeMovie(o, Threshold);
//					break; 
				}
				break;
			case AGE_ONLINE:
				switch(model){
				case CI_MODEL:
					winnerID = CIOnlineAge(o, Threshold);
					break;
//				case STEIN_MODEL:
//					winnerID = SteinMovie(o, Threshold);
//					break;
//				case HOEFFDING_MODEL:
//					winnerID = HoeffdingMovie(o, Threshold);
//					break;
//				case HOEFFDING_BINARY_MODEL:
//					winnerID = HoeffdingBinaryMovie(o, Threshold);
//					break;
//				case GRADED_MODEL:
//					winnerID = GradedJudgeMovie(o, Threshold);
//					break; 
				}
				break;
			case PHOTO_DATA:
				switch(model){
				case CI_MODEL:
					winnerID = CIPhoto(o, Threshold);
					break;
//				case STEIN_MODEL:
//					winnerID = SteinMovie(o, Threshold);
//					break;
//				case HOEFFDING_MODEL:
//					winnerID = HoeffdingMovie(o, Threshold);
//					break;
//				case HOEFFDING_BINARY_MODEL:
//					winnerID = HoeffdingBinaryMovie(o, Threshold);
//					break;
//				case GRADED_MODEL:
//					winnerID = GradedJudgeMovie(o, Threshold);
//					break; 
				}
				break;
			case LOCAL_SERVER:
				switch(model){
				case CI_MODEL:
					winnerID = CILocalServer(o, Threshold);
					break;
//				case STEIN_MODEL:
//					winnerID = SteinMovie(o, Threshold);
//					break;
//				case HOEFFDING_MODEL:
//					winnerID = HoeffdingMovie(o, Threshold);
//					break;
//				case HOEFFDING_BINARY_MODEL:
//					winnerID = HoeffdingBinaryMovie(o, Threshold);
//					break;
//				case GRADED_MODEL:
//					winnerID = GradedJudgeMovie(o, Threshold);
//					break; 
				}
				break;
			}
		}else{
			winnerID = o.Compare(this, Threshold, checkParent);
		}
		return winnerID;
	}
	
	public int estimateQuest(Object o){
		if (ID > o.getID()){
			return SteinGetExpectedQuest(o);
		}
		else {
			return o.SteinGetExpectedQuest(this);
		}
	}
	
	public int getCompareQuestIncrement(Object o){
		int increment;
		
		// compare with itself!
		if (ID == o.getID()){
			return 0;
		}
		
		if(ID > o.getID()){
			increment = RecordHash.get(o.getID()).getQuestIncrement();
		}else{
			increment = o.RecordHash.get(ID).getQuestIncrement();
		}
		return increment;
	}
	
	// get record
	public CompRecord getRecord(Object o){
		CompRecord record;
		
		if(ID > o.getID()){
			record = getRecordHash().get(o.getID());			
		}else{
			record = o.getRecordHash().get(ID);
		}
		
		return record;
	}
	
	// get winner's ID
	private int CI(Object o, int threshold){
		int questNum = 0;
		double x;
		double deltScore = Score - o.getScore();
		double d;
		double sum = 0;
		double squareSum = 0;
		double lower = -1;
		double upper = 1;
		double mu = 0;
		double sigma;
		
		if (threshold < 2){
			System.err.println("CI with threshold less than 2!");
			System.exit(1);
		}
		
		CompRecord record = new CompRecord();
		
		if(RecordHash.containsKey(o.getID())){
			record = RecordHash.get(o.getID());
			record.setQuestIncrement(0);
			
			if (record.getWinnerID() != -1 || record.getQuestNum() >= threshold){
				return record.getWinnerID();
			}
			
			sum = record.getSum();
			squareSum = record.getSquareSum();
			questNum = record.getQuestNum();
		}
		else {
			RecordHash.put(o.getID(), record);
		}
		
		CountOfSample++;

		double mu_dist = (1/( 1 + Math.pow(Math.E,(-beta*deltScore)))-1.0/2)*2;
		//System.out.format("True mu: %f%n", mu_dist);
		double sigma_dist = Math.abs(Math.pow(sita*mu_dist, -alpha));
		//Yan loose sigma to test max_threshold to infinite Oct30
		//sigma_dist = sigma_dist-2;
//		while(sigma_dist*3>1){
//			sigma_dist *= 0.999;
//		}
		//end Yan
		//System.out.format("True sigma: %f%n", sigma_dist);
		NormalDistribution gaussianDis= new NormalDistribution(mu_dist,sigma_dist);
		//gaussianDis.reseedRandomGenerator(20310);
		int i=questNum;
		if(i == 0){
			x = gaussianDis.sample();//first time
			if(x>1){
				x=1;
			}else if(x<-1){
				x=-1;
			}
			i++;
			sum = x;
			squareSum = x*x;
		}
		
		do {
			x = gaussianDis.sample();
			if (x>1){
				x=1;
			} else if(x<-1){
				x=-1;
			}
			i++;
			sum += x;
			squareSum += x*x;
			
			if (i < minSampleNumCI){
				continue;
			}
			
			mu = sum/i;
			sigma = squareSum/(i-1)-i/(i-1)*mu*mu;
			TDistribution t = new TDistribution(i-1);//i is current sample times
			double tValue = (t.inverseCumulativeProbability((1+confidence)/2));
			//System.out.println("tValue is " +tValue); //when first sample is 21 t(20)0.975=2.08596344726587
			//num = (int) Math.ceil(4*segma*Math.pow(tValue, 2)/Math.pow(interval, 2));
			d=tValue*Math.sqrt(sigma)/Math.sqrt(i);
			//System.out.println("Sample: "+s);
			
			lower = mu-d;
			upper = mu+d;
			
			//lower and upper bounds cut tails
			if(lower>0){//a win
				if(upper>1){
					upper = 1;
				}
				record.setWinnerID(ID);
				record.setQuestNum(i);
				record.setSum(sum);
				record.setSquareSum(squareSum);
				record.setQuestIncrement(i - questNum);
				Children.add(o);
				//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
				return ID;
			}else if(upper<0){//b win
				if(lower<-1){
					lower=-1;
				}
				record.setWinnerID(o.getID());
				record.setQuestNum(i);
				record.setSum(sum);
				record.setSquareSum(squareSum);
				record.setQuestIncrement(i - questNum);
				o.getChildren().add(this);
				//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
				return o.getID();
			}
		} while (i < threshold);

		// set confuse
		record.setQuestNum(i);
		record.setSum(sum);
		record.setSquareSum(squareSum);
		record.setQuestIncrement(i - questNum);

		//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
		return -1;
	}

	private int CIReal(Object o, int threshold){
		int questNum = 0;
		double x;
		double d;
		double sum = 0;
		double squareSum = 0;
		double lower = -1;
		double upper = 1;
		double mu = 0;
		double sigma;
		ItemPair pair = ItemPairs.get(o.getID());
		
		if (threshold < 2){
			System.err.println("CI with threshold less than 2!");
			System.exit(1);
		}
		
		CompRecord record = new CompRecord();
		
		if(RecordHash.containsKey(o.getID())){
			record = RecordHash.get(o.getID());
			record.setQuestIncrement(0);
			
			if (record.getWinnerID() != -1 || record.getQuestNum() >= threshold){
				return record.getWinnerID();
			}
			
			sum = record.getSum();
			squareSum = record.getSquareSum();
			questNum = record.getQuestNum();
		}
		else {
			RecordHash.put(o.getID(), record);
		}
		
		CountOfSample++;

		int i=questNum;
		if(i == 0){
			x = pair.getSample();//first time
			if(x>1){
				x=1;
			}else if(x<-1){
				x=-1;
			}
			i++;
			sum = x;
			squareSum = x*x;
		}
		
		do {
			x = pair.getSample();
			if (x>1){
				x=1;
			} else if(x<-1){
				x=-1;
			}
			i++;
			sum += x;
			squareSum += x*x;
			
			if (i < minSampleNumCI){
				continue;
			}
			
			mu = sum/i;
			sigma = squareSum/(i-1)-i/(i-1)*mu*mu;
			TDistribution t = new TDistribution(i-1);//i is current sample times
			double tValue = (t.inverseCumulativeProbability((1+confidence)/2));
			//System.out.println("tValue is " +tValue); //when first sample is 21 t(20)0.975=2.08596344726587
			//num = (int) Math.ceil(4*segma*Math.pow(tValue, 2)/Math.pow(interval, 2));
			d=tValue*Math.sqrt(sigma)/Math.sqrt(i);
			//System.out.println("Sample: "+s);
			
			lower = mu-d;
			upper = mu+d;
			
			//lower and upper bounds cut tails
			if(lower>0){//a win
				if(upper>1){
					upper = 1;
				}
				record.setWinnerID(ID);
				record.setQuestNum(i);
				record.setSum(sum);
				record.setSquareSum(squareSum);
				record.setQuestIncrement(i - questNum);
				Children.add(o);
				//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
				return ID;
			}else if(upper<0){//b win
				if(lower<-1){
					lower=-1;
				}
				record.setWinnerID(o.getID());
				record.setQuestNum(i);
				record.setSum(sum);
				record.setSquareSum(squareSum);
				record.setQuestIncrement(i - questNum);
				o.getChildren().add(this);
				//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
				return o.getID();
			}
		} while (i < threshold);

		// set confuse
		record.setQuestNum(i);
		record.setSum(sum);
		record.setSquareSum(squareSum);
		record.setQuestIncrement(i - questNum);

		//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
		return -1;
	}
	
	private int CIPhoto(Object o, int threshold){
		int questNum = 0;
		double x;
		double d;
		double sum = 0;
		double squareSum = 0;
		double lower = -1;
		double upper = 1;
		double mu = 0;
		double sigma;
		
		if (threshold < 2){
			System.err.println("CI with threshold less than 2!");
			System.exit(1);
		}
		
		CompRecord record = new CompRecord();
		
		if(RecordHash.containsKey(o.getID())){
			record = RecordHash.get(o.getID());
			record.setQuestIncrement(0);
			
			if (record.getWinnerID() != -1 || record.getQuestNum() >= threshold){
				return record.getWinnerID();
			}
			
			sum = record.getSum();
			squareSum = record.getSquareSum();
			questNum = record.getQuestNum();
		}
		else {
			RecordHash.put(o.getID(), record);
		}
		
		CountOfSample++;

		int i=questNum;
		if(i == 0){
			x = photoSample(o);//first time
			if(x>1){
				x=1;
			}else if(x<-1){
				x=-1;
			}
			i++;
			sum = x;
			squareSum = x*x;
		}
		
		do {
			x = photoSample(o);//first time
			if (x>1){
				x=1;
			} else if(x<-1){
				x=-1;
			}
			i++;
			sum += x;
			squareSum += x*x;
			
			if (i < minSampleNumCI){
				continue;
			}
			
			mu = sum/i;
			sigma = squareSum/(i-1)-i/(i-1)*mu*mu;
			TDistribution t = new TDistribution(i-1);//i is current sample times
			double tValue = (t.inverseCumulativeProbability((1+confidence)/2));
			//System.out.println("tValue is " +tValue); //when first sample is 21 t(20)0.975=2.08596344726587
			//num = (int) Math.ceil(4*segma*Math.pow(tValue, 2)/Math.pow(interval, 2));
			d=tValue*Math.sqrt(sigma)/Math.sqrt(i);
			//System.out.println("Sample: "+s);
			
			lower = mu-d;
			upper = mu+d;
			
			//lower and upper bounds cut tails
			if(lower>0){//a win
				if(upper>1){
					upper = 1;
				}
				record.setWinnerID(ID);
				record.setQuestNum(i);
				record.setSum(sum);
				record.setSquareSum(squareSum);
				record.setQuestIncrement(i - questNum);
				Children.add(o);
				//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
				return ID;
			}else if(upper<0){//b win
				if(lower<-1){
					lower=-1;
				}
				record.setWinnerID(o.getID());
				record.setQuestNum(i);
				record.setSum(sum);
				record.setSquareSum(squareSum);
				record.setQuestIncrement(i - questNum);
				o.getChildren().add(this);
				//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
				return o.getID();
			}
		} while (i < threshold);

		// set confuse
		record.setQuestNum(i);
		record.setSum(sum);
		record.setSquareSum(squareSum);
		record.setQuestIncrement(i - questNum);

		//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
		return -1;
	}
	
	private int CIMovie(Object o, int threshold){
		int questNum = 0;
		double x;
		double d;
		double sum = 0;
		double squareSum = 0;
		double lower = -1;
		double upper = 1;
		double mu = 0;
		double sigma;
		
		if (threshold < 2){
			System.err.println("CI with threshold less than 2!");
			System.exit(1);
		}
		
		CompRecord record = new CompRecord();
		
		if(RecordHash.containsKey(o.getID())){
			record = RecordHash.get(o.getID());
			record.setQuestIncrement(0);
			
			if (record.getWinnerID() != -1 || record.getQuestNum() >= threshold){
				return record.getWinnerID();
			}
			
			sum = record.getSum();
			squareSum = record.getSquareSum();
			questNum = record.getQuestNum();
		}
		else {
			RecordHash.put(o.getID(), record);
		}
		
		CountOfSample++;

		int i=questNum;
		if(i == 0){
			x = movieSample(o);//first time
			if(x>1){
				x=1;
			}else if(x<-1){
				x=-1;
			}
			i++;
			sum = x;
			squareSum = x*x;
		}
		
		do {
			x = movieSample(o);//first time
			if (x>1){
				x=1;
			} else if(x<-1){
				x=-1;
			}
			i++;
			sum += x;
			squareSum += x*x;
			
			if (i < minSampleNumCI){
				continue;
			}
			
			mu = sum/i;
			sigma = squareSum/(i-1)-i/(i-1)*mu*mu;
			TDistribution t = new TDistribution(i-1);//i is current sample times
			double tValue = (t.inverseCumulativeProbability((1+confidence)/2));
			//System.out.println("tValue is " +tValue); //when first sample is 21 t(20)0.975=2.08596344726587
			//num = (int) Math.ceil(4*segma*Math.pow(tValue, 2)/Math.pow(interval, 2));
			d=tValue*Math.sqrt(sigma)/Math.sqrt(i);
			//System.out.println("Sample: "+s);
			
			lower = mu-d;
			upper = mu+d;
			
			//lower and upper bounds cut tails
			if(lower>0){//a win
				if(upper>1){
					upper = 1;
				}
				record.setWinnerID(ID);
				record.setQuestNum(i);
				record.setSum(sum);
				record.setSquareSum(squareSum);
				record.setQuestIncrement(i - questNum);
				Children.add(o);
				//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
				return ID;
			}else if(upper<0){//b win
				if(lower<-1){
					lower=-1;
				}
				record.setWinnerID(o.getID());
				record.setQuestNum(i);
				record.setSum(sum);
				record.setSquareSum(squareSum);
				record.setQuestIncrement(i - questNum);
				o.getChildren().add(this);
				//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
				return o.getID();
			}
		} while (i < threshold);

		// set confuse
		record.setQuestNum(i);
		record.setSum(sum);
		record.setSquareSum(squareSum);
		record.setQuestIncrement(i - questNum);

		//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
		return -1;
	}
	
	private int CIJoke(Object o, int threshold){
		int questNum = 0;
		double x;
		double d;
		double sum = 0;
		double squareSum = 0;
		double lower = -1;
		double upper = 1;
		double mu = 0;
		double sigma;
		
		if (threshold < 2){
			System.err.println("CI with threshold less than 2!");
			System.exit(1);
		}
		
		CompRecord record = new CompRecord();
		
		if(RecordHash.containsKey(o.getID())){
			record = RecordHash.get(o.getID());
			record.setQuestIncrement(0);
			
			if (record.getWinnerID() != -1 || record.getQuestNum() >= threshold){
				return record.getWinnerID();
			}
			
			sum = record.getSum();
			squareSum = record.getSquareSum();
			questNum = record.getQuestNum();
			lower = record.getLowerbound();
			upper = record.getUpperbound();
		}
		else {
			RecordHash.put(o.getID(), record);
		}
		
		CountOfSample++;
		Random rand = new Random();

		int i=questNum;
		if(i == 0){
			x = jokeSample(o, rand);//first time
			if(x>1){
				x=1;
			}else if(x<-1){
				x=-1;
			}
			i++;
			sum = x;
			squareSum = x*x;
		}
		
		do {
			x = jokeSample(o, rand);//first time
			if (x>1){
				x=1;
			} else if(x<-1){
				x=-1;
			}
			i++;
			sum += x;
			squareSum += x*x;
			
			if (i < minSampleNumCI){
				continue;
			}
			
			mu = sum/i;
			sigma = squareSum/(i-1)-i/(i-1)*mu*mu;
			TDistribution t = new TDistribution(i-1);//i is current sample times
			double tValue = (t.inverseCumulativeProbability((1+confidence)/2));
			//System.out.println("tValue is " +tValue); //when first sample is 21 t(20)0.975=2.08596344726587
			//num = (int) Math.ceil(4*segma*Math.pow(tValue, 2)/Math.pow(interval, 2));
			d=tValue*Math.sqrt(sigma)/Math.sqrt(i);
			//System.out.println("Sample: "+s);
			
			lower = Math.max(mu-d, lower);
			upper = Math.min(mu+d, upper);
			
			//lower and upper bounds cut tails
			if(lower>0){//a win
				if(upper>1){
					upper = 1;
				}
				record.setWinnerID(ID);
				record.setQuestNum(i);
				record.setSum(sum);
				record.setSquareSum(squareSum);
				record.setQuestIncrement(i - questNum);
				record.setLowerbound(lower);
				record.setUpperbound(upper);
				Children.add(o);
				//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
				return ID;
			}else if(upper<0){//b win
				if(lower<-1){
					lower=-1;
				}
				record.setWinnerID(o.getID());
				record.setQuestNum(i);
				record.setSum(sum);
				record.setSquareSum(squareSum);
				record.setQuestIncrement(i - questNum);
				record.setLowerbound(lower);
				record.setUpperbound(upper);
				o.getChildren().add(this);
				//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
				return o.getID();
			}
		} while (i < threshold);

		// set confuse
		record.setQuestNum(i);
		record.setSum(sum);
		record.setSquareSum(squareSum);
		record.setQuestIncrement(i - questNum);
		record.setLowerbound(lower);
		record.setUpperbound(upper);

		//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
		return -1;
	}
	
	private int CIBook(Object o, int threshold){
		int questNum = 0;
		double x;
		double d;
		double sum = 0;
		double squareSum = 0;
		double lower = -1;
		double upper = 1;
		double mu = 0;
		double sigma;
		
		if (threshold < 2){
			System.err.println("CI with threshold less than 2!");
			System.exit(1);
		}
		
		CompRecord record = new CompRecord();
		
		if(RecordHash.containsKey(o.getID())){
			record = RecordHash.get(o.getID());
			record.setQuestIncrement(0);
			
			if (record.getWinnerID() != -1 || record.getQuestNum() >= threshold){
				return record.getWinnerID();
			}
			
			sum = record.getSum();
			squareSum = record.getSquareSum();
			questNum = record.getQuestNum();
		}
		else {
			RecordHash.put(o.getID(), record);
		}
		
		CountOfSample++;
		Random rand = new Random();

		int i=questNum;
		if(i == 0){
			x = bookSample(o);//first time
			if(x>1){
				x=1;
			}else if(x<-1){
				x=-1;
			}
			i++;
			sum = x;
			squareSum = x*x;
		}
		
		do {
			x = bookSample(o);//first time
			if (x>1){
				x=1;
			} else if(x<-1){
				x=-1;
			}
			i++;
			sum += x;
			squareSum += x*x;
			
			if (i < minSampleNumCI){
				continue;
			}
			
			mu = sum/i;
			sigma = squareSum/(i-1)-i/(i-1)*mu*mu;
			TDistribution t = new TDistribution(i-1);//i is current sample times
			double tValue = (t.inverseCumulativeProbability((1+confidence)/2));
			//System.out.println("tValue is " +tValue); //when first sample is 21 t(20)0.975=2.08596344726587
			//num = (int) Math.ceil(4*segma*Math.pow(tValue, 2)/Math.pow(interval, 2));
			d=tValue*Math.sqrt(sigma)/Math.sqrt(i);
			//System.out.println("Sample: "+s);
			
			lower = mu-d;
			upper = mu+d;
			
			//lower and upper bounds cut tails
			if(lower>0){//a win
				if(upper>1){
					upper = 1;
				}
				record.setWinnerID(ID);
				record.setQuestNum(i);
				record.setSum(sum);
				record.setSquareSum(squareSum);
				record.setQuestIncrement(i - questNum);
				Children.add(o);
				//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
				return ID;
			}else if(upper<0){//b win
				if(lower<-1){
					lower=-1;
				}
				record.setWinnerID(o.getID());
				record.setQuestNum(i);
				record.setSum(sum);
				record.setSquareSum(squareSum);
				record.setQuestIncrement(i - questNum);
				o.getChildren().add(this);
				//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
				return o.getID();
			}
		} while (i < threshold);

		// set confuse
		record.setQuestNum(i);
		record.setSum(sum);
		record.setSquareSum(squareSum);
		record.setQuestIncrement(i - questNum);

		//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
		return -1;
	}
	
	// Hoeffding Binary Version-Nov 13
	private int HoeffdingBinaryMovie(Object o, int threshold){
		int questNum = 0;
		double x;
		double d;
		double sum = 0;
		double squareSum = 0;
		double lower = -1;
		double upper = 1;
		double mu = 0;
		
		if (threshold < 2){
			System.err.println("CI with threshold less than 2!");
			System.exit(1);
		}
		
		CompRecord record = new CompRecord();
		
		if(RecordHash.containsKey(o.getID())){
			record = RecordHash.get(o.getID());
			record.setQuestIncrement(0);
			
			if (record.getWinnerID() != -1 || record.getQuestNum() >= threshold){
				return record.getWinnerID();
			}
			
			sum = record.getSum();
			squareSum = record.getSquareSum();
			questNum = record.getQuestNum();
		}
		else {
			RecordHash.put(o.getID(), record);
		}
		
		CountOfSample++;

		int i=questNum;
		if(i == 0){
			x = movieSample(o);//first time
			if(x>0){
				x=1;
			}
			else if(x<0){
				x=0;
			}
			else {
				x = 0.5;
			}
			i++;
			sum = x;
			squareSum = x*x;
		}
		
		do {
			x = movieSample(o);//first time
			if (x>0){
				x=1;
			}
			else if(x<0){
				x=0;
			}
			else {
				x = 0.5;
			}
			i++;
			sum += x;
			squareSum += x*x;
			
			if (i < minSampleNumCI){
				continue;
			}
			
			mu = sum/i;
			
			//Hoeffding 
			d = Math.sqrt(-0.5/i*Math.log((1-confidence)/2));
//			TDistribution t = new TDistribution(i-1);//i is current sample times
//			double tValue = (t.inverseCumulativeProbability((1+confidence)/2));
//			d=tValue*Math.sqrt(sigma)/Math.sqrt(i);
			
			
			lower = mu-d;
			upper = mu+d;
			
			//lower and upper bounds cut tails
			if(lower>0.5){//a win
				record.setWinnerID(ID);
				record.setQuestNum(i);
				record.setSum(sum);
				record.setSquareSum(squareSum);
				record.setQuestIncrement(i - questNum);
				Children.add(o);
				//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
				return ID;
			}else if(upper<0.5){//b win
				record.setWinnerID(o.getID());
				record.setQuestNum(i);
				record.setSum(sum);
				record.setSquareSum(squareSum);
				record.setQuestIncrement(i - questNum);
				o.getChildren().add(this);
				//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
				return o.getID();
			}
		} while (i < threshold);

		// set confuse
		record.setQuestNum(i);
		record.setSum(sum);
		record.setSquareSum(squareSum);
		record.setQuestIncrement(i - questNum);

		//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
		return -1;
	}
	
	// Hoeffding Weighted Version
	private int HoeffdingMovie(Object o, int threshold){
		int questNum = 0;
		double x;
		double d;
		double sum = 0;
		double squareSum = 0;
		double lower = -1;
		double upper = 1;
		double mu = 0;
		
		if (threshold < 2){
			System.err.println("CI with threshold less than 2!");
			System.exit(1);
		}
		
		CompRecord record = new CompRecord();
		
		if(RecordHash.containsKey(o.getID())){
			record = RecordHash.get(o.getID());
			record.setQuestIncrement(0);
			
			if (record.getWinnerID() != -1 || record.getQuestNum() >= threshold){
				return record.getWinnerID();
			}
			
			sum = record.getSum();
			squareSum = record.getSquareSum();
			questNum = record.getQuestNum();
		}
		else {
			RecordHash.put(o.getID(), record);
		}
		
		CountOfSample++;

		int i=questNum;
		if(i == 0){
			x = movieSample(o);//first time
			if(x>1){
				x=1;
			}
			else if(x<-1){
				x=-1;
			}
			
			x = x/2 + 0.5;
			
			i++;
			sum = x;
			squareSum = x*x;
		}
		
		do {
			x = movieSample(o);//first time
			if (x>1){
				x=1;
			}
			else if(x<-1){
				x=-1;
			}
			
			x = x/2 + 0.5;
			
			i++;
			sum += x;
			squareSum += x*x;
			
			if (i < minSampleNumCI){
				continue;
			}
			
			mu = sum/i;
			
			//Hoeffding 
			d = Math.sqrt(-0.5/i*Math.log((1-confidence)/2));
//			TDistribution t = new TDistribution(i-1);//i is current sample times
//			double tValue = (t.inverseCumulativeProbability((1+confidence)/2));
//			d=tValue*Math.sqrt(sigma)/Math.sqrt(i);
			
			
			lower = mu-d;
			upper = mu+d;
			
			//lower and upper bounds cut tails
			if(lower>0.5){//a win
				if(upper>1){
					upper = 1;
				}
				record.setWinnerID(ID);
				record.setQuestNum(i);
				record.setSum(sum);
				record.setSquareSum(squareSum);
				record.setQuestIncrement(i - questNum);
				Children.add(o);
				//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
				return ID;
			}else if(upper<0.5){//b win
				if(lower<0){
					lower=0;
				}
				record.setWinnerID(o.getID());
				record.setQuestNum(i);
				record.setSum(sum);
				record.setSquareSum(squareSum);
				record.setQuestIncrement(i - questNum);
				o.getChildren().add(this);
				//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
				return o.getID();
			}
		} while (i < threshold);

		// set confuse
		record.setQuestNum(i);
		record.setSum(sum);
		record.setSquareSum(squareSum);
		record.setQuestIncrement(i - questNum);

		//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
		return -1;
	}
	
	//Stein model
	private int SteinMovie(Object o, int threshold){
		int questNum = 0;
		double x;
		double sum = 0;
		double squareSum = 0;
		double mu = 0;
		double sigma;
		double interval = 0;
		double halfInterval = 0;
		int num;
		
		if (threshold < 2){
			System.err.println("CI with threshold less than 2!");
			System.exit(1);
		}
		
		CompRecord record = new CompRecord();
		
		if(RecordHash.containsKey(o.getID())){
			record = RecordHash.get(o.getID());
			record.setQuestIncrement(0);
			
			if (record.getWinnerID() != -1 || record.getQuestNum() >= threshold){
				return record.getWinnerID();
			}
			
			sum = record.getSum();
			squareSum = record.getSquareSum();
			questNum = record.getQuestNum();
		}
		else {
			RecordHash.put(o.getID(), record);
		}
		
		CountOfSample++;

//		double mu_dist = (1/( 1 + Math.pow(Math.E,(-beta*deltScore)))-1.0/2)*2;
//		//System.out.format("True mu: %f%n", mu_dist);
//		double sigma_dist = Math.abs(Math.pow(sita*mu_dist, -alpha));
//		//System.out.format("True sigma: %f%n", sigma_dist);
//		NormalDistribution gaussianDis= new NormalDistribution(mu_dist,sigma_dist);
//		//gaussianDis.reseedRandomGenerator(20310);
		
		
		int i=questNum;
		if(i == 0){
			//x = gaussianDis.sample();//first time
			x = movieSample(o);
			if(x>1){
				x=1;
			}else if(x<-1){
				x=-1;
			}
			i++;
			sum = x;
			squareSum = x*x;
		}
		
		do {
			//x = gaussianDis.sample();
			x = movieSample(o);
			if (x>1){
				x=1;
			} else if(x<-1){
				x=-1;
			}
			i++;
			sum += x;
			squareSum += x*x;
			
			if (i < minSampleNumCI){
				continue;
			}
			
			mu = sum/i;
			halfInterval = Math.abs(mu);
			interval = halfInterval*2;
			sigma = squareSum/(i-1)-i/(i-1)*mu*mu;
			TDistribution t = new TDistribution(i-1);//i is current sample times
			double tValue = (t.inverseCumulativeProbability((1+confidence)/2));
			//System.out.println("tValue is " +tValue); //when first sample is 21 t(20)0.975=2.08596344726587
			num = (int) Math.ceil(4*sigma*Math.pow(tValue, 2)/Math.pow(interval, 2));
			
			if(num<=i){
				if(mu > 0){// a win
					record.setWinnerID(ID);
					record.setQuestNum(i);
					record.setSum(sum);
					record.setSquareSum(squareSum);
					record.setQuestIncrement(i - questNum);
					Children.add(o);
					//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
					return ID;
				}
				else if(mu <0){// b win
					record.setWinnerID(o.getID());
					record.setQuestNum(i);
					record.setSum(sum);
					record.setSquareSum(squareSum);
					record.setQuestIncrement(i - questNum);
					o.getChildren().add(this);
					//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
					return o.getID();
				}
			}
			
		} while (i < threshold);

		// set confuse
		record.setQuestNum(i);
		record.setSum(sum);
		record.setSquareSum(squareSum);
		record.setQuestIncrement(i - questNum);

		//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
		return -1;
	}
	
	// online judgment on CrowdFlower
	private int CIOnline(Object o, int threshold) {
		int questNum = 0;
		double x;
		double d;
		double sum = 0;
		double squareSum = 0;
		double lower = -1;
		double upper = 1;
		double mu = 0;
		double sigma;
		
		if (threshold < 2){
			System.err.println("CI with threshold less than 2!");
			System.exit(1);
		}
		
		CompRecord record = new CompRecord();
		
		if(RecordHash.containsKey(o.getID())){
			record = RecordHash.get(o.getID());
			record.setQuestIncrement(0);
			
			if (record.getWinnerID() != -1 || record.getQuestNum() >= threshold){
				return record.getWinnerID();
			}
			
			sum = record.getSum();
			squareSum = record.getSquareSum();
			questNum = record.getQuestNum();
		}
		else {
			RecordHash.put(o.getID(), record);
		}
		
		CountOfSample++;
		
		ArrayList<Double> preferences = new ArrayList<Double>();
//		store.read(ID, o.ID, preferences);

		int i=questNum;
		if(i == 0){
			x = preferences.get(i);
			if(x>1){
				x=1;
			}else if(x<-1){
				x=-1;
			}
			i++;
			sum = x;
			squareSum = x*x;
		}
		

		while (i < threshold){
			x = preferences.get(i);
			if (x>1){
				x=1;
			} else if(x<-1){
				x=-1;
			}
			i++;
			sum += x;
			squareSum += x*x;
		}
		
		if (i >= minSampleNumCI){
			mu = sum/i;
			sigma = squareSum/(i-1)-i/(i-1)*mu*mu;
			TDistribution t = new TDistribution(i-1);//i is current sample times
			double tValue = (t.inverseCumulativeProbability((1+confidence)/2));
			//System.out.println("tValue is " +tValue); //when first sample is 21 t(20)0.975=2.08596344726587
			//num = (int) Math.ceil(4*segma*Math.pow(tValue, 2)/Math.pow(interval, 2));
			d=tValue*Math.sqrt(sigma)/Math.sqrt(i);
			//System.out.println("Sample: "+s);
			
			lower = mu-d;
			upper = mu+d;
			
			//lower and upper bounds cut tails
			if(lower>0){//a win
				if(upper>1){
					upper = 1;
				}
				record.setWinnerID(ID);
				record.setQuestNum(i);
				record.setSum(sum);
				record.setSquareSum(squareSum);
				record.setQuestIncrement(i - questNum);
				Children.add(o);
				//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
				return ID;
			}else if(upper<0){//b win
				if(lower<-1){
					lower=-1;
				}
				record.setWinnerID(o.getID());
				record.setQuestNum(i);
				record.setSum(sum);
				record.setSquareSum(squareSum);
				record.setQuestIncrement(i - questNum);
				o.getChildren().add(this);
				//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
				return o.getID();
			}
		} while (i < threshold);

		// set confuse
		record.setQuestNum(i);
		record.setSum(sum);
		record.setSquareSum(squareSum);
		record.setQuestIncrement(i - questNum);

		//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
		return -1;
	}
	
	private int CIOnlineAge(Object o, int threshold) {
		int questNum = 0;
		double x;
		double d;
		double sum = 0;
		double squareSum = 0;
		double lower = -1;
		double upper = 1;
		double mu = 0;
		double sigma;
		
		if (threshold < 2){
			System.err.println("CI with threshold less than 2!");
			System.exit(1);
		}
		
		CompRecord record = new CompRecord();
		
		if(RecordHash.containsKey(o.getID())){
			record = RecordHash.get(o.getID());
			record.setQuestIncrement(0);
			
			if (record.getWinnerID() != -1 || record.getQuestNum() >= threshold){
				return record.getWinnerID();
			}
			
			sum = record.getSum();
			squareSum = record.getSquareSum();
			questNum = record.getQuestNum();
		}
		else {
			RecordHash.put(o.getID(), record);
		}
		
		CountOfSample++;
		
		ArrayList<Double> preferences = new ArrayList<Double>();
//		store.readAge(ID, o.ID, preferences);

		int i=questNum;
		if(i == 0){
			x = preferences.get(i);
			if(x>1){
				x=1;
			}else if(x<-1){
				x=-1;
			}
			i++;
			sum = x;
			squareSum = x*x;
		}
		

		while (i < threshold){
			x = preferences.get(i);
			if (x>1){
				x=1;
			} else if(x<-1){
				x=-1;
			}
			i++;
			sum += x;
			squareSum += x*x;
		}
		
		if (i >= minSampleNumCI){
			mu = sum/i;
			sigma = squareSum/(i-1)-i/(i-1)*mu*mu;
			TDistribution t = new TDistribution(i-1);//i is current sample times
			double tValue = (t.inverseCumulativeProbability((1+confidence)/2));
			//System.out.println("tValue is " +tValue); //when first sample is 21 t(20)0.975=2.08596344726587
			//num = (int) Math.ceil(4*segma*Math.pow(tValue, 2)/Math.pow(interval, 2));
			d=tValue*Math.sqrt(sigma)/Math.sqrt(i);
			//System.out.println("Sample: "+s);
			
			lower = mu-d;
			upper = mu+d;
			
			//lower and upper bounds cut tails
			if(lower>0){//a win
				if(upper>1){
					upper = 1;
				}
				record.setWinnerID(ID);
				record.setQuestNum(i);
				record.setSum(sum);
				record.setSquareSum(squareSum);
				record.setQuestIncrement(i - questNum);
				Children.add(o);
				//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
				return ID;
			}else if(upper<0){//b win
				if(lower<-1){
					lower=-1;
				}
				record.setWinnerID(o.getID());
				record.setQuestNum(i);
				record.setSum(sum);
				record.setSquareSum(squareSum);
				record.setQuestIncrement(i - questNum);
				o.getChildren().add(this);
				//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
				return o.getID();
			}
		} while (i < threshold);

		// set confuse
		record.setQuestNum(i);
		record.setSum(sum);
		record.setSquareSum(squareSum);
		record.setQuestIncrement(i - questNum);

		//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
		return -1;
	}	

	
	// collect data from local server CI 
	private int CILocalServer(Object o, int threshold) {
		int questNum = 0;
		double x;
		double d;
		double sum = 0;
		double squareSum = 0;
		double lower = -1;
		double upper = 1;
		double mu = 0;
		double sigma;
		
		
		
		if (threshold < 2){
			System.err.println("CI with threshold less than 2!");
//			System.exit(1);
		}
		
		CompRecord record = new CompRecord();
		
		if(RecordHash.containsKey(o.getID())){
			record = RecordHash.get(o.getID());
			record.setQuestIncrement(0);
			
			if (record.getWinnerID() != -1 || record.getQuestNum() >= threshold){
				return record.getWinnerID();
			}
			
			sum = record.getSum();
			squareSum = record.getSquareSum();
			questNum = record.getQuestNum();
		}
		else {
			RecordHash.put(o.getID(), record);
		}
		
		CountOfSample++;
		
		ArrayList<Double> preferences = new ArrayList<Double>();
//		preferences = store.readAnswer(ID, o.ID);

		int i=questNum;
		if(i == 0){
			x = preferences.get(i);
			if(x>1){
				x=1;
			}else if(x<-1){
				x=-1;
			}
			i++;
			sum = x;
			squareSum = x*x;
		}
		

		while (i < threshold){
			x = preferences.get(i);
			if (x>1){
				x=1;
			} else if(x<-1){
				x=-1;
			}
			i++;
			sum += x;
			squareSum += x*x;
		}
		
		if (i >= minSampleNumCI){
			mu = sum/i;
			sigma = squareSum/(i-1)-i/(i-1)*mu*mu;
			TDistribution t = new TDistribution(i-1);//i is current sample times
			double tValue = (t.inverseCumulativeProbability((1+confidence)/2));
			//System.out.println("tValue is " +tValue); //when first sample is 21 t(20)0.975=2.08596344726587
			//num = (int) Math.ceil(4*segma*Math.pow(tValue, 2)/Math.pow(interval, 2));
			d=tValue*Math.sqrt(sigma)/Math.sqrt(i);
			//System.out.println("Sample: "+s);
			
			lower = mu-d;
			upper = mu+d;
			
			//lower and upper bounds cut tails
			if(lower>0){//a win
				if(upper>1){
					upper = 1;
				}
				record.setWinnerID(ID);
				record.setQuestNum(i);
				record.setSum(sum);
				record.setSquareSum(squareSum);
				record.setQuestIncrement(i - questNum);
				Children.add(o);
				//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
				// put averge workload to monitor hashset-yan
				workLoadSum += i;
				workLoadCount ++;
//				Object.store.setAvgWorkload(workLoadSum/(double)workLoadCount);
				return ID;
			}else if(upper<0){//b win
				if(lower<-1){
					lower=-1;
				}
				record.setWinnerID(o.getID());
				record.setQuestNum(i);
				record.setSum(sum);
				record.setSquareSum(squareSum);
				record.setQuestIncrement(i - questNum);
				o.getChildren().add(this);
				//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
				// put averge workload to monitor hashset-yan
				workLoadSum += i;
				workLoadCount ++;
//				Object.store.setAvgWorkload(workLoadSum/(double)workLoadCount);
				return o.getID();
			}
		} while (i < threshold);

		// set confuse
		record.setQuestNum(i);
		record.setSum(sum);
		record.setSquareSum(squareSum);
		record.setQuestIncrement(i - questNum);

		//System.out.format("%d compare %d, mu: %f | lower: %f | upper: %f%n", ID, o.getID(), mu, lower, upper);
		return -1;
	}
	private int SteinGetExpectedQuest(Object o){
		int questNum = 0;
		double sum = 0;
		double squareSum = 0;
		double mu, sigma;
		double interval = 0;
		double halfInterval = 0;
		int num;
		
		CompRecord record = new CompRecord();
		
		if(RecordHash.containsKey(o.getID())){
			record = RecordHash.get(o.getID());
			record.setQuestIncrement(0);
			
			if (record.getWinnerID() != -1){
				return record.getQuestNum();
			}
			
			sum = record.getSum();
			squareSum = record.getSquareSum();
			questNum = record.getQuestNum();
		}
		else {
			return -1;
		}
		
		mu = sum / questNum;
		halfInterval = Math.abs(mu);
		interval = halfInterval*2;
		sigma = squareSum/(questNum-1)-questNum/(questNum-1)*mu*mu;
		TDistribution t = new TDistribution(questNum-1);
		double tValue = (t.inverseCumulativeProbability((1+confidence)/2));
		num = (int) Math.ceil(4*sigma*Math.pow(tValue, 2)/Math.pow(interval, 2));
		
		return num;
	}
	
	public double GradeSample(int threshold){
		switch (dataset){
		case MOVIE_DATA:
			return GradedSampleMovie(threshold);
		case BOOK_DATA:
			return GradedSampleBook(threshold);
		case JOKE_DATA:
			return GradedSampleJoke(threshold);
		default:
			break;
		}
		return 0;
	}
	
	private double GradedSampleMovie(int threshold){
		for (int i = gradedSampleNum; i < threshold; i++){
			gradedSampleSum += movie.sample();
			gradedSampleNum++;
		}
		gradedScore = gradedSampleSum / gradedSampleNum;
		return gradedScore;
	}

	private double GradedSampleBook(int threshold){
		for (int i = gradedSampleNum; i < threshold; i++){
			gradedSampleSum += book.sample();
			gradedSampleNum++;
		}
		gradedScore = gradedSampleSum / gradedSampleNum;
		return gradedScore;
	}
	
	private double GradedSampleJoke(int threshold){
		final Random rand = new Random();
		for (int i = gradedSampleNum; i < threshold; i++){
			int user = rand.nextInt(Object.JokeInfo.size());
			gradedSampleSum += Object.JokeInfo.get(user).getJokeRatings().get(this.getID());
			gradedSampleNum++;
		}
		gradedScore = gradedSampleSum / gradedSampleNum;
		return gradedScore;
	}
	
	private int GradedJudgeMovie(Object o, int threshold){
		double score_me, score_o;
		
		if (threshold < 1){
			System.err.println("Graded Judge with threshold less than 1!");
			System.exit(1);
		}
		
		CompRecord record = new CompRecord();
		
		if(RecordHash.containsKey(o.getID())){
			record = RecordHash.get(o.getID());
		}
		else {
			RecordHash.put(o.getID(), record);
		}
		
		CountOfSample++;
		
		score_me = GradedSampleMovie(threshold);
		score_o = o.GradedSampleMovie(threshold);

		if (score_me > score_o){
			return ID;
		}
		else if (score_me < score_o){
			return o.getID();
		}
		else {
			return -1;
		}
	}
	
	private int GradedLikertJudgeMovie(Object o, int threshold){
		double score_me, score_o;
		
		if (threshold < 1){
			System.err.println("Graded Likert Judge with threshold less than 1!");
			System.exit(1);
		}
		
		CompRecord record = new CompRecord();
		
		if(RecordHash.containsKey(o.getID())){
			record = RecordHash.get(o.getID());
		}
		else {
			RecordHash.put(o.getID(), record);
		}
		
		CountOfSample++;
		
		score_me = GradedSampleMovie(threshold);
		score_o = o.GradedSampleMovie(threshold);

		if (score_me > score_o){
			return ID;
		}
		else if (score_me < score_o){
			return o.getID();
		}
		else {
			return -1;
		}
	}
	
	public double photoSample(Object o){
		Integer idA = ID;
		Integer idB = o.ID;
		String key = idA.toString() + "-" + idB.toString();
		Random random = new Random();
		ArrayList<Double> judgments = PhotoJudgments.get(key);
		int randomIndex = random.nextInt(judgments.size());
		
		return judgments.get(randomIndex);
	}

	
	public double movieSample(Object o){
		double a = movie.sample();
		double b = o.movie.sample();
		// test !!!!!!!!!!!!!!!
		return (a - b) / 10;	
	}

	public double bookSample(Object o){
		double a = book.sample();
		double b = o.book.sample();
		return (a - b) / 9;
	}
	
	// compare two objects by mean
	public static int meanCompareWithParent(Object a, Object b){
		double mu;
		CompRecord record;
		
		// get comparison result
		if (b.isParentOf(a.getID())){
			return b.getID();
		}
		else if (a.isParentOf(b.getID())){
			return a.getID();
		}
		
		// result not found, compare by mean
		if (a.getID() == b.getID()){
			mu = 0;
		}
		else if (b.getID() > a.getID()){
			record = b.getRecordHash().get(a.getID());
			mu = -1 * record.getSum() / record.getQuestNum();
		}
		else {
			record = a.getRecordHash().get(b.getID());
			mu = record.getSum() / record.getQuestNum();
		}
		
		// return winnerID
		if (mu < 0){
			return b.getID();
		}
		else {
			return a.getID();
		}
	}
	
	// compare two objects by giving a reference
	public static int referenceMeanCompare(Object reference, Object a, Object b){
		double aMu, bMu;
		CompRecord record;
		
		// get comparison result
		if (b.isParentOf(a.getID())){
			return b.getID();
		}
		else if (a.isParentOf(b.getID())){
			return a.getID();
		}
		
		// result not found, compare by mean
		if (reference.getID() == a.getID()){
			aMu = 0;
		}
		else if (reference.getID() > a.getID()){
			record = reference.getRecordHash().get(a.getID());
			aMu = -1 * record.getSum() / record.getQuestNum();
		}
		else {
			record = a.getRecordHash().get(reference.getID());
			aMu = record.getSum() / record.getQuestNum();
		}
		
		if (reference.getID() == b.getID()){
			bMu = 0;
		} else if (reference.getID() > b.getID()){
			record = reference.getRecordHash().get(b.getID());
			bMu = -1 * record.getSum() / record.getQuestNum();
		}
		else {
			record = b.getRecordHash().get(reference.getID());
			bMu = record.getSum() / record.getQuestNum();
		}
		
		
		// return winnerID
		if (aMu < bMu){
			return b.getID();
		}
		else {
			return a.getID();
		}
	}

	// compare two objects by mean
	public static int meanCompare(Object a, Object b){
		CompRecord record;
		double mu;
		
		record = a.getRecord(b);
		if (a.getID() == b.getID()){
			return -1;
		}
		else if (a.getID() > b.getID()){
			mu = record.getSum() / record.getQuestNum();
		}
		else {
			mu = -1 * record.getSum() / record.getQuestNum();					
		}
		
		// return winnerID
		if (mu < 0){
			return b.getID();
		} 
		else {
			return a.getID();
		}
	}
	
	
	public double jokeSample(Object o, Random rand){
		//Random rand = new Random();
		int user = rand.nextInt(Object.JokeInfo.size());
		double a = Object.JokeInfo.get(user).getJokeRatings().get(this.getID());
		double b = Object.JokeInfo.get(user).getJokeRatings().get(o.getID());
		//return a - b;
		return ( a - b ) / 20;
	}
}