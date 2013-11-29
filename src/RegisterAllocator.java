import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.text.html.HTMLDocument.Iterator;


//Allocate registers 
public class RegisterAllocator {
      //All of the live ranges
	  HashMap<String,HashMap<String,LiveRanges>> ranges;
	  //Map var names to register names or "spilled".
	  //When printing out final vapor program, replace all instances
	  //of a var w/ register we've assigned to it. Else, if 
	  //spilled, use one of the spill registers to the load it
	  //from memory whenever it is used. Will need to take into account
	  //whether it is callee-saved/caller saved. If callee-saved, 
	  //back-up val before writing to the register. if caller, 
	  //check live out to see whether necessary to back-up
	  HashMap<String,HashMap<String,String>> registerAssignments;
	  HashMap<String, HashSet<String>> spilledVariables;
	  
	  String [] spillRegisters;
	  String [] calleeSaved;
	  String [] callerSaved;
	  String [] arguments;
	  
	  public RegisterAllocator(HashMap<String,HashMap<String,LiveRanges>> ranges) {
		  	this.ranges = ranges;
		  	spilledVariables = new HashMap<String, HashSet<String>>();
		  	registerAssignments = new HashMap<String,HashMap<String,String>>();
		  
		    //Because only two operand operators in Vapor
			spillRegisters = new String[3];
			calleeSaved = new String[8];
			//Reserve register $t1 to use for spilling 
			callerSaved = new String[8];
			arguments = new String[4];
			
			String prefix = "$s";
			for(int i = 0; i < calleeSaved.length; i++) {
				calleeSaved[i] = prefix + String.valueOf(i);
			}
			prefix = "$t";
			for(int i = 0; i < callerSaved.length; i++) {
				callerSaved[i] = prefix + String.valueOf(i+1);
			}
			
			prefix = "$a";
			for(int i = 0; i < arguments.length; i++) {
				arguments[i] = prefix + String.valueOf(i);
			}
			
			spillRegisters[0] = "$v0";
			spillRegisters[1] = "$v1";
			spillRegisters[2] = "t0";
			
			
			//Assign registers
			for(Entry<String, HashMap<String,LiveRanges>> entry:ranges.entrySet()) {
				HashMap<String,LiveRanges> r = entry.getValue();
				HashSet<String> spilledVariables = new HashSet<String>();
				System.out.println("Allocation for function " + entry.getKey());
				assignRegisters(r, spilledVariables);
				this.spilledVariables.put(entry.getKey(), spilledVariables);
			}
	  }
	  
	  public void assignRegisters(HashMap<String,LiveRanges> r, HashSet<String> spilledVariables) {
		  LinkedHashSet<String> freeRegisterPool = new LinkedHashSet<String>();
		  HashMap<String,String> assignedRegisters = new HashMap<String,String>();
		  
		  for(String s:callerSaved) {
			  freeRegisterPool.add(s);
		  }
		  for(String s:calleeSaved) {
			  freeRegisterPool.add(s);
		  }
		  
	
		  for(Entry<String, LiveRanges> entry:r.entrySet()) {
			  java.util.Iterator<String> itr = freeRegisterPool.iterator();
			  String var = entry.getKey();
			  //Assign a free register to the variable.  If we are
			  //out of free registers, try to find a variable that has
			  //one of the free registers for which we have no
			  //liveness conflict. else, need to spill largest range
			  //and redo the algorithm
			  if(itr.hasNext()) {
				  String freeReg = itr.next();
				  assignedRegisters.put(var, freeReg);
				  freeRegisterPool.remove(freeReg);
				  System.out.println("Assigning free reg " + freeReg + " to " + var);
			  } else {
				  //Out of free registers. Try to find a variable for which we have
				  //no liveness conflicts
				  boolean foundRegister = false;
				  for(Entry<String, LiveRanges> e:r.entrySet()) {
					  String assignedVar = e.getKey();
					  String assignedReg = assignedRegisters.get(assignedVar);
					  if(assignedReg != null) {
						  LiveRanges rangesOfAssignedVar = r.get(assignedVar);
						  LiveRanges unassignedVarRange = r.get(var);
						  boolean conflict = rangesOfAssignedVar.rangeConflict(unassignedVarRange);
						  CalcLiveRanges.printLiveRanges(assignedVar, rangesOfAssignedVar);
						  CalcLiveRanges.printLiveRanges(var, unassignedVarRange);
						  
						  if(!conflict) {
						
							  //Use this register!
							  foundRegister = true;
							  assignedRegisters.put(var, assignedReg);
							  System.out.println("Assigning assigned reg " + assignedReg + " to " + var);
							  break;
						  }
					  }
					  
				  }
				  
				  if(!foundRegister) {
					  System.out.println("Spilling " + var + " and repeating algorithn");
					  //Spill the largest. Heuristic = minimize num registers spilled
					  int maxSize = 0;
					  String maxVar = "";
					  for(Entry<String, LiveRanges> e:r.entrySet()) {
				          LiveRanges l = e.getValue();
						  int size = l.ranges.size();
						  
						  if(maxVar.isEmpty() || (size > maxSize) ) {
							  maxVar = e.getKey();
							  maxSize = size;
						  }
					  }
					  
					  //Spill max var
					  spilledVariables.add(maxVar);
					  //No longer need to consider live ranges for this var
					  r.remove(maxVar);
					  assignRegisters(r, spilledVariables);
					  return;
				  }
			  }
			  
		  }
		  
	  }

}
