package fs.fibu2.view.render;

import java.awt.Component;
import java.util.Currency;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import fs.fibu2.data.format.DefaultCurrencyFormat;

/**
 * Renders a cell with a float value with an adjoint currency symbol and in red or black color depending on 
 * whether the value is < 0
 * @author Simon Hampe
 *
 */
public class MoneyCellRenderer extends DefaultTableCellRenderer {

	/**
	 * compiler-generated serial version uid 
	 */
	private static final long serialVersionUID = 322451993143484350L;
	
	private Currency currency = Currency.getInstance(Locale.getDefault());
	
	public MoneyCellRenderer(Currency currency) {
		if(currency == null) this.currency = currency;
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
				row, column);
		if(value instanceof Float) {
			label.setText(DefaultCurrencyFormat.formatAsHTML((Float)value, currency));
		}
		return label;
	}

	
	
}
