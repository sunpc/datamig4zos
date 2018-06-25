/**
 *  Class: ApplicationActionBarAdvisor
 *  Description: The menu bars.
 * 
 * 	Author: Peng Cheng Sun
 *  
 *  Modification History
 *  01. 06/18/2010: Code baseline. (V6.0 phase 1)
 *  02. 03/18/2011: Functional enhancements. (V6.0 phase 2)
 *  03. 03/23/2011: Remove the Window menu. (V6.0 phase 4)
 *  04. 03/23/2011: Remove the Edit menu. (V6.0 phase 4)
 *  05. 03/19/2012: Add Help Search menu. (V6.0.0)
 *  06. 11/05/2012: Add OpenQueryEditorAction. (V6.1)
 */
package net.sourceforge.datamig4zos;

import net.sourceforge.datamig4zos.actions.OpenQueryEditorAction;
import net.sourceforge.datamig4zos.actions.OpenRequestWizardAction;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;


/**
 * An action bar advisor is responsible for creating, adding, and disposing of the
 * actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

    // Actions - important to allocate these only in makeActions, and then use them
    // in the fill methods.  This ensures that the actions aren't recreated
    // when fillActionBars is called with FILL_PROXY.
	
	// File
	private OpenRequestWizardAction openRequestWizardAction;
	private OpenQueryEditorAction openQueryEditorAction;		// v6.1
    private IWorkbenchAction closeAction;
    private IWorkbenchAction closeAllAction;
    private IWorkbenchAction exitAction;
    
    // View
    private IContributionItem showViewItem;
    
    // Tools
    private IWorkbenchAction preferencesAction;
    
    // Help
    private IWorkbenchAction helpContentsAction;
    private IWorkbenchAction helpSearchAction;		// v6.0.0
    private IWorkbenchAction aboutAction;
    
    public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
    }
    
    protected void makeActions(final IWorkbenchWindow window) {
        // Creates the actions and registers them.
        // Registering is needed to ensure that key bindings work.
        // The corresponding commands keybindings are defined in the plugin.xml file.
        // Registering also provides automatic disposal of the actions when
        // the window is closed.
    	
    	// File
        openRequestWizardAction = new OpenRequestWizardAction(window);
        register(openRequestWizardAction);

        openQueryEditorAction = new OpenQueryEditorAction(window);		// v6.1
        register(openQueryEditorAction);								// v6.1
        
        closeAction = ActionFactory.CLOSE.create(window);
        register(closeAction);
        
        closeAllAction = ActionFactory.CLOSE_ALL.create(window);
        register(closeAllAction);

        exitAction = ActionFactory.QUIT.create(window);
        register(exitAction);
  
        // View
        showViewItem = ContributionItemFactory.VIEWS_SHORTLIST.create(window);
        
        // Tools
        preferencesAction = ActionFactory.PREFERENCES.create(window);
        register(preferencesAction);
        
        // Help
        helpContentsAction = ActionFactory.HELP_CONTENTS.create(window);
        register(helpContentsAction);

        helpSearchAction = ActionFactory.HELP_SEARCH.create(window);	// v6.0.0
        register(helpSearchAction);										// v6.0.0
        
        aboutAction = ActionFactory.ABOUT.create(window);
        register(aboutAction);
        
    }
    
    protected void fillMenuBar(IMenuManager menuBar) {
    	// Define the menus
        MenuManager fileMenu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
        MenuManager viewMenu = new MenuManager("&View", IWorkbenchActionConstants.SHOW_EXT);
        MenuManager toolsMenu = new MenuManager("&Tools", IWorkbenchActionConstants.M_PROJECT);
        MenuManager helpMenu = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP);
        
        // File
        fileMenu.add(openRequestWizardAction);
        fileMenu.add(openQueryEditorAction);		// v6.1
        fileMenu.add(new Separator());
        fileMenu.add(closeAction);
        fileMenu.add(closeAllAction);
        fileMenu.add(new Separator());
        fileMenu.add(exitAction);
        
        // View
        viewMenu.add(showViewItem);
        
        // Tools
        toolsMenu.add(preferencesAction);
        
        // Help
        helpMenu.add(helpContentsAction);
        helpMenu.add(helpSearchAction);
        helpMenu.add(new Separator());
        helpMenu.add(aboutAction);

        // Add a group marker indicating where action set menus will appear.
        menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        menuBar.add(fileMenu);
        
        menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        menuBar.add(viewMenu);
        
        menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        menuBar.add(toolsMenu);
        
        menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        menuBar.add(helpMenu);
    }
    
    protected void fillCoolBar(ICoolBarManager coolBar) {
        IToolBarManager toolbar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
        coolBar.add(new ToolBarContributionItem(toolbar, "main"));
        toolbar.add(openRequestWizardAction);
        toolbar.add(openQueryEditorAction);			// v6.1
    }
}
