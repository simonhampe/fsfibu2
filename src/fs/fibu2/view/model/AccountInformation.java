package fs.fibu2.view.model;

import java.util.HashSet;

import fs.fibu2.data.model.Account;

/**
 * This is a simple, immutable string tuple class for representing account information fields by their ID and their name
 * at the same time. For each such tuple there is a list of Accounts, which use that field with this exact name
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
	 * @return - 0, if name and id coincide <br>
	 * - -1/1, if either the names coincide but the ids differ and this.id </> o.id or this.name < / > o.name <br>
	 * - -1, if o == null
	 */
	@Override
	public int compareTo(AccountInformation o) {
		if(o == null) return -1;
		int r1 = name.compareTo(o.getName());
		if(r1 != 0) return r1;
		int r2 = id.compareTo(o.getId());
		return r2;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof AccountInformation)) return false;
		return ((AccountInformation)obj).getId().equals(id) && ((AccountInformation)obj).getName().equals(name);
	}

	@Override
	public int hashCode() {
		return name.hashCode() ^ id.hashCode();
	} 
	
	
	
	
}
