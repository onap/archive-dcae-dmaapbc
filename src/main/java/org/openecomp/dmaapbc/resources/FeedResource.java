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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;



















import org.openecomp.dmaapbc.authentication.AuthenticationErrorException;
import org.openecomp.dmaapbc.model.Feed;
import org.openecomp.dmaapbc.service.ApiService;
import org.openecomp.dmaapbc.service.FeedService;


@Path("/feeds")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FeedResource extends ApiResource {
	static final Logger logger = Logger.getLogger(FeedResource.class);

	
	@GET
	public List<Feed> getFeeds(@Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth) {
		logger.debug( "Entry: GET  " + uriInfo.getPath()  );
		ApiService resp = new ApiService();
		try {
			resp.checkAuthorization( basicAuth, uriInfo.getPath(), "GET");
		} catch ( AuthenticationErrorException ae ) {
			return null; //resp.unauthorized();
		} catch ( Exception e ) {
			logger.error( "Unexpected exception " + e );
			return null; //resp.unavailable(); 
		}
		FeedService feedService = new FeedService();
		List<Feed> nfeeds =  feedService.getAllFeeds();
// tried this: http://www.adam-bien.com/roller/abien/entry/jax_rs_returning_a_list
// but still didn't seem to work...
//		GenericEntity<List<Feed>> list = new GenericEntity<List<Feed>>(nfeeds){};
//		return Response.status(Status.OK)
//				.entity( list )
//				.build();
		return nfeeds;
	}
	

	
	@POST
	public Response addFeed( @WebParam(name = "feed") Feed feed , @Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth  ) {
		logger.debug( "Entry: POST  " + uriInfo.getPath());

		ApiService resp = new ApiService();
		try {
			resp.checkAuthorization( basicAuth, uriInfo.getPath(), "POST");
		} catch ( AuthenticationErrorException ae ) {
			return resp.unauthorized();
		} catch ( Exception e ) {
			logger.error( "Unexpected exception " + e );
			return resp.unavailable(); 
		}
		try {
			resp.required( "feedName", feed.getFeedName(), "");
			resp.required( "feedVersion", feed.getFeedVersion(), "");
			resp.required( "owner", feed.getOwner(), "" );
			resp.required( "asprClassification", feed.getAsprClassification(), "" );
		} catch ( RequiredFieldException rfe ) {
			logger.debug( resp.toString() );
			return Response.status(Status.BAD_REQUEST).entity( resp ).build();	
		}
		
		FeedService feedService = new FeedService();
		Feed nfeed =  feedService.addFeed( feed, resp.getErr() );
		if ( nfeed != null ) {
			return Response.status(Status.OK)
					.entity(nfeed)
					.build();
		} else {
			logger.error( "Unable to create: " + feed.getFeedName() + ":" + feed.getFeedVersion());

			return Response.status(resp.getErr().getCode())
					.entity( resp )
					.build();		
		}
	}
	
	@PUT
	@Path("/{id}")
	public Response updateFeed( @PathParam("id") String id, Feed feed, @Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth) {
		logger.debug( "Entry: PUT  " + uriInfo.getPath());

		FeedService feedService = new FeedService();
		ApiService resp = new ApiService();
		try {
			resp.checkAuthorization( basicAuth, uriInfo.getPathSegments().get(0).getPath(), "PUT");
		} catch ( AuthenticationErrorException ae ) {
			return resp.unauthorized();
		} catch ( Exception e ) {
			logger.error( "Unexpected exception " + e );
			return resp.unavailable(); 
		}
		try {
			resp.required( "feedId", id, "");
		} catch ( RequiredFieldException rfe ) {
			logger.debug( resp.toString() );
			return Response.status(Status.BAD_REQUEST.getStatusCode()).entity( resp ).build();	
		}

		Feed nfeed = feedService.getFeed( id, resp.getErr() );
		if ( nfeed == null ) {
			return Response.status(resp.getErr().getCode())
					.entity( resp.getErr() )
					.build();					
		}
	
		//  we assume there is no updates allowed for pubs and subs objects via this api...		
		// need to update any fields supported by PUT but preserve original field values. 
		nfeed.setSuspended(feed.isSuspended());
		nfeed.setFeedDescription(feed.getFeedDescription());
		nfeed.setFormatUuid(feed.getFormatUuid());
		
		nfeed =  feedService.updateFeed(nfeed, resp.getErr());
		if ( nfeed != null ) {
			return Response.status(Status.OK)
					.entity(nfeed)
					.build();
		} else {
			logger.info( "Unable to update: " + feed.getFeedName() + ":" + feed.getFeedVersion());

			return Response.status(resp.getErr().getCode())
					.entity( resp.getErr() )
					.build();		
		}
	}
	
	@DELETE
	@Path("/{id}")
	public Response deleteFeed( @PathParam("id") String id, @Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth ){
		ApiService resp = new ApiService();
		try {
			resp.checkAuthorization( basicAuth, uriInfo.getPathSegments().get(0).getPath(), "DELETE");
		} catch ( AuthenticationErrorException ae ) {
			return resp.unauthorized();
		} catch ( Exception e ) {
			logger.error( "Unexpected exception " + e );
			return resp.unavailable(); 
		}
		logger.debug( "Entry: DELETE  " + uriInfo.getPath());
		FeedService feedService = new FeedService();
		feedService.removeFeed(id);
		return Response.status(Status.NO_CONTENT)
				.build();
	}

	@GET
	@Path("/{id}")
	public Response getFeed( @PathParam("id") String id, @Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth ) {
		logger.debug( "Entry: GET  " + uriInfo.getPath());
		ApiService resp = new ApiService();
		try {
			resp.checkAuthorization( basicAuth, uriInfo.getPathSegments().get(0).getPath(), "GET");
		} catch ( AuthenticationErrorException ae ) {
			return resp.unauthorized();
		} catch ( Exception e ) {
			logger.error( "Unexpected exception " + e );
			return resp.unavailable(); 
		}
		FeedService feedService = new FeedService();
		Feed nfeed =  feedService.getFeed( id, resp.getErr() );
		if ( nfeed == null ) {
			return Response.status(Status.NOT_FOUND).entity( resp.getErr() ).build();
		}
		return Response.status(Status.OK)
				.entity(nfeed)
				.build();
	}
}
