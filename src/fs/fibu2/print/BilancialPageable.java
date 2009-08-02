package fs.fibu2.print;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Vector;

import fs.fibu2.data.Fsfibu2Constants;
import fs.fibu2.data.format.DefaultCurrencyFormat;
import fs.fibu2.data.model.Category;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.module.BilancialPane;
import fs.fibu2.print.PrintHelper.XAlign;
import fs.fibu2.print.PrintHelper.YALign;
import fs.fibu2.view.model.BilancialTreeModel.ExtendedCategory;

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
		//TODO: Test code
		printables.add(new Printable(){
		
			@Override
			public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
					throws PrinterException {
//				(new NodeTitleUnit(new ExtendedCategory(Category.getCategory(Category.getRootCategory(), "Fachschaft"),false),0)).print(graphics, 0, configuration.getLineHeight(), 0, 0);
//				(new TitleUnit()).print(graphics, 0, configuration.getLineHeight(), 0, 4);
				(new NodeSumUnit(new ExtendedCategory(Category.getCategory(Category.getRootCategory(), "Fachschaft"),false),50,true)).print(graphics, 0, configuration.getLineHeight(), 0, 0);
				return Printable.PAGE_EXISTS;
			}
		});
		
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
	
	// PRINT UNITS ***************************************
	// ***************************************************
	
	/**
	 * Prints the title of the page: One empty line, two lines for the title, one empty line. This unit can only be printed in one piece, 
	 * so the last two parameters of the print method are ignored
	 * @author Simon Hampe
	 *
	 */
	private class TitleUnit implements LinePrintUnit {

		@Override
		public int getNumberOfLines() {
			return 5;
		}

		@Override
		public void print(Graphics g, int firstLinePosition, double lineHeight, int startLine, int endLine) {
			double fontHeight = 2*lineHeight;
			g.setFont(g.getFont().deriveFont((float)fontHeight));
			g.setColor(Color.BLACK);
			double xpos = configuration.getFormat().getImageableX() + (configuration.getFormat().getImageableWidth() - g.getFontMetrics().getStringBounds(configuration.getTitle(), g).getWidth())/2;
			double ypos = configuration.getFormat().getImageableY() + ((double)(firstLinePosition + 3))*lineHeight;
			g.drawString(configuration.getTitle(), (int)xpos, (int)ypos);
		}
	}
	
	/**
	 * Creates the first line of a node bilancial, i.e. prints the category tail and a line underneath it.  
	 * @author Simon Hampe
	 *
	 */
	private class NodeTitleUnit implements LinePrintUnit {

		String tail = "";
		double offset = 0;
		
		/**
		 * Creates a title unit
		 * @param ec the category to print
		 * @param horizontalOffset The offset to the right
		 */
		public NodeTitleUnit(ExtendedCategory ec, double horizontalOffset) {
			tail = ec.category().tail;
			offset = horizontalOffset;
		}
		
		@Override
		public int getNumberOfLines() {
			return 1;
		}

		@Override
		public void print(Graphics g, int firstLinePosition, double lineHeight,
				int startLine, int endLine) {
			g.setColor(Color.BLACK);
			float fontHeight = (float)lineHeight-1;
			double maxWidth = configuration.getFormat().getImageableWidth()/2 - offset;
			g.setFont(g.getFont().deriveFont(fontHeight));
			while(g.getFontMetrics().getStringBounds(tail, g).getWidth() > maxWidth && fontHeight > 1) {
				fontHeight--;
				g.setFont(g.getFont().deriveFont(fontHeight));
			}
			double xpos = configuration.getFormat().getImageableX() + offset;
			double ypos = configuration.getFormat().getImageableY() + lineHeight*((double)(firstLinePosition+1)) -1;
			System.out.println("x: " + xpos + ", y: " + ypos);
			g.drawString(tail, (int)xpos, (int)ypos);
			g.drawLine((int)xpos, (int)ypos+1, (int)(configuration.getFormat().getImageableX() + configuration.getFormat().getImageableWidth()),(int)ypos+1);
		}
		
	}
	
	/**
	 * This prints the last line of a node bilancial, i.e. the final bilancial, with (optionally) a line above
	 * @author Simon Hampe
	 *
	 */
	private class NodeSumUnit implements LinePrintUnit {

		private String tail = "";
		private String in = "";
		private boolean inNegative = false;
		private String out = "";
		private boolean outNegative = false;
		private String sum = "";
		private boolean sumNegative = false;
		private double offset = 0;
		
		private boolean isSumNode = false;
		
		/**
		 * Creates a new unit
		 * @param ec The extended category for which this should be drawn
		 * @param levelOffset The horizontal offset to the right
		 */
		public NodeSumUnit(ExtendedCategory ec, double levelOffset, boolean isSum) {
			tail = isSum? Fsfibu2StringTableMgr.getString("fs.fibu2.print.BilancialPageable.sum") + " (" + ec.category().tail + "):" : 
					ec.category().tail;
			float fIn = configuration.getModel().getCategoryPlus(ec.category());
			float fOut = configuration.getModel().getCategoryMinus(ec.category());
			float fSum = configuration.getModel().getCategorySum(ec.category());
			inNegative = fIn < 0;
			outNegative = fOut < 0;
			sumNegative = fSum < 0;
			in = DefaultCurrencyFormat.getFormat(Fsfibu2Constants.defaultCurrency).format(fIn);
			out = DefaultCurrencyFormat.getFormat(Fsfibu2Constants.defaultCurrency).format(fOut);
			sum = DefaultCurrencyFormat.getFormat(Fsfibu2Constants.defaultCurrency).format(fSum);
			offset = levelOffset;
			isSumNode = isSum;
		}
		
		@Override
		public int getNumberOfLines() {
			return 1;
		}

		@Override
		public void print(Graphics g, int firstLinePosition, double lineHeight,
				int startLine, int endLine) {
			g.setColor(Color.BLACK);
			//Determine rectangles:
			double leftwidth = configuration.getFormat().getImageableWidth() / 2 - offset;
			double rightwidth = configuration.getFormat().getImageableWidth() / 6;
			int ypos = (int)(configuration.getFormat().getImageableY() + lineHeight*((double)(firstLinePosition+1))+1);
			Rectangle tailRect = new Rectangle((int)(configuration.getFormat().getImageableX() + offset), ypos ,
												(int)leftwidth, (int)lineHeight-1);
			Rectangle inRect = new Rectangle((int) (configuration.getFormat().getImageableX() + configuration.getFormat().getImageableWidth() - 3*rightwidth),
											ypos,
											(int)rightwidth, (int)lineHeight-1);
			Rectangle outRect = new Rectangle((int) (configuration.getFormat().getImageableX() + configuration.getFormat().getImageableWidth() - 2*rightwidth),
											ypos,
											(int)rightwidth, (int)lineHeight-1);
			Rectangle sumRect = new Rectangle((int) (configuration.getFormat().getImageableX() + configuration.getFormat().getImageableWidth() - rightwidth),
					ypos,
					(int)rightwidth, (int)lineHeight-1);
			//Print
			PrintHelper.printString(g, tail, tailRect, Color.BLACK, XAlign.LEFT, YALign.BOTTOM, true);
			PrintHelper.printString(g, in, inRect, inNegative? Color.RED : Color.BLACK, XAlign.RIGHT, YALign.BOTTOM, true);
			PrintHelper.printString(g, out, outRect, outNegative? Color.RED : Color.BLACK, XAlign.RIGHT, YALign.BOTTOM, true);
			PrintHelper.printString(g, sum, sumRect, sumNegative? Color.RED : Color.BLACK, XAlign.RIGHT, YALign.BOTTOM, true);
			if(isSumNode) g.drawLine(tailRect.x, tailRect.y-1, (int)(configuration.getFormat().getImageableX() + configuration.getFormat().getImageableWidth()), tailRect.y-1);	
		}	
	}
	
	
	

}
