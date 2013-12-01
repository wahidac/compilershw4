import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import cs132.util.ProblemException;
import cs132.vapor.parser.VaporParser;
import cs132.vapor.ast.VCodeLabel;
import cs132.vapor.ast.VDataSegment;
import cs132.vapor.ast.VFunction;
import cs132.vapor.ast.VInstr;
import cs132.vapor.ast.VOperand;
import cs132.vapor.ast.VReturn;
import cs132.vapor.ast.VaporProgram;
import cs132.vapor.ast.VBuiltIn.Op;

public class V2VM {
	public static void main(String[] args) {
		//V2VM.parseVapor(in, err)
		InputStream stream = null;
		if(args.length == 1) {
		   try {
			   stream = new FileInputStream(args[0]);
		   } catch (FileNotFoundException e) {
			   // TODO Auto-generated catch block
			   e.printStackTrace();
		   } 
		   
		} else {
			stream = System.in;
		}
		
		try {
			parseVapor(stream,System.err);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static VaporProgram parseVapor(InputStream in, PrintStream err)
			  throws IOException
			{
			  Op[] ops = {
			    Op.Add, Op.Sub, Op.MulS, Op.Eq, Op.Lt, Op.LtS,
			    Op.PrintIntS, Op.HeapAllocZ, Op.Error,
			  };
			  boolean allowLocals = true;
			  String[] registers = null;
			  boolean allowStack = false;

			  VaporProgram program;
			  try {
			    program = VaporParser.run(new InputStreamReader(in), 1, 1,
			                              java.util.Arrays.asList(ops),
			                              allowLocals, registers, allowStack);
			  }
			  catch (ProblemException ex) {
			    err.println(ex.getMessage());
			    return null;
			  }
			  
			  String dataSection = "";
			  //Just print out exact data section from input vapor code

			  for(VDataSegment seg:program.dataSegments) {
				  String currentSection = "const " + seg.ident;
				  for(VOperand.Static data: seg.values) {
					  String dataString = getIndentation(1)+data.toString();
					  currentSection = concatentateInstructions(currentSection, dataString);
				  }
				  dataSection = concatentateInstructions(dataSection, currentSection,"");
			  }
			  
			  //Build the CFG
			  InitCFG initCFGVisitor = null;
			  try {
				 initCFGVisitor = new InitCFG(program);
			  } catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			  }
			  
		
			  CalcLiveInLiveOutSets liveInLiveOutCalculator =  new CalcLiveInLiveOutSets(initCFGVisitor.CFGs, initCFGVisitor.instructionsToCFGNode, program);
			  CalcLiveRanges rangeCalculator = new CalcLiveRanges(liveInLiveOutCalculator.CFGs, initCFGVisitor.instructionsToCFGNode, program);
			  HashMap<String,HashMap<String,LiveRanges>> ranges = rangeCalculator.liveRanges;
			  //initCFGVisitor.printCFG();
			  //rangeCalculator.printLiveRanges();
			  RegisterAllocator regAllocater = new RegisterAllocator(ranges);
			  
			  //Calc largest num params
			  int largestNumOfParams = 0;
			  for(VFunction func:program.functions) {
				  int size =  func.params.length;
				  if(size > largestNumOfParams)
					  largestNumOfParams = size;
			  }
			  
			   //Go through functions now.
			  String functions = "";
			  PrintAST printASTVisitor = new PrintAST(regAllocater.registerAssignments,regAllocater.spilledVariables);
			  for(VFunction func:program.functions) {
				  VCodeLabel []labels =  func.labels;
				  //Function declaration
				  String currentFunction = "func " + func.ident;
				  printASTVisitor.currentFunction = func.ident;
				  int numLocals = func.vars.length;

				  //save any callee saved
				  //registers that are going to be used
				  HashMap<String,String> assignments = regAllocater.registerAssignments.get(func.ident);
				  ArrayList<String> calleeSavedRegisters = new ArrayList<String>();
				  String saveCalleeSavedReg = saveCalleeSavedRegisters(assignments,calleeSavedRegisters);
				  int numCalleeSavedRegisters = calleeSavedRegisters.size();
				  int numCallerSavedRegisters = printASTVisitor.numCallerSavedRegistersInUse();
				  int numSpilledVariables = printASTVisitor.spilledVariables.get(func.ident).size();
				  //Need room for backing up callee saved register, storing spilled variables, and
				  //backing up caller saved registers
				  int sizeOfLocalStack = numCalleeSavedRegisters + numSpilledVariables + numCallerSavedRegisters;
				  
				  int sizeOfInStack = 0;
				  int sizeOfOutStack = 0;
				  if(func.ident.equalsIgnoreCase("main")) {
					  sizeOfInStack = 0;
					  sizeOfOutStack = largestNumOfParams;
				  } else {
					  sizeOfInStack = largestNumOfParams;
					  sizeOfOutStack = largestNumOfParams;
				  }
				  
				  currentFunction += " [in " + String.valueOf(sizeOfInStack) +" , out " + String.valueOf(sizeOfOutStack) +" , local " + String.valueOf(sizeOfLocalStack) + "]";				 
				  currentFunction = concatentateInstructions(currentFunction, saveCalleeSavedReg);
				  
				  
				  printASTVisitor.spillStorageStartOffset = numCalleeSavedRegisters;
				  printASTVisitor.startingOffsetForCallerSavedRegisters = printASTVisitor.spillStorageStartOffset + numSpilledVariables;
			
				  //Load arguments to the functions from the arg registers + in stacks
				  String loadArgs = printASTVisitor.loadFromArgumentRegisters(func.params);
				  currentFunction = concatentateInstructions(currentFunction, loadArgs);
				  
				  for(int i = 0; i < func.body.length; i++) {
					  VInstr instruction = func.body[i];
					  //For each instruction, run our  visitor to generate code that should substitute what is in input file
					  try {
						String vaporMCode = instruction.accept(1,printASTVisitor);
						for(VCodeLabel l:labels) {
							if(l.instrIndex == i) {
								//Label refers to this instruction. Put it before the
								//generated vapor M code
								currentFunction = concatentateInstructions(currentFunction, l.ident+":");
							}
						}		
						currentFunction = concatentateInstructions(currentFunction, vaporMCode);
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					  
				  }
				  //Reload the callee saved registers
			      currentFunction = concatentateInstructions(currentFunction,loadCalleeSavedRegisters(calleeSavedRegisters));
				  currentFunction = concatentateInstructions(currentFunction, getIndentation(1)+"ret");

				  functions = concatentateInstructions(functions, currentFunction);

			  }
			  System.out.println(dataSection);
			  System.out.println(functions);
			  return program;
			}
	
	public static String saveCalleeSavedRegisters(HashMap<String,String> assignments, ArrayList<String> calleeSavedRegisters) {
		String returnString = "";
		//Backup all callee saved registers that will be used
		HashSet<String> setOfCalleeSavedRegs = new HashSet<String>();
		for(Entry<String, String> e:assignments.entrySet()) {
			String reg = e.getValue();
			String type = RegisterAllocator.registerType(reg);
			if(type.equals("CALLEE_SAVED")) {
				setOfCalleeSavedRegs.add(reg);
			}
		}
		
		for(String s:setOfCalleeSavedRegs) {
			calleeSavedRegisters.add(s);
		}
		//Order callee saved registers are in the arraylist = order they
		//will occupy the local stack
		
		//Backup into local
		for(int i = 0; i < calleeSavedRegisters.size(); i++) {
			String backupString = "local[" + String.valueOf(i) + "]";
			backupString = getIndentation(1) + backupString + " = " + calleeSavedRegisters.get(i); 
			returnString = concatentateInstructions(returnString, backupString);
		}
		
		return returnString;
	}
	
	public static String loadCalleeSavedRegisters(ArrayList<String> calleeSavedRegisters) {
		String returnString = "";
		
		//Load back the callee saved registers
		for(int i = 0; i < calleeSavedRegisters.size(); i++) {
			String backupString = "local[" + String.valueOf(i) + "]";
			backupString = getIndentation(1) + calleeSavedRegisters.get(i) + " = " + backupString; 
			returnString = concatentateInstructions(returnString, backupString);
		}
		
		return returnString;
	}
	
	  
	
	public static String getIndentation(Integer indentation) {
		String ret = "";
		String indentationSpacing = "  ";
		for(int i = 0; i<indentation;i++) {
			ret += indentationSpacing;
		}
		return ret;
	}
	
	public static String concatentateInstructions(String v1, String v2, String...strings) {
		String concatentedString = v1 + "\n" + v2;
		for(String s:strings) {
		  concatentedString += "\n" + s;
	   }
		return concatentedString;
	}

	
}
