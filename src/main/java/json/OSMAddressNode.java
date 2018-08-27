package json;

import java.util.Objects;

/**
 * {
 *  "place_id":"56907419",
 *  "licence":"Data © OpenStreetMap contributors, ODbL 1.0. https:\/\/osm.org\/copyright",
 *  "osm_type":"node",
 *  "osm_id":"4495273342",
 *  "lat":"43.9919503","lon":"12.6500115",
 *  "place_rank":"30",
 *  "category":"place",
 *  "type":"house",
 *  "importance":"0",
 *  "addresstype":"place",
 *  "name":null,
 *  "display_name":"15, Viale Cagliari, Abissinia, Riccione, RN, Emilia-Romagna, 47838, Italy",
 *  "address":{
 *      "house_number":"15",
 *      "road":"Viale Cagliari",
 *      "neighbourhood":"Abissinia",
 *      "town":"Riccione",
 *      county":"RN",
 *      "region":"Emilia-Romagna",
 *      "postcode":"47838",
 *      "country":"Italy",
 *      "country_code":"it"
 *      },
 *  "boundingbox":["43.9918503","43.9920503","12.6499115","12.6501115"]
 *  }
 *
 */

public class OSMAddressNode {

    private int houseNumber;
    private String road;
    private String neighbourhood;
    private String town;
    private String county; // like "RN"
    private String region; // like "Emilia-Romagna"
    private String postcode;
    private String country;
    private String countryCode;
    private String place; // address29

    public OSMAddressNode(final int houseNumber,
                          final String road,
                          final String neighbourhood,
                          final String town,
                          final String county,
                          final String region,
                          final String postcode,
                          final String country,
                          final String countryCode,
                          final String place) {
        this.houseNumber = houseNumber;
        this.road = road;
        this.neighbourhood = neighbourhood;
        this.town = town;
        this.county = county;
        this.region = region;
        this.postcode = postcode;
        this.country = country;
        this.countryCode = countryCode;
        this.place = place;
    }

    public void setHouseNumber(int houseNumber) {
        this.houseNumber = houseNumber;
    }

    public void setRoad(String road) {
        this.road = road;
    }

    public void setNeighbourhood(String neighbourhood) {
        this.neighbourhood = neighbourhood;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    @Override
    public String toString() {
        return "OSMAddressNode{" +
                "houseNumber='" + houseNumber + '\'' +
                ", road='" + road + '\'' +
                ", neighbourhood='" + neighbourhood + '\'' +
                ", town='" + town + '\'' +
                ", county='" + county + '\'' +
                ", region='" + region + '\'' +
                ", postcode='" + postcode + '\'' +
                ", country='" + country + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", place='" + place + '\'' +
                '}';
    }

    public int getHouseNumber() {
        return houseNumber;
    }

    public String getRoad() {
        return road;
    }

    public String getNeighbourhood() {
        return neighbourhood;
    }

    public String getTown() {
        return town;
    }

    public String getCounty() {
        return county;
    }

    public String getRegion() {
        return region;
    }

    public String getPostcode() {
        return postcode;
    }

    public String getCountry() {
        return country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getPlace() { return place; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OSMAddressNode)) return false;
        OSMAddressNode that = (OSMAddressNode) o;
        return getHouseNumber() == that.getHouseNumber() &&
                Objects.equals(getRoad(), that.getRoad()) &&
                Objects.equals(getNeighbourhood(), that.getNeighbourhood()) &&
                Objects.equals(getTown(), that.getTown()) &&
                Objects.equals(getCounty(), that.getCounty()) &&
                Objects.equals(getRegion(), that.getRegion()) &&
                Objects.equals(getPostcode(), that.getPostcode()) &&
                Objects.equals(getCountry(), that.getCountry()) &&
                Objects.equals(getCountryCode(), that.getCountryCode()) &&
                Objects.equals(getPlace(), that.getPlace());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHouseNumber(), getRoad(), getNeighbourhood(), getTown(), getCounty(), getRegion(), getPostcode(), getCountry(), getCountryCode(), getPlace());
    }

    public static OSMAddressNode empty() {
        return new OSMAddressNode(0,
                "", "", "",
                "", "", "",
                "", "", ""
        );
    }
}