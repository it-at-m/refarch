package de.muenchen.refarch.integration.dms.application.usecase;

import de.muenchen.refarch.integration.dms.application.port.out.SearchFileOutPort;
import de.muenchen.refarch.integration.dms.application.usecase.SearchFileUseCase;
import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SearchFileUseCaseTest {

    private final SearchFileOutPort searchFileOutPort = mock(SearchFileOutPort.class);

    private final SearchFileUseCase searchFileUseCase = new SearchFileUseCase(searchFileOutPort);

    @Test
    void searchFile() throws DmsException {

        when(searchFileOutPort.searchFile(any(), any(), any(), any())).thenReturn(List.of("coo"));

        searchFileUseCase.searchFile("searchString", "user", "reference", "value");

        verify(this.searchFileOutPort, times(1)).searchFile("searchString", "user", "reference", "value");
    }

}
