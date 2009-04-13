package fs.fibu2.undo;

import java.util.HashMap;
import java.util.HashSet;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import fs.fibu2.data.model.Journal;

/**
 * This class implements an UndoManager for fsfibu2 journals. There will be at most one UndoManager for each journal instance, 
 * which can be retrieved via a getInstance method
 * @author Simon Hampe
 *
 */
public class JournalUndoManager extends UndoManager {

	// FIELDS ****************************
	// ***********************************
	
	/**
	 * compiler-generated version uid
	 */
	private static final long serialVersionUID = -6199094622322353658L;
	
	private static HashMap<Journal,JournalUndoManager> managers = new HashMap<Journal, JournalUndoManager>();
	private HashSet<UndoableEditListener> listeners = new HashSet<UndoableEditListener>();
	
	// CONSTRUCTOR ***********************
	// *********************************** 
	
	protected JournalUndoManager() {
		
	}
	
	// STATIC RETRIEVAL **********************
	// ***************************************
	
	/**
	 * @return The instance associated to the journal j. If there is none, it is created. If j == null, this returns null.
	 */
	public static JournalUndoManager getInstance(Journal j) {
		if(j == null) return null;
		JournalUndoManager mgr = managers.get(j);
		if(mgr == null) {
			mgr = new JournalUndoManager();
			managers.put(j, mgr);
		}
		return mgr;
	}
	
	// OVERWRITTEN METHODS ********************
	// ****************************************
	
	@Override
	public synchronized boolean addEdit(UndoableEdit anEdit) {
		boolean r = super.addEdit(anEdit);
		fireUndoableEditHappened(new UndoableEditEvent(this,anEdit));
		return r;
	}

	@Override
	public synchronized void discardAllEdits() {
		super.discardAllEdits();
		fireUndoableEditHappened(new UndoableEditEvent(this,null));
	}

	@Override
	public synchronized void end() {
		super.end();
		fireUndoableEditHappened(new UndoableEditEvent(this,null));
	}

	@Override
	public synchronized void redo() throws CannotRedoException {
		super.redo();
		fireUndoableEditHappened(new UndoableEditEvent(this,null));
	}

	@Override
	protected void redoTo(UndoableEdit edit) throws CannotRedoException {
		super.redoTo(edit);
		fireUndoableEditHappened(new UndoableEditEvent(this,null));
	}

	@Override
	public synchronized void setLimit(int l) {
		super.setLimit(l);
		fireUndoableEditHappened(new UndoableEditEvent(this,null));
	}

	@Override
	protected void trimEdits(int from, int to) {
		super.trimEdits(from, to);
		fireUndoableEditHappened(new UndoableEditEvent(this,null));
	}

	@Override
	protected void trimForLimit() {
		super.trimForLimit();
		fireUndoableEditHappened(new UndoableEditEvent(this,null));
	}

	@Override
	public synchronized void undo() throws CannotUndoException {
		super.undo();
		fireUndoableEditHappened(new UndoableEditEvent(this,null));
	}

	@Override
	public synchronized void undoOrRedo() throws CannotRedoException,
			CannotUndoException {
		super.undoOrRedo();
		fireUndoableEditHappened(new UndoableEditEvent(this,null));
	}
	
	

	@Override
	protected void undoTo(UndoableEdit edit) throws CannotUndoException {
		super.undoTo(edit);
		fireUndoableEditHappened(new UndoableEditEvent(this,null));
	}

	// LISTENER METHODS ****************************
	// *********************************************
	
	
	protected void fireUndoableEditHappened(UndoableEditEvent e) {
		for(UndoableEditListener l : listeners) l.undoableEditHappened(e);
	}
	
	public void addUndoableEditListener(UndoableEditListener l) {
		if(l != null) listeners.add(l);
	}
	
	public void removeUndoableEditListener(UndoableEditListener l) {
		listeners.remove(l);
	}
	
}

