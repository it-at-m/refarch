package de.muenchen.refarch.integration.dms.adapter.out.fabasoft;

import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.ArrayOfLHMBAI151700GIAttachmentType;
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
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.LHMBAI151700GIObjectType;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.LHMBAI151700GIWSDSoap;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.ReadContentObjectGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.ReadContentObjectGIResponse;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.ReadContentObjectMetaDataGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.ReadContentObjectMetaDataGIResponse;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.ReadDocumentGIObjects;
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
import de.muenchen.refarch.integration.dms.application.port.out.CancelObjectOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.CreateDocumentOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.CreateProcedureOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.DepositObjectOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.ListContentOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.ReadContentOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.ReadMetadataOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.SearchFileOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.SearchSubjectAreaOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.UpdateDocumentOutPort;
import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.dms.domain.model.Content;
import de.muenchen.refarch.integration.dms.domain.model.Document;
import de.muenchen.refarch.integration.dms.domain.model.DocumentType;
import de.muenchen.refarch.integration.dms.domain.model.File;
import de.muenchen.refarch.integration.dms.domain.model.Metadata;
import de.muenchen.refarch.integration.dms.domain.model.Procedure;
import de.muenchen.refarch.integration.dms.application.port.out.CreateFileOutPort;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("PMD.UseObjectForClearerAPI")
public class FabasoftAdapter implements
        CreateFileOutPort,
        CreateProcedureOutPort,
        CreateDocumentOutPort,
        UpdateDocumentOutPort,
        DepositObjectOutPort,
        CancelObjectOutPort,
        ListContentOutPort,
        ReadContentOutPort,
        SearchFileOutPort,
        SearchSubjectAreaOutPort,
        ReadMetadataOutPort {

    private final FabasoftProperties properties;
    private final LHMBAI151700GIWSDSoap wsClient;

    private final DMSErrorHandler dmsErrorHandler = new DMSErrorHandler();

    @Override
    public String createFile(final File file, final String user) throws DmsException {
        //logging for dms team
        log.info("calling CreateFileGI Userlogin: {} Apentry: {} Filesubj: {} Shortname: {} Apentrysearch: true", user, file.apentryCOO(), file.title(),
                file.title());

        final CreateFileGI request = new CreateFileGI();
        request.setUserlogin(user);
        request.setBusinessapp(this.properties.getBusinessapp());
        request.setApentry(file.apentryCOO());
        request.setShortname(file.title());
        request.setApentrysearch(true); // looks for free parent entry

        final CreateFileGIResponse response = this.wsClient.createFileGI(request);

        dmsErrorHandler.handleError(response.getStatus(), response.getErrormessage());

        return response.getObjid();
    }

    @Override
    public Procedure createProcedure(final Procedure procedure, final String user) throws DmsException {
        log.info("calling CreateProcedureGI: {}", procedure.toString());

        final CreateProcedureGI request = new CreateProcedureGI();
        request.setUserlogin(user);
        request.setReferrednumber(procedure.fileCOO());
        request.setBusinessapp(this.properties.getBusinessapp());
        request.setShortname(procedure.title());
        if (StringUtils.isNotBlank(procedure.fileSubj())) {
            request.setFilesubj(procedure.fileSubj());
        }
        request.setFiletype("Elektronisch");

        final CreateProcedureGIResponse response = this.wsClient.createProcedureGI(request);

        dmsErrorHandler.handleError(response.getStatus(), response.getErrormessage());

        return new Procedure(response.getObjid(), procedure.fileCOO(), procedure.fileSubj(), procedure.title());
    }

    @Override
    public String createDocument(final Document document, final String user) throws DmsException {
        return switch (document.type()) {
        case EINGEHEND -> this.createIncomingDocument(document, user);
        case AUSGEHEND -> this.createOutgoingDocument(document, user);
        case INTERN -> this.createInternalDocument(document, user);
        };
    }

    private String createIncomingDocument(final Document document, final String user) throws DmsException {
        //logging for dms team
        log.info("calling CreateIncomingGI Userlogin: {} Referrednumber: {} Shortname: {} Filesubj: {}", user, document.procedureCOO(), document.title(),
                document.title());

        final CreateIncomingGI request = new CreateIncomingGI();
        request.setUserlogin(user);
        request.setReferrednumber(document.procedureCOO());
        request.setBusinessapp(this.properties.getBusinessapp());
        request.setShortname(document.title());
        if (document.date() != null) {
            request.setDelivery(this.convertDate(document.date()));
        }

        final ArrayOfLHMBAI151700GIAttachmentType attachmentType = new ArrayOfLHMBAI151700GIAttachmentType();
        final List<LHMBAI151700GIAttachmentType> files = attachmentType.getLHMBAI151700GIAttachmentType();

        for (final Content content : document.contents()) {
            files.add(this.parseContent(content));
        }

        request.setGiattachmenttype(attachmentType);

        final CreateIncomingGIResponse response = this.wsClient.createIncomingGI(request);

        dmsErrorHandler.handleError(response.getStatus(), response.getErrormessage());

        return response.getObjid();
    }

    private String createOutgoingDocument(final Document document, final String user) throws DmsException {
        //logging for dms team
        log.info("calling CreateOutgoingGI Userlogin: {} Referrednumber: {} Shortname: {} Filesubj: {}", user, document.procedureCOO(), document.title(),
                document.title());

        final CreateOutgoingGI request = new CreateOutgoingGI();
        request.setUserlogin(user);
        request.setReferrednumber(document.procedureCOO());
        request.setBusinessapp(this.properties.getBusinessapp());

        request.setShortname(document.title());
        if (document.date() != null) {
            request.setOutgoingdate(this.convertDate(document.date()));
        }

        final ArrayOfLHMBAI151700GIAttachmentType attachmentType = new ArrayOfLHMBAI151700GIAttachmentType();
        final List<LHMBAI151700GIAttachmentType> files = attachmentType.getLHMBAI151700GIAttachmentType();

        for (final Content content : document.contents()) {
            files.add(this.parseContent(content));
        }

        request.setGiattachmenttype(attachmentType);

        final CreateOutgoingGIResponse response = this.wsClient.createOutgoingGI(request);

        dmsErrorHandler.handleError(response.getStatus(), response.getErrormessage());

        return response.getObjid();
    }

    private String createInternalDocument(final Document document, final String user) throws DmsException {
        //logging for dms team
        log.info("calling CreateInternalGI Userlogin: {} Referrednumber: {} Shortname: {} Filesubj: {}", user, document.procedureCOO(), document.title(),
                document.title());

        final CreateInternalGI request = new CreateInternalGI();
        request.setUserlogin(user);
        request.setReferrednumber(document.procedureCOO());
        request.setBusinessapp(this.properties.getBusinessapp());
        request.setShortname(document.title());
        if (document.date() != null) {
            request.setDeliverydate(this.convertDate(document.date()));
        }

        final ArrayOfLHMBAI151700GIAttachmentType attachmentType = new ArrayOfLHMBAI151700GIAttachmentType();
        final List<LHMBAI151700GIAttachmentType> files = attachmentType.getLHMBAI151700GIAttachmentType();

        for (final Content content : document.contents()) {
            files.add(this.parseContent(content));
        }

        request.setGiattachmenttype(attachmentType);

        final CreateInternalGIResponse response = this.wsClient.createInternalGI(request);

        dmsErrorHandler.handleError(response.getStatus(), response.getErrormessage());

        return response.getObjid();
    }

    @Override
    public void updateDocument(final String documentCOO, final DocumentType type, final List<Content> contents, final String user) throws DmsException {
        switch (type) {
        case EINGEHEND:
            this.updateIncomingDocument(documentCOO, contents, user);
            return;
        case AUSGEHEND:
            this.updateOutgoingDocument(documentCOO, contents, user);
            return;
        case INTERN:
            this.updateInternalDocument(documentCOO, contents, user);
            return;
        default:
            throw new AssertionError("must not happen");
        }
    }

    private void updateIncomingDocument(final String documentCOO, final List<Content> contents, final String user) throws DmsException {
        log.info("calling UpdateIncomingGI: {}", documentCOO);

        final UpdateIncomingGI request = new UpdateIncomingGI();
        request.setUserlogin(user);
        request.setObjaddress(documentCOO);
        request.setBusinessapp(this.properties.getBusinessapp());

        final ArrayOfLHMBAI151700GIAttachmentType attachmentType = new ArrayOfLHMBAI151700GIAttachmentType();
        final List<LHMBAI151700GIAttachmentType> files = attachmentType.getLHMBAI151700GIAttachmentType();

        for (final Content content : contents) {
            files.add(this.parseContent(content));
        }

        request.setGiattachmenttype(attachmentType);

        final UpdateIncomingGIResponse response = this.wsClient.updateIncomingGI(request);

        dmsErrorHandler.handleError(response.getStatus(), response.getErrormessage());
    }

    private void updateOutgoingDocument(final String documentCOO, final List<Content> contents, final String user) throws DmsException {
        log.info("calling UpdateOutgoingGI: {}", documentCOO);

        final UpdateOutgoingGI request = new UpdateOutgoingGI();
        request.setUserlogin(user);
        request.setObjaddress(documentCOO);
        request.setBusinessapp(this.properties.getBusinessapp());

        final ArrayOfLHMBAI151700GIAttachmentType attachmentType = new ArrayOfLHMBAI151700GIAttachmentType();
        final List<LHMBAI151700GIAttachmentType> files = attachmentType.getLHMBAI151700GIAttachmentType();

        for (final Content content : contents) {
            files.add(this.parseContent(content));
        }

        request.setGiattachmenttype(attachmentType);

        final UpdateOutgoingGIResponse response = this.wsClient.updateOutgoingGI(request);

        dmsErrorHandler.handleError(response.getStatus(), response.getErrormessage());
    }

    private void updateInternalDocument(final String documentCOO, final List<Content> contents, final String user) throws DmsException {
        log.info("calling UpdateInternalGI: {}", documentCOO);

        final UpdateInternalGI request = new UpdateInternalGI();
        request.setUserlogin(user);
        request.setObjaddress(documentCOO);
        request.setBusinessapp(this.properties.getBusinessapp());

        final ArrayOfLHMBAI151700GIAttachmentType attachmentType = new ArrayOfLHMBAI151700GIAttachmentType();
        final List<LHMBAI151700GIAttachmentType> files = attachmentType.getLHMBAI151700GIAttachmentType();

        for (final Content content : contents) {
            files.add(this.parseContent(content));
        }

        request.setGiattachmenttype(attachmentType);

        final UpdateInternalGIResponse response = this.wsClient.updateInternalGI(request);

        dmsErrorHandler.handleError(response.getStatus(), response.getErrormessage());
    }

    @Override
    public void depositObject(final String objectCoo, final String user) throws DmsException {
        log.info("calling DepositObject: {}", objectCoo);

        final DepositObjectGI request = new DepositObjectGI();
        request.setUserlogin(user);
        request.setBusinessapp(this.properties.getBusinessapp());
        request.setObjaddress(objectCoo);

        final DepositObjectGIResponse response = this.wsClient.depositObjectGI(request);

        dmsErrorHandler.handleError(response.getStatus(), response.getErrormessage());
    }

    private LHMBAI151700GIAttachmentType parseContent(final Content content) {
        final LHMBAI151700GIAttachmentType attachment = new LHMBAI151700GIAttachmentType();
        attachment.setLHMBAI151700Filecontent(content.content());
        attachment.setLHMBAI151700Fileextension(content.extension());
        attachment.setLHMBAI151700Filename(content.name());
        return attachment;
    }

    @Override
    public void cancelObject(final String objectCoo, final String user) throws DmsException {
        log.info("calling CancelObjectGI Userlogin: {} Objaddress: {}", user, objectCoo);

        final CancelObjectGI cancelObjectGI = new CancelObjectGI();
        cancelObjectGI.setObjaddress(objectCoo);
        cancelObjectGI.setUserlogin(user);
        cancelObjectGI.setBusinessapp(this.properties.getBusinessapp());

        final CancelObjectGIResponse response = this.wsClient.cancelObjectGI(cancelObjectGI);

        dmsErrorHandler.handleError(response.getStatus(), response.getErrormessage());
    }

    @Override
    public List<String> listContentCoos(@NonNull final String documentCoo, @NonNull final String user) throws DmsException {
        final ReadDocumentGIObjects request = new ReadDocumentGIObjects();
        request.setUserlogin(user);
        request.setBusinessapp(this.properties.getBusinessapp());
        request.setObjaddress(documentCoo);

        final ReadDocumentGIObjectsResponse response = this.wsClient.readDocumentGIObjects(request);
        dmsErrorHandler.handleError(response.getStatus(), response.getErrormessage());

        return response.getGiobjecttype().getLHMBAI151700GIObjectType().stream()
                .map(LHMBAI151700GIObjectType::getLHMBAI151700Objaddress)
                .toList();
    }

    @Override
    public List<Content> readContent(final List<String> coos, final String user) throws DmsException {
        final List<Content> files = new ArrayList<>();

        for (final String coo : coos) {
            final ReadContentObjectGI request = new ReadContentObjectGI();
            request.setUserlogin(user);
            request.setBusinessapp(this.properties.getBusinessapp());
            request.setObjaddress(coo);
            final ReadContentObjectGIResponse response = this.wsClient.readContentObjectGI(request);
            dmsErrorHandler.handleError(response.getStatus(), response.getErrormessage());
            files.add(this.map(response));
        }

        return files;
    }

    private Content map(final ReadContentObjectGIResponse response) {
        return new Content(
                response.getGiattachmenttype().getLHMBAI151700Fileextension(),
                response.getGiattachmenttype().getLHMBAI151700Filename(),
                response.getGiattachmenttype().getLHMBAI151700Filecontent());
    }

    @Override
    public List<String> searchFile(final String searchString, final String user, final String reference, final String value) throws DmsException {
        return this.searchObject(searchString, DMSObjectClass.Sachakte, user, reference, value).stream()
                .map(LHMBAI151700GIObjectType::getLHMBAI151700Objaddress)
                .toList();
    }

    @Override
    public List<String> searchSubjectArea(final String searchString, final String user) throws DmsException {
        return this.searchObject(searchString, DMSObjectClass.Aktenplaneintrag, user).stream()
                .map(LHMBAI151700GIObjectType::getLHMBAI151700Objaddress)
                .toList();
    }

    @Override
    public Metadata readMetadata(final String coo, final String username) throws DmsException {
        log.info("calling ReadMetadataObjectGI Userlogin: {} COO: {}", username, coo);

        final ReadMetadataObjectGI request = new ReadMetadataObjectGI();
        request.setObjaddress(coo);
        request.setBusinessapp(this.properties.getBusinessapp());
        request.setUserlogin(username);
        final ReadMetadataObjectGIResponse response = this.wsClient.readMetadataObjectGI(request);

        dmsErrorHandler.handleError(response.getStatus(), response.getErrormessage());

        return new Metadata(
                response.getObjname(),
                response.getObjclass(),
                String.format(this.properties.getUiUrl(), coo));
    }

    @Override
    public Metadata readContentMetadata(final String coo, final String username) throws DmsException {
        log.info("calling ReadContentObjectMetaDataGI Userlogin: {} COO: {}", username, coo);

        final ReadContentObjectMetaDataGI request = new ReadContentObjectMetaDataGI();
        request.setObjaddress(coo);
        request.setBusinessapp(this.properties.getBusinessapp());
        request.setUserlogin(username);
        final ReadContentObjectMetaDataGIResponse response = this.wsClient.readContentObjectMetaDataGI(request);

        dmsErrorHandler.handleError(response.getStatus(), response.getErrormessage());

        return new Metadata(
                response.getGimetadatatype().getLHMBAI151700Filename(),
                response.getGimetadatatype().getLHMBAI151700Objclass(),
                String.format(this.properties.getUiUrl(), coo));
    }

    //------------------------------------- HELPER METHODS -------------------------------------------

    /**
     * Searches for an object.
     *
     * @param searchString string to search for
     * @param dmsObjectClass object class for a soap request
     * @param username account name
     * @return List of discovered objects
     */
    private List<LHMBAI151700GIObjectType> searchObject(final String searchString, final DMSObjectClass dmsObjectClass, final String username)
            throws DmsException {
        return searchObject(searchString, dmsObjectClass, username, null, null);
    }

    /**
     * Searches for an object.
     *
     * @param searchString string to search for
     * @param dmsObjectClass object class for a soap request
     * @param username account name
     * @param reference (optional) 'Fachdatum'/business case to refine a search
     * @param value (optional) value of 'Fachdatum'/business case
     * @return List of discovered objects
     */
    private List<LHMBAI151700GIObjectType> searchObject(final String searchString, final DMSObjectClass dmsObjectClass, final String username,
            final String reference, final String value)
            throws DmsException {
        //logging for dms team
        log.info("calling SearchObjNameGI Userlogin: {} SearchString: {} Objclass: {} Reference: {} Value: {}", username, searchString,
                dmsObjectClass.getName(), reference, value);

        final SearchObjNameGI params = new SearchObjNameGI();
        params.setUserlogin(username);
        params.setBusinessapp(this.properties.getBusinessapp());
        params.setObjclass(dmsObjectClass.getName());
        params.setSearchstring(searchString);
        if (Objects.nonNull(reference)) {
            params.setReference(reference);
        }
        if (Objects.nonNull(value)) {
            params.setValue(value);
        }

        final SearchObjNameGIResponse response = this.wsClient.searchObjNameGI(params);

        dmsErrorHandler.handleError(response.getStatus(), response.getErrormessage());

        if (response.getGiobjecttype() == null || response.getGiobjecttype().getLHMBAI151700GIObjectType() == null) {
            log.debug("No search results found");
            return Collections.emptyList();
        }
        return response.getGiobjecttype().getLHMBAI151700GIObjectType();
    }

    private XMLGregorianCalendar convertDate(final LocalDate date) {
        return DatatypeFactory.newDefaultInstance().newXMLGregorianCalendar(
                GregorianCalendar.from(date.atStartOfDay(ZoneId.systemDefault())));
    }
}
