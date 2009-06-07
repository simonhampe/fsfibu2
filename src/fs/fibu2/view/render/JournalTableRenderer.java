package fs.fibu2.view.render;

import java.awt.Color;
import java.awt.Component;
import java.util.Currency;
import java.util.Locale;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import org.dom4j.Document;

import fs.fibu2.data.error.EntryVerificationException;
import fs.fibu2.data.format.DefaultCurrencyFormat;
import fs.fibu2.data.format.Fsfibu2DateFormats;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.EntrySeparator;
import fs.fibu2.data.model.Journal;
import fs.fibu2.data.model.ReadingPoint;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.model.JournalTableModel;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;

/**
 * This class renders cells in a table with a {@link JournalTableModel}.
 * @author Simon Hampe
 *
 */
public class JournalTableRenderer implements TableCellRenderer, ResourceDependent {

	//The associated data model
	private JournalTableModel associatedModel;
	
	//A currency symbol for separators
	private Currency currency = Currency.getInstance(Locale.getDefault());
	
	//Color values ********************************************
	
	//A red color for negative values
	private final static Color color_value_negative = new Color(255,0,0);
	
	//We use two alternating colors to display category string sequences: black and dark red
	private final static String color_alternate_one = "#000000";
	private final static String color_alternate_two = "#FF0000";
	
	// Icons
	
	private ImageIcon indic_double = new ImageIcon(Fsfibu2DefaultReference.getDefaultReference().getFullResourcePath(this, "graphics/JournalTableRenderer/indic_double.png"));
	private ImageIcon indic_info = new ImageIcon(Fsfibu2DefaultReference.getDefaultReference().getFullResourcePath(this, "graphics/JournalTableRenderer/indic_info.png"));
	private ImageIcon indic_error = new ImageIcon(Fsfibu2DefaultReference.getDefaultReference().getFullResourcePath(this, "graphics/JournalTableRenderer/indic_error.png"));
	
	// CONSTRUCTOR ********************************************
	// ********************************************************
	
	/**
	 * Creates a renderer which retrieves the bilancial information needed from the given model
	 * @param The model to retrieve the bilancial data from
	 * @param The currency from which the symbol for reading point formatting is retrieved
	 */
	public JournalTableRenderer(JournalTableModel model, Currency currency) {
		associatedModel = model == null? new JournalTableModel(new Journal(),null,true,true,true) : model;
		this.currency = currency == null? this.currency: currency;
	}
	
	// RENDERER ************************************************
	// *********************************************************
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		JournalTableLabel label = new JournalTableLabel(isSelected,value instanceof EntrySeparator);
		if(value instanceof Entry) {
			Entry e = (Entry)value;
			switch(column) {
			case 0: EntryVerificationException ev = null;
					try {
						e.getAccount().verifyEntry(e);
					}
					catch(EntryVerificationException x) {
						ev = x;
					}
					if(e.getAdditionalInformation().length() > 0 && ev != null) {
						label.setIcon(indic_double);
						label.setToolTipText(ev.getHTMLRepresentation());
						break;
					}
					if(e.getAdditionalInformation().length() > 0) {
						label.setIcon(indic_info);
						break;
					}
					if(ev != null) {
						label.setIcon(indic_error);
						label.setToolTipText(ev.getHTMLRepresentation());
					}
					break;
			case 1: label.setText(e.getName());
					break;
			case 2: label.setText(Fsfibu2DateFormats.getEntryDateFormat().format(e.getDate().getTime()));
					break;
			case 3: label.setText(DefaultCurrencyFormat.getFormat(e.getCurrency()).format(e.getValue()));
					if(e.getValue() < 0) label.setForeground(color_value_negative);
					label.setHorizontalAlignment(SwingConstants.RIGHT);
					label.setToolTipText(associatedModel.getBilancialMapping(row).getMostRecent().information().
							getHTMLRepresentation(e.getCategory(), e.getAccount(), e.getCurrency()));
					break;
			case 4: label.setText(e.getAccount().getName());
					break;
			case 5: boolean usealternateone = true;
					StringBuilder cb = new StringBuilder();
					cb.append("<html>");
					Vector<String> ol = e.getCategory().getOrderedList();
					for(String c : ol) {
						cb.append("<font color=\"" + (usealternateone? color_alternate_one : color_alternate_two) + "\">");
						cb.append(c);
						if(c != ol.lastElement()) cb.append(": ");
						cb.append("</font>");
						usealternateone = !usealternateone;
					}
					cb.append("</html>");
					label.setText(cb.toString());
					break;
			case 6: StringBuilder b = new StringBuilder();
					b.append("<html>");
					TreeSet<String> ids = new TreeSet<String>(e.getAccountInformation().keySet()); 
					for(String id : ids) {
						b.append(e.getAccountInformation().get(id));
						if(id != ids.last()) b.append(" / ");
					}
					label.setText(b.toString());
					break;
			case 7: label.setText(e.getAdditionalInformation());
					break;
			}
		}
		//For an EntrySeparator, only the second to fourth column contain something (i.e.: the name and (if it is a ReadingPoint) the date and the overall
		//value
		if(value instanceof EntrySeparator) {
			if(column == 1) label.setText(((EntrySeparator)value).getName());
			if(column == 3) label.setText(DefaultCurrencyFormat.getFormat(currency).format(associatedModel.getBilancialMapping(row).getMostRecent().
					information().getOverallSum()));
			if(value instanceof ReadingPoint && column == 2) label.setText(Fsfibu2DateFormats.getEntryDateFormat().format(
					((ReadingPoint)value).getReadingDay().getTime()));
		}
		return label;
	}

	// RESROUCEDEPENDENT *************************************
	// *******************************************************
	
	@Override
	public void assignReference(ResourceReference r) {
		//Ignore
	}

	@Override
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree();
		String path = "graphics/JournalTableRenderer/";
		tree.addPath(path + "indic_double.png");
		tree.addPath(path + "indic_info.png");
		tree.addPath(path + "indic_error.png");
		return tree;
	}

}
