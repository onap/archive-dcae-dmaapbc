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

package org.openecomp.dmaapbc.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.dmaapbc.database.DatabaseClass;
import org.openecomp.dmaapbc.model.DcaeLocation;
import org.openecomp.dmaapbc.model.MR_Client;


public class Graph {
	private HashMap<String, String> graph;
	private boolean	hasCentral;
	
	private Map<String, DcaeLocation> locations = DatabaseClass.getDcaeLocations();
	
	//TODO add to properties file
	private static String centralDcaeLayerName = "central";

	
	public Graph(HashMap<String, String> graph) {
		super();
		this.graph = graph;
	}

	public Graph( List<MR_Client> clients, boolean strict ) {
		if ( clients == null )
			return;
		this.graph = new HashMap<String, String>();
		this.hasCentral = false;
		for( MR_Client client: clients ) {
			if ( ! strict || client.isStatusValid()) {
				String loc = client.getDcaeLocationName();
				for( String action : client.getAction() ){
					DcaeLocation dcaeLoc = locations.get(loc);
					if ( ! action.equals("view") && dcaeLoc != null ) {
						graph.put(loc, dcaeLoc.getDcaeLayer());
					}
				}
				String layer = graph.get(loc);
				if ( layer != null && layer.contains(centralDcaeLayerName) ) {
					this.hasCentral = true;
				}
	
			}		
		}		
	}
	
	public HashMap<String, String> getGraph() {
		return graph;
	}

	public void setGraph(HashMap<String, String> graph) {
		this.graph = graph;
	}
	
	public String put( String key, String val ) {
		return graph.put(key, val);
	}
	
	public String get( String key ) {
		return graph.get(key);
	}
	
	public Collection<String> getKeys() {
		return graph.keySet();
	}
	public boolean isHasCentral() {
		return hasCentral;
	}
	public void setHasCentral(boolean hasCentral) {
		this.hasCentral = hasCentral;
	}
	
	public String getCentralLoc() {
		if ( ! hasCentral ) {
			return null;
		}
		for( String loc : graph.keySet()) {
			if ( graph.get(loc).contains(centralDcaeLayerName)) {
				return loc;
			}
		}
		return null;
	}
	
	
}
