package fs.fibu2.data.model;

import java.util.HashMap;
import java.util.HashSet;

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
	
	public static HashSet<String> getListOfIDs() {
		return new HashSet<String>(accountMap.keySet());
	}
	
	/**
	 * Tries to call the nullary constructor on c to retrieve an Account object and stores it under its ID.
	 * If there already is an account object under this ID, this call is ignored.
	 * @throws UnsupportedOperationException - If the instantiation fails for some reason.
	 */
	public static void loadAccount(Class<?> c) throws UnsupportedOperationException {
		 try {
			 Account a = (Account)c.newInstance();
			 if(!accountMap.containsKey(a.getID())) {
				 accountMap.put(a.getID(), a);
			 }
		 }
		 catch(Exception e) {
			 throw new UnsupportedOperationException(e);
		 }
	}
	
}
