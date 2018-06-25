/**
 *  Class: actions.AddNewServerAction
 *  Description: Start a new Server Editor for new Server.
 * 
 * 	Author: Peng Cheng Sun
 * 
 *	Modification History
 *	01. 03/23/2011: Code baseline. (V6.0 phase 5)
 *  02. 02/29/2012: Enable the action when nothing is selected. (V6.0.0)
 */
package net.sourceforge.datamig4zos.actions;

import net.sourceforge.datamig4zos.objects.MFServer;
import net.sourceforge.datamig4zos.runtime.ICommandIds;
import net.sourceforge.datamig4zos.runtime.IEditorIds;
import net.sourceforge.datamig4zos.ui.editors.ServerEditorInput;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;


public class AddNewServerAction extends Action implements IAction {
	
	private final IWorkbenchWindow window;
	private final ISelectionProvider provider;
	
	private ServerEditorInput input;
	
	public AddNewServerAction(IWorkbenchWindow window, ISelectionProvider provider) {
		this.window = window;
		this.provider = provider;
		setText("New Server");
		// The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_ADD_NEW_SERVER);
		// Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(ICommandIds.CMD_ADD_NEW_SERVER);
		// setImageDescriptor(com.datamig.zos.Activator.getImageDescriptor("/icons/sample2.gif"));
	}
	
	@Override
	public void run() {
		if(window != null && isEnabled()) {
			try {
				// define the editor input - v6.0.phase5
				try {
					MFServer server = new MFServer();
					input = new ServerEditorInput(server);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
				// open the editor
				IWorkbenchPage page = window.getActivePage();
				if (input != null) {
					page.openEditor(input, IEditorIds.EDITOR_SERVER, false);
				}
				
			} catch (PartInitException e) {
				MessageDialog.openError(window.getShell(), "Error", "Error opening editor: " + e.getMessage());
			}
		}
	}
	
	@Override
	public boolean isEnabled() {
		ISelection selection = provider.getSelection();
		
		if (!selection.isEmpty()) {
			IStructuredSelection sSelection = (IStructuredSelection) selection;
			if (sSelection.size() == 1) {
				if (sSelection.getFirstElement() instanceof MFServer) {
					return true;
				}
			}
		} else {	// v6.0.0
			return true;
		}
		
		return false;
	}

}