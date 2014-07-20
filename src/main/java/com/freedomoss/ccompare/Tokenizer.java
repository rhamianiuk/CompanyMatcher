package com.freedomoss.ccompare;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.*;

public class Tokenizer {

    private static Map<String, String> businessEntitySynonyms = Maps.newHashMap();
    private static List<String> businessEntities = Lists.newArrayList();

    static {
        try {
            URL resource = Resources.getResource("business.entity.synonyms.dic");
            List<String> file = Resources.readLines(resource, Charset.defaultCharset());
            for (String line : file) {
                if (isNotBlank(line)) {
                    String[] leftright = line.split("=");
                    businessEntitySynonyms.put(leftright[1], leftright[0]);
                    businessEntities.add(leftright[0]);
                    businessEntities.add(leftright[1]);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Company tokenize(String company) {
        company = company.trim();
        Preconditions.checkArgument(StringUtils.isNotBlank(company));

        List<Morpheme> morphemes = new ArrayList<Morpheme>();

        Optional<String> be = findBe(company);
        Optional<BusinessEntity> businessEntity;
        if (be.isPresent()) {
            String s = businessEntitySynonyms.get(be.get());
            if (s != null) {
                businessEntity = Optional.of(new BusinessEntity(s));
            } else {
                businessEntity = Optional.of(new BusinessEntity(be.get()));
            }
            company = StringUtils.removeEndIgnoreCase(company, be.get());
        } else {
            businessEntity = Optional.empty();
        }

        morphemes.addAll(split(company));

        return new Company(morphemes, businessEntity);
    }

    private List<Morpheme> split(String company) {
        Preconditions.checkArgument(isNotBlank(company));
        List<Morpheme> res = Lists.newArrayList();
        String[] split = company.split("[\\(\\)/, .\"]");
        int i = 0;
        for (String s : split) {
            if (isNotBlank(s)) {
//                String[] morphemes = s.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
//                for (String m : morphemes) {
//                    res.add(new Morpheme(m, i));
//                }
                res.add(new Morpheme(s, i));
                i++;
            }
        }
        return res;
    }

    public Optional<String> findBe(String company) {
        for (String businessEntity : businessEntities) {
            boolean contains = containsIgnoreCase(company, " " + businessEntity);
            boolean endsWith = endsWithIgnoreCase(company, " " + businessEntity);
            if (contains && endsWith) {
                return Optional.of(businessEntity);
            }
        }
        return Optional.empty();
    }

}
