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

package org.openecomp.dmaapbc.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.openecomp.dmaapbc.aaf.AafDecrypt;
import org.openecomp.dmaapbc.aaf.AafService;
import org.openecomp.dmaapbc.aaf.AafService.ServiceType;
import org.openecomp.dmaapbc.aaf.DecryptionInterface;
import org.openecomp.dmaapbc.logging.BaseLoggingClass;
import org.openecomp.dmaapbc.logging.DmaapbcLogMessageEnum;
import org.openecomp.dmaapbc.model.ApiError;
import org.openecomp.dmaapbc.model.MR_Cluster;
import org.openecomp.dmaapbc.model.Topic;
import org.openecomp.dmaapbc.util.DmaapConfig;

public class MrProvConnection extends BaseLoggingClass{
	    
	private String provURL;
	
	private HttpsURLConnection uc;

	
	private String topicMgrCred;
	
	private String getCred( ) {
		String mechIdProperty = "aaf.TopicMgrUser";
		String pwdProperty = "aaf.TopicMgrPassword";
		DmaapConfig p = (DmaapConfig)DmaapConfig.getConfig();

		String user = p.getProperty( mechIdProperty, "noMechId@domain.netset.com" );

		String pwd = "";
		String encPwd = p.getProperty( pwdProperty, "notSet" );

		AafDecrypt decryptor = new AafDecrypt();
		pwd = decryptor.decrypt(encPwd);
	
		return user + ":" + pwd;
		
		
		
	}
	
	
	public MrProvConnection( ) {
		topicMgrCred =  getCred();

	}
	
	public boolean makeTopicConnection( MR_Cluster cluster ) {
		logger.info( "connect to cluster: " + cluster.getDcaeLocationName());
	

		provURL = cluster.getTopicProtocol() + "://" + cluster.getFqdn() + ":" + cluster.getTopicPort() + "/topics/create";

		return makeConnection( provURL );
	}

	private boolean makeConnection( String pURL ) {
		logger.info( "makeConnection to " + pURL );
	
		try {
			URL u = new URL( pURL );
			uc = (HttpsURLConnection) u.openConnection();
			uc.setInstanceFollowRedirects(false);
			logger.info( "successful connect to " + pURL );
			return(true);
		} catch( UnknownHostException uhe ){
        	logger.error( "Caught UnknownHostException for " + pURL);
        	return(false);
        } catch (Exception e) {
            logger.error("Unexpected error during openConnection of " + pURL );
            e.printStackTrace();
            return(false);
        } 

	}
	
	static String bodyToString( InputStream is ) {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader( new InputStreamReader(is));
		String line;
		try {
			while ((line = br.readLine()) != null ) {
				sb.append( line );
			}
		} catch (IOException ex ) {
			errorLogger.error( "IOexception:" + ex);
		}
			
		return sb.toString();
	}
	
	public String doPostTopic( Topic postTopic, ApiError err ) {
		String auth =  "Basic " + Base64.encodeBase64String(topicMgrCred.getBytes());


		String responsemessage = null;
		int rc = -1;


		try {
			byte[] postData = postTopic.getBytes();
			logger.info( "post fields=" + postData.toString() );
			uc.setRequestProperty("Authorization", auth);
			logger.info( "Authenticating with " + auth );
			uc.setRequestMethod("POST");
			uc.setRequestProperty("Content-Type", "application/json");
			uc.setRequestProperty( "charset", "utf-8");
			uc.setRequestProperty( "Content-Length", Integer.toString( postData.length ));
			uc.setUseCaches(false);
			uc.setDoOutput(true);
			OutputStream os = null;

			
			try {
                 uc.connect();
                 os = uc.getOutputStream();
                 os.write( postData );

            } catch (ProtocolException pe) {
                 // Rcvd error instead of 100-Continue
                 try {
                     // work around glitch in Java 1.7.0.21 and likely others
                     // without this, Java will connect multiple times to the server to run the same request
                     uc.setDoOutput(false);
                 } catch (Exception e) {
                 }
            } catch ( UnknownHostException uhe ) {
            	errorLogger.error( DmaapbcLogMessageEnum.UNKNOWN_HOST_EXCEPTION , uhe.getMessage() , provURL );
            	return new String( "500: " + uhe.getMessage());
            }catch ( ConnectException ce ) {
            	errorLogger.error( DmaapbcLogMessageEnum.HTTP_CONNECTION_EXCEPTION, provURL, ce.getMessage()  );
            	return new String( "500: " + ce.getMessage());
            }
			rc = uc.getResponseCode();
			logger.info( "http response code:" + rc );
			err.setCode(rc);
            responsemessage = uc.getResponseMessage();
            logger.info( "responsemessage=" + responsemessage );
            err.setMessage(responsemessage);


            if (responsemessage == null) {
                 // work around for glitch in Java 1.7.0.21 and likely others
                 // When Expect: 100 is set and a non-100 response is received, the response message is not set but the response code is
                 String h0 = uc.getHeaderField(0);
                 if (h0 != null) {
                     int i = h0.indexOf(' ');
                     int j = h0.indexOf(' ', i + 1);
                     if (i != -1 && j != -1) {
                         responsemessage = h0.substring(j + 1);
                     }
                 }
            }
            if (rc >= 200 && rc < 300 ) {
        		String responseBody = null;
     			responseBody = bodyToString( uc.getInputStream() );
    			logger.info( "responseBody=" + responseBody );
    			return responseBody;

            } 
            
		} catch (Exception e) {
            System.err.println("Unable to read response  " );
            e.printStackTrace();
        }
		finally {
			try {
				uc.disconnect();
			} catch ( Exception e ) {}
		}
		return new String( rc +": " + responsemessage );

	}
	


		
}
