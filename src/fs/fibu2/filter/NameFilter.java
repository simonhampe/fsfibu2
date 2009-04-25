package fs.fibu2.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import fs.fibu2.data.model.Entry;
import fs.fibu2.filter.StandardFilterComponent.Selection;
import fs.fibu2.lang.Fsfibu2StringTableMgr;

/**
 * This filters entries on comparing their 'name' value to a certain filter.
 * @author Simon Hampe
 *
 */
public class NameFilter implements EntryFilter {

	private Selection typeOfFilter;	//Equality, regex or range
	private String firstFilter;		//The filter for equality or regex and the min value for range
	private String secondFilter;	//The max value for range
	
	private Pattern pattern;
	
	/**
	 * Creates a name filter
	 * @param typeOfFilter Whether this filter checks for string equality, matching of a
	 * regular expression or alphabetical range. If null, EQUALITY is selected.
	 * @param firstFilter The filter used for equality or pattern matching or as minimal value. null is
	 * interpreted as the empty string or as minus infinity for range filtering
	 * @param secondFilter The filter used as maximal value for range comparison. A null string  means
	 * plus infinity
	 * @throws PatternSyntaxException - If REGEX is the type of filter and the first
	 * filter is not a valid regular expression
	 */
	public NameFilter(Selection typeOfFilter, String firstFilter,
			String secondFilter) throws PatternSyntaxException{
		this.typeOfFilter = typeOfFilter == null? Selection.EQUALITY : typeOfFilter;
		this.firstFilter = firstFilter == null && this.typeOfFilter != Selection.RANGE? "" : firstFilter;
		this.secondFilter = secondFilter;
		if(this.typeOfFilter == Selection.REGEX) {
			pattern = Pattern.compile(this.firstFilter);
		}
	}

	@Override
	public String getDescription() {
		String name = Fsfibu2StringTableMgr.getString("fs.fibu2.global.name");
		switch(typeOfFilter) {
		case EQUALITY: return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.describeequals",name,firstFilter);
		case REGEX: return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.describematches",name,firstFilter);
		case RANGE: return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.describerange",name,firstFilter,secondFilter);
		default: return "";
		}
	}

	@Override
	public EntryFilterEditor getEditor() {
		// TODO Auto-generated method stub
		//TODO: Write general validator usable for all
		return null;
	}

	/**
	 * @return "ff2filter_name"
	 */
	@Override
	public String getID() {
		return "ff2filter_name";
	}

	/**
	 * @return "Name filter" in the appropriate language
	 */
	@Override
	public String getName() {
		return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.NameFilter.name");
	}

	/**
	 * Verifies the entry according to the given parameters
	 */
	@Override
	public boolean verifyEntry(Entry e) {
		if(e == null) return false;
		switch(typeOfFilter) {
		case EQUALITY: return e.getName().equals(firstFilter);
		case REGEX: 
			Matcher matcher = pattern.matcher(e.getName());
			return matcher.matches();
		case RANGE:
			return (firstFilter == null? true : firstFilter.compareTo(e.getName()) <= 0) &&
					(secondFilter == null? true : secondFilter.compareTo(e.getName()) >= 0);
		}
		//We cannot arrive here...
		return false;
	}

}
