package fs.fibu2.filter;

import java.text.ParseException;
import java.util.GregorianCalendar;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import fs.fibu2.data.format.EntryDateComparator;
import fs.fibu2.data.format.Fsfibu2DateFormats;
import fs.fibu2.data.format.GivenFormatValidator;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.filter.StandardFilterComponent.Selection;
import fs.fibu2.filter.event.StandardComponentListener;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.validate.ValidationResult.Result;

/**
 * Filters entries according to their date
 * @author Simon Hampe
 *
 */
public class DateFilter implements EntryFilter {

	// FIELDS ************************
	// *******************************
	
	private Selection 			typeOfFilter;
	private GregorianCalendar  	equalityDate;
	private Pattern	  			regexFilter;
	private GregorianCalendar	minFilter;
	private GregorianCalendar	maxFilter;
	
	// CONSTRUCTORS ***********************
	// ************************************
	
	/**
	 * Constructs an equality filter for the current date
	 */
	public DateFilter() {
		this(Selection.EQUALITY,new GregorianCalendar(),null,null,null);
	}
	
	/**
	 * Constructs a currency filter
	 * @param typeOfFilter The type of the filter. The default is EQUALITY
	 * @param equalityFilter Only relevant, if type == EQUALITY. Only entries with this date are admitted. If null, the current date is used
	 * @param minFilter Only relevant, if type == RANGE. Only entries whose date is later than this one are admitted
	 * @param maxFilter Only relevant, if type == RANGE. Only entries whose date is earlier than this one are admitted
	 * @param regexFilter Only relevant, if type == REGEX. Only entries whose currency's ISO code matches the given regular expression, are admitted
	 * @throws PatternSyntaxException - If type == REGEX and regexFilter is not a valid regular expression
	 */
	public DateFilter(Selection typeOfFilter, GregorianCalendar equalityFilter, GregorianCalendar minFilter, GregorianCalendar maxFilter, String regexFilter) {
		this.typeOfFilter = typeOfFilter == null? Selection.EQUALITY : typeOfFilter;
		switch(this.typeOfFilter) {
		case EQUALITY: equalityDate = equalityFilter == null? new GregorianCalendar() : equalityFilter; break;
		case REGEX: this.regexFilter = regexFilter == null? Pattern.compile("") : Pattern.compile(regexFilter); break;
		case RANGE: this.minFilter = minFilter; this.maxFilter = maxFilter ;		
		}
	}
	
	@Override
	public String getDescription() {
		String name = Fsfibu2StringTableMgr.getString("fs.fibu2.Entry.date");
		switch(typeOfFilter) {
		case EQUALITY: return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.describeequals",name,Fsfibu2DateFormats.getEntryDateFormat().format(equalityDate.getTime()));
		case REGEX: return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.describematches",name,regexFilter.pattern());
		case RANGE: return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.describerange", name,
				minFilter == null? Fsfibu2StringTableMgr.getString("fs.fibu2.filter.minusinfinity") : Fsfibu2DateFormats.getEntryDateFormat().format(minFilter.getTime()),
				maxFilter == null? Fsfibu2StringTableMgr.getString("fs.fibu2.filter.plusinfinity"): Fsfibu2DateFormats.getEntryDateFormat().format(maxFilter.getTime()));
		default: return "";
		}
	}

	@Override
	public EntryFilterEditor getEditor(Journal j) {
		return new DateFilterEditor();
	}

	@Override
	public String getID() {
		return "ff2filter_date";
	}

	@Override
	public String getName() {
		return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.DateFilter.name");
	}

	@Override
	public boolean verifyEntry(Entry e) {
		if(e == null) return false;
		switch(typeOfFilter) {
		case EQUALITY: return new EntryDateComparator().compare(equalityDate, e.getDate()) == 0;
		case REGEX: Matcher m = regexFilter.matcher(Fsfibu2DateFormats.getEntryDateFormat().format(e.getDate().getTime()));
					return m.matches();
		case RANGE: EntryDateComparator c = new EntryDateComparator();
					return c.compare(minFilter, e.getDate()) <= 0 && c.compare(e.getDate(), maxFilter) <= 0;
		default: return false;
		}
	}
	
	@Override
	public EntryFilter createMeFromPreferences(Preferences filterNode)
			throws IllegalArgumentException {
		if(filterNode == null) throw new NullPointerException("Cannot read preferences from null node");
		Selection type = AbstractFilterPreferences.getType(filterNode);
		if(type == null) throw new IllegalArgumentException("Invalid node: No type entry");
		try {
			switch(type) {
			case EQUALITY: return new DateFilter(type,Fsfibu2DateFormats.parseEntryDateFormat(AbstractFilterPreferences.getEqualityString(filterNode)),
												null,null,null);
			case REGEX: return new DateFilter(type, null,null,null,AbstractFilterPreferences.getPatternString(filterNode));
			case RANGE: return new DateFilter(type, null, 
									Fsfibu2DateFormats.parseEntryDateFormat(AbstractFilterPreferences.getMinString(filterNode)),
									Fsfibu2DateFormats.parseEntryDateFormat(AbstractFilterPreferences.getMaxString(filterNode)),null);
			default: return new DateFilter();
			}
		}
		catch(ParseException pe) {
			throw new IllegalArgumentException("Invalid node: Invalid date format: " + pe.getMessage());
		}
	}

	@Override
	public void insertMyPreferences(Preferences node) throws NullPointerException{
		if(node == null) throw new NullPointerException("Cannot insert preferences in null node");
		AbstractFilterPreferences.insert(node.node("filter"),typeOfFilter, getID(), 
				equalityDate != null? Fsfibu2DateFormats.getEntryDateFormat().format(equalityDate.getTime()) : null,
				regexFilter != null? regexFilter.pattern() : null,
				minFilter != null? Fsfibu2DateFormats.getEntryDateFormat().format(minFilter.getTime()) : null,
				maxFilter != null? Fsfibu2DateFormats.getEntryDateFormat().format(maxFilter.getTime()) : null);
	}
	
	// LOCAL CLASS FOR EDITOR *************************
	// ************************************************
	
	private class DateFilterEditor extends EntryFilterEditor {

		private static final long serialVersionUID = -2927993096684716146L;
		private StandardFilterComponent comp;
		
		public DateFilterEditor() {
			comp = new StandardFilterComponent(Fsfibu2StringTableMgr.getString("fs.fibu2.Entry.date") + ": ",
					new GivenFormatValidator(Fsfibu2DateFormats.getEntryDateFormat()),
					new EntryDateComparator(),
					typeOfFilter == Selection.REGEX? regexFilter.pattern() : (typeOfFilter == Selection.EQUALITY ? Fsfibu2DateFormats.getEntryDateFormat().format(equalityDate.getTime()): ""),
					typeOfFilter == Selection.RANGE? (minFilter == null? "" : Fsfibu2DateFormats.getEntryDateFormat().format(minFilter.getTime())) : "",
					typeOfFilter == Selection.RANGE? (maxFilter == null? "" : Fsfibu2DateFormats.getEntryDateFormat().format(maxFilter.getTime())) : "",
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
				GregorianCalendar eq = new GregorianCalendar();
				GregorianCalendar min = new GregorianCalendar();
				GregorianCalendar max = new GregorianCalendar();
				try {
					switch(selection) {
					case EQUALITY: eq.setTime(Fsfibu2DateFormats.getEntryDateFormat().parse(comp.getSingleEntry())); break;
					case RANGE: if(comp.getMinEntry() != null) min.setTime(Fsfibu2DateFormats.getEntryDateFormat().parse(comp.getMinEntry()));
								else min = null;
								if(comp.getMaxEntry() != null) max.setTime(Fsfibu2DateFormats.getEntryDateFormat().parse(comp.getMaxEntry()));
								else max = null;
					}
					return new DateFilter(selection, eq,min,max,comp.getSingleEntry());
				}
				catch(ParseException e) {
					//Will never happen
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
