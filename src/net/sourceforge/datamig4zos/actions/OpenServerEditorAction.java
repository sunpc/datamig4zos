/**
 *  Class: actions.OpenServerEditorAction
 *  Description: Start a new Server Editor.
 * 
 * 	Author: Peng Cheng Sun
 * 
 *	Modification History
 *	01. 06/18/2010: Code baseline. (V6.0 phase 1)
 *	02. 03/12/2011: Rename from CallEditor and misc changes in execute() method. (V6.0 phase 2)
 *	03. 03/21/2011: Rewrite this class to cater for the new Server Explorer design. (V6.0 phase 3) 
 *		1) Extends Action instead of AbstractHandler.
 *		2) Add isEnabled() method.
 *	04. 03/23/2011: Allow to open the editor on a DB2Schema object. (V6.0 phase 5)
 */
package net.sourceforge.datamig4zos.actions;

import net.sourceforge.datamig4zos.objects.DB2Schema;
import net.sourceforge.datamig4zos.objects.MFServer;
import net.sourceforge.datamig4zos.runtime.ICommandIds;
import net.sourceforge.datamig4zos.runtime.IEditorIds;
import net.sourceforge.datamig4zos.ui.editors.ServerEditor;
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


public class OpenServerEditorAction extends Action implements IAction {
	
	private final IWorkbenchWindow window;
	private final ISelectionProvider provider;
	
	private ServerEditorInput input;
	
	private Object obj;
	
	public OpenServerEditorAction(IWorkbenchWindow window, ISelectionProvider provider) {
		this.window = window;
		this.provider = provider;
		setText("Open");
		// The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_OPEN_SERVER_EDITOR);
		// Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(ICommandIds.CMD_OPEN_SERVER_EDITOR);
		// setImageDescriptor(com.datamig.zos.Activator.getImageDescriptor("/icons/sample2.gif"));
	}
	
	@Override
	public void run() {
		if(window != null && isEnabled()) {
			try {
				// define the editor input - v6.0.phase5
				if (obj instanceof MFServer) {
					input = new ServerEditorInput((MFServer) obj);
				} else if (obj instanceof DB2Schema) {
					input = new ServerEditorInput((DB2Schema) obj);
				}
				
				// open the editor
				IWorkbenchPage page = window.getActivePage();
				if (input != null) {
					// if editor is already open
					if (page.findEditor(input) != null) {
						// bring the editor to the top.
						page.bringToTop(page.findEditor(input));
						
						// active the page - v6.0.phase5
						((ServerEditor) page.findEditor(input)).setActivePage(obj);
					} else {
						// open the editor but not activate it,
						// if activate it, the double-click does not work fine.
						page.openEditor(input, IEditorIds.EDITOR_SERVER, false);
					}
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