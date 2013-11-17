package visitor;
import syntaxtree.*;
import java.util.*;

public class VprintAssignRHS extends DepthFirstVisitor {
	public void visit(AssignmentStatement n) {
		System.out.println(n.toString());
		n.f2.accept(this);
		//TODO: implement VPrettyPrinter
	}
}
