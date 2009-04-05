package fs.fibu2.data.model;

import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

import org.dom4j.Element;
import org.dom4j.Node;

import fs.xml.XMLConfigurable;
import fs.xml.XMLReadConfigurationException;
import fs.xml.XMLWriteConfigurationException;

public class Entry implements XMLConfigurable {

	// MEMBERS ******************************
	
	private String 				name;
	private float 				value;
	private Currency 			currency;
	private GregorianCalendar	date;
	private Category			category;
	private Account				account;
	private Vector<String> 		accountInformation;
	private String				additionalInformation;
	
	
	// GETTERS / SETTERS *************************
	// *******************************************
	
	/**
	 * @return the name of this entry 
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of this entry (null is transformed to the empty string)
	 */
	public void setName(String name) {
		this.name = name == null? "" : name;
	}

	/**
	 * @return The value of this entry
	 */
	public float getValue() {
		return value;
	}

	/**
	 * Sets the value of this entry. The number of fraction digits actually displayed is determined by the currency.
	 */
	public void setValue(float value) {
		this.value = value;
	}

	/**
	 * @return The currency of this entry
	 */
	public Currency getCurrency() {
		return currency;
	}

	/**
	 * Sets the currency of this entry. 
	 * @throws NullPointerException - If currency == null
	 */
	public void setCurrency(Currency currency) throws NullPointerException {
		if(currency == null) throw new NullPointerException("Null currency invalid.");
		this.currency = currency;
	}

	/**
	 * The date of this entry (usually the invoice date)
	 */
	public GregorianCalendar getDate() {
		return (GregorianCalendar)date.clone();
	}

	/**
	 * Sets the date of this entry. 
	 * @throws NullPointerException - If date == null
	 */
	public void setDate(GregorianCalendar date) throws NullPointerException {
		if(date == null) throw new NullPointerException("Null date invalid");
		this.date = (GregorianCalendar)date.clone();
	}

	/**
	 * @return the category of this entry
	 */
	public Category getCategory() {
		return category;
	}

	/**
	 * Sets the category of this entry. The null category is replaced by the root category
	 */
	public void setCategory(Category category) {
		this.category = category == null? Category.getRootCategory() : category;
	}

	/**
	 * @return the account of this entry
	 */
	public Account getAccount() {
		return account;
	}

	/**
	 * Sets the account of this entry. 
	 * @throws NullPointerException - If account == null
	 */
	public void setAccount(Account account) throws NullPointerException {
		if(account == null) throw new NullPointerException("Null account invalid.");
		this.account = account;
	}

	/**
	 * @return A list of information fields specific for the account type of this entry
	 */
	public Vector<String> getAccountInformation() {
		return new Vector<String>(accountInformation);
	}

	/**
	 * Sets the list of information for the account type of this entry. A null list
	 * is replaced by the empty list
	 */
	public void setAccountInformation(Vector<String> accountInformation) {
		this.accountInformation = accountInformation == null? new Vector<String>() : accountInformation;
	}

	/**
	 * @return A string containing additional information about this entry
	 */
	public String getAdditionalInformation() {
		return additionalInformation;
	}

	/**
	 * Sets the additional information about this entry. A null string is replaced by the empty string
	 */
	public void setAdditionalInformation(String additionalInformation) {
		this.additionalInformation = additionalInformation == null? "" : additionalInformation;
	}

	// XMLCONFIFGURABLE *********************************
	// **************************************************
	
	
	@Override
	public String getIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isConfigured() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void configure(Node arg0) throws XMLWriteConfigurationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Element getConfiguration() throws XMLReadConfigurationException {
		// TODO Auto-generated method stub
		return null;
	}

}
