package fs.fibu2.print;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.print.PrintException;

import fs.fibu2.data.Fsfibu2Constants;
import fs.fibu2.data.format.DefaultCurrencyFormat;
import fs.fibu2.data.format.Fsfibu2DateFormats;
import fs.fibu2.data.model.Account;
import fs.fibu2.data.model.Entry;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.print.PrintHelper.XAlign;
import fs.fibu2.print.PrintHelper.YALign;
import fs.fibu2.view.model.JournalTableModel;

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
	
	private PageFormat format;
	private int integerLineHeight;
	
	private Vector<Printable> printables = new Vector<Printable>();
	private Vector<LinePrintUnit> remarkUnits = new Vector<LinePrintUnit>();
	
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
	 * Creates a new Pageable which prints the contents of a {@link JournalTableModel} (but without reading points).
	 * @param title The title which is printed on each page. If null, no title is printed
	 * @param subtitle The subtitle which is printed on each page. If null, no title is printed
	 * @param model The model from which the content is taken
	 * @param format The page format used for printing
	 * @param lineHeight The height of one line in points
	 * @param printCommentary Whether the explanatory commentary should be printed as well
	 * @throws PrintException - If any of the parameters is invalid and the journal cannot be printed
	 */
	public JournalPageable(String title, String subtitle, JournalTableModel model, PageFormat format, int lineHeight, boolean printCommentary) throws PrintException {
		
		//Copy data
		this.title = title;
		this.subtitle = subtitle;
		this.format = format;
		this.integerLineHeight = lineHeight;
		
		//Consistency check
		if(model == null) throw new PrintException("Cannot print journal: No model is given");
		if(format == null) throw new PrintException("Cannot print journal: No format is given");
		
		leftBorder = (int)format.getImageableX() + 1;
		upperBorder = (int) format.getImageableY() + 1;
		width = (int)format.getImageableWidth() - 2;
		height = (int) format.getImageableHeight() -2;
		
		//Consistency check: At least n lines per page
		int n = 9 - (title == null? 4 : 0) - (subtitle == null? 1 : 0);
		int linesPerPage = height / integerLineHeight; 
		if(linesPerPage < n) throw new PrintException("Cannot print journal. Page is too small for the chosen format");
		
		
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
		
		//Extract content from model
		
		//Extract all possible account field names and sort them
		HashSet<Account> accounts = model.getAssociatedJournal().getListOfAccounts();
		Vector<String> fieldIDs = new Vector<String>();
		Vector<String> fieldNames = new Vector<String>();
		for(Account a : accounts) {
			for(String id : a.getFieldIDs()) {
				if(!fieldIDs.contains(id)) {
					TreeSet<String> sorted = new TreeSet<String>(fieldIDs);
					sorted.add(id);
					fieldIDs = new Vector<String>(sorted);
					fieldNames.add(fieldIDs.indexOf(id), a.getFieldNames().get(id));
				}
			}
		}
		
		Vector<LinePrintUnit> tableRows = new Vector<LinePrintUnit>();
		int remark = 1;
		
		for(int i = 0; i < model.getRowCount(); i++) {
			Object o = model.getValueAt(i, 0);
			if(o instanceof Entry) {
				Entry e = (Entry)o;
				Vector<String> content = new Vector<String>();
				content.add(e.getName());
				content.add(Fsfibu2DateFormats.getEntryDateFormat().format(e.getDate().getTime()));
				content.add(DefaultCurrencyFormat.getFormat(Fsfibu2Constants.defaultCurrency).format(e.getValue()));
				content.add(e.getAccount().getName());
				content.add(e.getCategory().toString());
				content.add(insertAccountFields(fieldIDs, e));
				if(e.getAdditionalInformation().length() > 0) {
					content.add(Integer.toString(remark) + ")");
					remarkUnits.add(new TextLineUnit(e.getAdditionalInformation(),remark));
					remark++;
				}
				boolean thirdRowRed = e.getValue() < 0;
				tableRows.add(new TableRowUnit(content, thirdRowRed));
			}
		}
		
		
		//First create pages containing the table (at least one)
		int rowIndex = 0;
		int headerLines = (title == null? 0 : 4) + (subtitle == null? 0 : 1) + 2; //2 = page number + table header
		int remainingLines = linesPerPage - headerLines;
		do {
			//Calculate index of first row not printed
			int lastIndex = remainingLines < tableRows.size() - rowIndex? rowIndex + remainingLines  : tableRows.size();
			//Create page
			int page = printables.size() + 1;
			printables.add(new TablePagePrinter(new Vector<LinePrintUnit>(tableRows.subList(rowIndex, lastIndex)),page));
			//Calculate index of first row printed on next page
			rowIndex = lastIndex;
		}
		while(rowIndex < tableRows.size());
		
		//Now create commentary pages (if necessary)
		if(!printCommentary) return;
		
		StringBuilder accinfString = new StringBuilder();
			accinfString.append(Fsfibu2StringTableMgr.getString(sgroup + ".accinf"));
			accinfString.append(" = ");
			for(int i = 0; i < fieldNames.size(); i++) {
				accinfString.append(fieldNames.get(i));
				if(i != fieldNames.size()-1) accinfString.append(" / ");
			}
		remarkUnits.add(0,new TextLineUnit(accinfString.toString(),null));
		
		//Distribute line units
		
		int linesToPrint = 0;
		for(LinePrintUnit u : remarkUnits) linesToPrint += u.getNumberOfLines();
		
		int startLine = 0;
		int startUnit = 0;
		int endLine = 0;
		int endUnit = 0;
		while(linesToPrint > 0) {
			//Create new printable:
			int linesLeftOnPage = linesPerPage -  (title == null? 0 : 4) - (subtitle == null? 0 : 1);
			//The current unit
			int currentUnit = startUnit;
			//the current line in the current unit
			int currentLine = startLine;
			
			//Add as much as possible
			
			//Add units
			while(linesLeftOnPage > 0 && linesToPrint > 0) {
				//If there is enough space for the unit, insert it completely
				int unitLinesToPrint = remarkUnits.get(currentUnit).getNumberOfLines() - currentLine;  
				if(unitLinesToPrint <= linesLeftOnPage) {
					endUnit = currentUnit;
					endLine = remarkUnits.get(currentUnit).getNumberOfLines() -1;
					linesLeftOnPage -= unitLinesToPrint;
					linesToPrint -= unitLinesToPrint;
					currentLine = 0;
					currentUnit ++;
				}
				//If we do not have enough space, the behavior depends on the chosen policy
				else {
					if(unitLinesToPrint <= linesPerPage)  {
						linesLeftOnPage = 0;
					}
					else {
						endUnit = currentUnit;
						endLine = currentLine + linesLeftOnPage;
						linesToPrint -= (linesLeftOnPage+1);
						linesLeftOnPage = 0;
					}
				}
			}
			
			//Add it
			int page = printables.size() + 1;
			if(endUnit > startUnit || endLine > startLine) printables.add(new CommentPagePrinter(startUnit,endUnit,startLine,endLine,page));
			//Adapt indices
			if(endLine == remarkUnits.get(endUnit).getNumberOfLines() -1) {
				startLine = 0;
				startUnit = endUnit+1;
			}
			else {
				startLine = endLine + 1;
				startUnit = endUnit;
			}
		}
		
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
	
	// HELPER METHODS ******************
	// *********************************
	
	/**
	 * Inserts the account information of e into a tupel of the form ".. /... / ..", where the number of these entries
	 * is determined by ids. The value of each account field of e, whose id coincides with one in ids is put in the appropriate place 
	 */
	private String insertAccountFields(Vector<String> ids, Entry e) {
		StringBuilder b = new StringBuilder();
		
		for(int i = 0; i < ids.size(); i++) {
			if(e.getAccountInformation().containsKey(ids.get(i))) {
				b.append(e.getAccountInformation().get(ids.get(i)));
			}
			if(i != ids.size()-1) b.append(" / ");
		}
		
		return b.toString();
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
	 * This prints a left-aligned text over several lines, where each line is maximally 100 characters long (at font size 10 on DIN A4 landscape, 
	 * approx. 120 are possible).
	 * Line breaks are only done at blanks (if no blank is left, the text is simply scaled down). Optionally, a number can be specified. In this case, 
	 * the text is printed in the form Number) text, where the text's left border is aligned to the right border of 'number) '  
	 * @author Simon Hampe
	 *
	 */
	private class TextLineUnit implements LinePrintUnit {

		private final static int charPerLine = 120;
		
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
			for(int i = startLine; i < lines.size() && i <= endLine; i++) {
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
							columnWidths.get(i), integerLineHeight);
					g.setColor(new Color(200,200,200));
					g.fillRect(tableRect.x, tableRect.y, tableRect.width, tableRect.height);
					g.setColor(Color.BLACK);
					g.drawRect(tableRect.x, tableRect.y, tableRect.width, tableRect.height);
					xOffset += tableRect.width;
					Rectangle textRext = new Rectangle(tableRect.x + 1, tableRect.y, tableRect.width - 2, tableRect.height - 2);
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
					PrintHelper.printString(g, headerText, textRext, Color.BLACK, XAlign.LEFT, YALign.CENTER, true);
				}
		}
		
	}
	
	/**
	 * Prints a table row, filled with given values
	 * @author Simon Hampe
	 *
	 */
	private class TableRowUnit implements LinePrintUnit {

		private Vector<String> content = new Vector<String>();
		private boolean thirdRowRed = false;
		
		/**
		 * Constructs a table row filled with the given content in the given order. Missing strings are replaced by the empty string, 
		 * a surplus of strings is ignored. The second parameter specifies, whether the third row is painted in red font
		 */
		public TableRowUnit(Vector<String> content, boolean thirdRowRed) {
			if(content != null) this.content = content;
			this.thirdRowRed = thirdRowRed;
		}
		
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
						columnWidths.get(i), integerLineHeight );
				g.setColor(Color.BLACK);
				g.drawRect(tableRect.x, tableRect.y, tableRect.width, tableRect.height);
				xOffset += tableRect.width;
				Rectangle textRext = new Rectangle(tableRect.x + 1, tableRect.y, tableRect.width - 2, tableRect.height-2);
				String headerText = i < content.size()? content.get(i) : "";
				XAlign alignment = (i == 2 || i == 6)? XAlign.RIGHT : XAlign.LEFT;
				PrintHelper.printString(g, headerText, textRext, (i == 2 && thirdRowRed)? Color.RED : Color.BLACK, alignment, YALign.CENTER, true);
			}
		}
		
	}
	
	// PRINTER CLASSES ***********************************
	// ***************************************************
	
	/**
	 * Prints a page containing a header and a certain set of rows
	 */
	private class TablePagePrinter implements Printable {

		private Vector<LinePrintUnit> units = new Vector<LinePrintUnit>();
		
		/**
		 * Creates a page printer, which prints a header and the given rows with the given page index
		 */
		public TablePagePrinter(Vector<LinePrintUnit> tableRows, int page) {
			if(title != null) {
				//units.add(new EmptyLinePrintUnit(1));
				units.add(new TitleUnit());
				//units.add(new EmptyLinePrintUnit(1));
			}
			if(subtitle != null) units.add(new SubTitleUnit());
			units.add(new PageNumberUnit(page));
			units.add(new TableHeaderUnit());
			if(tableRows != null) units.addAll(tableRows);		
		}
		
		@Override
		public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
				throws PrinterException {
			int linePos = 0;
			for(LinePrintUnit u : units) {
				u.print(graphics, linePos, integerLineHeight, 0, u.getNumberOfLines()-1);
				linePos += u.getNumberOfLines();
			}
			return Printable.PAGE_EXISTS;
		}
		
	}

	/**
	 * Prints a certain subset of the remark line units
	 * @author Simon Hampe
	 *
	 */
	private class CommentPagePrinter implements Printable {

		private int firstUnitToPrint = 0;
		private int lastUnitToPrint = 0;
		private int firstLineToPrint = 0;
		private int lastLineToPrint = 0;
		
		private Vector<LinePrintUnit> defaultUnits = new Vector<LinePrintUnit>();
		
		/**
		 * Creates a printer which prints a subset of the remark line units		
		 * @param firstUnitToPrint The index of the unit in which to start printing
		 * @param lastUnitToPrint The index of the unit in which to end printing
		 * @param firstLineToPrint The first line to be printed of the first unit
		 * @param lastLineToPrint The last line to be printed of the last unit
		 * @param The page index to display
		 */
		public CommentPagePrinter(int firstUnitToPrint, int lastUnitToPrint,
				int firstLineToPrint, int lastLineToPrint, int page) {
			this.firstUnitToPrint = firstUnitToPrint;
			this.lastUnitToPrint = lastUnitToPrint;
			this.firstLineToPrint = firstLineToPrint;
			this.lastLineToPrint = lastLineToPrint;
			
			if(title != null) defaultUnits.add(new TitleUnit());
			if(subtitle != null) defaultUnits.add(new SubTitleUnit());
			defaultUnits.add(new PageNumberUnit(page));
			defaultUnits.add(new TextLineUnit(Fsfibu2StringTableMgr.getString(sgroup + ".commentary"),null));
			defaultUnits.add(new EmptyLinePrintUnit(1));
		}



		@Override
		public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
				throws PrinterException {
			int linePos = 0;
			for(LinePrintUnit u : defaultUnits) {
				u.print(graphics, linePos, integerLineHeight, 0, u.getNumberOfLines()-1);
				linePos += u.getNumberOfLines();
			}
			for(int i = firstUnitToPrint; i <= lastUnitToPrint; i ++) {
				int startLine = i == firstUnitToPrint? firstLineToPrint : 0;
				int endLine = i == lastUnitToPrint? lastLineToPrint : remarkUnits.get(i).getNumberOfLines() -1;
				remarkUnits.get(i).print(graphics, linePos, integerLineHeight, startLine, endLine);
				linePos += (endLine - startLine + 1);
			}
			return Printable.PAGE_EXISTS;
		}
		
	}
	
}
