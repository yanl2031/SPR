package pairwiseCrowdsourcing;


import java.io.*;
import java.util.*;

import pairwiseCrowdsourcing.Object;

public class PerfectHeap extends MinHeapTie{
	
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
	
	public void GenerateHeap(ArrayList<Object> heap, ArrayList<Object> objSet, int heapSize){
		// use top-k objects with descending order to initialize heap
		
		// ascending order
		Display(objSet);
		Collections.sort(objSet, new PositionComparator());
		Display(objSet);
		
		for(int i = 0; i < heapSize; i++){
			// descending order
			heap.add(0, objSet.remove(0));
		}
		
		Display(heap);
		
		Heapify(heap);
	}
	
	public void HeapScan(ArrayList<Object> heap, ArrayList<Object> objSet, int threshold){
		ArrayList<Object> confuse_set = new ArrayList<Object>();
		Object top = heap.get(0);
		int winner;
		
		while (threshold <= max_threshold && objSet.size() > 0){
			for(Object o : objSet){
				winner = top.Compare(o, threshold);
				TotalQuest += top.getCompareQuestIncrement(o);
				
				if (winner == o.getID()){
					//HeapReplace(heap, o);
					top = heap.get(0);
				}
				else if (winner == -1){
					confuse_set.add(o);
				}
			}
			// re-scan confuse set with higher threshold
			threshold++;
			objSet.clear();
			objSet.addAll(confuse_set);
			confuse_set.clear();
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		PerfectHeap test = new PerfectHeap();
		//test.ReadItemInfo();
		//test.SimpleTopK_1();
		test.TestLoop(100);
	}

}
