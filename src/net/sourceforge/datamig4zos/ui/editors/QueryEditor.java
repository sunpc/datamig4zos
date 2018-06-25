/**
 *  Class: ui.editors.QueryEditor
 *  Description: This is the Query Editor.
 * 
 * 	Author: Peng Cheng Sun
 * 
 *  Modification History
 *  01. 11/05/2012: Code baseline. (V6.1)
 */
package net.sourceforge.datamig4zos.ui.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.MultiPageEditorPart;


/**
 * The Server Editor has 2 pages:
 * <ul>
 * <li>page 1 - User query form
 * <li>page 2 - Query results
 * </ul>
 */
public class QueryEditor extends MultiPageEditorPart {

	public QueryEditor() {
		super();
	}
	
	/**
	 * Creates the pages of the multi-page editor.
	 */
	protected void createPages() {
		try {
			// set pages
			setPageText(addPage(new QueryEditorPageForm(this), this.getEditorInput()), "Query");
			setPageText(addPage(new QueryEditorPageResults(this), this.getEditorInput()), "Results");
			
			// determine results
			if (!((QueryEditorInput) this.getEditorInput()).getQueryResults().isEmpty()) {
				setPageResultsActive();
			}
			
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The <code>MultiPageEditorPart</code> implementation of this
	 * <code>IWorkbenchPart</code> method disposes all nested editors.
	 * Subclasses may extend.
	 */
	public void dispose() {
		super.dispose();
	}

	/**
	 * Saves the multi-page editor's document.
	 */
	public void doSave(IProgressMonitor monitor) {
		// getEditor(0).doSave(monitor);
	}

	/**
	 * Saves the multi-page editor's document as another file. Also updates the
	 * text for page 0's tab, and updates this multi-page editor's input to
	 * correspond to the nested editor's.
	 */
	public void doSaveAs() {

	}

	/**
	 * The <code>MultiPageEditorExample</code> implementation of this method
	 * checks that the input is an instance of <code>IFileEditorInput</code>.
	 */
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		setPartName(input.getName());
	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart.
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * Sets part name.
	 */
	public void setPartName(String partName) {
		super.setPartName(partName);
	}

	/**
	 * Sets query results as active.
	 */
	public void setPageResultsActive() {
		setActivePage(1);
		getEditor(1).setFocus();
	}

}
