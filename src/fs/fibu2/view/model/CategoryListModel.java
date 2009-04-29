package fs.fibu2.view.model;

import java.util.TreeSet;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.SwingWorker;

import fs.fibu2.data.event.JournalListener;
import fs.fibu2.data.model.Account;
import fs.fibu2.data.model.Category;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.data.model.ReadingPoint;

/**
 * This class implements a simple list of categories of a journal. It listens to the journal and reloads its content, every time the journal changes.
 * It orders the categories according to ordering induced by its Comparable interface. Recalculation of the list's content is done via a SwingWorker
 * object on a separate thread, so the application remains responsive
 * @author Simon Hampe
 *
 */
public class CategoryListModel extends AbstractListModel implements JournalListener {

	// FIELDS *********************
	// ****************************
	
	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = 1213941826365601172L;
	
	private Journal associatedJournal;
	private Vector<Category> listOfCategories;
	
	// CONSTRUCTORS *******************
	// ********************************
	
	/**
	 * Creates a list model representing a list of categories of a given journal. If associatedJournal == null, 
	 * an empty dummy journal will be created internally, so the category list will be empty
	 */
	public CategoryListModel(Journal associatedJournal) {
		this.associatedJournal = associatedJournal == null? new Journal() : associatedJournal;
		this.associatedJournal.addJournalListener(this);
		listOfCategories = new Vector<Category>(new TreeSet<Category>(this.associatedJournal.getListOfCategories()));
	}
	
	// LISTMODE INTERFACE *************
	// ********************************	
	
	@Override
	public Object getElementAt(int index) {
		return listOfCategories.get(index);
	}

	@Override
	public int getSize() {
		return listOfCategories.size();
	}

	// LISTENER INTERFACE **********************************
	// *****************************************************
	
	protected void recalculateModel() {
		(new Recalculator()).execute();
	}
	
	protected void fireContentsChanged() {
		fireContentsChanged(this,0,listOfCategories.size()-1);
	}
	
	@Override
	public void descriptionChanged(Journal source, String oldValue,
			String newValue) {
		//Ignored	
	}

	@Override
	public void entriesAdded(Journal source, Entry[] newEntries) {
		recalculateModel();
	}

	@Override
	public void entriesRemoved(Journal source, Entry[] oldEntries) {
		recalculateModel();
	}

	@Override
	public void entryReplaced(Journal source, Entry oldEntry, Entry newEntry) {
		recalculateModel();
	}

	@Override
	public void nameChanged(Journal source, String oldValue, String newValue) {
		//Ignored
	}

	@Override
	public void readingPointAdded(Journal source, ReadingPoint point) {
		//Ignored
	}

	@Override
	public void readingPointRemoved(Journal source, ReadingPoint point) {
		//Ignored
	}

	@Override
	public void startValueChanged(Journal source, Account a, Float oldValue,
			Float newValue) {
		//Ignored
	}

	@Override
	public void activityChanged(ReadingPoint source) {
		//Ignored
	}

	@Override
	public void dateChanged(ReadingPoint source) {
		//Ignored
	}

	@Override
	public void nameChanged(ReadingPoint source) {
		//Ignored
	}

	@Override
	public void visibilityChanged(ReadingPoint source) {
		//Ignored
	}

	// SWING WORKER CLASS FOR RECALCULATION *******************************
	// ********************************************************************
	
	/**
	 * This class recalculates the list of categories, whenever the journal is changed
	 */
	private class Recalculator extends SwingWorker<Vector<Category>, Vector<Category>> {
		
		@Override
		protected Vector<Category> doInBackground() throws Exception {
			return new Vector<Category>(new TreeSet<Category>(associatedJournal.getListOfCategories()));
		}

		@Override
		protected void done() {
			try {
				listOfCategories = get();
				fireContentsChanged();
			} catch (Exception e) {
				//Ignored, will not happen
			} 			
		}	
	}
	
}
