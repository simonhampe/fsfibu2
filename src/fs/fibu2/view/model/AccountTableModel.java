package fs.fibu2.view.model;

import java.util.Currency;
import java.util.HashSet;
import java.util.Locale;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import fs.fibu2.data.format.DefaultAccountComparator;
import fs.fibu2.data.format.DefaultCurrencyFormat;
import fs.fibu2.data.model.Account;
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
	private Currency displayCurrency;
	
	private TableModelListener modelListener = new TableModelListener() {
		@Override
		public void tableChanged(TableModelEvent e) {
			fireTableDataChanged();
		}		
	};
	
	// CONSTRUCTOR *****************************
	// *****************************************
	
	/**
	 * Constructs a table model. 
	 * @param model The table model from which the bilancial data is retrieved
	 * @param from The first entry considered. The account information from the <i>preceding</i> entry is used as 'before' data. If this value is null,
	 * the model is used from the first entry
	 * @param to The last entry considered. Its account information is used as 'after' data. If this value is null, the model is used up to the last entry
	 * @param currency The currency used to display the values
	 */
	public AccountTableModel(JournalTableModel model, Object from, Object to, Currency currency)  {
		associatedModel = model;
		if(associatedModel != null) {
			model.addTableModelListener(modelListener);
		}
		fromObject = from;
		toObject = to;
		displayCurrency = currency == null? Currency.getInstance(Locale.getDefault()) : currency;
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
		return String.class;
	}

	/**
	 * @return 3
	 */
	@Override
	public int getColumnCount() {
		return 3;
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
		case 1: return DefaultCurrencyFormat.getFormat(displayCurrency).format(
				fromObject == null? associatedModel.getBilancialMapping(0).getMostRecent().information().getAccountMappings().get(rowAccount) :
							  associatedModel.getBilancialMapping(fromObject).getMostRecent().information().getAccountMappings().get(rowAccount));
		case 2: return DefaultCurrencyFormat.getFormat(displayCurrency).format(
				toObject == null? associatedModel.getBilancialMapping(associatedModel.getRowCount()-1).getMostRecent().information().getAccountMappings().get(rowAccount) :
								  associatedModel.getBilancialMapping(toObject).getMostRecent().information().getAccountMappings().get(rowAccount));
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
