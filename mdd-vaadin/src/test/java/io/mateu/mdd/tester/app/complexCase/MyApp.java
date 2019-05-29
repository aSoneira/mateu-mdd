package io.mateu.mdd.tester.app.complexCase;



import io.mateu.mdd.core.app.AbstractArea;
import io.mateu.mdd.core.app.BaseMDDApp;
import io.mateu.mdd.core.app.Searcher;

import java.util.Arrays;
import java.util.List;

public class MyApp extends BaseMDDApp {
    public String getName() {
        return "My Appp";
    }

    public List<AbstractArea> buildAreas() {
        return Arrays.asList(new UseCasesArea(), new ShowcaseArea(), new PrivateArea());
    }

    @Override
    public Searcher getSearcher() {
        return new AppSearcher();
    }
}
