package pairwiseCrowdsourcing;

import java.util.*;

public class TreeNode {
	private TreeNode Parent;
	private Object CurrentObject;
	private ArrayList<TreeNode> Children = new ArrayList<TreeNode>();
	
	public Object getCurrentObject() {
		return CurrentObject;
	}
	public void setCurrentObject(Object currentObject) {
		CurrentObject = currentObject;
	}
	public TreeNode getParent() {
		return Parent;
	}
	public void setParent(TreeNode parent) {
		Parent = parent;
	}
	public ArrayList<TreeNode> getChildren() {
		return Children;
	}
	public void setChildren(TreeNode children) {
		Children.add(children);
	}
	public Boolean hasChildren(){
		if(Children.size() == 0){
			return false;
		}else{
			return true;
		}
	}

}