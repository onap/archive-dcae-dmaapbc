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
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.openecomp.dmaapbc.logging.BaseLoggingClass;
import org.openecomp.dmaapbc.logging.DmaapbcLogMessageEnum;
import org.openecomp.dmaapbc.model.ApiError;
import org.openecomp.dmaapbc.model.DR_Pub;
import org.openecomp.dmaapbc.model.DR_Sub;
import org.openecomp.dmaapbc.model.Feed;
import org.openecomp.dmaapbc.service.DmaapService;
import org.openecomp.dmaapbc.util.RandomInteger;



public class DrProvConnection extends BaseLoggingClass {
	   
   
	private String provURL;
	
	private HttpsURLConnection uc;


	public DrProvConnection() {
		provURL = new DmaapService().getDmaap().getDrProvUrl();
		if ( provURL.length() < 1 ) {
			errorLogger.error( DmaapbcLogMessageEnum.PREREQ_DMAAP_OBJECT, "getDrProvUrl");
		}
			
	}
	
	public boolean makeFeedConnection() {
		return makeConnection( provURL );
	}
	public boolean makeFeedConnection(String feedId) {
		return makeConnection( provURL + "/feed/" + feedId );	
	}
	public boolean makeSubPostConnection( String subURL ) {
		String[] parts = subURL.split("/");
		String revisedURL = provURL + "/" + parts[3] + "/" + parts[4];
		logger.info( "mapping " + subURL + " to " + revisedURL );
		return makeConnection( revisedURL );
	}
	public boolean makeSubPutConnection( String subId ) {
		String revisedURL = provURL + "/subs/" + subId;
		logger.info( "mapping " + subId + " to " + revisedURL );
		return makeConnection( revisedURL );
	}

	public boolean makeIngressConnection( String feed, String user, String subnet, String nodep ) {
		String uri = String.format("/internal/route/ingress/?feed=%s&user=%s&subnet=%s&nodepatt=%s", 
					feed, user, subnet, nodep );
		return makeConnection( provURL + uri );
	}
	public boolean makeEgressConnection( String sub, String nodep ) {
		String uri = String.format("/internal/route/egress/?sub=%s&node=%s", 
					sub,  nodep );
		return makeConnection( provURL + uri );
	}
	public boolean makeNodesConnection( String varName ) {
		
		String uri = String.format("/internal/api/%s", varName);
		return makeConnection( provURL + uri );
	}
	
	public boolean makeNodesConnection( String varName, String val ) {

		if ( val == null ) {
			return false;
		} 
		String cv = val.replaceAll("\\|", "%7C");
		String uri = String.format( "/internal/api/%s?val=%s", varName, cv );

		return makeConnection( provURL + uri );
	}
	
	private boolean makeConnection( String pURL ) {
	
		try {
			URL u = new URL( pURL );
			uc = (HttpsURLConnection) u.openConnection();
			uc.setInstanceFollowRedirects(false);
			logger.info( "successful connect to " + pURL );
			return(true);
		} catch (Exception e) {
			errorLogger.error( DmaapbcLogMessageEnum.HTTP_CONNECTION_ERROR,  pURL, e.getMessage() );
            e.printStackTrace();
            return(false);
        }

	}
	
	private String bodyToString( InputStream is ) {
		logger.info( "is=" + is );
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader( new InputStreamReader(is));
		String line;
		try {
			while ((line = br.readLine()) != null ) {
				sb.append( line );
			}
		} catch (IOException ex ) {
			errorLogger.error( DmaapbcLogMessageEnum.IO_EXCEPTION, ex.getMessage());
		}
			
		return sb.toString();
	}
	

	public  String doPostFeed( Feed postFeed, ApiError err ) {

		byte[] postData = postFeed.getBytes();
		logger.info( "post fields=" + postData.toString() );
		String responsemessage = null;
		String responseBody = null;

		try {
			logger.info( "uc=" + uc );
			uc.setRequestMethod("POST");
			uc.setRequestProperty("Content-Type", "application/vnd.att-dr.feed");
			uc.setRequestProperty( "charset", "utf-8");
			uc.setRequestProperty( "X-ATT-DR-ON-BEHALF-OF", postFeed.getOwner() );
			uc.setRequestProperty( "Content-Length", Integer.toString( postData.length ));
			uc.setUseCaches(false);
			uc.setDoOutput(true);
			OutputStream os = null;
			int rc = -1;
			
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
            }
			rc = uc.getResponseCode();
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
            if (rc == 201 ) {
     			responseBody = bodyToString( uc.getInputStream() );
    			logger.info( "responseBody=" + responseBody );

            } else {
            	err.setCode( rc );
            	err.setMessage(responsemessage);
            }
            
		} catch (ConnectException ce) {
			errorLogger.error(DmaapbcLogMessageEnum.HTTP_CONNECTION_EXCEPTION, provURL, ce.getMessage() );
            err.setCode( 500 );
        	err.setMessage("Backend connection refused");
		} catch (SocketException se) {
			errorLogger.error( DmaapbcLogMessageEnum.SOCKET_EXCEPTION, se.getMessage(), "response from prov server" );
			err.setCode( 500 );
			err.setMessage( "Unable to read response from DR");
        } catch (Exception e) {
            logger.warn("Unable to read response  " );
            e.printStackTrace();
            try {
	            err.setCode( uc.getResponseCode());
	            err.setMessage(uc.getResponseMessage());
            } catch (Exception e2) {
            	err.setCode( 500 );
            	err.setMessage("Unable to determine response message");
            }
        } 
		finally {
			try {
				uc.disconnect();
			} catch ( Exception e ) {}
		}
		return responseBody;

	}

	
	// the POST for /internal/route/ingress doesn't return any data, so needs a different function
	// the POST for /internal/route/egress doesn't return any data, so needs a different function	
	public int doXgressPost( ApiError err ) {
		
		String responsemessage = null;
		int rc = -1;

		try {
			uc.setRequestMethod("POST");
//			uc.setRequestProperty("Content-Type", "application/vnd.att-dr.feed");
//			uc.setRequestProperty( "charset", "utf-8");
//			uc.setRequestProperty( "X-ATT-DR-ON-BEHALF-OF", postFeed.getOwner() );
//			uc.setRequestProperty( "Content-Length", Integer.toString( postData.length ));
//			uc.setUseCaches(false);
//			uc.setDoOutput(true);	
			OutputStream os = null;
	
			
			try {
                 uc.connect();
                 os = uc.getOutputStream();


            } catch (ProtocolException pe) {
                 // Rcvd error instead of 100-Continue
                 try {
                     // work around glitch in Java 1.7.0.21 and likely others
                     // without this, Java will connect multiple times to the server to run the same request
                     uc.setDoOutput(false);
                 } catch (Exception e) {
                 }
            }
			rc = uc.getResponseCode();
			logger.info( "http response code:" + rc );
            responsemessage = uc.getResponseMessage();
            logger.info( "responsemessage=" + responsemessage );



            if (rc < 200 || rc >= 300 ) {
            	err.setCode( rc );
            	err.setMessage(responsemessage);
            }
		} catch (Exception e) {
            System.err.println("Unable to read response  " );
            e.printStackTrace();
        }		finally {
			try {
				uc.disconnect();
			} catch ( Exception e ) {}
		}
	
		return rc;

	}
	
	public String doPostDr_Sub( DR_Sub postSub, ApiError err ) {
		logger.info( "entry: doPostDr_Sub() "  );
		byte[] postData = postSub.getBytes();
		logger.info( "post fields=" + postData );
		String responsemessage = null;
		String responseBody = null;

		try {
	
			uc.setRequestMethod("POST");
		
			uc.setRequestProperty("Content-Type", "application/vnd.att-dr.subscription");
			uc.setRequestProperty( "charset", "utf-8");
			uc.setRequestProperty( "X-ATT-DR-ON-BEHALF-OF", "DGL" );
			uc.setRequestProperty( "Content-Length", Integer.toString( postData.length ));
			uc.setUseCaches(false);
			uc.setDoOutput(true);
			OutputStream os = null;
			int rc = -1;
			
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
            }
			rc = uc.getResponseCode();
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
            if (rc == 201 ) {
     			responseBody = bodyToString( uc.getInputStream() );
    			logger.info( "responseBody=" + responseBody );

            } else {
            	err.setCode(rc);
            	err.setMessage(responsemessage);
            }
            
		} catch (Exception e) {
            System.err.println("Unable to read response  " );
            e.printStackTrace();
        }		finally {
			try {
				uc.disconnect();
			} catch ( Exception e ) {}
		}
		return responseBody;

	}
	

	public String doPutFeed(Feed putFeed, ApiError err) {
		byte[] postData = putFeed.getBytes();
		logger.info( "post fields=" + postData.toString() );
		String responsemessage = null;
		String responseBody = null;

		try {
			logger.info( "uc=" + uc );
			uc.setRequestMethod("PUT");
			uc.setRequestProperty("Content-Type", "application/vnd.att-dr.feed");
			uc.setRequestProperty( "charset", "utf-8");
			uc.setRequestProperty( "X-ATT-DR-ON-BEHALF-OF", putFeed.getOwner() );
			uc.setRequestProperty( "Content-Length", Integer.toString( postData.length ));
			uc.setUseCaches(false);
			uc.setDoOutput(true);
			OutputStream os = null;
			int rc = -1;
			
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
            }
			rc = uc.getResponseCode();
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
            if (rc >= 200 && rc < 300 ) {
     			responseBody = bodyToString( uc.getInputStream() );
    			logger.info( "responseBody=" + responseBody );

            } else if ( rc == 404 ) {
            	err.setCode( rc );
            	err.setFields( "feedid");
            	String message =  "FeedId " + putFeed.getFeedId() + " not found on DR to update.  Out-of-sync condition?";
            	err.setMessage( message );
            	errorLogger.error( DmaapbcLogMessageEnum.PROV_OUT_OF_SYNC, "Feed", putFeed.getFeedId() );
            	
            } else {
            	err.setCode( rc );
            	err.setMessage(responsemessage);
            }
            
		} catch (ConnectException ce) {
			errorLogger.error( DmaapbcLogMessageEnum.HTTP_CONNECTION_EXCEPTION, provURL, ce.getMessage() );
            err.setCode( 500 );
        	err.setMessage("Backend connection refused");
		} catch (SocketException se) {
			errorLogger.error( DmaapbcLogMessageEnum.SOCKET_EXCEPTION, se.getMessage(), "response from Prov server" );
			err.setCode( 500 );
			err.setMessage( "Unable to read response from DR");
        } catch (Exception e) {
            logger.warn("Unable to read response  " );
            e.printStackTrace();
            try {
	            err.setCode( uc.getResponseCode());
	            err.setMessage(uc.getResponseMessage());
            } catch (Exception e2) {
            	err.setCode( 500 );
            	err.setMessage("Unable to determine response message");
            }
        } 		finally {
			try {
				uc.disconnect();
			} catch ( Exception e ) {}
		}
		return responseBody;
	}
	public String doPutDr_Sub(DR_Sub postSub, ApiError err) {
		logger.info( "entry: doPutDr_Sub() "  );
		byte[] postData = postSub.getBytes();
		logger.info( "post fields=" + postData );
		String responsemessage = null;
		String responseBody = null;

		try {
	
			uc.setRequestMethod("PUT");
		
			uc.setRequestProperty("Content-Type", "application/vnd.att-dr.subscription");
			uc.setRequestProperty( "charset", "utf-8");
			uc.setRequestProperty( "X-ATT-DR-ON-BEHALF-OF", "DGL" );
			uc.setRequestProperty( "Content-Length", Integer.toString( postData.length ));
			uc.setUseCaches(false);
			uc.setDoOutput(true);
			OutputStream os = null;
			int rc = -1;
			
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
            }
			rc = uc.getResponseCode();
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
            if (rc == 200 ) {
     			responseBody = bodyToString( uc.getInputStream() );
    			logger.info( "responseBody=" + responseBody );

            } else {
            	err.setCode(rc);
            	err.setMessage(responsemessage);
            }
            
		} catch (ConnectException ce) {
            errorLogger.error( DmaapbcLogMessageEnum.HTTP_CONNECTION_EXCEPTION, provURL, ce.getMessage() );
            err.setCode( 500 );
        	err.setMessage("Backend connection refused");
		} catch (Exception e) {
            System.err.println("Unable to read response  " );
            e.printStackTrace();
        } finally {
        	uc.disconnect();
        }
		return responseBody;

	}
	
	public String doGetNodes( ApiError err ) {
		logger.info( "entry: doGetNodes() "  );
		//byte[] postData = postSub.getBytes();
		//logger.info( "get fields=" + postData );
		String responsemessage = null;
		String responseBody = null;

		try {
	
			uc.setRequestMethod("GET");
		
			//uc.setRequestProperty("Content-Type", "application/vnd.att-dr.subscription");
			//uc.setRequestProperty( "charset", "utf-8");
			//uc.setRequestProperty( "X-ATT-DR-ON-BEHALF-OF", "DGL" );
			//uc.setRequestProperty( "Content-Length", Integer.toString( postData.length ));
			//uc.setUseCaches(false);
			//uc.setDoOutput(true);
			OutputStream os = null;
			int rc = -1;
			
			try {
                 uc.connect();
                 //os = uc.getOutputStream();
                 //os.write( postData );

            } catch (ProtocolException pe) {
                 // Rcvd error instead of 100-Continue
                 try {
                     // work around glitch in Java 1.7.0.21 and likely others
                     // without this, Java will connect multiple times to the server to run the same request
                     uc.setDoOutput(false);
                 } catch (Exception e) {
                 }
            } 
			rc = uc.getResponseCode();
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
        	err.setCode(rc);  // may not really be an error, but we save rc
            if (rc == 200 ) {
     			responseBody = bodyToString( uc.getInputStream() );
    			logger.info( "responseBody=" + responseBody );
            } else {
            	err.setMessage(responsemessage);
            }
            
		} catch (ConnectException ce) {
            errorLogger.error( DmaapbcLogMessageEnum.HTTP_CONNECTION_EXCEPTION, provURL, ce.getMessage() );
            err.setCode( 500 );
        	err.setMessage("Backend connection refused");
		} catch (Exception e) {
            System.err.println("Unable to read response  " );
            e.printStackTrace();
        } finally {
        	uc.disconnect();
        }
		return responseBody;

	}
	public String doPutNodes( ApiError err ) {
		logger.info( "entry: doPutNodes() "  );
		//byte[] postData = nodeList.getBytes();
		//logger.info( "get fields=" + postData );
		String responsemessage = null;
		String responseBody = null;

		try {
	
			uc.setRequestMethod("PUT");
		
			//uc.setRequestProperty("Content-Type", "application/vnd.att-dr.subscription");
			//uc.setRequestProperty( "charset", "utf-8");
			//uc.setRequestProperty( "X-ATT-DR-ON-BEHALF-OF", "DGL" );
			//uc.setRequestProperty( "Content-Length", Integer.toString( postData.length ));
			uc.setUseCaches(false);
			//uc.setDoOutput(true);
			OutputStream os = null;
			int rc = -1;
			
			try {
                 uc.connect();
                 //os = uc.getOutputStream();
                 //os.write( postData );

            } catch (ProtocolException pe) {
                 // Rcvd error instead of 100-Continue
                 try {
                     // work around glitch in Java 1.7.0.21 and likely others
                     // without this, Java will connect multiple times to the server to run the same request
                     uc.setDoOutput(false);
                 } catch (Exception e) {
                 }
            }
			rc = uc.getResponseCode();
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
          	err.setCode(rc);
            if (rc == 200 ) {
     			responseBody = bodyToString( uc.getInputStream() );
    			logger.info( "responseBody=" + responseBody );

            } else {
  
            	err.setMessage(responsemessage);
            }
            
		} catch (Exception e) {
            System.err.println("Unable to read response  " );
            e.printStackTrace();
        } finally {
        	uc.disconnect();
        }
		return responseBody;

	}
	
	/*
	 public static void main( String[] args ) throws Exception {
	        PropertyConfigurator.configure("log4j.properties");
	        logger.info("Started.");
	        
	        RandomInteger ri = new RandomInteger(10000);
		 	//String postJSON = String.format("{\"name\": \"dgl feed %d\", \"version\": \"v1.0\", \"description\": \"dgl feed N for testing\", \"authorization\": { \"classification\": \"unclassified\", \"endpoint_addrs\": [],\"endpoint_ids\": [{\"password\": \"test\",\"id\": \"test\"}]}}", ri.next()) ;
		 	int i = ri.next();
		 	Feed tst = new Feed( "dgl feed " + i,
		 						"v1.0",
		 						"dgl feed " + i + "for testing",
		 						"TEST",
		 						"unclassified"
		 			);
		 	ArrayList<DR_Pub> pubs = new ArrayList<DR_Pub>();
		 	pubs.add( new DR_Pub( "centralLocation" ) ); 
		 	tst.setPubs(pubs);

	        boolean rc;
	    	DrProvConnection context = new DrProvConnection();
	    	rc = context.makeFeedConnection();
	    	logger.info( "makeFeedConnection returns " + rc);
	    	ApiError err = new ApiError();
	    	if ( rc ) {
	    		String tmp  = context.doPostFeed( tst, err );
	    		logger.info( "doPostFeed returns " + tmp);
	    	}
    
	 }
 */
	
		
}
