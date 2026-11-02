# Note: For base images an architecture-agnostic SHA is always preferred, see https://refarch.oss.muenchen.de/templates/deploy.html#docker-images for more info.

# For documentation see https://rh-openjdk.github.io/redhat-openjdk-containers/
FROM registry.access.redhat.com/ubi10/openjdk-25-runtime:1.24-18@sha256:e898c4ad1c8cfb5afee86917906356226956813733ebc27bbf33f762bb3b8f08

# Copy runnable jar to deployments
COPY target/*.jar /deployments/application.jar
