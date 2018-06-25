/**
 *  Class: provider.ServerExplorerActionProvider
 *  Description: The action provider for Server Explorer.
 * 
 * 	Author: Peng Cheng Sun
 *  
 *  Code originally from
 *  http://scribbledideas.blogspot.com/2006/06/building-common-navigator-_115067357450703178.html
 *  
 *  Modification History
 *  01. 03/21/2011: Code baseline. (V6.0 phase 3)
 *  02. 03/23/2011: Right-click menu enhancements. (V6.0 phase 5)
 *  03. 11/06/2012: Add QueryTableDataAction. (V6.1)
 *  04. 11/07/2012: Add OpenQueryEditorAction. (V6.1.1)
 */
package net.sourceforge.datamig4zos.ui.providers;

import net.sourceforge.datamig4zos.actions.AddNewServerAction;
import net.sourceforge.datamig4zos.actions.DeleteExistingServerAction;
import net.sourceforge.datamig4zos.actions.ItemReceiveFromAction;
import net.sourceforge.datamig4zos.actions.ItemSendToAction;
import net.sourceforge.datamig4zos.actions.OpenQueryEditorAction;
import net.sourceforge.datamig4zos.actions.OpenServerEditorAction;
import net.sourceforge.datamig4zos.actions.QueryTableDataAction;
import net.sourceforge.datamig4zos.actions.RefreshServerExplorerAction;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;


/**
 * @author SunPC
 *
 */
public class ServerExplorerActionProvider extends CommonActionProvider {
	
	private OpenServerEditorAction openServerEditorAction;

	private AddNewServerAction addNewServerAction;
	private DeleteExistingServerAction deleteExistingServerAction;
	
	private RefreshServerExplorerAction refreshServerExplorerAction;
	
	private ItemSendToAction itemSendToAction;
	private ItemReceiveFromAction itemReceiveFromAction;

	private QueryTableDataAction queryTableDataActionSample;	// v6.1
	private QueryTableDataAction queryTableDataActionFull;		// v6.1
	private QueryTableDataAction queryTableDataActionCount;		// v6.1
	
	private OpenQueryEditorAction openQueryEditorAction;		// v6.1.1

    @Override
    public void init(ICommonActionExtensionSite aSite) {
        super.init(aSite);
        
        ICommonViewerSite viewSite = aSite.getViewSite();
        if (viewSite instanceof ICommonViewerWorkbenchSite) {
        	ICommonViewerWorkbenchSite workbenchSite = (ICommonViewerWorkbenchSite) viewSite;
        	IWorkbenchWindow window = workbenchSite.getWorkbenchWindow();
        	ISelectionProvider provider = workbenchSite.getSelectionProvider();
        	
        	openServerEditorAction = new OpenServerEditorAction(window, provider);
        	addNewServerAction = new AddNewServerAction(window, provider);
        	deleteExistingServerAction = new DeleteExistingServerAction(window, provider);
        	refreshServerExplorerAction = new RefreshServerExplorerAction(window, provider);
        	itemSendToAction = new ItemSendToAction(window, provider);
        	itemReceiveFromAction = new ItemReceiveFromAction(window, provider);
        	queryTableDataActionSample = new QueryTableDataAction(window, provider, 
        			QueryTableDataAction.SAMPLE_DATA_MODE);		// v6.1
        	queryTableDataActionFull = new QueryTableDataAction(window, provider, 
        			QueryTableDataAction.FULL_DATA_MODE);		// v6.1
        	queryTableDataActionCount = new QueryTableDataAction(window, provider, 
        			QueryTableDataAction.RECORD_COUNT_MODE);	// v6.1
        	openQueryEditorAction = new OpenQueryEditorAction(window, provider);	// v6.1.1
        }
    }


    // double click method
    @Override
    public void fillActionBars(IActionBars actionBars) {
    	if (openServerEditorAction.isEnabled()) {
    		actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openServerEditorAction);
        }
    }


    // right click pop up menu
    @Override
    public void fillContextMenu(IMenuManager menu) {
    	
    	if (openServerEditorAction.isEnabled()) {
    		menu.add(openServerEditorAction);
    	}
		
    	menu.add(new Separator());
    	
    	if (addNewServerAction.isEnabled()) {
    		menu.add(addNewServerAction);
    	}
    	
    	if (deleteExistingServerAction.isEnabled()) {
    		menu.add(deleteExistingServerAction);
    	}
		
    	menu.add(new Separator());
		
    	if (refreshServerExplorerAction.isEnabled()) {
    		menu.add(refreshServerExplorerAction);
    	}
		
    	menu.add(new Separator());
    	
    	if (itemSendToAction.isEnabled()) {
    		menu.add(itemSendToAction);
    	}
    	
    	if (itemReceiveFromAction.isEnabled()) {
    		menu.add(itemReceiveFromAction);
    	}
		
    	menu.add(new Separator());
    	
    	if (openQueryEditorAction.isEnabled()) {			// v6.1.1
    		menu.add(openQueryEditorAction);
    	}
    	
    	if (queryTableDataActionSample.isEnabled()) {		// v6.1
    		menu.add(queryTableDataActionSample);
    	}
    	
    	if (queryTableDataActionFull.isEnabled()) {			// v6.1
    		menu.add(queryTableDataActionFull);
    	}
    	
    	if (queryTableDataActionCount.isEnabled()) {		// v6.1
    		menu.add(queryTableDataActionCount);
    	}
		
    	menu.add(new Separator());
    }
}
