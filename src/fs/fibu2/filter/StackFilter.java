package fs.fibu2.filter;

import java.util.HashSet;
import java.util.Vector;

import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.lang.Fsfibu2StringTableMgr;

/**
 * This class implements a stack of filters. Filters can be added / removed via the editor. Each filter can be (de)activated, negated and edited via
 * its own editor which is added to the stack filter's editor as a subcomponent. Finally, an entry will be admitted by this filter, if it is admitted
 * by all active filters in its stack.
 * @author Simon Hampe
 *
 */
public class StackFilter implements EntryFilter {

	//The stack of filters 
	private Vector<EntryFilter> filterStack = new Vector<EntryFilter>();
	//All filters in this set are active, all others are not
	private HashSet<EntryFilter> isActive = new HashSet<EntryFilter>();
	//All filters in this set are negated, all others are not
	private HashSet<EntryFilter> isNegated = new HashSet<EntryFilter>();
	
	//TODO: Add editor variable
	
	// CONSTRUCTOR ****************************
	// ****************************************
	
	public StackFilter() {
		
	}
	
	// FILTER METHODS *************************
	// ****************************************
	
	@Override
	public String getDescription() {
		return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.StackFilter.description");
	}

	@Override
	public EntryFilterEditor getEditor(Journal j) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getID() {
		return "ff2filter_stack";
	}

	@Override
	public String getName() {
		return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.StackFilter.name");
	}

	/**
	 * @return true, if and only if e != null and all active filters verify e (i.e. return true, if they are not
	 * negated, false otherwise)
	 */
	@Override
	public boolean verifyEntry(Entry e) {
		if(e == null) return false;
		for(EntryFilter f : filterStack) {
			if(!(isActive.contains(f) && (f.verifyEntry(e) ^ isNegated.contains(f)))) return false;
		}
		return true;
	}

}
