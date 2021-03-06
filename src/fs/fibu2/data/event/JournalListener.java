package fs.fibu2.data.event;

import java.util.EventListener;

import fs.fibu2.data.model.Account;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.data.model.ReadingPoint;

/**
 * All objects listening to changes in an fsfibu2 journal implement this interface
 * @author Simon Hampe
 *
 */
public interface JournalListener extends ReadingPointListener, EventListener {
	
	/**
	 * This is called, whenever some entries are added
	 * @param source The journal to which the entries were added
	 * @param newEntries The added entries 
	 */
	public void entriesAdded(Journal source, Entry[] newEntries);
	
	/**
	 * This is called, whenever some entries are removed
	 * @param source The journal from which the entries are removed
	 * @param oldEntry The removed entries. 
	 */
	public void entriesRemoved(Journal source, Entry[] oldEntries);
	
	/**
	 * This is called, whenever one entry is replaced by another
	 * @param source The journal in which the replacement took place
	 * @param oldEntry The old entry
	 * @param newEntry The new entry
	 */
	public void entryReplaced(Journal source, Entry oldEntry, Entry newEntry);
	
	/**
	 * This is called, whenever a start value is added, removed or changed
	 * @param source The journal in which the change took place
	 * @param a The account for which the value was changed
	 * @param oldValue The old value. Null, if the value was newly added
	 * @param newValue The new value. Null, if the value was removed
	 */
	public void startValueChanged(Journal source, Account a,  Float oldValue, Float newValue);
	
	/**
	 * This is called, whenever a reading point is added
	 * @param source The journal to which the point was added
	 * @param point The new point
	 */
	public void readingPointAdded(Journal source, ReadingPoint point);
	
	/**
	 * This is called, whenever a reading point is removed
	 * @param source The journal from which it was removed
	 * @param point The removed point
	 */
	public void readingPointRemoved(Journal source, ReadingPoint point);
	
	/**
	 * This is called, when the name of a journal is changed.
	 * @param source The journal whose name was changed
	 * @param oldValue The old name
	 * @param newValue The new name
	 */
	public void nameChanged(Journal source, String oldValue, String newValue);
	
	/**
	 * This is called when the description of the journal is changed
	 * @param source The journal whose description is changed
	 * @param oldValue The old description
	 * @param newValue The new description
	 */
	public void descriptionChanged(Journal source, String oldValue, String newValue);
	
}
