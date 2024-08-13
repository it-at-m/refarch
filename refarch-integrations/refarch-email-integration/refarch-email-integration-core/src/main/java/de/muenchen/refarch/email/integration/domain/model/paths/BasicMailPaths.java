package de.muenchen.refarch.email.integration.domain.model.paths;

import de.muenchen.refarch.email.integration.domain.model.BasicMail;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@EqualsAndHashCode(callSuper = true)
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class BasicMailPaths extends BasicMail {
    private String filePaths;

    public BasicMailPaths(String receivers, String receiversCc, String receiversBcc, String subject, String replyTo, String fileContext, String filePaths) {
        super(receivers, receiversCc, receiversBcc, subject, replyTo);
        this.filePaths = filePaths;
    }

    public List<String> parseFilePaths() {
        if (StringUtils.isBlank(filePaths))
            return List.of();
        return List.of(filePaths.split("[,;]"));
    }
}
