package fs.fibu2.test.filter;

import javax.swing.JFrame;

import org.apache.log4j.BasicConfigurator;

import fs.fibu2.filter.CurrencyFilter;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.xml.FsfwDefaultReference;

/**
 * Tests the filter classes
 * @author Simon Hampe
 *
 */
public class FilterTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			BasicConfigurator.configure();
			String basedir = "/home/talio/eclipse/workspace/";
			Fsfibu2DefaultReference.setFsfibuDirectory(basedir + "fsfibu2/");
			FsfwDefaultReference.setFsfwDirectory(basedir + "fsframework/");
			JFrame mainFrame = new JFrame();
			
			//NameFilter filter = new NameFilter(Selection.REGEX,"S.*",null);
			//ValueFilter filter = new ValueFilter(Selection.RANGE,0,0.3f,14.2f,null);
			CurrencyFilter filter = new CurrencyFilter();
			
			mainFrame.add(filter.getEditor());
			
			mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			mainFrame.pack();
			mainFrame.setVisible(true);
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
