
import java.util.ArrayList;
import java.util.HashMap;


public class VarType {
	public VariableType type;
	//Class name if there is one
	public String className;
	
	public VarType(VariableType type) {
		this.type = type;
	}
	
	public VarType(VariableType type, String className) {
		this.type = type;
		this.className = className;
	}
	
	//Check to see whether legal to assign a var of type v2 to one of v1
    public static boolean canAssignVarType(VarType v1, VarType v2, HashMap<String,ClassBinding> symbolTable) {
    	 boolean ret = false;
    	 
    	 if(v1 == null || v2 == null)
    		 return false;
    	 
    	 
    	 if(v1.type == VariableType.CLASS && v2.type == VariableType.CLASS) {
    		 //Return if v1 is equal to v2 or if v1 is a subclass of v2
    		 if(v1.className.equals(v2.className))
    			 ret = true;
    		 else {
    			 //Check parents if any. Return true if v1 as an ancestor of  v2 
    			 ClassBinding c = symbolTable.get(v2.className);
    			 ret = c.hasAncestor(v1.className, symbolTable);
    		 }

	      } else {
	    	  ret = (v1.type == v2.type);
	      }
    	 return ret;
		
	}
		
}
