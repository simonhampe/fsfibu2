package fs.fibu2.view.render;

import java.util.HashMap;

import fs.fibu2.application.Fsfibu2;
import fs.fibu2.data.event.JournalAdapter;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;

/**
 * This class creates {@link EntryDialog}s. It takes care that for each entry in a given journal there is at most one dialog and
 * that a dialog is disposed when its associated entry is deleted.
 * 
 * @author Simon Hampe
 *
 */
public final class EntryDialogFactory extends JournalAdapter {

	//A static map of instances per Journal
	private static HashMap<Journal, EntryDialogFactory> factories = new HashMap<Journal,EntryDialogFactory>();
	
	//The map of entries
	private HashMap<Entry,EntryDialog> dialogs = new HashMap<Entry, EntryDialog>();
	
	//The associated journal
	private Journal journal;
	
	// CONSTRUCTOR ********************
	// ********************************
	
	/**
	 * Creates an instance 
	 */
	private EntryDialogFactory(Journal j) {journal = j;}
	
	/**
	 * @return The instance associated to j. If there is none, one is created and listens to the journal
	 */
	public static EntryDialogFactory getInstance(Journal j) {
		EntryDialogFactory f = factories.get(j);
		if(f == null) {
			f = new EntryDialogFactory(j);
			factories.put(j, f);
			if(j != null) j.addJournalListener(f);
		}
		return f;
	}
	
	// DIALOG CREATION *********************
	// *************************************
	
	public EntryDialog getDialog(Entry e) {
		if(e == null) return new EntryDialog(Fsfibu2.getFrame(),journal,null);
		EntryDialog d =  dialogs.get(e);
		if(d == null) {
			d = new EntryDialog(Fsfibu2.getFrame(),journal,e);
			dialogs.put(e, d);
		}
		return d;
	}
	
	// JOURNALADAPTER METHODS **************
	// *************************************

	@Override
	public void entriesRemoved(Journal source, Entry[] oldEntries) {
		for(Entry e : oldEntries) {
			if(dialogs.containsKey(e)) {
				dialogs.get(e).dispose();
				dialogs.remove(e);
			}
		}
	}

	@Override
	public void entryReplaced(Journal source, Entry oldEntry, Entry newEntry) {
		if(dialogs.containsKey(oldEntry)) {
			dialogs.get(oldEntry).dispose();
			dialogs.remove(oldEntry);
		}
	}

	
	
}

