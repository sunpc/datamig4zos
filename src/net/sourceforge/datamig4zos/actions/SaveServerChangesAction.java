/**
 *  Class: actions.SaveServerChangesAction
 *  Description: Save the changes to a server.
 * 
 * 	Author: Peng Cheng Sun
 *  
 * 	Modification History
 *  01. 03/22/2011: Code baseline. (V6.0 phase 4)
 *  Code mainly copied from the original ServerEditor.class.
 *  02. 03/24/2011: Refresh the server only instead of refresh the entire explorer. (V6.0 phase 5)
 *  This could happen because the bugs have been fixed in MFServer.class and DB2Schema.class.
 *  03. 02/29/2012: Refresh the entire explorer if a new server is being added. (V6.0.0)
 *  04. 03/09/2012: Remove the message dialog if save is successful. (V6.0.0)
 *  05. 08/08/2013: Call setPageFormText() when saving. (V6.3)
 */
package net.sourceforge.datamig4zos.actions;

import net.sourceforge.datamig4zos.Activator;
import net.sourceforge.datamig4zos.objects.MFServer;
import net.sourceforge.datamig4zos.runtime.ICommandIds;
import net.sourceforge.datamig4zos.runtime.IViewIds;
import net.sourceforge.datamig4zos.ui.editors.ServerEditor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;


/**
 * @author SunPC
 *
 */
public class SaveServerChangesAction extends Action implements IAction {
	
	private ServerEditor editor;
	private MFServer server;
	
	public SaveServerChangesAction(ServerEditor editor, MFServer server) {
		super("Save Server Changes");
		this.editor = editor;
		this.server = server;
        // The id is used to refer to the action in a menu or toolbar
        setId(ICommandIds.CMD_SAVE_SERVER_CHANGES);
        // Associate the action with a pre-defined command, to allow key bindings.
        setActionDefinitionId(ICommandIds.CMD_SAVE_SERVER_CHANGES);
        setImageDescriptor(Activator.getImageDescriptor("/images/save_action.gif"));
	}
	
	@Override
	public void run() {
		// get the old and new server names - v6.0.phase4
		String old_server_name = editor.getPartName();
		String new_server_name = server.getServerName();
		
		try {
			if (!old_server_name.isEmpty()) {		// v6.0.0
				// reset the server to the old - v6.0.phase4
				server.setServerName(old_server_name);
				
				// delete the old server first - v6.0.phase3
				server.deleteServerFromHsql();
			}
			
			// reset the server name to new - v6.0.phase4
			server.setServerName(new_server_name);
			
			// add the new server
			server.insertServerIntoHsql();
			
			// notification	- removed in v6.0.0
			// MessageDialog.openInformation(Display.getDefault()
			//		.getActiveShell(), "Success", "Server saved successfully!");
			
			// reset the navigator view (Server Explorer) - v6.0.phase4
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		    if (page != null) {
				IViewPart view = page.findView(IViewIds.VIEW_SERVER_EXPLORER);
		        if (view != null && view instanceof CommonNavigator) {
		        	CommonNavigator instance = ((CommonNavigator) view);
		        	if (instance != null) {
		        		if (old_server_name.isEmpty()) {	// v6.0.0
		        			instance.getCommonViewer().setInput(server.getRoot());
		        			instance.getCommonViewer().expandToLevel(2);
		        		} else {
		        			instance.getCommonViewer().refresh(server, true);	// v6.0.phase5
		        		}
				    }
		        }
		    }
			
			// reset the editor text - v6.0.phase3
			editor.setPartName(new_server_name);
			editor.setPageFormText();		// v6.3
			
		} catch (Exception ex) {
			// print the stack trace
			ex.printStackTrace();

			// try to restore the server info
			try {
				server.setServerName(old_server_name);
				server.insertServerIntoHsql();
			} catch (Exception ex1) {
				ex1.printStackTrace();
			}
			
			// notification
			MessageDialog.openError(Display.getDefault()
					.getActiveShell(), "Error", "Unable to save the server!");
		}
	}
}
