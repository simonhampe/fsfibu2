package fs.fibu2.filter;

import fs.fibu2.data.model.Entry;

/**
 * An EntryFilter is an object which can filter entries according to a certain criterion.
 * It optionally provides a graphical component (a panel) for editing 
 * @author Simon Hampe
 *
 */
public interface EntryFilter {
	
	/**
	 * @return The name of this filter, e.g. 'Date range filter'
	 */
	public String getName();
	
	/**
	 * @return Whether e fulfills the criteria of this filter
	 */
	public boolean verifyEntry(Entry e);
	
	/**
	 * @return An editor for the filter. If the filter should not be editable, this should return null
	 */
	public EntryFilterEditor<? extends EntryFilter> getEditor();
	
}
