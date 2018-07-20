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

# This script attempts to consume a message from the specified topic.
# 
# Arguments:
#	comma-separated list of host names (or IP addresses) specifying where the messaging service is hosted. 
#	topic name
#	consumer group
#	consumer id
#	api key
#	api secret. Note: this is usually emailed to a user when the api key is created.

if [ "$#" -ne 6 ]; then
    echo "Illegal number of parameters (expected 6)"
    echo "Usage: `basename $0` <url list> <topic> <consumer group> <consumer id> <api key> <api secret>" 1>&2
    exit 1
fi
base_dir=$(dirname $0)/..
# On Windows we must use a different CLASSPATH separator character
if [ "$(expr substr $(uname -s) 1 5)" == "MINGW" ]; then
	CPSEP=\;
else
	CPSEP=:
fi

java -cp ".${CPSEP}$base_dir/extJars/*" com.att.ecomp.event.client.test.TestEventConsumer "$1" "$2" "$3" "$4" "$5" "$6" > /dev/null || exit 1

echo
echo "See the log file ./logs/application.log"
exit 0
