package fs.fibu2.view.model;

import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import fs.fibu2.data.event.JournalListener;
import fs.fibu2.data.model.Account;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.data.model.ReadingPoint;

/**
 * Implements a list of reading points of a journal, sorted by date and afterwards by name
 * @author Simon Hampe
 *
 */
public class ReadingPointModel implements ListModel, JournalListener {

	private HashSet<ListDataListener> listenerList = new HashSet<ListDataListener>();
	
	private Vector<ReadingPoint> pointList = new Vector<ReadingPoint>();
	
	private Journal associatedJournal;
	
	private Comparator<ReadingPoint> comparator = new Comparator<ReadingPoint>() {
		@Override
		public int compare(ReadingPoint o1, ReadingPoint o2) {
			if(o1 == null && o2 == null) return 0;
			if(o1 == null || o2 == null) return -1;
			int r1 = o1.getReadingDay().compareTo(o2.getReadingDay());
			if(r1 != 0) return r1;
			int r2 = o1.getName().compareTo(o2.getName());
			if(r2 != 0) return r2;
			else return new Integer(o1.hashCode()).compareTo(new Integer(o2.hashCode()));
		}
	};
	
	// CONSTRUCTOR *********************
	// *********************************
	
	public ReadingPointModel(Journal j) {
		associatedJournal = j;
		if(j != null) associatedJournal.addJournalListener(this);
		calculateContent();
	}
	
	// LISTMODEL ***********************
	// *********************************
	
	@Override
	public void addListDataListener(ListDataListener arg0) {
		if(arg0 != null) listenerList.add(arg0);
	}

	@Override
	public Object getElementAt(int arg0) {
		return pointList.get(arg0);
	}

	@Override
	public int getSize() {
		return pointList.size();
	}

	@Override
	public void removeListDataListener(ListDataListener arg0) {
		listenerList.remove(arg0);
	}
	
	protected void calculateContent() {
		TreeSet<ReadingPoint> points = new TreeSet<ReadingPoint>(comparator);
		if(associatedJournal != null) points.addAll(associatedJournal.getReadingPoints());
		pointList = new Vector<ReadingPoint>(points);
		fireListChanged();
	}
	
	protected void fireListChanged() {
		for(ListDataListener l : listenerList) l.contentsChanged(new ListDataEvent(this,ListDataEvent.CONTENTS_CHANGED,0,getSize()-1));
	}
	
	// JOURNALLISTENER **********************
	// **************************************

	@Override
	public void descriptionChanged(Journal source, String oldValue,
			String newValue) {
		//Ignored
	}

	@Override
	public void entriesAdded(Journal source, Entry[] newEntries) {
		//Ignored
	}

	@Override
	public void entriesRemoved(Journal source, Entry[] oldEntries) {
		//Ignored
	}

	@Override
	public void entryReplaced(Journal source, Entry oldEntry, Entry newEntry) {
		//Ignored
	}

	@Override
	public void nameChanged(Journal source, String oldValue, String newValue) {
		//Ignored
	}

	@Override
	public void readingPointAdded(Journal source, ReadingPoint point) {
		calculateContent();
	}

	@Override
	public void readingPointRemoved(Journal source, ReadingPoint point) {
		calculateContent();
	}

	@Override
	public void startValueChanged(Journal source, Account a, Float oldValue,
			Float newValue) {
		//Ignored
	}

	@Override
	public void dateChanged(ReadingPoint source) {
		calculateContent();
	}

	@Override
	public void nameChanged(ReadingPoint source) {
		calculateContent();
	}

}
