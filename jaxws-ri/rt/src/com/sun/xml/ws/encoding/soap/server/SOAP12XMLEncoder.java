/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.xml.ws.encoding.soap.server;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.soap.SOAPBinding;

import com.sun.xml.ws.pept.ept.MessageInfo;
import com.sun.xml.ws.encoding.soap.message.SOAPFaultInfo;
import com.sun.xml.ws.encoding.soap.message.SOAP12FaultInfo;
import com.sun.xml.ws.encoding.soap.streaming.SOAP12NamespaceConstants;
import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.encoding.JAXWSAttachmentMarshaller;
import com.sun.xml.ws.util.MessageInfoUtil;
import com.sun.xml.ws.client.BindingProviderProperties;
import com.sun.xml.ws.server.*;

import static com.sun.xml.ws.client.BindingProviderProperties.*;
import com.sun.xml.ws.handler.MessageContextUtil;
import com.sun.xml.ws.spi.runtime.WSConnection;
import javax.xml.ws.handler.MessageContext;

public class SOAP12XMLEncoder extends SOAPXMLEncoder {
    /*
     * @see SOAPEncoder#startEnvelope(XMLStreamWriter)
     */
    @Override
    protected void startEnvelope(XMLStreamWriter writer) {
        try {
            writer.writeStartElement(SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE,
                SOAPNamespaceConstants.TAG_ENVELOPE, SOAP12NamespaceConstants.ENVELOPE);
            writer.setPrefix(SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE,
                             SOAP12NamespaceConstants.ENVELOPE);
            writer.writeNamespace(SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE,
                                  SOAP12NamespaceConstants.ENVELOPE);
        }
        catch (XMLStreamException e) {
            throw new ServerRtException(e);
        }
    }

    /*
     * @see SOAPEncoder#startBody(XMLStreamWriter)
     */
    @Override
    protected void startBody(XMLStreamWriter writer) {
        try {
            writer.writeStartElement(SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE,
                SOAPNamespaceConstants.TAG_BODY, SOAP12NamespaceConstants.ENVELOPE);
        }
        catch (XMLStreamException e) {
            throw new ServerRtException(e);
        }        
    }

    /*
     * @see SOAPEncoder#startHeader(XMLStreamWriter)
     */
    @Override
    protected void startHeader(XMLStreamWriter writer) {
        try {
            writer.writeStartElement(SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE,
                SOAPNamespaceConstants.TAG_HEADER, SOAP12NamespaceConstants.ENVELOPE); // <env:Header>
        }
        catch (XMLStreamException e) {
            throw new ServerRtException(e);
        }       
    }

    /* (non-Javadoc)
     * @see com.sun.xml.ws.rt.server.SOAPXMLEncoder#writeFault(com.sun.xml.ws.soap.message.SOAPFaultInfo, com.sun.pept.ept.MessageInfo, com.sun.xml.ws.streaming.XMLStreamWriter)
     */
    @Override
    protected void writeFault(SOAPFaultInfo faultInfo, MessageInfo messageInfo, XMLStreamWriter writer) {
        if(!(faultInfo instanceof SOAP12FaultInfo))
            return;
        // Set a status code for Fault
        MessageContext ctxt = MessageInfoUtil.getMessageContext(messageInfo);
        if (MessageContextUtil.getHttpStatusCode(ctxt) == null) {
            MessageContextUtil.setHttpStatusCode(ctxt, WSConnection.INTERNAL_ERR);
        }
        
        ((SOAP12FaultInfo)faultInfo).write(writer, messageInfo);
    }
    
    protected String getContentType(MessageInfo messageInfo, 
        JAXWSAttachmentMarshaller marshaller) 
    {
        String contentNegotiation = (String)
            messageInfo.getMetaData(BindingProviderProperties.CONTENT_NEGOTIATION_PROPERTY);

        if (marshaller == null) {
            marshaller = getAttachmentMarshaller(messageInfo);
        }
        
        if (marshaller != null && marshaller.isXopped()) {
            return XOP_SOAP12_XML_TYPE_VALUE;
        }
        else {
            return (contentNegotiation == "optimistic") ? 
                FAST_INFOSET_TYPE_SOAP12 : SOAP12_XML_CONTENT_TYPE_VALUE;
        }
    }    
    
    /**
     * This method is used to create the appropriate SOAPMessage (1.1 or 1.2 using SAAJ api).
     * @return the BindingID associated with this encoder
     */
    protected SOAPVersion getBindingId(){
        return SOAPVersion.SOAP_12;
    }
}
