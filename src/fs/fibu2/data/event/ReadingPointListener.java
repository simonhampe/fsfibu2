package fs.fibu2.data.event;

import fs.fibu2.data.model.ReadingPoint;

/**
 * A class implementing ReadingPointListener reacts to changes to a {@link ReadingPoint}.
 * @author Simon Hampe
 *
 */
public interface ReadingPointListener {

	/**
	 * Indicates that source's visibility has been changed
	 */
	public void visibilityChanged(ReadingPoint source);
	
	/**
	 * Indicates that source's 'active' flag has been changed
	 */
	public void activityChanged(ReadingPoint source);
	
	/**
	 * Indicates that the date of source has been changed
	 */
	public void dateChanged(ReadingPoint source);
	
	/**
	 * Indicates that the name of source has changed
	 */
	public void nameChanged(ReadingPoint source);
}
