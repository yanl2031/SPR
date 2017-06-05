package pairwiseCrowdsourcing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PBR extends TopkAlgorithm {
	class PBRComparator implements Comparator<Object> {
	    @Override
	    public int compare(Object a, Object b) {
	    	int aWinNum = 0, bWinNum = 0;
	    	int winnerID;
			for (Object o: ObjInfo){
				winnerID = Object.meanCompare(a, o);
				if (winnerID == a.getID()){
					aWinNum++;
				}
				winnerID = Object.meanCompare(b, o);
				if (winnerID == b.getID()){
					bWinNum++;
				}				
			}
			
			if (aWinNum > bWinNum){//a won, 
				return -1;
			}
			else if (aWinNum < bWinNum){
				return 1;
			}
	    	
	    	return 0;
	    }
	}
	
	
	public void Run(){
		Race();
	}
	
	public void Race(){
		Race(ObjInfo);
	}
	
	public void Race(ArrayList<Object> objSet){
		ArrayList<Object> selected = new ArrayList<Object>(); 
		ArrayList<Object> discarded = new ArrayList<Object>();
		ArrayList<Integer> z = new ArrayList<Integer>();
		ArrayList<Integer> w = new ArrayList<Integer>();
		int threshold = Object.minSampleNumCI;
		int winnerID;
		Object a, b;
		// statistic info
		int batchSize = 30;

		// initialize z and w
		for (int i = 0; i < objSet.size(); i++) {
			z.add(0);
			w.add(0);
		}
		
		// pre-compute the all the comparison with initial budget  
		for (int i = 0; i < objSet.size(); i++){
			a = objSet.get(i);
			for (int j = 0; j < objSet.size(); j++){
				b = objSet.get(j);
				winnerID = a.Compare(b, threshold);
				TotalQuest++;
			}
		}
				
		
		while (threshold <= max_threshold){
			for (int i = 0; i < objSet.size(); i++){
				a = objSet.get(i);
				if (!(discarded.contains(a) || selected.contains(a))){
					for (int j = 0; j < objSet.size(); j++){
						b = objSet.get(j);
						if (!(discarded.contains(b) || selected.contains(b))){
							TotalQuest += a.getCompareQuestIncrement(b);
							winnerID = a.Compare(b, threshold);
						}
					}
				}
			}
			
			//SSCO
			for (int i = 0; i < objSet.size(); i++){
				a = objSet.get(i);
				z.set(i, 0);
				w.set(i, 0);
				for (int j = 0; j < objSet.size(); j++){
					b = objSet.get(j);
					winnerID = a.Compare(b, loose_threshold);
					if (winnerID == a.getID()){
						w.set(i, w.get(i) + 1);
						continue;
					}
					if (winnerID == b.getID()){
						z.set(i, z.get(i) + 1);
						continue;
					}
				}
			}
			
			for (int i = 0; i < objSet.size(); i++){
				int winNum = 0;
				int loseNum = 0;
				
				a = objSet.get(i);
				
				// check if the object is already pruned
				if (selected.contains(a) || discarded.contains(a)){
					continue;
				}
				
				for (int j = 0; j < objSet.size(); j++){
					if (objSet.size() - z.get(j) < w.get(i)){
						winNum++;
					}
					if (objSet.size() - w.get(j) < z.get(i)){
						loseNum++;
					}		
				}
				
				// select o_i
				if (objSet.size() - TopK <= winNum){
					selected.add(objSet.get(i));
				}
				
				//discard o_i
				if (TopK <= loseNum){
					discarded.add(objSet.get(i));
				}
			}
			
			if (threshold < max_threshold){
				threshold = Math.min(threshold + batchSize, max_threshold);
			}
			else {
				break;
			}
			//threshold += (max_threshold - loose_threshold);
		}
		
		LatencyRound += incrementalLatency(max_threshold);
		
		objSet.removeAll(discarded);
		Collections.sort(objSet, new PBRComparator());
		
		while (TopKCandidate.size() < TopK){
			TopKCandidate.add(objSet.remove(0));
		}
	}
}
