package fs.fibu2.filter;

import java.text.ParseException;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.dom4j.Document;

import fs.fibu2.data.event.JournalAdapter;
import fs.fibu2.data.event.JournalListener;
import fs.fibu2.data.format.DefaultStringComparator;
import fs.fibu2.data.format.Fsfibu2DateFormats;
import fs.fibu2.data.model.Category;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.filter.StandardFilterComponent.Selection;
import fs.fibu2.filter.event.StandardComponentListener;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.model.CategoryListModel;
import fs.gui.SwitchIconLabel;
import fs.validate.LabelIndicValidator;
import fs.validate.ValidationResult;
import fs.validate.ValidationValidator;
import fs.validate.ValidationResult.Result;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;

/**
 * Filters entries according to their category
 * @author Simon Hampe
 *
 */
public class CategoryFilter implements EntryFilter {

	// FIELDS ************************
	// *******************************
	
	private Journal				associatedJournal;
	
	private Selection 			typeOfFilter;
	private Category		  	equalityCategory;
	
	private int					levelToCheck;
	private String				equalityString;
	private Pattern	  			regexFilter;
	private String				minFilter;
	private String				maxFilter;
	
	// CONSTRUCTORS ********************
	// *********************************
	
	/**
	 * Constructs a filter which selects the first catgeory in the list of categories of j for
	 * equality filtering. If there is none, it selects the empty string for equality filtering on the 
	 * first level
	 */
	public CategoryFilter(Journal j) {
		associatedJournal = j == null? new Journal() : j;
		typeOfFilter = Selection.EQUALITY;
		TreeSet<Category> set = new TreeSet<Category>(j.getListOfCategories());
		if(set.size() > 0) {
			equalityCategory = set.first();
		}
		else {
			equalityString = "";
			levelToCheck = 1;
		}
	}
	
	/**
	 * Only accepts entries in category c (and subcategories)
	 */
	public CategoryFilter(Category c, Journal j) {
		typeOfFilter = Selection.EQUALITY;
		equalityCategory = (c == null)? Category.getRootCategory() : c;
	}
	
	/**
	 * Only accepts entries, whose category has string c at level l
	 */
	public CategoryFilter(String c, int l) {
		typeOfFilter = Selection.EQUALITY;
		levelToCheck = l;
		equalityString = c == null? "" : c;
	}
	
	/**
	 * Only accepts entries, whose category matches pattern p at level l
	 */
	public CategoryFilter(Pattern p, int l) {
		typeOfFilter = Selection.REGEX;
		levelToCheck = l;
		regexFilter = p == null? Pattern.compile("") : p;
	}
	
	/**
	 * Only accepts entries, whose category is alphabetically between min and max at level l
	 */
	public CategoryFilter(String min, String max, int l) {
		typeOfFilter = Selection.RANGE;
		minFilter = min == null? "" : min;
		maxFilter = max == null? "" : max;
		levelToCheck = l;
	}
	
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
		return new CategoryFilterEditor();
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
			if(equalityCategory != null) return e.getCategory().isSubCategoryOf(equalityCategory);
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
	

	// LOCAL CLASS FOR EDITOR ********************************
	// *******************************************************
	
	private class CategoryFilterEditor extends EntryFilterEditor implements ResourceDependent {

		private StandardFilterComponent comp;
		
		private JComboBox comboBox = new JComboBox();
		private JRadioButton selectCategory = new JRadioButton();
		private JRadioButton selectAdvanced = new JRadioButton();
		private SwitchIconLabel levelLabel = new SwitchIconLabel();
		private JTextField levelField = new JTextField();
		
		private LabelIndicValidator<JTextField> levelValidator;
		
		private ImageIcon warn = new ImageIcon(Fsfibu2DefaultReference.getDefaultReference().getFullResourcePath(this, "graphics/share/warn.png"));
		
		private JournalListener listener = new JournalAdapter() {
			@Override
			public void entriesAdded(Journal source, Entry[] newEntries) {
				lock
			}

			@Override
			public void entriesRemoved(Journal source, Entry[] oldEntries) {
				// TODO Auto-generated method stub
				super.entriesRemoved(source, oldEntries);
			}

			@Override
			public void entryReplaced(Journal source, Entry oldEntry,
					Entry newEntry) {
				// TODO Auto-generated method stub
				super.entryReplaced(source, oldEntry, newEntry);
			}
			
			//(un)locks the category selection
			private lockCategory(boolean lock) {
				
			}
		};
		
		public CategoryFilterEditor() {
			//Init components
			comboBox.setModel(new CategoryListModel(associatedJournal));
			
			selectCategory.setText(Fsfibu2StringTableMgr.getString("fs.fibu2.Entry.category" + ": "));
			selectAdvanced.setText("");
			ButtonGroup group = new ButtonGroup();
				group.add(selectAdvanced);
				group.add(selectCategory);
			levelLabel.setIconReference(warn);
			levelLabel.setText("fs.fibu2.filter.CategoryFilter.level" + ": ");
			levelLabel.setHorizontalTextPosition(JLabel.LEFT);
				
			if(typeOfFilter == Selection.EQUALITY) {
				if(equalityCategory != null) {
					comboBox.setSelectedItem(equalityCategory);
					selectCategory.setSelected(true);
				}
				else {
					selectAdvanced.setSelected(true);
					levelField.setText(Integer.toString(levelToCheck));
				}
			}
			
			String singleString = (typeOfFilter == Selection.EQUALITY && equalityCategory == null? equalityString : (typeOfFilter == Selection.REGEX? regexFilter.pattern() : ""));
			comp = new StandardFilterComponent(Fsfibu2StringTableMgr.getString("fs.fibu2.Entry.category"),null,new DefaultStringComparator(),
										singleString,
										typeOfFilter == Selection.RANGE? minFilter : "",
										typeOfFilter == Selection.RANGE? maxFilter : "",typeOfFilter);
			
			//Layout
			Box layout = new Box(BoxLayout.Y_AXIS);
			Box box1 = new Box(BoxLayout.X_AXIS);
				box1.add(selectCategory); box1.add(comboBox);
				layout.add(box1);
			Box box2 = new Box(BoxLayout.X_AXIS);
				box2.add(selectAdvanced); box2.add(levelLabel);box2.add(levelField);
				layout.add(box2);
			layout.add(comp);
			
			//Validation
			levelValidator = new LabelIndicValidator<JTextField>(null, null, warn) {
				@Override
				protected void registerToComponent(JTextField arg0) {
					levelField.getDocument().addDocumentListener(this);
				}
				@Override
				protected void unregisterFromComponent(JTextField arg0) {
					levelField.getDocument().removeDocumentListener(this);
				}
				@Override
				public Result validate(JTextField component) {
					Result r = Result.CORRECT;
					if(!selectAdvanced.isSelected()) r = Result.CORRECT;
					else {
						try {
							int i = Integer.parseInt(levelField.getText());
							if(i <= 0) throw new NumberFormatException();
						}
						catch(NumberFormatException e) {
							setToolTipText(component, Fsfibu2StringTableMgr.getString("fs.fibu2.filter.CategoryFilter.nointeger"));
							r = Result.INCORRECT;
						}
					}
					if(r == Result.CORRECT) setToolTipText(component, null);
					return r;
				}
			};
			levelValidator.addComponent(levelField, levelLabel);
			
			levelValidator.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					fireStateChanged();
				}
			});
			comp.addStandardComponentListener(new StandardComponentListener() {
				@Override
				public void contentChanged(StandardFilterComponent source) {fireStateChanged();}
				@Override
				public void selectionChanged(StandardFilterComponent source,
						Selection newSelection) {fireStateChanged();}
			});
		}
		
		@Override
		public EntryFilter getFilter() {
			if(hasValidContent()) {
				
			}
			else return null;
		}

		@Override
		public boolean hasValidContent() {
			return comp.validateFilter() != Result.INCORRECT && levelValidator.validate().getOverallResult() != Result.INCORRECT;
		}

		@Override
		public void assignReference(ResourceReference r) {
			//Ignored
		}

		@Override
		public Document getExpectedResourceStructure() {
			XMLDirectoryTree tree = new XMLDirectoryTree();
			tree.addPath("graphics/share/warn.png");
			return tree;
		}
		
	}

}
