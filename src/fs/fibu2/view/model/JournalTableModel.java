package fs.fibu2.view.model;

import javax.swing.SwingWorker;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import fs.fibu2.data.event.JournalListener;
import fs.fibu2.data.model.Account;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.data.model.ReadingPoint;
import fs.fibu2.filter.StackFilter;

/**
 * This class implements a model for a Journal table. Each model is associated to a {@link StackFilter} to which is listens, as well as to the associated {@link Journal}.
 * There are several visibility settings for reading points and the model provides bilancial information for each entry. Since model recalculation
 * is rather extensive, it happens in a separate thread. There is an interface for adding listeners which want to be notified of running recalculation
 * tasks. 
 * @author Simon Hampe
 *
 */
public class JournalTableModel implements TableModel, JournalListener {

	@Override
	public void addTableModelListener(TableModelListener l) {
		// TODO Auto-generated method stub

	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getColumnName(int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		// TODO Auto-generated method stub

	}

	/**
	 * @throws UnsupportedOperationException Always
	 */
	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		throw new UnsupportedOperationException("Cannot modify journal from model");
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
