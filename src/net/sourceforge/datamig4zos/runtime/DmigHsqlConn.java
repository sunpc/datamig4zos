package net.sourceforge.datamig4zos.runtime;

import net.sourceforge.datamig4zos.util.HsqlConn;

public class DmigHsqlConn {

	private static HsqlConn conn;

	public static HsqlConn getConn() {
		return conn;
	}

	public static void setConn(HsqlConn conn) {
		DmigHsqlConn.conn = conn;
	}
	
}
