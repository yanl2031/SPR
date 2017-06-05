package pairwiseCrowdsourcing;

import java.io.*;
import java.util.*;

import pairwiseCrowdsourcing.Object;

/*
 * test sorting algorithms:
 *  tournament tree
 *  minheap
 *  quicksort 
 *  of TopK object
 * */

//class Quicksort extends Tournament{
//	public void QuickSort(ArrayList<Object> ObjSet, int first, int last){
//		int lower = first+1, upper = last;
//		Swap(ObjSet, first, (first+last)/2);
//		Object bound = ObjSet.get(first);
//		int winnerID = 0;//?
//		while(lower <= upper){
//			while(lower < last && ObjSet.get(lower).getScore() < bound.getScore()){
//				lower++;
//				
//			}
//			
//			while(bound.getScore()<ObjSet.get(upper).getScore()){
//				upper--;
//				
//			}
//			if(lower < upper){
//				Swap(ObjSet, lower++, upper--);
//			}else{
//				lower++;
//			}
//		}
//		Swap(ObjSet, upper, first);
//		if(first < upper-1){
//			QuickSort(ObjSet, first, upper-1);
//		}
//		if(upper+1 <last){
//			QuickSort(ObjSet, upper+1, last);
//		}
//	}
//	public void QuickSort(ArrayList<Object> ObjSet){
//		if(ObjSet.size()<2){
//			return ;
//		}
//		QuickSort(ObjSet, 0, ObjSet.size()-1);
//	}
//	public void QuickSort(){
//		QuickSort(ObjInfo);
//		TopKCandidate.addAll(ObjInfo);
//		Display(TopKCandidate);
//	}
//}


class Quicksort extends Tournament{
	public void QuickSort(ArrayList<Object> ObjSet, int first, int last){
		int lower = first+1, upper = last;
		Swap(ObjSet, first, (first+last)/2);
		Object bound = ObjSet.get(first);
		int winnerID = 0;//?
		while(lower <= upper){
			winnerID = ObjSet.get(lower).Compare(bound, max_threshold);
			TotalQuest += ObjSet.get(lower).getCompareQuestIncrement(bound);
			while(lower < last && winnerID == ObjSet.get(lower).getID()){
				lower++;
				winnerID = ObjSet.get(lower).Compare(bound, max_threshold);
				TotalQuest += ObjSet.get(lower).getCompareQuestIncrement(bound);
				//System.out.println("Quicksort~~: "+TotalQuest);
			}
			winnerID = ObjSet.get(upper).Compare(bound, max_threshold);
			TotalQuest += ObjSet.get(upper).getCompareQuestIncrement(bound);
			while((upper > first+1) && winnerID == bound.getID()){
				upper--;
				winnerID = ObjSet.get(upper).Compare(bound, max_threshold);
				TotalQuest += ObjSet.get(upper).getCompareQuestIncrement(bound);
				//System.out.println("Quicksort~~: "+TotalQuest);
			}
			if(lower < upper){
				Swap(ObjSet, lower++, upper--);
			}else{
				lower++;
			}
		}
		Swap(ObjSet, upper, first);
		if(first < upper-1){
			QuickSort(ObjSet, first, upper-1);
		}
		if(upper+1 <last){
			QuickSort(ObjSet, upper+1, last);
		}
	}
	public void QuickSort(ArrayList<Object> ObjSet){
		if(ObjSet.size()<2){
			return ;
		}
		QuickSort(ObjSet, 0, ObjSet.size()-1);
	}
	public void QuickSort(){
		QuickSort(ObjInfo);
		TopKCandidate.addAll(ObjInfo);
		System.out.println("!!!!!!!!!!!!!!!!!");
		Display(TopKCandidate);
	}
}

class BubblesortTest extends Tournament{
	
	public void BubbleSort(){
		BubbleSort(ObjInfo, TopK);
	}
	
	public void BubbleSort(ArrayList<Object> objSet, int k){
		int winnerID;
		Object a, b, reference;
		
		reference = objSet.get(0);
		
		k = Math.min(k, objSet.size());
		
		for (int i = 1; i < objSet.size(); i++){
			b = objSet.get(i);
			reference.Compare(b, loose_threshold);
			TotalQuest += reference.getCompareQuestIncrement(b);
		}
		
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
		
		
		for (int i = 0; i < k; i++){
			for (int j = objSet.size() - i - 1; j > 0; j--){
				a = objSet.get(j - 1);
				b = objSet.get(j);
				winnerID = a.Compare(b, max_threshold);
				TotalQuest += a.getCompareQuestIncrement(b);
				if (winnerID == b.getID()){
					Swap(objSet, j, j - 1);
				}
			}
		}
		
		Display(objSet);
	}
}



public class Sorting extends Tournament {
	int KSize = 40;
	public void TestSorting(int loop) throws IOException, IOException{
		int questTournament;
		int questMinheap;
		int questQuicksort;
		int questBubblesort;
		Random random = new Random(20310);
		
		Tournament tournament = new Tournament();
		MinHeapTie minheap = new MinHeapTie();
		Quicksort quicksort = new Quicksort();
		BubblesortTest bubblesortTest = new BubblesortTest();
		
		ReadItemInfo();
		// construct testing dataset
		Collections.sort(ObjInfo, new PositionComparator());
		ArrayList<Object> dataset = new ArrayList<Object>(ObjInfo.subList(0, KSize));
		//Collections.sort(dataset, new PositionComparator().reversed());
		ObjInfo.removeAll(dataset);
		ObjInfo.clear();
		setObjInfo(dataset);
		Display(dataset);
		
		
		// initialization
		questTournament = 0;
		questMinheap = 0;
		questQuicksort = 0;
		questBubblesort = 0;
		
		// set the parameters
		tournament.setTopK(KSize);
		minheap.setTopK(KSize);
		quicksort.setTopK(KSize);
		bubblesortTest.setTopK(KSize);
		
		//set confidence
		Object.confidence = Object.confidence;
		
		//set max_threshold
		//perfect.setMax_threshold(max_threshold);
	
		tournament.setMax_threshold(max_threshold);
		minheap.setMax_threshold(max_threshold);
		quicksort.setMax_threshold(max_threshold);
		bubblesortTest.setMax_threshold(max_threshold);
		
		// output redirection
//		PrintStream console = System.out;
//		File file = new File("sorting.txt");
//		FileOutputStream fos = new FileOutputStream(file);
//		PrintStream ps = new PrintStream(fos);
//		System.setOut(ps);

		
		// run loop
		for (int i = 0; i < loop; i++){
			// shuffle the item set
			Collections.shuffle(dataset, random);
			Display(dataset);
			
			
			// reset statistics
			tournament.clear();
			minheap.clear();
			quicksort.clear();
			bubblesortTest.clear();

			// reload item set
			tournament.setObjInfo(dataset);
			minheap.setObjInfo(dataset);
			quicksort.setObjInfo(dataset);
			bubblesortTest.setObjInfo(dataset);
			
			// execute ranking algorithm
			tournament.TournamentTree();
			minheap.MinHeap();
			quicksort.QuickSort();
			bubblesortTest.BubbleSort();

			// update total quest of each algorithm
			questTournament += tournament.getTotalQuest();
			questMinheap += minheap.getTotalQuest();
			questQuicksort += quicksort.getTotalQuest();
			questBubblesort += bubblesortTest.getTotalQuest();
		}
		
		// put all the dataset back to pool
		ObjInfo.addAll(dataset);
		dataset.clear();

		// compute average quests
		questTournament /= loop;
		questMinheap /= loop;
		questQuicksort /= loop;
		questBubblesort /= loop;
		
		// reset output to console
//		System.setOut(console);
		System.out.println("Tournamnet: "+questTournament);
		System.out.println("Minheap: "+questMinheap);
		System.out.println("Quicksort: "+questQuicksort);
		System.out.println("Bubblesort: "+questBubblesort);

	}
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Sorting test = new Sorting();
		test.TestSorting(10);
	}

}
