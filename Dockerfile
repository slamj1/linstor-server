FROM ubuntu:xenial as builder

ENV LINSTOR_VERSION 0.7.2

ENV LINSTOR_TGZNAME linstor-server
ENV LINSTOR_TGZ ${LINSTOR_TGZNAME}-${LINSTOR_VERSION}.tar.gz

RUN groupadd makepkg # !lbbuild
RUN useradd -m -g makepkg makepkg # !lbbuild

RUN apt-get update -y # !lbbuild

RUN apt-get install -y debhelper default-jdk-headless dh-systemd # !lbbuild
# I saw gradle pulling in a higher java dependency when java was not installed first.
# so keep it on a extra line
RUN apt-get install -y gradle javahelper # !lbbuild

COPY /${LINSTOR_TGZ} /tmp/

USER makepkg
RUN cd ${HOME} && \
	cp /tmp/${LINSTOR_TGZ} ${HOME} && \
	tar xvf ${LINSTOR_TGZ} && \
		 cd ${LINSTOR_TGZNAME}-${LINSTOR_VERSION} && \
		 debuild -us -uc -i -b

FROM ubuntu:xenial
MAINTAINER Roland Kammerer <roland.kammerer@linbit.com>
COPY --from=builder /home/makepkg/*.deb /tmp/
RUN apt-get update -y && apt-get install -y default-jre-headless && \
	dpkg -i /tmp/linstor-common*.deb && dpkg -i /tmp/linstor-controller*.deb && \
	rm /tmp/*.deb && apt-get clean -y

EXPOSE 3376/tcp 3377/tcp
ENTRYPOINT ["/usr/share/linstor-server/bin/Controller", "--logs=/var/log/linstor-controller", "--config-directory=/etc/linstor"]
