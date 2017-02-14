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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.openecomp.dmaapbc.aaf.AafService;
import org.openecomp.dmaapbc.aaf.DmaapPerm;
import org.openecomp.dmaapbc.aaf.AafService.ServiceType;
import org.openecomp.dmaapbc.database.DatabaseClass;
import org.openecomp.dmaapbc.model.ApiError;
import org.openecomp.dmaapbc.model.Dmaap;
import org.openecomp.dmaapbc.model.MR_Client;
import org.openecomp.dmaapbc.model.MR_Cluster;
import org.openecomp.dmaapbc.model.MirrorMaker;
import org.openecomp.dmaapbc.model.Topic;
import org.openecomp.dmaapbc.model.DmaapObject.DmaapObject_Status;
import org.openecomp.dmaapbc.util.DmaapConfig;
import org.openecomp.dmaapbc.util.Graph;

public class TopicService  {
	static final Logger logger = Logger.getLogger(TopicService.class);
	
	// TODO put these in properties file
	String topicFactory = "org.openecomp.dcae.dmaap.topicFactory";

	private Map<String, Topic> mr_topics = DatabaseClass.getTopics();
	private Map<String, MR_Cluster> clusters = DatabaseClass.getMr_clusters();
	
	private Dmaap dmaap = new DmaapService().getDmaap();
	private MR_ClientService clientService = new MR_ClientService();
	private MirrorMakerService	bridge = new MirrorMakerService();
	
	
	public Map<String, Topic> getTopics() {			
		return mr_topics;
	}
		
	public List<Topic> getAllTopics() {
		ArrayList<Topic> topics = new ArrayList<Topic>(mr_topics.values());
		for( Topic topic: topics ) {
			topic.setClients( clientService.getAllMrClients(topic.getFqtn()));
		}
		return topics;
	}
	
		
	public Topic getTopic( String key, ApiError apiError ) {	
		logger.info( "getTopic: key=" + key);
		Topic t = mr_topics.get( key );
		if ( t == null ) {
			apiError.setCode(Status.NOT_FOUND.getStatusCode());
			apiError.setFields( "fqtn");
			apiError.setMessage("topic with fqtn " + key + " not found");
			return null;
		}
		t.setClients( clientService.getAllMrClients( key ));
		apiError.setCode(Status.OK.getStatusCode());
		return t;
	}

	public Topic addTopic( Topic topic, ApiError err ) {
		logger.info( "Entry: addTopic");
		String nFqtn =  Topic.genFqtn( topic.getTopicName() );
		if ( getTopic( nFqtn, err ) != null ) {
			String t = "topic already exists: " + nFqtn;
			logger.info( t );
			err.setMessage( t );
			err.setFields( "fqtn");
			err.setCode(Status.CONFLICT.getStatusCode());
			return null;
		}
		logger.info( "fqtn: " + nFqtn );
		topic.setFqtn( nFqtn );

		boolean anythingWrong = false;
		AafService aaf = new AafService(ServiceType.AAF_TopicMgr);
		String t = dmaap.getTopicNsRoot() + "." + dmaap.getDmaapName() + ".mr.topic";
		String instance = ":topic." + topic.getFqtn();

		String[] actions = { "pub", "sub", "view" };
		for ( String action : actions ){
			DmaapPerm perm = new DmaapPerm( t, instance, action );
			int rc = aaf.addPerm( perm );
			if ( rc != 201 && rc != 409 ) {
				err.setCode(500);
				err.setMessage("Unexpected response from AAF:" + rc );
				err.setFields("t="+t + " instance="+ instance + " action="+ action);
				return null;
			}
		}


		if ( topic.getNumClients() > 0 ) {
			ArrayList<MR_Client> clients = new ArrayList<MR_Client>(topic.getClients());
		
	
			ArrayList<MR_Client> clients2 = new ArrayList<MR_Client>();
			for ( Iterator<MR_Client> it = clients.iterator(); it.hasNext(); ) {
				MR_Client c = it.next();

				logger.info( "c fqtn=" + c.getFqtn() + " ID=" + c.getMrClientId() + " url=" + c.getTopicURL());
				MR_Client nc = new MR_Client( c.getDcaeLocationName(), topic.getFqtn(), c.getClientRole(), c.getAction());
				nc.setFqtn(topic.getFqtn());
				logger.info( "nc fqtn=" + nc.getFqtn() + " ID=" + nc.getMrClientId() + " url=" + nc.getTopicURL());
				clients2.add( clientService.addMr_Client(nc, topic, err));
				if ( ! err.is2xx()) {
					return null;
				}
			}

			topic.setClients(clients2);
			Graph graph = new Graph( clients2 );
			
			String centralFqdn = new String();
			if ( graph.isHasCentral() ) {
				centralFqdn = clusters.get( graph.getCentralLoc() ).getFqdn();
			
			}

			Collection<String> locations = graph.getKeys();
			for( String loc : locations ) {
				logger.info( "loc=" + loc );
				MR_Cluster cluster = clusters.get(loc);
				logger.info( "cluster=" + cluster );

				if ( graph.isHasCentral() &&  ! centralFqdn.equals(cluster.getFqdn())) {
					logger.info( "Create a MM from " + cluster.getFqdn() + " to " + centralFqdn );
					try { 
						MirrorMaker mm = bridge.getMirrorMaker(cluster.getFqdn(), centralFqdn);
						if ( mm == null ) {
							mm = new MirrorMaker(cluster.getFqdn(), centralFqdn);
						}
						mm.addTopic(topic.getFqtn());
						bridge.updateMirrorMaker(mm);
					} catch ( Exception ex ) {
						err.setCode(500);
						err.setFields( "mirror_maker.topic");
						err.setMessage("Unexpected condition: " + ex );
						anythingWrong = true;
						break;
					}
					
				}
				
			}
		}
	
		if ( anythingWrong ) {
			topic.setStatus( DmaapObject_Status.INVALID);
			return null;
		}
	
		topic.setStatus( DmaapObject_Status.VALID);			

		mr_topics.put( nFqtn, topic );
		//String prov = bridge.commit();
		//logger.info( "prov=" + prov);
		err.setCode(Status.OK.getStatusCode());
		return topic;
	}
	
		
	public Topic updateTopic( Topic topic ) {
		if ( topic.getFqtn().isEmpty()) {
			return null;
		}
		mr_topics.put( topic.getFqtn(), topic );
		return topic;
	}
		
	public Topic removeTopic( String pubId, ApiError apiError ) {
		if ( mr_topics.get(pubId) == null ) {
			apiError.setCode(Status.NOT_FOUND.getStatusCode());
			apiError.setMessage("Topic " + pubId + " does not exist");
			apiError.setFields("fqtn");
			return null;
		}
		apiError.setCode(Status.OK.getStatusCode());
		return mr_topics.remove(pubId);
	}	
	public static ApiError setBridgeClientPerms( MR_Cluster node ) {
		DmaapConfig p = (DmaapConfig)DmaapConfig.getConfig();
		String mmProvRole = p.getProperty("MM.ProvRole");
		String mmAgentRole = p.getProperty("MM.AgentRole");
		String[] Roles = { mmProvRole, mmAgentRole };
		String[] actions = { "view", "pub", "sub" };
		Topic bridgeAdminTopic = new Topic();
		bridgeAdminTopic.setTopicName( DatabaseClass.getDmaap().get().getBridgeAdminTopic());
		bridgeAdminTopic.setTopicDescription( "RESERVED topic for MirroMaker Provisioning");
		bridgeAdminTopic.setOwner( "DBC" );
		ArrayList<MR_Client> clients = new ArrayList<MR_Client>();
		for( String role: Roles ) {
			MR_Client client = new MR_Client();
			client.setAction(actions);
			client.setClientRole(role);
			client.setDcaeLocationName( node.getDcaeLocationName());
			clients.add( client );
		}
		bridgeAdminTopic.setClients(clients);
		
		TopicService ts = new TopicService();
		ApiError err = new ApiError();
		ts.addTopic(bridgeAdminTopic, err);
		
		if ( err.is2xx() || err.getCode() == 409 ){
			err.setCode(200);
			return err;
		}
		
		logger.error( "Unable to create MM provisioning topic " + bridgeAdminTopic.getFqtn());
		return err;
	}	
}
