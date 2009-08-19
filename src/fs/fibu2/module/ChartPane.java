package fs.fibu2.module;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.dom4j.Document;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import fs.fibu2.data.model.Journal;
import fs.fibu2.filter.EntryFilterEditor;
import fs.fibu2.filter.StackFilter;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.event.ProgressListener;
import fs.fibu2.view.model.JournalTimeSeriesCollection;
import fs.fibu2.view.render.JournalTableBar;
import fs.gui.GUIToolbox;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;

/**
 * This class implements a panel which displays a time series chart corresponding to a certain entry collection (determined by a filter).
 * It essentially displays a continuous bilancial of the chosen entries. There also is a toolbar used 
 * @author Simon Hampe
 *
 */
public class ChartPane extends JPanel implements ResourceDependent {

	// DATA ************************************
	// *****************************************
	
	private JournalTimeSeriesCollection series;
	private StackFilter filter;	
	
	private final static String sgroup = "fs.fibu2.module.ChartPane";
	
	// COMPONENTS ******************************
	// *****************************************
	
	private JCheckBox displayButton = new JCheckBox(Fsfibu2StringTableMgr.getString(sgroup + ".display"));
	private JSlider displaySlider = new JSlider(1,120,30);
	
	private JToggleButton filterButton = new JToggleButton();
	
	private EntryFilterEditor editor;
	
	private JFreeChart chart;
	
	private JPanel progressPanel = new JPanel();
	private JLabel recalculateLabel = new JLabel(Fsfibu2StringTableMgr.getString("fs.fibu2.view.JournalTableBar.progress"));
	private JProgressBar progressBar = new JProgressBar();
	
	// LISTENERS *******************************
	// *****************************************
	
	private ItemListener displayListener = new ItemListener()  {
		@Override
		public void itemStateChanged(ItemEvent e) {
			series.setDisplayAverage(displayButton.isSelected());
		}
	};
	
	private ChangeListener periodListener = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			series.setMovingAveragePeriod(displaySlider.getValue());
		}
	};
	
	private ActionListener filterListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			editor.setVisible(filterButton.isSelected());			
		}
	};
	
	private ProgressListener<Object, Object> progressListener = new ProgressListener<Object, Object>() {
		@Override
		public void progressed(SwingWorker<Object, Object> source) {
			//ignored
		}
		@Override
		public void taskBegins(SwingWorker<Object, Object> source) {
			progressPanel.setVisible(true);
			progressBar.setIndeterminate(true);
		}
		@Override
		public void taskFinished(SwingWorker<Object, Object> source) {
			progressBar.setIndeterminate(false);
			progressPanel.setVisible(false);
		}
	};
	
	// CONSTRUCTOR *****************************
	// *****************************************
	
	public ChartPane(Journal j,String title, StackFilter f, Preferences node) {
		//Copy data
		
		boolean displayAverage = true;
		int averagePeriod = 30;
		filter = f == null? new StackFilter() : f;
		Journal journal = j == null? new Journal() : j;
		//Extract preferences
		if(node != null) {
			try {
				if(node.nodeExists("config")) {
					displayAverage = Boolean.parseBoolean(node.node("config").get("display", "true"));
					averagePeriod = Integer.parseInt(node.node("config").get("period", "30"));
				}
				if(node.nodeExists("filter")) {
					filter = (StackFilter)f.createMeFromPreferences(node.node("filter")); 
				}
			} catch (BackingStoreException e) {
				//ignore
			}
		}
		
		series = new JournalTimeSeriesCollection(journal,filter,displayAverage,averagePeriod);
		
		//Init GUI
		JToolBar bar = new JToolBar(JToolBar.HORIZONTAL);
			bar.setFloatable(false);
		JLabel sliderLabel = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".period"));
		JPanel barPanel = new JPanel();
			barPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			barPanel.add(displayButton);
			barPanel.add(sliderLabel);
			barPanel.add(displaySlider);
		chart = ChartFactory.createTimeSeriesChart(title == null? "" : title, Fsfibu2StringTableMgr.getString(sgroup + ".xaxis"), 
					Fsfibu2StringTableMgr.getString(sgroup + ".yaxis"), series, true, true, false);
		ChartPanel chartPane = new ChartPanel(chart);
		
		filterButton.setIcon(new ImageIcon(Fsfibu2DefaultReference.getDefaultReference().getFullResourcePath(this, "graphics/ChartPane/filter.png")));
		filterButton.setToolTipText(Fsfibu2StringTableMgr.getString("fs.fibu2.module.FilterPane.filter"));
		filterButton.addActionListener(filterListener);
		
		editor = filter.getEditor(journal);
			editor.setVisible(false);
		
		progressPanel.setVisible(false);
		progressPanel.add(recalculateLabel); 
		progressPanel.add(progressBar);
		series.addProgressListener(progressListener);
		
		displayButton.addItemListener(displayListener);
		displaySlider.addChangeListener(periodListener);
		
		//Layout
		//Toolbar
		bar.setLayout(new BorderLayout());
		bar.add(barPanel, BorderLayout.WEST);
		bar.add(progressPanel, BorderLayout.EAST);

		//Pane
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		
		GridBagConstraints gcBar = GUIToolbox.buildConstraints(0, 0, 1, 1); gcBar.weightx = 100;
		GridBagConstraints gcChart = GUIToolbox.buildConstraints(0, 1, 1, 1); gcChart.weighty = 100;
		GridBagConstraints gcFilter = GUIToolbox.buildConstraints(1, 0, 1, 2); gcFilter.weighty = 100;
		
		gbl.setConstraints(bar, gcBar);
		gbl.setConstraints(chartPane, gcChart);
		gbl.setConstraints(editor, gcFilter);
		
		add(bar); add(chartPane); add(editor);
		
		
	}
	
	// CONTROL METHODS *************************
	// *****************************************
	
	public void insertPreferences(Preferences node) {
		if(node != null) {
			
		}
	}
	
	/**
	 * Sets the chart title
	 */
	public void setTitle(String title) {
		chart.setTitle(title == null? "" : title);
	}
	
	// RESOURCEDEPENDENT ***********************
	// *****************************************
	
	@Override
	public void assignReference(ResourceReference r) {
		//Ignore
	}

	@Override
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree();
			tree.addPath("graphics/ChartPane/filter.png");
		return tree;
	}

}
