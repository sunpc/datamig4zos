/**
 *  Class: actions.ItemSendToAction
 *  Description: Open the Request Wizard to receive an item from somewhere
 * 
 * 	Author: Peng Cheng Sun
 * 
 * 	Modification History
 *  01. 03/23/2011: Code baseline. (V6.0 phase 5)
 */
package net.sourceforge.datamig4zos.actions;

import net.sourceforge.datamig4zos.objects.DB2Schema;
import net.sourceforge.datamig4zos.objects.DB2Table;
import net.sourceforge.datamig4zos.objects.MFServer;
import net.sourceforge.datamig4zos.runtime.ICommandIds;
import net.sourceforge.datamig4zos.ui.wizards.NewRequestWizard;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;



public class ItemReceiveFromAction extends Action implements IAction {

    private final IWorkbenchWindow window;
	private final ISelectionProvider provider;
	
	private Object obj = null;

    public ItemReceiveFromAction(IWorkbenchWindow window, ISelectionProvider provider) {
    	this.window = window;
		this.provider = provider;
		setText("Receive from...");
        // The id is used to refer to the action in a menu or toolbar
        setId(ICommandIds.CMD_ITEM_RECEIVE_FROM);
        // Associate the action with a pre-defined command, to allow key bindings.
        setActionDefinitionId(ICommandIds.CMD_ITEM_RECEIVE_FROM);
        // setImageDescriptor(Activator.getImageDescriptor("/icons/sample2.gif"));
        setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_BACK));
    }

    @Override
    public void run() {
    	if(window != null) {
    		NewRequestWizard wizard = new NewRequestWizard(obj, "R");
    		WizardDialog dialog = new WizardDialog(null, wizard);
    		dialog.open();
    	}
    }
	
	@Override
	public boolean isEnabled() {
		ISelection selection = provider.getSelection();
		if (!selection.isEmpty()) {
			IStructuredSelection sSelection = (IStructuredSelection) selection;
			if (sSelection.size() == 1) {
				if (sSelection.getFirstElement() instanceof MFServer
						|| sSelection.getFirstElement() instanceof DB2Schema
						|| sSelection.getFirstElement() instanceof DB2Table) {
					obj = sSelection.getFirstElement();
					return true;
				}
			}
		}
		return false;
	}
}