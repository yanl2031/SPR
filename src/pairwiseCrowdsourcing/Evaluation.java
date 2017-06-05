package pairwiseCrowdsourcing;

import java.io.*;

import java.util.*;

//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.WorkbookFactory;

import pairwiseCrowdsourcing.Object.Model;



class ScoreQuest{
	private double deltaScore;
	private double numQuest;
	private double accuracy;
	
	public double getDeltaScore() {
		return deltaScore;
	}
	public void setDeltaScore(double deltaScore) {
		this.deltaScore = deltaScore;
	}
	public double getNumQuest() {
		return numQuest;
	}
	public void setNumQuest(double numQuest) {
		this.numQuest = numQuest;
	}
	public double getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}
	
}

class DeltaScoreComparator implements Comparator<ScoreQuest> {
    @Override
    public int compare(ScoreQuest a, ScoreQuest b) {
		if (a.getDeltaScore() > b.getDeltaScore()){//a won, 
			return -1;
		}
		else if (a.getDeltaScore() < b.getDeltaScore()){
			return 1;
		}
    	return 0;
    }
}

public class Evaluation {
	
	ArrayList<Object> ObjInfo = new ArrayList<Object>();
	Hashtable<Integer, Object> ObjHash= new Hashtable<Integer, Object>();
	
	int TopKRatio;
	int itemNum;
	double confidence;
	int max_threshold;
	double c = 1.5;
	
	public void Display(ArrayList<Object> arr){
		for(int i=0;i<arr.size();i++){
			System.out.print(arr.get(i).getID()+"/"+arr.get(i).getScore()+"/"+arr.get(i).getPosition()+" ");
		}
		System.out.println();
		System.out.println("SIZE "+arr.size());
	}
	
	public void ReadItemInfo(String filename, int itemNum) throws FileNotFoundException, IOException{
		//to compare with simple topk algorithm, these two algorithms should use the same input PositioinScoreID.txt
		// format: Position| Score| ID
		Scanner  cin = new Scanner(new File(filename));
		
		String line;
		while(cin.hasNext()){
	        line = cin.nextLine();
	        String ss [] = line.split(" ");
	        Object item = new Object();
	        int id = Integer.parseInt(ss[0]);
	        double score = Double.parseDouble(ss[1]);
	        int pos = Integer.parseInt(ss[2]);
	       
	        item.setID(id);
	        item.setScore(score);
	        item.setPosition(pos);
	        
	        
	        ObjInfo.add(item);
	        ObjHash.put(id,item);
		}
		
		cin.close();
		
		Collections.sort(ObjInfo, new PositionComparator());
		//Display(ObjInfo);
		if (ObjInfo.size() > itemNum){
			ObjInfo = new ArrayList<Object>(ObjInfo.subList(0, itemNum));
			Display(ObjInfo);
		}

		itemNum = ObjInfo.size();
		System.out.format("#Items: %d%n", itemNum);
		
	}
	

	public void ReadMovieData(String filename, int maxItemNum) throws IOException{
		Scanner  cin = new Scanner(new File(filename), "UTF-8");
		// read moive data 
		// format distritbuion | vote| rank| name
		
		String line;
		int i = 1;
		while(cin.hasNext()){
			line = cin.nextLine();
			String ss [] = line.trim().split("\\s+", 4);
//			System.out.println("i "+i);// id
//			System.out.println(ss[0]);// distribution
//			System.out.println(ss[1]);// vote
//			System.out.println(ss[2]);// rank
//			System.out.println(ss[3]);// title
//			System.exit(0);
			
			String distribution = ss[0];
			int vote = Integer.parseInt(ss[1]);
			double rank = Double.parseDouble(ss[2]);
			String title = ss[3];
			
			if(vote > 100000){ // filter vote less than 100,000
				//System.out.println(ss[0]+"*");
				Object item = new Object();
				
				int id = i;
				item.setID(id);
				
				Movie movie = new Movie();
				movie.setDistributionValue(distribution);
				movie.setTitle(title);
				movie.setVote(vote);
				movie.setRank(rank);
				movie.convertDistribution();
				
				item.setMovie(movie);
				item.setScore(movie.getMean());
				
				i++;
				
				ObjInfo.add(item);
			    ObjHash.put(id,item);
			}
		}
		cin.close();
		
		System.out.println("Num of Movies:" + ObjInfo.size());
		
		maxItemNum = Math.min(ObjInfo.size(), maxItemNum);
		ObjInfo = new ArrayList<Object>(ObjInfo.subList(0, maxItemNum));
		
		Collections.sort(ObjInfo, new MeanComparator());
		
		
		for(int j = 0 ; j < ObjInfo.size() ; j++  ){
			ObjInfo.get(j).setPosition(j+1);
			//System.out.println(ObjInfo.get(j).getPosition()+"\t"+ObjInfo.get(j).getMovie().getMean()+"\t"+ObjInfo.get(j).getMovie().getTitle());
		}
		//Collections.shuffle(ObjInfo);
		//System.out.println(ObjInfo.size());
	}
	public void ReadRealData(String filename, int itemNum) throws FileNotFoundException, IOException{
		//to compare with simple topk algorithm, these two algorithms should use the same input PositioinScoreID.txt
		// format: Position| Score| ID
		for(int i = 0 ; i < 15 ; i++){
			Scanner  cin = new Scanner(new File(filename));
			
			String line;
			while(cin.hasNext()){
				line = cin.nextLine();
		        String ss [] = line.split("\t");
		        int idA = Integer.parseInt(ss[0]);
		        int idB = Integer.parseInt(ss[1]);
		        double score = Double.parseDouble(ss[2]);
		        ItemPair pair;
		       
		        
		        if (!ObjHash.containsKey(idA)){
			        Object item = new Object();	        
			        item.setID(idA);
			        item.setScore(-1);
			        item.setPosition(idA);
			        ObjInfo.add(item);
			        ObjHash.put(idA, item);
		        }
	
		        if (!ObjHash.containsKey(idB)){
			        Object item = new Object();	        
			        item.setID(idB);
			        item.setScore(-1);
			        item.setPosition(idB);
			        ObjInfo.add(item);
			        ObjHash.put(idB, item);
		        }
		        
		        if (idA > idB){
		        	if (!ObjHash.get(idA).getItemPairs().containsKey(idB)){
		        		pair = new ItemPair();
		        		pair.setIdA(idA);
		        		pair.setIdB(idB);
		        		pair.setNextQuest(0);
		        		ObjHash.get(idA).getItemPairs().put(idB, pair);
		        	}
		        	pair = ObjHash.get(idA).getItemPairs().get(idB);
		        }
		        else {
		        	if (!ObjHash.get(idB).getItemPairs().containsKey(idA)){
		        		pair = new ItemPair();
		        		pair.setIdA(idB);
		        		pair.setIdB(idA);
		        		pair.setNextQuest(0);
		        		ObjHash.get(idB).getItemPairs().put(idA, pair);
		        	}
		        	pair = ObjHash.get(idB).getItemPairs().get(idA);
		        }
		        
		        pair.getQuest().add(score);
			}
			
			cin.close();
		}
		Collections.sort(ObjInfo, new PositionComparator());
		//Display(ObjInfo);
		if (ObjInfo.size() > itemNum){
			ObjInfo = new ArrayList<Object>(ObjInfo.subList(0, itemNum));
			Display(ObjInfo);
		}

		this.itemNum = ObjInfo.size();
		System.out.format("#Items: %d%n", this.itemNum);
		
	}
	
	public int getQuestNum(int indexA, int indexB, int maxQuest){
		Object a,b;
		int questNum;
		
		a = ObjInfo.get(indexA).clone();
		b = ObjInfo.get(indexB).clone();
		
		a.Compare(b, maxQuest);
		questNum = a.getCompareQuestIncrement(b);
		
		return questNum;
	}
	
/*	public void testEnumeration(int loop, PrintStream outprint) throws IOException{
		// Number of Quests
		//double questPerfect;
		double questInfimum;
		double questTournament;
		double questMinheap;
		double questQuickselect;
		double questRandomselect;
		double questRandomselectHeuristic;
		double questBubbleSort;
		
		
		// NDCG
		double aveNDCGTournament;
		double aveNDCGMinheap;
		double aveNDCGQuickselect;
		double aveNDCGRandomselect;
		double aveNDCGRandomselectHeuristic;
		double aveNDCGBubbleSort;
		
		// Precision
		double avePrecisionTournament;
		double avePrecisionMinheap;
		double avePrecisionQuickselect;
		double avePrecisionRandomselect;
		double avePrecisionRandomselectHeuristic;
		double avePrecisionBubbleSort;
		
		
		// latency (rounds)
		double roundInfimum;
		double roundTournament;
		double roundMinheap;
		double roundQuickselect;
		double roundRandomselect;
		double roundRandomselectHeuristic;
		double roundBubbleSort;
		
		
		Random random = new Random(20310);
		
		// methods
		//PerfectHeap perfect = new PerfectHeap();
//		Infimum infimum = new Infimum();
//		Tournament tournament = new Tournament();
//		MinHeapTie minheap = new MinHeapTie();
//		QuickSelect quickselect = new QuickSelect();
		RandomSelection randomselect = new RandomSelection(false);
		RandomSelection randomselectHeuristic = new RandomSelection(true);
		BubbleSort bubbleSort = new BubbleSort();
		
		ArrayList<Object> dataset = new ArrayList<Object>();
		
//		// construct testing dataset
//		Collections.sort(ObjInfo, new PositionComparator());
//		if (itemNum < ObjInfo.size()){
//			dataset = new ArrayList<Object>(ObjInfo.subList(0, itemNum));
//			ObjInfo.removeAll(dataset);
//		}
//		else {
//			dataset.addAll(ObjInfo);
//			ObjInfo.clear();
//		}
		
		// initialization
		
		// initialize number of quests
		// questPerfect = 0;
		questInfimum = 0;
		questTournament = 0;
		questMinheap = 0;
		questQuickselect = 0;
		questRandomselect = 0;
		questRandomselectHeuristic = 0;
		questBubbleSort = 0;
		
		// initialize precision
		avePrecisionTournament = 0;
		avePrecisionMinheap = 0;
		avePrecisionQuickselect = 0;
		avePrecisionRandomselect = 0;
		avePrecisionRandomselectHeuristic = 0;
		avePrecisionBubbleSort = 0;
		
		// initialize NDCG
		aveNDCGTournament = 0;
		aveNDCGMinheap = 0;
		aveNDCGQuickselect = 0;
		aveNDCGRandomselect = 0;
		aveNDCGRandomselectHeuristic = 0;
		aveNDCGBubbleSort = 0;
		
		// initialize latency
		roundInfimum = 0;
		roundTournament = 0;
		roundMinheap = 0;
		roundQuickselect = 0;
		roundRandomselect = 0;
		roundRandomselectHeuristic = 0;
		roundBubbleSort = 0;
		
		
		// set the parameters
		//perfect.setTopK(TopK);
//		infimum.setTopK(TopK);
//		tournament.setTopK(TopK);
//		minheap.setTopK(TopK);
//		quickselect.setTopK(TopK);
		randomselect.setTopK(TopK);
		randomselectHeuristic.setTopK(TopK);
		bubbleSort.setTopK(TopK);
		
		//set confidence
		Object.confidence = confidence;
		
		//set max_threshold
		//perfect.setMax_threshold(max_threshold);
//		infimum.setMax_threshold(max_threshold);
//		tournament.setMax_threshold(max_threshold);
//		minheap.setMax_threshold(max_threshold);
//		quickselect.setMax_threshold(max_threshold);
		randomselect.setMax_threshold(max_threshold);
		randomselectHeuristic.setMax_threshold(max_threshold);
		bubbleSort.setMax_threshold(max_threshold);
		
		// set random selection parameters
		randomselect.setSweetspot(c);
		randomselectHeuristic.setSweetspot(c);
		randomselect.setRangeConfidence(1 - delta);
		randomselectHeuristic.setRangeConfidence(1 - delta);
		
		// output redirection
		PrintStream console = System.out;
		File file = new File("out.txt");
		FileOutputStream fos = new FileOutputStream(file);
		PrintStream ps = new PrintStream(fos);
		System.setOut(ps);

		// run loop
		int m_Max = 13;
		int x_Max = 13;
		int best_m = 0;
		int best_x = 0;
		double min_cost = Double.MAX_VALUE;
	
		// for randonselect version
		for(int m = 1 ; m < m_Max ; m++){
			for(int x = 1 ; x < x_Max ; x++){
				
				// construct testing dataset
				Collections.sort(ObjInfo, new PositionComparator());
				if (itemNum < ObjInfo.size()){
					dataset = new ArrayList<Object>(ObjInfo.subList(0, itemNum));
					ObjInfo.removeAll(dataset);
				}
				else {
					dataset.addAll(ObjInfo);
					ObjInfo.clear();
				}
				
				for (int i = 0; i < loop; i++){
					// shuffle the item set
					Collections.shuffle(dataset, random);
					
					// reset statistics
					randomselect.clear();
					//randomselectHeuristic.clear();
		
					// reload item set
					randomselect.setObjInfo(dataset);
					//randomselectHeuristic.setObjInfo(dataset);
					
					// execute ranking algorithm
					randomselect.KSelection(x,m);
					//randomselectHeuristic.KSelection(x,m);

					questRandomselect += randomselect.getTotalQuest();
					//questRandomselectHeuristic += randomselectHeuristic.getTotalQuest();
				}
				if(questRandomselect < min_cost){
					min_cost = questRandomselect;
					best_m = m;
					best_x = x;
				}
				
				// put all the dataset back to pool
				ObjInfo.addAll(dataset);
				dataset.clear();
				questRandomselect = 0;
			}
		}

		
//		// for randomselection heuristic version
//		int best_m_heuristic = 0;
//		int best_x_heuristic = 0;
//		double min_cost_heuristic = Double.MAX_VALUE;
//		for(int m = 1 ; m < m_Max ; m++){
//			for(int x = 1 ; x < x_Max ; x++){
//				for (int i = 0; i < loop; i++){
//					// shuffle the item set
//					Collections.shuffle(dataset, random);
//					
//					// reset statistics
//					//randomselect.clear();
//					randomselectHeuristic.clear();
//		
//					// reload item set
//					//randomselect.setObjInfo(dataset);
//					randomselectHeuristic.setObjInfo(dataset);
//					
//					// execute ranking algorithm
//					//randomselect.KSelection(x,m);
//					randomselectHeuristic.KSelection(x,m);
//
//					//questRandomselect += randomselect.getTotalQuest();
//					questRandomselectHeuristic += randomselectHeuristic.getTotalQuest();
//				}
//				if(questRandomselectHeuristic < min_cost_heuristic){
//					min_cost_heuristic = questRandomselectHeuristic;
//					best_m_heuristic = m;
//					best_x_heuristic = x;
//				}
//				
//				// put all the dataset back to pool
//				ObjInfo.addAll(dataset);
//				dataset.clear();
//				
//				// construct testing dataset
//				Collections.sort(ObjInfo, new PositionComparator());
//				if (itemNum < ObjInfo.size()){
//					dataset = new ArrayList<Object>(ObjInfo.subList(0, itemNum));
//					ObjInfo.removeAll(dataset);
//				}
//				else {
//					dataset.addAll(ObjInfo);
//					ObjInfo.clear();
//				}
//			}
//		}
		
		// put all the dataset back to pool
		ObjInfo.addAll(dataset);
		dataset.clear();
		
		// compute average quests
		min_cost /= loop;
//		min_cost_heuristic /= loop;

		// print result into result file
		outprint.format(", %f, %f, %f, %f, %f, %f, , %f, %f, %f, %f, %f, %f, , %f, %f, %f, %f, %f, %f, , %f, %f, %f, %f, %f, %f %n",
				questInfimum, questTournament, questMinheap, questQuickselect, questRandomselect, questRandomselectHeuristic,
				1.0, aveNDCGTournament, aveNDCGMinheap, aveNDCGQuickselect, aveNDCGRandomselect, aveNDCGRandomselectHeuristic,
				1.0, avePrecisionTournament, avePrecisionMinheap, avePrecisionQuickselect, avePrecisionRandomselect, avePrecisionRandomselectHeuristic,
				roundInfimum, roundTournament, roundMinheap, roundQuickselect, roundRandomselect, roundRandomselectHeuristic);
		
		// reset output to console
		System.setOut(console);
		
		// display average quest
		System.out.format("Average Number of Quest in %d times execution:%n", loop);
		//System.out.format("%-20s %d%n", "Perfect: ", questPerfect);
//		System.out.format("%-20s %-20.2f %-10s %-10.2f %-15s %-10.2f %-15s %-10.2f %n", "Infimum: ", questInfimum, "NDCG: ", 1.0, "Precision: ", 1.0, "Latency: ", roundInfimum);
//		System.out.format("%-20s %-20.2f %-10s %-10.2f %-15s %-10.2f %-15s %-10.2f %n", "Tournament: ", questTournament, "NDCG: ", aveNDCGTournament, "Precision: ", avePrecisionTournament, "Latency: ", roundTournament);
//		System.out.format("%-20s %-20.2f %-10s %-10.2f %-15s %-10.2f %-15s %-10.2f %n", "MinHeap: ", questMinheap, "NDCG: ", aveNDCGMinheap, "Precision: ", avePrecisionMinheap, "Latency: ", roundMinheap);
//		System.out.format("%-20s %-20.2f %-10s %-10.2f %-15s %-10.2f %-15s %-10.2f %n", "Quick Selection: ", questQuickselect, "NDCG: ", aveNDCGQuickselect, "Precision: ", avePrecisionQuickselect, "Latency: ", roundQuickselect);
//		System.out.format("%-20s %-20.2f %-10s %-10.2f %-15s %-10.2f %-15s %-10.2f %n", "Random Selection: ", questRandomselect, "NDCG: ", aveNDCGRandomselect, "Precision: ", avePrecisionRandomselect, "Latency: ", roundRandomselect);
//		System.out.format("%-20s %-20.2f %-10s %-10.2f %-15s %-10.2f %-15s %-10.2f %n", "Random Selection Heuristic: ", questRandomselectHeuristic, "NDCG: ", aveNDCGRandomselectHeuristic, "Precision: ", avePrecisionRandomselectHeuristic, "Latency: ", roundRandomselectHeuristic);		
//		System.out.format("%-20s %-20.2f %-10s %-10.2f %-15s %-10.2f %-15s %-10.2f %n", "BubbleSort: ", questBubbleSort, "NDCG: ", aveNDCGBubbleSort, "Precision: ", avePrecisionBubbleSort, "Latency: ", roundBubbleSort);
		
//		System.out.format("%-20s %-20.2f %-10s %-10.2f %-15s %-10.2f %-15s %-10.2f %n", "Random Selection: ", min_cost, "x: ", best_x, "m: ", best_m);
//		System.out.format("%-20s %-20.2f %-10s %-10.2f %-15s %-10.2f %-15s %-10.2f %n", "Random Selection Heuristic: ", min_cost_heuristic, "x: ", best_x_heuristic, "m: ", best_m_heuristic);	
		System.out.println("Random Selection: "+ min_cost+" x: "+best_x+ " m: "+best_m);
//		System.out.println("Random Selection Heuristic: "+ min_cost_heuristic+" x: "+best_x_heuristic+ " m: "+best_m_heuristic);
		
	}*/
	
	public void test(int loop, PrintStream outprint) throws IOException{
		// Number of Quests
		//double questPerfect;
		double questInfimum;
		double questTournament;
		double questMinheap;
		double questQuickselect;
		double questPBR;
		double questRandomselect;
		double questRandomselectHeuristic;
		double questBubbleSort;

		
		// NDCG
		double aveNDCGTournament;
		double aveNDCGMinheap;
		double aveNDCGQuickselect;
		double aveNDCGPBR;
		double aveNDCGRandomselect;
		double aveNDCGRandomselectHeuristic;
		double aveNDCGBubbleSort;
		
		// Precision
		double avePrecisionTournament;
		double avePrecisionMinheap;
		double avePrecisionQuickselect;
		double avePrecisionPBR;
		double avePrecisionRandomselect;
		double avePrecisionRandomselectHeuristic;
		double avePrecisionBubbleSort;
		
		
		// latency (rounds)
		double roundInfimum;
		double roundTournament;
		double roundMinheap;
		double roundQuickselect;
		double roundPBR;
		double roundRandomselect;
		double roundRandomselectHeuristic;
		double roundBubbleSort;

		
		Random random = new Random(20310);
		
		// methods
		//PerfectHeap perfect = new PerfectHeap();
		Infimum infimum = new Infimum();
		Tournament tournament = new Tournament();
		MinHeapTie minheap = new MinHeapTie();
		QuickSelect quickselect = new QuickSelect();
		PBR pbr = new PBR();
		SPR randomselect = new SPR();
		randomselect.setHeuristic(false);
		//randomselect.strategy = RandomSelection.SelectionStrategy.KN;
		randomselect.strategy = SPR.SelectionStrategy.MedianMax;
		SPR randomselectHeuristic = new SPR();
		randomselectHeuristic.setHeuristic(true);
		BubbleSort bubbleSort = new BubbleSort();
		
		ArrayList<Object> dataset = new ArrayList<Object>();
		
		// construct testing dataset
		Collections.sort(ObjInfo, new PositionComparator());
		if (itemNum < ObjInfo.size()){
			dataset = new ArrayList<Object>(ObjInfo.subList(0, itemNum));
			ObjInfo.removeAll(dataset);
		}
		else {
			dataset.addAll(ObjInfo);
			ObjInfo.clear();
		}
		
		// initialization
		
		// initialize number of quests
		//questPerfect = 0;
		questInfimum = 0;
		questTournament = 0;
		questMinheap = 0;
		questQuickselect = 0;
		questPBR = 0;
		questRandomselect = 0;
		questRandomselectHeuristic = 0;
		questBubbleSort = 0;
		
		// initialize precision
		avePrecisionTournament = 0;
		avePrecisionMinheap = 0;
		avePrecisionQuickselect = 0;
		avePrecisionPBR = 0;
		avePrecisionRandomselect = 0;
		avePrecisionRandomselectHeuristic = 0;
		avePrecisionBubbleSort = 0;
		
		// initialize NDCG
		aveNDCGTournament = 0;
		aveNDCGMinheap = 0;
		aveNDCGQuickselect = 0;
		aveNDCGPBR = 0;
		aveNDCGRandomselect = 0;
		aveNDCGRandomselectHeuristic = 0;
		aveNDCGBubbleSort = 0;
		
		// initialize latency
		roundInfimum = 0;
		roundTournament = 0;
		roundMinheap = 0;
		roundQuickselect = 0;
		roundPBR = 0;
		roundRandomselect = 0;
		roundRandomselectHeuristic = 0;
		roundBubbleSort = 0;
		
		
		// set the parameters
		//int topk = (int) (TopKRatio * 0.01 * itemNum);
		int topk = TopKRatio;
		//perfect.setTopK(topk);
		infimum.setTopK(topk);
		tournament.setTopK(topk);
		minheap.setTopK(topk);
		quickselect.setTopK(topk);
//		pbr.setTopK(topk);
		randomselect.setTopK(topk);
		randomselectHeuristic.setTopK(topk);
		bubbleSort.setTopK(topk);

		
		//set confidence
		Object.confidence = confidence;
		
		//set max_threshold
		//perfect.setMax_threshold(max_threshold);
		infimum.setMax_threshold(max_threshold);
		tournament.setMax_threshold(max_threshold);
		minheap.setMax_threshold(max_threshold);
		quickselect.setMax_threshold(max_threshold);
		pbr.setMax_threshold(max_threshold);
		randomselect.setMax_threshold(max_threshold);
		randomselectHeuristic.setMax_threshold(max_threshold);
		bubbleSort.setMax_threshold(max_threshold);

		
		// set random selection parameters
		randomselect.setSweetspot(c);
		randomselectHeuristic.setSweetspot(c);

		
		// output redirection
		PrintStream console = System.out;
		File file = new File("out.txt");
		FileOutputStream fos = new FileOutputStream(file);
		PrintStream ps = new PrintStream(fos);
		System.setOut(ps);

		
		// run loop
		for (int i = 0; i < loop; i++){
			// shuffle the item set
			Collections.shuffle(dataset, random);
			
			// reset statistics
			//perfect.clear();
			infimum.clear();
			tournament.clear();
			minheap.clear();
			quickselect.clear();
			pbr.clear();
			randomselect.clear();
			randomselectHeuristic.clear();
			bubbleSort.clear();


			// reload item set
			
			//perfect.setObjInfo(dataset);
			infimum.setObjInfo(dataset);
			tournament.setObjInfo(dataset);
			minheap.setObjInfo(dataset);
			quickselect.setObjInfo(dataset);
			bubbleSort.setObjInfo(dataset);
			pbr.setObjInfo(dataset);
			randomselect.setObjInfo(dataset);
			randomselectHeuristic.setObjInfo(dataset);

			// execute ranking algorithm
			//perfect.MinHeap();
			//System.setOut(console);
			randomselect.KSelection();
//			//System.setOut(ps);
//			randomselectHeuristic.KSelection();


			// update total quest of each algorithm
			//questPerfect += perfect.getTotalQuest();
			questInfimum += infimum.getTotalQuest();
			questTournament += tournament.getTotalQuest();
			questMinheap += minheap.getTotalQuest();
			questQuickselect += quickselect.getTotalQuest();
			questBubbleSort += bubbleSort.getTotalQuest();
			questPBR += pbr.getTotalQuest();
			questRandomselect += randomselect.getTotalQuest();
			questRandomselectHeuristic += randomselectHeuristic.getTotalQuest();
			
			
			// update precision of each algorithm
			avePrecisionTournament += tournament.getPrecision();
			avePrecisionMinheap += minheap.getPrecision();
			avePrecisionQuickselect += quickselect.getPrecision();
			avePrecisionBubbleSort += bubbleSort.getPrecision();
			avePrecisionPBR += pbr.getPrecision();
			avePrecisionRandomselect += randomselect.getPrecision();
			avePrecisionRandomselectHeuristic += randomselectHeuristic.getPrecision();
			
			// update NDCG of each algorithm
			aveNDCGTournament += tournament.getNDCG();
			aveNDCGMinheap += minheap.getNDCG();
			aveNDCGQuickselect += quickselect.getNDCG();
			aveNDCGBubbleSort += bubbleSort.getNDCG();
			aveNDCGPBR += pbr.getNDCG();
			aveNDCGRandomselect += randomselect.getNDCG();
			aveNDCGRandomselectHeuristic += randomselectHeuristic.getNDCG();
			
			// update latency
			roundInfimum += infimum.getLatencyRound();
			roundTournament += tournament.getLatencyRound();
			roundMinheap += minheap.getLatencyRound();
			roundQuickselect += quickselect.getLatencyRound();
			roundBubbleSort += bubbleSort.getLatencyRound();
			roundPBR += pbr.getLatencyRound();
			roundRandomselect += randomselect.getLatencyRound();
			roundRandomselectHeuristic += randomselectHeuristic.getLatencyRound();
		}
		
		// put all the dataset back to pool
		ObjInfo.addAll(dataset);
		dataset.clear();
		
		
		// compute average quests
		//questPerfect /= loop;
		questInfimum /= loop;
		questTournament /= loop;
		questMinheap /= loop;
		questQuickselect /= loop;
		questBubbleSort /= loop;
		questPBR /= loop;
		questRandomselect /= loop;
		questRandomselectHeuristic /= loop;
		
		// compute average NDCG
		aveNDCGTournament /= loop;
		aveNDCGMinheap /= loop;
		aveNDCGQuickselect /= loop;
		aveNDCGBubbleSort /= loop;
		aveNDCGPBR /= loop;
		aveNDCGRandomselect /= loop;
		aveNDCGRandomselectHeuristic /= loop;
		
		// compute average precision
		avePrecisionTournament /= loop;
		avePrecisionMinheap /= loop;
		avePrecisionQuickselect /= loop;
		avePrecisionBubbleSort /= loop;
		avePrecisionPBR /= loop;
		avePrecisionRandomselect /= loop;
		avePrecisionRandomselectHeuristic /= loop;
		
		// compute average latency
		roundInfimum /= loop;
		roundTournament /= loop;
		roundMinheap /= loop;
		roundQuickselect /= loop;
		roundBubbleSort /= loop;
		roundPBR /= loop;
		roundRandomselect /= loop;
		roundRandomselectHeuristic /= loop;

		// print result into result file
		
		// print monetary cost title
		outprint.format(", Infimum TMC, Tournament TMC, MinHeap TMC, Quickselect TMC, SPR TMC, SPR Approximate TMC, BubbleSort TMC, PBR TMC, ELo TMC, TrueSkill TMC, SPR Online TMC, Grade TMC, Hybrid+SPR TMC, ");
		// print NDCG title
		outprint.format(", Infimum NDCG, Tournament NDCG, MinHeap NDCG, Quickselect NDCG, SPR NDCG, SPR Approximate NDCG, BubbleSort NDCG, PBR NDCG, ELo NDCG, TrueSkill NDCG, SPR Online NDCG, Grade NDCG, Hybrid+SPR NDCG, ");
		// print precision title
		outprint.format(", Infimum Precision, Tournament Precision, MinHeap Precision, Quickselect Precision, SPR Precision, SPR Approximate Precision, BubbleSort Precision, PBR Precision, ELo Precision, TrueSkill Precision, SPR Online Precsion, Grade Precision, Hybrid+SPR Precision, ");
		// print latency title
		outprint.format(", Infimum Latency, Tournament Latency, MinHeap Latency, Quickselect Latency, SPR Latency, SPR Approximate Latency, BubbleSort Latency, PBR Latency, ELo Latency, TrueSkill Latency, SPR Online Latency, Grade Precision, Hybrid+SPR Latency%n");
		
		// print monetary cost
		outprint.format(", %f, %f, %f, %f, %f, %f, %f, %f, ",
				questInfimum, questTournament, questMinheap, questQuickselect, questRandomselect, questRandomselectHeuristic, questBubbleSort, questPBR);
		// print NDCG
		outprint.format(", %f, %f, %f, %f, %f, %f, %f, %f, ",
				1.0, aveNDCGTournament, aveNDCGMinheap, aveNDCGQuickselect, aveNDCGRandomselect, aveNDCGRandomselectHeuristic, aveNDCGBubbleSort, aveNDCGPBR);
		// print precision
		outprint.format(", %f, %f, %f, %f, %f, %f, %f, %f, ",
				1.0, avePrecisionTournament, avePrecisionMinheap, avePrecisionQuickselect, avePrecisionRandomselect, avePrecisionRandomselectHeuristic, avePrecisionBubbleSort, avePrecisionPBR);
		// print latency
		outprint.format(", %f, %f, %f, %f, %f, %f, %f, %f, %n",
				roundInfimum, roundTournament, roundMinheap, roundQuickselect, roundRandomselect, roundRandomselectHeuristic, roundBubbleSort, roundPBR);
		// reset output to console
		System.setOut(console);
		
		// display average quest
		System.out.format("Average Number of Quest in %d times execution:%n", loop);
		//System.out.format("%-20s %d%n", "Perfect: ", questPerfect);
		System.out.format("%-20s %-20.2f %-10s %-10.2f %-15s %-10.2f %-15s %-10.2f %n", "Infimum: ", questInfimum, "NDCG: ", 1.0, "Precision: ", 1.0, "Latency: ", roundInfimum);
		System.out.format("%-20s %-20.2f %-10s %-10.2f %-15s %-10.2f %-15s %-10.2f %n", "Tournament: ", questTournament, "NDCG: ", aveNDCGTournament, "Precision: ", avePrecisionTournament, "Latency: ", roundTournament);
		System.out.format("%-20s %-20.2f %-10s %-10.2f %-15s %-10.2f %-15s %-10.2f %n", "MinHeap: ", questMinheap, "NDCG: ", aveNDCGMinheap, "Precision: ", avePrecisionMinheap, "Latency: ", roundMinheap);
		System.out.format("%-20s %-20.2f %-10s %-10.2f %-15s %-10.2f %-15s %-10.2f %n", "Quick Selection: ", questQuickselect, "NDCG: ", aveNDCGQuickselect, "Precision: ", avePrecisionQuickselect, "Latency: ", roundQuickselect);
		System.out.format("%-20s %-20.2f %-10s %-10.2f %-15s %-10.2f %-15s %-10.2f %n", "SPR: ", questRandomselect, "NDCG: ", aveNDCGRandomselect, "Precision: ", avePrecisionRandomselect, "Latency: ", roundRandomselect);
		System.out.format("%-20s %-20.2f %-10s %-10.2f %-15s %-10.2f %-15s %-10.2f %n", "SPR Approximate: ", questRandomselectHeuristic, "NDCG: ", aveNDCGRandomselectHeuristic, "Precision: ", avePrecisionRandomselectHeuristic, "Latency: ", roundRandomselectHeuristic);
		System.out.format("%-20s %-20.2f %-10s %-10.2f %-15s %-10.2f %-15s %-10.2f %n", "BubbleSort: ", questBubbleSort, "NDCG: ", aveNDCGBubbleSort, "Precision: ", avePrecisionBubbleSort, "Latency: ", roundBubbleSort);		
		System.out.format("%-20s %-20.2f %-10s %-10.2f %-15s %-10.2f %-15s %-10.2f %n", "PBR: ", questPBR, "NDCG: ", aveNDCGPBR, "Precision: ", avePrecisionPBR, "Latency: ", roundPBR);

	}
	
	
	public void ReadBookData(String filename) throws IOException{
		BufferedReader cin = new BufferedReader(new FileReader(new File(filename)));
		//Scanner  cin = new Scanner(new File(filename));
		String line;
		int i = 1; // object id
//		int count = 0; // frequency of each book 
		Hashtable <String, Book> BookInfo = new Hashtable <String, Book> ();
		
		// book format 
		// User-ID| ISBN| Book-Rating
		
		line = cin.readLine(); // first line is the tile
		while((line = cin.readLine()) != null){
			Book book = new Book ();
			String ss [] = line.split(";");
			String name = ss[1];
			name = name.substring(1, (name.length()-1));
			String judgeStr = ss[2];
			judgeStr = judgeStr.substring(1, (judgeStr.length()-1));
			int judge = Integer.parseInt(judgeStr);
			book.setBookName(name);
			if(!BookInfo.containsKey(name)){
				book.setDisValue(judge);
				BookInfo.put(name, book);			
			}
			else {
				book = BookInfo.get(name);
				book.setDisValue(judge);
				BookInfo.put(name, book);
			}
		}
		cin.close();
		
		// set score 
		Set<String> keys = BookInfo.keySet();
		Iterator<String> itr = keys.iterator();
		String str;
		double score = 0;
		Book tmp;
		while (itr.hasNext()) { 
			str = itr.next();
			if(BookInfo.get(str).getCount()>=50){
				Object item = new Object();
				item.setID(i);				
				item.setBook(BookInfo.get(str));
				tmp = BookInfo.get(str);
				for(int j = 1; j < tmp.getDistribution().length ; j++){
					score += tmp.getDistribution()[j]*j;
				}
				score /= tmp.getCount();
				item.setScore(score);
				score = 0;
				i++;
				
				ObjInfo.add(item);
			}
		}
	
		System.out.println("Object size : "+ObjInfo.size());
		
		// set position according to score
		 Collections.sort(ObjInfo, new ScoreComparator());
         for(int j = 0 ; j < ObjInfo.size() ; j++){
         	ObjInfo.get(j).setPosition(j+1);      	
         }
         

	}
	

	// test book dataset 
	public static void TestBookData(int loop, Object.Model model) throws IOException{
		Evaluation evaluation = new Evaluation();
		String filename = "./input/BX-Book-Ratings.csv";
		String resultFile = "./result_Book_May27_SPR_ChangingReference16.csv";
		FileOutputStream fostream = new FileOutputStream(resultFile);
		PrintStream outprint = new PrintStream(fostream);
		evaluation.ReadBookData(filename);
		
		Object.dataset = Object.Dataset.BOOK_DATA;
		Object.model = model;
		
		// evaluation on various parameters by 1000 times
		evaluation.TopKRatio = 10;
		evaluation.itemNum = 537;
		evaluation.confidence = 0.98;
		evaluation.max_threshold = 1000;
		
		outprint.format("#Loop: %d%n", loop);
		
		// test all parameters
		// test k, default set itemNum = 100, max_threshold = 100,  confidence = 0.95
		ArrayList<Integer> KParameterSet = new ArrayList<Integer>();
//		KParameterSet.add(1);
//		KParameterSet.add(5);
		KParameterSet.add(10);
//		KParameterSet.add(15);
//		KParameterSet.add(20);
//		KParameterSet.add(25);
//		KParameterSet.add(30);
		outprint.format("------ varying k ------%n");
		for(Integer indexTopK : KParameterSet){
			evaluation.TopKRatio = indexTopK;
			System.out.format("------ TopK: %d -- ItemNum: %d -- Max_threshold: %d -- Confidence: %.2f ----%n", evaluation.TopKRatio, evaluation.itemNum, evaluation.max_threshold, evaluation.confidence);
			outprint.format("%d", indexTopK);
			evaluation.test(loop, outprint);
		}
		evaluation.TopKRatio = 10;
//		
//		
//		// test itemNum, default set k = 10, max_threshold = 100, confidence = 0.95
//		outprint.format("%n------ varying #item ------%n");
//		outprint.format(",Infimum,Tournament,HeapSort,QuickSelect,SPR,SPR Approximate,"
//				+ " ,Infimum NDCG,Tournament NDCG,HeapSort NDCG,QuickSelect NDCG,SPR NDCG,SPR Approximate NDCG,"
//				+ " ,Infimum Precision,Tournament Precision,HeapSort Precision,QuickSelect Precision,SPR Precision,SPR Approximate Precision,"
//				+ " ,Infimum Latency,Tournament Latency,HeapSort Latency,QuickSelect Latency,SPR Latency,SPR Approximate Latency%n");
//		for (int itemNum = 25; itemNum <= 400; itemNum = itemNum * 2){
//			evaluation.itemNum = itemNum;				
//			System.out.format("------ TopK: %d -- ItemNum: %d -- Max_threshold: %d -- Confidence: %.2f ----%n", evaluation.TopKRatio, evaluation.itemNum, evaluation.max_threshold, evaluation.confidence);
//			outprint.format("%d", itemNum);
//			evaluation.test(loop, outprint);
//		}
//		evaluation.itemNum = 537;
//		
//		// test max_threshold, defalut set k = 10, itemNum = 100, confidence = 0.95
//		outprint.format("%n------ varying max threshold ------%n");
//		outprint.format(",Infimum,Tournament,HeapSort,QuickSelect,SPR,SPR Approximate,"
//				+ " ,Infimum NDCG,Tournament NDCG,HeapSort NDCG,QuickSelect NDCG,SPR NDCG,SPR Approximate NDCG,"
//				+ " ,Infimum Precision,Tournament Precision,HeapSort Precision,QuickSelect Precision,SPR Precision,SPR Approximate Precision,"
//				+ " ,Infimum Latency,Tournament Latency,HeapSort Latency,QuickSelect Latency,SPR Latency,SPR Approximate Latency%n");
//		ArrayList<Integer> MaxThresholdParameterSet = new ArrayList<Integer>();
//		MaxThresholdParameterSet.add(30);
//		MaxThresholdParameterSet.add(100);
//		MaxThresholdParameterSet.add(200);
//		MaxThresholdParameterSet.add(500);
//		//MaxThresholdParameterSet.add(1000);
//		MaxThresholdParameterSet.add(2000);
//		MaxThresholdParameterSet.add(4000);
//		for(Integer indexMax : MaxThresholdParameterSet){
//			evaluation.max_threshold = indexMax;
//			System.out.format("------ TopK: %d -- ItemNum: %d -- Max_threshold: %d -- Confidence: %.2f ----%n", evaluation.TopKRatio, evaluation.itemNum, evaluation.max_threshold, evaluation.confidence);
//			outprint.format("%d", indexMax);
//			evaluation.test(loop, outprint);
//		}
//		evaluation.max_threshold = 1000;
//		
		// test confidence, default set k = 10, itemNum = 100, max_threshold = 100
//		outprint.format("%n------ varying confidence ------%n");
//		ArrayList<Double> ConfidenceParameterSet = new ArrayList<Double>();
//		ConfidenceParameterSet.add(0.80);
//		ConfidenceParameterSet.add(0.85);
//		ConfidenceParameterSet.add(0.90);
//		ConfidenceParameterSet.add(0.95);
//		ConfidenceParameterSet.add(0.98);		
//		ConfidenceParameterSet.add(0.95);
//		ConfidenceParameterSet.add(0.96);
//		ConfidenceParameterSet.add(0.97);
//		//ConfidenceParameterSet.add(0.98);
//		ConfidenceParameterSet.add(0.99);
//		for(Double indexConfidence : ConfidenceParameterSet ){
//			evaluation.confidence = indexConfidence;
//			System.out.format("------ TopK: %d -- ItemNum: %d -- Max_threshold: %d -- Confidence: %.2f ----%n", evaluation.TopKRatio, evaluation.itemNum, evaluation.max_threshold, evaluation.confidence);
//			outprint.format("%f", indexConfidence);
//			evaluation.test(loop, outprint);
//		}
//		evaluation.confidence = 0.98;
//		
//		// test c, default set itemNum = 100, max_threshold = 100,  confidence = 0.98, c = 1.5 
//		outprint.format("%n------ varying c ------%n");
//		ArrayList<Double> CParameterSet = new ArrayList<Double>();
//		CParameterSet.add(1.25);
//		CParameterSet.add(1.5);
//		CParameterSet.add(1.75);
//		CParameterSet.add(2.0);
//		outprint.format("------ varying c ------%n");
//		for(Double cvalue : CParameterSet){
//			evaluation.c = cvalue;
//			System.out.format("------ TopK: %d -- ItemNum: %d -- Max_threshold: %d -- Confidence: %.2f -- Sweetspot: %.2f ----%n", evaluation.TopKRatio, evaluation.itemNum, evaluation.max_threshold, evaluation.confidence, evaluation.c);
//			outprint.format("%f", cvalue);
//			evaluation.test(loop, outprint);
//		}
//		evaluation.c = 1.5;
		
	}

	

	
	public static void TestMovieData(int loop) throws IOException {
		Evaluation evaluation = new Evaluation();
		String filename = "./input/moive.txt";
		String resultFile = "./result_Movie_May27_SPR_ChangingReference16.csv";
		FileOutputStream fostream = new FileOutputStream(resultFile);
		PrintStream outprint = new PrintStream(fostream);
		// set popularity threshold to filter movies (otherwise someone may complain the dataset is too large and sparse...)
		//evaluation.ReadMovieData(filename, 50000);
		evaluation.ReadMovieData(filename, 100000);
		
		Object.dataset = Object.Dataset.MOVIE_DATA;
		//Object.model = Object.model.STEIN_MODEL;
		Object.model = Model.CI_MODEL;
		
		// evaluation on various parameters by 1000 times
		evaluation.TopKRatio = 10;
		evaluation.itemNum = 1255; // (100,000 votes)
//		evaluation.itemNum = 2379; // (50,000 votes)
//		evaluation.itemNum = 6810; // (10,000 votes)
		evaluation.confidence = 0.98;
		//evaluation.confidence = 0.90;
		evaluation.max_threshold = 1000;
		
		outprint.format("#Loop: %d%n", loop);
		
		// test all parameters
		// test k, default set itemNum = 100, max_threshold = 100,  confidence = 0.95
		ArrayList<Integer> KParameterSet = new ArrayList<Integer>();
//		KParameterSet.add(1);
//		KParameterSet.add(5);
		KParameterSet.add(10);
//		KParameterSet.add(15);
//		KParameterSet.add(20);
//		KParameterSet.add(5);
//		KParameterSet.add(10);
//		KParameterSet.add(15);
//		KParameterSet.add(20);
//		KParameterSet.add(25);
//		KParameterSet.add(30);
//		KParameterSet.add(40);
//		KParameterSet.add(45);
//		KParameterSet.add(50);
//		KParameterSet.add(55);
//		KParameterSet.add(60);
//		KParameterSet.add(65);
//		KParameterSet.add(70);
//		KParameterSet.add(75);
//		KParameterSet.add(80);
//		KParameterSet.add(85);
//		KParameterSet.add(90);
//		KParameterSet.add(95);
//		KParameterSet.add(100);
		outprint.format("------ varying k ------%n");
		for(Integer indexTopK : KParameterSet){
			evaluation.TopKRatio = indexTopK;
			System.out.format("------ TopK: %d -- ItemNum: %d -- Max_threshold: %d -- Confidence: %.2f ----%n", evaluation.TopKRatio, evaluation.itemNum, evaluation.max_threshold, evaluation.confidence);
			outprint.format("%d", indexTopK);
			evaluation.test(loop, outprint);
		}
		evaluation.TopKRatio = 10;
//		
//		
//		// test itemNum, default set k = 10, max_threshold = 100, confidence = 0.95
//		outprint.format("%n------ varying #item ------%n");
//		for (int itemNum = 25; itemNum <= 800; itemNum = itemNum * 2){
//			evaluation.itemNum = itemNum;
//			System.out.format("------ TopK: %d -- ItemNum: %d -- Max_threshold: %d -- Confidence: %.2f ----%n", evaluation.TopKRatio, evaluation.itemNum, evaluation.max_threshold, evaluation.confidence);
//			outprint.format("%d", itemNum);
//			evaluation.test(loop, outprint);
//		}
//		evaluation.itemNum = 1255;
//		
//		// test max_threshold, defalut set k = 10, itemNum = 100, confidence = 0.95
//		outprint.format("%n------ varying max threshold ------%n");
//		ArrayList<Integer> MaxThresholdParameterSet = new ArrayList<Integer>();
//		MaxThresholdParameterSet.add(30);
//		MaxThresholdParameterSet.add(100);
//		MaxThresholdParameterSet.add(200);
//		MaxThresholdParameterSet.add(500);
//		//MaxThresholdParameterSet.add(1000);
//		MaxThresholdParameterSet.add(2000);
//		MaxThresholdParameterSet.add(4000);
//		for(Integer indexMax : MaxThresholdParameterSet){
//			evaluation.max_threshold = indexMax;
//			System.out.format("------ TopK: %d -- ItemNum: %d -- Max_threshold: %d -- Confidence: %.2f ----%n", evaluation.TopKRatio, evaluation.itemNum, evaluation.max_threshold, evaluation.confidence);
//			outprint.format("%d", indexMax);
//			evaluation.test(loop, outprint);
//		}
//		evaluation.max_threshold = 1000;
//		
//		// test confidence, default set k = 10, itemNum = 100, max_threshold = 100
//		outprint.format("%n------ varying confidence ------%n");
//		ArrayList<Double> ConfidenceParameterSet = new ArrayList<Double>();
//		ConfidenceParameterSet.add(0.80);
//		ConfidenceParameterSet.add(0.85);
//		ConfidenceParameterSet.add(0.90);
//		ConfidenceParameterSet.add(0.95);
//		ConfidenceParameterSet.add(0.98);
//		ConfidenceParameterSet.add(0.95);
//		ConfidenceParameterSet.add(0.96);
//		ConfidenceParameterSet.add(0.97);
//		//ConfidenceParameterSet.add(0.98);
//		ConfidenceParameterSet.add(0.99);
//		for(Double indexConfidence : ConfidenceParameterSet ){
//			evaluation.confidence = indexConfidence;
//			System.out.format("------ TopK: %d -- ItemNum: %d -- Max_threshold: %d -- Confidence: %.2f ----%n", evaluation.TopKRatio, evaluation.itemNum, evaluation.max_threshold, evaluation.confidence);
//			outprint.format("%f", indexConfidence);
//			evaluation.test(loop, outprint);
//		}
//		evaluation.confidence = 0.98;
//		
//		// test c, default set itemNum = 100, max_threshold = 100,  confidence = 0.98, c = 1.5 
//		outprint.format("------ varying c ------%n");
//		ArrayList<Double> CParameterSet = new ArrayList<Double>();
//		CParameterSet.add(1.25);
//		CParameterSet.add(1.5);
//		CParameterSet.add(1.75);
//		CParameterSet.add(2.0);
//		for(Double cvalue : CParameterSet){
//			evaluation.c = cvalue;
//			System.out.format("------ TopK: %d -- ItemNum: %d -- Max_threshold: %d -- Confidence: %.2f -- Sweetspot: %.2f ----%n", evaluation.TopKRatio, evaluation.itemNum, evaluation.max_threshold, evaluation.confidence, evaluation.c);
//			outprint.format("%f", cvalue);
//			evaluation.test(loop, outprint);
//		}
//		evaluation.c = 1.5;

	}
		
	public static void main(String[] args) throws IOException {
			
		TestMovieData(100);
		
//		TestBookData(30, Object.Model.CI_MODEL);
	}
}