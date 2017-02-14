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

package org.openecomp.dmaapbc.aaf;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.openecomp.dmaapbc.util.DmaapConfig;

public class AafService {
	public enum ServiceType {
		AAF_Admin,
		AAF_TopicMgr
	}
	static final Logger logger = Logger.getLogger(AafService.class);
	
	private AafConnection aaf;
	private ServiceType ctype;
	private String aafURL ;
	
	private String getCred( boolean wPwd ) {
		String mechIdProperty = null;
		String pwdProperty = null;
		DmaapConfig p = (DmaapConfig)DmaapConfig.getConfig();

		if ( ctype == ServiceType.AAF_Admin ) {
			 mechIdProperty = "aaf.AdminUser";
			 pwdProperty = "aaf.AdminPassword";
		} else if ( ctype == ServiceType.AAF_TopicMgr ){	
			 mechIdProperty = "aaf.TopicMgrUser";
			 pwdProperty = "aaf.TopicMgrPassword";
		} else {
			logger.error( "Unexpected case for AAF credential type: " + ctype );
			return null;
		}
		String user = p.getProperty( mechIdProperty, "noMechId@domain.netset.com" );
		String pwd = AndrewDecryptor.valueOf(p.getProperty( pwdProperty, "notSet" ));
		
		if ( wPwd ) {
			return user + ":" + pwd;
		} else {
			return user;
		}
		
		
	}
	
	public AafService(ServiceType t ) {
		DmaapConfig p = (DmaapConfig)DmaapConfig.getConfig();
		aafURL = p.getProperty( "aaf.URL", "https://authentication.domain.netset.com:8095/proxy/");
		ctype = t;
		aaf = new AafConnection( getCred( true ) );
	}
	
	public int addPerm(DmaapPerm perm) {

		int rc = -1;
		logger.info( "entry: setPerm() "  );
		String pURL = aafURL + "authz/perm";
		rc = aaf.postAaf( perm, pURL );
        switch( rc ) {
    	case 401:
    		logger.fatal( "Service credentials (" + getCred( false ) + ") are not valid for AAF connection");
    		break;
    	case 403:
    		logger.fatal( "Service credentials (" + getCred( false ) + ") are not authorized for requested action");
    		break;

    	case 409:
    		logger.warn( "Perm already exists. Possible conflict.");
    		break;
 		
    	case 201:
    		logger.info( "expected response" );
    		break;
       	default :
    		logger.error( "Unexpected response: " + rc );
    		break;
        }
		
		return rc;
	}
	public int addGrant(DmaapGrant grant ) {

		int rc = -1;
		logger.info( "entry: setPerm() "  );

		String pURL = aafURL + "authz/role/perm";
		rc = aaf.postAaf( grant, pURL );
        switch( rc ) {
    	case 401:
    		logger.fatal( "Service credentials (" + getCred( false ) + ") are not valid for AAF connection");
    		break;
    	case 403:
    		logger.fatal( "Service credentials (" + getCred( false ) + ") are not authorized for requested action");
    		break;

    	case 409:
    		logger.warn( "Perm already exists. Possible conflict.");
    		break;
 		
    	case 201:
    		logger.info( "expected response" );
    		break;
       	default :
    		logger.error( "Unexpected response: " + rc );
    		break;
        }
		
		return rc;
	}

}
