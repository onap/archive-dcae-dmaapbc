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
import org.openecomp.dmaapbc.model.DR_Node;
import org.openecomp.dmaapbc.service.ApiService;
import org.openecomp.dmaapbc.service.DR_NodeService;

@Path("/dr_nodes")
@Api( value= "dr_nodes", description = "Endpoint for a Data Router Node server" )
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authorization
public class DR_NodeResource extends BaseLoggingClass {

	DR_NodeService dr_nodeService = new DR_NodeService();
	
	@GET
	@ApiOperation( value = "return DR_Node details", 
	notes = "Returns array of `DR_Node` object array.  Need to add filter by dcaeLocation.", 
	response = DR_Node.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 200, message = "Success", response = DR_Node.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	public Response getDr_Nodes() {
		ApiService resp = new ApiService();

		List<DR_Node> nodes = dr_nodeService.getAllDr_Nodes();

		GenericEntity<List<DR_Node>> list = new GenericEntity<List<DR_Node>>(nodes) {
        };
        return resp.success(list);
	}
	
	@POST
	@ApiOperation( value = "return DR_Node details", 
	notes = "create a `DR_Node` in a *dcaeLocation*.  Note that multiple `DR_Node`s may exist in the same `dcaeLocation`.", 
	response = DR_Node.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 200, message = "Success", response = DR_Node.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	public Response addDr_Node( 
			DR_Node node
			) {
		ApiService resp = new ApiService();

		try {
			resp.required( "dcaeLocation", node.getDcaeLocationName(), "");
			resp.required( "fqdn", node.getFqdn(), "");
		} catch ( RequiredFieldException rfe ) {
			resp.setCode(Status.BAD_REQUEST.getStatusCode());
			resp.setMessage("missing required field");
			resp.setFields("dcaeLocation, fqdn");
			
			return resp.error();
		}
		DR_Node nNode = dr_nodeService.addDr_Node(node, resp.getErr());
		if ( resp.getErr().is2xx()) {
			return resp.success(nNode);
		}
		return resp.error();
	}
	
	@PUT
	@ApiOperation( value = "return DR_Node details", 
	notes = "Update a single `DR_Node` object.", 
	response = DR_Node.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 200, message = "Success", response = DR_Node.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	@Path("/{fqdn}")
	public Response updateDr_Node( 
			@PathParam("fqdn") String name, 
			DR_Node node
			) {
		ApiService resp = new ApiService();

		try {
			resp.required( "dcaeLocation", name, "");
			resp.required( "fqdn", node.getFqdn(), "");
		} catch ( RequiredFieldException rfe ) {
			return resp.error();	
		}
		node.setFqdn(name);
		DR_Node nNode = dr_nodeService.updateDr_Node(node, resp.getErr());
		if ( resp.getErr().is2xx()) {
			return resp.success(nNode);
		}
		return resp.error();
	}
	
	@DELETE
	@ApiOperation( value = "No Content", 
	notes = "Delete a single `DR_Node` object.", 
	response = DR_Node.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 204, message = "Success", response = DR_Node.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	@Path("/{fqdn}")
	public Response deleteDr_Node( 
			@PathParam("fqdn") String name
			){

		ApiService resp = new ApiService();

		try {
			resp.required( "fqdn", name, "");
		} catch ( RequiredFieldException rfe ) {
			logger.debug( resp.toString() );
			return resp.error();	
		}
		dr_nodeService.removeDr_Node(name, resp.getErr());
		if ( resp.getErr().is2xx() ) {
			return resp.success(Status.NO_CONTENT.getStatusCode(), null);
		}
		return resp.error();
	}

	@GET
	@ApiOperation( value = "return DR_Node details", 
	notes = "Retrieve a single `DR_Node` object.", 
	response = DR_Node.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 200, message = "Success", response = DR_Node.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	@Path("/{fqdn}")
	public Response get( 
			@PathParam("fqdn") String name
			) {
		ApiService resp = new ApiService();

		DR_Node nNode = dr_nodeService.getDr_Node( name, resp.getErr() );
		if ( resp.getErr().is2xx() ) {
			return resp.success(nNode);
		}
		return resp.error();
	}
}
