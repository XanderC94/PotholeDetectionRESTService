package utils;

import com.opencsv.bean.CsvToBeanBuilder;
import csv.Province;

import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

public class ProvinceFilter extends NameFilter<Province> {

    @SuppressWarnings("unchecked")
    public ProvinceFilter(String csv) {
        super(csv);

        try {
            super.checkList = new CsvToBeanBuilder(new FileReader(csv))
                    .withType(Province.class)
                    .withSeparator(',')
                    .withQuoteChar('\"')
                    .build()
                    .parse();

            super.byISO = checkList.stream().collect(
                    Collectors.toMap(
                            p-> Utils.clean(p.getIso()),
                            p -> p,
                            (oldValue, newValue) -> oldValue,
                            LinkedHashMap::new
                    )
            );

            super.byName = checkList.stream().collect(
                    Collectors.toMap(
                            p -> Utils.clean(p.getName()),
                            p-> Utils.clean(p.getIso()),
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
