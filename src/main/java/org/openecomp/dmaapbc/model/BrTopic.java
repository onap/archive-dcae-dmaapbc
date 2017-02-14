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
public class BrTopic {
	static final Logger logger = Logger.getLogger(BrTopic.class);
	
	private String brSource;
	private String brTarget;
	private int topicCount;
	
	// no-op constructor used by framework
	public BrTopic() {
	}

	public String getBrSource() {
		return brSource;
	}

	public void setBrSource(String brSource) {
		this.brSource = brSource;
	}

	public String getBrTarget() {
		return brTarget;
	}

	public void setBrTarget(String brTarget) {
		this.brTarget = brTarget;
	}

	public int getTopicCount() {
		return topicCount;
	}

	public void setTopicCount(int topicCount) {
		this.topicCount = topicCount;
	}



}
