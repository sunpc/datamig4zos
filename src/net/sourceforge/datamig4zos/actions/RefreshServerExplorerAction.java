/**
 *  Class: actions.RefreshServerExplorerAction
 *  Description: Refresh the server explorer.
 * 
 * 	Author: Peng Cheng Sun
 * 
 *	Modification History
 *	01. 03/23/2010: Code baseline. (V6.0 phase 5)
 */
package net.sourceforge.datamig4zos.actions;

import net.sourceforge.datamig4zos.objects.DB2Schema;
import net.sourceforge.datamig4zos.objects.MFServer;
import net.sourceforge.datamig4zos.runtime.ICommandIds;
import net.sourceforge.datamig4zos.runtime.IViewIds;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.navigator.CommonNavigator;


public class RefreshServerExplorerAction extends Action implements IAction {
	
	private final IWorkbenchWindow window;
	private final ISelectionProvider provider;
	
	private Object obj = null;
	
	public RefreshServerExplorerAction(IWorkbenchWindow window, ISelectionProvider provider) {
		this.window = window;
		this.provider = provider;
		setText("Refresh");
		// The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_REFRESH_SERVER_EXPLORER);
		// Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(ICommandIds.CMD_REFRESH_SERVER_EXPLORER);
		// setImageDescriptor(com.datamig.zos.Activator.getImageDescriptor("/icons/sample2.gif"));
	}
	
	@Override
	public void run() {
		if(window != null && isEnabled()) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				IViewPart view = page.findView(IViewIds.VIEW_SERVER_EXPLORER);
		        if (view != null && view instanceof CommonNavigator) {
		        	CommonNavigator instance = ((CommonNavigator) view);
		        	if (instance != null) {
		        		instance.getCommonViewer().refresh(obj, true);
		        		// instance.getCommonViewer().setInput(server.getRoot());
		        		instance.getCommonViewer().expandToLevel(2);
				    }
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
				if (sSelection.getFirstElement() instanceof MFServer
						|| sSelection.getFirstElement() instanceof DB2Schema) {
					obj = sSelection.getFirstElement();
					return true;
				}
			}
		}
		return false;
	}

}