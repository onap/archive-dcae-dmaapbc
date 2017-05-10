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

import org.apache.log4j.Logger;
import org.openecomp.dmaapbc.logging.BaseLoggingClass;
import org.openecomp.dmaapbc.model.ApiError;
import org.openecomp.dmaapbc.model.DcaeLocation;
import org.openecomp.dmaapbc.model.Dmaap;
import org.openecomp.dmaapbc.service.ApiService;
import org.openecomp.dmaapbc.service.DcaeLocationService;


@Path("/dcaeLocations")
@Api( value= "dcaeLocations", description = "an OpenStack tenant purposed for OpenDCAE (i.e. where OpenDCAE components might be deployed)" )
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authorization
public class DcaeLocationResource extends BaseLoggingClass {
	static final Logger logger = Logger.getLogger(DcaeLocationResource.class);	
	DcaeLocationService locationService = new DcaeLocationService();
	
	@GET
	@ApiOperation( value = "return dcaeLocation details", 
		notes = "Returns array of  `dcaeLocation` objects.  All objects managed by DMaaP are deployed in some `dcaeLocation` which is a unique identifier for an *OpenStack* tenant purposed for a *dcaeLayer*  (ecomp or edge).", 
		response = DcaeLocation.class)
    @ApiResponses( value = {
        @ApiResponse( code = 200, message = "Success", response = Dmaap.class),
        @ApiResponse( code = 400, message = "Error", response = ApiError.class )
    })
	public Response getDcaeLocations() {
		ApiService check = new ApiService();

		List<DcaeLocation> locs = locationService.getAllDcaeLocations();

		GenericEntity<List<DcaeLocation>> list = new GenericEntity<List<DcaeLocation>>(locs) {
        };
        return check.success(list);
	}
	
	@POST
	@ApiOperation( value = "return dcaeLocation details", 
		notes = "Create some `dcaeLocation` which is a unique identifier for an *OpenStack* tenant purposed for a *dcaeLayer*  (ecomp or edge).", 
		response = DcaeLocation.class)
    @ApiResponses( value = {
        @ApiResponse( code = 200, message = "Success", response = Dmaap.class),
        @ApiResponse( code = 400, message = "Error", response = ApiError.class )
    })
	public Response addDcaeLocation( 
			DcaeLocation location 
			) {
		ApiService check = new ApiService();

		if ( locationService.getDcaeLocation(location.getDcaeLocationName()) != null ) {
				
			check.setCode(Status.CONFLICT.getStatusCode());
			check.setMessage("dcaeLocation already exists");
			check.setFields("dcaeLocation");
			
			return check.error();

		}
		DcaeLocation loc = locationService.addDcaeLocation(location);
		return check.success(Status.CREATED.getStatusCode(), loc);
	}
	
	@PUT
	@ApiOperation( value = "return dcaeLocation details", 
		notes = "update the openStackAvailabilityZone of a dcaeLocation", 
		response = DcaeLocation.class)
    @ApiResponses( value = {
        @ApiResponse( code = 200, message = "Success", response = Dmaap.class),
        @ApiResponse( code = 400, message = "Error", response = ApiError.class )
    })
	@Path("/{locationName}")
	public Response updateDcaeLocation( 
			@PathParam("locationName") String name, 
			DcaeLocation location
			 ) {
		ApiService check = new ApiService();

		location.setDcaeLocationName(name);
		if ( locationService.getDcaeLocation(location.getDcaeLocationName()) == null ) {
			ApiError err = new ApiError();
				
			err.setCode(Status.NOT_FOUND.getStatusCode());
			err.setMessage("dcaeLocation does not exist");
			err.setFields("dcaeLocation");
			
			return check.notFound();


		}
		DcaeLocation loc = locationService.updateDcaeLocation(location);
		return check.success(Status.CREATED.getStatusCode(), loc );
	}
	
	@DELETE
	@ApiOperation( value = "return dcaeLocation details", notes = "delete a dcaeLocation", response = Dmaap.class)
    @ApiResponses( value = {
        @ApiResponse( code = 204, message = "Success", response = Dmaap.class),
        @ApiResponse( code = 400, message = "Error", response = ApiError.class )
    })
	@Path("/{locationName}")
	public Response deleteDcaeLocation( 
			@PathParam("locationName") String name
			 ){
		ApiService check = new ApiService();

		locationService.removeDcaeLocation(name);
		return check.success(Status.NO_CONTENT.getStatusCode(), null);
	}

	@GET
	@ApiOperation( value = "return dcaeLocation details", notes = "Returns a specific `dcaeLocation` object with specified tag", response = Dmaap.class)
    @ApiResponses( value = {
        @ApiResponse( code = 200, message = "Success", response = Dmaap.class),
        @ApiResponse( code = 400, message = "Error", response = ApiError.class )
    })
	@Path("/{locationName}")
	public Response getDcaeLocation( 
			@PathParam("locationName") String name
			 ) {
		ApiService check = new ApiService();

		DcaeLocation loc =  locationService.getDcaeLocation( name );
		if ( loc == null ) {
			ApiError err = new ApiError();
				
			err.setCode(Status.NOT_FOUND.getStatusCode());
			err.setMessage("dcaeLocation does not exist");
			err.setFields("dcaeLocation");
			
			return check.error();


		}

		return check.success(loc);
	}
}
