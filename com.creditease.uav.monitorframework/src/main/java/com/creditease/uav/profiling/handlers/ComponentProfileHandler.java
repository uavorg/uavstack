/*-
 * <<
 * UAVStack
 * ==
 * Copyright (C) 2016 - 2017 UAVStack
 * ==
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * >>
 */

package com.creditease.uav.profiling.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.creditease.agent.helpers.NetworkHelper;
import com.creditease.agent.helpers.ReflectHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.UAVServer.ServerVendor;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.interceptframework.spi.InterceptConstants;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.uav.common.BaseComponent;
import com.creditease.uav.profiling.handlers.ComponentProfileHandler.DescriptorProcessor.XMLNodeType;
import com.creditease.uav.profiling.handlers.webservice.WebServiceProfileInfo;
import com.creditease.uav.profiling.spi.ProfileConstants;
import com.creditease.uav.profiling.spi.ProfileContext;
import com.creditease.uav.profiling.spi.ProfileElement;
import com.creditease.uav.profiling.spi.ProfileElementInstance;
import com.creditease.uav.profiling.spi.ProfileHandler;
import com.creditease.uav.profiling.spi.ProfileServiceMapMgr;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

/**
 * profiling for web application's target components which has annotations or declared in deployment descriptor such as
 * web.xml
 * 
 * @author zhen zhang
 *
 */
public class ComponentProfileHandler extends BaseComponent implements ProfileHandler {

    /**
     * getClassAnnoInfo
     * 
     * @param cls
     * @param info
     * @param annoClses
     */
    @SuppressWarnings("rawtypes")
    public static void getClassAnnoInfo(Class<?> cls, Map<String, Object> info, Class... annoClses) {

        Map<String, Object> annoInfos = new LinkedHashMap<String, Object>();

        for (Class annoCls : annoClses) {

            Map<String, Object> mAnnoInfos = ReflectHelper.getAnnotationAllFieldValues(cls, annoCls);

            if (null == mAnnoInfos || mAnnoInfos.size() == 0) {
                continue;
            }

            annoInfos.put(annoCls.getName(), mAnnoInfos);
        }

        info.put("anno", annoInfos);
    }

    /**
     * getMethodInfo 没有check @WebMethod
     * 
     * @param c
     * @param info
     */
    @SuppressWarnings({ "rawtypes" })
    public static void getMethodInfo(Class<?> cls, Map<String, Object> info, Class... annoClses) {

        Map<String, Object> methodInfos = new LinkedHashMap<String, Object>();
        Method[] methods;
        try {
            methods = cls.getMethods();
        }
        catch (Throwable e) {
            return;
        }

        for (Method m : methods) {

            // ignore those methods in black list map
            if (methodBlackListMap.containsKey(m.getName())) {
                continue;
            }

            Map<String, Object> annoInfos = getMethodAnnoInfo(cls, m, annoClses);

            List<String> paramInfos = getMethodParamInfo(m);

            Map<String, Object> methodInfo = new HashMap<String, Object>();

            if (null != annoInfos && annoInfos.size() > 0) {
                methodInfo.put("anno", annoInfos);
            }

            if (null != paramInfos && paramInfos.size() > 0) {
                methodInfo.put("para", paramInfos);
            }

            methodInfos.put(m.getName(), methodInfo);
        }

        info.put("methods", methodInfos);
    }

    @SuppressWarnings("rawtypes")
    public static Map<String, Object> getMethodAnnoInfo(Class<?> cls, Method m, Class... annoClses) {

        Map<String, Object> annoInfos = new LinkedHashMap<String, Object>();

        if (annoClses == null) {
            return annoInfos;
        }

        for (Class annoCls : annoClses) {

            Map<String, Object> mAnnoInfos = ReflectHelper.getAnnotationAllFieldValues(cls, m, annoCls);

            // Enhancement:Avoid mAnnoInfos is Null situation
            if (mAnnoInfos != null) {
                annoInfos.put(annoCls.getName(), mAnnoInfos);
            }

        }

        return annoInfos;
    }

    public static List<String> getMethodParamInfo(Method m) {

        List<String> pinfos = new ArrayList<String>();

        // 1. get input para
        Class<?>[] pTypes = m.getParameterTypes();

        if (null != pTypes && pTypes.length > 0) {
            for (Class<?> pCls : pTypes) {
                pinfos.add(pCls.getName());
            }
        }

        // 2. get return para
        Class<?> rType = m.getReturnType();

        if (null != rType) {
            pinfos.add("R:" + rType.getName());
        }

        return pinfos;
    }

    // -------------------------------------------Deployment Descriptor Processor------------------------------------
    /**
     * DescriptorProcessor is the basic class to handle the deployment descriptor file such as
     * web.xml,spring-context.xml...
     * 
     * @author zhen zhang
     *
     */
    public static abstract class DescriptorProcessor extends BaseComponent {

        public interface XMLParseHandler {

            /**
             * try parse the XML node info into something
             * 
             * @param dc
             * @param node
             * @return if true means continue next node, if false means return
             */
            public abstract boolean parse(DescriptorCollector dc, Node node);
        }

        public enum XMLNodeType {

            ELEMENT_NODE(Node.ELEMENT_NODE), ATTRIBUTE_NODE(Node.ATTRIBUTE_NODE);

            private short index;

            private XMLNodeType(short i) {
                index = i;
            }

            public short getIndex() {

                return index;
            }
        }

        protected XPath xpath;

        protected List<Document> docs = new ArrayList<Document>();

        protected boolean isLoad = false;

        protected ProfileContext context = null;

        protected DocumentBuilder db = null;

        public DescriptorProcessor(ProfileContext context) {
            XPathFactory factory = XPathFactory.newInstance();
            xpath = factory.newXPath();
            this.context = context;
        }

        public ProfileContext getContext() {

            return context;
        }

        public DocumentBuilder getDocumentBuilder() {

            if (db == null) {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setValidating(false);

                try {
                    db = dbf.newDocumentBuilder();
                }
                catch (ParserConfigurationException e) {
                    this.logger.error(
                            "Init DocumentBuilder FAILs. The deployement descriptor profiling CAN NOT be done.", e);
                    return null;
                }
            }
            return db;
        }

        /**
         * load descriptor files
         */
        protected boolean load(String webAppRoot) {

            if (isLoad == true) {
                return true;
            }

            List<String> filePaths = getDescriptorFileLocations(webAppRoot);

            if (filePaths.size() == 0) {
                return false;
            }

            if (getDocumentBuilder() == null)
                return false;

            for (String filePath : filePaths) {

                File fpath = new File(filePath);

                if (!fpath.exists()) {
                    continue;
                }

                Document doc = null;
                try {

                    doc = db.parse(new FileInputStream(fpath));
                    docs.add(doc);
                }
                catch (FileNotFoundException e) {
                    if (this.logger.isLogEnabled()) {
                        this.logger.warn("Load DescriptorFile[" + filePath + "] FAILs. Casue:" + e.getMessage(), null);
                    }
                }
                catch (SAXException e) {
                    if (this.logger.isLogEnabled()) {
                        this.logger.warn("Load DescriptorFile[" + filePath + "] FAILs. Casue:" + e.getMessage(), null);
                    }
                }
                catch (UnknownHostException e) {
                    if (this.logger.isLogEnabled()) {
                        this.logger.warn("Load DescriptorFile[" + filePath + "] FAILs. Casue:" + e.getMessage(), null);
                    }
                }
                catch (IOException e) {
                    if (this.logger.isLogEnabled()) {
                        this.logger.warn("Load DescriptorFile[" + filePath + "] FAILs. Cause:" + e.getMessage(), null);
                    }
                }
            }

            this.isLoad = true;

            return true;
        }

        /**
         * selectXMLNodeSet
         * 
         * @param xpath
         * @return List<NodeList>
         */
        public List<NodeList> selectXMLNodeSet(String xpath) {

            List<NodeList> infos = new ArrayList<NodeList>();

            for (Document doc : this.docs) {
                try {
                    NodeList nodeList = (NodeList) this.xpath.evaluate(xpath, doc, XPathConstants.NODESET);
                    infos.add(nodeList);
                }
                catch (XPathExpressionException e) {
                    if (this.logger.isLogEnabled()) {
                        this.logger.warn("Select nodes[" + xpath + "] from doc[" + doc + "] FAILs.", e);
                    }
                }
            }

            return infos;
        }

        /**
         * selectXMLNode
         * 
         * @param xpath
         * @return Node
         */
        public Node selectXMLNode(String xpath) {

            for (Document doc : this.docs) {
                try {
                    Node node = (Node) this.xpath.evaluate(xpath, doc, XPathConstants.NODE);

                    if (null != node) {
                        return node;
                    }

                }
                catch (XPathExpressionException e) {
                    if (this.logger.isLogEnabled()) {
                        this.logger.warn("Select nodes[" + xpath + "] from doc[" + doc + "] FAILs.", e);
                    }
                }
            }

            return null;
        }

        /**
         * selectXMLNode, its base xpath is from the input base node
         * 
         * @param xpath
         * @param node
         *            base node
         * @return
         */
        public Node selectXMLNode(String xpath, Node node) {

            Node reNode = null;
            try {
                reNode = (Node) this.xpath.evaluate(xpath, node, XPathConstants.NODE);
            }
            catch (XPathExpressionException e) {
                if (this.logger.isLogEnabled()) {
                    this.logger.warn("Select nodes[" + xpath + "] from doc[" + node + "] FAILs.", e);
                }
            }
            return reNode;
        }

        /**
         * parse
         * 
         * @param webAppRoot
         *            webapp root path
         * @param xpath
         *            the target xpath
         * @param XMLParseHandler
         *            the actual logic to get the info from Node and store them into something
         * @param xmlNodeTypes
         *            what kinds of Node should be parsed
         */
        public void parse(String webAppRoot, String xpath, XMLParseHandler handler, XMLNodeType... xmlNodeTypes) {

            // 1. select descriptor collector
            DescriptorCollector dc = selectDescriptorCollector(xpath);

            // 2. load descriptor files
            this.load(webAppRoot);

            // 3. select node list
            List<NodeList> cfgs = this.selectXMLNodeSet(xpath);

            Set<Short> set = new HashSet<Short>();

            for (XMLNodeType t : xmlNodeTypes) {
                set.add(t.getIndex());
            }

            for (NodeList nl : cfgs) {

                for (int i = 0; i < nl.getLength(); i++) {

                    Node node = nl.item(i);

                    short type = node.getNodeType();

                    if (set.contains(type)) {

                        if (!handler.parse(dc, node)) {
                            return;
                        }
                    }
                }
            }
        }

        public void parseToPEI(final ProfileElementInstance inst, String webAppRoot, String xpath,
                XMLNodeType... xmlNodeTypes) {

            final DescriptorProcessor dp = this;

            XMLParseHandler handler = new XMLParseHandler() {

                @SuppressWarnings("unchecked")
                @Override
                public boolean parse(DescriptorCollector dc, Node node) {

                    Map<String, Object> sInfo = new LinkedHashMap<String, Object>();

                    String sKeyRawValue = node.getTextContent();

                    String sKey = formatValue(sKeyRawValue);

                    // gxs add cover annotation with descriptor
                    dc.setProfileElement(inst).loadInfo(sKey, sKeyRawValue, sInfo);

                    String ikey = dc.getKey(sKey, sKeyRawValue);

                    Object ikeyMap = inst.getValues().get(ikey);

                    Map<String, Object> iInfo = null;
                    /**
                     * if there is no instance key, then that means there is
                     */
                    if (null == ikeyMap) {
                        iInfo = new LinkedHashMap<String, Object>();

                        inst.setValue(ikey, iInfo);
                    }
                    else {
                        iInfo = (Map<String, Object>) ikeyMap;
                    }

                    /**
                     * NOTE: this is a special process to find out service engine in descriptor configuration TODO:
                     * still human not micro AI
                     */
                    if (dp.getClass().getName().indexOf("WebXmlProcessor") > -1) {
                        WebServletInfoProcessor.figureOutServiceEngine(sKey, context, iInfo);
                    }

                    iInfo.put("des", sInfo);

                    return true;
                }

            };

            this.parse(webAppRoot, xpath, handler, xmlNodeTypes);
        }

        public void parseToPEIWithValueKey(final ProfileElementInstance inst, final String valueKey, String webAppRoot,
                String xpath, XMLNodeType... xmlNodeTypes) {

            XMLParseHandler handler = new XMLParseHandler() {

                @Override
                public boolean parse(DescriptorCollector dc, Node node) {

                    String sKey = node.getTextContent();

                    sKey = formatValue(sKey);

                    inst.setValue(valueKey, sKey);

                    return false;
                }

            };

            this.parse(webAppRoot, xpath, handler, xmlNodeTypes);
        }

        /**
         * collectNodeList
         * 
         * @param sInfo
         * @param keyName
         * @param nodeList
         */
        private void collectNodeList(Map<String, Object> sInfo, String keyName, List<NodeList> nodeList) {

            if (!nodeList.isEmpty()) {
                List<String> urls = new ArrayList<String>();
                for (NodeList upNL : nodeList) {
                    for (int j = 0; j < upNL.getLength(); j++) {
                        urls.add(upNL.item(j).getTextContent());
                    }
                }
                sInfo.put(keyName, urls);
            }
        }

        /**
         * select a DescriptorCollector instance for descriptor info collection
         * 
         * @param xpath
         * @return
         */
        public abstract DescriptorCollector selectDescriptorCollector(String xpath);

        /**
         * getDescriptorFileLocations
         * 
         * @return
         */
        protected abstract List<String> getDescriptorFileLocations(String webAppRoot);

        /**
         * remove \n \r
         * 
         * @param value
         * @return
         */
        protected static String formatValue(String value) {

            value = value.trim().replace("\n", "").replace("\r", "").replace("\"", "'");
            return value;
        }
    }

    /**
     * DescriptorCollector
     * 
     * @author zhen zhang modify interface to abstract class
     */
    public static abstract class DescriptorCollector {

        private ProfileElementInstance profileElement = null;

        /**
         * 
         * @param sKey
         *            格式化后的skey
         * @param sKeyRawValue
         *            未格式化的skey，用于取值
         * @param sInfo
         */
        public abstract void loadInfo(String sKey, String sKeyRawValue, Map<String, Object> sInfo);

        public DescriptorCollector setProfileElement(ProfileElementInstance instance) {

            this.profileElement = instance;
            return this;
        }

        public ProfileElementInstance getProfileElement() {

            return profileElement;
        }

        public String getKey(String sKey, String sKeyRawValue) {

            return sKey;
        }
    }

    public static class SpringXmlProcessor extends DescriptorProcessor {

        private static String springXMLContextParam = "/web-app/context-param[param-name='contextConfigLocation']/param-value";

        private static final String CONFIG_LOCATION_DELIMITERS = ",; \t\n";

        private static final String SPRING_ResourcePatternResolver_CLASSNAME = "org.springframework.core.io.support.PathMatchingResourcePatternResolver";

        private static final String SPRING_RESOURCE_CLASSNAME = "org.springframework.core.io.Resource";

        private static final String SPRING_SC_RESOURCE_LOADER_CLASSNAME = "org.springframework.web.context.support.ServletContextResourceLoader";

        private static final String SPRING_RESOURCE_LOADER_CLASSNAME = "org.springframework.core.io.ResourceLoader";

        private Object resourceloader = null;

        public SpringXmlProcessor(ProfileContext context) {
            super(context);
        }

        @Override
        public List<String> getDescriptorFileLocations(String webAppRoot) {

            List<String> files = new ArrayList<String>();

            WebXmlProcessor wxp = this.getContext().get(WebXmlProcessor.class);

            final List<String> springConfigLocations = new ArrayList<String>();

            wxp.parse(webAppRoot, springXMLContextParam, new XMLParseHandler() {

                @Override
                public boolean parse(DescriptorCollector dc, Node node) {

                    springConfigLocations.add(node.getTextContent());

                    return true;
                }

            }, XMLNodeType.ELEMENT_NODE);

            getSpringConfigfromAnno(springConfigLocations);
            if (null != springConfigLocations && springConfigLocations.size() == 1) {

                // springConfigPath to Abspath
                String[] paths = StringHelper.tokenizeToStringArray(springConfigLocations.get(0),
                        CONFIG_LOCATION_DELIMITERS);
                for (String path : paths) {
                    files.addAll(getFileLocation(webAppRoot, path));
                }
            }

            // add spring application xml import files
            Document doc = null;
            NodeList nodes = null;
            File file = null;
            List<String> importfile = new ArrayList<String>();
            for (String filepath : files) {
                file = new File(filepath);

                /**
                 * NOTE: the import file is relative to the path of main config file, not context path
                 */
                String mainConfgFolderAbsPath = file.getParentFile().getAbsolutePath();

                String importFilePathPart1 = mainConfgFolderAbsPath.substring(webAppRoot.length());

                importFilePathPart1 = importFilePathPart1.replace("\\", "/");

                try {
                    doc = this.getDocumentBuilder().parse(file);
                    nodes = (NodeList) this.xpath.evaluate("/beans/import/@resource", doc, XPathConstants.NODESET);
                    for (int i = 0; i < nodes.getLength(); i++) {

                        String importFilePathPart2 = nodes.item(i).getTextContent();

                        importFilePathPart2 = (importFilePathPart2.startsWith("/") == true)
                                ? importFilePathPart2.substring(1) : importFilePathPart2;

                        String importFileRelativePath = importFilePathPart1 + "/" + importFilePathPart2;

                        importfile.addAll(getFileLocation(webAppRoot, importFileRelativePath));
                    }
                }
                catch (IOException e) {
                    if (this.logger.isLogEnabled()) {
                        this.logger.warn("Load DescriptorFile[" + file.getPath() + "] FAILs.", e);
                    }
                }
                catch (SAXException e) {
                    if (this.logger.isLogEnabled()) {
                        this.logger.warn("Load DescriptorFile[" + file.getPath() + "] FAILs.", e);
                    }
                }
                catch (XPathExpressionException e) {
                    if (this.logger.isLogEnabled()) {
                        this.logger.warn("Load DescriptorFile[" + file.getPath() + "] FAILs.", e);
                    }
                }
            }
            files.addAll(importfile);
            return files;
        }

        @SuppressWarnings("unchecked")
        private void getSpringConfigfromAnno(List<String> springConfigLocations) {

            List<String> locationsInfo = (List<String>) context.get("ResouceLocations");
            if (locationsInfo == null) {
                return;
            }
            springConfigLocations.addAll(locationsInfo);

        }

        /**
         * get spring config.xml cxf webservice configuration information
         */
        @Override
        public DescriptorCollector selectDescriptorCollector(String xpath) {

            DescriptorCollector ic = null;
            final DescriptorProcessor processor = this;
            final ClassLoader webappclsLoader = (ClassLoader) this.getContext().get(InterceptContext.class)
                    .get(InterceptConstants.WEBAPPLOADER);

            /// beans/endpoint/@id /beans/server/@id
            if ("/beans/endpoint/@id".equals(xpath) || "/beans/server/@id".equals(xpath)) {

                ic = new DescriptorCollector() {

                    @Override
                    public void loadInfo(String sKey, String sKeyRawValue, Map<String, Object> sInfo) {

                        // String implName = null;
                        Node jaxws = processor.selectXMLNode("/beans/endpoint[@id='" + sKeyRawValue + "']");
                        if (jaxws == null) {
                            jaxws = processor.selectXMLNode("/beans/server[@id='" + sKeyRawValue + "']");

                        }
                        NamedNodeMap map = jaxws.getAttributes();

                        for (int i = 0; i < map.getLength(); i++) {
                            Node attr = map.item(i);
                            sInfo.put(attr.getNodeName(), attr.getNodeValue());
                        }
                    }

                    /**
                     * /beans/jaxws:endpoint/id or /beans/jaxws:servert/id to class implementor name
                     */
                    @Override
                    public String getKey(String sKey, String sKeyRawValue) {

                        String implName = null;
                        Node jaxws = processor.selectXMLNode("/beans/endpoint[@id='" + sKeyRawValue + "']");
                        if (jaxws == null) {
                            jaxws = processor.selectXMLNode("/beans/server[@id='" + sKeyRawValue + "']");
                            implName = getImplClassAsKey(sKey, jaxws, webappclsLoader, "serviceClass", "serviceBean");
                        }
                        else {
                            implName = getImplClassAsKey(sKey, jaxws, webappclsLoader, "implementorClass",
                                    "implementor");
                        }
                        return implName;
                    }

                    /**
                     * /beans/jaxws:server/id to class implementor name or /beans/jaxws:endpoint/id to class implementor
                     * name
                     */
                    private String getImplClassAsKey(String key, Node jaxws, ClassLoader cl, String serviceClass,
                            String serviceBean) {

                        /**
                         * step 1:check if serviceClass|implementorClass can give us such info
                         */
                        Node impl = jaxws.getAttributes().getNamedItem(serviceClass);

                        if (null != impl) {

                            String sClass = impl.getNodeValue();

                            try {
                                Class<?> c = cl.loadClass(sClass);

                                // step 1.1: we need see serviceBean|implementor if is interface
                                if (c.isInterface() == true) {
                                    if (logger.isDebugable()) {
                                        logger.debug(
                                                "JAXWS Impl class[" + sClass + "] from  from SPRING ConfigXml Element["
                                                        + serviceClass + "] is Interface Class",
                                                null);
                                    }
                                }
                                // step 1.2: if this class is not interface, we assume it is the impl class, because
                                // maybe this is a wrong class
                                else {
                                    return sClass;
                                }
                            }
                            catch (ClassNotFoundException e) {
                                // step 1.3: that means this is a unknown class, we need see serviceBean|implementor
                                if (logger.isDebugable()) {
                                    logger.debug("LOAD JAXWS Impl class[" + sClass
                                            + "] FAIL from SPRING ConfigXml Element Attribute[" + serviceClass + "].",
                                            e);
                                }
                            }

                        }

                        /**
                         * step 2: check if serviceBean can give us such info
                         */
                        // step 2.1 load from attribute serviceBean|implementor
                        impl = jaxws.getAttributes().getNamedItem(serviceBean);

                        if (impl != null) {

                            String sClass = impl.getNodeValue();
                            try {
                                // step 2.1.1 if the value is really a class
                                cl.loadClass(sClass);

                                return sClass;
                            }
                            catch (ClassNotFoundException e) {
                                if (logger.isDebugable()) {
                                    logger.debug("LOAD JAXWS Impl class[" + sClass
                                            + "] FAIL from SPRING ConfigXml Element Attribute[" + serviceBean + "].",
                                            e);
                                }
                                // step 2.1.2 if the value is spring bean id
                                Node beanClazz = processor.selectXMLNode("/beans/bean[@id='" + sClass + "']/@class");

                                return beanClazz.getNodeValue();
                            }
                        }

                        // step 2.2 load serviceBean|implementor/bean/@class
                        impl = processor.selectXMLNode(serviceBean + "/bean/@class", jaxws);

                        if (impl != null) {

                            return impl.getNodeValue();
                        }

                        // step 2.3 load serviceBean|implementor/ref/@bean
                        impl = processor.selectXMLNode(serviceBean + "/ref/@bean", jaxws);

                        if (impl != null) {

                            Node beanClazz = processor
                                    .selectXMLNode("/beans/bean[@id='" + impl.getNodeValue() + "']/@class");

                            return beanClazz.getNodeValue();
                        }

                        // step 2.3 load serviceBean|implementor/@ref
                        impl = processor.selectXMLNode(serviceBean + "/@ref", jaxws);

                        if (impl != null) {

                            Node beanClazz = processor
                                    .selectXMLNode("/beans/bean[@id='" + impl.getNodeValue() + "']/@class");

                            return beanClazz.getNodeValue();
                        }

                        return key;
                    }
                };
            }

            // get impl class
            return ic;
        }

        /**
         * path/file.xxx classpath:path/file.xxx
         * 
         * @param filepath
         * @return
         */
        private List<String> getFileLocation(String webAppRoot, String path) {

            List<String> absPaths = new ArrayList<String>();
            ClassLoader webappclsLoader = (ClassLoader) this.getContext().get(InterceptContext.class)
                    .get(InterceptConstants.WEBAPPLOADER);
            // get PathMatchingResourcePatternResolver implements ResourceLoader
            if (resourceloader == null) {
                if (path.startsWith("classpath") || path.startsWith("file:")) {
                    resourceloader = ReflectHelper.newInstance(SPRING_ResourcePatternResolver_CLASSNAME,
                            new Class[] { ClassLoader.class }, new Object[] { webappclsLoader }, webappclsLoader);
                }
                else {
                    Object sc = this.getContext().get(InterceptContext.class).get(InterceptConstants.SERVLET_CONTEXT);
                    resourceloader = ReflectHelper.newInstance(SPRING_SC_RESOURCE_LOADER_CLASSNAME,
                            new Class[] { ReflectHelper.tryLoadClass("javax.servlet.ServletContext", webappclsLoader) },
                            new Object[] { sc }, webappclsLoader);
                }
            }
            if (resourceloader == null) {
                return absPaths;
            }
            Object[] resources = null;
            if (path.startsWith("classpath") || path.startsWith("file:")) {
                resources = (Object[]) ReflectHelper.invoke(SPRING_ResourcePatternResolver_CLASSNAME, resourceloader,
                        "getResources", new Class[] { String.class }, new String[] { path }, webappclsLoader);
            }
            else {
                resources = new Object[] { ReflectHelper.invoke(SPRING_RESOURCE_LOADER_CLASSNAME, resourceloader,
                        "getResource", new Class[] { String.class }, new String[] { path }, webappclsLoader) };
            }

            File location = null;
            if (resources != null) {
                for (Object resource : resources) {
                    location = (File) ReflectHelper.invoke(SPRING_RESOURCE_CLASSNAME, resource, "getFile", null, null,
                            webappclsLoader);
                    if (location != null && location.isFile())
                        absPaths.add(location.getPath());
                }
            }
            return absPaths;
        }

    }

    /**
     * web.xml descriptor
     * 
     * @author zhen zhang
     *
     */
    public static class WebXmlProcessor extends DescriptorProcessor {

        public WebXmlProcessor(ProfileContext context) {
            super(context);
        }

        @Override
        protected List<String> getDescriptorFileLocations(String webAppRoot) {

            List<String> files = new ArrayList<String>();

            files.add(webAppRoot + "/WEB-INF/web.xml");

            return files;
        }

        @Override
        public DescriptorCollector selectDescriptorCollector(String xpath) {

            final DescriptorProcessor dp = this;

            DescriptorCollector ic = null;

            if ("/web-app/servlet/servlet-class".equals(xpath)) {

                // get servlets
                ic = new DescriptorCollector() {

                    @Override
                    public void loadInfo(String sKey, String sKeyRawValue, Map<String, Object> sInfo) {

                        Node sName = dp
                                .selectXMLNode("/web-app/servlet[servlet-class='" + sKeyRawValue + "']/servlet-name");
                        // servlet-name
                        String servletName = sName.getTextContent();
                        sInfo.put("name", servletName);

                        // loadOnStartup
                        Node loadOnStartup = dp.selectXMLNode(
                                "/web-app/servlet[servlet-class='" + sKeyRawValue + "']/load-on-startup");
                        if (null != loadOnStartup) {
                            sInfo.put("loadOnStartup", loadOnStartup.getTextContent());
                        }

                        // servlet-mapping
                        List<NodeList> urlPatterns = dp.selectXMLNodeSet(
                                "/web-app/servlet-mapping[servlet-name='" + servletName + "']/url-pattern");
                        dp.collectNodeList(sInfo, "urlPatterns", urlPatterns);

                        Node asyncSupported = dp.selectXMLNode(
                                "/web-app/servlet[servlet-class='" + sKeyRawValue + "']/async-supported");
                        if (null != asyncSupported) {
                            sInfo.put("asyncSupported", asyncSupported.getTextContent());
                        }
                    }
                };
            }
            else if ("/web-app/filter/filter-class".equals(xpath)) {
                // get filters
                ic = new DescriptorCollector() {

                    @Override
                    public void loadInfo(String sKey, String sKeyRawValue, Map<String, Object> sInfo) {

                        Node sName = dp
                                .selectXMLNode("/web-app/filter[filter-class='" + sKeyRawValue + "']/filter-name");
                        // filter-name
                        String filterName = sName.getTextContent();
                        sInfo.put("filterName", filterName);

                        // filter-mapping url-pattern
                        List<NodeList> urlPatterns = dp.selectXMLNodeSet(
                                "/web-app/filter-mapping[filter-name='" + filterName + "']/url-pattern");
                        dp.collectNodeList(sInfo, "urlPatterns", urlPatterns);

                        // filter-mapping servlet-name
                        List<NodeList> servlets = dp.selectXMLNodeSet(
                                "/web-app/filter-mapping[filter-name='" + filterName + "']/servlet-name");
                        dp.collectNodeList(sInfo, "servletNames", servlets);
                    }
                };
            }
            else if ("/web-app/listener/listener-class".equals(xpath)) {
                // get listeners
                ic = new DescriptorCollector() {

                    @Override
                    public void loadInfo(String sKey, String sKeyRawValue, Map<String, Object> sInfo) {

                        // doing nothing as no thing need being collected for listeners
                    }
                };
            }
            else if ("/web-app/display-name".equals(xpath)) {
                // get display-name as the application name
                ic = new DescriptorCollector() {

                    @Override
                    public void loadInfo(String sKey, String sKeyRawValue, Map<String, Object> sInfo) {

                        // doing nothing as no thing need being collected for display-name
                    }
                };
            }

            else if ("/web-app/context-param[param-name='contextConfigLocation']/param-value".equals(xpath))

            {
                // get spring config file location
                ic = new DescriptorCollector() {

                    @Override
                    public void loadInfo(String sKey, String sKeyRawValue, Map<String, Object> sInfo) {

                        Node sName = dp.selectXMLNode(
                                "/web-app/context-param[param-name='contextConfigLocation']/param-value");
                        String filterName = sName.getTextContent();
                        sInfo.put("springconfigfile", filterName);
                    }
                };
            }

            return ic;
        }

    }

    /**
     * 
     * sun-jaxws.xml descriptor
     *
     */
    public static class SunJaxWSXmlProcessor extends DescriptorProcessor {

        public SunJaxWSXmlProcessor(ProfileContext context) {
            super(context);
        }

        @Override
        public DescriptorCollector selectDescriptorCollector(String xpath) {

            final DescriptorProcessor dp = this;

            DescriptorCollector ic = null;

            if ("/endpoints/endpoint/@implementation".equals(xpath)) {

                // get servlets
                ic = new DescriptorCollector() {

                    @Override
                    public void loadInfo(String sKey, String sKeyRawValue, Map<String, Object> sInfo) {

                        Node endpoint = dp.selectXMLNode("/endpoints/endpoint[@implementation='" + sKeyRawValue + "']");
                        // service-name

                        NamedNodeMap map = endpoint.getAttributes();

                        if (endpoint != null) {
                            for (int i = 0; i < map.getLength(); i++) {
                                Node attr = map.item(i);
                                sInfo.put(attr.getNodeName(), attr.getNodeValue());
                            }
                        }
                    }
                };
            }

            return ic;
        }

        @Override
        protected List<String> getDescriptorFileLocations(String webAppRoot) {

            List<String> files = new ArrayList<String>();

            files.add(webAppRoot + "/WEB-INF/sun-jaxws.xml");

            return files;
        }

    }

    // ----------------------------------------------annotation based component
    // processor-------------------------------------------------------------

    /**
     * ComponentAnnotationProcessor is the interface to process component class to get the info via annotations
     * 
     * @author zhen zhang
     *
     */
    private static abstract class ComponentAnnotationProcessor {

        public final static String DEFAULT_VALUE = "_default_value";

        @SuppressWarnings("rawtypes")
        public abstract Map<String, Object> process(Class annoCls, Class<?> comCls, ProfileContext context);

        @SuppressWarnings({ "unchecked", "rawtypes" })
        protected <T> void putAnnoValue(Class<?> c, Class annoCls, String configName, Class<T> configCls,
                String keyName, Map<String, Object> info) {

            T value = (T) ReflectHelper.getAnnotationValue(c, annoCls, configName);

            if (null == value || DEFAULT_VALUE.equals(value)) {
                return;
            }

        }

        @SuppressWarnings("rawtypes")
        protected Class[] loadAnnoClasses(ProfileContext context, String... classNames) {

            InterceptContext ic = context.get(InterceptContext.class);

            ClassLoader webappclsLoader = (ClassLoader) ic.get(InterceptConstants.WEBAPPLOADER);

            List<Class> annoClassList = new ArrayList<Class>();

            for (String className : classNames) {

                Class c = null;
                try {
                    c = webappclsLoader.loadClass(className);
                }
                catch (ClassNotFoundException e) {
                    continue;
                }

                annoClassList.add(c);
            }

            Class[] annoClasses = new Class[annoClassList.size()];
            annoClassList.toArray(annoClasses);

            return annoClasses;
        }

    }

    /**
     * @WebSerice
     * 
     * 
     * @author zhen zhang
     *
     */
    public static class WebServiceInfoProcessor extends ComponentAnnotationProcessor {

        @SuppressWarnings("rawtypes")
        @Override
        public Map<String, Object> process(Class annoCls, Class<?> c, ProfileContext context) {

            Map<String, Object> info = new LinkedHashMap<String, Object>();

            Class[] annoClasses = this.loadAnnoClasses(context, "javax.jws.WebService", "javax.jws.WebMethod");

            if (null == annoClasses || annoClasses.length == 0) {
                return info;
            }
            // get annotation info
            getClassAnnoInfo(c, info, annoClasses[0]);

            // 2.get method infos
            getMethodInfo(c, info, annoClasses[1]);
            return info;
        }

    }

    /**
     * 
     * @WebServiceProvider
     * 
     * @author zhen zhang
     *
     */
    private static class WebServiceProviderInfoProcessor extends ComponentAnnotationProcessor {

        @SuppressWarnings("rawtypes")
        @Override
        public Map<String, Object> process(Class annoCls, Class<?> c, ProfileContext context) {

            Map<String, Object> info = new LinkedHashMap<String, Object>();

            Class[] annoClasses = this.loadAnnoClasses(context, "javax.xml.ws.WebServiceProvider");

            if (null == annoClasses || annoClasses.length == 0) {
                return info;
            }
            // get annotation info
            getClassAnnoInfo(c, info, annoClasses);

            return info;
        }
    }

    /**
     * @WebServlet
     * 
     * @author zhen zhang
     *
     */
    public static class WebServletInfoProcessor extends ComponentAnnotationProcessor {

        @SuppressWarnings("rawtypes")
        @Override
        public Map<String, Object> process(Class annoCls, Class<?> c, ProfileContext context) {

            Map<String, Object> info = new LinkedHashMap<String, Object>();

            // get annotation info
            Class[] annoClasses = this.loadAnnoClasses(context, "javax.servlet.annotation.WebServlet");

            if (null == annoClasses || annoClasses.length == 0) {
                return info;
            }
            // get annotation info
            getClassAnnoInfo(c, info, annoClasses);

            figureOutServiceEngine(c, context, info);

            return info;
        }

        /**
         * figureOutServiceEngine for descriptor
         * 
         * @param annoClsName
         * @param context
         * @param info
         */
        public static void figureOutServiceEngine(String annoClsName, ProfileContext context,
                Map<String, Object> info) {

            InterceptContext ic = context.get(InterceptContext.class);

            ClassLoader webappclsLoader = (ClassLoader) ic.get(InterceptConstants.WEBAPPLOADER);

            try {
                Class<?> annoCls = webappclsLoader.loadClass(annoClsName);

                figureOutServiceEngine(annoCls, context, info);
            }
            catch (ClassNotFoundException e) {
                // ignore
            }
        }

        /**
         * figureOutServiceEngine for annotation scanning
         * 
         * @param annoCls
         * @param context
         * @param info
         */
        @SuppressWarnings("rawtypes")
        public static void figureOutServiceEngine(Class annoCls, ProfileContext context, Map<String, Object> info) {

            /**
             * NOTE: identify CXF,JERSERY,SPRING,JAXWS-RI Engine Servlet HERE TODO: still hunman knowledge not Micro AI
             */
            InterceptContext ic = context.get(InterceptContext.class);

            ClassLoader webappclsLoader = (ClassLoader) ic.get(InterceptConstants.WEBAPPLOADER);

            try {
                Class<?> cxfClass = webappclsLoader.loadClass("org.apache.cxf.transport.servlet.CXFNonSpringServlet");

                if (cxfClass.isAssignableFrom(annoCls)) {
                    info.put("engine", "cxf");
                }
                else {
                    try {
                        cxfClass = webappclsLoader.loadClass("org.apache.cxf.transport.servlet.CXFServlet");

                        if (cxfClass.isAssignableFrom(annoCls)) {
                            info.put("engine", "cxf");
                        }
                    }
                    catch (NoClassDefFoundError error) {
                        // ignore
                    }
                }
            }
            catch (ClassNotFoundException e) {
                // ignore
            }

            try {
                Class<?> jerseyClass = webappclsLoader
                        .loadClass("com.sun.jersey.spi.spring.container.servlet.SpringServlet");
                if (jerseyClass.isAssignableFrom(annoCls)) {
                    String oEngine = (info.containsKey("engine")) ? (String) info.get("engine") + "," : "";
                    info.put("engine", oEngine + "jersey");
                }

            }
            catch (ClassNotFoundException e) {
                // ignore
            }

            try {
                // jersey 2.x
                Class<?> jerseyClass = webappclsLoader.loadClass("org.glassfish.jersey.servlet.ServletContainer");
                if (jerseyClass.isAssignableFrom(annoCls)) {
                    String oEngine = (info.containsKey("engine")) ? (String) info.get("engine") + "," : "";
                    if (oEngine.indexOf("jersey") == -1) {
                        info.put("engine", oEngine + "jersey");
                    }

                }

            }
            catch (ClassNotFoundException e) {
                // jersey 1.x
                try {
                    Class<?> jerseyClass = webappclsLoader
                            .loadClass("com.sun.jersey.spi.container.servlet.ServletContainer");
                    if (jerseyClass.isAssignableFrom(annoCls)) {
                        String oEngine = (info.containsKey("engine")) ? (String) info.get("engine") + "," : "";
                        if (oEngine.indexOf("jersey") == -1) {
                            info.put("engine", oEngine + "jersey");
                        }
                    }

                }
                catch (ClassNotFoundException ex) {
                    // ignore
                }
            }

            try {
                Class<?> springmvcClass = webappclsLoader
                        .loadClass("org.springframework.web.servlet.DispatcherServlet");
                if (springmvcClass.isAssignableFrom(annoCls)) {

                    String oEngine = (info.containsKey("engine")) ? (String) info.get("engine") + "," : "";

                    info.put("engine", oEngine + "springmvc");
                }
            }
            catch (ClassNotFoundException e) {
                // ignore
            }

            try {
                Class<?> jaxwsRIClass = webappclsLoader.loadClass("com.sun.xml.ws.transport.http.servlet.WSServlet");
                if (jaxwsRIClass.isAssignableFrom(annoCls)) {

                    String oEngine = (info.containsKey("engine")) ? (String) info.get("engine") + "," : "";

                    info.put("engine", oEngine + "jaxws-ri");
                }
            }
            catch (ClassNotFoundException e) {
                // ignore
            }

            // FIX XFire Support
            try {
                Class<?> jaxwsRIClass = webappclsLoader.loadClass("org.codehaus.xfire.spring.XFireSpringServlet");
                if (jaxwsRIClass.isAssignableFrom(annoCls)) {

                    String oEngine = (info.containsKey("engine")) ? (String) info.get("engine") + "," : "";

                    info.put("engine", oEngine + "xfire");
                }
            }
            catch (ClassNotFoundException e) {
                // ignore
            }

            // FIX Axis2 Support
            try {
                Class<?> jaxwsRIClass = webappclsLoader.loadClass("org.apache.axis2.transport.http.AxisServlet");
                if (jaxwsRIClass.isAssignableFrom(annoCls)) {

                    String oEngine = (info.containsKey("engine")) ? (String) info.get("engine") + "," : "";

                    info.put("engine", oEngine + "axis2");
                }
            }
            catch (ClassNotFoundException e) {
                // ignore
            }

            // FIX Hession Support
            try {
                Class<?> jaxwsRIClass = webappclsLoader.loadClass("com.caucho.hessian.server.HessianServlet");
                if (jaxwsRIClass.isAssignableFrom(annoCls)) {

                    String oEngine = (info.containsKey("engine")) ? (String) info.get("engine") + "," : "";

                    info.put("engine", oEngine + "hession");
                }
            }
            catch (ClassNotFoundException e) {
                // ignore
            }

            // FIX Wink Support
            try {
                Class<?> jaxwsRIClass = webappclsLoader
                        .loadClass("org.apache.wink.server.internal.servlet.RestServlet");
                if (jaxwsRIClass.isAssignableFrom(annoCls)) {

                    String oEngine = (info.containsKey("engine")) ? (String) info.get("engine") + "," : "";

                    info.put("engine", oEngine + "wink");
                }
            }
            catch (ClassNotFoundException e) {
                // ignore
            }
        }
    }

    /**
     * @WebFilter
     * 
     * @author zhen zhang
     *
     */
    private static class WebFilterInfoProcessor extends ComponentAnnotationProcessor {

        @SuppressWarnings("rawtypes")
        @Override
        public Map<String, Object> process(Class annoCls, Class<?> c, ProfileContext context) {

            Map<String, Object> info = new LinkedHashMap<String, Object>();

            Class[] annoClasses = this.loadAnnoClasses(context, "javax.servlet.annotation.WebFilter");

            if (null == annoClasses || annoClasses.length == 0) {
                return info;
            }
            // get annotation info
            getClassAnnoInfo(c, info, annoClasses);

            return info;
        }
    }

    /**
     * @WebListener
     * 
     * @author zhen zhang
     *
     */
    private static class WebListenerInfoProcessor extends ComponentAnnotationProcessor {

        @SuppressWarnings("rawtypes")
        @Override
        public Map<String, Object> process(Class annoCls, Class<?> c, ProfileContext context) {

            Map<String, Object> info = new LinkedHashMap<String, Object>();

            return info;
        }
    }

    /**
     * @Path info processor
     * 
     * @author zhen zhang
     *
     */
    private static class JAXRSResourceInfoProcessor extends ComponentAnnotationProcessor {

        @SuppressWarnings("rawtypes")
        @Override
        public Map<String, Object> process(Class annoCls, Class<?> comCls, ProfileContext context) {

            Map<String, Object> info = new LinkedHashMap<String, Object>();

            Class[] annoClasses = this.loadAnnoClasses(context, "javax.ws.rs.Path", "javax.ws.rs.GET",
                    "javax.ws.rs.DELETE", "javax.ws.rs.POST", "javax.ws.rs.OPTIONS", "javax.ws.rs.HEAD",
                    "javax.ws.rs.PUT", "javax.ws.rs.Consumes", "javax.ws.rs.Produces");

            if (null == annoClasses || annoClasses.length == 0) {
                return info;
            }

            // get @Path value on the resource class
            getClassAnnoInfo(comCls, info, annoClasses[0]);

            // collect the method info
            getMethodInfo(comCls, info, annoClasses);

            return info;
        }

        /**
         * TODO:need scan out all classes extends from "javax.ws.rs.core.Application"
         */
        @SuppressWarnings("unused")
        private List<Class<?>> figureOutResourcesFromApplicationClasses(ProfileContext context) {

            FastClasspathScanner fcs = context.get(FastClasspathScanner.class);

            List<String> applicationClasses = fcs.getNamesOfSubclassesOf("javax.ws.rs.core.Application");

            if (null == applicationClasses || applicationClasses.size() == 0) {
                return null;
            }
            // TODO: no finish
            return null;
        }

    }

    /**
     * @Controller
     * 
     * @author zhen zhang
     *
     */
    private static class SpringMVCInfoProcessor extends ComponentAnnotationProcessor {

        @SuppressWarnings("rawtypes")
        @Override
        public Map<String, Object> process(Class annoCls, Class<?> comCls, ProfileContext context) {

            Map<String, Object> info = new LinkedHashMap<String, Object>();

            // load @RequestMapping class
            Class[] annoClasses = this.loadAnnoClasses(context, "org.springframework.stereotype.Controller",
                    "org.springframework.web.bind.annotation.RestController",
                    "org.springframework.web.bind.annotation.RequestMapping");

            if (null == annoClasses || annoClasses.length == 0) {
                return info;
            }

            getClassAnnoInfo(comCls, info, annoClasses);

            // get method info
            getMethodInfo(comCls, info, annoClasses[annoClasses.length - 1]);

            return info;
        }
    }

    /**
     * @ImportResource
     * 
     * @author minglang yang
     *
     */
    public static class SpringResourceInfoProcessor extends ComponentAnnotationProcessor {

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public Map<String, Object> process(Class annoCls, Class<?> c, ProfileContext context) {

            Map<String, Object> info = new LinkedHashMap<String, Object>();

            // get annotation info
            getClassAnnoInfo(c, info, annoCls);

            // set resourceLocation info to context for SpringXmlProcessor
            LinkedList<String> locationsInfo = (LinkedList<String>) context.get("ResouceLocations");

            if (locationsInfo == null) {
                locationsInfo = new LinkedList<String>();
                context.put("ResouceLocations", locationsInfo);
            }

            Map<String, Object> annoInfos = ((Map<String, Object>) info.get("anno"));

            List<String> locations = (List<String>) ((Map<String, Object>) annoInfos.get(annoCls.getName()))
                    .get("value");

            locationsInfo.addAll(locations);

            return info;
        }

    }
    // --------------------------------------------------------------------------------------------

    /**
     * componentClassNames 功能1：作为每种Component的标识名称 功能2：由于要对每个webapp做anno scan，所以并不确定这些annotation class在每个webapp
     * 里面都存在，因此每次profiling动作，都要重新尝试加载这些annotation class，只有加载到的annotation class 才进行对这个annotation的扫描
     */
    private static final String[] componentClassNames = new String[] {
            // spring Resource , we shoud first scan this anno
            "org.springframework.context.annotation.ImportResource",
            // servlet,filter,all listeners
            "javax.servlet.annotation.WebServlet", "javax.servlet.annotation.WebFilter",
            "javax.servlet.annotation.WebListener",
            // jaxws
            "javax.jws.WebService", "javax.xml.ws.WebServiceProvider",
            // jaxrs
            "javax.ws.rs.Path",
            // spring MVC
            "org.springframework.stereotype.Controller", "org.springframework.web.bind.annotation.RestController"

    };

    /**
     * we should exclude the methods from Object
     */
    private static final String[] methodBlackLists = new String[] { "wait", "equals", "notify", "notifyAll", "hashCode",
            "toString", "getClass" };

    private static final Map<String, ComponentAnnotationProcessor> annoProcessors = new HashMap<String, ComponentAnnotationProcessor>();

    private static final Map<String, Class<? extends DescriptorProcessor>> xpathProcessors = new LinkedHashMap<String, Class<? extends DescriptorProcessor>>();

    private static final Map<String, String[]> anno2xpath = new HashMap<String, String[]>();

    private static final Map<String, String> methodBlackListMap = new HashMap<String, String>();

    private static String[] scanPackage = new String[1];

    static {

        /**
         * init annotation to descriptor's xpath
         */
        anno2xpath.put("javax.servlet.annotation.WebServlet", new String[] { "/web-app/servlet/servlet-class" });
        anno2xpath.put("javax.servlet.annotation.WebFilter", new String[] { "/web-app/filter/filter-class" });
        anno2xpath.put("javax.servlet.annotation.WebListener", new String[] { "/web-app/listener/listener-class" });
        anno2xpath.put("javax.jws.WebService",
                new String[] { "/beans/endpoint/@id", "/beans/server/@id", "/endpoints/endpoint/@implementation" });

        /**
         * init the mapping of anno class to their info processor
         */
        annoProcessors.put("javax.jws.WebService", new WebServiceInfoProcessor());
        annoProcessors.put("javax.xml.ws.WebServiceProvider", new WebServiceProviderInfoProcessor());
        annoProcessors.put("javax.servlet.annotation.WebServlet", new WebServletInfoProcessor());
        annoProcessors.put("javax.servlet.annotation.WebFilter", new WebFilterInfoProcessor());
        annoProcessors.put("javax.servlet.annotation.WebListener", new WebListenerInfoProcessor());
        annoProcessors.put("javax.ws.rs.Path", new JAXRSResourceInfoProcessor());
        annoProcessors.put("org.springframework.stereotype.Controller", new SpringMVCInfoProcessor());
        annoProcessors.put("org.springframework.web.bind.annotation.RestController", new SpringMVCInfoProcessor());
        annoProcessors.put("org.springframework.context.annotation.ImportResource", new SpringResourceInfoProcessor());
        /**
         * init xpath processors
         */
        xpathProcessors.put("/web-app/servlet/servlet-class", WebXmlProcessor.class);
        xpathProcessors.put("/web-app/filter/filter-class", WebXmlProcessor.class);
        xpathProcessors.put("/web-app/listener/listener-class", WebXmlProcessor.class);
        // /beans/bean/webservice id
        xpathProcessors.put("/beans/endpoint/@id", SpringXmlProcessor.class);
        xpathProcessors.put("/beans/server/@id", SpringXmlProcessor.class);
        xpathProcessors.put("/endpoints/endpoint/@implementation", SunJaxWSXmlProcessor.class);

        /**
         * init methodBlackListMap
         */
        for (String mName : methodBlackLists) {
            methodBlackListMap.put(mName, "");
        }
    }

    // -------------------------------------------------------------------------------------------------

    // for dubbo profiling
    private DubboProfileHandler dubboProfileHandler;

    public ComponentProfileHandler() {
        dubboProfileHandler = new DubboProfileHandler();
    }

    @Override
    public void doProfiling(ProfileElement elem, ProfileContext context) {

        UAVServer.ServerVendor sv = (UAVServer.ServerVendor) UAVServer.instance()
                .getServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR);

        // only support JEE Application, not support MSCP Application
        if (sv == UAVServer.ServerVendor.MSCP) {
            return;
        }

        if (!ProfileConstants.PROELEM_COMPONENT.equals(elem.getElemId())) {
            return;
        }

        InterceptContext ic = context.get(InterceptContext.class);

        if (ic == null) {
            this.logger.warn("Profile:Annotation FAILs as No InterceptContext available", null);
            return;
        }

        /**
         * 1.get webappclassloader
         */
        ClassLoader webappclsLoader = (ClassLoader) ic.get(InterceptConstants.WEBAPPLOADER);

        if (null == webappclsLoader) {
            this.logger.warn("Profile:JARS FAILs as No webappclsLoader available", null);
            return;
        }

        /**
         * 1.5 for other none JEE or Spring tech profiling
         */
        dubboProfileHandler.doProfiling(elem, context);

        /**
         * 2.load available annotation classes
         */
        Map<String, Class<?>> annoAvailableClasses = new HashMap<String, Class<?>>();

        for (String annoClsName : componentClassNames) {
            try {
                Class<?> c = webappclsLoader.loadClass(annoClsName);
                annoAvailableClasses.put(annoClsName, c);
            }
            catch (ClassNotFoundException e) {
                // ignore
                if (this.logger.isDebugable()) {
                    this.logger.warn("Annotation Class [" + annoClsName + "] is not found in web application ["
                            + elem.getRepository().getProfile().getId() + "]", e);
                }
            }
        }

        /**
         * 3. see what kind of components we could get via annotations
         */
        UAVServer.ServerVendor vendor = (ServerVendor) UAVServer.instance()
                .getServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR);

        /**
         * NOTE: currently for spring boot, we use its base classloader as the webappclassloader, and should scan all
         * packages
         */
        ClassLoader[] classLoaders = null;

        if (vendor != UAVServer.ServerVendor.SPRINGBOOT) {
            classLoaders = new ClassLoader[] { webappclsLoader };
            String scanPackages = System.getProperty("com.creditease.uav.uavmof.profile.package.header");
            if (StringHelper.isEmpty(scanPackages)) {
                scanPackage[0] = "com";
            }
            else {
                scanPackage = scanPackages.split(",");
            }
        }
        else {
            scanPackage[0] = "";
        }
        
        FastClasspathScanner fcs = new FastClasspathScanner(classLoaders, scanPackage);
        fcs.scan();
        // store FastClasspathScanner instance into ProfileContext
        context.put(FastClasspathScanner.class, fcs);

        /**
         * 4.tide components we get from annotations & deployment descriptors
         */
        // store the DescriptorProcessor instance for better performance
        Map<String, DescriptorProcessor> dpInstances = new LinkedHashMap<String, DescriptorProcessor>();

        // get web application root
        InterceptContext itContext = context.get(InterceptContext.class);
        String webAppRoot = (String) itContext.get(InterceptConstants.BASEPATH);

        for (String componentClassName : componentClassNames) {

            // set the instance id = simple name of the annotation class
            ProfileElementInstance inst = elem.getInstance(componentClassName);

            // load componentsByAnno first
            loadComponentsByAnno(context, webappclsLoader, fcs, annoAvailableClasses, componentClassName, inst);

            // try to load componentsByDescriptor
            loadComponentsByDescriptor(dpInstances, webAppRoot, context, componentClassName, inst);

            // try to load componentsByDynamic Creation, currently is for servlet 3.x and webservice
            loadComponentsByDynamic(itContext, componentClassName, inst, context, annoAvailableClasses, fcs);

            /**
             * NOTE: in order to control the monitor data url, we need collect ther servlet url to help to identify if
             * an url is a service url
             */
            if (componentClassName.equalsIgnoreCase("javax.servlet.annotation.WebServlet")) {

                collectServletInfoForMonitor(inst);
            }

            /**
             * collectProfileServiceMap
             */
            collectProfileServiceMap(componentClassName, inst);
        }

        /**
         * 4.1 add common info instance from descriptor to profile element
         */
        // web.xml related common info
        loadCommonInfoFromWebXML(elem, dpInstances, webAppRoot, context);

        /**
         * 5.confirm there is update
         */
        elem.getRepository().setUpdate(true);

        // release resources quickly
        dpInstances.clear();
    }

    /**
     * NOTE: 收集所有应用的服务代码与url模式的匹配关系
     * 
     * @param componentClassName
     * @param inst
     */
    @SuppressWarnings("unchecked")
    private void collectProfileServiceMap(String componentClassName, ProfileElementInstance inst) {

        if (componentClassName.equalsIgnoreCase("javax.servlet.annotation.WebListener")) {
            return;
        }

        ProfileServiceMapMgr smgr = (ProfileServiceMapMgr) UAVServer.instance().getServerInfo("profile.servicemapmgr");

        String appid = inst.getProfileElement().getRepository().getProfile().getId();

        for (String className : inst.getValues().keySet()) {

            Map<String, Object> classInfo = (Map<String, Object>) inst.getValues().get(className);

            // Servlet
            if (componentClassName.equalsIgnoreCase("javax.servlet.annotation.WebServlet")) {

                Collection<String> urls = getServletFilterUrlPatterns(classInfo);
                smgr.addServiceMapBinding(appid, className, "service", urls, 2);
            }
            // Filter
            else if (componentClassName.equalsIgnoreCase("javax.servlet.annotation.WebFilter")) {

                Collection<String> urls = getServletFilterUrlPatterns(classInfo);
                smgr.addServiceMapBinding(appid, className, "doFilter", urls, 1);
            }
            // JAXWS
            else if (componentClassName.equalsIgnoreCase("javax.jws.WebService")) {
                addAppFrkServiceMapBinding(smgr, appid, className, classInfo, "javax.jws.WebService", "serviceName",
                        null, true);
            }
            // JAXWS
            else if (componentClassName.equalsIgnoreCase("javax.xml.ws.WebServiceProvider")) {
                // TODO
            }
            // JAXRS
            else if (componentClassName.equalsIgnoreCase("javax.ws.rs.Path")) {
                addAppFrkServiceMapBinding(smgr, appid, className, classInfo, "javax.ws.rs.Path", "value",
                        "javax.ws.rs.Path", false);
            }
            // Spring
            else if (componentClassName.equalsIgnoreCase("org.springframework.stereotype.Controller")
                    || componentClassName.equalsIgnoreCase("org.springframework.web.bind.annotation.RestController")) {
                addAppFrkServiceMapBinding(smgr, appid, className, classInfo,
                        "org.springframework.web.bind.annotation.RequestMapping", "value",
                        "org.springframework.web.bind.annotation.RequestMapping", false);
            }
        }
    }

    /**
     * 
     * @param smgr
     * @param appid
     * @param className
     * @param classInfo
     * @param classPathAnnoClass
     * @param methodPathAnnoClass
     */
    @SuppressWarnings("unchecked")
    private void addAppFrkServiceMapBinding(ProfileServiceMapMgr smgr, String appid, String className,
            Map<String, Object> classInfo, String classPathAnnoClass, String classPathAnnoAttrName,
            String methodPathAnnoClass, boolean isJAXWS) {

        Map<String, Object> anno = (Map<String, Object>) classInfo.get("anno");

        if (anno == null) {
            return;
        }

        /**
         * Step 1: get the class anno to get the class's url path
         */
        Map<String, Object> classAnno = (Map<String, Object>) anno.get(classPathAnnoClass);

        if (classAnno == null) {
            return;
        }

        Object value = classAnno.get(classPathAnnoAttrName);

        Collection<String> classPaths = new ArrayList<String>();

        /**
         * for JaxWS, we can't match the method, so only className is enough
         */
        if (isJAXWS == true && value == null) {
            /**
             * by default, if without serviceName is declared, using the classSimpleName
             */
            String[] classDes = className.split("\\.");
            classPaths.add(this.formatRelativePath(classDes[classDes.length - 1], false));
            smgr.addServiceMapBinding(appid, className, null, classPaths, 0);
            return;
        }
        else if (isJAXWS == true && value != null) {
            classPaths.add(this.formatRelativePath(value.toString(), false));
            smgr.addServiceMapBinding(appid, className, null, classPaths, 0);
            return;
        }

        if (value == null) {
            return;
        }

        if (Collection.class.isAssignableFrom(value.getClass())) {
            classPaths = (Collection<String>) value;
        }
        else {
            classPaths.add(value.toString());
        }

        Map<String, Object> methods = (Map<String, Object>) classInfo.get("methods");

        /**
         * Step 2: get methods' url path
         */
        for (String classPath : classPaths) {

            String finalClassPath = this.formatRelativePath(classPath, false);

            for (String method : methods.keySet()) {

                Map<String, Object> methodInfo = (Map<String, Object>) methods.get(method);

                if (methodInfo == null) {
                    continue;
                }

                Map<String, Object> methodAnno = (Map<String, Object>) methodInfo.get("anno");

                if (methodAnno == null) {
                    continue;
                }

                Map<String, Object> methodPathAnno = (Map<String, Object>) methodAnno.get(methodPathAnnoClass);

                if (methodPathAnno == null) {
                    continue;
                }

                Object pvalue = methodPathAnno.get("value");

                if (pvalue == null) {
                    continue;
                }

                Collection<String> methodPaths = new ArrayList<String>();

                if (Collection.class.isAssignableFrom(pvalue.getClass())) {
                    methodPaths = (Collection<String>) pvalue;
                }
                else {
                    methodPaths.add(pvalue.toString());
                }

                Collection<String> finalMethodPaths = new ArrayList<String>();

                boolean allowMethodPathAbMatch = false;

                for (String methodPath : methodPaths) {

                    /**
                     * NOTE：支持方法级的模糊匹配，只要有一个方法的URL是带*的，该方法所有url都支持模糊匹配（有点小limitation）
                     */
                    /**
                     * option 1: /aaa/*
                     */
                    if (methodPath.endsWith("*") == true) {
                        allowMethodPathAbMatch = true;
                    }
                    /**
                     * option 2: /aaa/*.do
                     */
                    else {
                        // sometimes the patterns maybe /*.do or /xxxxx/*.do
                        int index = methodPath.indexOf("*");
                        if (index > -1) {
                            allowMethodPathAbMatch = true;
                        }
                    }

                    String finalMethodPath = finalClassPath + this.formatRelativePath(methodPath, false);

                    finalMethodPaths.add(finalMethodPath);
                }

                smgr.addServiceMapBinding(appid, className, method, finalMethodPaths, 0, allowMethodPathAbMatch);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Collection<String> getServletFilterUrlPatterns(Map<String, Object> servlerInfoMap) {

        Map<String, Object> dyn = (Map<String, Object>) servlerInfoMap.get("dyn");

        if (dyn != null) {
            Collection<String> urls = (Collection<String>) dyn.get("urlPatterns");
            Collection<String> turls = new ArrayList<String>();
            for (String url : urls) {
                turls.add(formatRelativePath(url, false));
            }
            return turls;
        }

        Map<String, Object> des = (Map<String, Object>) servlerInfoMap.get("des");

        if (des != null) {
            List<String> urls = (List<String>) des.get("urlPatterns");
            Collection<String> turls = new ArrayList<String>();
            for (String url : urls) {
                turls.add(formatRelativePath(url, false));
            }
            return turls;
        }

        Map<String, Object> anno = (Map<String, Object>) servlerInfoMap.get("anno");

        if (anno != null) {
            List<String> urls = (List<String>) anno.get("urlPatterns");
            Collection<String> turls = new ArrayList<String>();
            for (String url : urls) {
                turls.add(formatRelativePath(url, false));
            }
            return turls;
        }

        return Collections.emptyList();
    }

    /**
     * NOTE: 收集该进程里面所有应用的Servlet的url pattern，从而控制实时数据采集范围
     * 
     * @param inst
     */
    @SuppressWarnings("unchecked")
    private void collectServletInfoForMonitor(ProfileElementInstance inst) {

        HashSet<String> murls = (HashSet<String>) UAVServer.instance().getServerInfo("monitor.urls");

        for (Object sObj : inst.getValues().values()) {

            Map<String, Object> servlerInfoMap = (Map<String, Object>) sObj;

            Map<String, Object> dyn = (Map<String, Object>) servlerInfoMap.get("dyn");

            if (dyn != null) {
                Collection<String> urls = (Collection<String>) dyn.get("urlPatterns");
                if (urls != null) {
                    for (String url : urls) {
                        murls.add(formatRelativePath(url, true));
                    }
                }
                continue;
            }

            Map<String, Object> des = (Map<String, Object>) servlerInfoMap.get("des");

            if (des != null) {
                List<String> urls = (List<String>) des.get("urlPatterns");
                if (urls != null) {
                    for (String url : urls) {
                        murls.add(formatRelativePath(url, true));
                    }
                }
                continue;
            }

            Map<String, Object> anno = (Map<String, Object>) servlerInfoMap.get("anno");

            if (anno != null) {
                List<String> urls = (List<String>) anno.get("urlPatterns");
                if (urls != null) {
                    for (String url : urls) {
                        murls.add(formatRelativePath(url, true));
                    }
                }
                continue;
            }
        }
    }

    private String formatRelativePath(String resourceClassRelativePath, boolean needLastSlash) {

        // remove last *
        if (resourceClassRelativePath.endsWith("*") == true) {
            if (resourceClassRelativePath.length() == 1) {
                resourceClassRelativePath = "";
            }
            else {
                resourceClassRelativePath = resourceClassRelativePath.substring(0,
                        resourceClassRelativePath.length() - 1);
            }
        }
        else {
            // sometimes the patterns maybe /*.do or /xxxxx/*.do
            int index = resourceClassRelativePath.indexOf("*");
            if (index > -1) {
                resourceClassRelativePath = resourceClassRelativePath.substring(0, index);
            }
        }

        // add first /
        if (resourceClassRelativePath.indexOf("/") != 0) {
            resourceClassRelativePath = "/" + resourceClassRelativePath;
        }

        // add last /
        if (needLastSlash == true
                && resourceClassRelativePath.lastIndexOf("/") != resourceClassRelativePath.length() - 1) {
            resourceClassRelativePath = resourceClassRelativePath + "/";
        }
        // remove last /
        else if (needLastSlash == false
                && resourceClassRelativePath.lastIndexOf("/") == resourceClassRelativePath.length() - 1) {
            resourceClassRelativePath = resourceClassRelativePath.substring(0, resourceClassRelativePath.length() - 1);
        }

        return resourceClassRelativePath;
    }

    /**
     * loadComponentsByDynamic
     * 
     * @param context
     * 
     * @param context
     * @param componentClassName
     * @param inst
     * @param context
     */
    protected void loadComponentsByDynamic(InterceptContext itContext, String componentClassName,
            ProfileElementInstance inst, ProfileContext context, Map<String, Class<?>> annoAvailableClasses,
            FastClasspathScanner fcs) {

        /**
         * NOTE: in order to compatible with Servlet 2.5, using Reflection to call the Dynamic Servlet Profiling
         */
        getDynInfoForServlet(itContext, componentClassName, inst, context);
        // in order to compatible with jax-ws
        getDynInfoForWebService(componentClassName, inst, context, annoAvailableClasses, fcs);

    }

    @SuppressWarnings("unchecked")
    private void getDynInfoForWebService(String componentClassName, ProfileElementInstance inst, ProfileContext context,
            Map<String, Class<?>> annoAvailableClasses, FastClasspathScanner fcs) {

        if (!componentClassName.equals("javax.jws.WebService")) {
            return;
        }

        Class<?> annoClass = annoAvailableClasses.get(componentClassName);

        List<String> coms = fcs.getNamesOfClassesWithAnnotation(annoClass);

        if (null == coms || coms.isEmpty()) {
            return;
        }
        // construct data "dyn" for webservice
        InterceptContext ic = context.get(InterceptContext.class);
        List<WebServiceProfileInfo> wsli = (ArrayList<WebServiceProfileInfo>) ic.get("webservice.profile.info");
        if (wsli == null || wsli.isEmpty()) {
            return;
        }
        for (WebServiceProfileInfo wslinfo : wsli) {
            String url = wslinfo.getUrl();
            String clazz = (wslinfo.getImpl() instanceof String) ? (String) wslinfo.getImpl()
                    : wslinfo.getImpl().getClass().getName();
            if (coms.contains(clazz)) {
                Map<String, Object> dynInfos = new LinkedHashMap<String, Object>();
                Map<String, Object> info = (Map<String, Object>) inst.getValues().get(clazz);
                dynInfos.put("url", url);
                dynInfos.put("className", clazz);
                info.put("dyn", dynInfos);
                inst.getValues().put(clazz, info);
            }
        }

    }

    private void getDynInfoForServlet(InterceptContext itContext, String componentClassName,
            ProfileElementInstance inst, ProfileContext context) {

        Object obj = ReflectHelper.newInstance("com.creditease.monitor.jee.servlet30.DynamicServletConfigProfiler");

        if (obj != null) {
            ReflectHelper.invoke("com.creditease.monitor.jee.servlet30.DynamicServletConfigProfiler", obj,
                    "loadComponentsByDynamic", new Class<?>[] { InterceptContext.class, String.class,
                            ProfileElementInstance.class, ProfileContext.class },
                    new Object[] { itContext, componentClassName, inst, context });

        }

    }

    /**
     * loadCommonInfoFromWebXML
     * 
     * @param elem
     * @param dpInstances
     * @param webAppRoot
     */
    protected void loadCommonInfoFromWebXML(ProfileElement elem, Map<String, DescriptorProcessor> dpInstances,
            String webAppRoot, ProfileContext context) {

        // append a "webapp" instance to collect web.xml related common info
        ProfileElementInstance inst = elem.getInstance("webapp");
        DescriptorProcessor webxmlDP = dpInstances.get(WebXmlProcessor.class.getName());
        InterceptContext ic = context.get(InterceptContext.class);
        String contextpath = (String) ic.get(InterceptConstants.CONTEXTPATH);
        // get the web application display name as the application name
        String appname = (String) ic.get(InterceptConstants.APPNAME);
        if (appname != null && appname.length() > 0) {
            inst.setValue("appname", appname);
        }
        else {
            webxmlDP.parseToPEIWithValueKey(inst, "appname", webAppRoot, "/web-app/display-name",
                    XMLNodeType.ELEMENT_NODE);
        }
        // get the web application description as app description
        webxmlDP.parseToPEIWithValueKey(inst, "appdes", webAppRoot, "/web-app/description", XMLNodeType.ELEMENT_NODE);
        // get the spring context config location
        webxmlDP.parseToPEIWithValueKey(inst, "springctx", webAppRoot,
                "/web-app/context-param[param-name='contextConfigLocation']/param-value", XMLNodeType.ELEMENT_NODE);
        // get the real path of application context root
        inst.setValue("webapproot", webAppRoot);
        // get the app Http URL
        inst.setValue("appurl", getServiceURI(contextpath));
        // get customized metrics
        // getCustomizedMetrics(inst);
        // get app group
        getAppGroup(inst);
    }

    /**
     * getAppGroup
     * 
     * @param inst
     */
    private void getAppGroup(ProfileElementInstance inst) {

        String JAppGroup = System.getProperty("JAppGroup", "");

        inst.setValue("appgroup", JAppGroup);
    }

    // /**
    // * getCustomizedMetrics
    // *
    // * @param inst
    // */
    // private void getCustomizedMetrics(ProfileElementInstance inst) {
    //
    // @SuppressWarnings("rawtypes")
    // Map<String, Map> metrics = new HashMap<String, Map>();
    //
    // Enumeration<?> enumeration = System.getProperties().propertyNames();
    //
    // while (enumeration.hasMoreElements()) {
    //
    // String name = (String) enumeration.nextElement();
    //
    // int moIndex = name.indexOf("mo@");
    //
    // if (moIndex != 0) {
    // continue;
    // }
    //
    // try {
    // String[] metricsArray = name.split("@");
    //
    // // add metricName to customizedMetrics
    // if (metricsArray.length == 3) {
    // metrics.put(metricsArray[1], JSONHelper.toObject(metricsArray[2], Map.class));
    // }
    // else {
    // metrics.put(metricsArray[1], Collections.emptyMap());
    // }
    // }
    // catch (Exception e) {
    // logger.error("Parsing Custom Metrics[" + name + "] FAIL.", e);
    // continue;
    // }
    // }
    //
    // inst.setValue("appmetrics", JSONHelper.toString(metrics));
    // }

    private String getServiceURI(String contextpath) {

        String serviceURL = null;
        // schema://IP:port/context/
        StringBuffer serviceurl = new StringBuffer("http://");
        String ip = NetworkHelper.getLocalIP();
        int port = (Integer) this.getServerInfo(CaptureConstants.INFO_APPSERVER_LISTEN_PORT);
        serviceURL = serviceurl.append(ip).append(":").append(port).append(contextpath).append("/").toString();
        return serviceURL;
    }

    /**
     * loadComponentsByDescriptor
     * 
     * @param c
     * @param inst
     */
    protected void loadComponentsByDescriptor(Map<String, DescriptorProcessor> dpInstances, String webAppRoot,
            ProfileContext context, String componentClassName, ProfileElementInstance inst) {

        String[] xpaths = anno2xpath.get(componentClassName);

        if (null == xpaths) {
            return;
        }

        /**
         * NOTE: as one componentClassName may relay on more than one descriptors, here we need scan every possibility
         */
        for (String xpath : xpaths) {

            Class<? extends DescriptorProcessor> dpClass = xpathProcessors.get(xpath);

            if (null == dpClass) {
                continue;
            }

            DescriptorProcessor dpInst = null;
            if (!dpInstances.containsKey(dpClass.getName())) {

                dpInst = (DescriptorProcessor) ReflectHelper.newInstance(dpClass.getName(),
                        new Class<?>[] { ProfileContext.class }, new Object[] { context });

                if (null == dpInst) {
                    logger.error("INIT DescriptorProcessor instance of class[" + dpClass.getName() + "] FAIL.", null);
                    return;
                }

                // store dpInstance
                dpInstances.put(dpClass.getName(), dpInst);

                // put web.xml processor into profile context
                if (WebXmlProcessor.class.isAssignableFrom(dpClass)) {
                    context.put(WebXmlProcessor.class, (WebXmlProcessor) dpInst);
                }
                // put spring xml processor into profile context
                if (SpringXmlProcessor.class.isAssignableFrom(dpClass)) {
                    context.put(SpringXmlProcessor.class, (SpringXmlProcessor) dpInst);
                }
                // put sun-jaxws.xml processor into profile context
                if (SunJaxWSXmlProcessor.class.isAssignableFrom(dpClass)) {
                    context.put(SunJaxWSXmlProcessor.class, (SunJaxWSXmlProcessor) dpInst);
                }
            }
            else {
                dpInst = dpInstances.get(dpClass.getName());
            }

            // load componentsByDescriptor then the descriptors have a chance to override the anno, this is match the
            // JEE
            // SPEC or Common Sense
            if (dpInst != null) {
                dpInst.parseToPEI(inst, webAppRoot, xpath, XMLNodeType.ELEMENT_NODE, XMLNodeType.ATTRIBUTE_NODE);
            }
        }

    }

    /**
     * loadComponentsByAnno
     * 
     * @param context
     * @param webappclsLoader
     * @param fcs
     * @param annoClass
     * @param inst
     */
    protected void loadComponentsByAnno(ProfileContext context, ClassLoader webappclsLoader, FastClasspathScanner fcs,
            Map<String, Class<?>> annoAvailableClasses, String componentClassName, ProfileElementInstance inst) {

        if (!annoAvailableClasses.containsKey(componentClassName)) {
            return;
        }

        Class<?> annoClass = annoAvailableClasses.get(componentClassName);

        // get all classes with the target annotations
        List<String> coms = fcs.getNamesOfClassesWithAnnotation(annoClass);

        if (null == coms || coms.isEmpty()) {

            return;
        }

        // get the ComponentInfoProcessor
        ComponentAnnotationProcessor cip = annoProcessors.get(annoClass.getName());

        // set the instance values, key=class found,value=info of the class
        for (String com : coms) {

            Class<?> comCls = null;
            try {
                comCls = webappclsLoader.loadClass(com);
            }
            catch (ClassNotFoundException e) {
                // ignore
                if (this.logger.isDebugable()) {
                    this.logger.warn("Component Class [" + com + "] is not found in web application ["
                            + inst.getProfileElement().getRepository().getProfile().getId() + "]", e);
                }
                continue;
            }
            catch (NoClassDefFoundError e) {

                // ignore

                if (this.logger.isDebugable()) {

                    this.logger.warn("Component Class [" + com + "] is not found in web application ["

                            + inst.getProfileElement().getRepository().getProfile().getId() + "]", e);

                }

                continue;

            }
            try {
                // get the info of the target component class
                Map<String, Object> info = cip.process(annoClass, comCls, context);

                if (componentClassName.indexOf("org.springframework.context.annotation.ImportResource") == -1) {
                    // set info to profile instance
                    inst.setValue(com, info);
                }

            }
            catch (RuntimeException e) {
                // ignore
            }
        }

    }

}
