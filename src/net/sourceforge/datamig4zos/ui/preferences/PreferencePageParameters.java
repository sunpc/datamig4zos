/**
 *  Class: ui.preferences.PreferencePageParameters
 *  Description: This is the parameters Preference Page.
 * 
 * 	Author: Peng Cheng Sun
 *  
 *  Modification History
 *  01. 11/07/2012: Code baseline. (V6.1.1)
 */
package net.sourceforge.datamig4zos.ui.preferences;

import net.sourceforge.datamig4zos.Activator;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class PreferencePageParameters
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public PreferencePageParameters() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Specify the default JCL parameters.");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		addField(new StringFieldEditor(PreferenceConstants.P_JCL_PARAMETER_DB2LOAD, 
				"DB2 load library:", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.P_JCL_PARAMETER_DSNLOAD, 
				"DSN load library:", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.P_JCL_PARAMETER_SORTLIB, 
				"Sort library:", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.P_JCL_PARAMETER_SPACE, 
				"Default space allocation:", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.P_JCL_PARAMETER_UNIT, 
				"Default DASD unit:", getFieldEditorParent()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {

	}
	
}