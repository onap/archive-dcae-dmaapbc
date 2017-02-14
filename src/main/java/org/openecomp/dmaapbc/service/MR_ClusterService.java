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

import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.openecomp.dmaapbc.database.DatabaseClass;
import org.openecomp.dmaapbc.model.ApiError;
import org.openecomp.dmaapbc.model.DcaeLocation;
import org.openecomp.dmaapbc.model.MR_Cluster;
import org.openecomp.dmaapbc.model.DmaapObject.DmaapObject_Status;

public class MR_ClusterService {
	static final Logger logger = Logger.getLogger(MR_ClusterService.class);

	private Map<String, MR_Cluster> mr_clusters = DatabaseClass.getMr_clusters();
	
	public Map<String, MR_Cluster> getMR_Clusters() {			
		return mr_clusters;
	}
		
	public List<MR_Cluster> getAllMr_Clusters() {
		return new ArrayList<MR_Cluster>(mr_clusters.values());
	}
		
	public MR_Cluster getMr_Cluster( String key, ApiError apiError ) {			
		MR_Cluster mrc = mr_clusters.get( key );
		if ( mrc == null ) {
			apiError.setCode(Status.NOT_FOUND.getStatusCode());
			apiError.setFields( "dcaeLocationName");
			apiError.setMessage( "Cluster with dcaeLocationName " + key + " not found");
		}
		apiError.setCode(200);
		return mrc;
	}
	public MR_Cluster getMr_ClusterByFQDN( String key ) {		
		for( MR_Cluster cluster: mr_clusters.values() ) {
			if ( key.equals( cluster.getFqdn() ) ) {
				return cluster;
			}
		}
		return null;
	}

	public MR_Cluster addMr_Cluster( MR_Cluster cluster, ApiError apiError ) {
		logger.info( "Entry: addMr_Cluster");
		MR_Cluster mrc = mr_clusters.get( cluster.getDcaeLocationName() );
		if ( mrc != null ) {
			apiError.setCode(Status.CONFLICT.getStatusCode());
			apiError.setFields( "dcaeLocationName");
			apiError.setMessage( "Cluster with dcaeLocationName " + cluster.getDcaeLocationName() + " already exists");
			return null;
		}
		cluster.setLastMod();
		cluster.setStatus(DmaapObject_Status.VALID);
		mr_clusters.put( cluster.getDcaeLocationName(), cluster );
		DcaeLocationService svc = new DcaeLocationService();
		DcaeLocation loc = svc.getDcaeLocation( cluster.getDcaeLocationName() );
		if ( loc != null && loc.isCentral() ) {
			ApiError resp = TopicService.setBridgeClientPerms( cluster );
			if ( ! resp.is2xx() ) {
				logger.error( "Unable to provision Bridge to " + cluster.getDcaeLocationName() );
				cluster.setLastMod();
				cluster.setStatus(DmaapObject_Status.INVALID);
				mr_clusters.put( cluster.getDcaeLocationName(), cluster );
			}
		}
		apiError.setCode(200);
		return cluster;
	}
		
	public MR_Cluster updateMr_Cluster( MR_Cluster cluster, ApiError apiError ) {
		MR_Cluster mrc = mr_clusters.get( cluster.getDcaeLocationName() );
		if ( mrc == null ) {
			apiError.setCode(Status.NOT_FOUND.getStatusCode());
			apiError.setFields( "dcaeLocationName");
			apiError.setMessage( "Cluster with dcaeLocationName " + cluster.getDcaeLocationName() + " not found");
			return null;
		}
		cluster.setLastMod();
		mr_clusters.put( cluster.getDcaeLocationName(), cluster );
		DcaeLocationService svc = new DcaeLocationService();
		DcaeLocation loc = svc.getDcaeLocation( cluster.getDcaeLocationName() );
		if ( loc.isCentral() ) {
			ApiError resp = TopicService.setBridgeClientPerms( cluster );
			if ( ! resp.is2xx() ) {
				logger.error( "Unable to provision Bridge to " + cluster.getDcaeLocationName() );
				cluster.setLastMod();
				cluster.setStatus(DmaapObject_Status.INVALID);
				mr_clusters.put( cluster.getDcaeLocationName(), cluster );
			}
		}
		apiError.setCode(200);
		return cluster;
	}
		
	public MR_Cluster removeMr_Cluster( String key, ApiError apiError ) {
		MR_Cluster mrc = mr_clusters.get( key );
		if ( mrc == null ) {
			apiError.setCode(Status.NOT_FOUND.getStatusCode());
			apiError.setFields( "dcaeLocationName");
			apiError.setMessage( "Cluster with dcaeLocationName " + key + " not found");
			return null;
		}
		apiError.setCode(200);
		return mr_clusters.remove(key);
	}	
	

}
