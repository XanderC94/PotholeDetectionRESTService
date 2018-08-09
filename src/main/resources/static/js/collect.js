

var HttpClient = function(async) {
    this.GET = function(aUrl, aCallback) {
        var anHttpRequest = new XMLHttpRequest();
        anHttpRequest.onreadystatechange = function() {
            if (anHttpRequest.readyState == 4 && anHttpRequest.status == 200)
                aCallback(anHttpRequest.responseText);
        }

        anHttpRequest.open("GET", aUrl, async);
        anHttpRequest.setRequestHeader("Content-Type", "application/json");
        anHttpRequest.send(null);
    }
}

function fillMap() {

    var body = {country: "Italia", town:"Riccione"};

    console.log(body);

    var osmMap = L.map('osmMap').setView([51.505, -0.09], 13);

    L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}', {
        attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> ' +
        'contributors, <a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, ' +
        'Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
        maxZoom: 18,
        id: 'mapbox.streets',
        accessToken: 'pk.eyJ1IjoicHVtcGtpbnNoZWFkIiwiYSI6ImNqa2NuM3l2cDFzdGYzcXA4MmoyZ2dsYWsifQ.FahVhmZj5RODSwGjl5-EaQ'
    }).addTo(osmMap);

    new HttpClient(true).GET("http://localhost:8080/api/pothole/", function(response) {

        var potholes = (JSON.parse(response)).content;

        var selection = { lat: 43.9921, lng: 12.6503 };

        //
        // var googleMap = new google.maps.Map(document.getElementById('googleMap'), {
        //   zoom: 13,
        //   center: selection
        // });

        potholes.forEach(m => {
            //var marker = new google.maps.Marker({position: p, map: googleMap})
            console.log(m);
            var poi = L.marker(m.coordinates).addTo(osmMap);
            console.log(poi);
        });
    });
}

// function onMapClick(e){
//     popup.setLatLng(e.latlng)
//         .setContent("You clicked the map at " + e.latlng.toString())
//         .openOn(osmMap);
// }
//
// //EVENT BINDING
// osmMap.on('click', onMapClick);