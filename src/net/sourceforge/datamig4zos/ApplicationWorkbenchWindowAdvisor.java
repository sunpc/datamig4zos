/**
 *  Class: ApplicationWorkbenchWindowAdvisor
 *  Description: The workbench window advisor.
 * 
 * 	Author: Peng Cheng Sun
 *  
 *  Modification History
 *  01. 06/18/2010: Code baseline. (V6.0 phase 1)
 *  02. 03/18/2011: No object and shutdown required for Hsql class. (V6.0 phase 2)
 *  03. 03/22/2011: Change the initial size from 600,400 to 800,600. (V6.0 phase 4)
 *  04. 04/04/2011: Use DmigHsqlConn to replace the new HsqlConn object to save the Hsql sessions. (V6.0 beta 1)
 *  05. 11/07/2012: Run DataInstall to setup the system. (V6.1.1)
 */
package net.sourceforge.datamig4zos;

import net.sourceforge.datamig4zos.install.DataInstall;

import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;



public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

    public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new ApplicationActionBarAdvisor(configurer);
    }
    
    public void preWindowOpen() {
    	//setup window
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setInitialSize(new Point(800, 600));
        configurer.setShowCoolBar(true);
        configurer.setShowStatusLine(true);
        configurer.setShowProgressIndicator(true);
        
        //init hsql connection
		try {
			// v6.1.1: Run DataInstall
			DataInstall.runInstall();			
			// v6.1.1: Move the code into DataInstall
			// v6.0 beta 1: Use DmigHsqlConn
			// DmigHsqlConn.setConn(new HsqlConn());	// v6.0 phase 2: no need to create an object.			
	        // conn.shutdown();							// v6.0 phase 2: no need to shut down.
		} catch (Exception e) {
			e.printStackTrace();
		}
    }    

    public void postWindowCreate() {
    	getWindowConfigurer().getWindow().getShell().setMaximized(true);
    }
    
}
