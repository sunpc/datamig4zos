/**
 *  Class: ui.editors.ServerEditorInput
 *  Description: This is the Server Editor Input.
 * 
 * 	Author: Peng Cheng Sun
 * 
 *  Modification History
 *  01. 06/18/2010: Code baseline. (V6.0 phase 1)
 *  02. 03/20/2011: Change getName() and getToolTipText() methods. (V6.0 phase 3)
 *  03. 03/21/2011: Collapse damage for new data model objects. (V6.0 phase 3)
 *  04. 03/22/2011: Add DB2Schema. (V6.0 phase 4)
 *  05. 03/23/2011: Add a new constructor to handle DB2Schema. (V6.0 phase 5)
 */
package net.sourceforge.datamig4zos.ui.editors;

import net.sourceforge.datamig4zos.objects.DB2Schema;
import net.sourceforge.datamig4zos.objects.MFServer;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;


public class ServerEditorInput implements IEditorInput {

	private MFServer server;
	
	private DB2Schema schema;		// v6.0.phase5

	public ServerEditorInput(MFServer server) {
		try {
			this.server = server;
			this.server.setSchemas();

			DB2Schema[] schemas = this.server.getSchemas();		// v6.0.phase4
			
			for (int i=0; i<schemas.length; i++) {
				schemas[i].setTables();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public ServerEditorInput(DB2Schema schema) {
		this(schema.getServer());
		this.schema = schema;
		// if the schema is new, add it into the server - v6.0.phase5
		try {
			DB2Schema[] schemas = this.server.getSchemas();
			
			boolean is_schema_new = true;
			for (int i=0; i<schemas.length; i++) {
				if (schemas[i].equals(this.schema)) {
					is_schema_new = false;
				}
			}
			
			if (is_schema_new) {
				this.schema.setServer(server);
				this.server.addSchema(this.schema);
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public MFServer getServer() {
		return server;
	}

	public DB2Schema getSchema() {
		return schema;
	}

	@Override
	public boolean exists() {
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getName() {
		// V6.0.phase3
		return server.getServerName();
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return "Server " + server.getServerName() + ": " + server.getHostIp();
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}
	
	@Override
	public boolean equals(Object object) {
		if(object instanceof ServerEditorInput) {
			MFServer temp_server = ((ServerEditorInput)object).getServer();
			if(this.server.equals(temp_server)) {
				return true;
			}
		}		
		return false;
	}
}
