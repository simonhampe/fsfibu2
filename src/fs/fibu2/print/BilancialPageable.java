package fs.fibu2.print;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.TreeSet;
import java.util.Vector;

import javax.sound.sampled.Line;

import fs.fibu2.data.Fsfibu2Constants;
import fs.fibu2.data.format.DefaultAccountComparator;
import fs.fibu2.data.format.DefaultCurrencyFormat;
import fs.fibu2.data.model.Account;
import fs.fibu2.data.model.AccountLoader;
import fs.fibu2.data.model.Category;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.module.BilancialPane;
import fs.fibu2.print.PrintHelper.XAlign;
import fs.fibu2.print.PrintHelper.YALign;
import fs.fibu2.view.model.AccountListModel;
import fs.fibu2.view.model.BilancialTreeModel.ExtendedCategory;

/**
 * This class implements the actual printing of the data represented by a {@link BilancialPane}. It creates the necessary pages,, i.e. 
 * {@link Printable}s, from the given {@link BilancialPrintConfiguration}.
 * 
 * @author Simon Hampe
 *
 */
public class BilancialPageable implements Pageable {

	// DATA ******************************
	// ***********************************
	
	private BilancialPrintConfiguration configuration;
	
	private Vector<LinePrintUnit> units = new Vector<LinePrintUnit>();
	private Vector<Printable> printables = new Vector<Printable>();
	
	// PRINTING CONSTANTS *****************
	// ************************************
	
	private double leftRatio = 0.3; //How much of the available width should be used for the left part (i.e. the node names)?
	private double offsetPerLevel = 10; //How much do we go to the right per level?
	
	// CONSTRUCTOR ************************
	// ************************************
	
	public BilancialPageable(BilancialPrintConfiguration config) {
		configuration = config;
		//TODO: Test code
		printables.add(new Printable(){
		
			@Override
			public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
					throws PrinterException {
//				(new NodeTitleUnit(new ExtendedCategory(Category.getCategory(Category.getRootCategory(), "Fachschaft"),false),0,null)).print(graphics, 0, configuration.getLineHeight(), 0, 0);
//				(new TitleUnit()).print(graphics, 0, configuration.getLineHeight(), 0, 4);
//				(new NodeSumUnit(new ExtendedCategory(Category.getRootCategory(),true),0,true)).print(graphics, 0, configuration.getLineHeight(), 0, 0);
//				(new NthLevelNodeUnit(new ExtendedCategory(Category.getCategory(Category.getRootCategory(), "Fachschaft"),false),10,true)).print(graphics, 0, configuration.getLineHeight(), 0, 30);
//				(new OverallSumUnit()).print(graphics, 0, configuration.getLineHeight(), 0, 0);
//				(new AccountSumUnit(AccountLoader.getAccount("bank_account"))).print(graphics, 0, configuration.getLineHeight(), 0, 0);
//				(new CaptionUnit(false)).print(graphics, 0, configuration.getLineHeight(), 0, 0);
				(new AccountUnit()).print(graphics, 0, configuration.getLineHeight(), 0, 10);
				return Printable.PAGE_EXISTS;
			}
		});
		
		//Create printables
		
		
	}
	
	// PAGEABLE *******************************
	// ****************************************
	
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
		 * @param If this parameter is not null, the text for this line is not taken from ec but from this parameter
		 */
		public NodeTitleUnit(ExtendedCategory ec, double horizontalOffset, String alternateText) {
			if(alternateText == null) {
				tail = ec.category().tail;
				String mask = configuration.getModel().getMask(ec);
				if(mask != null) tail = mask;
			}
			else tail = alternateText;
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
			double maxWidth = configuration.getFormat().getImageableWidth()*leftRatio - offset;
			Rectangle fontRect = new Rectangle((int)(configuration.getFormat().getImageableX() + offset),
												(int)(configuration.getFormat().getImageableY() + lineHeight*((double)firstLinePosition)),
												(int)maxWidth, (int)lineHeight-1);
			PrintHelper.printString(g, tail, fontRect, Color.BLACK, XAlign.LEFT, YALign.BOTTOM, true);
			g.drawLine(fontRect.x, (int)(fontRect.y + lineHeight),(int)( configuration.getFormat().getImageableX() + configuration.getFormat().getImageableWidth()),
					(int)(fontRect.y + lineHeight));
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
			//Obtain name
			if(ec.category() == Category.getRootCategory()) {
				tail = Fsfibu2StringTableMgr.getString("fs.fibu2.print.BilancialPageable.overall");
				String mask = configuration.getModel().getMask(ec);
				if(mask != null) tail = mask;
			}
			else {
				tail = ec.category().tail;
				String mask = configuration.getModel().getMask(ec);
				if(mask != null) tail = mask;
				tail = isSum? Fsfibu2StringTableMgr.getString("fs.fibu2.print.BilancialPageable.sum") + " (" + tail + "):" : 
					tail;
				if(ec.isAdditional()) tail = "(" + tail + ")";
			}
			
			//Obtain values
			float fIn = ec.isAdditional()? configuration.getModel().getIndividualPlus(ec.category()) : configuration.getModel().getCategoryPlus(ec.category());
			float fOut = ec.isAdditional()? configuration.getModel().getIndividualMinus(ec.category()) : configuration.getModel().getCategoryMinus(ec.category());
			float fSum = ec.isAdditional()? configuration.getModel().getIndividualSum(ec.category()) : configuration.getModel().getCategorySum(ec.category());
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
			double leftwidth = configuration.getFormat().getImageableWidth() * leftRatio - offset;
			double rightwidth = (configuration.getFormat().getImageableWidth() * (1-leftRatio))/3;
			int ypos = (int)(configuration.getFormat().getImageableY() + lineHeight*((double)(firstLinePosition))+1);
			Rectangle tailRect = new Rectangle((int)(configuration.getFormat().getImageableX() + offset), ypos ,
												(int)leftwidth, (int)lineHeight-2);
			Rectangle inRect = new Rectangle((int) (configuration.getFormat().getImageableX() + configuration.getFormat().getImageableWidth() - 3*rightwidth),
											ypos,
											(int)rightwidth, (int)lineHeight-2);
			Rectangle outRect = new Rectangle((int) (configuration.getFormat().getImageableX() + configuration.getFormat().getImageableWidth() - 2*rightwidth),
											ypos,
											(int)rightwidth, (int)lineHeight-2);
			Rectangle sumRect = new Rectangle((int) (configuration.getFormat().getImageableX() + configuration.getFormat().getImageableWidth() - rightwidth),
					ypos,
					(int)rightwidth, (int)lineHeight-2);
			//Print
			PrintHelper.printString(g, tail, tailRect, Color.BLACK, XAlign.LEFT, YALign.BOTTOM, true);
			PrintHelper.printString(g, in, inRect, inNegative? Color.RED : Color.BLACK, XAlign.RIGHT, YALign.BOTTOM, true);
			PrintHelper.printString(g, out, outRect, outNegative? Color.RED : Color.BLACK, XAlign.RIGHT, YALign.BOTTOM, true);
			PrintHelper.printString(g, sum, sumRect, sumNegative? Color.RED : Color.BLACK, XAlign.RIGHT, YALign.BOTTOM, true);
			if(isSumNode) g.drawLine(tailRect.x, tailRect.y-1, (int)(configuration.getFormat().getImageableX() + configuration.getFormat().getImageableWidth()), tailRect.y-1);	
		}	
	}
	
	/**
	 * Prints an nth-level node, i.e. a node's bilancial with title and final line and all subnode units
	 * @author Simon Hampe
	 *
	 */
	private class NthLevelNodeUnit implements LinePrintUnit {
	
		private Vector<LinePrintUnit> units = new Vector<LinePrintUnit>();
		private double offsetPerLevel = 0;
		
		/**
		 * Creates a node unit
		 * @param ec The node concerned
		 * @param offsetPerLevel The offset per Level
		 * @param includeCaption Whether a caption should be included as first line
		 */
		public NthLevelNodeUnit(ExtendedCategory ec, double offsetPerLevel, boolean includeCaption) {
			this.offsetPerLevel = offsetPerLevel;
			insertNode(ec, 0,0,includeCaption);
		}
		
		//Inserts the given node's bilancial starting at the specified position in units
		private void insertNode(ExtendedCategory ec, int position, double offset, boolean includeCaption) {
			if(!configuration.getModel().isLeaf(ec)) {
				units.add(position,new NodeSumUnit(ec,offset,true));
				units.add(position,new EmptyLinePrintUnit(1));
				for(int i = configuration.getModel().getChildCount(ec)-1;i >= 0; i--) {
					ExtendedCategory ecc = (ExtendedCategory)configuration.getModel().getChild(ec, i); 
					if(configuration.getModel().isInheritedVisible(ecc.category(), ecc.isAdditional())) insertNode(ecc, position, offset + offsetPerLevel,false);
				}
				//units.add(new EmptyLinePrintUnit(1));
				units.add(position,new NodeTitleUnit(ec,offset,null));
			}
			else {
				units.add(position,new NodeSumUnit(ec,offset,false));
			}
			if(includeCaption) units.add(0,new CaptionUnit(true));
		}
		
		@Override
		public int getNumberOfLines() {
			return units.size();
		}

		@Override
		public void print(Graphics g, int firstLinePosition, double lineHeight,
				int startLine, int endLine) {
			if(startLine >= 0 && startLine < units.size()) {
				for(int i = startLine; i <= endLine && i < units.size(); i++) {
					units.get(i).print(g, firstLinePosition + (i-startLine), lineHeight, 0, 0);
				}
			}
		}
		
	}
	
	/**
	 * Prints the final bilancial over two lines, with a line drawn above. this can only be printed in one piece
	 * @author Simon Hampe
	 *
	 */
	private class OverallSumUnit implements LinePrintUnit {

		private String tail = Fsfibu2StringTableMgr.getString("fs.fibu2.print.BilancialPageable.finalsum");
		private String in = "";
		private boolean inNegative = false;
		private String out = "";
		private boolean outNegative = false;
		private String sum = "";
		private boolean sumNegative = false;
		
		public OverallSumUnit() {
			float fIn = configuration.getModel().getCategoryPlus(Category.getRootCategory());
			float fOut = configuration.getModel().getCategoryMinus(Category.getRootCategory());
			float fSum = configuration.getModel().getCategorySum(Category.getRootCategory());
			inNegative = fIn < 0;
			outNegative = fOut < 0;
			sumNegative = fSum < 0;
			in = DefaultCurrencyFormat.getFormat(Fsfibu2Constants.defaultCurrency).format(fIn);
			out = DefaultCurrencyFormat.getFormat(Fsfibu2Constants.defaultCurrency).format(fOut);
			sum = DefaultCurrencyFormat.getFormat(Fsfibu2Constants.defaultCurrency).format(fSum);
		}
		
		@Override
		public int getNumberOfLines() {
			return 2;
		}

		@Override
		public void print(Graphics g, int firstLinePosition, double lineHeight,
				int startLine, int endLine) {
			g.setColor(Color.BLACK);
			//Determine rectangles:
			double leftwidth = configuration.getFormat().getImageableWidth() * leftRatio;
			double rightwidth = (configuration.getFormat().getImageableWidth() * (1-leftRatio))/3;
			int ypos = (int)(configuration.getFormat().getImageableY() + lineHeight*((double)(firstLinePosition))+1);
			Rectangle tailRect = new Rectangle((int)(configuration.getFormat().getImageableX()), ypos ,
												(int)leftwidth, (int)(2*lineHeight-2));
			Rectangle inRect = new Rectangle((int) (configuration.getFormat().getImageableX() + configuration.getFormat().getImageableWidth() - 3*rightwidth),
											ypos,
											(int)rightwidth, (int)(2*lineHeight-2));
			Rectangle outRect = new Rectangle((int) (configuration.getFormat().getImageableX() + configuration.getFormat().getImageableWidth() - 2*rightwidth),
											ypos,
											(int)rightwidth, (int)(2*lineHeight-2));
			Rectangle sumRect = new Rectangle((int) (configuration.getFormat().getImageableX() + configuration.getFormat().getImageableWidth() - rightwidth),
					ypos,
					(int)rightwidth, (int)(2*lineHeight-2));
			//Determin font sizes
			float inSize = PrintHelper.maximalFontSize(g, in, inRect);
			float outSize = PrintHelper.maximalFontSize(g, out, outRect);
			float sumSize = PrintHelper.maximalFontSize(g, sum, sumRect);
			float minimalSize = Math.min(inSize, Math.min(outSize, sumSize));
			//Print
			PrintHelper.printString(g, tail, tailRect, Color.BLACK, XAlign.LEFT, YALign.BOTTOM, true);
			g.setFont(g.getFont().deriveFont(minimalSize));
			PrintHelper.printString(g, in, inRect, inNegative? Color.RED : Color.BLACK, XAlign.RIGHT, YALign.BOTTOM, false);
			PrintHelper.printString(g, out, outRect, outNegative? Color.RED : Color.BLACK, XAlign.RIGHT, YALign.BOTTOM, false);
			PrintHelper.printString(g, sum, sumRect, sumNegative? Color.RED : Color.BLACK, XAlign.RIGHT, YALign.BOTTOM, false);
			g.drawLine(tailRect.x, tailRect.y-1, (int)(configuration.getFormat().getImageableX() + configuration.getFormat().getImageableWidth()), tailRect.y-1);
		}	
	}
	
	/**
	 * Prints the bilancial for an account
	 * @author Simon Hampe
	 *
	 */
	private class AccountSumUnit implements LinePrintUnit {

		private String account = "";
		private String before = "";
		private boolean bNegative = false;
		private String after = "";
		private boolean aNegative = false;
		private String difference = "";
		private boolean dNegative = false;
		
		/**
		 * Creates a new account sum unit
		 * @param a The account concerned
		 */
		public AccountSumUnit(Account a) {
			account = a.getName();
			float fB = configuration.getModel().getAccountBefore(a);
			float fA = configuration.getModel().getAccountAfter(a);
			float fD = fA - fB;
			bNegative = fB < 0;
			aNegative = fA < 0;
			dNegative = fD < 0;
			before = DefaultCurrencyFormat.getFormat(Fsfibu2Constants.defaultCurrency).format(fB);
			after = DefaultCurrencyFormat.getFormat(Fsfibu2Constants.defaultCurrency).format(fA);
			difference = DefaultCurrencyFormat.getFormat(Fsfibu2Constants.defaultCurrency).format(fD);
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
			double leftwidth = configuration.getFormat().getImageableWidth() * leftRatio - offsetPerLevel;
			double rightwidth = (configuration.getFormat().getImageableWidth() * (1-leftRatio))/3;
			int ypos = (int)(configuration.getFormat().getImageableY() + lineHeight*((double)(firstLinePosition))+1);
			Rectangle tailRect = new Rectangle((int)(configuration.getFormat().getImageableX() + offsetPerLevel), ypos ,
												(int)leftwidth, (int)lineHeight-2);
			Rectangle beforeRect = new Rectangle((int) (configuration.getFormat().getImageableX() + configuration.getFormat().getImageableWidth() - 3*rightwidth),
											ypos,
											(int)rightwidth, (int)lineHeight-2);
			Rectangle afterRect = new Rectangle((int) (configuration.getFormat().getImageableX() + configuration.getFormat().getImageableWidth() - 2*rightwidth),
											ypos,
											(int)rightwidth, (int)lineHeight-2);
			Rectangle differenceRect = new Rectangle((int) (configuration.getFormat().getImageableX() + configuration.getFormat().getImageableWidth() - rightwidth),
					ypos,
					(int)rightwidth, (int)lineHeight-2);
			//Print
			PrintHelper.printString(g, account, tailRect, Color.BLACK, XAlign.LEFT, YALign.BOTTOM, true);
			PrintHelper.printString(g, before, beforeRect, bNegative? Color.RED : Color.BLACK, XAlign.RIGHT, YALign.BOTTOM, true);
			PrintHelper.printString(g, after, afterRect, aNegative? Color.RED : Color.BLACK, XAlign.RIGHT, YALign.BOTTOM, true);
			PrintHelper.printString(g, difference, differenceRect, dNegative? Color.RED : Color.BLACK, XAlign.RIGHT, YALign.BOTTOM, true);
			
		}		
	}
	
	/**
	 * Prints the explanatory line above a node or account bilancial, indicating which column contains which value
	 * @author Simon Hampe
	 *
	 */
	private class CaptionUnit implements LinePrintUnit {

		private boolean isNodeBilancial = true;
		
		private final String firstNode = Fsfibu2StringTableMgr.getString("fs.fibu2.print.BilancialPageable.incaption");
		private final String secondNode = Fsfibu2StringTableMgr.getString("fs.fibu2.print.BilancialPageable.outcaption");
		private final String thirdNode = Fsfibu2StringTableMgr.getString("fs.fibu2.print.BilancialPageable.sumcaption");
		
		private final String firstAccount = Fsfibu2StringTableMgr.getString("fs.fibu2.print.BilancialPageable.before");
		private final String secondAccount = Fsfibu2StringTableMgr.getString("fs.fibu2.print.BilancialPageable.after");
		private final String thirdAccount = Fsfibu2StringTableMgr.getString("fs.fibu2.print.BilancialPageable.difference");
		
		/**
		 * Constructs a new unit
		 * @param nodeBilancial Whether this is the caption of a node bilancial (true) or an account bilancial (false)
		 */
		public CaptionUnit(boolean nodeBilancial) {
			isNodeBilancial = nodeBilancial;
		}
		
		@Override
		public int getNumberOfLines() {
			return 1;
		}

		@Override
		public void print(Graphics g, int firstLinePosition, double lineHeight,
				int startLine, int endLine) {
			g.setColor(Color.BLACK);
			//Determine rectangles
			double width = (configuration.getFormat().getImageableWidth() * (1-leftRatio))/3;
			double y = lineHeight* ((double) firstLinePosition) + configuration.getFormat().getImageableY();
			Rectangle firstRect = new Rectangle((int)(configuration.getFormat().getImageableX() + configuration.getFormat().getImageableWidth() - 3*width),
									(int)y, (int)width,(int)lineHeight);
			Rectangle secondRect = new Rectangle((int)(configuration.getFormat().getImageableX() + configuration.getFormat().getImageableWidth() - 2*width),
					(int)y, (int)width,(int)lineHeight);
			Rectangle thirdRect = new Rectangle((int)(configuration.getFormat().getImageableX() + configuration.getFormat().getImageableWidth() - width),
					(int)y, (int)width,(int)lineHeight);
			//Draw strings
			PrintHelper.printString(g, isNodeBilancial? firstNode : firstAccount, firstRect, Color.BLACK, XAlign.RIGHT, YALign.BOTTOM, true);
			PrintHelper.printString(g, isNodeBilancial? secondNode : secondAccount, secondRect, Color.BLACK, XAlign.RIGHT, YALign.BOTTOM, true);
			PrintHelper.printString(g, isNodeBilancial? thirdNode : thirdAccount, thirdRect, Color.BLACK, XAlign.RIGHT, YALign.BOTTOM, true);
		}
	}
	
	/**
	 * Prints the complete account unit, i.e. caption, title bar and all account bilancials
	 * @author Simon Hampe
	 *
	 */
	private class AccountUnit implements LinePrintUnit {
		
		Vector<LinePrintUnit> units = new Vector<LinePrintUnit>();

		public AccountUnit() {
			units.add(new CaptionUnit(false));
			units.add(new NodeTitleUnit(null,0,Fsfibu2StringTableMgr.getString("fs.fibu2.print.BilancialPageable.accounts")));
			TreeSet<Account> accounts = new TreeSet<Account>(new DefaultAccountComparator());
				accounts.addAll(configuration.getModel().getAccounts());
			for(Account a : accounts) {
				units.add(new AccountSumUnit(a));
			}
		}
		
		@Override
		public int getNumberOfLines() {
			return units.size();
		}

		@Override
		public void print(Graphics g, int firstLinePosition, double lineHeight,
				int startLine, int endLine) {
			if(startLine >= 0 && startLine < units.size()) {
				for(int i = startLine; i <= endLine && i < units.size(); i++) {
					units.get(i).print(g, firstLinePosition + (i-startLine), lineHeight, 0, 0);
				}
			}
		}
		
	}
	
	/**
	 * A unit printer prints parts of the line print units
	 * @author Simon Hampe
	 *
	 */
	private class UnitPrinter implements Printable {

		private int firstUnit;
		private int firstLine;
		private int lastUnit;
		private int lastLine;
		
		/**
		 * Creates a printer
		 * @param firstUnit The index of the first LinePrintUnit to print
		 * @param firstLine The first line of this unit to print
		 * @param lastUnit The index of the last LinePrintUnit to print
		 * @param lastLine The last line of this unit to print
		 */
		public UnitPrinter(int firstUnit, int firstLine, int lastUnit, int lastLine) {
			this.firstUnit = firstUnit;
			this.firstLine = firstLine;
			this.lastUnit = lastUnit;
			this.lastLine = lastLine;
		}
		
		@Override
		public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
				throws PrinterException {
			int linePosition = 0;
			for(int i = firstUnit; i <= lastUnit; i++) {
				int startLine = i == firstUnit? firstLine : 0;
				int endLine = i == lastUnit? lastLine : units.get(i).getNumberOfLines()-1;
				units.get(i).print(graphics, linePosition, configuration.getLineHeight(), startLine, endLine);
				linePosition += (endLine - startLine) +1;
			}
			return Printable.PAGE_EXISTS;
		}
	}
	
}
