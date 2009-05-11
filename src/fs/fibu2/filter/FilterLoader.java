package fs.fibu2.filter;

import java.util.HashMap;

/**
 * This class supplies static methods to retrieve instances of {@link EntryFilter} objects using their ID. There also is a method for
 * dynamically adding a class implementing this interface. 
 * @author Simon Hampe
 *
 */
public final class FilterLoader {

	//Map of String (ID) -> class object for instance retrieval
	private static HashMap<String, Class<? extends EntryFilter>> filterMap = new HashMap<String, Class<? extends EntryFilter>>();
	
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
			}
		}
		catch(Exception e) {
			throw new UnsupportedOperationException(e);
		}
	}
	
}
