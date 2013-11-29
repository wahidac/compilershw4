import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;

import cs132.util.ProblemException;
import cs132.vapor.parser.VaporParser;
import cs132.vapor.ast.VDataSegment;
import cs132.vapor.ast.VFunction;
import cs132.vapor.ast.VInstr;
import cs132.vapor.ast.VOperand;
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
				  String indentation = "  ";
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
			  
			  
			 /* //Go through functions now.
			  String functions = "";
			  SpillAllFieldsVisitor spillFieldsVisitor = new SpillAllFieldsVisitor();
			  for(VFunction func:program.functions) {
				  //Function declaration
				  String currentFunction = "func " + func.ident;
				  
				  int stackSize = func.vars.length;
				  //Allocate mem for local based on how many local variables appear
				  currentFunction += " [in 0, out 0, local " + String.valueOf(stackSize) + "]";
				  for(VInstr instruction:func.body) {
					  //For each instruction, run our spill visitor to generate code that should substitute what is in input file
					  //  functions += 
					  try {
						String vaporMCode = instruction.accept(1,spillFieldsVisitor);
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					  
				  }

			  }
			  
			  System.out.println(dataSection);
			  */
			  CalcLiveInLiveOutSets liveInLiveOutCalculator =  new CalcLiveInLiveOutSets(initCFGVisitor.CFGs, initCFGVisitor.instructionsToCFGNode, program);
			  CalcLiveRanges rangeCalculator = new CalcLiveRanges(liveInLiveOutCalculator.CFGs, initCFGVisitor.instructionsToCFGNode, program);
			  HashMap<String,HashMap<String,LiveRanges>> ranges = rangeCalculator.liveRanges;
			  //initCFGVisitor.printCFG();
			  //rangeCalculator.printLiveRanges();
			  RegisterAllocator regAllocater = new RegisterAllocator(ranges);
		
			  
			  return program;
			}
	
	public static String getIndentation(Integer indentation) {
		String ret = "";
		String indentationSpacing = " ";
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
