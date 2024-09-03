package de.muenchen.refarch.integration.dms.application.usecase;

import de.muenchen.refarch.integration.dms.application.port.out.SearchSubjectAreaOutPort;
import de.muenchen.refarch.integration.dms.application.usecase.SearchSubjectAreaUseCase;
import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
