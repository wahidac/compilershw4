import java.util.HashMap;
import java.util.HashSet;

import cs132.vapor.ast.VFunction;
import cs132.vapor.ast.VInstr;
import cs132.vapor.ast.VaporProgram;


//Use the live in + live out sets to calculate live ranges
//for all of the variables so we can apply the linear
//scan algorithm. Number instructions based on
//their physical line number in the file
public class CalcLiveRanges {
	//Map function names to CFGs
	HashMap <String,CFGNode> CFGs;
	//Map instructions to CFGs
	HashMap <VInstr,CFGNode> instructionsToCFGNode;
	VaporProgram program;
	
	public CalcLiveRanges(HashMap <String,CFGNode> CFGs,HashMap <VInstr,CFGNode> instructionsToCFGNode, VaporProgram program ) {
		this.program = program;
		this.CFGs = CFGs;
		this.instructionsToCFGNode = instructionsToCFGNode;
		
		//Traverse each CFG
		for(VFunction f:program.functions) {
			HashMap<String,LiveRanges> ranges = new HashMap<String, LiveRanges>();
			HashMap<String,HashSet<int[]>> currentlyLive = new HashMap<String,HashSet<int[]>>();
			CalcLiveRangesForCFG(CFGs.get(f.ident),ranges,currentlyLive);
		}
	}
	
	//Traverse CFG to calc live ranges.
	public void CalcLiveRangesForCFG(CFGNode node, HashMap<String,LiveRanges> ranges,HashMap<String,HashSet<int[]>> currentlyLive ) {
		//Keep track of what variables are currently live
		for(String v:node.liveOut) {
			//Because this variable is live out, the var will be live in
			//in at least one of its successors. Find which successors, and
			//for each, add an edge to represent liveness
			for(CFGNode successor:node.successors) {
				if(successor.liveIn.contains(v)) {
					//int edge[2] = node.
				}
			}
			
			
			//If this variable is live on prev edge that led us to this node,
			//then insert this range into the existing list, else start a new
			//one because these ranges are disjoint
			
			if(currentlyLive.containsKey(v)) {
				for(String v:node.successors) {
					//
				}
			}
		}
	}

}
