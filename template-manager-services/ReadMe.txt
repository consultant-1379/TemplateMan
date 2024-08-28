To run the TemplateManager REST service locally use:
java -jar template-manager-services-1.0.61-SNAPSHOT.jar \
--git.service.url=http://10.44.149.55/api/v1 \
--git.service.access.token=5702924bc16245b020ddf4b31eff4b021ee3599d 
--docker.repo.server.url=armdocker.rnd.ericsson.se" \
--docker.repo.application.path=aia/test \
--artifactory.server.url=https://arm.epk.ericsson.se/artifactory/ \
--artifactory.server.path=docker-v2-global-local &

-----------------------------------------------------------------------------------------------------

To run the TemplateManager REST service on production as a jar use:
java -jar template-manager-services-1.0.61-SNAPSHOT.jar \
--spring.profiles.active=production &

-----------------------------------------------------------------------------------------------------

To run the TemplateManager REST service within a container use:

java -jar template-manager-services-1.0.61-SNAPSHOT.jar --spring.profiles.active=container &

This profile will match spring properties to the environmental properties:
git.service.type = ${GIT_SERVICE_TYPE}
git.service.url = ${GIT_SERVICE_URL}
git.service.access.token = ${GIT_SERVICE_ACCESS_TOKEN}
datastore.host = ${DATASTORE_HOST}
