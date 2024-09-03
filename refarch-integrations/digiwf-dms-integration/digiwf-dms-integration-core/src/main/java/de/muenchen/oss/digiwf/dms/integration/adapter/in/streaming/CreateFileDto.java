package de.muenchen.oss.digiwf.dms.integration.adapter.in.streaming;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CreateFileDto {

    private String apentryCOO;

    private String title;

    private String user;

}
