package fs.fibu2.view.render;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dom4j.Document;

import fs.fibu2.data.format.Fsfibu2DateFormats;
import fs.fibu2.data.model.Journal;
import fs.fibu2.data.model.ReadingPoint;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.model.ReadingPointModel;
import fs.gui.FrameworkDialog;
import fs.gui.GUIToolbox;
import fs.gui.SwitchIconLabel;
import fs.validate.LabelIndicValidator;
import fs.validate.SingleButtonValidator;
import fs.validate.ValidationResult.Result;
import fs.xml.PolyglotStringTable;
import fs.xml.XMLDirectoryTree;

/**
 * Implements a dialog for editing {@link ReadingPoint}s. There is at most one instance of this class for each exisiting {@link Journal}.
 * @author Simon Hampe
 *
 */
public class ReadingPointDialog extends FrameworkDialog {

	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = 8361938745150579880L;

	private static HashMap<Journal, ReadingPointDialog> dialogMap = new HashMap<Journal, ReadingPointDialog>();
	
	private Journal associatedJournal;
	
	private final static String sgroup = "fs.fibu2.view.ReadingPointDialog";
	
	private ReadingPoint currentlyEdited = null;
	
	// COMPONENTS ************************
	// ***********************************
	
	private JList pointList = new JList();
	private JButton newButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".new"));
	private JButton editButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".edit"));
	private JButton deleteButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".delete"));
	private JButton okButton = new JButton(Fsfibu2StringTableMgr.getString("fs.fibu2.global.ok"));
	private JButton cancelButton = new JButton(Fsfibu2StringTableMgr.getString("fs.fibu2.global.cancel"));
	private JButton closeButton = new JButton(Fsfibu2StringTableMgr.getString("fs.fibu2.global.close"));
	
	private JPanel editPanel = new JPanel();
	private JTextField nameField = new JTextField();
	private JTextField dateField = new JTextField();
	
	private ImageIcon warnIcon = new ImageIcon(resource.getFullResourcePath(this, "graphics/share/warn.png"));
	
	// LISTENERS *************************
	// ***********************************
	
	private SingleButtonValidator okValidator = new SingleButtonValidator(okButton);
	
	private ActionListener newListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			setCurrentlyEdited(null);
			editPanel.setVisible(true);
		}
	};
	
	private ActionListener editListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			setCurrentlyEdited((ReadingPoint)pointList.getSelectedValue());
			editPanel.setVisible(true);
		}
	};
	
	private ActionListener deleteListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			associatedJournal.removeReadingPointUndoable((ReadingPoint)pointList.getSelectedValue());
		}
	};
	
	private ListSelectionListener selectionListener = new ListSelectionListener() {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			int[] sels = pointList.getSelectedIndices();
			if(sels.length == 0) {
				editButton.setEnabled(false);
				deleteButton.setEnabled(false);
			}
			else {
				editButton.setEnabled(true);
				deleteButton.setEnabled(true);
			}
		}
	};
	
	private ActionListener closeListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			dispose();
		}
	};
	
	private ActionListener cancelListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			editPanel.setVisible(false);
		}
	};
	
	private ActionListener okListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if(currentlyEdited == null) {
				try {
					associatedJournal.addReadingPointUndoable(new ReadingPoint(nameField.getText(),Fsfibu2DateFormats.parseDateInputFormat(dateField.getText())));
				} catch (ParseException e1) {
					//Will not happen
				}
			}
			else {
				currentlyEdited.setName(nameField.getText());
				try {
					currentlyEdited.setReadingDay(Fsfibu2DateFormats.parseDateInputFormat(dateField.getText()));
				} catch (ParseException e1) {
					//Will not happen
				}
			}
			editPanel.setVisible(false);
		}
	};
	
	// CONSTRUCTOR ***********************
	// ***********************************
	
	protected ReadingPointDialog(Journal j) {
		super(Fsfibu2DefaultReference.getDefaultReference(),Fsfibu2StringTableMgr.getLoader(),PolyglotStringTable.getGlobalLanguageID());
		setTitle(Fsfibu2StringTableMgr.getString(sgroup + ".dialogtitle"));
		associatedJournal = j;
		
		//Init GUI
		pointList.setModel(new ReadingPointModel(associatedJournal));
		pointList.setCellRenderer(new ReadingPointRenderer());
		pointList.setBorder(BorderFactory.createTitledBorder(Fsfibu2StringTableMgr.getString(sgroup + ".readingpoints")));
		pointList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		JLabel nameLabel = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".name"));
		SwitchIconLabel dateLabel = new SwitchIconLabel(Fsfibu2StringTableMgr.getString(sgroup + ".date"));
			dateLabel.setIconReference(warnIcon);
			dateLabel.setHorizontalTextPosition(SwingConstants.LEFT);
		
		editPanel.setBorder(BorderFactory.createEtchedBorder());
		JPanel fillPanel = new JPanel();
		JScrollPane listPane = new JScrollPane(pointList);
		
		// Validation
		
		LabelIndicValidator<JTextField> dateValidator = new LabelIndicValidator<JTextField>(null, warnIcon,warnIcon) {
			@Override
			protected void registerToComponent(JTextField component) {
				dateField.getDocument().addDocumentListener(this);
			}
			@Override
			protected void unregisterFromComponent(JTextField component) {
				dateField.getDocument().removeDocumentListener(this);
			}
			@Override
			public Result validate(JTextField component) {
				Result r = Result.CORRECT;
				String tooltip = null;
				try {
					Fsfibu2DateFormats.getDateInputFormat().parse(component.getText());
				} catch (ParseException e) {
					r = Result.INCORRECT;
					tooltip = Fsfibu2StringTableMgr.getString(sgroup + ".invaliddate");
				}
				setToolTipText(component, tooltip);
				return r;
			}
		};
		dateValidator.addComponent(dateField, dateLabel);
		okValidator.addValidator(dateValidator);
		okValidator.validate();
		
		//Layout
		
		//Layout dialog
		
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		
		GridBagConstraints gcList 	= GUIToolbox.buildConstraints(0, 0, 1, 6);
		GridBagConstraints gcNew 	= GUIToolbox.buildConstraints(1, 0, 1, 1);
		GridBagConstraints gcEdit 	= GUIToolbox.buildConstraints(1, 1, 1, 1);
		GridBagConstraints gcDelete = GUIToolbox.buildConstraints(1, 2, 1, 1);
		GridBagConstraints gcPanel 	= GUIToolbox.buildConstraints(2, 0, 3, 3);
		GridBagConstraints gcClose 	= GUIToolbox.buildConstraints(1, 5, 1, 1); gcClose.fill = GridBagConstraints.HORIZONTAL; gcClose.anchor = GridBagConstraints.SOUTH;
		GridBagConstraints gcFill   = GUIToolbox.buildConstraints(3, 0, 1, 1); gcFill.weightx = 100;
		
		for(GridBagConstraints gc : Arrays.asList(gcList,gcNew,gcEdit,gcDelete,gcPanel,gcClose)) {
			gc.insets = new Insets(5,5,5,5);
		}
		
		gbl.setConstraints(listPane, gcList);
		gbl.setConstraints(newButton, gcNew);
		gbl.setConstraints(editButton, gcEdit);
		gbl.setConstraints(deleteButton, gcDelete);
		gbl.setConstraints(editPanel, gcPanel);
		gbl.setConstraints(closeButton, gcClose);
		gbl.setConstraints(fillPanel, gcFill);
		
		add(listPane); add(newButton); add(editButton); add(deleteButton); add(editPanel); add(closeButton);add(fillPanel);
		
		//Layout panel
		GridBagLayout gbl2 = new GridBagLayout();
		editPanel.setLayout(gbl2);
		
		GridBagConstraints gcLabelName = GUIToolbox.buildConstraints(0, 0, 1, 1);
		GridBagConstraints gcName = GUIToolbox.buildConstraints(1, 0, 2, 1);
		GridBagConstraints gcLabelDate = GUIToolbox.buildConstraints(0, 1, 1, 1);
		GridBagConstraints gcDate = GUIToolbox.buildConstraints(1, 1, 2, 1);
		GridBagConstraints gcOk = GUIToolbox.buildConstraints(1, 2, 1, 1);
		GridBagConstraints gcCancel = GUIToolbox.buildConstraints(2, 2, 1, 1);
		
		for(GridBagConstraints gc : Arrays.asList(gcLabelName, gcName, gcLabelDate, gcDate, gcOk, gcCancel)) {
			gc.insets = new Insets(5,5,5,5);
		}
		
		gbl2.setConstraints(nameLabel, gcLabelName);
		gbl2.setConstraints(nameField, gcName);
		gbl2.setConstraints(dateLabel, gcLabelDate);
		gbl2.setConstraints(dateField, gcDate);
		gbl2.setConstraints(okButton, gcOk);
		gbl2.setConstraints(cancelButton, gcCancel);
		
		editPanel.add(nameLabel); editPanel.add(nameField); editPanel.add(dateLabel);
		editPanel.add(dateField); editPanel.add(okButton); editPanel.add(cancelButton);
		
		pack();
		setResizable(false);
		
		editPanel.setVisible(false);
		
		//Listeners
		pointList.addListSelectionListener(selectionListener);
			selectionListener.valueChanged(null);
		newButton.addActionListener(newListener);
		editButton.addActionListener(editListener);
		deleteButton.addActionListener(deleteListener);
		closeButton.addActionListener(closeListener);
		cancelButton.addActionListener(cancelListener);
		okButton.addActionListener(okListener);
	}
	
	/**
	 * @return The dialog instance associated to j (Creates it, if it doesn't exist yet) or null, if j == null
	 */
	public static ReadingPointDialog getInstance(Journal j) {
		if(j == null) return null;
		else {
			ReadingPointDialog diag = dialogMap.get(j);
			if(diag != null) return diag;
			else {
				diag = new ReadingPointDialog(j);
				dialogMap.put(j, diag);
				return diag;
			}
		}
	}
	
	// CONTROL METHODS *************************
	// *****************************************
	
	private void setCurrentlyEdited(ReadingPoint p) {
		currentlyEdited = p;
		if(p == null) {
			nameField.setText("");
			dateField.setText("");
			okValidator.validate();			
		}
		else {
			nameField.setText(p.getName());
			dateField.setText(Fsfibu2DateFormats.getEntryDateFormat().format(p.getReadingDay().getTime()));
			okValidator.validate();
		}
	}
	
	// RESOURCEDEPENDENT ***********************
	// *****************************************

	@Override
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree();
		tree.addPath("graphics/share/warn.png");
		return tree;
	}
	
	
	
}
