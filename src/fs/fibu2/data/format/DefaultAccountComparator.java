package fs.fibu2.data.format;

import java.util.Comparator;

import fs.fibu2.data.model.Account;

/**
 * Compares two {@link Account} objects via their name, using {@link DefaultStringComparator}. The null object is always the smallest one.
 * @author Simon Hampe
 *
 */
public class DefaultAccountComparator implements Comparator<Account> {

	@Override
	public int compare(Account o1, Account o2) {
		if(o1 == null && o2 == null) return 0;
		if(o1 == null) return -1;
		if(o2 == null) return -1;
		return (new DefaultStringComparator()).compare(o1.getName(), o2.getName());
	}

}
