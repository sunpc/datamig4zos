/**
 *  Class: actions.DeleteExistingServerAction
 *  Description: Delete an existing Server.
 * 
 * 	Author: Peng Cheng Sun
 * 
 *	Modification History
 *	01. 03/23/2010: Code baseline. (V6.0 phase 5)
 */
package net.sourceforge.datamig4zos.actions;

import net.sourceforge.datamig4zos.objects.MFServer;
import net.sourceforge.datamig4zos.runtime.ICommandIds;
import net.sourceforge.datamig4zos.runtime.IViewIds;
import net.sourceforge.datamig4zos.ui.editors.ServerEditorInput;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;


public class DeleteExistingServerAction extends Action implements IAction {
	
	private final IWorkbenchWindow window;
	private final ISelectionProvider provider;
	
	private Object obj;
	
	public DeleteExistingServerAction(IWorkbenchWindow window, ISelectionProvider provider) {
		this.window = window;
		this.provider = provider;
		setText("Delete");
		// The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_DELETE_EXISTING_SERVER);
		// Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(ICommandIds.CMD_DELETE_EXISTING_SERVER);
		// setImageDescriptor(com.datamig.zos.Activator.getImageDescriptor("/icons/sample2.gif"));
		setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
	}
	
	@Override
	public void run() {
		if(window != null && isEnabled()) {
			IWorkbenchPage page = window.getActivePage();
			
			// confirm with the user
			if (obj instanceof MFServer
					&& MessageDialog.openQuestion(Display.getDefault().getActiveShell(), 
					"Confirm Delete", "Are you sure you want to delete server " + ((MFServer) obj).getServerName() + "?")) {
				
				// delete the server
				if (page != null) {
					// define the editor input
					ServerEditorInput input = new ServerEditorInput((MFServer) obj);
					
					// close the editor if opened
					if (page.findEditor(input) != null) {
						page.closeEditor(page.findEditor(input), false);
					}
					
					// delete the server from Hsql db
					try {
						((MFServer) obj).deleteServerFromHsql();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					
					// refresh the server explorer
					IViewPart view = page.findView(IViewIds.VIEW_SERVER_EXPLORER);
			        if (view != null && view instanceof CommonNavigator) {
			        	CommonNavigator instance = ((CommonNavigator) view);
			        	if (instance != null) {
			        		instance.getCommonViewer().setInput(((MFServer) obj).getRoot());
			        		instance.getCommonViewer().expandToLevel(2);
					    }
			        }
			        
			        // dispose the server
			        obj = null;
			    }
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
					obj = sSelection.getFirstElement();
					return true;
				}
			}
		}
		return false;
	}

}