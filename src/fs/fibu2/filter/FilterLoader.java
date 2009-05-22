package fs.fibu2.filter;

import java.util.HashMap;
import java.util.HashSet;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * This class supplies static methods to retrieve instances of {@link EntryFilter} objects using their ID. There also is a method for
 * dynamically adding a class implementing this interface. 
 * @author Simon Hampe
 *
 */
public final class FilterLoader {

	//Map of String (ID) -> class object for instance retrieval
	private static HashMap<String, Class<? extends EntryFilter>> filterMap = new HashMap<String, Class<? extends EntryFilter>>();
	
	//Listeners which listen to changes in the filter loader's map
	private static HashSet<ChangeListener> listeners = new HashSet<ChangeListener>();
	
	//Init code, load all basic filters
	static {
		filterMap.put(new NameFilter().getID(), NameFilter.class);
		filterMap.put(new ValueFilter().getID(), ValueFilter.class);
		filterMap.put(new DateFilter().getID(), DateFilter.class);
		filterMap.put(new CategoryFilter().getID(), CategoryFilter.class);
		filterMap.put(new AccountFilter().getID(), AccountFilter.class);
		filterMap.put(new AccountInformationFilter().getID(), AccountInformationFilter.class);
		filterMap.put(new AdditionalInformationFilter().getID(), AdditionalInformationFilter.class);
	}
	
	/**
	 * Creates an instance of the filter associated to this id and returns it
	 * @throws IllegalArgumentException - If there is no filter class for this id
	 * @throws InstantiationException - If the nullary constructor or the class can not be accessed 
	 */
	public static EntryFilter getFilter(String id) throws IllegalArgumentException, InstantiationException{
		if(id == null) throw new NullPointerException("Null id invalid for filter creation");
		try {
			return (filterMap.get(id).newInstance());
		}
		catch(NullPointerException ne)  {
			throw new IllegalArgumentException("No filter class found for id '" + id + "'");
		} catch (IllegalAccessException e) {
			throw new InstantiationException(e.getMessage());
		} 
	}
	
	/**
	 * Sets the filter class for the given id. If there already is a class for this id, it is overwritten. If filter == null, the corresponding class
	 * is removed. If id == null, this call is ignored
	 */
	public static void setFilter(String id, Class<? extends EntryFilter> filter) {
		if(id == null) return;
		if(filter == null) filterMap.remove(id);
		else filterMap.put(id, filter);
		fireStateChanged(new ChangeEvent(id));
	}
	
	/**
	 * This tries to create an EntryFilter instance from the given class and will then create a mapping for its id. If there is already a mapping for this
	 * id, this call is ignored
	 * @throws UnsupportedOperationException - If any error occurs during instantiation
	 */
	public static void loadFilter(Class<?> filterClass) {
		try {
			EntryFilter f = (EntryFilter) filterClass.newInstance();
			if(!filterMap.containsKey(f.getID())) {
				filterMap.put(f.getID(), f.getClass());
				fireStateChanged(new ChangeEvent(f.getID()));
			}
		}
		catch(Exception e) {
			throw new UnsupportedOperationException(e);
		}
	}
	
	/**
	 * @return A list of all ids for which this loader has a mapping
	 */
	public static HashSet<String> getFilterIDs() {
		return new HashSet<String>(filterMap.keySet());
	}
	
	// LISTENER METHODS ********************************
	// *************************************************
	
	/**
	 * Adds a listener to this class. Listeners are notified, whenever a filter is added to or removed from the loader. 
	 * The change event contains the corresponding filter id as source 
	 */
	public static void addChangeListener(ChangeListener l) {
		if(l != null) listeners.add(l);
	}
	
	/**
	 * Removes l as a listener from this class
	 */
	public static void removeChangeListener(ChangeListener l) {
		listeners.remove(l);
	}
	
	protected static void fireStateChanged(ChangeEvent e) {
		for(ChangeListener l : listeners) l.stateChanged(e);
	}
	
}
