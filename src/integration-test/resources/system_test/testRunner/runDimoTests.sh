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
source ${RUNNER}/DIMO.conf

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

		msg_count=$(grep "cambria.partition" ${RUNNER}/${topic}-app.log | wc -l)
		echo "INFO: msg_count = $msg_count"
		if [ ${msg_count} == "0" ]; then
			echo "INFO: No more messages on topic $topic"
			break
		fi
	done

	return 0
}

function topic_consume_and_extract_json
{
	local topic=$1
	local json_input=$2
	local actual=$3

#	echo "INFO: Function topic_consume_and_extract_json called with topic=$topic and json_input=$json_input and actual directory=$actual"
#	echo "INFO: Remove ${TOPIC_LOGS}/application.log"

	# need a clean log so rm existing log file
	rm ${TOPIC_LOGS}/application.log

	# consumed TOPIC event(s) will be written to a newly created application.log
	${VS_BIN}/event-consumer.sh $TOPIC_HOST $topic $TOPIC_CGROUP $TOPIC_CID $TOPIC_API_USERNAME $TOPIC_API_PASSWORD > /dev/null 2> /dev/null

	# take a local copy of the application.log
	cp ${TOPIC_LOGS}/application.log ${RUNNER}/topic_json.log

#	sed	-e '/ - url :/d' \
#		-e '/ - topic :/d' \
#		-e '/ - partition :/d' \
#		-e '/ - message :/d' \
#		-e '/ - username :/d' \
#		-e '/ - password :/d' \
#		-e '/c.att.nsa.apiClient.http.HttpClient/d' \
#		-e '/com.att.nsa.cambria.client.impl.CambriaConsumerImpl/d' \
#		-e '/c.a.n.c.c.impl.CambriaConsumerImpl/d' \
#		-e '/Count of messages consumed/d' \
#		-e 's/.*com.att.ecomp.event.client.test.TestEventConsumer - //g' ${RUNNER}/topic_json.log > ${actual}/${json_input}.raw.json

	sed	-e '/ - | url :/d' \
		-e '/ - | topic :/d' \
		-e '/ - | consumerGroup :/d' \
		-e '/ - | consumerId :/d' \
		-e '/ - | username :/d' \
		-e '/ - | password :/d' \
		-e '/c.att.nsa.apiClient.http.HttpClient/d' \
		-e '/com.att.nsa.cambria.client.impl.CambriaConsumerImpl/d' \
		-e '/c.a.n.c.c.impl.CambriaConsumerImpl/d' \
		-e '/Count of messages consumed/d' \
		-e 's/.*com.att.ecomp.event.client.test.TestEventConsumer - | //g' \
		-e 's/\(.*\)\(|\)/\1/g' ${RUNNER}/topic_json.log > ${actual}/${json_input}.raw.json

	# mask out variable information such as the unique identifier, the timestamp and the entityLink
	# Note: don't use the global flag as we only want to replace first occurrence
	sed	-e 's/[a-z0-9]\{8\}-[a-z0-9]\{4\}-[a-z0-9]\{4\}-[a-z0-9]\{4\}-[a-z0-9]\{12\}/VALIDATIONID/' \
		-e 's/20[1-9][0-9][0-1][1-9][0-3][0-9]T[0-2][0-9][0-5][0-9][0-5][0-9]Z/TIMESTAMP/' ${actual}/${json_input}.raw.json > ${actual}/${json_input}.res.json

	return 0
}


function topic_publish
{
	local topic=$1
	local json_file=$2

	#echo "INFO: Function topic_publish called with topic=$topic and json_file=$json_file"
	TOPIC_MESSAGE=$(cat ${json_file})
	${VS_BIN}/event-publisher.sh "$TOPIC_HOST" "$topic" "$TOPIC_PARTITION" "$TOPIC_MESSAGE" "$TOPIC_API_USERNAME" "$TOPIC_API_PASSWORD" > /dev/null 2> /dev/null

}


#
# runDimoTests.sh - script starts here
#


testtotal=0
testcount=0
passes=0
fails=0
skips=0
lrmOutput=""
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


# cd to Validation Service BIN so that TOPIC logs are written to $VS_BIN/logs/application.log
cd ${VS_BIN}
if [ $? != 0 ]; then
	echo -e "${FAIL}: exit - unable to cd to ${VS_BIN}"
	exit 1
fi

# exit if the validation service is not running
lrmOutput=$(/opt/app/swm/scldlrm/bin/lrmcli -running | grep "com.att.ajsc.validation-service" | grep -e "HEARTBEAT,COMPLETED_SUCCESSFULLY" -e "START,COMPLETED_SUCCESSFULLY")
if [ $? != 0 ]; then
	echo -e "${FAIL}: exit - the validation service is not running"
	exit 1
fi
echo -e "INFO: ${lrmOutput}"

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
rm -rf ${ACTUAL}
mkdir ${ACTUAL}

# find test directories under $DATA
testdir=$(find ${DATA} -type d | sort | tail -n +2)
if [[ -z $testdir ]]; then
	echo "ERROR: no test directories found under $DATA"
	exit 1
fi

# find rule directories under $RULES
ruledir=$(find ${RULE} -type d | sort | tail -n +2)
if [[ -z $ruledir ]]; then
	echo "ERROR: no rule directories found under $DATA"
	exit 1
fi

# for each test directory under $DATA
for tdir in ${testdir}; do

	ls -l ${tdir}/*.json > /dev/null 2> /dev/null
	if [ $? != 0 ]; then
		echo "WARN: No JSON (input) files found in ${tdir} - skipping this dir"
		continue
	fi
	tdir_name=$(basename $tdir)

	# for each config directory under $RULE
	for rdir in ${ruledir}; do

		ls -l ${rdir}/*.groovy > /dev/null 2> /dev/null
		if [ $? != 0 ]; then
			echo "WARN: No Groovy (config) files found in ${rdir} - skipping this dir"
			continue
		fi
		rdir_name=$(basename $rdir)

		echo "INFO: --------------------------------------------------------------------------------------"
		echo "INFO: rule directory=$rdir_name"
		echo "INFO: test directory=$tdir_name"
		echo "INFO: --------------------------------------------------------------------------------------"

		# set actual and expected paths
		actual=${ACTUAL}/${rdir_name}/${tdir_name}
		expected=${EXPECTED}/${rdir_name}/${tdir_name}

		# create the actual result directory
		# echo "INFO: mkdir -p ${actual}"
		mkdir -p ${actual}

		# for each JSON file in $tdir
		for json_file in ${tdir}/*.json ; do

			# loop through each test
			testcount=$((testcount + 1))

			json_filename=$(basename $json_file)

			# remove the extn - use rev so that f1 is the last field not the first field
			json_input=$(echo $json_filename | rev | cut -d'.' --complement -s -f1 | rev)

			echo -e "${TEST}: ${YELLOW_ON}$json_input${COLOUR_OFF}"

			if [ $verbose == "true" ]; then
				echo -e "INFO: ${BLUE_ON}JSON payload to be validated${COLOUR_OFF}"
				cat ${tdir}/${json_filename}
			fi

			# publish JSON payload to the INPUT topic
			topic_publish ${TOPIC_INPUT} ${json_file}

			# wait for the validation service to send results to the TOPIC
			# topic.consume.polling.interval.seconds=3
			# /opt/app/validation-service/etc/validation-service.properties
			# echo "INFO: Sleeping for 10s"
			sleep 15

			# consume from the OUTPUT topic and save retrieved JSON to the actual results directory
			topic_consume_and_extract_json ${TOPIC_OUTPUT} ${json_input} ${actual}

			# Skip the test if the expected results file does not exist
			if [ ! -e ${expected}/${json_input}.exp.json -a ! -e ${expected}/${json_input}.error ]; then
				echo -e "${SKIP}: no ${json_input}.exp.json or ${json_input}.error file found"
				skips=$((skips + 1))
				continue
			fi

			if [ -e ${expected}/${json_input}.error ]; then
			# not expecting a vadatiion json payload on the output queue
				if [ -s ${actual}/${json_input}.res.json ]; then
					# test fails if a non-zero length results file exists
					echo -e "${FAIL}: ${json_input}.json - unexpected validation message on the output queue"
					fails=$((fails + 1))
				else
					# test passes if a zero length results file exist
					echo -e "${PASS}: ${json_input}.json - no validation message on the output queue as expected"
					passes=$((passes + 1))
				fi
			else
			# a non-zero length result file is expected on the output queue 
				# Fail the test if the actual results file does not exist or is zero length
				if [ ! -e ${actual}/${json_input}.res.json ]; then
					echo -e "${FAIL}: ${json_input}.res.json - actual result file is missing"
					fails=$((fails + 1))
					continue
				else
					if [ ! -s ${actual}/${json_input}.res.json ]; then
						echo -e "${FAIL}: ${json_input}.res.json - actual result file is zero length (no JSON retrieved)"
						fails=$((fails + 1))
						continue
					fi
				fi

				# compare the actual results against the expected results
				diff --strip-trailing-cr ${expected}/${json_input}.exp.json ${actual}/${json_input}.res.json > ${actual}/${json_input}.diff

				diff_retval=$?
				if [ "$diff_retval" == "0" ]; then
					# actual and expected results match
					# use python to format the json payload (raw output)
					python -m json.tool ${actual}/${json_input}.raw.json > ${RUNNER}/pretty.json
					if [ $verbose == "true" ]; then
						# output the formatted validation payload
						echo -e "INFO: ${BLUE_ON}validation result JSON payload${COLOUR_OFF}"
						cat ${RUNNER}/pretty.json
						echo -e "INFO: ${BLUE_ON}validation payload matches the expected result${COLOUR_OFF}"
					fi
					# report a test PASS status
					echo -e "${PASS}: ${GREEN_ON}${json_input}.json${COLOUR_OFF}"
					rm ${actual}/${json_input}.diff
					passes=$((passes + 1))
				else
					# actual and expected results do not match
					fails=$((fails + 1))
					echo -e "${FAIL}: ${json_input}.json - the actual results do not match expected results"
					while read diffline
					do
						echo "DIFF: $diffline"
					done < ${actual}/${json_input}.diff
				fi
			fi
		done
	done
done

# output the test summary statistics
echo "INFO: ===================="
echo "INFO: Test Results Summary"
echo "INFO: ===================="

testtotal=$((passes + fails + skips))
if [ $testtotal != $testcount ]; then
	echo "ERROR: test statistics are inconsistent!"
fi

echo "Total passes = $passes"
echo "Total failures = $fails"
echo "Total skipped tests = $skips"
echo "Total tests executed = $testtotal"


