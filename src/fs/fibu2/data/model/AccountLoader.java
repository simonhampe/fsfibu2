package fs.fibu2.data.model;

import java.io.File;
import java.util.HashMap;

/**
 * This class maintains a map of account id's to account objects. It can load .class files dynamically to add further accounts.
 * The loader initially contains mappings for all account classes included in the fsfibu2 package
 * @author Simon Hampe
 *
 */
public final class AccountLoader {

	//Map of ID -> Account object
	private static HashMap<String,Account> accountMap = new HashMap<String, Account>();
	
	//Init code
	static {
		AbstractAccount absAcc = new AbstractAccount();
		accountMap.put(absAcc.getID(), absAcc);
		BankAccount bankAcc = new BankAccount();
		accountMap.put(bankAcc.getID(), bankAcc);
		CashBox cb = new CashBox();
		accountMap.put(cb.getID(), cb);
	}
	
	/**
	 * Returns the account object associated to the given id
	 * @throws IllegalArgumentException - If there is no account for this ID
	 */
	public static Account getAccount(String id) throws IllegalArgumentException {
		Account a = accountMap.get(id);
		if(a == null) throw new IllegalArgumentException("Account id " + id + " unknown");
		else return a;
	}
	
	/**
	 * Sets the account associated to the id. If there already is an account associated to this id, it is overwritten. If a == null,
	 * the mapping for this id is removed. If id == null, this call is ignored
	 */
	public static void setAccount(String id, Account a) {
		if(id != null) {
			if(a == null) accountMap.remove(id);
			else accountMap.put(id, a);
		}
	}
	
	public static void loadAccount(File f) {
		//TODO: Write dynamic class loading method... 
	}
	
}
