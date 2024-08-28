# Execute the following command in a Docker Quickstart Terminal to build a docker image and publish it to the docker registry:
mvn clean install deploy -Pbuild-docker-image


# Run the docker image using the command:
docker run -d -e "GIT_SERVICE_TYPE=GOGS" \
-e "GIT_SERVICE_URL=http://10.44.149.69/api/v1" \
-e "GIT_SERVICE_ACCESS_TOKEN=f9b6a045f04c36b466d6bf0cd5406f4a8a2d0418" \
-e "DATASTORE_HOST=172.17.0.7" \
-e "DOCKER_USER_NAME=eappsdk" \
-e "DOCKER_USER_PASSWORD=Kgidwl*fak$9yq8c" \
-e "DOCKER_REPO_URL=armdocker.rnd.ericsson.se" \
-e "DOCKER_REPO_PATH=aia/test" \
-e "ARTIFACTORY_URL=https://arm.epk.ericsson.se/artifactory/" \
-p 6767:6767 \
-v /root/.ssh/:/root/.ssh/ \
-t armdocker.rnd.ericsson.se/aia/template-manager-services:1.0.46-SNAPSHOT