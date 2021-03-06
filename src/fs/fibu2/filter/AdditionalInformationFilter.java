package fs.fibu2.filter;

import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import fs.fibu2.data.format.DefaultStringComparator;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.filter.StandardFilterComponent.Selection;
import fs.fibu2.filter.event.StandardComponentListener;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.validate.ValidationResult.Result;

/**
 * Filters entries for their additional information field
 * @author Simon Hampe
 *
 */
public class AdditionalInformationFilter implements EntryFilter {
	
	private Selection typeOfFilter;	//Equality, regex or range
	private String firstFilter;		//The filter for equality or regex and the min value for range
	private String secondFilter;	//The max value for range
	
	private Pattern pattern;		//The pattern compiled for REGEX
	
	/**
	 * Creates a standard filter, where Equality is the selected filter mode and the initial
	 * search string is the empty string
	 */
	public AdditionalInformationFilter() {
		this(Selection.EQUALITY,"",null);
	}
	
	/**
	 * Constructs a filter for either regex or equality filtering
	 * @param typeOfFilter Either REGEX or EQUALITY
	 * @param filter The associated filter string
	 * @throws IllegalArgumentException - If typeOfFilter == RANGE
	 * @throws PatternSyntaxException - If REGEX is the type of filter and the filter string is not a valid regular expression
	 */
	public AdditionalInformationFilter(Selection typeOfFilter, String filter) {
		this(typeOfFilter,filter,null);
		if(typeOfFilter == Selection.RANGE) throw new IllegalArgumentException("Wrong constructor for RANGE filter.");
	}
	
	/**
	 * Constructs a range filter
	 * @param min the minimum value. Empty, if null is passed
	 * @param max the maximal value. Empty, if null is passed
	 */
	public AdditionalInformationFilter(String min, String max) {
		this(Selection.RANGE, min,max);
	}
	
	/**
	 * Creates a filter
	 * @param typeOfFilter Whether this filter checks for string equality, matching of a
	 * regular expression or alphabetical range. If null, EQUALITY is selected.
	 * @param firstFilter The filter used for equality or pattern matching or as minimal value. null is
	 * interpreted as the empty string or as minus infinity for range filtering
	 * @param secondFilter The filter used as maximal value for range comparison. A null string  means
	 * plus infinity
	 * @throws PatternSyntaxException - If REGEX is the type of filter and the first
	 * filter is not a valid regular expression
	 */
	public AdditionalInformationFilter(Selection typeOfFilter, String firstFilter,
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
		String name = Fsfibu2StringTableMgr.getString("fs.fibu2.filter.AdditionalInformationFilter.shortname");
		switch(typeOfFilter) {
		case EQUALITY: return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.describeequals",name,firstFilter);
		case REGEX: return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.describematches",name,firstFilter);
		case RANGE: return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.describerange",name,
				firstFilter == null? Fsfibu2StringTableMgr.getString("fs.fibu2.filter.minusinfinity") : firstFilter,
				secondFilter == null? Fsfibu2StringTableMgr.getString("fs.fibu2.filter.plusinfinity"): secondFilter);
		default: return "";
		}
	}

	@Override
	public EntryFilterEditor getEditor(Journal j) {
		return new AdditionalInformationFilterEditor();
	}

	/**
	 * @return "ff2filter_additionalinformation"
	 */
	@Override
	public String getID() {
		return "ff2filter_additionalinformation";
	}

	/**
	 * @return "Additional information filter" in the appropriate language
	 */
	@Override
	public String getName() {
		return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.AdditionalInformationFilter.name");
	}

	/**
	 * Verifies the entry according to the given parameters
	 */
	@Override
	public boolean verifyEntry(Entry e) {
		if(e == null) return false;
		switch(typeOfFilter) {
		case EQUALITY: return e.getAdditionalInformation().equals(firstFilter);
		case REGEX: 
			Matcher matcher = pattern.matcher(e.getAdditionalInformation());
			return matcher.matches();
		case RANGE:
			return (firstFilter == null? true : firstFilter.compareTo(e.getAdditionalInformation()) <= 0) &&
					(secondFilter == null? true : secondFilter.compareTo(e.getAdditionalInformation()) >= 0);
		}
		//We cannot arrive here...
		return false;
	}

	@Override
	public EntryFilter createMeFromPreferences(Preferences filterNode)
			throws IllegalArgumentException {
		if(filterNode == null) throw new NullPointerException("Cannot read preferences from null node");
		Selection type = AbstractFilterPreferences.getType(filterNode);
		if(type == null) throw new IllegalArgumentException("Invalid node: No type entry");
		switch(type) {
		case EQUALITY: return new AdditionalInformationFilter(type, AbstractFilterPreferences.getEqualityString(filterNode));
		case REGEX: return new AdditionalInformationFilter(type, AbstractFilterPreferences.getPatternString(filterNode));
		case RANGE: return new AdditionalInformationFilter(AbstractFilterPreferences.getMinString(filterNode),AbstractFilterPreferences.getMaxString(filterNode));
		default: return new NameFilter();
		}
	}

	@Override
	public void insertMyPreferences(Preferences node) throws NullPointerException{
		if(node == null) throw new NullPointerException("Cannot insert preferences in null node");
		AbstractFilterPreferences.insert(node.node("filter"),typeOfFilter, getID(), firstFilter, firstFilter, firstFilter, secondFilter);
	}
	
	// LOCAL CLASS FOR EDITOR ******************
	// *****************************************
	
	private class AdditionalInformationFilterEditor extends EntryFilterEditor {

		private static final long serialVersionUID = 8289596103885776826L;
		private StandardFilterComponent comp;
		
		public AdditionalInformationFilterEditor() {
			comp = new StandardFilterComponent(
					Fsfibu2StringTableMgr.getString("fs.fibu2.filter.AdditionalInformationFilter.shortname") + ": ",
					null,new DefaultStringComparator(),
					typeOfFilter != Selection.RANGE? firstFilter : "",
					typeOfFilter == Selection.RANGE? firstFilter : "",
					typeOfFilter == Selection.RANGE? secondFilter : "",
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
				return new AdditionalInformationFilter(selection,selection != Selection.RANGE ? comp.getSingleEntry() : comp.getMinEntry(),
												selection != Selection.RANGE ? null : comp.getMaxEntry());
			}
			else return null;
		}

		@Override
		public boolean hasValidContent() {
			return comp.validateFilter() != Result.INCORRECT;
		}
		
	}
	
}
