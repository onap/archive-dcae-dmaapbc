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
import org.openecomp.dmaapbc.model.MR_Cluster;
import org.openecomp.dmaapbc.service.ApiService;
import org.openecomp.dmaapbc.service.MR_ClientService;
import org.openecomp.dmaapbc.service.MR_ClusterService;


@Path("/mr_clusters")
@Api( value= "MR_Clusters", description = "Endpoint for a Message Router servers in a Cluster configuration" )
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authorization
public class MR_ClusterResource extends BaseLoggingClass {

	MR_ClusterService mr_clusterService = new MR_ClusterService();
	MR_ClientService mr_clients = new MR_ClientService();
		
	@GET
	@ApiOperation( value = "return MR_Cluster details", 
	notes = "Returns array of  `MR_Cluster` objects.", 
	response = MR_Cluster.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 200, message = "Success", response = DR_Pub.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	public Response getMr_Clusters() {
		ApiService resp = new ApiService();

		List<MR_Cluster> clusters = mr_clusterService.getAllMr_Clusters();

		GenericEntity<List<MR_Cluster>> list = new GenericEntity<List<MR_Cluster>>(clusters) {
        };
        return resp.success(list);
	}
		
	@POST
	@ApiOperation( value = "return MR_Cluster details", 
	notes = "Create an  `MR_Cluster` object.", 
	response = MR_Cluster.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 200, message = "Success", response = DR_Pub.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	public Response  addMr_Cluster( 
			MR_Cluster cluster
			) {
		ApiService resp = new ApiService();

		try {
			resp.required( "dcaeLocationName", cluster.getDcaeLocationName(), "" );  
			resp.required( "fqdn", cluster.getFqdn(), "" );
		} catch( RequiredFieldException rfe ) {
			return resp.error();
		}
		MR_Cluster mrc =  mr_clusterService.addMr_Cluster(cluster, resp.getErr() );
		if ( mrc != null && mrc.isStatusValid() ) {
			return resp.success(Status.CREATED.getStatusCode(), mrc);
		}
		return resp.error();

	}
		
	@PUT
	@ApiOperation( value = "return MR_Cluster details", 
	notes = "Update an  `MR_Cluster` object, specified by clusterId.", 
	response = MR_Cluster.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 200, message = "Success", response = DR_Pub.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	@Path("/{clusterId}")
	public Response updateMr_Cluster( 
			@PathParam("clusterId") String clusterId, 
			MR_Cluster cluster
			) {
		ApiService resp = new ApiService();

		try {
			resp.required( "fqdn", clusterId, "" );
			resp.required( "dcaeLocationName", cluster.getDcaeLocationName(), "" );  
		} catch( RequiredFieldException rfe ) {
			return resp.error();
		}
		cluster.setDcaeLocationName(clusterId);
		MR_Cluster mrc =  mr_clusterService.updateMr_Cluster(cluster, resp.getErr() );
		if ( mrc != null && mrc.isStatusValid() ) {
			return resp.success(Status.CREATED.getStatusCode(), mrc);
		}
		return resp.error();
	}
		
	@DELETE
	@ApiOperation( value = "return MR_Cluster details", 
	notes = "Delete an  `MR_Cluster` object, specified by clusterId.", 
	response = MR_Cluster.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 204, message = "Success", response = DR_Pub.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	@Path("/{clusterId}")
	public Response deleteMr_Cluster( 
			@PathParam("clusterId") String id
			){
		ApiService resp = new ApiService();

		try {
			resp.required( "fqdn", id, "" );
		} catch( RequiredFieldException rfe ) {
			return resp.error();
		}
		mr_clusterService.removeMr_Cluster(id, resp.getErr() );
		if ( resp.getErr().is2xx()) {
			return resp.success(Status.NO_CONTENT.getStatusCode(), null);
		} 
		return resp.error();
	}

	@GET
	@ApiOperation( value = "return MR_Cluster details", 
	notes = "Retrieve an  `MR_Cluster` object, specified by clusterId.", 
	response = MR_Cluster.class)
	@ApiResponses( value = {
	    @ApiResponse( code = 200, message = "Success", response = DR_Pub.class),
	    @ApiResponse( code = 400, message = "Error", response = ApiError.class )
	})
	@Path("/{clusterId}")
	public Response getMR_Cluster( 
			@PathParam("clusterId") String id
			) {
		ApiService resp = new ApiService();

		try {
			resp.required( "dcaeLocationName", id, "" );
		} catch( RequiredFieldException rfe ) {
			return resp.error();
		}
		MR_Cluster mrc =  mr_clusterService.getMr_Cluster( id, resp.getErr() );
		if ( mrc != null && mrc.isStatusValid() ) {
			return resp.success(Status.CREATED.getStatusCode(), mrc);
		}
		return resp.error();
	}
}
