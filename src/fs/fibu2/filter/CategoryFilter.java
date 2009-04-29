package fs.fibu2.filter;

import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fs.fibu2.data.format.DefaultStringComparator;
import fs.fibu2.data.format.Fsfibu2DateFormats;
import fs.fibu2.data.model.Category;
import fs.fibu2.data.model.Entry;
import fs.fibu2.filter.StandardFilterComponent.Selection;
import fs.fibu2.lang.Fsfibu2StringTableMgr;

/**
 * Filters entries according to their category
 * @author Simon Hampe
 *
 */
public class CategoryFilter implements EntryFilter {

	// FIELDS ************************
	// *******************************
	
	private Selection 			typeOfFilter;
	private Category		  	equalityCategory;
	
	private int					levelToCheck;
	private String				equalityString;
	private Pattern	  			regexFilter;
	private String				minFilter;
	private String				maxFilter;
	
	// CONSTRUCTORS ********************
	// *********************************
	
	
	
	// FILTER METHODS ******************
	// *********************************
	
	@Override
	public String getDescription() {
		String name = Fsfibu2StringTableMgr.getString("fs.fibu2.Entry.category");
		switch(typeOfFilter) {
		case EQUALITY: return equalityCategory == null? Fsfibu2StringTableMgr.getString("fs.fibu2.filter.CategoryFilter.describeequals",equalityString,levelToCheck) 
				: Fsfibu2StringTableMgr.getString("fs.fibu2.filter.describeequals",name,equalityCategory.toString());
		case REGEX: return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.describematches",name,regexFilter.pattern());
		case RANGE: return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.describerange",name,minFilter,maxFilter);
		default: return "";
		}
	}

	@Override
	public EntryFilterEditor getEditor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getID() {
		return "ff2filter_category";
	}

	@Override
	public String getName() {
		return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.CategoryFilter.name");
	}

	@Override
	public boolean verifyEntry(Entry e) {
		if(e ==  null) return false;
		switch(typeOfFilter) {
		case EQUALITY: 
			if(equalityCategory != null) return equalityCategory == e.getCategory();
			else {
				if(e.getCategory().getOrder() < levelToCheck) return false;
				return e.getCategory().getOrderedList().get(levelToCheck-1).equals(equalityString);
			}
		case REGEX: Matcher m = regexFilter.matcher(e.getCategory().getOrderedList().get(levelToCheck-1));
					return m.matches();
		case RANGE: Comparator<String> c = new DefaultStringComparator();
					String toCompare = e.getCategory().getOrderedList().get(levelToCheck-1);
					return c.compare(minFilter, toCompare) <= 0 && c.compare(toCompare, maxFilter) <= 0;
		default: return false;
		}
	}

}
