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
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;


import org.json.simple.*;
import org.json.simple.parser.*;
import org.openecomp.dmaapbc.service.DmaapService;
import org.openecomp.dmaapbc.util.RandomString;

@XmlRootElement
public class Feed extends DmaapObject {
	static final Logger logger = Logger.getLogger(Feed.class);

		
		private String feedId;

		private String feedName;
		private String feedVersion;
		private String feedDescription;
		private String owner;
		private String asprClassification;
		private String publishURL;
		private String subscribeURL;
		private	boolean	suspended;
		private String logURL;
		private String formatUuid;

		private	ArrayList<DR_Pub> pubs;
		private ArrayList<DR_Sub> subs;	

		

		public boolean isSuspended() {
			return suspended;
		}

		public void setSuspended(boolean suspended) {
			this.suspended = suspended;
		}

		public String getSubscribeURL() {
			return subscribeURL;
		}

		public void setSubscribeURL(String subscribeURL) {
			this.subscribeURL = subscribeURL;
		}


		
		public Feed() {
			this.pubs = new ArrayList<DR_Pub>();
			this.subs = new ArrayList<DR_Sub>();
			this.setStatus( DmaapObject_Status.EMPTY );
			
		}
		
		public	Feed( String name,
					String version,
					String description,
					String owner,
					String aspr
					 ) {
			this.feedName = name;
			this.feedVersion = version;
			this.feedDescription = description;
			this.owner = owner;
			this.asprClassification = aspr;
			this.pubs = new ArrayList<DR_Pub>();
			this.subs = new ArrayList<DR_Sub>();
			this.setStatus( DmaapObject_Status.NEW );
			
		}
		
		// expects a String in JSON format, with known fields to populate Feed object
		public Feed ( String json ) {
			JSONParser parser = new JSONParser();
			JSONObject jsonObj;
			
			try {
				jsonObj = (JSONObject) parser.parse( json );
			} catch ( ParseException pe ) {
	            logger.error( "Error parsing provisioning data: " + json );
	            this.setStatus( DmaapObject_Status.INVALID );
	            return;
	        }
			this.setFeedName( (String) jsonObj.get("name"));


			this.setFeedVersion( (String) jsonObj.get("version"));
			this.setFeedDescription( (String) jsonObj.get("description"));
			this.setOwner( (String) jsonObj.get("publisher"));

			this.setSuspended( (boolean) jsonObj.get("suspend"));
			JSONObject links = (JSONObject) jsonObj.get("links");
			String url = (String) links.get("publish");
			this.setPublishURL( url );
			this.setFeedId( url.substring( url.lastIndexOf('/')+1, url.length() ));
			logger.info( "feedid="+ this.getFeedId() );
			this.setSubscribeURL( (String) links.get("subscribe") );					
			this.setLogURL( (String) links.get("log") );
			JSONObject auth = (JSONObject) jsonObj.get("authorization");
			this.setAsprClassification( (String) auth.get("classification"));
			JSONArray pubs = (JSONArray) auth.get( "endpoint_ids");
			int i;
			ArrayList<DR_Pub> dr_pub = new ArrayList<DR_Pub>();
			this.subs = new ArrayList<DR_Sub>();

			for( i = 0; i < pubs.size(); i++ ) {
				JSONObject entry = (JSONObject) pubs.get(i);
				dr_pub.add(  new DR_Pub( "someLocation", 
									(String) entry.get("id"),
									(String) entry.get("password"),
									this.getFeedId(),
									this.getFeedId() + "." +  DR_Pub.nextKey() ));
			
			}
			this.setPubs( dr_pub );
	
			this.setStatus( DmaapObject_Status.VALID );

		}

		public String getFeedId() {
			return feedId;
		}

		public void setFeedId(String feedId) {
			this.feedId = feedId;
		}

		public String getFeedName() {
			return feedName;
		}

		public void setFeedName(String feedName) {
			this.feedName = feedName;
		}

		public String getFeedVersion() {
			return feedVersion;
		}

		public void setFeedVersion(String feedVersion) {
			this.feedVersion = feedVersion;
		}

		public String getFeedDescription() {
			return feedDescription;
		}

		public void setFeedDescription(String feedDescription) {
			this.feedDescription = feedDescription;
		}

		public String getOwner() {
			return owner;
		}

		public void setOwner(String owner) {
			this.owner = owner;
		}

		public String getAsprClassification() {
			return asprClassification;
		}

		public void setAsprClassification(String asprClassification) {
			this.asprClassification = asprClassification;
		}

		public String getPublishURL() {
			return publishURL;
		}

		public void setPublishURL(String publishURL) {
			this.publishURL = publishURL;
		}

		public String getLogURL() {
			return logURL;
		}

		public void setLogURL(String logURL) {
			this.logURL = logURL;
		}


		
		public String getFormatUuid() {
			return formatUuid;
		}

		public void setFormatUuid(String formatUuid) {
			this.formatUuid = formatUuid;
		}

		// returns the Feed object in JSON that conforms to DR Prov Server expectations
		public String toProvJSON() {

			ArrayList<DR_Pub> pubs = this.getPubs();
			String postJSON = String.format("{\"name\": \"%s\", \"version\": \"%s\", \"description\": \"%s\", \"suspend\": %s, \"authorization\": { \"classification\": \"%s\", ",  
					this.getFeedName(), 
					this.getFeedVersion(),
					this.getFeedDescription(),
					this.isSuspended() ,
					this.getAsprClassification()
					);
			int i;
			postJSON += "\"endpoint_addrs\": [],\"endpoint_ids\": [";
			String comma = "";
			for( i = 0 ; i < pubs.size(); i++) {
				postJSON +=	String.format("	%s{\"id\": \"%s\",\"password\": \"%s\"}", 
						comma,
						pubs.get(i).getUsername(),
						pubs.get(i).getUserpwd()
						) ;
				comma = ",";
			}
			postJSON += "]}}";
			
			logger.info( "postJSON=" + postJSON);		
			return postJSON;
		}
		
		public ArrayList<DR_Pub> getPubs() {
			return pubs;
		}

		public void setPubs( ArrayList<DR_Pub> pubs) {
			this.pubs = pubs;
		}

		public ArrayList<DR_Sub> getSubs() {
			return subs;
		}

		public void setSubs( ArrayList<DR_Sub> subs) {
			this.subs = subs;
		}

		public byte[] getBytes() {
			return toProvJSON().getBytes(StandardCharsets.UTF_8);
		}
		
		public static String getSubProvURL( String feedId ) {
			String ret = new String();
			ret = new DmaapService().getDmaap().getDrProvUrl() + "/subscribe/" + feedId ;
			return ret;
		}

}
