/**
 *  Class: wizards.NewRequestWizard
 *  Description: The New Request Wizard
 * 
 * 	Author: Peng Cheng Sun
 * 
 * 	Modification History
 *  01. 03/18/2011: Code baseline. (V6.0 phase 2)
 *  02. 03/22/2011: Pass DB2Schema to OpenRequestEditorAction. (V6.0 phase 4)
 *  03. 03/23/2011: Add a new constructor to handle the pass-in object. (V6.0 phase 5)
 *  04. 08/17/2013: UI enhancements. (V6.3)
 *  	1) Separate table and data set items on Page 2.
 *  	2) Add a new page for table filters.
 *  	3) Store and pass the table filters to the request editor.
 */
package net.sourceforge.datamig4zos.ui.wizards;

import net.sourceforge.datamig4zos.actions.OpenRequestEditorAction;
import net.sourceforge.datamig4zos.objects.DB2Filter;
import net.sourceforge.datamig4zos.objects.DB2Schema;
import net.sourceforge.datamig4zos.objects.DB2Table;
import net.sourceforge.datamig4zos.objects.MFServer;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.PlatformUI;


public class NewRequestWizard extends Wizard {

	private Object obj;
	private String cmdType;		// S - Send to; R - Receive from.
	
	private NewRequestPage1 page1;
	private NewRequestPage2 page2;
	private NewRequestPage3 page3;
	private NewRequestPage4 page4;		// v6.3

	public NewRequestWizard() {
		super();
		setNeedsProgressMonitor(false);
		setWindowTitle("New Data Migration Request Wizard");
	}

	public NewRequestWizard(Object obj, String cmdType) {
		this();
		this.obj = obj;
		this.cmdType = cmdType;
	}

	@Override
	public void addPages() {
		// page 1
		if (obj instanceof MFServer 
				|| obj instanceof DB2Schema
				|| obj instanceof DB2Table) {
			page1 = new NewRequestPage1(obj, cmdType);		// v6.0.phase5
		} else {
			page1 = new NewRequestPage1();
		}
		
		// page 2
		if (obj instanceof DB2Table) {
			page2 = new NewRequestPage2(obj);		// v6.0.phase5
		} else {
			page2 = new NewRequestPage2();
		}
		
		// page 3
		page3 = new NewRequestPage3();
		
		// page 4 - v6.3
		page4 = new NewRequestPage4();
		
		// add all pages to the wizard
		addPage(page1);
		addPage(page2);
		addPage(page3);
		addPage(page4);		// v6.3
	}

	@Override
	public boolean performFinish() {
		// store the table filters - v6.3
		try {
			DB2Filter filter = new DB2Filter(page3.getTableFilterName(), page3.getTableFilterData());
			filter.saveFilterIntoHsql();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		// open request editor
		OpenRequestEditorAction request = new OpenRequestEditorAction(PlatformUI.getWorkbench().getWorkbenchWindows()[0]);
		request.setDesc(page1.getDesc());
		request.setSourceServerName(page1.getSourceServerName());
		request.setTargetServerName(page1.getTargetServerName());
		request.setSourceSchemaName(page1.getSourceSchemaName());
		request.setTargetSchemaName(page1.getTargetSchemaName());
		request.setTableItems(page2.getTableItems());				// v6.3
		request.setDatasetItems(page2.getDatasetItems());			// v6.3
		request.setTableFilterName(page3.getTableFilterName());		// v6.3
		request.setChkSameTableName(page4.getChkSameTableName());
		request.setChkRenameDatasetName(page4.getChkRenameDatasetName());
		request.run();

		// always return true
		return true;
	}
}