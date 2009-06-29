package fs.fibu2.application;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.tree.DefaultDocument;

import fs.event.DataRetrievalListener;
import fs.gui.GUIToolbox;
import fs.xml.FsfwConfigurator;
import fs.xml.FsfwDefaultReference;
import fs.xml.XMLReadConfigurationException;
import fs.xml.XMLToolbox;

/**
 * This class is responsible for locating and loading fsframework (i.e. initializing {@link FsfwDefaultReference}). 
 * There are two static methods, one for loading it and one for serializing the location (which is private). Loading takes place 
 * in the following way: <br>
 * - First, the loader looks for a file frameworkConfigurator.xml which contains the configuration data for fsframework <br>
 * - If the file is not found, a dialog pops up, asking the user to provide the path to fsframework. The path will be saved in the file mentioned above.<br>
 * The given fsframework path is probed for the existence of language/fsfwStringTable.xml. If the file does not exist,
 * an error message is logged and the above algorithm continues as if there were no data (the dialog will be repeated
 * until a valid path is given or it is cancelled. When a valid path is given in the dialog, the resulting data
 * is serialized <br>
 * @author talio
 *
 */
public final class FrameworkLoader {

	private static Logger logger = Logger.getLogger(FrameworkLoader.class);
	
	private final static String configuratorPath = "frameworkConfigurator.xml";
	
	/**
	 * Tries to load Fsframework
	 * @throws UnsupportedOperationException - If no framework can be loaded and the user cancels the
	 * input dialog
	 */
	public static void loadFramework() throws UnsupportedOperationException {
		logger.info("Loading fsframework...");
		
		//Look for file
		logger.info("Locating file frameworkConfigurator.xml");
		File f = new File(configuratorPath);
		if(f.exists()) {
			logger.info("Configuration file found. Applying configuration...");
			try {
				Document d = XMLToolbox.loadXMLFile(f);
				FsfwConfigurator configurator = new FsfwConfigurator("frameworkConfigurator",d.getRootElement());
				if(isValidPath(configurator.getDefaultDirectory())){
					configurator.applyConfiguration();
					logger.info("Successfully initialized fsframework");
					return;
				}
				else {
					logger.warn("Configuration seems invalid");
				}
			}
			catch(DocumentException de) {
				logger.warn("Could not open configuration file: " + de.getMessage());
			}
		}
		
		//Loading from given path
		logger.info("File not found or invalid. Asking user...");
		FrameworkLoaderDialog diag = new FrameworkLoaderDialog();
		final Thread currentThread = Thread.currentThread();
		final Object helperObject = new Object();
		diag.addRetrievalListener(new DataRetrievalListener() {
			@Override
			public void dataReady(Object source, Object data) {
				if(data != null) {
					//Apply data
					FsfwConfigurator configurator = new FsfwConfigurator("frameworkConfigurator");
					configurator.setDefaultDirectory(data.toString());
					configurator.applyConfiguration();
					logger.info("Successfully initialized fsframework");
					logger.info("Trying to serialize fsframework path");
					try {
						Element n = configurator.getConfiguration();
						DefaultDocument doc = new DefaultDocument();
						doc.setRootElement(n);
						XMLToolbox.saveXML(doc, configuratorPath);
						logger.info("Successfully saved configuration");
					} catch (XMLReadConfigurationException e) {
						logger.warn("Could not retrieve configuration data. It will not be saved.");
					} catch (IOException e) {
						logger.warn("Could not save configuration data: " + e.getMessage());
					}
					//Now wake up main thread
					synchronized (helperObject) {
						helperObject.notify();
					}
				}
				//Interrupt the main thread to notify it that no data has been specified
				else currentThread.interrupt();
			}
			
		});
		synchronized (helperObject) {
			diag.setVisible(true);
			//Wait for the completion of the dialog
			try {
				helperObject.wait();
			} catch (InterruptedException e) {
				String msg = "User cancelled process. Cannot load fsframework";
				logger.error(msg);
				throw new UnsupportedOperationException(msg);
			}
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
	
	// DIALOG CLASS *********************************
	// **********************************************
	
	private static class FrameworkLoaderDialog extends JDialog {
		
		// COMPONENTS
		
		/**
		 * compiler-generated serial version uid
		 */
		private static final long serialVersionUID = 5772080354549058506L;
		
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
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
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
