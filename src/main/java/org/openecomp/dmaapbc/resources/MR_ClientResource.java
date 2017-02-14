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
import org.openecomp.dmaapbc.model.ApiError;
import org.openecomp.dmaapbc.model.MR_Client;
import org.openecomp.dmaapbc.model.MR_Cluster;
import org.openecomp.dmaapbc.model.Topic;
import org.openecomp.dmaapbc.service.ApiService;
import org.openecomp.dmaapbc.service.MR_ClientService;
import org.openecomp.dmaapbc.service.MR_ClusterService;
import org.openecomp.dmaapbc.service.TopicService;


@Path("/mr_clients")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MR_ClientResource extends ApiResource {
	static final Logger logger = Logger.getLogger(MR_ClientResource.class);
	private MR_ClientService mr_clientService = new MR_ClientService();
		
	@GET
	public List<MR_Client> getMr_Clients(@Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth) {
		ApiService resp = new ApiService();
		try {
			resp.checkAuthorization( basicAuth, uriInfo.getPath(), "GET");
		} catch ( AuthenticationErrorException ae ) {
			return null; //resp.unauthorized();
		} catch ( Exception e ) {
			logger.error( "Unexpected exception " + e );
			return null; //resp.unavailable(); 
		}
		return mr_clientService.getAllMr_Clients();
	}
		
	@POST
	public Response addMr_Client( MR_Client client,
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
			resp.required( "fqtn", client.getFqtn(), "");
			resp.required( "dcaeLocationName", client.getDcaeLocationName(), "");
			resp.required( "clientRole", client.getClientRole(), "" );
			resp.required( "action", client.getAction(), "");

		} catch ( RequiredFieldException rfe ) {
			logger.debug( resp.toString() );
			return Response.status(resp.getErr().getCode())
					.entity( resp.getErr() )
					.build();	
		}
		MR_ClusterService clusters = new MR_ClusterService();

		MR_Cluster cluster = clusters.getMr_Cluster(client.getDcaeLocationName(), resp.getErr());
		if ( cluster == null ) {
			ApiError err = resp.getErr();
			err.setCode(Status.BAD_REQUEST.getStatusCode());
			err.setMessage( "MR_Cluster alias not found for dcaeLocation: " + client.getDcaeLocationName());
			err.setFields("dcaeLocationName");
			logger.warn( err.getMessage() );
			return Response.status(err.getCode()).entity( err ).build();	
		}
		String url = cluster.getFqdn();
		if ( url == null || url.isEmpty() ) {
			ApiError err = resp.getErr();
			err.setCode(Status.BAD_REQUEST.getStatusCode());
			err.setMessage("FQDN not set for dcaeLocation " + client.getDcaeLocationName() );
			err.setFields("fqdn");
			logger.warn( err.getMessage() );
			return Response.status(err.getCode()).entity( err ).build();
		}
		TopicService topics = new TopicService();
		ApiError err = resp.getErr();
		Topic t = topics.getTopic(client.getFqtn(), err);
		if ( t == null ) {
			return Response.status(err.getCode()).entity( err ).build();			
		}
		MR_Client nClient =  mr_clientService.addMr_Client(client, t, err);
		if ( err.is2xx()) {
			int n;
			ArrayList<MR_Client> tc = t.getClients();
			if ( tc == null ) {
				n = 0;
				tc = new ArrayList<MR_Client>();
			} else {
				n = tc.size();
			}
			logger.info( "number of existing clients for topic is " + n );


			logger.info( "n=" + n + " tc=" + tc + " client=" + client );
			tc.add( client );
			t.setClients(tc);
			topics.updateTopic( t );
			return Response.ok(nClient)
					.build();
		}
		else {
			return Response.status(resp.getErr().getCode())
					.entity( resp.getErr() )
					.build();			
		}
	}
		
	@PUT
	@Path("/{clientId}")
	public Response updateMr_Client( @PathParam("clientId") String clientId, MR_Client client,
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
			resp.required( "fqtn", client.getFqtn(), "");
			resp.required( "dcaeLocationName", client.getDcaeLocationName(), "");
			resp.required( "clientRole", client.getClientRole(), "" );
			resp.required( "action", client.getAction(), "");

		} catch ( RequiredFieldException rfe ) {
			logger.debug( resp.toString() );
			return Response.status(resp.getErr().getCode())
					.entity( resp.getErr() )
					.build();	
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
	@Path("/{subId}")
	public Response deleteMr_Client( @PathParam("subId") String id,
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
			resp.required( "clientId", id, "");
		} catch ( RequiredFieldException rfe ) {
			logger.debug( resp.toString() );
			return Response.status(resp.getErr().getCode())
					.entity( resp.getErr() )
					.build();	
		}
		MR_Client client = mr_clientService.removeMr_Client(id, resp.getErr() );
		if ( resp.getErr().is2xx()) {
			TopicService topics = new TopicService();
			ApiError err = resp.getErr();
			Topic t = topics.getTopic(client.getFqtn(), err);
			if ( t == null ) {
				logger.info( err.getMessage() );
				return Response.status(err.getCode()).entity( err ).build();			
			}
			
			ArrayList<MR_Client> tc = t.getClients();
			for( MR_Client c: tc) {
				if ( c.getMrClientId().equals(id)) {
					tc.remove(c);
					break;
				}
			}
			t.setClients(tc);
			topics.updateTopic( t );

			return Response.status(Status.NO_CONTENT.getStatusCode())
				.build();
		}
		
		return Response.status(resp.getErr().getCode())
				.entity( resp.getErr() )
				.build();
	}

	@GET
	@Path("/{subId}")
	public Response test( @PathParam("subId") String id,
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
		try {
			resp.required( "clientId", id, "");
		} catch ( RequiredFieldException rfe ) {
			logger.debug( resp.toString() );
			return Response.status(resp.getErr().getCode())
					.entity( resp.getErr() )
					.build();	
		}
		MR_Client nClient =  mr_clientService.getMr_Client( id, resp.getErr() );
		if ( resp.getErr().is2xx()) {
			return Response.ok(nClient)
				.build();
		}
		return Response.status(resp.getErr().getCode())
				.entity( resp.getErr() )
				.build();	
	}
}
