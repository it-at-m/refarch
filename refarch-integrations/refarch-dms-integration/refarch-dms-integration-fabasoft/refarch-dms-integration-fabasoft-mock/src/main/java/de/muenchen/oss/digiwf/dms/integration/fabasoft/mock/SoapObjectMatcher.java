package de.muenchen.oss.digiwf.dms.integration.fabasoft.mock;

import static de.muenchen.oss.digiwf.dms.integration.fabasoft.mock.WiremockWsdlUtility.deserializeSoapRequest;

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SoapObjectMatcher<T> extends RequestMatcherExtension {

    private final Class<T> clazz;
    private final String operation;
    private final Predicate<T> predicate;

    @Override
    public MatchResult match(Request request, Parameters parameters) {

        if (!request.getHeader("SOAPAction").equals("\"http://schemas.fabasoft.com/object/LHMBAI_15_1700_" + operation + "\"")) {
            return MatchResult.noMatch();
        }

        T object = deserializeSoapRequest(request.getBodyAsString(), clazz);
        boolean erg = predicate.test(object);
        return erg ? MatchResult.exactMatch() : MatchResult.noMatch();
    }
}
