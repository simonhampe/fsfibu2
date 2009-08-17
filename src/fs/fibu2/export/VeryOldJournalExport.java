package fs.fibu2.export;

import java.io.File;
import java.io.IOException;

import org.dom4j.Document;

import fs.fibu2.data.format.Fsfibu1Converter;
import fs.fibu2.data.format.JournalExport;
import fs.fibu2.data.model.Journal;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;
import fs.xml.XMLToolbox;

/**
 * This class expors a {@link Journal} to a csv format which can be imported in the original OpenOffice journal file.
 * @author Simon Hampe
 *
 */
public class VeryOldJournalExport implements JournalExport, ResourceDependent {

	private final static String sgroup = "fs.fibu2.export.VeryOldJournalExport";
	
	@Override
	public void exportJournal(Journal j, String fileName) throws IOException {
		Document d = Fsfibu1Converter.convertToOldJournal(j);
		try {
			XMLToolbox.transformDocument(d, new File(Fsfibu2DefaultReference.getDefaultReference().getFullResourcePath(this, "schema/kbtooldkb.xsl")), new File(fileName));
		} catch (Exception e) {
			throw new IOException(e);
		}

	}

	@Override
	public String getDescription() {
		return Fsfibu2StringTableMgr.getString(sgroup + ".description");
	}

	@Override
	public String getID() {
		return "ff2export_original";
	}

	@Override
	public String getName() {
		return Fsfibu2StringTableMgr.getString(sgroup + ".name");
	}

	@Override
	public void assignReference(ResourceReference r) {
		//Ignored
	}

	@Override
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree();
			tree.addPath("schema/kbtooldkb.xml");
		return tree;
	}

}
