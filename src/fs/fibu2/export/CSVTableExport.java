package fs.fibu2.export;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.TreeSet;

import fs.fibu2.data.Fsfibu2Constants;
import fs.fibu2.data.format.DefaultCurrencyFormat;
import fs.fibu2.data.format.EntryComparator;
import fs.fibu2.data.format.Fsfibu2DateFormats;
import fs.fibu2.data.format.JournalExport;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.lang.Fsfibu2StringTableMgr;

/**
 * Exports a {@link Journal} as a table to a csv format (the separator is a semicolon).
 * @author Simon Hampe
 *
 */
public class CSVTableExport implements JournalExport {

	private final static String sgroup = "fs.fibu2.export.CSVTableExport";
	
	@Override
	public void exportJournal(Journal j, String fileName) throws IOException {
		if(j == null || fileName == null) return;
		PrintWriter out = new PrintWriter(new FileWriter(fileName));
		
		//Write column headers
		String mgroup = "fs.fibu2.model.JournalTableModel";
		out.print(Fsfibu2StringTableMgr.getString(mgroup + ".columnName") + ";");
		out.print(Fsfibu2StringTableMgr.getString(mgroup + ".columnDate") + ";");
		out.print(Fsfibu2StringTableMgr.getString(mgroup + ".columnValue") + ";");
		out.print(Fsfibu2StringTableMgr.getString(mgroup + ".columnAccount") + ";");
		out.print(Fsfibu2StringTableMgr.getString(mgroup + ".columnCategory") + ";");
		out.print(Fsfibu2StringTableMgr.getString(mgroup + ".columnAccInfo") + ";");
		out.print(Fsfibu2StringTableMgr.getString(mgroup + ".columnAddInfo"));
		out.println();
		
		//Write entries
		TreeSet<Entry> sortedSet = new TreeSet<Entry>(new EntryComparator(false));
		sortedSet.addAll(j.getEntries());
		for(Entry e : sortedSet) {
			out.print(e.getName() + ";");
			out.print(Fsfibu2DateFormats.getEntryDateFormat().format(e.getDate().getTime()) + ";");
			out.print(DefaultCurrencyFormat.getFormat(Fsfibu2Constants.defaultCurrency).format(e.getValue()) + ";");
			out.print(e.getAccount().getName() + ";");
			out.print(e.getCategory().toString() + ";");
			StringBuilder accinf = new StringBuilder();
				for(String id : e.getAccountInformation().keySet()) {
					accinf.append(e.getAccount().getFieldNames().get(id) + ": " + e.getAccountInformation().get(id) + " ");
				}
			out.print(accinf + ";");
			out.print(e.getAdditionalInformation());
			out.println();
		}
		out.close();
	}

	@Override
	public String getDescription() {
		return Fsfibu2StringTableMgr.getString(sgroup + ".description");
	}

	@Override
	public String getID() {
		return "ff2export_csvtable";
	}

	@Override
	public String getName() {
		return Fsfibu2StringTableMgr.getString(sgroup + ".name");
	}

}
