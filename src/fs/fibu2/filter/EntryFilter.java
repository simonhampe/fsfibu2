package fs.fibu2.filter;

import fs.fibu2.data.model.Entry;

/**
 * An EntryFilter is an object which can filter entries according to a certain criterion.
 * It optionally provides a graphical component (a panel) for editing. Each class
 * implementing this interface should have a no-argument constructor, otherwise the
 * FilterLoader will not be able to create an instance. 
 * @author Simon Hampe
 *
 */
public interface EntryFilter {
	
	/**
	 * @return The general name of this filter's type, e.g. 'Date range filter'
	 */
	public String getName();
	
	/**
	 * @return An <i>exact</i> description of this filter, e.g. 'Date range filter 
	 * between 1.1.2009 and 23.4.2010'
	 */
	public String getDescription();
	
	/**
	 * @return A (possibly) unique ID used for retrieving an instance of this filter
	 * from the FilterLoader
	 */
	public String getID();
	
	/**
	 * @return Whether e fulfills the criteria of this filter
	 */
	public boolean verifyEntry(Entry e);
	
	/**
	 * @return An editor for the filter. If the filter should not be editable, this should return null.
	 * The editor component should already be filled with the data of the current configuration.
	 */
	public EntryFilterEditor getEditor();
	
}
