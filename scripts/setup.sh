#! /bin/bash

[ -z "$1" ] && echo "GITHUB_USER not set" && exit 1;
[ -z "$2" ] && echo "GITHUB_PACKAGE_READ_TOKEN not set" && exit 1;

cd gradle;
echo " allprojects {
  repositories {
    maven {
      url =  uri(\"https://maven.pkg.github.com/dqualizer/dqlang\")
      credentials {
        username = \"$1\"
        password = \"$2\"
      }
    }
  }
} " > init.gradle;
