package fs.fibu2.print;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Vector;

import fs.fibu2.module.BilancialPane;

/**
 * This class implements the actual printing of the data represented by a {@link BilancialPane}. It creates the necessary pages,, i.e. 
 * {@link Printable}s, from the given {@link BilancialPrintConfiguration}.
 * 
 * @author Simon Hampe
 *
 */
public class BilancialPageable implements Pageable {

	private BilancialPrintConfiguration configuration;
	
	private Vector<Printable> printables = new Vector<Printable>();
	
	public BilancialPageable(BilancialPrintConfiguration config) {
		configuration = config;
		//Consistency check
		
		//Create printables
	}
	
	@Override
	public int getNumberOfPages() {
		return printables.size();
	}

	@Override
	public PageFormat getPageFormat(int pageIndex)
			throws IndexOutOfBoundsException {
		return configuration.getFormat();
	}

	@Override
	public Printable getPrintable(int pageIndex)
			throws IndexOutOfBoundsException {
		return printables.get(pageIndex);
	}
	
	/**
	 * Prints the title of the page: One empty line, two lines for the title, one empty line
	 * @author Simon Hampe
	 *
	 */
	private class TitleUnit implements LinePrintUnit {

		@Override
		public int getNumberOfLines() {
			return 5;
		}

		@Override
		public void print(Graphics g, int firstLinePosition, double lineHeight) {
			double fontHeight = 2*lineHeight;
			g.setFont(g.getFont().deriveFont((float)fontHeight));
			g.setColor(Color.BLACK);
			double xpos = configuration.getFormat().getImageableWidth() - ((double)g.getFontMetrics().getStringBounds(configuration.getTitle(), g).getWidth())/2;
			double ypos = configuration.getFormat().getImageableY() + ((double)(firstLinePosition + 3))*lineHeight;
			g.drawString(configuration.getTitle(), (int)xpos, (int)ypos);
		}
		
	}
	
	

}
