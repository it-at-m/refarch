package de.muenchen.refarch.email.integration.domain.model.paths;

import de.muenchen.refarch.email.integration.domain.model.BasicMail;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class BasicMailPaths extends BasicMail {
    // TODO use List
    private List<String> filePaths;

    public BasicMailPaths(String receivers, String receiversCc, String receiversBcc, String subject, String replyTo, List<String> filePaths) {
        super(receivers, receiversCc, receiversBcc, subject, replyTo);
        this.filePaths = filePaths;
    }
}
