package fs.fibu2.data.error;

import javax.swing.undo.CannotRedoException;

/**
 * This class extends CannotRedoException with the additional ability to store a message
 * @author Simon Hampe
 *
 */
public class RedoException extends CannotRedoException {

	/**
	 * compiler-generated serial version uid 
	 */
	private static final long serialVersionUID = -8789926717224366434L;
	
	private final String msg;
	
	public RedoException(String message) {
		super();
		msg = message;
	}

	@Override
	public String getMessage() {
		return msg;
	}
	
	
	
}
