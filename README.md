Warning
=========================

<dl>
    <dt>Current plugin version is compatible with AEM 6.3 only. Please refer to separate branch https://github.com/ooyala/OoyalaAEM/tree/6_2_compatibility to get AEM 6.2 compatible plugin version.</dt>
</dl>

Ooyala Adobe AEM Connector
=========================

<dl>
	<dt>Compatibility</dt>
	<dd>Adobe AEM 6.3</dd>

	<dt>Usage</dt>
	<dd>A pre built package is available through github by clicking the download link above. Install the zip file into AEMs package manager. Configuration instructions are available in documentation/Ooyala-AdobeAEM-Documentation.pdf</dd>
</dl>


Building from Source
====================

<dl>
	<dt>Requirements</dt>
	<dd>Maven, Adobe AEM 6.3</dd>

	<dt>Build</dt>
	<dd>Clone the repository. Execute `mvn install` in the parent directory (OoyalaAEM). A full content-package zip can be found under deploy/target.</dd>

	<dt>Deploy</dt>
	<dd>You can automatically deploy to the local Adobe AEM instance, using the built in deploy profile. (mvn clean install -Pdeploy,local-author) All Adobe AEM connection parameters are exposed clearly within the parent POM for customization.</dd>
</dl>