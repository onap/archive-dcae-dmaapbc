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

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import org.apache.log4j.Logger;

public class DBFieldHandler	{
	static final Logger logger = Logger.getLogger(DBFieldHandler.class);
	public static interface	SqlOp	{
		public Object get(ResultSet rs, int index) throws Exception;
		public void set(PreparedStatement ps, int index, Object value) throws Exception;
	}
	private static class	AofString implements SqlOp {
		public Object get(ResultSet rs, int index) throws Exception {
			String val = rs.getString(index);
			if (val == null) {
				return(null);
			}
			String[] ret = val.split(",");
			for (int i = 0; i < ret.length; i++) {
				ret[i] = funesc(ret[i]);
			}
			return(ret);
		}
		public void set(PreparedStatement ps, int index, Object x) throws Exception {
			String[] val = (String[])x;
			if (val == null) {
				ps.setString(index, null);
				return;
			}
			StringBuffer sb = new StringBuffer();
			String sep = "";
			for (String s: val) {
				sb.append(sep).append(fesc(s));
				sep = ",";
			}
			ps.setString(index, sb.toString());
		}
	}
	private static class	EnumSql implements SqlOp {
		private Class	enclass;
		public EnumSql(Class enclass) {
			this.enclass = enclass;
		}
		@SuppressWarnings("unchecked")
		public Object get(ResultSet rs, int index) throws Exception {
			String val = rs.getString(index);
			if (val == null) {
				return(null);
			} else {
				return(Enum.valueOf(enclass, val));
			}
		}
		public void set(PreparedStatement ps, int index, Object value) throws Exception {
			if (value == null) {
				ps.setString(index, null);
			} else {
				ps.setString(index, value.toString());
			}
		}
	}
	private static class	SqlDate implements SqlOp {
		public Object get(ResultSet rs, int index) throws Exception {
			return(rs.getTimestamp(index));
		}
		public void set(PreparedStatement ps, int index, Object val) throws Exception {
			if (val instanceof java.util.Date && !(val instanceof java.sql.Timestamp)) {
				val = new java.sql.Timestamp(((java.util.Date)val).getTime());
			}
			ps.setTimestamp(index, (java.sql.Timestamp)val);
		}
	}
        private static class    SqlType implements SqlOp {
                private Method   sqlget;
                private Method   sqlset;
                private SqlType(String tag) throws Exception {
			sqlget = ResultSet.class.getMethod("get" + tag, Integer.TYPE);
                        sqlset = PreparedStatement.class.getMethod("set" + tag, Integer.TYPE, sqlget.getReturnType());
                        sqltypes.put(sqlget.getReturnType().getName(), this);
                }
		public Object get(ResultSet rs, int index) throws Exception {
			return(sqlget.invoke(rs, index));
		}
		public void set(PreparedStatement ps, int index, Object val) throws Exception {
			try {
				sqlset.invoke(ps, index, val);
			} catch (Exception e) {
				logger.error("Problem setting field " + index + " to " + val + " statement is " + ps);
				throw e;
			}
		}
        }
        private static Map<String, SqlOp> sqltypes;
        static {
                sqltypes = new HashMap<String, SqlOp>();
		sqltypes.put("[Ljava.lang.String;", new AofString());
		sqltypes.put("java.util.Date", new SqlDate());
                try {
                        new SqlType("Boolean");
                        new SqlType("Timestamp");
                        new SqlType("Double");
                        new SqlType("Float");
                        new SqlType("Int");
                        new SqlType("Long");
                        new SqlType("Short");
                        new SqlType("String");
                } catch (Exception e) {
                        logger.error("Problem initializing sql access methods " + e, e);
                }
        }
	private Method	objget;
	private Method	objset;
	private SqlOp	sqlop;
	private int	fieldnum;
	public void copy(Object from, Object to) throws Exception {
		objset.invoke(to, objget.invoke(from));
	}
	public void setKey(Object o, String key) throws Exception {
		objset.invoke(o, key);
	}
	public String getKey(Object o) throws Exception {
		return((String)objget.invoke(o));
	}
	public void toSQL(Object o, PreparedStatement ps) throws Exception {
		sqlop.set(ps, fieldnum, objget.invoke(o));
	}
	public void fromSQL(ResultSet r, Object o) throws Exception {
		objset.invoke(o, sqlop.get(r, fieldnum));
	}
	public DBFieldHandler(Class<?> c, String fieldname, int fieldnum) throws Exception {
		this(c, fieldname, fieldnum, null);
	}
	public DBFieldHandler(Class<?> c, String fieldname, int fieldnum, SqlOp op) throws Exception {
		this.fieldnum = fieldnum;
		StringBuffer sb = new StringBuffer();
		for (String s: fieldname.split("_")) {
			sb.append(s.substring(0, 1).toUpperCase()).append(s.substring(1));
		}
		String camelcase = sb.toString();
		try {
			objget = c.getMethod("is" + camelcase);
		} catch (Exception e) {
			objget = c.getMethod("get" + camelcase);
		}
		objset = c.getMethod("set" + camelcase, objget.getReturnType());
		sqlop = op;
		if (sqlop != null) {
			return;
		}
		Class<?> x = objget.getReturnType();
		if (x.isEnum()) {
			sqlop = new EnumSql(x);
			return;
		}
		sqlop = sqltypes.get(x.getName());
		if (sqlop != null) {
			return;
		}
		logger.error("No field handler for class " + c.getName() + " field " + fieldname + " index " + fieldnum + " type " + x.getName());
	}
	public static String fesc(String s) {
		if (s == null) {
			return(s);
		}
		return(s.replaceAll("@", "@a").replaceAll(";", "@s").replaceAll(",", "@c"));
	}
	public static String funesc(String s) {
		if (s == null) {
			return(s);
		}
		return(s.replaceAll("@c", ",").replaceAll("@s", ";").replaceAll("@a", "@"));
	}
}
