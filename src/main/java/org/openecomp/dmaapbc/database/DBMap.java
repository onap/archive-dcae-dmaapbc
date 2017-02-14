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

import java.sql.*;
import java.util.*;

public class DBMap<C> extends TableHandler<C> implements Map<String, C>	{
	public DBMap(Class<C> cls, String tabname, String keyfield) throws Exception {
		this(ConnectionFactory.getDefaultInstance(), cls, tabname, keyfield);
	}
	public DBMap(ConnectionFactory cf, Class<C> cls, String tabname, String keyfield) throws Exception {
		super(cf, cls, tabname, keyfield);
	}
	public void clear() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	public boolean containsKey(Object key) throws DBException {
		return(get(key) != null);
	}
	public boolean containsValue(Object value) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	public boolean isEmpty() {
		return(false);
	}
	public Set<Map.Entry<String, C>> entrySet() throws DBException {
		return(list());
	}
	public Set<String> keySet() throws DBException {
		Set<String> ret = new HashSet<String>();
		for (Map.Entry<String, C> x: list()) {
			ret.add(x.getKey());
		}
		return(ret);
	}
	public void putAll(Map<? extends String, ? extends C> m) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	public int size() {
		return(2);
	}
	public Collection<C> values() throws DBException {
		Collection<C> ret = new Vector<C>();
		for (Map.Entry<String, C> x: list()) {
			ret.add(x.getValue());
		}
		return(ret);
	}
	public C get(Object key) throws DBException {
		if (!(key instanceof String)) {
			return(null);
		}
		return((new ConnWrapper<C, String>() {
			protected C run(String key) throws Exception {
				ps = c.prepareStatement(getstmt);
				ps.setString(1, (String)key);
				rs = ps.executeQuery();
				if (!rs.next()) {
					return(null);
				}
				C ret = cls.newInstance();
				for (DBFieldHandler f: fields) {
					f.fromSQL(rs, ret);
				}
				return(ret);
			}
		}).protect(cf, (String)key));
	}
	public Set<Map.Entry<String, C>> list() throws DBException {
		return((new ConnWrapper<Set<Map.Entry<String, C>>, Object>() {
			protected Set<Map.Entry<String, C>> run(Object junk) throws Exception {
				DBFieldHandler keyfield = fields[fields.length - 1];
				ps = c.prepareStatement(liststmt);
				rs = ps.executeQuery();
				Set<Map.Entry<String, C>> ret = new HashSet<Map.Entry<String, C>>();
				while (rs.next()) {
					C val = cls.newInstance();
					for (DBFieldHandler f: fields) {
						f.fromSQL(rs, val);
					}
					String key = keyfield.getKey(val);
					ret.add(new AbstractMap.SimpleEntry<String, C>(key, val));
				}
				return(ret);
			}
		}).protect(cf, null));
	}
	public C put(String key, C val) throws DBException {
		try {
			fields[fields.length - 1].setKey(val, key);
		} catch (Exception e) {
			throw new DBException(e);
		}
		PreparedStatement ps = null;
		return((new ConnWrapper<C, C>() {
			protected C run(C val) throws Exception {
				ps = c.prepareStatement(insorreplstmt);
				for (DBFieldHandler f: fields) {
					f.toSQL(val, ps);
				}
				ps.executeUpdate();
				return(null);
			}
		}).protect(cf, val));
	}
	public C remove(Object key) throws DBException {
		if (!(key instanceof String)) {
			return(null);
		}
		return((new ConnWrapper<C, String>() {
			protected C run(String key) throws Exception {
				ps = c.prepareStatement(delstmt);
				ps.setString(1, key);
				ps.executeUpdate();
				return(null);
			}
		}).protect(cf, (String)key));
	}
}
