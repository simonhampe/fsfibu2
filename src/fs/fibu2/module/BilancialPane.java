package fs.fibu2.module;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeSelectionModel;

import org.dom4j.Document;

import fs.fibu2.data.Fsfibu2Constants;
import fs.fibu2.data.model.Journal;
import fs.fibu2.filter.EntryFilterEditor;
import fs.fibu2.filter.StackFilter;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.print.BilancialPrintDialog;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.event.ProgressListener;
import fs.fibu2.view.model.BilancialAccountModel;
import fs.fibu2.view.model.BilancialTableModel;
import fs.fibu2.view.model.BilancialTreeModel;
import fs.fibu2.view.render.BilancialTableRenderer;
import fs.fibu2.view.render.BilancialTree;
import fs.fibu2.view.render.MoneyCellRenderer;
import fs.gui.GUIToolbox;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;

/**
 * Contains a bilancial view of a journal together with a toolbar for editing filters, etc.
 * @author Simon Hampe
 *
 */
public class BilancialPane extends JPanel implements ResourceDependent {

	// COMPONENTS *************************
	// ************************************
	
	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = 8907347594836518531L;
	
	private BilancialTree tree;
	private JTable table;
	private JTable accountTable;
	
	private EntryFilterEditor editor;
	
	private JToolBar bar;
	
	private JButton printButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".print"));
	private JToggleButton filterButton = new JToggleButton();
	
	private ImageIcon filterIcon = new ImageIcon(Fsfibu2DefaultReference.getDefaultReference().getFullResourcePath(this, "graphics/BilancialPane/filter.png"));
	private ImageIcon printIcon = new ImageIcon(Fsfibu2DefaultReference.getDefaultReference().getFullResourcePath(this, "graphics/BilancialPane/print.png"));
	
	private JPanel progressPanel = new JPanel();
	private JLabel recalculateLabel = new JLabel(Fsfibu2StringTableMgr.getString("fs.fibu2.view.JournalTableBar.progress"));
	private JProgressBar progressBar = new JProgressBar();
	
	// DATA *******************************
	// ************************************
	
	private StackFilter filter;
	private Journal associatedJournal;
	
	// LISTENERS **************************
	// ************************************
	
	private TreeSelectionListener treeSelectionListener = new TreeSelectionListener() {
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			if(tree.getSelectionRows() == null || tree.getSelectionRows().length == 0) {
				table.clearSelection();
			}
			else {
				int index = tree.getSelectionRows()[0];
				table.setRowSelectionInterval(index, index);
			}
		}
	};
	
	private ListSelectionListener tableSelectionListener = new ListSelectionListener() {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if(table.getSelectedRowCount() == 0) {
				tree.clearSelection();
			}
			else {
				tree.setSelectionRow(table.getSelectedRow());
			}
		}
	};
	
	private ActionListener visibilityListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			editor.setVisible(!editor.isVisible());
		}
	};
	
	private ActionListener printListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			BilancialPrintDialog diag = new BilancialPrintDialog(tree.getModel());
			diag.setVisible(true);
		}		
	};
	
	private ProgressListener<Object, Object> progressListener = new ProgressListener<Object, Object>() {
		@Override
		public void progressed(SwingWorker<Object, Object> source) {
			//Ignored
		}
		@Override
		public void taskBegins(SwingWorker<Object, Object> source) {
			progressPanel.setVisible(true);
			progressBar.setIndeterminate(true);
		}

		@Override
		public void taskFinished(SwingWorker<Object, Object> source) {
			progressPanel.setVisible(false);
			progressBar.setIndeterminate(false);
		}
	};
	
	// MISC *******************************
	// ************************************
	
	private final static String sgroup = "fs.fibu2.module.BilancialPane";
	
	// CONSTRUCTOR ************************
	// ************************************
	
	/**
	 * Construct a filter pane for the given journal, using the given filter and the given preferences. If node == null, default values are used.
	 * Filter values from node override f.
	 */
	public BilancialPane(Journal j, StackFilter f, Preferences node) {
		associatedJournal = j == null? new Journal() : j;
		filter = f == null? new StackFilter() : f;
		
		//Read out preferences
		Preferences modelNode = null;
		try {
			if(node != null && node.nodeExists("model")) {
				modelNode = node.node("model");
			}
			if(node != null && node.nodeExists("filter")) {
				filter = (StackFilter)new StackFilter().createMeFromPreferences(node.node("filter"));
			}
		} catch (BackingStoreException e) {
			//Ignore
		}
		
		//Init GUI
		BilancialTreeModel model = new BilancialTreeModel(associatedJournal,filter,modelNode);
		
		tree = new BilancialTree(model);
			tree.setRowHeight(20);
			tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			tree.addTreeSelectionListener(treeSelectionListener);
		table = new JTable();
			table.setModel(new BilancialTableModel(tree));
			table.setDefaultRenderer(Float.class, new BilancialTableRenderer(tree));
			table.setRowHeight(20);
			table.setShowGrid(false);
			table.setShowVerticalLines(true);
			table.getTableHeader().setReorderingAllowed(false);
			table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.getSelectionModel().addListSelectionListener(tableSelectionListener);
		accountTable = new JTable();
			accountTable.setModel(new BilancialAccountModel(model));
			accountTable.setDefaultRenderer(Float.class, new MoneyCellRenderer(Fsfibu2Constants.defaultCurrency));
			accountTable.getTableHeader().setReorderingAllowed(false);
		editor = filter.getEditor(associatedJournal);
			editor.setVisible(false);
		bar = new JToolBar();
			bar.setFloatable(false);
			progressPanel.setVisible(false);
			progressPanel.add(recalculateLabel); 
			progressPanel.add(progressBar);
			model.addProgressListener(progressListener);
		filterButton.setToolTipText(Fsfibu2StringTableMgr.getString("fs.fibu2.module.FilterPane.filter"));
			filterButton.setSelected(false);
			filterButton.addActionListener(visibilityListener);
			filterButton.setIcon(filterIcon);
		printButton.setToolTipText(Fsfibu2StringTableMgr.getString(sgroup + ".printtip"));
			printButton.setEnabled(true);
			printButton.addActionListener(printListener);
			printButton.setIcon(printIcon);
		
		
		JPanel treePanel = new JPanel();
			treePanel.setBackground(Color.WHITE);
		JScrollPane treeScroll = new JScrollPane(treePanel);
		JPanel accountPanel = new JPanel();
		JScrollPane accountScroll = new JScrollPane(accountTable);
			accountScroll.setBorder(BorderFactory.createTitledBorder(Fsfibu2StringTableMgr.getString(sgroup + ".accounttable")));
			accountScroll.setPreferredSize(new Dimension(accountTable.getPreferredSize().width*2, accountTable.getPreferredSize().height*3));
		JPanel cheatPanel = new JPanel();
			cheatPanel.setBackground(Color.WHITE);
		JPanel fillPanel = new JPanel();
			fillPanel.setBackground(Color.WHITE);
		JPanel fillPanel2 = new JPanel();
			fillPanel2.setBackground(Color.WHITE);
		
		//Layout
		//Toolbar
		bar.setLayout(new BorderLayout());
		bar.add(printButton, BorderLayout.WEST);
		bar.add(progressPanel, BorderLayout.EAST);
			
			
		//Tree panel
		GridBagLayout tbl = new GridBagLayout();
		treePanel.setLayout(tbl);
		GridBagConstraints gcCheat = GUIToolbox.buildConstraints(0, 0, 2, 1); 
		GridBagConstraints gcHeader = GUIToolbox.buildConstraints(2, 0, 1, 1); 
		GridBagConstraints gcIntTree = GUIToolbox.buildConstraints(0, 1, 1, 1); gcIntTree.weightx = 0;
		GridBagConstraints gcTable = GUIToolbox.buildConstraints(2, 1, 1, 1); gcTable.weightx = 80;
		GridBagConstraints gcFill = GUIToolbox.buildConstraints(1, 1, 1, 1); gcFill.weightx = 20;
		GridBagConstraints gcFill2 = GUIToolbox.buildConstraints(0, 2, 3, 1); gcFill2.weighty = 100;
		tbl.setConstraints(cheatPanel, gcCheat);
		tbl.setConstraints(table.getTableHeader(), gcHeader);
		tbl.setConstraints(tree, gcIntTree);
		tbl.setConstraints(table, gcTable);
		tbl.setConstraints(fillPanel, gcFill);
		tbl.setConstraints(fillPanel2, gcFill2);
		treePanel.add(cheatPanel); treePanel.add(table.getTableHeader());
		treePanel.add(tree); treePanel.add(table); treePanel.add(fillPanel);
		treePanel.add(fillPanel2);
		
		//Account panel
		accountPanel.setLayout(new BorderLayout());
		accountPanel.add(accountScroll,BorderLayout.WEST);
		accountPanel.add(new JPanel(),BorderLayout.EAST);
		
		
		//Overall layout
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		GridBagConstraints gcBar = GUIToolbox.buildConstraints(0,0,1,1); gcBar.weightx = 100;
		GridBagConstraints gcButton = GUIToolbox.buildConstraints(1, 0, 1, 1);
		GridBagConstraints gcTree = GUIToolbox.buildConstraints(0, 1, 2, 1); gcTree.weightx = gcTree.weighty = 100;
		GridBagConstraints gcAccount = GUIToolbox.buildConstraints(0, 2, 2, 1);
		GridBagConstraints gcFilter = GUIToolbox.buildConstraints(2, 0, 1, 3);
		gbl.setConstraints(bar, gcBar);
		gbl.setConstraints(filterButton, gcButton);
		gbl.setConstraints(treeScroll, gcTree);
		gbl.setConstraints(accountPanel, gcAccount);
		gbl.setConstraints(editor, gcFilter);
		
		add(bar); add(filterButton);add(treeScroll); add(accountPanel); add(editor);
	}
	
	// CONTROL METHODS ********************
	// ************************************
	
	public void insertMyPreferences(Preferences node) {
		try {
			if(node.nodeExists("model")) node.node("model").removeNode();
			if(node.nodeExists("filter")) node.node("filter").removeNode();
			tree.getModel().insertMyPreferences(node.node("model"));
			filter.insertMyPreferences(node);
		} catch (BackingStoreException e) {
			//Ignore
		}
		
	}
	
	/**
	 * @return The filter associated to this pane
	 */
	public StackFilter getFilter() {
		return filter;
	}
	
	// RESOURCEDEPENDENT ******************
	// ************************************
	
	@Override
	public void assignReference(ResourceReference r) {
		//Ignored
	}

	@Override
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree();
			tree.addPath("graphics/BilancialPane/filter.png");
		return tree;
	}

}
