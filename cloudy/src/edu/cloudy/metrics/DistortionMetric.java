package edu.cloudy.metrics;

import edu.cloudy.geom.SWCPoint;
import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.layout.LayoutResult;
import edu.cloudy.layout.WordGraph;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.ItemPair;

import java.util.List;
import java.util.Map;

/**
 * May 3, 2013
 * computes average distortion: dist(u,v)/dissimilarity(u,v)
 */
public class DistortionMetric implements QualityMetric
{
    @Override
    public double getValue(WordGraph wordGraph, LayoutResult layout)
    {
        List<Word> words = wordGraph.getWords();
        Map<ItemPair<Word>, Double> similarity = wordGraph.getSimilarity();
        
        if (words.isEmpty())
            return 0;

        double[][] matrixDissimilarity = getDissimilarityMatrix(words, similarity, layout);
        double[][] matrixGeomDistance = getGeomDistance(words, similarity, layout);

        double dist = computeDistortion(matrixDissimilarity, matrixGeomDistance);
        return (dist + 1.0) / 2.0;
    }

    private double computeDistortion(double[][] A, double[][] B)
    {
        assert (A.length == B.length);
        int n = A.length;

        double avgA = average(A);
        double devA = deviation(A, avgA);
        double avgB = average(B);
        double devB = deviation(B, avgB);

        assert (avgA > 0.0 && avgB > 0.0);

        double distortion = 0, cnt = 0;
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
            {
                if (i == j)
                    continue;

                if (A[i][j] == -1 || B[i][j] == -1)
                    continue;

                double deltaA = (A[i][j] - avgA) / devA;
                double deltaB = (B[i][j] - avgB) / devB;

                distortion += deltaA * deltaB;
                assert (!Double.isNaN(distortion));
                cnt++;
            }

        return distortion / cnt;
    }

    private double average(double[][] A)
    {
        double sum = 0;
        double cnt = 0;

        for (int i = 0; i < A.length; i++)
            for (int j = 0; j < A[i].length; j++)
            {
                if (A[i][j] == -1)
                    continue;
                sum += A[i][j];
                cnt++;
            }

        if (cnt > 0)
            sum /= cnt;
        return sum;
    }

    private double deviation(double[][] A, double avgA)
    {
        double sum = 0;
        double cnt = 0;

        for (int i = 0; i < A.length; i++)
            for (int j = 0; j < A[i].length; j++)
            {
                if (A[i][j] == -1)
                    continue;

                sum += (A[i][j] - avgA) * (A[i][j] - avgA);
                cnt++;
            }

        if (cnt > 0)
            sum /= cnt;

        return Math.sqrt(sum);
    }

    private double[][] getDissimilarityMatrix(List<Word> words, Map<ItemPair<Word>, Double> similarity, LayoutResult algo)
    {
        int n = words.size();

        double[][] matrix = new double[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
            {
                ItemPair<Word> wp = new ItemPair<Word>(words.get(i), words.get(j));
                if (similarity.containsKey(wp))
                    matrix[i][j] = 1.0 - similarity.get(wp);
                else
                    matrix[i][j] = -1;
            }

        return matrix;
    }

    private double[][] getGeomDistance(List<Word> words, Map<ItemPair<Word>, Double> similarity, LayoutResult algo)
    {
        int n = words.size();

        double[][] matrix = new double[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
            {
                matrix[i][j] = distance2(words.get(i), words.get(j), algo);
            }

        return matrix;
    }

    @SuppressWarnings("unused")
    private double distance(Word first, Word second, LayoutResult algo)
    {
        SWCRectangle rect1 = algo.getWordPosition(first);
        SWCRectangle rect2 = algo.getWordPosition(second);

        //TODO: maybe it is better to compute min distance between boundaries
        SWCPoint p1 = new SWCPoint(rect1.getCenterX(), rect1.getCenterY());
        SWCPoint p2 = new SWCPoint(rect2.getCenterX(), rect2.getCenterY());

        return p1.distance(p2);
    }

    private double distance2(Word first, Word second, LayoutResult algo)
    {
        SWCRectangle rect1 = algo.getWordPosition(first);
        SWCRectangle rect2 = algo.getWordPosition(second);

        double dist = Double.POSITIVE_INFINITY;
        SWCPoint[] corners1 = getCorners(rect1);
        SWCPoint[] corners2 = getCorners(rect2);
        for (SWCPoint p1 : corners1)
            for (SWCPoint p2 : corners2)
                dist = Math.min(dist, p1.distance(p2));

        return dist;
    }

    private SWCPoint[] getCorners(SWCRectangle rect)
    {

        SWCPoint[] res = new SWCPoint[4];
        if (rect == null)
            return res;
        res[0] = new SWCPoint(rect.getMinX(), rect.getMinY());
        res[1] = new SWCPoint(rect.getMaxX(), rect.getMinY());
        res[2] = new SWCPoint(rect.getMinX(), rect.getMaxY());
        res[3] = new SWCPoint(rect.getMaxX(), rect.getMaxY());
        return res;
    }

}
