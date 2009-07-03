package fs.fibu2.application;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.tree.DefaultDocument;

import sun.applet.AppletPanel;

import com.sun.security.auth.UserPrincipal;

import fs.event.DocumentChangeFlag;
import fs.fibu2.data.event.JournalChangeFlag;
import fs.fibu2.data.model.Journal;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.gui.EditCloseTabComponent;
import fs.xml.FsfwDefaultReference;
import fs.xml.XMLReadConfigurationException;
import fs.xml.XMLToolbox;

/**
 * This class implements the main frame of an fsfibu2 application. It keeps a
 * list of open journals (fsfibu2 is a multi-document application) and
 * (de)serializes this list from/to the user {@link Preferences}.
 * 
 * @author Simon Hampe
 * 
 */
public class MainFrame extends JFrame {

	// DATA ******************************************
	// ***********************************************

	private Logger logger = Logger.getLogger(this.getClass());

	// List of open journals
	private Vector<JournalVector> journalsOpen = new Vector<JournalVector>();

	//TODO: Add 'last files' mechanism
	
	private final static String sgroup = "fs.fibu2.MainFrame";
	
	// COMPONENTS **********************************
	// *********************************************

	private JTabbedPane tabPane = new JTabbedPane();
	
	private JToolBar toolBar = new JToolBar();
		private JButton newButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".button.new"));
		private JButton openButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".button.open"));
		private JButton saveButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".button.save"));
		private JButton saveAsButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".button.saveas"));
		private JButton exportButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".button.export"));
		private JButton helpButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".button.help"));
		private JButton exitButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".button.exit"));
		

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

		// Create file list from preferences
		try {
			logger.info(Fsfibu2StringTableMgr
					.getString("fs.fibu2.init.prefjournals"));
			Preferences openNode = Preferences.userRoot().node(
					"fsfibu2/session/openjournals");
			int i = 1;
			while (openNode.nodeExists(Integer.toString(i))) {
				Preferences journalNode = openNode.node(Integer.toString(i));
				String path = journalNode.get("path", null);
				if (path != null) {
					addJournal(new File(path),journalNode.node("prefs"));
				}
				i++;
			}

		} catch (Exception e) {
			logger.warn(Fsfibu2StringTableMgr.getString(
					"fs.fibu2.init.prefjournalserror", e.getMessage()));
		}

		updateTitle();
		
		
		add(toolBar);
	}

	// CONTROL METHODS ****************************
	// ********************************************

	/**
	 * Sets the frame title according to the currently open journal and its changed/saved-status
	 */
	private void updateTitle() {
		StringBuilder b = new StringBuilder();
		b.append("fsfibu 2 - ");
		if (tabPane.getTabCount() == 0)
			b.append(Fsfibu2StringTableMgr
					.getString("fs.fibu2.MainFrame.nojournal"));
		else {
			JournalVector vector = journalsOpen.get(tabPane.getSelectedIndex());
			b.append(vector.journal.getName());
			if (vector.flag.hasBeenChanged())
				b.append("*");
		}
		setTitle(b.toString());
	}

	/**
	 * Adds a journal. If f == null, a new Journal is created, otherwise the journal is loaded from the file.
	 * The method logs an error, if the file cannot be opened. The preference node is passed to the JournalView to
	 * configure it. 
	 */
	private void addJournal(File f, Preferences prefNode) {
		JournalVector vector = new JournalVector();
		if(f == null) {
			vector.journal = new Journal();
		}
		else {
			try {
				Document d = XMLToolbox.loadXMLFile(f);
				vector.journal = new Journal(d.getRootElement());
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
		addVector(vector);
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
		tabPane.remove(index);
		journalsOpen.remove(index);
	}
	
	/**
	 * Adds a {@link JournalVector} to the tabbed view and internal list
	 */
	private void addVector(JournalVector vector) {
		journalsOpen.add(vector);
		tabPane.add(vector.view);
			int index = tabPane.indexOfComponent(vector.view);
			EditCloseTabComponent component = new EditCloseTabComponent(vector.journal.getName(),tabPane,false,true,FsfwDefaultReference.getDefaultReference());
			component.activateCloseButton(false);
			tabPane.setTabComponentAt(index, component);
			//TODO: Add listener to close button
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
