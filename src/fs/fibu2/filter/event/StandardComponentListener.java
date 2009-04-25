package fs.fibu2.filter.event;

import fs.fibu2.filter.StandardFilterComponent;
import fs.fibu2.filter.StandardFilterComponent.Selection;

/**
 * A StandardComponentListener listens to changes in a {@link StandardFilterComponent}.
 * @author Simon Hampe
 *
 */
public interface StandardComponentListener {

	/**
	 * Notifies of a change in the selection of the filter type
	 */
	public void selectionChanged(StandardFilterComponent source, Selection newSelection);
	
	/**
	 * Notifies of a change in the content of one of the entry fields
	 */
	public void contentChanged(StandardFilterComponent source);
	
}
