package de.muenchen.oss.digiwf.dms.integration.adapter.in.streaming;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CancelObjectDto {

    private String objectCoo;

    private String user;

}
