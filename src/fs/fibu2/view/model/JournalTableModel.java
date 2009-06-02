package fs.fibu2.view.model;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import sun.awt.image.ImageWatched.Link;

import fs.fibu2.data.event.JournalListener;
import fs.fibu2.data.format.EntryComparator;
import fs.fibu2.data.format.EntryDateComparator;
import fs.fibu2.data.model.Account;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.EntrySeparator;
import fs.fibu2.data.model.ExtremeSeparator;
import fs.fibu2.data.model.Journal;
import fs.fibu2.data.model.LinkedSeparator;
import fs.fibu2.data.model.ReadingPoint;
import fs.fibu2.filter.StackFilter;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.view.event.ProgressListener;
import fs.fibu2.view.event.YearSeparatorListener;

/**
 * This class implements a model for a Journal table. Each model is associated to a {@link StackFilter} to which is listens, as well as to the associated {@link Journal}.
 * There are several visibility settings for reading points and the model provides bilancial information for each entry. Since model recalculation
 * is rather extensive, it happens in a separate thread. There is an interface for adding listeners which want to be notified of running recalculation
 * tasks. 
 * @author Simon Hampe
 *
 */
public class JournalTableModel implements TableModel, JournalListener, YearSeparatorListener, ChangeListener {

	//Data ******
	
	//Reading Point lists
	private ExtremeSeparator startSeparator = new ExtremeSeparator(Fsfibu2StringTableMgr.getString(sgroup + ".start"),true);
	private ExtremeSeparator endSeparator = new ExtremeSeparator(Fsfibu2StringTableMgr.getString(sgroup + ".end"),false);
	private HashSet<LinkedSeparator> linkedSeparators = new HashSet<LinkedSeparator>();
	
	//Backing data (= visible entries + all entries before)
	private TreeSet<Object> sortedData;
	private Vector<Object> indexedData = new Vector<Object>();
	private int indexToStartDisplay; //The index of the first element which is actually displayed (this will always be the index of startSeparator)
	
	private StackFilter filter;
	private Journal		associatedJournal;
	
	//Displayed data (including bilancial data)
	private Vector<Object> displayedData = new Vector<Object>();
		//In 1:1-corr. with indexedData, contains for each element a mapping from preceding EntrySeparators to BilancialInformation relative
		//to that separator. For the starting separator (which is always the first element) and for
		//all entries before indexToStartDisplay, this is just one single mapping for null, containing an overall sum
	private Vector<HashMap<EntrySeparator,BilancialInformation>> bilancialData = new Vector<HashMap<EntrySeparator,BilancialInformation>>();	
	
	//Listeners ******
	
	//TableModelListeners
	private HashSet<TableModelListener> listenerList = new HashSet<TableModelListener>();
	//ProgressListeners for Recalculation
	private HashSet<ProgressListener<Object, Object>> progressListeners = new HashSet<ProgressListener<Object,Object>>();
	
	//Misc ******
	
	private final static String sgroup = "fs.fibu2.model.JournalTableModel";
	
	//Visibility flags
	private boolean displayYearSeparators = true;
	private boolean displayLinkedSeparators = true;
	private boolean displayReadingPoints = true;
	
	// CONSTRUCTOR **************************************
	// **************************************************
	
	/**
	 * Creates a table model, where all contained reading points are initially visible and entries are filtered according to
	 * the given filter.
	 */
	public JournalTableModel(Journal associatedJournal, StackFilter filter, boolean displayYearSeparators, 
									boolean displayLinkedSeparators, boolean displayReadingPoints) {
		//Copy data
		this.associatedJournal = associatedJournal == null? new Journal() : associatedJournal;
		this.filter = filter;
		this.displayYearSeparators = displayYearSeparators;
		this.displayLinkedSeparators = displayLinkedSeparators;
		this.displayReadingPoints = displayReadingPoints;
		
		//Register listeners
		associatedJournal.addJournalListener(this);
		YearSeparators.getInstance(associatedJournal).addYearSeparatorListener(this);
		if(filter != null) filter.addChangeListener(this);
		
		//Recalculate (when instantiating, this is actually done in the AWT thread)
		recalculateLists();
		recalculateBilancials(0);
	}
	
	// TABLEMODEL ***************************************
	// **************************************************
	
	@Override
	public void addTableModelListener(TableModelListener l) {
		if(l != null) listenerList.add(l);
	}

	/**
	 * @return Object.class for all
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return Object.class;
	}

	/**
	 * @return 8
	 */
	@Override
	public int getColumnCount() {
		return 8;
	}

	/**
	 * @return A descriptive name for the given column
	 */
	@Override
	public String getColumnName(int columnIndex) {
		switch(columnIndex) {
		case 0: return Fsfibu2StringTableMgr.getString(sgroup + ".columnIndic");
		case 1: return Fsfibu2StringTableMgr.getString(sgroup + ".columnName");
		case 2: return Fsfibu2StringTableMgr.getString(sgroup + ".columnDate");
		case 3: return Fsfibu2StringTableMgr.getString(sgroup + ".columnValue");
		case 4: return Fsfibu2StringTableMgr.getString(sgroup + ".columnAccount");
		case 5: return Fsfibu2StringTableMgr.getString(sgroup + ".columnCategory");
		case 6: return Fsfibu2StringTableMgr.getString(sgroup + ".columnAccInfo");
		case 7: return Fsfibu2StringTableMgr.getString(sgroup + ".columnAddInfo");
		default: return "";
		}
	}

	@Override
	public int getRowCount() {
		return displayedData.size();
	}

	/**
	 * @return The entry or separator in the row, regardless of the columnIndex
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return displayedData.get(rowIndex);
	}

	/**
	 * @return false
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		listenerList.remove(l);
	}

	/**
	 * @throws UnsupportedOperationException Always
	 */
	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		throw new UnsupportedOperationException("Cannot modify journal from model");
	}
	
	// RECALCULATION METHODS ***************************************
	// *************************************************************
	
	/**
	 * This method recalculates the sorted lists of entries/reading points. The unsorted reading point collections
	 * are not reloaded but used as they are.  
	 */
	protected void recalculateLists() {
		TreeSet<Object> sortedSet = new TreeSet<Object>(new TableModelComparator());
		//Load all entries and add separators
		sortedSet.addAll(associatedJournal.getEntries());
		if(displayLinkedSeparators) sortedSet.addAll(linkedSeparators);
		if(displayReadingPoints) sortedSet.addAll(associatedJournal.getReadingPoints());
		if(displayYearSeparators) sortedSet.addAll(YearSeparators.getInstance(associatedJournal).getNecessarySeparators());
		sortedSet.add(startSeparator);
		sortedSet.add(endSeparator);
		
		//Remove unused entries and reading points
		int firstContainedIndex = -1; 	//The index of the first element actually displayed
		int currentIndex = 0;			//The index we're currently at
		HashSet<Entry> elementsToRemove = new HashSet<Entry>(); //The set of entries thrown out
		HashSet<Entry> elementsNotDisplayed = new HashSet<Entry>(); //Entries not thrown out, but also not displayed
		HashSet<EntrySeparator> separatorsToRemoveBefore = new HashSet<EntrySeparator>(); //Separators before the first displayed entry
		HashSet<EntrySeparator> separatorsToRemoveAfter = new HashSet<EntrySeparator>(); //Separators after the last displayed entry 
		for(Object o : sortedSet) {
			if(o instanceof Entry) {
				//We keep entries which are filtered out, if we have not reached any accepted entry yet
				if(filter != null && !filter.verifyEntry(((Entry)o))) {
					if(firstContainedIndex  >= 0) {
						elementsToRemove.add(((Entry)o));
					}
					else {
						elementsNotDisplayed.add(((Entry)o));
					}
				}
				else {
					//If this is the first accepted entry, mark the point
					if(firstContainedIndex < 0) firstContainedIndex = currentIndex;	
					//Clear list of deleted reading points
					separatorsToRemoveAfter.clear();
				}
			}
			else {
				//Remove reading points which are before the first displayed entry
				if(firstContainedIndex <0 && !(o instanceof ExtremeSeparator)) separatorsToRemoveBefore.add((EntrySeparator)o);
				//For each displayed rp we initially assume, that it has to be removed. If another entry occurs
				//afterwards, it is not removed
				else {
					if(!(o instanceof ExtremeSeparator)) separatorsToRemoveAfter.add((EntrySeparator)o);
				}
			}
			currentIndex++;
		}
		sortedSet.removeAll(elementsToRemove);
		
		//Copy data
		synchronized (this) {
			sortedData = sortedSet;
			indexedData = new Vector<Object>(sortedData);
			indexToStartDisplay = firstContainedIndex;
			displayedData = new Vector<Object>(indexedData);
			displayedData.removeAll(elementsNotDisplayed);
		}
	}
	
	/**
	 * Recalculates the bilancial vector, starting from a given index in the range of the size of indexedData. All preceding bilancial 
	 * data will be used as a base for further calculation. If index <= 0, the data is computed completely anew. If the
	 * index is greater than the actual size of indexedData, nothing changes. Only the bilancial data for displayed elements is stored in detail.
	 * The bilancial data for all elements which come before is only stored in sum in the start separator.
	 */
	protected void recalculateBilancials(int index) {
		if(index >= displayedData.size()) return;
		
		Vector<HashMap<EntrySeparator,BilancialInformation>> newbilancials = new Vector<HashMap<EntrySeparator,BilancialInformation>>();
		//Copy correct data
		synchronized (this) {
			for(int i = 0; i < index; i++) newbilancials.add(bilancialData.get(i));
		}
		
		//Derive last valid bilancial data
		HashMap<EntrySeparator, BilancialInformation> lastBilancial = new HashMap<EntrySeparator, BilancialInformation>();
		EntrySeparator unhandledSeparator = null; //If a separator is added, the corresponding bilancial information is only added in the next step
		//TODO: How to manage bilancial mapping creation for new separators?
		if(index == 0) {
			BilancialInformation initInfo = new BilancialInformation();
			for(Account a : associatedJournal.getListOfAccounts()) initInfo.accountSums.put(a, associatedJournal.getStartValue(a));
			lastBilancial.put(null, initInfo);
		}
		else {
			lastBilancial = newbilancials.get(index -1);
			if(indexedData.get(index -1) instanceof EntrySeparator) unhandledSeparator = (EntrySeparator)indexedData.get(index -1);
		}
		
		//Calculate new bilancials
		for(int i = index; i < indexedData.size(); i++) {
			Object o = indexedData.get(i);
			//If it is a reading point, just copy the last bilancial info and add it to the separator list
			if(o instanceof EntrySeparator) {
				HashMap<EntrySeparator, BilancialInformation> nextBilancial = new HashMap<EntrySeparator, BilancialInformation>();
				for(EntrySeparator s : lastBilancial.keySet()) {
					nextBilancial.put(s, lastBilancial.get(s).clone());
				}
			}
			//If it is an entry, add values
			if(o instanceof Entry) {
				HashMap<EntrySeparator,BilancialInformation> nextInfo = new HashMap<EntrySeparator, BilancialInformation>();
				for(EntrySeparator s : lastBilancial.keySet()) {
					BilancialInformation nextBilancial = lastBilancial.get(s).clone();
					float entryValue = ((Entry)o).getValue();					
					
					nextBilancial.overallSum += entryValue;
					
					Float catValue = nextBilancial.categorySums.get(((Entry)o).getCategory());
					nextBilancial.categorySums.put(((Entry)o).getCategory(), entryValue + (catValue == null? 0 : catValue));
					
					Float accValue = nextBilancial.accountSums.get(((Entry)o).getAccount());
					nextBilancial.accountSums.put(((Entry)o).getAccount(), entryValue + (accValue == null? 0 : accValue));
					
					nextInfo.put(s, nextBilancial);
				}
			}
		}
		
	}
	
	// CHANGELISTENER **********************************************
	// *************************************************************
	
	//Called when filter changes
	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	// YEARSSEPARATOR LISTENER *************************************
	// *************************************************************
	
	@Override
	public void separatorAdded(Journal source, ReadingPoint yearSeparator) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void separatorRemoved(Journal source, ReadingPoint yearSeparator) {
		// TODO Auto-generated method stub
		
	}

	// LISTENER MECHANISM ***********************************************
	// ******************************************************************
	
	
	protected void fireTableChanged(TableModelEvent e) {
		for(TableModelListener l : listenerList) l.tableChanged(e);
	}
	
	protected void fireTaskBegins(SwingWorker<Object,Object> r) {
		for(ProgressListener<Object, Object> l : progressListeners) l.taskBegins(r);
	}
	
	protected void fireProgressed(SwingWorker<Object,Object> r) {
		for(ProgressListener<Object, Object> l : progressListeners) l.progressed(r);
	}
	
	protected void fireTaskFinished(SwingWorker<Object, Object> r) {
		for(ProgressListener<Object, Object> l : progressListeners) l.taskFinished(r);
	}
	
	/**
	 * Adds a listener which is notified of the beginning, the intermediate progress and the termination
	 * of a recalculation process of this model
	 */
	public void addProgressListener(ProgressListener<Object, Object> l) {
		if(l != null) progressListeners.add(l);
	}
	
	public void removeProgressListener(ProgressListener<Object, Object> l) {
		progressListeners.remove(l);
	}
	
	// JOURNALLISTENER **************************************************
	// ******************************************************************

	@Override
	public void descriptionChanged(Journal source, String oldValue,
			String newValue) {
		//Ignore
		
	}

	@Override
	public void entriesAdded(Journal source, Entry[] newEntries) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void entriesRemoved(Journal source, Entry[] oldEntries) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void entryReplaced(Journal source, Entry oldEntry, Entry newEntry) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nameChanged(Journal source, String oldValue, String newValue) {
		//Ignore
	}

	@Override
	public void readingPointAdded(Journal source, ReadingPoint point) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readingPointRemoved(Journal source, ReadingPoint point) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startValueChanged(Journal source, Account a, Float oldValue,
			Float newValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dateChanged(ReadingPoint source) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nameChanged(ReadingPoint source) {
		// TODO Auto-generated method stub
		
	}
	
	// COMPARATOR *************************************************************
	// ************************************************************************
	
	/**
	 * An instance of this class compares objects which are displayed in a journal table, i.e.
	 * {@link Entry} and {@link EntrySeparator} objects. If objects of any other type are compared or any of them is null,
	 * it throws an {@link IllegalArgumentException}.
	 */
	public static class TableModelComparator implements Comparator<Object> {

		private EntryComparator entryComparator = new EntryComparator(false);
		private EntryDateComparator entryDateComparator = new EntryDateComparator();
		
		@Override
		public int compare(Object o1, Object o2) {
			if(o1 == null || o2 == null) throw new IllegalArgumentException("Cannot compare null objects");
			
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
			
			if(o1 instanceof LinkedSeparator && o2 instanceof LinkedSeparator) {
				return entryComparator.compare(((LinkedSeparator)o1).getLinkedEntry(),((LinkedSeparator)o2).getLinkedEntry());
			}
			
			//Now try heterogeneous pairings
			for(Object p1 : Arrays.asList(o1,o2)) {
				Object p2 = (p1 == o1) ? o2 : o1;
				int factor = (p1 == o1)? 1 : -1;
				
				if(p1 instanceof Entry && p2 instanceof EntrySeparator) {
					return factor * (((EntrySeparator)p2).isLessOrEqualThanMe(((Entry)p1)) ? -1 : 1);
				}
				
				if(p1 instanceof ExtremeSeparator) return factor * (((ExtremeSeparator)p1).isBeforeAll()? -1 : 1);
				
				if(p1 instanceof ReadingPoint && p2 instanceof LinkedSeparator) {
					return factor * (((ReadingPoint)p1).isLessOrEqualThanMe(((LinkedSeparator)p2).getLinkedEntry()) ? 1 : -1);
				}
			}
			
			//If we arrive here, this is not a valid call
			throw new IllegalArgumentException("Cannot compare: Only Entry and EntrySeparator types are allowed.");
		}
		
	}

	// RECALCULATOR CLASS *******************************************************
	// *************************************************************************

	/**
	 * Recalculates the bilancial information of the model in a separate thread, starting from a certain index. There is always only one running instance of this object.
	 * If one is requested, while one is still running, the old one is cancelled and a new calculation is started from the lower index of both
	 */
	private static class BilancialRecalculator extends SwingWorker<Object, Object> {

		//The currently running/last requested instance
		private static BilancialRecalculator runningInstance = null;
		
		private int calculationIndex = 0;
		
		// CONSTRUCTION **************************************
		// ***************************************************
		
		private BilancialRecalculator(int index) {
			calculationIndex = index >= 0? index : 0;
		}
		
		/**
		 * @param startCalculationAt The index in the list of data at which to start the calculation
		 * @return An instance of Recalculator. Cancels a running instance, if it exists and returns one which has the lower 
		 * index of both as calculation index
		 */
		public static synchronized BilancialRecalculator getInstance(int startCalculationAt) {
			if(runningInstance == null) {
				runningInstance = new BilancialRecalculator(startCalculationAt);
				return runningInstance;
			}
			else {
				runningInstance.cancel(true);
				runningInstance = new BilancialRecalculator(
						runningInstance.getCalculationIndex() <= startCalculationAt? runningInstance.getCalculationIndex() : startCalculationAt);
				return runningInstance;
			}
		}
		
		// GETTERS *******************************************
		// ***************************************************
		
		public int getCalculationIndex() {
			return calculationIndex;
		}
		
		// OVERRIDDEN ****************************************
		// ***************************************************
		
		@Override
		protected Object doInBackground() throws Exception {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected void done() {
			// TODO Auto-generated method stub
			super.done();
		}		
	}
	
	
	
}
