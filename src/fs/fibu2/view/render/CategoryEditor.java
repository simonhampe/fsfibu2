package fs.fibu2.view.render;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import fs.event.DataRetrievalListener;
import fs.fibu2.data.model.Category;
import fs.fibu2.data.model.Journal;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.model.CategoryListModel;
import fs.gui.FrameworkDialog;
import fs.gui.GUIToolbox;
import fs.gui.SwitchIconLabel;
import fs.validate.LabelIndicValidator;
import fs.validate.SingleButtonValidator;
import fs.validate.ValidationResult.Result;
import fs.xml.PolyglotStringTable;

/**
 * This class implements a short dialog for creating a new category
 * @author Simon Hampe
 *
 */
public class CategoryEditor extends FrameworkDialog {
	
	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = 8153720307396007569L;

	private final static String sgroup = "fs.fibu2.view.CategoryEditor";
	
	// COMPONENTS ***************************
	// **************************************
	
	private JComboBox comboCategory = new JComboBox();
	private JButton addButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".add"));
	
	private JPanel panelSub = new JPanel();
	
	private JButton okButton = new JButton(Fsfibu2StringTableMgr.getString("fs.fibu2.global.ok"));
	private JButton cancelButton = new JButton(Fsfibu2StringTableMgr.getString("fs.fibu2.global.cancel"));
	
	private ImageIcon warn = new ImageIcon(Fsfibu2DefaultReference.getDefaultReference().getFullResourcePath(this,"graphics/share/warn.png"));
	
	// DATA *********************************
	// **************************************
	
	//A list of all subcategories currently edited
	private Vector<String> subCategories = new Vector<String>();
	
	//A list of text fields for entering subcategories and their validators
	private Vector<JTextField> subFields = new Vector<JTextField>();
	private Vector<LabelIndicValidator<JTextField>> subValidators = new Vector<LabelIndicValidator<JTextField>>();
	
	// LISTENERS ****************************
	// **************************************
	
	private ActionListener addListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			subCategories.add("");
			updatePanel();
			summary.validate();
		}
	};
	
	private ActionListener closeButtonListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			exit(e.getSource() == okButton);
		}
	};
	
	private WindowListener closeListener = new WindowAdapter() {
		@Override
		public void windowClosing(WindowEvent e) {
			exit(false);
		}
	};
	
	// VALIDATORS ***************************
	// **************************************
	
	private SingleButtonValidator summary = new SingleButtonValidator(okButton);
	
	// CONSTRUCTOR **************************
	// **************************************
	
	/**
	 * Creates a new dialog
	 * @param owner The parent dialog
	 * @param categoryBase The journal from which to construct the list of existing categories
	 */
	public CategoryEditor(JDialog owner, Journal categoryBase) {
		super(owner,Fsfibu2DefaultReference.getDefaultReference(),Fsfibu2StringTableMgr.getLoader(),PolyglotStringTable.getGlobalLanguageID());
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setTitle(Fsfibu2StringTableMgr.getString(sgroup + ".title"));
		
		//Init components
		comboCategory.setModel(new CategoryListModel(categoryBase,true));
		comboCategory.setRenderer(new CategoryListRenderer(" > "));
		
		JLabel labelCombo = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".basecategory"));
		JScrollPane panePanel = new JScrollPane(panelSub);
		
		//Layout
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		
		GridBagConstraints gcLabel = GUIToolbox.buildConstraints(0, 0, 1, 1);
		GridBagConstraints gcCombo = GUIToolbox.buildConstraints(1, 0, 1, 1);
		GridBagConstraints gcOk = GUIToolbox.buildConstraints(2, 0, 1, 1);
		GridBagConstraints gcAdd = GUIToolbox.buildConstraints(0, 1, 1, 1);
		GridBagConstraints gcCancel = GUIToolbox.buildConstraints(2, 1, 1, 1);
		GridBagConstraints gcScroll = GUIToolbox.buildConstraints(0, 2, 2, 1);
		for(GridBagConstraints c : Arrays.asList(gcLabel,gcCombo,gcOk)) c.insets = new Insets(10,5,5,5);
		gcAdd.insets = new Insets(5,5,5,5);
		gcCancel.insets = new Insets(5,5,5,5);
		gcScroll.insets = new Insets(5,5,10,5);
		
		gbl.setConstraints(labelCombo, gcLabel);
		gbl.setConstraints(comboCategory,gcCombo);
		gbl.setConstraints(okButton, gcOk);
		gbl.setConstraints(addButton, gcAdd);
		gbl.setConstraints(cancelButton, gcCancel);
		gbl.setConstraints(panePanel, gcScroll);
		
		add(labelCombo); add(comboCategory); add(addButton); add(panePanel); add(okButton); add(cancelButton);
		pack();
		setResizable(false);
		
		//Register listeners
		addButton.addActionListener(addListener);
		okButton.addActionListener(closeButtonListener);
		cancelButton.addActionListener(closeButtonListener);
		addWindowListener(closeListener);
	}
	
	/**
	 * Layouts the subcategories - panel according to the vector subCategories
	 */
	private void updatePanel() { 
		panelSub.removeAll();
		GridBagLayout gbl = new GridBagLayout();
		panelSub.setLayout(gbl);
		//Reset validator and lists
		for(LabelIndicValidator<JTextField> v : subValidators) summary.removeValidator(v);
		subValidators = new Vector<LabelIndicValidator<JTextField>>();
		subFields = new Vector<JTextField>();
		//Init new components
		int row = 0;
		for(String sc : subCategories) {
			SwitchIconLabel label = new SwitchIconLabel(warn);
			final JTextField field = new JTextField(sc);
			JButton button = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".delete"));
			GridBagConstraints gcLabel = GUIToolbox.buildConstraints(0, row, 1, 1); gcLabel.insets = new Insets(5,5,5,5);
			GridBagConstraints gcField = GUIToolbox.buildConstraints(1, row, 1, 1); gcField.insets = new Insets(5,5,5,5); gcField.weightx = 100;
			GridBagConstraints gcButton = GUIToolbox.buildConstraints(2, row, 1, 1); gcButton.insets = new Insets(5,5,5,5);
			gbl.setConstraints(label, gcLabel);
			gbl.setConstraints(field, gcField);
			gbl.setConstraints(button, gcButton);
			panelSub.add(label); panelSub.add(field);panelSub.add(button);
			//Add listeners
			field.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void changedUpdate(DocumentEvent e) {
					updateList(subFields.indexOf(field), field.getText());
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					updateList(subFields.indexOf(field), field.getText());
				}
				@Override
				public void removeUpdate(DocumentEvent e) {
					updateList(subFields.indexOf(field), field.getText());
				}
				public void updateList(int i, String s) {
					if(0 <= i && i < subCategories.size()) subCategories.set(i, s);
				}
			});
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					subCategories.remove(subFields.indexOf(field));
					updatePanel();
					summary.validate();
				}
			});
			//Init new Validator
			LabelIndicValidator<JTextField> validator = new LabelIndicValidator<JTextField>(null, null, warn) {
				@Override
				protected void registerToComponent(JTextField arg0) {
					field.getDocument().addDocumentListener(this);
				}
				@Override
				protected void unregisterFromComponent(JTextField arg0) {
					field.getDocument().removeDocumentListener(this);
				}
				@Override
				public Result validate(JTextField component) {
					Result r = Result.CORRECT;
					String tooltip  = null;
					if(component.getText().trim().equals("")) {
						r = Result.INCORRECT;
						tooltip = Fsfibu2StringTableMgr.getString(sgroup + ".emptycategory");
					}
					setToolTipText(component, tooltip);
					return r;
				}
			};
			validator.addComponent(field, label);
			summary.addValidator(validator);
			subValidators.add(validator);
			subFields.add(field);
			row++;
		}
		pack();
		repaint();
	}
	
	/**
	 * @return The resulting category. Null, if the input is invalid
	 */
	private Category createCategory() {
		if(summary.validate().getOverallResult() == Result.INCORRECT) return null;
		Category c = (Category)comboCategory.getSelectedItem();
		for(String sub : subCategories) {
			c = Category.getCategory(c, sub);
		}
		return c;
	}
	
	/**
	 * Notifies all {@link DataRetrievalListener}s: With null, if returnvalue ==  false, with the resulting
	 * category otherwise
	 */
	private void exit(boolean returnvalue) {
		Category c = null;
		if(returnvalue) {
			c = createCategory();
		}
		for(DataRetrievalListener l : listeners) {
			l.dataReady(this, c);
		}
	}
	
}
