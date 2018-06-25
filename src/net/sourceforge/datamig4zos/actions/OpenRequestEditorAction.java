/**
 *  Class: actions.OpenRequestEditorAction
 *  Description: Start a new Request Editor.
 * 
 * 	Author: Peng Cheng Sun
 * 
 * 	Modification History
 *  01. 03/12/2011: Code baseline - renamed from OpenViewAction. (V6.0 phase 2)
 *  02. 03/19/2011: Pass the wizard inputs to the editor input. (V6.0 phase 2)
 *  03. 03/21/2011: Implements IAction. (V6.0 phase 3)
 *  04. 03/22/2011: Add schema names. (V6.0 phase 4)
 *  05. 08/17/2013: Add table_items, dataset_items and table_filter_name. (V6.3)
 */
package net.sourceforge.datamig4zos.actions;

import net.sourceforge.datamig4zos.Activator;
import net.sourceforge.datamig4zos.runtime.ICommandIds;
import net.sourceforge.datamig4zos.runtime.IEditorIds;
import net.sourceforge.datamig4zos.ui.editors.RequestEditorInput;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;




public class OpenRequestEditorAction extends Action implements IAction {
	
	private final IWorkbenchWindow window;
	
	private String desc;
	private String source_server_name;
	private String target_server_name;
	private String source_schema_name;
	private String target_schema_name;
	private String table_items;
	private String dataset_items;
	private String table_filter_name;
	
	private boolean chk_same_table_name;
	private boolean chk_rename_dataset_name;
	
	public OpenRequestEditorAction(IWorkbenchWindow window) {
		this.window = window;
		setText("New Data Migration Request");
        // The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_OPEN_REQUEST_EDITOR);
        // Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(ICommandIds.CMD_OPEN_REQUEST_EDITOR);
		setImageDescriptor(Activator.getImageDescriptor("/images/new_request.gif"));
	}
	
	@Override
	public void run() {
		if(window != null) {
			try {
				// define the editor input
				RequestEditorInput input = new RequestEditorInput();
				input.setDesc(desc);
				input.setSourceServerName(source_server_name);
				input.setTargetServerName(target_server_name);
				input.setSourceSchemaName(source_schema_name);
				input.setTargetSchemaName(target_schema_name);
				input.setTableItems(table_items, table_filter_name);	// v6.3
				input.setDatasetItems(dataset_items);					// v6.3
				input.setChkSameTableName(chk_same_table_name);
				input.setChkRenameDatasetName(chk_rename_dataset_name);
				
				// open the editor
				window.getActivePage().openEditor(input, IEditorIds.EDITOR_REQUEST);
				//window.getActivePage().showView(viewId, Integer.toString(instanceNum++), IWorkbenchPage.VIEW_ACTIVATE);
			} catch (Exception ex) {
				MessageDialog.openError(window.getShell(), "Error", "Error opening editor: " + ex.getMessage());
			}
		}
	}
	
	/**
	 * @param desc the desc to set
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}

	/**
	 * @param sourceServer the source_server to set
	 */
	public void setSourceServerName(String sourceServerName) {
		source_server_name = sourceServerName;
	}

	/**
	 * @param targetServer the target_server to set
	 */
	public void setTargetServerName(String targetServerName) {
		target_server_name = targetServerName;
	}
	
	/**
	 * @param sourceSchemaName the source_schema_name to set
	 */
	public void setSourceSchemaName(String sourceSchemaName) {
		source_schema_name = sourceSchemaName;
	}
	
	/**
	 * @param targetSchemaName the target_schema_name to set
	 */
	public void setTargetSchemaName(String targetSchemaName) {
		target_schema_name = targetSchemaName;
	}

	/**
	 * @param tableItems the table_items to set
	 */
	public void setTableItems(String tableItems) {				// v6.3
		table_items = tableItems;
	}

	/**
	 * @param datasetItems the dataset_items to set
	 */
	public void setDatasetItems(String datasetItems) {			// v6.3
		dataset_items = datasetItems;
	}

	/**
	 * @param tableFilterName the table_filter_name to set
	 */
	public void setTableFilterName(String tableFilterName) {	// v6.3
		table_filter_name = tableFilterName;
	}

	/**
	 * @param chkSameTableName the chk_same_table_name to set
	 */
	public void setChkSameTableName(boolean chkSameTableName) {
		chk_same_table_name = chkSameTableName;
	}

	/**
	 * @param chkRenameDatasetName the chk_rename_dataset_name to set
	 */
	public void setChkRenameDatasetName(boolean chkRenameDatasetName) {
		chk_rename_dataset_name = chkRenameDatasetName;
	}
	
}