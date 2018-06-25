/**
 *  Class: ui.editors.ServerEditor
 *  Description: This is the Server Editor.
 * 
 * 	Author: Peng Cheng Sun
 * 
 *  Modification History
 *  01. 06/18/2010: Code baseline. (V6.0 phase 1)
 *  02. 03/11/2011: Functional enhancements. (V6.0 phase 1)
 *  03. 03/12/2011: Form design enhancements. (V6.0 phase 2)
 *  	1) Change the form title from Server Editor to Server.
 *  	2) Add an expandable help section.
 *  	3) Adjust the table size.
 *  04. 03/18/2011: Form design enhancements. (V6.0 phase 2)
 *  	1) Re-design the layout of the Server section.
 *  	2) Add SWT check and full selection in the table lists.
 *  	3) Add link to remove multiple tables from the list.
 *  05. 03/20/2011: Server save and Table unload actions enhancements. (V6.0 phase 3)
 *  	1) Use deleteServer() and addNewServer() to replace saveServer() to cater for server name and type changes. 
 *  	2) Reset the form after the server is saved to cater for server name and type changes. 
 *  	3) Reset the server after the table unloaded as some attributes may get changed during the unload.
 *  06. 03/21/2011: Collapse damage for new data model objects. (V6.0 phase 3)
 *  07. 03/22/2011: Editor re-designed with multiple pages. (V6.0 phase 4)
 *  08. 03/24/2011: Functional enhancements (V6.0 phase 5) 
 *  	1) Add a new function to set the schema as active.
 *  	2) Add new methods addNewSchema() and deleteExistingSchema().
 *  09. 03/01/2012: Add setActiveSchemaPageText() and updateSchemaTable() function. (V6.0.0)
 *  10. 08/08/2013: Add setPageFormText() function. (V6.3)
 */
package net.sourceforge.datamig4zos.ui.editors;

import net.sourceforge.datamig4zos.objects.DB2Schema;
import net.sourceforge.datamig4zos.objects.MFServer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.MultiPageEditorPart;


/**
 * The Server Editor has 2 or more pages:
 * <ul>
 * <li>page 1 - Server Overview
 * <li>page 2 and more - Schemas
 * </ul>
 */
public class ServerEditor extends MultiPageEditorPart {

	public ServerEditor() {
		super();
	}
	
	/**
	 * Set the Active Schema Page Text of the multi-page editor. (V6.0.0)
	 * 
	 * @throws PartInitException
	 */
	public void setActiveSchemaPageText(String text) {
		int curr_page_idx = getActivePage();
		setPageText(curr_page_idx, text);
	}
	
	/**
	 * Set the Active Schema Page Text of the multi-page editor. (V6.0.0)
	 * 
	 * @throws PartInitException
	 */
	public void updateSchemaTable(String schemaName, String newValue, int colIndex) {
		ServerEditorPageOverview editor = (ServerEditorPageOverview) getEditor(0);
		editor.updateSchemaTable(schemaName, newValue, colIndex);
	}

	/**
	 * Creates the Overview page of the multi-page editor.
	 * 
	 * @throws PartInitException
	 */
	public void createServerOverviewPage() throws PartInitException {
		// add the overview page
		int index = addPage(new ServerEditorPageOverview(this), this.getEditorInput());
		setPageText(index, "Overview");
	}

	/**
	 * Creates one Schema page of the multi-page editor.
	 * 
	 * @throws PartInitException
	 */
	public void createSchemaPage(DB2Schema schema) throws PartInitException {
		int index = addPage(new ServerEditorPageSchema(this, schema),
				this.getEditorInput());
		setPageText(index, schema.getSchemaName());
	}


	/**
	 * Removes one Schema page of the multi-page editor.
	 * 
	 * @throws PartInitException
	 */
	public void removeSchemaPage(DB2Schema schema) throws PartInitException {
		for (int i = 0; i < getPageCount(); i++) {
			if (getPageText(i).equals(schema.getSchemaName())) {
				removePage(i);
			}
		}
	}

	/**
	 * Creates all Schema pages of the multi-page editor.
	 * 
	 * @throws PartInitException
	 */
	public void createSchemaPages() throws PartInitException {
		/*
		 * Composite composite = new Composite(getContainer(), SWT.NONE);
		 * FillLayout layout = new FillLayout(); composite.setLayout(layout);
		 */
		// text = new StyledText(composite, SWT.H_SCROLL | SWT.V_SCROLL);
		// text.setEditable(false);

		// get the server from the input
		MFServer server = ((ServerEditorInput) this.getEditorInput()).getServer();
		DB2Schema[] schemas = server.getSchemas();

		for (int i = 0; i < schemas.length; i++) {
			// add page
			int index = addPage(new ServerEditorPageSchema(this, schemas[i]),
					this.getEditorInput());
			setPageText(index, schemas[i].getSchemaName());
			
			// get schema and set active page - v6.0.phase5
			DB2Schema schema = ((ServerEditorInput) this.getEditorInput()).getSchema();

			if (schema instanceof DB2Schema && schema.equals(schemas[i])) {
				setActivePage(index);
			}
		}
	}

	/**
	 * Creates the pages of the multi-page editor.
	 */
	protected void createPages() {
		try {
			createServerOverviewPage();
			createSchemaPages();
		} catch (PartInitException ex) {
			ex.printStackTrace();
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
	 * Sets active page.
	 */
	public void setActivePage(Object obj) {
		int count = getPageCount();
		String obj_text = "";
		
		if (obj instanceof MFServer) {
			obj_text = "Overview";
		} else if (obj instanceof DB2Schema) {
			obj_text = ((DB2Schema) obj).getSchemaName();
		}
		
		for (int i = 0; i < count; i++) {
			String page_text = getPageText(i);
			
			if (page_text.equals(obj_text)) {
				setActivePage(i);
				break;
			}
		}
	}

	/**
	 * Sets page form text. (V6.3)
	 */
	public void setPageFormText() {
		((ServerEditorPageOverview) getEditor(0)).setFormText();
		
		for (int i = 1; i < getPageCount(); i++) {
			((ServerEditorPageSchema) getEditor(i)).setFormText();
		}
	}
}
