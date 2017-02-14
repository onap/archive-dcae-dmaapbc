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

package org.openecomp.dmaapbc.authentication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
 









import org.apache.log4j.Logger;
import org.openecomp.dmaapbc.aaf.DmaapPerm;
import org.openecomp.dmaapbc.util.DmaapConfig;

/* disable until available...
import org.openecomp.policy.api.DecisionRequestParameters;
import org.openecomp.policy.api.PolicyDecision;
import org.openecomp.policy.api.DecisionResponse;
import org.openecomp.policy.api.PolicyDecisionException;
import org.openecomp.policy.api.PolicyEngine;
import org.openecomp.policy.api.PolicyEngineException;
*/
 
public class DecisionPolicy {
	static final Logger logger = Logger.getLogger(DecisionPolicy.class);
	//PolicyEngine policyEngine = null;
	String  aafEnvironment;
	
	public DecisionPolicy() {
		logger.info("Constructing DecisionPolicy using property");
		DmaapConfig p = (DmaapConfig)DmaapConfig.getConfig();
		String config = p.getProperty("PolicyEngineProperties", "etc/PolicyEngineApi.properties");
		aafEnvironment = p.getProperty("PeAafEnvironment", "PROD"); // TEST= UAT, PROD = PROD, DEVL = TEST
		logger.info( "Initializing DecisionPolicy for env " + aafEnvironment + " using " + config);
		this.init( config );
	}
	public DecisionPolicy( String config ) {
		logger.info("Constructing DecisionPolicy using arg " + config );
		this.init( config );
	}
	
	public void init( String config ) {
		logger.info( "stubbed out PolicyEngine()" );
/*
		try {
			policyEngine = new PolicyEngine( config );
		} catch (PolicyEngineException e) {
	        logger.error( "Trying to read " + config + " and caught PolicyEngineException" + e );
			}
*/
	}
	public void check( String mechid, String pwd, DmaapPerm p ) throws AuthenticationErrorException {	
		logger.debug( mechid + " Permitted to do action " + p.getPermission() + "|" + p.getPtype() + "|" + p.getAction());

/*
        // Create a Decision Policy Request.
        DecisionRequestParameters decisionRequestParameters = new DecisionRequestParameters();
        decisionRequestParameters.setECOMPComponentName("DMaaP"); // Required ECOMP Name
        decisionRequestParameters.setRequestID(UUID.randomUUID());  // Optional UUID Request
        // Put all the required Attributes into Decision Attributes.
   
        HashMap<String,String> decisionAttributes = new HashMap<String, String>();
        decisionAttributes.put("AAF_ID", mechid);    // Fully qualified ID. 
        decisionAttributes.put("AAF_PASS", pwd);
        decisionAttributes.put("AAF_TYPE", p.getPermission());
        decisionAttributes.put("AAF_INSTANCE", p.getPtype());
        decisionAttributes.put("AAF_ACTION", p.getAction());
        decisionAttributes.put("AAF_ENVIRONMENT", aafEnvironment); // TEST= UAT, PROD = PROD, DEVL = TEST
        decisionRequestParameters.setDecisionAttributes(decisionAttributes);
        // Send the request to Policy Engine and that returns a PolicyDecision Object.
        try {
            DecisionResponse response = policyEngine.getDecision(decisionRequestParameters);
            if(response.getDecision().equals(PolicyDecision.PERMIT)){
                logger.debug( mechid + " Permitted to do action " + p.getPermission() + "|" + p.getPtype() + "|" + p.getAction());
            }else{
                logger.debug( mechid + " NOT Permited to do action "+ p.getPermission() + "|" + p.getPtype() + "|" + p.getAction());
                logger.debug(response.getDetails());
                throw  new AuthenticationErrorException();
            }
        } catch (PolicyDecisionException e) {
            System.err.println(e);
            logger.error(e);
            throw  new AuthenticationErrorException();
        }
*/
        
	     
    
    }

/*
    public static void main(String args[]){
    	DecisionPolicy d = new DecisionPolicy();
    	d.test();
    }
    	
    public  void test() {
    
		// TODO: pass in users and pwd from cmd line	
    	String[] ids = {"user1@namespace1",
    					"user2@namespace2"};
    	String[] pwds = {
    			"pwd1",
    			"pwd2"
    	};
    	List<DmaapPerm> perms = new ArrayList<DmaapPerm>();
    	
    	
    	perms.add( new DmaapPerm("org.openecomp.dmaapBC.dmaap", "mtnje2", "POST"));
    	perms.add( new DmaapPerm("org.openecomp.dmaapBC.dcaeLocations", "mtnje2", "POST"));
    	perms.add( new DmaapPerm("org.openecomp.dmaapBC.dmaap", "mtnje2", "GET"));
    	perms.add( new DmaapPerm("org.openecomp.dmaapBC.dmaap", "mtnje2", "PUT"));
    	perms.add( new DmaapPerm("org.openecomp.dmaapBC.dmaap", "mtnje2", "DELETE"));
    	
    
    	
    	for( DmaapPerm p : perms ) {
    		for( int i = 0; i < ids.length; i++) {
    			try {
    				this.check( ids[i], pwds[i], p);
    				System.out.println( "PERMIT! for "+ ids[i] + "  " + p.getPermission() + "|" + p.getPtype() + "|" + p.getAction());
    			} catch ( AuthenticationErrorException aee ) {
    				System.out.println( "FAILED! for "+ ids[i] + "  " + p.getPermission() + "|" + p.getPtype() + "|" + p.getAction());
    			} 
    		}
    	}
    
    }
*/
    
}
