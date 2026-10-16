FROM ghcr.io/opentofu/opentofu:1.13.1-minimal@sha256:dfbc3f0a8bed0adfa2cf49ff418f158afe24080eea6d6e748db8799fc4553db6 AS tofu

FROM alpine:3.24.2

# Copy the tofu binary from the minimal image
COPY --from=tofu /usr/local/bin/tofu /usr/local/bin/tofu

# Add script dependencies
RUN apk add --no-cache bash curl jq git

# Copy scripts
COPY entrypoint.sh /bin
COPY update_roles_scope.sh /bin
