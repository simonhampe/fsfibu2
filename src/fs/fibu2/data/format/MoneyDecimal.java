package fs.fibu2.data.format;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * This class contains convenience methods for creating and manipulating {@link BigDecimal} objects in monetary arithmetic. It creates, adds and substracts BigDecimals and returns the result
 * with a fraction digit scale of 2 (while doing the actual computation in infinite precision)
 * @author Simon Hampe
 *
 */
public class MoneyDecimal {

	/**
	 * The default math context for any monetary arithmetic: infinite precision and no rounding
	 */
	public final static MathContext curmc = new MathContext(0, RoundingMode.UNNECESSARY);
	
	/**
	 * The default rounding mode for monetary values: HALF_UP
	 */
	public final static RoundingMode currm = RoundingMode.HALF_UP;
	
	/**
	 * Creates a {@link BigDecimal} from a float value, rounding it to 2 decimal places
	 */
	public static BigDecimal bigd(float f) {
		BigDecimal d = new BigDecimal(f, curmc);
		return d.setScale(2,currm);
	}
	
	/**
	 * Computes the sum of a and b and returns the result rounded to two decimal places
	 */
	public static BigDecimal add(BigDecimal a, BigDecimal b) {
		return a.add(b,curmc).setScale(2,currm);
	}
	
	/**
	 * Computes a - b and returns the result rounded to two decimal places
	 */
	public static BigDecimal substract(BigDecimal a, BigDecimal b) {
		return a.subtract(b, curmc).setScale(2, currm);
	}
}
