package fs.fibu2.data.model;

import java.util.Vector;

import fs.fibu2.data.error.EntryVerificationException;

/**
 *  This interface represents an fsfibu2 account, i.e. for example a bank account or a cash
 *  box. Each account requires different fields with different value formats and with different
 *  restrictions. For example a bank account transaction requires the number of the associated account statement.<br>
 *  Classes implementing this interface should usually have a standard no-argument constructor and no write access methods. 
 * @author Simon Hampe
 *
 */
public abstract class Account {
	
	/**
	 * @return A list of IDs of the fields which are required for this type of account.
	 */
	public abstract Vector<String> getFieldIDs();
	
	/**
	 * @return A list of the names of the required fields, such as might be used for label texts in an
	 * entry dialog. The order should be the same as in getFieldIDs()
	 */
	public abstract Vector<String> getFieldNames();
	
	/**
	 * @return A list of descriptions for each fiels, such as might be used for a tooltip.
	 * The order should be the same as in getFieldIDs() 
	 */
	public abstract Vector<String> getFieldDescriptions();
	
	/**
	 * @return A description of this type of account, i.e.: In which context it is used, what it stands for and what kind of
	 * information it generally requires.
	 */
	public abstract String getDescription();
	
	/**
	 * @return The ID of this account. This ID will be used to identify the account of an fsfibu2 journal entry. So you should take care
	 * to make this ID uniqe. The convention is to take the name of the account, where spaces are replaced by '_' (e.g. 'bank_account', 'cash_box')
	 */
	public abstract String getID();
	
	/**
	 * @return The name of this account (e.g. 'Bank account', 'Cash box')
	 */
	public abstract String getName();
	
	/**
	 * Checks whether an entry is correct for this type of account.
	 * @param e The entry to be verified
	 * @throws EntryVerificationException - if the entry is not correct
	 */
	public abstract void verifyEntry(Entry e) throws EntryVerificationException;
	
}
