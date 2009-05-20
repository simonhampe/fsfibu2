package fs.fibu2.view.model;

import java.util.HashSet;

import fs.fibu2.data.model.Account;

/**
 * This is a simple, immutable string tuple class for representing account information fields by their ID. For each such tuple there 
 * is a list of Accounts, which use that field (This list is provided by the user, so there is no actual guarantee, that all accounts in that
 * list use the given field or that these are all fields using it) and a name string which is used for user-friendly representation.
 * Two objects of this type are equal if and only if their id is equal as a String.
 * @author Simon Hampe
 *
 */
public final class AccountInformation implements Comparable<AccountInformation> {

	private String id = "";
	private String name= "";
	private HashSet<Account> accounts = new HashSet<Account>();
	
	public AccountInformation(String id, String name, HashSet<Account> accounts) {
		super();
		this.id = id == null? "" : id;
		this.name = name == null? "" : name;
		this.accounts = accounts == null? new HashSet<Account>() : accounts;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public HashSet<Account> getAccounts() {
		return new HashSet<Account>(accounts);
	}

	/**
	 * @return - 0, if the ids coincide <br>
	 * - -1/1, if this.id </> o.id or <br>
	 * - -1, if o == null
	 */
	@Override
	public int compareTo(AccountInformation o) {
		if(o == null) return -1;
		int r2 = id.compareTo(o.getId());
		return r2;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof AccountInformation)) return false;
		return ((AccountInformation)obj).getId().equals(id) ;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	} 
	
	
	
	
}
