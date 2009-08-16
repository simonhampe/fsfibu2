package fs.fibu2.filter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.prefs.Preferences;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fs.fibu2.data.model.Journal;
import fs.fibu2.view.render.JournalModule;

/**
 * Several {@link JournalModule}s use {@link StackFilter}s for their views and sometimes it might be useful to copy an existing filter into another
 * module without the need to recreate it from scratch. Hence each {@link JournalModule} can add/remove the StackFilters it uses to this pool.
 * They can then be retrieved by other modules. More precisely, the pool stores the filter and returns it, whenever it is requested. A requesting module
 * should always clone the filter retrieved to avoid side-effects, but there is no guarantee as to that.
 * @author Simon Hampe
 *
 */
public class FilterPool {

	//A list of instances per journal
	private static HashMap<Journal, FilterPool> pools = new HashMap<Journal, FilterPool>();
	
	//A list of stack filters, where they come from and which name they bear
	private HashMap<StackFilter, JournalModule> filterModules = new HashMap<StackFilter, JournalModule>();
	private HashMap<StackFilter, String> filterNames = new HashMap<StackFilter, String>();
	
	//A list of listeners
	private HashSet<ChangeListener> listenerList = new HashSet<ChangeListener>();
	
	// CONSTRUCTOR ************************
	// ************************************
	
	private FilterPool() {}
	
	/**
	 * @return The filter pool associated to a given journal. If it does not exist yet, it is created
	 */
	public static FilterPool getPool(Journal j) {
		FilterPool p = pools.get(j);
		if(p == null) {
			p = new FilterPool();
			pools.put(j, p);
		}
		return p;
	}
	
	// GETTERS / SETTERS ********************
	// **************************************
	
	/**
	 * Adds a filter to the pool. This call is ignored, if the filter is already in the pool
	 * @param filter The filter to be added. It will be stored in form of a {@link Preferences} node
	 * @param owner The module in which the filter was created. Only used for rendering purposes
	 * @param name The name of the filter. This is usually something like the title of the tab of the view associated to the filter
	 */
	public void addFilter(StackFilter filter, JournalModule owner, String name)  {
		if(!filterModules.containsKey(filter)) {
			filterModules.put(filter, owner);
			filterNames.put(filter, name);
			fireStateChanged();
		}
	}
	
	/**
	 * Changes the name of the filter (if it exists in this pool)
	 * @param filter The filter whose name should be changed
	 * @param newName The new name. If null, the empty string is used
	 */
	public void setFilterName(StackFilter filter, String newName) {
		if(filterNames.containsKey(filter)) filterNames.put(filter, newName == null? "" : newName);
		fireStateChanged();
	}
	
	/**
	 * Removes the given filter, if it is contained in the pool
	 */
	public void removeFilter(StackFilter filter) {
		if(filterModules.containsKey(filter)) {
			filterModules.remove(filter);
			filterNames.remove(filter);
			fireStateChanged();
		}
	}
	
	public HashSet<StackFilterTripel> getListOfFilters() {
		HashSet<StackFilterTripel> set = new HashSet<StackFilterTripel>();
		for(StackFilter f : filterModules.keySet()) {
			set.add(new StackFilterTripel(f,filterModules.get(f), filterNames.get(f)));
		}
		return set;
	}
	
	// LISTENER MECHANISM *********************
	// ****************************************
	
	/**
	 * Adds a change listener (if it isn't null), which is notified, whenever a filter is added or removed
	 */
	public void addChangeListener(ChangeListener l) {
		if(l != null) listenerList.add(l);
	}
	
	public void removeChangeListener(ChangeListener l) {
		listenerList.remove(l);
	}
	
	protected void fireStateChanged() {
		for(ChangeListener l : listenerList) l.stateChanged(new ChangeEvent(this));
	}
	
	// LOCAL INFORMATION CLASS ***************
	// ***************************************
	
	/**
	 * An information tripel containing a filter, its creator module and its name
	 */
	public class StackFilterTripel  {
	
		public StackFilter filter;
		public JournalModule module;
		public String name;
	
		public StackFilterTripel(StackFilter f, JournalModule m, String n) {
			filter = f;
			module = m;
			name = n;
		}
		
		
	}
	
}
