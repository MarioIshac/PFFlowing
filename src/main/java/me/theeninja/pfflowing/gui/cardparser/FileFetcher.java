package me.theeninja.pfflowing.gui.cardparser;

import java.io.IOException;
import java.util.function.Consumer;

abstract class FileFetcher<T> {
    abstract String getHTMLOfFile(T file) throws IOException;
    abstract void feedFetchedFile(Consumer<T> fileConsumerCallback) throws IOException;

    protected void feedFetchedHTML(Consumer<String> htmlConsumerCallback) throws IOException {
        feedFetchedFile(fetchedFile -> {
            try {
                String html = getHTMLOfFile(fetchedFile);
                System.out.println(html);

                htmlConsumerCallback.accept(html);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
