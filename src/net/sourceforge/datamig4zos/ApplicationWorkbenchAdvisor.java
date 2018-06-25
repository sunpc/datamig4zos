/**
 *  Class: ApplicationWorkbenchAdvisor
 *  Description: The workbench advisor.
 *  
 *  Author: Peng Cheng Sun
 * 
 *  Modification History
 *  01. 06/18/2010: Code baseline. (V6.0 phase 1)
 */
package net.sourceforge.datamig4zos;

import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

/**
 * This workbench advisor creates the window advisor, and specifies
 * the perspective id for the initial window.
 */
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {
	
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        return new ApplicationWorkbenchWindowAdvisor(configurer);
    }

	public String getInitialWindowPerspectiveId() {
		return Perspective.ID;
	}
	
}
