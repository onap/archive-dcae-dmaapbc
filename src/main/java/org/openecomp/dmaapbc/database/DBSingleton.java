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

import org.openecomp.dmaapbc.util.Singleton;

public class DBSingleton<C> extends TableHandler<C> implements Singleton<C>	{
	private C singleton;
	public DBSingleton(Class<C> cls, String tabname) throws Exception {
		this(ConnectionFactory.getDefaultInstance(), cls, tabname);
	}
	public DBSingleton(ConnectionFactory cf, Class<C> cls, String tabname) throws Exception {
		super(cf, cls, tabname, null);
		singleton = cls.newInstance();
	}
	public C get() throws DBException {
		return((new ConnWrapper<C, Object>() {
			protected C run(Object junk) throws Exception {
				ps = c.prepareStatement(getstmt);
				rs = ps.executeQuery();
				if (!rs.next()) {
					return(null);
				}
				for (DBFieldHandler f: fields) {
					f.fromSQL(rs, singleton);
				}
				return(singleton);
			}
		}).protect(cf, null));
	}
	public void init(C val) throws DBException {
		if (get() != null) {
			return;
		}
		(new ConnWrapper<Void, C>() {
			protected Void run(C val) throws Exception {
				ps = c.prepareStatement(initstmt);
				for (DBFieldHandler f: fields) {
					f.toSQL(val, ps);
				}
				ps.executeUpdate();
				if (val != singleton) {
					for (DBFieldHandler f: fields) {
						f.copy(val, singleton);
					}
				}
				return(null);
			}
		}).protect(cf, val);
	}
	public void update(C val) throws DBException {
		(new ConnWrapper<Void, C>() {
			protected Void run(C val) throws Exception {
				ps = c.prepareStatement(insorreplstmt);
				for (DBFieldHandler f: fields) {
					f.toSQL(val, ps);
				}
				ps.executeUpdate();
				if (val != singleton) {
					for (DBFieldHandler f: fields) {
						f.copy(val, singleton);
					}
				}
				return(null);
			}
		}).protect(cf, val);
	}
	public void remove() throws DBException {
		(new ConnWrapper<Void, Object>() {
			protected Void run(Object junk) throws Exception {
				ps = c.prepareStatement(delstmt);
				ps.executeUpdate();
				return(null);
			}
		}).protect(cf, null);
	}
}
