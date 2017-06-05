package pairwiseCrowdsourcing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import org.apache.commons.math3.util.Pair;

public class Elo extends TopkAlgorithm {
	public void SortByExternalValue(ArrayList<Object> objSet, ArrayList<Double> score){
		ArrayList<Pair<Object, Double>> pairs = new ArrayList<Pair<Object, Double>>();
		for (int i = 0; i < score.size(); i++){
			pairs.add(new Pair<Object, Double>(objSet.remove(0), score.get(i)));
		}
		
		Collections.sort(pairs, new Comparator<Pair<Object, Double>>() {
		    @Override
		    public int compare(final Pair<Object, Double> o1, final Pair<Object, Double> o2) {
		        // TODO: implement your logic here
	            if (o1.getValue() > o2.getValue()) {
	                return -1;
	            } else if (o1.getValue().equals(o2.getValue())) {
	                return 0; // You can change this to make it then look at the
	                          //words alphabetical order
	            } else {
	                return 1;
	            }
		    }
		});
		
		for (int i = 0; i < score.size(); i++){
			score.set(i, pairs.get(i).getValue());
			objSet.add(pairs.get(i).getKey());
		}
	}
	
	public void Run(){
		if (sampleSize > 0){
			EloMethod(ObjInfo, sampleSize, 3);
		}
		else {
			System.err.println("Elo algorithm: please set sample size by setSampleSize(int) before call Run()");
			System.exit(1);
		}
	}
	
	public void EloMethod(ArrayList<Object> objSet, int sampleSize, int minSample){
		ArrayList<Double> score = new ArrayList<Double>();
		Object a, b;
		int aId, bId, aIndex, bIndex, winnerId;
		Random random = new Random();
		
		int minSampleBackup = Object.minSampleNumCI;
		
		Object.minSampleNumCI = minSample;
		loose_threshold = Object.minSampleNumCI;
		
		for (int i = 0; i < objSet.size(); i++){
			score.add(500.0);
		}
		
		Collections.sort(objSet, Collections.reverseOrder(new Comparator<Object>() {
		    @Override
		    public int compare(final Object o1, final Object o2) {
		        // TODO: implement your logic here
	            if (o1.getPosition() > o2.getPosition()) {
	                return -1;
	            } else if (o1.getPosition() == o2.getPosition()) {
	                return 0; // You can change this to make it then look at the
	                          //words alphabetical order
	            } else {
	                return 1;
	            }
		    }
		}));
		
		
		// Elo algorithm
		for (int i = 0; i < sampleSize; i++){
			aIndex = random.nextInt(objSet.size());
			bIndex = random.nextInt(objSet.size());
			while (aIndex == bIndex){
				bIndex = random.nextInt(objSet.size());				
			}
			a = objSet.get(aIndex);
			b = objSet.get(bIndex);
			aId = a.getID();
			bId = b.getID();
			
			winnerId = a.Compare(b, loose_threshold);
			winnerId = Object.meanCompare(a, b);
			TotalQuest += a.getCompareQuestIncrement(b);
			a.getRecord(b).clear();
			a.getChildren().clear();
			b.getChildren().clear();
			
			
			// update elo score
			double deltaScoreA, deltaScoreB;
			if (winnerId == aId){
				deltaScoreA = 32 * (1 - 1 / (1 + Math.pow(10, (score.get(bIndex) - score.get(aIndex)) / 400.0)));
				deltaScoreB = 32 * (0 - 1 / (1 + Math.pow(10, (score.get(aIndex) - score.get(bIndex)) / 400.0)));
		        score.set(aIndex, score.get(aIndex) + deltaScoreA);
		        score.set(bIndex, score.get(bIndex) + deltaScoreB);
		        if (bIndex == 0){
		        	System.out.format("A:%d, B:%d%n", aIndex, bIndex);
		        }
			}
			else if (winnerId == bId){
				deltaScoreA = 32 * (0 - 1 / (1 + Math.pow(10, (score.get(bIndex) - score.get(aIndex)) / 400.0)));
				deltaScoreB = 32 * (1 - 1 / (1 + Math.pow(10, (score.get(aIndex) - score.get(bIndex)) / 400.0)));
		        score.set(aIndex, score.get(aIndex) + deltaScoreA);
		        score.set(bIndex, score.get(bIndex) + deltaScoreB);
		        if (aIndex == 0){
		        	System.out.format("A:%d, B:%d%n", aIndex, bIndex);
		        }
			}
		}
		
		SortByExternalValue(objSet, score);
		TopKCandidate.addAll(objSet.subList(0, TopK));
		System.out.format("Precision: %f%n", getPrecision());
		System.out.format("NDCG: %f%n", getNDCG());
		System.out.format("TMC: %d%n", TotalQuest);
		
		Object.minSampleNumCI = minSampleBackup;
	}
}
