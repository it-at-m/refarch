package de.muenchen.refarch.integration.dms.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.refarch.integration.dms.application.port.out.SearchSubjectAreaOutPort;
import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import java.util.List;
import org.junit.jupiter.api.Test;

class SearchSubjectAreaUseCaseTest {

    private final SearchSubjectAreaOutPort searchSubjectAreaOutPort = mock(SearchSubjectAreaOutPort.class);

    private final SearchSubjectAreaUseCase searchSubjectAreaUseCase = new SearchSubjectAreaUseCase(searchSubjectAreaOutPort);

    @Test
    void searchSubject() throws DmsException {

        when(searchSubjectAreaOutPort.searchSubjectArea(any(), any())).thenReturn(List.of("coo"));

        searchSubjectAreaUseCase.searchSubjectArea("searchString", "user");

        verify(this.searchSubjectAreaOutPort, times(1)).searchSubjectArea("searchString", "user");
    }

}
