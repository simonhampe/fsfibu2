package fs.fibu2.data.model;

import java.util.HashMap;
import java.util.Vector;

import fs.fibu2.data.error.EntryVerificationException;
import fs.fibu2.lang.Fsfibu2StringTableMgr;

/**
 * This class represents a bank account, i.e. it requires for each entry the number (i.e. an integer value) of the account statement sheet 
 * (= 'Kontoauszugsnummer'). So the fields (potentially) required are:<br>
 * - invoice (for negative entries)<br>
 * - statement (for all entries) <br>
 * The id of this account is 'bank_account'
 * @author Simon Hampe
 *
 */
public class BankAccount extends AbstractAccount {

	private static String accountID = "bank_account";
	private static String statementID = "statement";
	private static String sgroup = "fs.fibu2.BankAccount";
	
	/* (non-Javadoc)
	 * @see fs.fibu2.data.model.AbstractAccount#getDescription()
	 */
	@Override
	public String getDescription() {
		return Fsfibu2StringTableMgr.getString(sgroup + ".description");
	}

	/* (non-Javadoc)
	 * @see fs.fibu2.data.model.AbstractAccount#getFieldDescriptions()
	 */
	@Override
	public HashMap<String, String> getFieldDescriptions() {
		HashMap<String, String> returnValue = super.getFieldDescriptions();
		returnValue.put(statementID,Fsfibu2StringTableMgr.getString(sgroup + ".statementdescription"));
		return returnValue;
	}

	/* (non-Javadoc)
	 * @see fs.fibu2.data.model.AbstractAccount#getFieldIDs()
	 */
	@Override
	public Vector<String> getFieldIDs() {
		Vector<String> returnValue = super.getFieldIDs();
		returnValue.add(statementID);
		return returnValue;
	}

	/* (non-Javadoc)
	 * @see fs.fibu2.data.model.AbstractAccount#getFieldNames()
	 */
	@Override
	public HashMap<String, String> getFieldNames() {
		HashMap<String, String> returnValue = super.getFieldNames();
		returnValue.put(statementID, Fsfibu2StringTableMgr.getString(sgroup + ".statement"));
		return returnValue;
	}

	/* (non-Javadoc)
	 * @see fs.fibu2.data.model.AbstractAccount#getID()
	 */
	@Override
	public String getID() {
		return accountID;
	}

	/* (non-Javadoc)
	 * @see fs.fibu2.data.model.AbstractAccount#getName()
	 */
	@Override
	public String getName() {
		 return Fsfibu2StringTableMgr.getString(sgroup + ".name");
	}

	/**
	 * @throws EntryVerificationException - in the following cases: <br>
	 * - The entry has a negative value, but no 'invoice' field
	 * - The entry has no 'statement' field or the field contains data which is not an integer
	 * (Both errors are non-critical and cumulative, i.e. if both fields are faulty, the exception
	 * will contain descriptions of both errors)
	 */
	@Override
	public void verifyEntry(Entry e) throws EntryVerificationException {
		EntryVerificationException ev = new EntryVerificationException(e,new Vector<String>(), new HashMap<String, Boolean>(), new HashMap<String, String>());
		boolean errorOccured = false;
		//Check for invoice number
		try {
			super.verifyEntry(e);
		}
		catch(EntryVerificationException ve) {
			ev = ve;
			errorOccured = true;
		}
		//Check for statement number
		if(e == null || e.getAccountInformation().get(statementID) == null || e.getAccountInformation().get(statementID).trim().equals("")) {
				ev.getListOfFaultyFields().add(statementID);
				ev.getListOfCriticality().put(statementID, false);
				ev.getFaultDescriptions().put(statementID, Fsfibu2StringTableMgr.getString(sgroup + ".nostatement"));
				errorOccured = true;
		}
		else {
			try {
				Integer.parseInt(e.getAccountInformation().get(statementID));
			}
			catch(NumberFormatException ne) {
				ev.getListOfFaultyFields().add(statementID);
				ev.getFaultDescriptions().put(statementID, Fsfibu2StringTableMgr.getString(sgroup + ".faultystatement"));
				ev.getListOfCriticality().put(statementID, false);
				errorOccured = true;
			}
		}
		if(errorOccured) throw ev;
	}

}
