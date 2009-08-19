package fs.fibu2.view.model;

import java.util.HashSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import fs.fibu2.data.format.DefaultAccountComparator;
import fs.fibu2.data.model.AbstractAccount;
import fs.fibu2.data.model.Account;
import fs.fibu2.data.model.AccountLoader;
import fs.fibu2.data.model.Journal;
import fs.fibu2.lang.Fsfibu2StringTableMgr;

/**
 * This implements a table model which in the left column displays accounts of a {@link Journal} and in the right column
 * allows the user to edit start values of these accounts.
 * @author Simon Hampe
 *
 */
public class StartValueModel implements TableModel {

	// DATA **********************
	// ***************************
	
	private Journal associatedJournal;
	private HashSet<TableModelListener> listeners = new HashSet<TableModelListener>();
	
	private Vector<Account> accounts = new Vector<Account>();
	
	private static final String sgroup = "fs.fibu2.model.StartValueModel";
	
	// CONSTRUCTOR ******************
	// ******************************
	
	public StartValueModel(Journal j) {
		associatedJournal = j == null? new Journal() : j;
		TreeSet<Account> sortedAccounts = new TreeSet<Account>(new DefaultAccountComparator());
		String noid = (new AbstractAccount()).getID();
		for(String id : AccountLoader.getListOfIDs()) {
			if(!id.equals(noid))sortedAccounts.add(AccountLoader.getAccount(id));
		}
		accounts = new Vector<Account>(sortedAccounts);
	}
	
	// TABLEMODEL *******************
	// ******************************
	
	@Override
	public void addTableModelListener(TableModelListener l) {
		if(l != null) listeners.add(l);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch(columnIndex) {
		case 0: return Account.class;
		case 1: return Float.class;
		default: return Object.class;
		}
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int columnIndex) {
		switch(columnIndex) {
		case 0: return Fsfibu2StringTableMgr.getString(sgroup + ".account");
		case 1: return Fsfibu2StringTableMgr.getString(sgroup + ".startvalue");
		default: return "";
		}
	}

	@Override
	public int getRowCount() {
		return accounts.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch(columnIndex) {
		case 0: return accounts.get(rowIndex).getName();
		case 1: return associatedJournal.getStartValue(accounts.get(rowIndex));
		default: return null;
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 1;
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		listeners.remove(l);
	}

	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		if(columnIndex == 1) {
			try {
				Float f = Float.parseFloat(value.toString());
				if(f == 0) associatedJournal.removeStartValueUndoable(accounts.get(rowIndex));
				else associatedJournal.setStartValueUndoable(accounts.get(rowIndex), f);
				fireTableChanged();
			}
			catch(NumberFormatException e) {
				//Ignore
			}
		}
	}

	protected void fireTableChanged() {
		for(TableModelListener l : listeners) l.tableChanged(new TableModelEvent(this));
	}
	
}
