package fs.fibu2.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import fs.fibu2.data.model.Account;
import fs.fibu2.data.model.Journal;
import fs.fibu2.lang.Fsfibu2StringTableMgr;

/**
 * This class represents a change in the start value of a journal. It is represented by a journal to which the changes are made,
 * an account object for which the start value is set and two Float values representing old and new value. If one of the Float values is null,
 * it stands for a value removal. The edit does not care whether the start value for the account actually is the 'old value' (or 'new value'
 * for undo), it just sets the account start value to the given Float.
 * @author Simon Hampe
 *
 */
public class UndoableJournalStartEdit extends AbstractUndoableEdit {

	private Journal journal; 	//The journal to which the changes are made 
	private Account account;	//The account for which the values are set
	private Float oldValue;		//The value to set on undo
	private Float newValue;		//The value to set on redo
	
	private final static String sgroup = "fs.fibu2.undo.JournalStartEdit";
	
	/**
	 * Creates an edit that sets the start value for a certain account or removes it
	 * @param journal The journal to which the changes are made 
	 * @param account The account for which the values are set
	 * @param oldValue The value to set on undo. A null value means removal.
	 * @param newValue The value to set on redo. A null value means removal.
	 */
	public UndoableJournalStartEdit(Journal journal, Account account, Float oldValue, Float newValue) {
		super();
		this.journal = journal;
		this.account = account;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	@Override
	public String getPresentationName() {
		return Fsfibu2StringTableMgr.getString(sgroup + ".presentation",
				account != null? account.getName() : "?",
				journal != null? journal.getName() : "?");
	}

	@Override
	public String getRedoPresentationName() {
		return newValue == null ?
				Fsfibu2StringTableMgr.getString(sgroup + ".remove",account != null? account.getName() : "?",
																   journal != null? journal.getName() : "?") :
				Fsfibu2StringTableMgr.getString(sgroup + ".setto", account != null? account.getName() : "?",
																	newValue.floatValue(),
																	journal != null? journal.getName() : "?");
	}

	@Override
	public String getUndoPresentationName() {
		return oldValue == null ?
				Fsfibu2StringTableMgr.getString(sgroup + ".remove",account != null? account.getName() : "?",
						   journal != null? journal.getName() : "?") :
				Fsfibu2StringTableMgr.getString(sgroup + ".setto", account != null? account.getName() : "?",
							oldValue.floatValue(),
							journal != null? journal.getName() : "?");
	}

	@Override
	public void redo() throws CannotRedoException {
		// TODO Auto-generated method stub
		super.redo();
	}

	@Override
	public void undo() throws CannotUndoException {
		// TODO Auto-generated method stub
		super.undo();
	}
	
	
	
	
}
