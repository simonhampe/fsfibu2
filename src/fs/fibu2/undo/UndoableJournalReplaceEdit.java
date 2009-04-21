package fs.fibu2.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import fs.fibu2.data.error.RedoException;
import fs.fibu2.data.error.UndoException;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.lang.Fsfibu2StringTableMgr;

/**
 * This class represents a replacement of an fsfibu2 journal entry by another. If the replacing value is
 * null, the edit has no effect. If you want to remove an entry, use {@link UndoableJournalEntryEdit}.
 * @author Simon Hampe
 *
 */
public class UndoableJournalReplaceEdit extends AbstractUndoableEdit {
	
	/**
	 * compiler-generated version uid 
	 */
	private static final long serialVersionUID = -4928695351423745915L;
	
	private Journal journal;	//The journal to which the changes are made
	private Entry oldValue;		//The value to be replaced on redo 
	private Entry newValue;		//The value to be replaced on undo
	
	private final static String sgroup = "fs.fibu2.undo.JournalReplaceEdit";
	
	/**
	 * Generates an edit which replaces an entry by another
	 * @param journal The journal to which the changes are made
	 * @param oldValue The value being replaced. If null, no changes are made
	 * @param newValue The value used for replacement. If null, no changes are made
	 */
	public UndoableJournalReplaceEdit(Journal journal, Entry oldValue,
			Entry newValue) {
		super();
		this.journal = journal;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	@Override
	public String getPresentationName() {
		return Fsfibu2StringTableMgr.getString(sgroup + ".presentation", journal == null? "?" : journal.getName());
	}

	@Override
	public String getRedoPresentationName() {
		return Fsfibu2StringTableMgr.getString(sgroup + ".modify", oldValue == null? "?" : oldValue.getName(), journal == null? "?" : journal.getName());
	}

	@Override
	public String getUndoPresentationName() {
		return Fsfibu2StringTableMgr.getString(sgroup + ".modify", newValue == null? "?" : newValue.getName(), journal == null? "?" : journal.getName());
	}

	/**
	 * Replaces the old value by the new value, if both are not null. Otherwise this has no effect.
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
		if(oldValue != null && newValue != null) {
			journal.replaceEntry(oldValue, newValue);
		}
	}

	/**
	 * Replaces the new value by the old value, if both are not null. Otherwise this has no effect.
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
		if(oldValue != null && newValue != null) {
			journal.replaceEntry(newValue, oldValue);
		}
	}
	
	
	
	
}
