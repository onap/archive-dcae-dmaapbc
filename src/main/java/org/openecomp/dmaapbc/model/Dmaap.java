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

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

@XmlRootElement
public class Dmaap extends DmaapObject {
	
	private String version;
	private String topicNsRoot;
	private String dmaapName;
	private String drProvUrl;
	private	String	bridgeAdminTopic;
	private	String loggingUrl;
	private	String nodeKey;
	private	String	accessKeyOwner;



	// no-op constructor used by framework
	public Dmaap() {
		
	}
	
	public Dmaap( String ver, 
					String tnr,
					String dn,
					String dpu,
					String lu,
					String bat,
					String nk,
					String ako ) {
		this.version = ver;
		this.topicNsRoot = tnr;
		this.dmaapName = dn;
		this.drProvUrl = dpu;
		this.bridgeAdminTopic = bat;
		this.loggingUrl = lu;
		this.nodeKey = nk;
		this.accessKeyOwner = ako;
		this.setStatus( DmaapObject_Status.NEW );
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getTopicNsRoot() {
		return topicNsRoot;
	}

	public void setTopicNsRoot(String topicNsRoot) {
		this.topicNsRoot = topicNsRoot;
	}

	public String getDmaapName() {
		return dmaapName;
	}

	public void setDmaapName(String dmaapName) {
		this.dmaapName = dmaapName;
	}

	public String getDrProvUrl() {
		return drProvUrl;
	}

	public void setDrProvUrl(String drProvUrl) {
		this.drProvUrl = drProvUrl;
	}


	public String getNodeKey() {
		return nodeKey;
	}

	public void setNodeKey(String nodeKey) {
		this.nodeKey = nodeKey;
	}

	public String getAccessKeyOwner() {
		return accessKeyOwner;
	}

	public void setAccessKeyOwner(String accessKeyOwner) {
		this.accessKeyOwner = accessKeyOwner;
	}

	
	public String getBridgeAdminTopic() {
		return bridgeAdminTopic;
	}

	public void setBridgeAdminTopic(String bridgeAdminTopic) {
		this.bridgeAdminTopic = bridgeAdminTopic;
	}

	public String getLoggingUrl() {
		return loggingUrl;
	}

	public void setLoggingUrl(String loggingUrl) {
		this.loggingUrl = loggingUrl;
	}

	
}
