/**
 *  Class: panels.ServerExplorerView
 *  Description: This is the navigation view to display the servers.
 * 
 * 	Author: Peng Cheng Sun
 * 
 *  Modification History
 *  01. 06/18/2010: Code baseline. (V6.0 phase 1)
 *  02. 03/12/2011: Create code in createServerModel() method. (V6.0 phase 2)
 *  03. 03/21/2011: Rewrite this class by extending CommonNavigator instead of ViewPart. (V6.0 phase 3)
 */
package net.sourceforge.datamig4zos.ui.views;

import net.sourceforge.datamig4zos.objects.CNFRoot;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;


public class ServerExplorerView extends CommonNavigator {
	
	protected IAdaptable getInitialInput() {
		return new CNFRoot();
	}
	
	public void createPartControl(Composite aParent) {
		super.createPartControl(aParent);
		getCommonViewer().expandToLevel(2);
	}
	
	protected CommonViewer createCommonViewerObject(Composite aParent) {
		return new CommonViewer(getViewSite().getId(), aParent,
				SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
	}
	
}