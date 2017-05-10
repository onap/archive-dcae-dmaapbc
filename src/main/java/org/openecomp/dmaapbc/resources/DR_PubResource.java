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

import java.util.ArrayList;
import java.util.Iterator;
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
import org.openecomp.dmaapbc.model.Feed;
import org.openecomp.dmaapbc.service.ApiService;
import org.openecomp.dmaapbc.service.DR_PubService;
import org.openecomp.dmaapbc.service.FeedService;


@Path("/dr_pubs")
@Api( value= "dr_pubs", description = "Endpoint for a Data Router client that implements a Publisher" )
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authorization
public class DR_PubResource extends BaseLoggingClass {

	DR_PubService dr_pubService = new DR_PubService();
	
	@GET
	@ApiOperation( value = "return DR_Pub details", 
	notes = "Returns array of  `DR_Pub` objects.  Add filter for feedId.", 
	response = DR_Pub.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 200, message = "Success", response = DR_Pub.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	public  Response getDr_Pubs() {
		ApiService resp = new ApiService();

		logger.info( "Entry: GET /dr_pubs");
		List<DR_Pub> pubs = dr_pubService.getAllDr_Pubs();

		GenericEntity<List<DR_Pub>> list = new GenericEntity<List<DR_Pub>>(pubs) {
        };
        return resp.success(list);
	}
	
	@POST
	@ApiOperation( value = "return DR_Pub details", 
	notes = "create a DR Publisher in the specified environment.", 
	response = DR_Pub.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 200, message = "Success", response = DR_Pub.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	public Response addDr_Pub( 
			DR_Pub pub
			) {
		ApiService resp = new ApiService();

		logger.info( "Entry: POST /dr_pubs");
		ApiError err = new ApiError();
		try {
			resp.required( "feedId", pub.getFeedId(), "");
			resp.required( "dcaeLocationName", pub.getDcaeLocationName(), "");
		} catch ( RequiredFieldException rfe ) {
			logger.debug( err.toString() );
			return resp.error();	
		}

		FeedService feeds = new FeedService();
		Feed fnew = feeds.getFeed( pub.getFeedId(), err);
		if ( fnew == null ) {
			logger.info( "Specified feed " + pub.getFeedId() + " not known to Bus Controller");	
			return resp.error();	
		}

		ArrayList<DR_Pub> pubs = fnew.getPubs();
		logger.info( "num existing pubs before = " + pubs.size() );
		
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
			return resp.error();			
		}
		pubs = fnew.getPubs();
		logger.info( "num existing pubs after = " + pubs.size() );
		
		DR_Pub pnew = dr_pubService.getDr_Pub(pub.getPubId(), err);
		return resp.success(Status.CREATED.getStatusCode(), pnew);
	}
	
	@PUT
	@ApiOperation( value = "return DR_Pub details", 
	notes = "update a DR Publisher in the specified environment.  Update a `DR_Pub` object by pubId", 
	response = DR_Pub.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 200, message = "Success", response = DR_Pub.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	@Path("/{pubId}")
	public Response updateDr_Pub( 
			@PathParam("pubId") String name, 
			DR_Pub pub
			) {
		ApiService resp = new ApiService();

		logger.info( "Entry: PUT /dr_pubs");
		pub.setPubId(name);
		DR_Pub res = dr_pubService.updateDr_Pub(pub);
		return resp.success(res);
	}
	
	@DELETE
	@ApiOperation( value = "return DR_Pub details", 
	notes = "delete a DR Publisher in the specified environment. Delete a `DR_Pub` object by pubId", 
	response = DR_Pub.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 204, message = "Success", response = DR_Pub.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	@Path("/{pubId}")
	public Response deleteDr_Pub( 
			@PathParam("pubId") String id
			){

		ApiService resp = new ApiService();

		try {
			resp.required( "pubId", id, "");
		} catch ( RequiredFieldException rfe ) {
			return resp.error();
		}

		DR_Pub pub =  dr_pubService.getDr_Pub( id, resp.getErr() );
		if ( ! resp.getErr().is2xx()) {	
			return resp.error();					
		}
		FeedService feeds = new FeedService();
		Feed fnew = feeds.getFeed( pub.getFeedId(), resp.getErr() );
		if ( fnew == null ) {
			logger.info( "Specified feed " + pub.getFeedId() + " not known to Bus Controller");	
			return resp.error();
		}
		ArrayList<DR_Pub> pubs = fnew.getPubs();
		if ( pubs.size() == 1 ) {
			resp.setCode(Status.BAD_REQUEST.getStatusCode());
			resp.setMessage( "Can't delete the last publisher of a feed");
			return resp.error();	
		}
		
		for( Iterator<DR_Pub> i = pubs.iterator(); i.hasNext(); ) {
			DR_Pub listItem = i.next();
			if ( listItem.getPubId().equals(id)) {
				pubs.remove( listItem );
			}
		}
		fnew.setPubs(pubs);
		fnew = feeds.updateFeed( fnew, resp.getErr() );
		if ( ! resp.getErr().is2xx()) {	
			return resp.error();			
		}
		
		dr_pubService.removeDr_Pub(id, resp.getErr() );
		if ( ! resp.getErr().is2xx()) {	
			return resp.error();		
		}
		return resp.success(Status.NO_CONTENT.getStatusCode(), null);
	}

	@GET
	@ApiOperation( value = "return DR_Pub details", 
	notes = "returns a DR Publisher in the specified environment. Gets a `DR_Pub` object by pubId", 
	response = DR_Pub.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 200, message = "Success", response = DR_Pub.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	@Path("/{pubId}")
	public Response get( 
			@PathParam("pubId") String id
			) {
		ApiService resp = new ApiService();

		try {
			resp.required( "feedId", id, "");
		} catch ( RequiredFieldException rfe ) {
			return resp.error();	
		}

		DR_Pub pub =  dr_pubService.getDr_Pub( id, resp.getErr() );
		if ( ! resp.getErr().is2xx()) {	
			resp.getErr();			
		}
		return resp.success(Status.OK.getStatusCode(), pub);
	}
}
