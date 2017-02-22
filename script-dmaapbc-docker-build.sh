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


IMAGE='dcae_dmaapbc'
TAG='1.0.0'
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
docker tag ${LFQI} ${RFQI}
docker push ${RFQI}

