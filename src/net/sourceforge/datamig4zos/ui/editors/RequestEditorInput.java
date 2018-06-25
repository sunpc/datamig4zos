/**
 *  Class: ui.editors.RequestEditorInput
 *  Description: This is the Request Editor Input.
 * 
 * 	Author: Peng Cheng Sun
 * 
 *  Modification History
 *  01. 03/12/2011: Code baseline. (V6.0 phase 2)
 *  02. 03/19/2011: Get inputs from the New Request Wizard. (V6.0 phase 2)
 *  03. 03/21/2011: Collapse damage for new data model objects. (V6.0 phase 3)
 *  04. 03/22/2011: Add schema names. (V6.0 phase 4)
 *  05. 04/15/2011: Add reference process ids. (V6.0 beta 1)
 *  06. 08/17/2013: Add setTableItems(), setDatasetItems() and remove setTextItems(). (V6.3)
 */
package net.sourceforge.datamig4zos.ui.editors;

import net.sourceforge.datamig4zos.objects.DB2Filter;
import net.sourceforge.datamig4zos.objects.DB2Schema;
import net.sourceforge.datamig4zos.objects.MFServer;
import net.sourceforge.datamig4zos.util.TextProcessor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;


public class RequestEditorInput implements IEditorInput {
	
	private String desc = "";
	private String source_server_name = "";
	private String target_server_name = "";
	private String source_schema_name = "";
	private String target_schema_name = "";
	
	private boolean chk_same_table_name = true;
	private boolean chk_rename_dataset_name = true;
	
	private MFServer source_server;
	private MFServer target_server;
	private DB2Schema source_schema;
	private DB2Schema target_schema;
	
	private String[][] table_items = {};
	private String[][] dataset_items = {};
	
	private String[][] ref_proc_ids = {};

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
		return "New Request Editor";
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return "Submit a new data migration request";
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	/**
	 * @return the desc
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * @param desc the desc to set
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}

	/**
	 * @return the source_server
	 */
	public String getSourceServerName() {
		return source_server_name;
	}

	/**
	 * @param sourceServer the source_server to set
	 * @throws Exception 
	 */
	public void setSourceServerName(String sourceServerName) throws Exception {
		source_server_name = sourceServerName;
		source_server = new MFServer(source_server_name);
	}

	/**
	 * @return the target_server
	 */
	public String getTargetServerName() {
		return target_server_name;
	}

	/**
	 * @param targetServer the target_server to set
	 * @throws Exception 
	 */
	public void setTargetServerName(String targetServerName) throws Exception {
		target_server_name = targetServerName;
		target_server = new MFServer(target_server_name);
	}
	
	/**
	 * @return table_items
	 */
	public String[][] getTableItems() {		// get table list - simplified in v6.3
		return table_items;
	}
	
	/**
	 * @param textItems the txt_table_items to set
	 */
	public void setTableItems(String textItems, String tableFilterName) {		
	// the original method moved from getTableItems - v6.3
		try {
			// get table filters - v6.3
			DB2Filter filter = new DB2Filter(tableFilterName);
			
			// look up each item on text_items
			int index = 0;
			String[] array_items = textItems.split("\n");
			String[][] new_table_items = new String[array_items.length][3];		// v6.3
			
			for (int i = 0; i < array_items.length; i++) {
				String item_name = array_items[i].toUpperCase().trim();
				
				if (!item_name.isEmpty() && !(source_schema.getTable(item_name).getDbName().equals(""))) {
					if (isSameTableName()) {
						if (!(target_schema.getTable(item_name).getDbName().equals(""))) {
							new_table_items[index] = new String[] {item_name, item_name, 
									filter.getSqlClause(source_server_name, source_schema_name, item_name)};
						} else {
							new_table_items[index] = new String[] {item_name, "", 
									filter.getSqlClause(source_server_name, source_schema_name, item_name)};
						}
					} else {
						new_table_items[index] = new String[] {item_name, "", 
								filter.getSqlClause(source_server_name, source_schema_name, item_name)};
					}
					
					index++;
				}
			}
			
			// move new table items to table items
			table_items = new String[index][3];		// v6.3
			for (int i = 0; i < index; i++) {
				table_items[i] = new_table_items[i];
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}

	/**
	 * @return dataset_items
	 */
	public String[][] getDatasetItems() {	// get data set list - simplified in v6.3
		return dataset_items;
	}
	
	/**
	 * @param textItems the txt_dataset_items to set
	 */
	public void setDatasetItems(String textItems) {		// v6.3
		try {
			// look up each item on text_items
			int index = 0;
			String[] array_items = textItems.split("\n");
			String[][] new_dataset_items = new String[array_items.length][2];
			
			for (int i = 0; i < array_items.length; i++) {
				String item_name = array_items[i].toUpperCase().trim();
					
				if(isRenameDatasetName()) {
					String new_item_name = TextProcessor.replaceStr(item_name, 
							source_schema.getDatasetPrefix(), target_schema.getDatasetPrefix());
					new_dataset_items[index] = new String[] {item_name, new_item_name};
				} else {
					new_dataset_items[index] = new String[] {item_name, item_name};
				}
				
				index++;
			}
			
			// move new dataset items to dataset items
			dataset_items = new String[index][2];
			for (int i = 0; i < index; i++) {
				dataset_items[i] = new_dataset_items[i];
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * @return the chk_same_table_name
	 */
	public boolean isSameTableName() {
		return chk_same_table_name;
	}

	/**
	 * @param chkSameTableName the chk_same_table_name to set
	 */
	public void setChkSameTableName(boolean chkSameTableName) {
		chk_same_table_name = chkSameTableName;
	}

	/**
	 * @return the chk_rename_dataset_name
	 */
	public boolean isRenameDatasetName() {
		return chk_rename_dataset_name;
	}

	/**
	 * @param chkRenameDatasetName the chk_rename_dataset_name to set
	 */
	public void setChkRenameDatasetName(boolean chkRenameDatasetName) {
		chk_rename_dataset_name = chkRenameDatasetName;
	}

	/**
	 * @return the source_schema_name
	 */
	public String getSourceSchemaName() {
		return source_schema_name;
	}

	/**
	 * @return the target_schema_name
	 */
	public String getTargetSchemaName() {
		return target_schema_name;
	}

	/**
	 * @param sourceSchemaName the source_schema_name to set
	 * @throws Exception 
	 */
	public void setSourceSchemaName(String sourceSchemaName) throws Exception {
		source_schema_name = sourceSchemaName;
		source_schema = new DB2Schema(source_schema_name, source_server);
		source_schema.setTables();
	}

	/**
	 * @param targetSchemaName the target_schema_name to set
	 * @throws Exception 
	 */
	public void setTargetSchemaName(String targetSchemaName) throws Exception {
		target_schema_name = targetSchemaName;
		target_schema = new DB2Schema(target_schema_name, target_server);
		target_schema.setTables();
	}

	public String[][] getRefProcIds() {
		return ref_proc_ids;
	}

	public void setRefProcIds(String[][] refProcIds) {
		ref_proc_ids = refProcIds;
	}
	
}
