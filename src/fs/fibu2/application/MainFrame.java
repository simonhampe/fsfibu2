package fs.fibu2.application;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.tree.DefaultDocument;

import fs.fibu2.data.event.JournalChangeFlag;
import fs.fibu2.data.format.Fsfibu1Converter;
import fs.fibu2.data.model.Journal;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.undo.JournalUndoManager;
import fs.gui.EditCloseTabComponent;
import fs.gui.GUIToolbox;
import fs.gui.SwingAppender;
import fs.xml.FsfwDefaultReference;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;
import fs.xml.XMLToolbox;

/**
 * This class implements the main frame of an fsfibu2 application. It keeps a
 * list of open journals (fsfibu2 is a multi-document application) and
 * (de)serializes this list from/to the user {@link Preferences}.
 * 
 * @author Simon Hampe
 * 
 */
public class MainFrame extends JFrame implements ResourceDependent {

	// DATA ******************************************
	// ***********************************************

	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = -6895514864808121805L;

	private Logger logger = Logger.getLogger(this.getClass());
	
	private OptionManager optionManager;

	// List of open journals
	private Vector<JournalVector> journalsOpen = new Vector<JournalVector>();
	
	private final static String sgroup = "fs.fibu2.MainFrame";
	
	// COMPONENTS **********************************
	// *********************************************

	private JTabbedPane tabPane = new JTabbedPane();
	
	private JToolBar toolBar = new JToolBar();
		private JButton newButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".button.new"));
		private JButton openButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".button.open"));
		private JButton saveButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".button.save"));
		private JButton saveAsButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".button.saveas"));
		private JButton optionButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".button.option"));
		private JButton helpButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".button.help"));
		private JButton exitButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".button.exit"));
		private JButton undoButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".button.undo"));
		private JButton redoButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".button.redo"));
		
	private SwingAppender logAppender;	
		
	// LISTENERS ************************************
	// **********************************************
		
	//Button listeners	
		
	private ActionListener newListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			addJournal(null, null);
		}
	};
	
	private ActionListener openListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			openJournal();
		};	
	};
	
	private Action saveListener = new AbstractAction() {
		/**
		 * compiler-generated serial version uid
		 */
		private static final long serialVersionUID = 6452547778859847096L;

		@Override
		public void actionPerformed(ActionEvent e) {
			saveJournal();
		}
	};
	
	private ActionListener saveAsListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			saveJournalAs();
		}
	};
	
	private ActionListener helpListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				Desktop.getDesktop().open(new File("doc/manual.pdf"));
			} catch (Exception e1) {
				String msg = Fsfibu2StringTableMgr.getString(sgroup + ".helperror");
				logger.error(msg);
				JOptionPane.showMessageDialog(null,msg, Fsfibu2StringTableMgr.getString("fs.fibu2.global.error"),JOptionPane.ERROR_MESSAGE);
			}
		}
	};
	
	private ActionListener optionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			optionManager.optionDialog();
		}
	};
	
	private ActionListener exitListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			exit();
		}
	};

	//Adjusts the tab title according to the name of the journal and its changing status
	private ChangeListener journalListener = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			updateTitleAndButtons();
		}
	};
	
	//Adjusts the window title and the status of the undo/redo buttons according to the selected tab 
	private ChangeListener tabSelectionListener = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			updateTitleAndButtons();
		}
	};
	
	//Listens to each tab close button
	private ActionListener closeTabListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			//Obtain tab concerned and select it
			int index = -1;
				for(int i = 0; i < tabPane.getTabCount(); i++) {
					if(e.getSource() == ((EditCloseTabComponent)tabPane.getTabComponentAt(i)).getCloseButton()) {
						index = i;
						break;
					}
				}
			if(index == -1) return;
			//Close tab
			tabPane.setSelectedIndex(index);
			removeJournal();
		}
	};
	
	private UndoableEditListener undoListener = new UndoableEditListener() {
		@Override
		public void undoableEditHappened(UndoableEditEvent e) {
			updateTitleAndButtons();
		}
	};
	
	private ActionListener undoButtonListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			JournalUndoManager mgr = JournalUndoManager.getInstance(journalsOpen.get(tabPane.getSelectedIndex()).journal);
			if(mgr.canUndo()) mgr.undo();
		}
	};
	
	private ActionListener redoButtonListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			JournalUndoManager mgr = JournalUndoManager.getInstance(journalsOpen.get(tabPane.getSelectedIndex()).journal);
			if(mgr.canRedo()) mgr.redo();
		}
	};
	
	
	// CONSTRUCTOR **********************************
	// **********************************************

	/**
	 * Constructs a main frame. A list of journals which were open the last time
	 * is retrieved from the user preferences and the associated
	 * {@link JournalView}s are constructed.
	 */
	public MainFrame() {
		super();
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setIconImage(new ImageIcon(Fsfibu2DefaultReference.getDefaultReference().getFullResourcePath(this, "graphics/MainFrame/main.png")).getImage());
		
		// Create file list from preferences
		try {
			logger.info(Fsfibu2StringTableMgr.getString("fs.fibu2.init.generalprefs"));
			try {
				Preferences optionNode = Preferences.userRoot().node("fsfibu2/general");
				optionManager = new OptionManager(optionNode);
			}
			catch(Exception e) {
				optionManager = new OptionManager(null);
			}
			logger.info(Fsfibu2StringTableMgr
					.getString("fs.fibu2.init.prefjournals"));
			Preferences openNode = Preferences.userRoot().node(
					"fsfibu2/session/openjournals");
			int i = 0;
			while (openNode.nodeExists(Integer.toString(i))) {
				Preferences journalNode = openNode.node(Integer.toString(i));
				String path = journalNode.get("path", null);
				if (path != null) {
					addJournal(new File(path),journalNode.node("prefs"));
				}
				i++;
			}
			String selected = openNode.get("selected", null);
			if(selected != null) {
				try{
					Integer selectedIndex = Integer.parseInt(selected);
					if(0 <= selectedIndex && selectedIndex < tabPane.getTabCount()) tabPane.setSelectedIndex(selectedIndex);
				}
				catch(NumberFormatException ne) {
					//Ignore
				}
			}
		} catch (Exception e) {
			logger.warn(Fsfibu2StringTableMgr.getString(
					"fs.fibu2.init.prefjournalserror", e.getMessage()));
			e.printStackTrace();
		}
		updateTitleAndButtons();
		
		//Add buttons
		GridBagLayout gbl = new GridBagLayout();
		toolBar.setLayout(gbl);
		
		int col = 0;
		for(JButton b : Arrays.asList(newButton, openButton, saveButton, saveAsButton,optionButton, helpButton, exitButton)) {
			GridBagConstraints gc = GUIToolbox.buildConstraints(col, 0, 1, 1);
			gbl.setConstraints(b, gc);
			toolBar.add(b);
			col ++;
		}
		
		JPanel fillPanel = new JPanel();
		GridBagConstraints gcFill = GUIToolbox.buildConstraints(col, 0, 1, 1); col++;
			gcFill.weightx = 100;
		gbl.setConstraints(fillPanel, gcFill);
		toolBar.add(fillPanel);
		
		GridBagConstraints gcUndo = GUIToolbox.buildConstraints(col, 0, 1, 1); col++;
		GridBagConstraints gcRedo = GUIToolbox.buildConstraints(col, 0, 1, 1);
		gbl.setConstraints(undoButton, gcUndo);
		gbl.setConstraints(redoButton, gcRedo);
		toolBar.add(undoButton); toolBar.add(redoButton);
		
		String path = "graphics/MainFrame/";
		Fsfibu2DefaultReference ref = Fsfibu2DefaultReference.getDefaultReference();
		newButton.setIcon(new ImageIcon(ref.getFullResourcePath(this, path + "new.png")));
			newButton.addActionListener(newListener);
		openButton.setIcon(new ImageIcon(ref.getFullResourcePath(this, path + "open.png")));
			openButton.addActionListener(openListener);
		saveButton.setIcon(new ImageIcon(ref.getFullResourcePath(this,path + "save.png")));
			saveButton.addActionListener(saveListener);
			saveButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S,ActionEvent.CTRL_MASK), "save");
			saveButton.getActionMap().put("save", saveListener);
		saveAsButton.setIcon(new ImageIcon(ref.getFullResourcePath(this, path + "save.png")));
			saveAsButton.addActionListener(saveAsListener);
		optionButton.setIcon(new ImageIcon(ref.getFullResourcePath(this, path + "options.png")));
			optionButton.addActionListener(optionListener);
		helpButton.setIcon(new ImageIcon(ref.getFullResourcePath(this, path + "help.png")));
			helpButton.addActionListener(helpListener);
		exitButton.setIcon(new ImageIcon(ref.getFullResourcePath(this, path + "exit.png")));
			exitButton.addActionListener(exitListener);
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					exit();
				}
			});
		undoButton.setIcon(new ImageIcon(ref.getFullResourcePath(this, path + "undo.png")));
			undoButton.addActionListener(undoButtonListener);
		redoButton.setIcon(new ImageIcon(ref.getFullResourcePath(this, path + "redo.png")));
			redoButton.addActionListener(redoButtonListener);
		toolBar.setFloatable(false);
		tabPane.addChangeListener(tabSelectionListener);
		
		logAppender = new SwingAppender(Fsfibu2StringTableMgr.getString(".appendertitle"));
			
		Logger.getLogger("fs.fibu2").addAppender(logAppender.getModel());
		JPanel statusBar = new JPanel(new BorderLayout());
		statusBar.add(logAppender,BorderLayout.WEST);
		
		//Layout
		setLayout(new BorderLayout());
		
		add(toolBar,BorderLayout.NORTH);
		add(statusBar,BorderLayout.SOUTH);
		
		//Add journals
		add(tabPane, BorderLayout.CENTER);
	}

	// CONTROL METHODS ****************************
	// ********************************************

	/**
	 * Sets the frame title according to the currently open journal and its changed/saved-status and updates the status of
	 * the save,saveas,export,undo,redo buttons and the titles of the tabs.
	 */
	private void updateTitleAndButtons() {
		StringBuilder b = new StringBuilder();
		b.append("fsfibu 2 - ");
		if (tabPane.getTabCount() == 0) {
			b.append(Fsfibu2StringTableMgr
					.getString("fs.fibu2.MainFrame.nojournal"));
			saveButton.setEnabled(false);
			saveAsButton.setEnabled(false);
			undoButton.setEnabled(false);
			redoButton.setEnabled(false);
		}
		else {
			JournalVector vector = journalsOpen.get(tabPane.getSelectedIndex());
			if(vector.file == null) b.append(Fsfibu2StringTableMgr.getString("fs.fibu2.MainFrame.unnamed"));
			else b.append(vector.file.getAbsolutePath());
			if (vector.flag.hasBeenChanged())
				b.append("*");
			saveButton.setEnabled(true);
			saveAsButton.setEnabled(true);
			for(int i = 0 ; i < journalsOpen.size(); i++) {
				String name = journalsOpen.get(i).journal.getName();
				if(name == null || name.trim().equals("")) name = Fsfibu2StringTableMgr.getString("fs.fibu2.MainFrame.unnamed");
				if(journalsOpen.get(i).flag.hasBeenChanged()) name = name + "*";
				if(tabPane.getTabComponentAt(i) != null)((EditCloseTabComponent)tabPane.getTabComponentAt(i)).getTextLabel().setText(name);
			}
			//Now update undo and redo button
			JournalUndoManager mgr = JournalUndoManager.getInstance(vector.journal);
			undoButton.setEnabled(mgr.canUndo());
				if(undoButton.isEnabled()) undoButton.setToolTipText(mgr.getUndoPresentationName());
			redoButton.setEnabled(mgr.canRedo());
				if(redoButton.isEnabled()) redoButton.setToolTipText(mgr.getRedoPresentationName());
		}
		setTitle(b.toString());
		

	}
	
	/**
	 * Adds a journal. If f == null, a new Journal is created, otherwise the journal is loaded from the file.
	 * The method logs an error, if the file cannot be opened. The preference node is passed to the JournalView to
	 * configure it. The method checks if the root node of the loaded document is named 'journal'. If this is not the case,
	 * it checks, if the journal conforms to the fsfibu1 specification and in that case offers to import it. If the xml document does neither
	 * conform to the fsfibu2 nor the fsfibu1 format, the user is asked, if he still wants to load this document.
	 */
	private void addJournal(File f, Preferences prefNode) {
		JournalVector vector = new JournalVector();
		boolean converted = false; //Indicates that the document was loaded from an fsfibu1 journal
		if(f == null) {
			logger.info(Fsfibu2StringTableMgr.getString("fs.fibu2.MainFrame.loadingnew"));
			vector.journal = new Journal();
		}
		else {
			try {
				vector.file = f;
				Document d = XMLToolbox.loadXMLFile(f);
				//Now check if it actually can be an fsfibu2 document
				if(!d.getRootElement().getName().equals("journal")) {
					try {
						logger.info(Fsfibu2StringTableMgr.getString("fs.fibu2.MainFrame.logfibu1format"));
						Journal j = Fsfibu1Converter.convertFsfibu1Journal(d);
						converted = true;
						int ans2 = JOptionPane.showConfirmDialog(this, Fsfibu2StringTableMgr.getString("fs.fibu2.MainFrame.fibu1format"), 
								Fsfibu2StringTableMgr.getString("fs.fibu2.MainFrame.confirmloadtitle"), 
								JOptionPane.YES_NO_OPTION);
						if(ans2 == JOptionPane.NO_OPTION) return;
						else {
							vector.journal = j;
							vector.file = null;
						}
					} catch (Exception e) {
						logger.warn(Fsfibu2StringTableMgr.getString("fs.fibu2.MainFrame.lognoknownformat"));
						int ans3 = JOptionPane.showConfirmDialog(this, Fsfibu2StringTableMgr.getString("fs.fibu2.MainFrame.noknownformat"),
								Fsfibu2StringTableMgr.getString("fs.fibu2.MainFrame.confirmloadtitle"),
								JOptionPane.YES_NO_OPTION);
						if(ans3 == JOptionPane.NO_OPTION) return;
						else vector.journal = new Journal(d.getRootElement());
					}
				}
				//If this is the case just add it
				else vector.journal = new Journal(d.getRootElement());
			}
			catch(Exception e) {
				logger.error(Fsfibu2StringTableMgr.getString("fs.fibu2.MainFrame.journalfileerror", f.getName(),e.getMessage()));
				return;
			}
		}
		vector.view = new JournalView(vector.journal, prefNode);
		JournalChangeFlag flag = new JournalChangeFlag();
		vector.journal.addJournalListener(flag);
		vector.flag = flag;
		//Any converted journal is considered changed and unsaved
		if(converted) vector.flag.setChangeFlag(true);
		addVector(vector);
		logger.info(Fsfibu2StringTableMgr.getString(f == null? "fs.fibu2.MainFrame.openednewjournal" : "fs.fibu2.MainFrame.openedjournal",vector.journal.getName()));
	}
	
	/**
	 * Opens a file choosing dialog and tries to open the selected file. 
	 */
	private void openJournal() {
		JFileChooser chooser = new JFileChooser(".");
		int ans = chooser.showOpenDialog(this);
		if(ans == JFileChooser.APPROVE_OPTION) {
			addJournal(chooser.getSelectedFile(), null);
		}
	}
	
	/**
	 * Saves the currently selected journal. If no journals are open, this call is ignored. If there is no file associated to the 
	 * journal, a dialog is opened, which prompts the user to give a file name
	 * @return true, if the journal was successfully saved
	 */
	private boolean saveJournal() {
		if(tabPane.getTabCount() == 0) return true;
		JournalVector vector = journalsOpen.get(tabPane.getSelectedIndex());
		//If there is an associated file, just save to it
		if(vector.file != null) {
			DefaultDocument doc = new DefaultDocument();
			try {
				doc.setRootElement(vector.journal.getConfiguration());
				XMLToolbox.saveXML(doc, vector.file.getAbsolutePath());
				vector.flag.setChangeFlag(false);
				updateTitleAndButtons();
				return true;
			} catch (Exception e) {
				String msg = Fsfibu2StringTableMgr.getString("fs.fibu2.MainFrame.cannotsave");
				logger.error(msg);
				JOptionPane.showMessageDialog(this,msg,Fsfibu2StringTableMgr.getString("fs.fibu2.global.error"),JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		//Otherwise prompt the user to open a dialog
		else return saveJournalAs();
	}

	/**
	 * Prompts the user to enter a filename and tries to save the currently selected journal to the selected file. If no journals are open, this call is ignored. 
	 * @return true, if the journal was successfully saved
	 */
	private boolean saveJournalAs() {
		if(tabPane.getTabCount() == 0) return true;
		JournalVector vector = journalsOpen.get(tabPane.getSelectedIndex());
		JFileChooser chooser = new JFileChooser();
		chooser.addChoosableFileFilter(new FileNameExtensionFilter(Fsfibu2StringTableMgr.getString("fs.fibu2.global.xmldescription"),"xml"));
		int ans = chooser.showSaveDialog(this);
		if(ans == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			DefaultDocument doc = new DefaultDocument();
			try {
				doc.setRootElement(vector.journal.getConfiguration());
				XMLToolbox.saveXML(doc, f.getAbsolutePath());
				vector.file = f;
				vector.flag.setChangeFlag(false);
				updateTitleAndButtons();
				return true;
			} catch (Exception e) {
				String msg = Fsfibu2StringTableMgr.getString("fs.fibu2.MainFrame.cannotsave");
				logger.error(msg);
				JOptionPane.showMessageDialog(this,msg,Fsfibu2StringTableMgr.getString("fs.fibu2.global.error"),JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		else return false;
	}
	
	/**
	 * Removes the currently selected journal. If no journals are open, this call is ignored. If the currently selected journal has been changed, but not saved,
	 * a confirmation dialog pops up.
	 */
	private void removeJournal() {
		if(tabPane.getTabCount() == 0) return;
		JournalVector vector = journalsOpen.get(tabPane.getSelectedIndex());
		//Ask to save before removing
		if(vector.flag.hasBeenChanged()) {
			int ans = JOptionPane.showConfirmDialog(this, Fsfibu2StringTableMgr.getString("fs.fibu2.MainFrame.confirmsave"), 
					Fsfibu2StringTableMgr.getString("fs.fibu2.MainFrame.confirmsavetitle"), JOptionPane.YES_NO_CANCEL_OPTION);
			switch(ans) {
			case JOptionPane.CANCEL_OPTION: return;
			case JOptionPane.YES_OPTION: 
				saveJournal();
			}
		}
		int index = tabPane.getSelectedIndex();
		journalsOpen.remove(index);
		tabPane.remove(index);
		updateTitleAndButtons();
	}
	
	/**
	 * Adds a {@link JournalVector} to the tabbed view and internal list
	 */
	private void addVector(JournalVector vector) {
		journalsOpen.add(vector);
		tabPane.removeChangeListener(tabSelectionListener); //We don't want to notify here, since the new tab does not have the right tab component yet
		tabPane.add(vector.view);
			int index = tabPane.indexOfComponent(vector.view);
			EditCloseTabComponent component = new EditCloseTabComponent(vector.journal.getName(),tabPane,false,true,FsfwDefaultReference.getDefaultReference());
			component.activateCloseButton(false);
			component.getCloseButton().addActionListener(closeTabListener);
			tabPane.setTabComponentAt(index, component);
		tabPane.addChangeListener(tabSelectionListener);
		vector.flag.addChangeListener(journalListener);
		JournalUndoManager.getInstance(vector.journal).addUndoableEditListener(undoListener);
			updateTitleAndButtons();
	}

	/**
	 * Exits the application and saves all preferences. If there are any unsaved documents, prompts the user to save them
	 */
	private void exit() {
		int selectedIndex = tabPane.getSelectedIndex();
		//First check, if there are any unsaved documents
		boolean unsaved = false;
		for(JournalVector v : journalsOpen) {
			if(v.flag.hasBeenChanged()){
				unsaved = true;break;
			}
		}
		//If this is the case, prompt the user to save them
		if(unsaved) {
			int ans = JOptionPane.showConfirmDialog(this,Fsfibu2StringTableMgr.getString("fs.fibu2.MainFrame.exitsave"), 
											Fsfibu2StringTableMgr.getString("fs.fibu2.MainFrame.confirmsavetitle"), JOptionPane.YES_NO_CANCEL_OPTION);
			switch(ans) {
			case JOptionPane.CANCEL_OPTION: return;
			case JOptionPane.YES_OPTION: 
				//Save each document
				logger.info(Fsfibu2StringTableMgr.getString("fs.fibu2.MainFrame.saving"));
				for(int i = 0; i < journalsOpen.size(); i++) {
					tabPane.setSelectedIndex(i);
					if(!journalsOpen.get(i).flag.hasBeenChanged()) continue;
					boolean success = saveJournal();
					//Abort, if any journal wasn't saved successfully
					if(!success) return;
				}
			}
		}
		//Now save preferences
		logger.info(Fsfibu2StringTableMgr.getString("fs.fibu2.MainFrame.savingprefs"));
		optionManager.saveLanguage();
		Preferences optionNode = Preferences.userRoot().node("fsfibu2/general");
		optionManager.insertPreferences(optionNode);
		Preferences openNode = Preferences.userRoot().node("fsfibu2/session/openjournals");
		if(selectedIndex >= 0) openNode.put("selected", Integer.toString(selectedIndex));
		//First we clear all preferences
		int clear = 0;
		try {
			while(openNode.nodeExists(Integer.toString(clear))) {
				openNode.node(Integer.toString(clear)).removeNode();
				clear++;
			}
		} catch (BackingStoreException e) {
			logger.warn(Fsfibu2StringTableMgr.getString("fs.fibu2.MainFrame.cannotclear"));
		}
		int j = 0;
		for(JournalVector v : journalsOpen) {
			if(v.file != null) {
				Preferences journalNode = openNode.node(Integer.toString(j));
				journalNode.put("path", v.file.getAbsolutePath());
				v.view.insertPreferences(journalNode.node("prefs"));
				j++;
			}
		}
		try {
			Preferences.userRoot().flush();
		} catch (BackingStoreException e) {
			logger.warn(Fsfibu2StringTableMgr.getString(sgroup + ".cannotsaveprefs",e.getLocalizedMessage()));
		}
		System.exit(0);
	}
	
	// RESOURCEDEPENDENT ***********************************
	// *****************************************************
	
	@Override
	public void assignReference(ResourceReference r) {
		//Ignored
	}

	@Override
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree();
			String path = "graphics/MainFrame/";
			tree.addPath(path + ".new.png");
			tree.addPath(path + "open.png");
			tree.addPath(path + "save.png");
			tree.addPath(path + "export.png");
			tree.addPath(path + "help.png");
			tree.addPath(path + "exit.png");
			tree.addPath(path + "main.png");
		return tree;
	}

	
	// LOCAL QUADRUPLE CLASS **********************
	// ********************************************

	/**
	 * This class represents a simple quadruple of data: A journal, its
	 * associated file (might be null), its view and its
	 * {@link JournalChangeFlag}
	 */
	private class JournalVector {
		public Journal journal;
		public File file;
		public JournalView view;
		public JournalChangeFlag flag;
	}

}
