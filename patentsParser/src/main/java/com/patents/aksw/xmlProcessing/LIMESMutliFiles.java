package com.patents.aksw.xmlProcessing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.uni_leipzig.simba.controller.PPJoinController;
import io.readFile;
import io.writeFile;

public class LIMESMutliFiles {
    static List<String> files =null;
    static String ntFolderPath="";
    static String xmlFilePath=null;
    static String specsFolder=null;

    static int tmp=0;

    public static void main(String[] args) {


        String option = args[0];
        if(option.equals("xml"))
        {
            ntFolderPath = checkPath(args[1]);
            xmlFilePath = args[2];

            files = getFilesWithExtension(ntFolderPath, "n3",true);
            if(files == null)
            {
                System.out.println("No n3 files in the folder");
                System.exit(1);
            }
            processXMLFile(); 
        }
        else if (option.equals("txt"))
        {
            ntFolderPath = checkPath(args[1]);
            xmlFilePath = args[2];
            specsFolder = args[3];

            files = getFilesWithExtension(ntFolderPath, "n3",true);
            if(files == null)
            {
                System.out.println("No n3 files in the folder");
                System.exit(1);
            }
            processXMLFileAsTxt();
        }
        else
            System.out.println("You must specify the as first param.spec processing as xml or txt: 'xml' ,'txt'");


    }
    public static void processXMLFileAsTxt()
    {
        boolean fileSwitch=false;
        List<String> lines = readFile.read(xmlFilePath);
        List<String> newLines = new ArrayList<String>();
        String fileName="";
        //for each file in the .n3
        for (String file :files) {
            //read lines from spec file

            for (String line : lines) {
                if(line.contains("PREFIX") || line.contains("<NAMESPACE>") || line.contains("<LABEL>"))
                {
                    newLines.add(line);
                    continue;
                }
                if(line.contains("<SOURCE>"))
                    fileSwitch=true;

                if(fileSwitch && line.contains("<ENDPOINT>"))
                {
                    fileSwitch =false;
                    String newLine = "\t<ENDPOINT>"+file+"</ENDPOINT>\n";
                    newLines.add(newLine);
                }
                else  if(line.contains("<FILE>") && line.contains("Acceptance"))
                {
                    fileName = file.substring(file.lastIndexOf("/")+1, file.lastIndexOf("."));
                    String newLine = "<FILE>Acceptance_US_WB_"+fileName+".nt</FILE>";
                    newLines.add(newLine);
                }
                else  if(line.contains("<FILE>") && line.contains("Review"))
                {
                    fileName = file.substring(file.lastIndexOf("/")+1, file.lastIndexOf("."));
                    String newLine = "<FILE>Review_US_WB_"+fileName+".nt</FILE>";
                    newLines.add(newLine);
                }
                else
                    newLines.add(line);
            }
            //String newXMLFile = xmlFilePath.substring(0,xmlFilePath.lastIndexOf("/")+1)+fileName+".xml";
            String newXMLFile = specsFolder+fileName+".xml";
            writeFile.write(newLines, newXMLFile);
            lines =newLines;
            newLines = new ArrayList<String>();
/*
            PPJoinController p = new PPJoinController();
            p.run(xmlFilePath);*/

        }

    }

/*    public static void processXMLFileAsTxt()
    {
        boolean fileSwitch=false;
        List<String> lines = readFile.read(xmlFilePath);
        List<String> newLines = new ArrayList<String>();
        //for each file in the .n3
        for (String file :files) {
            //read lines from spec file

            for (String line : lines) {
                if(line.contains("PREFIX") || line.contains("<NAMESPACE>") || line.contains("<LABEL>"))
                {
                    newLines.add(line);
                    continue;
                }
                if(line.contains("<SOURCE>"))
                    fileSwitch=true;

                if(fileSwitch && line.contains("<ENDPOINT>"))
                {
                    fileSwitch =false;
                    String newLine = "\t<ENDPOINT>"+file+"</ENDPOINT>\n";
                    newLines.add(newLine);
                }
                else  if(line.contains("<FILE>") && line.contains("Acceptance"))
                {
                    String fileName = file.substring(file.lastIndexOf("/")+1, file.lastIndexOf("."));
                    String newLine = "<FILE>Acceptance_US_WB_"+fileName+".nt</FILE>";
                    newLines.add(newLine);
                }
                else  if(line.contains("<FILE>") && line.contains("Review"))
                {
                    String fileName = file.substring(file.lastIndexOf("/")+1, file.lastIndexOf("."));
                    String newLine = "<FILE>Review_US_WB_"+fileName+".nt</FILE>";
                    newLines.add(newLine);
                }
                else
                    newLines.add(line);
            }

            writeFile.write(newLines, xmlFilePath);
            lines =newLines;
            newLines = new ArrayList<String>();

            PPJoinController p = new PPJoinController();
            p.run(xmlFilePath);

        }

    }*/
    public static void processXMLFile()
    {
        try {

            File fXmlFile = new File(xmlFilePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setValidating(false);
            dbFactory.setNamespaceAware(true);
            dbFactory.setFeature("http://xml.org/sax/features/namespaces", false);
            dbFactory.setFeature("http://xml.org/sax/features/validation", false);
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            // dbFactory.setFeature("http://apache.org/xml/features/validation/dynamic", true);


            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            //  System.out.println("Processing file " +file.substring(file.lastIndexOf("/")+1, file.length()));

            for (String f : files) {

                NodeList nList = doc.getElementsByTagName("ENDPOINT");
                System.out.println("----------------------------");

                for (int temp = 0; temp < nList.getLength(); temp++) {

                    Node nNode = nList.item(temp); // get the us-grant node


                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                        Element eElement = (Element) nNode;
                        System.out.println(eElement.getTextContent());
                        if(eElement.getParentNode().getNodeName().equals("SOURCE"))
                            eElement.setTextContent(f);
                        //NodeList childNodes = eElement.getElementsByTagName("ENDPOINT");
                    }

                }
                ///////////////////////////////////
                nList = doc.getElementsByTagName("FILE");
                String s = f.substring(f.lastIndexOf("/")+1, f.lastIndexOf("."));

                System.out.println("----------------------------");

                for (int temp = 0; temp < nList.getLength(); temp++) {

                    Node nNode = nList.item(temp); // get the us-grant node

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                        Element eElement = (Element) nNode;
                        if(eElement.getParentNode().getNodeName().equals("ACCEPTANCE"))
                            eElement.setTextContent("Acceptance_US_WB_Country_"+s+".nt");
                        else if(eElement.getParentNode().getNodeName().equals("REVIEW"))
                            eElement.setTextContent("Review_US_WB_Country_"+s+".nt");


                        //NodeList childNodes = eElement.getElementsByTagName("ENDPOINT");
                    }

                }
                ///////////////////////////////////////////////////////
                //write it down back
                try 
                {  
                    /*                    String fileName  = file.substring(file.lastIndexOf("/")+1, file.length());
                    fileName="mod"+fileName;*/
                    Source source = new DOMSource(doc); 
                    File xmlFile = new File(xmlFilePath);            
                    StreamResult result = new StreamResult(new OutputStreamWriter(new FileOutputStream(xmlFile), "UTF-8"/*"ISO-8859-1"*/));
                    Transformer xformer = TransformerFactory.newInstance().newTransformer();

                    xformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    xformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                    xformer.setOutputProperty(OutputKeys.METHOD, "xml");
                    DOMImplementation domImpl = doc.getImplementation();
                    DocumentType doctype = domImpl.createDocumentType("doctype","LIMES","limes.dtd");
                    // xformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doctype.getPublicId());
                    xformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());
                    source = new DOMSource(doc);
                    result = new StreamResult(new File(xmlFilePath));


                    xformer.transform(source, result);
                    /////////////////////////////////////////////////////////
                    //                    
                    //                    ProcessBuilder pb = new ProcessBuilder("/path/to/java", "-jar", "your.jar");
                    //                    pb.directory(new File("preferred/working/directory"));
                    //                    Process p = pb.start();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }

                PPJoinController p = new PPJoinController();
                p.run(xmlFilePath);
            }






        } catch (Exception e) {
            e.printStackTrace();
        }
    }


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

    public static String checkPath(String path)
    {
        if(!path.endsWith("/"))
            path=path+"/";
        return path;
    }



}
