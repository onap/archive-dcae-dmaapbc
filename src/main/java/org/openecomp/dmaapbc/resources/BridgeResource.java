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

package org.openecomp.dmaapbc.resources;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.openecomp.dmaapbc.authentication.AuthenticationErrorException;
import org.openecomp.dmaapbc.model.BrTopic;
import org.openecomp.dmaapbc.model.MirrorMaker;
import org.openecomp.dmaapbc.service.ApiService;
import org.openecomp.dmaapbc.service.MirrorMakerService;

@Path("/bridge")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BridgeResource {
	
	static final Logger logger = Logger.getLogger(BridgeResource.class);
	
	private MirrorMakerService mmService = new MirrorMakerService();

	@GET
	public Response	getBridgedTopics(@QueryParam("source") String source,
						   			@QueryParam("target") String target,
						   		 @Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth){
		ApiService check = new ApiService();
		BrTopic brTopic = new BrTopic();
		
		try {
			check.checkAuthorization( basicAuth, uriInfo.getPath(), "GET");
		} catch ( AuthenticationErrorException ae ) {
			return check.unauthorized();
		} catch ( Exception e ) {
			logger.error( "Unexpected exception " + e );
			return check.unavailable(); 
		}

		logger.info( "getBridgeTopics():" + " source=" + source + ", target=" + target);
//		System.out.println("getBridgedTopics() " + "source=" + source + ", target=" + target );
		if (source != null && target != null) {		// get topics between 2 bridged locations
			brTopic.setBrSource(source);
			brTopic.setBrTarget(target);
			MirrorMaker mm = mmService.getMirrorMaker(source, target);
			if ( mm != null ) {
				brTopic.setTopicCount( mm.getTopicCount() );
			} 

			logger.info( "topicCount [2 locations]: " + brTopic.getTopicCount() );
		}
		else if (source == null && target == null ) {
			List<String> mmList = mmService.getAllMirrorMakers();
			brTopic.setBrSource("all");
			brTopic.setBrTarget("all");
			int totCnt = 0;
			for( String key: mmList ) {
				int mCnt = 0;
				MirrorMaker mm = mmService.getMirrorMaker(key);
				if ( mm != null ) {
					mCnt = mm.getTopicCount();
				}
				logger.info( "Count for "+ key + ": " + mCnt);
				totCnt += mCnt;
			}
			
			logger.info( "topicCount [all locations]: " + totCnt );
			brTopic.setTopicCount(totCnt);
//			System.out.println("BridgeResource() d.getBrSource()=" + d.getBrSource());
		}
		else {
//			System.out.println("A source or target Parameter is missing");
//			return Response.serverError().build();
			logger.error( "source or target is missing");
			return Response.status(Status.BAD_REQUEST)
					.entity( "Either 2 locations or no location must be provided")
					.build();
		}
		return Response.ok(brTopic).
				build();
	}
}
