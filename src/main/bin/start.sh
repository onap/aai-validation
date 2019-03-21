#!/bin/sh
# ============LICENSE_START===================================================
# Copyright (c) 2018 Amdocs
# ============================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ============LICENSE_END=====================================================

# APP_HOME is required for EELF logging.
# This path is referenced in the file logback.xml.
APP_HOME="${APP_HOME-/opt/app/validation-service}"

JARFILE="$APP_HOME/validation.jar"
LOGBACK_FILE=logback.xml

# CONFIG_HOME is used as the base folder for relative paths, e.g. in the file aai-environment.properties
if [ -z "$CONFIG_HOME" ]; then
    echo "CONFIG_HOME must be set in order to start up the process"
    echo "E.g. CONFIG_HOME=${APP_HOME}/config"    
    exit 1
fi

# Some properties are repeated here for debugging purposes.
PROPS="-DAPP_HOME=$APP_HOME"
PROPS="${PROPS} -DCONFIG_HOME=${CONFIG_HOME}"
PROPS="${PROPS} -Dcom.att.eelf.logging.path=${APP_HOME}"
PROPS="${PROPS} -Dcom.att.eelf.logging.file=${LOGBACK_FILE}"
PROPS="${PROPS} -Dlogback.configurationFile=${APP_HOME}/${LOGBACK_FILE}"
PROPS="${PROPS} -DKEY_STORE_PASSWORD="
JVM_MAX_HEAP=${MAX_HEAP:-1024}

if [ -z "${java_runtime_arguments}" ]; then
  java_runtime_arguments="-Xms75m -Xmx${JVM_MAX_HEAP}m \
 -Dcom.sun.management.jmxremote \
 -Dcom.sun.management.jmxremote.authenticate=false \
 -Dcom.sun.management.jmxremote.ssl=false \
 -Dcom.sun.management.jmxremote.local.only=false \
 -Dcom.sun.management.jmxremote.port=1099 \
 -Dcom.sun.management.jmxremote.rmi.port=1099 \
 -Djava.rmi.server.hostname=127.0.0.1"
fi

echo "java $java_runtime_arguments $PROPS -jar $JARFILE"
java $java_runtime_arguments $PROPS -jar $JARFILE

