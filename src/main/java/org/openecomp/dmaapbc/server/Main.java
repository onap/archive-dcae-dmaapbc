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
import java.net.InetAddress;
import java.util.Properties;

import java.util.UUID;

import org.openecomp.dmaapbc.authentication.ApiPerms;
import org.openecomp.dmaapbc.database.DatabaseClass;
import org.openecomp.dmaapbc.model.Dmaap;
import org.openecomp.dmaapbc.util.DmaapConfig;
import org.openecomp.dmaapbc.util.Singleton;
import org.openecomp.dmaapbc.logging.*;


import static com.att.eelf.configuration.Configuration.*;
import org.slf4j.MDC;

public class Main extends BaseLoggingClass {

	
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

    private void main()  {
    
        MDC.clear();

        MDC.put(MDC_SERVICE_INSTANCE_ID, "");
        try {
            MDC.put(MDC_SERVER_FQDN, InetAddress.getLocalHost().getHostName());
            MDC.put(MDC_SERVER_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }
        MDC.put(MDC_INSTANCE_UUID, UUID.randomUUID().toString());
        MDC.put(MDC_ALERT_SEVERITY, "0");


        MDC.put(MDC_TARGET_ENTITY, "DCAE");


/*
    	String msg = "This is a sample {} message to demo EELF logging.";
    
    	appLogger.info( msg, "appLogger.info");

    	auditLogger.auditEvent( msg, "auditLogger.auditEvent");
    	errorLogger.error(DmaapbcLogMessageEnum.MESSAGE_SAMPLE_NOARGS);
    	errorLogger.error(DmaapbcLogMessageEnum.MESSAGE_SAMPLE_ONE_ARG, "errorLogger.error");
    	errorLogger.error(DmaapbcLogMessageEnum.MESSAGE_SAMPLE_TWO_ARGS, new Date().toString(), "errorLogger.error" );
    	metricsLogger.metricsEvent( msg, "metricsLogger.metricsEvent" );
    	debugLogger.debug( msg, "debugLogger.debug");
    	
    	//String log = System.getProperty( "log4j.configuration");
    	//if ( log.isEmpty() ) {
    	//	log = "log4j.properties";
    	//}
        //PropertyConfigurator.configure( log );
         * 
         */
        appLogger.info("Started.");
        parameters = DmaapConfig.getConfig();
        setProvFQDN( parameters.getProperty("ProvFQDN", "ProvFQDN.notset.com"));
		
		
		// for fresh installs, we may come up with no dmaap name so need to have a way for Controller to talk to us
		Singleton<Dmaap> dmaapholder = DatabaseClass.getDmaap();
		String name = dmaapholder.get().getDmaapName();
		if ( name == null || name.isEmpty()) {
			ApiPerms p = new ApiPerms();
			p.setBootMap();
		}

    	
        try {
        	new JettyServer( parameters );
        } catch (Exception e) {
            errorLogger.error("Unable to start Jetty " + DmaapConfig.getConfigFileName(), e);
            System.exit(1);
        }

    }

}
