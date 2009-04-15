package fs.fibu2.test.model;

import java.io.File;

import org.dom4j.Document;
import org.dom4j.tree.DefaultDocument;

import fs.fibu2.data.model.Journal;
import fs.xml.XMLToolbox;

/**
 * Tests the class journal
 * @author Simon Hampe
 *
 */
public class JournalTest {

	public static void main(String[] args) {
		try {
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
