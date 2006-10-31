/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.tools.ws.processor.generator;

import com.sun.codemodel.*;
import com.sun.tools.ws.processor.model.Fault;
import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.wscompile.ErrorReceiver;
import com.sun.tools.ws.wscompile.WsimportOptions;

import javax.xml.ws.WebFault;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author WS Development Team
 */
public class CustomExceptionGenerator extends GeneratorBase {
    private Map<String, JClass> faults = new HashMap<String, JClass>();

    public static void generate(Model model,
        WsimportOptions options,
        ErrorReceiver receiver){
        CustomExceptionGenerator exceptionGen = new CustomExceptionGenerator(model, options, receiver);
        exceptionGen.doGeneration();        
    }
    private CustomExceptionGenerator(
        Model model,
        WsimportOptions options,
        ErrorReceiver receiver) {
        super(model, options, receiver);
    }

    public GeneratorBase getGenerator(Model model, WsimportOptions options, ErrorReceiver receiver) {
        return new CustomExceptionGenerator(model, options, receiver);
    }

    @Override
    public void visit(Fault fault) throws Exception {
        if (isRegistered(fault))
            return;
        registerFault(fault);
    }

    private boolean isRegistered(Fault fault) {
        if(faults.keySet().contains(fault.getJavaException().getName())){
            fault.setExceptionClass(faults.get(fault.getJavaException().getName()));
            return true;
        }
        return false;
    }

    private void registerFault(Fault fault) {
         try {
            write(fault);
            faults.put(fault.getJavaException().getName(), fault.getExceptionClass()); 
        } catch (Exception e) {
            throw new GeneratorException("generator.nestedGeneratorError",e);
        }
    }

    private void write(Fault fault) throws JClassAlreadyExistsException {
        String className = Names.customExceptionClassName(fault);

        JDefinedClass cls = cm._class(className, ClassType.CLASS);
        JDocComment comment = cls.javadoc();
        if(fault.getJavaDoc() != null){
            comment.add(fault.getJavaDoc());
            comment.add("\n\n");
        }

        for (String doc : getJAXWSClassComment()) {
            comment.add(doc);
        }
        
        cls._extends(java.lang.Exception.class);

        //@WebFault
        JAnnotationUse faultAnn = cls.annotate(WebFault.class);
        faultAnn.param("name", fault.getBlock().getName().getLocalPart());
        faultAnn.param("targetNamespace", fault.getBlock().getName().getNamespaceURI());

        JType faultBean = fault.getBlock().getType().getJavaType().getType().getType();

        //faultInfo filed
        JFieldVar fi = cls.field(JMod.PRIVATE, faultBean, "faultInfo");

        //add jaxb annotations
        fault.getBlock().getType().getJavaType().getType().annotate(fi);

        fi.javadoc().add("Java type that goes as soapenv:Fault detail element.");
        JFieldRef fr = JExpr.ref(JExpr._this(), fi);

        //Constructor
        JMethod constrc1 = cls.constructor(JMod.PUBLIC);
        JVar var1 = constrc1.param(String.class, "message");
        JVar var2 = constrc1.param(faultBean, "faultInfo");
        constrc1.javadoc().addParam(var1);
        constrc1.javadoc().addParam(var2);
        JBlock cb1 = constrc1.body();
        cb1.invoke("super").arg(var1);

        cb1.assign(fr, var2);

        //constructor with Throwable
        JMethod constrc2 = cls.constructor(JMod.PUBLIC);
        var1 = constrc2.param(String.class, "message");
        var2 = constrc2.param(faultBean, "faultInfo");
        JVar var3 = constrc2.param(Throwable.class, "cause");
        constrc2.javadoc().addParam(var1);
        constrc2.javadoc().addParam(var2);
        constrc2.javadoc().addParam(var3);
        JBlock cb2 = constrc2.body();
        cb2.invoke("super").arg(var1).arg(var3);
        cb2.assign(fr, var2);


        //getFaultInfo() method
        JMethod fim = cls.method(JMod.PUBLIC, faultBean, "getFaultInfo");
        fim.javadoc().addReturn().add("returns fault bean: "+faultBean.fullName());
        JBlock fib = fim.body();
        fib._return(fi);
        fault.setExceptionClass(cls);

    }
}
