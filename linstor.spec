Name: linstor
Version: 0.2.6
Release: 1%{?dist}
Summary: LINSTOR SDS
%define GRADLE_TASKS installdist
%define GRADLE_FLAGS --offline --gradle-user-home /tmp --no-daemon --exclude-task generateJava
%define LS_PREFIX /usr/share/linstor-server
%define NAME_VERS %{name}-server-%{version}

Group: System Environment/Daemons
License: GPLv2+
URL: https://github.com/LINBIT/linstor-server
Source0: http://www.linbit.com/downloads/linstor/linstor-server-%{version}.tar.gz

BuildRequires: java-1.8.0-openjdk-headless

%description
TODO.


%prep
%setup -q -n %{NAME_VERS}


%build
rm -rf ./build/install
gradle %{GRADLE_TASKS} %{GRADLE_FLAGS}

%install
mkdir -p $RPM_BUILD_ROOT/%{LS_PREFIX}
cp -r $RPM_BUILD_DIR/%{NAME_VERS}/build/install/linstor-server/lib $RPM_BUILD_ROOT/%{LS_PREFIX}
mkdir -p $RPM_BUILD_ROOT/%{LS_PREFIX}/bin
cp -r $RPM_BUILD_DIR/%{NAME_VERS}/build/install/linstor-server/bin/Controller $RPM_BUILD_ROOT/%{LS_PREFIX}/bin
cp -r $RPM_BUILD_DIR/%{NAME_VERS}/build/install/linstor-server/bin/Satellite $RPM_BUILD_ROOT/%{LS_PREFIX}/bin
cp -r $RPM_BUILD_DIR/%{NAME_VERS}/build/install/linstor-server/bin/linstor-config $RPM_BUILD_ROOT/%{LS_PREFIX}/bin
cp -r $RPM_BUILD_DIR/%{NAME_VERS}/scripts/postinstall.sh $RPM_BUILD_ROOT/%{LS_PREFIX}/bin/controller.postinst.sh
mkdir -p $RPM_BUILD_ROOT/%{_unitdir}
cp -r $RPM_BUILD_DIR/%{NAME_VERS}/scripts/linstor-controller.service $RPM_BUILD_ROOT/%{_unitdir}
cp -r $RPM_BUILD_DIR/%{NAME_VERS}/scripts/linstor-satellite.service $RPM_BUILD_ROOT/%{_unitdir}

### common
%package common
Summary: Common files shared between controller and satellite
Requires: jre-headless

%description common
TODO.


%files common
%dir %{LS_PREFIX}
%dir %{LS_PREFIX}/lib
%{LS_PREFIX}/lib/*.jar
%dir %{LS_PREFIX}/lib/conf
%{LS_PREFIX}/lib/conf/logback.xml

### controller
%package controller
Summary: Linstor controller specific files
Requires: linstor-common = %{version}

%description controller
TODO.


%files controller
%dir %{LS_PREFIX}
%dir %{LS_PREFIX}/bin
%{LS_PREFIX}/bin/Controller
%{LS_PREFIX}/bin/linstor-config
%{LS_PREFIX}/bin/controller.postinst.sh
%{_unitdir}/linstor-controller.service

%post controller
%{LS_PREFIX}/bin/controller.postinst.sh
%systemd_post linstor-controller.service

%preun controller
%systemd_preun linstor-controller.service

### satellite
%package satellite
Summary: Linstor satellite specific files
Requires: linstor-common = %{version}
Requires: lvm2
Requires: drbd-utils

%description satellite
TODO.


%files satellite
%dir %{LS_PREFIX}
%dir %{LS_PREFIX}/bin
%{LS_PREFIX}/bin/Satellite
%{_unitdir}/linstor-satellite.service

%post satellite
%systemd_post linstor-satellite.service

%preun satellite
%systemd_preun linstor-satellite.service

%changelog
* Mon Jul 30 2018 Roland Kammerer <roland.kammerer@linbit.com> 0.2.6-1
- New upstream release.