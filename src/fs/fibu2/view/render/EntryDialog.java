package fs.fibu2.view.render;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.dom4j.tree.DefaultEntity;

import fs.event.DataRetrievalListener;
import fs.fibu2.data.error.EntryVerificationException;
import fs.fibu2.data.format.DefaultCurrencyFormat;
import fs.fibu2.data.format.Fsfibu2DateFormats;
import fs.fibu2.data.model.Account;
import fs.fibu2.data.model.Category;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.model.AccountListModel;
import fs.fibu2.view.model.CategoryListModel;
import fs.gui.FrameworkDialog;
import fs.gui.GUIToolbox;
import fs.gui.SwitchIconLabel;
import fs.validate.LabelIndicValidator;
import fs.validate.ValidationResult;
import fs.validate.ValidationValidator;
import fs.validate.ValidationResult.Result;
import fs.xml.PolyglotStringTable;
import fs.xml.ResourceDependent;

/**
 * This class implements a dialog for editing / creating fsfibu2 journal {@link Entry}s. It notifies {@link DataRetrievalListener} with the resulting
 * Entry, if OK is clicked and null, if Cancel is clicked.
 * @author Simon Hampe
 *
 */
public class EntryDialog extends FrameworkDialog {

	private final static String sgroup = "fs.fibu2.view.EntryDialog";
	
	// COMPONENTS **************************
	// *************************************
	
	private SwitchIconLabel labelName = new SwitchIconLabel(Fsfibu2StringTableMgr.getString(sgroup + ".name"));
	private SwitchIconLabel labelDate = new SwitchIconLabel(Fsfibu2StringTableMgr.getString(sgroup + ".date"));
	private SwitchIconLabel labelValue = new SwitchIconLabel(Fsfibu2StringTableMgr.getString(sgroup + ".value"));
	private SwitchIconLabel labelCategory = new SwitchIconLabel(Fsfibu2StringTableMgr.getString(sgroup + ".category"));
	private SwitchIconLabel labelAccount = new SwitchIconLabel(Fsfibu2StringTableMgr.getString(sgroup + ".account"));
	private SwitchIconLabel labelAccInf = new SwitchIconLabel(Fsfibu2StringTableMgr.getString(sgroup + ".accinf"));
	
	private JLabel labelPreviewDate = new JLabel();
	private JLabel labelPreviewValue = new JLabel();
	private JLabel labelPreviewCreate = new JLabel();
	
	private JTextField fieldName = new JTextField();
	private JTextField fieldDate = new JTextField();
	private JTextField fieldValue = new JTextField();
	private JComboBox  comboCategory = new JComboBox();
	private JRadioButton radioExisting = new JRadioButton();
	private JRadioButton radioNew = new JRadioButton(Fsfibu2StringTableMgr.getString(sgroup + ".newcat"));
	private JRadioButton radioCreate = new JRadioButton();
	private JTextField fieldNewCategory = new JTextField();
	private JComboBox comboAccount = new JComboBox();
	private JPanel panelAccInf = new JPanel();
	private JCheckBox checkAdditional = new JCheckBox(Fsfibu2StringTableMgr.getString(sgroup + ".addinf"));
	private JTextArea areaInfo = new JTextArea();
	
	private JButton createButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".createcategory"));
	private JButton okButton = new JButton(Fsfibu2StringTableMgr.getString("fs.fibu2.global.ok"));
	private JButton cancelButton = new JButton(Fsfibu2StringTableMgr.getString("fs.fibu2.global.cancel"));
	
	private ImageIcon warn = new ImageIcon("graphics/share/warn.png");
	
	//A list of jtextfields/switchiconlabels for each information field of a given account
	private HashMap<String, JTextField> accountMap = new HashMap<String, JTextField>();
	private HashMap<String, SwitchIconLabel> labelMap = new HashMap<String, SwitchIconLabel>();
	
	// VALIDATORS ******************************
	// *****************************************
	
	private LabelIndicValidator<JTextField> nameValidator = new LabelIndicValidator<JTextField>(null, warn, warn) {
		@Override
		public Result validate(JTextField component) {
			Result r = Result.CORRECT;
			String tooltip = null;
			if(component.getText().trim().equals("")) {
				r = Result.INCORRECT;
				tooltip = Fsfibu2StringTableMgr.getString(sgroup + ".emptyname");
			}
			setToolTipText(component, tooltip);
			return r;
		}
		@Override
		protected void unregisterFromComponent(JTextField arg0) {
			arg0.getDocument().removeDocumentListener(this);
		}
		@Override
		protected void registerToComponent(JTextField arg0) {
			arg0.getDocument().addDocumentListener(this);
		}
	};
	
	private LabelIndicValidator<JTextField> dateValidator = new LabelIndicValidator<JTextField>(null, warn, warn) {
		@Override
		public Result validate(JTextField component) {
			Result r = Result.CORRECT;
			String tooltip = null;
			try {
				Fsfibu2DateFormats.getDateInputFormat().parse(component.getText());
			} catch (ParseException e) {
				r = Result.INCORRECT;
				tooltip = Fsfibu2StringTableMgr.getString(sgroup + ".wrongdate");
			}
			setToolTipText(component, tooltip);
			return r;
		}
		@Override
		protected void unregisterFromComponent(JTextField arg0) {
			arg0.getDocument().removeDocumentListener(this);
		}
		@Override
		protected void registerToComponent(JTextField arg0) {
			arg0.getDocument().addDocumentListener(this);
		}
	};
	
	private LabelIndicValidator<JTextField> valueValidator = new LabelIndicValidator<JTextField>(null, warn, warn) {
		@Override
		public Result validate(JTextField component) {
			Result r = Result.CORRECT;
			String tooltip = null;
			try {
				DefaultCurrencyFormat.getFormat().parse(component.getText());
			}
			catch(ParseException e) {
				r = Result.INCORRECT;
				tooltip = Fsfibu2StringTableMgr.getString(sgroup + ".wrongvalue");
			}
			setToolTipText(component, tooltip);
			return r;
		}
		@Override
		protected void unregisterFromComponent(JTextField arg0) {
			arg0.getDocument().removeDocumentListener(this);
		}
		@Override
		protected void registerToComponent(JTextField arg0) {
			arg0.getDocument().addDocumentListener(this);
		}
	};
	
	private LabelIndicValidator<JTextField> categoryValidator = new LabelIndicValidator<JTextField>(null, warn, warn) {
		@Override
		public Result validate(JTextField component) {
			Result r = Result.CORRECT;
			String tooltip = null;
			if(radioNew.isSelected() && component.getText().trim().equals("")) {
				r = Result.INCORRECT;
				tooltip = Fsfibu2StringTableMgr.getString(sgroup + ".emptycategory");
			}
			setToolTipText(component, tooltip);
			return r;
		}
		@Override
		protected void unregisterFromComponent(JTextField arg0) {
			arg0.getDocument().removeDocumentListener(this);
			radioNew.removeChangeListener(this);
		}
		@Override
		protected void registerToComponent(JTextField arg0) {
			arg0.getDocument().addDocumentListener(this);
			radioNew.addChangeListener(this);
		}
	};
	
	//Returns, whether the above validators all return correctly
	private ValidationValidator basicSummary = new ValidationValidator() {

		{
			addValidator(nameValidator); addValidator(dateValidator);
			addValidator(valueValidator); addValidator(categoryValidator);
		}
		
		@Override
		public void validationPerformed(ValidationResult result) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	//Validates the account information fields
	private LabelIndicValidator<JPanel> accInfValidator = new LabelIndicValidator<JPanel>(null, warn, warn) {
		@Override
		protected void registerToComponent(JPanel arg0) {
			//This is done elswhere
		}
		@Override
		protected void unregisterFromComponent(JPanel arg0) {
			//This is done elsewhere
		}
		@Override
		public Result validate(JPanel component) {
			Result r = Result.CORRECT;
			String tooltip = null;
			//If the basic values are incorrect, we dont validate
			if(basicSummary.validate().getOverallResult() != Result.INCORRECT) {
				//tooltip = Fsfibu2StringTableMgr.getString(sgroup + ".wrongaccinf");
				
			}
			setToolTipText(component, tooltip);
			return r;
		}
	};

	
	// DATA ********************************
	// *************************************
	
	//A copy of the newly created category (if one is created)
	private Category createdCategory;
	
	// CONSTRUCTOR *************************
	// *************************************
	
	/**
	 * Creates a new dialog.
	 * @param owner the frame owning this dialog
	 * @param j The journal in which this entry is edited or created
	 * @param e The entry to edit. If e == null, a new entry is created
	 */
	public EntryDialog(JFrame owner, Journal j, Entry e) {
		super(owner,Fsfibu2DefaultReference.getDefaultReference(),Fsfibu2StringTableMgr.getLoader(),PolyglotStringTable.getGlobalLanguageID());
		setTitle(Fsfibu2StringTableMgr.getString(e == null? sgroup + ".titlecreate" : sgroup + ".titleedit"));
		
		//Init components
		for(SwitchIconLabel l : Arrays.asList(labelName, labelDate, labelValue, labelCategory,labelAccount, labelAccInf)) {
			l.setIconReference(new ImageIcon("graphics/share/warn.png"));
			l.setHorizontalTextPosition(SwingConstants.LEFT);
		}
		comboCategory.setModel(new CategoryListModel(j,true));
		comboCategory.setRenderer(new CategoryListRenderer(" > "));
		comboAccount.setModel(new AccountListModel(j));
		comboAccount.setRenderer(new AccountListRenderer());
		ButtonGroup group = new ButtonGroup();
			group.add(radioExisting); group.add(radioNew); group.add(radioCreate);
			radioExisting.setSelected(true);
		checkAdditional.setSelected(e == null? false : (e.getAdditionalInformation().trim().equals("")? false : true));
		areaInfo.setEnabled(checkAdditional.isSelected());
		areaInfo.setBorder(BorderFactory.createEtchedBorder());
		panelAccInf.setBorder(BorderFactory.createEtchedBorder());
		
		//Layout
		
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		GridBagConstraints gcLabelName = GUIToolbox.buildConstraints(0, 0, 1, 1);
		GridBagConstraints gcLabelDate = GUIToolbox.buildConstraints(0, 1, 1, 1);
		GridBagConstraints gcLabelValue = GUIToolbox.buildConstraints(0, 2, 1, 1);
		GridBagConstraints gcLabelCategory = GUIToolbox.buildConstraints(0, 3, 1, 1);
		GridBagConstraints gcLabelAccount = GUIToolbox.buildConstraints(0, 5, 1, 1);
		GridBagConstraints gcFieldName = GUIToolbox.buildConstraints(1, 0, 2, 1);
		GridBagConstraints gcFieldDate = GUIToolbox.buildConstraints(1, 1, 2, 1);
		GridBagConstraints gcFieldValue = GUIToolbox.buildConstraints(1, 2, 2, 1);
		GridBagConstraints gcRadioEx = GUIToolbox.buildConstraints(1,3, 1, 1);
		GridBagConstraints gcComboCat = GUIToolbox.buildConstraints(2, 3, 1, 1);
		GridBagConstraints gcRadioCreate = GUIToolbox.buildConstraints(1, 4, 1, 1);
		GridBagConstraints gcCreate = GUIToolbox.buildConstraints(2, 4, 1, 1);
		GridBagConstraints gcComboAccount = GUIToolbox.buildConstraints(1, 5, 2, 1);
		GridBagConstraints gcPreviewDate = GUIToolbox.buildConstraints(3, 1, 2, 1);
		GridBagConstraints gcPreviewValue = GUIToolbox.buildConstraints(3, 2, 2, 1);
		GridBagConstraints gcRadioNew = GUIToolbox.buildConstraints(3, 3, 1, 1);
		GridBagConstraints gcFieldNew = GUIToolbox.buildConstraints(4, 3, 1, 1);
		GridBagConstraints gcPreviewCreate = GUIToolbox.buildConstraints(3, 4, 2, 1);
		GridBagConstraints gcLabelAccInf = GUIToolbox.buildConstraints(0, 6, 5, 1);
		GridBagConstraints gcPanelAcc = GUIToolbox.buildConstraints(0, 7, 5, 1);
		GridBagConstraints gcCheckAdd = GUIToolbox.buildConstraints(0, 8, 5, 1);
		GridBagConstraints gcAreaAdd = GUIToolbox.buildConstraints(0, 9, 5, 1);
		GridBagConstraints gcOk = GUIToolbox.buildConstraints(3, 10, 1, 1);
		GridBagConstraints gcCancel = GUIToolbox.buildConstraints(4, 10, 1, 1);
		
		for(GridBagConstraints gc : Arrays.asList(gcLabelName,gcLabelDate, gcLabelValue, gcLabelCategory, gcLabelAccount, gcLabelAccInf,
										gcFieldName, gcFieldNew, gcFieldDate, gcFieldValue, gcRadioEx, gcRadioNew,gcRadioCreate,gcCreate,
										gcComboCat, gcComboAccount, gcPanelAcc, gcPreviewDate, gcPreviewValue,gcPreviewCreate, gcCheckAdd,
										gcAreaAdd,gcOk,gcCancel)) {
			gc.insets = new Insets(5,5,5,5);
		}
		gcLabelName.insets = new Insets(10,5,5,5);
		gcFieldName.insets = new Insets(10,5,5,5);
		gcOk.insets = new Insets(5,5,10,5);
		gcCancel.insets = new Insets(5,5,10,5);
		gcAreaAdd.ipady = 50;
		gcFieldNew.ipadx = 150;
		
		gbl.setConstraints(labelName, gcLabelName);
		gbl.setConstraints(labelDate, gcLabelDate);
		gbl.setConstraints(labelValue, gcLabelValue);
		gbl.setConstraints(labelCategory, gcLabelCategory);
		gbl.setConstraints(labelAccount, gcLabelAccount);
		gbl.setConstraints(fieldName, gcFieldName);
		gbl.setConstraints(fieldDate, gcFieldDate);
		gbl.setConstraints(fieldValue, gcFieldValue);
		gbl.setConstraints(radioExisting, gcRadioEx);
		gbl.setConstraints(comboCategory, gcComboCat);
		gbl.setConstraints(radioCreate, gcRadioCreate);
		gbl.setConstraints(createButton, gcCreate);
		gbl.setConstraints(comboAccount, gcComboAccount);
		gbl.setConstraints(labelPreviewDate, gcPreviewDate);
		gbl.setConstraints(labelPreviewValue, gcPreviewValue);
		gbl.setConstraints(radioNew, gcRadioNew);
		gbl.setConstraints(fieldNewCategory, gcFieldNew);
		gbl.setConstraints(labelPreviewCreate, gcPreviewCreate);
		gbl.setConstraints(labelAccInf, gcLabelAccInf);
		gbl.setConstraints(panelAccInf, gcPanelAcc);
		gbl.setConstraints(checkAdditional, gcCheckAdd);
		gbl.setConstraints(areaInfo, gcAreaAdd);
		gbl.setConstraints(okButton, gcOk);
		gbl.setConstraints(cancelButton, gcCancel);
	
		for(JLabel l : Arrays.asList(labelName,labelDate, labelValue, labelCategory,labelAccount, labelAccInf, labelPreviewCreate, labelPreviewDate, 
				labelPreviewValue)) add(l);
		add(fieldName); add(fieldDate); add(fieldValue); add(radioExisting); add(comboCategory); 
		add(radioNew); add(fieldNewCategory); add(radioCreate);add(createButton); add(comboAccount);
		add(panelAccInf);
		add(checkAdditional);
		add(areaInfo);
		add(okButton); add(cancelButton);
		if(e == null) replaceAccountInformationPanel((Account)comboAccount.getSelectedItem(), null);
		else insertValues(e);
		pack();
		setResizable(false);
	}

	// HELPER METHODS ****************************************
	// *******************************************************
	
	/**
	 * If e != null, inserts the values of e into the dialog.
	 */
	private void insertValues(Entry e) {
		if(e == null) return;
		fieldName.setText(e.getName());
		fieldDate.setText(Fsfibu2DateFormats.getEntryDateFormat().format(e.getDate().getTime()));
		fieldValue.setText(DefaultCurrencyFormat.getFormat().format(e.getValue()));
		if(((CategoryListModel)comboCategory.getModel()).getListOfItems().contains(e.getCategory())) {
			comboCategory.setSelectedItem(e.getCategory());
			radioExisting.setSelected(true);
		}
		else {
			setCreatedCategory(e.getCategory());
			radioCreate.setSelected(true);
		}
		if(((AccountListModel)comboAccount.getModel()).getListOfItems().contains(e.getAccount())) {
			comboAccount.setSelectedItem(e.getAccount());
		}
		replaceAccountInformationPanel(e.getAccount(), e);
		checkAdditional.setSelected(!e.getAdditionalInformation().trim().equals(""));
		if(checkAdditional.isSelected()) areaInfo.setText(e.getAdditionalInformation());
		updatePreviews();		
	}
	
	/**
	 * Returns a panel containg all necessary entry possibilites for the selected account
	 */
	private void replaceAccountInformationPanel(Account a, Entry e) {
		panelAccInf.removeAll();
		
	}
	
	/**
	 * Sets the created category and updates the corresponding preview label. Does not change radio button selection
	 */
	private void setCreatedCategory(Category c) {
		createdCategory = c;
		if(c == null) labelPreviewCreate.setText("");
		else labelPreviewCreate.setText(c.toString());
	}
	
	/**
	 * Updates the preview labels for value and date
	 */
	private void updatePreviews() {
		try {
			String s = DefaultCurrencyFormat.getFormat(Currency.getInstance("EUR")).format(DefaultCurrencyFormat.getFormat().parse(fieldValue.getText()));
			labelPreviewValue.setText("= '" + s + "'");
		} catch (ParseException e) {
			labelPreviewValue.setText(sgroup + ".previnvalid");
		}
		try {
			String t = Fsfibu2DateFormats.getEntryDateFormat().format(Fsfibu2DateFormats.getDateInputFormat().parse(fieldDate.getText()));
			labelPreviewDate.setText("= '" + t + "'");
		} catch (ParseException e) {
			labelPreviewDate.setText(sgroup + ".previnvalid");
		}
	}
	
	/**
	 * @return The entry created from the given values. Returns null, if any basic value is invalid but is not concerned with validity of account
	 * information fields
	 */
	private Entry createEntry() {
		if(basicSummary.validate().getOverallResult() == Result.INCORRECT) return null;
		HashMap<String, String> accMap = new HashMap<String, String>();
			for(String k : accountMap.keySet()) {
				accMap.put(k, accountMap.get(k).getText());
			}
		try {
			return new Entry(fieldName.getText(),
							DefaultCurrencyFormat.getFormat().parse(fieldValue.getText()).floatValue(),Currency.getInstance("EUR"),Fsfibu2DateFormats.parseDateInputFormat(fieldDate.getText()),
							radioExisting.isSelected()? (Category)comboCategory.getSelectedItem() :
								(radioNew.isSelected()? Category.getCategory((Category)comboCategory.getSelectedItem(),fieldNewCategory.getText()) :
														createdCategory),((Account)comboAccount.getSelectedItem()).getID(),accMap,
								checkAdditional.isSelected()? areaInfo.getText() : null);
		} catch (Exception e) {
			//Will not happen, since we've already validated
			return null;
		}
	}
	
}

