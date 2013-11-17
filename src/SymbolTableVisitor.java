

import java.util.HashMap;
import java.util.Map;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import syntaxtree.*;
import visitor.DepthFirstVisitor;

//Construct the symbol table

public class SymbolTableVisitor extends DepthFirstVisitor {
	   public HashMap<String, ClassBinding> table;
	   public ClassBinding currentClassBinding;
	   public MethodBinding currentMethodBinding;
	   public boolean tableSuccessfullyCreated;
	   
	   public SymbolTableVisitor() {
		   table = new HashMap<String,ClassBinding>();
		   currentClassBinding = null;
		   currentMethodBinding = null;
		   tableSuccessfullyCreated = true;
	   }
	   
	   /**
	    * Grammar production:
	    * f0 -> "class"
	    * f1 -> Identifier()
	    * f2 -> "{"
	    * f3 -> ( VarDeclaration() )*
	    * f4 -> ( MethodDeclaration() )*
	    * f5 -> "}"
	    */
	   public void visit(ClassDeclaration n) {
		   String identifier = n.f1.f0.toString();
		   currentClassBinding = new ClassBinding();
		   currentMethodBinding = null;
		   //Visit variable declarations
		   n.f3.accept(this);
		   //Visit method declarations
		   n.f4.accept(this);
		   
		   //Add the mapping
		   insertIntoMap(table,currentClassBinding, identifier);
		   
		   currentClassBinding = null;
		   currentMethodBinding = null;
	   }
	   
	   /**
	    * f0 -> "class"
	    * f1 -> Identifier()
	    * f2 -> "extends"
	    * f3 -> Identifier()
	    * f4 -> "{"
	    * f5 -> ( VarDeclaration() )*
	    * f6 -> ( MethodDeclaration() )*
	    * f7 -> "}"
	    */
	   public void visit(ClassExtendsDeclaration n) {
		   String identifier = n.f1.f0.toString();
		   currentClassBinding = new ClassBinding();
		   currentMethodBinding = null;
		   //Take note of parent class
		   currentClassBinding.parentClass = identifierForIdentifierNode(n.f3);
		   
		   //Visit variable declarations
		   n.f5.accept(this);
		   //Visit method declarations
		   n.f6.accept(this);
		   
		   //Add the mapping
		   insertIntoMap(table,currentClassBinding, identifier);
		   currentClassBinding = null;
		   currentMethodBinding = null;
	   }

	   
	   
	   /**
	    * Grammar production:
	    * f0 -> "public"
	    * f1 -> Type()
	    * f2 -> Identifier()
	    * f3 -> "("
	    * f4 -> ( FormalParameterList() )?
	    * f5 -> ")"
	    * f6 -> "{"
	    * f7 -> ( VarDeclaration() )*
	    * f8 -> ( Statement() )*
	    * f9 -> "return"
	    * f10 -> Expression()
	    * f11 -> ";"
	    * f12 -> "}"
	    */
	   public void visit(MethodDeclaration n) {
		   String identifier = n.f2.f0.toString();
		   currentMethodBinding = new MethodBinding();
		   HashMap<String, MethodBinding> map = currentClassBinding.getMethodBindings();
		   insertIntoMap(map,currentMethodBinding,identifier);
		     
		   //Get return type
		   VarType retType = new VarType(varTypeForTypeNode(n.f1));
		   if(retType.type == VariableType.CLASS ) {
			   assert(n.f1.f0.choice instanceof Identifier);
			   retType.className = identifierForIdentifierNode((Identifier)n.f1.f0.choice);
	   		}
		   currentMethodBinding.returnValue = retType;

		   //Visit the parameter list
		   n.f4.accept(this);
		   
		   //Visit the local variable declarations
		   n.f7.accept(this);
	   
		   currentMethodBinding = null;
	   }
	   
	   
	   /**
	    * f0 -> Type()
	    * f1 -> Identifier()
	    */
	   public void visit(FormalParameter n) {
		   if(currentMethodBinding == null) {
			   System.err.println("No current method defined for parameter list!");
			   System.exit(0);
		   } 
		   
		   VarType type = new VarType(varTypeForTypeNode(n.f0));
		   String identifier = identifierForIdentifierNode(n.f1);
		   if(type.type == VariableType.CLASS ) {
			   assert(n.f0.f0.choice instanceof Identifier);
			   type.className = identifierForIdentifierNode((Identifier)n.f0.f0.choice);
	   		}
			   
		   HashMap<String,VarType> map = currentMethodBinding.parameters;
		   insertIntoMap(map, type, identifier);
	   }
	   
	   
	   /**
	    * Grammar production:
	    * f0 -> "class"
	    * f1 -> Identifier()
	    * f2 -> "{"
	    * f3 -> "public"
	    * f4 -> "static"
	    * f5 -> "void"
	    * f6 -> "main"
	    * f7 -> "("
	    * f8 -> "String"
	    * f9 -> "["
	    * f10 -> "]"
	    * f11 -> Identifier()
	    * f12 -> ")"
	    * f13 -> "{"
	    * f14 -> ( VarDeclaration() )*
	    * f15 -> ( Statement() )*
	    * f16 -> "}"
	    * f17 -> "}"
	    */
	   public void visit(MainClass n) {
		   //Add class to symbol table
		   ClassBinding c = new ClassBinding();
		   MethodBinding m = new MethodBinding();
		   //The class name
		   String identifier = identifierForIdentifierNode(n.f1);
		   currentClassBinding = c;
		   currentMethodBinding = m;
		   
		   insertIntoMap(currentMethodBinding.parameters,new VarType(VariableType.STRING_ARRAY),identifierForIdentifierNode(n.f11));
		   
		  //Visit the variable declarations
		   n.f14.accept(this);
		   
		   //Add the mapping
		   insertIntoMap(table,currentClassBinding, identifier);
		   //Add the method mapping
		   insertIntoMap(currentClassBinding.getMethodBindings(),currentMethodBinding,"main");
		   //Don't care about return type
		   currentClassBinding = null;
		   currentMethodBinding = null;
	   }
	   
	   /**
	    * Grammar production:
	    * f0 -> Type()
	    * f1 -> Identifier()
	    * f2 -> ";"
	    */
	   public void visit(VarDeclaration n) {
		   	  //Get variable type and add it to the correct binding of the
		   	  //current class
			  String identifier = identifierForIdentifierNode(n.f1);
			  VariableType type = varTypeForTypeNode(n.f0);
		
			  HashMap<String,VarType> map;
			  
			  if(this.currentMethodBinding != null) {
				  //Var declaration for method in current class
				  map = currentMethodBinding.locals;
				  //Make sure hasn't been declared in the parameter list
				  for(Map.Entry<String, VarType> v:currentMethodBinding.parameters.entrySet()) {
					  if(identifier.equals(v.getKey())) {
						  System.err.println("Duplicate local var name as a method parameter!");
						  tableSuccessfullyCreated = false;
						  return;
					  }
				  }
			  } else {
				  //Var declaration for a field in the class
				  map = currentClassBinding.getFields();
			  }
			  
			  VarType v= new VarType(type);
			  if(v.type == VariableType.CLASS ) {
				   assert(n.f0.f0.choice instanceof Identifier);
				   v.className = identifierForIdentifierNode((Identifier)n.f0.f0.choice);
		   	  }
			  
			  insertIntoMap(map,v,identifier);
	   }
	   
	   
	   public static VariableType varTypeForTypeNode(Type n) {
		   VariableType ret = VariableType.INTEGER;
		   switch(n.f0.which) {
		   case 0: 
		   			ret = VariableType.INT_ARRAY;
		   			break;
		   case 1:  ret = VariableType.BOOLEAN;
		   			break;
		   case 2:  ret = VariableType.INTEGER;
		   			break;
		   case 3:  ret = VariableType.CLASS;
		   			break;
		   default:
			   		System.err.println("Cannot assign type!");
			   		System.exit(0);
		   }
		   
		   return ret;		  
	   }
	   
	   public static String identifierForIdentifierNode(Identifier n)
	   {
		   String identifier = n.f0.toString();
		   return identifier;
	   }
	   
	   public void insertIntoMap(HashMap<String,VarType> map, VarType t, String identifier) {
		   //Check to make sure var isn't already declared
			  if(map.get(identifier) != null) {
				  //Already a declaration!
				  System.err.println("Duplicate variable detected!");
				  tableSuccessfullyCreated = false;
			  } else {
				  map.put(identifier, t);
			  }
			  
			 
	   }
	   
	   public void insertIntoMap(HashMap<String,MethodBinding> map, MethodBinding b, String identifier) {
		   //Check to make sure var isn't already declared
			  if(map.get(identifier) != null) {
				  //Already a declaration!
				  System.err.println("Duplicate method detected!");
				  tableSuccessfullyCreated = false;
			  } else {
				  map.put(identifier, b);
			  }
	   }
	   

	   public void insertIntoMap(HashMap<String,ClassBinding> map, ClassBinding c, String identifier) {
		   //Check to make sure var isn't already declared
			  if(map.get(identifier) != null) {
				  //Already a declaration!
				  System.err.println("Duplicate class detected!");
				  tableSuccessfullyCreated = false;
			  } else {
				  map.put(identifier, c);
			  }
	   }
	   
}
