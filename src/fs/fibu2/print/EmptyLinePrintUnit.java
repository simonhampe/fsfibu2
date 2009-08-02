package fs.fibu2.print;

import java.awt.Graphics;

/**
 * Prints a certain number of empty lines (i.e. actually paints nothing at all)
 * @author Simon Hampe
 *
 */
public class EmptyLinePrintUnit implements LinePrintUnit {

	int lineNumber = 1;
	
	/**
	 * Creates a unit
	 * @param numberOfLines The number of empty lines to 'paint'. If smaller than 1, the value is set to 1.
	 */
	public EmptyLinePrintUnit(int numberOfLines) {
		lineNumber = numberOfLines > 0? numberOfLines : 1;
	}
	
	@Override
	public int getNumberOfLines() {
		return lineNumber;
	}

	@Override
	public void print(Graphics g, int firstLinePosition, double lineHeight,
			int startLine, int endLine) {
		//Paint nothing
	}

}
