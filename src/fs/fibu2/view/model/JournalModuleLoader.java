package fs.fibu2.view.model;

import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fs.fibu2.filter.EntryFilter;

/**
 * This class handles loading of {@link JournalModule} objects by ID. It also supports dynamic loading of JournalModule classes.
 * @author Simon Hampe
 *
 */
public class JournalModuleLoader {

	//The map of all class objects associated to their IDs
	private static HashMap<String, Class<? extends JournalModule>> moduleMap = new HashMap<String, Class<? extends JournalModule>>();
	//The set of all listeners
	private static HashSet<ChangeListener> listenerList = new HashSet<ChangeListener>();
	
	//Init all default modules
	static {
		
	}
	
	/**
	 * Creates an instance of the module associated to this id and returns it
	 * @throws IllegalArgumentException - If there is no module class for this id
	 * @throws InstantiationException - If the nullary constructor or the class can not be accessed 
	 */
	public static JournalModule getModule(String id) throws IllegalArgumentException, InstantiationException{
		if(id == null) throw new NullPointerException("Null id invalid for module creation");
		try {
			return (moduleMap.get(id).newInstance());
		}
		catch(NullPointerException ne)  {
			throw new IllegalArgumentException("No module class found for id '" + id + "'");
		} catch (IllegalAccessException e) {
			throw new InstantiationException(e.getMessage());
		} 
	}
	
	/**
	 * Sets the module class for the given id. If there already is a class for this id, it is overwritten. If module == null, the corresponding class
	 * is removed. If id == null, this call is ignored
	 */
	public static void setFilter(String id, Class<? extends JournalModule> module) {
		if(id == null) return;
		if(module == null) moduleMap.remove(id);
		else moduleMap.put(id, module);
		fireStateChanged(new ChangeEvent(id));
	}
	
	/**
	 * This tries to create a {@link JournalModule} instance from the given class and will then create a mapping for its id. If there is already a mapping for this
	 * id, this call is ignored
	 * @throws UnsupportedOperationException - If any error occurs during instantiation
	 */
	public static void loadModule(Class<?> moduleClass) {
		try {
			JournalModule f = (EntryFilter) moduleClass.newInstance();
			if(!moduleMap.containsKey(f.getID())) {
				moduleMap.put(f.getID(), f.getClass());
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
	
}
