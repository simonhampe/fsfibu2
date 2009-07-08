package fs.fibu2.view.model;

import java.util.Arrays;
import java.util.Comparator;

import fs.fibu2.data.format.EntryComparator;
import fs.fibu2.data.format.EntryDateComparator;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.EntrySeparator;
import fs.fibu2.data.model.ExtremeSeparator;
import fs.fibu2.data.model.ReadingPoint;

/**
 * An instance of this class compares objects which are displayed in a journal table, i.e.
 * {@link Entry} and {@link EntrySeparator} objects. If objects of any other type are compared it throws an {@link IllegalArgumentException}.
 * The null object is smaller than any other non-null object
 */
public class TableModelComparator implements Comparator<Object> {

	private EntryComparator entryComparator = new EntryComparator(false);
	private EntryDateComparator entryDateComparator = new EntryDateComparator();
	
	@Override
	public int compare(Object o1, Object o2) {
		if(o1 == null && o2 == null) return 0;
		if(o1 == null) return -1;
		if(o2 == null) return 1;
		
		//First try all cases where both are of same type
		
		if(o1 instanceof Entry && o2 instanceof Entry) {
			return entryComparator.compare((Entry)o1, (Entry)o2); 
		}
		
		if(o1 instanceof ReadingPoint && o2 instanceof ReadingPoint) {
			return entryDateComparator.compare(((ReadingPoint)o1).getReadingDay(), ((ReadingPoint)o2).getReadingDay());
		}
		
		if(o1 instanceof ExtremeSeparator && o2 instanceof ExtremeSeparator) {
			if(((ExtremeSeparator)o1).isBeforeAll()) {
				return ((ExtremeSeparator)o2).isBeforeAll()? 0 : -1;
			}
			else return ((ExtremeSeparator)o2).isBeforeAll()? 1 : 0;
		}
		
		//Now try heterogeneous pairings
		for(Object p1 : Arrays.asList(o1,o2)) {
			Object p2 = (p1 == o1) ? o2 : o1;
			int factor = (p1 == o1)? 1 : -1;
			
			if(p1 instanceof Entry && p2 instanceof EntrySeparator) {
				return factor * (((EntrySeparator)p2).isLessOrEqualThanMe(((Entry)p1)) ? -1 : 1);
			}
			
			if(p1 instanceof ExtremeSeparator) return factor * (((ExtremeSeparator)p1).isBeforeAll()? -1 : 1);
			
		}
		
		//If we arrive here, this is not a valid call
		throw new IllegalArgumentException("Cannot compare: Only Entry and EntrySeparator types are allowed.");
	}
	
}
