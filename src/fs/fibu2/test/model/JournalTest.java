package fs.fibu2.test.model;

import java.io.File;
import java.text.NumberFormat;

import org.apache.log4j.BasicConfigurator;
import org.dom4j.Document;
import org.dom4j.tree.DefaultDocument;

import fs.fibu2.data.model.Journal;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.xml.FsfwDefaultReference;
import fs.xml.XMLToolbox;

/**
 * Tests the class journal
 * @author Simon Hampe
 *
 */
public class JournalTest {

	public static void main(String[] args) {
		try {
			BasicConfigurator.configure();
			Fsfibu2DefaultReference.setFsfibuDirectory("/home/talio/eclipse/workspace/fsfibu2/");
			FsfwDefaultReference.setFsfwDirectory("/home/talio/eclipse/workspace/fsframework/");
			Document d = XMLToolbox.loadXMLFile(new File("examples/journal.xml"));
			Journal j = new Journal(d.getRootElement());
			DefaultDocument d2 = new DefaultDocument();
			d2.setRootElement(j.getConfiguration());
			System.out.println(XMLToolbox.getDocumentAsPrettyString(d2));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

}
