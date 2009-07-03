package fs.fibu2.data.event;

import fs.event.DocumentChangeFlag;
import fs.fibu2.data.model.Account;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.data.model.ReadingPoint;

/**
 * A document change flag for {@link Journal}
 * @author Simon Hampe
 *
 */
public class JournalChangeFlag extends DocumentChangeFlag implements JournalListener {

	@Override
	public void descriptionChanged(Journal source, String oldValue,
			String newValue) {
		setChangeFlag(true);		
	}

	@Override
	public void entriesAdded(Journal source, Entry[] newEntries) {
		setChangeFlag(true);
	}

	@Override
	public void entriesRemoved(Journal source, Entry[] oldEntries) {
		setChangeFlag(true);
	}

	@Override
	public void entryReplaced(Journal source, Entry oldEntry, Entry newEntry) {
		setChangeFlag(true);
	}

	@Override
	public void nameChanged(Journal source, String oldValue, String newValue) {
		setChangeFlag(true);
	}

	@Override
	public void readingPointAdded(Journal source, ReadingPoint point) {
		setChangeFlag(true);
	}

	@Override
	public void readingPointRemoved(Journal source, ReadingPoint point) {
		setChangeFlag(true);
	}

	@Override
	public void startValueChanged(Journal source, Account a, Float oldValue,
			Float newValue) {
		setChangeFlag(true);
	}

	@Override
	public void dateChanged(ReadingPoint source) {
		setChangeFlag(true);
	}

	@Override
	public void nameChanged(ReadingPoint source) {
		setChangeFlag(true);
	}

	
	
}
