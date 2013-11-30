
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import cs132.vapor.ast.*;

//NOTE: at beginning of function, save any callee saved
//registers that are going to be used


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
	
	//Either assign loaded mem value to resultReg or assign the assigned reg 
	//to this var to the resultReg
	public String spillOrSubstituteVariable(String resultReg,String variable,Integer indentation) {
		ArrayList<String> spilledVariablesForFunc = spilledVariables.get(currentFunction);
		HashMap<String,String> regAssignments = registerAssignments.get(currentFunction);
		String returnString = "";
		int index = spilledVariablesForFunc.indexOf(variable);
		if(index != -1) {
			//Load val from memory and put in result reg
			returnString = retrieveValueFromMemory(currentFunction, variable, resultReg, indentation);
		} else {
			//swap w/ reg we've given 
			variable = regAssignments.get(variable);
			if(variable == null)
				variable = spillRegisters[2];
			//Assign register val to result reg
			returnString = assign(resultReg, variable, indentation);
		}
		
		return returnString;
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
	//Assignment statement
	public String visit(Integer indentation, VAssign arg1) throws Throwable {
		ArrayList<String> spilledVariablesForFunc = spilledVariables.get(currentFunction);
		HashMap<String,String> regAssignments = registerAssignments.get(currentFunction);

		String dest = arg1.dest.toString();
		String val = arg1.source.toString();
		String assignment = "";
		
		if(isOperandVariable(arg1.source)) {
			assignment = spillOrSubstituteVariable(spillRegisters[0], val, indentation);
			val = spillRegisters[0];
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
				String spillReg = spillRegisters[argReg];
				String val = op.toString();
				String s = spillOrSubstituteVariable(spillReg, val, indentation);
				builtInCall = concatentateInstructions(builtInCall, s);
				argRegisters[argReg] = spillReg;
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
	//Memory write operation
	public String visit(Integer indentation, VMemWrite arg1) throws Throwable {
		String memWrite = "";
		ArrayList<String> spilledVariablesForFunc = spilledVariables.get(currentFunction);
		HashMap<String,String> regAssignments = registerAssignments.get(currentFunction);
		String sourceVar = arg1.source.toString();
		
		if(isOperandVariable(arg1.source)) {
			//Operand is a variable. 
			String spillReg = spillRegisters[0];
			memWrite = spillOrSubstituteVariable(spillReg, sourceVar, indentation);
			sourceVar = spillReg;
		}
		
		if(arg1.dest instanceof VMemRef.Global) {
			VMemRef.Global ref = (VMemRef.Global)arg1.dest;
			String baseAddr = variableFromMemAddress(ref.base);
			if(baseAddr != null) {
				//Base address is a var
				String resultReg = spillRegisters[1];
				String s = spillOrSubstituteVariable(resultReg, baseAddr, indentation);
				String lhs = accessMemory(resultReg, ref.byteOffset);
				memWrite = concatentateInstructions(memWrite, s, assign(lhs,sourceVar,indentation));
			} else {
				String lhs = accessMemory(ref.base.toString(), ref.byteOffset);
				memWrite = concatentateInstructions(memWrite, assign(lhs,sourceVar,indentation));

			}
		} else {
			assert(false);
		}
		
		
		return memWrite;
	}

	@Override
	public String visit(Integer indentation, VMemRead arg1) throws Throwable {
		String memRead = "";
		ArrayList<String> spilledVariablesForFunc = spilledVariables.get(currentFunction);
		HashMap<String,String> regAssignments = registerAssignments.get(currentFunction);
		String destVar = arg1.dest.toString();

		
		String sourceResultReg = "";
		if(arg1.source instanceof VMemRef.Global) {
			VMemRef.Global ref = (VMemRef.Global)arg1.source;
			String baseAddr = variableFromMemAddress(ref.base);
			sourceResultReg = spillRegisters[0];
			if(baseAddr != null) {
				//Base address is a var
				String s = spillOrSubstituteVariable(sourceResultReg, baseAddr, indentation);
				String rhs = assign(sourceResultReg,accessMemory(sourceResultReg, ref.byteOffset),indentation);
				memRead = concatentateInstructions(memRead, s,rhs);
			} else {
				String rhs = accessMemory(ref.base.toString(), ref.byteOffset);
				memRead = concatentateInstructions(memRead, assign(sourceResultReg,rhs,indentation));
			}
		} else {
			assert(false);
		}
		
		
		int index = spilledVariablesForFunc.indexOf(destVar);
		if(index != -1) {
			//Store val in memory
			memRead = concatentateInstructions(memRead, storeValueInMemory(currentFunction, destVar, sourceResultReg, indentation));
		} else {
			String destReg = regAssignments.get(destVar);
			if(destReg == null)
				destReg = spillRegisters[2];
			memRead = concatentateInstructions(memRead, assign(destReg, sourceResultReg, indentation));
		}
		
		return memRead;
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
