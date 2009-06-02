package fs.fibu2.view.model;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Vector;

import fs.fibu2.data.event.JournalAdapter;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.data.model.ReadingPoint;
import fs.fibu2.view.event.YearSeparatorListener;

/**
 * This class keeps track of the default year separators necessary for a journal. There is at most one instance for each journal object. Listeners
 * are notified of added and removed separators. A year separator (which is nothing but a {@link ReadingPoint} for the 31.12. of a certain year) is 
 * considered necessary, when there is at least one entry before this separator and one afterwards. In particular, there might be separators for 
 * 'empty years'.
 * @author Simon Hampe
 *
 */
public class YearSeparators extends JournalAdapter {

	//A map of existing instances
	private static HashMap<Journal, YearSeparators> separatorsMap =  new HashMap<Journal, YearSeparators>();
	
	//A map of years to number of entries in that year
	private HashMap<Integer, Integer> yearEntryMap = new HashMap<Integer, Integer>();
	//An ordered list of years actually used
	private Vector<Integer> usedYears = new Vector<Integer>();
	//An ordered list of separators actually necessary ( = all years which are >= first one and < last one in the above list)
	private Vector<ReadingPoint> necessaryYears = new Vector<ReadingPoint>();
	//The associated journal
	private Journal associatedJournal;
	
	//A list of listeners
	private HashSet<YearSeparatorListener> listeners = new HashSet<YearSeparatorListener>();
	
	// CONSTRUCTION ******************************
	// *******************************************
	
	/**
	 * Creates a year separator, registers to j as a listener and calculates all necessary data
	 */
	private YearSeparators(Journal j) {
		associatedJournal = j == null? new Journal() : j;
		calculateAllData();
		j.addJournalListener(this);
	}
	
	/**
	 * Recalculates the complete entry data
	 */
	protected void calculateAllData() {
		yearEntryMap = new HashMap<Integer, Integer>();
		necessaryYears = new Vector<ReadingPoint>();
		for(Entry e : associatedJournal.getEntries()) {
			Integer entryYear = e.getDate().get(GregorianCalendar.YEAR);
			Integer noOfEntries = yearEntryMap.get(entryYear);
			if(noOfEntries == null) {
				yearEntryMap.put(entryYear, 1);
			}
			else yearEntryMap.put(entryYear, noOfEntries + 1);
		}
		usedYears = new Vector<Integer>(new TreeSet<Integer>(yearEntryMap.keySet()));
		//All years which are below the year at the last position induce a necessary separator
		if(usedYears.size() > 1) {
			for(int y = usedYears.get(0); y < usedYears.get(usedYears.size()-1); y++) {
				necessaryYears.add(new ReadingPoint(Integer.toString(y),new GregorianCalendar(y,11,31)));
			}
		}
	}
	
	/**
	 * Creates an instance for j, if necessary and returns it
	 * @return The instance associated to j (null, if j == null)
	 */
	public static YearSeparators getInstance(Journal j) {
		if(j == null) return null;
		YearSeparators sep = separatorsMap.get(j);
		if(sep != null) {
			return sep; 
		}
		else {
			sep = new YearSeparators(j);
			separatorsMap.put(j, sep);
			return sep;
		}
	}
	
	// GETTERS *********************************
	// *****************************************
	
	/**
	 * @return A list of necessary separators. A year separator is necessary, if there is at least one entry before and after it.
	 */
	public Vector<ReadingPoint> getNecessarySeparators() {
		return new Vector<ReadingPoint>(necessaryYears);
	}
	
	// LISTENERS METHODS ***********************
	// *****************************************
	
	//Increments the number of entries using entryYear (and adds it if nec.)
	protected void incrementYear(Integer entryYear) {
		Integer noOfEntries = yearEntryMap.get(entryYear);
		if(noOfEntries == null) {
			yearEntryMap.put(entryYear, 1);
			usedYears = new Vector<Integer>(new TreeSet<Integer>(yearEntryMap.keySet()));
			int index = usedYears.indexOf(entryYear);
			//Add necessary separators, if the new entry is at first or last position
			if(usedYears.size() != 1 && (index == 0 || index == usedYears.size()-1)) {
				//If it is the first element, add years from this one up to (and not including) the one at position 1
				if(index == 0) { 
					for(int y = usedYears.get(1)-1; y >= entryYear; y--) {
						ReadingPoint newrp = new ReadingPoint(Integer.toString(y),new GregorianCalendar(y,11,31));
						necessaryYears.add(0,newrp);
						fireSeparatorAdded(newrp);
					}
				}
				//If it is the last element add years from the element before up to (and not including) this year
				else {
					for(int y = usedYears.get(index -1); y < entryYear; y++) {
						ReadingPoint newrp = new ReadingPoint(Integer.toString(y),new GregorianCalendar(y,11,31));
						necessaryYears.add(newrp);
						fireSeparatorAdded(newrp);
					}
				}
			}
		}
		else yearEntryMap.put(entryYear, noOfEntries+1);
	}
	
	//Decrements the number of entries using entryYear (and removes it if nec.)
	protected void decrementYear(Integer entryYear) {
		Integer noOfEntries = yearEntryMap.get(entryYear);
		if(noOfEntries != null) {
			if(noOfEntries <= 1) {
				yearEntryMap.remove(entryYear);
				Integer index = usedYears.indexOf(entryYear);
				int size = usedYears.size();
				usedYears.remove(entryYear);
				//If there are now 1 or less years, we don't need any separators
				if(size <= 2) {
					for(ReadingPoint oldrp : necessaryYears) {
						necessaryYears.remove(oldrp);
						fireSeparatorRemoved(oldrp);
					}
					return;
				}
				//If it the last or first element, we have to remove years:
				if((index == 0 || index == size-1)) {
					//If it is the first element, remove years from this one up to (and not including) the year which is now the first one
					if(index == 0) {
						int numberOfRemovedYears = usedYears.get(0) - entryYear;
						for(int i = 0; i < numberOfRemovedYears; i++) {
							ReadingPoint oldrp = necessaryYears.get(0);
							necessaryYears.remove(0);
							fireSeparatorRemoved(oldrp);
						}
					}
					//If it is the last one, remove years from the year which is now last up (and not including) this one
					else {
						int numberOfRemovedYears = entryYear - usedYears.get(usedYears.size()-1);
						for(int i = 0; i < numberOfRemovedYears; i++) {
							ReadingPoint oldrp = necessaryYears.get(necessaryYears.size()-1);
							necessaryYears.remove(necessaryYears.size()-1);
							fireSeparatorRemoved(oldrp);
						}
					}
				}
			}
			else {
				yearEntryMap.put(entryYear, noOfEntries-1);
			}
		}
	}
	
	@Override
	public void entriesAdded(Journal source, Entry[] newEntries) {
		if(newEntries != null) {
			for(Entry e : newEntries) {
				Integer entryYear = e.getDate().get(GregorianCalendar.YEAR);
				incrementYear(entryYear);
			}
		}
	}

	@Override
	public void entriesRemoved(Journal source, Entry[] oldEntries) {
		if(oldEntries != null) {
			for(Entry e : oldEntries) {
				Integer entryYear = e.getDate().get(GregorianCalendar.YEAR);
				decrementYear(entryYear);
			}
		}
	}

	@Override
	public void entryReplaced(Journal source, Entry oldEntry, Entry newEntry) {
		if(oldEntry.getDate().get(GregorianCalendar.YEAR) != newEntry.getDate().get(GregorianCalendar.YEAR)) {
			incrementYear(newEntry.getDate().get(GregorianCalendar.YEAR));
			decrementYear(oldEntry.getDate().get(GregorianCalendar.YEAR));
		}
	}
	
	protected void fireSeparatorAdded(ReadingPoint r) {
		for(YearSeparatorListener l : listeners) l.separatorAdded(associatedJournal, r);
	}
	
	protected void fireSeparatorRemoved(ReadingPoint r) {
		for(YearSeparatorListener l : listeners) l.separatorRemoved(associatedJournal, r);
	}
	
	public void addYearSeparatorListener(YearSeparatorListener l) {
		if(l != null) listeners.add(l);
	}
	
	public void removeYearSeparatorListener(YearSeparatorListener l) {
		listeners.remove(l);
	}
	
	
}
