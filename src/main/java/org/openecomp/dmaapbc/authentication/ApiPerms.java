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

package org.openecomp.dmaapbc.authentication;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.openecomp.dmaapbc.aaf.AafService;
import org.openecomp.dmaapbc.aaf.DmaapGrant;
import org.openecomp.dmaapbc.aaf.DmaapPerm;
import org.openecomp.dmaapbc.aaf.AafService.ServiceType;
import org.openecomp.dmaapbc.logging.BaseLoggingClass;
import org.openecomp.dmaapbc.logging.DmaapbcLogMessageEnum;
import org.openecomp.dmaapbc.model.Dmaap;
import org.openecomp.dmaapbc.service.DmaapService;
import org.openecomp.dmaapbc.util.DmaapConfig;

public  class ApiPerms extends BaseLoggingClass {
	
	private static class PermissionMap {
		static final EELFLogger logger = EELFManager.getInstance().getLogger( PermissionMap.class );
		static final EELFLogger errorLogger = EELFManager.getInstance().getErrorLogger();
		String uri;
		String action;
		String[] roles;
		
		public String getUri() {
			return uri;
		}
		public void setUri(String uri) {
			this.uri = uri;
		}
		public String getAction() {
			return action;
		}
		public void setAction(String action) {
			this.action = action;
		}

		public String[] getRoles() {
			return roles;
		}
		public void setRoles(String[] roles) {
			this.roles = roles;
		}

		private PermissionMap( String u, String a, String[] r ) {
			this.setUri(u);
			this.setAction(a);
			this.setRoles(r);
		}
		
		static public void initMap( PermissionMap[] pmap, String instance ) {

			DmaapConfig p = (DmaapConfig)DmaapConfig.getConfig();
			String api = p.getProperty("ApiNamespace", "apiNamespace.not.set");
			
			// this is needed because PE AAF may be a different instance than AAF used by MR
			String peEnv = p.getProperty("PeAafEnvironment", "notSet");
			String url = p.getProperty( new String( "PeAafUrl." + peEnv ), "URL.not.set" );
			logger.info( "PeAafEnvironment=" + peEnv + " using URL " + url);
			AafService aaf = new AafService(ServiceType.AAF_Admin, url );
			
			for ( int i = 0; i < pmap.length ; i++ ) {
				String uri = new String( api + "." + pmap[i].getUri());
				DmaapPerm perm = new DmaapPerm( uri, instance, pmap[i].getAction() );
				int rc = aaf.addPerm( perm );
				if ( rc != 201 &&  rc != 409 ) {
					errorLogger.error( DmaapbcLogMessageEnum.AAF_UNEXPECTED_RESPONSE,  Integer.toString(rc), "add perm",  perm.toString() );

				}
				for( String r: pmap[i].getRoles()) {
					String fr = new String( api + "." + r );
					logger.debug( "i:" + i + " granting perm " + perm.toString()+ " to role=" + fr );
					DmaapGrant grant = new DmaapGrant( perm, fr );
					rc = aaf.addGrant( grant );
					if ( rc != 201 && rc != 409 ) {
						errorLogger.error( DmaapbcLogMessageEnum.AAF_UNEXPECTED_RESPONSE,  Integer.toString(rc), "grant perm",  perm.toString() );
					}
				}
				
			}
		}
	}
	
	static PermissionMap[] bootMap = {
		new PermissionMap( "dmaap", "GET", new String[] { "Controller" }),
		new PermissionMap( "dmaap", "POST", new String[] { "Controller" }),	
		new PermissionMap( "dmaap", "PUT", new String[] { "Controller" }),
		new PermissionMap( "dmaap", "DELETE", new String[] { "Controller" })
	
	};

	static PermissionMap[] envMap = {
		new PermissionMap( "dmaap", "GET", new String[] { "Controller", "Orchestrator", "Inventory", "Metrics", "PortalUser" }),
		new PermissionMap( "dmaap", "POST", new String[] { "Controller" } ),		
		new PermissionMap( "dmaap", "PUT", new String[] { "Controller" }),
		new PermissionMap( "dmaap", "DELETE", new String[] { "Controller" }),
		new PermissionMap( "bridge", "GET", new String[] {  "Metrics" }),
		//new PermissionMap( "bridge", "POST", new String[] { "Metrics" } ),		
		//new PermissionMap( "bridge", "PUT", new String[] { "Metrics" }),
		//new PermissionMap( "bridge", "DELETE", new String[] { "Metrics" }),
		new PermissionMap( "dcaeLocations", "GET", new String[] { "Controller", "Orchestrator", "Inventory", "Metrics", "PortalUser" }),
		new PermissionMap( "dcaeLocations", "POST", new String[] { "Controller" } ),		
		new PermissionMap( "dcaeLocations", "PUT", new String[] { "Controller" }),
		new PermissionMap( "dcaeLocations", "DELETE", new String[] { "Controller" }),
		new PermissionMap( "dr_nodes", "GET", new String[] { "Controller", "Orchestrator", "Inventory",  "PortalUser" }),
		new PermissionMap( "dr_nodes", "POST", new String[] { "Controller" } ),		
		new PermissionMap( "dr_nodes", "PUT", new String[] { "Controller" }),
		new PermissionMap( "dr_nodes", "DELETE", new String[] { "Controller" }),
		new PermissionMap( "dr_pubs", "GET", new String[] { "Controller", "Orchestrator", "Inventory", "Metrics", "PortalUser" }),
		new PermissionMap( "dr_pubs", "POST", new String[] { "Controller", "Orchestrator","PortalUser" } ),		
		new PermissionMap( "dr_pubs", "PUT", new String[] { "Controller", "Orchestrator","PortalUser" }),
		new PermissionMap( "dr_pubs", "DELETE", new String[] { "Controller", "Orchestrator","PortalUser" }),
		new PermissionMap( "dr_subs", "GET", new String[] { "Controller", "Orchestrator", "Inventory", "Metrics", "PortalUser" }),
		new PermissionMap( "dr_subs", "POST", new String[] { "Controller", "Orchestrator","PortalUser" } ),		
		new PermissionMap( "dr_subs", "PUT", new String[] { "Controller", "Orchestrator","PortalUser" }),
		new PermissionMap( "dr_subs", "DELETE", new String[] { "Controller", "Orchestrator","PortalUser" }),
		new PermissionMap( "feeds", "GET", new String[] { "Controller", "Orchestrator", "Inventory", "Metrics", "PortalUser" }),
		new PermissionMap( "feeds", "POST", new String[] { "Controller", "Orchestrator","PortalUser" } ),		
		new PermissionMap( "feeds", "PUT", new String[] { "Controller", "Orchestrator", "PortalUser" }),
		new PermissionMap( "feeds", "DELETE", new String[] { "Controller", "PortalUser" }),
		new PermissionMap( "mr_clients", "GET", new String[] { "Controller", "Orchestrator", "Inventory", "Metrics", "PortalUser" }),
		new PermissionMap( "mr_clients", "POST", new String[] { "Controller","Orchestrator", "PortalUser" } ),		
		new PermissionMap( "mr_clients", "PUT", new String[] { "Controller", "Orchestrator","PortalUser" }),
		new PermissionMap( "mr_clients", "DELETE", new String[] { "Controller","Orchestrator", "PortalUser" }),
		new PermissionMap( "mr_clusters", "GET", new String[] { "Controller", "Orchestrator", "Inventory", "Metrics", "PortalUser" }),
		new PermissionMap( "mr_clusters", "POST", new String[] { "Controller" } ),		
		new PermissionMap( "mr_clusters", "PUT", new String[] { "Controller" }),
		new PermissionMap( "mr_clusters", "DELETE", new String[] { "Controller" }),
		new PermissionMap( "topics", "GET", new String[] { "Controller", "Orchestrator", "Inventory", "Metrics", "PortalUser" }),
		new PermissionMap( "topics", "POST", new String[] { "Controller", "Orchestrator" } ),		
		new PermissionMap( "topics", "PUT", new String[] { "Controller", "Orchestrator" }),
		new PermissionMap( "topics", "DELETE", new String[] { "Controller", "Orchestrator" })
	};
	
	public void setBootMap() {
		String instance = "boot";
		PermissionMap.initMap( bootMap, instance );
	}
	
	public void setEnvMap() {
		Dmaap dmaap = new DmaapService().getDmaap();
		String dmaap_name = dmaap.getDmaapName();
		PermissionMap.initMap( envMap, dmaap_name );
	}
	

}
