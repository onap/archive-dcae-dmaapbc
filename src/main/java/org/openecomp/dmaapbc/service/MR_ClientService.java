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


import org.openecomp.dmaapbc.aaf.AafService;
import org.openecomp.dmaapbc.aaf.DmaapGrant;
import org.openecomp.dmaapbc.aaf.DmaapPerm;
import org.openecomp.dmaapbc.aaf.AafService.ServiceType;
import org.openecomp.dmaapbc.client.MrProvConnection;
import org.openecomp.dmaapbc.database.DatabaseClass;
import org.openecomp.dmaapbc.logging.BaseLoggingClass;
import org.openecomp.dmaapbc.model.ApiError;
import org.openecomp.dmaapbc.model.DcaeLocation;
import org.openecomp.dmaapbc.model.MR_Client;
import org.openecomp.dmaapbc.model.MR_Cluster;
import org.openecomp.dmaapbc.model.Topic;
import org.openecomp.dmaapbc.model.DmaapObject.DmaapObject_Status;
import org.openecomp.dmaapbc.util.DmaapConfig;

public class MR_ClientService extends BaseLoggingClass{

	private int deleteLevel;
	private Map<String, MR_Client> mr_clients = DatabaseClass.getMr_clients();
	private Map<String, MR_Cluster> clusters = DatabaseClass.getMr_clusters();
	private Map<String, Topic> topics = DatabaseClass.getTopics();
	private Map<String, DcaeLocation> locations = DatabaseClass.getDcaeLocations();
	private DmaapService dmaap = new DmaapService();
	
	public MR_ClientService() {
		DmaapConfig p = (DmaapConfig)DmaapConfig.getConfig();
		
		deleteLevel = Integer.valueOf(p.getProperty("MR.ClientDeleteLevel", "0" ));
	}
	
	public Map<String, MR_Client> getMR_Clients() {			
		return mr_clients;
	}
		
	public List<MR_Client> getAllMr_Clients() {
		return new ArrayList<MR_Client>(mr_clients.values());
	}
	
	public ArrayList<MR_Client> getAllMrClients(String fqtn) {
		ArrayList<MR_Client> results = new ArrayList<MR_Client>();
		for (Map.Entry<String, MR_Client> entry : mr_clients.entrySet())
		{
			MR_Client client = entry.getValue();
		    if ( fqtn.equals(client.getFqtn() ) ) {
		    	results.add( client );
		    }
		}
		return results;
	}	

	public ArrayList<MR_Client> getClientsByLocation(String location) {
		ArrayList<MR_Client> results = new ArrayList<MR_Client>();
		for (Map.Entry<String, MR_Client> entry : mr_clients.entrySet())
		{
			MR_Client client = entry.getValue();
		    if ( location.equals(client.getDcaeLocationName() ) ) {
		    	results.add( client );
		    }
		}
		return results;
	}	
	
	public void refreshClients( String location ) {
		ApiError err = new ApiError();
		ArrayList<MR_Client> clients = getClientsByLocation( location );
		for( MR_Client client : clients ) {
			Topic topic = topics.get(client.getFqtn());
			if ( topic != null ) {
				addMr_Client( client, topic, err);
			}
			
			
		}
	}
	
	public MR_Client getMr_Client( String key, ApiError apiError ) {			
		MR_Client c =  mr_clients.get( key );
		if ( c == null ) {
			apiError.setCode(Status.NOT_FOUND.getStatusCode());
			apiError.setFields( "mrClientId");
			apiError.setMessage("mrClientId " + key + " not found" );
		} else {
			apiError.setCode(200);
		}
		return c;
	}

	public MR_Client addMr_Client( MR_Client client, Topic topic, ApiError err ) {
		if ( client.getDcaeLocationName().isEmpty()) {
			logger.info( "Client  dcaeLocation that doesn't exist or not specified" );
			return null;
		}
		grantClientPerms(  client,  err);
		if ( ! client.isStatusValid()) {
			return null;
		}
		String centralFqdn = null;
		DcaeLocation candidate = locations.get(client.getDcaeLocationName());
		if ( candidate != null && candidate.isCentral() ) {
			DmaapConfig p = ( DmaapConfig)DmaapConfig.getConfig();
			centralFqdn = p.getProperty("MR.CentralCname");
		}
		MR_Cluster cluster = clusters.get( client.getDcaeLocationName());
		if (  cluster != null ) {
			client.setTopicURL(cluster.genTopicURL(centralFqdn, client.getFqtn()));
			if ( centralFqdn == null ) {
				client.setStatus( addTopicToCluster( cluster, topic, err));
				if( ! err.is2xx() && err.getCode() != 409 ) {
					topic.setFqtn(err.getMessage());
					return null;
				}
			
			} else {
				MR_ClusterService clusters = new MR_ClusterService();	
				// in 1610, MM should only exist for edge-to-central
				//  we use a cname for the central target
				// but still need to provision topics on all central MRs
				for( MR_Cluster central: clusters.getCentralClusters() ) {
					client.setStatus( addTopicToCluster( central, topic, err));
					if( ! err.is2xx() && err.getCode() != 409 ) {
						topic.setFqtn(err.getMessage());
						return null;
					}
				}
			}
			
		} else {
			logger.info( "Client references a dcaeLocation that doesn't exist:" + client.getDcaeLocationName());
			client.setStatus( DmaapObject_Status.STAGED);
			//return null;
		}

		mr_clients.put( client.getMrClientId(), client );

		err.setCode(200);
		
		return client;
	}
	
	private DmaapObject_Status addTopicToCluster( MR_Cluster cluster, Topic topic, ApiError err  ){
		
		MrProvConnection prov = new MrProvConnection();
		logger.info( "POST topic " + topic.getFqtn() + " to cluster " + cluster.getFqdn() + " in loc " + cluster.getDcaeLocationName());
		if ( prov.makeTopicConnection(cluster)) {
			String resp = prov.doPostTopic(topic, err);
			logger.info( "response code: " + err.getCode() );
			if ( err.is2xx() || err.getCode() == 409 ) {
				return DmaapObject_Status.VALID;
			} 
		}
		return DmaapObject_Status.INVALID;
	}
	
	private void grantClientPerms( MR_Client client, ApiError err) {
		AafService aaf = new AafService(ServiceType.AAF_TopicMgr);
		
		String instance = ":topic." + client.getFqtn();
		client.setStatus( DmaapObject_Status.VALID);
		for( String want : client.getAction() ) {
			int rc;
			DmaapPerm perm = new DmaapPerm( dmaap.getTopicPerm(), instance, want );
			DmaapGrant g = new DmaapGrant( perm, client.getClientRole() );
			rc = aaf.addGrant( g );
			if ( rc != 201 && rc != 409 ) {
				client.setStatus( DmaapObject_Status.INVALID);
				err.setCode(rc);
				err.setMessage( "Grant of " + dmaap.getTopicPerm() + "|" + instance + "|" + want + " failed for " + client.getClientRole() );
				logger.warn( err.getMessage());
				return;
			} 
		}
	}
	
	private void revokeClientPerms( MR_Client client, ApiError err) {
		AafService aaf = new AafService(ServiceType.AAF_TopicMgr);
		
		String instance = ":topic." + client.getFqtn();
		client.setStatus( DmaapObject_Status.VALID);
		for( String want : client.getAction() ) {
			int rc;
			DmaapPerm perm = new DmaapPerm( dmaap.getTopicPerm(), instance, want );
			DmaapGrant g = new DmaapGrant( perm, client.getClientRole() );
			rc = aaf.delGrant( g );
			if ( rc != 200 && rc != 404 ) {
				client.setStatus( DmaapObject_Status.INVALID);
				err.setCode(rc);
				err.setMessage( "Revoke of " + dmaap.getTopicPerm() + "|" + instance + "|" + want + " failed for " + client.getClientRole() );
				logger.warn( err.getMessage());
				return;
			} 
		}
	}
	
	public MR_Client updateMr_Client( MR_Client client, ApiError apiError ) {
		MR_Client c =  mr_clients.get( client.getMrClientId());
		if ( c == null ) {
			apiError.setCode(Status.NOT_FOUND.getStatusCode());
			apiError.setFields( "mrClientId");
			apiError.setMessage("mrClientId " + client.getMrClientId() + " not found" );
		} else {
			apiError.setCode(200);
		}
		mr_clients.put( client.getMrClientId(), client );
		return client;
	}
		
	public void removeMr_Client( String key, boolean updateTopicView, ApiError apiError ) {
		MR_Client client =  mr_clients.get( key );
		if ( client == null ) {
			apiError.setCode(Status.NOT_FOUND.getStatusCode());
			apiError.setFields( "mrClientId");
			apiError.setMessage("mrClientId " + key + " not found" );
			return;
		} else {
			apiError.setCode(200);
		}
		
		if ( updateTopicView == true ) {

			TopicService topics = new TopicService();
			
			Topic t = topics.getTopic(client.getFqtn(), apiError );
			if ( t != null ) {	
				ArrayList<MR_Client> tc = t.getClients();
				for( MR_Client c: tc) {
					if ( c.getMrClientId().equals(client.getMrClientId())) {
						tc.remove(c);
						break;
					}
				}
				t.setClients(tc);
				topics.updateTopic( t, apiError );
			}

		}

		
		// remove from AAF
		if ( deleteLevel >= 2 ) {
			revokeClientPerms( client, apiError );
			if ( ! apiError.is2xx()) {
				return;
			}
		}
		// remove from DB 
		if ( deleteLevel >= 1 ) {
			mr_clients.remove(key);
		}

		return;
	}
	
}
