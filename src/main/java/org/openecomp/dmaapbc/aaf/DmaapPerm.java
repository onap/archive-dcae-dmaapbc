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

import org.apache.log4j.Logger;


public class DmaapPerm extends AafObject  {
	static final Logger logger = Logger.getLogger(DmaapPerm.class);
	
	private String 	permission;
	private	String	ptype;
	private	String	action;
	
	public DmaapPerm(String permission, String ptype, String action) {
		super();
		this.permission = permission;
		this.ptype = ptype;
		this.action = action;
	}
	public String getPermission() {
		return permission;
	}
	public void setPermission(String permission) {
		this.permission = permission;
	}
	public String getPtype() {
		return ptype;
	}
	public void setPtype(String ptype) {
		this.ptype = ptype;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String toJSON() {

		String postJSON = String.format(" { \"type\": \"%s\", \"instance\": \"%s\", \"action\": \"%s\"}", 
				this.getPermission(), 
				this.getPtype(),
				this.getAction() );
		logger.info( "returning JSON: " + postJSON);
			
		return postJSON;
	}
	
	
	
	
}
