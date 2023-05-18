Summary:            Angry IP Scanner - Custom Mod
Name:               imodscan
Version:            RPM_VERSION
Release:			1%{?dist}
License:            GPLv2+
Group:              Applications/Internet
BuildRoot: 			%{_builddir}/%{name}
URL:                https://angryip.org/
Packager:			Anton Keks, GoogleX
Requires:			java-11

%description
Angry IP Scanner is a cross-platform network scanner written in Java.
It can scan IP-based networks in any range, scan ports, and resolve
other information. With Custom Modification to be able scan something more.

The program provides an easy to use GUI interface and is very extensible,
see https://angryip.org/ for more information.

%prep

%build

%install
rm -rf $RPM_BUILD_ROOT
mkdir -p $RPM_BUILD_ROOT/%{_libdir}/imodscan $RPM_BUILD_ROOT/%{_datadir}/applications $RPM_BUILD_ROOT/%{_datadir}/pixmaps $RPM_BUILD_ROOT/%{_bindir}
cp ../../%{name}-%{platform}-VERSION.jar $RPM_BUILD_ROOT/%{_libdir}/imodscan/
cp ../../../../ext/deb-bundle/usr/share/applications/imodscan.desktop $RPM_BUILD_ROOT/%{_datadir}/applications/
cp ../../../../resources/images/icon128.png $RPM_BUILD_ROOT/%{_datadir}/pixmaps/ipscan.png
cp ../../../../ext/deb-bundle/usr/bin/imodscan $RPM_BUILD_ROOT/%{_bindir}/
chmod a+x $RPM_BUILD_ROOT/%{_bindir}/imodscan

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root,-)
%{_libdir}/imodscan/%{name}-%{platform}-VERSION.jar
%{_datadir}/applications/imodscan.desktop
%{_datadir}/pixmaps/ipscan.png
%{_bindir}/imodscan
