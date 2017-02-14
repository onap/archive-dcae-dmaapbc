/*-
 * ============LICENSE_START=======================================================
 * OpenECOMP - org.openecomp.dmaapbc
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.dmaapbc.database;

import java.util.*;
import java.lang.reflect.*;
import java.sql.*;

class TableHandler<C>	{
	protected ConnectionFactory cf;
	protected boolean	haskey;
	protected String	delstmt;
	protected String	insorreplstmt;
	protected String	getstmt;
	protected String	liststmt;
	protected String	initstmt;
	protected Class<C>	cls;
	protected DBFieldHandler[] fields;
	private static Map<String, Map<String, DBFieldHandler.SqlOp>> exceptions = new HashMap<String, Map<String, DBFieldHandler.SqlOp>>();
	public static void setSpecialCase(String dbtabname, String dbfldname, DBFieldHandler.SqlOp handler) {
		Map<String, DBFieldHandler.SqlOp> m = exceptions.get(dbtabname);
		if (m == null) {
			m = new HashMap<String, DBFieldHandler.SqlOp>();
			exceptions.put(dbtabname, m);
		}
		m.put(dbfldname, handler);
	}
	public static DBFieldHandler.SqlOp getSpecialCase(String dbtabname, String dbfldname) {
		Map<String, DBFieldHandler.SqlOp> m = exceptions.get(dbtabname);
		if (m != null) {
			return(m.get(dbfldname));
		}
		return(null);
	}
	protected TableHandler(Class<C> cls, String tabname, String keyname) throws Exception {
		this(ConnectionFactory.getDefaultInstance(), cls, tabname, keyname);
	}
	protected TableHandler(ConnectionFactory cf, Class<C> cls, String tabname, String keyname) throws Exception {
		this.cf = cf;
		Connection c = null;
		try {
			c = cf.get(false);
			setup(c.getMetaData(), cls, tabname, keyname);
		} finally {
			if (c != null) {
				cf.release(c);
			}
		}
	}
	private void setup(DatabaseMetaData dmd, Class<C> cls, String tabname, String keyname) throws Exception {
		this.cls = cls;
		Vector<DBFieldHandler> h = new Vector<DBFieldHandler>();
		ResultSet rs = dmd.getColumns("", "public", tabname, null);
		StringBuffer sb1 = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();
		StringBuffer sb3 = new StringBuffer();
		int	count = 0;
		while (rs.next()) {
			if (!rs.getString(3).equals(tabname)) {
				continue;
			}
			String cname = rs.getString(4);
			if (cname.equals(keyname)) {
				haskey = true;
				continue;
			}
			sb1.append(", ").append(cname);
			sb2.append(", ?");
			sb3.append(", EXCLUDED.").append(cname);
			count++;
			h.add(new DBFieldHandler(cls, cname, count, getSpecialCase(tabname, cname)));
		}
		if (count == 0) {
			throw new SQLException("Table " + tabname + " not found");
		}
		String clist = sb1.substring(2);
		String qlist = sb2.substring(2);
		String elist = sb3.substring(2);
		if (keyname != null && !haskey) {
			throw new SQLException("Table " + tabname + " does not have key column " + keyname + " not found");
		}
		if (haskey) {
			count++;
			h.add(new DBFieldHandler(cls, keyname, count, getSpecialCase(tabname, keyname)));
			delstmt = "DELETE FROM " + tabname + " WHERE " + keyname + " = ?";
			insorreplstmt = "INSERT INTO " + tabname + " (" + clist + ", " + keyname + ") VALUES (" + qlist + ", ?) ON CONFLICT(" + keyname + ") DO UPDATE SET (" + clist + ") = (" + elist + ")";
			getstmt = "SELECT " + clist + ", " + keyname + " FROM " + tabname + " WHERE " + keyname + " = ?";
			liststmt = "SELECT " + clist + ", " + keyname + " FROM " + tabname;
		} else {
			delstmt = "DELETE FROM " + tabname;
			initstmt = "INSERT INTO " + tabname + " (" + clist + ") VALUES (" + qlist + ")";
			insorreplstmt = "UPDATE " + tabname + " SET (" + clist + ") = (" + qlist + ")";
			getstmt = "SELECT " + clist + ", " + keyname + " FROM " + tabname;
		}
		fields = h.toArray(new DBFieldHandler[h.size()]);
	}
}
