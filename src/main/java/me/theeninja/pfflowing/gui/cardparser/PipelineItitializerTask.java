package me.theeninja.pfflowing.gui.cardparser;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import javafx.concurrent.Task;

import java.util.Properties;

public class PipelineItitializerTask extends Task<StanfordCoreNLP> {
    /**
     * {@inheritDoc}
     */
    @Override
    protected StanfordCoreNLP call() throws Exception {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
        props.setProperty("ner.useSUTime", "0");
        return new StanfordCoreNLP(props);
    }
}
