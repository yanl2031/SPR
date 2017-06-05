package pairwiseCrowdsourcing;


import java.io.*;
import java.util.*;


public class Tournament extends MinHeapTie{
	public void Run(){
		TournamentTree();
	}
	
	public void TournamentTree(){
		TournamentTree(ObjInfo, TopKCandidate, TopK);
	}
	
	public void TournamentTree(ArrayList<Object> objSet, ArrayList<Object> topKObj, int k){
		TreeNode node, root;
		Queue <TreeNode> IntNode = new LinkedList<TreeNode>();
		
		for (Object o : objSet){
			node = new TreeNode();
			node.setCurrentObject(o);
			IntNode.add(node);
		}
		
		root = BuildTree(IntNode);
		topKObj.add(root.getCurrentObject());
		for (int i = 0; i < k - 1; i++){
			root = FindNewRoot(root);
			topKObj.add(root.getCurrentObject());
		}
	}
	
	public TreeNode BuildTree(Queue <TreeNode> IntNode){
		int maxRound;
		int winnerID;
		TreeNode parent;
		TreeNode nodea;
		TreeNode nodeb;
		Object a, b;
		
		//Collections.shuffle(ObjInfo);
		
		// build the first complete tree
		while(IntNode.size() != 1){
			maxRound = 0;
			for (int i = 0; i < IntNode.size() / 2; i++){
				parent = new TreeNode();
				nodea = IntNode.poll();
				nodeb = IntNode.poll();
				
				a = nodea.getCurrentObject();
				b = nodeb.getCurrentObject();
				winnerID = a.Compare(b, max_threshold);
				TotalQuest += a.getCompareQuestIncrement(b);
				maxRound = Math.max(maxRound, incrementalLatency(a.getCompareQuestIncrement(b)));
				
				if(winnerID == a.getID()){
					parent.setCurrentObject(a);
				}
				else{
					parent.setCurrentObject(b);
				}

				parent.setChildren(nodea);
				parent.setChildren(nodeb);
				nodea.setParent(parent);
				nodeb.setParent(parent);
				IntNode.add(parent);
			}
			LatencyRound += maxRound;
		}

		return IntNode.poll();
	}
	
	public TreeNode FindNewRoot(TreeNode root){
		//TreeNode
		Queue <TreeNode> IntNode = new LinkedList<TreeNode>();

		while(root.hasChildren()){
			int id = root.getCurrentObject().getID();
			if(id == root.getChildren().get(0).getCurrentObject().getID()){
				IntNode.add(root.getChildren().get(1));
				root = root.getChildren().get(0);
			}else{
				IntNode.add(root.getChildren().get(0));
				root = root.getChildren().get(1);
			}
		}
		
		//return BuildTree(IntNode);
		
		TreeNode parent;
		TreeNode nodea;
		TreeNode nodeb;
		Object a, b;
		int round;
		int winnerID;
		
		parent = IntNode.poll();
		
		while(IntNode.size() > 0){
			nodea = parent;
			nodeb = IntNode.poll();
			parent = new TreeNode();
			
			a = nodea.getCurrentObject();
			b = nodeb.getCurrentObject();
			winnerID = a.Compare(b, max_threshold);
			TotalQuest += a.getCompareQuestIncrement(b);
			round = incrementalLatency(a.getCompareQuestIncrement(b));
			LatencyRound += round;
			
			if(winnerID == a.getID()){
				parent.setCurrentObject(a);
			}
			else{
				parent.setCurrentObject(b);
			}

			parent.setChildren(nodea);
			parent.setChildren(nodeb);
			nodea.setParent(parent);
			nodeb.setParent(parent);
		}
		
		return parent;
	}
	
	public void TestLoop(int round) throws IOException, IOException{
		int aveQuest =0;
		int times =round;
		//Random random = new Random(20310);
		for(int i=0;i<times;i++){
			
			ReadItemInfo();
			//Collections.shuffle(ObjInfo, random);
			LatencyRound = 0;
			TournamentTree();
			aveQuest += TotalQuest;
//			Writer writer = null;
//			writer = new BufferedWriter(new OutputStreamWriter(
//			           new FileOutputStream("D:/WorkSpace/JAVAProgram/CrowdSourcing_1.umac.mo/src/BaselineTournament/Output/"+i+"TournametTree.txt"), "utf-8"));
//			for(int j=0;j<CompInfo.size();j++){
//				writer.write(CompInfo.get(j).getRound()+" "+
//						CompInfo.get(j).getIda()+" "+CompInfo.get(j).getIdb()+" "+CompInfo.get(j).getComparison()+" "
//						+CompInfo.get(j).getWinner()+"\n");	
//			}
//			writer.close();
//			
//			writer = new BufferedWriter(new OutputStreamWriter(
//			           new FileOutputStream("D:/WorkSpace/JAVAProgram/CrowdSourcing_1.umac.mo/src/BaselineTournament/Result/"+i+"TournametTreeTopK.txt"), "utf-8"));
//			for(int m=0;m<TopCandidate.size();m++){
//				writer.write(TopCandidate.get(m).getID()+" "+TopCandidate.get(m).getScore()+" "+TopCandidate.get(m).getPosition()+"\n");	
//			}
//			writer.close();
			System.out.println("Count of sample : " + Object.CountOfSample);
			Display(TopKCandidate);
			TotalQuest = 0;
			Object.CountOfSample = 0;
			ObjInfo.clear();
			TopKCandidate.clear();
		}
		aveQuest /= times;
		System.out.println("Ave Total Quest : "+aveQuest);
	}
	public static void main(String[] args) throws IOException, Exception {
		// TODO Auto-generated method stub
		Tournament test = new Tournament();
		//test.ReadItemInfo();
		//test.BuildTree();
		
		test.TestLoop(100);
	}

}
