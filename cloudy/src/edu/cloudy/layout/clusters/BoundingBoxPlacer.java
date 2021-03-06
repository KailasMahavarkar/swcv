package edu.cloudy.layout.clusters;

import edu.cloudy.geom.BoundingBoxGenerator;
import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.layout.LayoutResult;
import edu.cloudy.layout.clusters.RectanglePacker.Rectangle;
import edu.cloudy.nlp.Word;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author spupyrev
 * May 3, 2013
 */
public class BoundingBoxPlacer implements WordPlacer
{
    private List<Word> words;
    private Map<Word, SWCRectangle> wordPositions = new HashMap<Word, SWCRectangle>();
    private List<? extends LayoutResult> singlePlacers;

    private BoundingBoxGenerator bbGenerator;
    private double weightToAreaFactor;

    private double MAX_WIDTH;
    private double MAX_HEIGHT;

    public BoundingBoxPlacer(List<Word> words, List<? extends LayoutResult> singlePlacers, double weightToAreaFactor, BoundingBoxGenerator bbGenerator)
    {
        this.words = words;
        this.singlePlacers = singlePlacers;
        this.bbGenerator = bbGenerator;
        this.weightToAreaFactor = weightToAreaFactor;

        run();
    }

    @Override
    public SWCRectangle getRectangleForWord(Word w)
    {
        assert (wordPositions.containsKey(w));
        return wordPositions.get(w);
    }

    @Override
    public boolean contains(Word w)
    {
        return true;
    }

    private void run()
    {
        //get the groups of words: stars, cycles etc
        List<Cluster> clusters = createClusters();

        //compute MAX_WIDTH and MAX_HEIGHT
        computeCloudDimensions(clusters);

        double scale = 1.05;
        //try to layout words
        while (!doPacking(clusters))
        {
            //increase cloud dimensions
            MAX_WIDTH *= scale;
            MAX_HEIGHT *= scale;
        }

        doPacking(clusters);
    }

    private boolean doPacking(List<Cluster> clusters)
    {
        //TODO: what should be these values??
        RectanglePacker<Cluster> packer = new RectanglePacker<Cluster>((int)MAX_WIDTH, (int)MAX_HEIGHT, 0);

        for (Cluster cluster : clusters)
        {
            SWCRectangle bbox = cluster.getBoundingBox();
            RectanglePacker.Rectangle res = packer.insert((int)bbox.getWidth(), (int)bbox.getHeight(), cluster);

            //unable to pack rectangles
            if (res == null)
                return false;
        }

        //fill out wordPositions
        for (Cluster cluster : clusters)
        {
            SWCRectangle bbox = cluster.getBoundingBox();
            Rectangle rect = packer.findRectangle(cluster);

            for (Word w : cluster.wordPositions.keySet())
            {
                SWCRectangle r = cluster.wordPositions.get(w);
                wordPositions.put(w, new SWCRectangle(r.getX() + rect.x - bbox.getX(), r.getY() + rect.y - bbox.getY(), r.getWidth(), r.getHeight()));
            }
        }

        return true;
    }

    private List<Cluster> createClusters()
    {
        List<Cluster> result = new ArrayList<Cluster>();
        for (int i = 0; i < singlePlacers.size(); i++)
            result.add(new Cluster());

        for (Word w : words)
        {
            SWCRectangle rect = null;
            for (int i = 0; i < singlePlacers.size(); i++)
            {
                SWCRectangle tmp = singlePlacers.get(i).getWordPosition(w);
                if (tmp != null)
                {
                    result.get(i).wordPositions.put(w, tmp);
                    rect = tmp;
                    break;
                }
            }

            //create its own cluster
            if (rect == null)
            {
                Cluster c = new Cluster();
                c.wordPositions.put(w, bbGenerator.getBoundingBox(w, weightToAreaFactor * w.weight));
                result.add(c);
            }
        }

        return result;
    }

    private void computeCloudDimensions(List<Cluster> clusters)
    {
        double area = 0;
        for (Cluster c : clusters)
        {
            SWCRectangle bb = c.getBoundingBox();
            area += bb.getHeight() * bb.getWidth();
        }

        double aspectRatio = 4.0 / 3.0;
        MAX_HEIGHT = Math.sqrt(1.5 * area / aspectRatio);
        MAX_WIDTH = MAX_HEIGHT * aspectRatio;
    }

    private class Cluster
    {
        private Map<Word, SWCRectangle> wordPositions = new HashMap<Word, SWCRectangle>();

        public SWCRectangle getBoundingBox()
        {
            SWCRectangle bb = new SWCRectangle();
            for (SWCRectangle r : wordPositions.values())
                bb.add(r);

            return bb;
        }

        public String toString()
        {
            StringBuffer sb = new StringBuffer();
            for (Word w : wordPositions.keySet())
                sb.append(" " + w.word);
            return sb.toString();
        }
    }
}
