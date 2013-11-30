
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import cs132.vapor.ast.*;


//Traverse tree, swapping variable references to the registers they
//are assigned to, or a position in memory if they've been spilled
public class PrintAST extends VInstr.VisitorPR<Integer, String, Throwable>  {
	  
	//Map functions to register assignments
    HashMap<String,HashMap<String,String>> registerAssignments;
	//Map functions to any spilled variables in the function
	HashMap<String, ArrayList<String>> spilledVariables;
	String currentFunction;
	String indentationSpacing;
	
	String [] spillRegisters;
    String [] arguments;

	public PrintAST(HashMap<String,HashMap<String,String>> registerAssignments, HashMap<String, ArrayList<String>> spilled ) {
		this.registerAssignments = registerAssignments;
		this.spilledVariables = spilled;
		currentFunction = "";
		spillRegisters = new String[3];
		arguments = new String[4];
		
		spillRegisters[0] = "$v0";
		spillRegisters[1] = "$v1";
		spillRegisters[2] = "$t0";
		
		String prefix = "$a";
		for(int i = 0; i < arguments.length; i++) {
			arguments[i] = prefix + String.valueOf(i);
		}
		
		
		indentationSpacing = "  ";	
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
	
	public String storeValueInMemory(String funName, String variable, String sourceVal, Integer indentation) {
		ArrayList<String> spilledVarsForFunc = spilledVariables.get(funName);
		//index in local array = index in array list
		int memoryOffset = spilledVarsForFunc.indexOf(variable);
			
		//Generate vapor code for spill. Assumed that locals array already
		//has enough space allocated  
		String spillString = "local[" + String.valueOf(memoryOffset) + "]";
		spillString = assign(spillString, sourceVal, indentation);

		return spillString;
	}
	
	//Retrieve a spilled variable and load it into destReg
	public String retrieveValueFromMemory(String funName, String variable, String destReg, Integer indentation) {
		ArrayList<String> spilledVarsForFunc = spilledVariables.get(funName);
		//index in local array = index in array list
		int indexOfVar = spilledVarsForFunc.indexOf(variable);
		
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
		ArrayList<String> spilledVariablesForFunc = spilledVariables.get(currentFunction);
		HashMap<String,String> regAssignments = registerAssignments.get(currentFunction);

		String dest = arg1.dest.toString();
		String val = arg1.source.toString();
		String assignment = "";
		
		if(isOperandVariable(arg1.source)) {
			//Operand is a variable. 
			int index = spilledVariablesForFunc.indexOf(val);
			if(index != -1) {
				//Load val from memory and put in a spill register
				assignment = retrieveValueFromMemory(currentFunction, val, spillRegisters[0], indentation);
				val = spillRegisters[0];
			} else {
				//swap w/ reg we've given 
				val = regAssignments.get(val);
				if(val == null)
					val = spillRegisters[2];
			}
		}
		
		
		int index = spilledVariablesForFunc.indexOf(dest);
		if(index != -1) {
			//Store val in memory
			assignment = concatentateInstructions(assignment, storeValueInMemory(currentFunction, dest, val, indentation));
		} else {
			dest = regAssignments.get(dest);
			if(dest == null)
				dest = spillRegisters[2];
			assignment = concatentateInstructions(assignment, assign(dest, val, indentation));
		}
		
		return assignment;	
	}

	@Override
	public String visit(Integer indentation, VCall arg1) throws Throwable {
		//Function call. Look at arguments function expects and assign
		//parameters to arg registers + out arr if too many params.
		
	/*	int argReg = 0;
		String assignArgsToReg = "";
		for(VOperand operand:arg1.args) {
			//If an argument is a variable, load it from the stack memory
			boolean isVariable = isOperandVariable(operand);
			String argRegOperand = "";
			String destReg = spillRegisters[0];
			String opString = operand.toString();
			if(isVariable) {//Spill it to dest reg
				String spillString = retrieveValueFromMemory(currentFunction,opString, destReg, indentation);
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
		}*/
		return null;
	}
	
		

	@Override
	public String visit(Integer indentation, VBuiltIn arg1) throws Throwable {
		ArrayList<String> spilledVariablesForFunc = spilledVariables.get(currentFunction);
		HashMap<String,String> regAssignments = registerAssignments.get(currentFunction);
		String builtInCall = "";
		//Check whether arguments are variables
		String []argRegisters = {"",""};
		int argReg = 0;
		for(argReg = 0; argReg < arg1.args.length; argReg++) {
			VOperand op = arg1.args[argReg];
			if(isOperandVariable(op)) {
				String val = op.toString();
				int index = spilledVariablesForFunc.indexOf(val);
				if(index != -1) {
					//load from mem
					String spillReg = spillRegisters[argReg];
					builtInCall = concatentateInstructions(builtInCall,retrieveValueFromMemory(currentFunction, val, spillReg, indentation));
					argRegisters[argReg] = spillReg;
				} else {
					//swap w/ reg we've given 
					val = regAssignments.get(val);
					if(val == null)
						val = spillRegisters[2];
					argRegisters[argReg] = val;
				}
			}
		}

	
		String arguments = "";
		for(int i = 0; i < arg1.args.length; i++) {
			if(argRegisters[i].isEmpty())
				arguments += " " + arg1.args[i].toString();
			else
				arguments += " " + argRegisters[i];
		}
		String call = arg1.op.name + "(" + arguments + ")";
		if(arg1.dest == null) {
			builtInCall = concatentateInstructions(builtInCall, call);
		} else {
			//Assigning call result to a variable
			String val = arg1.dest.toString();
			int index = spilledVariablesForFunc.indexOf(val);
			if(index != -1) {
				//assign result to a mem location
				String spillReg = spillRegisters[2];
				builtInCall = concatentateInstructions(builtInCall, assign(spillReg, call, indentation));
				String s = storeValueInMemory(currentFunction, val, spillReg, indentation);
				builtInCall = concatentateInstructions(builtInCall, s);
			} else {
				//swap w/ reg we've given 
				val = regAssignments.get(val);
				if(val == null)
					val = spillRegisters[2];
				builtInCall = concatentateInstructions(builtInCall, assign(val, call, indentation));
			}
				
		}
		
		return builtInCall;

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
