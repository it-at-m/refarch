package de.muenchen.refarch.email.model;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mail {

    @NotBlank
    private String receivers;
    @NotBlank
    private String subject;
    @NotBlank
    private String body;
    @Builder.Default
    private boolean htmlBody = false;
    private String replyTo;
    private String receiversCc;
    private String receiversBcc;
    private String sender;
    private List<FileAttachment> attachments;

    public boolean hasAttachement() {
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
