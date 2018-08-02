package json;

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

    private final int houseNumber;
    private final String road;
    private final String neighbourhood;
    private final String town;
    private final String county; // like "RN"
    private final String region; // like "Emilia-Romagna"
    private final String postcode;
    private final String country;
    private final String countryCode;
    private final String place; // address29

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
}