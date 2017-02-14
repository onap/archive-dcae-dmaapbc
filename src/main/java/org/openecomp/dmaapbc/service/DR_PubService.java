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
import org.openecomp.dmaapbc.client.DrProvConnection;
import org.openecomp.dmaapbc.database.DatabaseClass;
import org.openecomp.dmaapbc.model.ApiError;
import org.openecomp.dmaapbc.model.DR_Pub;
import org.openecomp.dmaapbc.model.DmaapObject.DmaapObject_Status;

public class DR_PubService {
	static final Logger logger = Logger.getLogger(DR_PubService.class);
	private Map<String, DR_Pub> dr_pubs = DatabaseClass.getDr_pubs();
	private DR_NodeService nodeService = new DR_NodeService();
	private static DrProvConnection prov;
	
	public DR_PubService() {
		super();
		prov = new DrProvConnection();
	}

	public Map<String, DR_Pub> getDr_Pubs() {			
		return dr_pubs;
	}
		
	public List<DR_Pub> getAllDr_Pubs() {
		return new ArrayList<DR_Pub>(dr_pubs.values());
	}
	
	public ArrayList<DR_Pub> getDr_PubsByFeedId( String feedId ) {
		ArrayList<DR_Pub> somePubs = new ArrayList<DR_Pub>();
		for( DR_Pub pub : dr_pubs.values() ) {
			if ( feedId.equals(  pub.getFeedId()  )) {
				somePubs.add( pub );
			}
		}
			
		return somePubs;
	}
		
	public DR_Pub getDr_Pub( String key, ApiError err ) {	
		DR_Pub pub = dr_pubs.get( key );
		if ( pub == null ) {
			err.setCode(Status.NOT_FOUND.getStatusCode());
			err.setFields( "pubId");
			err.setMessage("DR_Pub with pubId = " + key + " not found");
		} else {
			err.setCode(Status.OK.getStatusCode());
		}
		return pub;
	}
	
	private void addIngressRoute( DR_Pub pub, ApiError err ) {
		
		String nodePattern = nodeService.getNodePatternAtLocation( pub.getDcaeLocationName());
		if ( nodePattern != null && nodePattern.length() > 0 ) {
			logger.info( "creating ingress rule: pub " + pub.getPubId() + " on feed " + pub.getFeedId() + " to " + nodePattern);
			prov.makeIngressConnection( pub.getFeedId(), pub.getUsername(), "-", nodePattern);
			int rc = prov.doIngressPost(err);
			logger.info( "rc=" + rc + " error code=" + err.getCode() );
			
			if ( rc != 200 ) {
				switch( rc ) {
				case 403:
					logger.error( "Not authorized for DR ingress API");
					err.setCode(500);
					err.setMessage("API deployment/configuration error - contact support");
					err.setFields( "PROV_AUTH_ADDRESSES");
					break;
				
				default: 
					logger.warn( "unable to create ingress rule for " + pub.getPubId() + " on feed " + pub.getFeedId() + " to " + nodePattern);
				}
			}

		}
	}

	public DR_Pub addDr_Pub( DR_Pub pub ) {
		ApiError err = new ApiError();
		if ( pub.getPubId() != null && ! pub.getPubId().isEmpty() ) {
			addIngressRoute( pub, err);
			if ( err.getCode() > 0 ) {
				pub.setStatus(DmaapObject_Status.INVALID);
			}
			pub.setLastMod();
			dr_pubs.put( pub.getPubId(), pub );
			return pub;
		}
		else {
			return null;
		}
	}
		
	public DR_Pub updateDr_Pub( DR_Pub pub ) {
		if ( pub.getPubId().isEmpty()) {
			return null;
		}
		pub.setLastMod();
		dr_pubs.put( pub.getPubId(), pub );
		return pub;
	}
		
	public DR_Pub removeDr_Pub( String pubId, ApiError err ) {

		DR_Pub pub =  dr_pubs.get( pubId );
		if ( pub == null ) {
			err.setCode(Status.NOT_FOUND.getStatusCode());
			err.setFields( "pubId");
			err.setMessage( "pubId " + pubId + " not found");
		} else {
			dr_pubs.remove(pubId);
			err.setCode(Status.OK.getStatusCode());
		}
		return pub;
				
	}	

}
