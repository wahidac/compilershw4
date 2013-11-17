
package visitor;
import syntaxtree.*;
import syntaxtree.Type;

import java.lang.reflect.*;
import java.util.*;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

/* 	Return a string that described the AST below the root node.
 * For each level, indent.
 */
public class PrintSubtreeVisitor implements GJVisitor<String,Integer> {
	
 public String printSubtree(Node n, Integer indentation) {	 
	   String subtree = "";
	   int indent = indentation.intValue();
	   //Iterate through node fields. If a field starts with f, 
	   //assume it is a child node in the AST. 	   
	   Class c = n.getClass();
	   System.out.println("Examining node: " + c.getName());
	   Field[] fields = c.getFields();	  
	   for(Field f:fields) {
		   String varName = f.getName();
		   if(varName.matches("f[0-9]+")) {
			   //Traverse the subtree!
			try {
				Node node = (Node) f.get(n);
				subtree += node.accept(this,indent + 1);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		   }
	   }
	   return subtree;
}
 
 //
 // Auto class visitors--probably don't need to be overridden.
 //
 public String visit(NodeList n, Integer argu) {
    String subtree = "";
    int _count=0;
    for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
       subtree += e.nextElement().accept(this,argu);
       _count++;
    }
    return subtree;
 }

 public String visit(NodeListOptional n, Integer argu) {
    if ( n.present() ) {
       String subtree = "";
       int _count=0;
       for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
          subtree += e.nextElement().accept(this,argu);
          _count++;
       }
       return subtree;
    }
    else
       return "";
 }

 public String visit(NodeOptional n, Integer argu) {
    if ( n.present() )
       return n.node.accept(this,argu);
    else
       return "";
 }

 public String visit(NodeSequence n, Integer argu) {
    String subtree = "";
    int _count=0;
    for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
       subtree += e.nextElement().accept(this,argu);
       _count++;
    }
    return subtree;
 }

 public String visit(NodeToken n, Integer argu) {
	  System.out.println("Examining node: " + n.getClass().getName());
	  String indentStr = "->->";
	  String returnString = "";
	   for(int i = 0; i < argu.intValue(); i++) {
		   returnString += indentStr;
	 }
	 return returnString + " " +  n.tokenImage + "\n" ;
	 //return n.tokenImage ;
	 
 }

 //
 // User-generated visitor methods below
 //

@Override
public String visit(Goal n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(MainClass n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(TypeDeclaration n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(ClassDeclaration n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(ClassExtendsDeclaration n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(VarDeclaration n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(MethodDeclaration n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(FormalParameterList n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(FormalParameter n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(FormalParameterRest n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(Type n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(ArrayType n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(BooleanType n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(IntegerType n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(Statement n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(Block n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(AssignmentStatement n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(ArrayAssignmentStatement n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(IfStatement n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(WhileStatement n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(PrintStatement n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(Expression n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(AndExpression n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(CompareExpression n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(PlusExpression n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(MinusExpression n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(TimesExpression n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(ArrayLookup n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(ArrayLength n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(MessageSend n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(ExpressionList n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(ExpressionRest n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(PrimaryExpression n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(IntegerLiteral n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(TrueLiteral n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(FalseLiteral n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(Identifier n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(ThisExpression n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(ArrayAllocationExpression n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(AllocationExpression n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(NotExpression n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}

@Override
public String visit(BracketExpression n, Integer argu) {
	// TODO Auto-generated method stub
	return printSubtree(n, argu);
}



}