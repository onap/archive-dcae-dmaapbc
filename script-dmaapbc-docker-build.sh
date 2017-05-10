#!/bin/bash
# Create a debian package and push to remote repo
#
#
# build the docker image. tag and then push to the remote repo
#

# !!! make sure the yaml file include docker-login as a builder before calling
# this script

phase=$1

VERSION=$(xpath -e '//project/version/text()' 'pom.xml')
VERSION=${VERSION//\"/}
EXT=$(echo "$VERSION" | rev | cut -s -f1 -d'-' | rev)
if [ -z "$EXT" ]; then
  EXT="STAGING"
fi
case $phase in 
  verify|merge)
    if [ "$EXT" != 'SNAPSHOT' ]; then
      echo "$phase job only takes SNAPSHOT version, got \"$EXT\" instead"
      exit 1
    fi 
    ;;
  release)
    if [ ! -z "$EXT" ] && [ "$EXT" != 'STAGING' ]; then
      echo "$phase job only takes STAGING or pure numerical version, got \"$EXT\" instead"
      exit 1
    fi
    ;; 
  *)
    echo "Unknown phase \"$phase\""
    exit 1
esac
echo "Running \"$phase\" job for version \"$VERSION\""



IMAGE='openecomp/dcae-dmaapbc'
VERSION="${VERSION//[^0-9.]/}"
VERSION2=$(echo "$VERSION" | cut -f1-2 -d'.')

TIMESTAMP="-$(date +%C%y%m%dT%H%M%S)"
LFQI="${IMAGE}:${VERSION}${TIMESTAMP}"
BUILD_PATH="${WORKSPACE}"
# build a docker image
docker build --rm -f "${WORKSPACE}"/Dockerfile -t "${LFQI}" "${BUILD_PATH}"

if [ "$phase" == "verify" ]; then
    exit
fi

#
# push the image
#
# io registry  DOCKER_REPOSITORIES="nexus3.openecomp.org:10001 \
# release registry                   nexus3.openecomp.org:10002 \
# snapshot registry                   nexus3.openecomp.org:10003"
# staging registry                   nexus3.openecomp.org:10004"
case $EXT in
SNAPSHOT|snapshot)
    #REPO='nexus3.openecomp.org:10003'
    REPO='nexus3.onap.org:10003'
    EXT="-SNAPSHOT"
    ;;
STAGING|staging)
    #REPO='nexus3.openecomp.org:10003'
    #REPO='nexus3.openecomp.org:10004'
    REPO='nexus3.onap.org:10003'
    EXT="-STAGING"
    ;;
"")
    #REPO='nexus3.openecomp.org:10002'
    REPO='nexus3.onap.org:10002'
    EXT=""
    echo "version has no extension, intended for release, in \"$phase\" phase. donot do release here"
    exit 1
    ;;
*)
    echo "Unknown extension \"$EXT\" in version"
    exit 1
    ;;
esac

OLDTAG="${LFQI}"
PUSHTAGS="${REPO}/${IMAGE}:${VERSION}${EXT}${TIMESTAMP} ${REPO}/${IMAGE}:latest ${REPO}/${IMAGE}:${VERSION2}${EXT}-latest"
for NEWTAG in ${PUSHTAGS}
do
   echo "tagging ${OLDTAG} to ${NEWTAG}" 
   docker tag "${OLDTAG}" "${NEWTAG}"
   echo "pushing ${NEWTAG}" 
   docker push "${NEWTAG}"
   OLDTAG="${NEWTAG}"
done

