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

import org.openecomp.dmaapbc.util.RandomString;

@XmlRootElement
public class DR_Pub extends DmaapObject {

	private String dcaeLocationName;
	private String username;
	private String userpwd;
	private String feedId;
	private String pubId;

	
	public DR_Pub() {
		status = DmaapObject_Status.EMPTY;
		
	}
	
	public DR_Pub( String dLN ) {
		this.dcaeLocationName = dLN;
		this.status = DmaapObject_Status.STAGED;
	}
	
	public DR_Pub( String dLN, 
					String uN,
					String uP,
					String fI,
					String pI ) {
		this.dcaeLocationName = dLN;
		this.username = uN;
		this.userpwd = uP;
		this.feedId = fI;
		this.pubId = pI;
		this.status = DmaapObject_Status.VALID;
	}


	public DR_Pub( String dLN, 
							String uN,
							String uP,
							String fI ) {
		this.dcaeLocationName = dLN;
		this.username = uN;
		this.userpwd = uP;
		this.feedId = fI;
		this.pubId = fI + "." +  DR_Pub.nextKey();
		this.status = DmaapObject_Status.VALID;	
	}
			

	public String getDcaeLocationName() {
		return dcaeLocationName;
	}

	public void setDcaeLocationName(String dcaeLocationName) {
		this.dcaeLocationName = dcaeLocationName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUserpwd() {
		return userpwd;
	}

	public void setUserpwd(String userpwd) {
		this.userpwd = userpwd;
	}

	public String getFeedId() {
		return feedId;
	}

	public void setFeedId(String feedId) {
		this.feedId = feedId;
	}

	public String getPubId() {
		return pubId;
	}

	public void setPubId(String pubId) {
		this.pubId = pubId;
	}
	
	public void setNextPubId() {
		this.pubId = this.feedId + "." +  DR_Pub.nextKey();
	}

	public DR_Pub setRandomUserName() {
		RandomString r = new RandomString(15);
		this.username = "tmp_" + r.nextString();	
		return this;
	}
	public DR_Pub setRandomPassword() {
		RandomString r = new RandomString(15);
		this.userpwd = r.nextString();
		return this;
	}

	public static String nextKey() {
		RandomString ri = new RandomString(5);
		return ri.nextString();
		
	}
	
}
