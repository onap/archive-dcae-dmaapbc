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

package org.openecomp.dmaapbc.util;

import java.util.Date;
// source: http://www.javapractices.com/topic/TopicAction.do?Id=62
// with some modifications
import java.util.Random;


public final class RandomInteger {
	private static Random randomGenerator;
	private int range;
	
	public RandomInteger( int r ) {
		randomGenerator = new Random();
		randomGenerator.setSeed((new Date()).getTime());
		range = r;
	}
	
	public int next(){
		return randomGenerator.nextInt(range);
	}
  
  /** Generate 10 random integers in the range 0..99. */
  public static final void main(String... aArgs){
    log("Generating 10 random integers in range 0..99.");
    RandomInteger ri = new RandomInteger(100);
    //note a single Random object is reused here
    
    for (int idx = 1; idx <= 10; ++idx){
      int randomInt = ri.next();
      log("Generated : " + randomInt);
    }
    
    log("Done.");
  }
  
  private static void log(String aMessage){
    System.out.println(aMessage);
  }
}
 
