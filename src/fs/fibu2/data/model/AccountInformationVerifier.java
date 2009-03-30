package fs.fibu2.data.model;

import fs.xml.XMLConfigurable;

/**
 * Each class whose purpose is verification of fsfibu2 account information should implement this interface. 
 * To be truly XMLConfigurable it should make sure, its definition and ID is known by the AIVFactory class, so an instance
 * can be created by 
 * @author Simon Hampe
 *
 */
public interface AccountInformationVerifier extends XMLConfigurable {

}
