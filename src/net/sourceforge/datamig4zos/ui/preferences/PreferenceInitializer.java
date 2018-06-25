/**
 *  Class: ui.preferences.PreferenceInitializer
 *  Description: This is the Preference Initializer.
 * 
 * 	Author: Peng Cheng Sun
 *  
 *  Modification History
 *  01. 03/20/2012: Code baseline. (V6.0.0)
 *  02. 11/02/2012: Add P_JCL_TEMPLATE_QUERY_DB. (V6.1)
 *  03. 11/07/2012: Add JCL parameters. (V6.1.1)
 */
package net.sourceforge.datamig4zos.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import net.sourceforge.datamig4zos.Activator;
import net.sourceforge.datamig4zos.runtime.IFileNames;
import net.sourceforge.datamig4zos.runtime.IJclDefaults;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		store.setDefault(PreferenceConstants.P_DATA_DIRECTORY, IFileNames.DATA_FOLDER_NAME);
		
		store.setDefault(PreferenceConstants.P_JCL_TEMPLATE_ALLOC_CNTL, IFileNames.JCL_TEMPLATE_ALLOC_CNTL);
		store.setDefault(PreferenceConstants.P_JCL_TEMPLATE_SET_CNTL, IFileNames.JCL_TEMPLATE_SET_CNTL);
		store.setDefault(PreferenceConstants.P_JCL_TEMPLATE_UNLOAD_SOURCE, IFileNames.JCL_TEMPLATE_UNLOAD_SOURCE);
		store.setDefault(PreferenceConstants.P_JCL_TEMPLATE_FTP_DATASET, IFileNames.JCL_TEMPLATE_FTP_DATASET);
		store.setDefault(PreferenceConstants.P_JCL_TEMPLATE_LOAD_TARGET, IFileNames.JCL_TEMPLATE_LOAD_TARGET);
		store.setDefault(PreferenceConstants.P_JCL_TEMPLATE_QUERY_DB, IFileNames.JCL_TEMPLATE_QUERY_DB);	// v6.1
		
		store.setDefault(PreferenceConstants.P_JCL_PARAMETER_DB2LOAD, IJclDefaults.JCL_PARAMETER_DB2LOAD);	// v6.1.1
		store.setDefault(PreferenceConstants.P_JCL_PARAMETER_DSNLOAD, IJclDefaults.JCL_PARAMETER_DSNLOAD);	// v6.1.1
		store.setDefault(PreferenceConstants.P_JCL_PARAMETER_SORTLIB, IJclDefaults.JCL_PARAMETER_SORTLIB);	// v6.1.1
		store.setDefault(PreferenceConstants.P_JCL_PARAMETER_SPACE, IJclDefaults.JCL_PARAMETER_SPACE);		// v6.1.1
		store.setDefault(PreferenceConstants.P_JCL_PARAMETER_UNIT, IJclDefaults.JCL_PARAMETER_UNIT);			// v6.1.1
	}

}
