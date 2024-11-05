package de.muenchen.refarch.integration.dms.adapter.out.fabasoft;

import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.ArrayOfLHMBAI151700GIObjectType;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CancelObjectGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CancelObjectGIResponse;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CreateFileGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CreateFileGIResponse;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CreateIncomingGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CreateIncomingGIResponse;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CreateInternalGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CreateInternalGIResponse;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CreateOutgoingGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CreateOutgoingGIResponse;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CreateProcedureGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CreateProcedureGIResponse;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.DepositObjectGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.DepositObjectGIResponse;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.LHMBAI151700GIAttachmentType;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.LHMBAI151700GIMetadataType;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.LHMBAI151700GIObjectType;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.LHMBAI151700GIWSDSoap;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.ReadContentObjectGIResponse;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.ReadContentObjectMetaDataGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.ReadContentObjectMetaDataGIResponse;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.ReadDocumentGIObjectsResponse;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.ReadMetadataObjectGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.ReadMetadataObjectGIResponse;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.SearchObjNameGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.SearchObjNameGIResponse;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.UpdateIncomingGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.UpdateIncomingGIResponse;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.UpdateInternalGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.UpdateInternalGIResponse;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.UpdateOutgoingGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.UpdateOutgoingGIResponse;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.dms.domain.model.Content;
import de.muenchen.refarch.integration.dms.domain.model.Document;
import de.muenchen.refarch.integration.dms.domain.model.DocumentType;
import de.muenchen.refarch.integration.dms.domain.model.File;
import de.muenchen.refarch.integration.dms.domain.model.Metadata;
import de.muenchen.refarch.integration.dms.domain.model.Procedure;
import de.muenchen.refarch.integration.dms.fabasoft.mock.FabasoftClienFactory;
import de.muenchen.refarch.integration.dms.fabasoft.mock.WiremockWsdlUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@WireMockTest()
@SuppressWarnings({ "PMD.UnitTestShouldIncludeAssert", "PMD.CouplingBetweenObjects" })
class FabasoftAdapterTest {

    public static final String USER = "user";
    public static final String OBJID = "1234567890";
    public static final byte[] CONTENT = "content".getBytes();
    public static final String NAME = "name";
    public static final String EXTENSION = "extension";
    public static final String DOCUMENT_COO = "documentCOO";

    private final FabasoftProperties properties = new FabasoftProperties();
    private FabasoftAdapter fabasoftAdapter;

    @BeforeEach
    public void setUp(final WireMockRuntimeInfo wmRuntimeInfo) {
        this.properties.setUsername(USER);
        this.properties.setPassword("password");
        this.properties.setBusinessapp("businessapp");
        this.properties.setUiUrl("uiurl");
        final LHMBAI151700GIWSDSoap soapClient = FabasoftClienFactory.dmsWsClient("http://localhost:" + wmRuntimeInfo.getHttpPort() + "/");
        fabasoftAdapter = new FabasoftAdapter(properties, soapClient);
    }

    @Test
    void executeCreateFileRequest() throws DmsException {
        final CreateFileGIResponse response = new CreateFileGIResponse();
        response.setObjid(OBJID);

        WiremockWsdlUtility.stubOperation(
                "CreateFileGI",
                CreateFileGI.class, (u) -> "new file".equals(u.getShortname()),
                response);

        final File file = new File("apentryCOO", "new file");

        final String procedureResponse = fabasoftAdapter.createFile(file, USER);

        assertEquals(procedureResponse, OBJID);
    }

    @Test
    void executeCreateProcedureRequest() throws DmsException {
        final CreateProcedureGIResponse response = new CreateProcedureGIResponse();
        response.setObjid(OBJID);

        WiremockWsdlUtility.stubOperation(
                "CreateProcedureGI",
                CreateProcedureGI.class, (u) -> "new procedure".equals(u.getShortname()),
                response);

        final Procedure procedure = new Procedure("fileCOO", "new procedure", "custom file subject");

        final Procedure procedureResponse = fabasoftAdapter.createProcedure(procedure, USER);

        assertEquals(procedureResponse.coo(), OBJID);
    }

    @Test
    void executeDepositObjectRequest() throws DmsException {
        final DepositObjectGIResponse response = new DepositObjectGIResponse();
        response.setObjid("objectCoo");

        WiremockWsdlUtility.stubOperation(
                "DepositObjectGI",
                DepositObjectGI.class, (u) -> true,
                response);

        fabasoftAdapter.depositObject("objectCoo", USER);
    }

    @Test
    void executeCreateIncomingDocumentRequest() throws DmsException {
        final Content content = new Content(EXTENSION, NAME, CONTENT);

        final CreateIncomingGIResponse response = new CreateIncomingGIResponse();
        response.setObjid(DOCUMENT_COO);

        WiremockWsdlUtility.stubOperation(
                "CreateIncomingGI",
                CreateIncomingGI.class, (u) -> true,
                response);

        final String documentResponse = fabasoftAdapter
                .createDocument(new Document("procedureCOO", "title", LocalDate.parse("2023-12-31"), DocumentType.EINGEHEND, List.of(content)), USER);

        assertEquals(documentResponse, DOCUMENT_COO);
    }

    @Test
    void executeCreateOutgoingDocumentRequest() throws DmsException {
        final Content content = new Content(EXTENSION, NAME, CONTENT);

        final CreateOutgoingGIResponse response = new CreateOutgoingGIResponse();
        response.setObjid(DOCUMENT_COO);

        WiremockWsdlUtility.stubOperation(
                "CreateOutgoingGI",
                CreateOutgoingGI.class, (u) -> true,
                response);

        final String documentResponse = fabasoftAdapter
                .createDocument(new Document("procedureCOO", "title", LocalDate.parse("2023-12-31"), DocumentType.AUSGEHEND, List.of(content)), USER);

        assertEquals(documentResponse, DOCUMENT_COO);
    }

    @Test
    void executeCreateInternalDocumentRequest() throws DmsException {
        final Content content = new Content(EXTENSION, NAME, CONTENT);

        final CreateInternalGIResponse response = new CreateInternalGIResponse();
        response.setObjid(DOCUMENT_COO);

        WiremockWsdlUtility.stubOperation(
                "CreateInternalGI",
                CreateInternalGI.class, (u) -> true,
                response);

        final String documentResponse = fabasoftAdapter
                .createDocument(new Document("procedureCOO", "title", LocalDate.parse("2023-12-31"), DocumentType.INTERN, List.of(content)), USER);

        assertEquals(documentResponse, DOCUMENT_COO);
    }

    @Test
    void executeUpdateIncomingDocumentRequest() throws DmsException {
        final Content content = new Content(EXTENSION, NAME, CONTENT);

        final UpdateIncomingGIResponse response = new UpdateIncomingGIResponse();
        response.setObjid(DOCUMENT_COO);

        WiremockWsdlUtility.stubOperation(
                "UpdateIncomingGI",
                UpdateIncomingGI.class, (u) -> true,
                response);

        fabasoftAdapter.updateDocument(DOCUMENT_COO, DocumentType.EINGEHEND, List.of(content), USER);
    }

    @Test
    void executeUpdateOutgoingDocumentRequest() throws DmsException {
        final Content content = new Content(EXTENSION, NAME, CONTENT);

        final UpdateOutgoingGIResponse response = new UpdateOutgoingGIResponse();
        response.setObjid(DOCUMENT_COO);

        WiremockWsdlUtility.stubOperation(
                "UpdateOutgoingGI",
                UpdateOutgoingGI.class, (u) -> true,
                response);

        fabasoftAdapter.updateDocument(DOCUMENT_COO, DocumentType.AUSGEHEND, List.of(content), USER);
    }

    @Test
    void executeUpdateInternalDocumentRequest() throws DmsException {
        final Content content = new Content(EXTENSION, NAME, CONTENT);

        final UpdateInternalGIResponse response = new UpdateInternalGIResponse();
        response.setObjid(DOCUMENT_COO);

        WiremockWsdlUtility.stubOperation(
                "UpdateInternalGI",
                UpdateInternalGI.class, (u) -> true,
                response);

        fabasoftAdapter.updateDocument(DOCUMENT_COO, DocumentType.INTERN, List.of(content), USER);
    }

    @Test
    void executeCancelObjectRequest() throws DmsException {
        final CancelObjectGIResponse response = new CancelObjectGIResponse();
        response.setStatus(0);

        WiremockWsdlUtility.stubOperation(
                "CancelObjectGI",
                CancelObjectGI.class, (u) -> true,
                response);

        fabasoftAdapter.cancelObject("objectCoo", USER);
    }

    @Test
    void executeListRiles() throws DmsException {
        final LHMBAI151700GIObjectType file1 = new LHMBAI151700GIObjectType();
        file1.setLHMBAI151700Objaddress("contentCoo1");
        file1.setLHMBAI151700Objname("File-Name");
        final ArrayOfLHMBAI151700GIObjectType content = new ArrayOfLHMBAI151700GIObjectType();
        content.getLHMBAI151700GIObjectType().add(file1);

        final ReadDocumentGIObjectsResponse response = new ReadDocumentGIObjectsResponse();
        response.setStatus(0);
        response.setGiobjecttype(content);

        WiremockWsdlUtility.stubOperation(
                "ReadDocumentGIObjects",
                CancelObjectGI.class, (u) -> true,
                response);

        final List<String> contentCoos = fabasoftAdapter.listContentCoos("coo1", USER);

        final List<String> expectedCoos = List.of("contentCoo1");

        assertThat(contentCoos.size()).isEqualTo(1);
        assertEquals(expectedCoos, contentCoos);
    }

    @Test
    void executeReadFiles() throws DmsException {
        final LHMBAI151700GIAttachmentType content = new LHMBAI151700GIAttachmentType();
        content.setLHMBAI151700Filename("filename");
        content.setLHMBAI151700Fileextension(EXTENSION);
        content.setLHMBAI151700Filecontent(CONTENT);

        final ReadContentObjectGIResponse response = new ReadContentObjectGIResponse();
        response.setStatus(0);
        response.setGiattachmenttype(content);

        WiremockWsdlUtility.stubOperation(
                "ReadContentObjectGI",
                CancelObjectGI.class, (u) -> true,
                response);

        final List<Content> files = fabasoftAdapter.readContent(List.of("coo1"), USER);

        final Content expectedFile = new Content(EXTENSION, "filename", CONTENT);

        assertThat(files.size()).isEqualTo(1);
        assertThat(files.getFirst()).usingRecursiveComparison().isEqualTo(expectedFile);
    }

    /**
     * Tests a file search.
     */
    @Test
    void executeSearchFileRequest() throws DmsException {
        internalSearchFileCallTest(DMSObjectClass.Sachakte, "searchString", USER, null, null);
    }

    /**
     * Tests a file search but includes refinement on a business date/'Fachdatum'.
     */
    @Test
    void executeSearchFileRequestBusinessData() throws DmsException {
        internalSearchFileCallTest(DMSObjectClass.Sachakte, "searchString", USER, "reference", "value");
    }

    /**
     * Tests a subject search.
     */
    @Test
    void executeSearchSubjectAreaRequest() throws DmsException {
        final LHMBAI151700GIObjectType file = new LHMBAI151700GIObjectType();
        file.setLHMBAI151700Objaddress("testCoo");
        file.setLHMBAI151700Objname("testName");

        final ArrayOfLHMBAI151700GIObjectType array = new ArrayOfLHMBAI151700GIObjectType();
        array.getLHMBAI151700GIObjectType().add(file);

        final SearchObjNameGIResponse response = new SearchObjNameGIResponse();
        response.setStatus(0);
        response.setGiobjecttype(array);

        WiremockWsdlUtility.stubOperation(
                "SearchObjNameGI",
                SearchObjNameGI.class, (u) -> u.getObjclass().equals(DMSObjectClass.Aktenplaneintrag.getName()),
                response);

        final List<String> files = fabasoftAdapter.searchSubjectArea("searchString", USER);

        assertThat(files.size()).isEqualTo(1);
    }

    private void internalSearchFileCallTest(final DMSObjectClass dmsObjectClass, final String searchString, final String user, final String reference,
            final String value)
            throws DmsException {
        final LHMBAI151700GIObjectType file = new LHMBAI151700GIObjectType();
        file.setLHMBAI151700Objaddress("testCoo");
        file.setLHMBAI151700Objname("testName");

        final ArrayOfLHMBAI151700GIObjectType array = new ArrayOfLHMBAI151700GIObjectType();
        array.getLHMBAI151700GIObjectType().add(file);

        final SearchObjNameGIResponse response = new SearchObjNameGIResponse();
        response.setStatus(0);
        response.setGiobjecttype(array);

        WiremockWsdlUtility.stubOperation(
                "SearchObjNameGI",
                SearchObjNameGI.class, (u) -> u.getObjclass().equals(dmsObjectClass.getName()) && validateBusinessData(u),
                response);

        final List<String> files = fabasoftAdapter.searchFile(searchString, user, reference, value);

        assertThat(files.size()).isEqualTo(1);
    }

    private boolean validateBusinessData(final SearchObjNameGI searchObjNameGI) {
        final String reference = Optional.ofNullable(searchObjNameGI.getReference()).orElse("");
        final String value = Optional.ofNullable(searchObjNameGI.getValue()).orElse("");
        if (reference.isEmpty()) {
            return true;
        }
        if (value.isEmpty()) {
            return false;
        }
        return !"reference".equals(reference) || "value".equals(value);
    }

    @Test
    void executeReadMetadata() throws DmsException {
        final ReadMetadataObjectGIResponse response = new ReadMetadataObjectGIResponse();
        response.setStatus(0);
        response.setObjclass("Vorgang");
        response.setObjname(NAME);

        WiremockWsdlUtility.stubOperation(
                "ReadMetadataObjectGI",
                ReadMetadataObjectGI.class, (u) -> true,
                response);

        final Metadata metadata = fabasoftAdapter.readMetadata("coo", USER);

        assertThat(metadata.name()).isEqualTo(NAME);
        assertThat(metadata.type()).isEqualTo("Vorgang");
    }

    @Test
    void executeReadContentMetadata() throws DmsException {
        final LHMBAI151700GIMetadataType content = new LHMBAI151700GIMetadataType();
        content.setLHMBAI151700Filename(NAME);
        content.setLHMBAI151700Objclass("pdf");

        final ReadContentObjectMetaDataGIResponse response = new ReadContentObjectMetaDataGIResponse();
        response.setStatus(0);
        response.setGimetadatatype(content);

        WiremockWsdlUtility.stubOperation(
                "ReadContentObjectMetaDataGI",
                ReadContentObjectMetaDataGI.class, (u) -> true,
                response);

        final Metadata metadata = fabasoftAdapter.readContentMetadata("coo", USER);

        assertThat(metadata.name()).isEqualTo(NAME);
        assertThat(metadata.type()).isEqualTo("pdf");
    }

}
