package fs.fibu2.export;

import java.io.IOException;

import org.dom4j.Document;

import fs.fibu2.data.format.Fsfibu1Converter;
import fs.fibu2.data.format.JournalExport;
import fs.fibu2.data.model.Journal;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.xml.XMLToolbox;

/**
 * Exports a journal to the old fsfibu 1 XML format as described in {@link Fsfibu1Converter}.
 * @author Simon Hampe
 *
 */
public class OldJournalExport implements JournalExport {

	private final static String sgroup = "fs.fibu2.export.OldJournalExport";
	
	@Override
	public void exportJournal(Journal j, String fileName) throws IOException {
		if(j == null) return;
		if(fileName == null) throw new IOException("Cannot export to null file");
		Document doc = Fsfibu1Converter.convertToOldJournal(j);
		XMLToolbox.saveXML(doc, fileName);
	}

	@Override
	public String getDescription() {
		return Fsfibu2StringTableMgr.getString(sgroup + ".description");
	}

	@Override
	public String getID() {
		return "ff2export_ff1";
	}

	@Override
	public String getName() {
		return Fsfibu2StringTableMgr.getString(sgroup + ".name");
	}

}
