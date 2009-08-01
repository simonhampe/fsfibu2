package fs.fibu2.print;

import java.awt.Graphics;

/**
 * This interface represents a 'building block' in printed document consisting of lines. A LinePrintUnit consists of several lines, the number of
 * which it provides together with a method to print them at a certain vertical offset.
 * @author Simon Hampe
 *
 */
public interface LinePrintUnit {

	/**
	 * @return The number of lines this unit contains
	 */
	public int getNumberOfLines();
	
	/**
	 * Prints this unit
	 * @param g The graphics context on which to print
	 * @param firstLinePosition The position of the first line of this unit in the given context, counted in lines as well. The count starts at 0.
	 * @param lineHeight The height of one line
	 */
	public void print(Graphics g, int firstLinePosition, double lineHeight);	
}
