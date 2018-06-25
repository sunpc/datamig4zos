/**
 *  Class: actions.SaveQueryResultsAction
 *  Description: Save the query results.
 * 
 * 	Author: Peng Cheng Sun
 *  
 * 	Modification History
 *  01. 11/06/2012: Code baseline. (V6.1)
 */
package net.sourceforge.datamig4zos.actions;

import java.io.File;

import net.sourceforge.datamig4zos.Activator;
import net.sourceforge.datamig4zos.runtime.ICommandIds;
import net.sourceforge.datamig4zos.util.TextProcessor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;


/**
 * @author SunPC
 *
 */
public class SaveQueryResultsAction extends Action implements IAction {
	
	private Text txt_results;
	
	public SaveQueryResultsAction(Text txtResults) {
		super("Save Query Results");
		
		// Set the private variables
		txt_results = txtResults;
		
		// The id is used to refer to the action in a menu or toolbar
        setId(ICommandIds.CMD_SAVE_SERVER_CHANGES);
        // Associate the action with a pre-defined command, to allow key bindings.
        setActionDefinitionId(ICommandIds.CMD_SAVE_SERVER_CHANGES);
        setImageDescriptor(Activator.getImageDescriptor("/images/save_action.gif"));
	}
	
	@Override
	public void run() {		
		// verify the query results before save
		if (txt_results.getText().trim().isEmpty()) {
			MessageDialog.openError(Display.getDefault().getActiveShell(),
					"Error", "No query results available!");
			return;
		}
		
		// initialize the file name
		String file_name = "";
		
		while (file_name.isEmpty()) {			
			// open file save dialog
			FileDialog dialog = new FileDialog(Display.getDefault().getActiveShell(), SWT.SAVE);
			dialog.setFilterNames(new String[] {"Query Results (*.sql)","Text Documents (*.txt)", "All files (*.*)"});
			dialog.setFilterExtensions(new String[]{"*.sql", "*.txt", "*.*"});
					
			// choose the file
			file_name = dialog.open();
			
			// verify the file name
			if (file_name == null)
				break;
			
			// determine if the file exists
			File file = new File(file_name);
			if (file.exists()) {
				if (!MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
						"Confirm Save",
						file.getName() + " already exists.\nDo you want to replace it?"))
					file_name = "";
			}	
			
		}			
		
		// save the file		
		try {
			if (file_name != null && !file_name.isEmpty())
				TextProcessor.writeFile(txt_results.getText(), file_name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
