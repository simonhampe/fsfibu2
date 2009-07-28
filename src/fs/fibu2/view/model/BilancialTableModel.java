package fs.fibu2.view.model;

import java.util.HashSet;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.TableModel;

import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.view.model.BilancialTreeModel.ExtendedCategory;
import fs.fibu2.view.render.BilancialTree;

/**
 * This model contains the bilancial valued obtained from a {@link BilancialTree}. It does not save any values, but gets them from the tree's model.
 * It also listens to the expansion state of the tree and displays only rows for visible tree rows
 * @author Simon Hampe
 *
 */
public class BilancialTableModel implements TableModel, TreeExpansionListener,
		TreeModelListener {

	private BilancialTree tree;
	
	private HashSet<TableModelListener> listenerList = new HashSet<TableModelListener>();
	
	private final static String sgroup = "fs.fibu2.model.BilancialTableModel";
	
	// CONSTRUCTOR *****************************
	// *****************************************
	
	/**
	 * Constructs a new model, obtaining its data from the given tree
	 */
	public BilancialTableModel(BilancialTree tree) {
		if(tree == null) throw new NullPointerException("Cannot create BilancialTableModel from null tree");
		else this.tree = tree;
	}
	
	// TABLEMODEL ******************************
	// *****************************************
	
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
		return tree.getRowCount();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(0 > rowIndex || rowIndex >= getRowCount() || columnIndex < 0 || columnIndex >= getColumnCount()) return null;
		ExtendedCategory ec  = (ExtendedCategory) tree.getPathForRow(rowIndex).getLastPathComponent();
		switch(columnIndex) {
		case 0: return ec.isAdditional()? tree.getModel().getIndividualPlus(ec.category()) : tree.getModel().getCategoryPlus(ec.category());
		case 1: return ec.isAdditional()? tree.getModel().getIndividualMinus(ec.category()) : tree.getModel().getCategoryMinus(ec.category());
		case 2: return ec.isAdditional()? tree.getModel().getIndividualSum(ec.category()) : tree.getModel().getCategorySum(ec.category());
		case 3: return !tree.getModel().isVisible(ec);
		case 4: String mask = tree.getModel().getMask(ec);
				return mask == null? "" : mask;
		default: return "";
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return (columnIndex == 3 && rowIndex != 0) || columnIndex == 4;
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		listenerList.remove(l);
	}

	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		if(!isCellEditable(rowIndex, columnIndex) ) return;
		else {
			ExtendedCategory ec = (ExtendedCategory) tree.getPathForRow(rowIndex).getLastPathComponent();
			if(columnIndex == 3 && value instanceof Boolean) {
				if(ec.isAdditional()) tree.getModel().setIndividualVisibility(ec.category(), !(Boolean)value);
				else tree.getModel().setVisibility(ec.category(), !(Boolean)value);
			}
			if(columnIndex == 4 && value instanceof String) {
				if(ec.isAdditional()) tree.getModel().setIndividualMask(ec.category(), value.equals("")? null : (String)value);
				else tree.getModel().setMask(ec.category(), value.equals("")? null : (String)value);
			}
		}
	}
	
	protected void fireTableChanged() {
		for(TableModelListener l : listenerList) l.tableChanged(new TableModelEvent(this));
	}
	
	// TREELISTENER ******************************
	// *******************************************

	@Override
	public void treeCollapsed(TreeExpansionEvent event) {
		fireTableChanged();
	}

	@Override
	public void treeExpanded(TreeExpansionEvent event) {
		fireTableChanged();
	}

	@Override
	public void treeNodesChanged(TreeModelEvent e) {
		fireTableChanged();
	}	

	@Override
	public void treeNodesInserted(TreeModelEvent e) {
		fireTableChanged();
	}

	@Override
	public void treeNodesRemoved(TreeModelEvent e) {
		fireTableChanged();
	}

	@Override
	public void treeStructureChanged(TreeModelEvent e) {
		fireTableChanged();	}

}
