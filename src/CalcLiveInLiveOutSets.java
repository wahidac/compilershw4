import java.util.HashMap;

import cs132.vapor.ast.VAddr;
import cs132.vapor.ast.VAssign;
import cs132.vapor.ast.VBranch;
import cs132.vapor.ast.VBuiltIn;
import cs132.vapor.ast.VCall;
import cs132.vapor.ast.VCodeLabel;
import cs132.vapor.ast.VFunction;
import cs132.vapor.ast.VGoto;
import cs132.vapor.ast.VInstr;
import cs132.vapor.ast.VMemRead;
import cs132.vapor.ast.VMemRef;
import cs132.vapor.ast.VMemWrite;
import cs132.vapor.ast.VOperand;
import cs132.vapor.ast.VReturn;
import cs132.vapor.ast.VTarget;
import cs132.vapor.ast.VVarRef;
import cs132.vapor.ast.VaporProgram;

//Init use + def sets
public class CalcLiveInLiveOutSets extends VInstr.VisitorP<CFGNode,Throwable>  {

	//Map function names to CFGs
	HashMap <String,CFGNode> CFGs;
	//Map instructions to CFGs
	HashMap <VInstr,CFGNode> instructionsToCFGNode;
	VaporProgram program;
	
	public CalcLiveInLiveOutSets(HashMap<String,CFGNode>) throws Throwable {
		CFGs = new HashMap<String,CFGNode>();
		instructionsToCFGNode = new HashMap<VInstr,CFGNode>();
		this.program = program;
		CFGNode prev = null;
		for(VFunction func:program.functions) {
			CFGNode root;
			for(int i = 0; i < func.body.length; i++) {
				CFGNode n = new CFGNode(func,func.body[i]);
				instructionsToCFGNode.put(func.body[i], n);
				if(i == 0) {
					root = n;
					CFGs.put(func.ident, root);
				}
				//Calc use + def set
				func.body[i].accept(n, this);
				if(prev != null) {
					prev.successors.add(n);
				} 
				prev = n;
			}			
			
			//Now add in extra edges by looking at goto statements. 
			for(int i = 0; i < func.body.length; i++) {
				//Goto's appear in goto statements and branches. A goto 
				VInstr instr = func.body[i];
				CFGNode n = instructionsToCFGNode.get(instr);
				if(instr instanceof VBranch) {
					VBranch branch = (VBranch)instr;
					int targetInstructionIndex = branch.target.getTarget().instrIndex;
					VInstr targetInstruction = func.body[targetInstructionIndex];
					//Get the CFG Node associated with this instruction
					CFGNode targetNode = instructionsToCFGNode.get(targetInstruction);
					assert(targetNode != null);
					//Add an edge from this node to the target node
					n.successors.add(targetNode);
				} else if(instr instanceof VGoto) {
					VGoto gotoInstr = (VGoto)instr;
					VAddr<VCodeLabel> address = gotoInstr.target;
					if(address instanceof VAddr.Label<?>) {
						//A static label, so we know where this goto will lead to
						VAddr.Label<VCodeLabel> label = (VAddr.Label<VCodeLabel>) address;
						int targetInstructionIndex = label.label.getTarget().instrIndex;
						VInstr targetInstruction = func.body[targetInstructionIndex];
						//Get the CFG Node associated with this instruction
						CFGNode targetNode = instructionsToCFGNode.get(targetInstruction);
						assert(targetNode != null);
						//Add an edge from this node to the target node
						n.successors.add(targetNode);
					} else if(address instanceof VAddr.Var<?>) {
						//Address is stored inside a variable
						//Can't at compile time know all labels this goto may go to so 
						//add an edge between this node and all labels in this function
						//NOTE: if considering scope of whole program, need to look at all gotos
						
						//Iterate through all labels in this function
						for(VCodeLabel l:func.labels) {
							int targetInstructionIndex = l.instrIndex;
							VInstr targetInstruction = func.body[targetInstructionIndex];
							//Get the CFG Node associated with this instruction
							CFGNode targetNode = instructionsToCFGNode.get(targetInstruction);
							assert(targetNode != null);
							//Add an edge from this node to the target node
							n.successors.add(targetNode);
						}
					}
				}
			}
		
		}	
	}
	

}
