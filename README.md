DMaaP Bus Controller API
=======================

Data Movement as a Platform (DMaaP) Bus Controller provides an API for other OpenDCAE infrastructure components to provision DMaaP resources.
A typical DMaaP resource is a Data Router Feed or a Message Router Topic, and their associated publishers and subscribers.
Other infrastucture resources such as DR Nodes and MR Clusters are also provisioned through this API.

### Build Instructions for a Continuous Integration environment using Jenkins

When this component is included in a Continuous Integration environment, such as structured by the Linux Foundation, the artifacts can be created and deployed via Jenkins.  The following maven targets are currently supported in the Build step:
```
clean install
javadoc:javadoc
sonar:sonar
```

In addition, the docker image is deployed during the Post Build step.

### Build Instructions for external developers

This project is organized as a mvn project for a jar package.
After cloning from this git repo:

```
mvn clean install javadoc:javadoc
```


### Docker Packaging

We can utilize docker to build and register the dmaapBC container in a local dev repository.
Note the Dockerfile follows OpenECOMP convention of running app as root.

```
<following a successful build, assuming DOCKER_HOST is set appropriately for your environment>

$ docker build -f ./Dockerfile .
```


### OpenECOMP 1701 deployment

Assumes a DCAE Controller deployment on a Docker host which has access to Rackspace Nexus server, and likely running other OpenECOMP containers.
Prior to starting container, place environment specific vars in /tmp/docker-databus-controller.conf on the Docker host,
and map that file to /opt/app/config/conf.
Run the container with the dmaapbc deploy command, which will update the container runtime properties appropriately.
For example, in IAD1 environment, /tmp/docker-databus-controller.conf looks like:
```

# DMaaP Bus Controller OpenSource environment vars
CONT_DOMAIN=dcae.simpledemo.openecomp.org
DMAAPBC_INSTANCE_NAME=iad1

#   The https port
#   set to 0 if certificate is not ready
DMAAPBC_INT_HTTPS_PORT=0

DMAAPBC_KSTOREFILE=/opt/app/dcae-certificates
DMAAPBC_KSTOREPASS=foofoofoo
DMAAPBC_PVTKEYPASS=barbarbar

DMAAPBC_PG_ENABLED=true
DMAAPBC_PGHOST=zldciad1vipstg00.simpledemo.openecomp.org
DMAAPBC_PGCRED=test234-ftl

DMAAPBC_DRPROV_FQDN=zldciad1vidrps00.simpledemo.openecomp.org

DMAAPBC_AAF_URL=https://aafapi.${CONT_DOMAIN}:8095/proxy/

DMAAPBC_TOPICMGR_USER=m99751@dmaapBC.openecomp.org
DMAAPBC_TOPICMGR_PWD=enc:zyRL9zbI0py3rJAjMS0dFOnYfEw_mJhO
DMAAPBC_ADMIN_USER=m99501@dcae.openecomp.org
DMAAPBC_ADMIN_PWD=enc:YEaHwOJrwhDY8a6usetlhbB9mEjUq9m

DMAAPBC_PE_ENABLED=false
DMAAPBC_PE_AAF_ENV=TBD
```
Then the following steps could be used to pull and run the Bus Controller.
```
$ 
$ docker pull ecomp-nexus:51212/dcae_dmaapbc:1.0.0
$ docker run -d -p 18080:8080 -v /tmp/docker-databus-controller.conf:/opt/app/config/conf ecomp-nexus:51212/dcae_dmaapbc:1.0.0
```

