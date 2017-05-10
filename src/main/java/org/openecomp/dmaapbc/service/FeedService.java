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

import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.openecomp.dmaapbc.client.DrProvConnection;
import org.openecomp.dmaapbc.database.DatabaseClass;
import org.openecomp.dmaapbc.logging.BaseLoggingClass;
import org.openecomp.dmaapbc.model.ApiError;
import org.openecomp.dmaapbc.model.DR_Pub;
import org.openecomp.dmaapbc.model.DR_Sub;
import org.openecomp.dmaapbc.model.Feed;
import org.openecomp.dmaapbc.model.DmaapObject.DmaapObject_Status;

public class FeedService  extends BaseLoggingClass {
	
	private Map<String, Feed> feeds = DatabaseClass.getFeeds();
	private DR_PubService pubService = new DR_PubService();
	private DR_SubService subService = new DR_SubService();
	private DcaeLocationService dcaeLocations = new DcaeLocationService();
	public FeedService() {
		logger.info( "new FeedService");

	}
	
	public Map<String, Feed> getFeeds() {			
		return feeds;
	}
	
	private void getSubObjects( Feed f ) {
		ArrayList<DR_Pub> pubs = pubService.getDr_PubsByFeedId( f.getFeedId() );
		f.setPubs(pubs);
		ArrayList<DR_Sub> subs = subService.getDr_SubsByFeedId( f.getFeedId() );
		f.setSubs(subs);	
	}
		
	public List<Feed> getAllFeeds() {
		ArrayList<Feed> fatFeeds = new ArrayList<Feed>();
		for( Feed f:  feeds.values() ) {
			getSubObjects(f);
			fatFeeds.add(f);
		}
		return fatFeeds;
	}
		
	public Feed getFeed( String key, ApiError err ) {
		Feed f = feeds.get( key );
		if ( f != null ) {
			getSubObjects( f );
		} else {
			err.setCode(Status.NOT_FOUND.getStatusCode());
			err.setMessage("feed not found");
			err.setFields("feedId=" + key );
		}
		err.setCode(200);
		return f;
	}
	//TODO: clean this up after testing proves it is no longer needed
	/*
	private void saveChildren( Feed fnew, Feed req ) {
		// save any pubs
		DR_PubService pubSvc = new DR_PubService();
		ArrayList<DR_Pub> reqPubs = req.getPubs();
		ArrayList<DR_Pub> newPubs = fnew.getPubs();
		logger.info( "reqPubs size=" + reqPubs.size() + " newPubs size=" + newPubs.size() );

		// NOTE: when i > 1 newPubs are in reverse order from reqPubs
		int nSize = newPubs.size();
		int rSize = reqPubs.size();
		if ( nSize != rSize ) {
			logger.error( "Resulting set of publishers do not match requested set of publishers " + nSize + " vs " + rSize );
			fnew.setStatus( DmaapObject_Status.INVALID);
			return;
		}
		for( int i = 0; i < reqPubs.size(); i++ ) {
			DR_Pub reqPub = reqPubs.get(i);
			DR_Pub newPub = newPubs.get(nSize - i - 1);
			

			// make sure to re-use original pubId if it exists
			String origPubId = reqPub.getPubId();
			if ( origPubId != null && ! origPubId.isEmpty()) {
				newPub.setPubId( origPubId );
			}
			newPub.setDcaeLocationName(reqPub.getDcaeLocationName());
			
			
			newPubs.set( nSize - i - 1 , pubSvc.addDr_Pub( newPub ));
		}
		
		// save any subs
		ArrayList<DR_Sub> subs = req.getSubs();
		if ( subs.size() == 0 ) {
			logger.info( "No subs specified");
		} else {
			DR_SubService subSvc = new DR_SubService( fnew.getSubscribeURL() );
			ApiError err = new ApiError();
			for( int i = 0; i <  subs.size(); i++ ) {
				subs.set( i,  subSvc.addDr_Sub(subs.get(i), err));
			}
			fnew.setSubs(subs);
		}


		fnew.setStatus(DmaapObject_Status.VALID);

	}
	*/
	
	private boolean savePubs( Feed f ) {
		return savePubs( f, f );
	}
	// need to save the Pub objects independently and copy pubId from original request
	private boolean savePubs( Feed fnew, Feed req ) {
		// save any pubs
		DR_PubService pubSvc = new DR_PubService();
		ArrayList<DR_Pub> reqPubs = req.getPubs();
		ArrayList<DR_Pub> newPubs = fnew.getPubs();
		

		
		int nSize = newPubs.size();
		int rSize = reqPubs.size();
		logger.info( "reqPubs size=" + rSize + " newPubs size=" + nSize );
		if ( nSize != rSize ) {
			errorLogger.error( "Resulting set of publishers do not match requested set of publishers " + nSize + " vs " + rSize );
			fnew.setStatus( DmaapObject_Status.INVALID);
			return false;
		}
		// NOTE: when i > 1 newPubs are in reverse order from reqPubs
		for( int i = 0; i < reqPubs.size(); i++ ) {
			DR_Pub reqPub = reqPubs.get(i);	
			ApiError err = new ApiError();
			if ( pubSvc.getDr_Pub( reqPub.getPubId(), err ) == null ) {
				DR_Pub newPub = newPubs.get(nSize - i - 1);
				reqPub.setPubId(newPub.getPubId());
				reqPub.setFeedId(newPub.getFeedId());
				reqPub.setStatus(DmaapObject_Status.VALID);
				if ( reqPub.getDcaeLocationName() == null ) {
					reqPub.setDcaeLocationName("notSpecified");
				}
				pubSvc.addDr_Pub( reqPub );
			}
			
		}
		
		fnew.setPubs(reqPubs);
		fnew.setStatus(DmaapObject_Status.VALID);
		return true;

	}
	
	private boolean saveSubs( Feed f ) {
		return saveSubs( f, f );
	}
	// need to save the Sub objects independently
	private boolean saveSubs( Feed fnew, Feed req ) {	
		ArrayList<DR_Sub> subs = req.getSubs();
		if ( subs.size() == 0 ) {
			logger.info( "No subs specified");
		} else {
			DR_SubService subSvc = new DR_SubService( fnew.getSubscribeURL() );
			ApiError err = new ApiError();
			for( int i = 0; i <  subs.size(); i++ ) {
				DR_Sub sub = subs.get(i);
				if ( subSvc.getDr_Sub( sub.getSubId(), err) == null ) {
					subs.set( i,  subSvc.addDr_Sub(sub, err));
					if ( ! err.is2xx())  {
						logger.error( "i=" + i + " url=" + sub.getDeliveryURL() + " err=" + err.getCode() );
						return false;
					}
				}
				
			}
			fnew.setSubs(subs);
		}


		fnew.setStatus(DmaapObject_Status.VALID);
		return true;

	}

	public  Feed addFeed( Feed req, ApiError err ) {

		// at least 1 pub is required by DR, so create a default pub if none is specified
		if ( req.getPubs().size() == 0 ) {
			logger.info( "No pubs specified - creating tmp pub");
			ArrayList<DR_Pub> pubs = new ArrayList<DR_Pub>();
			pubs.add( new DR_Pub( dcaeLocations.getCentralLocation())
								.setRandomUserName()
								.setRandomPassword());
			req.setPubs(pubs);
		} 
		
		DrProvConnection prov = new DrProvConnection();
		prov.makeFeedConnection();	
		String resp = prov.doPostFeed( req, err );
		logger.info( "resp=" + resp );
		if ( resp == null ) {
			switch( err.getCode() ) {
			case 400: 
				err.setFields( "feedName=" + req.getFeedName() + " + feedVersion=" + req.getFeedVersion() );
				break;
			case 403:
				err.setCode(500);
				err.setMessage("API deployment/configuration error - contact support");
				err.setFields( "PROV_AUTH_ADDRESSES");
				logger.error( "Prov response: 403. " + err.getMessage() + " regarding " + err.getFields() );
				break;
			default:
				err.setCode(500);
				err.setMessage( "Unexpected response from DR backend" );
				err.setFields("response");
			}
			return null;
		}


		Feed fnew = new Feed( resp );
		logger.info( "fnew status is:" + fnew.getStatus() );
		if ( ! fnew.isStatusValid()) {		
			err.setCode(500);
			err.setMessage( "Unexpected response from DR backend" );
			err.setFields("response");		
			return null;
		}
		
		//saveChildren( fnew, req );
		if ( ! savePubs( fnew, req ) || ! saveSubs( fnew, req ) ) {
			err.setCode(Status.BAD_REQUEST.getStatusCode());
			err.setMessage("Unable to save Pub or Sub objects");
			return null;
		}
		fnew.setFormatUuid(req.getFormatUuid());
		fnew.setLastMod();
		feeds.put( fnew.getFeedId(), fnew );
		return fnew;
	}
		
	public Feed updateFeed( Feed req, ApiError err ) {
	

		DrProvConnection prov = new DrProvConnection();
		prov.makeFeedConnection( req.getFeedId() );
		String resp = prov.doPutFeed( req, err );
		logger.info( "resp=" + resp );
		if ( resp == null ) {
			switch( err.getCode() ) {
			case 400: 
				err.setFields( "feedName=" + req.getFeedName() + " + feedVersion=" + req.getFeedVersion() );
				break;
			case 403:
				err.setCode(500);
				err.setMessage("API deployment/configuration error - contact support");
				err.setFields( "PROV_AUTH_ADDRESSES");
				break;
			default:
				err.setCode(500);
				err.setMessage( "Unexpected response from DR backend" );
				err.setFields("response");
			}
			return null;
		}


		Feed fnew = new Feed( resp );
		logger.info( "fnew status is:" + fnew.getStatus() );
		if ( ! fnew.isStatusValid()) {		
			err.setCode(500);
			err.setMessage( "Unexpected response from DR backend" );
			err.setFields("response");		
			return null;
		}

		if ( ! savePubs( fnew, req ) || ! saveSubs( fnew, req ) ) {
			err.setCode(Status.BAD_REQUEST.getStatusCode());
			err.setMessage("Unable to save Pub or Sub objects");
			return null;
		}
		fnew.setFormatUuid(req.getFormatUuid());
		fnew.setLastMod();
		feeds.put( fnew.getFeedId(), fnew );
		return fnew;
	}
		
	public Feed removeFeed( String pubId ) {
		return feeds.remove(pubId);
	}	

}
