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

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

@XmlRootElement
public class DcaeLocation extends DmaapObject {
	static final Logger logger = Logger.getLogger(MR_Cluster.class);
	private String clli;
	private String dcaeLayer;
	private String dcaeLocationName;
	private String openStackAvailabilityZone;
	private String subnet;

	

	public DcaeLocation() {

	}

	public DcaeLocation( String c,
						String dL,
						String dLN,
						String oSAZ,
						String s ) {
		
		this.clli = c;
		this.dcaeLayer = dL;
		this.dcaeLocationName = dLN;
		this.openStackAvailabilityZone = oSAZ;
		this.subnet = s;
	}

	public String getClli() {
		return clli;
	}

	public void setClli(String clli) {
		this.clli = clli;
	}

	public String getDcaeLayer() {
		return dcaeLayer;
	}

	public void setDcaeLayer(String dcaeLayer) {
		this.dcaeLayer = dcaeLayer;
	}
	public boolean isCentral() {
		return dcaeLayer != null && dcaeLayer.contains("central");
	}
	public boolean isLocal() {
		return dcaeLayer != null && dcaeLayer.contains("local");
	}

	public String getDcaeLocationName() {
		return dcaeLocationName;
	}

	public void setDcaeLocationName(String dcaeLocationName) {
		this.dcaeLocationName = dcaeLocationName;
	}
	


	public String getOpenStackAvailabilityZone() {
		return openStackAvailabilityZone;
	}

	public void setOpenStackAvailabilityZone(String openStackAvailabilityZone) {
		this.openStackAvailabilityZone = openStackAvailabilityZone;
	}
	
	public String getSubnet() {
		return subnet;
	}

	public void setSubnet(String subnet) {
		this.subnet = subnet;
	}

}
