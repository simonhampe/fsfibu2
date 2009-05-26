package fs.fibu2.data.model;

import fs.fibu2.data.format.EntryComparator;
import fs.fibu2.lang.Fsfibu2StringTableMgr;

/**
 * A linked separator is a separator which is associated to a certain entry. All entries which come before it (as by 
 * {@link EntryComparator}, come before the separator, all others after it. If the linked entry comes before or after the reading point is specified
 * when creating the separator
 * @author Simon Hampe
 *
 */
public class LinkedSeparator implements EntrySeparator {

	private Entry linkedEntry;
	private boolean entryComesBefore = true;
	
	private EntryComparator comparator = new EntryComparator(false);
	
	/**
	 * Creates a linked separator
	 * @param linkedEntry The entry to which it is linked
	 * @param entryComesBefore Whether the linked entry comes before this separator (if not, it comes directly after it)
	 */
	public LinkedSeparator(Entry linkedEntry, boolean entryComesBefore) {
		this.linkedEntry = linkedEntry;
		this.entryComesBefore = entryComesBefore;
	}
	
	@Override
	public String getName() {
		return Fsfibu2StringTableMgr.getString("fs.fibu2.LinkedSeparator.name");
	}

	@Override
	public boolean isLessOrEqualThanMe(Entry e) {
		if(linkedEntry == null) return false;
		if(e == null) return true;
		
		if(e == linkedEntry) return entryComesBefore;
		
		return comparator.compare(e, linkedEntry) < 0;
	}
}
