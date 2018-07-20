#!/bin/bash
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

source default.conf
source ${RUNNER}/DARE.conf

function topic_consume
{
	local topic=$1
	local msg_count=0
	local msg_total=0

	echo "INFO: Function topic_consume called with topic=$topic"

	while true
	do
		if [ -e ${TOPIC_LOGS}/application.log ]; then
			#echo "INFO: Remove ${TOPIC_LOGS}/application.log"
			rm ${TOPIC_LOGS}/application.log
		else
			echo "INFO: ${TOPIC_LOGS}/application.log does not exist"
		fi
		${VS_BIN}/event-consumer.sh $TOPIC_HOST $topic $TOPIC_CGROUP $TOPIC_CID $TOPIC_API_USERNAME $TOPIC_API_PASSWORD > /dev/null 2> /dev/null
		cp ${TOPIC_LOGS}/application.log ${RUNNER}/${topic}-app.log

#		msg_count=$(grep "cambria.partition" ${RUNNER}/${topic}-app.log | wc -l)
		msg_count=$(grep -e '"event-header":{"event-type"' -e '{"validationId"' ${RUNNER}/${topic}-app.log | wc -l)
		echo "INFO: msg_count = $msg_count"
		if [ ${msg_count} == "0" ]; then
			echo "INFO: No more messages on topic $topic"
			break
		fi
	done

	return 0
}


#
# runDareTests.sh - script starts here
#


testtotal=0
testcount=0
passes=0
fails=0
skips=0
lrmOutput=""
ecount=0
verbose="false"

# colour terminal escape sequences
PASS="\e[32mPASS\e[39m"
FAIL="\e[31mFAIL\e[39m"
SKIP="\e[33mSKIP\e[39m"
TEST="\e[33mTEST\e[39m"
GREEN_ON="\e[32m"
BLUE_ON="\e[36m"
YELLOW_ON="\e[93m"
COLOUR_OFF="\e[39m"


# check command line arguments
if [ $# -gt 1 ]; then
	echo "Too many arguments: $*"
	echo "Usage: $0 [-verbose]"
	exit 1
fi

# output message contents in verbose mode
if [[ $# == 1 ]]; then
	if [[ $1 == "-verbose" ]]; then
		verbose="true"
	else
		echo echo "Unrecognised argument: $1"
		echo "Usage: $0 [-verbose]"
		exit 1
	fi
fi


# remove the GAP service application.log to start from a clean log
if [ -e ${GS_LOGS}/application.log ]; then
	echo "INFO: removing ${GS_LOGS}/application.log"
	rm ${GS_LOGS}/application.log
fi


# remove the Validation service application.log to start from a clean log
if [ -e ${VS_LOGS}/application.log ]; then
	echo "INFO: removing ${VS_LOGS}/application.log"
	rm ${VS_LOGS}/application.log
fi


# set DEBUG log level if VS or GS logback.xml is currently INFO level
sed -i "s/root level=\"INFO\"/root level=\"DEBUG\"/" ${GS_LOGBK}/logback.xml
sed -i "s/root level=\"INFO\"/root level=\"DEBUG\"/" ${VS_LOGBK}/logback.xml


# restart the GAP microservice
echo "INFO: restarting the GAP Service for APP_SERVER=${APP_SERVER}"
/opt/app/swm/scldlrm/bin/lrmcli -bounce -name com.att.ajsc.gap-service -version 1.0.1 -routeoffer ${APP_SERVER}

# restart the Validation microservice
echo "INFO: restarting the Validation Service for APP_SERVER=${APP_SERVER}"
/opt/app/swm/scldlrm/bin/lrmcli -bounce -name com.att.ajsc.validation-service -version 1.0.1 -routeoffer ${APP_SERVER}

sleep 5

# exit if the gap-service or validation-service is not running
for service in gap-service validation-service
do
	lrmOutput=$(/opt/app/swm/scldlrm/bin/lrmcli -running | grep "com.att.ajsc.${service}" | grep -e "HEARTBEAT,COMPLETED_SUCCESSFULLY" -e "START,COMPLETED_SUCCESSFULLY")
	if [ $? != 0 ]; then
		echo -e "${FAIL}: exit - the ${service} is not running"
		exit 1
	fi
	echo -e "INFO: ${lrmOutput}"
done


# cd to Validation Service HOME so that TOPIC logs are written to $VS_BIN/logs/application.log
cd ${VS_BIN}
if [ $? != 0 ]; then
	echo -e "${FAIL}: exit - unable to cd to ${VS_BIN}"
	exit 1
fi


# exit if consumer/publisher scripts are not present or not executable
if [[ ! -x ${VS_BIN}/event-consumer.sh || ! -x ${VS_BIN}/event-publisher.sh ]]; then
	echo -e "${FAIL}: exit - consumer/publisher scripts are not present or not executable"
	exit 1
fi

#echo "INFO: Clear the INPUT event queue"
topic_consume ${TOPIC_INPUT}

#echo "INFO: Clear the OUTPUT event queue"
topic_consume ${TOPIC_OUTPUT}

#echo "INFO: Clear ${ACTUAL}"
#rm -rf ${ACTUAL}
#mkdir ${ACTUAL}

# exit if the gap directory does not exist
if [[ ! -d ${GAP} ]]; then
	echo "ERROR: gap directory not found: $GAP"
	exit 1
fi

###########################################
#sleep 20
###########################################

# execute the gap-client.py python script to trigger a DARE request
SECONDS=0
duration=0
echo "INFO: "`date +"%F %T"` "GAP POST request STARTED"
echo "INFO: source ${GS_BIN}/gap-client.sh; python ${GS_BIN}/gap-client.py $GAP"
source ${GS_BIN}/gap-client.sh; python ${GS_BIN}/gap-client.py $GAP > ${RUNNER}/gap-req.log 2>&1

echo "INFO: "`date +"%F %T"` "GAP POST request FINISHED"

# output elapsed time
duration=$SECONDS
echo -e ${BLUE_ON}"INFO: GAP request took $(($duration / 3600)) hours $(($duration / 60)) minutes and $(($duration % 60)) seconds to complete"${COLOUR_OFF}

# display the output from gap-client.py as INFO
while read gline
do
	echo "INFO: $gline"
done < ${RUNNER}/gap-req.log


# verfiy the request completed successfully
grep "Request processed successfully" ${RUNNER}/gap-req.log > /dev/null 2>&1
if [ $? != 0 ]; then
	echo -e "${FAIL}: exit - the DARE request was not successful"
	exit 1
fi

# gap-client.py has completed so all entites should have been returned from the AAI
# use sed to extract all AAI entities from the GAP log and save them to entity.log
grep "Retrieved payload from rest client. Payload :" ${GS_LOGS}/application.log | sed -e "s/\([^{]*\)\([^|]*\).*/\2/" > ${RUNNER}/entity.log 2>&1
if [ $? != 0 ]; then
	echo -e "${FAIL}: exit - no Retrieved entities found in ${GS_LOGS}/application.log"
	exit 1
fi

# display each entity retrieved from the AAI as INFO if verbose=true
if [ ${verbose} == "true" ]; then
	while read eline; do
		echo "INFO: $eline"	
	done < ${RUNNER}/entity.log
fi

# count how many entities have been retrieved from the AAI
ecount=$(wc -l ${RUNNER}/entity.log | cut -d" " -f1)
echo -e "INFO: ${BLUE_ON}Number of objects retrieved from the A&AI = $ecount"${COLOUR_OFF}

#
# count how many events GAP publishes to the topic for the validation service to consume
#

gap_curr=0
gap_prev=0
gap_retry=0
while [ $gap_retry -lt 3 ]
do
#	sleep 1
	gap_curr=$(grep --line-buffered -i -e '"event-type":"AAI-DATA-EXPORT-API"' -e '"event-type":"AAI-DATA-EXPORT-NQ"' /opt/app/gap-service/logs/application.log | wc -l)
	if [ $gap_curr -gt $gap_prev ]; then
		echo "INFO: GAP Service events published = $gap_curr"
		gap_prev=$gap_curr
		gap_retry=0
		continue
	else
		if [ $gap_curr -lt $gap_prev ]; then
			echo "INFO: gap_curr = $gap_curr"
			echo "INFO: gap_prev = $gap_prev"
			echo -e "${FAIL}: exit - gap_curr should never be less than gap_prev"
			exit 1
		fi
		if [ $gap_curr -eq $gap_prev ]; then
			let gap_retry+=1
			echo "INFO: GAP retry counter = $gap_retry"
			echo "INFO: GAP Service events published = $gap_curr"
			sleep 10
			continue
		fi
	fi
done

# gap_curr should equal ecount
echo -e "INFO: ${BLUE_ON}Number of events published by GAP = $gap_curr"${COLOUR_OFF}
#echo "INFO: number of objects retrieved from the A&AI = $ecount"
if [ $gap_curr -eq $ecount ]; then
	echo -e "${PASS}: ${GREEN_ON}number of events published by GAP matches the number of objects retrieved from the A&AI"${COLOUR_OFF}
else
	echo -e "${FAIL}: number of events published by GAP does not match the number of objects retrieved from the A&AI"
fi


#
# count how many validation payloads the validation service publishes to the topic for the DI UI to consume
#

vs_curr=0
vs_prev=0
vs_retry=0
while [ $vs_retry -lt 3 ]
do
#       sleep 1
        vs_curr=$(grep --line-buffered -i -e "validationid" /opt/app/validation-service/logs/application.log | wc -l)
        if [ $vs_curr -gt $vs_prev ]; then
                echo "INFO: Validation Service events published = $vs_curr"
                vs_prev=$vs_curr
                vs_retry=0
                continue
        else
                if [ $vs_curr -lt $vs_prev ]; then
                        echo "INFO: vs_curr = $vs_curr"
                        echo "INFO: vs_prev = $vs_prev"
                        echo -e "${FAIL}: exit - vs_curr should never be less than vs_prev"
                        exit 1
                fi
                if [ $vs_curr -eq $vs_prev ]; then
                        let vs_retry+=1
                        echo "INFO: Validation retry counter = $vs_retry"
                        echo "INFO: Validation Service payloads published = $vs_curr"
                        sleep 10
                        continue
                fi
        fi
done

# output a timestamp when the validation service has finished publishing payloads
echo -e "INFO: "`date +"%F %T"` "Validation Service has FINISHED publishing validation payloads"

# vs_curr should equal ecount
echo -e "INFO: ${BLUE_ON}Number of validation payloads published by the validation service = $vs_curr"${COLOUR_OFF}
#echo "INFO: number of objects retrieved from the A&AI = $ecount"
if [ $vs_curr -eq $ecount ]; then
        echo -e "${PASS}: ${GREEN_ON}number of events published by the validation service matches the number of objects retrieved from the A&AI"${COLOUR_OFF}
else
        echo -e "${FAIL}: number of events published by the validation service does not match the number of objects retrieved from the A&AI"
fi

# output elapsed time
duration=$SECONDS
echo -e "INFO: ${BLUE_ON}Total E2E test execution took $(($duration / 3600)) hours $(($duration / 60)) minutes and $(($duration % 60)) seconds to complete"${COLOUR_OFF}


