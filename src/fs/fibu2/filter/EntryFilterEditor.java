package fs.fibu2.filter;

import java.util.HashSet;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * An EntryFilterEditor provides a graphical interface for editing a filter. It
 * can check at any time, whether the current content is valid or not, it provides a
 * listener mechanism for changes in the data and can return the filter associated to
 * its content (if it is valid).
 * @author Simon Hampe
 *
 */
public abstract class EntryFilterEditor extends JPanel{

	/**
	 * Compiler-generated serial version UID 
	 */
	private static final long serialVersionUID = 4365873600172270982L;
	
	//A list of listeners
	private HashSet<ChangeListener> listeners = new HashSet<ChangeListener>();
	
	/**
	 * @return Whether the content of the editor is valid content, e.g. all
	 * fields where a number should be inserted, contain data which can be parsed to
	 * an Integer
	 */
	public abstract boolean isValid();
	
	/**
	 *  @return The filter associated to the current content. If the content is not valid,
	 *  this should return null.
	 */
	public abstract EntryFilter getFilter();
	
	/**
	 * Adds l to the list of change listeners, if it isn't null. {@link ChangeListener}s
	 * are notified, whenever the content of the editor changes, such that the associated
	 * filter changes
	 */
	public void addChangeListener(ChangeListener l) {
		if(l != null) listeners.add(l);
	}
	
	/**
	 * Removes l from the list of change listeners.
	 */
	public void removeChangeListener(ChangeListener l) {
		listeners.remove(l);
	}
	
	/**
	 * Notifies all listeners, that the content of the editor has changes, such that
	 * the associated filter is affected
	 */
	protected void fireStateChanged() {
		for(ChangeListener l : listeners) l.stateChanged(new ChangeEvent(this));
	}
	
}
