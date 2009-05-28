package fs.fibu2.view.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.SwingWorker;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import fs.fibu2.data.event.JournalListener;
import fs.fibu2.data.model.Account;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.EntrySeparator;
import fs.fibu2.data.model.Journal;
import fs.fibu2.data.model.LinkedSeparator;
import fs.fibu2.data.model.ReadingPoint;
import fs.fibu2.filter.StackFilter;
import fs.fibu2.lang.Fsfibu2StringTableMgr;

/**
 * This class implements a model for a Journal table. Each model is associated to a {@link StackFilter} to which is listens, as well as to the associated {@link Journal}.
 * There are several visibility settings for reading points and the model provides bilancial information for each entry. Since model recalculation
 * is rather extensive, it happens in a separate thread. There is an interface for adding listeners which want to be notified of running recalculation
 * tasks. 
 * @author Simon Hampe
 *
 */
public class JournalTableModel implements TableModel, JournalListener {

	//Data ******
	
	//The list of row objects
	private Vector<Object> listOfRows = new Vector<Object>();
	//The set of linked separators
	private HashSet<LinkedSeparator> linkedSeparators = new HashSet<LinkedSeparator>();
	//The list of bilancial information (in 1:1-correspondance to listOfRows)
	private Vector<HashMap<EntrySeparator, BilancialInformation>> bilancialRows = new Vector<HashMap<EntrySeparator, BilancialInformation>>();
	//The associated stack filter
	private StackFilter associatedFilter;
	
	//Listeners ******
	
	private HashSet<TableModelListener> listenerList = new HashSet<TableModelListener>();
	
	//Misc ******
	
	private final static String sgroup = "fs.fibu2.model.JournalTableModel";
	
	// CONSTRUCTOR **************************************
	// **************************************************
	
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
		return listOfRows.size();
	}

	/**
	 * @return The entry or separator in the row, regardless of the columnIndex
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(rowIndex < 0 || rowIndex >= listOfRows.size()) {
			throw new ArrayIndexOutOfBoundsException("Illegal row argument: " + rowIndex);
		}
		return listOfRows.get(rowIndex);
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
	
	// LISTENER MECHANISM ***********************************************
	// ******************************************************************
	
	protected void fireTableChanged(TableModelEvent e) {
		for(TableModelListener l : listenerList) l.tableChanged(e);
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
	 * Recalculates the model in a separate thread
	 */
	private class Recalculator extends SwingWorker<Object, Object> {

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
