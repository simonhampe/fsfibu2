package fs.fibu2.test;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JFrame;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.util.SortOrder;

/**
 * Theoretically I wrote this class to test the {@link Preferences} class - but didn't really need it, so I'm using it for small one-time tests.
 * @author Simon Hampe
 *
 */
public class PreferencesTest {

	/**
	 * @param args
	 * @throws BackingStoreException 
	 */
	public static void main(String[] args) throws BackingStoreException {
			JFrame frame = new JFrame();
			
			DefaultPieDataset dataset = new DefaultPieDataset();
				dataset.setValue("Süßigkeiten", 10);
				dataset.setValue("Fachschaft", 20);
				dataset.sortByKeys(SortOrder.ASCENDING);
			PiePlot3D plot = new PiePlot3D(dataset);
				
			ChartPanel cframe = new ChartPanel(new JFreeChart(plot));
			frame.add(cframe);
			
			frame.pack();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
	}

}
