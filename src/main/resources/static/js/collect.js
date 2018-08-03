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

    var body = {town:"Riccione"};

    console.log(body);

    new HttpClient(true).GET("http://localhost:8080/collect?", function(response) {

        var potholes = (JSON.parse(response)).content;

        var selection = { lat: 43.9921, lng: 12.6503 };

        var osmMap = L.map('osmMap').setView([51.505, -0.09], 13);

        L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}', {
            attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, <a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
            maxZoom: 18,
            id: 'mapbox.streets',
            accessToken: 'pk.eyJ1IjoicHVtcGtpbnNoZWFkIiwiYSI6ImNqa2NuM3l2cDFzdGYzcXA4MmoyZ2dsYWsifQ.FahVhmZj5RODSwGjl5-EaQ'
        }).addTo(osmMap);

        //
        // var googleMap = new google.maps.Map(document.getElementById('googleMap'), {
        //   zoom: 13,
        //   center: selection
        // });

        potholes.forEach(p => {
            //var marker = new google.maps.Marker({position: p, map: googleMap})
            var marker = L.marker(p).addTo(osmMap);
            console.log(marker);
        });
    });
}

