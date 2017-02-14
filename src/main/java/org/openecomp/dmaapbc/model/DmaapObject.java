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

package org.openecomp.dmaapbc.model;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public abstract class DmaapObject {
	protected Date lastMod;
	protected	DmaapObject_Status	status;
	
	public Date getLastMod() {
		return lastMod;
	}

	public void setLastMod(Date lastMod) {
		this.lastMod = lastMod;
	}

	public void setLastMod() {
		this.lastMod = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();
	}
	
	public enum DmaapObject_Status {
		EMPTY,
		NEW,
		STAGED,
		VALID,
		INVALID,
		DELETED
	}
	public DmaapObject_Status getStatus() {
		return status;
	}

	public void setStatus(DmaapObject_Status status) {
		this.status = status;
	}
	
	public boolean isStatusValid() {
		if ( this.status == DmaapObject_Status.VALID ) {
			return true;
		}
		return false;
	}
}
