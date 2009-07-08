package fs.fibu2.module;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import org.dom4j.Document;

import fs.fibu2.data.model.Journal;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.model.CategoryListModel;
import fs.fibu2.view.model.JournalModule;
import fs.fibu2.view.model.JournalTableModel;
import fs.fibu2.view.model.YearListModel;
import fs.fibu2.view.render.CategoryListRenderer;
import fs.fibu2.view.render.JournalTable;
import fs.fibu2.view.render.JournalTableBar;
import fs.fibu2.view.render.YearListRenderer;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;

/**
 * This module contains a journal table with an editing toolbar and combo boxes for basic year and category filters. By default, the current
 * year is automatically selected.
 * @author Simon Hampe
 *
 */
public class OverviewModule extends JPanel implements JournalModule, ResourceDependent {

	// DATA ******************************
	// ***********************************
	
	private Journal associatedJournal;
	
	private static HashMap<Journal, OverviewModule> modules = new HashMap<Journal, OverviewModule>();
	
	private final static String sgroup = "fs.fibu2.module.OverviewModule";
	
	// COMPONENTS ************************
	// ***********************************
	
	private ImageIcon tabIcon = new ImageIcon(Fsfibu2DefaultReference.getDefaultReference().getFullResourcePath(this, "graphics/OverviewModule/tab.png"));
	
	private JournalTableBar bar;
	private JournalTable table;
	private JToolBar comboBar = new JToolBar();
	private JComboBox yearBox = new JComboBox();
	private JComboBox categoryBox = new JComboBox();
	
	// LISTENERS *************************
	// ***********************************
	
	
	
	// CONSTRUCTOR ***********************
	// ***********************************
	
	/**
	 * Constructs the module which creates the actual view
	 */
	public OverviewModule() {}
	
	/**
	 * Constructs the actual component for a journal j and the given preferences
	 */
	protected OverviewModule(Preferences node, Journal j) {
		super();
		setLayout(new BorderLayout());
		
		table = new JournalTable(new JournalTableModel(j,null,true,true));
		bar = new JournalTableBar(table);
		categoryBox.setModel(new CategoryListModel(j,true));
		categoryBox.setRenderer(new CategoryListRenderer(" > "));
		yearBox.setModel(new YearListModel(j));
		yearBox.setRenderer(new YearListRenderer());
		
		comboBar.add(new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".yearlabel")));
		comboBar.add(yearBox);
		comboBar.add(new JToolBar.Separator());
		comboBar.add(new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".categorylabel")));
		comboBar.add(categoryBox);
		
		add(bar,BorderLayout.NORTH);
		add(comboBar,BorderLayout.NORTH);
		//add(new JScrollPane(table), BorderLayout.CENTER);
	}
	
	// MODULE ****************************
	// ***********************************
	
	@Override
	public JPanel getComponent(Preferences node, Journal j) {
		OverviewModule module = modules.get(j);
		if(module == null) {
			module = new OverviewModule(node, j);
			modules.put(j, module);
		}
		return module;
	}

	@Override
	public String getID() {
		return "ff2module_overview";
	}

	@Override
	public Icon getTabViewIcon() {
		return tabIcon;
	}

	@Override
	public String getTabViewName() {
		return Fsfibu2StringTableMgr.getString("fs.fibu2.module.OverviewModule.tabname");
	}

	@Override
	public String getTabViewTooltip() {
		return Fsfibu2StringTableMgr.getString("fs.fibu2.module.OverviewModule.tabtooltip");
	}

	@Override
	public void insertPreferences(Preferences node) {
		// TODO Auto-generated method stub

	}
	
	// RESOURCEDEPENDENT ******************************
	// ************************************************

	@Override
	public void assignReference(ResourceReference r) {
		//Ignore		
	}

	@Override
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree();
		tree.addPath("graphics/OverviewModule/tab.png");
		return tree;
	}
}
