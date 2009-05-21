package fs.fibu2.filter;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import fs.fibu2.data.format.DefaultFloatComparator;
import fs.fibu2.data.format.GivenFormatValidator;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.filter.StandardFilterComponent.Selection;
import fs.fibu2.filter.event.StandardComponentListener;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.validate.ValidationResult.Result;

/**
 * This filter filters entries according to their value field.
 * @author Simon Hampe
 *
 */
public class ValueFilter implements EntryFilter {

	// FIELDS *************************
	// ********************************
	
	private Selection typeOfFilter;
	private float equalityFilter;
	private float minFilter;
	private float maxFilter;
	private Pattern regexFilter;
	
	private static NumberFormat format = NumberFormat.getInstance();
	
	/**
	 * Creates a value filter using range filtering between 0 and 0
	 */
	public ValueFilter() {
		this(Selection.RANGE,0,0,0,null); 
	}
	
	/**
	 * Constructs an equality filter
	 */
	public ValueFilter(float filter) {
		this(Selection.EQUALITY, filter, 0,0,null);
	}
	
	/**
	 * Constructs a regular expression filter
	 * @throws PatternSyntaxException - If regex is not a valid regular expression
	 */
	public ValueFilter(String regex) {
		this(Selection.REGEX, 0,0,0,regex);
	}
	
	/**
	 * Constructs a range filter
	 */
	public ValueFilter(float min, float max) {
		this(Selection.RANGE, 0,min,max,null);
	}
	
	/**
	 * Creates a ValueFilter.
	 * @param typeOfFilter Whether entries with a certain value are desired (EQUALITY), a certain range (RANGE) or entries whose value matches
	 * a certain regular expression (REGEX). Depending on this value, only a subset of the other parameters is really needed. The default value is
	 * RANGE
	 * @param equalityFilter The filter value for equality filtering 
	 * @param minFilter The minimum value for range filtering. The default is 0
	 * @param maxFilter The maximum value for range filtering. The default is 0
	 * @param regexFilter The string used for regular expression filtering
	 * @throws PatternSyntaxException - If regex is not a valid regular expression and REGEX is selected
	 */
	public ValueFilter(Selection typeOfFilter, float equalityFilter,
			float minFilter, float maxFilter, String regexFilter) {
		super();
		this.typeOfFilter = typeOfFilter == null? Selection.EQUALITY : typeOfFilter;
		this.equalityFilter = equalityFilter;
		this.minFilter = minFilter;
		this.maxFilter = maxFilter;
		if(this.typeOfFilter == Selection.REGEX) {
			this.regexFilter = Pattern.compile(regexFilter == null? "" : regexFilter);
		}
	}

	@Override
	public String getDescription() {
		String name = Fsfibu2StringTableMgr.getString("fs.fibu2.Entry.value");
		switch(typeOfFilter) {
		case EQUALITY: return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.describeequals",name,format.format(equalityFilter));
		case REGEX: return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.describematches",name,regexFilter.pattern());
		case RANGE: return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.describerange",name,format.format(minFilter),format.format(maxFilter));
		default: return "";
		}
	}

	@Override
	public EntryFilterEditor getEditor(Journal j) {
		ValueFilterEditor editor = new ValueFilterEditor();
		return editor;
	}

	/**
	 * @return "ff2filter_value"
	 */
	@Override
	public String getID() {
		return "ff2filter_value";
	}

	@Override
	public String getName() {
		return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.ValueFilter.name");
	}

	@Override
	public boolean verifyEntry(Entry e) {
		if(e == null) return false;
		switch(typeOfFilter) {
		case EQUALITY: return e.getValue() == equalityFilter;
		case RANGE: return minFilter <= e.getValue() && e.getValue() <= maxFilter;
		case REGEX: Matcher m = (regexFilter.matcher(format.format(e.getValue())));
					return m.matches();
		default: 	return false;
		}
	}
	
	@Override
	public EntryFilter createMeFromPreferences(Preferences filterNode)
			throws IllegalArgumentException {
		if(filterNode == null) throw new NullPointerException("Cannot read preferences from null node");
		Selection type = AbstractFilterPreferences.getType(filterNode);
		if(type == null) throw new IllegalArgumentException("Invalid node: No type entry");
		switch(type) {
		case EQUALITY: return new ValueFilter(Float.parseFloat(AbstractFilterPreferences.getEqualityString(filterNode)));
		case REGEX: return new ValueFilter(AbstractFilterPreferences.getPatternString(filterNode));
		case RANGE: return new ValueFilter(Float.parseFloat(AbstractFilterPreferences.getMinString(filterNode)),
				Float.parseFloat(AbstractFilterPreferences.getMaxString(filterNode)));
		default: return new ValueFilter();
		}
	}

	@Override
	public void insertMyPreferences(Preferences node) throws NullPointerException{
		if(node == null) throw new NullPointerException("Cannot insert preferences in null node");
		AbstractFilterPreferences.insert(node.node("filter"),typeOfFilter, getID(), Float.toString(equalityFilter), 
				regexFilter != null? regexFilter.pattern() : null, 
				Float.toString(minFilter), Float.toString(maxFilter));
	}
	
	// LOCAL CLASS FOR EDITOR *************************
	// ************************************************
	
	private class ValueFilterEditor extends EntryFilterEditor {

		private static final long serialVersionUID = 2835173633883277671L;
		private StandardFilterComponent comp;
		
		public ValueFilterEditor() {
			comp = new StandardFilterComponent(Fsfibu2StringTableMgr.getString("fs.fibu2.Entry.value") + ": ",new GivenFormatValidator(format),new DefaultFloatComparator(format),
					typeOfFilter == Selection.REGEX? regexFilter.pattern() : (typeOfFilter == Selection.EQUALITY ? Float.toString(equalityFilter) : ""),
					typeOfFilter == Selection.RANGE? format.format(minFilter) : "",
					typeOfFilter == Selection.RANGE? format.format(maxFilter) : "",
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
				try {
					return new ValueFilter(selection, selection == Selection.EQUALITY? format.parse(comp.getSingleEntry()).floatValue() : 0,
													  selection == Selection.RANGE? format.parse(comp.getMinEntry()).floatValue() : 0,
													  selection == Selection.RANGE? format.parse(comp.getMaxEntry()).floatValue() : 0,
													  selection == Selection.REGEX? comp.getSingleEntry() : "");
				} catch (ParseException e) {
					//Will not happen
					return null;
				}
			}
			else return null;
		}

		@Override
		public boolean hasValidContent() {
			return comp.validateFilter() != Result.INCORRECT;
		}
		
	}

}
