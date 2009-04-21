package fs.fibu2.undo;

import java.util.HashSet;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import fs.fibu2.data.error.RedoException;
import fs.fibu2.data.error.UndoException;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.lang.Fsfibu2StringTableMgr;

/**
 * This class represents an addition or removal of entries to a journal in the
 * form of an undoable edit. It is implemented in a non-strict way, i.e. an entry addition
 * would 'work', even if all the entries were already in the journal
 * @author Simon Hampe
 *
 */
public class UndoableJournalEntryEdit extends AbstractUndoableEdit {

	/**
	 * compiler-generated serial version uid 
	 */
	private static final long serialVersionUID = -6562588666448302070L;
	
	private Journal journal;			 //The journal to which the changes are made
	private HashSet<Entry> entries;		 //The entries which are added or removed
	private boolean entriesAreAdded;	 //If true, the entries are added. Otherwise removed
	
	private final static String sgroup = "fs.fibu2.undo.JournalEntryEdit";
	
	/**
	 * Creates an UndoableEdit which adds entries to a journal or removes them from it.
	 * @param journal The journal to which the changes are made
	 * @param entries The entries to be added / removed
	 * @param entriesAreAdded True, if the entries should be added, false if they should
	 * be removed
	 */
	public UndoableJournalEntryEdit(Journal journal, HashSet<Entry> entries, boolean entriesAreAdded) {
		super();
		this.journal = journal;
		this.entries = new HashSet<Entry>(entries);
		this.entriesAreAdded = entriesAreAdded;
	}

	/**
	 * @return A localized description of this edit
	 */
	@Override
	public String getPresentationName() {
		return Fsfibu2StringTableMgr.getString(sgroup + ".presentation",entriesAreAdded? 
				Fsfibu2StringTableMgr.getString(sgroup + ".add") :
				Fsfibu2StringTableMgr.getString(sgroup + ".remove"),
				journal != null? journal.getName() : "");
	}

	/**
	 * @return A localized description of this edit
	 */
	@Override
	public String getRedoPresentationName() {
		return getPresentationName();
	}

	/**
	 * @return A localized description of this edit's counter-operation
	 */
	@Override
	public String getUndoPresentationName() {
		return Fsfibu2StringTableMgr.getString(sgroup + ".presentation",!entriesAreAdded? 
				Fsfibu2StringTableMgr.getString(sgroup + ".add") :
				Fsfibu2StringTableMgr.getString(sgroup + ".remove"),
				journal != null? journal.getName() : "");
	}

	/**
	 * Tries to add/remove the specified edits to/from the journal. 
	 * @throws RedoException - if journal == null
	 */
	@Override
	public void redo() throws RedoException {
		try {
			super.redo();
		}
		catch(CannotRedoException ce) {
			throw new RedoException("Cannot redo. Edit is not alive or has already been done.");
		}
		if(journal == null) throw new RedoException("Cannot redo on null journal");
		if(entriesAreAdded) journal.addAllEntries(entries);
		else journal.removeAllEntries(entries);
	}

	/**
	 * Tries to add/remove the specified edits to/from the journal
	 * @throws UndoException - if journal == null
	 */
	@Override
	public void undo() throws UndoException {
		try {
			super.undo();
		}
		catch(CannotUndoException ce) {
			throw new UndoException("Cannot undo. Edit is not alive or has not been done.");
		}
		if(journal == null) throw new UndoException("Cannot undo on null journal");
		if(entriesAreAdded) journal.removeAllEntries(entries);
		else journal.addAllEntries(entries);
	}

}
