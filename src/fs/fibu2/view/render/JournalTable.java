package fs.fibu2.view.render;

import java.util.Currency;

import javax.swing.JTable;

import fs.fibu2.data.model.Journal;
import fs.fibu2.view.model.JournalTableModel;

/**
 * This class implements a JTable with a few general settings useful for displaying fsfibu2 {@link Journal}s, as well as some
 * more type-specific methods for model retrieval and such
 * @author Simon Hampe
 *
 */
public class JournalTable extends JTable {

	/**
	 * compiler-generated serial version uid 
	 */
	private static final long serialVersionUID = 4204996320101119252L;
	
	private JournalTableModel internalModel;
	
	/**
	 * Creates a new table, using the given model
	 * @throws IllegalArgumentException - if model == null
	 */
	public JournalTable(JournalTableModel model) throws IllegalArgumentException{
		super(model);
		if(model == null) throw new IllegalArgumentException("Cannot create table from null model");
		internalModel = model;
		setDefaultRenderer(Object.class, new JournalTableRenderer(model,Currency.getInstance("EUR")));
		setDragEnabled(false);
		getTableHeader().setReorderingAllowed(false);
		//Adapt column widths
		setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		columnModel.getColumn(0).setPreferredWidth(36);
		columnModel.getColumn(1).setPreferredWidth(350);
		columnModel.getColumn(2).setPreferredWidth(100);
		columnModel.getColumn(3).setPreferredWidth(100);
		columnModel.getColumn(4).setPreferredWidth(100);
		columnModel.getColumn(5).setPreferredWidth(250);
		columnModel.getColumn(6).setPreferredWidth(150);
		columnModel.getColumn(7).setPreferredWidth(500);
		
		//Set Row sorters
		//I actually decided, not to use row sorters. It didn't really seem necessary
//		TableRowSorter<JournalTableModel> rowSorter = new TableRowSorter<JournalTableModel>(internalModel);
//			for(int i = 0; i < internalModel.getColumnCount(); i++) rowSorter.setComparator(i, new RowSorterComparator(i));
//		setRowSorter(rowSorter);
		
		
	}
	
	public JournalTableModel getJournalTableModel()  {
		return internalModel;
	}
	
//	/**
//	 * This is a column-dependent comparator which compares entries of a JournalTableModel
//	 * @author Simon Hampe
//	 *
//	 */
//	private class RowSorterComparator implements Comparator<Object> {
//		private int column = 0;
//		
//		public RowSorterComparator(int column) {
//			this.column = column;
//		}
//
//		@Override
//		public int compare(Object o1, Object o2) {
//			if(o1 == null && o2 == null) return 0;
//			if(o1 == null || o2 == null) return -1;
//			if(o1 == o2) return 0;
//			
//			if(o1 instanceof ExtremeSeparator) {
//				if(((ExtremeSeparator)o1).isBeforeAll()) return -1;
//				else return 1;
//			}
//			if(o2 instanceof ExtremeSeparator) {
//				if(((ExtremeSeparator)o2).isBeforeAll()) return 1;
//				else return -1;
//			}
//			
//			if(o1 instanceof Entry && o2 instanceof EntrySeparator) return -1;
//			if(o1 instanceof EntrySeparator && o2 instanceof Entry) return 1;
//			
//			if(o1 instanceof Entry && o2 instanceof Entry) {
//				switch(column) {
//				case 0: 
//					boolean firstValid = true;
//					boolean secondValid = true;
//					boolean firstComment = false;
//					boolean secondComment = false;
//					try {
//						((Entry)o1).getAccount().verifyEntry((Entry)o1);
//					} catch(EntryVerificationException e) { firstValid = false;}
//					try {
//						((Entry)o2).getAccount().verifyEntry((Entry)o2);
//					} catch(EntryVerificationException e) { secondValid = false;}
//					if(!((Entry)o1).getAdditionalInformation().equals("")) firstComment = true;
//					if(!((Entry)o2).getAdditionalInformation().equals("")) secondComment = true;
//					
//					if(firstValid && !firstComment && secondValid && !secondComment) return 0; //Both have 0 symbols
//					if(firstComment && !firstValid && secondComment && !secondValid) return 0; //Both have 2 symbols
//					if((firstValid && !firstComment) && (!secondValid || secondComment)) return -1; //One has zero, one has one or two
//					if((!firstValid || firstComment) && (secondValid && !secondComment)) return 1; //One has zero, one has one or two
//					if(firstComment && secondComment) return 0; //Both are equal
//					if(!firstValid && ! secondValid) return 0; //both are equal
//					if(firstComment) return -1; //Both are not equal
//					else return 1;
//					
//					
//				case 1:return ((Entry)o1).getName().compareTo(((Entry)o2).getName());
//				case 2:return ((Entry)o1).getDate().compareTo(((Entry)o2).getDate());
//				case 3:return (new Float(((Entry)o1).getValue())).compareTo(new Float(((Entry)o2).getValue()));
//				case 4:return ((Entry)o1).getAccount().getName().compareTo(((Entry)o2).getAccount().getName());
//				case 5:return ((Entry)o1).getCategory().compareTo(((Entry)o2).getCategory());
//				case 6:if(((Entry)o1).getAccountInformation().size() != ((Entry)o2).getAccountInformation().size()) {
//							return (new Integer(((Entry)o1).getAccountInformation().size())).compareTo(new Integer(((Entry)o2).getAccountInformation().size()));
//						}
//					else {
//						Vector<String> kset1 = new Vector<String>(new TreeSet<String>(((Entry)o1).getAccountInformation().keySet()));
//						Vector<String> kset2 = new Vector<String>(new TreeSet<String>(((Entry)o2).getAccountInformation().keySet()));
//						for(int i = kset1.size() -1; i >= 0; i--) {
//							int ckey = kset1.get(i).compareTo(kset2.get(i));
//							if(ckey != 0) return ckey;
//							else {
//								int cval = ((Entry)o1).getAccountInformation().get(kset1.get(i)).compareTo(((Entry)o2).getAccountInformation().get(kset2.get(i)));
//								if(cval != 0) return cval;
//							}
//						}
//					}
//				case 7:return ((Entry)o1).getAdditionalInformation().compareTo(((Entry)o2).getAdditionalInformation());
//				default: return 0;
//				}
//			}
//			
//			if(o1 instanceof EntrySeparator && o2 instanceof EntrySeparator) {
//				switch(column) { 
//				case 1:	return ((EntrySeparator)o1).getName().compareTo(((EntrySeparator)o2).getName());
//				default: return 0;
//				}
//			}
//			
//			return 0;
//		}
//	}
	
}
