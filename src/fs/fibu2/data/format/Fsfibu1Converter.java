package fs.fibu2.data.format;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.xml.sax.SAXException;

import fs.fibu2.data.model.Account;
import fs.fibu2.data.model.AccountLoader;
import fs.fibu2.data.model.Category;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.data.model.ReadingPoint;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.xml.XMLReadConfigurationException;
import fs.xml.XMLToolbox;

/**
 * This class provides static methods for converting an fsfibu1 xml document to an fsfibu2 journal and vice versa. This
 * implementation is static, i.e. if at any time in the future the fsfibu1 xml specification would change, this might not work.
 * @author Simon Hampe
 *
 */
public class Fsfibu1Converter {

	/**
	 * Converts an xml document containing an fsfibu1 journal into an fsfibu2 journal. The conversion works as follows: <br>
	 * - Bank and Kasse startvalues are considered as bank_account and cash_box start values<br>
	 * - All entries are either bank_account or cash_box entries <br>
	 * - All Messpunkte are imported as ReadingPoints with their name and the date. All
	 * derived ReadingPoints are initially active and visible <br>
	 * - The currency of each entry is EUR <br>
	 * - Account information for bank_account and cash_box will be inserted appropriately, coming from the fields
	 * 'rnummer', 'auszug' <br>
	 * - The field 'echtdatum' and the attribute 'uid' are ignored<br>
	 * - The category is derived from the fields 'kategorie' (level 1) and 'gruppe' (level 2)<br>
	 * - the fields 'name', 'wert', 'info' are interpreted as 'name', 'value', 'additionalinformation'<br>	 * 
	 * @param d The XML document containing the journal
	 * @return The converted journal
	 * @throws SAXException - If the schema file necessary for validation of d cannot be loaded or d == null
	 * @throws IOException - If an I/O-error occurs
	 */
	public static Journal convertFsfibu1Journal(Document d) throws SAXException, IOException {
		if(d == null) throw new SAXException("Can't convert from null document");
		//Validate
		SchemaFactory fac = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		Schema scheme = fac.newSchema(new File(Fsfibu2DefaultReference.getDefaultReference().getFullResourcePath(null, "schema/fsfibu1journal.xsd")));
		XMLToolbox.validateXML(d.getRootElement(), scheme);

		Journal j = new Journal();
		
		//Read out

		//Start values:
		Node root = d.getRootElement();
		Node bankNode =  root.selectSingleNode("/kassenbuch/startwerte/bank");
		Node cashNode = root.selectSingleNode("/kassenbuch/startwerte/kasse");
		j.setStartValue(AccountLoader.getAccount("bank_account"), Float.parseFloat(bankNode.getText()));
		j.setStartValue(AccountLoader.getAccount("cash_box"), Float.parseFloat(cashNode.getText()));
		
		//Reading points
		for(Object o : root.selectNodes("/kassenbuch/messpunkte/messpunkt")) {
			Node mp = (Node)o;
			GregorianCalendar c = null;
			try {
				c = Fsfibu2DateFormats.parseFsfibu1RPFormat(mp.selectSingleNode("./datum").getText());
			} catch (ParseException e) {
				//Will not happen
			}
			ReadingPoint rp = new ReadingPoint(mp.selectSingleNode("./bezeichnung").getText(),c,true,true);
			j.addReadingPoint(rp);
		}
		
		//Entries!
		for(Object o : root.selectNodes("/kassenbuch/einträge/eintrag")) {
			Node e = (Node)o;
			String name = e.selectSingleNode("./name").getText();
			float value = Float.parseFloat(e.selectSingleNode("./wert").getText());
			Currency currency = Currency.getInstance("EUR");
			GregorianCalendar date = null;
			try {
				//Convert date directly to correct format
				date = Fsfibu2DateFormats.parseFsfibu1EntryFormat(e.selectSingleNode("./datum").getText());
			} catch (ParseException e1) {
				//Will not happen
			}
			Category category = Category.getCategory(new Vector<String>(
					Arrays.asList(e.selectSingleNode("./kategorie").getText(),e.selectSingleNode("./gruppe").getText())));
			boolean isbank = Boolean.parseBoolean(e.selectSingleNode("./istkontobewegung").getText());
			String account = isbank? 
				"bank_account" : 
				"cash_box";
			HashMap<String, String> accountinformation = new HashMap<String, String>();
			String invoice = e.selectSingleNode("./rnummer").getText();
			if(!invoice.equals("")) accountinformation.put("invoice", invoice);
			if(isbank) {
				String statement = e.selectSingleNode("./auszug").getText();
				if(!statement.equals("")) accountinformation.put("statement", statement);
			}
			String additionalinformation = e.selectSingleNode("./infos").getText();
			Entry newentry = new Entry(name,value,currency,date,category,account,accountinformation,additionalinformation);
			j.addEntry(newentry);
		}
		
		return j;
	}

}
