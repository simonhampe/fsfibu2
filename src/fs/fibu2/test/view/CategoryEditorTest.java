package fs.fibu2.test.view;

import java.io.File;
import java.util.Locale;

import org.apache.log4j.BasicConfigurator;

import fs.event.DataRetrievalListener;
import fs.fibu2.data.format.Fsfibu1Converter;
import fs.fibu2.data.model.Category;
import fs.fibu2.data.model.Journal;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.render.CategoryEditor;
import fs.xml.FsfwDefaultReference;
import fs.xml.XMLToolbox;

/**
 * Tests the class {@link CategoryEditor}
 * @author Simon Hampe
 *
 */
public class CategoryEditorTest {

	public static void main(String[] args) {
		BasicConfigurator.configure();
		String basedir = "../";
		Fsfibu2DefaultReference.setFsfibuDirectory(basedir + "fsfibu2/");
		FsfwDefaultReference.setFsfwDirectory(basedir + "fsframework/");
		Locale.setDefault(Locale.GERMANY);
		
		try {
			final Journal j = Fsfibu1Converter.convertFsfibu1Journal(XMLToolbox.loadXMLFile(new File(basedir + "/fsfibu/KassenbuchAb2008.xml")));
			CategoryEditor editor = new CategoryEditor(null, j);
			editor.setVisible(true);
			editor.addDataRetrievalListener(new DataRetrievalListener() {
				@Override
				public void dataReady(Object source, Object data) {
					if(data == null) System.out.println("is null");
					else System.out.println(((Category)data).toString());
				}
			});
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

}
