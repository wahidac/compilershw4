import java.io.FileInputStream;
import java.io.FileNotFoundException;

import syntaxtree.Node;


public class J2V {
   public static void main(String [] args) {
      try {
    	 Node root;
    	 //if(args.length == 1) {
    	//	FileInputStream stream = new FileInputStream(args[0]); 
    	//	root = new MiniJavaParser(stream).Goal();
    	// } else {
            root = new MiniJavaParser(System.in).Goal();
    	// }
    	 
         SymbolTableVisitor symVisitor = new SymbolTableVisitor();
         //Create the symbol table
         root.accept(symVisitor);
         JumpTable j = new JumpTable(symVisitor.table);
         //Now implement the functions
         VisitFunctionDefinitions functionDefinitionVisitor = new VisitFunctionDefinitions(symVisitor.table, j);
         System.out.println(root.accept(functionDefinitionVisitor,0));
      }
      catch (ParseException e) {
         System.out.println(e.toString());
      } /*catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
   }
}
