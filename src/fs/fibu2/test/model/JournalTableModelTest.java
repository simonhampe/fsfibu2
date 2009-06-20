package fs.fibu2.test.model;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;

import org.apache.log4j.BasicConfigurator;

import fs.fibu2.data.format.Fsfibu1Converter;
import fs.fibu2.data.model.Category;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.filter.StackFilter;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.event.ProgressListener;
import fs.fibu2.view.model.JournalTableModel;
import fs.fibu2.view.render.BilancialPanel;
import fs.fibu2.view.render.JournalTableRenderer;
import fs.xml.FsfwDefaultReference;
import fs.xml.XMLToolbox;

/**
 * Tests the class {@link JournalTableModel}
 * @author Simon Hampe
 *
 */
public class JournalTableModelTest {

	public static void main(String[] args) {
		BasicConfigurator.configure();
		String basedir = "../";
		Fsfibu2DefaultReference.setFsfibuDirectory(basedir + "fsfibu2/");
		FsfwDefaultReference.setFsfwDirectory(basedir + "fsframework/");
		Locale.setDefault(Locale.GERMANY);
		
		try {
			final Journal j = Fsfibu1Converter.convertFsfibu1Journal(XMLToolbox.loadXMLFile(new File(basedir + "/fsfibu/KassenbuchAb2008.xml")));
								//new Journal();
			
			//EntryFilter filter = DefaultFilters.getYearFilter(2009);
								//new CategoryFilter(Category.getCategory(new Vector<String>(Arrays.asList("Fachschaft"))));
			StackFilter filter = new StackFilter();
			
			//Init table model
			
			long time1 = System.currentTimeMillis();
			final JournalTableModel model = new JournalTableModel(j,filter,true,true,false);
			long time2 = System.currentTimeMillis();
			System.out.println("Time for model calculation: " + (time2-time1));
			System.out.println("Model size: " + model.getRowCount());
			
			//Init table
			
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setLayout(new BorderLayout());

			final JTable table = new JTable();
				table.setModel(model);
				table.setDefaultRenderer(Object.class, new JournalTableRenderer(model,Currency.getInstance("EUR")));
				//System.out.println(((JLabel)(new DefaultTableCellRenderer()).getTableCellRendererComponent(table, null, false, false, 0, 0)).getBorder().getClass().getCanonicalName());
				//table.setIntercellSpacing(new Dimension(0,0));
				table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
			JScrollPane pane = new JScrollPane(table);
			
			//Init progress bar
			final JProgressBar bar = new JProgressBar();
			bar.setBorder(BorderFactory.createEtchedBorder());
			model.addProgressListener(new ProgressListener<Object, Object>() {
				@Override
				public void progressed(SwingWorker<Object, Object> source) {
					//Ignore
				}
				@Override
				public void taskBegins(SwingWorker<Object, Object> source) {
					bar.setIndeterminate(true);	
				}
				@Override
				public void taskFinished(SwingWorker<Object, Object> source) {
					bar.setIndeterminate(false);
				}
			});
			final JButton button = new JButton("Test");
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					j.addEntry(new Entry("Bla",34,Currency.getInstance("EUR"),new GregorianCalendar(),Category.getRootCategory(),"cash_box",null,null));
				}
			});
			frame.add(bar, BorderLayout.NORTH);
			frame.add(new BilancialPanel(table),BorderLayout.SOUTH);
			frame.add(filter.getEditor(j), BorderLayout.EAST);
			frame.add(pane, BorderLayout.CENTER);
			
			frame.setSize(frame.getMaximumSize());
			time1 = System.currentTimeMillis();
			frame.setVisible(true);
			time2 = System.currentTimeMillis();
			System.out.println("Time for drawing: " + (time2-time1));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
