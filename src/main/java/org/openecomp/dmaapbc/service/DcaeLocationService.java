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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;








import org.openecomp.dmaapbc.database.DatabaseClass;
import org.openecomp.dmaapbc.model.DcaeLocation;
import org.openecomp.dmaapbc.model.DmaapObject.DmaapObject_Status;

public class DcaeLocationService {
	
	private Map<String, DcaeLocation> dcaeLocations = DatabaseClass.getDcaeLocations();
	
	public Map<String, DcaeLocation> getDcaeLocations() {
		return dcaeLocations;
	}
	
	public List<DcaeLocation> getAllDcaeLocations() {
		return new ArrayList<DcaeLocation>(dcaeLocations.values());
	}
	
	public DcaeLocation getDcaeLocation( String name ) {
		return dcaeLocations.get(name);
	}

	public DcaeLocation addDcaeLocation( DcaeLocation location ) {
		location.setLastMod();
		location.setStatus(DmaapObject_Status.VALID);
		dcaeLocations.put( location.getDcaeLocationName(), location );
		return location;
	}
	
	public DcaeLocation updateDcaeLocation( DcaeLocation location ) {
		if ( location.getDcaeLocationName().isEmpty()) {
			return null;
		}
		location.setLastMod();
		dcaeLocations.put( location.getDcaeLocationName(), location );
		return location;
	}
	
	public DcaeLocation removeDcaeLocation( String locationName ) {
		return dcaeLocations.remove(locationName);
	}

	public String getCentralLocation() {
		for( Map.Entry<String, DcaeLocation> entry: dcaeLocations.entrySet() ) {
			DcaeLocation loc = entry.getValue();
			if ( loc.isCentral() ) {
				// use the name of the first central location we hit
				return loc.getDcaeLocationName();
			}
			
		}
		return "aCentralLocation";  // default value that is obvious to see is wrong
	}	

}
