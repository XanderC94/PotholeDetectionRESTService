package csv;

import com.opencsv.bean.CsvBindByName;

public class State {

    @CsvBindByName(column = "Country", required = true)
    private String country;
    @CsvBindByName(column = "ISO", required = true)
    private String iso;
    @CsvBindByName(column = "State", required = true)
    private String name;
    @CsvBindByName(column = "Signature", required = true)
    private String signature;

    public State(String country, String iso, String name, String signature) {
        this.country = country;
        this.iso = iso;
        this.name = name;
        this.signature = signature;
    }

    public State() {

    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getIso() {
        return iso;
    }

    public void setIso(String iso) {
        this.iso = iso;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
