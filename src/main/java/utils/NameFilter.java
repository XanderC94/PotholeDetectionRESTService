package utils;

import com.opencsv.bean.CsvToBeanBuilder;
import csv.Province;
import csv.State;

import java.io.FileReader;
import java.util.*;

@SuppressWarnings("unchecked")
public abstract class NameFilter<T> {

    protected Map<String, T> byISO;
    protected Map<String, String> byName;

    protected List<T> checkList;

    protected NameFilter(String csv) { }

    public String filter(final String key) {
        String stub = Utils.clean(key);
        if (byName.containsKey(stub)) {
            return byName.get(stub);
        } else if (byISO.containsKey(stub)) {
            return stub;
        } else {
            return key;
        }
    }

    abstract public String unfilter(String key);

    public static void main(String[] args) {

        Logging.println(System.getProperty("user.dir").replace('\\', '/'));

        try {

            String provinces = "src/main/resources/iso/provinces.csv";
            String states = "src/main/resources/iso/states.csv";

            NameFilter f1 = new ProvinceFilter(provinces);

            List<Province> p = new CsvToBeanBuilder(new FileReader(provinces)).withType(Province.class)
                    .withSeparator(',').withQuoteChar('\"').build().parse();

            p.forEach(e -> {
                Logging.println(
                        String.valueOf(f1.filter(e.getName().toLowerCase()).equals(e.getIso()))
                );
            });

            p.forEach(e -> {

                Logging.println(
                        String.valueOf(f1.filter(e.getIso().toLowerCase()).equals(e.getIso()))
                );
            });

            p.forEach(e -> {
                Logging.println(
                        String.valueOf(f1.unfilter(e.getName().toLowerCase()).equals(e.getName()))
                );
            });

            p.forEach(e -> {

                Logging.println(
                        String.valueOf(f1.unfilter(e.getIso().toLowerCase()).equals(e.getName()))
                );
            });

            Logging.println("---------------------------------------------------------------------");

            NameFilter f2= new RegionFilter(states);

            List<State> s = new CsvToBeanBuilder(new FileReader(states)).withType(State.class)
                    .withSeparator(',').withQuoteChar('\"').build().parse();

            s.forEach(e -> {
                Logging.println(
                    String.valueOf(f2.filter(e.getName().toLowerCase()).equals(e.getSignature()))
                );
            });

            s.forEach(e -> {
                Logging.println(
                    String.valueOf(f2.filter(e.getSignature().toLowerCase()).equals(e.getSignature()))
                );
            });

            s.forEach(e -> {
                Logging.println(
                        String.valueOf(f2.unfilter(e.getSignature().toLowerCase()).equals(e.getName()))
                );
            });

            s.forEach(e -> {
                Logging.println(
                        String.valueOf(f2.unfilter(e.getName().toLowerCase()).equals(e.getName()))
                );
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
