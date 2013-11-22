import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import cs132.vapor.ast.*;

public class SpillAllFieldsVisitor extends VInstr.VisitorPR<Integer, String, Throwable>  {

	//Mapping to keep track of where in memory our spilled variables are.
	ArrayList<String> spilledVariables;
	String [] spillRegisters;
	String [] calleeSaved;
	String [] callerSaved;
	String [] arguments;
	
	
	String indentationSpacing;
	

	public SpillAllFieldsVisitor() {
		spilledVariables = new ArrayList<String>();
		indentationSpacing = "  ";
		//Because only two operand operators in Vapor
		spillRegisters = new String[3];
		calleeSaved = new String[8];
		callerSaved = new String[9];
		arguments = new String[4];
		
		String prefix = "$s";
		for(int i = 0; i < calleeSaved.length; i++) {
			calleeSaved[i] = prefix + String.valueOf(i);
		}
		prefix = "$t";
		for(int i = 0; i < callerSaved.length; i++) {
			callerSaved[i] = prefix + String.valueOf(i);
		}
		
		prefix = "$a";
		for(int i = 0; i < arguments.length; i++) {
			arguments[i] = prefix + String.valueOf(i);
		}
		
		prefix = "$t";
		for(int i = 0; i < spillRegisters.length; i++) {
			spillRegisters[i] = prefix + String.valueOf(i);
		}
		
	
	}
	
	public String getIndentation(Integer indentation) {
		String ret = "";
		for(int i = 0; i<indentation;i++) {
			ret += indentationSpacing;
		}
		return ret;
	}
	
	private String concatentateInstructions(String v1, String v2, String...strings) {
		String concatentedString = v1 + "\n" + v2;
		for(String s:strings) {
		  concatentedString += "\n" + s;
	   }
		return concatentedString;
	}
	
	public String spillVariable(String variable, String sourceVal, Integer indentation) {
		//NOTE: are we considering goto to arbitrary places in code? if so, need more identifying
		//info than just variable name
		int indexOfVar = spilledVariables.indexOf(variable);
		int memoryOffset = 0;
		if(indexOfVar == -1) {
			//Haven't spilled before
			spilledVariables.add(variable);
			memoryOffset = spilledVariables.size() - 1;
		} else {
			memoryOffset = indexOfVar;
		}
		
		//Generate vapor code for spill. Assumed that locals array already
		//has enough space allocated  
		String spillString = "local[" + String.valueOf(memoryOffset) + "]";
		spillString = assign(spillString, sourceVal, indentation);

		return spillString;
	}
	
	//Retrieve a spilled variable and load it into destReg
	public String retrieveSpilledVariable(String variable, String destReg, Integer indentation) {
		int indexOfVar = spilledVariables.indexOf(variable);
		if(indexOfVar == -1) {
			//Check in stack
		}
		assert(indexOfVar != -1);
		
		String spillString = "local[" + String.valueOf(indexOfVar) + "]";
		spillString = assign(destReg, spillString, indentation);	
		return spillString;
	}
	
	public static boolean isOperandVariable(VOperand operand) {
		return (operand instanceof VVarRef.Local);
	}
	
	@Override
	//Assignment statement
	public String visit(Integer indentation, VAssign arg1) throws Throwable {
		String dest = arg1.dest.toString();
		String val = arg1.source.toString();
		String assignment = "";
		
		//If val is a variable, load its value from spilled reg
		if(isOperandVariable(arg1.source)) {
			assignment = retrieveSpilledVariable(val, spillRegisters[0], indentation);
			val = spillRegisters[0];
		}
		
		//Put val in stack memory location that dest maps to 
		assignment = concatentateInstructions(assignment, spillVariable(dest, val, indentation));
		//Assign value to temp register and spill that temp register
		return assignment;	
	}

	@Override
	public String visit(Integer indentation, VCall arg1) throws Throwable {
		//Function call. Look at arguments function expects and assign
		//parameters to arg registers + out arr if too many params.
		
		int argReg = 0;
		String assignArgsToReg = "";
		for(VOperand operand:arg1.args) {
			//If an argument is a variable, load it from the stack memory
			boolean isVariable = isOperandVariable(operand);
			String argRegOperand = "";
			String destReg = spillRegisters[0];
			String opString = operand.toString();
			if(isVariable) {//Spill it to dest reg
				String spillString = retrieveSpilledVariable(opString, destReg, indentation);
				assignArgsToReg = concatentateInstructions(assignArgsToReg, spillString);
				argRegOperand = destReg;
			} else {
				argRegOperand = opString;
			}
			
			if(argReg > 3) {
				//Start puttin inside out storage. 
				//NOTE: make sure out storage size is sufficient. Can do by incrementing
				//counter and then at top level set declaration after body has been traversed
				int outIndex = argReg-4;
				assignArgsToReg = concatentateInstructions(assignArgsToReg, assign("out[" + String.valueOf(outIndex) + "]",argRegOperand, indentation));
			}
			else {
				assignArgsToReg = concatentateInstructions(assignArgsToReg,assign(arguments[argReg],argRegOperand,indentation));
			}
			argReg++;	
		}
		return null;
	}
	
		

	@Override
	public String visit(Integer indentation, VBuiltIn arg1) throws Throwable {
		// TODO Auto-generated method stub
		String builtInCall = "";
		//Check whether arguments, if so spill them
		String []argRegisters = {"",""};
		int argReg = 0;
		for(argReg = 0; argReg < arg1.args.length; argReg++) {
			VOperand op = arg1.args[argReg];
			if(isOperandVariable(op)) {
				//load from mem
				//NOTE: built ins w/ more than one operand? test in vapor parser
				String spillReg = spillRegisters[argReg];
				builtInCall = concatentateInstructions(builtInCall,retrieveSpilledVariable(op.toString(), spillReg, indentation));
				argRegisters[argReg] = spillReg;
			}
		}

		String dest = spillRegisters[argReg];
		String arguments = "";
		for(int i = 0; i < argRegisters.length; i++) {
			if(argRegisters[i].isEmpty())
				arguments += " " + arg1.args[i].toString();
			else
				arguments += " " + argRegisters[i];
		}
		String call = arg1.op.name + "(" ;
		builtInCall = concatentateInstructions(builtInCall, assign(dest, arg1., indentation), strings)
		built
		
		String val = arg1.source.toString();
		String assignment = "";
		
		//If val is a variable, load its value from spilled reg
		if(isOperandVariable(arg1.source)) {
			assignment = retrieveSpilledVariable(val, spillRegisters[0], indentation);
			val = spillRegisters[0];
		}
		
		//Put val in stack memory location that dest maps to 
		assignment = concatentateInstructions(assignment, spillVariable(dest, val, indentation));
		//Assign value to temp register and spill that temp register
		return null;
	}

	@Override
	public String visit(Integer arg0, VMemWrite arg1) throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visit(Integer arg0, VMemRead arg1) throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visit(Integer arg0, VBranch arg1) throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visit(Integer arg0, VGoto arg1) throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visit(Integer arg0, VReturn arg1) throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	  //Commonly used strings
	   private String accessMemory(String var, int offset) {
		   return "[" + var + "+" + String.valueOf(offset) + "]";
	   }
	   
	   private String assign(String v1, String v2, Integer indentation) {
		   return getIndentation(indentation) + v1 + " = " + v2;
	   }
	   
}
