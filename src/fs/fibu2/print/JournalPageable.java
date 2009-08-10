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

import org.w3c.dom.css.Rect;

import fs.fibu2.lang.Fsfibu2StringTableMgr;
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
	private int integerLineHeight;
	
	private Vector<Printable> printables = new Vector<Printable>();
	
	private Vector<Integer> columnWidths = new Vector<Integer>();
	
	//Drawing constants, already rounded to int
	private int leftBorder;
	private int upperBorder;
	private int width;
	private int height;
	
	private final static String sgroup = "fs.fibu2.print.JournalPageable";
	
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
	public JournalPageable(String title, String subtitle, JournalTableModel model, PageFormat format, int lineHeight) throws PrintException {
		
		//Copy data
		this.title = title;
		this.subtitle = subtitle;
		this.model = model;
		this.format = format;
		this.integerLineHeight = lineHeight;
		
		leftBorder = (int)format.getImageableX() + 1;
		upperBorder = (int) format.getImageableY() + 1;
		width = (int)format.getImageableWidth() - 2;
		height = (int) format.getImageableHeight() -2;
		
		//Distribute column widths
		double totalPageWidth = (double)width;
		columnWidths.add((int)(totalPageWidth * 0.25)); //Name
		columnWidths.add((int)(totalPageWidth * 0.1)); //Date
		columnWidths.add((int)(totalPageWidth * 0.1)); //Value
		columnWidths.add((int)(totalPageWidth * 0.1)); //Account
		columnWidths.add((int)(totalPageWidth * 0.25)); //Category
		columnWidths.add((int)(totalPageWidth * 0.15)); //AccountInformation
		columnWidths.add((int)(totalPageWidth * 0.05)); //Remark
		int sumOfWidths = 0;
		for(Integer i : columnWidths) sumOfWidths += i;
		columnWidths.set(0, columnWidths.get(0) + (width - sumOfWidths));
		
		
		//Create units
		//TODO: Debug
		printables.add(new Printable() {

			@Override
			public int print(Graphics graphics, PageFormat pageFormat,
					int pageIndex) throws PrinterException {
				//(new TitleUnit()).print(graphics, 0, JournalPageable.this.lineHeight, 0, 4);
				//(new SubTitleUnit()).print(graphics, 0, JournalPageable.this.lineHeight, 0, 0);
				(new PageNumberUnit(3)).print(graphics, 0, JournalPageable.this.integerLineHeight, 0, 0);
				//.print(graphics, 0, JournalPageable.this.lineHeight, 0, 0);
//				(new TextLineUnit("Auf dem Kontoauszug ist die Überweisung nicht explizit aufgeführt, da das Geld sowohl für den Putztag als auch für die E-Wochen fälschlicherweise an die Maschbauer überwiesen wurden. Die haben dann beide Beträge zusammen überwiesen.",4)).print(graphics, 0, JournalPageable.this.integerLineHeight, 0, 0);	
				(new TableHeaderUnit()).print(graphics, 1, integerLineHeight, 0, 0);
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
			int ypos = (firstLinePosition + 1) * integerLineHeight;
			Rectangle rect = new Rectangle(leftBorder,ypos + upperBorder, width,2*integerLineHeight);
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
			Rectangle rect = new Rectangle(leftBorder,upperBorder + firstLinePosition*integerLineHeight, 
					width ,integerLineHeight);
			//Print
			PrintHelper.printString(g, subtitle, rect, Color.BLACK, XAlign.CENTER, YALign.BOTTOM, true);
		}
		
	}
	
	/**
	 * Prints a page number in the format 'x / total' at the right border
	 * @author Simon Hampe
	 *
	 */
	private class PageNumberUnit implements LinePrintUnit {

		private int page;
		
		/**
		 * Prints 'page / total'
		 */
		public PageNumberUnit(int page) {
			this.page = page;
		}
		
		@Override
		public int getNumberOfLines() {
			return 1;
		}

		@Override
		public void print(Graphics g, int firstLinePosition, double lineHeight,
				int startLine, int endLine) {
			Rectangle rect = new Rectangle(leftBorder,upperBorder + firstLinePosition*integerLineHeight, 
					width,integerLineHeight - 2);
			PrintHelper.printString(g, Integer.toString(page) + " / " + Integer.toString(printables.size()), rect, Color.BLACK, XAlign.RIGHT, YALign.BOTTOM, true);
		}
		
	}
	
	/**
	 * This prints a left-aligned text over several lines, where each line is maximally 100 characters long (at font size 10, approx. 120 are possible).
	 * Line breaks are only done at blanks (if no blank is left, the text is simply scaled down). Optionally, a number can be specified. In this case, 
	 * the text is printed in the form Number) text, where the text's left border is aligned to the right border of 'number) '  
	 * @author Simon Hampe
	 *
	 */
	private class TextLineUnit implements LinePrintUnit {

		private final static int charPerLine = 100;
		
		//The text lines
		private Vector<String> lines = new Vector<String>();
		//The optional number text
		private String numberString = null;
		//The index of the longest string in lines
		private int longestIndex = 0;
		
		
		/**
		 * Creates a unit. If number is null, the text is just printed left-aligned.
		 */
		public TextLineUnit(String txt, Integer number) {
			numberString = number == null? null : number.toString() + ") ";
			//Divide text
			String remainingText = txt == null? "" : txt;
			remainingText = remainingText.trim();
			int maxLength = 0;
			while(remainingText.length() > 0) {
				String nextString = remainingText;
				while(nextString.length() > charPerLine && nextString.lastIndexOf(" ") != -1) {
					int index = nextString.lastIndexOf(" ");
					nextString = nextString.substring(0, index);
				}
				lines.add(nextString);
				if(nextString.length() > maxLength) {
					maxLength = nextString.length();
					longestIndex = lines.size() - 1;
				}
				if(nextString.length() == remainingText.length()) remainingText = "";
				else remainingText = remainingText.substring(nextString.length(), remainingText.length()).trim();
			}
		}
		
		@Override
		public int getNumberOfLines() {
			return lines.size();
		}

		@Override
		public void print(Graphics g, int firstLinePosition, double lineHeight,
				int startLine, int endLine) {
			//If necessary, first draw the number
			int xOffset = 0;
			if(numberString != null) {
				Rectangle rect = new Rectangle(leftBorder,upperBorder + firstLinePosition*integerLineHeight, 
						width,integerLineHeight);
				PrintHelper.printString(g, numberString, rect, Color.BLACK, XAlign.LEFT, YALign.BOTTOM, true);
				xOffset = (int)g.getFontMetrics().getStringBounds(numberString, g).getWidth()+1;
			}
			//Now draw text
			Rectangle measureRect = new Rectangle(leftBorder + xOffset, upperBorder + firstLinePosition*integerLineHeight,
					width - xOffset, integerLineHeight);
			float size = PrintHelper.maximalFontSize(g, lines.get(longestIndex), measureRect);
			g.setFont(g.getFont().deriveFont(size));
			for(int i = 0; i < lines.size(); i++) {
				Rectangle rect = new Rectangle(leftBorder + xOffset, upperBorder + (firstLinePosition + i)*integerLineHeight,
						width - xOffset, integerLineHeight);
				PrintHelper.printString(g, lines.get(i), rect, Color.BLACK, XAlign.LEFT, YALign.BOTTOM, false);
			}
		}
	}
	
	/**
	 * Prints the table header
	 * @author Simon Hampe
	 *
	 */
	private class TableHeaderUnit implements LinePrintUnit {

		@Override
		public int getNumberOfLines() {
			return 1;
		}

		@Override
		public void print(Graphics g, int firstLinePosition, double lineHeight,
				int startLine, int endLine) {
				int xOffset = 0;
				for(int i = 0; i < columnWidths.size(); i++) {
					Rectangle tableRect = new Rectangle(leftBorder + xOffset, upperBorder + firstLinePosition*integerLineHeight - 1,
							columnWidths.get(i), integerLineHeight + 1);
					g.setColor(new Color(200,200,200));
					g.fillRect(tableRect.x, tableRect.y, tableRect.width, tableRect.height);
					g.setColor(Color.BLACK);
					g.drawRect(tableRect.x, tableRect.y, tableRect.width, tableRect.height);
					xOffset += tableRect.width;
					Rectangle textRext = new Rectangle(tableRect.x + 1, tableRect.y + 1, tableRect.width - 2, tableRect.height - 2);
					String headerText = "";
					switch(i) {
					case 0: headerText = Fsfibu2StringTableMgr.getString(sgroup + ".name"); break;
					case 1: headerText = Fsfibu2StringTableMgr.getString(sgroup + ".date"); break;
					case 2: headerText = Fsfibu2StringTableMgr.getString(sgroup + ".value"); break;
					case 3: headerText = Fsfibu2StringTableMgr.getString(sgroup + ".account"); break;
					case 4: headerText = Fsfibu2StringTableMgr.getString(sgroup + ".category"); break;
					case 5: headerText = Fsfibu2StringTableMgr.getString(sgroup + ".accinf"); break;
					case 6: headerText = Fsfibu2StringTableMgr.getString(sgroup + ".remark"); break;
					}
					PrintHelper.printString(g, headerText, textRext, Color.BLACK, XAlign.LEFT, YALign.BOTTOM, true);
				}
		}
		
	}

}
