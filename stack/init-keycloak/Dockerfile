FROM ghcr.io/opentofu/opentofu:1.13.5-minimal@sha256:ca43a94b18b8d962299d110822af56a03192fd8ea74d3d7358aaee3f93ed3d90 AS tofu

FROM alpine:3.24.3@sha256:be2656061b536c1883c4034790e21d0a09c5aaaf51968b89368a3c0371ea2c9a

# Copy the tofu binary from the minimal image
COPY --from=tofu /usr/local/bin/tofu /usr/local/bin/tofu

# Add script dependencies
RUN apk add --no-cache bash curl jq git

# Copy scripts
COPY entrypoint.sh /bin
COPY update_roles_scope.sh /bin
