package gr.ntua.iccs.imu.recommendersimulation;

import gr.ntua.iccs.imu.recommendersimulation.persistence.model.controllers.exceptions.NonexistentEntityException;
import gr.ntua.iccs.imu.recommendersimulation.persistence.model.controllers.exceptions.PreexistingEntityException;
import gr.ntua.iccs.imu.recommendersimulation.services.EventParsingService;
import gr.ntua.iccs.imu.recommendersimulation.services.EventSimulationService;
import gr.ntua.iccs.imu.recommendersimulation.services.MailEventParsingService;
import java.io.IOException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnit;
import javax.transaction.UserTransaction;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;

/**
 * Hello world!
 *
 */
public class App {

    private final static String filePathNew = "C:\\Users\\Kostas\\Documents\\Work\\Dev\\IEEESoftwareRecommenderSystemEvaluator\\ALERT.KEUI.IssueNew.Annotated";
    private final static String filePathUpdated = "C:\\Users\\Kostas\\Documents\\Work\\Dev\\IEEESoftwareRecommenderSystemEvaluator\\ALERT.KEUI.IssueUpdate.Annotated";
    @PersistenceUnit(unitName = "socrates_persistence")
    public static EntityManagerFactory emf;
    @PersistenceUnit(unitName = "stardom_persistence")
    public static EntityManagerFactory emfStardom;
    @Resource
    private static UserTransaction utx;
    private static String filePathNewMail = "C:\\Users\\Kostas\\Documents\\Work\\Dev\\IEEESoftwareRecommenderSystemEvaluator\\ALERT.KEUI.MailNew.Annotated";

    public static UserTransaction getUtx() {
        return utx;
    }

    public static void setUtx(UserTransaction utx) {
        App.utx = utx;
    }

    
    public static EntityManagerFactory getEmf() {
        return emf;
    }


    public static void main(String[] args) {

        emf = Persistence.createEntityManagerFactory("socrates_persistence");
        emfStardom = Persistence.createEntityManagerFactory("stardom_persistence");
    
        
        System.out.println("Parsing Data from Events");
        
//        EventParsingService eventParsingService = new EventParsingService();
//        try {
//
//            eventParsingService.readXmlsFrom(filePathNew);
//            eventParsingService.readXmlsFrom(filePathUpdated);
//        } catch (ParseException ex) {
//            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
//
//        } catch (ParserConfigurationException ex) {
//            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (SAXException ex) {
//            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (XPathExpressionException ex) {
//            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
//        }
        
//                MailEventParsingService mailEventParsingService = new MailEventParsingService();
//                        try {
//
//            mailEventParsingService.readXmlsFrom(filePathNewMail);
//            
//        } catch (ParseException ex) {
//            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
//
//        } catch (ParserConfigurationException ex) {
//            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (SAXException ex) {
//            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (XPathExpressionException ex) {
//            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
//        }
        
        System.out.println("Parsed Data, Now performing simulation");
        EventSimulationService eventSimulationService=new EventSimulationService();
        try {
//            eventSimulationService.gatherTextForAppearingEvents();
              eventSimulationService.goThroughEvents();
//        } catch (NonexistentEntityException ex) {
//            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (PreexistingEntityException ex) {
//            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }

    public static EntityManagerFactory getEmfStardom() {
        return emfStardom;
    }

    public static void setEmfStardom(EntityManagerFactory emfStardom) {
        App.emfStardom = emfStardom;
    }
}
