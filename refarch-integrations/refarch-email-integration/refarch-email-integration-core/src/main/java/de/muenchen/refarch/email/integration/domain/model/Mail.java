package de.muenchen.refarch.email.integration.domain.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public record Mail(
        @NotBlank String receivers,
        String receiversCc,
        String receiversBcc,
        @NotBlank String subject,
        @NotBlank String body,
        boolean htmlBody,
        String sender,
        String replyTo,
        List<Attachment> attachments) {
    public Mail(@Valid final BasicMail mail, @NotBlank final String body, final boolean htmlBody) {
        this(mail.getReceivers(),
                mail.getReceiversCc(),
                mail.getReceiversBcc(),
                mail.getSubject(),
                body,
                htmlBody,
                null,
                mail.getReplyTo(),
                mail.getAttachments());
    }

    public boolean hasAttachment() {
        return attachments != null && !attachments.isEmpty();
    }

    public boolean hasReplyTo() {
        return StringUtils.isNotBlank(this.replyTo);
    }

    public boolean hasReceiversCc() {
        return StringUtils.isNotBlank(this.receiversCc);
    }

    public boolean hasReceiversBcc() {
        return StringUtils.isNotBlank(this.receiversBcc);
    }

    public boolean hasSender() {
        return StringUtils.isNotBlank(this.sender);
    }
}
