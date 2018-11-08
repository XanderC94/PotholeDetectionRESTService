package utils;

import com.opencsv.bean.CsvToBeanBuilder;
import csv.State;

import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

public class RegionFilter extends NameFilter<State> {

    @SuppressWarnings("unchecked")
    public RegionFilter(String csv) {
        super(csv);
        try {
            super.checkList = new CsvToBeanBuilder(new FileReader(csv))
                    .withType(State.class)
                    .withSeparator(',')
                    .withQuoteChar('\"')
                    .build()
                    .parse();

            super.byISO = checkList.stream().collect(
                    Collectors.toMap(
                            p-> Utils.clean(p.getSignature()),
                            p -> p,
                            (oldValue, newValue) -> oldValue,
                            LinkedHashMap::new
                    )
            );

            super.byName = checkList.stream().collect(
                    Collectors.toMap(
                            p -> Utils.clean(p.getName()),
                            p-> Utils.clean(p.getSignature()),
                            (oldValue, newValue) -> oldValue,
                            LinkedHashMap::new
                    )
            );

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("Duplicates")
    @Override
    public String unfilter(String key) {
        String stub = Utils.clean(key);
        if (byISO.containsKey(stub)) {
            return byISO.get(stub).getName();
        } else if (byName.containsKey(stub)) {
            return byISO.get(byName.get(stub)).getName();
        } else {
            return key;
        }
    }
}
