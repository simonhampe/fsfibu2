package fs.fibu2.print;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Vector;

import javax.print.PrintException;

import fs.fibu2.print.PrintHelper.XAlign;
import fs.fibu2.print.PrintHelper.YALign;
import fs.fibu2.view.model.JournalTableModel;
import fs.fibu2.view.render.JournalTable;

/**
 * Creates a pageable which (partially) prints the content of a {@link JournalTableModel}.
 * @author Simon Hampe
 *
 */
public class JournalPageable implements Pageable {

	// DATA *********************
	// **************************
	
	private String title;
	private String subtitle;
	
	private JournalTableModel model;
	
	private PageFormat format;
	private double lineHeight;
	
	private Vector<Printable> printables = new Vector<Printable>();
	
	// CONSTRUCTOR **************
	// **************************
	
	/**
	 * Creates a new Pageable which prints the contents of a {@link JournalTableModel}.
	 * @param title The title which is printed on each page. If null, no title is printed
	 * @param subtitle The subtitle which is printed on each page. If null, no title is printed
	 * @param model The model from which the content is taken
	 * @param format The page format used for printing
	 * @param lineHeight The height of one line in points
	 * @throws PrintException - If any of the parameters is invalid and the journal cannot be printed
	 */
	public JournalPageable(String title, String subtitle, JournalTableModel model, PageFormat format, double lineHeight) throws PrintException {
		
		//Copy data
		this.title = title;
		this.subtitle = subtitle;
		this.model = model;
		this.format = format;
		this.lineHeight = lineHeight;
		
		//Create units
		//TODO: Debug
		printables.add(new Printable() {

			@Override
			public int print(Graphics graphics, PageFormat pageFormat,
					int pageIndex) throws PrinterException {
				//(new TitleUnit()).print(graphics, 0, JournalPageable.this.lineHeight, 0, 4);
				//(new SubTitleUnit()).print(graphics, 0, JournalPageable.this.lineHeight, 0, 0);
				//(new PageNumberUnit(3,14)).print(graphics, 0, JournalPageable.this.lineHeight, 0, 0);
				(new TextLineUnit("Auf dem Kontoauszug ist die Überweisung nicht explizit aufgeführt, da das Geld sowohl für den Putztag als auch für die E-Wochen fälschlicherweise an die Maschbauer überwiesen wurden. Die haben dann beide Beträge zusammen überwiesen."))
					.print(graphics, 0, JournalPageable.this.lineHeight, 0, 0);
				return Printable.PAGE_EXISTS;
			}
			
		});
	}
	
	// PAGEABLE *****************
	// **************************
	
	@Override
	public int getNumberOfPages() {
		return printables.size();
	}

	@Override
	public PageFormat getPageFormat(int pageIndex)
			throws IndexOutOfBoundsException {
		return format;
	}

	@Override
	public Printable getPrintable(int pageIndex)
			throws IndexOutOfBoundsException {
		return printables.get(pageIndex);
	}
	
	// PRINT UNITS *********************
	// *********************************
	
	/**
	 * Prints the title over 2 lines with a line above and below. Can only be printed in one piece
	 * @author Simon Hampe 
	 */
	private class TitleUnit implements LinePrintUnit {
		@Override
		public int getNumberOfLines() {
			return 4;
		}
		@Override
		public void print(Graphics g, int firstLinePosition, double lineHeight,
				int startLine, int endLine) {
			//Create rectangle
			double ypos = (firstLinePosition + 1) * lineHeight;
			Rectangle rect = new Rectangle((int)format.getImageableX(),(int)(ypos + format.getImageableY()), (int)format.getImageableWidth(),(int)(2*lineHeight));
			//Print
			PrintHelper.printString(g, title, rect, Color.BLACK, XAlign.CENTER, YALign.BOTTOM, true);
		}
	}
	
	/**
	 * Prints the subtitle over 1 line
	 * @author Simon Hampe
	 *
	 */
	private class SubTitleUnit implements LinePrintUnit {

		@Override
		public int getNumberOfLines() {
			return 1;
		}

		@Override
		public void print(Graphics g, int firstLinePosition, double lineHeight,
				int startLine, int endLine) {
			//Create rectangle
			Rectangle rect = new Rectangle((int)format.getImageableX(),(int)(format.getImageableY() + firstLinePosition*lineHeight), 
					(int)format.getImageableWidth(),(int)lineHeight);
			//Print
			PrintHelper.printString(g, subtitle, rect, Color.BLACK, XAlign.CENTER, YALign.BOTTOM, true);
		}
		
	}
	
	/**
	 * Prints a page number in the format 'x / y' at the right border
	 * @author Simon Hampe
	 *
	 */
	private class PageNumberUnit implements LinePrintUnit {

		private int page;
		private int total;
		
		/**
		 * Prints 'page / total'
		 */
		public PageNumberUnit(int page, int total) {
			this.page = page;
			this.total = total;
		}
		
		@Override
		public int getNumberOfLines() {
			return 1;
		}

		@Override
		public void print(Graphics g, int firstLinePosition, double lineHeight,
				int startLine, int endLine) {
			Rectangle rect = new Rectangle((int)format.getImageableX(),(int)(format.getImageableY() + firstLinePosition*lineHeight), 
					(int)format.getImageableWidth(),(int)lineHeight);
			PrintHelper.printString(g, Integer.toString(page) + " / " + Integer.toString(total), rect, Color.BLACK, XAlign.RIGHT, YALign.BOTTOM, true);
		}
		
	}
	
	/**
	 * Prints a left-aligned text over two lines (but the font only one line high). Line breaks are done on blanks. If there is no blank, the text is scaled down.
	 * Optionally, a number can be specified. Then the text will be printed as 'number) text', where the multi-line text is printed left-aligned 
	 * to the right border of the 'number)' text.
	 * @author Simon Hampe
	 *
	 */
	private class TextLineUnit implements LinePrintUnit {

		private String text = "";
		private String noText = "";
		
		private boolean oneLine;
		
		private int xOffset = 0;
		
		/**
		 * Creates a unit. If number is null, the text is just printed left-aligned.
		 * The last parameter indicates, whether one or two lines should be used
		 */
		public TextLineUnit(String txt, Integer number, boolean forceOneLine) {
			text = txt == null? "" : txt;
			oneLine = forceOneLine;
			//Init number text
			if(number != null) {
				noText = number.toString() + ") ";
			}			
		}
		
		@Override
		public int getNumberOfLines() {
			return oneLine? 1 : 2;
		}

		@Override
		public void print(Graphics g, int firstLinePosition, double lineHeight,
				int startLine, int endLine) {
			Rectangle rect1 = new Rectangle((int)format.getImageableX() + xOffset,(int)(format.getImageableY() + firstLinePosition*lineHeight), 
					(int)format.getImageableWidth(),(int)lineHeight);
			Rectangle rect2 = new Rectangle((int)format.getImageableX() + xOffset,(int)(format.getImageableY() + firstLinePosition*lineHeight), 
					(int)format.getImageableWidth(),(int)lineHeight);
			if(oneLine) {
				PrintHelper.printString(g, noText + text, rect1, Color.BLACK, XAlign.LEFT, YALign.BOTTOM, true);
			}
			else {
				g.setFont(g.getFont().deriveFont((float)lineHeight));
				//Determine first and second line
				String firstString = noText + text;
				String secondString;
				while(g.getFontMetrics().getStringBounds(firstString, g).getWidth() > rect1.getWidth() && firstString.lastIndexOf(" ") != -1) {
					secondString = firstString.substring(firstString.lastIndexOf(" ") + 1, firstString.length());
				}
			}
		}
		
	}
	

}
