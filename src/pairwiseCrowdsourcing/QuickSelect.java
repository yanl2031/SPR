package pairwiseCrowdsourcing;

import java.io.*;
import java.util.*;

import pairwiseCrowdsourcing.Object;


// written by ..., date, email, web-link
// acknowledgement
// modified by ..., date, email


public class QuickSelect extends Tournament{
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
	
	public void Run(){
		KSelection();
	}
	
	public Object KSelection(){
		return KSelection(ObjInfo, TopKCandidate, TopK);
	}

	public Object KSelection(ArrayList<Object> objSet, ArrayList<Object> topKObj, int k) {
		Object obj = KSelection(objSet, 0, objSet.size() - 1, k - 1);
		Display(objSet);
		topKObj.addAll(objSet.subList(0, k));
		TopKSorting(topKObj);
		System.out.println("Quest "+TotalQuest);
		System.out.println("Count of sample : " + Object.CountOfSample);
		return obj;
	}
	
	private Object KSelection(ArrayList<Object> objSet, int first, int last, int k) {
		if (first <= last) {
			int pivot = Partition(objSet, first, last);
			System.out.println(objSet.get(pivot).getPosition());
			System.out.println("Count of sample : " + Object.CountOfSample);
			System.out.println("Quest "+TotalQuest);
			if (pivot == k) {
				return objSet.get(k);
			}
			if (pivot > k) {
				return KSelection(objSet, first, pivot - 1, k);
			}
			return KSelection(objSet, pivot + 1, last, k);
		}
		//return Integer.MIN_VALUE;
		return null;
	}
		 
	private int Partition(ArrayList<Object> objSet, int first, int last) {
		int pivot = first + new Random().nextInt(last - first + 1);
		int winnerID;
		int maxRound = 0;
		
		Swap(objSet, last, pivot);
		for (int i = first; i < last; i++) {
			winnerID = objSet.get(i).Compare(objSet.get(last), max_threshold);
			//winnerID = Object.meanCompare(objSet.get(i), objSet.get(last));
			TotalQuest += objSet.get(i).getCompareQuestIncrement(objSet.get(last));
			maxRound = Math.max(maxRound, incrementalLatency(objSet.get(i).getCompareQuestIncrement(objSet.get(last))));
			if(winnerID == objSet.get(i).getID()){
				Swap(objSet, i, first);
				first++;
			}
		}
		
		LatencyRound += maxRound;
		Swap(objSet, first, last);
		return first;
	}
	 
	public void TopKSorting(ArrayList<Object> list){
		if(list.size()>1){
			//Collections.sort(list, new CustomComparator());
			ArrayList<Object> remainTopKObj = new ArrayList<Object>();
			TournamentTree(list, remainTopKObj, list.size());
			list.clear();
			list.addAll(remainTopKObj);
		}
	}
	
	
	public void TestLoop(int round) throws IOException, IOException{
		int aveQuest = 0;
		int times =round;
		Object kobj;
		//Random random = new Random(20310);
		for(int i=0;i<times;i++){
			
			ReadItemInfo();
			Collections.shuffle(ObjInfo);
			kobj = KSelection(ObjInfo, TopKCandidate, TopK);
			System.out.println("K-th Object :" + kobj.getPosition());
			aveQuest += TotalQuest;
			System.out.println("Count of sample : " + Object.CountOfSample);
			Display(TopKCandidate);
			TotalQuest = 0;
			Object.CountOfSample = 0;
			ObjInfo.clear();
		}
		aveQuest /= times;
		System.out.println("Ave Total Quest : "+aveQuest);
	}
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		QuickSelect quickSelect = new QuickSelect();
		quickSelect.TestLoop(1);
	}

}
