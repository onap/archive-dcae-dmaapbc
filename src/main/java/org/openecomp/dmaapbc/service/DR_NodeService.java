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
import org.openecomp.dmaapbc.logging.BaseLoggingClass;
import org.openecomp.dmaapbc.model.ApiError;
import org.openecomp.dmaapbc.model.DR_Node;
import org.openecomp.dmaapbc.model.DmaapObject.DmaapObject_Status;

public class DR_NodeService extends BaseLoggingClass {
	private  class DrProv {
		String currentNodes;
		String currentStaticNodes;
		
		private String getX( String X, ApiError apiError ) {
			
			DrProvConnection prov = new DrProvConnection();
			prov.makeNodesConnection( X );	
			String resp  = prov.doGetNodes(  apiError );
			logger.info( "rc=" + apiError.getCode() );
			return resp;
		}
		
		private void setX( String X, String list, ApiError apiError ) {
			DrProvConnection prov = new DrProvConnection();
			prov.makeNodesConnection( X, list );	
			String resp = prov.doPutNodes( apiError );
		}
		
		private String removeFromList( String aNode, String aList ) {
			String[] nodeList = aList.split("\\|");
			StringBuilder res = new StringBuilder();
			for ( String n: nodeList ) {
				logger.info( "compare existing node " + n + " vs " + aNode );
				if ( ! n.equals(aNode)) {
					if (res.length() > 0 ) {
						res.append( "|" );
					}
					res.append(n);
				}
			}
			logger.info( "result=" + res.toString() );
			return res.toString();
		}
		
		 boolean containsNode( String aNode , ApiError apiError ){
	
			//DrProvConnection prov = new DrProvConnection();
			//prov.makeNodesConnection();	
			currentNodes = getX( "NODES", apiError );
			if ( ! apiError.is2xx() || currentNodes == null ) {
				return false;
			}
			logger.info( "NODES now=" + currentNodes );
			String[] nodeList = currentNodes.split("\\|");
			for( String n: nodeList ) {
				logger.info( "compare existing node " + n + " vs " + aNode );
				if ( n.equals(aNode) ) {
					return true;
				}
			}
			return false;
		}
		
		 void addNode( String aNode, ApiError apiError ) {
			
			currentNodes = currentNodes + "|" + aNode;
			setX( "NODES", currentNodes, apiError );	

			
		}
		void removeNode( String aNode, ApiError apiError ) {
			currentNodes = removeFromList( aNode, currentNodes );
			setX( "NODES", currentNodes, apiError );			
		}

		public boolean containsStaticNode(String aNode, ApiError apiError) {
	
			//DrProvConnection prov = new DrProvConnection();
			//prov.makeNodesConnection();	
			currentStaticNodes = getX( "STATIC_ROUTING_NODES", apiError );
			if (! apiError.is2xx() || currentStaticNodes == null ) {
				return false;
			}
			logger.info( "STATIC_ROUTING_NODES now=" + currentNodes );
			String[] nodeList = currentStaticNodes.split("\\|");
			for( String n: nodeList ) {
				logger.info( "compare existing node " + n + " vs " + aNode );
				if ( n.equals(aNode) ) {
					return true;
				}
			}
			return false;
		}
		

		public void addStaticNode(String aNode, ApiError apiError) {
			currentStaticNodes = currentStaticNodes + "|" + aNode;
			setX( "STATIC_ROUTING_NODES", currentStaticNodes, apiError );
		}
		void removeStaticNode( String aNode, ApiError apiError ) {
			currentStaticNodes = removeFromList( aNode, currentStaticNodes );
			setX( "STATIC_ROUTING_NODES", currentStaticNodes, apiError );			
		}
	}	
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
		
		DrProv drProv = new DrProv();

		if ( ! drProv.containsNode( node.getFqdn(), apiError ) && apiError.is2xx() ) {
			drProv.addNode( node.getFqdn(), apiError );
		}
		if ( ! apiError.is2xx()) {
			return null;
		}
		DcaeLocationService locService = new DcaeLocationService();
		if ( locService.isEdgeLocation( node.getDcaeLocationName()) && ! drProv.containsStaticNode( node.getFqdn(), apiError ) ) {
			if ( apiError.is2xx() ) {
				drProv.addStaticNode( node.getFqdn(), apiError );
			}
		}
		if ( ! apiError.is2xx()) {
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
		
		DrProv drProv = new DrProv();
		if ( drProv.containsNode( old.getFqdn(), apiError ) && apiError.is2xx() ) {
			drProv.removeNode( old.getFqdn(), apiError );
		}
		DcaeLocationService locService = new DcaeLocationService();
		if ( locService.isEdgeLocation( old.getDcaeLocationName()) && drProv.containsStaticNode( old.getFqdn(), apiError ) ) {
			if ( apiError.is2xx()) {
				drProv.removeStaticNode( old.getFqdn(), apiError );
			}
			
		}
		
		apiError.setCode(200);
		return dr_nodes.remove(nodeName);
	}		

	public String getNodePatternAtLocation( String loc, boolean allowMult ) {
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
				if ( ! allowMult ) {
					break;
				}
			}
		}
		logger.info( "returning " + str.toString() );
		return str.toString();
	}
}
