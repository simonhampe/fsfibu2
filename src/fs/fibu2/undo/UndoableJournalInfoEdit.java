package fs.fibu2.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import fs.fibu2.data.error.RedoException;
import fs.fibu2.data.error.UndoException;
import fs.fibu2.data.model.Journal;
import fs.fibu2.lang.Fsfibu2StringTableMgr;

/**
 * This class represents an edit which changes the value of the name OR description of an fsfibu2 journal. It is represented by an old
 * and a new value. The edit does not care whether the value actually is identical to the old one before redo (or the new one before undo).
 * It justs set the appropriate value.
 * @author Simon Hampe
 *
 */
public class UndoableJournalInfoEdit extends AbstractUndoableEdit {

	/**
	 * compiler-generated version uid 
	 */
	private static final long serialVersionUID = -2532864761312222352L;
	
	private Journal journal;		//The journal to which the changes are made
	private String oldValue; 		//The value to set on undo
	private String newValue; 		//The value to set on redo
	private boolean changesName;	//Whether the name is changed. Otherwise this edit changes the description
	
	private final static String sgroup = "fs.fibu2.undo.JournalInfoEdit";

	/**
	 * Creates an edit which changes the name or description of a journal
	 * @param journal The journal to which the changes are made
	 * @param oldValue The value to set on undo
	 * @param newValue The value to set on redo
	 * @param changesName Whether the name is changed. Otherwise this edit changes the description
	 */
	public UndoableJournalInfoEdit(Journal journal, String oldValue,
			String newValue, boolean changesName) {
		super();
		this.journal = journal;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.changesName = changesName;
	}

	@Override
	public String getPresentationName() {
		return Fsfibu2StringTableMgr.getString(sgroup + ".presentation", changesName? Fsfibu2StringTableMgr.getString(sgroup + ".name") :
																		Fsfibu2StringTableMgr.getString(sgroup + ".description"),
																		journal == null? "?" : journal.getName());	
	}

	@Override
	public String getRedoPresentationName() {
		return Fsfibu2StringTableMgr.getString(sgroup + ".presentation", changesName? Fsfibu2StringTableMgr.getString(sgroup + ".name") :
			Fsfibu2StringTableMgr.getString(sgroup + ".description"),
			journal == null? "?" : journal.getName(),
			newValue);
	}

	@Override
	public String getUndoPresentationName() {
		return Fsfibu2StringTableMgr.getString(sgroup + ".presentation", changesName? Fsfibu2StringTableMgr.getString(sgroup + ".name") :
			Fsfibu2StringTableMgr.getString(sgroup + ".description"),
			journal == null? "?" : journal.getName(),
			oldValue);
	}

	/**
	 * Tries to set the appropriate value
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
		if(changesName) journal.setName(newValue);
		else journal.setDescription(newValue);
	}

	/**
	 * Tries to set the appropriate value
	 * @throws UndoException - If journal == null
	 */
	@Override
	public void undo() throws CannotUndoException {
		try {
			super.undo();
		}
		catch(CannotUndoException ce) {
			throw new UndoException("Cannot undo. Edit is not alive or has not been done.");
		}
		if(journal == null) throw new UndoException("Cannot undo on null journal.");
		if(changesName) journal.setName(oldValue);
		else journal.setDescription(oldValue);
	}
	
	
	
	 
	
}
