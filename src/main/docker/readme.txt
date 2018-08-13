Brief steps on creating docker image manually. 
==============================================

1. build validation-service using
	mvn clean install [-DskipTests]

2. Build Docker image
	sudo docker build -t openecomp/validation-service target

3. Run docker image
	sudo docker run -it -p 9501:9501 -v <path to appconfig dir>:/opt/app/validation-service/appconfig -v /etc/hosts:/etc/hosts openecomp/validation-service
	eg. sudo docker run -it -p 9501:9501 -v /home/AAI/docker/validation/appconfig-local:/opt/app/validation-service/appconfig -v /etc/hosts:/etc/hosts openecomp/validation-service

Troubleshooting
---------------

To run the docker image as an interactive bash shell
	sudo docker run -it -p 9501:9501 -v /home/AAI/docker/validation/appconfig-local:/opt/app/validation-service/appconfig -v /etc/hosts:/etc/hosts openecomp/validation-service bash -il
	
	This will take you to a bash shell on the running docker container. cd to /opt/app/validation-service to check configurations or run it manually.
