package fs.fibu2.test.view;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/**
 * Tests features of the JFreeChart library
 * @author Simon Hampe
 *
 */
public class ChartTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		
		TimeSeriesCollection collection = new TimeSeriesCollection();
		TimeSeries series = new TimeSeries("Test",Day.class);
			series.add(new Day(12,8,2009),13.0f);
			series.add(new Day(13,9,2009),15);
		collection.addSeries(series);
		
		JFreeChart chart = ChartFactory.createTimeSeriesChart("bla", "zeit", "wert", collection, true, true, false);
		ChartPanel panel = new ChartPanel(chart);
		frame.setContentPane(panel);
		frame.pack();
		
		TimeSeries series2 = MovingAverage.createMovingAverage(series, "avg", 15, 0);
			collection.addSeries(series2);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

}
