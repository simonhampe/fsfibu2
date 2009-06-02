package fs.fibu2.view.event;

import fs.fibu2.data.model.Journal;
import fs.fibu2.data.model.ReadingPoint;
import fs.fibu2.view.model.YearSeparators;

/**
 * Listens to a {@link YearSeparators} object for changes.
 * @author Simon Hampe
 *
 */
public interface YearSeparatorListener {

	/**
	 * Indicates that a given separator has been added for the given journal
	 */
	public void separatorAdded(Journal source, ReadingPoint yearSeparator);
	
	/**
	 * Indicates that a given separator has been removed for the given journal
	 */
	public void separatorRemoved(Journal source, ReadingPoint yearSeparator);
}
