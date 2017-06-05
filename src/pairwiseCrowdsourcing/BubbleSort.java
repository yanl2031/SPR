package pairwiseCrowdsourcing;

import java.util.ArrayList;

public class BubbleSort extends SPR{
	public void KSort(){
		KSort(ObjInfo);
	}
	
	public void FastBubbleSortApproximate(ArrayList<Object> objSet, int k, ArrayList<Integer> rounds, int threshold){
		Object reference, o;
		int maxRound = 0;
		
		reference = objSet.get(0);
		for (int i = 1; i < objSet.size(); i++){
			o = objSet.get(i);
			reference.Compare(o, 2*loose_threshold);
			TotalQuest += reference.getCompareQuestIncrement(o);
			maxRound = Math.max(maxRound, incrementalLatency(reference.getCompareQuestIncrement(o)));
		}
		rounds.add(maxRound);

		ReferenceMeanSort(objSet, reference);
		
		while (objSet.size() > Math.max(1.5*k, k + 10)){
			objSet.remove(objSet.size() - 1);
		}
		
		System.out.println("reference scan in sort: " + TotalQuest);
		ReferenceBubbleSort(objSet, k, reference, rounds, threshold);
		System.out.println("reference bubblesort: " + TotalQuest);
	}
	
	public void KSort(ArrayList<Object> objSet){
		ArrayList<Integer> rounds = new ArrayList<Integer>();
		
		FastBubbleSort(objSet, TopK, rounds, max_threshold);
		//FastBubbleSortApproximate(objSet, TopK, rounds, max_threshold);
		LatencyRound += rounds.stream().mapToInt(Integer::intValue).sum();
		rounds.clear();
		TopKCandidate.addAll(objSet.subList(0, TopK));
		Display(TopKCandidate);
		System.out.println("Total Quest: " + TotalQuest);
		System.out.println("Total Round: " + LatencyRound);
	}
}