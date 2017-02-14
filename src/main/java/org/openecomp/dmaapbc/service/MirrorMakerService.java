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

package org.openecomp.dmaapbc.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;













import org.openecomp.dmaapbc.aaf.AndrewDecryptor;
import org.openecomp.dmaapbc.client.MrTopicConnection;
import org.openecomp.dmaapbc.database.DatabaseClass;
import org.openecomp.dmaapbc.model.ApiError;
import org.openecomp.dmaapbc.model.MR_Cluster;
import org.openecomp.dmaapbc.model.MirrorMaker;
import org.openecomp.dmaapbc.model.DmaapObject.DmaapObject_Status;
import org.openecomp.dmaapbc.util.DmaapConfig;
import org.openecomp.dmaapbc.util.RandomInteger;

public class MirrorMakerService {
	static final Logger logger = Logger.getLogger(MirrorMakerService.class);
	
	private Map<String, MirrorMaker> mirrors = DatabaseClass.getMirrorMakers();
	private static MrTopicConnection prov;
	
	public MirrorMakerService() {
		super();
		// TODO Auto-generated constructor stub
	}

	// will create a MM on MMagent if needed
	// will update the MMagent whitelist with all topics for this MM
	public MirrorMaker updateMirrorMaker( MirrorMaker mm ) {
		logger.info( "updateMirrorMaker");
		DmaapConfig p = (DmaapConfig)DmaapConfig.getConfig();
		String provUser = p.getProperty("MM.ProvUserMechId");
		String provUserPwd = AndrewDecryptor.valueOf(p.getProperty( "MM.ProvUserPwd", "notSet" ));
		prov = new MrTopicConnection( provUser, provUserPwd );
		MR_ClusterService clusters = new MR_ClusterService();
		DmaapService dmaap = new DmaapService();
		//TODO: this should find the cluster!!!!
		
		MR_Cluster central = clusters.getMr_ClusterByFQDN(mm.getTargetCluster());
		if ( central != null ) {
			prov.makeTopicConnection(central, dmaap.getBridgeAdminFqtn() );
			ApiError resp = prov.doPostMessage(mm.createMirrorMaker());
			if ( ! resp.is2xx() ) {
				//logger.error( "Unable to publish MR Bridge provisioning message. rc=" + resp.getCode()  + " msg=" + resp.getMessage());
				logger.error( "Unable to publish create MM provisioning message. rc=" + resp.getCode()  + " msg=" + resp.getMessage());
				mm.setStatus(DmaapObject_Status.INVALID);
			} else {
				prov.makeTopicConnection(central, dmaap.getBridgeAdminFqtn() );
				resp = prov.doPostMessage(mm.updateWhiteList());
				if ( ! resp.is2xx()) {
					logger.error( "Unable to publish MR Bridge provisioning message. rc=" + resp.getCode()  + " msg=" + resp.getMessage());
					mm.setStatus(DmaapObject_Status.INVALID);
				} else {
					mm.setStatus(DmaapObject_Status.VALID);
				}
			}
		
		} else {
			logger.warn( "target cluster " + mm.getTargetCluster() + " not found!");
		}

		mm.setLastMod();
		return mirrors.put( mm.getMmName(), mm);
	}
	public MirrorMaker getMirrorMaker( String part1, String part2 ) {
		logger.info( "getMirrorMaker using " + part1 + " and " + part2 );
		return mirrors.get(MirrorMaker.genKey(part1, part2));
	}	
	public MirrorMaker getMirrorMaker( String key ) {
		logger.info( "getMirrorMaker using " + key);
		return mirrors.get(key);
	}
	
	/*public MirrorMaker updateMirrorMaker( MirrorMaker mm ) {
		logger.info( "updateMirrorMaker");
		return mirrors.put( mm.getMmName(), mm);
	}
	*/
	
	public void delMirrorMaker( MirrorMaker mm ) {
		logger.info( "delMirrorMaker");
		mirrors.remove(mm.getMmName());
	}
	
	// TODO: this should probably return sequential values or get replaced by the MM client API
	// but it should be sufficient for initial 1610 development
	public static String genTransactionId() {
		RandomInteger ri = new RandomInteger(100000);
	    int randomInt = ri.next();
	    return Integer.toString(randomInt);
	}
	public List<String> getAllMirrorMakers() {
		List<String> ret = new ArrayList<String>();
		for( String key: mirrors.keySet()) {
			ret.add( key );
		}
		
		return ret;
	}

}
