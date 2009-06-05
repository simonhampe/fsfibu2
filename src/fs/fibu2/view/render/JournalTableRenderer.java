package fs.fibu2.view.render;

import java.awt.Color;
import java.awt.Component;
import java.text.NumberFormat;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import fs.fibu2.data.format.DefaultFloatComparator;
import fs.fibu2.data.format.Fsfibu2DateFormats;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.EntrySeparator;
import fs.fibu2.data.model.Journal;
import fs.fibu2.data.model.ReadingPoint;
import fs.fibu2.view.model.JournalTableModel;

/**
 * This class renders cells in a table with a {@link JournalTableModel}.
 * @author Simon Hampe
 *
 */
public class JournalTableRenderer implements TableCellRenderer {

	//The associated data model
	private JournalTableModel associatedModel;
	
	//Color values ********************************************
	
	//A red color for negative values
	private final static Color color_value_negative = new Color(255,0,0);
	
	//We use two alternating colors to display category string sequences: black and dark red
	private final static String color_alternate_one = "#000000";
	private final static String color_alternate_two = "#FF0000";
	
	// CONSTRUCTOR ********************************************
	// ********************************************************
	
	/**
	 * Creates a renderer which retrieves the bilancial information needed from the given model
	 */
	public JournalTableRenderer(JournalTableModel model) {
		associatedModel = model == null? new JournalTableModel(new Journal(),null,true,true,true) : model;
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
			case 0: //TODO: Graphical indicators 
					break;
			case 1: label.setText(e.getName());
					break;
			case 2: label.setText(Fsfibu2DateFormats.getEntryDateFormat().format(e.getDate().getTime()));
					break;
			case 3: label.setText(NumberFormat.getInstance().format(e.getValue()) + " " + e.getCurrency().getSymbol());
					if(e.getValue() < 0) label.setForeground(color_value_negative);
					label.setHorizontalAlignment(SwingConstants.RIGHT);
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
		//For an EntrySeparator, only the second and third column contain something (i.e.: the name and (if it is a ReadingPoint) the date)
		if(value instanceof EntrySeparator) {
			if(column == 1) label.setText(((EntrySeparator)value).getName());
			if(value instanceof ReadingPoint && column == 2) label.setText(Fsfibu2DateFormats.getEntryDateFormat().format(
					((ReadingPoint)value).getReadingDay().getTime()));
		}
		return label;
	}

}
