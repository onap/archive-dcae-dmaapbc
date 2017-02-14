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
import org.openecomp.dmaapbc.aaf.AafService;
import org.openecomp.dmaapbc.aaf.DmaapGrant;
import org.openecomp.dmaapbc.aaf.DmaapPerm;
import org.openecomp.dmaapbc.aaf.AafService.ServiceType;
import org.openecomp.dmaapbc.client.MrProvConnection;
import org.openecomp.dmaapbc.database.DatabaseClass;
import org.openecomp.dmaapbc.model.ApiError;
import org.openecomp.dmaapbc.model.MR_Client;
import org.openecomp.dmaapbc.model.MR_Cluster;
import org.openecomp.dmaapbc.model.Topic;
import org.openecomp.dmaapbc.model.DmaapObject.DmaapObject_Status;

public class MR_ClientService {
	static final Logger logger = Logger.getLogger(MR_ClientService.class);

	private Map<String, MR_Client> mr_clients = DatabaseClass.getMr_clients();
	private Map<String, MR_Cluster> clusters = DatabaseClass.getMr_clusters();
	private Map<String, Topic> topics = DatabaseClass.getTopics();
	private DmaapService dmaap = new DmaapService();
	
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
			logger.error( "Client  dcaeLocation that doesn't exist or not specified" );
			return null;
		}
		MR_Cluster cluster = clusters.get( client.getDcaeLocationName());
		if (  cluster != null ) {
			client.setTopicURL(cluster.genTopicURL(client.getFqtn()));
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
					return null;
				} 
			}

		
			logger.info( "cluster=" + cluster );
			MrProvConnection prov = new MrProvConnection();
			logger.info( "POST topic " + topic.getFqtn() + " to cluster " + cluster.getFqdn() + " in loc " + cluster.getDcaeLocationName());
			if ( prov.makeTopicConnection(cluster)) {
				String resp = prov.doPostTopic(topic);
				logger.info( "response: " + resp );
				if ( resp == null ) {
					client.setStatus( DmaapObject_Status.INVALID);
				}
			}
			
		} else {
			logger.info( "Client references a dcaeLocation that doesn't exist:" + client.getDcaeLocationName());
			client.setStatus( DmaapObject_Status.STAGED);
			//return null;
		}

		mr_clients.put( client.getMrClientId(), client );
	
		
		//TODO: this section on updating an existing topic with a new client needs to belong someplace else
		//Topic t = topics.get(topic.getFqtn());
		/*
		int n;
		ArrayList<MR_Client> tc = topic.getClients();
		if ( tc == null ) {
			n = 0;
			tc = new ArrayList<MR_Client>();
		} else {
			n = tc.size();
		}
		logger.info( "number of existing clients for topic is " + n );


		logger.info( "n=" + n + " tc=" + tc + " client=" + client );
		tc.add( client );
		topic.setClients(tc);
		*/
		topics.put(topic.getFqtn(), topic);
		err.setCode(200);
		
		return client;
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
		
	public MR_Client removeMr_Client( String key, ApiError apiError ) {
		MR_Client c =  mr_clients.get( key );
		if ( c == null ) {
			apiError.setCode(Status.NOT_FOUND.getStatusCode());
			apiError.setFields( "mrClientId");
			apiError.setMessage("mrClientId " + key + " not found" );
		} else {
			apiError.setCode(200);
		}
	
		return mr_clients.remove(key);
	}

	
}
