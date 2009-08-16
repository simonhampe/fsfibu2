package fs.fibu2.export;

import java.io.IOException;

import org.dom4j.Element;
import org.dom4j.tree.DefaultDocument;

import fs.fibu2.data.format.JournalExport;
import fs.fibu2.data.model.Journal;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.xml.XMLReadConfigurationException;
import fs.xml.XMLToolbox;

/**
 * 'Exports' a journal to the standard fsfibu 2 XML format.
 * @author Simon Hampe
 *
 */
public class StandardJournalExport implements JournalExport {

	private final static String sgroup = "fs.fibu2.export.StandardJournalExport";
	
	@Override
	public void exportJournal(Journal j, String fileName) throws IOException {
		if(j == null) return;
		if(fileName == null) throw new IOException("Cannot save to null file");
		try {
			Element d = j.getConfiguration();
			DefaultDocument doc = new DefaultDocument();
			doc.setRootElement(d);
			XMLToolbox.saveXML(doc, fileName);
		} catch (XMLReadConfigurationException e) {
			throw new IOException("Cannot read journal configuration");
		}
	}

	@Override
	public String getDescription() {
		return Fsfibu2StringTableMgr.getString(sgroup + ".description");
	}

	@Override
	public String getID() {
		return "ff2export_standard";
	}

	@Override
	public String getName() {
		return Fsfibu2StringTableMgr.getString(sgroup + ".name");
	}

}
