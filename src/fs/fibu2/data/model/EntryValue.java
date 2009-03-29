package fs.fibu2.data.model;

import java.util.Currency;

import org.dom4j.Element;
import org.dom4j.Node;

import fs.xml.XMLConfigurable;
import fs.xml.XMLReadConfigurationException;
import fs.xml.XMLWriteConfigurationException;

/**
 * This class represent a value of an fsfibu2 journal entry together with a currency specification.
 * @author Simon Hampe
 *
 */
public class EntryValue implements XMLConfigurable, Comparable<EntryValue>{

	//Data
	private Currency currency;
	private float value;
	
	// CONSTRUCTOR *******************************************
	// *******************************************************
	
	public EntryValue(float value, Currency currency) throws NullPointerException {
		this.setValue(value);
		this.setCurrency(currency);
	}
	
	// GETTERS AND SETTERS ************************************
	// ********************************************************

	/**
	 * Sets the value
	 */
	public void setValue(float value) {
		this.value = value;
	}

	/**
	 * @return the value
	 */
	public float getValue() {
		return value;
	}

	/**
	 * Sets the currency
	 * @throws NullPointerException - if currency == null
	 */
	public void setCurrency(Currency currency) throws NullPointerException {
		if(currency == null) throw new NullPointerException("Null currency is not a valid currency");
		this.currency = currency;
	}

	/**
	 * @return The currency
	 */
	public Currency getCurrency() {
		return currency;
	}
	
	// XMLCONFIGURABLE ****************************************
	// ********************************************************
	
	/**
	 * This expects a node named 'entryvalue' with subnodes 'value' and 'currency', which contain the
	 * float value and the ISO 4217 code of the currency
	 * @throws XMLWriteConfigurationException - if the expected nodes are not present or if the currency code is not a valid ISO 4217 code
	 */
	@Override
	public void configure(Node arg0) throws XMLWriteConfigurationException {
		// TODO Auto-generated method stub
	}

	@Override
	public Element getConfiguration() throws XMLReadConfigurationException {
		// TODO Auto-generated method stub
		return null;
	}

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
	
	// EQUALS, HASHCODE AND SUCH **************************************
	// ****************************************************************

	/**
	 * Compares the values of the two entry values
	 */
	@Override
	public int compareTo(EntryValue o) {
		if(o == null) return 1;
		return (new Float(value)).compareTo(o.getValue());
	}

	/**
	 * Returns true, if and only if the values of the two entry values agree. 
	 * The currency does not play any role.
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null | obj.getClass() != this.getClass()) return false;
		return(value == ((EntryValue)obj).getValue());
	}

	/**
	 * @return Float.floatToIntBits(value)
	 */
	@Override
	public int hashCode() {
		return Float.floatToIntBits(value);
	}

	/**
	 * @return The value + " " + currency symbol
	 */
	@Override
	public String toString() {
		return value + currency.getSymbol();
	}

	


}
