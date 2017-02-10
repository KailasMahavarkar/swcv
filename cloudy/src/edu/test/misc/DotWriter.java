package edu.test.misc;

import edu.cloudy.nlp.ParseOptions;
import edu.cloudy.nlp.SWCDocument;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.ItemPair;
import edu.cloudy.nlp.ranking.TFRankingAlgo;
import edu.cloudy.nlp.similarity.CosineCoOccurenceAlgo;
import edu.cloudy.nlp.similarity.SimilarityAlgo;
import edu.cloudy.utils.Logger;
import edu.cloudy.utils.WikipediaXMLReader;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author spupyrev 
 */
public class DotWriter
{

    public static void main(String argc[])
    {
        Logger.doLogging = false;
        new DotWriter().run();
    }

    private void run()
    {
        WikipediaXMLReader xmlReader = new WikipediaXMLReader("data/words");
        xmlReader.read();
        Iterator<String> texts = xmlReader.getTexts();

        SWCDocument doc = null;
        while (texts.hasNext())
        {
            doc = new SWCDocument(texts.next());
            doc.parse(new ParseOptions());
        }

        System.out.println("#words: " + doc.getWords().size());
        doc.weightFilter(150, new TFRankingAlgo());

        List<Word> words = new ArrayList<Word>();
        Map<ItemPair<Word>, Double> similarity = new HashMap<ItemPair<Word>, Double>();
        extractSimilarities(doc, words, similarity);

        writeDotFile("words.gv", words, similarity);
    }

    private void extractSimilarities(SWCDocument wordifier, List<Word> words, final Map<ItemPair<Word>, Double> similarity)
    {
        SimilarityAlgo coOccurenceAlgo = new CosineCoOccurenceAlgo();
        Map<ItemPair<Word>, Double> sim = coOccurenceAlgo.computeSimilarity(wordifier);

        for (Word w : wordifier.getWords())
            words.add(w);

        for (ItemPair<Word> wp : sim.keySet())
            similarity.put(wp, sim.get(wp));

    }

    private void writeDotFile(String filename, List<Word> words, Map<ItemPair<Word>, Double> similarity)
    {

        try
        {
            PrintWriter out = new PrintWriter(new File(filename));
            out.print("graph {\n");
            for (Word w : words)
                out.print("  \"" + w.word + "\";\n");

            for (int i = 0; i < words.size(); i++)
                for (int j = i + 1; j < words.size(); j++)
                {
                    double sim = similarity.get(new ItemPair<Word>(words.get(i), words.get(j)));
                    String sw = String.format("%.5f", sim);

                    out.print("  \"" + words.get(i).word + "\" -- \"" + words.get(j).word + "\" [len=" + sw + "];\n");
                }

            out.print("}\n");
            out.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

}
