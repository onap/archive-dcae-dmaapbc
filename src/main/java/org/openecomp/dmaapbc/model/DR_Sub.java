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

import java.nio.charset.StandardCharsets;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@XmlRootElement
public class DR_Sub extends DmaapObject {
	static final Logger logger = Logger.getLogger(DR_Sub.class);

	private String dcaeLocationName;
	private String username;
	private String userpwd;
	private String feedId;
	private String deliveryURL;
	private String logURL;
	private String subId;
	private boolean use100;
	private boolean suspended;
	private String owner;
	
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public boolean isSuspended() {
		return suspended;
	}

	public void setSuspended(boolean suspended) {
		this.suspended = suspended;
	}



	public boolean isUse100() {
		return use100;
	}

	public void setUse100(boolean use100) {
		this.use100 = use100;
	}

	public DR_Sub() {

	}
	
	public DR_Sub( String dLN,
					String uN,
					String uP,
					String fI,
					String dU,
					String lU,
					boolean u100 ) {
		this.dcaeLocationName = dLN;
		this.username = uN;
		this.userpwd = uP;
		this.feedId = fI;
		this.deliveryURL = dU;
		this.logURL = lU;
		this.use100 = u100;
		this.setStatus( DmaapObject_Status.NEW );
		this.subId = "0";
	}
	
	public DR_Sub ( String json ) {
		logger.info( "DR_Sub:" + json );
		JSONParser parser = new JSONParser();
		JSONObject jsonObj;
		
		try {
			jsonObj = (JSONObject) parser.parse( json );
		} catch ( ParseException pe ) {
            logger.error( "Error parsing provisioning data: " + json );
            this.setStatus( DmaapObject_Status.INVALID );
            return;
        }

		this.setOwner( (String) jsonObj.get("subscriber"));
		this.setSuspended( (boolean) jsonObj.get("suspend"));
		
		JSONObject links = (JSONObject) jsonObj.get("links");
		String url = (String) links.get("feed");
		this.setFeedId( url.substring( url.lastIndexOf('/')+1, url.length() ));
		url = (String) links.get("self");
		this.setSubId( url.substring( url.lastIndexOf('/')+1, url.length() ));
		logger.info( "feedid="+ this.getFeedId() );
		this.setLogURL( (String) links.get("log") );
		
		JSONObject del = (JSONObject) jsonObj.get("delivery");
		this.setDeliveryURL( (String) del.get("url") );	
		this.setUsername( (String) del.get("user"));
		this.setUserpwd( (String) del.get( "password"));
		this.setUse100((boolean) del.get( "use100"));

		this.setStatus( DmaapObject_Status.VALID );

		logger.info( "new DR_Sub returning");
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

	public String getDeliveryURL() {
		return deliveryURL;
	}

	public void setDeliveryURL(String deliveryURL) {
		this.deliveryURL = deliveryURL;
	}

	public String getLogURL() {
		return logURL;
	}

	public void setLogURL(String logURL) {
		this.logURL = logURL;
	}

	public String getSubId() {
		return subId;
	}

	public void setSubId(String subId) {
		this.subId = subId;
	}



	public byte[] getBytes() {
		return toProvJSON().getBytes(StandardCharsets.UTF_8);
	}
	// returns the DR_Sub object in JSON that conforms to DR Prov Server expectations
	public String toProvJSON() {
		
		// in DR 3.0, API v2.1 a new groupid field is added.  We are not using this required field so just set it to 0.
		// we send this regardless of DR Release because older versions of DR seem to safely ignore it
		// and soon those versions won't be around anyway...
		String postJSON = String.format("{\"delivery\": {\"url\": \"%s\", \"user\": \"%s\", \"password\": \"%s\", \"use100\":  \"%s\"}, \"metadataOnly\": false, \"groupid\": \"0\"}", 
				this.getDeliveryURL(), 
				this.getUsername(),
				this.getUserpwd(),
				this.isUse100() );	
		
		logger.info( postJSON );
		return postJSON;
	}
}
