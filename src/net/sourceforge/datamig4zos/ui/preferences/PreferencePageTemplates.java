/**
 *  Class: ui.preferences.PreferencePageTemplates
 *  Description: This is the template Preference Page.
 * 
 * 	Author: Peng Cheng Sun
 *  
 *  Modification History
 *  01. 03/20/2012: Code baseline. (V6.0.0)
 *  02. 11/02/2012: Add P_JCL_TEMPLATE_QUERY_DB2. (V6.1)
 *  03. 11/12/2012: Move P_JCL_TEMPLATE_FTP_DATASET to the last. (V6.1.2)
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

public class PreferencePageTemplates
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public PreferencePageTemplates() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Specify the job template file names.");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		addField(new StringFieldEditor(PreferenceConstants.P_JCL_TEMPLATE_ALLOC_CNTL, 
				"Control library allocation JCL template:", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.P_JCL_TEMPLATE_SET_CNTL, 
				"Control member set up JCL template:", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.P_JCL_TEMPLATE_UNLOAD_SOURCE, 
				"Table unload job template:", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.P_JCL_TEMPLATE_LOAD_TARGET, 
				"Table load job template:", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.P_JCL_TEMPLATE_QUERY_DB, 
				"Table query job template:", getFieldEditorParent()));		// v6.1
		addField(new StringFieldEditor(PreferenceConstants.P_JCL_TEMPLATE_FTP_DATASET, 
				"Dataset FTP job template:", getFieldEditorParent()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {

	}
	
}