package fs.fibu2.application;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;

import sun.java2d.loops.GeneralRenderer;

import fs.event.DataRetrievalListener;
import fs.gui.GUIToolbox;
import fs.xml.FsfwConfigurator;
import fs.xml.FsfwDefaultReference;

/**
 * This class is responsible for locating and loading fsframework (i.e. initializing {@link FsfwDefaultReference}). 
 * There are two static methods, one for loading it and one for serializing the location (which is private). Loading takes place 
 * in the following way: <br>
 * - First, the system preferences are probed for initialization data in a node fsfibu2/fsframework/<br>
 * - If there is no data, the user preferences are probed for initialization data in the same node <br>
 * - If there is no data as well, a dialog pops up which asks the user for the location of fsframework <br>
 * The given fsframework path is probed for the existence of language/fsfwStringTable.xml. If the file does not exist,
 * an error message is logged and the above algorithm continues as if there were no data (the dialog will be repeated
 * until a valid path is given or it is cancelled. When a valid path is given in the dialog, the resulting data
 * is serialized <br>
 * When serializing, the fsframework initialization data is saved in the node mentioned above first in the System
 * preferences and, if that doesn't work, in the user preferences.
 * @author talio
 *
 */
public final class FrameworkLoader {

	private static Logger logger = Logger.getLogger(FrameworkLoader.class);
	
	/**
	 * Tries to load Fsframework
	 * @throws UnsupportedOperationException - If no framework can be loaded and the user cancels the
	 * input dialog
	 */
	public static void loadFramework() throws UnsupportedOperationException {
		logger.info("Loading fsframework...");
		//Loading from system / user prefs
		boolean system = true;
		for(Preferences node : Arrays.asList(Preferences.systemRoot(), Preferences.userRoot())) {
			if(system) logger.info("Probing system preferences...:");
			else logger.info("Probing user preferences...:");
			system = !system;
			try {
				if(node.nodeExists("fsfibu2/fsframework")) {
					logger.info("Initialization data found");
					if(!isValidPath(path(node))) logger.warn("fsframework path " + path(node) + " seems to be invalid");
					else {
						applyData(path(node));
						logger.info("Successfully initialized fsframework");
						return;
					}
				}
				else logger.warn("No data found.");
			} catch (BackingStoreException e) {
				logger.warn("Could not read backing store...");
			}
		}
		//Loading from given path
		logger.info("Asking user...");
		FrameworkLoaderDialog diag = new FrameworkLoaderDialog();
		final Thread currentThread = Thread.currentThread();
		diag.addRetrievalListener(new DataRetrievalListener() {
			@Override
			public void dataReady(Object source, Object data) {
				if(data != null) {
					//Apply data, then let the main thread continue
					applyData(data.toString());
					logger.info("Successfully initialized fsframework");
					logger.info("Trying to serialize fsframework path");
					saveFramework(data.toString());
					Thread.currentThread().notifyAll();
				}
				//Interrupt the main thread to notify it that no data has been specified
				else currentThread.interrupt();
			}
			
		});
		diag.setVisible(true);
		//Wait for the completion of the dialog
//		try {
//			Thread.currentThread().wait();
//		} catch (InterruptedException e) {
//			String msg = "User cancelled process. Cannot load fsframework";
//			logger.error(msg);
//			throw new UnsupportedOperationException(msg);
//		}
		System.out.println("test");
	}
	
	private static void saveFramework(String path) {
		boolean system = true;
		for(Preferences node : Arrays.asList(Preferences.systemRoot(), Preferences.userRoot())) {
			try {
				Preferences saveNode = node.node("fsfibu2/fsframework");
				saveNode.put("path", path);
				node.flush();
				return;
			}
			catch(BackingStoreException e) {
				if(system) logger.warn("Could not save to system preferences. Trying user preferences");
				else logger.warn("Could not save to user preferences. Fsframework path will not be saved");
			}
			system = !system;
		}
	}
	
	// HELPER METHODS ******************************************
	// *********************************************************
	
	/**
	 * @return true, iff path is a directory and contains /language/fsfwStringTable.xml
	 */
	private static boolean isValidPath(String path) {
		if(path == null) return false;
		if(!(new File(path)).isDirectory()) return false;
		if(!(new File((new File(path)).getAbsolutePath() + "/language/fsfwStringTable.xml").exists())) return false;
		else return true;
	}
	
	/**
	 * @return The value for the key 'path' (Default: null)
	 */
	private static String path(Preferences node) {
		return node.get("path", null);
	}
	
	
	private static void applyData(String path) {
		FsfwDefaultReference.setFsfwDirectory(path);
	}
	
	// DIALOG CLASS *********************************
	// **********************************************
	
	private static class FrameworkLoaderDialog extends JDialog {
		
		// COMPONENTS
		
		private JLabel messageLabel = new JLabel("<html>No fsframework directory has been found.<br>" +
				"Please specify the correct directory:</html>");
		private JTextField pathField = new JTextField("this/is/a/size-calibrating/test/text");
		private JButton browseButton = new JButton("Browse...");
		private JButton okButton = new JButton("OK");
		private JButton cancelButton = new JButton("Cancel");
		private JLabel diagnosticLabel = new JLabel("Path seems invalid. language/fsfwStringTable.xml not found");
		
		// LISTENERS
		
		private DocumentListener dl = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateLabel();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateLabel();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				updateLabel();
			}
		};
		
		private ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
				fireDataReady(e.getSource() == okButton? pathField.getText() : null);
			}
		};
		
		private ActionListener bl = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				browse();
			}
		};
		
		//MISC
		
		private HashSet<DataRetrievalListener> listeners = new HashSet<DataRetrievalListener>();
		
		// CONSTRUCTOR *******************************
		// *******************************************
		
		public FrameworkLoaderDialog() {
			super((JFrame)null,"Locating fsframwork...");
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			
			//Init GUI
			
			JPanel fillPanel = new JPanel();
			messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
			GridBagLayout layout = new GridBagLayout();
			setLayout(layout);
			GridBagConstraints gcMessage = GUIToolbox.buildConstraints(0, 0, 4, 1);
				gcMessage.weightx = 100;
				gcMessage.insets = new Insets(5,5,5,5);
			GridBagConstraints gcField = GUIToolbox.buildConstraints(0, 1, 3, 1);	
				gcField.insets = new Insets(5, 5, 5, 5);
			GridBagConstraints gcBrowse = GUIToolbox.buildConstraints(3, 1, 1, 1);
				gcBrowse.insets = new Insets(5,5,5,5);
			GridBagConstraints gcOk = GUIToolbox.buildConstraints(2, 2, 1, 1);
				gcOk.insets = new Insets(5,5,5,5);
			GridBagConstraints gcCancel = GUIToolbox.buildConstraints(3, 2, 1, 1);
				gcCancel.insets = new Insets(5,5,5,5);
			GridBagConstraints gcFill = GUIToolbox.buildConstraints(0, 3, 2, 1);
				gcFill.weightx = 100;
			GridBagConstraints gcDiag = GUIToolbox.buildConstraints(0, 3, 4, 1);
			layout.setConstraints(messageLabel, gcMessage);
			layout.setConstraints(pathField, gcField);
			layout.setConstraints(browseButton, gcBrowse);
			layout.setConstraints(okButton, gcOk);
			layout.setConstraints(cancelButton, gcCancel);
			layout.setConstraints(fillPanel, gcFill);
			layout.setConstraints(diagnosticLabel, gcDiag);
			add(messageLabel);
			add(pathField); add(browseButton); add(okButton); add(cancelButton);
			add(diagnosticLabel); add(fillPanel);
			pack();
			
			pathField.getDocument().addDocumentListener(dl);
			okButton.addActionListener(al);
			cancelButton.addActionListener(al);
			browseButton.addActionListener(bl);
			
			pathField.setText("");
			updateLabel();
		}
		
		// HELPER METHODS ********************************
		// ***********************************************
		
		//Checks the validity of the given path and adjusts the label and the ok button accordingly
		private void updateLabel() {
			if(isValidPath(pathField.getText())) {
				diagnosticLabel.setText("Path seems valid. language/fsfwStringTable.xml found");
				okButton.setEnabled(true);
			}
			else {
				diagnosticLabel.setText("Path seems invalid. language/fsfwStringTable.xml not found");
				okButton.setEnabled(false);
			}
		}
		
		//Opens the browsing dialog
		private void browse() {
			JFileChooser chooser = new JFileChooser();
			int result = chooser.showOpenDialog(this);
			if(result == JFileChooser.APPROVE_OPTION) {
				pathField.setText(chooser.getSelectedFile().getAbsolutePath());
			}
		}
		
		// LISTENER MECHANISM ******************************
		// *************************************************
		
		public void addRetrievalListener(DataRetrievalListener l) {
			if(l != null) listeners.add(l);
		}
		
		public void removeRetrievalListener(DataRetrievalListener l) {
			listeners.remove(l);
		}
		
		private void fireDataReady(String data) {
			for(DataRetrievalListener l : listeners) l.dataReady(this, data);
		}
		
	}
}
