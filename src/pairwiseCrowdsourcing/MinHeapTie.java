package pairwiseCrowdsourcing;

import java.io.*;
import java.util.*;

import pairwiseCrowdsourcing.Object;




public class MinHeapTie extends TopkAlgorithm {
	
	public void ReadItemInfo() throws FileNotFoundException, IOException{
			
		//to compare with simple topk algorithm, these two algorithms should use the same input PositioinScoreID.txt
		// format: Position| Score| ID
		Scanner  cin = new Scanner(new File("D:/WorkSpace/JAVAProgram/CrowdSourcing_1.umac.mo/src/BaselineMinheapTie/Input/RandomIDScorePositioin.txt"));
		//Scanner  cin = new Scanner(new File("D:/WorkSpace/JAVAProgram/CrowdSourcing_1.umac.mo/src/PerfectMinHeap/50Objects/Random50/Random50IDScorePos.txt"));
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
	}
	
	public void Run(){
		MinHeap();
	}
	
	public void MinHeap(){
		// set loose_threshold equal to max_threshold
		loose_threshold = max_threshold;
		
		//top k candidate
		GenerateHeap(TopKCandidate, ObjInfo, TopK);
		HeapScan(TopKCandidate, ObjInfo, loose_threshold);
//		HeapScanSmart(TopKCandidate, ObjInfo, loose_threshold);
		System.out.println("************");
		Display(TopKCandidate);
		System.out.println("************");
		TopList(TopKCandidate);
		System.out.println("Count Sample : " + Object.CountOfSample);
		System.out.println("Total Quest : " + TotalQuest);
	}

	public void Heapify(ArrayList<Object> heap){
		ArrayList<Integer> rounds = new ArrayList<Integer>();
		int maxRound = 0;
		
		for(int i = heap.size()/2 - 1; i >= 0; --i){
			MoveDown(heap, i, heap.size() - 1, rounds);
			maxRound = Math.max(rounds.remove(0), maxRound);
		}
		
		LatencyRound += (int) ((Math.ceil(Math.log(heap.size()) / Math.log(2)) - 1) * maxRound); 
	}
	
	public void HeapReplace(ArrayList<Object> heap, Object o){
		ArrayList<Integer> rounds = new ArrayList<Integer>();
		heap.remove(0);
		heap.add(0, o);
		MoveDown(heap, 0, heap.size() -1, rounds);
		LatencyRound += rounds.get(0);
	}
	
	public void GenerateHeap(ArrayList<Object> heap, ArrayList<Object> objSet, int heapSize){
		//each time randomly generate ten candidates
		Random rand = new Random();
		int temp;
		for(int i = 0; i < heapSize; i++){
			temp = rand.nextInt(objSet.size());
			heap.add(objSet.remove(temp));
		}
		
		Heapify(heap);
	}
	
	public void HeapScan(ArrayList<Object> heap, ArrayList<Object> objSet, int threshold){
		ArrayList<Object> confuse_set = new ArrayList<Object>();
		Object top = heap.get(0);
		int winner;
		
		loose_threshold = Math.max(Object.minSampleNumCI,loose_threshold);
		
		while (threshold <= max_threshold && objSet.size() > 0){
			for(Object o : objSet){
				winner = top.Compare(o, threshold);
				TotalQuest += top.getCompareQuestIncrement(o);
				LatencyRound += incrementalLatency(top.getCompareQuestIncrement(o));
				
				if (winner == o.getID()){
					HeapReplace(heap, o);
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
	
	public void HeapScanSmart(ArrayList<Object> heap, ArrayList<Object> objSet, int threshold){
		ArrayList<Object> confuse_set = new ArrayList<Object>();
		Object top = heap.get(0);
		int winner;
		
		loose_threshold = Math.max(Object.minSampleNumCI,loose_threshold);
		
		if (threshold <= max_threshold && objSet.size() > 0){
			for (Object o : objSet){
				winner = top.Compare(o, threshold);
				TotalQuest += top.getCompareQuestIncrement(o);
				LatencyRound += incrementalLatency(top.getCompareQuestIncrement(o));
				
				if (winner == o.getID()){
					HeapReplace(heap, o);
					top = heap.get(0);
				}
				else if (winner == -1){
					confuse_set.add(o);
				}
			}
			
			// re-scan the confuse set with a better top
			objSet.clear();
			objSet.addAll(confuse_set);
			confuse_set.clear();

			for (Object o : objSet){
				winner = top.Compare(o, threshold);
				TotalQuest += top.getCompareQuestIncrement(o);
				LatencyRound += incrementalLatency(top.getCompareQuestIncrement(o));
			}

			// estimate the expected #quest of confuse set with a better top
			int quest;
			for (Object o : objSet){
				quest = top.estimateQuest(o);
				o.setExpectedQuest(quest);
			}

			// re-order the confuse set based on estimations by Stein Model
			Collections.sort(objSet, new ExpectedQuestComparator());
			
			// re-scan the confuse set with maximum pairwise budget
			for (Object o : objSet){
				winner = top.Compare(o, max_threshold);
				TotalQuest += top.getCompareQuestIncrement(o);
				LatencyRound += incrementalLatency(top.getCompareQuestIncrement(o));
				
				if (winner == o.getID()){
					HeapReplace(heap, o);
					top = heap.get(0);
				}
				else if (winner == -1){
					confuse_set.add(o);
				}
			}
		}
	}
	
//	public void MoveDown_test(ArrayList<Object> Top,int first, int last){
//		int largest = 2*first+1;
//		while(largest<=last){
//			if(largest<last&&Top.get(largest).getScore()>Top.get(largest+1).getScore()){
//				largest++;
//			}
//			if(Top.get(first).getScore()>Top.get(largest).getScore()){
//				Swap(Top, first, largest);
//				first = largest;
//				largest = 2*first+1;
//			}else{
//				largest=last+1;
//			}
//		}
//	}
	
	public void MoveDown(ArrayList<Object> heap, int first, int last, ArrayList<Integer> rounds){
		int largest = 2*first+1;
		int winnerID;
		int roundNum = 0;
		Object left, right, top;
		
		while(largest<=last){
			if(largest<last){
				left = heap.get(largest);
				right = heap.get(largest+1);
				winnerID = left.Compare(right, max_threshold);
				TotalQuest += left.getCompareQuestIncrement(right);
				roundNum += incrementalLatency(left.getCompareQuestIncrement(right));
				if(winnerID == left.getID()){
					largest++;
				}
			}
			
			top = heap.get(first);
			left = heap.get(largest);
			winnerID = left.Compare(top, max_threshold);
			TotalQuest += left.getCompareQuestIncrement(top);
			roundNum += incrementalLatency(left.getCompareQuestIncrement(top));
			
			if(winnerID == top.getID()){
				Swap(heap, first, largest);
				first = largest;
				largest = 2*first+1;
			}
//			else if (winnerID == -1){
//				// otherwise compare them by mu
//				CompRecord record;
//				double mu;
//					
//				record = left.getRecord(top);
//				if (left.getID() > top.getID()){
//					mu = record.getSum() / record.getQuestNum();
//				}
//				else {
//					mu = -1 * record.getSum() / record.getQuestNum();					
//				}
//				
//				if (mu < 0){
//					Swap(heap, first, largest);
//					first = largest;
//					largest = 2*first+1;
//				}
//				else {
//					break;
//				}
//			}
			else {
				break;
			}
		}
		
		rounds.add(roundNum);
	}
	
	public Object Pop(ArrayList<Object> Heap){
		ArrayList<Integer> rounds = new ArrayList<Integer>();
		
		Object popTop = Heap.get(0);
		Heap.remove(0);
		int index = Heap.size()-1;
		
		if (index > 0){ 
			Object top = Heap.get(index);
			Heap.remove(index);
			Heap.add(0, top);
			MoveDown(Heap, 0, Heap.size()-1, rounds);
			LatencyRound += rounds.get(0);
		}
		
		return popTop;
	}
	public void TestLoop(int round) throws IOException{
		int aveQuest =0;
		int times =round;
		Random random = new Random(20310);
		for(int i=0;i<times;i++){
			ReadItemInfo();
			Collections.shuffle(ObjInfo, random);
			LatencyRound = 0;
			MinHeap();
			aveQuest += TotalQuest;
//			Writer writer = null;
//			writer = new BufferedWriter(new OutputStreamWriter(
//			           new FileOutputStream("D:/WorkSpace/JAVAProgram/CrowdSourcing_1.umac.mo/src/BaselineMinheap/Output/"+i+"Heap.txt"), "utf-8"));
//			for(int j=0;j<CompInfo.size();j++){
//				writer.write(CompInfo.get(j).getRound()+" "+
//						CompInfo.get(j).getIda()+" "+CompInfo.get(j).getIdb()+" "+CompInfo.get(j).getComparison()+" "
//						+CompInfo.get(j).getWinner()+"\n");	
//			}
//			writer.close();
//			writer = new BufferedWriter(new OutputStreamWriter(
//			           new FileOutputStream("D:/WorkSpace/JAVAProgram/CrowdSourcing_1.umac.mo/src/BaselineMinheap/Result/"+i+"HeapTopK.txt"), "utf-8"));
//			for(int m=0;m<TopCandidate.size();m++){
//				writer.write(TopCandidate.get(m).getID()+" "+TopCandidate.get(m).getScore()+" "+TopCandidate.get(m).getPosition()+"\n");	
//			}
//			writer.close();
			TotalQuest = 0;
			Object.CountOfSample = 0;
			ObjInfo.clear();
			TopKCandidate.clear();
			clear();
		}
	
		aveQuest /= times;
		System.out.println("Ave Total Quest : "+aveQuest);
	}
	
	public void TopList(ArrayList<Object> Heap){
		Object tmp = new Object();
		ArrayList<Object> topk = new ArrayList<Object>();
		//System.out.println("Tops:");
		int size = Heap.size();
		for(int i=0;i<size-1;i++){
			tmp = Pop(Heap);
			topk.add(0, tmp);
		//	System.out.println(tmp.getID()+" "+tmp.getScore()+" "+tmp.getPosition());
		}
		tmp = Heap.get(0);
		Heap.remove(0);
		topk.add(0, tmp);
		Heap.clear();
		Heap.addAll(topk);
		//System.out.println(tmp.getID()+" "+tmp.getScore()+" "+tmp.getPosition());
	}

	
	public static void main(String[] args) throws IOException, Exception {
		// TODO Auto-generated method stub
		MinHeapTie test = new MinHeapTie();
		//test.ReadItemInfo();
		//test.SimpleTopK_1();
		test.TestLoop(10);
	}

}
