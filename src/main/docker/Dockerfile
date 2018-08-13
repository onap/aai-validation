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
FROM ubuntu:14.04

ARG MICRO_HOME=/opt/app/validation-service
ARG BIN_HOME=$MICRO_HOME/bin
ARG LOG_HOME=$MICRO_HOME/logs/AAI-VS
ARG JAR_FILE=validation-service.jar

RUN apt-get update

# Install and set up Java 8
RUN apt-get update && apt-get install -y software-properties-common
## sudo -E is required to preserve the environment. If you remove that line, it will most like freeze at this step
RUN sudo -E add-apt-repository ppa:openjdk-r/ppa && apt-get update && apt-get install -y openjdk-8-jdk
## Set up JAVA_HOME for docker command-line
ENV JAVA_HOME usr/lib/jvm/java-8-openjdk-amd64
RUN export JAVA_HOME

# Build up the deployment folder structure
RUN mkdir -p $MICRO_HOME
COPY ${JAR_FILE} $MICRO_HOME/
COPY classes/logback.xml $MICRO_HOME/
COPY bundleconfig/ $MICRO_HOME/bundleconfig/
RUN mkdir -p $BIN_HOME
COPY *.sh $BIN_HOME
RUN chmod 755 $BIN_HOME/*
RUN mkdir -p $LOG_HOME
RUN ln -s $MICRO_HOME/logs /logs

# Create the aai user
RUN mkdir /opt/aaihome && \
    groupadd -g 492381 aaiadmin && \
    useradd -r -u 341790 -g 492381 -ms /bin/bash -d /opt/aaihome/aaiadmin aaiadmin && \
    chown -R aaiadmin:aaiadmin $MICRO_HOME
USER aaiadmin

EXPOSE 9501 9501

CMD ["/opt/app/validation-service/bin/start.sh"]
