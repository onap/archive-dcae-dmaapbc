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
import java.sql.*;

import org.apache.log4j.Logger;
import org.openecomp.dmaapbc.model.DR_Node;
import org.openecomp.dmaapbc.model.DR_Pub;
import org.openecomp.dmaapbc.model.DR_Sub;
import org.openecomp.dmaapbc.model.DcaeLocation;
import org.openecomp.dmaapbc.model.Dmaap;
import org.openecomp.dmaapbc.model.Feed;
import org.openecomp.dmaapbc.model.MR_Client;
import org.openecomp.dmaapbc.model.MR_Cluster;
import org.openecomp.dmaapbc.model.MirrorMaker;
import org.openecomp.dmaapbc.model.ReplicationVector;
import org.openecomp.dmaapbc.model.Topic;
import org.openecomp.dmaapbc.service.DmaapService;
import org.openecomp.dmaapbc.util.DmaapConfig;
import org.openecomp.dmaapbc.util.Singleton;

import org.openecomp.dmaapbc.model.*;




public class DatabaseClass {
	
	static final Logger logger = Logger.getLogger(DatabaseClass.class);
	
	private static Singleton<Dmaap> dmaap;
	private static Map<String, DcaeLocation> dcaeLocations;
	private static Map<String, DR_Node> dr_nodes;
	private static Map<String, DR_Pub> dr_pubs;
	private static Map<String, DR_Sub> dr_subs;
	private static Map<String, MR_Client> mr_clients;
	private static Map<String, MR_Cluster> mr_clusters;
	private static Map<String, Feed> feeds;
	private static Map<String, Topic> topics;
	private static Map<String, MirrorMaker> mirrors;
	
	private static long lastTime = 0L;
	
	private static class MirrorVectorHandler implements DBFieldHandler.SqlOp {
		public Object get(ResultSet rs, int index) throws Exception {
			String val = rs.getString(index);
			if (val == null) {
				return(null);
			}
			Set<ReplicationVector> rv = new HashSet<ReplicationVector>();
			for (String s: val.split(",")) {
				String[] f = s.split(";");
				if (f.length < 3) {
					continue;
				}
				rv.add(new ReplicationVector(DBFieldHandler.funesc(f[0]), DBFieldHandler.funesc(f[1]), DBFieldHandler.funesc(f[2])));
			}
			return(rv);
		}
		public void set(PreparedStatement ps, int index, Object val) throws Exception {
			if (val == null) {
				ps.setString(index, null);
				return;
			}
			Set xv = (Set)val;
			StringBuffer sb = new StringBuffer();
			String sep = "";
			for (Object o: xv) {
				ReplicationVector rv = (ReplicationVector)o;
				sb.append(sep).append(DBFieldHandler.fesc(rv.getFqtn())).append(';').append(DBFieldHandler.fesc(rv.getSourceCluster())).append(';').append(DBFieldHandler.fesc(rv.getTargetCluster()));
				sep = ",";
			}
			ps.setString(index, sb.toString());
		}
	}

	// modified version of MirrorVectorHandler for Topics
	private static class MirrorTopicsHandler implements DBFieldHandler.SqlOp {
		public Object get(ResultSet rs, int index) throws Exception {
			String val = rs.getString(index);
			if (val == null) {
				return(null);
			}
			List<String> rv = new ArrayList<String>();
			for (String s: val.split(",")) {
				//String[] f = s.split(";");
				//if (f.length < 3) {
				//	continue;
				//}
				rv.add(new String(s));
			}
			return(rv);
		}
		public void set(PreparedStatement ps, int index, Object val) throws Exception {
			if (val == null) {
				ps.setString(index, null);
				return;
			}
			@SuppressWarnings("unchecked")
			List<String> xv = (List<String>)val;
			StringBuffer sb = new StringBuffer();
			String sep = "";
			for (Object o: xv) {
				String rv = (String)o;
				sb.append(sep).append(DBFieldHandler.fesc(rv));
				sep = ",";
			}
			ps.setString(index, sb.toString());
		}
	}
	public static Singleton<Dmaap> getDmaap() {
		return dmaap;
	}
	

	
	public static Map<String, DcaeLocation> getDcaeLocations() {
		return dcaeLocations;
	}
	
	public static Map<String, DR_Node> getDr_nodes() {
		return dr_nodes;
	}
	
	public static Map<String, DR_Sub> getDr_subs() {
		return dr_subs;
	}
	public static Map<String, DR_Pub> getDr_pubs() {
		return dr_pubs;
	}

	public static Map<String, MR_Client> getMr_clients() {
		return mr_clients;
	}


	public static Map<String, MR_Cluster> getMr_clusters() {
		return mr_clusters;
	}
	
	public static Map<String, Feed> getFeeds() {
		return feeds;
	}
	public static Map<String, Topic> getTopics() {
		return topics;
	}
	public static Map<String, MirrorMaker> getMirrorMakers() {
		return mirrors;
	}

	static {
		try {
		logger.info( "begin static initialization");
		logger.info( "initializing dmaap" );
		DmaapConfig p = (DmaapConfig)DmaapConfig.getConfig();
		if ("true".equalsIgnoreCase(p.getProperty("UsePGSQL", "false"))) {
			logger.info("Data from database");
			try {
				LoadSchema.upgrade();
			} catch (Exception e) {
				logger.warn("Problem updating DB schema", e);
			}
			try {
				dmaap = new DBSingleton<Dmaap>(Dmaap.class, "dmaap");
				dcaeLocations = new DBMap<DcaeLocation>(DcaeLocation.class, "dcae_location", "dcae_location_name");
				dr_nodes = new DBMap<DR_Node>(DR_Node.class, "dr_node", "fqdn");
				dr_pubs = new DBMap<DR_Pub>(DR_Pub.class, "dr_pub", "pub_id");
				dr_subs = new DBMap<DR_Sub>(DR_Sub.class, "dr_sub", "sub_id");
				mr_clients = new DBMap<MR_Client>(MR_Client.class, "mr_client", "mr_client_id");
				mr_clusters = new DBMap<MR_Cluster>(MR_Cluster.class, "mr_cluster", "dcae_location_name");
				feeds = new DBMap<Feed>(Feed.class, "feed", "feed_id");
				topics = new DBMap<Topic>(Topic.class, "topic", "fqtn");
				//TableHandler.setSpecialCase("mirror_maker", "vectors", new MirrorVectorHandler());
				TableHandler.setSpecialCase("mirror_maker", "topics", new MirrorTopicsHandler());
				mirrors = new DBMap<MirrorMaker>(MirrorMaker.class, "mirror_maker", "mm_name");
			} catch (Exception e) {
				logger.fatal("Error initializing database access " + e, e);
				System.exit(1);
			}
		} else {
			logger.info("Data from memory");
			dmaap = new Singleton<Dmaap>() {
				private Dmaap dmaap;
				public void remove() {
					dmaap = null;
				}
				public void init(Dmaap val) {
					if (dmaap == null) {
						dmaap = val;
					}
				}
				public Dmaap get() {
					return(dmaap);
				}
				public void update(Dmaap nd) {
					dmaap.setVersion(nd.getVersion());
					dmaap.setTopicNsRoot(nd.getTopicNsRoot());
					dmaap.setDmaapName(nd.getDmaapName());
					dmaap.setDrProvUrl(nd.getDrProvUrl());
					dmaap.setBridgeAdminTopic(nd.getBridgeAdminTopic());
					dmaap.setLoggingUrl(nd.getLoggingUrl());
					dmaap.setNodeKey(nd.getNodeKey());
					dmaap.setAccessKeyOwner(nd.getAccessKeyOwner());
				}
			};
			dcaeLocations = new HashMap<String, DcaeLocation>();
			dr_nodes = new HashMap<String, DR_Node>();
			dr_pubs = new HashMap<String, DR_Pub>();
			dr_subs = new HashMap<String, DR_Sub>();
			mr_clients = new HashMap<String, MR_Client>();
			mr_clusters = new HashMap<String, MR_Cluster>();
			feeds = new HashMap<String, Feed>();
			topics = new HashMap<String, Topic>();
			mirrors = new HashMap<String, MirrorMaker>();
		}
		dmaap.init(new Dmaap("0", "", "", "", "", "", "", ""));
		// check for, and set up initial data, if it isn't already there
		Dmaap dmx = dmaap.get();
		if ("0".equals(dmx.getVersion())) {

			dmx = new Dmaap("0", "", "", "", "", "", "", "");
			dmx.setDmaapName(p.getProperty("DmaapName"));
			dmx.setDrProvUrl("https://" + p.getProperty("DR.provhost"));
			dmx.setVersion("1");
			dmx.setTopicNsRoot("org.openecomp.dcae.dmaap");
			dmx.setBridgeAdminTopic("DCAE_MM_AGENT");

			(new DmaapService()).addDmaap(dmx);
		}
		} catch (Exception e) {
			logger.error("Error loading database " + e, e);
		}
	}
	
	public synchronized static String getNextClientId() {
		
		long id = System.currentTimeMillis();
		if ( id <= lastTime ) {
			id = lastTime + 1;
		}
		lastTime = id;
		return Long.toString(id);
	}

	


}
