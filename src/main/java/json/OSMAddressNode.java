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

    private String country;
    private String countryCode;
    private String region; // like "Emilia-Romagna"
    private String county; // like "RN"
    private String city;
    private String district;
    private String suburb;
    private String town;
    private String village;
    private String neighbourhood;
    private String place; // address29
    private String postcode;
    private String road;
    private String houseNumber;

    public OSMAddressNode(final String houseNumber,
                          final String road,
                          final String city,
                          final String neighbourhood,
                          final String town,
                          final String county,
                          final String region,
                          final String postcode,
                          final String country,
                          final String countryCode,
                          final String district,
                          final String suburb,
                          final String village,
                          final String place) {

        this.houseNumber = houseNumber;
        this.road = road;
        this.city = city;
        this.neighbourhood = neighbourhood;
        this.town = town;
        this.county = county;
        this.region = region;
        this.postcode = postcode;
        this.country = country;
        this.countryCode = countryCode;
        this.district = district;
        this.suburb = suburb;
        this.village = village;
        this.place = place;
    }

    public void setHouseNumber(String houseNumber) {
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

    public void setCity(String city) { this.city = city; }

    public String getDistrict() { return district; }

    public void setDistrict(String district) { this.district = district; }

    public String getSuburb() { return suburb; }

    public void setSuburb(String suburb) { this.suburb = suburb; }

    public String getVillage() { return village; }

    public void setVillage(String village) { this.village = village; }

    public String getHouseNumber() {
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

    public String getCity() {
        return city;
    }

    @Override
    public String toString() {
        return "OSMAddressNode{" +
                "houseNumber='" + houseNumber + '\'' +
                ", road='" + road + '\'' +
                ", neighbourhood='" + neighbourhood + '\'' +
                ", city='" + city + '\'' +
                ", town='" + town + '\'' +
                ", county='" + county + '\'' +
                ", region='" + region + '\'' +
                ", postcode='" + postcode + '\'' +
                ", country='" + country + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", place='" + place + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OSMAddressNode)) return false;
        OSMAddressNode that = (OSMAddressNode) o;
        return Objects.equals(getCountry(), that.getCountry()) &&
                Objects.equals(getCountryCode(), that.getCountryCode()) &&
                Objects.equals(getRegion(), that.getRegion()) &&
                Objects.equals(getCounty(), that.getCounty()) &&
                Objects.equals(getCity(), that.getCity()) &&
                Objects.equals(getDistrict(), that.getDistrict()) &&
                Objects.equals(getSuburb(), that.getSuburb()) &&
                Objects.equals(getTown(), that.getTown()) &&
                Objects.equals(getVillage(), that.getVillage()) &&
                Objects.equals(getNeighbourhood(), that.getNeighbourhood()) &&
                Objects.equals(getPlace(), that.getPlace()) &&
                Objects.equals(getPostcode(), that.getPostcode()) &&
                Objects.equals(getRoad(), that.getRoad()) &&
                Objects.equals(getHouseNumber(), that.getHouseNumber());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getCountry(), getCountryCode(), getRegion(),
                getCounty(), getCity(), getDistrict(), getSuburb(),
                getTown(), getVillage(), getNeighbourhood(), getPlace(),
                getPostcode(), getRoad(), getHouseNumber()
        );
    }

    public static OSMAddressNode empty() {
        return new OSMAddressNode("",
                "", "", "", "",
                "", "", "",
                "", "",
                "", "", "", ""
        );
    }
}