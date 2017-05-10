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

import javax.jws.WebParam;
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
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;



















import org.openecomp.dmaapbc.authentication.AuthenticationErrorException;
import org.openecomp.dmaapbc.logging.BaseLoggingClass;
import org.openecomp.dmaapbc.logging.DmaapbcLogMessageEnum;
import org.openecomp.dmaapbc.model.ApiError;
import org.openecomp.dmaapbc.model.DR_Pub;
import org.openecomp.dmaapbc.model.DR_Sub;
import org.openecomp.dmaapbc.model.Feed;
import org.openecomp.dmaapbc.model.Topic;
import org.openecomp.dmaapbc.service.ApiService;
import org.openecomp.dmaapbc.service.FeedService;


@Path("/feeds")
@Api( value= "Feeds", description = "Endpoint for a Data Router Feed" )
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authorization
public class FeedResource extends BaseLoggingClass {
	
	@GET
	@ApiOperation( value = "return Feed details", 
	notes = "Returns array of  `Feed` objects.", 
	response = Feed.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 200, message = "Success", response = DR_Pub.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	public Response getFeeds() {

		ApiService resp = new ApiService();

		FeedService feedService = new FeedService();
		List<Feed> nfeeds =  feedService.getAllFeeds();
		GenericEntity<List<Feed>> list = new GenericEntity<List<Feed>>(nfeeds) {
        };
        return resp.success(list);
	}
	

	
	@POST
	@ApiOperation( value = "return Feed details", 
	notes = "Create a of  `Feed` object.", 
	response = Feed.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 200, message = "Success", response = DR_Pub.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	public Response addFeed( 
			@WebParam(name = "feed") Feed feed 
			) {

		ApiService resp = new ApiService();

		try {
			resp.required( "feedName", feed.getFeedName(), "");
			resp.required( "feedVersion", feed.getFeedVersion(), "");
			resp.required( "owner", feed.getOwner(), "" );
			resp.required( "asprClassification", feed.getAsprClassification(), "" );
		} catch ( RequiredFieldException rfe ) {
			logger.debug( resp.toString() );
			return resp.error();	
		}
		
		FeedService feedService = new FeedService();
		Feed nfeed =  feedService.addFeed( feed, resp.getErr() );
		if ( nfeed != null ) {
			return resp.success(nfeed);
		} else {
			logger.error( "Unable to create: " + feed.getFeedName() + ":" + feed.getFeedVersion());

			return resp.error();			
		}
	}
	
	@PUT
	@ApiOperation( value = "return Feed details", 
	notes = "Update a  `Feed` object, specified by id.", 
	response = Feed.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 200, message = "Success", response = DR_Pub.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	@Path("/{id}")
	public Response updateFeed( 
			@PathParam("id") String id,
			@WebParam(name = "feed") Feed feed 
			) {

		FeedService feedService = new FeedService();
		ApiService resp = new ApiService();

		try {
			resp.required( "feedId", id, "");
		} catch ( RequiredFieldException rfe ) {
			logger.debug( resp.toString() );
			return resp.error();	
		}

		Feed nfeed = feedService.getFeed( id, resp.getErr() );
		if ( nfeed == null ) {
			return resp.notFound();						
		}
	
		//  we assume there is no updates allowed for pubs and subs objects via this api...		
		// need to update any fields supported by PUT but preserve original field values. 
		nfeed.setSuspended(feed.isSuspended());
		nfeed.setFeedDescription(feed.getFeedDescription());
		nfeed.setFormatUuid(feed.getFormatUuid());
		
		nfeed =  feedService.updateFeed(nfeed, resp.getErr());
		if ( nfeed != null ) {
			return resp.success(nfeed);
		} else {
			logger.info( "Unable to update: " + feed.getFeedName() + ":" + feed.getFeedVersion());

			return resp.error();	
		}
	}
	
	@DELETE
	@ApiOperation( value = "return Feed details", 
	notes = "Delete a  `Feed` object, specified by id.", 
	response = Feed.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 204, message = "Success", response = DR_Pub.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	@Path("/{id}")
	public Response deleteFeed( 
			@PathParam("id") String id
			){
		ApiService resp = new ApiService();

		logger.debug( "Entry: DELETE  " + id);
		FeedService feedService = new FeedService();
		feedService.removeFeed(id);
		return resp.success(Status.NO_CONTENT.getStatusCode(), null);
	}

	@GET
	@ApiOperation( value = "return Feed details", 
	notes = "Retrieve a  `Feed` object, specified by id.", 
	response = Feed.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 200, message = "Success", response = DR_Pub.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	@Path("/{id}")
	public Response getFeed( 
			@PathParam("id") String id
			) {
		ApiService resp = new ApiService();

		FeedService feedService = new FeedService();
		Feed nfeed =  feedService.getFeed( id, resp.getErr() );
		if ( nfeed == null ) {
			resp.setCode(Status.NOT_FOUND.getStatusCode());
			return resp.error();
		}
		return resp.success(nfeed);
	}
}
