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

function post_json_payload
{
	local http_type=$1
	local json_file=$2
	local validation_service_url=""

	#echo "INFO: Function post_json_payload called with http_type=$http_type and json_file=$json_file"
	case ${http_type} in

		http)
			validation_service_url="http://localhost:9500/services/validation-service/v1/app/validate"
			;;
		https)
			validation_service_url="https://localhost:9501/services/validation-service/v1/app/validate"
			;;
		*)
			# should not get here
			return 1
			;;
	esac

	# POST the JSON payload and save the JSON response as the actual result file (raw)
	# Note: the --tlsv1.2 option is only relevant for SSL https connections, it will be ignored for http
	curl --insecure -X POST -H "Content-Type:application/json"  -H "Accept: application/json" \
		--tlsv1.2 -d @${json_file} ${validation_service_url} > ${actual}/${json_input}.raw.json 2> /dev/null

	# mask out variable information such as the unique identifier, the timestamp and the entityLink
	# Note: don't use the global flag as we only want to replace first occurrence
	sed     -e 's/[a-z0-9]\{8\}-[a-z0-9]\{4\}-[a-z0-9]\{4\}-[a-z0-9]\{4\}-[a-z0-9]\{12\}/VALIDATIONID/' \
		-e 's/20[1-9][0-9][0-1][1-9][0-3][0-9]T[0-2][0-9][0-5][0-9][0-5][0-9]Z/TIMESTAMP/' ${actual}/${json_input}.raw.json > ${actual}/${json_input}.res.json

	# add a newline to the end of the file
	echo "" >> ${actual}/${json_input}.res.json

	return 0
}


#
# runVsRestTests.sh - script starts here
#


testtotal=0
testcount=0
passes=0
fails=0
skips=0
lrmOutput=""
verbose="false"
vs_port=0

# colour terminal escape sequences
PASS="\e[32mPASS\e[39m"
FAIL="\e[31mFAIL\e[39m"
SKIP="\e[33mSKIP\e[39m"
TEST="\e[33mTEST\e[39m"
RED_ON="\e[31m"
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

# exit if the validation service is not running
lrmOutput=$(/opt/app/swm/scldlrm/bin/lrmcli -running | grep "com.att.ajsc.validation-service" | grep -e "HEARTBEAT,COMPLETED_SUCCESSFULLY" -e "START,COMPLETED_SUCCESSFULLY")
if [ $? != 0 ]; then
	echo -e "${FAIL}: exit - the validation service is not running"
	exit 1
fi
echo -e "INFO: ${lrmOutput}"

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
	
			# execute each test twice
			# once for http and once https
			for protocol in http https; do

				# loop through each test
				testcount=$((testcount + 1))

				json_filename=$(basename $json_file)

				# remove the extn - use rev so that f1 is the last field not the first field
				json_input=$(echo $json_filename | rev | cut -d'.' --complement -s -f1 | rev)

				# set port according to protocol
				if [ "${protocol}" == "http" ]; then
					port=9500
				else
					port=9501
				fi

				echo -e "${TEST}: ${YELLOW_ON}$json_input${COLOUR_OFF} - POST request using ${protocol} protocol on port ${port}"

				if [ $verbose == "true" ]; then
					echo -e "INFO: ${BLUE_ON}JSON payload to be validated${COLOUR_OFF}"
					cat ${tdir}/${json_filename}
				fi

				# POST JSON payload to the validation service
				post_json_payload ${protocol} ${json_file}

				# Skip the test if the expected results file does not exist
				if [ ! -e ${expected}/${json_input}.exp.json -a ! -e ${expected}/${json_input}.error ]; then
					echo -e "${SKIP}: no ${json_input}.exp.json or ${json_input}.error file found"
					skips=$((skips + 1))
					continue
				fi

				if [ -e ${expected}/${json_input}.error ]; then
				# not expecting a validation json payload, instead a plain text error message is returned 
					if [ -s ${actual}/${json_input}.res.json ]; then
						# expecting a non-zero length file
						# compare the actual results against the expected results
						diff --strip-trailing-cr ${expected}/${json_input}.error ${actual}/${json_input}.res.json > ${actual}/${json_input}.diff
						if [ $? != 0 ]; then
							echo -e "${FAIL}: ${RED_ON}${json_input}${COLOUR_OFF} - POST response error text does not match expected result"
							fails=$((fails + 1))
						else
							echo -e "${PASS}: ${GREEN_ON}${json_input}${COLOUR_OFF} - POST response error text matches expected result"
							passes=$((passes + 1))
						fi
					else
						# test fails if no POST response is returned
						echo -e "${FAIL}: ${RED_ON}${json_input}${COLOUR_OFF} - No POST response returned"
						fails=$((fails + 1))
					fi
					continue
				else
				# expecting a non-zero length validation json payload to be returned
					# Fail the test if the actual results file does not exist or is zero length
					if [ ! -e ${actual}/${json_input}.res.json ]; then
						echo -e "${FAIL}: ${RED_ON}${json_input}.res.json${COLOUR_OFF} - actual result file is missing"
						fails=$((fails + 1))
						continue
					else
						if [ ! -s ${actual}/${json_input}.res.json ]; then
							echo -e "${FAIL}: ${RED_ON}${json_input}.res.json${COLOUR_OFF} - actual result file is zero length (no JSON retrieved)"
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
						echo -e "${PASS}: ${GREEN_ON}${json_input}${COLOUR_OFF} - POST response JSON payload matches expected result"
						rm ${actual}/${json_input}.diff
						passes=$((passes + 1))
					else
						# actual and expected results do not match
						fails=$((fails + 1))
						echo -e "${FAIL}: ${RED_ON}${json_input}${COLOUR_OFF} - POST response JSON payload does not match the expected result"
						while read diffline
						do
							echo "DIFF: $diffline"
						done < ${actual}/${json_input}.diff
					fi
				fi
			done
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


