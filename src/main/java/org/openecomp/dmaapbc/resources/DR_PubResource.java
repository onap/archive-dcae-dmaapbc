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

import java.util.ArrayList;
import java.util.Iterator;
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
import org.openecomp.dmaapbc.model.DR_Pub;
import org.openecomp.dmaapbc.model.Feed;
import org.openecomp.dmaapbc.service.ApiService;
import org.openecomp.dmaapbc.service.DR_PubService;
import org.openecomp.dmaapbc.service.FeedService;


@Path("/dr_pubs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DR_PubResource extends ApiResource {
	static final Logger logger = Logger.getLogger(DR_PubResource.class);
	DR_PubService dr_pubService = new DR_PubService();
	
	@GET
	public  List<DR_Pub> getDr_Pubs(@Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth) {
		ApiService resp = new ApiService();
		try {
			resp.checkAuthorization( basicAuth, uriInfo.getPath(), "GET");
		} catch ( AuthenticationErrorException ae ) {
			return null; //resp.unauthorized();
		} catch ( Exception e ) {
			logger.error( "Unexpected exception " + e );
			return null; //resp.unavailable(); 
		}
		logger.info( "Entry: GET /dr_pubs");
		return dr_pubService.getAllDr_Pubs();
	}
	
	@POST
	public Response addDr_Pub( DR_Pub pub,
			@Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth) {
		ApiService resp = new ApiService();
		try {
			resp.checkAuthorization( basicAuth, uriInfo.getPath(), "POST");
		} catch ( AuthenticationErrorException ae ) {
			return resp.unauthorized();
		} catch ( Exception e ) {
			logger.error( "Unexpected exception " + e );
			return resp.unavailable(); 
		}
		logger.info( "Entry: POST /dr_pubs");
		ApiError err = new ApiError();
		try {
			checkRequired( "feedId", pub.getFeedId(), "", err);
			checkRequired( "dcaeLocationName", pub.getDcaeLocationName(), "", err);
		} catch ( RequiredFieldException rfe ) {
			logger.debug( err.toString() );
			return Response.status(Status.BAD_REQUEST).entity( err ).build();	
		}

		FeedService feeds = new FeedService();
		Feed fnew = feeds.getFeed( pub.getFeedId(), err);
		if ( fnew == null ) {
			logger.info( "Specified feed " + pub.getFeedId() + " not known to Bus Controller");	
			return Response.status(err.getCode())
					.entity( err )
					.build();	
		}

		ArrayList<DR_Pub> pubs = fnew.getPubs();
		logger.info( "num existing pubs before = " + pubs.size() );
/*
		DR_Pub pnew = new DR_Pub( pub.getDcaeLocationName());
		pnew.setFeedId(pub.getFeedId());
		pnew.setPubId(pub.getPubId());
		String tmp = pub.getUsername();
		if ( tmp != null ) {
			pub.setUsername(tmp);
		}
		tmp = pub.getUserpwd();
		if ( tmp != null ) {
			pub.setUserpwd(tmp);
		}
		pnew.setNextPubId();
*/
		
		
		logger.info( "update feed");
		pub.setNextPubId();
		if ( pub.getUsername() == null ) {
			pub.setRandomUserName();
		}
		if ( pub.getUserpwd() == null ) {
			pub.setRandomPassword();
		}
		pubs.add( pub );
		fnew.setPubs(pubs);
		fnew = feeds.updateFeed( fnew, err );	
		
		if ( ! err.is2xx()) {	
			return Response.status(err.getCode())
					.entity( err )
					.build();			
		}
		pubs = fnew.getPubs();
		logger.info( "num existing pubs after = " + pubs.size() );
		
		DR_Pub pnew = dr_pubService.getDr_Pub(pub.getPubId(), err);
		return Response.status(Status.CREATED.getStatusCode())
				.entity(pnew)
				.build();
	}
	
	@PUT
	@Path("/{pubId}")
	public Response updateDr_Pub( @PathParam("pubId") String name, DR_Pub pub,
			@Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth) {
		ApiService resp = new ApiService();
		try {
			resp.checkAuthorization( basicAuth, uriInfo.getPathSegments().get(0).getPath(), "PUT");
		} catch ( AuthenticationErrorException ae ) {
			return resp.unauthorized();
		} catch ( Exception e ) {
			logger.error( "Unexpected exception " + e );
			return resp.unavailable(); 
		}
		logger.info( "Entry: PUT /dr_pubs");
		pub.setPubId(name);
		DR_Pub res = dr_pubService.updateDr_Pub(pub);
		return Response.ok()
				.entity(res)
				.build();
	}
	
	@DELETE
	@Path("/{pubId}")
	public Response deleteDr_Pub( @PathParam("pubId") String id,
			@Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth){
		logger.info( "Entry: DELETE /dr_pubs");
		ApiService resp = new ApiService();
		try {
			resp.checkAuthorization( basicAuth, uriInfo.getPathSegments().get(0).getPath(), "DELETE");
		} catch ( AuthenticationErrorException ae ) {
			return resp.unauthorized();
		} catch ( Exception e ) {
			logger.error( "Unexpected exception " + e );
			return resp.unavailable(); 
		}
		ApiError err = new ApiError();
		try {
			checkRequired( "feedId", id, "", err);
		} catch ( RequiredFieldException rfe ) {
			logger.debug( err.toString() );
			return Response.status(Status.BAD_REQUEST).entity( err ).build();	
		}

		DR_Pub pub =  dr_pubService.getDr_Pub( id, err );
		if ( ! err.is2xx()) {	
			return Response.status(err.getCode())
					.entity( err )
					.build();			
		}
		FeedService feeds = new FeedService();
		Feed fnew = feeds.getFeed( pub.getFeedId(), err);
		if ( fnew == null ) {
			logger.info( "Specified feed " + pub.getFeedId() + " not known to Bus Controller");	
			return Response.status(err.getCode())
					.entity( err )
					.build();	
		}
		ArrayList<DR_Pub> pubs = fnew.getPubs();
		if ( pubs.size() == 1 ) {
			err.setCode(Status.BAD_REQUEST.getStatusCode());
			err.setMessage( "Can't delete the last publisher of a feed");
			return Response.status(err.getCode())
					.entity( err )
					.build();	
		}
		Iterator<DR_Pub> i = pubs.iterator();
		while( i.hasNext() ) {
			DR_Pub listItem = i.next();
			if ( listItem.getPubId().equals(id)) {
				pubs.remove( listItem );
			}
		}
		fnew.setPubs(pubs);
		fnew = feeds.updateFeed( fnew, err );
		if ( ! err.is2xx()) {	
			return Response.status(err.getCode())
					.entity( err )
					.build();			
		}
		
		dr_pubService.removeDr_Pub(id, err);
		if ( ! err.is2xx()) {	
			return Response.status(err.getCode())
					.entity( err )
					.build();			
		}
		return Response.status(Status.NO_CONTENT.getStatusCode())
				.build();
	}

	@GET
	@Path("/{pubId}")
	public Response get( @PathParam("pubId") String id,
			@Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth) {
		ApiService resp = new ApiService();
		try {
			resp.checkAuthorization( basicAuth, uriInfo.getPathSegments().get(0).getPath(), "GET");
		} catch ( AuthenticationErrorException ae ) {
			return resp.unauthorized();
		} catch ( Exception e ) {
			logger.error( "Unexpected exception " + e );
			return resp.unavailable(); 
		}
		ApiError err = new ApiError();
		try {
			checkRequired( "feedId", id, "", err);
		} catch ( RequiredFieldException rfe ) {
			logger.debug( err.toString() );
			return Response.status(Status.BAD_REQUEST).entity( err ).build();	
		}
		logger.info( "Entry: GET /dr_pubs");
		DR_Pub pub =  dr_pubService.getDr_Pub( id, err );
		if ( ! err.is2xx()) {	
			return Response.status(err.getCode())
					.entity( err )
					.build();			
		}
		return Response.status(Status.OK.getStatusCode())
				.entity(pub)
				.build();
	}
}
