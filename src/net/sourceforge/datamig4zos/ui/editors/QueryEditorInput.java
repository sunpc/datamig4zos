/**
 *  Class: ui.editors.QueryEditorInput
 *  Description: This is the Query Editor Input.
 * 
 * 	Author: Peng Cheng Sun
 * 
 *  Modification History
 *  01. 11/05/2012: Code baseline. (V6.1)
 */
package net.sourceforge.datamig4zos.ui.editors;

import net.sourceforge.datamig4zos.objects.DB2Schema;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;


public class QueryEditorInput implements IEditorInput {

	private DB2Schema schema;	
	private String sql_stmts = "";	
	private String query_results = "";

	public DB2Schema getSchema() {
		return schema;
	}

	public void setSchema(DB2Schema schema) {
		this.schema = schema;
	}
	
	public String getSqlStmts() {
		return sql_stmts;
	}

	public void setSqlStmts(String sqlStmts) {
		this.sql_stmts = sqlStmts;
	}

	public String getQueryResults() {
		return query_results;
	}

	public void setQueryResults(String queryResults) {
		this.query_results = queryResults;
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
		return "Database Query";
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return "Run a database query";
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}
	
	@Override
	public boolean equals(Object object) {
		if(object instanceof QueryEditorInput) {
			if(schema instanceof DB2Schema && schema.equals(((QueryEditorInput)object).getSchema())) {
				return true;
			}
		}		
		return false;
	}
}
