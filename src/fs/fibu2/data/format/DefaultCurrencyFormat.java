package fs.fibu2.data.format;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;

/**
 * Provides a creation method for a {@link NumberFormat}, which formats numbers to two fraction digits with a locale-specific separator and a 
 * given currency symbol.
 * @author Simon Hampe
 *
 */
public class DefaultCurrencyFormat {
	
	public static NumberFormat getFormat(Currency currency) {
		NumberFormat format = NumberFormat.getCurrencyInstance();
			format.setCurrency(currency);
			format.setMinimumFractionDigits(2);
			format.setMaximumFractionDigits(2);
		return format;
	}
	
	/**
	 * Formats values as the format obtained by {@link #getFormat(Currency)}, but additionally puts it
	 * in a html bracket which renders the text red, if the value is < 0
	 */
	public static String formatAsHTML(float value, Currency currency) {
		NumberFormat nf = getFormat(currency);
		String result = "<html><font color=\"" + (value >= 0? "000000" : "FF0000")   
						+ "\">" + nf.format(value) + "</font></html>";
		return result;
	}
	
	/**
	 * Simply calls on {@link #formatAsHTML(float, Currency)} by using value.floatValue()
	 */
	public static String formatAsHTML(BigDecimal value, Currency currency) {
		return formatAsHTML(value.floatValue(), currency);
	}
	
	/**
	 * @return the same format as {@link #getFormat(Currency)}, but without the currency symbol
	 */
	public static NumberFormat getFormat() {
		NumberFormat format = NumberFormat.getInstance();
			format.setMinimumFractionDigits(2);
			format.setMaximumFractionDigits(2);
		return format;
	}
	
}
