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
import org.openecomp.dmaapbc.model.DR_Sub;
import org.openecomp.dmaapbc.model.Feed;

public class DR_SubService {
	static final Logger logger = Logger.getLogger(DR_SubService.class);
	private Map<String, DR_Sub> dr_subs = DatabaseClass.getDr_subs();
	private String provURL;
	//private  DrProvConnection prov;
	
	public DR_SubService(  ) {
		logger.info( "Entry: DR_SubService (with no args)" );
//		prov = new DrProvConnection();

	}	
	public DR_SubService( String subURL ) {
		logger.info( "Entry: DR_SubService " + subURL );
		provURL = subURL;
//		prov = new DrProvConnection();
//		prov.makeSubConnection( subURL );
	}
	public Map<String, DR_Sub> getDR_Subs() {
		logger.info( "enter getDR_Subs()");
		return dr_subs;
	}
		
	public List<DR_Sub> getAllDr_Subs() {
		logger.info( "enter getAllDR_Subs()");
		return new ArrayList<DR_Sub>(dr_subs.values());
	}
	
	public ArrayList<DR_Sub> getDr_SubsByFeedId( String pubId ) {
		ArrayList<DR_Sub> someSubs = new ArrayList<DR_Sub>();
		for( DR_Sub sub : dr_subs.values() ) {
			if ( pubId.equals(  sub.getFeedId()  )) {
				someSubs.add( sub );
			}
		}
			
		return someSubs;
	}
	public DR_Sub getDr_Sub( String key, ApiError apiError ) {	
		logger.info( "enter getDR_Sub()");
		DR_Sub sub = dr_subs.get( key );
		if ( sub == null ) {
			apiError.setCode(Status.NOT_FOUND.getStatusCode());
			apiError.setFields( "subId");
			apiError.setMessage("subId " + key + " not found");
		} else {
			apiError.setCode(200);
		}
		return sub;
	}

	public DR_Sub addDr_Sub( DR_Sub sub, ApiError apiError ) {
		logger.info( "enter addDR_Subs()");
		DrProvConnection prov = new DrProvConnection();
		prov.makeSubConnection( provURL );
		String resp = prov.doPostDr_Sub( sub );
		logger.info( "resp=" + resp );

		DR_Sub snew = null;

		if ( resp != null ) {
			snew = new DR_Sub( resp );
			snew.setDcaeLocationName(sub.getDcaeLocationName());
			snew.setLastMod();
			dr_subs.put( snew.getSubId(), snew );	
			apiError.setCode(200);
		} else {
			apiError.setCode(400);
		}
		
		return snew;
	}

		
	public DR_Sub updateDr_Sub( DR_Sub obj, ApiError apiError ) {
		logger.info( "enter updateDR_Subs()");

		DR_Sub sub = dr_subs.get( obj.getSubId() );
		if ( sub == null ) {
			apiError.setCode(Status.NOT_FOUND.getStatusCode());
			apiError.setFields( "subId");
			apiError.setMessage("subId " + obj.getSubId() + " not found");
			return null;
		} 
		sub.setLastMod();
		dr_subs.put( sub.getSubId(), sub );
		apiError.setCode(200);
		return sub;
	}
		
	public void removeDr_Sub( String key, ApiError apiError ) {
		logger.info( "enter removeDR_Subs()");
		DR_Sub sub = dr_subs.get( key );
		if ( sub == null ) {
			apiError.setCode(Status.NOT_FOUND.getStatusCode());
			apiError.setFields( "subId");
			apiError.setMessage("subId " + key + " not found");
		} else {	
			dr_subs.remove(key);
			apiError.setCode(200);
		}

		return;
	}	

}
