package pairwiseCrowdsourcing;

import java.io.*;
import java.util.*;

import pairwiseCrowdsourcing.Object;

public class Infimum extends MinHeapTie {

	public void InfimumScan(){
		InfimumScan(ObjInfo, TopKCandidate, TopK);
	}
	public void InfimumScan(ArrayList<Object> objSet, ArrayList<Object> topKObj, int k){
		int maxQuest = 0;
		
		Collections.sort(objSet, new PositionComparator());
		
		Display(objSet);
		
		Object a = null, b;
		b = objSet.get(0);
		for(int i = 1; i < objSet.size(); i++){
			if (i <= k) {a = b;}
			b = objSet.get(i);
			a.Compare(b, max_threshold);
			TotalQuest += a.getCompareQuestIncrement(b);
			if (i <= k){
				LatencyRound += incrementalLatency(a.getCompareQuestIncrement(b)); 
			}
			else {
				maxQuest = Math.max(maxQuest, a.getCompareQuestIncrement(b));
			}
		}
		
		LatencyRound += incrementalLatency(maxQuest); 
		topKObj.addAll(objSet.subList(0, k));
	}
	public void TestLoop(int round) throws IOException{
		int aveQuest = 0;
		int times =round;
		//Random random = new Random(20310);
		for(int i=0;i<times;i++){
			
			ReadItemInfo();
			Collections.shuffle(ObjInfo);
			LatencyRound = 0;
			InfimumScan(ObjInfo, TopKCandidate, TopK);
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
		Infimum test = new Infimum();
		test.TestLoop(1);
	}

}
