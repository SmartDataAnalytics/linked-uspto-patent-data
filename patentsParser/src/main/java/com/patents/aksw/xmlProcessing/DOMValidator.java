package com.patents.aksw.xmlProcessing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.custommonkey.xmlunit.Validator;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class DOMValidator{
	/*public static void testValidation() throws Exception {
		XMLUnit.getTestDocumentBuilderFactory().setValidating(true);
		// As the document is parsed it is validated against its referenced DTD
		Document myTestDocument = XMLUnit.buildTestDocument("/media/mofeed/A0621C46621C24164/03_Work/AmrapaliWorks/patents2015/samples/splitted/ipg070123_0000.xml");
		String mySystemId = "Mofo";
		String myDTDUrl = new File("/media/mofeed/A0621C46621C24164/03_Work/AmrapaliWorks/patents2015/samples/splitted/patents.dtd").toURL().toExternalForm();
		Validator myValidator = new Validator(myTestDocument, mySystemId,myDTDUrl);
		if(myValidator.isValid())
			System.out.println("test document validates against unreferenced DTD");
		else
			System.out.println("Worse");
		}*/
//	public static void main(String[] args) throws Exception {
//		testValidation();
	 /*     try {
	         File x = new File(args[0]);
	         DocumentBuilderFactory f
	            = DocumentBuilderFactory.newInstance();
	         f.setValidating(true); // Default is false
	         DocumentBuilder b = f.newDocumentBuilder();
	         // ErrorHandler h = new DefaultHandler();
	         ErrorHandler h = new MyErrorHandler();
	         b.setErrorHandler(h);
	         Document d = b.parse(x);
	      } catch (ParserConfigurationException e) {
	         System.out.println(e.toString());      
	      } catch (SAXException e) {
	         System.out.println(e.toString());      
	      } catch (IOException e) {
	         System.out.println(e.toString());      
	      }*/
	//   }
	   private static class MyErrorHandler implements ErrorHandler {
	      public void warning(SAXParseException e) throws SAXException {
	         System.out.println("Warning: "); 
	         printInfo(e);
	      }
	      public void error(SAXParseException e) throws SAXException {
	         System.out.println("Error: "); 
	         printInfo(e);
	      }
	      public void fatalError(SAXParseException e) 
	         throws SAXException {
	         System.out.println("Fattal error: "); 
	         printInfo(e);
	      }
	      private void printInfo(SAXParseException e) {
	         System.out.println("   Public ID: "+e.getPublicId());
	         System.out.println("   System ID: "+e.getSystemId());
	         System.out.println("   Line number: "+e.getLineNumber());
	         System.out.println("   Column number: "+e.getColumnNumber());
	         System.out.println("   Message: "+e.getMessage());
	      }
	   }
	/*public static boolean validate(String xmlFile, String dtdFile)
	{
		try{
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setValidating(true);
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		builder.setErrorHandler(new ErrorHandler() {
		    public void error(SAXParseException exception) throws SAXException {
		        // do something more useful in each of these handlers
  	
		    	
		        exception.printStackTrace();
		    }
		    public void fatalError(SAXParseException exception) throws SAXException {
		        exception.printStackTrace();
		    }

		    public void warning(SAXParseException exception) throws SAXException {
		        exception.printStackTrace();
		    }
		});
		Document doc = null;
		doc = builder.parse("employee.xml");
		if(doc!=null)
			return true;
		}
		catch(Exception e){}
		return false;
	}*/
	   ////////////////////////////////////////////////////////////////////////////////////////////////////
	   static public void main(String[] arg) {
		    boolean validate = false;

		    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		   try { dbf.setValidating(validate);
		    dbf.setNamespaceAware(false);
		    dbf.setIgnoringElementContentWhitespace(true);
		    dbf.setValidating(false);
		    dbf.setNamespaceAware(true);
		    
				dbf.setFeature("http://xml.org/sax/features/namespaces", false);
			
		    dbf.setFeature("http://xml.org/sax/features/validation", false);
		    dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
		    dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
} catch (ParserConfigurationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		    // Parse the input to produce a parse tree with its root
		    // in the form of a Document object
		    Document doc = null;
		    try {
		      DocumentBuilder builder = dbf.newDocumentBuilder();
		      builder.setErrorHandler(new MyErrorHandler());
		      InputSource is = new InputSource("/media/mofeed/A0621C46621C24164/03_Work/AmrapaliWorks/patents2015/samples/splitted/ipg070123_0000.xml");
		      doc = builder.parse(is);
		    } catch (SAXException e) {
		      System.exit(1);
		    } catch (ParserConfigurationException e) {
		      System.err.println(e);
		      System.exit(1);
		    } catch (IOException e) {
		      System.err.println(e);
		      System.exit(1);
		    }
		    dump(doc,1);
		    /////////////////
		    try {
			      DocumentBuilder builder = dbf.newDocumentBuilder();
			      builder.setErrorHandler(new MyErrorHandler());
			      InputSource is = new InputSource("/media/mofeed/A0621C46621C24164/03_Work/AmrapaliWorks/patents2015/samples/splitted/ipg070123_0001.xml");
			      doc = builder.parse(is);
			    } catch (SAXException e) {
			      System.exit(1);
			    } catch (ParserConfigurationException e) {
			      System.err.println(e);
			      System.exit(1);
			    } catch (IOException e) {
			      System.err.println(e);
			      System.exit(1);
			    }
			    dump(doc,2);
			    if(compareTrees())
			    	System.out.println("equals");
			    else
			    	System.out.println("different");
		  }
	   	
	   	  private static boolean compareTrees()
	   	  {
	   		  boolean equal = true;
	   		  for(int i=0; i<xmlTree1.size();i++)
	   		  {
	   			  if(!xmlTree1.get(i).equals(xmlTree2.get(i)))
	   			  {
	   				 System.out.println("Difference:" +xmlTree1.get(i)+"<>"+xmlTree2.get(i));
	   				equal= false;
	   			  }
	   		  }
	   		  return equal;
	   	  }
		  private static void dump(Document doc, int fileId) {
		    dumpLoop((Node) doc, "",fileId);
		  }
		  public static List<String> xmlTree1 = new ArrayList<String>();
		  public static List<String> xmlTree2 = new ArrayList<String>();

		  private static void dumpLoop(Node node, String indent,int fileId) {
		    switch (node.getNodeType()) {
	/*	    case Node.CDATA_SECTION_NODE:
		      System.out.println(indent + "CDATA_SECTION_NODE");
		      break;
		    case Node.COMMENT_NODE:
		      System.out.println(indent + "COMMENT_NODE");
		      break;
		    case Node.DOCUMENT_FRAGMENT_NODE:
		      System.out.println(indent + "DOCUMENT_FRAGMENT_NODE");
		      break;
		    case Node.DOCUMENT_NODE:
		      System.out.println(indent + "DOCUMENT_NODE"+node.getNodeName());
		      break;
		    case Node.DOCUMENT_TYPE_NODE:
		      System.out.println(indent + "DOCUMENT_TYPE_NODE");
		      break;*/
		    case Node.ELEMENT_NODE:
		      System.out.println(indent + "ELEMENT_NODE: "+node.getNodeName());
		      if(fileId==1)
		    	  xmlTree1.add(indent + "ELEMENT_NODE: "+node.getNodeName());
		      else
		    	  xmlTree2.add(indent + "ELEMENT_NODE: "+node.getNodeName());

		      break;
		  /*  case Node.ENTITY_NODE:
		      System.out.println(indent + "ENTITY_NODE");
		      break;
		    case Node.ENTITY_REFERENCE_NODE:
		      System.out.println(indent + "ENTITY_REFERENCE_NODE");
		      break;
		    case Node.NOTATION_NODE:
		      System.out.println(indent + "NOTATION_NODE");
		      break;
		    case Node.PROCESSING_INSTRUCTION_NODE:
		      System.out.println(indent + "PROCESSING_INSTRUCTION_NODE");
		      break;
		    case Node.TEXT_NODE:
		      System.out.print(indent + "TEXT_NODE");
		      System.out.println(" : "+node.getTextContent());
		      break;
		    default:
		      System.out.println(indent + "Unknown node");
		      break;*/
		    }

		    NodeList list = node.getChildNodes();
		    for (int i = 0; i < list.getLength(); i++){
		      dumpLoop(list.item(i), indent + "   ",fileId);
		    }
		  }
		}


		class MyErrorHandler implements ErrorHandler {
		  public void warning(SAXParseException e) throws SAXException {
		    show("Warning", e);
		    throw (e);
		  }

		  public void error(SAXParseException e) throws SAXException {
		    show("Error", e);
		    throw (e);
		  }

		  public void fatalError(SAXParseException e) throws SAXException {
		    show("Fatal Error", e);
		    throw (e);
		  }

		  private void show(String type, SAXParseException e) {
		    System.out.println(type + ": " + e.getMessage());
		    System.out.println("Line " + e.getLineNumber() + " Column " + e.getColumnNumber());
		    System.out.println("System ID: " + e.getSystemId());
		  }

}
