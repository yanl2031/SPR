package pairwiseCrowdsourcing;

import java.util.ArrayList;
import java.util.Hashtable;

public abstract class TopkAlgorithm {
	int max_threshold = 1000;
	int loose_threshold = 30;
	int TopK = 10;
	
	int TotalQuest = 0;
	int LatencyRound = 0;
	int QuestNumPerRound = 30;
	
	int sampleSize = 0;
	
	int ReusedQuest = 0;
	
	ArrayList<Object> ObjInfo = new ArrayList<Object>();
	ArrayList<Object> TopKCandidate = new ArrayList<Object>();
	
	Hashtable<Integer, Object> ObjHash = new Hashtable<Integer, Object>();
	
	public ArrayList<Object> getObjInfo() {
		return ObjInfo;
	}

	public void setObjInfo(ArrayList<Object> objInfo) {
		// clear old data
		ObjHash.clear();
		ObjInfo.clear();
		
		Object o;
		for (int i = 0; i < objInfo.size(); i++){
			o = objInfo.get(i).clone();
			ObjInfo.add(o);
			ObjHash.put(o.getID(), o);
		}
	}

	
	public int getTopK() {
		return TopK;
	}


	public void setTopK(int topK) {
		TopK = topK;
	}


	public int getTotalQuest() {
		return TotalQuest;
	}


	public void setTotalQuest(int totalQuest) {
		TotalQuest = totalQuest;
	}

	public int getLatencyRound() {
		return LatencyRound;
	}


	public void setLatencyRound(int latencyRound) {
		LatencyRound = latencyRound;
	}


	public int getMax_threshold() {
		return max_threshold;
	}


	public void setMax_threshold(int max_threshold) {
		this.max_threshold = max_threshold;
	}


	public int getLoose_threshold() {
		return loose_threshold;
	}


	public void setLoose_threshold(int loose_threshold) {
		this.loose_threshold = loose_threshold;
	}

	public int getQuestNumPerRound() {
		return QuestNumPerRound;
	}

	public void setQuestNumPerRound(int questNumPerRound) {
		QuestNumPerRound = questNumPerRound;
	}

	public void clear(){
		TotalQuest = 0;
		LatencyRound = 0;
		ReusedQuest = 0;
		Object.CountOfSample = 0;
		ObjInfo.clear();
		TopKCandidate.clear();
	}
	
	public int incrementalLatency(double quest){
		int round = (int) Math.ceil(quest / QuestNumPerRound);
		return round;
	}
	
	public void Swap(ArrayList<Object> Top, int a, int b){
		Object obja = Top.get(a);
		Object objb = Top.get(b);
		Top.remove(a);
		Top.add(a, objb);
		Top.remove(b);
		Top.add(b, obja);
	}
	
	public void Display(ArrayList<Object> arr){
		for(int i=0;i<arr.size();i++){
			System.out.format("%d|%.2f|%d\t", arr.get(i).getID(), arr.get(i).getScore(), arr.get(i).getPosition());
			//System.out.print(arr.get(i).getPosition()+" ");
		}
		System.out.println();
		System.out.println("SIZE "+arr.size());
	}
	

	public double getPrecision(){
		if (TopKCandidate.size() < TopK){
			return 0;
		}
		Display(TopKCandidate);
		return getPrecision(TopKCandidate);
	}

	
	public double getPrecision(ArrayList<Object> topKObjs){
		double count = 0;
		
		for(int i=0; i<topKObjs.size(); i++){
			if(topKObjs.get(i).getPosition()>=1 && topKObjs.get(i).getPosition()<=topKObjs.size()){
				count++;
			}
		}
		
		return count/topKObjs.size();
	}
	
	public int getSampleSize() {
		return sampleSize;
	}

	public void setSampleSize(int sampleSize) {
		this.sampleSize = sampleSize;
	}

	public int Relevance(int position, int real_position){
		return Relevance(position, real_position, TopK);
	}
	
	public int Relevance(int position, int real_position, int topK){
		return Math.max(0, topK - Math.abs(position - real_position));
	}
	
	public double getNDCG(){
		if (TopKCandidate.size() < TopK){
			return 0;
		}
		return getNDCG(TopKCandidate);
	}

	public double getNDCG(ArrayList<Object> topKObjs){
		// Discounted Cumulative Gain
		double DCG;
		// Ideal Discounted Cumulative Gain
		double IDCG;
		
		double weight;
		
		DCG = Relevance(topKObjs.get(0).getPosition(), 1, topKObjs.size());
		IDCG = Relevance(0, 0, topKObjs.size());
		
		for (int i = 1; i < topKObjs.size(); i++){
			weight = Math.log(i + 1) / Math.log(2);
			DCG += Relevance(topKObjs.get(i).getPosition(), i + 1, topKObjs.size()) / weight;
			IDCG += Relevance(i + 1, i + 1, topKObjs.size()) / weight;
		}
		
		return DCG/IDCG;
	}
	
	public abstract void Run();
}


class TestMethod {
	public TopkAlgorithm method;
	public double questNum = 0;
	public double ndcg = 0;
	public double precision = 0;
	public double round = 0;
	
	public void update(){
		// update total monetary cost
		questNum += method.getTotalQuest();
		// update NDCG
		ndcg += method.getNDCG();
		// update precision
		precision += method.getPrecision();
		// update latency
		round += method.getLatencyRound();
	}
	
	public void averageStat(int loopNum){
		// update total monetary cost
		questNum /= loopNum;
		// update NDCG
		ndcg /= loopNum;
		// update precision
		precision /= loopNum;
		// update latency
		round /= loopNum;
	}
}