package fs.fibu2.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import fs.fibu2.data.error.RedoException;
import fs.fibu2.data.error.UndoException;
import fs.fibu2.data.model.Journal;
import fs.fibu2.data.model.ReadingPoint;
import fs.fibu2.lang.Fsfibu2StringTableMgr;

/**
 * This class represents an edit which removes a reading point from a journal or adds one. It does not care, whether
 * this edit has any effect, i.e. if you add a point which is already in the journal, this edit will still redo successfully.
 * @author Simon Hampe
 *
 */
public class UndoableJournalPointEdit extends AbstractUndoableEdit {

	/**
	 * compiler-generated version uid 
	 */
	private static final long serialVersionUID = -4174533041320205224L;
	
	private Journal journal;		//The journal to which the changes are made
	private ReadingPoint point;		//The reading point being added/removed
	private boolean isAdded;		//Whether the reading point is added. Otherwise it is removed
	
	private final static String sgroup = "fs.fibu2.undo.JournalPointEdit";

	/**
	 * Creates an edit which removes a reading point from a journal or adds it
	 * @param journal The journal to which the changes are made
	 * @param point The reading point being added/removed
	 * @param isAdded Whether the reading point is added. Otherwise it is removed
	 */
	public UndoableJournalPointEdit(Journal journal, ReadingPoint point,
			boolean isAdded) {
		super();
		this.journal = journal;
		this.point = point;
		this.isAdded = isAdded;
	}

	@Override
	public String getPresentationName() {
		return Fsfibu2StringTableMgr.getString(sgroup + ".presentation",
				isAdded? "fs.fibu2.undo.add": "fs.fibu2.undo.remove",
				point == null? "?" : point.getName(),
				journal == null? "?" : journal.getName());
	}

	@Override
	public String getRedoPresentationName() {
		return getPresentationName();
	}

	@Override
	public String getUndoPresentationName() {
		return Fsfibu2StringTableMgr.getString(sgroup + ".presentation",
				!isAdded? Fsfibu2StringTableMgr.getString("fs.fibu2.undo.add") : Fsfibu2StringTableMgr.getString("fs.fibu2.undo.remove"),
				point == null? "?" : point.getName(),
				journal == null? "?" : journal.getName());
	}

	/**
	 * Tries to add/remove the reading point from the journal
	 * @throws RedoException - If journal == null
	 */
	@Override
	public void redo() throws RedoException {
		try {
			super.redo();
		}
		catch(CannotRedoException ce) {
			throw new RedoException("Cannot redo. Edit is not alive or has already been done.");
		}
		if(journal == null) throw new RedoException("Cannot undo on null journal.");
		if(isAdded) journal.addReadingPoint(point);
		else journal.removeReadingPoint(point);
	}

	/**
	 * Tries to add/remove the reading point from the journal
	 * @throws UndoException - If journal == null
	 */
	@Override
	public void undo() throws UndoException {
		try {
			super.undo();
		}
		catch(CannotUndoException ce) {
			throw new UndoException("Cannot undo. Edit is not alive or has not been done.");
		}
		if(journal == null) throw new UndoException("Cannot undo on null journal.");
		if(isAdded) journal.removeReadingPoint(point);
		else journal.addReadingPoint(point);
	}
	
	/**
	 * @return true
	 */
	@Override
	public boolean canRedo() {
		return true;
	}

	/**
	 * @return true
	 */
	@Override
	public boolean canUndo() {
		return true;
	}	
}
