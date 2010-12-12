package fs.fibu2.view.model;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;

import fs.fibu2.data.format.DefaultCurrencyFormat;
import fs.fibu2.data.format.MoneyDecimal;
import fs.fibu2.data.model.Account;
import fs.fibu2.data.model.Category;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.lang.Fsfibu2StringTableMgr;

/**
 * A BilancialInformation is an immutable tupel of bilancial sums for a fsfibu2 journal table row. It contains sums for: <br>
 * - The sum of all entries (up to a certain point)<br>
 * - The sum of all entries in certain categories <br>
 * - The sum of all entries in certain accounts PLUS the starting values <br>
 * All elements can be publicly modified
 * @author Simon Hampe
 *
 */
public final class BilancialInformation {

	/**
	 * The overall sum of all entries
	 */
	private BigDecimal overallSum = MoneyDecimal.bigd(0);
	
	/**
	 * Sums of entries in certain categories
	 */
	private HashMap<Category, BigDecimal> categorySums = new HashMap<Category, BigDecimal>();
	
	/**
	 * Sums of entries using certain accounts
	 */
	private HashMap<Account, BigDecimal> accountSums = new HashMap<Account, BigDecimal>();
	
	// CONSTRUCTOR ***************************
	// ***************************************
	
	/**
	 * Creates a new bilancial information with overall value 0 and empty maps
	 */
	public BilancialInformation() {
		this(MoneyDecimal.bigd(0),null,null);
	}
	
	/**
	 * Creates a new bilancial information. The maps are cloned
	 */
	public BilancialInformation(BigDecimal overallSum, HashMap<Category, BigDecimal> categorySums, HashMap<Account, BigDecimal> accountSums) {
		this.overallSum = overallSum;
		this.categorySums = categorySums == null? new HashMap<Category, BigDecimal>() : new HashMap<Category, BigDecimal>(categorySums);
		this.accountSums = accountSums == null? new HashMap<Account, BigDecimal>() : new HashMap<Account, BigDecimal>(accountSums);
	}
	
	/**
	 * Creates a new bilancial information which only copies the account start values as account mappings. The rest is initialized to 0, resp. empty.
	 * If j == null, this constructor has the same effect as the empty constructor
	 */
	public BilancialInformation(Journal j) {
		if(j != null) {	
			HashMap<Account, BigDecimal> start = new HashMap<Account, BigDecimal>();
			for(Account a : j.getListOfAccounts()) {				
				start.put(a, MoneyDecimal.bigd(j.getStartValue(a)));
			}
			accountSums = start;
		}
	}
	
	/**
	 * This constructor is NOT a copy constructor! If you want to create a copy, use clone(). This constructor only copies the account mappings and
	 * initializes the rest to 0, resp. empty
	 * @param info
	 */
	public BilancialInformation(BilancialInformation info) {
		this(MoneyDecimal.bigd(0),null,info == null? null: info.getAccountMappings());
	}
	
	// GETTERS ******************************
	// **************************************
	
	/**
	 * @return The bilancial information obtained by adding the value of e to the overall sum, its category and account mapping (If there is no
	 * mapping, it is created). If e == null, a clone of this information is returned.
	 */
	public BilancialInformation increment(Entry e) {
		if(e == null)return clone();
		BigDecimal evalue = MoneyDecimal.bigd(e.getValue());
		HashMap<Category, BigDecimal> newcategory = new HashMap<Category, BigDecimal>(categorySums);
			Category cat = e.getCategory();
			do {
				BigDecimal c = newcategory.get(cat);
				newcategory.put(cat, c == null? 
						evalue : MoneyDecimal.add(evalue, c));
				cat = cat.parent;
			}
			while(/*cat != Category.getRootCategory() && */ cat != null);
		HashMap<Account, BigDecimal> newaccount = new HashMap<Account, BigDecimal>(accountSums);
			BigDecimal a = newaccount.get(e.getAccount());
			newaccount.put(e.getAccount(), a == null? evalue : MoneyDecimal.add(evalue, a));
		return new BilancialInformation(MoneyDecimal.add(overallSum, evalue), newcategory,newaccount);
	}
	
	/**
	 * @return The bilancial information obtained by adding the value of e ONLY to its account mapping. If e == null, a clone is created
	 */
	public BilancialInformation incrementAccount(Entry e) {
		if(e == null) return clone();
		BigDecimal evalue = MoneyDecimal.bigd(e.getValue());
		HashMap<Account, BigDecimal> newaccount = new HashMap<Account, BigDecimal>(accountSums);
			BigDecimal a = newaccount.get(e.getAccount());
			newaccount.put(e.getAccount(), a == null? evalue : MoneyDecimal.add(evalue, a));
		return new BilancialInformation(overallSum, categorySums, newaccount);
	}
	
	/**
	 * @return The bilancial information obtained by incrementing with an entry which is identical to e, except that is has value ( - e.getValue())
	 */
	public BilancialInformation decrement(Entry e) {
		if(e == null) return clone();
		return increment(new Entry(e.getName(),-e.getValue(),
				e.getCurrency(),e.getDate(),e.getCategory(),e.getAccount().getID(),e.getAccountInformation(),e.getAdditionalInformation()));
	}
	
	public BigDecimal getOverallSum() {
		return overallSum;
	}
	
	public HashMap<Account, BigDecimal> getAccountMappings() {
		return new HashMap<Account, BigDecimal>(accountSums);
	}
	
	public HashMap<Category, BigDecimal> getCategoryMappings() {
		return new HashMap<Category, BigDecimal>(categorySums);
	}
	
	public BilancialInformation clone() {
		BilancialInformation clone = new BilancialInformation();
		
		clone.overallSum = overallSum;
		clone.categorySums = new HashMap<Category, BigDecimal>(categorySums);
		clone.accountSums = new HashMap<Account, BigDecimal>(accountSums);
		
		return clone;
	}
	
	/**
	 * @return An HTML string representing the overall sum and the sum associated to the given category and account in a nice way 
	 * (using the given currency symbol).
	 * If there are no mappings for the given parameters, these sums are assumed to be 0.
	 */
	public String getHTMLRepresentation(Category c, Account a, Currency cur) {
		if(cur == null) cur = Currency.getInstance(Locale.getDefault());
		NumberFormat format = DefaultCurrencyFormat.getFormat(cur);
		StringBuilder b = new StringBuilder();
		b.append("<html><b>");
		b.append(Fsfibu2StringTableMgr.getString("fs.fibu2.model.BilancialInformation.report"));
		b.append(":</b><br>");
		b.append(Fsfibu2StringTableMgr.getString("fs.fibu2.model.BilancialInformation.overallsum"));
			b.append(format.format(overallSum.floatValue()));
			b.append("<br>");
		if(c != null) {
			Category cat = c;
			do {
				b.append(cat.tail);
				b.append(": ");
				BigDecimal value = categorySums.get(cat);
				b.append(format.format(value == null? 0 : value.floatValue()));
				b.append("<br>");
				cat = cat.parent;
			}
			while(cat != Category.getRootCategory() && cat != null);
		}
		if(a != null) {
			b.append(a.getName());
			b.append(": ");
			BigDecimal value = accountSums.get(a);
			b.append(format.format(value == null? 0 : value.floatValue()));
		}
		b.append("</html>");
		return b.toString();
	}
	
}
