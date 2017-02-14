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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response.Status;

import org.openecomp.dmaapbc.model.ApiError;

//TODO: Retire this class
public class ApiResource {
	
	static void checkRequired( String name, Object val, String expr, ApiError err ) throws RequiredFieldException {
		if ( val == null ) {
			err.setCode(Status.BAD_REQUEST.getStatusCode());
			err.setMessage("missing required field");
			err.setFields( name );	
			throw new RequiredFieldException();
		}
		

	}
	

}
