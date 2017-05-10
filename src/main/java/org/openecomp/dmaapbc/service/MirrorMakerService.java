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
import java.util.List;
import java.util.Map;


import org.openecomp.dmaapbc.aaf.AafDecrypt;
//import org.openecomp.dmaapbc.aaf.AndrewDecryptor;
import org.openecomp.dmaapbc.client.MrTopicConnection;
import org.openecomp.dmaapbc.database.DatabaseClass;
import org.openecomp.dmaapbc.logging.BaseLoggingClass;
import org.openecomp.dmaapbc.logging.DmaapbcLogMessageEnum;
import org.openecomp.dmaapbc.model.ApiError;
import org.openecomp.dmaapbc.model.DmaapObject.DmaapObject_Status;
import org.openecomp.dmaapbc.model.MR_Cluster;
import org.openecomp.dmaapbc.model.MirrorMaker;
import org.openecomp.dmaapbc.util.DmaapConfig;
import org.openecomp.dmaapbc.util.RandomInteger;

public class MirrorMakerService extends BaseLoggingClass {
	
	private Map<String, MirrorMaker> mirrors = DatabaseClass.getMirrorMakers();
	private static MrTopicConnection prov;
	private static AafDecrypt decryptor;
	
	public MirrorMakerService() {
		super();
		
		decryptor = new AafDecrypt();
	}

	// will create a MM on MMagent if needed
	// will update the MMagent whitelist with all topics for this MM
	public MirrorMaker updateMirrorMaker( MirrorMaker mm ) {
		logger.info( "updateMirrorMaker");
		DmaapConfig p = (DmaapConfig)DmaapConfig.getConfig();
		String provUser = p.getProperty("MM.ProvUserMechId");
		String provUserPwd = decryptor.decrypt(p.getProperty( "MM.ProvUserPwd", "notSet" ));
		prov = new MrTopicConnection( provUser, provUserPwd );

		String centralFqdn = p.getProperty("MR.CentralCname", "notSet");
		
		DmaapService dmaap = new DmaapService();
		MR_ClusterService clusters = new MR_ClusterService();
	
		// in 1610, MM should only exist for edge-to-central
		//  we use a cname for the central MR cluster that is active, and provision on agent topic on that target
		// but only send 1 message so MM Agents can read it relying on kafka delivery
		for( MR_Cluster central: clusters.getCentralClusters() ) {
			prov.makeTopicConnection(central, dmaap.getBridgeAdminFqtn(), centralFqdn  );
			ApiError resp = prov.doPostMessage(mm.createMirrorMaker());
			if ( ! resp.is2xx() ) {
	
				errorLogger.error( DmaapbcLogMessageEnum.MM_PUBLISH_ERROR, "create MM", Integer.toString(resp.getCode()), resp.getMessage());
				mm.setStatus(DmaapObject_Status.INVALID);
			} else {
				prov.makeTopicConnection(central, dmaap.getBridgeAdminFqtn(), centralFqdn );
				resp = prov.doPostMessage(mm.updateWhiteList());
				if ( ! resp.is2xx()) {
					errorLogger.error( DmaapbcLogMessageEnum.MM_PUBLISH_ERROR,"MR Bridge", Integer.toString(resp.getCode()), resp.getMessage());
					mm.setStatus(DmaapObject_Status.INVALID);
				} else {
					mm.setStatus(DmaapObject_Status.VALID);
				}
			}
			
			// we only want to send one message even if there are multiple central clusters
			break;
		
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
