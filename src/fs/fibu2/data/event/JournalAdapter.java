package fs.fibu2.data.event;

import fs.fibu2.data.model.Account;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.data.model.ReadingPoint;

/**
 * A class implementing all {@link JournalListener} methods, but doing nothing.
 * @author Simon Hampe
 *
 */
public class JournalAdapter implements JournalListener {

	@Override
	public void descriptionChanged(Journal source, String oldValue,
			String newValue) {
	}

	@Override
	public void entriesAdded(Journal source, Entry[] newEntries) {
	}

	@Override
	public void entriesRemoved(Journal source, Entry[] oldEntries) {
	}

	@Override
	public void entryReplaced(Journal source, Entry oldEntry, Entry newEntry) {
	}

	@Override
	public void nameChanged(Journal source, String oldValue, String newValue) {
	}

	@Override
	public void readingPointAdded(Journal source, ReadingPoint point) {
	}

	@Override
	public void readingPointRemoved(Journal source, ReadingPoint point) {
	}

	@Override
	public void startValueChanged(Journal source, Account a, Float oldValue,
			Float newValue) {
	}

	@Override
	public void activityChanged(ReadingPoint source) {
	}

	@Override
	public void dateChanged(ReadingPoint source) {
	}

	@Override
	public void nameChanged(ReadingPoint source) {
	}

	@Override
	public void visibilityChanged(ReadingPoint source) {
	}

}
	