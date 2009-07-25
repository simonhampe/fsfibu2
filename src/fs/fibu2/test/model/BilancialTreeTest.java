package fs.fibu2.test.model;

import java.io.File;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JTree;

import org.apache.log4j.BasicConfigurator;

import fs.fibu2.data.format.Fsfibu1Converter;
import fs.fibu2.data.model.Journal;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.model.BilancialTreeModel;
import fs.fibu2.view.render.BilancialTreeRenderer;
import fs.xml.FsfwDefaultReference;
import fs.xml.XMLToolbox;

/**
 * Tests the features of {@link BilancialTreeModel} and associated classes
 * 
 * @author Simon Hampe
 *
 */
public class BilancialTreeTest {

	public static void main(String[] args) {
		BasicConfigurator.configure();
		String basedir = "../";
		Fsfibu2DefaultReference.setFsfibuDirectory(basedir + "fsfibu2/");
		FsfwDefaultReference.setFsfwDirectory(basedir + "fsframework/");
		Locale.setDefault(Locale.GERMANY);
		
		try {
			final Journal j = Fsfibu1Converter.convertFsfibu1Journal(XMLToolbox.loadXMLFile(new File(basedir + "/fsfibu/KassenbuchAb2008.xml")));
			
			JFrame frame = new JFrame();
			
			JTree tree = new JTree();
				BilancialTreeModel model = new BilancialTreeModel(j,null,null);
				tree.setModel(model);
				BilancialTreeRenderer renderer = new BilancialTreeRenderer();
				tree.setCellRenderer(renderer);
			frame.add(tree);
			frame.pack();
				
			frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

}
