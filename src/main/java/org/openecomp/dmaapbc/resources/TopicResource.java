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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.openecomp.dmaapbc.authentication.AuthenticationErrorException;
import org.openecomp.dmaapbc.model.ApiError;
import org.openecomp.dmaapbc.model.Topic;
import org.openecomp.dmaapbc.service.ApiService;
import org.openecomp.dmaapbc.service.TopicService;

@Path("/topics")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TopicResource {

	static final Logger logger = Logger.getLogger(TopicResource.class);

	TopicService mr_topicService = new TopicService();
		
	@GET
	public List<Topic> getTopics(@Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth) {
		logger.info("Entry: /GET" );
		ApiService check = new ApiService();
		try {
			check.checkAuthorization( basicAuth, uriInfo.getPath(), "GET");
		} catch ( AuthenticationErrorException ae ) {
			return null; // check.unauthorized();
		} catch ( Exception e ) {
			logger.error( "Unexpected exception " + e );
			return null; //check.unavailable(); 
		}
		return mr_topicService.getAllTopics();
	}
		
	@POST
	public Response  addTopic( Topic topic, @Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth ) {
		logger.info("Entry: /POST" );
		ApiService check = new ApiService();
		try {
			check.checkAuthorization( basicAuth, uriInfo.getPath(), "POST");
		} catch ( AuthenticationErrorException ae ) {
			return check.unauthorized();
		} catch ( Exception e ) {
			logger.error( "Unexpected exception " + e );
			return check.unavailable(); 
		}
		try {
			check.required( "topicName", topic.getTopicName(), "^\\S+$" );  //no white space allowed in topicName
			check.required( "topicDescription", topic.getTopicDescription(), "" );
			check.required( "owner", topic.getOwner(), "" );
		} catch( RequiredFieldException rfe ) {
			return Response.status(check.getErr().getCode())
					.entity( check.getErr() )
					.build();
		}
		//String fqtn = Topic.genFqtn(topic.getTopicName());
		ApiError err = check.getErr();

		topic.setLastMod();
		
		Topic mrc =  mr_topicService.addTopic(topic, err);
		if ( mrc != null && mrc.isStatusValid() ) {
			return Response.status(Status.CREATED)
					.entity(mrc)
					.build();
		}
		return Response.status(err.getCode())
				.entity(err)
				.build();

	}
	
	@PUT
	@Path("/{topicId}")
	public Response updateTopic( @PathParam("topicId") String topicId, Topic topic, @Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth ) {
		logger.info("Entry: /PUT " + topic );
		ApiError err = new ApiError();
		err.setCode(Status.BAD_REQUEST.getStatusCode());
		err.setMessage( "Method /PUT not supported for /topics");
		
		return Response.status(err.getCode())
				.entity( err )
				.build();
		//return mr_topicService.updateTopic(topic);
	}
		
	@DELETE
	@Path("/{topicId}")
	public Response deleteTopic( @PathParam("topicId") String id, @Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth ){
		logger.info("Entry: /DELETE " + id );
		ApiService check = new ApiService();
		try {
			check.checkAuthorization( basicAuth, uriInfo.getPathSegments().get(0).getPath(), "DELETE");
		} catch ( AuthenticationErrorException ae ) {
			return check.unauthorized();
		} catch ( Exception e ) {
			logger.error( "Unexpected exception " + e );
			return check.unavailable(); 
		}
		try {
			check.required( "fqtn", id, "" );
		} catch( RequiredFieldException rfe ) {
			return Response.status(check.getErr().getCode())
					.entity( check.getErr() )
					.build();
		}
		
		Topic topic = mr_topicService.removeTopic(id, check.getErr());
		if ( check.getErr().is2xx()) {
			return Response.status(Status.NO_CONTENT.getStatusCode())
						.build();
		} 
		return Response.status(check.getErr().getCode())
				.entity( check.getErr() )
				.build();
	}
	

	@GET
	@Path("/{topicId}")
	public Response getTopic( @PathParam("topicId") String id , @Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth) {
		logger.info("Entry: /GET " + id);
		ApiService check = new ApiService();
		try {
			check.checkAuthorization( basicAuth, uriInfo.getPathSegments().get(0).getPath(), "GET");
		} catch ( AuthenticationErrorException ae ) {
			return check.unauthorized();
		} catch ( Exception e ) {
			logger.error( "Unexpected exception " + e );
			return check.unavailable(); 
		}
		try {
			check.required( "topicName", id, "^\\S+$" );  //no white space allowed in topicName
		} catch( RequiredFieldException rfe ) {
			return Response.status(check.getErr().getCode())
					.entity( check.getErr() )
					.build();
		}
		Topic mrc =  mr_topicService.getTopic( id, check.getErr() );
		if ( mrc == null ) {
			return Response.status(check.getErr().getCode())
					.entity(check.getErr())
					.build();
		}
		return Response.ok(mrc)
				.build();
		}
}
