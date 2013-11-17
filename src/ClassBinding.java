
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ClassBinding {
	private LinkedHashMap<String, VarType> fields;
	private LinkedHashMap<String,MethodBinding> methods;
	//For when this class extends another class, 
	public String parentClass;
	//numBytesToRepresent = 4 for method table pointer + 4*numFieldsDeclared + 4*numFieldsInherited.
	public int numBytesToRepresent;
	//All fields, including inherited ones
	private LinkedHashMap<String,VarType> allFields;
	//All methods, including inherited ones
	private LinkedHashMap<String,MethodBinding> allMethods;
	
	public ClassBinding() {
		fields = new LinkedHashMap<String,VarType>();
		methods = new LinkedHashMap<String,MethodBinding>();
		parentClass = "";
	}
	
	//Return method binding, considering super-classes if they exist
	public MethodBinding getMethodBinding(String id, HashMap<String,ClassBinding> symbolTable) {
		//Look through the hierarchy to find the binding
		MethodBinding m = methods.get(id);
		if(m == null && !this.parentClass.isEmpty()) {
				ClassBinding c = symbolTable.get(this.parentClass);
				m = c.getMethodBinding(id, symbolTable);
				return m;
		}
		
		return m;
	}
	
	//Return method binding, considering super-classes if they exist
	public VarType getField(String id, HashMap<String,ClassBinding> symbolTable) {
			//Look through the hierarchy to find the field, returning the first one found.
			VarType v = fields.get(id);
			if(v == null && !this.parentClass.isEmpty()) {
					ClassBinding c = symbolTable.get(this.parentClass);
					v= c.getField(id, symbolTable);
					return v;
			}
			
			return v;
	} 
	
	public boolean hasAncestor(String ancestorId, HashMap<String,ClassBinding> symbolTable) {
		//Is there an ancestor w/ the name ancestorId?
		if(parentClass.isEmpty())
			return false;
		
		if(parentClass.equals(ancestorId))
			return true;
		else {
			ClassBinding c = symbolTable.get(this.parentClass);
			return c.hasAncestor(ancestorId, symbolTable);
		}
		
	}
	
	public HashMap<String,MethodBinding> getMethodBindings() {
		return methods;
	}
	
	public HashMap<String,VarType> getFields() {
		return fields;
	}
	
	//Return a map of fields, taking into consideration shadowing.
	public LinkedHashMap<String,VarType> getAllFields(HashMap<String,ClassBinding> symbolTable, String prefix) {
		//Take all local fields and put into the map
		if(this.allFields != null)
			return this.allFields;
		this.allFields = new LinkedHashMap<String,VarType>();
		LinkedHashMap<String,VarType> fieldsDeclaredByClass = new LinkedHashMap<String,VarType>();
		
		//Iterate through and tack on the prefix
		for(Map.Entry<String, VarType> v:this.fields.entrySet()) {
					String key = v.getKey();
					fieldsDeclaredByClass.put(prefix+"."+key,v.getValue());
		}	
		
		if(!this.parentClass.isEmpty()) {
			//We inherit fields. 
			ClassBinding parentBinding = symbolTable.get(this.parentClass);
			HashMap<String,VarType> parentFields = parentBinding.getAllFields(symbolTable,parentClass);
			this.allFields.putAll(parentFields);
		}
		
		this.allFields.putAll(fieldsDeclaredByClass);
		return this.allFields;
	}
	
	//Return a map of methods, taking into consideration method overriding
	//Tack on prefix to the beginning of the method name
	public HashMap<String,MethodBinding> getAllMethods(HashMap<String,ClassBinding> symbolTable, String prefix) {
		//Take all local fields and put into the map
		if(this.allMethods != null)
			return this.allMethods;
		this.allMethods = new LinkedHashMap<String,MethodBinding>();
		LinkedHashMap<String,MethodBinding> methodsNotInheritedOrShadowed = new LinkedHashMap<String,MethodBinding>();
		
		//Iterate through and tack on the prefix
		for(Map.Entry<String, MethodBinding> v:this.methods.entrySet()) {
					String key = v.getKey();
					methodsNotInheritedOrShadowed.put(prefix+"."+key,v.getValue());
		}	
				
		if(!this.parentClass.isEmpty()) {
			//We inherit methods. Add methods that this class doesn't override
			ClassBinding parentBinding = symbolTable.get(this.parentClass);
			HashMap<String,MethodBinding> parentMethods = parentBinding.getAllMethods(symbolTable,parentClass);
			for(Map.Entry<String,MethodBinding> v:parentMethods.entrySet()) {
				String parentMethod = v.getKey();
				//Strip off the prefix
				String methodWithoutPrefix = parentMethod.split("\\.")[1];
				MethodBinding parentMethodBinding = v.getValue();
				MethodBinding currentMethodBinding = this.methods.get(methodWithoutPrefix);
				if(currentMethodBinding != null) {
					//We have overridden this method
					parentMethod = prefix + "." + methodWithoutPrefix;
					this.allMethods.put(parentMethod, currentMethodBinding);
					MethodBinding removedBinding = methodsNotInheritedOrShadowed.remove(parentMethod);
					assert(removedBinding != null);
				} else {
					//Inherit this from the parent
					this.allMethods.put(parentMethod, parentMethodBinding);
				}	
			}
		}	
		
		this.allMethods.putAll(methodsNotInheritedOrShadowed);
		return this.allMethods;
	}
	
}
