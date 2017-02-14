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
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;



import org.openecomp.dmaapbc.authentication.AuthenticationErrorException;
import org.openecomp.dmaapbc.model.ApiError;
import org.openecomp.dmaapbc.model.DcaeLocation;
import org.openecomp.dmaapbc.service.ApiService;
import org.openecomp.dmaapbc.service.DcaeLocationService;


@Path("/dcaeLocations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DcaeLocationResource {
	static final Logger logger = Logger.getLogger(DcaeLocationResource.class);	
	DcaeLocationService locationService = new DcaeLocationService();
	
	@GET
	public List<DcaeLocation> getDcaeLocations( @Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth) {
		ApiService check = new ApiService();
		try {
			check.checkAuthorization( basicAuth, uriInfo.getPath(), "GET");
		} catch ( AuthenticationErrorException ae ) {
			return null; //check.unauthorized();
		} catch ( Exception e ) {
			logger.error( "Unexpected exception " + e );
			return null; //check.unavailable(); 
		}
		return locationService.getAllDcaeLocations();
	}
	
	@POST
	public Response addDcaeLocation( DcaeLocation location,  
			@Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth ) {
		ApiService check = new ApiService();
		try {
			check.checkAuthorization( basicAuth, uriInfo.getPath(), "POST");
		} catch ( AuthenticationErrorException ae ) {
			return check.unauthorized();
		} catch ( Exception e ) {
			logger.error( "Unexpected exception " + e );
			return check.unavailable(); 
		}
		if ( locationService.getDcaeLocation(location.getDcaeLocationName()) != null ) {
			ApiError err = new ApiError();
				
			err.setCode(Status.CONFLICT.getStatusCode());
			err.setMessage("dcaeLocation already exists");
			err.setFields("dcaeLocation");
			
			logger.warn( err );
			return Response.status(Status.CONFLICT).entity( err ).build();


		}
		DcaeLocation loc = locationService.addDcaeLocation(location);
		return Response.status(Status.CREATED)
				.entity(loc)
				.build();
	}
	
	@PUT
	@Path("/{locationName}")
	public Response updateDcaeLocation( @PathParam("locationName") String name, DcaeLocation location,
			 @Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth) {
		ApiService check = new ApiService();
		try {
			check.checkAuthorization( basicAuth, uriInfo.getPathSegments().get(0).getPath(), "PUT");
		} catch ( AuthenticationErrorException ae ) {
			return check.unauthorized();
		} catch ( Exception e ) {
			logger.error( "Unexpected exception " + e );
			return check.unavailable(); 
		}
		location.setDcaeLocationName(name);
		if ( locationService.getDcaeLocation(location.getDcaeLocationName()) == null ) {
			ApiError err = new ApiError();
				
			err.setCode(Status.NOT_FOUND.getStatusCode());
			err.setMessage("dcaeLocation does not exist");
			err.setFields("dcaeLocation");
			
			logger.warn( err );
			return Response.status(Status.NOT_FOUND).entity( err ).build();


		}
		DcaeLocation loc = locationService.updateDcaeLocation(location);
		return Response.status(Status.CREATED)
				.entity(loc)
				.build();
	}
	
	@DELETE
	@Path("/{locationName}")
	public Response deleteDcaeLocation( @PathParam("locationName") String name,
			 @Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth){
		ApiService check = new ApiService();
		try {
			check.checkAuthorization( basicAuth, uriInfo.getPathSegments().get(0).getPath(), "DELETE");
		} catch ( AuthenticationErrorException ae ) {
			return check.unauthorized();
		} catch ( Exception e ) {
			logger.error( "Unexpected exception " + e );
			return check.unavailable(); 
		}
		locationService.removeDcaeLocation(name);
		return Response.status(Status.NO_CONTENT).build();
	}

	@GET
	@Path("/{locationName}")
	public Response getDcaeLocation( @PathParam("locationName") String name,
			 @Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth) {
		ApiService check = new ApiService();
		try {		
			//List<PathSegment> segments = uriInfo.getPathSegments();
			check.checkAuthorization( basicAuth, uriInfo.getPathSegments().get(0).getPath(), "GET");
		} catch ( AuthenticationErrorException ae ) {
			return check.unauthorized();
		} catch ( Exception e ) {
			logger.error( "Unexpected exception " + e );
			return check.unavailable(); 
		}
		DcaeLocation loc =  locationService.getDcaeLocation( name );
		if ( loc == null ) {
			ApiError err = new ApiError();
				
			err.setCode(Status.NOT_FOUND.getStatusCode());
			err.setMessage("dcaeLocation does not exist");
			err.setFields("dcaeLocation");
			
			logger.warn( err );
			return Response.status(Status.NOT_FOUND).entity( err ).build();


		}

		return Response.status(Status.OK)
				.entity(loc)
				.build();
	}
}
