package fs.fibu2.filter;

import java.util.Currency;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import fs.fibu2.data.format.DefaultStringComparator;
import fs.fibu2.data.model.Entry;
import fs.fibu2.filter.StandardFilterComponent.Selection;
import fs.fibu2.filter.event.StandardComponentListener;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.validate.ValidationResult.Result;

/**
 * This class filters entries according to their currency field
 * @author Simon Hampe
 *
 */
public class CurrencyFilter implements EntryFilter {

	// FIELDS ************************
	// *******************************
	
	private Selection typeOfFilter;
	private Currency  equalityCurrency;
	private Pattern	  regexFilter;
	private String	  minFilter;
	private String	  maxFilter;
	
	// CONSTRUCTORS ***********************
	// ************************************
	
	/**
	 * Constructs an equality filter for the default currency
	 */
	public CurrencyFilter() {
		this(Selection.EQUALITY,Currency.getInstance(Locale.getDefault()),null,null,null);
	}
	
	/**
	 * Constructs a currency filter
	 * @param typeOfFilter The type of the filter. The default is EQUALITY
	 * @param equalityFilter Only relevant, if type == EQUALITY. Only entries with this currency are admitted. If null, the default currency is used
	 * @param minFilter Only relevant, if type == RANGE. Only entries whose currency is alphabetically larger than this one are admitted
	 * @param maxFilter Only relevant, if type == RANGE. Only entries whose currency is alphabetically smaller than this one are admitted
	 * @param regexFilter Only relevant, if type == REGEX. Only entries whose currency's ISO code matches the given regular expression, are admitted
	 * @throws PatternSyntaxException - If type == REGEX and regexFilter is not a valid regular expression
	 */
	public CurrencyFilter(Selection typeOfFilter, Currency equalityFilter, String minFilter, String maxFilter, String regexFilter) {
		this.typeOfFilter = typeOfFilter == null? Selection.EQUALITY : typeOfFilter;
		switch(this.typeOfFilter) {
		case EQUALITY: equalityCurrency = equalityFilter == null? Currency.getInstance(Locale.getDefault()) : equalityFilter; break;
		case REGEX: this.regexFilter = regexFilter == null? Pattern.compile("") : Pattern.compile(regexFilter); break;
		case RANGE: this.minFilter = minFilter; this.maxFilter = maxFilter ;		
		}
	}
	
	@Override
	public String getDescription() {
		String name = Fsfibu2StringTableMgr.getString("fs.fibu2.Entry.currency");
		switch(typeOfFilter) {
		case EQUALITY: return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.describeequals",name,equalityCurrency.getCurrencyCode());
		case REGEX: return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.describematches",name,regexFilter.pattern());
		case RANGE: return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.describerange",name,minFilter,maxFilter);
		default: return "";
		}
	}

	@Override
	public EntryFilterEditor getEditor() {
		return new CurrencyFilterEditor();
	}

	/**
	 * @return "ff2filter_currency"
	 */
	@Override
	public String getID() {
		return "ff2filter_currency";
	}

	@Override
	public String getName() {
		return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.ValueFilter.name");
	}

	@Override
	public boolean verifyEntry(Entry e) {
		if(e == null) return false;
		switch(typeOfFilter) {
		case EQUALITY: return equalityCurrency == e.getCurrency();
		case REGEX: Matcher m = regexFilter.matcher(e.getCurrency().getCurrencyCode());
					return m.matches();
		case RANGE: DefaultStringComparator c = new DefaultStringComparator();
					return c.compare(minFilter, e.getCurrency().getCurrencyCode()) <= 0 && c.compare(e.getCurrency().getCurrencyCode(), maxFilter) <= 0;
		default: return false;
		}
	}
	
	// LOCAL CLASS FOR EDITOR *************************
	// ************************************************
	
	private class CurrencyFilterEditor extends EntryFilterEditor {

		private static final long serialVersionUID = 5711524848654111903L;
		private StandardFilterComponent comp;
		
		public CurrencyFilterEditor() {
			comp = new StandardFilterComponent(Fsfibu2StringTableMgr.getString("fs.fibu2.Entry.currency") + ": ",null,new DefaultStringComparator(),
					typeOfFilter == Selection.REGEX? regexFilter.pattern() : (typeOfFilter == Selection.EQUALITY ? equalityCurrency.getCurrencyCode() : ""),
					typeOfFilter == Selection.RANGE? minFilter : "",
					typeOfFilter == Selection.RANGE? maxFilter : "",
					typeOfFilter);
			comp.addStandardComponentListener(new StandardComponentListener() {
				@Override
				public void contentChanged(StandardFilterComponent source) {fireStateChanged();}
				@Override
				public void selectionChanged(StandardFilterComponent source,
						Selection newSelection) {fireStateChanged();}
			});
			add(comp);	
		}
		
		@Override
		public EntryFilter getFilter() {
			if(comp.validateFilter() != Result.INCORRECT) {
				Selection selection = comp.getSelection();
				return new CurrencyFilter(selection, selection == Selection.EQUALITY? Currency.getInstance(comp.getSingleEntry()): null,
											   comp.getMinEntry(), comp.getMaxEntry(),comp.getSingleEntry());
			}
			else return null;
		}

		@Override
		public boolean hasValidContent() {
			return comp.validateFilter() != Result.INCORRECT;
		}
		
	}

}
