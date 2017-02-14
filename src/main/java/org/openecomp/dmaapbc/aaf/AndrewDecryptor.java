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

import java.io.*;

import org.openecomp.dmaapbc.util.DmaapConfig;

// Commented out this package and the code that uses it until it becomes available.

//import com.xxx.cadi.*;

public class AndrewDecryptor	{
	private static AndrewDecryptor instance;
	//private Symm symm;

	private AndrewDecryptor() {
		InputStream is = null;
		try {
			is = new FileInputStream(DmaapConfig.getConfig().getProperty("CredentialCodecKeyfile", "LocalKey"));
			//symm = Symm.obtain(is);
		} catch (Exception e) {
		} finally {
			try { 
				if (is != null) {
					is.close();
				}
			} catch (Exception x) {
				
			}
		}
	}
	public String decrypt(String enc) throws IOException {
		return(enc);
		//return((enc != null && enc.startsWith(Symm.ENC))? symm.depass(enc): enc);
	}
	public static AndrewDecryptor getInstance() {
		if (instance == null) {
			instance = new AndrewDecryptor();
		}
		return(instance);
	}
	public static String valueOf(String enc) {
		try {
			return(getInstance().decrypt(enc));
		} catch (Exception e) {
			return(null);
		}
	}
}
