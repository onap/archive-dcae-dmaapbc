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


import java.util.Properties;

import javax.net.ssl.SSLContext;

import org.apache.log4j.Logger;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
 
/**
 * A  Jetty server which supports:
 * 	- http and https (simultaneously for dev env)
 *  - REST API context
 *  - static html pages (for documentation).
 */
public class JettyServer {
	
	static final Logger logger = Logger.getLogger(JettyServer.class);

    public JettyServer( Properties params ) throws Exception {

        Server server = new Server();
    	int httpPort = Integer.valueOf(params.getProperty("IntHttpPort", "80" ));
       	int sslPort = Integer.valueOf(params.getProperty("IntHttpsPort", "443" ));
       	boolean allowHttp = Boolean.valueOf(params.getProperty("HttpAllowed", "false"));
    	logger.info( "port params: http=" + httpPort + " https=" + sslPort );
    	logger.info( "allowHttp=" + allowHttp );
        
        // HTTP Server

    	HttpConfiguration http_config = new HttpConfiguration();
    	http_config.setSecureScheme("https");
    	http_config.setSecurePort(sslPort);
    	http_config.setOutputBufferSize(32768);

    	
    	
        ServerConnector httpConnector = new ServerConnector(server, new HttpConnectionFactory(http_config));
        httpConnector.setPort(httpPort);  
        httpConnector.setIdleTimeout(30000);
  
        
        // HTTPS Server
 
        HttpConfiguration https_config = new HttpConfiguration(http_config);
        https_config.addCustomizer(new SecureRequestCustomizer());
        SslContextFactory sslContextFactory = new SslContextFactory();
        String keystore = params.getProperty("KeyStoreFile", "etc/keystore");
        logger.info( "https Server using keystore at " + keystore );
        String keystorePwd = params.getProperty( "KeyStorePassword", "changeit");
        String keyPwd = params.getProperty("KeyPassword", "changeit");
 

        sslContextFactory.setKeyStorePath(keystore);
        sslContextFactory.setKeyStorePassword(keystorePwd);
        sslContextFactory.setKeyManagerPassword(keyPwd);     

  
		ServerConnector sslConnector = null;
		if ( sslPort != 0 ) {
			sslConnector = new ServerConnector(server,
					new SslConnectionFactory(sslContextFactory, "http/1.1"),
					new HttpConnectionFactory(https_config));
			sslConnector.setPort(sslPort);
        	if ( allowHttp ) {
            	logger.info("Starting httpConnector on port " + httpPort );
            	logger.info("Starting sslConnector on port " +   sslPort + " for https");
        		server.setConnectors( new Connector[] { httpConnector, sslConnector });
        	} else {
            	logger.info("NOT starting httpConnector because HttpAllowed param is " + allowHttp  );	
            	logger.info("Starting sslConnector on port " +   sslPort + " for https");
          		server.setConnectors( new Connector[] { sslConnector });     	
        	}
		}
		else {
            logger.info("NOT starting sslConnector on port " +   sslPort + " for https");
        	if ( allowHttp ) {
            	logger.info("Starting httpConnector on port " + httpPort );
        		server.setConnectors( new Connector[] { httpConnector });
			} 
        } 
 
        // Set context for servlet.  This is shared for http and https
       	ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    	context.setContextPath("/");
        server.setHandler( context );

        ServletHolder jerseyServlet = context.addServlet( org.glassfish.jersey.servlet.ServletContainer.class, "/webapi/*");
        jerseyServlet.setInitOrder(1);
        jerseyServlet.setInitParameter("jersey.config.server.provider.packages", "org.openecomp.dmaapbc.resources" );    
        
        // also serve up some static pages...
        ServletHolder staticServlet = context.addServlet(DefaultServlet.class,"/*");
        staticServlet.setInitParameter("resourceBase","www");
        staticServlet.setInitParameter("pathInfoOnly","true");

        try {

            logger.info("Starting jetty server");
        	server.start();
        	server.dumpStdErr();
            server.join();
        } catch ( Exception e ) {
        	logger.error( "Exception " + e );
        	logger.error( "possibly unable to use keystore " + keystore + " with passwords " + keystorePwd +  " and " + keyPwd );
        	//System.exit(1);
        } finally {
        	server.destroy();
        }
        
    }
}
