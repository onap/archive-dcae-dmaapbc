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
import org.apache.log4j.Logger;

public class ReplicationVector {
	


	static final Logger logger = Logger.getLogger(ReplicationVector.class);
	public enum ReplicationVector_Status {
		EMPTY,
		NEW,
		STAGED,
		VALID,
		INVALID,
		INVALID_DUP,
		DELETED
	}

	String 	fqtn;
	String	sourceCluster;
	String	targetCluster;
	ReplicationVector_Status status;
	
	public ReplicationVector(){
		
	}
	
	public ReplicationVector(String fqtn, String sourceCluster,
			String targetCluster) {
		super();
		this.fqtn = fqtn;
		this.sourceCluster = sourceCluster;
		this.targetCluster = targetCluster;
	}

	public String getFqtn() {
		return fqtn;
	}

	public void setFqtn(String fqtn) {
		this.fqtn = fqtn;
	}

	public String getSourceCluster() {
		return sourceCluster;
	}

	public void setSourceCluster(String sourceCluster) {
		this.sourceCluster = sourceCluster;
	}

	public String getTargetCluster() {
		return targetCluster;
	}

	public void setTargetCluster(String targetCluster) {
		this.targetCluster = targetCluster;
	}
	
	public int hashCode() {
		StringBuilder tmp = new StringBuilder( this.fqtn );
		tmp.append(this.sourceCluster);
		tmp.append(this.targetCluster);
		
		return tmp.toString().hashCode();
	}
	private static boolean xeq(String s1, String s2) {
		if (s1 == null) {
			return(s2 == null);
		} else {
			return(s1.equals(s2));
		}
	}
	public boolean equals(Object o) {
		if (o == this) {
			return(true);
		}
		if (!(o instanceof ReplicationVector)) {
			return(false);
		}
		ReplicationVector x = (ReplicationVector)o;
		return(xeq(fqtn, x.fqtn) && xeq(sourceCluster, x.sourceCluster) && xeq(targetCluster, x.targetCluster));
	}
}
