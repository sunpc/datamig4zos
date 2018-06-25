/**
 *  Class: Perspective
 *  Description: The RCP Perspective
 * 
 * 	Author: Peng Cheng Sun
 * 
 * 	Modification History
 *  01. 06/18/2010: Code baseline. (V6.0 phase 1)
 *  02. 03/18/2011: Set ServerExplorerView as close-able. (V6.0 phase 2)
 *  03. 03/19/2011: Use addShowViewShortcut to display views. (V6.0 phase 2)
 *  04. 03/22/2011: Change bottom view size from 0.35f to 0.5f. (V6.0 phase 4)
 */
package net.sourceforge.datamig4zos;

import net.sourceforge.datamig4zos.runtime.IViewIds;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;


public class Perspective implements IPerspectiveFactory {

	/**
	 * The ID of the perspective as specified in the extension.
	 */
	public static final String ID = "net.sourceforge.datamig4zos.Perspective";

	public void createInitialLayout(IPageLayout layout) {
		// create the editor area
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
				
		// create console and progress views on the bottom
		// layout.addStandaloneView(ConsoleView.ID, true, IPageLayout.BOTTOM,
		// 0.35f, editorArea);
		IFolderLayout folder = layout.createFolder("CPbar", IPageLayout.BOTTOM,
				0.5f, editorArea);			// v6.0.phase4
		// folder.addPlaceholder(View.ID + ":*");
		folder.addView(IViewIds.VIEW_CONSOLE);
		layout.addShowViewShortcut(IViewIds.VIEW_CONSOLE);
		folder.addView(IViewIds.VIEW_PROGRESS);
		layout.addShowViewShortcut(IViewIds.VIEW_PROGRESS);

		// create the server explorer view on the left
		layout.addStandaloneView(IViewIds.VIEW_SERVER_EXPLORER, true, IPageLayout.LEFT,
				0.35f, editorArea);
		//layout.getViewLayout(ServerExplorerView.ID).setCloseable(false);
		layout.addShowViewShortcut(IViewIds.VIEW_SERVER_EXPLORER);

	}
}
