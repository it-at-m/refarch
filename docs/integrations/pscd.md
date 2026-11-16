# PSCD Integration

Integration for delivering PSCD "Satzarten" batches to SAP PSCD (Public Sector Collection and Disbursement).

The integration accepts one, checks it, and hands it to the PSCD SOAP endpoint. Delivery is one-way: PSCD returns no result, so nothing about what it made of the batch comes back on any channel.

## Modules

The modules follow the [default naming convention](./index.md#naming-conventions).

## Usage

Add the following dependency for using the PSCD integration as a library.

```xml
<dependencies>
    <dependency>
        <groupId>de.muenchen.oss.refarch</groupId>
        <artifactId>refarch-pscd-integration-starter</artifactId>
        <version>...</version>
    </dependency>
</dependencies>
```

After that the [SubmitPscdBatchInPort](https://github.com/it-at-m/refarch/blob/main/refarch-integrations/refarch-pscd-integration/refarch-pscd-integration-core/src/main/java/de/muenchen/oss/refarch/integration/pscd/application/port/in/SubmitPscdBatchInPort.java) can be used for submitting a batch (e.g. via autowiring, as the port is available as bean).

The core accepts exactly one datatype: the [PscdSatzarten](https://github.com/it-at-m/refarch/blob/main/refarch-integrations/refarch-pscd-integration/refarch-pscd-integration-core/src/main/java/de/muenchen/oss/refarch/integration/pscd/domain/model/PscdSatzarten.java) domain aggregate. Assembling it from whatever format the consumer receives is the consumer's concern; the generated SOAP types never cross the port.

```java
submitPscdBatchInPort.submit(PscdSatzarten.builder()
        .filename("batch-2026-0001.dat")
        .satzart010(Satzart010.builder().satzart("010").abstimmsumme("12345").vorzeichen("+").build())
        .satzart200(List.of(Satzart200.builder()
                .satzart("200").psobkey("221851164006").einnahmeart("20002").betrw("00000253820")
                .faedn("20260131").bldat("20260101").xblnr("000000720320").fvBelnr("000000541441")
                .build()))
        .build());
```

The starter brings the outbound client only. It starts no server and polls no directory, so a consuming application cannot accidentally expose an inbound channel.

## Standalone service

`refarch-pscd-integration-service` runs the same core as an application and adds three independent inbound channels. Any combination may run at once.

| Channel | Address | Default |
| ------- | ------- | ------- |
| REST    | `POST /api/pscd/batches` (JSON) | on |
| SOAP    | `POST /ws/pscd` (canonical contract) | on |
| File    | polls a directory for flat-file batches | off |

REST and SOAP speak the same refarch-owned canonical contract, generated from `pscd-canonical.xsd`. Its artefacts are served unauthenticated so a sending system can be built before it has an account:

- `GET /ws/pscd/pscdInbound.wsdl`
- `GET /ws/pscd/pscdCanonical.xsd`
- `GET /v3/api-docs`, UI at `/swagger-ui.html`

::: warning Migrating from the predecessor
The SOAP address is `/ws/pscd`, **not** the predecessor's `/services/webservicePscd`. Spring WS dispatches on the payload root element, so a POST to the old path returns 404: every sending system has to be repointed.
:::

Submitting requires HTTP Basic on both HTTP channels. There is no default account and no switch to turn it off: the service refuses to start until `refarch.pscd.inbound.security.username` and `.password` are set.

### File channel

A batch passes through three subdirectories of the polled one, none of them polled: the working directory while it is being processed, then the done or error directory. It is stamped with the time it was taken up (`batch.txt` becomes `batch_20260804_161500123.txt`) and keeps that name into its archive, so a reused filename cannot overwrite an earlier run's copy.

**Taken up before it is delivered.** The move into the working directory happens first, before the file is even read. That is to *delivered at most once*: from that moment the poll cannot see the file again, so a later failure to file it as done cannot bring it back for a second delivery. The cost is that a batch whose processing is interrupted (a failed final move, a crash, a kill) stays in the working directory and is **not** retried. Those are reported at startup:

```
PSCD working directory '/srv/pscd-inbox/.working' holds 1 batch(es) from an earlier run whose
delivery status is unknown; they are not picked up again and need settling by hand: batch_20260804_161500123.txt
```

Settle each by hand. A batch left there may or may not already be at PSCD.

**Only files that have stopped changing are taken up.** A batch is picked up once its size and modification time have held still for `stable-for` (default 2s, rounding up to a multiple of `poll-interval`); otherwise a file still being written would be read half-finished. A file that goes on changing for more than a minute is reported as a stalled transfer.

Records are fixed-width, so **column offsets are character offsets**: `refarch.pscd.inbound.file.charset` must match what the sending side writes. The default is the host export's `ISO-8859-1`. Decoding an ISO-8859-1 export as UTF-8 fails on the first umlaut; decoding a UTF-8 file as ISO-8859-1 silently shifts every column after one.

This channel deliberately behaves differently from REST and SOAP, carried over from the predecessor: a record missing a mandatory field is **repaired, not rejected**. The field is filled with `REQUIRED`, the record's `FEHLER` names what was missing, and the batch is delivered anyway for PSCD to decide about. A line whose SATZART cannot be read becomes a `SatzartFehler` record. The same defect over REST or SOAP is a `400`.

### Logs

Two files to reconciles against, separate from the service log and written to `PSCD_LOG_DIR` (default `./logs`):

- `account-error.log`: one line per record that could not be processed as it arrived
- `completion.log`: one line per file handed to PSCD

### Failure notification

Off by default. When enabled per channel, a mail goes out through the [email integration](./email.md), which owns the SMTP settings, for a batch that could not be delivered and, on the file channel, for one that was delivered but carried record errors. Notification never fails a batch: an unreachable mail server is logged and swallowed.

## Configuration

### refarch-pscd-integration-starter

| Property                        | Description                                                                     |
| ------------------------------- | ------------------------------------------------------------------------------- |
| `refarch.pscd.client.url`       | PSCD SOAP endpoint the batches are delivered to. Mandatory                      |
| `refarch.pscd.client.username`  | Username for HTTP Basic against that endpoint. Optional; unset sends no credentials |
| `refarch.pscd.client.password`  | Password for the above, sent preemptively. Supply from a secret, not a committed file |

### refarch-pscd-integration-service

| Property                                       | Description                                                                              |
| ---------------------------------------------- | ---------------------------------------------------------------------------------------- |
| `refarch.pscd.inbound.security.username`       | Account the sending systems authenticate with. **Mandatory**, no default                 |
| `refarch.pscd.inbound.security.password`       | Password for the above. **Mandatory**. Plain text unless it carries a `{bcrypt}` prefix   |
| `refarch.pscd.inbound.rest.enabled`            | Expose `POST /api/pscd/batches`. Default `true`                                          |
| `refarch.pscd.inbound.soap.enabled`            | Expose the SOAP endpoint. Default `true`                                                 |
| `refarch.pscd.inbound.file.enabled`            | Poll a directory for flat-file batches. Default `false`                                  |
| `refarch.pscd.inbound.file.directory`          | Directory polled. Default `./pscd-inbox`                                                 |
| `refarch.pscd.inbound.file.stable-for`         | How long a file must stop changing before it is taken up. `0` takes it up on sight. Default `2s` |
| `refarch.pscd.inbound.file.working-directory`  | Subdirectory a batch is held in while it is processed. Default `.working`                |
| `refarch.pscd.inbound.file.done-directory`     | Subdirectory a delivered batch is moved to. Default `.done`                              |
| `refarch.pscd.inbound.file.error-directory`    | Subdirectory a failed batch is moved to. Default `.error`                                |
| `refarch.pscd.inbound.file.poll-interval`      | Milliseconds between the end of one poll and the start of the next. Default `1000`       |
| `refarch.pscd.inbound.file.charset`            | Charset the batch files are decoded with. Default `ISO-8859-1`                           |
| `refarch.pscd.notification.to`                 | Recipients of the failure mail, comma separated. Nothing is sent while blank             |
| `refarch.pscd.notification.subject`            | Subject when the batch was not delivered at all                                          |
| `refarch.pscd.notification.record-error-subject` | Subject when the batch was delivered but carried record errors                         |
| `refarch.pscd.notification.template`           | Spring resource location of the mail body template. Default `classpath:mail/pscd-failure.txt` |
| `refarch.pscd.notification.file.enabled`       | Notify for the file channel. Default `false`                                             |
| `refarch.pscd.notification.rest.enabled`       | Notify for the REST channel. Default `false`                                             |
| `refarch.pscd.notification.soap.enabled`       | Notify for the SOAP channel. Default `false`                                             |
| `spring.webservices.path`                      | Where the SOAP endpoint and the contract artefacts are served. Pinned to `/ws/pscd`      |
| `PSCD_LOG_DIR`                                 | Where the two accounting trails are written. Default `logs`                              |

The SMTP server (`spring.mail.*`) and sender address (`refarch.mail.from-address`) belong to the [email integration](./email.md).
