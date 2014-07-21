package com.freedomoss.ccompare;

import com.wcohen.ss.AbstractStringDistance;
import com.wcohen.ss.api.StringWrapper;

public class MatcherStringDistance extends AbstractStringDistance {

    @Override
    public double score(StringWrapper s, StringWrapper t) {
        String s1 = s.unwrap();
        String s2 = t.unwrap();

        Main main = new Main();
        int compare = main.compare(s1, s2);
        return (double) compare / 100;
    }

    @Override
    public String explainScore(StringWrapper s, StringWrapper t) {
        return "No explain";
    }
}
