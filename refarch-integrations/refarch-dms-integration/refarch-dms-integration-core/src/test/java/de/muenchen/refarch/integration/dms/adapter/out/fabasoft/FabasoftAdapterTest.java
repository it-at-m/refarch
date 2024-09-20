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
import de.muenchen.refarch.integration.dms.domain.model.Procedure;
import de.muenchen.refarch.integration.dms.fabasoft.mock.FabasoftClienFactory;
import de.muenchen.refarch.integration.dms.fabasoft.mock.WiremockWsdlUtility;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@WireMockTest()
class FabasoftAdapterTest {

    private final FabasoftProperties properties = new FabasoftProperties();
    private FabasoftAdapter fabasoftAdapter;

    @BeforeEach
    public void setUp(WireMockRuntimeInfo wmRuntimeInfo) {
        this.properties.setUsername("user");
        this.properties.setPassword("password");
        this.properties.setBusinessapp("businessapp");
        this.properties.setUiUrl("uiurl");
        val soapClient = FabasoftClienFactory.dmsWsClient("http://localhost:" + wmRuntimeInfo.getHttpPort() + "/");
        fabasoftAdapter = new FabasoftAdapter(properties, soapClient);
    }

    @Test
    void execute_createFile_request() throws DmsException {
        val response = new CreateFileGIResponse();
        response.setObjid("1234567890");

        WiremockWsdlUtility.stubOperation(
                "CreateFileGI",
                CreateFileGI.class, (u) -> "new file".equals(u.getShortname()),
                response);

        val file = new File("apentryCOO", "new file");

        val procedureResponse = fabasoftAdapter.createFile(file, "user");

        assertEquals(procedureResponse, "1234567890");
    }

    @Test
    void execute_createProcedure_request() throws DmsException {
        val response = new CreateProcedureGIResponse();
        response.setObjid("1234567890");

        WiremockWsdlUtility.stubOperation(
                "CreateProcedureGI",
                CreateProcedureGI.class, (u) -> "new procedure".equals(u.getShortname()),
                response);

        val procedure = new Procedure("fileCOO", "new procedure", "custom file subject");

        val procedureResponse = fabasoftAdapter.createProcedure(procedure, "user");

        assertEquals(procedureResponse.coo(), "1234567890");
    }

    @Test
    void execute_depositObject_request() throws DmsException {
        val response = new DepositObjectGIResponse();
        response.setObjid("objectCoo");

        WiremockWsdlUtility.stubOperation(
                "DepositObjectGI",
                DepositObjectGI.class, (u) -> true,
                response);

        fabasoftAdapter.depositObject("objectCoo", "user");
    }

    @Test
    void execute_createIncomingDocument_request() throws DmsException {
        Content content = new Content("extension", "name", "content".getBytes());

        val response = new CreateIncomingGIResponse();
        response.setObjid("documentCOO");

        WiremockWsdlUtility.stubOperation(
                "CreateIncomingGI",
                CreateIncomingGI.class, (u) -> true,
                response);

        val documentResponse = fabasoftAdapter
                .createDocument(new Document("procedureCOO", "title", LocalDate.parse("2023-12-31"), DocumentType.EINGEHEND, List.of(content)), "user");

        assertEquals(documentResponse, "documentCOO");
    }

    @Test
    void execute_createOutgoingDocument_request() throws DmsException {
        Content content = new Content("extension", "name", "content".getBytes());

        val response = new CreateOutgoingGIResponse();
        response.setObjid("documentCOO");

        WiremockWsdlUtility.stubOperation(
                "CreateOutgoingGI",
                CreateOutgoingGI.class, (u) -> true,
                response);

        val documentResponse = fabasoftAdapter
                .createDocument(new Document("procedureCOO", "title", LocalDate.parse("2023-12-31"), DocumentType.AUSGEHEND, List.of(content)), "user");

        assertEquals(documentResponse, "documentCOO");
    }

    @Test
    void execute_createInternalDocument_request() throws DmsException {
        Content content = new Content("extension", "name", "content".getBytes());

        val response = new CreateInternalGIResponse();
        response.setObjid("documentCOO");

        WiremockWsdlUtility.stubOperation(
                "CreateInternalGI",
                CreateInternalGI.class, (u) -> true,
                response);

        val documentResponse = fabasoftAdapter
                .createDocument(new Document("procedureCOO", "title", LocalDate.parse("2023-12-31"), DocumentType.INTERN, List.of(content)), "user");

        assertEquals(documentResponse, "documentCOO");
    }

    @Test
    void execute_updateIncomingDocument_request() throws DmsException {
        Content content = new Content("extension", "name", "content".getBytes());

        val response = new UpdateIncomingGIResponse();
        response.setObjid("documentCOO");

        WiremockWsdlUtility.stubOperation(
                "UpdateIncomingGI",
                UpdateIncomingGI.class, (u) -> true,
                response);

        fabasoftAdapter.updateDocument("documentCOO", DocumentType.EINGEHEND, List.of(content), "user");
    }

    @Test
    void execute_updateOutgoingDocument_request() throws DmsException {
        Content content = new Content("extension", "name", "content".getBytes());

        val response = new UpdateOutgoingGIResponse();
        response.setObjid("documentCOO");

        WiremockWsdlUtility.stubOperation(
                "UpdateOutgoingGI",
                UpdateOutgoingGI.class, (u) -> true,
                response);

        fabasoftAdapter.updateDocument("documentCOO", DocumentType.AUSGEHEND, List.of(content), "user");
    }

    @Test
    void execute_updateInternalDocument_request() throws DmsException {
        Content content = new Content("extension", "name", "content".getBytes());

        val response = new UpdateInternalGIResponse();
        response.setObjid("documentCOO");

        WiremockWsdlUtility.stubOperation(
                "UpdateInternalGI",
                UpdateInternalGI.class, (u) -> true,
                response);

        fabasoftAdapter.updateDocument("documentCOO", DocumentType.INTERN, List.of(content), "user");
    }

    @Test
    void execute_cancelObject_request() throws DmsException {
        val response = new CancelObjectGIResponse();
        response.setStatus(0);

        WiremockWsdlUtility.stubOperation(
                "CancelObjectGI",
                CancelObjectGI.class, (u) -> true,
                response);

        fabasoftAdapter.cancelObject("objectCoo", "user");
    }

    @Test
    void execute_list_files() throws DmsException {
        val file1 = new LHMBAI151700GIObjectType();
        file1.setLHMBAI151700Objaddress("contentCoo1");
        file1.setLHMBAI151700Objname("File-Name");
        val content = new ArrayOfLHMBAI151700GIObjectType();
        content.getLHMBAI151700GIObjectType().add(file1);

        val response = new ReadDocumentGIObjectsResponse();
        response.setStatus(0);
        response.setGiobjecttype(content);

        WiremockWsdlUtility.stubOperation(
                "ReadDocumentGIObjects",
                CancelObjectGI.class, (u) -> true,
                response);

        val contentCoos = fabasoftAdapter.listContentCoos("coo1", "user");

        val expectedCoos = List.of("contentCoo1");

        assertThat(contentCoos.size()).isEqualTo(1);
        assertEquals(expectedCoos, contentCoos);
    }

    @Test
    void execute_read_files() throws DmsException {
        val content = new LHMBAI151700GIAttachmentType();
        content.setLHMBAI151700Filename("filename");
        content.setLHMBAI151700Fileextension("extension");
        content.setLHMBAI151700Filecontent("content".getBytes());

        val response = new ReadContentObjectGIResponse();
        response.setStatus(0);
        response.setGiattachmenttype(content);

        WiremockWsdlUtility.stubOperation(
                "ReadContentObjectGI",
                CancelObjectGI.class, (u) -> true,
                response);

        val files = fabasoftAdapter.readContent(List.of("coo1"), "user");

        val expectedFile = new Content("extension", "filename", "content".getBytes());

        assertThat(files.size()).isEqualTo(1);
        assertThat(files.getFirst()).usingRecursiveComparison().isEqualTo(expectedFile);
    }

    /**
     * Tests a file search.
     */
    @Test
    void execute_searchFile_request() throws DmsException {
        internalSearchFileCallTest(DMSObjectClass.Sachakte, "searchString", "user", null, null);
    }

    /**
     * Tests a file search but includes refinement on a business date/'Fachdatum'.
     */
    @Test
    void execute_searchFile_request_business_data() throws DmsException {
        internalSearchFileCallTest(DMSObjectClass.Sachakte, "searchString", "user", "reference", "value");
    }

    /**
     * Tests a subject search.
     */
    @Test
    void execute_searchSubjectArea_request() throws DmsException {
        val file = new LHMBAI151700GIObjectType();
        file.setLHMBAI151700Objaddress("testCoo");
        file.setLHMBAI151700Objname("testName");

        val array = new ArrayOfLHMBAI151700GIObjectType();
        array.getLHMBAI151700GIObjectType().add(file);

        val response = new SearchObjNameGIResponse();
        response.setStatus(0);
        response.setGiobjecttype(array);

        WiremockWsdlUtility.stubOperation(
                "SearchObjNameGI",
                SearchObjNameGI.class, (u) -> u.getObjclass().equals(DMSObjectClass.Aktenplaneintrag.getName()),
                response);

        val files = fabasoftAdapter.searchSubjectArea("searchString", "user");

        assertThat(files.size()).isEqualTo(1);
    }

    private void internalSearchFileCallTest(final DMSObjectClass dmsObjectClass, final String searchString, final String user, final String reference,
            final String value)
            throws DmsException {
        val file = new LHMBAI151700GIObjectType();
        file.setLHMBAI151700Objaddress("testCoo");
        file.setLHMBAI151700Objname("testName");

        val array = new ArrayOfLHMBAI151700GIObjectType();
        array.getLHMBAI151700GIObjectType().add(file);

        val response = new SearchObjNameGIResponse();
        response.setStatus(0);
        response.setGiobjecttype(array);

        WiremockWsdlUtility.stubOperation(
                "SearchObjNameGI",
                SearchObjNameGI.class, (u) -> u.getObjclass().equals(dmsObjectClass.getName()) && validateBusinessData(u),
                response);

        val files = fabasoftAdapter.searchFile(searchString, user, reference, value);

        assertThat(files.size()).isEqualTo(1);
    }

    private boolean validateBusinessData(final SearchObjNameGI searchObjNameGI) {
        val reference = Optional.ofNullable(searchObjNameGI.getReference()).orElse("");
        val value = Optional.ofNullable(searchObjNameGI.getValue()).orElse("");
        if (reference.isEmpty()) return true;
        if (value.isEmpty()) return false;
        return !reference.equals("reference") || value.equals("value");
    }

    @Test
    void execute_read_metadata() throws DmsException {

        val response = new ReadMetadataObjectGIResponse();
        response.setStatus(0);
        response.setObjclass("Vorgang");
        response.setObjname("name");

        WiremockWsdlUtility.stubOperation(
                "ReadMetadataObjectGI",
                ReadMetadataObjectGI.class, (u) -> true,
                response);

        val metadata = fabasoftAdapter.readMetadata("coo", "user");

        assertThat(metadata.name()).isEqualTo("name");
        assertThat(metadata.type()).isEqualTo("Vorgang");
    }

    @Test
    void execute_read_content_metadata() throws DmsException {
        val content = new LHMBAI151700GIMetadataType();
        content.setLHMBAI151700Filename("name");
        content.setLHMBAI151700Objclass("pdf");

        val response = new ReadContentObjectMetaDataGIResponse();
        response.setStatus(0);
        response.setGimetadatatype(content);

        WiremockWsdlUtility.stubOperation(
                "ReadContentObjectMetaDataGI",
                ReadContentObjectMetaDataGI.class, (u) -> true,
                response);

        val metadata = fabasoftAdapter.readContentMetadata("coo", "user");

        assertThat(metadata.name()).isEqualTo("name");
        assertThat(metadata.type()).isEqualTo("pdf");
    }

}
