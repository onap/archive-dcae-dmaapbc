---
-- ============LICENSE_START=======================================================
-- OpenECOMP - org.openecomp.dmaapbc
-- ================================================================================
-- Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
-- ================================================================================
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- 
--      http://www.apache.org/licenses/LICENSE-2.0
-- 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
-- ============LICENSE_END=========================================================
---

@drop table dcae_location;
create table dcae_location	(
	dcae_location_name	VARCHAR(100),
	clli	VARCHAR(100),
	dcae_layer	VARCHAR(100),
	open_stack_availability_zone	VARCHAR(100),
	last_mod	TIMESTAMP,
	PRIMARY KEY(dcae_location_name)
);
@drop table dmaap;
create table dmaap	(
	version	VARCHAR(100),
	topic_ns_root	VARCHAR(100),
	dmaap_name	VARCHAR(100),
	dr_prov_url	VARCHAR(200),
	node_key	VARCHAR(100),
	access_key_owner	VARCHAR(100),
	last_mod	TIMESTAMP,
	status		VARCHAR(100),
	bridge_admin_topic	VARCHAR(100),
	logging_url	VARCHAR(200)
);
@drop table dr_node;
create table dr_node	(
	fqdn	VARCHAR(100),
	dcae_location_name	VARCHAR(100),
	host_name	VARCHAR(100),
	version	VARCHAR(100),
	PRIMARY KEY(fqdn)
);
@drop table dr_pub;
create table dr_pub	(
	dcae_location_name	VARCHAR(100),
	username	VARCHAR(100),
	userpwd	VARCHAR(100),
	feed_id	VARCHAR(100),
	pub_id	VARCHAR(100),
	status	VARCHAR(100),
	PRIMARY KEY(pub_id)
);
@drop table dr_sub;
create table dr_sub	(
	owner	VARCHAR(100),
	suspended	BOOLEAN,
	status	VARCHAR(100),
	use100	BOOLEAN,
	dcae_location_name	VARCHAR(100),
	username	VARCHAR(100),
	userpwd	VARCHAR(100),
	feed_id	VARCHAR(100),
	delivery_u_r_l	VARCHAR(200),
	log_u_r_l	VARCHAR(200),
	sub_id	VARCHAR(100),
	PRIMARY KEY(sub_id)
);
@drop table mr_client;
create table mr_client	(
	dcae_location_name	VARCHAR(100),
	fqtn	VARCHAR(100),
	client_role	VARCHAR(100),
	action	VARCHAR(300),
	mr_client_id	VARCHAR(100),
	status	VARCHAR(100),
	topic_u_r_l	VARCHAR(200),
	last_mod	TIMESTAMP,
	PRIMARY KEY(mr_client_id)
);
@drop table mr_cluster;
create table mr_cluster	(
	last_mod	TIMESTAMP,
	dcae_location_name	VARCHAR(100),
	fqdn	VARCHAR(100),
	hosts	VARCHAR(300),
	topic_protocol	VARCHAR(100),
	topic_port	VARCHAR(100),
	PRIMARY KEY(dcae_location_name)
);
@drop table feed;
create table feed	(
	suspended	BOOLEAN,
	subscribe_u_r_l	VARCHAR(200),
	feed_id	VARCHAR(100),
	feed_name	VARCHAR(100),
	feed_version	VARCHAR(100),
	feed_description	VARCHAR(1000),
	owner	VARCHAR(100),
	aspr_classification	VARCHAR(100),
	publish_u_r_l	VARCHAR(200),
	log_u_r_l	VARCHAR(200),
	status	VARCHAR(100),
	-- pubs not stored here
	-- subs not stored here
	PRIMARY KEY(feed_id)
);
@drop table topic;
create table topic	(
	last_mod	TIMESTAMP,
	fqtn	VARCHAR(100),
	topic_name	VARCHAR(100),
	topic_description	VARCHAR(1000),
	tnx_enabled	VARCHAR(100),
	owner	VARCHAR(100),
	status	VARCHAR(100),
	-- clients not stored here
	PRIMARY KEY(fqtn)
);
@drop table mirror_maker;
create table mirror_maker	(
	mm_name	VARCHAR(100),
	source_cluster	VARCHAR(100),
	target_cluster	VARCHAR(100),
	last_mod	TIMESTAMP,
	vectors		TEXT,
	PRIMARY KEY(source_cluster)
);
update dmaapbc_sch_ver set version = 1 where version = 0;
