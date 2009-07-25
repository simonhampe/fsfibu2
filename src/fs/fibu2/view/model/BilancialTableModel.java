package fs.fibu2.view.model;

import java.util.HashSet;

import javax.swing.event.TableModelListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableModel;

import com.sun.corba.se.spi.orbutil.fsm.FSM;

import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.view.render.BilancialTree;

/**
 * This model contains the bilancial valued obtained from a {@link BilancialTree}. It does not save any values, but gets them from the tree's model.
 * It also listens to the expansion state of the tree and displays only rows for visible tree rows
 * @author Simon Hampe
 *
 */
public class BilancialTableModel implements TableModel, TreeExpansionListener,
		TreeModelListener, TreeSelectionListener {

	private HashSet<TableModelListener> listenerList = new HashSet<TableModelListener>();
	
	private final static String sgroup = "fs.fibu2.model.BilancialTableModel";
	
	@Override
	public void addTableModelListener(TableModelListener l) {
		if(l != null) listenerList.add(l);
	}

	/**
	 * @return Float for the first 3 columns, Boolean for the 4th and String for the 5th
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch(columnIndex) {
		case 0:
		case 1:
		case 2: return Float.class;
		case 3: return Boolean.class;
		case 4: return String.class;
		default: return Object.class;
		}
	}

	@Override
	public int getColumnCount() {
		return 5;
	}

	@Override
	public String getColumnName(int columnIndex) {
		switch(columnIndex) {
		case 0: return Fsfibu2StringTableMgr.getString(sgroup + ".in");
		case 1: return Fsfibu2StringTableMgr.getString(sgroup + ".out");
		case 2: return Fsfibu2StringTableMgr.getString(sgroup + ".sum");
		case 3: return Fsfibu2StringTableMgr.getString(sgroup + ".invisible");
		case 4: return Fsfibu2StringTableMgr.getString(sgroup + ".mask");
		default: return "";
		}
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

	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub

	}

	@Override
	public void treeCollapsed(TreeExpansionEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void treeExpanded(TreeExpansionEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void treeNodesChanged(TreeModelEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void treeNodesInserted(TreeModelEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void treeNodesRemoved(TreeModelEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void treeStructureChanged(TreeModelEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		// TODO Auto-generated method stub

	}

}
