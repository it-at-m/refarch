package de.muenchen.oss.refarch.integration.pscd.service.adapter.in;

/**
 * The channels a PSCD batch can arrive on, as they are named in a log line and in a failure mail.
 */
public enum PscdInboundChannel {

    FILE("file"),

    REST("REST"),

    SOAP("SOAP");

    private final String displayName;

    PscdInboundChannel(final String displayName) {
        this.displayName = displayName;
    }

    public String label() {
        return this.displayName;
    }
}
