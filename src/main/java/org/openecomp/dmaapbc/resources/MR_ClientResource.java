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
import org.openecomp.dmaapbc.model.DR_Pub;
import org.openecomp.dmaapbc.model.MR_Client;
import org.openecomp.dmaapbc.model.MR_Cluster;
import org.openecomp.dmaapbc.model.Topic;
import org.openecomp.dmaapbc.service.ApiService;
import org.openecomp.dmaapbc.service.MR_ClientService;
import org.openecomp.dmaapbc.service.MR_ClusterService;
import org.openecomp.dmaapbc.service.TopicService;


@Path("/mr_clients")
@Api( value= "MR_Clients", description = "Endpoint for a Message Router Client that implements a Publisher or a Subscriber" )
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authorization
public class MR_ClientResource extends BaseLoggingClass {

	private MR_ClientService mr_clientService = new MR_ClientService();
		
	@GET
	@ApiOperation( value = "return MR_Client details", 
	notes = "Returns array of  `MR_Client` objects.", 
	response = MR_Client.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 200, message = "Success", response = DR_Pub.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	public Response getMr_Clients() {
		ApiService resp = new ApiService();

		List<MR_Client> clients = mr_clientService.getAllMr_Clients();

		GenericEntity<List<MR_Client>> list = new GenericEntity<List<MR_Client>>(clients) {
        };
        return resp.success(list);		
	}
		
	@POST
	@ApiOperation( value = "return MR_Client details", 
	notes = "Create a  `MR_Client` object.", 
	response = MR_Client.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 200, message = "Success", response = DR_Pub.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	public Response addMr_Client( 
			MR_Client client
			) {
		ApiService resp = new ApiService();

		try {
			resp.required( "fqtn", client.getFqtn(), "");
			resp.required( "dcaeLocationName", client.getDcaeLocationName(), "");
			resp.required( "clientRole", client.getClientRole(), "" );
			resp.required( "action", client.getAction(), "");

		} catch ( RequiredFieldException rfe ) {
			logger.debug( resp.toString() );
			return resp.error();	
		}
		MR_ClusterService clusters = new MR_ClusterService();

		MR_Cluster cluster = clusters.getMr_Cluster(client.getDcaeLocationName(), resp.getErr());
		if ( cluster == null ) {

			resp.setCode(Status.BAD_REQUEST.getStatusCode());
			resp.setMessage( "MR_Cluster alias not found for dcaeLocation: " + client.getDcaeLocationName());
			resp.setFields("dcaeLocationName");
			logger.warn( resp.toString() );
			return resp.error();
		}
		String url = cluster.getFqdn();
		if ( url == null || url.isEmpty() ) {

			resp.setCode(Status.BAD_REQUEST.getStatusCode());
			resp.setMessage("FQDN not set for dcaeLocation " + client.getDcaeLocationName() );
			resp.setFields("fqdn");
			logger.warn( resp.toString() );
			return resp.error();
		}
		TopicService topics = new TopicService();

		Topic t = topics.getTopic(client.getFqtn(), resp.getErr() );
		if ( t == null ) {
			return resp.error();		
		}
		MR_Client nClient =  mr_clientService.addMr_Client(client, t, resp.getErr());
		if ( resp.getErr().is2xx()) {
			t = topics.getTopic(client.getFqtn(),  resp.getErr());
			topics.checkForBridge(t, resp.getErr());
			return resp.success(nClient);
		}
		else {
			return resp.error();			
		}
	}
		
	@PUT
	@ApiOperation( value = "return MR_Client details", 
	notes = "Update a  `MR_Client` object, specified by clientId", 
	response = MR_Client.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 200, message = "Success", response = DR_Pub.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	@Path("/{clientId}")
	public Response updateMr_Client( 
			@PathParam("clientId") String clientId, 
			MR_Client client
			) {
		ApiService resp = new ApiService();

		try {
			resp.required( "fqtn", client.getFqtn(), "");
			resp.required( "dcaeLocationName", client.getDcaeLocationName(), "");
			resp.required( "clientRole", client.getClientRole(), "" );
			resp.required( "action", client.getAction(), "");

		} catch ( RequiredFieldException rfe ) {
			logger.debug( resp.toString() );
			return resp.error();		
		}
		client.setMrClientId(clientId);
		MR_Client nClient = mr_clientService.updateMr_Client(client, resp.getErr() );
		if ( resp.getErr().is2xx()) {
			return Response.ok(nClient)
				.build();
		}
		return Response.status(resp.getErr().getCode())
				.entity( resp.getErr() )
				.build();
	}
		
	@DELETE
	@ApiOperation( value = "return MR_Client details", 
	notes = "Delete a  `MR_Client` object, specified by clientId", 
	response = MR_Client.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 204, message = "Success", response = DR_Pub.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	@Path("/{subId}")
	public Response deleteMr_Client( 
			@PathParam("subId") String id
			){
		ApiService resp = new ApiService();

		try {
			resp.required( "clientId", id, "");
		} catch ( RequiredFieldException rfe ) {
			logger.debug( resp.toString() );
			return resp.error();	
		}
		mr_clientService.removeMr_Client(id, true, resp.getErr() );
		if ( resp.getErr().is2xx()) {
			return resp.success(Status.NO_CONTENT.getStatusCode(), null);
		}
		
		return resp.error();
	}

	@GET
	@ApiOperation( value = "return MR_Client details", 
	notes = "Retrieve a  `MR_Client` object, specified by clientId", 
	response = MR_Client.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 200, message = "Success", response = DR_Pub.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	@Path("/{subId}")
	public Response test( 
			@PathParam("subId") String id
			) {
		ApiService resp = new ApiService();

		try {
			resp.required( "clientId", id, "");
		} catch ( RequiredFieldException rfe ) {
			logger.debug( resp.toString() );
			return resp.error();	
		}
		MR_Client nClient =  mr_clientService.getMr_Client( id, resp.getErr() );
		if ( resp.getErr().is2xx()) {
			return resp.success(nClient);
		}
		return resp.error();	
	}
}
