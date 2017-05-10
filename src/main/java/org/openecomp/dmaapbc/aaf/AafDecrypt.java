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

import java.io.IOException;

import org.openecomp.dmaapbc.logging.BaseLoggingClass;
import org.openecomp.dmaapbc.logging.DmaapbcLogMessageEnum;
import org.openecomp.dmaapbc.util.DmaapConfig;

public class AafDecrypt extends BaseLoggingClass  {
	String dClass = null;
	DecryptionInterface dec = null;
	
	public AafDecrypt() {
		DmaapConfig p = (DmaapConfig)DmaapConfig.getConfig();
		dClass = p.getProperty( "AafDecryption.Class", "org.openecomp.dmaapbc.aaf.ClearDecrypt");
		try {
			dec = (DecryptionInterface) (Class.forName(dClass).newInstance());	
			dec.init( p.getProperty("CredentialCodecKeyfile", "LocalKey"));
		} catch (Exception ee ) {
			errorLogger.error(DmaapbcLogMessageEnum.UNEXPECTED_CONDITION, "attempting to instantiate " + dClass  );		
		}	
	}
	
	public String decrypt( String encPwd ) {
	
		String pwd = "notDecrypted";
		try {		
			pwd = dec.decrypt( encPwd );
		} catch( IOException io ) {
			errorLogger.error(DmaapbcLogMessageEnum.DECRYPT_IO_ERROR, dClass, encPwd );
		} 
		
		return pwd;
	
	}

}
