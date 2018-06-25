/**
 *  Class: actions.OpenRequestWizardAction
 *  Description: Start a new Request Wizard.
 * 
 * 	Author: Peng Cheng Sun
 * 
 * 	Modification History
 *  01. 03/18/2011: Code baseline. (V6.0 phase 2)
 *  02. 03/21/2011: Implements IAction. (V6.0 phase 3)
 */
package net.sourceforge.datamig4zos.actions;

import net.sourceforge.datamig4zos.Activator;
import net.sourceforge.datamig4zos.runtime.ICommandIds;
import net.sourceforge.datamig4zos.ui.wizards.NewRequestWizard;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;



public class OpenRequestWizardAction extends Action implements IAction {

    private final IWorkbenchWindow window;

    public OpenRequestWizardAction(IWorkbenchWindow window) {
        super("&New Data Migration Request...");
        this.window = window;
        // The id is used to refer to the action in a menu or toolbar
        setId(ICommandIds.CMD_OPEN_REQUEST_WIZARD);
        // Associate the action with a pre-defined command, to allow key bindings.
        setActionDefinitionId(ICommandIds.CMD_OPEN_REQUEST_WIZARD);
        setImageDescriptor(Activator.getImageDescriptor("/images/new_request.gif"));
    }

    @Override
    public void run() {
    	if(window != null) {
    		//MessageDialog.openInformation(window.getShell(), "Open", "Open Message Dialog!");
    		NewRequestWizard wizard = new NewRequestWizard();
    		WizardDialog dialog = new WizardDialog(null, wizard);
    		dialog.open();
    	}
    }
}