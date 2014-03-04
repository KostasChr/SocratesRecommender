/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.ntua.iccs.imu.recommendersimulation.services;

import gr.ntua.iccs.imu.recommendersimulation.App;
import gr.ntua.iccs.imu.recommendersimulation.persistence.model.socrates.EventReceived;
import gr.ntua.iccs.imu.recommendersimulation.persistence.model.controllers.EventReceivedJpaController;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Kostas
 */
public class EventParsingService {
protected static final String JSI_URL = "http://ailab.ijs.si/alert/resource/";
    public EventParsingService() {
    }

    public boolean readXmlsFrom(String filepath) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, ParseException {

        EventReceivedJpaController eventReceivedJpaController = new EventReceivedJpaController(App.getUtx(),App.getEmf());
        //getting xml prerequisite
        DocumentBuilderFactory domFactory =
                DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        XPath xpath = XPathFactory.newInstance().newXPath();

        NamespaceContext ctx = new NamespaceContext() {
            public String getNamespaceURI(String prefix) {
                String uri;
                if (prefix.equals("soap")) {
                    uri = "http://www.w3.org/2003/05/soap-envelope";
                } else if (prefix.equals("wsnt")) {
                    uri = "http://docs.oasis-open.org/wsn/b-2";
                } else if (prefix.equals("ns1")) {
                    uri = "http://www.alert-project.eu/";
                } else if (prefix.equals("s")) {
                    uri = "http://www.alert-project.eu/strevents-kesi";
                } else if (prefix.equals("s1")) {
                    uri = "http://www.alert-project.eu/strevents-keui";
                } else {
                    uri = null;
                }
                return uri;
            }

            public String getPrefix(String string) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public Iterator getPrefixes(String string) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        xpath.setNamespaceContext(ctx);
        XPathExpression userEmailExpr = xpath.compile("//soap:Envelope/soap:Body/wsnt:Notify/wsnt:NotificationMessage/wsnt:Message/ns1:event/ns1:payload/ns1:eventData/s:kesi/s:issueAuthor/s:email");
        XPathExpression userEmailSecondExpr = xpath.compile("//soap:Envelope/soap:Body/wsnt:Notify/wsnt:NotificationMessage/wsnt:Message/ns1:event/ns1:payload/ns1:eventData/s:kesi/s:issueComment/s:commentPerson/s:email");
        XPathExpression issueTextExpr = xpath.compile("//soap:Envelope/soap:Body/wsnt:Notify/wsnt:NotificationMessage/wsnt:Message/ns1:event/ns1:payload/ns1:eventData/s:kesi/s:issueComment/s:commentText");
        XPathExpression issueIdExpr = xpath.compile("//soap:Envelope/soap:Body/wsnt:Notify/wsnt:NotificationMessage/wsnt:Message/ns1:event/ns1:payload/ns1:eventData/s:kesi/s:issueId");
        XPathExpression issueDateExpr = xpath.compile("//soap:Envelope/soap:Body/wsnt:Notify/wsnt:NotificationMessage/wsnt:Message/ns1:event/ns1:payload/ns1:eventData/s:kesi/s:issueDateOpened");
        XPathExpression commentDateExpr = xpath.compile("//soap:Envelope/soap:Body/wsnt:Notify/wsnt:NotificationMessage/wsnt:Message/ns1:event/ns1:payload/ns1:eventData/s:kesi/s:issueComment/s:commentDate");
        XPathExpression statusExpr = xpath.compile("//soap:Envelope/soap:Body/wsnt:Notify/wsnt:NotificationMessage/wsnt:Message/ns1:event/ns1:payload/ns1:eventData/s:kesi/s:issueStatus");
        XPathExpression issueActivityWhenExpr = xpath.compile("//soap:Envelope/soap:Body/wsnt:Notify/wsnt:NotificationMessage/wsnt:Message/ns1:event/ns1:payload/ns1:eventData/s:kesi/s:issueActivity/s:activityWhen");
        XPathExpression issueActivityWhatExpr = xpath.compile("//soap:Envelope/soap:Body/wsnt:Notify/wsnt:NotificationMessage/wsnt:Message/ns1:event/ns1:payload/ns1:eventData/s:kesi/s:issueActivity/s:activityWhat");
        XPathExpression issueActivityWhoExpr = xpath.compile("//soap:Envelope/soap:Body/wsnt:Notify/wsnt:NotificationMessage/wsnt:Message/ns1:event/ns1:payload/ns1:eventData/s:kesi/s:issueActivity/s:activityWho");
        XPathExpression annotationsExpr = xpath.compile("//soap:Envelope/soap:Body/wsnt:Notify/wsnt:NotificationMessage/wsnt:Message/ns1:event/ns1:payload/ns1:eventData/s1:keui/s1:issueComment/s1:commentTextConcepts/s1:concept");
        
        //getting list of files
        final File folder = new File(filepath);
        final List<File> fileList = Arrays.asList(folder.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        }));


        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss");
        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");

        for (File f : fileList) {
            System.out.println("Reading file " + f.getName());
            Date parsedDate = null;
            Date parsedCommentDate = null;
            Document doc = builder.parse(f);
            String userEmail = userEmailExpr.evaluate(doc);
            if (userEmail.equals("")) {
                userEmail = userEmailSecondExpr.evaluate(doc);
                if (userEmail.equals("")){
                userEmail = issueActivityWhoExpr.evaluate(doc);
                }
            }
            String issueText = issueTextExpr.evaluate(doc);
            String issueId = issueIdExpr.evaluate(doc);
            String dateStr = issueDateExpr.evaluate(doc);
            if (dateStr.equals("")) {
                dateStr = issueActivityWhenExpr.evaluate(doc);
                if (dateStr.equals("")) { // could not find the date the item was generated. 
                    parsedDate = new Date();
                } else {
                    parsedDate = df2.parse(dateStr);
                }
            } else {
                parsedDate = df.parse(dateStr);
            }
            String commentDateStr = commentDateExpr.evaluate(doc);
            if (commentDateStr.equals("")) {
                commentDateStr = issueActivityWhenExpr.evaluate(doc);
                parsedCommentDate = df2.parse(commentDateStr);
            } else {
                parsedCommentDate = df2.parse(commentDateStr);
            }

              NodeList subjectSet = (NodeList) annotationsExpr.evaluate(doc,XPathConstants.NODESET);
            String textAdded="";
           for (int i=0;i<subjectSet.getLength();i++){
                Node item = subjectSet.item(i);
//                System.out.println( "uri" + item.getFirstChild().getTextContent());
                String tagStr=item.getFirstChild().getTextContent().replaceAll(JSI_URL,"");
//                System.out.println( "weight" + item.getLastChild().getTextContent());
                double weight=Double.parseDouble(item.getLastChild().getTextContent().replaceAll("\\,","\\."));
                
                    while (weight>0){
                        weight--;
                        textAdded+=tagStr + " ";
                        
                    }
                
          }
//           System.out.println(textAdded) ;
//           if(textAdded.equals("")){
//               System.out.println("PROBLEM!!!!!!!===============");
//           }
//                <s:issueDateOpened>2011-01-31T17:04:00+01:00</s:issueDateOpened>


            String statusStr = statusExpr.evaluate(doc);
            if (statusStr.equals("")) {
                statusStr = issueActivityWhatExpr.evaluate(doc);
            }
            System.out.println("Issue  \n ==============\n "
                    + issueId + " \n " + userEmail + "\n" + dateStr + " = " + parsedDate.toString() + " " + "\n" + statusStr + "\n" +  "\n *****************");


            EventReceived eventReceived = new EventReceived(null, issueId, statusStr, textAdded.getBytes(), userEmail, parsedCommentDate, parsedDate);
            eventReceivedJpaController.create(eventReceived);

        }



        return true;


    }
}
