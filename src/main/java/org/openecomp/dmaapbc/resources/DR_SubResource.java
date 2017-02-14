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
import org.openecomp.dmaapbc.model.DR_Sub;
import org.openecomp.dmaapbc.model.Feed;
import org.openecomp.dmaapbc.service.ApiService;
import org.openecomp.dmaapbc.service.DR_SubService;
import org.openecomp.dmaapbc.service.FeedService;


@Path("/dr_subs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DR_SubResource extends ApiResource {
	static final Logger logger = Logger.getLogger(DR_SubResource.class);

		
	@GET
	public List<DR_Sub> getDr_Subs(@Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth) {
		logger.info( "Entry: GET /dr_subs");
		ApiService resp = new ApiService();
		try {
			resp.checkAuthorization( basicAuth, uriInfo.getPath(), "GET");
		} catch ( AuthenticationErrorException ae ) {
			return null; //resp.unauthorized();
		} catch ( Exception e ) {
			logger.error( "Unexpected exception " + e );
			return null; //resp.unavailable(); 
		}
		DR_SubService dr_subService = new DR_SubService();
		return dr_subService.getAllDr_Subs();
	}
		
	@POST
	public Response addDr_Sub( DR_Sub sub,
			@Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth) {
		logger.info( "Entry: POST /dr_subs");
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
			resp.required( "feedId", sub.getFeedId(), "");
			resp.required( "dcaeLocationName", sub.getDcaeLocationName(), "");
	
		} catch ( RequiredFieldException rfe ) {
			logger.debug( resp.toString() );
			return Response.status(Status.BAD_REQUEST)
					.entity( resp.getErr() )
					.build();	
		}
		
		FeedService feeds = new FeedService();
		Feed fnew = feeds.getFeed( sub.getFeedId(), resp.getErr() );
		if ( fnew == null ) {
			logger.warn( "Specified feed " + sub.getFeedId() + " not known to Bus Controller");
			return Response.status( resp.getErr().getCode() )
					.entity( resp.getErr() )
					.build();
		}

		DR_SubService dr_subService = new DR_SubService( fnew.getSubscribeURL());
		ArrayList<DR_Sub> subs = fnew.getSubs();
		logger.info( "num existing subs before = " + subs.size() );
		DR_Sub snew = dr_subService.addDr_Sub(sub, resp.getErr() );
		if ( ! resp.getErr().is2xx() ) {
			return Response.status( resp.getErr().getCode() )
					.entity( resp.getErr() )
					.build();
		}
		subs.add( snew );
		logger.info( "num existing subs after = " + subs.size() );
		
		fnew.setSubs(subs);
		logger.info( "update feed");
		//feeds.updateFeed( fnew, err );			
		
		return Response.status(Status.CREATED)
				.entity(snew)
				.build();

	}
		
	@PUT
	@Path("/{subId}")
	public Response updateDr_Sub( @PathParam("subId") String name, DR_Sub sub,
			@Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth) {
		logger.info( "Entry: PUT /dr_subs");
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
			resp.required( "subId", name, "");
			resp.required( "feedId", sub.getFeedId(), "");
			resp.required( "dcaeLocationName", sub.getDcaeLocationName(), "");
	
		} catch ( RequiredFieldException rfe ) {
			logger.debug( resp.toString() );
			return Response.status(Status.BAD_REQUEST)
					.entity( resp.getErr() )
					.build();	
		}
		DR_SubService dr_subService = new DR_SubService();
		sub.setSubId(name);
		DR_Sub nsub = dr_subService.updateDr_Sub(sub, resp.getErr() );
		if ( nsub != null && nsub.isStatusValid() ) {
			return Response.status(Status.OK)
					.entity(nsub)
					.build();
		}
		return Response.status(resp.getErr().getCode())
				.entity(resp.getErr())
				.build();
	}
		
	@DELETE
	@Path("/{subId}")
	public Response deleteDr_Sub( @PathParam("subId") String id,
			@Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth){
		logger.info( "Entry: DELETE /dr_subs");
		ApiService resp = new ApiService();
		try {
			resp.checkAuthorization( basicAuth, uriInfo.getPathSegments().get(0).getPath(), "DELETE");
		} catch ( AuthenticationErrorException ae ) {
			return resp.unauthorized();
		} catch ( Exception e ) {
			logger.error( "Unexpected exception " + e );
			return resp.unavailable(); 
		}
		
		try {
			resp.required( "subId", id, "");
		} catch ( RequiredFieldException rfe ) {
			logger.debug( resp.toString() );
			return Response.status(Status.BAD_REQUEST.getStatusCode())
					.entity( resp.getErr() )
					.build();	
		}
		DR_SubService dr_subService = new DR_SubService();
		dr_subService.removeDr_Sub(id, resp.getErr() );
		if ( ! resp.getErr().is2xx() ) {
			return Response.status( resp.getErr().getCode() )
					.entity( resp.getErr() )
					.build();
		}
		return Response.status(Status.NO_CONTENT.getStatusCode())
				.build();
	}

	@GET
	@Path("/{subId}")
	public Response get( @PathParam("subId") String id,
			@Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth) {
		logger.info( "Entry: GET /dr_subs");
		ApiService resp = new ApiService();
		try {
			resp.checkAuthorization( basicAuth, uriInfo.getPathSegments().get(0).getPath(), "GET");
		} catch ( AuthenticationErrorException ae ) {
			return resp.unauthorized();
		} catch ( Exception e ) {
			logger.error( "Unexpected exception " + e );
			return resp.unavailable(); 
		}

		try {
			resp.required( "subId", id, "");
		} catch ( RequiredFieldException rfe ) {
			logger.debug( resp.toString() );
			return Response.status(Status.BAD_REQUEST.getStatusCode())
					.entity( resp.getErr() )
					.build();	
		}
		DR_SubService dr_subService = new DR_SubService();
		DR_Sub sub =  dr_subService.getDr_Sub( id, resp.getErr() );
		if ( sub != null && sub.isStatusValid() ) {
			return Response.status(Status.OK.getStatusCode())
					.entity(sub)
					.build();
		}
		return Response.status(resp.getErr().getCode())
				.entity(resp.getErr())
				.build();
	}
}
