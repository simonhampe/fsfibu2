package fs.fibu2.view.model;

import java.util.HashSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import fs.fibu2.data.event.JournalListener;
import fs.fibu2.data.model.Account;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.EntrySeparator;
import fs.fibu2.data.model.ExtremeSeparator;
import fs.fibu2.data.model.Journal;
import fs.fibu2.data.model.LinkedSeparator;
import fs.fibu2.data.model.ReadingPoint;
import fs.fibu2.filter.EntryFilter;
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
	
	//A sorted list of all displayed entries and separators + all entries not displayed which come before the first entry displayed
	//Despite the order induced by the comparator the starting separator will be moved to firstIndexDisplayed when creating the list
	private Vector<Object> indexedData = new Vector<Object>();
	//The index of the first element displayed in indexedData (i.e. of the start separator)
	private int firstIndexDisplayed;
	
	private EntryFilter filter;
	private Journal		associatedJournal;
	
	//Displayed elements
	private Vector<Object> displayedData = new Vector<Object>();
	//In 1:1-corr. with indexedData, contains for each element a bilancial mapping. For the starting separator and for
		//all entries before indexToStartDisplay, this is just one single mapping for null, containing an overall sum. For the starting separator this is actually a
		//an overall sum (for the account mappings) over all entries not displayed. 
	private Vector<BilancialMapping> bilancialData = new Vector<BilancialMapping>();
	//Displayed separators, sorted
	private Vector<EntrySeparator> displayedSeparators = new Vector<EntrySeparator>();
	
	//Listeners ******
	
	//TableModelListeners
	private HashSet<TableModelListener> listenerList = new HashSet<TableModelListener>();
	//ProgressListeners for Recalculation
	private HashSet<ProgressListener<Object, Object>> progressListeners = new HashSet<ProgressListener<Object,Object>>();
	
	//Misc ******
	
	private final static String sgroup = "fs.fibu2.model.JournalTableModel";
	
	//The currently running/last requested instance of the recalculator
	private static Recalculator runningInstance = null;
	
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
	public JournalTableModel(Journal associatedJournal, EntryFilter filter, boolean displayYearSeparators, 
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
		if(filter != null && (filter instanceof StackFilter)) ((StackFilter)filter).addChangeListener(this);
		
		//Recalculate (when instantiating, this is actually done in the AWT thread)
		recalculateLists();
		recalculateBilancials(0);
	}
	
	// GETTERS / SETTERS ********************************
	// **************************************************
	
	/**
	 * @returns The bilancial mapping for the given row or null, if there is none
	 */
	public BilancialMapping getBilancialMapping(Object row) {
		return getBilancialMapping(displayedData.indexOf(row));
	}
	
	/**
	 * @return The bilancial mapping for the given row index or null, if the index is out of bounds
	 */
	public BilancialMapping getBilancialMapping(int index) {
		if(index < 0 || index >= getRowCount()) return null;
		else return bilancialData.get(index+ firstIndexDisplayed);
	}
	
	/**
	 * @return An instance of Recalculator. Cancels a running instance, if it exists 
	 */
	private synchronized Recalculator getRecalculatorInstance() {
		if(runningInstance == null) {
			runningInstance = new Recalculator();
			return runningInstance;
		}
		else {
			runningInstance.cancel(true);
			runningInstance = new Recalculator();
			return runningInstance;
		}
	}
	
	private void doRecalculation() {
		Recalculator c = getRecalculatorInstance();
		fireTaskBegins(c);
		c.execute();
	}
	
	/**
	 * @return A sorted list of displayed separators 
	 */
	public Vector<EntrySeparator> getDisplayedSeparators() {
		return new Vector<EntrySeparator>(displayedSeparators);
	}
	
	/**
	 * @return The associated journal
	 */
	public Journal getAssociatedJournal() {
		return associatedJournal;
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
		TreeSet<EntrySeparator> sortedSeparators = new TreeSet<EntrySeparator>(new TableModelComparator());
		//Load all entries and add separators
		sortedSet.addAll(associatedJournal.getEntries());
		if(displayLinkedSeparators) sortedSet.addAll(linkedSeparators);
		if(displayReadingPoints) sortedSet.addAll(associatedJournal.getReadingPoints());
		if(displayYearSeparators) sortedSet.addAll(YearSeparators.getInstance(associatedJournal).getNecessarySeparators());
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
				sortedSeparators.add((EntrySeparator)o);
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

		//Insert the start separator
		Vector<Object> finalData = new Vector<Object>(sortedSet);
		finalData.add(firstContainedIndex < 0 ? 0 : firstContainedIndex, startSeparator);
		
		finalData.removeAll(elementsToRemove);
		finalData.removeAll(separatorsToRemoveBefore);
		finalData.removeAll(separatorsToRemoveAfter);
		sortedSeparators.removeAll(separatorsToRemoveAfter);
		sortedSeparators.removeAll(separatorsToRemoveBefore);
		firstContainedIndex = finalData.indexOf(startSeparator);
		
		//Copy data
		synchronized (this) {
			indexedData = finalData;
			displayedData = new Vector<Object>(indexedData);
			displayedData.removeAll(elementsNotDisplayed);
			firstIndexDisplayed = firstContainedIndex;
			displayedSeparators = new Vector<EntrySeparator>(sortedSeparators);
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
		if(index <  0) index = 0;
		
		Vector<BilancialMapping> newbilancials = new Vector<BilancialMapping>();
		//Copy correct data
		synchronized (this) {
			for(int i = 0; i < index; i++) {
				newbilancials.add(bilancialData.get(i));
			}
		}
		
		//Derive last valid bilancial data
		BilancialMapping lastMapping = new BilancialMapping();
		Vector<EntrySeparator> precedingSeparators = new Vector<EntrySeparator>();
		if(index == 0) {
			BilancialInformation initInfo = new BilancialInformation(associatedJournal);
			lastMapping.put(null, initInfo);
			precedingSeparators.add(null);
		}
		else {
			lastMapping= newbilancials.get(index -1);
			precedingSeparators.addAll(lastMapping.keySet());
		}
		
		//Calculate new bilancial
		for(int i = index; i < indexedData.size(); i++) {
			Object o = indexedData.get(i);
			BilancialMapping nextMapping = lastMapping.clone();
			//If this is the first displayed element, add the StartSeparator
			if(i == firstIndexDisplayed) {
				precedingSeparators.add(startSeparator);
			}
			//If it is a reading point, just copy the last bilancial info and add it to the separator list
			//But if we are not yet at the displayed elements, we don't add the separator
			if(o instanceof EntrySeparator && i > firstIndexDisplayed) {
				precedingSeparators.add((EntrySeparator)o);
			}
			//If it is an entry, add values
			if(o instanceof Entry) {
				for(EntrySeparator s : precedingSeparators) {
					BilancialInformation info = lastMapping.get(s);
					//If there is not yet a mapping, create one 
					if(info == null) {
						info = new BilancialInformation(lastMapping.getMostRecent().information());
					}
					info = info.increment((Entry)o);
					nextMapping.put(s, info);
				}
			}
			newbilancials.add(nextMapping);
			lastMapping = nextMapping;
		}
		synchronized (this) {
			bilancialData = newbilancials;
		}
	}
	
	// CHANGELISTENER **********************************************
	// *************************************************************
	
	//Called when filter changes
	@Override
	public void stateChanged(ChangeEvent e) {
		doRecalculation();
	}
	
	// YEARSSEPARATOR LISTENER *************************************
	// *************************************************************
	
	@Override
	public void separatorAdded(Journal source, ReadingPoint yearSeparator) {
		doRecalculation();
	}

	@Override
	public void separatorRemoved(Journal source, ReadingPoint yearSeparator) {
		doRecalculation();
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
		doRecalculation();
	}

	@Override
	public void entriesRemoved(Journal source, Entry[] oldEntries) {
		doRecalculation();
	}

	@Override
	public void entryReplaced(Journal source, Entry oldEntry, Entry newEntry) {
		doRecalculation();
	}

	@Override
	public void nameChanged(Journal source, String oldValue, String newValue) {
		//Ignore
	}

	@Override
	public void readingPointAdded(Journal source, ReadingPoint point) {
		doRecalculation();
	}

	@Override
	public void readingPointRemoved(Journal source, ReadingPoint point) {
		doRecalculation();
	}

	@Override
	public void startValueChanged(Journal source, Account a, Float oldValue,
			Float newValue) {
		doRecalculation();
	}

	@Override
	public void dateChanged(ReadingPoint source) {
		doRecalculation();
	}

	@Override
	public void nameChanged(ReadingPoint source) {
		doRecalculation();
	}
	
	// RECALCULATOR CLASS *******************************************************
	// *************************************************************************

	/**
	 * Recalculates all entries and their bilancials. Originally this class was intended to do this in a faster way, depending on the changes made, but
	 * the recalculation is still so quick that it did eventually seem a bit too much effort. Its background method does not return anything, it
	 * just calls the corresponding calculation methods of the {@link JournalTableModel}
	 */
	private class Recalculator extends SwingWorker<Object, Object> {
		
		@Override
		protected Object doInBackground() throws Exception {
			recalculateLists();
			recalculateBilancials(0);
			return null;
		}

		@Override
		protected void done() {
			fireTaskFinished(this);
			fireTableChanged(new TableModelEvent(JournalTableModel.this));
		}	
		
		
		
	}
	
	
	
}
