# nabtrade to Xero statement converter

Updated for nabtrade UI update 2025

### Build

Requires maven

```shell
mvn package
```

### Usage

This example will convert all CSV files in a user directory called Downloads. Example in Bash.

```shell
java -jar target/nabtrade-connect-xero-0.1-SNAPSHOT.jar ~/Downloads
```

---

# Version Updates

* The plugin update requires manual checks as it is a report.
* Version updates automatic and are configured to skip alpha, beta, rc and old date format versions.
* maven-versions-plugin has backup poms disabled as VCS is here.

## Set a new release version

```shell
mvn versions:set -DprocessAllModules -DgenerateBackupPoms=false -DnewVersion=XX-SNAPSHOT 
```

Do a replacement in this readme file so that examples are updated to the new version.

## Report on what plugin updates are available

```shell
   mvn versions:display-plugin-updates | more

```

## Update all library versions and parent dependencies

```shell
mvn versions:update-parent -U
mvn versions:update-properties -U
mvn versions:use-latest-releases -U
```
