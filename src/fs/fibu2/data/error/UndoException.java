package fs.fibu2.data.error;

import javax.swing.undo.CannotUndoException;


/**
 * This class extends CannotUndoException with the additional ability to store a message
 * @author Simon Hampe
 *
 */
public class UndoException extends CannotUndoException {

	/**
	 * compiler-generated version uid 
	 */
	private static final long serialVersionUID = 472628627410487382L;
	
	private final String msg;
	
	public UndoException(String message) {
		super();
		msg = message;
	}

	@Override
	public String getMessage() {
		return msg;
	}	
}
