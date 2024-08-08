package de.muenchen.refarch.s3.integration.configuration.nfcconverter;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

/**
 * Helper class for NFC normalisation.
 *
 * @see Normalizer
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class NfcHelper {

    /**
     * Converting a string into the canonical Unicode normal form (NFC)
     *
     * @param in Input string
     * @return Normalised string
     * @see Normalizer#normalize(CharSequence, Normalizer.Form)
     */
    public static String nfcConverter(final String in) {
        if (in == null) {
            log.debug("String BEFORE nfc conversion is \"null\".");
            return null;
        }

        log.debug("String BEFORE nfc conversion: \"{}\".", in);
        log.debug("Length of String BEFORE nfc conversion: {}.", in.length());
        final String nfcConvertedContent = Normalizer.normalize(in, Normalizer.Form.NFC);
        log.debug("String AFTER nfc conversion: \"{}\".", nfcConvertedContent);
        log.debug("Length of String AFTER nfc conversion: {}.", nfcConvertedContent.length());
        return nfcConvertedContent;
    }

    /**
     * Convert {@link StringBuffer} content to canonical Unicode normal form (NFC)
     *
     * @param in Input
     * @return Normalised content
     * @see #nfcConverter(String)
     * @see Normalizer#normalize(CharSequence, Normalizer.Form)
     */
    public static StringBuffer nfcConverter(final StringBuffer in) {
        return new StringBuffer(nfcConverter(in.toString()));
    }

    /**
     * Converting an array of strings into the canonical Unicode normal form (NFC)
     *
     * @param original Input array
     * @return Array with normalised content
     * @see #nfcConverter(String)
     * @see Normalizer#normalize(CharSequence, Normalizer.Form)
     */
    public static String[] nfcConverter(final String[] original) {
        return Arrays.stream(original)
                .map(NfcHelper::nfcConverter)
                .toArray(String[]::new);
    }

    /**
     * Convert a {@link Map} of strings into the canonical Unicode normal form (NFC).
     *
     * @param original Input map
     * @return Map with normalised content
     * @see #nfcConverter(String)
     * @see Normalizer#normalize(CharSequence, Normalizer.Form)
     */
    public static Map<String, String[]> nfcConverter(final Map<String, String[]> original) {
        final HashMap<String, String[]> nfcConverted = new HashMap<>(original.size());
        original.forEach((nfdKey, nfdValueArray) -> nfcConverted.put(
                nfcConverter(nfdKey),
                nfcConverter(nfdValueArray)));
        return nfcConverted;
    }

    /**
     * Convert a {@link Cookie} to the canonical Unicode normal form (NFC).
     *
     * @param original Cookie
     * @return Cookie with normalised content
     * @see #nfcConverter(String)
     * @see Normalizer#normalize(CharSequence, Normalizer.Form)
     */
    public static Cookie nfcConverter(Cookie original) {
        final Cookie nfcCookie = new Cookie(NfcHelper.nfcConverter(original.getName()), NfcHelper.nfcConverter(original.getValue()));
        if (original.getDomain() != null) {
            nfcCookie.setDomain(NfcHelper.nfcConverter(original.getDomain()));
        }
        nfcCookie.setPath(NfcHelper.nfcConverter(original.getPath()));
        return nfcCookie;
    }

    /**
     * Convert an array of {@link Cookie}s into the canonical Unicode normal form (NFC).
     *
     * @param original Cookies
     * @return Cookies with normalised content
     * @see #nfcConverter(String)
     * @see Normalizer#normalize(CharSequence, Normalizer.Form)
     */
    public static Cookie[] nfcConverter(final Cookie[] original) {
        if (original == null) {
            return null;
        }
        return Arrays.stream(original)
                .map(NfcHelper::nfcConverter)
                .toArray(Cookie[]::new);
    }

    /**
     * Convert the headers of a {@link HttpServletRequest} from strings to the canonical Unicode normal form (NFC).
     *
     * @param originalRequest The {@link HttpServletRequest} for extracting and converting the headers
     * @return Map with normalised content
     * @see #nfcConverter(String)
     * @see Normalizer#normalize(CharSequence, Normalizer.Form)
     */
    public static Map<String, List<String>> nfcConverterForHeadersFromOriginalRequest(final HttpServletRequest originalRequest) {
        final Map<String, List<String>> converted = new CaseInsensitiveMap<>();
        Collections.list(originalRequest.getHeaderNames()).forEach(nfdHeaderName -> {
            final String nfcHeaderName = NfcHelper.nfcConverter(nfdHeaderName);
            final List<String> nfcHeaderEntries = Collections.list(originalRequest.getHeaders(nfdHeaderName)).stream()
                    .map(NfcHelper::nfcConverter)
                    .collect(Collectors.toList());
            converted.put(nfcHeaderName, nfcHeaderEntries);
        });
        return converted;
    }

}
