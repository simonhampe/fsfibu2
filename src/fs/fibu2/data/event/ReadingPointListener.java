package fs.fibu2.data.event;

import java.util.EventListener;

import fs.fibu2.data.model.ReadingPoint;

/**
 * A class implementing ReadingPointListener reacts to changes to a {@link ReadingPoint}.
 * @author Simon Hampe
 *
 */
public interface ReadingPointListener extends EventListener{
	
	/**
	 * Indicates that the date of source has been changed
	 */
	public void dateChanged(ReadingPoint source);
	
	/**
	 * Indicates that the name of source has changed
	 */
	public void nameChanged(ReadingPoint source);
}
