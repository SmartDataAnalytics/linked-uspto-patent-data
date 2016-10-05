package com.patents.aksw.xmlProcessing;
import io.writeFile;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.xpath.axes.SubContextList;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.nio.file.StandardCopyOption.*;
/**
 * This class implements several methods to do different tasks in order to prepare the patent's xml file to be
 * triplyfied into RDF data these tasks include:
 * 1- parsing a single patent xml to process its tags and add the additonal proper tags that enrich patent's infromation. 
 * 2- extracting codes used in processing the files such as kind, role and classification codes
 *  
 *
 */
public class PatentParser 
{
    //the data structures used to store the countries,states, role and kind codes
    public static Map<String,String> countries = new HashMap<String, String>();
    public static Map<String,String> roles = new HashMap<String, String>();
    public static Map<String,String> kinds = new HashMap<String, String>();
    public static Map<String,String> EPkinds = new HashMap<String, String>();
    public static Map<String,String> WOkinds = new HashMap<String, String>();
    public static Map<String,String> states = new HashMap<String, String>();

    // the data structures contain the ipc classification codes and their labels
    public static Map<String,String> classesIPCR = new HashMap<String, String>();
    public static Map<String,String> subSections = new HashMap<String, String>();
    public static Map<String,String> groups = new HashMap<String, String>();
    public static Map<String,String> subGroups = new HashMap<String, String>();

    //the data structures contain the locarno classification codes and their labels
    public static Map<String,String> classesLocarno = new HashMap<String, String>();
    public static Map<String,String> subClassesLocarno = new HashMap<String, String>();

    //the data structures contain the us classification codes and their labels
    public static Map<String,String> classesUSPC = new HashMap<String, String>();
    public static Map<String,Map<String,String>> subClassesUSPC = new HashMap<String,Map<String,String>>();

    public static List<String> missedCodes = new ArrayList<String>();

    public static List<String> getFilesWithExtension(String path, String extension, boolean absolute)
    {
        List<String> files = new ArrayList<String>();
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        for(int i = 0; i < listOfFiles.length; i++)
        {
            String filename = listOfFiles[i].getName();
            if(filename.endsWith("."+extension.toLowerCase())||filename.endsWith("."+extension.toUpperCase()))
            {
                if(absolute)
                    files.add(listOfFiles[i].toString());
                else
                    files.add(filename);
            }
        }	
        return files;
    }
    /**
     * This methods loads the recorded codes and their labels for a ptent's xml file such as date, year, kind..etc
     * This is done from their corresponding stored files that contain code:label pairs
     */
    public static void loadSetup()
    {
        String currentFodler="";
        if(IPCCodesFolder.length()==0)
        {
            currentFodler = System.getProperty("user.dir");
            currentFodler=checkPath(currentFodler);
        }
        else
            currentFodler=IPCCodesFolder;


        //////////////////////countries code
        List<String> data=null;
        File f = new File(currentFodler+"countries");
        if(f.exists() && !f.isDirectory()) 
        { 
            data = io.readFile.read(currentFodler+"countries");
            processData(data,countries,":");
        }
        else
            System.out.println("countries file is not exist");

        //////////////////////role code
        data=null;
        f = new File(currentFodler+"roles");
        if(f.exists() && !f.isDirectory()) 
        { 
            data = io.readFile.read(currentFodler+"roles");
            processData(data,roles,":");
        }
        else
            System.out.println("roles file is not exist");

        //////////////////////kinds code
        data=null;
        f = new File(currentFodler+"kinds");
        if(f.exists() && !f.isDirectory()) 
        { 
            data = io.readFile.read(currentFodler+"kinds");
            processData(data,kinds,":");
        }
        else
            System.out.println("kinds file is not exist");
        //////////////////////states code
        data=null;
        f = new File(currentFodler+"states");
        if(f.exists() && !f.isDirectory()) 
        { 
            data = io.readFile.read(currentFodler+"states");
            processData(data,states,":");
        }
        else
            System.out.println("states file is not exist");
        /////////////////// us classification (national) main classes
        data=null;
        f = new File(currentFodler+"nationalClassificationClasses");
        if(f.exists() && !f.isDirectory()) 
        { 
            data = io.readFile.read(currentFodler+"nationalClassificationClasses");
            processData(data,classesUSPC,"\t");

            //////// processing the us classes code to remove the leading 0s

            Map<String,String> newClassesCode = new HashMap<String, String>();
            Iterator<Map.Entry<String,String>> iter = classesUSPC.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String,String> entry = iter.next();
                String key = entry.getKey();
                if(key.startsWith("00"))
                {
                    String updatedKey =  key.substring(2, key.length());
                    newClassesCode.put(updatedKey, classesUSPC.get(key));//save the new key with the old value before removing it from the classes map
                    iter.remove();
                }
                else if(key.startsWith("0"))
                {
                    String updatedKey =  key.substring(1, key.length());
                    newClassesCode.put(updatedKey, classesUSPC.get(key));//save the new key with the old value before removing it from the classes map
                    iter.remove();
                }
            }
            //update the old classes map
            for (String key : newClassesCode.keySet()) {//iterate to add the new classes codes to the original map
                classesUSPC.put(key, newClassesCode.get(key));

            }
        }
        else
            System.out.println("nationalClassificationClasses file is not exist");
        ///////////////////ipc classification codes-classes///////////////
        data=null;
        f = new File(currentFodler+"classes");
        if(f.exists() && !f.isDirectory()) 
        { 
            data = io.readFile.read(currentFodler+"classes");
            processData(data,classesIPCR,":");
        }
        else
            System.out.println("classes file is not exist");
        ///////////////////ipc classification codes-sections///////////////
        data=null;
        f = new File(currentFodler+"sections");
        if(f.exists() && !f.isDirectory()) 
        { 
            data = io.readFile.read(currentFodler+"sections");
            processData(data,sections,":");
        }
        else
            System.out.println("sections file is not exist");
        ///////////////////ipc classification codes-groups///////////////
        data=null;
        f = new File(currentFodler+"groups");
        if(f.exists() && !f.isDirectory()) 
        { 
            data = io.readFile.read(currentFodler+"groups");
            processData(data,groups,":");
        }
        else
            System.out.println("groups file is not exist");
        ///////////////////ipc classification codes-subgroups///////////////
        data=null;
        f = new File(currentFodler+"subGroups");
        if(f.exists() && !f.isDirectory()) 
        { 
            data = io.readFile.read(currentFodler+"subGroups");
            processData(data,subGroups,":");
        }
        else
            System.out.println("subGroups file is not exist");
        ///////////////////ipc classification codes-subsections///////////////
        data=null;
        f = new File(currentFodler+"subSections");
        if(f.exists() && !f.isDirectory()) 
        { 
            data = io.readFile.read(currentFodler+"subSections");
            processData(data,subSections,":");
        }
        else
            System.out.println("subSections file is not exist");

        //////////////////////////////////// filling us subclasses

        for (String classKey : classesUSPC.keySet()) {

            data=null;
            //As files are saved with classes key formats where classes codes are led by 0's we modified the key to compensate that
            String leading0ClassKey=classKey; //required to read the the file assign it to the original key
            if(classKey.length() == 1)
                leading0ClassKey = "00"+classKey;
            else if(classKey.length() == 2)
                leading0ClassKey = "0"+classKey;
            // leading0ClassKey contains either the original value if length =3 otherwise the updated value if less
            f = new File(currentFodler+leading0ClassKey); 
            if(f.exists() && !f.isDirectory()) 
            { 
                data = io.readFile.read(currentFodler+leading0ClassKey);// read the file with the class name to get its subclasses
                ectractUSSubclassesCodes(classKey,data,subClassesUSPC);//to add to the map we need the unmodified version of the class key
            }
            else
                System.out.println("File "+classKey+" is not exist");
        }

    }

    public static void fillMap(List<String> data,String dataType, Map<String,String> map, String splitter)
    {
        data=null;
        String currentFodler="";
        if(IPCCodesFolder.length()==0)
        {
            currentFodler = System.getProperty("user.dir");
            currentFodler=checkPath(currentFodler);
        }
        else
            currentFodler=IPCCodesFolder;
        if(dataType.toLowerCase().equals("locarnoclasses"))
        {
            if(USCodesFolder.length()==0)
            {
                currentFodler = System.getProperty("user.dir");
                currentFodler=checkPath(currentFodler);
            }
            else
                currentFodler=USCodesFolder;
        }

        File f = new File(currentFodler+dataType);
        if(f.exists() && !f.isDirectory()) 
        { 
            data = io.readFile.read(currentFodler+dataType);
            processData(data,map,splitter);
        }
        else
            System.out.println("File "+dataType+" is not exist");
    }

    public static void processData(List<String> data , Map<String,String> mappedData,String splitter)
    {
        for (String line : data) {
            line=line.trim();
            if(line.length() > 0)// not empty line
                mappedData.put(line.split(splitter)[0], line.split(splitter)[1])   ;
        }
    }
    /**
     * This method process the patents' data xml files and add more tags for labels or information required in RML Mapping
     * @param file:  the xml file to be processed for a patent
     */
    public static void processXMLFile(String file)
    {
        try {

            File fXmlFile = new File(file);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setValidating(false);
            dbFactory.setNamespaceAware(true);
            dbFactory.setFeature("http://xml.org/sax/features/namespaces", false);
            dbFactory.setFeature("http://xml.org/sax/features/validation", false);
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);




            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("us-patent-grant");

            System.out.println("----------------------------");

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp); // get the us-grant node


                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;
                    //process the date tags formats
                    NodeList childNodes = eElement.getElementsByTagName("date");
                    for (int i = 0; i < childNodes.getLength(); i++) 
                    {

                        String content = childNodes.item(i).getTextContent().trim(); //get a date tag node
                        childNodes.item(i).setTextContent(content.substring(0, 4)+"-"+content.substring(4, 6)+"-"+content.substring(6, 8));
                        // add year tag as sibling
                        Node node=null;
                        node = doc.createElement("year");
                        node.appendChild(doc.createTextNode(content.substring(0, 4)));
                        childNodes.item(i).getParentNode().appendChild(node);
                    }
                    ///////////////////////////////////////////////////////////////////////////////////////////
                    //add the role label tags formats
                    childNodes = eElement.getElementsByTagName("role");
                    for (int i = 0; i < childNodes.getLength(); i++) 
                    {
                        String content = childNodes.item(i).getTextContent().trim();
                        Node node=null;
                        String roleLabel = roles.get(content);
                        if(roleLabel!=null)
                        {
                            node = doc.createElement("role-label");
                            node.appendChild(doc.createTextNode(roleLabel));
                            childNodes.item(i).getParentNode().appendChild(node);
                        } 
                        else if(content.equals("omitted") || content.equals("unknown"))
                        {
                            node = doc.createElement("role-label");
                            node.appendChild(doc.createTextNode(content));
                            childNodes.item(i).getParentNode().appendChild(node);
                        }
                        else
                        {
                            System.out.println(file+": false role numebr"+ content);
                            undefinedCodesMessages.add(file+":role:"+ content);

                            Set<String> undefinedCodes = allUndefinedCodesMessages.get("role");
                            if(undefinedCodes ==  null)
                                undefinedCodes= new HashSet<String>();
                            undefinedCodes.add(content);
                            allUndefinedCodesMessages.put("role",undefinedCodes);

                        }
                    }

                    //////////////////////////////////////////////////////////////////
                    childNodes = eElement.getElementsByTagName("country");

                    Map<Node,Node> omittedNodes= new HashMap<Node, Node>();
                    for (int i = 0; i < childNodes.getLength(); i++) 
                    {
                        String content = childNodes.item(i).getTextContent().trim();
                        Node node=null;

                        String countryLabel = countries.get(content);
                        if (countryLabel!=null)
                        {
                            node = doc.createElement("country-label");//create the tag
                            node.appendChild(doc.createTextNode(countryLabel));// add the tag value to it
                            //	childNodes.item(i).getParentNode().removeChild(node);//remove it if before exists
                            childNodes.item(i).getParentNode().appendChild(node);
                        }
                        else if(content.equals("omitted") || content.equals("unknown"))
                        {
                            node = doc.createElement("country-label");
                            node.appendChild(doc.createTextNode(content));
                            childNodes.item(i).getParentNode().appendChild(node);
                        }else 
                        {
                            System.out.println(file+": false country numebr: "+ content);
                            undefinedCodesMessages.add(file+":country:"+ content);

                            Set<String> undefinedCodes = allUndefinedCodesMessages.get("country");
                            if(undefinedCodes ==  null)
                                undefinedCodes= new HashSet<String>();
                            undefinedCodes.add(content);
                            allUndefinedCodesMessages.put("country",undefinedCodes);
                        }
                    }
                    ////////////////////////////////////////////////////////////////
                    //add the kind label tags formats
                    childNodes = eElement.getElementsByTagName("kind");
                    for (int i = 0; i < childNodes.getLength(); i++) 
                    {
                        String content = childNodes.item(i).getTextContent().trim();
                        Node node=null;
                        String kindLabel = kinds.get(content);
                        if(kindLabel==null)
                        {
                            kindLabel = EPkinds.get(content);
                            if(kindLabel==null)
                            {
                                kindLabel = WOkinds.get(content);
                                if(kindLabel==null)
                                {
                                    System.out.println(file+": false kind numebr: "+content);
                                    undefinedCodesMessages.add(file+":kind:"+ content);

                                    Set<String> undefinedCodes = allUndefinedCodesMessages.get("kind");
                                    if(undefinedCodes ==  null)
                                        undefinedCodes= new HashSet<String>();
                                    undefinedCodes.add(content);
                                    allUndefinedCodesMessages.put("kind",undefinedCodes);
                                }
                            }
                        }

                        if(kindLabel!=null)
                        {
                            node = doc.createElement("kind-label");
                            node.appendChild(doc.createTextNode(kindLabel));
                            childNodes.item(i).getParentNode().appendChild(node);
                        }
                        else if(content.equals("omitted") || content.equals("unknown"))
                        {
                            node = doc.createElement("kind-label");
                            node.appendChild(doc.createTextNode(content));
                            childNodes.item(i).getParentNode().appendChild(node);
                        }
                        else 
                        {
                            System.out.println(file+": false kind numebr: "+content);
                            undefinedCodesMessages.add(file+":kind:"+ content);

                            Set<String> undefinedCodes = allUndefinedCodesMessages.get("kind");
                            if(undefinedCodes ==  null)
                                undefinedCodes= new HashSet<String>();
                            undefinedCodes.add(content);
                            allUndefinedCodesMessages.put("kind",undefinedCodes);
                        }
                    }
                    ////////////////////////////////////////////////////////////////
                    //add the kind label tags formats
                    childNodes = eElement.getElementsByTagName("state");
                    for (int i = 0; i < childNodes.getLength(); i++) 
                    {
                        String content = childNodes.item(i).getTextContent().trim();
                        Node node=null;
                        String kindLabel = states.get(content);
                        if(kindLabel!=null)
                        {
                            node = doc.createElement("state-label");
                            node.appendChild(doc.createTextNode(kindLabel));
                            childNodes.item(i).getParentNode().appendChild(node);
                        }
                        else if(content.equals("omitted") || content.equals("unknown"))
                        {
                            node = doc.createElement("state-label");
                            node.appendChild(doc.createTextNode(content));
                            childNodes.item(i).getParentNode().appendChild(node);
                        }else
                        {
                            System.out.println(file+": false state numebr: "+content);
                            undefinedCodesMessages.add(file+":state:"+ content);

                            Set<String> undefinedCodes = allUndefinedCodesMessages.get("state");
                            if(undefinedCodes ==  null)
                                undefinedCodes= new HashSet<String>();
                            undefinedCodes.add(content);
                            allUndefinedCodesMessages.put("state",undefinedCodes);

                        }
                    }
                    ////////////////////////////////ipc classification////////////////////////////////
                    //add the kind label tags formats
                    childNodes = eElement.getElementsByTagName("classification-ipcr");
                    //        			System.out.println(childNodes.getLength());
                    for (int i = 0; i < childNodes.getLength(); i++) 
                    {
                        Node child = childNodes.item(i);
                        //       				System.out.println(child.getNodeName());
                        if(child.getNodeType() == Node.ELEMENT_NODE)
                        {
                            NodeList childrenNodes= child.getChildNodes();
                            String localSection="",localClass="",localsubsection="",localSubclass="",localGroup="",localSubgroup="";
                            // note the file names group provides the subclass values and the subgroup provides the group values  :) my mistake, but it works anyway
                            for(int j = 0; j < childrenNodes.getLength(); j++) 
                            {
                                if(child.getNodeType() == Node.ELEMENT_NODE)
                                {
                                    //                				System.out.println(childrenNodes.item(j).getNodeName());
                                    if(childrenNodes.item(j).getNodeName().equals("section"))
                                        localSection = childrenNodes.item(j).getTextContent().trim();
                                    else if(childrenNodes.item(j).getNodeName().equals("class"))
                                        localClass = childrenNodes.item(j).getTextContent().trim();
                                    else if(childrenNodes.item(j).getNodeName().equals("subclass"))
                                        localSubclass = childrenNodes.item(j).getTextContent().trim();
                                    else if(childrenNodes.item(j).getNodeName().equals("main-group"))
                                        localGroup = childrenNodes.item(j).getTextContent().trim();
                                    else if(childrenNodes.item(j).getNodeName().equals("subgroup"))
                                        localSubgroup = childrenNodes.item(j).getTextContent().trim();
                                }
                                if(localSection.length() > 0 && localClass.length() > 0 && localSubclass.length() > 0 && localGroup.length() > 0 && localSubgroup.length() > 0)
                                    break;

                            }
                            String sectionLabel = sections.get(localSection);
                            if(sectionLabel != null)
                            {
                                Node node=null;
                                node = doc.createElement("section-label");
                                sectionLabel = sectionLabel.replace("SECTION A  ", "");
                                sectionLabel = sectionLabel.replace("SECTION B  ", "");
                                sectionLabel = sectionLabel.replace("SECTION C  ", "");
                                sectionLabel = sectionLabel.replace("SECTION D  ", "");
                                sectionLabel = sectionLabel.replace("SECTION E  ", "");
                                sectionLabel = sectionLabel.replace("SECTION F  ", "");
                                sectionLabel = sectionLabel.replace("SECTION G  ", "");
                                sectionLabel = sectionLabel.replace("SECTION H  ", "");
                                node.appendChild(doc.createTextNode(sectionLabel));
                                child.appendChild(node);
                            }
                            else
                            {
                                System.out.println(file+": false ipc section numebr: "+localSection);
                                undefinedCodesMessages.add(file+":ipc-section:"+ localSection);

                                Set<String> undefinedCodes = allUndefinedCodesMessages.get("ipcSection");
                                if(undefinedCodes ==  null)
                                    undefinedCodes= new HashSet<String>();
                                undefinedCodes.add(localSection);
                                allUndefinedCodesMessages.put("ipcSection",undefinedCodes);

                            }
                            ////////////////////////////////////////////////////////////
                            localClass =localSection+localClass;
                            String classLabel = classesIPCR.get(localClass);
                            if(classLabel != null)
                            {
                                Node node=null;
                                node = doc.createElement("class-label");
                                node.appendChild(doc.createTextNode(classLabel));
                                child.appendChild(node);
                            }
                            else
                            {
                                System.out.println(file+": false ipc class numebr: "+localClass);
                                undefinedCodesMessages.add(file+":ipc-class:"+ localClass);

                                Set<String> undefinedCodes = allUndefinedCodesMessages.get("ipcClass");
                                if(undefinedCodes ==  null)
                                    undefinedCodes= new HashSet<String>();
                                undefinedCodes.add(localClass);
                                allUndefinedCodesMessages.put("ipcClass",undefinedCodes);

                            }
                            ///////////////////////////////////////////////////////
                            localSubclass =localClass+localSubclass;
                            String SubClassLabel = groups.get(localSubclass);
                            if(SubClassLabel != null)
                            {
                                Node node=null;
                                node = doc.createElement("subclass-label");
                                node.appendChild(doc.createTextNode(SubClassLabel));
                                child.appendChild(node);
                            }
                            else
                            {
                                System.out.println(file+": false ipc subclass numebr: "+localSubclass);
                                undefinedCodesMessages.add(file+":ipc-subclass:"+ localSubclass);

                                Set<String> undefinedCodes = allUndefinedCodesMessages.get("ipcSubclass");
                                if(undefinedCodes ==  null)
                                    undefinedCodes= new HashSet<String>();
                                undefinedCodes.add(localSubclass);
                                allUndefinedCodesMessages.put("ipcSubclass",undefinedCodes);

                            }

                            ///////////////////////////////////////////////////////
                            if(localGroup.length()==1)
                                localGroup =localSubclass+"000"+localGroup+"000000";
                            else if(localGroup.length()==2)
                                localGroup =localSubclass+"00"+localGroup+"000000";

                            String localGroupLabel = subGroups.get(localGroup);
                            if(localGroupLabel != null)
                            {
                                Node node=null;
                                node = doc.createElement("main-group-label");
                                node.appendChild(doc.createTextNode(localGroupLabel));
                                child.appendChild(node);
                            }
                            else
                            {
                                System.out.println(file+": false ipc main group numebr: "+localGroup);
                                undefinedCodesMessages.add(file+":ipc-main-group:"+ localGroup);

                                Set<String> undefinedCodes = allUndefinedCodesMessages.get("ipcMainGroup");
                                if(undefinedCodes ==  null)
                                    undefinedCodes= new HashSet<String>();
                                undefinedCodes.add(localGroup);
                                allUndefinedCodesMessages.put("ipcMainGroup",undefinedCodes);

                            }

                        }

                    }
                    ////////////////////////////////us classification////////////////////////////////

                    childNodes = eElement.getElementsByTagName("classification-national");
                    //System.out.println(childNodes.getLength());
                    for (int i = 0; i < childNodes.getLength(); i++) 
                    {
                        Node child = childNodes.item(i);
                        if(child.getParentNode().getNodeName().equals("us-bibliographic-data-grant"))
                        {
                            //	        				System.out.println(child.getNodeName());
                            if(child.getNodeType() == Node.ELEMENT_NODE)
                            {
                                NodeList childrenNodes= child.getChildNodes();//country,main-classification,further classification
                                for(int j = 0; j < childrenNodes.getLength(); j++) 
                                {
                                    //	        						System.out.println(childrenNodes.item(j).getNodeName());
                                    if(childrenNodes.item(j).getNodeType() == Node.ELEMENT_NODE && childrenNodes.item(j).getNodeName().equals("main-classification"))
                                    {
                                        //changes are done here
                                        String key = childrenNodes.item(j).getTextContent().trim().replaceAll("\\s+", "");
                                        //The case the class code is 3 digits
                                        String classCode = key.substring(0, 3); // the first three letters are the class code
                                        String classLabel= classesUSPC.get(classCode);
                                        //for its subclass
                                        String subclassCode = key.substring(3,key.length());
                                        Map <String,String> subClassInfo =  subClassesUSPC.get(classCode);//get list of its subclasses
                                        String subClassLabel =null;
                                        try{
                                            subClassLabel = subClassInfo.get(subclassCode); //get the corresponding sublclass label
                                        }
                                        catch(java.lang.NullPointerException e){}

                                        if(subClassLabel != null)// subclass without . is found
                                        {
                                            Node node=null;
                                            node = doc.createElement("main-classification-subclass-label");
                                            node.appendChild(doc.createTextNode(subClassLabel));
                                            child.appendChild(node);

                                            node=null;
                                            node = doc.createElement("main-classification-class-label");
                                            node.appendChild(doc.createTextNode(classLabel));
                                            child.appendChild(node);
                                        }
                                        else //check with 2 digit class key
                                        {
                                            //check if the class code is only two digits
                                            classCode = key.substring(0, 2); // the first three letters are the class code
                                            classLabel= classesUSPC.get(classCode);
                                            subclassCode = key.substring(2,key.length());
                                            subClassInfo =  subClassesUSPC.get(classCode);
                                            subClassLabel =null;
                                            try{
                                                subClassLabel = subClassInfo.get(subclassCode);
                                            }
                                            catch(java.lang.NullPointerException e){}

                                            if(subClassLabel != null)//subclass is found
                                            {
                                                Node node=null;
                                                node = doc.createElement("main-classification-subclass-label");
                                                node.appendChild(doc.createTextNode(subClassLabel));
                                                child.appendChild(node);

                                                node=null;
                                                node = doc.createElement("main-classification-class-label");
                                                node.appendChild(doc.createTextNode(classLabel));
                                                child.appendChild(node);
                                            }
                                            else
                                            {
                                                //check if the class code is only one digits
                                                classCode = key.substring(0, 1); // the first three letters are the class code
                                                classLabel= classesUSPC.get(classCode);
                                                subclassCode = key.substring(1,key.length());
                                                subClassInfo =  subClassesUSPC.get(classCode);
                                                subClassLabel =null;
                                                try{
                                                    subClassLabel = subClassInfo.get(subclassCode);
                                                }
                                                catch(java.lang.NullPointerException e){}
                                                if(subClassLabel != null)//subclass is found
                                                {
                                                    Node node=null;
                                                    node = doc.createElement("main-classification-subclass-label");
                                                    node.appendChild(doc.createTextNode(subClassLabel));
                                                    child.appendChild(node);

                                                    node=null;
                                                    node = doc.createElement("main-classification-class-label");
                                                    node.appendChild(doc.createTextNode(classLabel));
                                                    child.appendChild(node);
                                                }
                                                else
                                                {
                                                    System.out.println(file+ " false us main classification "+ key);
                                                    undefinedCodesMessages.add(file+":us-classification:"+ key);

                                                    Set<String> undefinedCodes = allUndefinedCodesMessages.get("usClassification");
                                                    if(undefinedCodes ==  null)
                                                        undefinedCodes= new HashSet<String>();
                                                    undefinedCodes.add(key);
                                                    allUndefinedCodesMessages.put("usClassification",undefinedCodes);

                                                }
                                            }

                                        }
                                    }//end of main-classification
                                    /////////////////////////////////////////////////////////////////////////
                                    else if(childrenNodes.item(j).getNodeType() == Node.ELEMENT_NODE && childrenNodes.item(j).getNodeName().equals("further-classification"))
                                    {
                                        //changes are done here
                                        String key = childrenNodes.item(j).getTextContent().trim().replaceAll("\\s+", "");

                                        String classCode = key.substring(0, 3); // the first three letters are the class code
                                        String subclassCode = key.substring(3,key.length());
                                        String classLabel= classesUSPC.get(classCode);
                                        Map <String,String> subClassInfo =  subClassesUSPC.get(classCode);
                                        String classificationLabel = null;
                                        try{
                                            classificationLabel = subClassInfo.get(subclassCode);
                                        }
                                        catch(java.lang.NullPointerException e){}
                                        ///end of changes

                                        if(classificationLabel != null)
                                        {
                                            Node node=null;
                                            node = doc.createElement("further-classification-subclass-label");
                                            node.appendChild(doc.createTextNode(classificationLabel));
                                            child.appendChild(node);

                                            node=null;
                                            node = doc.createElement("further-classification-class-label");
                                            node.appendChild(doc.createTextNode(classLabel));
                                            child.appendChild(node);
                                        }
                                        else
                                        {
                                            //check if the class code is only two digits
                                            classCode = key.substring(0, 2); // the first three letters are the class code
                                            classLabel= classesUSPC.get(classCode);
                                            subclassCode = key.substring(2,key.length());
                                            subClassInfo =  subClassesUSPC.get(classCode);
                                            classificationLabel =null;
                                            try{
                                                classificationLabel = subClassInfo.get(subclassCode);
                                            }
                                            catch(java.lang.NullPointerException e){}

                                            if(classificationLabel != null)
                                            {
                                                Node node=null;
                                                node = doc.createElement("further-classification-subclass-label");
                                                node.appendChild(doc.createTextNode(classificationLabel));
                                                child.appendChild(node);

                                                node=null;
                                                node = doc.createElement("further-classification-class-label");
                                                node.appendChild(doc.createTextNode(classLabel));
                                                child.appendChild(node);
                                            }
                                            else
                                            {
                                                //check if the class code is only one digits
                                                classCode = key.substring(0, 1); // the first three letters are the class code
                                                classLabel= classesUSPC.get(classCode);
                                                subclassCode = key.substring(1,key.length());
                                                subClassInfo =  subClassesUSPC.get(classCode);
                                                classificationLabel =null;
                                                try{
                                                    classificationLabel = subClassInfo.get(subclassCode);
                                                }
                                                catch(java.lang.NullPointerException e){}
                                                if(classificationLabel != null)
                                                {
                                                    Node node=null;
                                                    node = doc.createElement("further-classification-subclass-label");
                                                    node.appendChild(doc.createTextNode(classificationLabel));
                                                    child.appendChild(node);

                                                    node=null;
                                                    node = doc.createElement("further-classification-class-label");
                                                    node.appendChild(doc.createTextNode(classLabel));
                                                    child.appendChild(node);
                                                }
                                                else
                                                {
                                                    System.out.println(file+ " false us further classification "+ key);
                                                    undefinedCodesMessages.add(file+":us-classification:"+ key);

                                                    Set<String> undefinedCodes = allUndefinedCodesMessages.get("usClassification");
                                                    if(undefinedCodes ==  null)
                                                        undefinedCodes= new HashSet<String>();
                                                    undefinedCodes.add(key);
                                                    allUndefinedCodesMessages.put("usClassification",undefinedCodes);

                                                }
                                            }

                                        }

                                    }

                                }
                            }
                            break; // no more checks
                        }

                    }
                    //////////////////////////////////////////////////// end

                }
            }
            //write it down back
            try 
            {  
                String fileName  = file.substring(file.lastIndexOf("/")+1, file.length());
                fileName="mod"+fileName;
                Source source = new DOMSource(doc); 
                File xmlFile = new File(destFolderPath+fileName);            
                StreamResult result = new StreamResult(new OutputStreamWriter(new FileOutputStream(xmlFile), "UTF-8"/*"ISO-8859-1"*/));
                Transformer xformer = TransformerFactory.newInstance().newTransformer();                        
                xformer.transform(source, result);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean getXMLFileStructure(String file, String expression)
    {
        try {
            File fXmlFile = new File(file);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setValidating(false);
            dbFactory.setNamespaceAware(true);
            dbFactory.setFeature("http://xml.org/sax/features/namespaces", false);
            dbFactory.setFeature("http://xml.org/sax/features/validation", false);
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            XPath xPath =  XPathFactory.newInstance().newXPath();

            String email = xPath.compile(expression).evaluate(doc);
            Node node = (Node) xPath.compile(expression).evaluate(doc, XPathConstants.NODE);
            if(node == null)
                return false;
            else
                return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * get the tags exist in certain node given an xpath expression
     * @param expression
     * @param file
     * @return
     */
    public static List<String> getTags(String expression, String file)
    {
        List<String> values = new ArrayList<String>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        String value = "";
        try {

            builder = factory.newDocumentBuilder();
            Document doc2 = builder.parse(new FileInputStream(file));
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile(expression);
            value = expr.evaluate(doc2);
            values.add(value);
            NodeList nodeList = (NodeList) xpath.compile(expression).evaluate(doc2,XPathConstants.NODESET);
            for (int temp = 0; temp < nodeList.getLength(); temp++) 
            {
                Node nNode = nodeList.item(temp); // get the ipcEntry node
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    //     			System.out.println(nNode.getTextContent());
                    NodeList children = nNode.getChildNodes();
                }

            }

        } catch (ParserConfigurationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return values;
    }
    public static Map<String, String> sections= new HashMap<String, String>();
    /**
     *  extracts sections ipc codes using xpath 
     * @param file
     */
    public static void extractSectionsCodes(String file)
    {
        String sectionExpression ="/ipcEntries/ipcEntry/textBody/title/titlePart/text";
        String sectionSymExpression = "/ipcEntries/ipcEntry/@symbol";
        String Sectiontitle = (getTags(sectionExpression,file)).get(0);
        String sectionSymbol = (getTags(sectionSymExpression,file)).get(0);
        sections.put(sectionSymbol, Sectiontitle);


    }
    public static Map<String,List<String>> classes= new HashMap<String,List<String>>();
    /**
     * extracts classes ipc codes using xpath 
     * @param file
     */
    public static void extractClassesCodes(String file)
    {
        String classExpression ="/ipcEntries/ipcEntry/ipcEntry/textBody/title/titlePart";
        String classSymExpression = "/ipcEntries/ipcEntry/ipcEntry[@kind='c']/@symbol";
        String classtitle = (getTags(classExpression,file)).get(0);
        String classSymbol = (getTags(classSymExpression,file)).get(0);
        if(classes.containsKey(classSymbol))//not exist before
        {
            List<String> titles=classes.get(classSymbol);
            if(!titles.contains(classtitle))
            {
                titles.add(classtitle);
                classes.put(classSymbol, titles);
            }
        }
        else
        {
            List<String> titles= new ArrayList<String>();
            titles.add(classtitle);
            classes.put(classSymbol,titles );
        }



    }
    public static Map<String, String> groupsCodeUsingXPath= new HashMap<String, String>();

    /**
     * Extracts the group codes using xpath
     * @param file
     */
    public static void extractGroupsCodes(String file)
    {
        String sectionExpression ="/ipcEntries/ipcEntry/ipcEntry/ipcEntry/ipcEntry[@kind='m']/textBody/title/titlePart/text";
        String sectionSymExpression = "/ipcEntries/ipcEntry/ipcEntry/ipcEntry/ipcEntry[@kind='m']/@symbol";
        List<String> ex = getTags(sectionExpression,file);
        for (String string : ex) {
            System.out.println(string);
        }
    }
    /**
     * This method to extract the ipc code from xml files using xpath rather than DOM
     * @param folder: that contains the fie
     */
    public static void extractIPCCodes(String folder)
    {
        List<String> files =getFilesWithExtension(folder, "xml",true);
        for (String file : files) {
            if(file.substring(file.lastIndexOf("/"), file.lastIndexOf(".")).length()==4)
                extractSectionsCodes(file);
        }
        for (String key : sections.keySet()) {
            System.out.println(key+":"+sections.get(key));
        }
        for (String file : files) {
            if(file.substring(file.lastIndexOf("/"), file.lastIndexOf(".")).length()==4)
                extractGroupsCodes(file);//extractClassesCodes(file);
        }
        for (String key : classes.keySet()) {
            System.out.println(key+":"+classes.get(key));
        }
    }
    /**
     * Display all lists of codes
     */
    public static void displayExtractedCodes()
    {
        for (String key : sections.keySet()) {
            System.out.println(key+":"+sections.get(key));
        }
        for (String key : subSections.keySet()) {
            System.out.println(key+":"+subSections.get(key));
        }

    }
    /**
     * @param n: the node represents the <BodyText> of an ipcEntry. It is recursive as it contains <text> tag but not indirectly and has other tags contain <text> by going deep into them
     * @return The text contents that represent the label of an extracted code
     */
    public static String getText(Node n)
    {
        String res="";
        if(n.getNodeName().equals("text"))
            return n.getTextContent();

        NodeList list = n.getChildNodes();
        for(int i=0 ;i < list.getLength();i++)
            if(list.item(i).getNodeType() == Node.ELEMENT_NODE)
                return getText(list.item(i));
        return "WRONG";
    }
    /**
     * @param n :the node represents the <BodyText> of an ipcEntry.It is recursive as <text> is not contained directly. It is accumulative
     *  as their may be more than one text tag enclosed with each others
     * @return The text contents that represent the label of an extracted code
     */
    public static String getTextAcc(Node n)
    {
        String res="";
        if(n.getNodeName().equals("text"))
            return n.getTextContent();

        NodeList list = n.getChildNodes();
        for(int i=0 ;i < list.getLength();i++)//more than one text tag, loop and recursin will get them
            if(list.item(i).getNodeType() == Node.ELEMENT_NODE)
            {
                String t= getTextAcc(list.item(i));// get the text
                res+= t+";";
                if(res.endsWith(";;"))//empty t did not work so this to avoid adding empty text
                    res=res.substring(0, res.length()-1);

            }
        return res;
    }

    /**
     * This method extracts the code of ipc from xml files using DOM rather than xpath
     * @param files: the list of xml files that contain the codes of the ipc coding
     */
    public static void extractUsingDOM(List<String> files)
    {

        for (String file : files) {
            try {

                File fXmlFile = new File(file);
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                dbFactory.setValidating(false);
                dbFactory.setNamespaceAware(true);
                dbFactory.setFeature("http://xml.org/sax/features/namespaces", false);
                dbFactory.setFeature("http://xml.org/sax/features/validation", false);
                dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
                dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(fXmlFile);
                //optional, but recommended
                //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
                doc.getDocumentElement().normalize();

                NodeList nList = doc.getElementsByTagName("ipcEntry"); //get all ipcEntry in the files


                for (int temp = 0; temp < nList.getLength(); temp++) {//for each node

                    Node nNode = nList.item(temp); // get the ipcEntry node
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        NamedNodeMap attr = nNode.getAttributes();//get the node attributes
                        if(attr.getNamedItem("kind").getNodeValue().equals("s"))//section
                        {
                            String code =  attr.getNamedItem("symbol").getNodeValue();//get the section code
                            if(!sections.containsKey(code))// if it is not in the map
                            {
                                NodeList l = nNode.getChildNodes();//get its children
                                if(l!= null)// it it has
                                {
                                    String section ="";
                                    for(int j=0; j< l.getLength();j++) //iterate over them
                                    {
                                        if(l.item(j).getNodeType() == Node.ELEMENT_NODE && l.item(j).getNodeName().equals("textBody"))//we want the text =>title of section
                                        {
                                            section = getText(l.item(j));// get the title and accumulate in case of more than one textbody
                                            sections.put(code, section);//add to sections map
                                            break;//finished
                                        }

                                    }


                                }
                            }

                        }
                        else if(attr.getNamedItem("kind").getNodeValue().equals("t"))//section
                        {
                            String code =  attr.getNamedItem("symbol").getNodeValue();//get the section code
                            if(!subSections.containsKey(code))// if it is not in the map
                            {
                                NodeList l = nNode.getChildNodes();//get its children
                                if(l!= null)// it it has
                                {
                                    for(int j=0; j< l.getLength();j++) //iterate over them
                                        if(l.item(j).getNodeType() == Node.ELEMENT_NODE && l.item(j).getNodeName().equals("textBody"))//we want the text =>title of section
                                        {
                                            String section = getText(l.item(j));// get the title
                                            subSections.put(code, section);//add to sections map
                                            break;//finished
                                        }

                                }
                            }

                            //sections.put(attr.getNamedItem("sumbol").getNodeValue(), text.getNodeValue());
                        }
                        else if(attr.getNamedItem("kind").getNodeValue().equals("c"))//section
                        {
                            String code =  attr.getNamedItem("symbol").getNodeValue();//get the section code
                            if(!classesIPCR.containsKey(code))// if it is not in the map
                            {
                                NodeList l = nNode.getChildNodes();//get its children
                                if(l!= null)// it it has
                                {
                                    for(int j=0; j< l.getLength();j++) //iterate over them
                                        if(l.item(j).getNodeType() == Node.ELEMENT_NODE && l.item(j).getNodeName().equals("textBody"))//we want the text =>title of section
                                        {
                                            String section = getTextAcc(l.item(j));// get the title
                                            classesIPCR.put(code, section.substring(0, section.lastIndexOf(";")));//add to sections map
                                            break;//finished
                                        }

                                }
                            }
                        }
                        else if(attr.getNamedItem("kind").getNodeValue().equals("u"))//section
                        {
                            String code =  attr.getNamedItem("symbol").getNodeValue();//get the section code
                            if(!groups.containsKey(code))// if it is not in the map
                            {
                                NodeList l = nNode.getChildNodes();//get its children
                                if(l!= null)// it it has
                                {
                                    for(int j=0; j< l.getLength();j++) //iterate over them
                                        if(l.item(j).getNodeType() == Node.ELEMENT_NODE && l.item(j).getNodeName().equals("textBody"))//we want the text =>title of section
                                        {
                                            String section = getText(l.item(j));// get the title
                                            groups.put(code, section);//add to sections map
                                            break;//finished
                                        }
                                }
                            }
                        }
                        else if(attr.getNamedItem("kind").getNodeValue().equals("m"))//section
                        {
                            String code =  attr.getNamedItem("symbol").getNodeValue();//get the section code
                            if(!subGroups.containsKey(code))// if it is not in the map
                            {
                                NodeList l = nNode.getChildNodes();//get its children
                                if(l!= null)// it it has
                                {
                                    for(int j=0; j< l.getLength();j++) //iterate over them
                                        if(l.item(j).getNodeType() == Node.ELEMENT_NODE && l.item(j).getNodeName().equals("textBody"))//we want the text =>title of section
                                        {
                                            String theGroup = getText(l.item(j));// get the title
                                            subGroups.put(code, theGroup);//add to sections map
                                            break;//finished
                                        }
                                }
                            }
                        }
                    }
                }
                writeIPCCodesoFile();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private static void writeIPCCodesoFile()
    {
        if(sections.size() > 0)
            writeFile.write(sections, classificationresultsFolder+"sections");
        if(subSections.size() > 0)
            writeFile.write(subSections, classificationresultsFolder+"subSections");
        if(classesIPCR.size() > 0)
            writeFile.write(classesIPCR, classificationresultsFolder+"classes");
        /*		if(subSections.size() > 0)
			writeFile.write(subSections, classificationresultsFolder+"sections");*/
        if(groups.size() > 0)
            writeFile.write(groups, classificationresultsFolder+"groups");
        if(subGroups.size() > 0)
            writeFile.write(subGroups, classificationresultsFolder+"subGroups");

    }
    public static void getHyperLinksFromPage()
    {
        File input = new File("http://www.wipo.int/ipc/itos4ipc/ITSupport_and_download_area/IPC3/subclass/core/en/xml/");
        org.jsoup.nodes.Document doc;
        try {
            doc = Jsoup.parse(input, "UTF-8", "http://example.com/");
            Elements  links = doc.select("a[href]"); // a with href
            String linkhref=links.attr("href");
            System.out.println(linkhref);


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    /**
     * @param path: the given path to be checked
     * @return the same path in canonical form by adding / at the end of the path if not exist
     */
    public static String checkPath(String path)
    {
        if(!path.endsWith("/"))
            path=path+"/";
        return path;
    }
    private static void patentProcessing()
    {
        loadSetup();
        List<String> files = getFilesWithExtension(sourceFolderPath, "xml",true);
        for (String file : files) {

            processXMLFile(file);
            /*if(undefinedCodesMessages.size()> 0)// file has already something missed
			{
				writeFile.write(undefinedCodesMessages, missedCodesFolder+"/missedCodes"+file.substring(file.lastIndexOf("/")+1, file.lastIndexOf(".")));
				undefinedCodesMessages.clear();
			}*/


        }  
    }
    private static void ectractIPCCodes()
    {
        //	extractIPCCodes(home);
        List<String> files = getFilesWithExtension(classificationCodeFolder, "xml", true);
        extractUsingDOM(files);
        displayExtractedCodes();
    }

    /**
     * @param usClassName the class number
     * @param list the subclass data extracted from its file in the method caller (note that the file are subclass code and its final label, the method ectractUSCodes()
     * already handeled the dot (.) levels
     * @param map the map where the subclasses should be filled
     * This method is responsible for extracting us subclasses by giving it class key and get its subclasses from a file with the same name
     */
    private static void ectractUSSubclassesCodes(String usClassName,List<String> list,Map<String,Map<String,String>> map)
    {
        String subClassLabel="";
        String subClassCode="";
        int i=0;
        for(String line : list)//iterate over the subclasses lines
        {
            //split each line extracting the code and the label
            subClassLabel = line.split("\t")[1].trim();
            subClassCode = line.split("\t")[0].trim();
            if(subClassCode.equals("101"))
                i=9;
            /*            if(subClassCode.equals("2.11"))
                i=8;
            if(subClassCode.equals("211"))
                i=8;*/
            Map<String,String> subC = map.get(usClassName); // get the subclass maps for this class
            if(subC==null)// create new subclass entry for this class
                subC = new HashMap<String, String>();

            String updatedSubClassCode= "";
            if(subClassCode.contains(".") && subClassCode.length() >= 6) //e.g. 1122.3 ; the max value of subclass without dots is 999.9 so 6 and more is safe
            {
                updatedSubClassCode = subClassCode.replace(".", "");
                String tmp = subC.get(updatedSubClassCode);
                if(tmp == null) //not exist before // does it exist before the set of sublcasses of this class (duplication from previous value) 
                    subC.put(updatedSubClassCode,subClassLabel);
                else
                    System.out.println("Error the key resulting from . removal will be duplicated");
            }
            else //does not contain .
            {
                String tmp = subC.get(subClassCode);
                if(tmp == null) //no previous existence of this key
                    subC.put(subClassCode,subClassLabel);
                else
                    System.out.println("Error the key will be duplicated");
            }

            map.put(usClassName, subC);
        }



    }
    private static void extractClassificationCodes()
    {
        if(classificationCodesType.equals("ipc"))
            ectractIPCCodes();
        else if(classificationCodesType.equals("us"))
            ectractUSCodes(); // are manually extracted in files (they are not in xml files)
        else
            System.out.println("Invalid classificaion type");
    }
    private static void ectractUSCodes()
    {
        Stack subTite = new Stack();
        Stack dot1 = new Stack();
        Stack dot2 = new Stack();
        Stack dot3 = new Stack();
        Stack dot4 = new Stack();
        Stack dot5 = new Stack();
        Stack dot6 = new Stack();
        Stack dot7 = new Stack();
        Stack dot8 = new Stack();
        Stack dot9 = new Stack();



        String subTitlePattern = "class=\"SubTtl\"><b><big></big>\\s(.*)\\s</b>";
        String subTitleCodePattern = "\\[Link to Class Definition for class ([^\\]]*) subclass ([^\\]]*)\\]\">\\s*(.*)\\s*</a>";
        String doRankPattern = "<img src=\"../images/(.*).gif\" alt.*";
        String codeLabelPattern = "\\[List of Patents for class ([^\\]]*) subclass ([^\\]]*)\\]\">\\s+(.*)\\s</a>";


        Pattern r1 = Pattern.compile(doRankPattern);
        Pattern r2 = Pattern.compile(codeLabelPattern);
        Pattern r3 = Pattern.compile(subTitlePattern);
        Pattern r4 = Pattern.compile(subTitleCodePattern);



        List<String> mainContent = getURLContents("http://www.uspto.gov/web/patents/classification/selectnumwithtitle.htm");
        for (String line : mainContent) {
            if(line.startsWith("function class"))
            {
                List<String> usClassesCodes= new ArrayList<String>();

                String[] numbers = line.split("\"");
                String classNr = numbers[1];
                String pageURL = "http://www.uspto.gov/web/patents/classification/uspc"+numbers[1]+"/sched"+numbers[1]+".htm" ;
                List<String> pagContent = getURLContents(pageURL);
                for (String ln : pagContent)
                {
                    Matcher m1 = r1.matcher(ln);
                    Matcher m2 = r2.matcher(ln);
                    Matcher m3 = r3.matcher(ln);
                    Matcher m4 = r4.matcher(ln);


                    if(m3.find() &&  m4.find())
                    {
                        String subTitel= m3.group(1);
                        String subClassCode =m4.group(2);
                        subTite.push(subTitel);
                        usClassesCodes.add(subClassCode+"\t"+ subTite.peek().toString());

                    }
                    if(m1.find() &&  m2.find())
                    {
                        String dot = m1.group(1);
                        String classCode = m2.group(1);
                        String subClassCode =m2.group(2);
                        String initLabel =m2.group(3);
                        if(dot.startsWith("1"))
                        {
                            String subt = subTite.peek().toString();
                            dot1.push(subt+" "+  initLabel);
                            usClassesCodes.add(subClassCode+"\t"+ dot1.peek().toString());
                        }
                        else if(dot.startsWith("2"))
                        {
                            String topLabel = dot1.peek().toString();
                            dot2.push(topLabel+" "+  initLabel);
                            usClassesCodes.add(subClassCode+"\t"+dot2.peek().toString());

                        }
                        else if(dot.startsWith("3"))
                        {
                            String topLabel = dot2.peek().toString();
                            dot3.push(topLabel+" "+ initLabel);
                            usClassesCodes.add(subClassCode+"\t"+dot3.peek().toString());

                        }
                        else if(dot.startsWith("4"))
                        {
                            String topLabel = dot3.peek().toString();
                            dot4.push(topLabel+" "+ initLabel);
                            usClassesCodes.add(subClassCode+"\t"+dot4.peek().toString());

                        }
                        else if(dot.startsWith("5"))
                        {
                            String topLabel = dot4.peek().toString();
                            dot5.push(topLabel+" "+ initLabel);
                            usClassesCodes.add(subClassCode+"\t"+dot5.peek().toString());

                        }
                        else if(dot.startsWith("6"))
                        {
                            String topLabel = dot5.peek().toString();
                            dot6.push(topLabel+" "+ initLabel);
                            usClassesCodes.add(subClassCode+"\t"+dot6.peek().toString());

                        }
                        else if(dot.startsWith("7"))
                        {
                            String topLabel = dot6.peek().toString();
                            dot7.push(topLabel+" "+ initLabel);
                            usClassesCodes.add(subClassCode+"\t"+dot7.peek().toString());

                        }
                        else if(dot.startsWith("8"))
                        {
                            String topLabel = dot7.peek().toString();
                            dot8.push(topLabel+" "+ initLabel);
                            usClassesCodes.add(subClassCode+"\t"+dot8.peek().toString());

                        }
                        else if(dot.startsWith("9"))
                        {
                            String topLabel = dot8.peek().toString();
                            dot9.push(topLabel+" "+ initLabel);
                            usClassesCodes.add(subClassCode+"\t"+dot9.peek().toString());

                        }

                        for(int i =0; i< usClassesCodes.size();i++)
                        {
                            System.out.println(classNr+"\t"+usClassesCodes.get(i));
                        }

                        //						System.out.println(dot+":"+classCode+":"+subClassCode+":"+initLabel );
                    }
                }
                io.writeFile.write(usClassesCodes, classificationresultsFolder+classNr);
            }

        }

    }
    private static List<String> getURLContents(String url)
    {
        List<String> data =new ArrayList<String>();
        try
        {
            URL oracle = new URL(url);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(oracle.openStream()));

            String inputLine;

            while ((inputLine = in.readLine()) != null)
                data.add(inputLine);
            in.close();
        }
        catch (Exception e){}
        return data;
    }

    public static String sourceFolderPath="",destFolderPath="";
    public static String IPCCodesFolder="";
    public static String USCodesFolder="";


    private static String option="";//ext:extract,pro:processing
    private static String classificationCodesType="";//ipc,us
    private static String classificationCodeFolder="";
    private static String classificationresultsFolder="";
    private static String missedCodesFolder ="";

    private static List<String> undefinedCodesMessages= new ArrayList<String>();
    private static Map<String,Set<String>> allUndefinedCodesMessages= new HashMap<String, Set<String>>();//type of code, list of missed ones
    public static void main( String[] args )
    {
        System.out.println("Options: ext,pro,classify");
        option = args[0];
        if(option.equals("ext"))	// extract classification codes from files
        {
            /*    		ext
    		ipc/us
    		/media/mofeed/A0621C46621C24164/03_Work/AmrapaliWorks/patents2015/IPC_Codes_XML/allExtracted
    		/media/mofeed/A0621C46621C24164/03_Work/AmrapaliWorks/patents2015/IPC_Codes_XML/IPCCodes
             */
            if(args.length < 3)
            {
                System.out.println("Error in classification code extraction number of parameters (option,codes type,folder)");
                return;
            }
            classificationCodesType=args[1];
            classificationCodeFolder=args[2];
            classificationresultsFolder=args[3];
            missedCodesFolder=args[4];

            classificationCodeFolder=checkPath(classificationCodeFolder);
            classificationresultsFolder= checkPath(classificationresultsFolder);
            missedCodesFolder=checkPath(missedCodesFolder);

            extractClassificationCodes();
        }
        else if(option.equals("pro")) //processing the xml patents file
        {
            if(args.length < 3)
            {
                System.out.println("Error in patent processing number of parameters (option,source folder,destination folder,codes folder (optional))");
                return;
            }
            /*    		pro
    		/media/mofeed/TOSHIBA2T/Patents2015/2005C/test/
    		/media/mofeed/TOSHIBA2T/Patents2015/2005C/processed/
    		/media/mofeed/TOSHIBA2T/Patents2015/IPC_Codes_XML/IPCCodes/
    		/media/mofeed/TOSHIBA2T/Patents2015/US/
    		/media/mofeed/TOSHIBA2T/Patents2015/2005C/
             */
            sourceFolderPath = args[1];
            destFolderPath=args[2];
            IPCCodesFolder =args[3];
            USCodesFolder =args[4];
            missedCodesFolder=args[5];

            sourceFolderPath=checkPath(sourceFolderPath);
            destFolderPath=checkPath(destFolderPath);
            IPCCodesFolder=checkPath(IPCCodesFolder);
            USCodesFolder=checkPath(USCodesFolder);
            missedCodesFolder=checkPath(missedCodesFolder);


            patentProcessing();///media/mofeed/A0621C46621C24164/03_Work/AmrapaliWorks/patents2015/testsplitprocessing/
            //   		writeFile.write(allUndefinedCodesMessages, missedCodesFolder+"/missedCodes",":");
            writeFile.write(undefinedCodesMessages, missedCodesFolder+"/missedCodes");



        }
        else if(option.equals("classify")) // to classify the missed codes
        {
            /*
        classify
        /media/mofeed/TOSHIBA2T/patents2016/2005Samples/ (source)
        /media/mofeed/TOSHIBA2T/patents2016/2005SampleB2/ (target)
        /us-patent-grant/us-bibliographic-data-grant/parties/agents/agent/addressbook/orgname (expression to move based on)
             */    	    sourceFolderPath = args[1];
             sourceFolderPath=checkPath(sourceFolderPath);

             destFolderPath = args[2];
             destFolderPath=checkPath(destFolderPath);

             String expression =args[3];
             List<String> files = getFilesWithExtension(sourceFolderPath, "xml",true);
             for (String file : files) {
                 System.out.println(file);
                 if(getXMLFileStructure(file,expression))
                 {
                     File afile =new File(file);
                     String newFile = destFolderPath + afile.getName();

                     try {
                         Files.move(Paths.get(file),Paths.get(newFile) , REPLACE_EXISTING);
                     } catch (IOException e) {
                         // TODO Auto-generated catch block
                         e.printStackTrace();
                     }
                 }
                 System.out.println("------------------------------------------------");
             } 

        }
        else System.out.println("Wrong Option");



    }
}
