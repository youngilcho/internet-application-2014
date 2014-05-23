// BSD License (http://www.galagosearch.org/license)

package org.galagosearch.exercises.zipf;

import java.io.IOException;
import org.galagosearch.core.parse.TagTokenizer;
import org.galagosearch.core.parse.UniversalParser;
import org.galagosearch.core.parse.WordCountReducer;
import org.galagosearch.core.parse.WordCounter;
import org.galagosearch.core.tools.BuildIndex;
import org.galagosearch.core.types.DocumentSplit;
import org.galagosearch.core.types.WordCount;
import org.galagosearch.tupleflow.Parameters;
import org.galagosearch.tupleflow.StandardStep;
import org.galagosearch.tupleflow.TextWriter;
import org.galagosearch.tupleflow.Utility;
import org.galagosearch.tupleflow.execution.ConnectionAssignmentType;
import org.galagosearch.tupleflow.execution.ConnectionPointType;
import org.galagosearch.tupleflow.execution.InputStep;
import org.galagosearch.tupleflow.execution.Job;
import org.galagosearch.tupleflow.execution.OutputStep;
import org.galagosearch.tupleflow.execution.Stage;
import org.galagosearch.tupleflow.execution.StageConnectionPoint;
import org.galagosearch.tupleflow.execution.Step;
import org.galagosearch.tupleflow.InputClass;
import org.galagosearch.tupleflow.OutputClass;
import org.galagosearch.tupleflow.execution.ErrorStore;
import org.galagosearch.tupleflow.execution.JobExecutor;
import org.galagosearch.tupleflow.execution.Verified;

// you should modify package path of ZipfCount that you made
import org.galagosearch.exercises.tokenizer.TokenizerExample;
import org.galagosearch.exercises.types.ZipfCount;

/**
 * This is a sample solution for Exercise 5.16 in
 * "Search Engines: Information Retrieval in Practice".
 *
 * <p>This TupleFlow pipeline has 5 stages.  The first, inputSplit, is borrowed from
 * BuildIndex.  It sends information about the input files into the pipeline.</p>
 * 
 * <p>The first MapReduce is the wordCount/reduceCounts pair.  The wordCount stage
 * parses and counts words in each document, while the reduceCounts stage gathers
 * that data into total word counts for the corpus.
 * data for each word.</p>
 *
 * <p>The second MapReduce is the invertByCount/zipfReduce pair.  The invertByCount
 * stage sorts the incoming word count data by count.  The zipfReduce stage then
 * groups unique words together to find out how many words appear once in the corpus,
 * etc.</p>
 *
 * <p>This solution took me about two hours to write, including writing the TextWriter
 * class and fixing some toolkit bugs.  The exercise requires students to dive deep into
 * the Galago/TupleFlow system so I expect it will be a challenging assignment.</p>
 *
 * <p>Note that the ZipfCount.galagotype file is also a part of this solution.</p>
 *
 * @author trevor
 */
public class ZipfsLaw {
    /**
     * Parses input text, then counts the word tokens.  The output is a
     * stream of WordCount tokens sorted by word.
     * 
     * @return A stage description for the wordCount stage.
     */
    public Stage getWordCountStage() {
        Stage stage = new Stage("wordCount");

        stage.add(new StageConnectionPoint(
                ConnectionPointType.Input,
                "splits", new DocumentSplit.FileNameStartKeyOrder()));
        stage.add(new StageConnectionPoint(
                ConnectionPointType.Output,
                "wordCounts", new WordCount.WordOrder()));

        stage.add(new InputStep("splits"));
        stage.add(new Step(UniversalParser.class));
        // change here to use your tokenizer
        // stage.add(new Step(TagTokenizer.class)) : use default tokenizer
        // stage.add(new Step(TokenizerExample.class)) : use your tokenizer
//        stage.add(new Step(TagTokenizer.class));
        stage.add(new Step(TokenizerExample.class));
        stage.add(new Step(WordCounter.class));
        stage.add(Utility.getSorter(new WordCount.WordOrder(), WordCountReducer.class));
        stage.add(new OutputStep("wordCounts"));

        return stage;
    }

    /**
     * Gathers wordCount data from all the wordCounts stages
     * and computes count totals for each word.
     * 
     * @return A stage description for the reduceCounts stage.
     */
    public Stage getReduceCountsStage() {
        Stage stage = new Stage("reduceCounts");

        stage.add(new StageConnectionPoint(
                ConnectionPointType.Input,
                "wordCounts", new WordCount.WordOrder()));
        stage.add(new StageConnectionPoint(
                ConnectionPointType.Output,
                "reducedCounts", new WordCount.WordOrder()));

        stage.add(new InputStep("wordCounts"));
        stage.add(new Step(WordCountReducer.class));
        stage.add(new OutputStep("reducedCounts"));

        return stage;
    }

    /**
     * Inverts wordCount data so that it is sorted by count, not by word.
     * The output is in ZipfCount objects, produced by the ZipfCounter
     * class.
     *
     * @return A stage description for the invertByCount stage.
     */
    public Stage getInvertByCountStage() {
        Stage stage = new Stage("invertByCount");

        stage.add(new StageConnectionPoint(
                ConnectionPointType.Input,
                "reducedCounts", new WordCount.WordOrder()));
        stage.add(new StageConnectionPoint(
                ConnectionPointType.Output,
                "zipfCounts", new ZipfCount.OccurrenceCountOrder()));

        stage.add(new InputStep("reducedCounts"));
        stage.add(new Step(ZipfCounter.class));
        stage.add(Utility.getSorter(new ZipfCount.OccurrenceCountOrder()));
        stage.add(new OutputStep("zipfCounts"));

        return stage;
    }

    /**
     * This final stage combines the ZipfCount tuples together and
     * computes final occurrence counts.  The data is written to a
     * text file with the TextWriter class.
     *
     * @return A stage description for the zipfReduce stage.
     */
    public Stage getZipfReduceStage(String outputFile) {
        Stage stage = new Stage("zipfReduce");

        stage.add(new StageConnectionPoint(
                ConnectionPointType.Input,
                "zipfCounts", new ZipfCount.OccurrenceCountOrder()));

        stage.add(new InputStep("zipfCounts"));
        stage.add(new Step(ZipfReducer.class));
        Parameters p = new Parameters();
        p.add("class", ZipfCount.class.getName());
        p.add("filename", outputFile);
        stage.add(new Step(TextWriter.class, p));

        return stage;
    }

    /**
     * Builds the entire job.
     */
    public Job getZipfJob(String outputFile, String[] inputs) throws IOException {
        Job job = new Job();
        BuildIndex b = new BuildIndex();

        job.add(b.getSplitStage(inputs));
        job.add(getWordCountStage());
        job.add(getReduceCountsStage());
        job.add(getInvertByCountStage());
        job.add(getZipfReduceStage(outputFile));
        
        job.connect("inputSplit", "wordCount", ConnectionAssignmentType.Each);
        job.connect("wordCount", "reduceCounts", ConnectionAssignmentType.Each);
        job.connect("reduceCounts", "invertByCount", ConnectionAssignmentType.Each);
        job.connect("invertByCount", "zipfReduce", ConnectionAssignmentType.Combined);

        return job;
    }

    /**
     * Converts WordCount tuples into ZipfCount tuples.  A WordCount tuple
     * stores (word, count).  However, we are interested in the number of
     * words that occur the same number of times in the corpus.  We change
     * (word, count) to (1, count).  The ZipfReducer then combines this data
     * together.
     */
    @InputClass(className = "org.galagosearch.core.types.WordCount")
    @OutputClass(className = "org.galagosearch.exercises.types.ZipfCount")
    @Verified
    public static class ZipfCounter extends StandardStep<WordCount, ZipfCount> {
        @Override
        public void process(WordCount object) throws IOException {
            processor.process(new ZipfCount(1, object.count));
        }
    }

    /**
     * This is a combination class for ZipfCount tuples.  We get lots of
     * (1, count) tuples as input, sorted by occurrence count.  The output
     * is (total, count).
     */
    @InputClass(className = "org.galagosearch.exercises.types.ZipfCount")
    @OutputClass(className = "org.galagosearch.exercises.types.ZipfCount")
    @Verified
    public static class ZipfReducer extends StandardStep<ZipfCount, ZipfCount> {
        long lastOccurrenceCount = -1;
        long totalUniqueWords = 0;
        @Override
        public void process(ZipfCount object) throws IOException {
            // If this is a different kind of tuple than we've seen before, write
            // a ZipfCount tuple describing the last tuple group.
            if (lastOccurrenceCount != object.occurrenceCount)
                flush();
            lastOccurrenceCount = object.occurrenceCount;
            totalUniqueWords += object.uniqueWords;
        }

        /**
         * Flush a ZipfCount tuple.  This is called either when the pipeline is
         * done, or when we're starting to see a new set of tuples in the input
         * with different count values.
         */
        private void flush() throws IOException {
            if (lastOccurrenceCount >= 0) {
                processor.process(new ZipfCount(totalUniqueWords, lastOccurrenceCount));
                totalUniqueWords = 0;
            }
        }

        /**
         * This writes out the final tuple, then tells the remaining the
         * TupleFlow steps to quit.
         * 
         * @throws java.io.IOException
         */
        @Override
        public void close() throws IOException {
            flush();
            processor.close();
        }
    }

    /**
     * Usage: java ZipfsLaw.class /tmp/output-counts /tmp/wiki-small.corpus
     */
    public static void main(String[] args) throws Exception {
        String outputFile = args[0];
        String[] inputFiles = Utility.subarray(args, 1);

        Job job = new ZipfsLaw().getZipfJob(outputFile, inputFiles);
        ErrorStore store = new ErrorStore();
        JobExecutor.runLocally(job, store, false);

        if (store.hasStatements())
            System.err.println(store.toString());
    }
}
