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
package com.sun.xml.ws.transport.http.servlet;
import java.security.Principal;
import com.sun.xml.ws.spi.runtime.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.servlet.http.HttpServletRequest;

public class WebServiceContextImpl implements WebServiceContext  {
    
    public ThreadLocal msgContext = new ThreadLocal();
    
    public MessageContext getMessageContext() {
        MessageContext ctxt = (MessageContext)msgContext.get();
        if (ctxt == null) {
            throw new IllegalStateException();
        }
        return ctxt;
    }
    
    public void setMessageContext(MessageContext ctxt) {
        msgContext.set(ctxt);
    }
    
    public Principal getUserPrincipal() {
        MessageContext ctxt = (MessageContext)msgContext.get();
        if (ctxt != null) {
            HttpServletRequest req = (HttpServletRequest)ctxt.get(
                    MessageContext.SERVLET_REQUEST);
            if (req != null) {
                return req.getUserPrincipal();
            }
        }
        throw new IllegalStateException();
    }


    public boolean isUserInRole(String role) {
        MessageContext ctxt = (MessageContext)msgContext.get();
        if (ctxt != null) {
            HttpServletRequest req = (HttpServletRequest)ctxt.get(
                    MessageContext.SERVLET_REQUEST);
            if (req != null) {
                return req.isUserInRole(role);
            }
        }
        throw new IllegalStateException();
    }
    
}
