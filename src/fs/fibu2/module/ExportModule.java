package fs.fibu2.module;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.dom4j.Document;

import fs.fibu2.data.format.JournalExport;
import fs.fibu2.data.format.JournalExportLoader;
import fs.fibu2.data.model.Journal;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.view.model.ExportListModel;
import fs.fibu2.view.render.JournalExportRenderer;
import fs.fibu2.view.render.JournalModule;
import fs.gui.GUIToolbox;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;

/**
 * This class implements a module used for exporting and (automatically) backupping a journal.
 * @author Simon Hampe
 *
 */
public class ExportModule extends JPanel implements JournalModule, ResourceDependent {

	// DATA **********************************
	// ***************************************
	
	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = -6927990773224321128L;

	private final static String sgroup = "fs.fibu2.module.ExportModule";
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	private Journal associatedJournal;
	
	private static HashMap<Journal, ExportModule> moduleMap = new HashMap<Journal, ExportModule>();
	
	//For each export ID: The number of minutes after which a backup is made 
	private HashMap<String, Integer> backupTimer = new HashMap<String, Integer>();
	//For each export ID: the Filename under which a backup is saved
	private HashMap<String, String> fileNames = new HashMap<String, String>();
	
	//The timers for each ID
	private HashMap<String, Timer> timers = new HashMap<String, Timer>();
	
	// COMPONENTS ****************************
	// ***************************************
	
	private JList exportList = new JList();
	private JPanel rightPanel = new JPanel();
	
	private JLabel nameLabel = new JLabel();
	private JTextArea descriptionArea = new JTextArea();
	
	private JButton exportButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".export"));
	private JCheckBox backupCheck = new JCheckBox(Fsfibu2StringTableMgr.getString(sgroup + ".backup"));
	
	private JSpinner minuteSpinner = new JSpinner(new SpinnerNumberModel(15,5,60,1));
	private JLabel fileNameLabel = new JLabel();
	private JButton fileButton = new JButton("...");
	
	// LISTENERS *****************************
	// ***************************************
	
	private ListSelectionListener selectionListener = new ListSelectionListener() {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if(exportList.getSelectedIndex() == -1) rightPanel.setVisible(false);
			else {
				rightPanel.setVisible(true);
				fillData();
			}
		}
	};
	
	private ItemListener checkListener = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			JournalExport export = (JournalExport)exportList.getSelectedValue();
			if(backupCheck.isSelected()) {
				File f = null;
				Integer min = (Integer)minuteSpinner.getValue();
				if(fileNameLabel.getText().equals("")) {
					f = getExportFile();
					if(f != null) fileNameLabel.setText(f.getAbsolutePath());
					else {
						backupCheck.setSelected(false);
					}
				}
				else f = new File(fileNameLabel.getText());
				if(f != null) setBackup(export, min, f);
			}
			else {
				setBackup(export, 0, null);
			}
			minuteSpinner.setEnabled(backupCheck.isSelected());
			fileButton.setEnabled(backupCheck.isSelected());
		}
	};
	
	private ActionListener exportListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			JournalExport export = (JournalExport) exportList.getSelectedValue();
			File f = getExportFile();
			if(f != null)
			try {
				doExport(export, f);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(null, Fsfibu2StringTableMgr.getString(sgroup + ".backuperror",export.getName(), 
						e1.getMessage()),Fsfibu2StringTableMgr.getString("fs.fibu2.global.error"),JOptionPane.ERROR_MESSAGE);
			}
		}
	};
	
	private ChangeListener spinnerListener = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			setBackup((JournalExport) exportList.getSelectedValue(), (Integer)minuteSpinner.getValue(), new File(fileNameLabel.getText()));
		}
	};
	
	private ActionListener fileNameListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			File f = getExportFile();
			if(f != null) {
				setBackup((JournalExport)exportList.getSelectedValue(), (Integer)minuteSpinner.getValue(), f);
				fileNameLabel.setText(f.getAbsolutePath());
			}
		}
	};
	
	// CONSTRUCTOR ***************************
	// ***************************************
	
	public ExportModule() {}
	
	protected ExportModule(Journal j, Preferences node) {
		associatedJournal = j;
		
		//Extract preferences
		if(node != null) {
			for(String id : JournalExportLoader.getExportIDs()) {
				try {
					if(node.nodeExists("backup/" + id)) {
						Preferences idNode = node.node("backup/" + id);
						Integer minutes = Integer.parseInt(idNode.get("minutes", "5"));
						String fileName = idNode.get("file", null);
						backupTimer.put(id, minutes);
						fileNames.put(id, fileName);
						setBackup(JournalExportLoader.getExport(id), minutes, new File(fileName));
					}
				} catch (BackingStoreException e) {
					//Ignore
				}
			}
		}
		
		//Init GUI
		exportList.setModel(new ExportListModel());
		exportList.setCellRenderer(new JournalExportRenderer());
		exportList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		exportList.addListSelectionListener(selectionListener);
			selectionListener.valueChanged(null);
		
		rightPanel.setBorder(BorderFactory.createEtchedBorder());
		
		descriptionArea.setEditable(false);
		descriptionArea.setWrapStyleWord(true);
		descriptionArea.setBackground(rightPanel.getBackground());
		
		backupCheck.addItemListener(checkListener);
		exportButton.addActionListener(exportListener);
		fileButton.addActionListener(fileNameListener);
		minuteSpinner.addChangeListener(spinnerListener);
		
		JPanel fillPanel = new JPanel();
		
		JScrollPane pane = new JScrollPane(exportList);
		pane.setBorder(BorderFactory.createTitledBorder(Fsfibu2StringTableMgr.getString(sgroup + ".listtitle")));	
		
		JLabel minuteLabel = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".interval"));
		JLabel fileLabel = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".filename"));
		
		//Layout
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		
		GridBagConstraints gcList = GUIToolbox.buildConstraints(0, 0, 1, 2); gcList.ipadx = 150; gcList.weighty = 100;
		GridBagConstraints gcRight = GUIToolbox.buildConstraints(1, 0, 1, 1); gcRight.weightx = 100; gcRight.anchor = GridBagConstraints.NORTH;
		GridBagConstraints gcFill = GUIToolbox.buildConstraints(2, 0, 1, 2); gcFill.weightx = 100; 
		
		gbl.setConstraints(pane, gcList);
		gbl.setConstraints(rightPanel, gcRight);
		gbl.setConstraints(fillPanel, gcFill);
		
		add(pane);
		add(rightPanel);
		add(fillPanel);
		
		GridBagLayout gbl2 = new GridBagLayout();
		rightPanel.setLayout(gbl2);
		
		GridBagConstraints gcName = GUIToolbox.buildConstraints(0, 0, 1, 1);
		GridBagConstraints gcDesc = GUIToolbox.buildConstraints(0, 1, 3, 1);
		GridBagConstraints gcExport = GUIToolbox.buildConstraints(0, 2, 1, 1);
		GridBagConstraints gcBackup = GUIToolbox.buildConstraints(0, 3, 2, 1);
		GridBagConstraints gcMinLabel = GUIToolbox.buildConstraints(0, 4, 2, 1);
		GridBagConstraints gcMinSpinner = GUIToolbox.buildConstraints(2, 4, 1, 1);
			gcMinSpinner.fill = GridBagConstraints.NONE;
		GridBagConstraints gcFileLabel = GUIToolbox.buildConstraints(0, 5, 1, 1);
		GridBagConstraints gcFileName = GUIToolbox.buildConstraints(1, 5, 1, 1);
		GridBagConstraints gcFileButton = GUIToolbox.buildConstraints(2, 5, 1, 1); 
			gcFileButton.fill = GridBagConstraints.NONE;
		
		for(GridBagConstraints gc : Arrays.asList(gcName,gcDesc,gcExport,gcBackup,gcMinLabel, gcMinSpinner, gcFileLabel, gcFileName, gcFileButton)) {
			gc.insets = new Insets(5,5,5,5);
			gc.anchor = GridBagConstraints.WEST;
		}
		
		gbl2.setConstraints(nameLabel, gcName);
		gbl2.setConstraints(descriptionArea, gcDesc);
		gbl2.setConstraints(exportButton, gcExport);
		gbl2.setConstraints(backupCheck, gcBackup);
		gbl2.setConstraints(minuteLabel, gcMinLabel);
		gbl2.setConstraints(minuteSpinner, gcMinSpinner);
		gbl2.setConstraints(fileLabel, gcFileLabel);
		gbl2.setConstraints(fileNameLabel, gcFileName);
		gbl2.setConstraints(fileButton, gcFileButton);
		
		rightPanel.add(nameLabel); rightPanel.add(descriptionArea); rightPanel.add(exportButton); rightPanel.add(backupCheck);
		rightPanel.add(minuteLabel); rightPanel.add(minuteSpinner); rightPanel.add(fileLabel); rightPanel.add(fileNameLabel); rightPanel.add(fileButton);
	}
	
	// JOURNALMODULE *************************
	// ***************************************
	
	@Override
	public JPanel getComponent(Preferences node, Journal j) {
		ExportModule module = moduleMap.get(j);
		if(module == null) {
			module = new ExportModule(j, node);
			moduleMap.put(j, module);
		}
		return module;
	}

	@Override
	public String getID() {
		return "ff2module_export";
	}

	@Override
	public Icon getTabViewIcon() {
		return new ImageIcon("graphics/ExportModule/tab.png");
	}

	@Override
	public String getTabViewName() {
		return Fsfibu2StringTableMgr.getString(sgroup + ".name");
	}

	@Override
	public String getTabViewTooltip() {
		return Fsfibu2StringTableMgr.getString(sgroup + ".tooltip");
	}

	@Override
	public void insertPreferences(Preferences node, Journal j) {
		if(moduleMap.containsKey(j)) moduleMap.get(j).insertPreferences(node);
	}
	
	protected void insertPreferences(Preferences node) {
		if(node != null) {
			try {
				//Clear configuration
				if(node.nodeExists("backup")) {
					node.node("backup").removeNode();
				}
				//Insert configuration
				Preferences bNode = node.node("backup");
				for(String id : timers.keySet()) {
					Preferences idNode = bNode.node(id);
					idNode.put("minutes", Integer.toString(backupTimer.get(id)));	
					idNode.put("file", fileNames.get(id));
				}
			} catch (BackingStoreException e) {
				//Ignore
			}
		}
	}

	// CONTROL METHODS ********************************
	// ************************************************
	
	/**
	 * Fills the configuration panel with the data of the currently selected export
	 */
	private void fillData() {
		if(exportList.getSelectedIndex() >= 0) {
			JournalExport export = (JournalExport)exportList.getSelectedValue();
			nameLabel.setText("<html><b>" + export.getName() + "</b></html>");
			descriptionArea.setText(export.getDescription());
			if(backupTimer.containsKey(export.getID())) {
				fileNameLabel.setText(fileNames.get(export.getID()));
				minuteSpinner.setValue(backupTimer.get(export.getID()));
				backupCheck.setSelected(true);
			}
			else {
				backupCheck.setSelected(false);
				minuteSpinner.setValue(new Integer(15));
				fileNameLabel.setText("");
			}
			checkListener.itemStateChanged(null);
		}
	}
	
	/**
	 * Opens up a dialog for choosing a file to save an export to (null, if cancelled)
	 */
	private File getExportFile() {
		JFileChooser chooser = new JFileChooser();
		int ans = chooser.showSaveDialog(this);
		switch(ans) {
		case JFileChooser.APPROVE_OPTION: return chooser.getSelectedFile();
		case JFileChooser.CANCEL_OPTION: return null;
		default: return null;
		}
	}
	
	/**
	 * Sets the backup of the given export. If f == null, deletes the backup
	 */
	private void setBackup(final JournalExport export, Integer minute, final File f) {
		if(timers.containsKey(export.getID())) {
			timers.get(export.getID()).stop();
			timers.remove(export.getID());
		}
		if(f != null){
			backupTimer.put(export.getID(),minute);
			fileNames.put(export.getID(), f.getAbsolutePath());
			Timer t = new Timer(minute * 60000, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						doExport(export, f);
					} catch (IOException e1) {
						logger.error(Fsfibu2StringTableMgr.getString(sgroup + ".backuperror",export.getName(),e1.getMessage()));
					}
				}
			});
			t.start();
			timers.put(export.getID(), t);
		}
	}
	
	/**
	 * Exports the given export to the given file
	 * @param e
	 */
	private void doExport(JournalExport e, File f)  throws IOException {
		e.exportJournal(associatedJournal, f.getAbsolutePath());
		logger.info(Fsfibu2StringTableMgr.getString(sgroup + ".successfulbackup",e.getName()));
	}
	
	// RESOURCEDEPENDENT ******************************
	// ************************************************
	
	@Override
	public void assignReference(ResourceReference r) {
		//Ignored
	}

	@Override
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree();
			tree.addPath("graphics/ExportModule/tab.png");
		return tree;
	}

}
