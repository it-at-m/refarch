FROM ghcr.io/opentofu/opentofu:1.13.10-minimal@sha256:5c6cef27227e50060139fec7122c83d14088b5dd2677162545f0e699e1f6196c AS tofu

FROM alpine:3.24.4@sha256:f59abe656b692d901e184be3445e084786602108599d2a51d229c0f5a48c71bf

# Copy the tofu binary from the minimal image
COPY --from=tofu /usr/local/bin/tofu /usr/local/bin/tofu

# Add script dependencies
RUN apk add --no-cache bash curl jq git

# Copy scripts
COPY entrypoint.sh /bin
COPY update_roles_scope.sh /bin
