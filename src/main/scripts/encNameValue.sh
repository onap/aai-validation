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

# The script invokes the com.amdocs.aai.audit.security.encryption.EncryptedPropValue class to generate an encrypted value
# e.g
# ./encNameValue.sh odl.auth.password admin
# will return:
# odl.auth.password.x=f1e2c25183ef4b4ff655e7cd94d0c472
#
if [ "$#" -ne 2 ]; then
    echo "Illegal number of parameters (expected 2)"
    echo "Usage: `basename $0` <property name> <property value>" 1>&2
    exit 1
fi
base_dir=$(dirname $0)/..
# On Windows we must use a different CLASSPATH separator character
if [ "$(expr substr $(uname -s) 1 5)" == "MINGW" ]; then
	CPSEP=\;
else
	CPSEP=:
fi

java -cp ".${CPSEP}$base_dir/extJars/*" com.att.aai.util.EncryptedPropValue -n $1 -v $2
