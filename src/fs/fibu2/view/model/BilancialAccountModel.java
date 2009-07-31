package fs.fibu2.view.model;

import java.util.HashSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.TableModel;

import fs.fibu2.data.format.DefaultAccountComparator;
import fs.fibu2.data.model.Account;
import fs.fibu2.lang.Fsfibu2StringTableMgr;

/**
 * This model is created from a {@link BilancialTreeModel} and contains the values for all
 * accounts used in that model.
 * @author Simon Hampe
 *
 */
public class BilancialAccountModel implements TableModel, TreeModelListener {

	private HashSet<TableModelListener> listenerList = new HashSet<TableModelListener>();
	
	private BilancialTreeModel model;
	
	private Vector<Account> accounts;
	
	public BilancialAccountModel(BilancialTreeModel m) {
		if(m == null) throw new NullPointerException("Cannot create model from null tree model");
		model = m;
		model.addTreeModelListener(this);
		recalculate();
	}
	
	@Override
	public void addTableModelListener(TableModelListener l) {
		if(l != null) listenerList.add(l);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0: return String.class;
		case 1: 
		case 2: 
		case 3: return Float.class;
		default: return Object.class;
		}
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
		case 0: return Fsfibu2StringTableMgr.getString("fs.fibu2.view.AccountTableModel.account");
		case 1: return Fsfibu2StringTableMgr.getString("fs.fibu2.view.AccountTableModel.before");
		case 2: return Fsfibu2StringTableMgr.getString("fs.fibu2.view.AccountTableModel.after");
		case 3: return Fsfibu2StringTableMgr.getString("fs.fibu2.view.AccountTableModel.difference");
		default: return "?";		
		}
	}

	@Override
	public int getRowCount() {
		return accounts.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(rowIndex < 0 || rowIndex >= getRowCount() || columnIndex < 0 || columnIndex >= getColumnCount()) 
			return null;
		Account a = accounts.get(rowIndex);
		switch(columnIndex) {
		case 0: return a.getName();
		case 1: return model.getAccountBefore(a);
		case 2: return model.getAccountAfter(a);
		case 3: return model.getAccountAfter(a) - model.getAccountBefore(a);
		default: return "";
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		listenerList.remove(l);
	}

	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		//Ignored
	}

	protected void recalculate() {
		TreeSet<Account> sortedSet = new TreeSet<Account>(new DefaultAccountComparator());
			sortedSet.addAll(model.getAccounts());
		accounts = new Vector<Account>(sortedSet);
		fireTableChanged();
	}
	
	protected void fireTableChanged() {
		for(TableModelListener l : listenerList) l.tableChanged(new TableModelEvent(this));
	}
	
	@Override
	public void treeNodesChanged(TreeModelEvent e) {
		recalculate();
	}

	@Override
	public void treeNodesInserted(TreeModelEvent e) {
		recalculate();
	}

	@Override
	public void treeNodesRemoved(TreeModelEvent e) {
		recalculate();
	}

	@Override
	public void treeStructureChanged(TreeModelEvent e) {
		recalculate();
	}

}
