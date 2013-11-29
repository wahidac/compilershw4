import java.util.ArrayList;
import java.util.LinkedHashSet;

import cs132.vapor.ast.VFunction;


//Represent live ranges for some variable
public class LiveRanges {
	String varName;
	VFunction enclosingFunction;
	//Represent ranges as a list of a set of range tuples
	//so a live range of 1-->2-->3--->4-->5-->2 will
	//be represented like [{(1,2),(2,3),(3,4),(4,5),(5,2)}, ....]
	//We can figure out the longest range by seeing which set is largest.
	//Each set represents a contiguous liveness range
	ArrayList<LinkedHashSet<int[]>> ranges;
	
	public LiveRanges() {
		ranges = new ArrayList<LinkedHashSet<int[]>>();
	}
	
	public void addLiveRange(LinkedHashSet<int[]> liveRange) {
		this.ranges.add(new LinkedHashSet<int[]>(liveRange));
	}
	
	//Does this variable occupy a range between lineStart and lineEnd?
	//If Yes, then return true so indicate that the variable for which
	//we're checking whether a conflict exists cannot simultaneously be
	//in the same register as this variable
	public boolean rangeConflict(int lineStart, int lineEnd) {
		int []edge = {lineStart, lineEnd};
		for(LinkedHashSet<int[]> range:ranges) {
			if(range.contains(edge)) {
				return true;
			}
		}
		return false;
	}
}
