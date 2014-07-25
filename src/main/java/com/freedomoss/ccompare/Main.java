package com.freedomoss.ccompare;

import com.freedomoss.ccompare.util.DamerauLevenshtein;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import com.google.common.base.Optional;

public class Main {

    public static final double SYNONYM_FACTOR = 0.8;
    private static final Tokenizer TOKENIZER = new Tokenizer();
    private static final List<String> stopWords;
    private static final Map<String, String> synonyms = Maps.newHashMap();

    static {
        URL stoplistDic = Resources.getResource("stoplist.dic");
        URL synonymsDic = Resources.getResource("synonyms.dic");
        try {
            stopWords = Resources.readLines(stoplistDic, Charset.defaultCharset());
            List<String> file = Resources.readLines(synonymsDic, Charset.defaultCharset());
            for (String line : file) {
                String[] leftRight = line.split("=");
                synonyms.put(leftRight[1], leftRight[0]);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public int compare(String company1, String company2) {

        Company c1 = TOKENIZER.tokenize(company1);
        Company c2 = TOKENIZER.tokenize(company2);

        company1 = getNormalizedTitle(c1);
        company2 = getNormalizedTitle(c2);

//        for (String stopWord : stopWords) {
//            company1 = replace(company1, stopWord, " ");
//            company2 = replace(company2, stopWord, " ");
//        }

        for (Map.Entry<String, String> synonym : synonyms.entrySet()) {
            company1 = replace(company1, synonym.getKey(), synonym.getValue());
            company2 = replace(company2, synonym.getKey(), synonym.getValue());
        }


        int fullComparisonConfidence = getFullComparisonConfidence(company1, company2);

        int res = 100;
        BusinessEntityMatch beMatch = businessEntitiesMatch(c1.businessEntity, c2.businessEntity);

        if (beMatch.type.equals(BusinessEntityMatch.Type.NONE)) {
            res = res - 50;
            fullComparisonConfidence = fullComparisonConfidence - 50;
        }
        if (beMatch.type.equals(BusinessEntityMatch.Type.FEASIBLE)) {
            res = res - 10;
            fullComparisonConfidence = fullComparisonConfidence - 10;
        }


        List<MorphemeMatch> matches = getMatches(c1.morphemes, c2.morphemes);

        int max = Math.max(c1.morphemes.size(), c2.morphemes.size());

        int weight = res / max;
        res = res - (max - getNumberOfConfidentMatches(matches)) * weight;

        for (MorphemeMatch match : matches) {
            if (match.type.equals(MorphemeMatch.Type.SYNONYM)) {
                res = res - (int) (weight * SYNONYM_FACTOR);
            }

            if (match.type.equals(MorphemeMatch.Type.SHORTENING)) {
                res = res - (int) Math.round(weight - weight * match.score / 100d);
            }

            if (match.type.equals(MorphemeMatch.Type.LEVENSHTAIN)) {
                res = res - (int) Math.round(weight - weight * match.score / 100d);
            }
        }

        return Math.max(res, fullComparisonConfidence);
    }

    private String replace(String company, String key, String value) {
        return company.replaceAll("(?i) " + key + " ", " " + value + " ");
    }

    private int getNumberOfConfidentMatches(List<MorphemeMatch> matches) {
        int res = 0;
        for (MorphemeMatch match : matches) {
            if (!match.type.equals(MorphemeMatch.Type.NONE)) {
                res++;
            }
        }
        return res;
    }

    private List<MorphemeMatch> getMatches(List<Morpheme> morphemes1, List<Morpheme> morphemes2) {
        List<MorphemeMatch> matches = Lists.newArrayList();

        for (Morpheme m1 : morphemes1) {
            for (Morpheme m2 : morphemes2) {
                matches.add(findMatch(m1, m2));
            }
        }

        Collections.sort(matches, new Comparator<MorphemeMatch>() {
            @Override
            public int compare(MorphemeMatch mm1, MorphemeMatch mm2) {
                MorphemeMatch.Type t1 = mm1.type;
                MorphemeMatch.Type t2 = mm2.type;
                if (t1.weight < t2.weight) {
                    return 1;
                }
                if (t1.weight > t2.weight) {
                    return -1;
                }
                return -ObjectUtils.compare(mm1.score, mm2.score);
            }
        });

        List<Morpheme> deleted1 = Lists.newArrayList();
        List<Morpheme> deleted2 = Lists.newArrayList();
        List<MorphemeMatch> result = Lists.newArrayList();

        for (MorphemeMatch match : matches) {
            if (!deleted1.contains(match.morpheme1) && !deleted2.contains(match.morpheme2)) {
                result.add(match);
                deleted1.add(match.morpheme1);
                deleted2.add(match.morpheme2);
            }
        }

        return result;
    }

    private MorphemeMatch findMatch(Morpheme m1, Morpheme m2) {
        if (m1.payload.equalsIgnoreCase(m2.payload)) {
            return new MorphemeMatch(m1, m2, MorphemeMatch.Type.FULL, 100);
        }

        double dlConfidence = 1 - getRelativeDlDistance(m1, m2);
        if (dlConfidence > 0.7) {
            return new MorphemeMatch(m1, m2, MorphemeMatch.Type.LEVENSHTAIN, (int) (dlConfidence * 100));
        }

        double shorteningConfidence = getShorteningConfidence(m1, m2);

        if (shorteningConfidence > 0.0) {
            return new MorphemeMatch(m1, m2, MorphemeMatch.Type.SHORTENING, (int) (shorteningConfidence * 100));
        }

        return new MorphemeMatch(m1, m2, MorphemeMatch.Type.NONE, 0);
    }

    private double getShorteningConfidence(Morpheme m1, Morpheme m2) {
        String p1 = m1.payload;
        String p2 = m2.payload;

        double confidence;
        if (p2.startsWith(p1)) {
            confidence = (double) p1.length() / p2.length();
        } else if (p1.startsWith(p2)) {
            confidence = (double) p2.length() / p1.length();
        } else {
            confidence = 0.0;
        }

        return confidence;
    }

    private double getRelativeDlDistance(Morpheme m1, Morpheme m2) {
        return getRelativeDlDistance(m1.payload.toLowerCase(), m2.payload.toLowerCase());
    }

    private String getNormalizedTitle(Company c) {
        List<String> payload = toPayloads(c);
        return Joiner.on(' ').join(payload);
    }

    private int getFullComparisonConfidence(String s1, String s2) {
        double dist = getRelativeDlDistance(s1.toLowerCase(), s2.toLowerCase());
        return (int) ((1 - dist) * 100);
    }

    private List<String> toPayloads(Company c1) {
        return Lists.transform(c1.morphemes, new Function<Morpheme, String>() {
            @Override
            public String apply(Morpheme input) {
                return input.payload;
            }
        });
    }

    private double getRelativeDlDistance(String s1, String s2) {
        DamerauLevenshtein dl = new DamerauLevenshtein(1, 1, 1, 1);
        int distance = dl.execute(s1, s2);
        return (double) distance / Math.max(s1.length(), s2.length());
    }


    private BusinessEntityMatch businessEntitiesMatch(Optional<BusinessEntity> be1, Optional<BusinessEntity> be2) {
        boolean b1 = be1.isPresent();
        boolean b2 = be2.isPresent();
        if (b1 ^ b2) {
            return new BusinessEntityMatch(be1, be2, BusinessEntityMatch.Type.FEASIBLE);
        }
        if (!b1 && !b2) {
            return new BusinessEntityMatch(be1, be2, BusinessEntityMatch.Type.FEASIBLE);
        }
        BusinessEntity entity1 = be1.get();
        BusinessEntity entity2 = be2.get();

        if (!entity1.payload.equalsIgnoreCase(entity2.payload)) {
            return new BusinessEntityMatch(be1, be2, BusinessEntityMatch.Type.NONE);
        }
        return new BusinessEntityMatch(be1, be2, BusinessEntityMatch.Type.FULL);
    }


    public static void main(String[] args) {
        Main m = new Main();
        int confidence = m.compare("United Technologies Corporation/NY", "UNITED TECHNOLOGIES CORPORATION OF NEW YORK CITY");
        System.out.println(confidence);
    }
}
