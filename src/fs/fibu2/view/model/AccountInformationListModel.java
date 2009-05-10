package fs.fibu2.view.model;

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
		//First create a list without associated accounts
		Vector<AccountInformation> preset = new Vector<AccountInformation>();
			for(Account a : accounts) {
				for(String id : a.getFieldIDs()) preset.add(new AccountInformation(id, a.getFieldNames().get(id),null));
			}
		//Now create a list WITH associated accounts
		Vector<AccountInformation> finalset = new Vector<AccountInformation>();
		for(AccountInformation info : preset) {
			//If the tupel has not yet been added, add it
			if(!finalset.contains(info)) {
				finalset.add(info);
			}
			//Otherwise add accounts
			else {
				HashSet<Account> usingaccounts = finalset.get(finalset.indexOf(info)).getAccounts();
				usingaccounts.addAll(info.getAccounts());
				finalset.remove(info);
				finalset.add(new AccountInformation(info.getId(),info.getName(),usingaccounts));
			}
		}
		return new Vector<AccountInformation>(new TreeSet<AccountInformation>(finalset));
	}
	
	/**
	 * Executes a swing worker object for model recalculation
	 */
	protected void updateThreaded() {
		(new Recalculator()).execute();
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
	public void activityChanged(ReadingPoint source) {
		//Ignored
	}

	@Override
	public void dateChanged(ReadingPoint source) {
		//IGnored
	}

	@Override
	public void nameChanged(ReadingPoint source) {
		//Ignored
	}

	@Override
	public void visibilityChanged(ReadingPoint source) {
		//IGnored
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
				fireContentsChanged();
			} catch (Exception e) {
				//Ignore
			}
		}	
	}

}
