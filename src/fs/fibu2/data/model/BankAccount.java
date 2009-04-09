package fs.fibu2.data.model;

import java.util.HashMap;
import java.util.Vector;

import fs.fibu2.data.error.EntryVerificationException;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.xml.PolyglotStringTable;

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
		return Fsfibu2StringTableMgr.getLoader().getString(sgroup + ".description", PolyglotStringTable.getGlobalLanguageID());
	}

	/* (non-Javadoc)
	 * @see fs.fibu2.data.model.AbstractAccount#getFieldDescriptions()
	 */
	@Override
	public HashMap<String, String> getFieldDescriptions() {
		HashMap<String, String> returnValue = super.getFieldDescriptions();
		returnValue.put(statementID,Fsfibu2StringTableMgr.getLoader().getString(sgroup + ".statementdescription", PolyglotStringTable.getGlobalLanguageID()));
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
		returnValue.put(statementID, Fsfibu2StringTableMgr.getLoader().getString(sgroup + ".statementdescription", PolyglotStringTable.getGlobalLanguageID()));
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
		 return Fsfibu2StringTableMgr.getLoader().getString(sgroup + ".name", PolyglotStringTable.getGlobalLanguageID());
	}

	/* (non-Javadoc)
	 * @see fs.fibu2.data.model.AbstractAccount#toString()
	 */
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}

	/* (non-Javadoc)
	 * @see fs.fibu2.data.model.AbstractAccount#verifyEntry(fs.fibu2.data.model.Entry)
	 */
	@Override
	public void verifyEntry(Entry e) throws EntryVerificationException {
		// TODO Auto-generated method stub
		super.verifyEntry(e);
	}

}
