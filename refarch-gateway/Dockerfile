# Note: For base images an architecture-agnostic SHA is always preferred, see https://refarch.oss.muenchen.de/templates/deploy.html#docker-images for more info.

# For documentation see https://rh-openjdk.github.io/redhat-openjdk-containers/
FROM registry.access.redhat.com/ubi10/openjdk-25-runtime:1.24-13@sha256:68525bc239f93a62070625354e3b863be0963f61f1338794011665d5b8a946f5

# Copy runnable jar to deployments
COPY target/*.jar /deployments/application.jar
