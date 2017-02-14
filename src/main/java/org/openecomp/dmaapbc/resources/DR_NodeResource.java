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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.openecomp.dmaapbc.authentication.AuthenticationErrorException;
import org.openecomp.dmaapbc.model.DR_Node;
import org.openecomp.dmaapbc.service.ApiService;
import org.openecomp.dmaapbc.service.DR_NodeService;

@Path("/dr_nodes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DR_NodeResource extends ApiResource {
	static final Logger logger = Logger.getLogger(DR_NodeResource.class);
	DR_NodeService dr_nodeService = new DR_NodeService();
	
	@GET
	public List<DR_Node> getDr_Nodes(@Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth) {
		ApiService resp = new ApiService();
		try {
			resp.checkAuthorization( basicAuth, uriInfo.getPath(), "GET");
		} catch ( AuthenticationErrorException ae ) {
			return null; //resp.unauthorized();
		} catch ( Exception e ) {
			logger.error( "Unexpected exception " + e );
			return null; //resp.unavailable(); 
		}
		return dr_nodeService.getAllDr_Nodes();
	}
	
	@POST
	public Response addDr_Node( DR_Node node,
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
		try {
			resp.required( "dcaeLocation", node.getDcaeLocationName(), "");
			resp.required( "fqdn", node.getFqdn(), "");
		} catch ( RequiredFieldException rfe ) {
			logger.debug( resp.toString() );
			return Response.status(Status.BAD_REQUEST).entity( resp ).build();	
		}
		DR_Node nNode = dr_nodeService.addDr_Node(node, resp.getErr());
		if ( resp.getErr().is2xx()) {
			return Response.status(Status.OK.getStatusCode())
					.entity(nNode)
					.build();
		}
		return Response.status( resp.getErr().getCode())
				.entity( resp.getErr() )
				.build();
	}
	
	@PUT
	@Path("/{fqdn}")
	public Response updateDr_Node( @PathParam("fqdn") String name, DR_Node node,
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
		try {
			resp.required( "dcaeLocation", name, "");
			resp.required( "fqdn", node.getFqdn(), "");
		} catch ( RequiredFieldException rfe ) {
			logger.debug( resp.toString() );
			return Response.status(Status.BAD_REQUEST).entity( resp ).build();	
		}
		node.setFqdn(name);
		DR_Node nNode = dr_nodeService.updateDr_Node(node, resp.getErr());
		if ( resp.getErr().is2xx()) {
			return Response.status(Status.OK.getStatusCode())
					.entity(nNode)
					.build();
		}
		return Response.status( resp.getErr().getCode())
				.entity( resp.getErr() )
				.build();
	}
	
	@DELETE
	@Path("/{fqdn}")
	public Response deleteDr_Node( @PathParam("fqdn") String name,
			@Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth){

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
			resp.required( "fqdn", name, "");
		} catch ( RequiredFieldException rfe ) {
			logger.debug( resp.toString() );
			return Response.status(Status.BAD_REQUEST).entity( resp ).build();	
		}
		dr_nodeService.removeDr_Node(name, resp.getErr());
		if ( resp.getErr().is2xx() ) {
			return Response.status(Status.NO_CONTENT.getStatusCode())
					.build();
		}
		return Response.status( resp.getErr().getCode())
				.entity( resp.getErr() )
				.build();
	}

	@GET
	@Path("/{fqdn}")
	public Response get( @PathParam("fqdn") String name,
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
		DR_Node nNode = dr_nodeService.getDr_Node( name, resp.getErr() );
		if ( resp.getErr().is2xx() ) {
			return Response.status(Status.OK.getStatusCode())
					.entity(nNode)
					.build();
		}
		return Response.status( resp.getErr().getCode())
				.entity( resp.getErr() )
				.build();
	}
}
