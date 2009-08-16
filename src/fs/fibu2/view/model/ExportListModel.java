package fs.fibu2.view.model;

import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import fs.fibu2.data.format.DefaultStringComparator;
import fs.fibu2.data.format.JournalExport;
import fs.fibu2.data.format.JournalExportLoader;

/**
 * Implements a simple list of {@link JournalExport}, sorted alphabetically by their name.
 * @author Simon Hampe
 *
 */
public class ExportListModel implements ListModel {

	private HashSet<ListDataListener> listenerList = new HashSet<ListDataListener>();
	
	private Vector<JournalExport> exports = new Vector<JournalExport>();
	
	public ExportListModel() {
		Comparator<JournalExport> comparator = new Comparator<JournalExport>() {
			@Override
			public int compare(JournalExport o1, JournalExport o2) {
				if(o1 == null && o2 == null) return 0;
				if(o1 == null || o2 == null) return -1;
				return (new DefaultStringComparator()).compare(o1.getName(), o2.getName());
			}
		};
		TreeSet<JournalExport> sortedSet = new TreeSet<JournalExport>(comparator);
		for(String id : JournalExportLoader.getExportIDs()) sortedSet.add(JournalExportLoader.getExport(id));
		exports = new Vector<JournalExport>(sortedSet);
	}
	
	@Override
	public void addListDataListener(ListDataListener l) {
		if(l != null) listenerList.add(l);
	}

	@Override
	public Object getElementAt(int index) {
		return exports.get(index);
	}

	@Override
	public int getSize() {
		return JournalExportLoader.getExportIDs().size();
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		listenerList.remove(l);
	}
	
	protected void fireContentsChanged() {
		for(ListDataListener l : listenerList) l.contentsChanged(new ListDataEvent(this,ListDataEvent.CONTENTS_CHANGED,0,getSize()-1));
	}

}
