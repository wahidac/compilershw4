

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import syntaxtree.*;
import visitor.GJDepthFirst;

//Visitor to return the type and expression would return.
//Return null if an expression will not type check. 
public class TypeCalculator extends GJDepthFirst<VarType, HashMap<String, ClassBinding>>  {
	public String currentClass;
	public ClassBinding currentClassBinding;
	public MethodBinding currentMethodBinding;	
	
	/**
	    * f0 -> AndExpression()
	    *       | CompareExpression()
	    *       | PlusExpression()
	    *       | MinusExpression()
	    *       | TimesExpression()
	    *       | ArrayLookup()
	    *       | ArrayLength()
	    *       | MessageSend()
	    *       | PrimaryExpression()
	    */
	   public VarType visit(Expression n, HashMap<String, ClassBinding> symbolTable) {
	      return n.f0.accept(this, symbolTable);
	    
	   }
	   
	   /**
	    * f0 -> PrimaryExpression()
	    * f1 -> "&&"
	    * f2 -> PrimaryExpression()
	    */
	   public VarType visit(AndExpression n, HashMap<String, ClassBinding> symbolTable) {
		   	  VarType b1 = n.f0.accept(this, symbolTable);
		   	  VarType b2 = n.f2.accept(this, symbolTable);
		   	  if(b1 == null || b2 == null)
		   		  return null;
		   	  if(b1.type == VariableType.BOOLEAN && b2.type == VariableType.BOOLEAN)
		   		  return new VarType(VariableType.BOOLEAN);
		   	  else
		   		  return null;
		}
	   
	   
	   /**
	    * f0 -> PrimaryExpression()
	    * f1 -> "<"
	    * f2 -> PrimaryExpression()
	    */
	   public VarType visit(CompareExpression n, HashMap<String, ClassBinding> symbolTable) {
		   	  VarType b1 = n.f0.accept(this, symbolTable);
		   	  VarType b2 = n.f2.accept(this, symbolTable);
		   	  if(b1 == null || b2 == null)
		   		  return null;
		   	  if(b1.type == VariableType.INTEGER && b2.type == VariableType.INTEGER)
		   		  return new VarType(VariableType.BOOLEAN);
		   	  else
		   		  return null;
		}
		   
		   	   
	   /**
	    * f0 -> PrimaryExpression()
	    * f1 -> "+"
	    * f2 -> PrimaryExpression()
	    */
	   public VarType visit(PlusExpression n, HashMap<String, ClassBinding> symbolTable) {
		   	  VarType b1 = n.f0.accept(this, symbolTable);
		   	  VarType b2 = n.f2.accept(this, symbolTable);
		   	  if(b1 == null || b2 == null)
		   		  return null;
		   	  if(b1.type == VariableType.INTEGER && b2.type == VariableType.INTEGER)
		   		  return new VarType(VariableType.INTEGER);
		   	  else
		   		  return null;
		}

	   
	   /**
	    * f0 -> PrimaryExpression()
	    * f1 -> "-"
	    * f2 -> PrimaryExpression()
	    */
	   public VarType visit(MinusExpression n, HashMap<String, ClassBinding> symbolTable) {
		   	  VarType b1 = n.f0.accept(this, symbolTable);
		   	  VarType b2 = n.f2.accept(this, symbolTable);
		   	  if(b1 == null || b2 == null)
		   		  return null;
		   	  if(b1.type == VariableType.INTEGER && b2.type == VariableType.INTEGER)
		   		  return new VarType(VariableType.INTEGER);
		   	  else
		   		  return null;
		}
	   
	   /**
	    * f0 -> PrimaryExpression()
	    * f1 -> "*"
	    * f2 -> PrimaryExpression()
	    */
	   public VarType visit(TimesExpression n, HashMap<String, ClassBinding> symbolTable) {
		   	  VarType b1 = n.f0.accept(this, symbolTable);
		   	  VarType b2 = n.f2.accept(this, symbolTable);
		   	  if(b1 == null || b2 == null)
		   		  return null;
		   	  if(b1.type == VariableType.INTEGER && b2.type == VariableType.INTEGER)
		   		  return new VarType(VariableType.INTEGER);
		   	  else
		   		  return null;
		}
	   
	   /**
	    * f0 -> PrimaryExpression()
	    * f1 -> "["
	    * f2 -> PrimaryExpression()
	    * f3 -> "]"
	    */
	   public VarType visit(ArrayLookup n, HashMap<String, ClassBinding> symbolTable) {
		   	  VarType b1 = n.f0.accept(this, symbolTable);
		   	  VarType b2 = n.f2.accept(this, symbolTable);
		   	  if(b1 == null || b2 == null)
		   		  return null;
		   	  if(b1.type == VariableType.INT_ARRAY && b2.type == VariableType.INTEGER)
		   		  return new VarType(VariableType.INTEGER);
		   	  else
		   		  return null;
		}
	   
	   /**
	    * f0 -> PrimaryExpression()
	    * f1 -> "."
	    * f2 -> "length"
	    */
	   
	   public VarType visit(ArrayLength n, HashMap<String, ClassBinding> symbolTable) {
		   	  VarType b1 = n.f0.accept(this, symbolTable);
		   	  if(b1 == null)
		   		  return null;
		   	  if(b1.type == VariableType.INT_ARRAY)
		   		  return new VarType(VariableType.INTEGER);
		   	  else
		   		  return null;
		}
	   
	
	   /**
	    * f0 -> PrimaryExpression()
	    * f1 -> "."
	    * f2 -> Identifier()
	    * f3 -> "("
	    * f4 -> ( ExpressionList() )?
	    * f5 -> ")"
	    */
	   public VarType visit(MessageSend n, HashMap<String, ClassBinding> symbolTable) {
		   	 //f0 must be a class.
		   	  VarType object = n.f0.accept(this,symbolTable);
		   	  
		   	  if(object == null || object.type != VariableType.CLASS)
		   		  return null;
		   	  
		   	  //f2 must be a method in the class given by f0, verify this
		   	  //Return class designated by f0
		   	  String identifier = object.className;
		   	  assert(!identifier.isEmpty());
		   	  ClassBinding c = symbolTable.get(identifier);
		   	  assert(c != null);
		   	  
		   	  String method = SymbolTableVisitor.identifierForIdentifierNode(n.f2);
		   	  MethodBinding m = c.getMethodBinding(method, symbolTable);
		   	  if(m == null) {
		   		  //No method!!!
		   		  return null;
		   	  }
		   	  
		   	  VarType returnType = m.returnValue;
		   	  
		   	  //Make sure everything in the expression list type checks w/ what is expected as input
		   	  //to the method m
		   	  if(n.f4.present()) {
		   		  if(!validateExpressionListInput(m,n.f4.node,symbolTable))
		   			  return null;
		   	  } else if(m.parameters.size() > 0) {
		   		  return null;
		   	  }
		   	  
		   	  return returnType;
		}
	   
       public boolean validateExpressionListInput(MethodBinding m, Node node, HashMap<String, ClassBinding> symbolTable) {
    	   assert(node instanceof ExpressionList);
    	   ExpressionList n = (ExpressionList) node;
		   Expression currentExpression = n.f0;
		   boolean legitInput = true;
		   NodeListOptional nodeList = n.f1;
		   Vector<Node> expressions = nodeList.nodes;
		   Iterator<Node> itr = expressions.iterator();
		   LinkedHashMap<String, VarType> params = m.parameters;
		   
		   if(params.size() != expressions.size() + 1)
			   return false;
		   
		   for(Map.Entry<String, VarType> v:params.entrySet()) {
			   VarType p = v.getValue();
			   //Make sure expression type checks to the right type
			   VarType exprTypr = currentExpression.accept(this, symbolTable);
			   if(!VarType.canAssignVarType(p, exprTypr,symbolTable)) {
				   legitInput = false;
				   break;
			   }
			   
			   //Get next expression
			   if(itr.hasNext()) {
				   ExpressionRest r = (ExpressionRest)itr.next();
			   	   currentExpression = r.f1;
			   }
		
		   }
		   
		   return legitInput;
		    
	   }
	  
	    
	   
	   
	   /**
	    * f0 -> IntegerLiteral()
	    *       | TrueLiteral()
	    *       | FalseLiteral()
	    *       | Identifier()
	    *       | ThisExpression()
	    *       | ArrayAllocationExpression()
	    *       | AllocationExpression()
	    *       | NotExpression()
	    *       | BracketExpression()
	    */
	   public VarType visit(PrimaryExpression n, HashMap<String, ClassBinding> symbolTable) {
		   return n.f0.choice.accept(this, symbolTable);
	   }
	   
	   /**
	    * f0 -> <INTEGER_LITERAL>
	    */
	   public VarType visit(IntegerLiteral n, HashMap<String, ClassBinding> symbolTable) {
		   return new VarType(VariableType.INTEGER);
	   }
	   
	   /**
	    * f0 -> "true"
	    */
	   public VarType visit(TrueLiteral n, HashMap<String, ClassBinding> symbolTable) {
		   return new VarType(VariableType.BOOLEAN);
	   }
	   
	   
	   /**
	    * f0 -> "false"
	    */
	   public VarType visit(FalseLiteral n, HashMap<String, ClassBinding> symbolTable) {
		   return new VarType(VariableType.BOOLEAN);
	   }
	   
	  	   
	   /**
	    * f0 -> <IDENTIFIER>
	    */
	   //Return type of a variable.
	   public VarType visit(Identifier n, HashMap<String, ClassBinding> symbolTable) {
		   //Need to consult symbol table for type. 
		   VarType v = null;
		   //Check local variable declarations, which take priority over class declarations.
		   //If we find the mapping in the method dec, don't look at the class fields.
		   //Use to visit variable names declared in the var declaration portions
		   String id = SymbolTableVisitor.identifierForIdentifierNode(n);
		   v = currentMethodBinding.locals.get(id);
		   if(v == null) {
			   v = currentMethodBinding.parameters.get(id);		
		   }
		   if(v == null) {
			   v = currentClassBinding.getField(id, symbolTable);
		   }
		   
		   return v;
	   }
	   
	   
	   /**
	    * f0 -> "this"
	    */
	   public VarType visit(ThisExpression n, HashMap<String, ClassBinding> symbolTable) {
		   //this refers to whatever class the current class binding refers to.
		   assert(currentClass != null && !currentClass.isEmpty());
		   VarType v = new VarType(VariableType.CLASS,currentClass);
		   return v;
	   }
	   
	   /**
	    * f0 -> "new"
	    * f1 -> "int"
	    * f2 -> "["
	    * f3 -> Expression()
	    * f4 -> "]"
	    */
	   public VarType visit(ArrayAllocationExpression n, HashMap<String, ClassBinding> symbolTable) {
		   //this refers to whatever class the current class binding refers to.
		   VarType v = n.f3.accept(this,symbolTable);
		   if(v == null)
		   	   return null;
		   if(v.type == VariableType.INTEGER)
			   return new VarType(VariableType.INT_ARRAY);
		   else 
			   return null;
	   }
	   
	   /**
	    * f0 -> "new"
	    * f1 -> Identifier()
	    * f2 -> "("
	    * f3 -> ")"
	    */
	   
	   public VarType visit(AllocationExpression n, HashMap<String, ClassBinding> symbolTable) {
		   String id = SymbolTableVisitor.identifierForIdentifierNode(n.f1);
		   ClassBinding c = symbolTable.get(id);
		   if(c == null)
			   return null;
		   else
		   {
			   VarType v = new VarType(VariableType.CLASS,id);
			   return v;
		   }
		  
	   }
	   
	   /**
	    * f0 -> "!"
	    * f1 -> Expression()
	    */
	   public VarType visit(NotExpression n, HashMap<String, ClassBinding> symbolTable) {
		   //this refers to whatever class the current class binding refers to.
		   VarType v = n.f1.accept(this,symbolTable);
		   if(v == null)
			   return null;
		   if(v.type == VariableType.BOOLEAN)
			   return v;
		   else 
			   return null;
	   }
	   
	   /**
	    * f0 -> "("
	    * f1 -> Expression()
	    * f2 -> ")"
	    */
	   public VarType visit(BracketExpression n, HashMap<String, ClassBinding> symbolTable) {
		   //this refers to whatever class the current class binding refers to.
		   return n.f1.accept(this,symbolTable);
	   }
	
}
