package fs.fibu2.view.model;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import fs.fibu2.data.format.DefaultAccountComparator;
import fs.fibu2.data.format.MoneyDecimal;
import fs.fibu2.data.model.Account;
import fs.fibu2.data.model.Entry;
import fs.fibu2.lang.Fsfibu2StringTableMgr;

/**
 * This class implements a model for a very small table containing information about account states at the 
 * beginning and the end of a certain {@link JournalTableModel} (or a connected subset of it)
 * @author Simon Hampe
 *
 */
public class AccountTableModel implements TableModel {

	private HashSet<TableModelListener> listeners = new HashSet<TableModelListener>();
	
	private JournalTableModel associatedModel;
	private Object fromObject;
	private Object toObject;
	private Vector<Entry> entries;
	
	private TableModelListener modelListener = new TableModelListener() {
		@Override
		public void tableChanged(TableModelEvent e) {
			fireTableDataChanged();
		}		
	};
	
	// CONSTRUCTOR *****************************
	// *****************************************
	
	/**
	 * Constructs a table model which sums up a certain range
	 * @param model The table model from which the bilancial data is retrieved
	 * @param from The first entry considered. The account information from the <i>preceding</i> entry is used as 'before' data. If this value is null,
	 * the model is used from the first entry
	 * @param to The last entry considered. Its account information is used as 'after' data. If this value is null, the model is used up to the last entry
	 * @param currency The currency used to display the values
	 */
	public AccountTableModel(JournalTableModel model, Object from, Object to)  {
		associatedModel = model;
		if(associatedModel != null) {
			model.addTableModelListener(modelListener);
		}
		fromObject = from;
		toObject = to;
	}
	
	/**
	 * Constructs a table model which sums up a certain selection of entries
	 * @param model The table model from which the initial bilancial data is taken
	 * @param entries The set of entries to sum up. The initial data is the data of the first minus its value.
	 */
	public AccountTableModel(JournalTableModel model, Vector<Entry> entries) {
		associatedModel = model;
		if(associatedModel != null) {
			model.addTableModelListener(modelListener);
		}
		this.entries = new Vector<Entry>(entries);
	}
	
	// SETTERS *********************************
	
	/**
	 * Configures this model to sum up the values of the given entries 
	 */
	public void setEntries(Vector<Entry> entries) {
		this.entries = new Vector<Entry>(entries);
		fireTableDataChanged();
	}
	
	/**
	 * Configures this model to sum up values of all entries from 'from' to 'to' (including both)
	 * @param from
	 * @param to
	 */
	public void setRange(Object from, Object to) {
		fromObject = from;
		toObject = to;
		entries = null;
		fireTableDataChanged();
	}
	
	// TABLEMODEL ******************************
	// *****************************************
	
	@Override
	public void addTableModelListener(TableModelListener arg0) {
		if(arg0 != null) listeners.add(arg0);
	}

	/**
	 * @return String.class
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch(columnIndex) {
		case 0: return String.class;
		default: return Float.class;
		}
	}

	/**
	 * @return 4
	 */
	@Override
	public int getColumnCount() {
		return 4;
	}

	/**
	 * @return The name of the column
	 */
	@Override
	public String getColumnName(int columnIndex) {
		switch(columnIndex) {
		case 0: return Fsfibu2StringTableMgr.getString("fs.fibu2.view.AccountTableModel.account");
		case 1: return Fsfibu2StringTableMgr.getString("fs.fibu2.view.AccountTableModel.before");
		case 2: return Fsfibu2StringTableMgr.getString("fs.fibu2.view.AccountTableModel.after");
		case 3: return Fsfibu2StringTableMgr.getString("fs.fibu2.view.AccountTableModel.difference");
		default: return "";
		}
	}

	@Override
	public int getRowCount() {
		if(associatedModel != null) {
			return associatedModel.getBilancialMapping(associatedModel.getRowCount()-1).getMostRecent().information().getAccountMappings().keySet().size();
		}
		else return 0;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(associatedModel == null) return "";
		TreeSet<Account> accounts = new TreeSet<Account>(new DefaultAccountComparator());
		accounts.addAll(associatedModel.getBilancialMapping(associatedModel.getRowCount()-1).getMostRecent().information().getAccountMappings().keySet());
		Vector<Account> indexAccounts = new Vector<Account>(accounts);
		Account rowAccount = indexAccounts.get(rowIndex);
		switch(columnIndex) {
		case 0: return rowAccount.getName();
		case 1: if(entries == null)  {
					BilancialInformation info = fromObject == null? associatedModel.getBilancialMapping(0).getMostRecent().information() :
												associatedModel.getBilancialMapping(fromObject).getMostRecent().information();
					if(fromObject == null || !(fromObject instanceof Entry)) return info.getAccountMappings().get(rowAccount);
					else return info.decrement((Entry)fromObject).getAccountMappings().get(rowAccount);
				}
				else {
					BilancialInformation info = associatedModel.getBilancialMapping(entries.firstElement()).getMostRecent().information();
					return info.decrement(entries.firstElement()).getAccountMappings().get(rowAccount);
				}
		case 2: if(entries == null) {
					return (toObject == null? associatedModel.getBilancialMapping(associatedModel.getRowCount()-1).getMostRecent().information() :
										associatedModel.getBilancialMapping(toObject).getMostRecent().information()).getAccountMappings().get(rowAccount);
				}
				else {
					BilancialInformation info = associatedModel.getBilancialMapping(entries.firstElement()).getMostRecent().information()
															.decrement(entries.firstElement());
					for(Entry e : entries) info = info.increment(e);
					return info.getAccountMappings().get(rowAccount);
				}
		case 3: return //(Float)getValueAt(rowIndex, 2) - (Float)getValueAt(rowIndex, 1);
				MoneyDecimal.substract((BigDecimal)getValueAt(rowIndex, 2), (BigDecimal)getValueAt(rowIndex, 1));
		default: return "";
		}
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
		listeners.remove(l);
	}

	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		throw new UnsupportedOperationException("Cannot modify account table manually");
	}
	
	protected void fireTableDataChanged() {
		for(TableModelListener l : listeners) l.tableChanged(new TableModelEvent(this));
	}

}
