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
import org.openecomp.dmaapbc.model.MR_Cluster;
import org.openecomp.dmaapbc.service.ApiService;
import org.openecomp.dmaapbc.service.MR_ClusterService;


@Path("/mr_clusters")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MR_ClusterResource {
	static final Logger logger = Logger.getLogger(MR_ClusterResource.class);

	MR_ClusterService mr_clusterService = new MR_ClusterService();
		
	@GET
	public List<MR_Cluster> getMr_Clusters(@Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth) {
		ApiService resp = new ApiService();
		try {
			resp.checkAuthorization( basicAuth, uriInfo.getPath(), "GET");
		} catch ( AuthenticationErrorException ae ) {
			return null; //resp.unauthorized();
		} catch ( Exception e ) {
			logger.error( "Unexpected exception " + e );
			return null; //resp.unavailable(); 
		}
		return mr_clusterService.getAllMr_Clusters();
	}
		
	@POST
	public Response  addMr_Cluster( MR_Cluster cluster,
			@Context UriInfo uriInfo, @HeaderParam("Authorization") String basicAuth) {
		logger.info("Entry: /POST" );
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
			resp.required( "dcaeLocationName", cluster.getDcaeLocationName(), "" );  
			resp.required( "fqdn", cluster.getFqdn(), "" );
		} catch( RequiredFieldException rfe ) {
			return Response.status(resp.getErr().getCode())
					.entity( resp.getErr() )
					.build();
		}
		MR_Cluster mrc =  mr_clusterService.addMr_Cluster(cluster, resp.getErr() );
		if ( mrc != null && mrc.isStatusValid() ) {
			return Response.status(Status.CREATED)
					.entity(mrc)
					.build();
		}
		return Response.status(resp.getErr().getCode())
				.entity(resp.getErr())
				.build();

	}
		
	@PUT
	@Path("/{clusterId}")
	public Response updateMr_Cluster( @PathParam("clusterId") String clusterId, MR_Cluster cluster,
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
			resp.required( "fqdn", clusterId, "" );
			resp.required( "dcaeLocationName", cluster.getDcaeLocationName(), "" );  
		} catch( RequiredFieldException rfe ) {
			return Response.status(resp.getErr().getCode())
					.entity( resp.getErr() )
					.build();
		}
		cluster.setDcaeLocationName(clusterId);
		MR_Cluster mrc =  mr_clusterService.updateMr_Cluster(cluster, resp.getErr() );
		if ( mrc != null && mrc.isStatusValid() ) {
			return Response.status(Status.CREATED)
					.entity(mrc)
					.build();
		}
		return Response.status(resp.getErr().getCode())
				.entity(resp.getErr())
				.build();
	}
		
	@DELETE
	@Path("/{clusterId}")
	public Response deleteMr_Cluster( @PathParam("clusterId") String id,
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
			resp.required( "fqdn", id, "" );
		} catch( RequiredFieldException rfe ) {
			return Response.status(resp.getErr().getCode())
					.entity( resp.getErr() )
					.build();
		}
		mr_clusterService.removeMr_Cluster(id, resp.getErr() );
		if ( resp.getErr().is2xx()) {
			return Response.status(Status.NO_CONTENT.getStatusCode())
						.build();
		} 
		return Response.status(resp.getErr().getCode())
				.entity( resp.getErr() )
				.build();
	}

	@GET
	@Path("/{clusterId}")
	public Response getMR_Cluster( @PathParam("clusterId") String id,
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
			resp.required( "dcaeLocationName", id, "" );
		} catch( RequiredFieldException rfe ) {
			return Response.status(resp.getErr().getCode())
					.entity( resp.getErr() )
					.build();
		}
		MR_Cluster mrc =  mr_clusterService.getMr_Cluster( id, resp.getErr() );
		if ( mrc != null && mrc.isStatusValid() ) {
			return Response.status(Status.CREATED)
					.entity(mrc)
					.build();
		}
		return Response.status(resp.getErr().getCode())
				.entity(resp.getErr())
				.build();
	}
}
