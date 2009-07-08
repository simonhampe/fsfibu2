package fs.fibu2.view.model;

import java.util.HashSet;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import fs.fibu2.data.event.JournalAdapter;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;

/**
 * This model represents a list of all years for which there are entries in a journal + a null object which represents 'all years'
 * @author Simon Hampe
 *
 */
public class YearListModel extends JournalAdapter implements ComboBoxModel {

	// DATA *********************************
	// **************************************
	
	private Integer selectedItem;
	
	private HashSet<ListDataListener> listenerList = new HashSet<ListDataListener>();
	
	private Journal associatedJournal;
	
	// CONSTRUCTOR ***************************
	// ***************************************
	
	/**
	 * Creates a list model for the given journal
	 */
	public YearListModel(Journal j) {
		associatedJournal = j;
	}
	
	// LIST MODEL ****************************
	// ***************************************
	
	@Override
	public Object getSelectedItem() {
		return selectedItem;
	}

	@Override
	public void setSelectedItem(Object arg0) {
		if(YearSeparators.getInstance(associatedJournal).getUsedYears().contains(arg0)) {
			selectedItem = (Integer) arg0;
		}
		else if(arg0 == null) selectedItem = null;
	}

	@Override
	public void addListDataListener(ListDataListener arg0) {
		if(arg0 != null) listenerList.add(arg0);
	}

	@Override
	public Object getElementAt(int arg0) {
		Vector<Integer> list = YearSeparators.getInstance(associatedJournal).getUsedYears();
		if(arg0 >= 0 && arg0 < list.size()) return list.get(arg0);
		else return null;
	}

	@Override
	public int getSize() {
		return YearSeparators.getInstance(associatedJournal).getUsedYears().size() + 1;
	}

	@Override
	public void removeListDataListener(ListDataListener arg0) {
		listenerList.remove(arg0);
	}
	
	protected void fireContentsChanged() {
		setSelectedItem(selectedItem);
		for(ListDataListener l : listenerList) l.contentsChanged(new ListDataEvent(this,ListDataEvent.CONTENTS_CHANGED,0,getSize()));
	}

	@Override
	public void entriesAdded(Journal source, Entry[] newEntries) {
		fireContentsChanged();
	}

	@Override
	public void entriesRemoved(Journal source, Entry[] oldEntries) {
		fireContentsChanged();
	}

	@Override
	public void entryReplaced(Journal source, Entry oldEntry, Entry newEntry) {
		fireContentsChanged();
	}

	
	
}
