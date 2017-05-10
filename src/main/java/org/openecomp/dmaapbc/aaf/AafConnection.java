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





import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.codec.binary.Base64;
import org.openecomp.dmaapbc.logging.BaseLoggingClass;
import org.openecomp.dmaapbc.logging.DmaapbcLogMessageEnum;
import org.openecomp.dmaapbc.service.DmaapService;


public class AafConnection extends BaseLoggingClass {


	   
   

	private String aafCred;

	
	private HttpsURLConnection uc;


	public AafConnection( String cred ) {
		aafCred = cred;
	}
	

	private boolean makeConnection( String pURL ) {
	
		try {
			URL u = new URL( pURL );
			uc = (HttpsURLConnection) u.openConnection();
			uc.setInstanceFollowRedirects(false);
			logger.info( "successful connect to " + pURL );
			return(true);
		} catch (Exception e) {
	        errorLogger.error(DmaapbcLogMessageEnum.HTTP_CONNECTION_ERROR,  pURL, e.getMessage() );
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
			errorLogger.error( DmaapbcLogMessageEnum.IO_EXCEPTION,  ex.getMessage());
		}
			
		return sb.toString();
	}
	


	public int postAaf( AafObject obj, String pURL ) {
		logger.info( "entry: postAaf() to  " + pURL  );
		String auth =  "Basic " + Base64.encodeBase64String(aafCred.getBytes());
		int rc = -1;

		
		if ( ! makeConnection( pURL ) ) {
			return rc;
		};
		

		byte[] postData = obj.getBytes();
		//logger.info( "post fields=" + postData );  //byte isn't very readable
		String responsemessage = null;
		String responseBody = null;

		try {
			if (auth != null) {
				uc.setRequestProperty("Authorization", auth);
	        }
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
            } catch ( SSLHandshakeException she ) {
               	errorLogger.error( DmaapbcLogMessageEnum.SSL_HANDSHAKE_ERROR, pURL);
            } 
			try {
				rc = uc.getResponseCode();
			} catch ( SSLHandshakeException she ) {
				errorLogger.error( DmaapbcLogMessageEnum.SSL_HANDSHAKE_ERROR, pURL);
            	rc = 500;
            	return rc;
            }
			logger.info( "http response code:" + rc );
            responsemessage = uc.getResponseMessage();
            logger.info( "responsemessage=" + responsemessage );

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
            if ( rc >= 200 && rc < 300 ) {
            	responseBody = bodyToString( uc.getInputStream() );
            	logger.info( "responseBody=" + responseBody );
            } else {
            		logger.warn( "Unsuccessful response: " + responsemessage );
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
		//return responseBody;
	
		return rc;
		
	}
	
	public int delAaf(AafObject obj, String pURL) {
		logger.info( "entry: delAaf() to  " + pURL  );
		String auth =  "Basic " + Base64.encodeBase64String(aafCred.getBytes());
		int rc = -1;

		
		if ( ! makeConnection( pURL ) ) {
			return rc;
		};
		

		byte[] postData = obj.getBytes();
		//logger.info( "post fields=" + postData );  //byte isn't very readable
		String responsemessage = null;
		String responseBody = null;

		try {
			if (auth != null) {
				uc.setRequestProperty("Authorization", auth);
	        }
			uc.setRequestMethod("DELETE");
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
            } catch ( SSLHandshakeException she ) {
            	errorLogger.error( DmaapbcLogMessageEnum.SSL_HANDSHAKE_ERROR, pURL);
            }
			try {
				rc = uc.getResponseCode();
			} catch ( SSLHandshakeException she ) {
				errorLogger.error( DmaapbcLogMessageEnum.SSL_HANDSHAKE_ERROR, pURL);
            	rc = 500;
            	return rc;
            }
			logger.info( "http response code:" + rc );
            responsemessage = uc.getResponseMessage();
            logger.info( "responsemessage=" + responsemessage );

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
            if ( rc >= 200 && rc < 300 ) {
            	responseBody = bodyToString( uc.getInputStream() );
            	logger.info( "responseBody=" + responseBody );
            } else {
            		logger.warn( "Unsuccessful response: " + responsemessage );
            } 
            
		} catch (Exception e) {
            System.err.println("Unable to read response  " );
            e.printStackTrace();
        }
		//return responseBody;
	
		return rc;
		
	}
	

}
