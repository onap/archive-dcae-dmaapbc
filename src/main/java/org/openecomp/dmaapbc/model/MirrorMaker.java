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

package org.openecomp.dmaapbc.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openecomp.dmaapbc.logging.DmaapbcLogMessageEnum;
import org.openecomp.dmaapbc.service.MirrorMakerService;

public class MirrorMaker extends DmaapObject {
	static final Logger logger = Logger.getLogger(MirrorMaker.class);

	private String	sourceCluster;
	private String	targetCluster;
	private String	mmName;
	private	ArrayList<String> topics;  //re-using this var name for backwards DB compatibility
	
	private Set<ReplicationVector> vectors;

	
	public MirrorMaker(){
		
	}

	public MirrorMaker(String source, String target) {
		sourceCluster = source;
		targetCluster = target;
		mmName = genKey(source, target);
		vectors = new HashSet<ReplicationVector>();
		topics = new ArrayList<String>();

	}
	
	public String getMmName() {
		return mmName;
	}

	public void setMmName(String mmName) {
		this.mmName = mmName;
	}

	
	public void addVector( String fqtn, String source, String target ) {
		logger.info( "addVector: fqtn=" + fqtn + " source=" + source + " target=" + target );
		if ( ! sourceCluster.equals( source ) ){
			errorLogger.error( DmaapbcLogMessageEnum.MM_CIRCULAR_REF,  source,  sourceCluster );
		}
		vectors.add(new ReplicationVector( fqtn, source, target ));
	}
	
	public void delVector( String fqtn, String source, String target ) {
		vectors.remove(new ReplicationVector( fqtn, source, target));
	}

	
	
	public String toJSON() {
		StringBuilder str = new StringBuilder( "{ \"source\": " + sourceCluster + ",\"topics\": ["  );
		int numTargets = 0;
		for (ReplicationVector rv: vectors) {
			if ( numTargets > 0 ) {
				str.append( ",");
			}
			str.append( " \"target\": " + rv.getTargetCluster() + ", \"topic\": " + rv.getFqtn());
			numTargets++;
		}
		str.append( "] }" );
		
		return str.toString();
	}
		
	
	// returns the JSON for MM message containing which Topics to replicate
	/* 
	 * example:
	 * 
			{
			    "messageID":"12349",
			    "updateWhiteList":
			        {
			            "name":"Global1ToGlobal3",
			            "whitelist":"org.openecomp.dcae.topic1,org.openecomp.dcae.topic2"
			        }
			}   
	 */
	public String updateWhiteList() {
		StringBuilder str = new StringBuilder( "{ \"messageID\": \"" + MirrorMakerService.genTransactionId() + "\", \"updateWhiteList\": {"  );
		str.append( " \"name\": \"" + this.getMmName() + "\", \"whitelist\": \"" );
		int numTargets = 0;

		//for (ReplicationVector rv: vectors) {
		for (String rv: topics) {
			if ( numTargets > 0 ) {
				str.append( ",");
			}
			//str.append(  rv.getFqtn() );
			str.append( rv );
			numTargets++;
		}
		str.append( "\" } }" );
		
		return str.toString();
	}
	
	// returns the JSON for MM message indicating that a MM agent is needed between two clusters
	// example:
	/*
	 * 
			{
			    "messageID":"12345"
			    "createMirrorMaker":
			        {
			            "name":"Global1ToGlobal2",
			            "consumer":"192.168.0.1:2181",
			            "producer":"192.168.0.2:9092"
			        }
			}
	 */
	public String createMirrorMaker() {
		StringBuilder str = new StringBuilder( "{ \"messageID\": \"" + MirrorMakerService.genTransactionId() + "\", \"createMirrorMaker\": {"  );
		str.append( " \"name\": \"" + this.getMmName() + "\", " );
		str.append( " \"consumer\": \"" + this.sourceCluster + ":2181\", " );
		str.append( " \"producer\": \"" + this.targetCluster + ":9092\" ");
		
		str.append( " } }" );
		
		return str.toString();
	}


	public String getSourceCluster() {
		return sourceCluster;
	}

	public void setSourceCluster(String sourceCluster) {
		this.sourceCluster = sourceCluster;
	}

	public String getTargetCluster() {
		return targetCluster;
	}

	public void setTargetCluster(String targetCluster) {
		this.targetCluster = targetCluster;
	}


	public Set<ReplicationVector> getVectors() {
		return vectors;
	}

	public void setVectors(Set<ReplicationVector> vectors) {
		this.vectors = vectors;
	}
	public ArrayList<String> getTopics() {
		return topics;
	}

	//public void setVectors(Set<ReplicationVector> vectors) {
	public void setTopics(ArrayList<String> topics) {
		this.topics = topics;
	}


	public static String genKey( String s, String t) {
		StringBuilder str = new StringBuilder();
		str.append(s);
		str.append("-To-");
		str.append(t);
		return str.toString();
	}


	
	public void addTopic( String topic ) {
		topics.add(topic);
	}
	
	public int getTopicCount() {
		return topics.size();
	}
}
