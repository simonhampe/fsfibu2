package fs.fibu2.filter;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.dom4j.Document;

import fs.fibu2.data.format.DefaultFloatComparator;
import fs.fibu2.data.format.DefaultStringComparator;
import fs.fibu2.data.model.AccountLoader;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.filter.StandardFilterComponent.Selection;
import fs.fibu2.filter.event.StandardComponentListener;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.model.AccountInformation;
import fs.fibu2.view.model.AccountInformationListModel;
import fs.fibu2.view.render.AccountInformationListRenderer;
import fs.gui.SwitchIconLabel;
import fs.validate.LabelIndicValidator;
import fs.validate.ValidationResult.Result;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;

/**
 * Filters entries according to the information supplied for an account. As for the range filter, the given values can optionally be interpreted as
 * numerical values
 * @author Simon Hampe
 *
 */
public class AccountInformationFilter implements EntryFilter {

	private AccountInformation information; //TODO: COntains a list of accounts using it. What if this changes?
	
	private Selection 	typeOfFilter;
	private String	   	equalityString;
	private Pattern		regexFilter;
	
	private String		minFilter;
	private String 		maxFilter;
	
	private String		minFloatFilter;
	private String		maxFloatFilter;
	
	private boolean 	numericalRangeFilter;
	
	private static NumberFormat format = NumberFormat.getInstance();
	
	// CONSTRUCTORS ****************************
	// *****************************************
	
	/**
	 * Constructs a filter which looks for entries with empty 'invoice' field
	 */
	public AccountInformationFilter() {
		typeOfFilter = Selection.EQUALITY;
		information = new AccountInformation("invoice",AccountLoader.getAccount("bank_account").getFieldNames().get("invoice"),null);
		equalityString = "";
	}
	
	/**
	 * Constructs a filter which only admits entries, whose field corresponding to info has a value equal to equalityString
	 */
	public AccountInformationFilter(AccountInformation info, String equalityString) {
		this();
		if(info != null) {
			information = info;
			this.equalityString = equalityString == null? "" : equalityString;
		}
	}
	
	/**
	 * Constructs a filter which only admits entries, whose field corresponding to info matches the given pattern
	 */
	public AccountInformationFilter(AccountInformation info, Pattern p) {
		this();
		if(info!= null) {
			information = info;
			typeOfFilter = Selection.REGEX;
			regexFilter = p == null? Pattern.compile("") : p;
		}
	}
	
	/**
	 * Constructs a filter which only admits entries, whose field corresponding to info is alphabetically between min and max
	 */
	public AccountInformationFilter(AccountInformation info, String min, String max ) {
		this();
		if(info != null) {
			information = info;
			typeOfFilter = Selection.RANGE;
			minFilter = min == null? "": min;
			maxFilter = max == null? "": max;
			numericalRangeFilter = false;
		}
	}
	
	/**
	 * Constructs a filter which only admits entries, whose field corresponding to info contains a numerical value and is between min and max
	 */
	public AccountInformationFilter(AccountInformation info, Float min, Float max) {
		this();
		if(info != null) {
			information = info;
			typeOfFilter = Selection.RANGE;
			minFloatFilter = min == null? null : format.format(min);
			maxFloatFilter = max == null? null : format.format(max);
			numericalRangeFilter = true;
		}
	}
	
	// FILTER METHODS **************************
	// *****************************************
	
	@Override
	public String getDescription() {
		String name = information.getName();
		switch(typeOfFilter) {
		case EQUALITY: return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.describeequals",name,equalityString);
		case REGEX: return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.describematches",name,regexFilter.pattern());
		case RANGE: return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.describerange",name,numericalRangeFilter? minFloatFilter : minFilter,
															numericalRangeFilter? maxFloatFilter : maxFilter);
		default: return "";
		}
	}

	@Override
	public EntryFilterEditor getEditor(Journal j) {
		return new AccountInformationEditor(j);
	}

	@Override
	public String getID() {
		return "ff2filter_accountinformation";
	}

	@Override
	public String getName() {
		return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.AccountInformationFilter.name");
	}

	/**
	 * @return true, if and only if e has an account information field for the given id AND name which matches the criteria.
	 */
	@Override
	public boolean verifyEntry(Entry e) {
		if(e == null) return false;
		String entryInfo = e.getAccountInformation().get(information.getId()); 
		if( entryInfo == null) return false;
		if(!information.getName().equals(e.getAccount().getFieldNames().get(information.getId()))) return false;
		switch(typeOfFilter) {
		case EQUALITY: return equalityString.equals(entryInfo);
		case REGEX: Matcher m = regexFilter.matcher(entryInfo);
					return m.matches();
		case RANGE: if(!numericalRangeFilter) {
						DefaultStringComparator comp = new DefaultStringComparator();
						return comp.compare(minFilter, entryInfo) <= 0 && comp.compare(entryInfo, maxFilter) <= 0;
					}
					else {
						try {
							format.parse(entryInfo).floatValue();
							DefaultFloatComparator comp = new DefaultFloatComparator(format);
							return  comp.compare(minFloatFilter, entryInfo) <= 0 && comp.compare(entryInfo, maxFloatFilter) <= 0;
						}
						catch(ParseException pe) {
							return false; //If the info is not parseable, the entry is not valid
						}
					}			
					
		default: return false;
		}
	}
	
//	@Override
//	public EntryFilter createMeFromPreferences(Preferences filterNode)
//			throws IllegalArgumentException {
//		if(filterNode == null) throw new NullPointerException("Cannot read preferences from null node");
//		Selection type = AbstractFilterPreferences.getType(filterNode);
//		if(type == null) throw new IllegalArgumentException("Invalid node: No type entry");
//		try {
//			if(!filterNode.nodeExists("accountinformation")) throw new IllegalArgumentException("Invalid node: No account information node");
//		} catch (BackingStoreException e) {
//			throw new IllegalArgumentException(e);
//		}
//		
//		String id = filterNode.node("accountinformation").get("id", null);
//		String name = filterNode.node("accountinformation").get("name", null);
//		if(id == null || name == null) throw new IllegalArgumentException("Invalid node: Name and/or id for account information field missing");
//		
//		AccountInformation info = new AccountInformation(id,name,null);
//		
//		switch(type) {
//		case EQUALITY: return new AccountInformationFilter(info,AbstractFilterPreferences.getEqualityString(filterNode));
//		case REGEX: return new AccountInformationFilter(info, Pattern.compile(AbstractFilterPreferences.getPatternString(filterNode)));
//		case RANGE: return new AccountInformationFilter(info, AbstractFilterPreferences.getMinString(filterNode),
//				AbstractFilterPreferences.getMaxString(filterNode));
//		default: return new AccountInformationFilter();
//		}
//	}
//
//	@Override
//	public void insertMyPreferences(Preferences node) throws NullPointerException{
//		if(node == null) throw new NullPointerException("Cannot insert preferences in null node");
//		Preferences fnode = node.node("filter");
//		AbstractFilterPreferences.insert(fnode, typeOfFilter, getID(),equalityString,regexFilter != null? regexFilter.pattern() : null,
//				numericalRangeFilter? minFloatFilter.toString() : minFilter,
//				numericalRangeFilter? maxFloatFilter.toString() : maxFilter);
//		Preferences accinfNode = fnode.node("accountinformation");
//			accinfNode.put("id", information.getId());
//			accinfNode.put("name", information.getName());
//	}
	
	// LOCAL CLASS FOR EDITOR ******************************
	// *****************************************************
	
	private class AccountInformationEditor extends EntryFilterEditor implements ResourceDependent{

		/**
		 * compiler-generated serial version uid
		 */
		private static final long serialVersionUID = -7578243079819018105L;

		private StandardFilterComponent comp;
		
		private JComboBox comboBox = new JComboBox();
		private JLabel	  comboLabel = new JLabel();
		
		private JCheckBox 		numericBox = new JCheckBox();
		private SwitchIconLabel numericLabel = new SwitchIconLabel();
		
		private ImageIcon warn = new ImageIcon(Fsfibu2DefaultReference.getDefaultReference().getFullResourcePath(this, "graphics/share/warn.png"));
		
		private LabelIndicValidator<JCheckBox> validator;
		
		private Journal associatedJournal;
		
		// CONSTRUCTOR ************************************
		// ************************************************
		
		public AccountInformationEditor(Journal j) {
			associatedJournal = j == null? new Journal() : j;
			
			//Init components
			
			comboBox.setModel(new AccountInformationListModel(associatedJournal));
			comboBox.setRenderer(new AccountInformationListRenderer());
			comboLabel.setText(Fsfibu2StringTableMgr.getString("fs.fibu2.filter.AccountInformationFilter.field") + ": ");
			
			numericBox.setSelected(numericalRangeFilter);
			numericLabel.setText(Fsfibu2StringTableMgr.getString("fs.fibu2.filter.AccountInformationFilter.numericrange"));
			numericLabel.setHorizontalTextPosition(JLabel.LEFT);
			numericLabel.setIconReference(warn);
		
			comp = new StandardFilterComponent(Fsfibu2StringTableMgr.getString("fs.fibu2.filter.AccountInformationFilter.field") + ": ",null,new DefaultStringComparator(),
									typeOfFilter == Selection.EQUALITY? equalityString : (typeOfFilter == Selection.REGEX? regexFilter.pattern() : ""),
									typeOfFilter == Selection.RANGE? (numericalRangeFilter? minFloatFilter : minFilter): "",
									typeOfFilter == Selection.RANGE? (numericalRangeFilter? maxFloatFilter : maxFilter): "",typeOfFilter);
			
			//Layout
			
			Box layout = new Box(BoxLayout.Y_AXIS);
				Box hbox1 = new Box(BoxLayout.X_AXIS);
				hbox1.add(comboLabel); hbox1.add(comboBox);
			layout.add(hbox1);
			layout.add(Box.createVerticalStrut(5));
				Box hbox2 = new Box(BoxLayout.X_AXIS);
				hbox2.add(numericBox); hbox2.add(numericLabel);
			layout.add(hbox2);
			layout.add(Box.createVerticalStrut(5));
			layout.add(comp);
			add(layout);
			
			//Validation
			//This validator issues a warning, if numerical range filtering is turned on but min and max are not valid numerical values
			validator = new LabelIndicValidator<JCheckBox> (null, warn, warn) {
			
				private StandardComponentListener listener = new StandardComponentListener() {
					@Override
					public void contentChanged(StandardFilterComponent source) {
						fireStateChanged(new ChangeEvent(source));
					}
					@Override
					public void selectionChanged(
							StandardFilterComponent source,
							Selection newSelection) {
						fireStateChanged(new ChangeEvent(source));
					}
				};
				
				@Override
				public Result validate(JCheckBox component) {
					if(!numericBox.isSelected()) return Result.CORRECT;
					setToolTipText(component, null);
					try {
						format.parse(comp.getMinEntry());
						format.parse(comp.getMaxEntry());
					}
					catch(ParseException pe) {
						setToolTipText(component,Fsfibu2StringTableMgr.getString("fs.fibu2.filter.AccountInformationFilter.numericalerror"));
						return Result.WARNING;
					}
					catch(NullPointerException e) {
						//Ignore, since null values ARE valid
					}
					DefaultFloatComparator c = new DefaultFloatComparator(format);
					if(c.compare(comp.getMinEntry(), comp.getMaxEntry()) > 0) {
						setToolTipText(component, Fsfibu2StringTableMgr.getString("fs.fibu2.filter.AccountInformationFilter.rangeerror"));
						return Result.WARNING;
					}
					
					return Result.CORRECT;
				}
			
				@Override
				protected void unregisterFromComponent(JCheckBox arg0) {
					numericBox.removeChangeListener(this);
					comp.removeStandardComponentListener(listener);
				}
			
				@Override
				protected void registerToComponent(JCheckBox arg0) {
					numericBox.addChangeListener(this);
					comp.addStandardComponentListener(listener);
				}
			};
			validator.addComponent(numericBox, numericLabel);
			validator.validate();
			
			//Notification
			comp.addStandardComponentListener(new StandardComponentListener() {
				@Override
				public void contentChanged(StandardFilterComponent source) {fireStateChanged();}
				@Override
				public void selectionChanged(StandardFilterComponent source,
						Selection newSelection) {fireStateChanged();}
			});
			numericBox.addChangeListener(new ChangeListener(){
				@Override
				public void stateChanged(ChangeEvent e) { fireStateChanged();}
			});
			comboBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {fireStateChanged();}
			});
		}
		
		// EDITOR METHODS *********************************
		// ************************************************
		
		@Override
		public EntryFilter getFilter() {
			if(!hasValidContent()) return null;
			
			return null; //TODO: Do
		}

		@Override
		public boolean hasValidContent() {
			// TODO Auto-generated method stub
			return false;
		}

		// RESOURCEDEPENDENT *****************************
		// ***********************************************
		
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
