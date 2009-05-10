package fs.fibu2.data.model;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;

import org.dom4j.Document;

import fs.fibu2.data.event.JournalAdapter;
import fs.fibu2.data.event.JournalListener;
import fs.fibu2.data.format.DefaultStringComparator;
import fs.fibu2.filter.EntryFilter;
import fs.fibu2.filter.EntryFilterEditor;
import fs.fibu2.filter.StandardFilterComponent;
import fs.fibu2.filter.StandardFilterComponent.Selection;
import fs.fibu2.filter.event.StandardComponentListener;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.view.model.AccountListModel;
import fs.fibu2.view.render.AccountListRenderer;
import fs.validate.ValidationResult.Result;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;

/**
 * Filters entries according to their account field
 * @author Simon Hampe
 *
 */
public class AccountFilter implements EntryFilter {

	// FIELDS ************************
	// *******************************
	
	private Selection 			typeOfFilter;
	private Account			  	equalityAccount;
	
	private String				equalityString;
	private Pattern	  			regexFilter;
	private String				minFilter;
	private String				maxFilter;
	
	// CONSTRUCTORS ********************
	// *********************************
	
	/**
	 * Constructs a filter which selects "bank_account" as account filter
	 */
	public AccountFilter() {
		typeOfFilter = Selection.EQUALITY;
		equalityAccount = AccountLoader.getAccount("bank_account");
	}
	
	/**
	 * Only accepts entries in Account a 
	 */
	public AccountFilter(Account a) {
		typeOfFilter = Selection.EQUALITY;
		equalityAccount = (a == null)? AccountLoader.getAccount("bank_account") : a;
	}
	
	/**
	 * Only accepts entries, whose account NAME is equal to c
	 */
	public AccountFilter(String a) {
		typeOfFilter = Selection.EQUALITY;
		equalityString = a == null? "" : a;
	}
	
	/**
	 * Only accepts entries, whose account NAME matches pattern p 
	 */
	public AccountFilter(Pattern p) {
		typeOfFilter = Selection.REGEX;
		regexFilter = p == null? Pattern.compile("") : p;
	}
	
	/**
	 * Only accepts entries, whose account NAME is alphabetically between min and max 
	 */
	public AccountFilter(String min, String max) {
		typeOfFilter = Selection.RANGE;
		minFilter = min == null? "" : min;
		maxFilter = max == null? "" : max;
	}
	
	// FILTER METHODS ******************
	// *********************************
	
	@Override
	public String getDescription() {
		String name = Fsfibu2StringTableMgr.getString("fs.fibu2.Entry.account");
		switch(typeOfFilter) {
		case EQUALITY: Fsfibu2StringTableMgr.getString("fs.fibu2.filter.describeequals",name,equalityAccount == null? equalityString : equalityAccount.getName());
		case REGEX: return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.describematches",name,regexFilter.pattern());
		case RANGE: return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.describerange",name,minFilter,maxFilter);
		default: return "";
		}
	}

	@Override
	public EntryFilterEditor getEditor(Journal j) {
		return new AccountFilterEditor(j);
	}

	@Override
	public String getID() {
		return "ff2filter_account";
	}

	@Override
	public String getName() {
		return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.AccountFilter.name");
	}

	@Override
	public boolean verifyEntry(Entry e) {
		if(e ==  null) return false;
		switch(typeOfFilter) {
		case EQUALITY: 
			if(equalityAccount != null) return e.getAccount() == equalityAccount;
			else {
				return e.getAccount().getName().equals(equalityString);
			}
		case REGEX: Matcher m = regexFilter.matcher(e.getAccount().getName());
					return m.matches();
		case RANGE: Comparator<String> c = new DefaultStringComparator();
					String toCompare = e.getAccount().getName();
					return c.compare(minFilter, toCompare) <= 0 && c.compare(toCompare, maxFilter) <= 0;
		default: return false;
		}
	}

	// LOCAL CLASS FOR EDITOR ********************************
	// *******************************************************
	
	private class AccountFilterEditor extends EntryFilterEditor implements ResourceDependent {
		
		/**
		 * compiler-generated serial version uid
		 */
		private static final long serialVersionUID = 6124504299015696735L;

		private StandardFilterComponent comp;
		
		private JComboBox comboBox = new JComboBox();
		private JRadioButton selectAccount = new JRadioButton();
		private JRadioButton selectAdvanced = new JRadioButton();
			
		
		private JournalListener listener = new JournalAdapter() {
			@Override
			public void entriesAdded(Journal source, Entry[] newEntries) {
				updateAccount(source);
			}
			@Override
			public void entriesRemoved(Journal source, Entry[] oldEntries) {
				updateAccount(source);
			}
			@Override
			public void entryReplaced(Journal source, Entry oldEntry,
					Entry newEntry) {
				updateAccount(source);
			}
		};
		
		// CONSTRUCTOR ****************************
		// ****************************************
		
		public AccountFilterEditor(Journal j) {
			//Listen to journal
			if(j != null) j.addJournalListener(listener);
			
			//Init components
			updateAccount(j);
			
			comboBox.setRenderer(new AccountListRenderer());
			
			selectAccount.setText(Fsfibu2StringTableMgr.getString("fs.fibu2.Entry.account") + ": ");
			selectAdvanced.setText(Fsfibu2StringTableMgr.getString("fs.fibu2.filter.AccountFilter.advanced") + ":");
			ButtonGroup group = new ButtonGroup();
				group.add(selectAdvanced);
				group.add(selectAccount);
				
			if(typeOfFilter == Selection.EQUALITY) {
				if(equalityAccount != null && comboBox.getModel().getSize() > 0) { //We only filter for accounts, if at least one is used
					comboBox.setSelectedItem(equalityAccount);
					selectAccount.setSelected(true);
				}
				else {
					selectAdvanced.setSelected(true);
				}
			}
			
			String singleString = (typeOfFilter == Selection.EQUALITY && equalityAccount == null? equalityString : (typeOfFilter == Selection.REGEX? regexFilter.pattern() : ""));
			comp = new StandardFilterComponent(Fsfibu2StringTableMgr.getString("fs.fibu2.Entry.account") + ": ",null,new DefaultStringComparator(),
										singleString,
										typeOfFilter == Selection.RANGE? minFilter : "",
										typeOfFilter == Selection.RANGE? maxFilter : "",typeOfFilter);			
			
			//Layout
			Box layout = new Box(BoxLayout.Y_AXIS);
			Box hbox1 = new Box(BoxLayout.X_AXIS);
				hbox1.add(selectAccount); hbox1.add(comboBox);
			Box hbox2 = new Box(BoxLayout.X_AXIS);
				hbox2.add(selectAdvanced);
				hbox2.add(Box.createHorizontalGlue());
			layout.add(hbox1);
			layout.add(Box.createVerticalStrut(5));
			layout.add(hbox2);
			layout.add(Box.createVerticalStrut(5));
			layout.add(new JSeparator(JSeparator.HORIZONTAL));
			layout.add(Box.createVerticalStrut(5));
			Box box3 = new Box(BoxLayout.X_AXIS);
				box3.setAlignmentX(RIGHT_ALIGNMENT);
				box3.add(comp);box3.add(Box.createHorizontalGlue());
				layout.add(box3);
			add(layout);
			
			
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
				if(selectAccount.isSelected()) {
					return new AccountFilter((Account)comboBox.getSelectedItem());
				}
				else {
					switch(comp.getSelection()) {
					case EQUALITY: return new AccountFilter(comp.getSingleEntry());
					case REGEX: return new AccountFilter(Pattern.compile(comp.getSingleEntry()));
					case RANGE: return new AccountFilter(comp.getMinEntry(),comp.getMaxEntry());
					default: return null;
					}
				}
			}
			else return null;
		}

		@Override
		public boolean hasValidContent() {
			return comp.validateFilter() != Result.INCORRECT  || selectAccount.isSelected();
		}

		//updates the category list and changes the selection if necessary
		private void updateAccount(Journal j) {
			//Reload model
			comboBox.setModel(new AccountListModel(j));
			//Change filter mode, if necessary
			if(comboBox.getModel().getSize() == 0) {
				selectAdvanced.setSelected(true);
				selectAccount.setEnabled(false);
				comboBox.setEnabled(false);
			}
			else {
				selectAccount.setEnabled(true);
				comboBox.setEnabled(true);
			}
			repaint();
		}
		

		@Override
		public Document getExpectedResourceStructure() {
			XMLDirectoryTree tree = new XMLDirectoryTree();
			tree.addPath("graphics/share/warn.png");
			return tree;
		}

		@Override
		public void assignReference(ResourceReference r) {
			//Ignored
		}
		
	}
	
}