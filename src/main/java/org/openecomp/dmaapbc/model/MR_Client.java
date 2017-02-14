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

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;
import org.openecomp.dmaapbc.database.DatabaseClass;

@XmlRootElement
public class MR_Client extends DmaapObject {
	static final Logger logger = Logger.getLogger(MR_Client.class);

	private String dcaeLocationName;
	private	String	topicURL;
	private String fqtn;
	private String clientRole;
	private String[] action;
	private String mrClientId;
	

	public MR_Client() {
		this.mrClientId = DatabaseClass.getNextClientId();
		this.lastMod = new Date();
		this.setLastMod();
		logger.debug( "MR_Client constructor " + this.lastMod );
			
	}
	
	public MR_Client( String dLN,
					String f,
					String cR,
					String[] a ) {
		this.dcaeLocationName = dLN;
		this.fqtn = f;
		this.clientRole = cR;
		int i = 0;
		
		if ( this.action == null ) {
			this.action = new String[a.length];
		}
		for( String aa : a ) {
			this.action[i++] = new String( aa );
		}
		this.setStatus( DmaapObject_Status.NEW );
		this.mrClientId = DatabaseClass.getNextClientId();
		this.setLastMod();
		logger.debug( "MR_Client constructor w initialization " + this.lastMod );
	}

	public String getDcaeLocationName() {
		return dcaeLocationName;
	}

	public void setDcaeLocationName(String dcaeLocationName) {
		this.dcaeLocationName = dcaeLocationName;
	}

	public String getFqtn() {
		return fqtn;
	}

	public void setFqtn(String fqtn) {
		this.fqtn = fqtn;
	}

	public String getClientRole() {
		return clientRole;
	}

	public void setClientRole(String clientRole) {
		this.clientRole = clientRole;
	}

	public String[] getAction() {
		return action;
	}

	public void setAction(String[] action) {
		this.action = action;
	}

	public String getMrClientId() {
		return mrClientId;
	}

	public void setMrClientId(String mrClientId) {
		this.mrClientId = mrClientId;
	}



	public String getTopicURL() {
		return topicURL;
	}

	public void setTopicURL(String topicURL) {
		this.topicURL = topicURL;
	}
	
}
