package net.sourceforge.datamig4zos.runtime;

/**
 * Interface defining the application's command IDs.
 * Key bindings can be defined for specific commands.
 * To associate an action with a command, use IAction.setActionDefinitionId(commandId).
 *
 * @see org.eclipse.jface.action.IAction#setActionDefinitionId(String)
 */
public interface ICommandIds {
	
	// commands for main application
    public static final String CMD_OPEN_REQUEST_EDITOR = "net.sourceforge.datamig4zos.actions.OpenRequestEditor";
    public static final String CMD_OPEN_REQUEST_WIZARD = "net.sourceforge.datamig4zos.actions.OpenRequestWizard";
    public static final String CMD_OPEN_QUERY_EDITOR = "net.sourceforge.datamig4zos.actions.OpenQueryEditor";
    
    // commands for server editor
    public static final String CMD_OPEN_SCHEMA_WIZARD = "net.sourceforge.datamig4zos.actions.OpenSchemaWizard";
    public static final String CMD_SAVE_SERVER_CHANGES = "net.sourceforge.datamig4zos.actions.SaveServerChanges";
    
    // commands for request editor
    public static final String CMD_START_DATA_MIGRATION = "net.sourceforge.datamig4zos.actions.StartDataMigration";
    
    // commands for query editor
    public static final String CMD_RUN_DATABASE_QUERY = "net.sourceforge.datamig4zos.actions.RunDatabaseQuery";
    public static final String CMD_SAVE_QUERY_RESULTS = "net.sourceforge.datamig4zos.actions.SaveQueryResults";

    // commands for Server Explorer
    public static final String CMD_OPEN_SERVER_EDITOR = "net.sourceforge.datamig4zos.actions.OpenServerEditor";
    public static final String CMD_ADD_NEW_SERVER = "net.sourceforge.datamig4zos.actions.AddNewServer";    
    public static final String CMD_DELETE_EXISTING_SERVER = "net.sourceforge.datamig4zos.actions.DeleteExistingServer";
    public static final String CMD_REFRESH_SERVER_EXPLORER = "net.sourceforge.datamig4zos.actions.RefreshServerExplorer";
    public static final String CMD_ITEM_SEND_TO = "net.sourceforge.datamig4zos.actions.ItemSendTo";
    public static final String CMD_ITEM_RECEIVE_FROM = "net.sourceforge.datamig4zos.actions.ItemReceiveFrom";
    public static final String CMD_QUERY_TABLE_DATA = "net.sourceforge.datamig4zos.actions.QueryTableData";
}
