
import java.util.HashMap;
import java.util.Map;

//Given a symbol table as input, create
//a jump table that can be used to 
//reference methods declared by classes.
//This table will be returned as a string
//that should be placed at the very top of the
//vapor file (a list of const references to
//code labels).

public class JumpTable {
	private HashMap<String,ClassBinding> symbolTable;
	//Need to track where in jumptable a method is
	public HashMap<String,HashMap<String,Integer>> methodIndexInJumpTable;
	public HashMap<String, HashMap<String,Integer>> fieldOffsets;
	String vaporJumpTable; //String representation of the table
	
	public JumpTable(HashMap<String,ClassBinding> symbolTable) {
		this.symbolTable = symbolTable;
		this.methodIndexInJumpTable = new HashMap<String,HashMap<String,Integer>>();
		this.fieldOffsets = new HashMap<String, HashMap<String,Integer>>();
		vaporJumpTable = "";
		populateBindingsWithSizes();
		createJumpTable();
		createFieldMappings();
	}
	
	private void populateBindingsWithSizes() {
		//Go through bindings and add sizing info so we know how much
		//memory to allocate when we hit the new operator
		for(Map.Entry<String, ClassBinding> c:symbolTable.entrySet()) {
			String className = c.getKey();
			ClassBinding binding = c.getValue();
			int numFields = binding.getAllFields(this.symbolTable,className).size();
			binding.numBytesToRepresent = 4 + 4*numFields;
		}
	}
	
	private void createJumpTable() {
		for(Map.Entry<String, ClassBinding> c:symbolTable.entrySet()) {
			String className = c.getKey();
			ClassBinding binding = c.getValue();
			if(binding.getMethodBindings().size() == 1 &&
				binding.getMethodBinding("main", symbolTable) != null) {
					//Main function
					continue;
				}
			HashMap<String,Integer> methodOffsets = new HashMap<String,Integer>();
			methodIndexInJumpTable.put(className, methodOffsets);
			//Create the jump table for this class
			String table = "const methods_"+className;
			//Iterate through methods.
			int i = 0;
			for(Map.Entry<String, MethodBinding> m:binding.getAllMethods(symbolTable,className).entrySet()) {
				String methodName = m.getKey().split("\\.")[1];
				methodOffsets.put(methodName, 4*i);
				String entry = ":" + m.getKey();
				table += "\n  " + entry;
				i++;
			}
			vaporJumpTable += "\n\n" + table;
		}
	}
	
	//Create mapping of field names to offsets
	public void createFieldMappings() {
		for(Map.Entry<String, ClassBinding> c:symbolTable.entrySet()) {
			String className = c.getKey();
			ClassBinding binding = c.getValue();
		
			HashMap<String,Integer> fieldOffset = new HashMap<String,Integer>();
			fieldOffsets.put(className, fieldOffset);
			//Iterate through fields. put them all in field offsets, overriding offset locations
			//of fields that are shadowed
			int i = 0;
			//System.out.println("Num fields:" + binding.getAllFields(symbolTable, className).size());
			//System.out.println("Class " + className);
			for(Map.Entry<String, VarType> f:binding.getAllFields(symbolTable,className).entrySet()) {
				String fieldName = f.getKey().split("\\.")[1];
				//System.out.println(f.getKey() + ":" +  4*i + " VarType: " + f.getValue().type);
				fieldOffset.put(fieldName, 4+4*i);
				i++;
			}
			//System.out.println("\nOffset Table:");
			//for(Map.Entry<String, Integer> f:fieldOffset.entrySet()) {
				//System.out.println(f.getKey() + ":" +  f.getValue());
			//}
			
			
			//System.out.println("\n");
		}
	}
	
	
	
}
