package fs.fibu2.view.model;

import java.util.HashMap;

import fs.fibu2.data.model.Account;
import fs.fibu2.data.model.Category;

/**
 * A BilancialInformation is a tupel of bilancial sums for a fsfibu2 journal table row. It contains sums for: <br>
 * - The sum of all entries (up to a certain point)<br>
 * - The sum of all entries in certain categories <br>
 * - The sum of all entries in certain accounts PLUS the starting values <br>
 * All elements can be publicly modified
 * @author Simon Hampe
 *
 */
public class BilancialInformation {

	/**
	 * The overall sum of all entries
	 */
	public int overallSum = 0;
	
	/**
	 * Sums of entries in certain categories
	 */
	public HashMap<Category, Integer> categorySums = new HashMap<Category, Integer>();
	
	/**
	 * Sums of entries using certain accounts
	 */
	public HashMap<Account, Integer> accountSums = new HashMap<Account, Integer>();
	
	
}
