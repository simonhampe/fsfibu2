package fs.fibu2.data.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.tree.DefaultElement;

import fs.fibu2.data.event.JournalListener;
import fs.fibu2.data.event.ReadingPointListener;
import fs.fibu2.undo.JournalUndoManager;
import fs.fibu2.undo.UndoableJournalEntryEdit;
import fs.fibu2.undo.UndoableJournalInfoEdit;
import fs.fibu2.undo.UndoableJournalPointEdit;
import fs.fibu2.undo.UndoableJournalReplaceEdit;
import fs.fibu2.undo.UndoableJournalStartEdit;
import fs.xml.XMLConfigurable;
import fs.xml.XMLReadConfigurationException;
import fs.xml.XMLWriteConfigurationException;

/**
 * This class implements an fsfibu2 journal. A journal is essentially an (unsorted) collection of entries together
 * with some additional data: <br>
 * - ReadingPoints: A reading point is a certain day of the year at which the user whishes to see a bilancial overview and potentially reset
 * all sums. <br>
 * - Start values: A journal contains a start value for each account it uses.<br>
 * - A name and a description for this journal
 * <br> 
 * All the write access methods exist in two versions: The normal one and the one that automatically creates an associated {@link UndoableEdit}
 * and posts it to the proper JournalUndoManager. Both versions notify any Listeners of changes. 
 * @author Simon Hampe
 *
 */
public class Journal implements XMLConfigurable, ReadingPointListener {

	// FIELDS ****************************************
	// ***********************************************
	
	private HashSet<Entry> listOfEntries = new HashSet<Entry>();
	private HashSet<ReadingPoint> listOfReadingPoints = new HashSet<ReadingPoint>();
	private HashMap<Account, Float> startValues = new HashMap<Account, Float>();
	
	private String name = "";
	private String description = "";
	
	private JournalUndoManager manager = JournalUndoManager.getInstance(this);
	
	private HashSet<JournalListener> listeners = new HashSet<JournalListener>(); 
	
	// CONSTRUCTOR ***********************************
	// ***********************************************
	
	/**
	 * Creates an empty journal.
	 */
	public Journal() {
	}
	
	/**
	 * Creates a journal from the given node 
	 * @throws XMLWriteConfigurationException - If the node is null or invalid
	 */
	public Journal(Node n) throws XMLWriteConfigurationException  {
		configure(n);
	}
	
	// GETTERS AND SETTERS ***************************
	// ***********************************************
	
	/**
	 * @return The list of entries
	 */
	public synchronized HashSet<Entry> getEntries() {
		return new HashSet<Entry>(listOfEntries);
	}
	
	/**
	 * @return The list of reading points
	 */
	public synchronized HashSet<ReadingPoint> getReadingPoints() {
		return new HashSet<ReadingPoint>(listOfReadingPoints);
	}
	
	/**
	 * @return The list of start values
	 */
	public synchronized HashMap<Account, Float> getStartValues() {
		return new HashMap<Account, Float>(startValues);
	}
	
	/**
	 * @return The start value associated to the account or 0 if there is none.
	 */
	public synchronized float getStartValue(Account a) throws IllegalArgumentException {
		Float f = startValues.get(a);
		if(f != null ) return f;
		else return 0;
	}
	
	/**
	 * @return The name of this journal
	 */
	public synchronized String getName() {
		return name;
	}
	
	/**
	 * @return A description of this journal
	 */
	public synchronized String getDescription() {
		return description;
	}
	
	/**
	 * @return A list of all categories (including supercategories) used by entries in this journal. The root category is not included
	 */
	public synchronized HashSet<Category> getListOfCategories() {
		HashSet<Category> cats = new HashSet<Category>();
		for(Entry e : getEntries()) {
			Category c = e.getCategory();
			for(int i = c.getOrder(); i >= 1; i--) {
				cats.add(c);
				c = c.parent;
			}
		}
		return cats;
	}
	
	/**
	 * @return A list of all accounts used by entries in this journal or which have a start value
	 */
	public synchronized HashSet<Account> getListOfAccounts() {
		HashSet<Account> accounts = new HashSet<Account>();
		for(Entry e : getEntries()) {
			accounts.add(e.getAccount());
		}
		for(Account a : startValues.keySet()) {
			accounts.add(a);
		}
		return accounts;
	}
	
	/**
	 * Adds the entry e to the list (if it isn't already contained in it). If e == null,
	 * this call is ignored
	 */
	public synchronized void addEntry(Entry e) {
		if(e != null) {
			boolean b = listOfEntries.add(e);
			if(b) fireEntriesAdded(new Entry[] {e});
		}
		
	}
	
	/**
	 * Removes e from the journal
	 */
	public synchronized void removeEntry(Entry e) {
		if(e != null) {
			boolean b = listOfEntries.remove(e);
			if(b) fireEntriesRemoved(new Entry[] {e});
		}
		
	}
	
	/**
	 * Adds all entries in c, if they are not null
	 */
	public synchronized void addAllEntries(Collection<? extends Entry> c) {
		if(c != null) {
			Entry[] trueEntries= new Entry[c.size()];
			int i = 0;
			for(Entry e : c) {
				if(e != null) {
					boolean b = listOfEntries.add(e);
					if(b) {
						trueEntries[i] = e;
						i++;
					}
				}
			}
			fireEntriesAdded(Arrays.<Entry>copyOf(trueEntries, i));
		}
	}
	
	/**
	 * Removes all entries which are in c from the journal
	 */
	public synchronized void removeAllEntries(Collection<? extends Entry> c) {
		if(c != null) {
			Entry[] trueEntries= new Entry[c.size()];
			int i = 0;
			for(Entry e : c) {
				if(e != null) {
					boolean b = listOfEntries.remove(e);
					if(b) {
						trueEntries[i] = e;
						i++;
					}
				}
			}
			fireEntriesRemoved(Arrays.<Entry>copyOf(trueEntries, i));
		}
	}
	
	/**
	 * Removes the entry oldEntry and adds the entry newEntry. If one of them is null,
	 * the call is ignored
	 */
	public synchronized void replaceEntry(Entry oldEntry, Entry newEntry) {
		if(oldEntry != null && newEntry != null && oldEntry != newEntry) {
			listOfEntries.remove(oldEntry);
			listOfEntries.add(newEntry);
			fireEntryReplaced(oldEntry, newEntry);
		}
	}
	
	/**
	 * Sets the start value for the account a. If a == null, this call is ignored. 
	 */
	public synchronized void setStartValue(Account a, float f) {
		if(a != null) {
			Float old = startValues.get(a);
			startValues.put(a, f);
			if(old == null || old != f) fireStartValueChanged(a, old, f);
		}
	}
	
	/**
	 * Removes the start value for a.
	 */
	public synchronized void removeStartValue(Account a) {
		if(a != null) {
			Float old = startValues.get(a);
			startValues.remove(a);
			if(old != null) fireStartValueChanged(a, old, null);
		}		
	}
	
	/**
	 * Adds rp to the list of reading points, if it isn't null. The journal 
	 * automatically adds itself as a listener to the reading point
	 */
	public synchronized void addReadingPoint(ReadingPoint rp) {
		if(rp != null) {
			boolean b = listOfReadingPoints.add(rp);
			if(b) {
				rp.addReadingPointListener(this);
				fireReadingPointAdded(rp);
			}
		}
	}
	
	/**
	 * Removes the reading point rp from the list of reading points. The journal
	 * automatically removes itself as a listener from the reading point
	 */
	public synchronized void removeReadingPoint(ReadingPoint rp) {
		boolean b = listOfReadingPoints.remove(rp);
		if(b) {
			rp.removeReadingPointListener(this);
			fireReadingPointRemoved(rp);
		}
	}
	
	/**
	 * Sets the name of this journal. The name will be used for representation in lists
	 * and window titles and such
	 * @param newName the new name. If null, the empty string is used
	 */
	public synchronized void setName(String newName) {
		String old = name;
		this.name = newName == null? "" : newName;
		fireNameChanged(old, name);
	}
	
	/**
	 * Sets the description of this journal. The description should shortly 
	 * specify the content and purpose of this journal
	 * @param newDescription The new description. If null, the empty string is used
	 */
	public synchronized void setDescription(String newDescription) {
		String old = description;
		this.description = newDescription == null? "" : newDescription;
		fireDescriptionChanged(old, description);
	}
	
	// UNDOABLEEDITS *********************************
	// ***********************************************
	
	/**
	 * @see Journal#addEntry(Entry)
	 */
	public synchronized void addEntryUndoable(Entry e) {
		UndoableJournalEntryEdit edit = new UndoableJournalEntryEdit(this,new HashSet<Entry>(Arrays.asList(e)),true);
		edit.redo();
		manager.addEdit(edit);
	}
	
	/**
	 * @see Journal#removeEntry(Entry)
	 */
	public synchronized void removeEntryUndoable(Entry e) {
		UndoableJournalEntryEdit edit = new UndoableJournalEntryEdit(this, new HashSet<Entry>(Arrays.asList(e)),false);
		edit.redo();
		manager.addEdit(edit);
	}
	
	/**
	 * @see Journal#addAllEntries(Collection)
	 */
	public synchronized void addAllEntriesUndoable(Collection<? extends Entry> c) {
		UndoableJournalEntryEdit edit = new UndoableJournalEntryEdit(this, c, true);
		edit.redo();
		manager.addEdit(edit);
	}
	
	/**
	 * @see Journal#removeAllEntries(Collection)
	 */
	public synchronized void removeAllEntriesUndoable(Collection<? extends Entry> c) {
		UndoableJournalEntryEdit edit = new UndoableJournalEntryEdit(this,c,false);
		edit.redo();
		manager.addEdit(edit);
	}
	
	/**
	 * @see Journal#replaceEntry(Entry, Entry)
	 */
	public synchronized void replaceEntryUndoable(Entry oldValue, Entry newValue) {
		UndoableJournalReplaceEdit edit = new UndoableJournalReplaceEdit(this,oldValue, newValue);
		edit.redo();
		manager.addEdit(edit);
	}
	
	/**
	 * @see Journal#setStartValue(Account, float)
	 */
	public synchronized void setStartValueUndoable(Account a, float value) {
		UndoableJournalStartEdit edit = new UndoableJournalStartEdit(this,a,getStartValue(a),value);
		edit.redo();
		manager.addEdit(edit);
	}
	
	/**
	 * @see Journal#removeStartValue(Account)
	 */
	public synchronized void removeStartValueUndoable(Account a) {
		UndoableJournalStartEdit edit = new UndoableJournalStartEdit(this,a,getStartValue(a),null);
		edit.redo();
		manager.addEdit(edit);
	}
	
	/**
	 * @see Journal#addReadingPoint(ReadingPoint)
	 */
	public synchronized void addReadingPointUndoable(ReadingPoint point) {
		UndoableJournalPointEdit edit = new UndoableJournalPointEdit(this,point,true);
		edit.redo();
		manager.addEdit(edit);
	}
	
	/**
	 * @see Journal#removeReadingPoint(ReadingPoint)
	 */
	public synchronized void removeReadingPointUndoable(ReadingPoint point) {
		UndoableJournalPointEdit edit = new UndoableJournalPointEdit(this,point,false);
		edit.redo();
		manager.addEdit(edit);
	}
	
	/**
	 * @see Journal#setName(String)
	 */
	public synchronized void setNameUndoable(String newname) {
		UndoableJournalInfoEdit edit = new UndoableJournalInfoEdit(this,getName(),newname,true);
		edit.redo();
		manager.addEdit(edit);
	}
	
	/**
	 * @see Journal#setDescription(String)
	 */
	public synchronized void setDescriptionUndoable(String newdescription) {
		UndoableJournalInfoEdit edit = new UndoableJournalInfoEdit(this,getDescription(),newdescription,false);
		edit.redo();
		manager.addEdit(edit);
	}	
	
	// XMLCONFIGURABLE *******************************
	// ***********************************************
	
	/**
	 * Expects the following node structure:<br>
	 * - Optionally the nodes name and description with arbitrary content <br>
	 * - An optional node called startvalues with subnodes for each account. The subnodes have
	 * as name the account id (which must be known to AccountLoader) and as text a valid
	 * float value indicating the start value for this account. All accounts for which
	 * no start value has been given will implicitely be initialized to zero <br> 
	 * - An arbitrary number of valid {@link ReadingPoint} nodes <br>
	 * - An arbitrary number of valid {@link Entry} nodes <br>
	 * This operation is not undoable, however it will reset the associated undomanager. The journal
	 * and its {@link UndoManager} will remain completely unchanged, if any {@link XMLWriteConfigurationException} occurs. 
	 * @throws XMLWriteConfigurationException - If any of the following cases holds:<br>
	 * - There is an account ID node with an unknown ID <br>
	 * - Any of the {@link ReadingPoint} or {@link Entry} nodes is faulty <br>
	 * - n == null
	 */
	@Override
	public synchronized void configure(Node n) throws XMLWriteConfigurationException {
		if(n == null) throw new XMLWriteConfigurationException("Can't create journal from null node.");
		
		HashMap<Account, Float> newstartvalues = new HashMap<Account, Float>();
		HashSet<Entry> newentries = new HashSet<Entry>();
		HashSet<ReadingPoint> newreadings = new HashSet<ReadingPoint>();
		String newname = "";
		String newdescription = "";
		
		Node nameNode = n.selectSingleNode("./name");
		if(nameNode != null) {
			newname = nameNode.getText();
		}
		
		Node descriptionNode = n.selectSingleNode("./description");
		if(descriptionNode != null) {
			newdescription = descriptionNode.getText();
		}
		
		Node startNode = n.selectSingleNode("./startvalues");
		if(startNode != null) {
			for(Object accountNode : startNode.selectNodes("./*")) {
				try {
					Account a = AccountLoader.getAccount(((Node)accountNode).getName());
					float v = Float.parseFloat(((Node)accountNode).getText());
					newstartvalues.put(a, v);
				}
				catch(ClassCastException ce) {
					//Skip
				}
				catch(NumberFormatException ne) {
					throw new XMLWriteConfigurationException("Invalid journal configuration: " + 
							"The value " + ((Node)accountNode).getText() + " for account " + 
							((Node)accountNode).getName() + " is not a valid float value");
				}
				catch(IllegalArgumentException ie) {
					throw new XMLWriteConfigurationException("Invalid journal configuration: Account ID " + ((Node)accountNode).getName() + " unknown.");
				}
			}
		}
		
		for(Object pointNode : n.selectNodes("./readingpoint")) {
			try {
				ReadingPoint rp = new ReadingPoint((Node)pointNode);
				newreadings.add(rp);
			}
			catch(ClassCastException ce) {
				throw new XMLWriteConfigurationException("Invalid journal configuration: Reading point node is not a node");
			}
		}
		
		for(Object entryNode : n.selectNodes("./entry")) {
			try {
				Entry e = new Entry((Node) entryNode);
				newentries.add(e);
			}
			catch(ClassCastException ce) {
				throw new XMLWriteConfigurationException("Invalid journal configuration: Entry node is not a node");
			}
		}
		
		//Copy values and reset manager
		
		removeAllEntries(listOfEntries);
		addAllEntries(newentries);
		
		for(ReadingPoint rp : listOfReadingPoints) removeReadingPoint(rp);
		for(ReadingPoint rp : newreadings) addReadingPoint(rp);
		
		for(Account a : startValues.keySet()) removeStartValue(a);
		for(Account a : newstartvalues.keySet()) setStartValue(a, newstartvalues.get(a));
		
		setName(newname);
		setDescription(newdescription);
		
		manager.discardAllEdits();
		
	}

	/**
	 * @return A node named 'journal' with subnodes as specified by configure(Node n)
	 */
	@Override
	public synchronized Element getConfiguration() throws XMLReadConfigurationException {
		DefaultElement root = new DefaultElement("journal");
		
		DefaultElement nameNode = new DefaultElement("name");
		nameNode.setText(name);
		root.add(nameNode);
		
		DefaultElement descNode = new DefaultElement("description");
		descNode.setText(description);
		root.add(descNode);
		
		DefaultElement startNode = new DefaultElement("startvalues");
		for(Account a : startValues.keySet()) {
			DefaultElement accountNode = new DefaultElement(a.getID());
			accountNode.setText(startValues.get(a).toString());
			startNode.add(accountNode);
		}
		root.add(startNode);
		
		for(ReadingPoint rp : listOfReadingPoints) {
			root.add(rp.getConfiguration());
		}
		
		for(Entry e : listOfEntries) {
			root.add(e.getConfiguration());
		}
		
		return root;
	}

	/**
	 * @return 'journal'
	 */
	@Override
	public String getIdentifier() {
		return "journal";
	}

	/**
	 * @return true
	 */
	@Override
	public boolean isConfigured() {
		return true;
	}
	
	// LISTENERHANDLING ********************************************
	// *************************************************************
	
	/**
	 * Adds l to the list of listeners (if l != null)
	 */
	public void addJournalListener(JournalListener l) {
		if(l != null) listeners.add(l);
	}
	
	/**
	 * Removes l from the list of listeners
	 */
	public void remvoeJournalListener(JournalListener l) {
		listeners.remove(l);
	}
	
	protected void fireActivityChanged(ReadingPoint source) {
		for(JournalListener l : listeners) l.activityChanged(source);
	}
	
	protected void fireDateChanged(ReadingPoint source) {
		for(JournalListener l : listeners) l.dateChanged(source);
	}
	
	protected void fireVisibilityChanged(ReadingPoint source) {
		for(JournalListener l : listeners) l.visibilityChanged(source);
	}
	
	protected void fireNameChanged(ReadingPoint source) {
		for(JournalListener l : listeners) l.nameChanged(source);
	}
	
	protected void fireEntriesAdded(Entry[] newEntries) {
		for(JournalListener l : listeners) l.entriesAdded(this, newEntries);
	}
	
	protected void fireEntriesRemoved(Entry[] oldEntries) {
		for(JournalListener l : listeners) l.entriesRemoved(this, oldEntries);
	}
	
	protected  void  fireEntryReplaced(Entry oldEntry, Entry newEntry) {
		for(JournalListener l : listeners) l.entryReplaced(this, oldEntry, newEntry);
	}
	
	protected void fireStartValueChanged(Account a, Float oldValue, Float newValue) {
		for(JournalListener l : listeners) l.startValueChanged(this, a, oldValue, newValue);
	}
	
	protected void fireReadingPointAdded(ReadingPoint point) {
		for(JournalListener l : listeners) l.readingPointAdded(this, point);
	}
	
	protected void fireReadingPointRemoved(ReadingPoint point) {
		for(JournalListener l : listeners) l.readingPointRemoved(this, point);
	}
	
	protected void fireNameChanged(String oldValue, String newValue) {
		for(JournalListener l : listeners) l.nameChanged(this, oldValue, newValue);
	}
	
	protected void fireDescriptionChanged(String oldValue, String newValue) {
		for(JournalListener l : listeners) l.descriptionChanged(this, oldValue, newValue);
	}
	
	// READINGPOINTLISTENER ****************************************
	// *************************************************************

	@Override
	public void activityChanged(ReadingPoint source) {
		fireActivityChanged(source);
	}

	@Override
	public void dateChanged(ReadingPoint source) {
		fireDateChanged(source);
	}

	@Override
	public void visibilityChanged(ReadingPoint source) {
		fireVisibilityChanged(source);
	}
	
	@Override
	public void nameChanged(ReadingPoint source) {
		fireNameChanged(source);
	}

}
