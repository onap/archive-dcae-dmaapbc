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

package org.openecomp.dmaapbc.logging;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public abstract class BaseLoggingClass {
	protected  EELFLogger logger = EELFManager.getInstance().getLogger( super.getClass());
	protected static final EELFLogger appLogger = EELFManager.getInstance().getApplicationLogger();
	protected static final EELFLogger auditLogger = EELFManager.getInstance().getAuditLogger();
	protected static final EELFLogger debugLogger = EELFManager.getInstance().getDebugLogger();
	protected static final EELFLogger errorLogger = EELFManager.getInstance().getErrorLogger();
	protected static final EELFLogger metricsLogger = EELFManager.getInstance().getMetricsLogger();
	protected static final EELFLogger serverLogger = EELFManager.getInstance().getServerLogger();

	
}
