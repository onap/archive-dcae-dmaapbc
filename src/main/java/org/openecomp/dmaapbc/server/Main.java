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

package org.openecomp.dmaapbc.server;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.openecomp.dmaapbc.util.DmaapConfig;

public class Main {
	
    private Properties parameters;
    private static String provFQDN;

    public static String getProvFQDN() {
		return provFQDN;
	}
	public void setProvFQDN(String provFQDN) {
		Main.provFQDN = provFQDN;
	}
	private Main() {
    }
    public static void main(String[] args) throws Exception {
        (new Main()).main();
    }

	static final Logger logger = Logger.getLogger(Main.class);	   
    private void main()  {
    	String log = System.getProperty( "log4j.configuration");
    	if ( log.isEmpty() ) {
    		log = "log4j.properties";
    	}
        PropertyConfigurator.configure( log );
        logger.info("Started.");
        parameters = DmaapConfig.getConfig();
        setProvFQDN( parameters.getProperty("ProvFQDN", "ProvFQDN.notset.com"));

    	
        try {
        	//new JettyServer( Integer.valueOf(parameters.getProperty("IntHttpPort", "80" )),
        		//	Integer.valueOf(parameters.getProperty("IntHttpsPort","443")));
        	new JettyServer( parameters );
        } catch (Exception e) {
            logger.fatal("Unable to start Jetty " + DmaapConfig.getConfigFileName(), e);
            System.exit(1);
        }

    }

}
