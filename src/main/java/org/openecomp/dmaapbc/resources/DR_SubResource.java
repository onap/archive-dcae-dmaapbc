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
import org.openecomp.dmaapbc.model.DR_Sub;
import org.openecomp.dmaapbc.model.Feed;
import org.openecomp.dmaapbc.service.ApiService;
import org.openecomp.dmaapbc.service.DR_SubService;
import org.openecomp.dmaapbc.service.FeedService;


@Path("/dr_subs")
@Api( value= "dr_subs", description = "Endpoint for a Data Router client that implements a Subscriber" )
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authorization
public class DR_SubResource extends BaseLoggingClass {
		
	@GET
	@ApiOperation( value = "return DR_Sub details", 
	notes = "Returns array of  `DR_Sub` objects.  Add filter for feedId.", 
	response = DR_Sub.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 200, message = "Success", response = DR_Pub.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	public Response getDr_Subs() {

		ApiService resp = new ApiService();

		DR_SubService dr_subService = new DR_SubService();
		List<DR_Sub> subs = dr_subService.getAllDr_Subs();

		GenericEntity<List<DR_Sub>> list = new GenericEntity<List<DR_Sub>>(subs) {
        };
        return resp.success(list);
	}
		
	@POST
	@ApiOperation( value = "return DR_Sub details", 
	notes = "Create a  `DR_Sub` object.  ", 
	response = DR_Sub.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 200, message = "Success", response = DR_Pub.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	public Response addDr_Sub( 
			DR_Sub sub
			) {
	
		ApiService resp = new ApiService();

		try {
			resp.required( "feedId", sub.getFeedId(), "");
			resp.required( "dcaeLocationName", sub.getDcaeLocationName(), "");
	
		} catch ( RequiredFieldException rfe ) {
			logger.debug( resp.toString() );
			return resp.error();	
		}
		
		FeedService feeds = new FeedService();
		Feed fnew = feeds.getFeed( sub.getFeedId(), resp.getErr() );
		if ( fnew == null ) {
			logger.warn( "Specified feed " + sub.getFeedId() + " not known to Bus Controller");
			resp.setCode(Status.NOT_FOUND.getStatusCode());
			return resp.error();
		}

		DR_SubService dr_subService = new DR_SubService( fnew.getSubscribeURL());
		ArrayList<DR_Sub> subs = fnew.getSubs();
		logger.info( "num existing subs before = " + subs.size() );
		DR_Sub snew = dr_subService.addDr_Sub(sub, resp.getErr() );
		if ( ! resp.getErr().is2xx() ) {
			return resp.error();
		}
		subs.add( snew );
		logger.info( "num existing subs after = " + subs.size() );
		
		fnew.setSubs(subs);
		logger.info( "update feed");
		//feeds.updateFeed( fnew, err );			
		
		return resp.success(Status.CREATED.getStatusCode(), snew);

	}
		
	@PUT
	@ApiOperation( value = "return DR_Sub details", 
	notes = "Update a  `DR_Sub` object, selected by subId", 
	response = DR_Sub.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 200, message = "Success", response = DR_Pub.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	@Path("/{subId}")
	public Response updateDr_Sub( 
			@PathParam("subId") String name, 
			DR_Sub sub
			) {

		ApiService resp = new ApiService();

		try {
			resp.required( "subId", name, "");
			resp.required( "feedId", sub.getFeedId(), "");
			resp.required( "dcaeLocationName", sub.getDcaeLocationName(), "");
	
		} catch ( RequiredFieldException rfe ) {
			logger.debug( resp.toString() );
			return resp.error();
		}
		FeedService feeds = new FeedService();
		Feed fnew = feeds.getFeed( sub.getFeedId(), resp.getErr() );
		if ( fnew == null ) {
			logger.warn( "Specified feed " + sub.getFeedId() + " not known to Bus Controller");
			return resp.error();
		}
		
		DR_SubService dr_subService = new DR_SubService();
		sub.setSubId(name);
		DR_Sub nsub = dr_subService.updateDr_Sub(sub, resp.getErr() );
		if ( nsub != null && nsub.isStatusValid() ) {
			return resp.success(nsub);
		}
		return resp.error();
	}
		
	@DELETE
	@ApiOperation( value = "return DR_Sub details", 
	notes = "Delete a  `DR_Sub` object, selected by subId", 
	response = DR_Sub.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 200, message = "Success", response = DR_Pub.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	@Path("/{subId}")
	public Response deleteDr_Sub( 
			@PathParam("subId") String id
			){

		ApiService resp = new ApiService();

		try {
			resp.required( "subId", id, "");
		} catch ( RequiredFieldException rfe ) {
			logger.debug( resp.toString() );
			return resp.error();	
		}
		DR_SubService dr_subService = new DR_SubService();
		dr_subService.removeDr_Sub(id, resp.getErr() );
		if ( ! resp.getErr().is2xx() ) {
			return resp.error();
		}
		return resp.success(Status.NO_CONTENT.getStatusCode(), null );
	}

	@GET
	@ApiOperation( value = "return DR_Sub details", 
	notes = "Retrieve a  `DR_Sub` object, selected by subId", 
	response = DR_Sub.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 200, message = "Success", response = DR_Pub.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	@Path("/{subId}")
	public Response get( 
			@PathParam("subId") String id
			) {
		ApiService resp = new ApiService();

		try {
			resp.required( "subId", id, "");
		} catch ( RequiredFieldException rfe ) {
			logger.debug( resp.toString() );
			return resp.error();
		}
		DR_SubService dr_subService = new DR_SubService();
		DR_Sub sub =  dr_subService.getDr_Sub( id, resp.getErr() );
		if ( sub != null && sub.isStatusValid() ) {
			return resp.success(sub);
		}
		return resp.error();
	}
}
