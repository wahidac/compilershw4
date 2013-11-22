import java.util.HashMap;

import cs132.vapor.ast.VAddr;
import cs132.vapor.ast.VAssign;
import cs132.vapor.ast.VBranch;
import cs132.vapor.ast.VBuiltIn;
import cs132.vapor.ast.VCall;
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
public class InitCFG extends VInstr.VisitorP<CFGNode,Throwable>  {

	//Map function names to CFGs
	HashMap <String,CFGNode> CFGs;
	//Map instructions to CFGs
	HashMap <VInstr,CFGNode> instructionsToCFGNode;
	
	public InitCFG(VaporProgram program) throws Throwable {
		CFGs = new HashMap<String,CFGNode>();
		instructionsToCFGNode = new HashMap<VInstr,CFGNode>();
		
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
		
		}
		
		//Now add in extra edges by looking at goto statements
	}
	
	public static boolean isOperandVariable(VOperand operand) {
		return (operand instanceof VVarRef.Local);
	}

	public static String variableFromMemAddress(VAddr varAddr) {
		if(varAddr instanceof VAddr.Var<?>) {
			VAddr.Var v = (VAddr.Var)varAddr;
			String var = v.var.toString();
			return var;
		}
		return null;
	}
	
	
	@Override
	public void visit(CFGNode n, VAssign arg1) throws Throwable {
		//RHS variables are use set
		if(isOperandVariable(arg1.source)) {
			n.use.add(arg1.source.toString());
		}
		//LHS in def
		n.def.add(arg1.dest.toString());
	}


	@Override
	public void visit(CFGNode n, VCall arg1) throws Throwable {
		//Function call
		//All variable arguments in use 
		for(VOperand op:arg1.args) {
			if(isOperandVariable(op)) {
				//NOTE:'this' show up as local var right?
				n.use.add(op.toString());
			}
		}
		
		if(arg1.dest != null) {
			n.def.add(arg1.dest.toString());
		}
		
	}


	@Override
	public void visit(CFGNode n, VBuiltIn arg1) throws Throwable {
		//All variable arguments in use 
		for(VOperand op:arg1.args) {
			if(isOperandVariable(op)) {
				//NOTE:'this' show up as local var right?
				n.use.add(op.toString());
			}
		}
		if(arg1.dest != null) {		
			n.def.add(arg1.dest.toString());
		}
	}


	@Override
	public void visit(CFGNode n, VMemWrite arg1) throws Throwable {
		if(isOperandVariable(arg1.source)) {
			n.use.add(arg1.source.toString());
		}
		
		//Handle case in which base address is in a variable
		if(arg1.dest instanceof VMemRef.Global) {
			VMemRef.Global ref = (VMemRef.Global)arg1.dest;
			String baseAddr = variableFromMemAddress(ref.base);
			if(baseAddr != null) {
				n.use.add(baseAddr);
			}
		}
	}


	@Override
	public void visit(CFGNode n, VMemRead arg1) throws Throwable {
		//Handle case in which base address is in a variable
		
		if(arg1.source instanceof VMemRef.Global) {
			VMemRef.Global ref = (VMemRef.Global)arg1.source;
			String baseAddr = variableFromMemAddress(ref.base);
			if(baseAddr != null) {
				n.use.add(baseAddr);
			}
		}
		
		n.def.add(arg1.dest.toString());
		
	}


	@Override
	public void visit(CFGNode n, VBranch arg1) throws Throwable {
		if(isOperandVariable(arg1.value)) {
			n.use.add(arg1.value.toString());
		}
	}


	@Override
	public void visit(CFGNode n, VGoto arg1) throws Throwable {
		String var = variableFromMemAddress(arg1.target);
		if(var != null) {
			n.use.add(var);
		}
	}


	@Override
	public void visit(CFGNode n, VReturn arg1) throws Throwable {
		if(isOperandVariable(arg1.value)) {
			n.use.add(arg1.value.toString());
		}
		
	}

}
