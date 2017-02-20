FROM java:openjdk-8-jre
MAINTAINER Dominic Lunanuova
ENV insdir  /opt/app/dmaapbc
RUN \
    mkdir -p ${insdir}/lib \
    && mkdir -p ${insdir}/etc \
    && mkdir -p ${insdir}/logs \
    && mkdir -p ${insdir}/www && mkdir -p ${insdir}/www/doc \
	&& mkdir -p ${insdir}/config \
	&& mkdir -p ${insdir}/misc \
	&& mkdir -p ${insdir}/bin
WORKDIR ${insdir}
USER root
COPY target/dcae_dmaapbc.jar ${insdir}/lib/
COPY target/deps/*.jar ${insdir}/lib/
COPY src/main/resources/log4j.properties ${insdir}/etc/
COPY www/ ${insdir}/www/
# COPY target/site/apidocs/ ${insdir}/www/doc/
COPY misc/LocalKey ${insdir}/etc/
COPY misc/opensource.env ${insdir}/misc/
COPY misc/*.tmpl ${insdir}/misc/
COPY misc/dmaapbc ${insdir}/bin/
COPY misc/doaction ${insdir}/bin/

VOLUME ${insdir}/log
CMD ["./bin/dmaapbc", "deploy" ]
