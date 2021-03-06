package fs.fibu2.view.model;

import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingWorker;

import fs.fibu2.data.event.JournalListener;
import fs.fibu2.data.model.AbstractAccount;
import fs.fibu2.data.model.Account;
import fs.fibu2.data.model.AccountLoader;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.data.model.ReadingPoint;

/**
 * Implements a list model for a sorted list of all accounts used in a journal. It listens to journal changes and sorts
 * accounts alphabetically according to their name. If the associated journal is null, it displays all accounts known to {@link AccountLoader}
 * (except {@link AbstractAccount})
 * @author Simon Hampe
 *
 */
public class AccountListModel extends AbstractListModel implements
		JournalListener, ComboBoxModel {

	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = 8092654957034398630L;
	
	private Vector<Account> listOfAccounts = new Vector<Account>();
	private Journal associatedJournal;
	
	private DefaultComboBoxModel model;
	
	//Not null, if a recalculation is currently running
	private SwingWorker<Vector<Account>, Vector<Account>> currentRecalculator = null; 
	
	private Comparator<Account> comparator = new Comparator<Account>() {
		@Override
		public int compare(Account o1, Account o2) {
			if(o1 == null && o2 == null) return 0;
			if(o1 == null || o2 == null) return -1;
			return o1.getName().compareTo(o2.getName());
		}
	};
	
	/**
	 * Creates a list model for the journal j. If j == null, an empty dummy journal is created
	 */
	public AccountListModel(Journal j) {
		associatedJournal = j;
		if(j != null) associatedJournal.addJournalListener(this);
		listOfAccounts = updateList();
		model = new DefaultComboBoxModel(listOfAccounts);
	}
	
	/**
	 * Updates the account list 
	 */
	protected Vector<Account> updateList() {
		TreeSet<Account> accounts = new TreeSet<Account>(comparator);
		if(associatedJournal != null) accounts.addAll(associatedJournal.getListOfAccounts());
		else {
			String aaID = new AbstractAccount().getID();
			for(String id : AccountLoader.getListOfIDs()) {
				if(!id.equals(aaID)) accounts.add(AccountLoader.getAccount(id));
			}
		}
		return new Vector<Account>(accounts);
	}
	
	/**
	 * Notifies all listeners
	 */
	protected void fireContentsChanged() {
		fireContentsChanged(this, 0, getSize());
	}
	
	/**
	 * Creates and executes a swing worker to recalculate the model
	 */
	protected void updateThreaded() {
		if(currentRecalculator != null) {
			currentRecalculator.cancel(true);
		}
		currentRecalculator = new Recalculator();
		currentRecalculator.execute();
	}
	
	@Override
	public Object getElementAt(int index) {
		return listOfAccounts.get(index);
	}

	@Override
	public int getSize() {
		return listOfAccounts.size();
	}
	

	@Override
	public Object getSelectedItem() {
		return model.getSelectedItem();
	}

	@Override
	public void setSelectedItem(Object anItem) {
		model.setSelectedItem(anItem);
	}
	
	/**
	 * @return A list of all items in this model
	 */
	public HashSet<Account> getListOfItems() {
		return new HashSet<Account>(listOfAccounts);
	}

	// LISTENER METHDOS *******************************
	// ************************************************
	
	
	@Override
	public void descriptionChanged(Journal source, String oldValue,
			String newValue) {
		//Ignored
	}

	@Override
	public void entriesAdded(Journal source, Entry[] newEntries) {
		updateThreaded();
	}

	@Override
	public void entriesRemoved(Journal source, Entry[] oldEntries) {
		updateThreaded();
	}

	@Override
	public void entryReplaced(Journal source, Entry oldEntry, Entry newEntry) {
		updateThreaded();
	}

	@Override
	public void nameChanged(Journal source, String oldValue, String newValue) {
		//Ignored
	}

	@Override
	public void readingPointAdded(Journal source, ReadingPoint point) {
		//Ignored
	}

	@Override
	public void readingPointRemoved(Journal source, ReadingPoint point) {
		//Ignored
	}

	@Override
	public void startValueChanged(Journal source, Account a, Float oldValue,
			Float newValue) {
		updateThreaded();
	}
	
	@Override
	public void dateChanged(ReadingPoint source) {
		//Ignored
	}

	@Override
	public void nameChanged(ReadingPoint source) {
		//Ignored
	}

	// SWING WORKER CLASS FOR RECALCULATION *************************
	// **************************************************************
	
	private class Recalculator extends SwingWorker<Vector<Account>, Vector<Account>> {

		@Override
		protected Vector<Account> doInBackground() throws Exception {
			return updateList();
		}
		
		@Override
		protected void done() {
			try {
				listOfAccounts = get();
				//Preserve selection if possible
				Account c = (Account)getSelectedItem();
				model = new DefaultComboBoxModel(listOfAccounts);
				if(listOfAccounts.contains(c)) model.setSelectedItem(c);
				currentRecalculator = null;
				fireContentsChanged();
			} catch (Exception e) {
				//Ignored, will not happen
			} 			
		}	
		
	}
}
