
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

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
	//Starting offset for spilled variables (everything after the backup storage)
	int spillStorageStartOffset;
	//Starting offset for backup storage of caller saved registers
	int startingOffsetForCallerSavedRegisters;
	
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
	
	public String loadFromArgumentRegisters(VVarRef.Local []params) {
		ArrayList<String> spilledVariablesForFunc = spilledVariables.get(currentFunction);
		HashMap<String,String> regAssignments = registerAssignments.get(currentFunction);
		String loadArguments = "";
		//Load everything from the argument registers
		for(int i = 0; i < params.length; i++) {
			String paramVal = "";
			if(i < 4) {
				paramVal = arguments[i];
			} else {
				//Load this parameter from the in stack
				int indexForInStack = i - 4;
				String assignment = assign(spillRegisters[0], "in[ " +  String.valueOf(indexForInStack) + " ]", 1);
				loadArguments = concatentateInstructions(loadArguments, assignment);
				paramVal = spillRegisters[0];
			}
				String parameter = params[i].toString();
				int index = spilledVariablesForFunc.indexOf(parameter);
				if(index != -1) {
					//Store val in memory
					loadArguments = concatentateInstructions(loadArguments, storeValueInMemory(currentFunction, parameter, paramVal, 1));
				} else {
					String destReg = regAssignments.get(parameter);
					if(destReg == null)
						destReg = spillRegisters[2];
					loadArguments = concatentateInstructions(loadArguments, assign(destReg, paramVal, 1));
				}
		
		  }
		
		  return loadArguments;
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
		String spillString = "local[" + String.valueOf(memoryOffset+spillStorageStartOffset) + "]";
		spillString = assign(spillString, sourceVal, indentation);

		return spillString;
	}
	
	//Retrieve a spilled variable and load it into destReg
	public String retrieveValueFromMemory(String funName, String variable, String destReg, Integer indentation) {
		ArrayList<String> spilledVarsForFunc = spilledVariables.get(funName);
		//index in local array = index in array list
		int indexOfVar = spilledVarsForFunc.indexOf(variable);
		
		String spillString = "local[" + String.valueOf(indexOfVar+spillStorageStartOffset) + "]";
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
		ArrayList<String> spilledVariablesForFunc = spilledVariables.get(currentFunction);
		HashMap<String,String> regAssignments = registerAssignments.get(currentFunction);
		//Function call. Look at arguments function expects and assign
		//parameters to arg registers + out arr if too many params.
		String functionCall = "";
		//Backup caller saved registers in use
		ArrayList<String> callerSavedRegisters = new ArrayList<String>();
		String backupString = saveCallerSavedRegisters(callerSavedRegisters);
		String reloadString = loadCallerSavedRegisters(callerSavedRegisters);
		
		int paramNum = 0;
		String handleParams = "";
		for(VOperand operand:arg1.args) {
			boolean isVariable = isOperandVariable(operand);
			String currentArgument = "";
			if(isVariable) {
				String s = spillOrSubstituteVariable(spillRegisters[0], operand.toString(), indentation);
				handleParams = concatentateInstructions(handleParams, s);
				currentArgument = spillRegisters[0];
			} else {
				currentArgument = operand.toString();
			}
			
			if(paramNum < 4) {
				String destReg = this.arguments[paramNum];
				handleParams = concatentateInstructions(handleParams, assign(destReg,currentArgument,indentation));
			} else {
				int outOffset = paramNum - 4;
				String destAddress = "out[" + String.valueOf(outOffset) + "]";
				handleParams = concatentateInstructions(handleParams, assign(destAddress,currentArgument,indentation));
			}
			
			paramNum++;
		}
		
		String funcCallString = "";
		//Now make the function call
		String funcCallAddress = variableFromMemAddress(arg1.addr);
		if(funcCallAddress != null) {
			String s = spillOrSubstituteVariable(spillRegisters[0], funcCallAddress, indentation);
			funcCallString = concatentateInstructions(functionCall, s);
			funcCallAddress = spillRegisters[0];
		} else {
			funcCallAddress = arg1.addr.toString();
		}
		String c = getIndentation(indentation) + "call " + funcCallAddress;
		funcCallString = concatentateInstructions(funcCallString, c);
		
		//If return val assign value in $v0 to variable getting return value
		String retHandling = "";
		if(arg1.dest != null) {
			String destVar = arg1.dest.toString();
			int index = spilledVariablesForFunc.indexOf(destVar);
			if(index != -1) {
				//Store val in memory
				retHandling = concatentateInstructions(retHandling, storeValueInMemory(currentFunction, destVar, "$v0", indentation));
			} else {
				destVar = regAssignments.get(destVar);
				if(destVar == null)
					destVar = spillRegisters[2];
				retHandling = concatentateInstructions(retHandling, assign(destVar, "$v0", indentation));
			}
		} 
		
		functionCall = concatentateInstructions(backupString, handleParams,funcCallString,reloadString,retHandling);		
		return functionCall;
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
			builtInCall = concatentateInstructions(builtInCall, getIndentation(indentation)+ call);
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
	public String visit(Integer indentation, VBranch arg1) throws Throwable {
		String ifString = "";
		String ifCondition = "";
		if(isOperandVariable(arg1.value)) {
			//Operand is a variable. 
			String var = arg1.value.toString();
			ifString = spillOrSubstituteVariable(spillRegisters[0], var, indentation);
		} else {
			ifString = assign(spillRegisters[0],arg1.value.toString(),indentation);
		}
		String branchReg = spillRegisters[0];
		
		if(arg1.positive) {
			ifCondition = "if ";
		} else {
			ifCondition = "if0 ";
		}
		 
		ifCondition = getIndentation(indentation) + ifCondition + branchReg + " goto " + arg1.target.toString();
		return concatentateInstructions(ifString, ifCondition);
	}

	@Override
	public String visit(Integer indentation, VGoto arg1) throws Throwable {
		String var = variableFromMemAddress(arg1.target);
		String gotoString = "";
		if(var != null) {
			gotoString = spillOrSubstituteVariable(spillRegisters[0], var, indentation);
			gotoString = concatentateInstructions(gotoString, getIndentation(indentation) + "goto " + spillRegisters[0]);
			return gotoString;
		} else {
			return "goto " + arg1.target.toString();
		}
	}

	@Override
	public String visit(Integer indentation, VReturn arg1) throws Throwable {
		String returnString = "";
		
		if(arg1.value == null) {
			return "";
		}
		
		if(isOperandVariable(arg1.value)) {
			//Operand is a variable. 
			String var = arg1.value.toString();
			returnString = spillOrSubstituteVariable("$v0", var, indentation);
		} else {
			returnString = assign("$v0", arg1.value.toString(), indentation);
		}
		
		return returnString; 
	}
	
	public int numCallerSavedRegistersInUse() {
		HashMap<String,String> assignments = registerAssignments.get(currentFunction);
		HashSet<String> setOfCallerSavedRegs = new HashSet<String>();
		for(Entry<String, String> e:assignments.entrySet()) {
			String reg = e.getValue();
			String type = RegisterAllocator.registerType(reg);
			if(type.equals("CALLER_SAVED")) {
				setOfCallerSavedRegs.add(reg);
			}
		}
		return setOfCallerSavedRegs.size();
	}
	
	public String saveCallerSavedRegisters(ArrayList<String> callerSavedRegisters) {
		String returnString = "";
		//Backup all caller saved registers that are being used 
		HashSet<String> setOfCallerSavedRegs = new HashSet<String>();
		HashMap<String,String> assignments = registerAssignments.get(currentFunction);
		for(Entry<String, String> e:assignments.entrySet()) {
			String reg = e.getValue();
			String type = RegisterAllocator.registerType(reg);
			if(type.equals("CALLER_SAVED")) {
				setOfCallerSavedRegs.add(reg);
			}
		}
		
		for(String s:setOfCallerSavedRegs) {
			callerSavedRegisters.add(s);
		}

		//Backup into local
		for(int i = 0; i < callerSavedRegisters.size(); i++) {
			String backupString = "local[" + String.valueOf(i+startingOffsetForCallerSavedRegisters) + "]";
			backupString = getIndentation(1) + backupString + " = " + callerSavedRegisters.get(i); 
			returnString = concatentateInstructions(returnString, backupString);
		}
		
		return returnString;
	}
	
	public String loadCallerSavedRegisters(ArrayList<String> callerSavedRegisters) {
		String returnString = "";
		
		//Load back the caller saved registers
		for(int i = 0; i < callerSavedRegisters.size(); i++) {
			String backupString = "local[" + String.valueOf(i+startingOffsetForCallerSavedRegisters) + "]";
			backupString = getIndentation(1) + callerSavedRegisters.get(i) + " = " + backupString; 
			returnString = concatentateInstructions(returnString, backupString);
		}
		
		return returnString;
	}
	
	
    //Commonly used strings
	private String accessMemory(String var, int offset) {
		return "[" + var + "+" + String.valueOf(offset) + "]";
    }
	  
    private String assign(String v1, String v2, Integer indentation) {
        return getIndentation(indentation) + v1 + " = " + v2;
	}
	   
}
