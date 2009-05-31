package fs.fibu2.view.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.SwingWorker;
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
import fs.fibu2.filter.StackFilter;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.view.event.ProgressListener;
import fs.fibu2.view.event.YearsSeparatorListener;

/**
 * This class implements a model for a Journal table. Each model is associated to a {@link StackFilter} to which is listens, as well as to the associated {@link Journal}.
 * There are several visibility settings for reading points and the model provides bilancial information for each entry. Since model recalculation
 * is rather extensive, it happens in a separate thread. There is an interface for adding listeners which want to be notified of running recalculation
 * tasks. 
 * @author Simon Hampe
 *
 */
public class JournalTableModel implements TableModel, JournalListener, YearsSeparatorListener {

	//Data ******
	
	//Reading Point lists
	private ExtremeSeparator startSeparator = new ExtremeSeparator(Fsfibu2StringTableMgr.getString(sgroup + ".start"),true);
	private ExtremeSeparator endSeparator = new ExtremeSeparator(Fsfibu2StringTableMgr.getString(sgroup + ".end"),false);
	private HashSet<ReadingPoint> journalReadingPoints = new HashSet<ReadingPoint>();
	private HashSet<ReadingPoint> yearSeparators = new HashSet<ReadingPoint>();
	private HashSet<LinkedSeparator> linkedSeparators = new HashSet<LinkedSeparator>();
	
	private TreeSet<EntrySeparator> visibleSeparators; //A subset of the union of the above lists
	
	//Backing data (= visible entries + all entries before)
	private TreeSet<Object> sortedData;
	private Vector<Object> indexedData = new Vector<Object>();
	private int indexToStartDisplay; //The index of the first element which is actually displayed (this will always be the index of startSeparator)
	
	private StackFilter filter;
	
	//Displayed data (including bilancial data)
	private Vector<Object> displayedData = new Vector<Object>();
		//In 1:1-corr. with the above list, contains for each element a mapping from preceding EntrySeparators to BilancialInformation relative
		//to that separator. For the starting separator (which is always the first element), this is a mapping for itself
	private Vector<HashMap<EntrySeparator,BilancialInformation>> bilancialData = new Vector<HashMap<EntrySeparator,BilancialInformation>>();	
	
	//Listeners ******
	
	//TableModelListeners
	private HashSet<TableModelListener> listenerList = new HashSet<TableModelListener>();
	//ProgressListeners for Recalculation
	private HashSet<ProgressListener<Object, Object>> progressListeners = new HashSet<ProgressListener<Object,Object>>();
	
	//Misc ******
	
	private final static String sgroup = "fs.fibu2.model.JournalTableModel";
	
	// CONSTRUCTOR **************************************
	// **************************************************
	
	/**
	 * Creates a table model, where all contained reading points are initially visible and entries are filtered according to
	 * the given filter.
	 */
	public JournalTableModel(StackFilter filter) {
		
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
