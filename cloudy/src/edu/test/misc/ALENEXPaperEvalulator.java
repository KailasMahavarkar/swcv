package edu.test.misc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.cloudy.layout.ContextPreservingAlgo;
import edu.cloudy.layout.CycleCoverAlgo;
import edu.cloudy.layout.InflateAndPushAlgo;
import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.layout.LayoutResult;
import edu.cloudy.layout.MDSAlgo;
import edu.cloudy.layout.SeamCarvingAlgo;
import edu.cloudy.layout.StarForestAlgo;
import edu.cloudy.layout.WordGraph;
import edu.cloudy.layout.WordleAlgo;
import edu.cloudy.metrics.AdjacenciesMetric;
import edu.cloudy.metrics.AspectRatioMetric;
import edu.cloudy.metrics.DistortionMetric;
import edu.cloudy.metrics.MaxPlanarSubgraphMetric;
import edu.cloudy.metrics.SpaceMetric;
import edu.cloudy.metrics.StressMetric;
import edu.cloudy.metrics.TotalWeightMetric;
import edu.cloudy.metrics.UniformAreaMetric;
import edu.cloudy.nlp.ItemPair;
import edu.cloudy.nlp.ParseOptions;
import edu.cloudy.nlp.SWCDocument;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.ranking.RankingAlgo;
import edu.cloudy.nlp.ranking.TFRankingAlgo;
import edu.cloudy.nlp.similarity.CosineCoOccurenceAlgo;
import edu.cloudy.nlp.similarity.SimilarityAlgo;
import edu.cloudy.utils.Logger;
import edu.cloudy.utils.WikipediaXMLReader;

@SuppressWarnings("all")
public class ALENEXPaperEvalulator
{
    public static String[] FILES_WIKI = {
            "data/wiki_00",
            "data/wiki_01",
            "data/wiki_02",
            "data/wiki_03",
            "data/wiki_05" };
    public static String[] FILES_ALENEX = { "data/alenex_papers" };

    public static void main(String[] args)
    {
        Logger.doLogging = false;

        List<SWCDocument> allDocuments = readDocuments(FILES_WIKI);
        testRuntime(allDocuments, new TFRankingAlgo(), new CosineCoOccurenceAlgo());

        /*List<WCVDocument> allDocuments = readDocuments(FILES_WIKI);
        System.out.println("=====WIKI TFIDF Cosine=======");
        testRuntime(allDocuments, new TFIDFRankingAlgo(allDocuments), new CosineCoOccurenceAlgo());
        System.out.println("=====WIKI TFIDF Jaccard=======");
        testRuntime(allDocuments, new TFIDFRankingAlgo(allDocuments), new JaccardCoOccurenceAlgo());
        System.out.println("=====WIKI TF Cosine=======");
        testRuntime(allDocuments, new TFRankingAlgo(), new CosineCoOccurenceAlgo());
        System.out.println("=====WIKI TF Jaccard=======");
        testRuntime(allDocuments, new TFRankingAlgo(), new JaccardCoOccurenceAlgo());
        System.out.println("=====WIKI Lex Cosine=======");
        testRuntime(allDocuments, new LexRankingAlgo(), new CosineCoOccurenceAlgo());
        System.out.println("=====WIKI Lex Jaccard=======");
        testRuntime(allDocuments, new LexRankingAlgo(), new JaccardCoOccurenceAlgo());*/

        /*List<WCVDocument> allDocuments = readDocuments(FILES_ALENEX);
        System.out.println("=====ALENEX TFIDF Cosine=======");
        testRuntime(allDocuments, new TFIDFRankingAlgo(allDocuments), new CosineCoOccurenceAlgo());
        System.out.println("=====ALENEX TFIDF Jaccard=======");
        testRuntime(allDocuments, new TFIDFRankingAlgo(allDocuments), new JaccardCoOccurenceAlgo());
        System.out.println("=====ALENEX TF Cosine=======");
        testRuntime(allDocuments, new TFRankingAlgo(), new CosineCoOccurenceAlgo());
        System.out.println("=====ALENEX TF Jaccard=======");
        testRuntime(allDocuments, new TFRankingAlgo(), new JaccardCoOccurenceAlgo());
        System.out.println("=====ALENEX Lex Cosine=======");
        testRuntime(allDocuments, new LexRankingAlgo(), new CosineCoOccurenceAlgo());
        System.out.println("=====ALENEX Lex Jaccard=======");
        testRuntime(allDocuments, new LexRankingAlgo(), new JaccardCoOccurenceAlgo());*/

        /*List<WCVDocument> allDocuments = readDocuments(FILES_ALENEX);
        System.out.println("=====ALENEX TFIDF Random=======");
        testRuntime(allDocuments, new TFIDFRankingAlgo(allDocuments), new RandomSimilarityAlgo());
        System.out.println("=====ALENEX TF Random=======");
        testRuntime(allDocuments, new TFRankingAlgo(), new RandomSimilarityAlgo());
        System.out.println("=====ALENEX Lex Random=======");
        testRuntime(allDocuments, new LexRankingAlgo(), new RandomSimilarityAlgo());*/
    }

    private static void testRuntime(List<SWCDocument> allDocuments, RankingAlgo rankingAlgo, SimilarityAlgo similarityAlgo)
    {
        RunResult.outputLegend();
        for (int wc = 150; wc <= 150; wc += 50)
        {
            List<RunResult> wordleResult = new ArrayList<RunResult>();
            List<RunResult> cpResults = new ArrayList<RunResult>();
            List<RunResult> seamResult = new ArrayList<RunResult>();
            List<RunResult> inflateResult = new ArrayList<RunResult>();
            List<RunResult> starsResult = new ArrayList<RunResult>();
            List<RunResult> cyclesResult = new ArrayList<RunResult>();
            List<RunResult> mdsResult = new ArrayList<RunResult>();

            for (int i = 0; i < allDocuments.size(); i++)
            {
                SWCDocument document = new SWCDocument(allDocuments.get(i).getText());
                document.parse(new ParseOptions());

                document.weightFilter(wc, rankingAlgo);

                // OK, give me the similarity
                SimilarityAlgo coOccurenceAlgo = similarityAlgo;
                Map<ItemPair<Word>, Double> similarity = coOccurenceAlgo.computeSimilarity(document);
                WordGraph wordGraph = new WordGraph(document.getWords(), similarity);

                int runCount = 3;
                for (int j = 0; j < runCount; j++)
                {
                    wordleResult.add(computeMetrics(wordGraph, new WordleAlgo()));
                    cpResults.add(computeMetrics(wordGraph, new ContextPreservingAlgo()));
                    if (i % 5 == 0)
                        seamResult.add(computeMetrics(wordGraph, new SeamCarvingAlgo()));
                    inflateResult.add(computeMetrics(wordGraph, new InflateAndPushAlgo()));
                    starsResult.add(computeMetrics(wordGraph, new StarForestAlgo()));
                    cyclesResult.add(computeMetrics(wordGraph, new CycleCoverAlgo()));
                    mdsResult.add(computeMetrics(wordGraph, new MDSAlgo()));
                }

                takeAverage(wordleResult, runCount);
                takeAverage(cpResults, runCount);
                if (i % 5 == 0)
                    takeAverage(seamResult, runCount);
                takeAverage(inflateResult, runCount);
                takeAverage(starsResult, runCount);
                takeAverage(cyclesResult, runCount);
                takeAverage(mdsResult, runCount);
            }

            //outputResult(wordleResult, "RANDOM", wc);
            //outputResult(cpResults, "CPWCV", wc);
            //outputResult(seamResult, "SEAM", wc);
            //outputResult(inflateResult, "INFLATE", wc);
            //outputResult(starsResult, "STAR", wc);
            //outputResult(cyclesResult, "CYCLE", wc);
            //outputResult(mdsResult, "MDS", wc);

            outputDetailedResult(wordleResult, "RANDOM", wc);
            outputDetailedResult(cpResults, "CPWCV", wc);
            outputDetailedResult(seamResult, "SEAM", wc);
            outputDetailedResult(inflateResult, "INFLATE", wc);
            outputDetailedResult(starsResult, "STAR", wc);
            outputDetailedResult(cyclesResult, "CYCLE", wc);
            outputDetailedResult(mdsResult, "MDS", wc);
        }
    }

    public static List<SWCDocument> readDocuments(String[] files)
    {
        List<SWCDocument> docs = new ArrayList<SWCDocument>();

        for (String filename : files)
        {
            Iterator<String> texts;
            WikipediaXMLReader xmlReader = new WikipediaXMLReader(filename);
            xmlReader.read();
            texts = xmlReader.getTexts();

            while (texts.hasNext())
            {
                String text = texts.next();

                SWCDocument wordifier = new SWCDocument(text);
                wordifier.parse(new ParseOptions());

                if (wordifier.getWords().size() < 200)
                {
                    // skipping texts with less than 200 words
                    continue;
                }

                docs.add(wordifier);

                if (docs.size() > 50)
                    break;
            }
        }

        return docs;
    }

    private static void outputResult(List<RunResult> algoResult, String alg, int wc)
    {
        System.out.printf(alg + " %d", wc);
        RunResult avg = RunResult.computeAverage(algoResult);
        avg.output();
    }

    private static void outputDetailedResult(List<RunResult> algoResult, String alg, int wc)
    {
        for (RunResult rr : algoResult)
        {
            System.out.printf(alg + " %d", wc);
            rr.output();
        }
    }

    private static void takeAverage(List<RunResult> algResult, int runCount)
    {
        assert (algResult.size() >= runCount);
        int n = algResult.size();
        List<RunResult> list = new ArrayList<RunResult>();
        for (int i = n - 1; i >= n - runCount; i--)
        {
            list.add(algResult.get(i));
            algResult.remove(i);
        }

        algResult.add(RunResult.computeAverage(list));

    }

    private static RunResult computeMetrics(WordGraph wordGraph, LayoutAlgo algo)
    {
        //System.out.print("running " + algo + " ...");
        long startTime = System.currentTimeMillis();
        LayoutResult layout = null;
        try
        {
            layout = algo.layout(wordGraph);
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
            throw new RuntimeException(e);
        }

        //System.out.println("done");

        long stopTime = System.currentTimeMillis();
        RunResult result = new RunResult();
        result.runningTime = (stopTime - startTime) / 1000.0;
        result.uniformity = new UniformAreaMetric().getValue(wordGraph, layout);
        result.aspectRatio = new AspectRatioMetric().getValue(wordGraph, layout);
        result.distortion = new DistortionMetric().getValue(wordGraph, layout);
        result.stress = new StressMetric().getValue(wordGraph, layout);
        result.adjacencies = new AdjacenciesMetric().getValue(wordGraph, layout) / new TotalWeightMetric().getValue(wordGraph, layout);
        result.compactnessBB = new SpaceMetric(false).getValue(wordGraph, layout);
        result.compactnessCH = new SpaceMetric(true).getValue(wordGraph, layout);
        result.approxomation = new AdjacenciesMetric().getValue(wordGraph, layout) / new MaxPlanarSubgraphMetric().getValue(wordGraph, layout);

        return result;
    }

    static class RunResult
    {
        double runningTime = 0;
        double adjacencies = 0;
        double compactnessBB = 0;
        double compactnessCH = 0;
        double distortion = 0;
        double stress = 0;
        double aspectRatio = 0;
        double uniformity = 0;
        double approxomation = 0;

        public static RunResult computeAverage(List<RunResult> list)
        {
            RunResult r = new RunResult();

            for (RunResult item : list)
            {
                r.add(item);
            }

            if (list.size() > 0)
                r.scale(1.0 / list.size());

            return r;
        }

        public void output()
        {
            System.out.printf(" %.4f  %.4f  %.4f  %.4f  %.4f  %.4f  %.4f  %.4f\n", runningTime, adjacencies, compactnessBB, compactnessCH, distortion, stress, aspectRatio, uniformity);
            //System.out.printf(" %.4f   %.4f\n", distortion, stress);
        }

        public static void outputLegend()
        {
            System.out.printf("#alg_name #word_count #time #adj #comp_bb #comp_ch #distortion #stress #aspect_ratio #uniformity\n");
        }

        public void add(RunResult o)
        {
            runningTime += o.runningTime;
            adjacencies += o.adjacencies;
            compactnessBB += o.compactnessBB;
            compactnessCH += o.compactnessCH;
            distortion += o.distortion;
            stress += o.stress;
            aspectRatio += o.aspectRatio;
            uniformity += o.uniformity;
            approxomation += o.approxomation;
        }

        public void scale(double s)
        {
            runningTime *= s;
            adjacencies *= s;
            compactnessBB *= s;
            compactnessCH *= s;
            distortion *= s;
            stress *= s;
            aspectRatio *= s;
            uniformity *= s;
            approxomation *= s;
        }
    }
}
