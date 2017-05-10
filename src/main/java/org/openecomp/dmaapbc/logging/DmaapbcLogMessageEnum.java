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

import com.att.eelf.i18n.EELFResourceManager;
import com.att.eelf.i18n.EELFResolvableErrorEnum;

public enum DmaapbcLogMessageEnum implements EELFResolvableErrorEnum {
//0xx sample stock messages
  MESSAGE_SAMPLE_NOARGS,
  MESSAGE_SAMPLE_ONE_ARG,
  MESSAGE_SAMPLE_TWO_ARGS,

// 1xx Permission Errors
  AAF_CREDENTIAL_ERROR,
  CODEC_CREDENTIAL_ERROR,
  PE_AUTHENTICATION_ERROR,
  DR_PROV_AUTHORIZATION,

// 2xx Availability Errors/Timeouts
  DRIVER_UNAVAILABLE,
  HTTP_CONNECTION_ERROR,
  HTTP_CONNECTION_EXCEPTION,
  UNKNOWN_HOST_EXCEPTION,

  
// 3xx Data Errors
  IO_EXCEPTION,
  SSL_HANDSHAKE_ERROR,
  AAF_UNEXPECTED_RESPONSE,
  PE_EXCEPTION,
  SOCKET_EXCEPTION,
  JSON_PARSING_ERROR,
  DECRYPT_IO_ERROR,
  
//4xx Schema Errors
  DB_UPGRADE_ERROR,
  DB_INIT_ERROR,
  DB_UPDATE_ERROR,
  DB_ACCESS_ERROR,
  DB_FIELD_INIT_ERROR,
  DB_ACCESS_INIT_ERROR,
  DB_NO_FIELD_HANDLER,


// 5xx Business Process Errors
  PREREQ_DMAAP_OBJECT,
  PROV_OUT_OF_SYNC,
  MM_CIRCULAR_REF,
  TOPIC_CREATE_ERROR,
  INGRESS_CREATE_ERROR,
  FEED_PUB_PROV_ERROR,
  FEED_SUB_PROV_ERROR,
  MM_PUBLISH_ERROR,
  EGRESS_CREATE_ERROR,

// 900 Unknown Errors
  UNEXPECTED_CONDITION;

	static {
		EELFResourceManager.loadMessageBundle("logmsg");
	}
}
