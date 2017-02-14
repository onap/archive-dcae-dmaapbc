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
import org.openecomp.dmaapbc.model.DR_Node;
import org.openecomp.dmaapbc.model.DmaapObject.DmaapObject_Status;

public class DR_NodeService {
	static final Logger logger = Logger.getLogger(DR_NodeService.class);
	private Map<String, DR_Node> dr_nodes = DatabaseClass.getDr_nodes();
	
	public Map<String, DR_Node> getDr_Nodes() {
		return dr_nodes;
	}
	
	public List<DR_Node> getAllDr_Nodes() {
		return new ArrayList<DR_Node>(dr_nodes.values());
	}
	
	public DR_Node getDr_Node( String fqdn, ApiError apiError ) {
		DR_Node old = dr_nodes.get( fqdn );
		if ( old == null ) {
			apiError.setCode(Status.NOT_FOUND.getStatusCode());
			apiError.setFields( "fqdn");
			apiError.setMessage( "Node " + fqdn + " does not exist");
			return null;
		}
		apiError.setCode(200);
		return old;
	}

	public DR_Node addDr_Node( DR_Node node, ApiError apiError ) {
		String fqdn = node.getFqdn();
		DR_Node old = dr_nodes.get( fqdn );
		if ( old != null ) {
			apiError.setCode(Status.CONFLICT.getStatusCode());
			apiError.setFields( "fqdn");
			apiError.setMessage( "Node " + fqdn + " already exists");
			return null;
		}
		node.setLastMod();
		node.setStatus(DmaapObject_Status.VALID);
		dr_nodes.put( node.getFqdn(), node );
		apiError.setCode(200);
		return node;
	}
	
	public DR_Node updateDr_Node( DR_Node node, ApiError apiError ) {
		DR_Node old = dr_nodes.get( node );
		if ( old == null ) {
			apiError.setCode(Status.NOT_FOUND.getStatusCode());
			apiError.setFields( "fqdn");
			apiError.setMessage( "Node " + node + " does not exist");
			return null;
		}
		node.setLastMod();
		dr_nodes.put( node.getFqdn(), node );
		apiError.setCode(200);
		return node;
	}
	
	public DR_Node removeDr_Node( String nodeName, ApiError apiError ) {
		DR_Node old = dr_nodes.get( nodeName );
		if ( old == null ) {
			apiError.setCode(Status.NOT_FOUND.getStatusCode());
			apiError.setFields( "fqdn");
			apiError.setMessage( "Node " + nodeName + " does not exist");
			return null;
		}
		apiError.setCode(200);
		return dr_nodes.remove(nodeName);
	}	

	public String getNodePatternAtLocation( String loc ) {
		logger.info( "loc=" + loc );
		if ( loc == null ) {
			return null;
		}
		StringBuilder str = new StringBuilder();
		for( DR_Node node : dr_nodes.values() ) {
			if ( loc.equals( node.getDcaeLocationName()) ) {
				if ( str.length() > 0 ) {
					str.append( ",");
				}
				str.append( node.getFqdn());
			}
		}
		logger.info( "returning " + str.toString() );
		return str.toString();
	}
}
