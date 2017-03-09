#!/bin/bash
# Create a debian package and push to remote repo
#
#
# build the docker image. tag and then push to the remote repo
#

# !!! make sure the yaml file include docker-login as a builder before calling
# this script

if [ "$#" != "1" ]; then
    phase="verify"
else
    phase="$1"
    case $phase in
        verify|merge|release)
            echo "Running $phase job"
        ;;
        *)
            echo "Unknown phase $phase"
            exit 1
    esac
fi


IMAGE='openecomp/dcae_dmaapbc'
VERSION=$(xpath -e "//project/version/text()" "pom.xml")
EXT=$(echo "$VERSION" | rev | cut -s -f1 -d'-' | rev)
if [ -z "$EXT" ]; then
    VERSION=$(echo "${VERSION}-STAGING")
fi
TIMESTAMP=$(date +%C%y%m%dT%H%M%S)
echo $VERSION
echo $TIMESTAMP
TAG="${VERSION}-${TIMESTAMP}"
LFQI="${IMAGE}:${TAG}"
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
REPO='nexus3.openecomp.org:10003'
RFQI="${REPO}/${LFQI}"
echo "$LFQI"
echo "$RFQI"
docker tag "${LFQI}" "${RFQI}"
docker push "${RFQI}"

TAG="latest"
LFQI="${IMAGE}:${TAG}"
RFQI2="${REPO}/${LFQI}"
echo "$LFQI"
echo "$RFQI2"
docker tag "${RFQI}" "${RFQI2}"
docker push "${RFQI2}"

