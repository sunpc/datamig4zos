/**
 *  Class: core.TestServerConnection
 *  Description: This is to test the connection to a server.
 *  
 *  Author: Peng Cheng Sun
 *  
 *  Modification History
 *  01. 03/10/2011: Code baseline. (V6.0 phase 1)
 *  02. 03/12/2011: Renamed from ServerConnectionTest to TestServerConnection. (V6.0 phase 2)
 *  03. 03/22/2011: Remove the parameters of displayServerDetail(). (V6.0 phase 4)
 *  04. 04/03/2011: Extends CoreJob instead of implements IRunnableWithProgress. (V6.0 beta 1)
 */
package net.sourceforge.datamig4zos.core;

import net.sourceforge.datamig4zos.core.base.CoreJob;
import net.sourceforge.datamig4zos.objects.MFServer;


/**
 * @author SunPC
 * 
 */
public class TestServerConnection extends CoreJob {

	// constructor
	public TestServerConnection(MFServer server) {
		super("Server Connection Test");
	
		setLoggerClass(TestServerConnection.class);
		
		// set servers
		setServers(new MFServer[] { server });
		
		try {
			server.displayServerDetail();
		} catch (Exception ex) {
			ex.printStackTrace();
			getLogger().error(getProcName() + " Failed");
		}
	}

}
