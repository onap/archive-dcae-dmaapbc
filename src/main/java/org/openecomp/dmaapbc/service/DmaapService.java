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

import org.apache.log4j.Logger;
import org.openecomp.dmaapbc.aaf.AafService;
import org.openecomp.dmaapbc.aaf.DmaapGrant;
import org.openecomp.dmaapbc.aaf.DmaapPerm;
import org.openecomp.dmaapbc.aaf.AafService.ServiceType;
import org.openecomp.dmaapbc.database.DatabaseClass;
import org.openecomp.dmaapbc.model.ApiError;
import org.openecomp.dmaapbc.model.DcaeLocation;
import org.openecomp.dmaapbc.model.Dmaap;
import org.openecomp.dmaapbc.model.MR_Client;
import org.openecomp.dmaapbc.model.Topic;
import org.openecomp.dmaapbc.model.DmaapObject.DmaapObject_Status;
import org.openecomp.dmaapbc.util.DmaapConfig;
import org.openecomp.dmaapbc.util.Singleton;

public class DmaapService {
	static final Logger logger = Logger.getLogger(DmaapService.class);
	
	private Singleton<Dmaap> dmaapholder = DatabaseClass.getDmaap();
	
	// TODO put these in properties file
	String topicFactory = "org.openecomp.dcae.dmaap.topicFactory";
	String topicMgrRole = "org.openecomp.dmaapBC.TopicMgr";
	
	// TODO confirm this is  equivalent to dmaap.getTopicNsRoot() so we can retire it
	String dcaeTopicNs = "org.openecomp.dcae.dmaap";
	
	
	public DmaapService() {
		
	}
	
	public Dmaap getDmaap() {
		logger.info( "entering getDmaap()" );
		return(dmaapholder.get());
	}
	
	public Dmaap addDmaap( Dmaap nd ) {
		
		logger.info( "entering addDmaap()" );
		Dmaap dmaap = dmaapholder.get();
		if ( dmaap.getVersion().equals( "0")) {

			nd.setLastMod();
			dmaapholder.update(nd);

			AafService aaf = new AafService( ServiceType.AAF_Admin);
			
			boolean anythingWrong = setTopicMgtPerms(  nd,  aaf ) || createMmaTopic();
					
			if ( anythingWrong ) {
				dmaap.setStatus(DmaapObject_Status.INVALID); 
			}
			else {
				dmaap.setStatus(DmaapObject_Status.VALID);  
			}
			

			return dmaap;
		
		}
		else { 
			return dmaap;
		}
	}
	
	public Dmaap updateDmaap( Dmaap nd ) {
		logger.info( "entering updateDmaap()" );
		
		boolean anythingWrong = false;
		AafService aaf = new AafService( ServiceType.AAF_Admin);
		Dmaap dmaap = dmaapholder.get();
		
		// some triggers for when we attempt to reprovision perms and MMA topic:
		// - if the DMaaP Name changes
		// - if the version is 0  (this is a handy test to force this processing by updating the DB)
		// - if the object is invalid, reprocessing might fix it.
		if ( ! dmaap.isStatusValid()  || ! nd.getDmaapName().equals(dmaap.getDmaapName()) || dmaap.getVersion().equals( "0") ) {
			nd.setLastMod();
			dmaapholder.update(nd);  //need to set this so the following perms will pick up any new vals.
			anythingWrong = setTopicMgtPerms(  nd,  aaf ) || createMmaTopic();
		}
					
		if ( anythingWrong ) {
			nd.setStatus(DmaapObject_Status.INVALID); 
		}
		else {
			nd.setStatus(DmaapObject_Status.VALID);  
		}
		nd.setLastMod();
		dmaapholder.update(nd);  // may need to update status...
		return(dmaapholder.get());
		
	}
	
	public String getTopicPerm(){
		Dmaap dmaap = dmaapholder.get();
		return getTopicPerm( dmaap.getDmaapName() );
	}
	public String getTopicPerm( String val ) {
		Dmaap dmaap = dmaapholder.get();
		return dmaap.getTopicNsRoot() + "." + val + ".mr.topic";
	}
	
	public String getBridgeAdminFqtn(){
		Dmaap dmaap = dmaapholder.get();
		return(dmaap.getTopicNsRoot() + "." + dmaap.getDmaapName() + "." + dmaap.getBridgeAdminTopic());
	}

	private boolean setTopicMgtPerms( Dmaap nd, AafService aaf ){
		String[] actions = { "create", "destroy" };
		String instance = ":" + dcaeTopicNs + "." + nd.getDmaapName() + ".mr.topic:" + dcaeTopicNs + "." + nd.getDmaapName();
		
		for( String action : actions ) {

			DmaapPerm perm = new DmaapPerm( topicFactory, instance, action );
		
			int rc = aaf.addPerm( perm );
			if ( rc != 201 &&  rc != 409 ) {
				logger.error( "unable to add perm for "+ topicFactory + "|" + instance + "|" + action );
				return true;
			}

			DmaapGrant grant = new DmaapGrant( perm, topicMgrRole );
			rc = aaf.addGrant( grant );
			if ( rc != 201 && rc != 409 ) {
				logger.error( "unable to grant to " + topicMgrRole + " perm for "+ topicFactory + "|" + instance + "|" + action );
				return true;
			}
		}
		
		String t = dcaeTopicNs +"." + nd.getDmaapName() + ".mr.topic";
		String[] s = { "view", "pub", "sub" };
		actions = s;
		instance = "*";
		
		for( String action : actions ) {

			DmaapPerm perm = new DmaapPerm( t, instance, action );
		
			int rc = aaf.addPerm( perm );
			if ( rc != 201 &&  rc != 409 ) {
				logger.error( "unable to add perm for "+ t + "|" + instance + "|" + action );
				return true;
			}

			DmaapGrant grant = new DmaapGrant( perm, topicMgrRole );
			rc = aaf.addGrant( grant );
			if ( rc != 201 && rc != 409 ) {
				logger.error( "unable to grant to " + topicMgrRole + " perm for "+ topicFactory + "|" + instance + "|" + action );
				return true;
			}
				
		}
		return false;
	}
	
	// create the special topic for MMA provisioning.
	// return true indicating a problem in topic creation, 
	// else false means it was ok  (created or previously existed)
	private boolean createMmaTopic() {
		boolean rc = true;
		DmaapConfig p = (DmaapConfig)DmaapConfig.getConfig();
		Dmaap dmaap = dmaapholder.get();
		
		ArrayList<MR_Client> clients = new ArrayList<MR_Client>();
		String[] actions = { "pub", "sub", "view" };
		String centralMR = new DcaeLocationService().getCentralLocation();
		if ( centralMR == null ) {
			return rc;
		}
		logger.info( "Location for " + dmaap.getBridgeAdminTopic() + " is " + centralMR );
	
		// first client is the Role used by Bus Controller to send messages to MMA
		String provRole = p.getProperty("MM.ProvRole");
		MR_Client nClient = new MR_Client();
		nClient.setAction(actions);
		nClient.setClientRole(provRole);
		nClient.setDcaeLocationName(centralMR);
		clients.add( nClient );
	
		// second client is the Role used by MMA to listen to messages from Bus Controller
		String agentRole = p.getProperty("MM.AgentRole");
		nClient = new MR_Client();
		nClient.setAction(actions);
		nClient.setClientRole(agentRole);
		nClient.setDcaeLocationName(centralMR);
		clients.add( nClient );
	
		// initialize Topic
		Topic mmaTopic = new Topic();
		mmaTopic.setTopicName(dmaap.getBridgeAdminTopic());
		mmaTopic.setClients(clients);
		mmaTopic.setOwner("BusController");
		mmaTopic.setTopicDescription("topic reserved for MirrorMaker Administration");
		mmaTopic.setTnxEnabled("false");
		
		ApiError err = new ApiError();
		TopicService svc = new TopicService();
		Topic nTopic = svc.addTopic(mmaTopic, err);
		if ( err.is2xx() || err.getCode() == 409 ) {
			return false;
		}
		logger.error( "Unable to create topic for " + dmaap.getBridgeAdminTopic() + " err=" + err.getFields() + " fields=" + err.getFields() + " msg=" + err.getMessage());
		
		return rc;
		
	}
}
