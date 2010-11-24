/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.tools.ws.processor.modeler.annotation;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Messager;
import com.sun.mirror.declaration.*;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.SourcePosition;
import com.sun.mirror.util.Types;
import com.sun.tools.ws.ToolVersion;
import com.sun.tools.ws.processor.generator.GeneratorUtil;
import com.sun.tools.ws.processor.generator.Names;
import com.sun.tools.ws.processor.model.Operation;
import com.sun.tools.ws.processor.model.Port;
import com.sun.tools.ws.processor.model.Service;
import com.sun.tools.ws.processor.modeler.ModelerException;
import com.sun.tools.ws.processor.modeler.wsdl.ConsoleErrorReporter;
import com.sun.tools.ws.resources.WebserviceapMessages;
import com.sun.tools.ws.wscompile.*;
import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.Localizer;
import org.xml.sax.SAXParseException;

import javax.jws.WebService;
import javax.xml.ws.WebServiceProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * WebServiceAP is a APT AnnotationProcessor for processing javax.jws.* and 
 * javax.xml.ws.* annotations. This class is used either by the WsGen (CompileTool) tool or 
 *    idirectly via the {@link com.sun.istack.ws.AnnotationProcessorFactoryImpl} when invoked by APT.
 *
 * @author WS Development Team
 */
public class WebServiceAP implements AnnotationProcessor, ModelBuilder, WebServiceConstants{

    protected AnnotationProcessorEnvironment apEnv;

    private File sourceDir;

    private TypeDeclaration remoteDecl;
    private TypeDeclaration remoteExceptionDecl;
    private TypeDeclaration exceptionDecl;
    private TypeDeclaration runtimeExceptionDecl;
    private TypeDeclaration defHolderDecl;
    private Service service;
    private Port port;
    protected AnnotationProcessorContext context;
    private Set<TypeDeclaration> processedTypeDecls = new HashSet<TypeDeclaration>();
    protected Messager messager;
    private boolean doNotOverWrite = false;
    private WsgenOptions options;
    private ErrorReceiver receiver;
    private PrintStream out;

    /*
    * Is this invocation from APT or JavaC?
    */
    private boolean isAPTInvocation = false;


    public void run() {
    }

    protected  boolean parseArguments(String[] args) {
       return true;
    }


    public WebServiceAP(WsgenOptions options, AnnotationProcessorContext context, ErrorReceiver receiver, PrintStream out) {
        this.options = options;
        this.sourceDir = (options != null)?options.sourceDir:null;
        this.doNotOverWrite = (options != null) && options.doNotOverWrite;
        this.receiver = receiver;
        this.out = out;
        this.context = context;
    }

    public void init(AnnotationProcessorEnvironment apEnv) {
        this.apEnv = apEnv;
        remoteDecl = this.apEnv.getTypeDeclaration(REMOTE_CLASSNAME);
        remoteExceptionDecl = this.apEnv.getTypeDeclaration(REMOTE_EXCEPTION_CLASSNAME);
        exceptionDecl = this.apEnv.getTypeDeclaration(EXCEPTION_CLASSNAME);
        runtimeExceptionDecl = this.apEnv.getTypeDeclaration(RUNTIME_EXCEPTION_CLASSNAME);
        defHolderDecl = this.apEnv.getTypeDeclaration(HOLDER_CLASSNAME);
        if (options == null) {
            options = new WsgenOptions();
            out = new PrintStream(new ByteArrayOutputStream());
            class Listener extends WsimportListener {
                ConsoleErrorReporter cer = new ConsoleErrorReporter(out);

                @Override
                public void generatedFile(String fileName) {
                    message(fileName);
                }

                @Override
                public void message(String msg) {
                    out.println(msg);
                }

                @Override
                public void error(SAXParseException exception) {
                    cer.error(exception);
                }

                @Override
                public void fatalError(SAXParseException exception) {
                    cer.fatalError(exception);
                }

                @Override
                public void warning(SAXParseException exception) {
                    cer.warning(exception);
                }

                @Override
                public void info(SAXParseException exception) {
                    cer.info(exception);
                }
            }

            final Listener listener = new Listener();
            receiver = new ErrorReceiverFilter(new Listener()) {
                public void info(SAXParseException exception) {
                    if (options.verbose)
                        super.info(exception);
                }

                public void warning(SAXParseException exception) {
                    if (!options.quiet)
                        super.warning(exception);
                }

                @Override
                public void pollAbort() throws AbortException {
                    if (listener.isCanceled())
                        throw new AbortException();
                }
            };
            Map<String, String> apOptions = apEnv.getOptions();
            String classDir = apOptions.get("-d");
            if (classDir == null)
                classDir = ".";
            if (apOptions.get("-s") != null)
                sourceDir = new File(apOptions.get("-s"));
            else
                sourceDir = new File(classDir);
            String cp = apOptions.get("-classpath");
            String cpath = classDir +
                    File.pathSeparator +
                    cp + File.pathSeparator +
                    System.getProperty("java.class.path");
            options.classpath = cpath;
            boolean setVerbose = false;
            for (String key : apOptions.keySet()) {
                if (key.equals("-verbose"))
                    setVerbose = true;
            }
            options.verbose = setVerbose;
            messager = apEnv.getMessager();
            isAPTInvocation = true;
        }
        options.filer = apEnv.getFiler();
//        env.setFiler(apEnv.getFiler());
    }

    public AnnotationProcessorEnvironment getAPEnv() {
        return apEnv;
    }

    public WsgenOptions getOptions() {
        return options;
    }

    public File getSourceDir() {
        return sourceDir;
    }

    public void onError(String message) {
        if (messager != null) {
            messager.printError(message);
            throw new AbortException();
        } else {
            throw new ModelerException(message);
        }
    }

    private final static Localizer localizer = new Localizer();

    public void onError(SourcePosition pos, Localizable msg) throws ModelerException {
        if (messager != null) {
            messager.printError(pos, localizer.localize(msg));
        } else {
            throw new ModelerException(msg);
        }
    }


    public void onWarning(String message) {
        if (messager != null) {
            messager.printWarning(message);
        } else {
            report(message);
        }
    }
    public void onInfo(String message) {
        if (messager != null) {
            messager.printNotice(message);
        } else {
            report(message);
        }
    }

    protected void report(String msg) {
        PrintStream outstream =
                out instanceof PrintStream
                        ? out
                        : new PrintStream(out, true);
        outstream.println(msg);
        outstream.flush();
    }

    public void process() {
        if (context.getRound() == 1) {
            buildModel();
        }
        context.incrementRound();
    }

    public boolean checkAndSetProcessed(TypeDeclaration typeDecl) {
        if (!processedTypeDecls.contains(typeDecl)) {
            processedTypeDecls.add(typeDecl);
            return false;
        }
        return true;
    }

    public void clearProcessed() {
        processedTypeDecls.clear();
    }

    public void setService(Service service) {
        this.service = service;
    }

    public void setPort(Port port) {
        this.port = port;
        service.addPort(port);
    }

    public void addOperation(Operation operation) {
        port.addOperation(operation);
    }

    public void setWrapperGenerated(boolean wrapperGenerated) {
    }

    public TypeDeclaration getTypeDeclaration(String typeName) {
        return apEnv.getTypeDeclaration(typeName);
    }

    public String getSourceVersion() {
        return ToolVersion.VERSION.MAJOR_VERSION;
    }

    private void buildModel() {
        WebService webService;
        WebServiceProvider webServiceProvider;
        WebServiceVisitor wrapperGenerator = createWrapperGenerator();
        boolean processedEndpoint = false;
        for (TypeDeclaration typedecl: apEnv.getTypeDeclarations()) {
            if (!(typedecl instanceof ClassDeclaration))
                continue;
            webServiceProvider = typedecl.getAnnotation(WebServiceProvider.class);
            webService = typedecl.getAnnotation(WebService.class);
            if (webServiceProvider != null) {
                if (webService != null) {
                    onError(WebserviceapMessages.WEBSERVICEAP_WEBSERVICE_AND_WEBSERVICEPROVIDER(typedecl.getQualifiedName()));
                }
                processedEndpoint = true;
            }
            if (!shouldProcessWebService(webService))
                continue;
            typedecl.accept(wrapperGenerator);
            processedEndpoint = true;
        }
        if (!processedEndpoint) {
            if (isAPTInvocation)
                onWarning(WebserviceapMessages.WEBSERVICEAP_NO_WEBSERVICE_ENDPOINT_FOUND());
            else
                onError(WebserviceapMessages.WEBSERVICEAP_NO_WEBSERVICE_ENDPOINT_FOUND());
        }
    }

    protected WebServiceVisitor createWrapperGenerator() {
        return new WebServiceWrapperGenerator(this, context);
    }

    protected boolean shouldProcessWebService(WebService webService) {
        return webService != null;
    }


    public boolean isException(TypeDeclaration typeDecl) {
        return isSubtype(typeDecl, exceptionDecl);
    }

    public boolean isRemoteException(TypeDeclaration typeDecl) {
        return isSubtype(typeDecl, remoteExceptionDecl);
    }
    public boolean isServiceException(TypeDeclaration typeDecl) {
        if(!isSubtype(apEnv,typeDecl,exceptionDecl))
            return false;
        if(isSubtype(apEnv,typeDecl,runtimeExceptionDecl) || isSubtype(apEnv,typeDecl,remoteExceptionDecl))
            return false;
        return true;
    }

    public boolean isRemote(TypeDeclaration typeDecl) {
        return isSubtype(typeDecl, remoteDecl);
    }

    public static boolean isSubtype(AnnotationProcessorEnvironment env, TypeDeclaration d1, TypeDeclaration d2) {
        Types typeUtils = env.getTypeUtils();
        return typeUtils.isSubtype(typeUtils.getDeclaredType(d1),typeUtils.getDeclaredType(d2));
    }

    public static boolean isSubtype(TypeDeclaration d1, TypeDeclaration d2) {
        if (d1.equals(d2))
            return true;
        ClassDeclaration superClassDecl = null;
        if (d1 instanceof ClassDeclaration) {
            ClassType superClass = ((ClassDeclaration)d1).getSuperclass();
            if (superClass != null) {
                superClassDecl = superClass.getDeclaration();
                if (superClassDecl.equals(d2))
                    return true;
            }
        }
        InterfaceDeclaration superIntf = null;
        for (InterfaceType interfaceType : d1.getSuperinterfaces()) {
            superIntf = interfaceType.getDeclaration();
            if (superIntf.equals(d2))
                return true;
        }
        if (superIntf != null && isSubtype(superIntf, d2)) {
            return true;
        } else if (superClassDecl != null && isSubtype(superClassDecl, d2)) {
            return true;
        }
        return false;
    }


    public static String getMethodSig(MethodDeclaration method) {
        StringBuffer buf = new StringBuffer(method.getSimpleName() + "(");
        Iterator<TypeParameterDeclaration> params = method.getFormalTypeParameters().iterator();
        TypeParameterDeclaration param;
        for (int i =0; params.hasNext(); i++) {
            if (i > 0)
                buf.append(", ");
            param = params.next();
            buf.append(param.getSimpleName());
        }
        buf.append(")");
        return buf.toString();
    }

    public String getOperationName(String messageName) {
        return messageName;
    }

    public String getResponseName(String operationName) {
        return Names.getResponseName(operationName);
    }


    public TypeMirror getHolderValueType(TypeMirror type) {
        return TypeModeler.getHolderValueType(type, defHolderDecl);
    }

    public boolean canOverWriteClass(String className) {
        return !((doNotOverWrite && GeneratorUtil.classExists(options, className)));
    }

    public void log(String msg) {
        if (options != null && options.verbose) {
            String message = "[" + msg + "]";
            if (messager != null) {
                messager.printNotice(message);
            } else {
                System.out.println(message);
            }
        }
    }

    public String getXMLName(String javaName) {
        return javaName;
    }
}


