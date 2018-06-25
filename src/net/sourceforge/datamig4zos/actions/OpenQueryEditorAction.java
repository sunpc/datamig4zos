/**
 *  Class: actions.OpenQueryEditorAction
 *  Description: Start a new Query Editor.
 * 
 * 	Author: Peng Cheng Sun
 * 
 * 	Modification History
 *  01. 11/05/2012: Code baseline. (V6.1)
 *  02. 11/07/2012: Add provider. (V6.1.1)
 */
package net.sourceforge.datamig4zos.actions;

import net.sourceforge.datamig4zos.Activator;
import net.sourceforge.datamig4zos.objects.DB2Schema;
import net.sourceforge.datamig4zos.objects.MFServer;
import net.sourceforge.datamig4zos.runtime.ICommandIds;
import net.sourceforge.datamig4zos.runtime.IEditorIds;
import net.sourceforge.datamig4zos.ui.editors.QueryEditorInput;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;

public class OpenQueryEditorAction extends Action implements IAction {
	
	private final IWorkbenchWindow window;
	private final ISelectionProvider provider;
	
	private Object obj;
	private DB2Schema schema;
	
	private QueryEditorInput editor_input;
	
	public OpenQueryEditorAction(IWorkbenchWindow window) {		// v6.1.1
		this(window, null);
	}

	public OpenQueryEditorAction(IWorkbenchWindow window, ISelectionProvider provider) {	// v6.1.1
		this.window = window;
		this.provider = provider;
		
		if (provider == null)
			setText("New Database Query...");
		else
			setText("Query...");
		
        // The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_OPEN_QUERY_EDITOR);
        // Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(ICommandIds.CMD_OPEN_QUERY_EDITOR);
		setImageDescriptor(Activator.getImageDescriptor("/images/new_query.gif"));
	}
	
	@Override
	public void run() {
		if(window != null) {
			try {
				// define the editor input
				editor_input = new QueryEditorInput();
				
				// set up the schema - v6.1.1
				if (obj instanceof DB2Schema) {
					schema = (DB2Schema) obj;
				} else if (obj instanceof MFServer) {
					schema = new DB2Schema("", "", "", (MFServer) obj);
				} else {
					schema = null;
				}
				
				editor_input.setSchema(schema);
				
				// open the editor
				window.getActivePage().openEditor(editor_input, IEditorIds.EDITOR_QUERY);
			} catch (Exception e) {
				MessageDialog.openError(window.getShell(), "Error", "Error opening editor: " + e.getMessage());
			}
		}
	}
	
	@Override
	public boolean isEnabled() {	// v6.1.1
		if (provider == null)
			return true;
		
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