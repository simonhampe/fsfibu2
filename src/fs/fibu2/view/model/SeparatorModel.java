package fs.fibu2.view.model;

import java.util.HashSet;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import fs.fibu2.data.model.EntrySeparator;

/**
 * This class models a list of displayed separators in a {@link JournalTableModel}.
 * @author Simon Hampe
 *
 */
public class SeparatorModel implements ComboBoxModel {
	
	private JournalTableModel associatedModel;
	private EntrySeparator selectedItem;
	
	private HashSet<ListDataListener> listeners = new HashSet<ListDataListener>();
	
	
	
	private TableModelListener tableListener = new TableModelListener() {
		@Override
		public void tableChanged(TableModelEvent e) {
			fireContentsChanged();
		}
	};
	
	public SeparatorModel(JournalTableModel model) {
		associatedModel = model;
		if(associatedModel != null) {
			associatedModel.addTableModelListener(tableListener);
			if(getSize() > 0) selectedItem = associatedModel.getDisplayedSeparators().get(0);
		}
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		if(l != null) listeners.add(l);
	}

	@Override
	public Object getElementAt(int index) {
		if(associatedModel != null) return associatedModel.getDisplayedSeparators().get(index);
		else return null;
	}

	@Override
	public int getSize() {
		if(associatedModel != null) return associatedModel.getDisplayedSeparators().size();
		else return 0;
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		listeners.remove(l);
	}
	
	protected void fireContentsChanged() {
		for(ListDataListener l : listeners) l.contentsChanged(new ListDataEvent(this,ListDataEvent.CONTENTS_CHANGED,0,getSize()));
	}

	@Override
	public Object getSelectedItem() {
		return selectedItem;
	}

	@Override
	public void setSelectedItem(Object arg0) {
		if(associatedModel != null && associatedModel.getDisplayedSeparators().contains(arg0)) {
			selectedItem = (EntrySeparator)arg0;
			fireContentsChanged();
		}
	}
	
	
	
}
