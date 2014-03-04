/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.ntua.iccs.imu.recommendersimulation.services;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.pipe.iterator.StringArrayIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.doublealgo.Statistic;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import gr.ntua.iccs.imu.metric.metrics.MeanReciprocalRank;
import gr.ntua.iccs.imu.metric.metrics.RocCurve;
import gr.ntua.iccs.imu.metric.model.RecommendedItem;
import gr.ntua.iccs.imu.metric.model.RecommendedTemporalItem;
import gr.ntua.iccs.imu.recommendersimulation.App;
import gr.ntua.iccs.imu.recommendersimulation.persistence.model.socrates.EventReceived;
import gr.ntua.iccs.imu.recommendersimulation.persistence.model.stardom.IdentityProfileView;
import gr.ntua.iccs.imu.recommendersimulation.persistence.model.socrates.IssueSubjectView;
import gr.ntua.iccs.imu.recommendersimulation.persistence.model.socrates.IssueText;
import gr.ntua.iccs.imu.recommendersimulation.persistence.model.controllers.EventReceivedJpaController;
import gr.ntua.iccs.imu.recommendersimulation.persistence.model.controllers.IdentityProfileViewJpaController;
import gr.ntua.iccs.imu.recommendersimulation.persistence.model.controllers.IssueSubjectViewJpaController;
import gr.ntua.iccs.imu.recommendersimulation.persistence.model.controllers.IssueTextJpaController;
import gr.ntua.iccs.imu.recommendersimulation.persistence.model.controllers.MetricSingleTableJpaController;
import gr.ntua.iccs.imu.recommendersimulation.persistence.model.controllers.exceptions.NonexistentEntityException;
import gr.ntua.iccs.imu.recommendersimulation.persistence.model.controllers.exceptions.PreexistingEntityException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.stat.clustering.EuclideanIntegerPoint;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.netlib.lapack.Strrfs;

/**
 *
 * @author Kostas
 */
public class EventSimulationService {

    private static final double IssueMultiplicationCnt = 10.0;
    public static final String JSI_URL = "http://ailab.ijs.si/alert/resource/";
    private static final Integer numTopics = 25;
    public static final String ISSUES_RESOLVED_STR = "IssuesResolved";
    public static final int ITERATIONS_COUNT = 2000;
    public static final double MIXING_PARAMETER = 15.0;
    private RAMDirectory luceneDirectoryOfDocuments;
    private EnglishAnalyzer englishAnalyzer;
    private IndexWriterConfig indexWriterConfig;
    private IndexWriter issueIndexWriter;
    private RAMDirectory luceneUserDirectoryOfDocuments;
    private IndexWriter userIndexWriter;
    private IndexWriterConfig indexUserWriterConfig;

    public EventSimulationService() {
    }

    public int gatherTextForAppearingEvents() throws NonexistentEntityException, PreexistingEntityException, Exception {
        EventReceivedJpaController eventReceivedJpaController = new EventReceivedJpaController(App.getUtx(), App.getEmf());
        IssueSubjectViewJpaController issueSubjectViewJpaController = new IssueSubjectViewJpaController(App.getEmf());
        IssueTextJpaController issueTextJpaController = new IssueTextJpaController(App.getUtx(), App.getEmf());

        List<EventReceived> eventReceivedList = eventReceivedJpaController.findEventReceivedEntities();



        for (EventReceived er : eventReceivedList) {



            IssueText it = issueTextJpaController.findIssueText(String.valueOf(er.getId()));
            if (it != null) {
//                newText = newText + " " + it.getText();
                issueTextJpaController.destroy(String.valueOf(er.getId()));
            }
            issueTextJpaController.create(new IssueText(String.valueOf(er.getId()), new String(er.getDescription())));

        }
        return 0;

    }

    public int goThroughEvents() throws IOException, ParseException {

        //Initialize JPA Controllers
        MetricSingleTableJpaController metricSingleTableJpaController = new MetricSingleTableJpaController(App.getUtx(), App.getEmfStardom());
        IdentityProfileViewJpaController identityProfileViewJpaController = new IdentityProfileViewJpaController(App.getUtx(), App.getEmfStardom());
        IssueTextJpaController issueTextJpaController = new IssueTextJpaController(App.getUtx(), App.getEmf());
//        IssueSubjectViewJpaController issueSubjectViewJpaController = new IssueSubjectViewJpaController(App.getEmf());
        EventReceivedJpaController eventReceivedJpaController = new EventReceivedJpaController(App.getUtx(), App.getEmf());

        //Prepare, make topic model to use for analysis of texts
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add(new CharSequenceLowercase());
        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
        pipeList.add(new TokenSequence2FeatureSequence());
        InstanceList instances = new InstanceList(new SerialPipes(pipeList));
        Integer countTexts = issueTextJpaController.getIssueTextCount();
        String data[] = new String[countTexts];
        List<IssueText> IssueTextList = issueTextJpaController.findIssueTextEntities();
        int counter = 0;
        for (IssueText it : IssueTextList) {
            data[counter] = it.getText();
            counter++;
        }
        instances.addThruPipe(new StringArrayIterator(data));
        // Create a model with 10 topics, alpha_t = 0.01, beta_w = 0.01

        ParallelTopicModel model = new ParallelTopicModel(numTopics, 1.0, 0.01);
        model.addInstances(instances);
        model.setNumThreads(2);
        model.setNumIterations(ITERATIONS_COUNT);
        model.estimate();
        TopicInferencer inferencer = model.getInferencer();



        //prepare lucene index for issues && distribution map for issues
        luceneDirectoryOfDocuments = new RAMDirectory();
        englishAnalyzer = new EnglishAnalyzer(Version.LUCENE_31, null);
        indexWriterConfig = new IndexWriterConfig(Version.LUCENE_31, englishAnalyzer);
        issueIndexWriter = new IndexWriter(luceneDirectoryOfDocuments, indexWriterConfig);

        HashMap<String, double[]> issueDistributions = new HashMap<String, double[]>();
        List<EventReceived> eventReceivedList = eventReceivedJpaController.findEventReceivedEntities();
        for (EventReceived er : eventReceivedList) {
            InstanceList instances2 = new InstanceList(new SerialPipes(pipeList));
            String data2[] = new String[1];
            data2[0] = new String(er.getDescription());
            instances2.addThruPipe(new StringArrayIterator(data2));
            issueDistributions.put(String.valueOf(er.getId()), inferencer.getSampledDistribution(instances2.get(0), 250, 10, 100));
            String issueId = String.valueOf(er.getId());
            addDoc(issueIndexWriter, issueId, new String(er.getDescription()));
        }
        issueIndexWriter.close();

        //Prepare the topic distributions2 (multiplied by 10) for the suggested model
        HashMap<String, double[]> issueDistributions2 = new HashMap<String, double[]>();
        Iterator it2 = issueDistributions.entrySet().iterator();
        while (it2.hasNext()) {
            Map.Entry pairs = (Map.Entry) it2.next();
            String issueId = (String) pairs.getKey();
            double[] issueDistribution = (double[]) pairs.getValue();
            for (int i = 0; i < issueDistribution.length; i++) {
                issueDistribution[i] = issueDistribution[i] * IssueMultiplicationCnt;
            }
            issueDistributions2.put(issueId, issueDistribution);
        }

        //Prepare the events received, sorting them by date
        eventReceivedList = eventReceivedJpaController.findEventReceivedEntities();
        Collections.sort(eventReceivedList);
        Collections.reverse(eventReceivedList);

        //HashSets of RocCurves and MeanReciprocalRanks for metrics
        HashMap<String, RocCurve> rocCurvesMap = new HashMap<String, RocCurve>();
        HashMap<String, MeanReciprocalRank> mrrMap = new HashMap<String, MeanReciprocalRank>();
        prepareMetrics(rocCurvesMap, mrrMap);

        //Initialize map of users to text
        HashMap<String, String> userTextMap = new HashMap<String, String>();
        //Initialize index of users
        luceneUserDirectoryOfDocuments = new RAMDirectory();
        indexUserWriterConfig = new IndexWriterConfig(Version.LUCENE_31, englishAnalyzer);
        userIndexWriter = new IndexWriter(luceneUserDirectoryOfDocuments, indexUserWriterConfig);
        userIndexWriter.commit();
        IndexReader userIndexReader = IndexReader.open(luceneUserDirectoryOfDocuments);
        IndexSearcher userIndexSearcher = new IndexSearcher(userIndexReader);
        //Initialize maps of users to double where we will store the topic based distributions
        HashMap<String, double[]> userDistributions = new HashMap<String, double[]>();
        HashMap<String, double[]> userDistributions2 = new HashMap<String, double[]>();

        ArrayList<RecommendedItem> IssuesResolvedDevelopersLst = new ArrayList<RecommendedItem>();
        ArrayList<RecommendedItem> EmailsSentDevelopersLst = new ArrayList<RecommendedItem>();
        ArrayList<RecommendedItem> OtherItsActivityDevelopersLst = new ArrayList<RecommendedItem>();
        ArrayList<RecommendedItem> AggregatedDevelopersLst = new ArrayList<RecommendedItem>();
        ArrayList<RecommendedTemporalItem> IssuesResolvedTemporalDevelopersLst = new ArrayList<RecommendedTemporalItem>();
        ArrayList<RecommendedTemporalItem> EmailsSentTemporalDevelopersLst = new ArrayList<RecommendedTemporalItem>();
        ArrayList<RecommendedTemporalItem> OtherItsActivityTemporalDevelopersLst = new ArrayList<RecommendedTemporalItem>();
        Integer eventCounter = 0;
        for (EventReceived er : eventReceivedList) {

            //if the user email  AND issueId is not null, not None, continue
            if (er.getEmail().equals("") || er.getEmail().equals("None") || String.valueOf(er.getId()) == null) {
                continue;
            }




            //Initialize with the correct developer
            IdentityProfileView findIdentityProfileViewEntitiesByUserEmail = identityProfileViewJpaController.findIdentityProfileViewEntitiesByUserEmail(er.getEmail());
            if (findIdentityProfileViewEntitiesByUserEmail == null) {
                continue;
            }
            RecommendedItem correctDeveloper = new RecommendedItem(String.valueOf(findIdentityProfileViewEntitiesByUserEmail.getId()), 1.0);
            String currentText = issueTextJpaController.findIssueText(String.valueOf(er.getId())).getText();
//            System.out.println("developer found " + correctDeveloper.getId() + "\n=========\n" + currentText);
//            if (er.getStatus().equals("resolution") || er.getStatus().equals("Resolved")) {
            if (er.getStatus().equals("RSL")) {
                System.out.println("RESOLVED" + er.getDate() + " " + er.getEmail());
                System.out.println("developer found " + correctDeveloper.getId() + "\n=========\n" + currentText);
                eventCounter++;
//                //  "IssuesResolved"
//                List<RecommendedItem> recommendedItemsList = metricSingleTableJpaController.findIirmRankEntitiesBeforeDate(er.getDate());
//                ArrayList<RecommendedItem> suggestedIirmDevelopers = new ArrayList<RecommendedItem>();
//                for (RecommendedItem ri : recommendedItemsList) {
//                    suggestedIirmDevelopers.add(ri);
//                }
//                Collections.sort(suggestedIirmDevelopers);
                Collections.sort(IssuesResolvedDevelopersLst);
                Collections.sort(EmailsSentDevelopersLst);
                Collections.sort(OtherItsActivityDevelopersLst);

                
                
                //Aggregate in one list
                Double listAggregationCntr = 160.0;
                for (RecommendedItem ri : IssuesResolvedDevelopersLst) {
                    AggregatedDevelopersLst.add(new RecommendedItem(ri.getId(), listAggregationCntr/4.0));
                    listAggregationCntr--;
                    if (listAggregationCntr < 0) {
                        break;
                    }
                }
                listAggregationCntr = 5.0;
                for (RecommendedItem ri : EmailsSentDevelopersLst) {
                    Double previousCount = 0.0;
                    Iterator<RecommendedItem> AggregatedDevelopersLstIt = AggregatedDevelopersLst.iterator();
                    while (AggregatedDevelopersLstIt.hasNext()) {
                        RecommendedItem ri2 = AggregatedDevelopersLstIt.next();
                        if (ri2.getId().equals(ri.getId())) {
                            previousCount = ri.getSimilarity();
                            AggregatedDevelopersLstIt.remove();
                        }
                    }
                    AggregatedDevelopersLst.add(new RecommendedItem(ri.getId(), (previousCount + listAggregationCntr)/4.0));
                    listAggregationCntr--;
                    if (listAggregationCntr < 0) {
                        break;
                    }
                }
                listAggregationCntr = 5.0;
                for (RecommendedItem ri : OtherItsActivityDevelopersLst) {
                    Double previousCount = 0.0;
                    Iterator<RecommendedItem> AggregatedDevelopersLstIt = AggregatedDevelopersLst.iterator();
                    while (AggregatedDevelopersLstIt.hasNext()) {
                        RecommendedItem ri2 = AggregatedDevelopersLstIt.next();
                        if (ri2.getId().equals(ri.getId())) {
                            previousCount = ri.getSimilarity();
                            AggregatedDevelopersLstIt.remove();
                        }
                    }
                    AggregatedDevelopersLst.add(new RecommendedItem(ri.getId(), (previousCount + listAggregationCntr)/4.0));

                    listAggregationCntr--;
                    if (listAggregationCntr < 0) {
                        break;
                    }
                }
                 Collections.sort(AggregatedDevelopersLst);
                //"Improving" the topicbased2 suggestions
//               
//                HashMap<String, double[]> userDistributions3 = (HashMap<String, double[]>) userDistributions.clone();
//                Integer HighUsersCntr = 20;
//                for (RecommendedItem ri : IssuesResolvedDevelopersLst) {
//                    double[] userDistribution = userDistributions3.get(ri.getId());
//                    if (userDistribution == null) {
//                        continue;
//                    }
//                    System.out.println("Previous sample " + userDistribution [0] + " " +userDistribution [3] );
//                    for (int i = 0; i < userDistribution.length; i++) {
//                        userDistribution[i] = userDistribution[i] * (HighUsersCntr / 2.0);
//                    }
//                    System.out.println("Next sample " + userDistribution [0] + " " +userDistribution [3] );
//                    userDistributions3.remove(ri.getId());
//                    userDistributions3.put(ri.getId(), userDistribution);
//                    HighUsersCntr--;
//                    if (HighUsersCntr < 0) {
//                        break;
//                    }
//                }

                //"ItsTemporal"
//                recommendedItemsList = metricSingleTableJpaController.findItstRankEntitiesBeforeDate(er.getDate());
//                ArrayList<RecommendedItem> suggestedItstDevelopers = new ArrayList<RecommendedItem>();
//
//                for (RecommendedItem ri : recommendedItemsList) {
//                    suggestedItstDevelopers.add(ri);
//                }
//                Collections.sort(suggestedIirmDevelopers);


                //"MailingListTemporal"
//                recommendedItemsList = metricSingleTableJpaController.findMltRankEntitiesBeforeDate(er.getDate());
//                ArrayList<RecommendedItem> suggestedMltDevelopers = new ArrayList<RecommendedItem>();
//
//                for (RecommendedItem ri : recommendedItemsList) {
//                    suggestedMltDevelopers.add(ri);
//                }
//                Collections.sort(suggestedIirmDevelopers);

                //"ScmApiIntroduced"
//                recommendedItemsList = metricSingleTableJpaController.findSaiRankEntitiesBeforeDate(er.getDate());
//                ArrayList<RecommendedItem> suggestedSaiDevelopers = new ArrayList<RecommendedItem>();
//
//                for (RecommendedItem ri : recommendedItemsList) {
//                    suggestedSaiDevelopers.add(ri);
//                }
//                Collections.sort(suggestedSaiDevelopers);

                //"ScmTemporal"
//                recommendedItemsList = metricSingleTableJpaController.findScmtRankEntitiesBeforeDate(er.getDate());
//                ArrayList<RecommendedItem> suggestedScmtDevelopers = new ArrayList<RecommendedItem>();
//
//                for (RecommendedItem ri : recommendedItemsList) {
//                    suggestedScmtDevelopers.add(ri);
//                }
//                Collections.sort(suggestedScmtDevelopers);

                //"Semantic"
                //get the issue description
                currentText = issueTextJpaController.findIssueText(String.valueOf(er.getId())).getText();
                //use it to search in the user indexer
                ArrayList<RecommendedItem> suggestedSemanticDevelopers = searchUserIndex(userIndexSearcher, currentText);
                Collections.sort(suggestedSemanticDevelopers);

                //"TopicBased"
                //gather text of issue
                List<EventReceived> eventReceivedListIssue = eventReceivedJpaController.findEventReceivedByIssueId(er.getIssueId());
                String description = "";
                for (EventReceived er2 : eventReceivedListIssue) {
                    description = " " + new String(er2.getDescription());
                }
                //Infer Issue description 
                InstanceList instances2 = new InstanceList(new SerialPipes(pipeList));
                String data2[] = new String[1];
                data2[0] = new String(description);
                instances2.addThruPipe(new StringArrayIterator(data2));
                double[] issueDistribution = inferencer.getSampledDistribution(instances2.get(0), 250, 10, 100); // issueDistributions.get(String.valueOf(er.getId()));
               
                //calculate the list of suggestions
                Iterator it = userDistributions.entrySet().iterator();
                ArrayList<RecommendedItem> suggestedTopicDevelopers = new ArrayList<RecommendedItem>();
                while (it.hasNext()) {
                    Map.Entry pairs = (Map.Entry) it.next();
                    String userId = (String) pairs.getKey();
                    double[] userDistribution = (double[]) pairs.getValue();
                    DoubleMatrix1D a = new DenseDoubleMatrix1D(issueDistribution);
                    DoubleMatrix1D b = new DenseDoubleMatrix1D(userDistribution);
                    double similarity = a.zDotProduct(b) / Math.sqrt(a.zDotProduct(a) * b.zDotProduct(b));
                    if (similarity > 0) {
                        suggestedTopicDevelopers.add(new RecommendedItem(userId, similarity));
                    }
                }



                //"TopicBased2"
                it = userDistributions.entrySet().iterator();
                ArrayList<RecommendedItem> suggestedTopic2Developers = new ArrayList<RecommendedItem>();
                while (it.hasNext()) {
                    Map.Entry pairs = (Map.Entry) it.next();
                    String userId = (String) pairs.getKey();
                    double[] userDistribution = (double[]) pairs.getValue();
                    DoubleMatrix1D a = new DenseDoubleMatrix1D(issueDistribution);
                    DoubleMatrix1D b = new DenseDoubleMatrix1D(userDistribution);
                    
                    
                    //If the user belongs to "THELIST" multiply the matrix with his position
                    Iterator<RecommendedItem> AggregatedDevelopersLstIt = AggregatedDevelopersLst.iterator();
                    Integer iteratorCounter=20;
                    while (AggregatedDevelopersLstIt.hasNext()) {
                        RecommendedItem ri = AggregatedDevelopersLstIt.next();
                        if (ri.getId().equals(userId)) {
                                 b = b.assign(cern.jet.math.Functions.mult(iteratorCounter/2.0));
                        }
                        iteratorCounter--;
                        if (iteratorCounter<0) {
                            break;
                        }
                    }
                    

            
                    
//                    double similarity = (a.zDotProduct(b) / Math.sqrt(a.zDotProduct(a) * b.zDotProduct(b))) + MIXING_PARAMETER*( (  Math.sqrt(b.zDotProduct(b))* Math.sqrt(b.zDotProduct(b)))/ Math.sqrt(a.zDotProduct(a)));
                    double similarity = a.zDotProduct(b) ;
//                    if (similarity > 0) {
//                        suggestedTopicDevelopers.add(new RecommendedItem(userId, similarity));
//                    }

//                    double similarity = 1.0;
//                     
//                    double euclid =Statistic.EUCLID.apply(a, b); 
//                    if (euclid > 0) {
//                        similarity = 1 / euclid;
//                    }
//                    
//                    
                    if (similarity > 0) {
                        suggestedTopic2Developers.add(new RecommendedItem(userId, similarity));
                    }
                }
                if (eventCounter > 100) {
//old by fotis                    insertDataToMetrics(mrrMap, rocCurvesMap, correctDeveloper, suggestedItstDevelopers, suggestedSaiDevelopers, suggestedMltDevelopers, suggestedScmtDevelopers, suggestedIirmDevelopers, suggestedSemanticDevelopers, suggestedTopicDevelopers, suggestedTopic2Developers);
//new by me --> insert the temporals
                    insertDataToMetrics(mrrMap, rocCurvesMap, correctDeveloper, IssuesResolvedDevelopersLst, EmailsSentDevelopersLst, IssuesResolvedDevelopersLst, IssuesResolvedDevelopersLst, IssuesResolvedDevelopersLst, suggestedSemanticDevelopers, suggestedTopicDevelopers, suggestedTopic2Developers);
                }
//                if (eventCounter > 200) {
//                    break;
//                }
            }
            addUserToCounts(correctDeveloper, er, IssuesResolvedDevelopersLst, EmailsSentDevelopersLst, OtherItsActivityDevelopersLst, IssuesResolvedTemporalDevelopersLst, EmailsSentTemporalDevelopersLst, OtherItsActivityTemporalDevelopersLst);
            addUserToMaps(currentText, er, correctDeveloper, userTextMap, userDistributions, userDistributions2, inferencer, pipeList);

        }


        //Print results for ROC curves and MRR
        Iterator it = rocCurvesMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            System.out.println(pairs.getKey());
            ((RocCurve) pairs.getValue()).getCurve();
        }
        it = mrrMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            System.out.println(pairs.getKey());
            System.out.println("MRR " + ((MeanReciprocalRank) pairs.getValue()).getMrr());
        }

        return 0;




    }

    private static void addDoc(IndexWriter w, String title, String value) throws IOException {
        Document doc = new Document();
        doc.add(new Field("title", value, Field.Store.YES, Field.Index.ANALYZED));
        w.addDocument(doc);
    }

    private ArrayList<RecommendedItem> searchUserIndex(IndexSearcher userIndexSearcher, String currentText) throws ParseException, IOException {

        ArrayList<RecommendedItem> suggestedSemanticUsers = new ArrayList<RecommendedItem>();
        TopScoreDocCollector collector = TopScoreDocCollector.create(100, true);
        String searchString = currentText;
        if (searchString.trim().equals("")) {
            return suggestedSemanticUsers;
        }
        if (!searchString.contains(" ")) {
            return suggestedSemanticUsers;
        }
        QueryParser qp = new QueryParser(Version.LUCENE_31, "title", englishAnalyzer);
        Query q;
        try {
            q = (BooleanQuery) qp.parse(searchString);
            BooleanQuery.setMaxClauseCount(100000);
        } catch (ClassCastException ccex) {
            q = (TermQuery) qp.parse(searchString);
        }
        userIndexSearcher.search(q, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;
        for (ScoreDoc hit : hits) {
            int j = hit.doc;
            double similarity = hit.score;
            suggestedSemanticUsers.add(new RecommendedItem(String.valueOf(hit.doc), similarity));
        }
        return suggestedSemanticUsers;
    }

    private void addUserToMaps(String currentText, EventReceived er, RecommendedItem correctDeveloper, HashMap<String, String> userTextMap, HashMap<String, double[]> userDistributions, HashMap<String, double[]> userDistributions2, TopicInferencer inferencer, ArrayList<Pipe> pipeList) throws IOException {
        if (userTextMap.containsKey(correctDeveloper.getId())) {
            currentText += userTextMap.get(correctDeveloper.getId());
            userTextMap.remove(correctDeveloper.getId());
        } else {
            userTextMap.put(correctDeveloper.getId(), currentText);
        }

        //add the text to the index of users
        addDoc(userIndexWriter, correctDeveloper.getId(), currentText);
        userIndexWriter.commit();

        //analyze it and add it to the map of topic distributions
        userDistributions.remove(correctDeveloper.getId());
        userDistributions2.remove(correctDeveloper.getId());
        InstanceList instances2 = new InstanceList(new SerialPipes(pipeList));
        String data2[] = new String[1];
        data2[0] = currentText;
        instances2.addThruPipe(new StringArrayIterator(data2));
        userDistributions.put(correctDeveloper.getId(), inferencer.getSampledDistribution(instances2.get(0), 250, 10, 100));
        userDistributions2.put(correctDeveloper.getId(), inferencer.getSampledDistribution(instances2.get(0), 250, 10, 100));

    }

    private void prepareMetrics(HashMap<String, RocCurve> rocCurvesMap, HashMap<String, MeanReciprocalRank> mrrMap) {

        rocCurvesMap.put(ISSUES_RESOLVED_STR, new RocCurve());
//        rocCurvesMap.put("ItsTemporal", new RocCurve());
//        rocCurvesMap.put("MailingListTemporal", new RocCurve());
//        rocCurvesMap.put("ScmApiIntroduced", new RocCurve());
//        rocCurvesMap.put("ScmTemporal", new RocCurve());
//        rocCurvesMap.put("Semantic", new RocCurve());
        rocCurvesMap.put("TopicBased", new RocCurve());
        rocCurvesMap.put("TopicBased2", new RocCurve());

        mrrMap.put(ISSUES_RESOLVED_STR, new MeanReciprocalRank());
//        mrrMap.put("ItsTemporal", new MeanReciprocalRank());
//        mrrMap.put("MailingListTemporal", new MeanReciprocalRank());
//        mrrMap.put("ScmApiIntroduced", new MeanReciprocalRank());
//        mrrMap.put("ScmTemporal", new MeanReciprocalRank());
//        mrrMap.put("Semantic", new MeanReciprocalRank());
//        mrrMap.put("ScmTemporal", new MeanReciprocalRank());
//        mrrMap.put("Semantic", new MeanReciprocalRank());
        mrrMap.put("TopicBased", new MeanReciprocalRank());
        mrrMap.put("TopicBased2", new MeanReciprocalRank());

    }

    private void insertDataToMetrics(HashMap<String, MeanReciprocalRank> mrrMap, HashMap<String, RocCurve> rocCurvesMap, RecommendedItem correctDeveloper, ArrayList<RecommendedItem> suggestedItstDevelopers, ArrayList<RecommendedItem> suggestedSaiDevelopers, ArrayList<RecommendedItem> suggestedMltDevelopers, ArrayList<RecommendedItem> suggestedScmtDevelopers, ArrayList<RecommendedItem> suggestedIirmDevelopers, ArrayList<RecommendedItem> suggestedSemanticDevelopers, ArrayList<RecommendedItem> suggestedTopicDevelopers, ArrayList<RecommendedItem> suggestedTopic2Developers) {
//        rocCurvesMap.get("ItsTemporal").addSingleCurve(suggestedItstDevelopers, correctDeveloper);
//        mrrMap.get("ItsTemporal").addMrrValue(suggestedItstDevelopers, correctDeveloper);
//
//        rocCurvesMap.get("ScmApiIntroduced").addSingleCurve(suggestedSaiDevelopers, correctDeveloper);
//        mrrMap.get("ScmApiIntroduced").addMrrValue(suggestedSaiDevelopers, correctDeveloper);
//
//        rocCurvesMap.get("MailingListTemporal").addSingleCurve(suggestedMltDevelopers, correctDeveloper);
//        mrrMap.get("MailingListTemporal").addMrrValue(suggestedMltDevelopers, correctDeveloper);
//
//        rocCurvesMap.get("ScmTemporal").addSingleCurve(suggestedScmtDevelopers, correctDeveloper);
//        mrrMap.get("ScmTemporal").addMrrValue(suggestedScmtDevelopers, correctDeveloper);
//
//        rocCurvesMap.get("Semantic").addSingleCurve(suggestedSemanticDevelopers, correctDeveloper);
//        mrrMap.get("Semantic").addMrrValue(suggestedSemanticDevelopers, correctDeveloper);

        rocCurvesMap.get("TopicBased").addSingleCurve(suggestedTopicDevelopers, correctDeveloper);
        mrrMap.get("TopicBased").addMrrValue(suggestedTopicDevelopers, correctDeveloper);
        printListSuggestions("TopicBased", suggestedTopicDevelopers);

        rocCurvesMap.get("IssuesResolved").addSingleCurve(suggestedIirmDevelopers, correctDeveloper);
        mrrMap.get("IssuesResolved").addMrrValue(suggestedIirmDevelopers, correctDeveloper);
        printListSuggestions("IssuesResolved", suggestedIirmDevelopers);
        rocCurvesMap.get("TopicBased2").addSingleCurve(suggestedTopic2Developers, correctDeveloper);
        mrrMap.get("TopicBased2").addMrrValue(suggestedTopic2Developers, correctDeveloper);
        printListSuggestions("TopicBased2", suggestedTopic2Developers);
    }

    private HashMap<String, double[]> deepCopy(HashMap<String, double[]> userDistributions2) {
        HashMap<String, double[]> deepCopyMap = new HashMap<String, double[]>();
        Iterator it = userDistributions2.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            String userId = (String) pairs.getKey();
            double[] similarity = (double[]) pairs.getValue();
            deepCopyMap.put(userId, similarity);
        }
        return deepCopyMap;
    }

    private void printListSuggestions(String titleString, ArrayList<RecommendedItem> suggestedIirmDevelopers) {
        System.out.print(titleString + "[");
        for (RecommendedItem ri : suggestedIirmDevelopers) {
            System.out.print(" " + ri.getId() + ", ");
        }
        System.out.println("]");
    }

    private void addUserToCounts(RecommendedItem correctDeveloper, EventReceived er, ArrayList<RecommendedItem> IssuesResolvedDevelopersLst, ArrayList<RecommendedItem> EmailsSentDevelopersLst, ArrayList<RecommendedItem> OtherItsActivityDevelopersLst, ArrayList<RecommendedTemporalItem> IssuesResolvedTemporalDevelopersLst, ArrayList<RecommendedTemporalItem> EmailsSentTemporalDevelopersLst, ArrayList<RecommendedTemporalItem> OtherItsActivityTemporalDevelopersLst) {
        String devIdInt = correctDeveloper.getId();
        if (er.getStatus().equals("RSL")) {
            Double previousCount = 0.0;
            Iterator<RecommendedItem> IssuesResolvedDevelopersLstIt = IssuesResolvedDevelopersLst.iterator();
            while (IssuesResolvedDevelopersLstIt.hasNext()) {
                RecommendedItem ri = IssuesResolvedDevelopersLstIt.next();


                if (ri.getId().equals(devIdInt)) {
                    previousCount = ri.getSimilarity();
//                    IssuesResolvedDevelopersLst.remove(ri);
                    IssuesResolvedDevelopersLstIt.remove();
                }
            }
            IssuesResolvedDevelopersLst.add(new RecommendedItem(devIdInt, previousCount + 1.0));

            Iterator<RecommendedTemporalItem> IssuesResolvedTemporalDevelopersLstIt = IssuesResolvedTemporalDevelopersLst.iterator();
            while (IssuesResolvedTemporalDevelopersLstIt.hasNext()) {
                RecommendedTemporalItem rti = IssuesResolvedTemporalDevelopersLstIt.next();
                if (rti.getId().equals(devIdInt)) {
//                    IssuesResolvedTemporalDevelopersLst.remove(rti);
                    IssuesResolvedTemporalDevelopersLstIt.remove();
                }

            }
            IssuesResolvedTemporalDevelopersLst.add(new RecommendedTemporalItem(devIdInt, er.getDate()));

        } else if (er.getStatus().equals("email")) {
            Double previousCount = 0.0;
              Iterator<RecommendedItem> EmailsSentDevelopersLstIt = EmailsSentDevelopersLst.iterator();
            while (EmailsSentDevelopersLstIt.hasNext()) {
                RecommendedItem ri = EmailsSentDevelopersLstIt.next();
                if (ri.getId().equals(devIdInt)) {
                    previousCount = ri.getSimilarity();
//                    EmailsSentDevelopersLst.remove(ri);
                    EmailsSentDevelopersLstIt.remove();

                }
            }
            EmailsSentDevelopersLst.add(new RecommendedItem(devIdInt, previousCount + 1.0));
            Iterator<RecommendedTemporalItem> EmailsSentTemporalDevelopersLstIt = EmailsSentTemporalDevelopersLst.iterator();
            while (EmailsSentTemporalDevelopersLstIt.hasNext()) {
                RecommendedTemporalItem rti = EmailsSentTemporalDevelopersLstIt.next();
                if (rti.getId().equals(devIdInt)) {
//                    EmailsSentTemporalDevelopersLst.remove(rti);
                    EmailsSentTemporalDevelopersLstIt.remove();

                }
            }
            EmailsSentTemporalDevelopersLst.add(new RecommendedTemporalItem(devIdInt, er.getDate()));

        } else {

            Double previousCount = 0.0;
            Iterator<RecommendedItem> OtherItsActivityDevelopersLstIt = OtherItsActivityDevelopersLst.iterator();
            while (OtherItsActivityDevelopersLstIt.hasNext()) {
                RecommendedItem ri = OtherItsActivityDevelopersLstIt.next();
                if (ri.getId().equals(devIdInt)) {
                    previousCount = ri.getSimilarity();
//                    OtherItsActivityDevelopersLst.remove(ri);
                    OtherItsActivityDevelopersLstIt.remove();

                }
            }
            OtherItsActivityDevelopersLst.add(new RecommendedItem(devIdInt, previousCount + 1.0));
            Iterator<RecommendedTemporalItem> OtherItsActivityTemporalDevelopersLstIt = OtherItsActivityTemporalDevelopersLst.iterator();
            while (OtherItsActivityTemporalDevelopersLstIt.hasNext()) {
                RecommendedTemporalItem rti = OtherItsActivityTemporalDevelopersLstIt.next();
                if (rti.getId().equals(devIdInt)) {
//                    OtherItsActivityTemporalDevelopersLst.remove(rti);
                    OtherItsActivityTemporalDevelopersLstIt.remove();

                }
            }
            OtherItsActivityTemporalDevelopersLst.add(new RecommendedTemporalItem(devIdInt, er.getDate()));


        }
    }
}
