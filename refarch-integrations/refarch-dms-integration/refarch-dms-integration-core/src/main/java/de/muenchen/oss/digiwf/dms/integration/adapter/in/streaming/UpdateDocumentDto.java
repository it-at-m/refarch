package de.muenchen.oss.digiwf.dms.integration.adapter.in.streaming;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class UpdateDocumentDto {

    private String documentCoo;

    private String user;

    private String type;

    private String filepaths;

    private String fileContext;

    public List<String> getFilepathsAsList() {
        return Arrays.asList(filepaths.split(","));
    }

}