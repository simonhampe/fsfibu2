package fs.fibu2.filter;

import java.util.prefs.Preferences;

import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;

/**
 * An EntryFilter is an object which can filter entries according to a certain criterion.
 * It optionally provides a graphical component (a panel) for editing. Each class
 * implementing this interface should have a no-argument constructor, 
 * otherwise the FilterLoader will not be able to create an instance. This interface also contains two methods for 
 * (de)serializing filters in form of a {@link Preferences} node.   
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
	 * from the FilterLoader. The convention for fsfibu2 filters is "ff2filter_" + 
	 * something descriptive like "name" or "onlywithbeer".
	 */
	public String getID();
	
	/**
	 * @return Whether e fulfills the criteria of this filter. The null entry should
	 * verify no criterion at all.
	 */
	public boolean verifyEntry(Entry e);
	
	/**
	 * @param j The editor might need data about the journal in which it filters (e.g. a list of available categories). 
	 * @return An editor for the filter. If the filter should not be editable, this should return null.
	 * The editor component should already be filled with the data of the current configuration.
	 */
	public EntryFilterEditor getEditor(Journal j);
	
	/**
	 * Inserts this filter's configuration into the given node. The format should always be as follows: <br>
	 * - A node with name 'filter' should be inserted in node. All further nodes are inserted within this node<br>
	 * - In its map, this node should have an entry with key 'id' and value as returned by {@link EntryFilter}{@link #getID()}. This is necessary
	 * to be able to create filters from their preference nodes
	 * @param node The node under which this filter's configuration should be inserted
	 * @throws NullPointerException - If node == null
	 */
	public void insertMyPreferences(Preferences node) throws NullPointerException;
	
	/**
	 * Creates a new filter from the given {@link Preferences} object, which (supposedly) has been created by calling the method {@link EntryFilter}{@link #insertMyPreferences(Preferences)}
	 * before. 
	 * @param filterNode This should be the node of name filter which was inserted by the {@link EntryFilter}{@link #insertMyPreferences(Preferences)} method.
	 * @return An entry filter conforming to the given preferences. 
	 * @throws IllegalArgumentException - If the given node is null or does not contain a valid configuration
	 */
	public EntryFilter createMeFromPreferences(Preferences filterNode) throws IllegalArgumentException;
	
}
