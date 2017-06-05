package pairwiseCrowdsourcing;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import org.apache.commons.math3.util.CombinatoricsUtils;


public class SPR extends Tournament{
	class CustomComparator implements Comparator<Object> {
	    @Override
	    public int compare(Object a, Object b) {
	    	int winnerID;
			if (a.isParentOf(b.getID())){
				winnerID = a.getID();
			}
			else if (b.isParentOf(a.getID())){
				winnerID = b.getID();
			}
			else {
				winnerID = a.Compare(b, max_threshold); //the rest VS the heap top(minimum object)
				TotalQuest += a.getCompareQuestIncrement(b);
			}
			
			if (winnerID == a.getID()){//a won, 
				return -1;
			}
			else if (winnerID == b.getID()){
				return 1;
			}
	    	
	    	return 0;
	    }
	}
	
	class CustomComparatorLoose implements Comparator<Object> {
	    @Override
	    public int compare(Object a, Object b) {
	    	int winnerID;
			if (a.isParentOf(b.getID())){
				winnerID = a.getID();
			}
			else if (b.isParentOf(a.getID())){
				winnerID = b.getID();
			}
			else {
				winnerID = a.Compare(b, loose_threshold); //the rest VS the heap top(minimum object)
				TotalQuest += a.getCompareQuestIncrement(b);
			}
			
			if (winnerID == a.getID()){//a won, 
				return -1;
			}
			else if (winnerID == b.getID()){
				return 1;
			}
	    	
	    	return 0;
	    }
	}
	
	// Strategies for reference selection
	public enum SelectionStrategy {MedianMax, KN};
	public SelectionStrategy strategy = SelectionStrategy.MedianMax;
	// the selection proportion variable for kth-in-n method
	public double proportion = 1;

	int TopK_remain;
	double Sweetspot = 2;
	double RangeConfidence = 0.8;
	int MaxTopKRemain = 0;
	private boolean heuristic = false;
	
	public double getSweetspot() {
		return Sweetspot;
	}

	public void setSweetspot(double sweetspot) {
		Sweetspot = sweetspot;
	}

	public double getRangeConfidence() {
		return RangeConfidence;
	}

	public void setRangeConfidence(double rangeConfidence) {
		RangeConfidence = rangeConfidence;
	}

	public int getMaxTopKRemain() {
		return MaxTopKRemain;
	}

	public void setMaxTopKRemain(int maxTopKRemain) {
		MaxTopKRemain = maxTopKRemain;
	}
	
	public boolean isHeuristic() {
		return heuristic;
	}

	public void setHeuristic(boolean heuristic) {
		this.heuristic = heuristic;
	}

	public void BubbleSort(ArrayList<Object> objSet, int k, ArrayList<Integer> rounds, int threshold){
		int winnerID;
		Object a, b;
		
		k = Math.min(k, objSet.size());
		
		for (int i = 0; i < k; i++){
			for (int j = objSet.size() - 1; j > i; j--){
				a = objSet.get(j - 1);
				b = objSet.get(j);
				winnerID = a.Compare(b, threshold);
				TotalQuest += a.getCompareQuestIncrement(b);
				rounds.add((int) Math.ceil(a.getCompareQuestIncrement(b) / QuestNumPerRound));
				
				if (winnerID == -1){
					winnerID = Object.meanCompare(a, b);
				}
				
				if (winnerID == b.getID()){
					Swap(objSet, j, j - 1);
				}
			}
		}
		
		Display(objSet);
	}
	
	public void ReferenceMeanSort(ArrayList<Object> objSet, Object reference){
		int winnerID;
		Object a, b;
		
		Display(objSet);
		
		for (int i = 0; i < objSet.size(); i++){
			for (int j = 0; j < objSet.size() - i - 1; j++){
				a = objSet.get(j);
				b = objSet.get(j + 1);
				winnerID = Object.referenceMeanCompare(reference, a, b);
				if (winnerID == b.getID()){
					Swap(objSet, j, j + 1);
				}
			}
		}
		
		Display(objSet);
	}
	
	public void ReferenceBubbleSort(ArrayList<Object> objSet, int k, Object reference, ArrayList<Integer> rounds, int threshold){
		int winnerID;
		Object a, b;
		
		k = Math.min(k, objSet.size());
		
		ReferenceMeanSort(objSet, reference);
		
		for (int i = 0; i < k; i++){
			for (int j = objSet.size() - 1; j > i; j--){
				a = objSet.get(j - 1);
				b = objSet.get(j);
				winnerID = a.Compare(b, threshold);
				TotalQuest += a.getCompareQuestIncrement(b);
				rounds.add((int) Math.ceil(a.getCompareQuestIncrement(b) / QuestNumPerRound));
				
//				if (winnerID == -1){
//					winnerID = Object.meanCompare(a, b);
//				}
				
				if (winnerID == b.getID()){
					Swap(objSet, j, j - 1);
				}
			}
		}
		
		Display(objSet);
	}

	public void FastBubbleSort(ArrayList<Object> objSet, int k, ArrayList<Integer> rounds, int threshold){
		FastBubbleSort(objSet, k, rounds, threshold, false);
	}
	
	public void FastBubbleSort(ArrayList<Object> objSet, int k, ArrayList<Integer> rounds, int threshold, boolean approximate){
		Object reference;
		reference = objSet.get(0);
		FastBubbleSort(objSet, k, reference, rounds, threshold, approximate);
	}
	
	public void FastBubbleSort(ArrayList<Object> objSet, int k, Object reference, ArrayList<Integer> rounds, int threshold){
		FastBubbleSort(objSet, k, reference, rounds, threshold, false);
	}
		
	public void FastBubbleSort(ArrayList<Object> objSet, int k, Object reference, ArrayList<Integer> rounds, int threshold, boolean approximate){
		Object o;
		int maxRound = 0;
		
		for (int i = 0; i < objSet.size(); i++){
			o = objSet.get(i);
			reference.Compare(o, loose_threshold);
			TotalQuest += reference.getCompareQuestIncrement(o);
			maxRound = Math.max(maxRound, incrementalLatency(reference.getCompareQuestIncrement(o)));
		}
		rounds.add(maxRound);
		System.out.println("reference scan in sort: " + TotalQuest);
		if (approximate){
			ReferenceMeanSort(objSet, reference);
		}
		else {
			ReferenceBubbleSort(objSet, k, reference, rounds, threshold);
		}
		System.out.println("reference bubblesort: " + TotalQuest);		
	}
	
	// sample distinct objects
	public Object RandomPickReference(ArrayList<Object> objSet, int sampleNum, ArrayList<Integer> rounds){
		ArrayList<Object> testedObjs = new ArrayList<Object>();
		Random random = new Random();
		int s;
		int winnerID;
		Object max, o;
		int maxRound = 0;
		int roundSum = 0;
		ArrayList<Integer> subRounds = new ArrayList<Integer>();

		if (objSet.size() == 0){
			System.err.println("Sampling from emptyset !!");
			System.exit(1);
		}
		
		s = random.nextInt(objSet.size());
		max = objSet.remove(s);
		
		for (int i = 1; i < sampleNum; i++){
			s = random.nextInt(objSet.size());
			o = objSet.remove(s);
			winnerID = max.Compare(o, loose_threshold);
			//winnerID = max.Compare(o, max_threshold);
			winnerID = Object.meanCompare(max, o);
			TotalQuest += max.getCompareQuestIncrement(o);
			//maxQuest = Math.max(max.getCompareQuestIncrement(o), maxQuest);
			subRounds.add(incrementalLatency(max.getCompareQuestIncrement(o)));
			
			if (winnerID == o.getID()){
				testedObjs.add(max);
				max = o;
			}
			else {
				testedObjs.add(o);
			}
		}
		
		while (subRounds.size() > 1){
			maxRound = 0 ; 
			for (int i = 0; i < subRounds.size() / 2; i++){
				maxRound = Math.max(maxRound, subRounds.remove(0));
			}
			roundSum += maxRound;
		}
		
		if (subRounds.size() > 0){
			roundSum += subRounds.remove(0);
		}
		
		rounds.add(roundSum);

		// add back all the tested objects including the reference
		objSet.addAll(testedObjs);
		objSet.add(max);
		System.out.println("PickReference " + TotalQuest);
		
		return max;
	}
	
	// sample objects (put back after each sample)
	public Object RandomPickReferencePutBack(ArrayList<Object> objSet, int sampleNum, ArrayList<Integer> rounds){
		Random random = new Random();
		int s;
		int winnerID;
		Object max, o;
		int maxRound = 0;
		int roundSum = 0;
		ArrayList<Integer> subRounds = new ArrayList<Integer>();

		if (objSet.size() == 0){
			System.err.println("Sampling from emptyset !!");
			System.exit(1);
		}
		
		s = random.nextInt(objSet.size());
		max = objSet.remove(s);
		
		for (int i = 1; i < sampleNum; i++){
			s = random.nextInt(objSet.size());
			o = objSet.get(s);
			winnerID = max.Compare(o, loose_threshold);
			//winnerID = max.Compare(o, max_threshold);
			winnerID = Object.meanCompare(max, o);
			TotalQuest += max.getCompareQuestIncrement(o);
			//maxQuest = Math.max(max.getCompareQuestIncrement(o), maxQuest);
			subRounds.add(incrementalLatency(max.getCompareQuestIncrement(o)));
			
			if (winnerID == o.getID() || max.getID() == o.getID()){
				max = o;
			}
		}
		
		while (subRounds.size() > 1){
			maxRound = 0 ; 
			for (int i = 0; i < subRounds.size() / 2; i++){
				maxRound = Math.max(maxRound, subRounds.remove(0));
			}
			roundSum += maxRound;
		}
		
		if (subRounds.size() > 0){
			roundSum += subRounds.remove(0);
		}
		
		rounds.add(roundSum);

		System.out.println("PickReference " + TotalQuest);
		
		return max;
	}
	
	public Object PickMin(ArrayList<Object> objSet){
		Object o, min;
		int winnerID;
		
		min = objSet.get(0);
		
		for (int i = 1; i < objSet.size(); i++){
			o = objSet.get(i);
			winnerID = min.Compare(o, max_threshold);
			winnerID = Object.meanCompare(min, o);
			TotalQuest += min.getCompareQuestIncrement(o);
			if (winnerID == min.getID()){
				min = o;
			}
		}
		
		return min;
	}

	public Object PickMin(ArrayList<Object> objSet, Object min){
		Object o;
		int winnerID;
		
		for (int i = 0; i < objSet.size(); i++){
			o = objSet.get(i);
			winnerID = min.Compare(o, max_threshold);
			winnerID = Object.meanCompare(min, o);
			TotalQuest += min.getCompareQuestIncrement(o);
			if (winnerID == min.getID()){
				min = o;
			}
		}
		
		return min;
	}
	
	public void PartitionHeuristic(ArrayList<Object> objSet, ArrayList<Object> topKCandidate, Object reference, int k, ArrayList<Integer> rounds){
		ArrayList<Object> confuse_set = new ArrayList<Object>();
		ArrayList<Object> objRemain = new ArrayList<Object>();
		int winnerID;
		int quest_before = TotalQuest;
		int threshold = loose_threshold;
		int num_reference = 1;
		Object min;
		
		objRemain.addAll(objSet);
		objSet.clear();

		// Scan the objRemain and try to find a better reference 
		for (Object o : objRemain){
			winnerID = reference.Compare(o, threshold);
			TotalQuest += reference.getCompareQuestIncrement(o);
			if (winnerID == -1){
				confuse_set.add(o);
			}
			else if (winnerID == o.getID()) {
				topKCandidate.add(o);
				if (topKCandidate.size() == k && num_reference == 1){
					
					rounds.add((int) Math.ceil(threshold / QuestNumPerRound));
					
					// find a object *likes* min'
					ReferenceMeanSort(topKCandidate, reference);
					min = topKCandidate.get(k-1);
					
					// refine min' and find min
					reference = PickMin(topKCandidate, min);
					
					// pop the reference(i.e. min) from top-k candidates 
					topKCandidate.remove(reference);
					confuse_set.add(reference);
					
					num_reference++;
				}
			}
		}
		
		objRemain.removeAll(confuse_set);
		objRemain.removeAll(topKCandidate);
		objSet.addAll(objRemain);
		objRemain.clear();
		
		if ((confuse_set.size() + topKCandidate.size()) <= k){
			topKCandidate.addAll(confuse_set);
			confuse_set.clear();
		}
		else {
		
			objRemain.addAll(confuse_set);
			confuse_set.clear();
			
			// re-scan objRemain to estimate the expected #quest
			for (Object o : objRemain){
//				winnerID = reference.Compare(o, threshold);
				winnerID = reference.Compare(o, max_threshold / 2);
				TotalQuest += reference.getCompareQuestIncrement(o);
			}
	
			// estimate the expected #quest of confuse set with a better top
			int quest;
			for (Object o : objRemain){
				quest = reference.estimateQuest(o);
				if (quest > max_threshold / 10){
					winnerID = reference.Compare(o, max_threshold / 10);
					TotalQuest += reference.getCompareQuestIncrement(o);
				}
				quest = reference.estimateQuest(o);
				o.setExpectedQuest(quest);
			}

			// re-order the confuse set based on estimations by Stein Model
			Collections.sort(objRemain, new ExpectedQuestComparator());
			
			// re-scan the confuse set with maximum pairwise budget
			for (Object o : objRemain){
				if (o.getExpectedQuest() > max_threshold){
					confuse_set.add(o);
					continue;
				}
				winnerID = reference.Compare(o, max_threshold);
				TotalQuest += reference.getCompareQuestIncrement(o);
				if (winnerID == -1){
					confuse_set.add(o);
				}
				else if (winnerID == o.getID()) {
					topKCandidate.add(o);
				}
			}
			
			// construct top k set
			objRemain.removeAll(confuse_set);
			objRemain.removeAll(topKCandidate);
			objSet.addAll(objRemain);
			objRemain.clear();
			
			ReferenceMeanSort(confuse_set, reference);
			
			if ((confuse_set.size() + topKCandidate.size()) <= k){
				topKCandidate.addAll(confuse_set);
				confuse_set.clear();
			}

			objRemain.addAll(confuse_set);
			confuse_set.clear();
		}
		
		// scan, re-scan, re-scan
		rounds.add(1+1+1);

		objRemain.remove(reference);
		objRemain.add(0, reference);		
		
		while (topKCandidate.size() < k && objRemain.size() > 0){
			topKCandidate.add(objRemain.remove(0));
		}

		objSet.addAll(objRemain);			
			
		System.out.println("Scanning Quest: " + (TotalQuest - quest_before));
		Display(TopKCandidate);
	}
	
	public void Partition(ArrayList<Object> objSet, ArrayList<Object> topKCandidate, Object reference, int k, ArrayList<Integer> rounds){
		ArrayList<Object> confuse_set = new ArrayList<Object>();
		ArrayList<Object> objRemain = new ArrayList<Object>();
		int winnerID;
		int quest_before = TotalQuest;
		int threshold = loose_threshold;
		int num_reference = 10;
		Object min;
		
		objRemain.addAll(objSet);
		objSet.clear();
		
		while (threshold <= max_threshold && objRemain.size() > 0){
			for (Object o : objRemain){
				winnerID = reference.Compare(o, threshold);
				TotalQuest += reference.getCompareQuestIncrement(o);
				if (winnerID == -1){
					confuse_set.add(o);
				}
				else if (winnerID == o.getID()) {
					topKCandidate.add(o);
					if (topKCandidate.size() == k && num_reference > 0){
						
						rounds.add((int) Math.ceil(threshold / QuestNumPerRound));
						
						min = topKCandidate.get(k-1);
						
						// refine min' and find min
						reference = PickMin(topKCandidate, min);
						
						// pop the reference(i.e. min) from top-k candidates 
						topKCandidate.remove(reference);
						confuse_set.add(reference);
						
						num_reference--;
						threshold = loose_threshold;
					}
				}
			}
		
			objRemain.removeAll(confuse_set);
			objRemain.removeAll(topKCandidate);
			objSet.addAll(objRemain);
			objRemain.clear();
			
//			if ((confuse_set.size() + topKCandidate.size()) < k){
//				topKCandidate.addAll(confuse_set);
//				confuse_set.clear();
//			}
			
			objRemain.addAll(confuse_set);
			confuse_set.clear();
			
			threshold++;
		}

		rounds.add((int) Math.ceil(threshold / QuestNumPerRound));

		objRemain.remove(reference);
		objRemain.add(0, reference);		
		
//		if (topKCandidate.size() < k && objRemain.size() > k - topKCandidate.size()){
//			FastBubbleSort(objRemain, k - topKCandidate.size(), reference, rounds, loose_threshold);
//		}

		while (topKCandidate.size() < k && objRemain.size() > 0){
			topKCandidate.add(objRemain.remove(0));
		}

		objSet.addAll(objRemain);			
			
		System.out.println("Scanning Quest: " + (TotalQuest - quest_before));
		Display(TopKCandidate);
	}
		
	public void ScanAndAdd(ArrayList<Object> objSet, ArrayList<Object> topKCandidate, Object reference, int k, ArrayList<Integer> rounds){
		ArrayList<Object> confuse_set = new ArrayList<Object>();
		ArrayList<Object> objRemain = new ArrayList<Object>();
		int winnerID;
		int quest_before = TotalQuest;
		int maxQuest = 0;
		
		objRemain.addAll(objSet);
		objSet.clear();
		
		for (Object o : objRemain){
			winnerID = reference.Compare(o, max_threshold);
			TotalQuest += reference.getCompareQuestIncrement(o);
			maxQuest = Math.max(maxQuest, reference.getCompareQuestIncrement(o));
			if (winnerID == -1){
				confuse_set.add(o);
			}
			else if (winnerID == o.getID()) {
				topKCandidate.add(o);
			}
		}
		
		rounds.add((int) Math.ceil(maxQuest / QuestNumPerRound));
		
		objRemain.removeAll(confuse_set);
		objRemain.removeAll(topKCandidate);
		objSet.addAll(objRemain);

		objRemain.clear();
		objRemain.addAll(confuse_set);
		confuse_set.clear();

		objRemain.remove(reference);
		objRemain.add(0, reference);		
		
//		if (topKCandidate.size() < k && objRemain.size() > k - topKCandidate.size()){
//			FastBubbleSort(objRemain, k - topKCandidate.size(), reference, rounds, loose_threshold);
//		}
		
		while (topKCandidate.size() < k && objRemain.size() > 0){
			topKCandidate.add(objRemain.remove(0));
		}

		objSet.addAll(objRemain);			
		
		System.out.println("Scanning Quest: " + (TotalQuest - quest_before));
		Display(TopKCandidate);
	}
	
	
	// each time, sample x items and add the maximum one (donated by r) into R.
	// and we compute the probability of each r in [1, k-1] as p
	// we compute the probability of each r in [1, ck] as q
	// final we get the probability of the median of R in [k, ck] as s
	// this function returns the x, m (=|R|), cost (sampling cost), probability (in sweet spot)
	// parameters: objSet--objects, k--topk, c--sweet spot range, max_cost--maximum acceptable sampling cost.
	public ParameterElement getReferenceParameter(int n, int k, double c, double max_cost, int position){
		// probability of r in [1, k-1]
		double p;
		// probability of r in [1, ck]
		double q;
		// probability of median of R in sweet spot 
		double s, best_s = 0; 
		// the size of R
		int m, best_m;
		// convert ck to integer
		int spot = (int) (c*k);
		// number of candidates per reference
		int x, best_x;
		// total cost
		double cost, best_cost;
				
		int ceil, tail;
		
		best_x = 1;
		best_m = 1;
		best_cost = 0;
		
		// enumerate x
		for (x = 1; x <= Math.min(max_cost, n) ; x++){
			// calculate p and q by Formula 1
			p = 1;
			q = 1;
			for (int i = 0; i < x; i++){
				if (p > 0){
					//p *= n - (k - k/2) + 1 - i;
					p *= n - k + 1 - i;
					p /= n - i;
				}
				if (q > 0){
					q *= n - (spot + 1) + 1 - i;
					q /= n - i;
				}
			}
			p = 1 - p;
			q = 1 - q;
			
			// enumerate m
			for (m = 1; m <= Math.min(max_cost, n); m++){
				// compute cost
				cost = (x - 1) * m + 3.0/8.0 * (m * m - 1); // tight upper-bound
				//cost = (x - 1) * m + 0.5 * (m * (m - 1)); // upper-bound
				
				if (cost > max_cost){
					break;
				}
				
				// calculate s by Formula 2
				s = 1;
				ceil = m / position;
				if (m % position > 0){
					ceil++;
				}
				tail = m - ceil + 1;
				
				// the probability of r in [1, k-1]
				for (int i = ceil; i <= m; i++){
					s -= CombinatoricsUtils.binomialCoefficientDouble(m, i) * Math.pow(p, i) * Math.pow(1 - p, m - i);
				}
				
				// the probability of r in [ck+1, N]
				for (int i = tail; i <= m; i++){
					s -= CombinatoricsUtils.binomialCoefficientDouble(m, i) * Math.pow(1 - q, i) * Math.pow(q, m - i);
				}
				
				if (s > best_s){
					best_s = s;
					best_cost = cost;
					best_x = x;
					best_m = m;
				}
			}
		}
		
		ParameterElement para = new ParameterElement();
		para.setCost((int)best_cost);
		para.setM(best_m);
		para.setProbability(best_s);
		para.setX(best_x);

		return para;
	}

	
	public void RandomPickSamples(ArrayList<Object> samples, ArrayList<Object> objSet, int sample_num){
		Random random = new Random();
//		for (int i = 1; i < sample_num; i++){
//			samples.add(objSet.remove(random.nextInt(objSet.size())));
//		}
//		objSet.addAll(samples);
		for (int i = 1; i < sample_num; i++){
			samples.add(objSet.get(random.nextInt(objSet.size())));
		}
	}
	
	public void Run(){
		KSelection();
	}
	
	public void KSelection(){
		KSelection(ObjInfo);
	}
	
	public void KSelection(ArrayList<Object> objSet){
			KSelection(objSet, MaxTopKRemain, Sweetspot, RangeConfidence, 6, 5);
	}
		
	// Yan test parameter
	public void KSelection(ArrayList<Object> objSet, int i){
		KSelection(objSet, MaxTopKRemain, i, RangeConfidence, 6, 5);
	}	
	// end Yan
	
	// test m and x combinations using KSelectionTest 
	public void KSelection(int x, int m){
		KSelection(ObjInfo, x, m);
	}
	
	public void KSelection(ArrayList<Object> objSet, int x, int m){
		KSelection(objSet, MaxTopKRemain, Sweetspot, RangeConfidence, x, m);
	}
	

	public void KSelection(ArrayList<Object> objSet, int maxTopKRemain, double length, double rangeConfidence, int sampleNum, int candidateNum){
		ArrayList<Object> referenceSet = new ArrayList<Object>();
		ArrayList<Object> topKObj = new ArrayList<Object>();
		Object reference;
		// the 1st reference, the reference for sorting
		Object sort_reference = new Object();
		boolean first_round = true;;
		int rangeCandidateNum;
		int rangeSampleNum;
		int median;
		ArrayList<Integer> rounds = new ArrayList<Integer>();
		ParameterElement parameter;

		loose_threshold = Math.max(Object.minSampleNumCI,loose_threshold);
		TopK_remain = TopK;
		
		while (TopKCandidate.size() < TopK_remain && TopK_remain > maxTopKRemain){
			// initialize reference
			reference = objSet.get(0);
			switch (strategy){
			case MedianMax:
				//rangeCandidateNum = getReferenceNum(objSet, TopK_remain, length, rangeConfidence);
				//if (first_round) rangeCandidateNum = candidateNum;
				parameter = getReferenceParameter(objSet.size(), TopK_remain, Sweetspot, objSet.size(), 2);
				rangeCandidateNum = parameter.getM();
				for (int i = 0; i < rangeCandidateNum; i++){
					//rangeSampleNum = getRangeSampleNum(objSet.size(), TopK_remain, length);
					//if (first_round) rangeSampleNum = sampleNum;
					rangeSampleNum = parameter.getX();
					reference = RandomPickReference(objSet, rangeSampleNum, rounds);
					//reference = RandomPickReferencePutBack(objSet, rangeSampleNum, rounds);
					referenceSet.add(reference);
				}

				LatencyRound += Collections.max(rounds);
				rounds.clear();
				
				// pop the median reference
				median = (int) (referenceSet.size() / 2);
				if (referenceSet.size() % 2 == 0){
					median--;
				}
				//FastBubbleSort(referenceSet, median, rounds, max_threshold);
				FastBubbleSort(referenceSet, median, rounds, loose_threshold * 10);
				LatencyRound += rounds.stream().mapToInt(Integer::intValue).sum();
				rounds.clear();
				reference = referenceSet.get(median);
				referenceSet.remove(reference);
				referenceSet.clear();
				
				break;
			case KN:
				proportion = ((double) TopK_remain) / objSet.size();
				// number of samples
				//int set_size = (int) Math.floor(Math.sqrt(objSet.size()));
				int set_size = (int) (objSet.size() / Math.log(objSet.size()));
				RandomPickSamples(referenceSet, objSet, set_size);

				// pop the k/N reference
				int reference_index = (int) Math.floor(proportion * referenceSet.size());
				
				//FastBubbleSort(referenceSet, median, rounds, max_threshold);
				FastBubbleSort(referenceSet, reference_index, rounds, loose_threshold * 10);
				LatencyRound += rounds.stream().mapToInt(Integer::intValue).sum();
				rounds.clear();
				reference = referenceSet.get(reference_index);
				//referenceSet.remove(reference);
				referenceSet.clear();
				break;
			}
						
			// (Partition) Scan all the objects using the reference object
			System.out.println("Reference Position: " + reference.getPosition());
			
			if (heuristic ){
				//ScanAndAddHeuristic(objSet, TopKCandidate, reference, TopK_remain, rounds);
				PartitionHeuristic(objSet, TopKCandidate, reference, TopK_remain, rounds);
				LatencyRound += rounds.stream().mapToInt(Integer::intValue).sum();
				rounds.clear();
			}
			else {
				Partition(objSet, TopKCandidate, reference, TopK_remain, rounds);
				//ScanAndAdd(objSet, TopKCandidate, reference, TopK_remain, rounds);
				LatencyRound += rounds.stream().mapToInt(Integer::intValue).sum();
				rounds.clear();
			}
			
			// update remaining top-k size
			if (TopKCandidate.size() <= TopK_remain){
				topKObj.addAll(TopKCandidate);
				TopK_remain -= TopKCandidate.size();
				TopKCandidate.clear();
			}
			

			// clean old references
			referenceSet.clear();
			
			if (first_round){
				sort_reference = reference;
				first_round = false;
				//System.out.println("Reference position " + sort_reference.getPosition());
			}
			
			if (TopKCandidate.size() + topKObj.size() > 2 * TopK){
				objSet.clear();
				objSet.addAll(TopKCandidate);
				TopKCandidate.clear();
			}
		}
		
		if (TopK_remain <= maxTopKRemain && TopK_remain > 0){
			TopKCandidate.addAll(objSet);
			objSet.clear();
		}
		
		System.out.println("Build Large Candidate Set, Quest: " + TotalQuest);
		
		if (TopKCandidate.size() > TopK_remain){
			if (TopK_remain > maxTopKRemain){
				// run Tournament
				//ArrayList<Object> remainTopKObj = new ArrayList<Object>();
				//TournamentTree(TopKCandidate, remainTopKObj, TopK_remain);
				//TopKCandidate = remainTopKObj;
				
				// run BubbleSort
				ReferenceSort(TopKCandidate, TopK_remain, sort_reference, rounds, max_threshold);
				LatencyRound += rounds.stream().mapToInt(Integer::intValue).sum();
				rounds.clear();
				ArrayList<Object> remainTopKObj = new ArrayList<Object>();
				remainTopKObj.addAll(TopKCandidate.subList(0, TopK_remain));
				TopKCandidate = remainTopKObj;
			}
			else {

				if (TopK > maxTopKRemain){
					// run BubbleSort
					ReferenceSort(TopKCandidate, TopK_remain, sort_reference, rounds, max_threshold);
					LatencyRound += rounds.stream().mapToInt(Integer::intValue).sum();
					rounds.clear();
					ArrayList<Object> remainTopKObj = new ArrayList<Object>();
					remainTopKObj.addAll(TopKCandidate.subList(0, TopK_remain));
					TopKCandidate = remainTopKObj;
				}
				else {
					// run MinHeap
					ArrayList<Object> heap = new ArrayList<Object>();
					GenerateHeap(heap, TopKCandidate, TopK_remain);
					HeapScanSmart(heap, TopKCandidate, loose_threshold);
					HeapSort(heap);
					TopKCandidate = heap;
				}
			}
		}
		else {
			if (TopK > maxTopKRemain){
				//TopList(TopKCandidate);
				ReferenceSort(TopKCandidate, TopK_remain, sort_reference, rounds, max_threshold);
				LatencyRound += rounds.stream().mapToInt(Integer::intValue).sum();
				rounds.clear();
			}
			else {
				// run MinHeap
				ArrayList<Object> heap = new ArrayList<Object>();
				GenerateHeap(heap, TopKCandidate, TopK_remain);
				HeapScanSmart(heap, TopKCandidate, loose_threshold);
				HeapSort(heap);
				TopKCandidate = heap;
			}
		}

		ReferenceSort(topKObj, topKObj.size(), sort_reference, rounds, max_threshold);
		LatencyRound += rounds.stream().mapToInt(Integer::intValue).sum();
		rounds.clear();
		TopKCandidate.addAll(0, topKObj);
		topKObj.clear();
		Display(TopKCandidate);
		System.out.println("Total Quest: " + TotalQuest);
		System.out.println("Total Round: " + LatencyRound);
	}

	public void HeapSort(ArrayList<Object> heap){
		ArrayList<Object> topKList = new ArrayList<Object>(); 
		Object o;
		while (heap.size() > 0){
			if (heap.size() == 1){
				o = heap.remove(0);				
				topKList.add(0, o);
			}
			else {
				o = Pop(heap);
				topKList.add(0, o);
			}
		}
		heap.addAll(topKList);
	}
	
	public void ReferenceSort(ArrayList<Object> list, int k, Object reference, ArrayList<Integer> rounds, int threshold){
		if(list.size()>1){
			FastBubbleSort(list, k, reference, rounds, threshold);
		}
	}



	
	// Yan test ErrorRateSet
	public void TestLoop(int times) throws IOException, IOException{
		double aveQuest = 0;
		double aveHeapCost = 0;
		double avePrecision = 0;
		double aveNDCG = 0;
		
		Object.dataset = Object.Dataset.SYNTHETIC_DATA;
		Object.model = Object.Model.CI_MODEL;
		
		for(int i=0;i<times;i++){
			System.out.println("-------------------------------------------");
			System.out.println("Case " + i);
			ReadItemInfo();
			Collections.shuffle(ObjInfo);
			LatencyRound = 0;
			KSelection(ObjInfo);
			aveQuest += TotalQuest;
			avePrecision += getPrecision(TopKCandidate);
			aveNDCG += getNDCG(TopKCandidate);
			System.out.println("Count of sample : " + Object.CountOfSample);
			clear();
		}
		aveQuest /= times;
		aveHeapCost /= times;
		avePrecision /= times;
		aveNDCG /= times;
		System.out.println("Ave Total Quest : "+aveQuest);
		System.out.println("Ave Heap Quest : "+aveHeapCost);
		System.out.format("Ave Error Rate in TopK Set : %.2f%n", avePrecision);
		System.out.format("Ave Error Rate in TopK Order : %.2f%n", aveNDCG);
	}
	// end Yan 

	public static void main(String[] args) throws IOException, Exception {
		// TODO Auto-generated method stub
		SPR test = new SPR();
		test.setLoose_threshold(100);
		Object.minSampleNumCI = 30;
		test.setMax_threshold(1000);
		test.TestLoop(10);
	}

}
