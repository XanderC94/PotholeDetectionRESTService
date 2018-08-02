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

        var map = new google.maps.Map(document.getElementById('map'), {
          zoom: 13,
          center: selection
        });

        potholes.forEach(p => {
            var marker = new google.maps.Marker({position: p, map: map})
            console.log(marker);
        })
    });
}

