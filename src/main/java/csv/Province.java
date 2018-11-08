package csv;

import com.opencsv.bean.CsvBindByName;

public class Province {

    @CsvBindByName(column = "Country", required = true)
    private String country;

    @CsvBindByName(column = "ISO", required = true)
    private String iso;

    @CsvBindByName(column = "Province", required = true)
    private String name;

    @CsvBindByName(column = "Istat", required = true)
    private Integer istat;

    public Province(String country, String iso, String name, Integer istat) {
        this.country = country;
        this.iso = iso;
        this.name = name;
        this.istat = istat;
    }

    public Province() { }

    public String getCountry() {
        return country;
    }

    public String getIso() {
        return iso;
    }

    public String getName() {
        return name;
    }

    public Integer getIstat() {
        return istat;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setIso(String iso) {
        this.iso = iso;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIstat(Integer istat) {
        this.istat = istat;
    }

    @Override
    public String toString() {
        return "Province{" +
                "country='" + country + '\'' +
                ", iso='" + iso + '\'' +
                ", name='" + name + '\'' +
                ", istat=" + istat +
                '}';
    }
}
