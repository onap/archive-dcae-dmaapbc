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

import org.apache.log4j.Logger;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.openecomp.dmaapbc.logging.DmaapbcLogMessageEnum;
import org.openecomp.dmaapbc.util.DmaapConfig;

public class ConnectionFactory	{
	 static final EELFLogger logger = EELFManager.getInstance().getLogger( ConnectionFactory.class );
	 static final EELFLogger appLogger = EELFManager.getInstance().getApplicationLogger();
	 static final EELFLogger auditLogger = EELFManager.getInstance().getAuditLogger();
	 static final EELFLogger debugLogger = EELFManager.getInstance().getDebugLogger();
	 static final EELFLogger errorLogger = EELFManager.getInstance().getErrorLogger();
	 static final EELFLogger metricsLogger = EELFManager.getInstance().getMetricsLogger();
	 static final EELFLogger serverLogger = EELFManager.getInstance().getServerLogger();

	static {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (Exception e) {
			logger.error("Unable to load postgres driver " + e, e);
		}
	}
	private static ConnectionFactory instance = new ConnectionFactory();
	private String	host;
	private String	dbname;
	private String	dbuser;
	private String	dbcr;
	public ConnectionFactory() {
		Properties p = DmaapConfig.getConfig();
		host = p.getProperty("DB.host", "dcae-pstg-write-ftl.domain.notset.com");
		dbname = p.getProperty("DB.name", "dmaap");
		dbuser = p.getProperty("DB.user", "dmaap_admin");
		dbcr = p.getProperty("DB.cred", "test234-ftl");
	}
	public static ConnectionFactory getDefaultInstance() {
		return(instance);
	}
	private Connection[] pool = new Connection[5];
	private int	cur;
	public Connection get(boolean fresh) throws SQLException {
		if (!fresh) {
			synchronized(this) {
				if (cur > 0) {
					return(pool[--cur]);
				}
			}
		}
		Properties p = new Properties();
		p.put("user", dbuser);
		p.put("password", dbcr);
		return(DriverManager.getConnection("jdbc:postgresql://" + host + "/" + dbname, p));
	}
	public void release(Connection c) {
		synchronized(this) {
			if (cur < pool.length) {
				pool[cur++] = c;
				return;
			}
		}
		try { c.close(); } catch (Exception e) {}
	}
}
