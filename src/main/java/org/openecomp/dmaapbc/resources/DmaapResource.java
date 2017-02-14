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

//
// $Id$

package org.openecomp.dmaapbc.resources;



import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.openecomp.dmaapbc.authentication.AuthenticationErrorException;
import org.openecomp.dmaapbc.model.Dmaap;
import org.openecomp.dmaapbc.service.ApiService;
import org.openecomp.dmaapbc.service.DmaapService;



@Path("/dmaap")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DmaapResource {
	static final Logger logger = Logger.getLogger(DmaapResource.class);

	DmaapService dmaapService = new DmaapService();
	
	@GET
	public Response getDmaap(@Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth ) {
		logger.debug( "Entry: GET  " + uriInfo.getPath()  );
		ApiService check = new ApiService();
		try {
			check.checkAuthorization( basicAuth, uriInfo.getPath(), "GET");
		} catch ( AuthenticationErrorException ae ) {
			return check.unauthorized();
		} catch ( Exception e ) {
			logger.error( "Unexpected exception " + e );
			return check.unavailable(); 
		}
		Dmaap d =  dmaapService.getDmaap();
		return Response.ok(d)
				.build();
	}
	
	@POST
	public Response addDmaap( Dmaap obj,@Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth ) {
		
		ApiService check = new ApiService();
		try {
			check.checkAuthorization( basicAuth, uriInfo.getPath(), "POST");
		} catch ( AuthenticationErrorException ae ) {
			return check.unauthorized();
		} catch ( Exception e ) {
			logger.error( "Unexpected exception " + e );
			return check.unavailable(); 
		}
		try { //check for required fields
			check.required( "dmaapName", obj.getDmaapName(), "^\\S+$" );  //no white space allowed in dmaapName
			check.required( "dmaapProvUrl", obj.getDrProvUrl(), "" );
			check.required( "topicNsRoot", obj.getTopicNsRoot(), "" );
			check.required( "bridgeAdminTopic", obj.getBridgeAdminTopic(), "" );
		} catch( RequiredFieldException rfe ) {
			return Response.status(check.getErr().getCode())
					.entity( check.getErr() )
					.build();
		}
	
		Dmaap d =  dmaapService.addDmaap(obj);
		if ( d == null ) {
			return Response.status(Status.NOT_FOUND)
					.build();	

		} 

		return Response.ok(d)
				.build();
	}
	
	@PUT
	public Response updateDmaap(  Dmaap obj, @Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth ) {
		ApiService check = new ApiService();
		try {
			check.checkAuthorization( basicAuth, uriInfo.getPath(), "PUT");
		} catch ( AuthenticationErrorException ae ) {
			return check.unauthorized();
		} catch ( Exception e ) {
			logger.error( "Unexpected exception " + e );
			return check.unavailable(); 
		}
		try { //check for required fields
			check.required( "dmaapName", obj.getDmaapName(), "^\\S+$" );  //no white space allowed in dmaapName
			check.required( "dmaapProvUrl", obj.getDrProvUrl(), "" );
			check.required( "topicNsRoot", obj.getTopicNsRoot(), "" );
			check.required( "bridgeAdminTopic", obj.getBridgeAdminTopic(), "" );
		} catch( RequiredFieldException rfe ) {
			return Response.status(check.getErr().getCode())
					.entity( check.getErr() )
					.build();
		}
		Dmaap d =  dmaapService.updateDmaap(obj);
		if ( d != null ) {
			return Response.ok(d)
					.build();
		} else {
			return Response.status(Status.NOT_FOUND)
					.build();		
		}	
	}
	
	
}
