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

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public enum ReplicationType {
	REPLICATION_NOT_SPECIFIED(0),
	REPLICATION_NONE(1),
	REPLICATION_EDGE_TO_CENTRAL(10),
	REPLICATION_EDGE_TO_CENTRAL_TO_GLOBAL(110),
	REPLICATION_CENTRAL_TO_EDGE(20),
	REPLICATION_CENTRAL_TO_GLOBAL(21),
	REPLICATION_GLOBAL_TO_CENTRAL(30),
	REPLICATION_GLOBAL_TO_CENTRAL_TO_EDGE(120);

    private int value;
    private static Map map = new HashMap<>();

    private ReplicationType(int value) {
        this.value = value;
    }

    static {
        for (ReplicationType repType : ReplicationType.values()) {
            map.put(repType.value, repType);
        }
    }

    public static ReplicationType valueOf(int repType) {
        return (ReplicationType) map.get(repType);
    }

    public int getValue() {
        return value;
    }

    static public ReplicationType Validator( String input ){
   
		ReplicationType t;
		try {
			t = ReplicationType.valueOf( input );
		} catch ( IllegalArgumentException e ) {
			t = REPLICATION_NOT_SPECIFIED;
		}
		return t;
	}

	public boolean involvesGlobal() {
		if ( this.compareTo(REPLICATION_CENTRAL_TO_GLOBAL) == 0 ||
				this.compareTo(REPLICATION_GLOBAL_TO_CENTRAL) == 0 ||
				this.compareTo(REPLICATION_EDGE_TO_CENTRAL_TO_GLOBAL) == 0 ||
				this.compareTo(REPLICATION_GLOBAL_TO_CENTRAL_TO_EDGE) == 0) {
			return true;
		}
		return false;
	}



}