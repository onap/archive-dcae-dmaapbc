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

package org.openecomp.dmaapbc.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;
import org.openecomp.dmaapbc.aaf.DmaapPerm;
import org.openecomp.dmaapbc.authentication.AuthenticationErrorException;
import org.openecomp.dmaapbc.authentication.DecisionPolicy;
import org.openecomp.dmaapbc.model.ApiError;
import org.openecomp.dmaapbc.model.Dmaap;
import org.openecomp.dmaapbc.resources.RequiredFieldException;
import org.openecomp.dmaapbc.util.DmaapConfig;

public class ApiService {
	static final Logger logger = Logger.getLogger(ApiService.class);
	static private String apiNamespace;
	static private boolean usePE;

	private ApiError err;
	
	public ApiService() {
		err = new ApiError();
		
		if (apiNamespace == null) {
			DmaapConfig p = (DmaapConfig)DmaapConfig.getConfig();
			usePE = "true".equalsIgnoreCase(p.getProperty("UsePE", "false"));
			apiNamespace = p.getProperty("ApiNamespace", "org.openecomp.dmaapBC.api");
		}
		
	}
	
	
	public ApiError getErr() {
		return err;
	}


	public void setErr(ApiError err) {
		this.err = err;
	}


	// test for presence of a required field
	public void required( String name, Object val, String expr ) throws RequiredFieldException {
		if ( val == null  ) {
			err.setCode(Status.BAD_REQUEST.getStatusCode());
			err.setMessage("missing required field");
			err.setFields( name );	
			throw new RequiredFieldException();
		}
		if ( expr != null && ! expr.isEmpty() ) {
			Pattern pattern = Pattern.compile(expr);
			Matcher matcher = pattern.matcher((CharSequence) val);
			if ( ! matcher.find() ) {
				err.setCode(Status.BAD_REQUEST.getStatusCode());
				err.setMessage( "value '" + val + "' violates regexp check '" + expr + "'");
				err.setFields( name );
				throw new RequiredFieldException();
			}
		}
	}
	
	// utility to serialize ApiErr object
	public String toString() {
		return String.format( "code=%d msg=%s fields=%s", err.getCode(), err.getMessage(), err.getFields() );
	}


	public void setCode(int statusCode) {
		err.setCode(statusCode);	
	}


	public void setMessage(String string) {
		err.setMessage(string);
	}


	public void setFields(String string) {
		err.setFields(string);
	}

	private Response  buildResponse() {
		return Response.status( err.getCode())
				.entity(getErr())
				.build();
	}
	public Response response(int statusCode) {
		err.setCode(statusCode);
		return buildResponse();
	}
	public Response unauthorized() {
		err.setCode(Status.UNAUTHORIZED.getStatusCode());
		err.setFields( "Authorization");
		err.setMessage( "User credentials in HTTP Header field Authorization are not authorized for the requested action");
		return buildResponse();
	}
	public Response unavailable() {
		err.setCode(Status.SERVICE_UNAVAILABLE.getStatusCode());
		err.setMessage( "Request is unavailable due to unexpected condition");
		return buildResponse();
	}
	
	public void checkAuthorization( String authorization, String uriPath, String method ) throws AuthenticationErrorException, Exception {
		if ( ! usePE ) return;  // skip authorization if not enabled
		if ( authorization == null || authorization.isEmpty()) {
			String errmsg = "No basic authorization value provided ";
			logger.info( errmsg );
			throw new AuthenticationErrorException( );
		}
		if ( uriPath == null || uriPath.isEmpty()) {
			String errmsg = "No URI value provided ";
			logger.info( errmsg );
			throw new AuthenticationErrorException( );			
		}
		if ( method == null || method.isEmpty()) {
			String errmsg = "No method value provided ";
			logger.info( errmsg );
			throw new AuthenticationErrorException( );			
		}
		DmaapService dmaapService = new DmaapService();
		Dmaap dmaap = dmaapService.getDmaap();
		String env = 		dmaap.getDmaapName();
		
		// special case during bootstrap of app when DMaaP environment may not be set.
		// this allows us to authorize certain APIs used for initialization during this window.
		if ( env.isEmpty() ) {
			env = "boot";
		}

        String credentials = authorization.substring("Basic".length()).trim();
        byte[] decoded = DatatypeConverter.parseBase64Binary(credentials);
        String decodedString = new String(decoded);
        String[] actualCredentials = decodedString.split(":");
        String ID = actualCredentials[0];
        String Password = actualCredentials[1];
		
logger.info( "User " + ID + " allowed - DecisionPolicy() not compiled in yet!" );
/* disable until PolicyEngine avail...
		try {
			DecisionPolicy d = new DecisionPolicy();
			DmaapPerm p = new DmaapPerm( apiNamespace + "." + uriPath, env, method );
			d.check( ID, Password, p);
		} catch ( AuthenticationErrorException ae ) {
			logger.info( "User " + ID + " failed authentication/authorization");
			throw ae;

		} 
*/
	}
}
