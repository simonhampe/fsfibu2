package fs.fibu2.filter;

import java.util.Comparator;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.dom4j.Document;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;

import fs.fibu2.data.format.DefaultStringComparator;
import fs.fibu2.data.model.Category;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.filter.StandardFilterComponent.Selection;
import fs.fibu2.filter.event.StandardComponentListener;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.model.CategoryListModel;
import fs.fibu2.view.render.CategoryListRenderer;
import fs.gui.SwitchIconLabel;
import fs.validate.LabelIndicValidator;
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
	 * Constructs a filter which selects the root category as filter category
	 */
	public CategoryFilter() {
		typeOfFilter = Selection.EQUALITY;
		equalityCategory = Category.getRootCategory();
	}
	
	/**
	 * Only accepts entries in category c (and subcategories)
	 */
	public CategoryFilter(Category c) {
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
		minFilter =  min;
		maxFilter =  max;
		levelToCheck = l;
	}
	
	// GETTERS *************************
	// *********************************
	
	/**
	 * @return The category for equality check, or null, if this is an advanced filter
	 */
	public Category getEqualityCategory() {
		return equalityCategory;
	}
	
	// FILTER METHODS ******************
	// *********************************
	
	@Override
	public String getDescription() {
		String name = Fsfibu2StringTableMgr.getString("fs.fibu2.Entry.category");
		switch(typeOfFilter) {
		case EQUALITY: return equalityCategory == null? Fsfibu2StringTableMgr.getString("fs.fibu2.filter.CategoryFilter.describeequals",equalityString,levelToCheck) 
				: Fsfibu2StringTableMgr.getString("fs.fibu2.filter.describeequals",name,equalityCategory.toString());
		case REGEX: return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.CategoryFilter.describematches", regexFilter.pattern(),levelToCheck);
		case RANGE: return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.CategoryFilter.describerange",
				minFilter == null? Fsfibu2StringTableMgr.getString("fs.fibu2.filter.minusinfinity") : minFilter,
				maxFilter == null? Fsfibu2StringTableMgr.getString("fs.fibu2.filter.plusinfinity"): maxFilter,
				levelToCheck);
		default: return "";
		}
	}

	@Override
	public EntryFilterEditor getEditor(Journal j) {
		return new CategoryFilterEditor(j);
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
				if(e.getCategory().getOrder() < levelToCheck || levelToCheck <= 0) return false;
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
	

	@Override
	public EntryFilter createMeFromPreferences(Preferences filterNode)
			throws IllegalArgumentException {
		if(filterNode == null) throw new NullPointerException("Cannot read preferences from null node");
		Selection type = AbstractFilterPreferences.getType(filterNode);
		if(type == null) throw new IllegalArgumentException("Invalid node: No type entry");
		switch(type) {
		case EQUALITY: 
			try {
				if(filterNode.nodeExists("category")) {
					Preferences tailNode = filterNode.node("category");
					Vector<String> list = new Vector<String>();
					while(tailNode.nodeExists("tail")) {
						tailNode = tailNode.node("tail");
						list.add(tailNode.get("tail", ""));
					}
					return new CategoryFilter(Category.getCategory(list));
				}
				else {
					try {
						int l = Integer.parseInt(filterNode.get("level", null));
						return new CategoryFilter(AbstractFilterPreferences.getEqualityString(filterNode),l);
					}
					catch(ParseException pe) {
						throw new IllegalArgumentException("Invalid node: No level entry");
					}
				}
			} catch (BackingStoreException e) {
				throw new IllegalArgumentException("Cannot access backing store");
			}
		case REGEX: 
			try {
				int l = Integer.parseInt(filterNode.get("level", null));
				return new CategoryFilter(Pattern.compile(AbstractFilterPreferences.getPatternString(filterNode)),l);
			}
			catch(ParseException pe) {
				throw new IllegalArgumentException("Invalid node: No level entry");
			}
		case RANGE: 
			try {
				int l = Integer.parseInt(filterNode.get("level", null));
				return new CategoryFilter(AbstractFilterPreferences.getMinString(filterNode),AbstractFilterPreferences.getMaxString(filterNode),l);
			}
			catch(ParseException pe) {
				throw new IllegalArgumentException("Invalid node: No level entry");
			}
		default: return new CategoryFilter();
		}
	}

	@Override
	public void insertMyPreferences(Preferences node) throws NullPointerException{
		if(node == null) throw new NullPointerException("Cannot insert preferences in null node");
		Preferences filterNode = node.node("filter");
		AbstractFilterPreferences.insert(filterNode,typeOfFilter, getID(), equalityString,regexFilter != null? regexFilter.pattern(): null,minFilter,maxFilter);
		filterNode.put("level", Integer.toString(levelToCheck));
		if(equalityCategory != null) {
			Preferences categoryNode = filterNode.node("category");
			Preferences tailNode = categoryNode;
			for(String s : equalityCategory.getOrderedList()) {
				tailNode = tailNode.node("tail");
				tailNode.put("tail", s);
			}
		}
	}
	
	// LOCAL CLASS FOR EDITOR ********************************
	// *******************************************************
	
	private class CategoryFilterEditor extends EntryFilterEditor implements ResourceDependent {
		private static final long serialVersionUID = -7259619306180644175L;

		private Journal associatedJournal;
		
		private StandardFilterComponent comp;
		
		private JComboBox comboBox = new JComboBox();
		private JRadioButton selectCategory = new JRadioButton();
		private JRadioButton selectAdvanced = new JRadioButton();
		private SwitchIconLabel levelLabel = new SwitchIconLabel();
		private JTextField levelField = new JTextField();
		
		private LabelIndicValidator<JTextField> levelValidator;
		
		private ImageIcon warn = new ImageIcon(Fsfibu2DefaultReference.getDefaultReference().getFullResourcePath(this, "graphics/share/warn.png"));
		
		private ListDataListener listener = new ListDataListener() {

			@Override
			public void contentsChanged(ListDataEvent e) {
				updateCategory();
			}

			@Override
			public void intervalAdded(ListDataEvent e) {
				updateCategory();
			}

			@Override
			public void intervalRemoved(ListDataEvent e) {
				updateCategory();
			}
			
		};
		
		// CONSTRUCTOR ****************************
		// ****************************************
		
		public CategoryFilterEditor(Journal j) {
			associatedJournal = j == null? new Journal() : j;
			
			//Init components
			comboBox.setModel(new CategoryListModel(associatedJournal,true));
			updateCategory();
			comboBox.getModel().addListDataListener(listener);
			
			comboBox.setRenderer(new CategoryListRenderer(" > "));
			
			selectCategory.setText(Fsfibu2StringTableMgr.getString("fs.fibu2.Entry.category") + ": ");
			selectAdvanced.setText("");
			ButtonGroup group = new ButtonGroup();
				group.add(selectAdvanced);
				group.add(selectCategory);
			levelLabel.setIconReference(warn);
			levelLabel.setText(Fsfibu2StringTableMgr.getString("fs.fibu2.filter.CategoryFilter.level") + ": ");
			levelLabel.setHorizontalTextPosition(JLabel.LEFT);
				
			if(typeOfFilter == Selection.EQUALITY && equalityCategory != null && comboBox.getModel().getSize() > 0) {
				//We only filter for categories, if at least one is used
				comboBox.setSelectedItem(equalityCategory);
				selectCategory.setSelected(true);
			}
			else {
				selectAdvanced.setSelected(true);
				levelField.setText(Integer.toString(levelToCheck >= 1? levelToCheck : 1));
			}
			
			String singleString = (typeOfFilter == Selection.EQUALITY && equalityCategory == null? equalityString : (typeOfFilter == Selection.REGEX? regexFilter.pattern() : ""));
			comp = new StandardFilterComponent(Fsfibu2StringTableMgr.getString("fs.fibu2.Entry.category") + ": ",null,new DefaultStringComparator(),
										singleString,
										typeOfFilter == Selection.RANGE? minFilter : "",
										typeOfFilter == Selection.RANGE? maxFilter : "",typeOfFilter);			
			
			//Layout
			Box layout = new Box(BoxLayout.Y_AXIS);
			Box vbox1 = new Box(BoxLayout.Y_AXIS);
				Box hbox0 = new Box(BoxLayout.X_AXIS);
					hbox0.add(selectCategory);
					hbox0.add(Box.createHorizontalGlue());
				vbox1.add(hbox0);
				vbox1.add(Box.createVerticalStrut(5));
				Box hbox1 = new Box(BoxLayout.X_AXIS);
					hbox1.add(selectAdvanced); hbox1.add(levelLabel);hbox1.add(Box.createHorizontalGlue());
				vbox1.add(hbox1);
			Box vbox2 = new Box(BoxLayout.Y_AXIS);
				vbox2.add(comboBox);
				vbox2.add(Box.createVerticalStrut(5));
				Box hbox2 = new Box(BoxLayout.X_AXIS);
					hbox2.setAlignmentX(RIGHT_ALIGNMENT);
					hbox2.add(levelField); hbox2.add(Box.createHorizontalGlue());
				vbox2.add(hbox2);
			Box hbox = new Box(BoxLayout.X_AXIS);
				hbox.add(vbox1);
				hbox.add(vbox2);
			layout.add(hbox);
			layout.add(Box.createVerticalStrut(5));
			layout.add(new JSeparator(JSeparator.HORIZONTAL));
			layout.add(Box.createVerticalStrut(5));
			Box box3 = new Box(BoxLayout.X_AXIS);
				box3.setAlignmentX(RIGHT_ALIGNMENT);
				box3.add(comp);box3.add(Box.createHorizontalGlue());
				layout.add(box3);
			add(layout);
			
			//Validation
			levelValidator = new LabelIndicValidator<JTextField>(null, null, warn) {
				@Override
				protected void registerToComponent(JTextField arg0) {
					levelField.getDocument().addDocumentListener(this);
					selectCategory.addChangeListener(this);
					selectAdvanced.addChangeListener(this);
				}
				@Override
				protected void unregisterFromComponent(JTextField arg0) {
					levelField.getDocument().removeDocumentListener(this);
					selectCategory.removeChangeListener(this);
					selectAdvanced.removeChangeListener(this);
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
				if(selectCategory.isSelected()) {
					return new CategoryFilter((Category)comboBox.getSelectedItem());
				}
				else {
					switch(comp.getSelection()) {
					case EQUALITY: return new CategoryFilter(comp.getSingleEntry(),Integer.parseInt(levelField.getText()));
					case REGEX: return new CategoryFilter(Pattern.compile(comp.getSingleEntry()),Integer.parseInt(levelField.getText()));
					case RANGE: return new CategoryFilter(comp.getMinEntry(),comp.getMaxEntry(),Integer.parseInt(levelField.getText()));
					default: return null;
					}
				}
			}
			else return null;
		}

		@Override
		public boolean hasValidContent() {
			return comp.validateFilter() != Result.INCORRECT && levelValidator.validate().getOverallResult() != Result.INCORRECT;
		}

		//updates the category list and changes the selection if necessary
		private void updateCategory() {
			//Change filter mode, if necessary
			if(comboBox.getModel().getSize() == 0) {
				selectAdvanced.setSelected(true);
				selectCategory.setEnabled(false);
				comboBox.setEnabled(false);
			}
			else {
				selectCategory.setEnabled(true);
				comboBox.setEnabled(true);
			}
			repaint();
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
