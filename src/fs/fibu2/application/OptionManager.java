package fs.fibu2.application;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.tree.DefaultDocument;

import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.gui.FrameworkDialog;
import fs.gui.GUIToolbox;
import fs.polyglot.model.Language;
import fs.polyglot.view.LanguageListCellRenderer;
import fs.xml.FsfwConfigurator;
import fs.xml.FsfwDefaultReference;
import fs.xml.PolyglotStringLoader;
import fs.xml.PolyglotStringTable;
import fs.xml.XMLReadConfigurationException;
import fs.xml.XMLToolbox;

/**
 * This class manages the global application options. It provides functions to save these options to a {@link Preferences} node and 
 * to retrieve them from it. The only option that cannot be changed via Preferences is the the application language, which is saved in the
 * {@link FrameworkLoader} file and has to be saved explicitly. The manager also provides a dialog for the user to access these options.
 * The option manager should be instantiated after Fsframework has been initialized.
 * @author Simon Hampe
 *
 */
public class OptionManager {

	// MISC **************************
	// *******************************
	
	private Logger logger = Logger.getLogger(OptionManager.class);
	
	// OPTIONS ************************
	// ********************************
	
	/**
	 * This is not the language ID which is used by the application, but only the one which 
	 * is saved as 'user language', when exiting the application
	 */
	private String languageID = "en";
	
	// CONSTRUCTOR ********************
	// ********************************
	
	/**
	 * Constructs an option manager. If a node is specified, all values which can be found in a subnode 'options'
	 * in node, are read out. The manager does not 'apply' any options, they have to be obtained from other parts of
	 * the application
	 */
	public OptionManager(Preferences node) {
		languageID = PolyglotStringTable.getGlobalLanguageID();
	}
	
	// CONTROL METHODS ****************
	
	/**
	 * Inserts the application preferences into a subnode 'options' in the given node
	 */
	public void insertPreferences(Preferences node) {
		//So far: nothing :-)
	}
	
	/**
	 * This saves the language the user chose to the framework configurator file so that when starting the application the next time, 
	 * this language is chosen
	 */
	public void saveLanguage() {
		FsfwConfigurator configurator = new FsfwConfigurator("frameworkConfigurator");
			configurator.setGlobalLanguageID(languageID);
			configurator.setDefaultDirectory(FsfwDefaultReference.getFsfwDirectory());
		try {
			Element e = configurator.getConfiguration();
			DefaultDocument d = new DefaultDocument();
			d.setRootElement(e);
			XMLToolbox.saveXML(d, FrameworkLoader.configuratorPath);
		} catch (XMLReadConfigurationException e) {
			//Will not happen
		} catch (IOException e) {
			logger.error("Cannot save Framework configuration: " + e.getLocalizedMessage());
		}
	}
	
	/**
	 * Displays a dialog for editing the options
	 */
	public void optionDialog() {
		(new OptionDialog()).setVisible(true);
	}
	
	// OPTION DIALOG ******************************
	// ********************************************
	
	/**
	 * This class implements a simple dialog for editing fsfibu2 application options
	 * 
	 * @author Simon Hampe
	 */
	private class OptionDialog extends FrameworkDialog {
		
		/**
		 * compiler-generated serial version uid
		 */
		private static final long serialVersionUID = -6220530134379323403L;

		private final static String sgroup = "fs.fibu2.OptionDialog";
		
		// COMPONENTS *************************
		// ************************************
		
		private JButton okButton = new JButton(Fsfibu2StringTableMgr.getString("fs.fibu2.global.ok"));
		private JButton cancelButton = new JButton(Fsfibu2StringTableMgr.getString("fs.fibu2.global.cancel"));
		
		private JComboBox languageBox = new JComboBox();
		
		// LISTENERS **************************
		// ************************************
		
		private ActionListener okListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Language l = (Language)languageBox.getSelectedItem();
				OptionManager.this.languageID = l.id;
				dispose();
			}
		};
		
		private ActionListener cancelListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		};
		
		// CONSTRUCTOR ************************
		// ************************************
		
		public OptionDialog() {
			super(Fsfibu2DefaultReference.getDefaultReference(),Fsfibu2StringTableMgr.getLoader(), PolyglotStringTable.getGlobalLanguageID());
			setTitle(Fsfibu2StringTableMgr.getString(sgroup + ".title"));
			
			//Init GUI
			Vector<Language> languageList = new Vector<Language>();
			Language selected = null;
			for(String l : Fsfibu2StringTableMgr.getLoader().getLanguageList()) {
				Language lang = new Language(l,Fsfibu2StringTableMgr.getLoader().getLanguageDescription(l),false,Fsfibu2StringTableMgr.getLoader().getSupport(l));
				languageList.add(lang);
				if(l.equals(PolyglotStringTable.getGlobalLanguageID())) selected = lang;
			}
			languageBox = new JComboBox(languageList);
			if(selected != null) languageBox.setSelectedItem(selected);
			languageBox.setRenderer(new LanguageListCellRenderer(FsfwDefaultReference.getDefaultReference(),
					PolyglotStringLoader.getDefaultLoader(),PolyglotStringTable.getGlobalLanguageID()));
			
			JLabel languageLabel = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".language"));
			
			//Init layout
			GridBagLayout gbl = new GridBagLayout();
			setLayout(gbl);
			
			GridBagConstraints gcLanguageLabel = GUIToolbox.buildConstraints(0, 0, 1, 1);
			GridBagConstraints gcLanguage = GUIToolbox.buildConstraints(1, 0, 2, 1);
			GridBagConstraints gcOK = GUIToolbox.buildConstraints(1, 2, 1, 1);
			GridBagConstraints gcCancel = GUIToolbox.buildConstraints(2, 2, 1, 1);
			
			for(GridBagConstraints gc : Arrays.asList(gcLanguage, gcLanguageLabel, gcOK, gcCancel)) {
				gc.insets = new Insets(5,5,5,5);
			}
			
			gbl.setConstraints(languageLabel, gcLanguageLabel);
			gbl.setConstraints(languageBox, gcLanguage);
			gbl.setConstraints(okButton, gcOK);
			gbl.setConstraints(cancelButton, gcCancel);
			
			okButton.addActionListener(okListener);
			cancelButton.addActionListener(cancelListener);
			
			add(languageLabel); add(languageBox);
			add(okButton); add(cancelButton);
			
			pack();
			setResizable(false);
		}
		
	}
	
}
