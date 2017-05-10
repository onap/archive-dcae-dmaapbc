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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.openecomp.dmaapbc.logging.BaseLoggingClass;
import org.openecomp.dmaapbc.model.ApiError;
import org.openecomp.dmaapbc.model.DR_Pub;
import org.openecomp.dmaapbc.model.ReplicationType;
import org.openecomp.dmaapbc.model.Topic;
import org.openecomp.dmaapbc.service.ApiService;
import org.openecomp.dmaapbc.service.TopicService;

@Path("/topics")
@Api( value= "topics", description = "Endpoint for retreiving MR Topics" )
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authorization
public class TopicResource extends BaseLoggingClass {

	TopicService mr_topicService = new TopicService();
		
	@GET
	@ApiOperation( value = "return Topic details", 
	notes = "Returns array of  `Topic` objects.", 
	response = Topic.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 200, message = "Success", response = DR_Pub.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	public Response getTopics() {

		ApiService check = new ApiService();

		List<Topic> allTopics = mr_topicService.getAllTopics();
		
		GenericEntity<List<Topic>> list = new GenericEntity<List<Topic>>(allTopics) {
		        };
		return check.success(list);
		
	}
		
	@POST
	@ApiOperation( value = "return Topic details", 
	notes = "Create  `Topic` object.", 
	response = Topic.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 200, message = "Success", response = DR_Pub.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	public Response  addTopic( 
			Topic topic
			) {
		logger.info( "addTopic request: " + String.valueOf(topic) );
		ApiService check = new ApiService();

		try {
			check.required( "topicName", topic.getTopicName(), "^\\S+$" );  //no white space allowed in topicName
			check.required( "topicDescription", topic.getTopicDescription(), "" );
			check.required( "owner", topic.getOwner(), "" );
		} catch( RequiredFieldException rfe ) {
			return check.error();
		}
		
		//String repReq = topic.getReplicationRequest();
		ReplicationType t = topic.getReplicationCase();
		if ( t == null || t == ReplicationType.REPLICATION_NOT_SPECIFIED ) {
			topic.setReplicationCase( mr_topicService.reviewTopic(topic));
		} 
		
		topic.setLastMod();
		
		Topic mrc =  mr_topicService.addTopic(topic, check.getErr());
		if ( mrc != null && mrc.isStatusValid() ) {
			return check.success(Status.CREATED.getStatusCode(), mrc);
		}
		return check.error();
	}
	
	@PUT
	@ApiOperation( value = "return Topic details", 
	notes = "Update a  `Topic` object, identified by topicId", 
	response = Topic.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 200, message = "Success", response = DR_Pub.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	@Path("/{topicId}")
	public Response updateTopic( 
			@PathParam("topicId") String topicId
			) {
		ApiService check = new ApiService();

		check.setCode(Status.BAD_REQUEST.getStatusCode());
		check.setMessage( "Method /PUT not supported for /topics");
		
		return check.error();
	}
		
	@DELETE
	@ApiOperation( value = "return Topic details", 
	notes = "Delete a  `Topic` object, identified by topicId", 
	response = Topic.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 204, message = "Success", response = DR_Pub.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	@Path("/{topicId}")
	public Response deleteTopic( 
			@PathParam("topicId") String id
			){
		ApiService check = new ApiService();

		try {
			check.required( "fqtn", id, "" );
		} catch( RequiredFieldException rfe ) {
			return check.error();
		}
		
		mr_topicService.removeTopic(id, check.getErr());
		if ( check.getErr().is2xx()) {
			return check.success(Status.NO_CONTENT.getStatusCode(), null);
		} 
		return check.error();
	}
	

	@GET
	@ApiOperation( value = "return Topic details", 
	notes = "Retrieve a  `Topic` object, identified by topicId", 
	response = Topic.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 200, message = "Success", response = DR_Pub.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	@Path("/{topicId}")
	public Response getTopic( 
			@PathParam("topicId") String id
			) {
		logger.info("Entry: /GET " + id);
		ApiService check = new ApiService();

		try {
			check.required( "topicName", id, "^\\S+$" );  //no white space allowed in topicName
		} catch( RequiredFieldException rfe ) {
			return check.error();
		}
		Topic mrc =  mr_topicService.getTopic( id, check.getErr() );
		if ( mrc == null ) {
			return check.error();
		}
		return check.success(mrc);
		}
}
