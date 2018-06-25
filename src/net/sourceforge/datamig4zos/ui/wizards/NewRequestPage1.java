/**
 *  Class: wizards.NewRequestPage1
 *  Description: The New Request Wizard - Page 1
 * 
 * 	Author: Peng Cheng Sun
 * 
 * 	Modification History
 *  01. 03/19/2011: Code baseline. (V6.0 phase 2)
 *  02. 03/20/2011: Collapse damage for new data model objects. (V6.0 phase 3)
 *  03. 03/22/2011: Add DB2Schema on the panel. (V6.0 phase 4)
 *  04. 03/23/2011: Add a new constructor to handle the pass-in object. (V6.0 phase 5)
 *  05. 03/24/2011: Limit the size of the combos. (V6.0 phase 5)
 *  06. 08/17/2013: UI enhancements. (V6.3)
 *  	1) Grouping the input fields.
 *  	2) Select the schemas automatically upon server selections.
 */
package net.sourceforge.datamig4zos.ui.wizards;

import net.sourceforge.datamig4zos.objects.DB2Schema;
import net.sourceforge.datamig4zos.objects.DB2Table;
import net.sourceforge.datamig4zos.objects.MFServer;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;


public class NewRequestPage1 extends WizardPage {
	
	private String source_server_name = "";			// v6.0.phase5
	private String source_schema_name = "";			// v6.0.phase5
	private String target_server_name = "";			// v6.0.phase5
	private String target_schema_name = "";			// v6.0.phase5
	
	private Combo cmb_source_server;
	private Combo cmb_source_schema;
	private Combo cmb_target_server;
	private Combo cmb_target_schema;
	
	private Composite container;
	
	private Group grp_source;		// v6.3
	private Group grp_target;		// v6.3
	
	public NewRequestPage1() {
		super("Servers and Schemas");
		setTitle("Servers and Schemas");
		setDescription("Select the servers and schemas.");
	}
	
	public NewRequestPage1(Object obj, String cmdType) {
		this();
		
		// initialize the server and schema names - v6.0.phase5
		if (obj instanceof MFServer) {
			if (cmdType.equals("S")) {
				source_server_name = ((MFServer) obj).getServerName();
			} else if (cmdType.equals("R")) {
				target_server_name = ((MFServer) obj).getServerName();
			}
		} else if (obj instanceof DB2Schema) {
			if (cmdType.equals("S")) {
				source_server_name = ((DB2Schema) obj).getServer().getServerName();
				source_schema_name = ((DB2Schema) obj).getSchemaName();
			} else if (cmdType.equals("R")) {
				target_server_name = ((DB2Schema) obj).getServer().getServerName();
				target_schema_name = ((DB2Schema) obj).getSchemaName();
			}
		} else if (obj instanceof DB2Table) {
			if (cmdType.equals("S")) {
				source_server_name = ((DB2Table) obj).getSchema().getServer().getServerName();
				source_schema_name = ((DB2Table) obj).getSchema().getSchemaName();
			} else if (cmdType.equals("R")) {
				target_server_name = ((DB2Table) obj).getSchema().getServer().getServerName();
				target_schema_name = ((DB2Table) obj).getSchema().getSchemaName();
			}			
		}
		
	}
	
	private final void verifyPageComplete() {
		if (cmb_source_server.getSelectionIndex() != -1
				&& cmb_target_server.getSelectionIndex() != -1
				&& cmb_source_schema.getSelectionIndex() != -1
				&& cmb_target_schema.getSelectionIndex() != -1) {
			setPageComplete(true);
		}
	}
	
	@Override
	public void createControl(Composite parent) {
		// create grid data
		GridData gd = new GridData();		// v6.0.phase5
		gd.widthHint = 120;					// v6.0.phase5	// v6.3
		
		// create container
		container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(1, true));
		
		// create groups - v6.3
		grp_source = new Group(container, SWT.NULL);		// v6.3
		grp_source.setText("Source");						// v6.3
		grp_source.setLayout(new GridLayout(4, false));		// v6.3
		
		grp_target = new Group(container, SWT.NULL);		// v6.3
		grp_target.setText("Target");						// v6.3
		grp_target.setLayout(new GridLayout(4, false));		// v6.3
		
		// source server
		Label lab_src = new Label(grp_source, SWT.NULL);	// v6.3
		lab_src.setText("Server: ");						// v6.3
		
		cmb_source_server = new Combo(grp_source, SWT.DROP_DOWN
				| SWT.BORDER | SWT.READ_ONLY);				// v6.3
		cmb_source_server.setLayoutData(gd);
		
		try {
			// get all servers - V6.0.phase4
			MFServer[] source_servers = MFServer.getAllServers();
			
			for (int i=0; i<source_servers.length; i++) {
				cmb_source_server.add(source_servers[i].getServerName());
				
				// select the default value - v6.0.phase5
				if (!source_server_name.isEmpty()
						&& source_servers[i].getServerName().equals(source_server_name)) {
					cmb_source_server.select(i);
				}
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		cmb_source_server.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					cmb_source_schema.removeAll();
					
					MFServer server = new MFServer(getSourceServerName());
					server.setSchemas();
					
					DB2Schema[] schemas = server.getSchemas();
					for (int i = 0; i < schemas.length; i++) {
						cmb_source_schema.add(schemas[i].getSchemaName());
						
						if (i == 0)
							cmb_source_schema.select(i); 		// v6.3
					}
					
					verifyPageComplete();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			
		});
		
		// source schema
		Label lab_srcsc = new Label(grp_source, SWT.NULL);		// v6.3
		lab_srcsc.setText("\tSchema: ");						// v6.3
		
		cmb_source_schema = new Combo(grp_source, SWT.DROP_DOWN
				| SWT.BORDER | SWT.READ_ONLY);					// v6.3
		cmb_source_schema.setLayoutData(gd);

		try {
			// set default schema - v6.0.phase5
			if (!(getSourceServerName().isEmpty())) {
				// get all schemas
				MFServer server = new MFServer(getSourceServerName());
				server.setSchemas();			
				DB2Schema[] schemas = server.getSchemas();
				for (int j=0; j<schemas.length; j++) {
					cmb_source_schema.add(schemas[j].getSchemaName());
					// select the default value
					if (!source_schema_name.isEmpty()
							&& schemas[j].getSchemaName().equals(source_schema_name)) {
						cmb_source_schema.select(j);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
		
		cmb_source_schema.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				verifyPageComplete();
			}
			
		});
		
		// target server
		Label lab_tgt = new Label(grp_target, SWT.NULL);		// v6.3
		lab_tgt.setText("Server: ");							// v6.3
		
		cmb_target_server = new Combo(grp_target, SWT.DROP_DOWN
				| SWT.BORDER | SWT.READ_ONLY);					// v6.3
		cmb_target_server.setLayoutData(gd);
		
		try {
			// get all servers - V6.0.phase4
			MFServer[] target_servers = MFServer.getAllServers();
			
			for (int i=0; i<target_servers.length; i++) {
				cmb_target_server.add(target_servers[i].getServerName());
				
				// select the default value - v6.0.phase5
				if (!target_server_name.isEmpty()
						&& target_servers[i].getServerName().equals(target_server_name)) {
					cmb_target_server.select(i);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		cmb_target_server.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					cmb_target_schema.removeAll();
					
					MFServer server = new MFServer(getTargetServerName());
					server.setSchemas();
					
					DB2Schema[] schemas = server.getSchemas();
					for (int i = 0; i < schemas.length; i++) {
						cmb_target_schema.add(schemas[i].getSchemaName());
						
						if (i == 0)
							cmb_target_schema.select(i); 		// v6.3
					}
					
					verifyPageComplete();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			
		});
		
		// target schema
		Label lab_tgtsc = new Label(grp_target, SWT.NULL);		// v6.3
		lab_tgtsc.setText("\tSchema: ");						// v6.3
		
		cmb_target_schema = new Combo(grp_target, SWT.DROP_DOWN
				| SWT.BORDER | SWT.READ_ONLY);					// v6.3
		cmb_target_schema.setLayoutData(gd);

		try {
			// set default schema - v6.0.phase5
			if (!(getTargetServerName().isEmpty())) {
				// get all schemas
				MFServer server = new MFServer(getTargetServerName());
				server.setSchemas();			
				DB2Schema[] schemas = server.getSchemas();
				for (int j=0; j<schemas.length; j++) {
					cmb_target_schema.add(schemas[j].getSchemaName());
					// select the default value
					if (!target_schema_name.isEmpty()
							&& schemas[j].getSchemaName().equals(target_schema_name)) {
						cmb_target_schema.select(j);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}	
		
		cmb_target_schema.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				verifyPageComplete();
			}
			
		});
		
		// Required to avoid an error in the system
		setControl(container);
		setPageComplete(false);
	}
	
	// get description
	public String getDesc() {
		return "Data Migration Request from " 
			+ getSourceServerName()	+ "." + getSourceSchemaName() 
			+ " to " 
			+ getTargetServerName() + "." + getTargetSchemaName();
	}
	
	// get source server name
	public String getSourceServerName() {
		if (cmb_source_server.getSelectionIndex() == -1)
			return "";
		
		int index = cmb_source_server.getSelectionIndex();
		return cmb_source_server.getItem(index);
	}
	
	// get target server name
	public String getTargetServerName() {
		if (cmb_target_server.getSelectionIndex() == -1)
			return "";
		
		int index = cmb_target_server.getSelectionIndex();
		return cmb_target_server.getItem(index);
	}
	
	// get source schema name - v6.0.phase4
	public String getSourceSchemaName() {
		if (cmb_source_schema.getSelectionIndex() == -1)
			return "";
		
		int index = cmb_source_schema.getSelectionIndex();
		return cmb_source_schema.getItem(index);
	}
	
	// get target server name - v6.0.phase4
	public String getTargetSchemaName() {
		if (cmb_target_schema.getSelectionIndex() == -1)
			return "";
		
		int index = cmb_target_schema.getSelectionIndex();
		return cmb_target_schema.getItem(index);
	}
}