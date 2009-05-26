package fs.fibu2.view.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingWorker;

import fs.fibu2.data.event.JournalListener;
import fs.fibu2.data.model.Account;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.data.model.ReadingPoint;

/**
 * Implements a list model for the set of all account information fields. Two such fields are considered identical, if they have the same id
 * and name. Apart from that, they are ordered alphabetically according to their name 
 * @author Simon Hampe
 *
 */
public class AccountInformationListModel extends AbstractListModel implements
		JournalListener, ComboBoxModel {

	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = -1498799268898208285L;

	private Vector<AccountInformation> listOfInformation = new Vector<AccountInformation>();
	
	private DefaultComboBoxModel model;
	
	private Journal associatedJournal;
	
	//Not null, if the model is currently being recalculated
	private SwingWorker<Vector<AccountInformation>, Vector<AccountInformation>> currentRecalculator = null;
	
	// CONSTRUCTOR ******************************************
	// ******************************************************
	
	public AccountInformationListModel(Journal j) {
		associatedJournal = j == null? new Journal() : j;
		associatedJournal.addJournalListener(this);
		listOfInformation = calculateModel();
		model = new DefaultComboBoxModel(listOfInformation);
	}
	
	// UPDATE METHODS ***************************************
	// ******************************************************
	
	/**
	 * Notifies all listeners
	 */
	protected void fireContentsChanged() {
		fireContentsChanged(this, 0, getSize());
	}
	
	/**
	 * Recalculates the model
	 */
	protected Vector<AccountInformation> calculateModel() {
		HashSet<Account> accounts = associatedJournal.getListOfAccounts();
		//Now create a list WITH associated accounts
		HashMap<AccountInformation, HashSet<Account>> usingAccounts = new HashMap<AccountInformation, HashSet<Account>>();
		for(Account a : accounts) {
			for(String id : a.getFieldIDs()) {
				AccountInformation info = new AccountInformation(id,a.getFieldNames().get(id),null);
				if(!usingAccounts.containsKey(info)) {
					usingAccounts.put(info, new HashSet<Account>(Arrays.asList(a)));
				}
				else {
					usingAccounts.get(info).add(a);
				}
			}
		}
		TreeSet<AccountInformation> finalset = new TreeSet<AccountInformation>();
		for(AccountInformation info : usingAccounts.keySet()) {
			AccountInformation newinfo = new AccountInformation(info.getId(),info.getName(),usingAccounts.get(info));
			finalset.add(newinfo);
		}
		return new Vector<AccountInformation>(finalset);
	}
	
	/**
	 * Executes a swing worker object for model recalculation. If a recalculation is already running, it is aborted and 
	 * the new one started immediately
	 */
	protected void updateThreaded() {
		if(currentRecalculator != null) {
			currentRecalculator.cancel(true);
		}
		currentRecalculator = new Recalculator();
		currentRecalculator.execute();
	}
	
	// LIST AND COMBOBOX METHODS ****************************
	// ******************************************************

	@Override
	public Object getSelectedItem() {
		return model.getSelectedItem();
	}

	@Override
	public void setSelectedItem(Object anItem) {
		model.setSelectedItem(anItem);
	}

	@Override
	public Object getElementAt(int index) {
		return listOfInformation.get(index);
	}

	@Override
	public int getSize() {
		return listOfInformation.size();
	}

	
	//LISTENER METHODS ****************************************
	// ********************************************************
	
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
		//IGnored
	}

	@Override
	public void nameChanged(ReadingPoint source) {
		//Ignored
	}
	
	// SWING WORKER CLASS FOR MODEL RECALCULATION *****************************
	// ************************************************************************
	
	private class Recalculator extends SwingWorker<Vector<AccountInformation>, Vector<AccountInformation>> {

		@Override
		protected Vector<AccountInformation> doInBackground() throws Exception {
			return calculateModel();
		}

		@Override
		protected void done() {
			try {
				listOfInformation = get();
				//Preserve selection if possible
				AccountInformation c = (AccountInformation)getSelectedItem();
				model = new DefaultComboBoxModel(listOfInformation);
				if(listOfInformation.contains(c)) model.setSelectedItem(c);
				currentRecalculator = null;
				fireContentsChanged();
			} catch (Exception e) {
				//Ignore
			}
		}	
	}

}
