var $ = jQuery;
var lengthPrev = 0;
var lengthPrev_hourly = 0;

var minute_time_stamp = [];
var nh3_values = [];
var co_values = [];
var no2_values = [];
var dust_values = [];

var minute_time_stamp_hourly = [];
var nh3_values_hourly = [];
var co_values_hourly = [];
var no2_values_hourly = [];
var dust_values_hourly = [];


var aqi_time_stamp_hourly = [];
var aqi_hourly = [];
var lengthPrev_aqi_hourly = 0;

var latitude_array = [];
var longitude_array = [];

function hotspotlocation(){
	var db = firebase.firestore();
    var collection = db.collection("location_hour_collection").where("peak_AQI",">=",0).orderBy("peak_AQI","desc").limit(1);
	collection.get().then(function(querysnapshot) {
	querysnapshot.forEach(function(doc)
	{
		doc_data1 = 	JSON.parse(JSON.stringify(doc.data()));
		document.getElementById("hotspot_location").innerHTML = "Location:-  "+doc_data1.location;
		document.getElementById("hotspot_nh3").innerHTML = "NH3:-  "+doc_data1.NH3;
		document.getElementById("hotspot_co").innerHTML = "CO:-  "+doc_data1.CO;
		document.getElementById("hotspot_no2").innerHTML = "NO2:-   "+doc_data1.NO2;
		document.getElementById("hotspot_peak_aqi").innerHTML = "Peak AQI:-   "+doc_data1.peak_AQI;
		document.getElementById("hotspot_dust").innerHTML = "Dust:-    "+doc_data1.Dust_Concentration;
		document.getElementById("hotspot_date").innerHTML = "Date:-    "+doc_data1.date;
		document.getElementById("hotspot_hour").innerHTML = "Hour:-   "+doc_data1.time;
	});
	});
}
function loadoptions(){
	var location_element = document.getElementById("location");
	var location_secondelement = document.getElementById("location_frame");
	var location_thirdelement = document.getElementById("aqi_location");
	var counter= 0;
	var db = firebase.firestore();
    var collection = db.collection("location_hour_collection").where("peak_AQI",">=",0);
	collection.get().then(function(querysnapshot) {
	querysnapshot.forEach(function(doc)
	{
		doc_data1 = 	JSON.parse(JSON.stringify(doc.data()));
		optionValue = doc_data1.location;
		optionText = doc_data1.location;
		location_name = doc_data1.location;
		if(!(location_name in data)){
			if(counter == 0){
		location_element.append(new Option(optionText, optionValue));
		
		location_element.options[0].selected = true;
		
		location_secondelement.append(new Option(optionText, optionValue));
		
		location_secondelement.options[0].selected = true;
		
		location_thirdelement.append(new Option(optionText, optionValue));
		
		location_thirdelement.options[0].selected = true;
		
								  counter ++;
			}
		else{
			location_element.append(new Option(optionText, optionValue)); 
			location_secondelement.append(new Option(optionText, optionValue)); 
			location_thirdelement.append(new Option(optionText, optionValue)); 
		}
		}
	}
	
	);
	
	getdatahourly();
	getaqi();
	getaqihourly();
	  });
}

var test_arr = [];
function getmap(map,infowindow,data){
	var i=0;
	data = JSON.parse(JSON.stringify(data));
	
	for (var key in data) {
	marker = new google.maps.Marker({
      position: new google.maps.LatLng(data[key]['latitude'], data[key]['longitude']),
      map: map,
    });
    google.maps.event.addListener(
      marker,
      'click',
      (function(marker, key) {
        return function() {
          infowindow.setContent('<div><strong>' + '<b>Location: </b>'+ key+ '</strong><br>'+
								'<strong><b>NH3: </b>'+data[key].nh3+'</strong><br>' +
								'<strong><b>CO: </b>'+data[key].co+'</strong><br>' +
								'<strong><b>NO2: </b>'+data[key].no2+'</strong><br>' +
								'<strong><b>DUST: </b>'+data[key].dust+'</strong><br>' +
								'<strong><b>Peak AQI: </b>'+data[key].peak_aqi+'</strong><br>' +
								'<strong><b>date: </b>'+data[key].date+'</strong><br>' +
								'<strong><b>Hour: </b>'+data[key].hour+'</strong><br>' +
                '</div>');
          infowindow.open(map, marker)
        }
      })(marker, key)
    )
	
  }

	
	
}
var data = {};
  function initMap(){
	
	var map;
	var db = firebase.firestore();
    var collection = db.collection("location_hour_collection").where("peak_AQI",">",0);
	var map = new google.maps.Map(document.getElementById('map'), {
    zoom: 13,
    center: new google.maps.LatLng(55.9533, -3.1883),
    mapTypeId: 'terrain',
  })

  var infowindow = new google.maps.InfoWindow({})

  var marker, i
	
	collection.get().then(function(querysnapshot) {
	querysnapshot.forEach(function(doc)
	{
		doc_data1 = 	JSON.parse(JSON.stringify(doc.data()));
		if (doc_data1.peak_AQI!=-99){
		if (doc.id in data){
			
			if (doc_data1['peak_AQI'] != data[doc.id]['peak_aqi']){
				data[doc.id]['nh3'] = doc_data1.NH3;
		data[doc.id]['co'] = doc_data1.CO;
		data[doc.id]['dust'] = doc_data1.Dust_Concentration;
		data[doc.id]['no2'] = doc_data1.NO2;
		data[doc.id]['peak_aqi'] = doc_data1.peak_AQI;
		data[doc.id]['date'] = doc_data1.date;
		data[doc.id]['hour'] = doc_data1.time;
			}
		
		} else{
			data[doc.id] = {};
			data[doc.id]['latitude'] = doc_data1.latitude;
		data[doc.id]['longitude'] = doc_data1.longitude;
		data[doc.id]['nh3'] = doc_data1.NH3;
		data[doc.id]['co'] = doc_data1.CO;
		data[doc.id]['no2'] = doc_data1.NO2;
		data[doc.id]['dust'] = doc_data1.Dust_Concentration;
		data[doc.id]['peak_aqi'] = doc_data1.peak_AQI;
		data[doc.id]['date'] = doc_data1.date;
		data[doc.id]['hour'] = doc_data1.time;
		}
		}
		
		
		
	}
	
	);
	getmap(map,infowindow,data);
	hotspotlocation();
	  });
}
setInterval(initMap,600000);//6 minutes update map api 


var prev_aqi_location_name='null';
function getaqihourly(){
	var e = document.getElementById("aqi_location");
	
	if(e.options.length){
var location_name = e.options[e.selectedIndex].text;
if (prev_aqi_location_name == 'null')
	prev_aqi_location_name = location_name;
//document.getElementById("test3").innerHTML =  prev_location_name;
	var db = firebase.firestore();
	var date1 = new Date();	


var minutes = date1.getMinutes();
var hours = date1.getHours();
if (parseInt(hours)==00){
	hours = 23;
	date1.setDate(date1.getDate()-1);
}else{
	hours = hours-1;
}
var month = (date1.getMonth()+1);
	if (month.toString().length == 1){
		month = "0"+month.toString();
	}
	var date2 = date1.getDate();
	if (date2.toString().length == 1){
		date2 = "0"+date2.toString();
	}
var date = date1.getFullYear()+"."+(month)+"."+date2;
var time = parseInt(hours);
if(prev_aqi_location_name != location_name){
	aqi_time_stamp_hourly = [];
 aqi_hourly = [];
 lengthPrev_aqi_hourly = 0;
 prev_aqi_location_name = location_name;
}

	
	
	
	
	//remove this later- only for testing purpose
	var collection = db.collection("hour_collection").where("location","==",location_name).where("date","==","2020.03.30").where("time","<",24).orderBy("time");
	if(location_name=='Nicolson Square')
		var collection = db.collection("hour_collection").where("location","==",location_name).where("date","==","2020.03.12").where("time","<",24).orderBy("time");
	if(location_name=='Morgan Lane')
		var collection = db.collection("hour_collection").where("location","==",location_name).where("date","==","2020.03.26").where("time","<",24).orderBy("time");
	
	//depolyment uncomment below code when deploying for real time processing
	//var collection = db.collection("hour_collection").where("location","==",location_name).where("date","==",date).where("time","<",time).orderBy("time");

//}


  collection.get().then(function(querysnapshot) {
		
	  var lengthNow_hourly = querysnapshot.size;
	  
	  if(lengthNow_hourly!=0){
	  if(lengthNow_hourly != lengthPrev_aqi_hourly) {
		  aqi_time_stamp_hourly = [];
 aqi_hourly = [];
		querysnapshot.forEach(function(doc)
		{
			lengthPrev_aqi_hourly = lengthNow_hourly;
		doc_data1 = 	JSON.stringify(doc.data());
		doc_data = JSON.parse(doc_data1);
		
		aqi_time_stamp_hourly.push(doc_data.time);
		aqi_hourly.push(doc_data.avg_aqi);
		}
		);
	  update_aqi_hourly();  
	  }
      
	  }
	
  });
	}
}




var prev_location_name='null';
function getdatahourly(){
	var e = document.getElementById("location");
	
	if(e.options.length){
var location_name = e.options[e.selectedIndex].text;
if (prev_location_name == 'null')
	prev_location_name = location_name;
	var db = firebase.firestore();
	var date1 = new Date();	


var minutes = date1.getMinutes();
var hours = date1.getHours();
if (parseInt(hours)==00){
	hours = 23;
	date1.setDate(date1.getDate()-1);
}else{
	hours = hours-1;
}
var month = (date1.getMonth()+1);
	if (month.toString().length == 1){
		month = "0"+month.toString();
	}
	var date2 = date1.getDate();
	if (date2.toString().length == 1){
		date2 = "0"+date2.toString();
	}
var date = date1.getFullYear()+"."+(month)+"."+date2;
var time = parseInt(hours);
if(prev_location_name != location_name){
	minute_time_stamp_hourly = [];
nh3_values_hourly = [];
co_values_hourly = [];
no2_values_hourly = [];
dust_values_hourly = [];
 aqi_hourly = [];
 lengthPrev_hourly = 0;
 prev_location_name = location_name;
}

	
	
	
	
	
	//remove this later for testing purpose
	var collection = db.collection("hour_collection").where("location","==",location_name).where("date","==","2020.03.30").where("time","<",24).orderBy("time");
	if(location_name=='Nicolson Square')
		var collection = db.collection("hour_collection").where("location","==",location_name).where("date","==","2020.03.12").where("time","<",24).orderBy("time");
	
	
	if(location_name=='Morgan Lane')
		var collection = db.collection("hour_collection").where("location","==",location_name).where("date","==","2020.03.26").where("time","<",24).orderBy("time");
	
	//during deployment decomment below and comment above lines
	//var collection = db.collection("hour_collection").where("location","==",location_name).where("date","==",date).where("time","<",time).orderBy("time");
	
//}


  collection.get().then(function(querysnapshot) {
		
	  var lengthNow_hourly = querysnapshot.size;
	  
	  if(lengthNow_hourly!=0){
	  if(lengthNow_hourly != lengthPrev_hourly) {
		  minute_time_stamp_hourly = [];
nh3_values_hourly = [];
co_values_hourly = [];
no2_values_hourly = [];
dust_values_hourly = [];
 aqi_hourly = [];
 prev_location_name = location_name;
		querysnapshot.forEach(function(doc)
		{
			lengthPrev_hourly = lengthNow_hourly;
		doc_data1 = 	JSON.stringify(doc.data());
		doc_data = JSON.parse(doc_data1);
		
		minute_time_stamp_hourly.push(doc_data.time);
		nh3_values_hourly.push(doc_data.avg_nh3);
		co_values_hourly.push(doc_data.avg_co);
		no2_values_hourly.push(doc_data.avg_no2);
		dust_values_hourly.push(doc_data.avg_dust);
		}
		);
		
	  update_hourly();  
	  }
      
	  }
	
  });
	}
}


function getData() {
	for (var key in data) {
    }
	var db = firebase.firestore();
	var date1 = new Date();
	var date2 = date1.getDate();
	var month = (date1.getMonth()+1);
	if (month.toString().length == 1){
		month = "0"+month.toString();
	}
	if (date2.toString().length == 1){
		date2 = "0"+date2.toString();
	}
var date = date1.getFullYear()+"."+month+"."+date2;

var minutes = date1.getMinutes();
var hours = date1.getHours();
var prev_minutes;
var prev_hours;
if (parseInt(minutes)==00){
	prev_minutes = 58;
	if(parseInt(hours)==00){
		prev_hours = 23;
	}else{
		prev_hours = hours-1;
	}
}else{
	prev_minutes = minutes-2;
	prev_hours = hours;
}

if(minutes.toString().length == 1)
	minutes = "0"+minutes.toString();
if(hours.toString().length == 1)
	hours = "0"+hours.toString();
var time = hours+":"+minutes;
var prev_time = prev_hours+":"+prev_minutes;

	//only for testing purpose remove when deplying
	var collection = db.collection("minutely_collection").where("date","==","2020.04.02").where("time",">","18:00").where("time",">","18:30").orderBy("time");
	//deployment comment above line and decomment below code
	//var collection = db.collection("minutely_collection").where("date","==",date).where("time","<",time).orderBy("time");
//}


  collection.get().then(function(querysnapshot) {
		
	  var lengthNow = querysnapshot.size;
	  //document.getElementById("test2").innerHTML = lengthNow;
	  if(lengthNow !=0){
		  
	  if(lengthNow != lengthPrev) {
		  minute_time_stamp = [];
		  nh3_values = [];
		  co_values = [];
		  no2_values = [];
		  dust_values = [];
		querysnapshot.forEach(function(doc)
		{
			lengthPrev = lengthNow;
		doc_data1 = 	JSON.stringify(doc.data());
		doc_data = JSON.parse(doc_data1);
		
		minute_time_stamp.push(doc_data.time);
		nh3_values.push(doc_data.avg_nh3);
		//document.getElementById("test2").innerHTML = doc_data.avg_nh3;
		co_values.push(doc_data.avg_co);
		no2_values.push(doc_data.avg_no2);
		dust_values.push(doc_data.avg_dust);
		}
		);
	  update();
	  }
      
	  }
	  
  });
}

function getaqi(){
var hist_time_stamp_hourly = [];
 var nh3_values_hist_hourly = [];
var co_values_hist_hourly = [];
var no2_values_hist_hourly = [];
var dust_values_hist_hourly = [];
	
	
	var e = document.getElementById("location_frame");
	var time = document.getElementById("time_frame");
	if(e.options.length){
var location_name = e.options[e.selectedIndex].text;
var time_less = time.options[time.selectedIndex].value;

	var db = firebase.firestore();
	var date1 = new Date();
	date1.setDate(date1.getDate()-parseInt(time_less));
	var month = (date1.getMonth()+1);
	var date2 = date1.getDate()
	if (month.toString().length == 1){
		month = "0"+month.toString();
	}
	if (date2.toString().length == 1){
		date2 = "0"+date2.toString();
	}
var date = date1.getFullYear()+"."+(month)+"."+date2;


//update();

	var collection = db.collection("hour_collection").where("location","==",location_name).where("date","==",date.toString()).orderBy("time");
	//remove this later testing purpose
	//if(location_name=='Nicolson Square')
		//var collection = db.collection("hour_collection").where("location","==",location_name).where("date","==","2020.03.12").where("time",">=",13).orderBy("time");
	
	

	//var collection = db.collection("hour_collection").where("location","==",location_name).where("date","==",date).where("time",">=",time).orderBy("time");



  collection.get().then(function(querysnapshot) {
	
	  
	  
	 
		querysnapshot.forEach(function(doc)
		{
			
		doc_data1 = 	JSON.stringify(doc.data());
		doc_data = JSON.parse(doc_data1);
		
		hist_time_stamp_hourly.push(doc_data.time);
		nh3_values_hist_hourly.push(doc_data.avg_nh3);
		co_values_hist_hourly.push(doc_data.avg_co);
		no2_values_hist_hourly.push(doc_data.avg_no2);
		dust_values_hist_hourly.push(doc_data.avg_dust);
		}
		);
		//document.getElementById("test2").innerHTML = minute_time_stamp_hourly;
		var date_str = date+" pollutant levels"
		
	  update_history(hist_time_stamp_hourly,nh3_values_hist_hourly,co_values_hist_hourly,no2_values_hist_hourly,dust_values_hist_hourly,date_str);  
	
  });
	}
	
}



loadoptions();
getData();
setInterval(getData, 60000);      //minutely data function called every 60 seconds i.e minute
setInterval(getdatahourly, 3600000); //hourly data function called every 3600 seconds i.e 1 hour
setInterval(getaqihourly,3600000);//hourly data function called every 3600 seconds i.e 1 hour
function update(){
	//document.getElementById("test2").innerHTML = "testing2";
if ($('#verview-shart').length) {
    var myConfig = {
        "type": "line",

        "scale-x": { //X-Axis
            "labels": minute_time_stamp,  // label time array variable
            "label": {
                "font-size": 14,
                "offset-x": 0,
				"text":"Timestamp",
            },
			
            "item": { //Scale Items (scale values or labels)
                "font-size": 10,
            },
            "guide": { //Guides
                "visible": false,
                "line-style": "solid", //"solid", "dotted", "dashed", "dashdot"
                "alpha": 1
            }
        },
		
        "plot": { 'highlight-state': {
      'line-color': "black",
      'line-width': 3,
      'line-style': "dashdot"
    },
    'highlight-marker': {
      'type': "gear5",
      'size': 7,
      'background-color': "black white",
      'border-width': 3,
      'border-color': "black",
      shadow: false
    }},
		"legend": {
    'highlight-plot': true
  },
        "series": [{
                "values": nh3_values,              // nh3
				"text": "Nh3 minutely values in ppm",
      'legend-text': "NH3 level in ppm",
                "line-color": "#F0B41A",
                /* "dotted" | "dashed" */
                "line-width": 5 /* in pixels */ ,
                "marker": { /* Marker object */
                    "background-color": "#D79D3B",
                    /* hexadecimal or RGB value */
                    "size": 5,
                    /* in pixels */
                    "border-color": "#D79D3B",
                    /* hexadecimal or RBG value */
                }
            },
            {
                "values": co_values,
				text: "Co minutely values",
      'legend-text': "CO level in ppm",
                "line-color": "#0884D9",
                /* "dotted" | "dashed" */
                "line-width": 5 /* in pixels */ ,
                "marker": { /* Marker object */
                    "background-color": "#067dce",
                    /* hexadecimal or RGB value */
                    "size": 5,
                    /* in pixels */
                    "border-color": "#067dce",
                    /* hexadecimal or RBG value */
                }
            },
			{
				"values": no2_values,
				text: "NO2 minutely values",
      'legend-text': "NO2 level in ppm",
			},
			{
				"values": dust_values,
				text: "Dust minutely values",
      'legend-text': "Dust level in ug/cubicmeter",
			},
        ]
		
    };

    zingchart.render({
        id: 'verview-shart',
        data: myConfig,
        height: "100%",
        width: "100%"
    });
}

}
function update_hourly(){
	
	if ($('#hourly-status').length) {
		
    var myConfig1 = {
        "type": "line",

        "scale-x": { //X-Axis
            "labels": minute_time_stamp_hourly,  // label time array variable
            "label": {
                "font-size": 14,
                "offset-x": 0,
				"text":"Timestamp",
            },
            "item": { //Scale Items (scale values or labels)
                "font-size": 10,
            },
            "guide": { //Guides
                "visible": false,
                "line-style": "solid", //"solid", "dotted", "dashed", "dashdot"
                "alpha": 1
            }
        },
        "plot": { 'highlight-state': {
      'line-color': "black",
      'line-width': 3,
      'line-style': "dashdot"
    },
    'highlight-marker': {
      'type': "gear5",
      'size': 7,
      'background-color': "black white",
      'border-width': 3,
      'border-color': "black",
      shadow: false
    }},
		"legend": {
    'highlight-plot': true
  },
        "series": [{
                "values": nh3_values_hourly,              // nh3
				text: "Nh3 minutely values",
      'legend-text': "NH3 level in ppm",
                "line-color": "#F0B41A",
                /* "dotted" | "dashed" */
                "line-width": 5 /* in pixels */ ,
                "marker": { /* Marker object */
                    "background-color": "#D79D3B",
                    /* hexadecimal or RGB value */
                    "size": 5,
                    /* in pixels */
                    "border-color": "#D79D3B",
                    /* hexadecimal or RBG value */
                }
            },
            {
                "values": co_values_hourly,
				text: "Co minutely values",
      'legend-text': "CO level in ppm",
                "line-color": "#0884D9",
                /* "dotted" | "dashed" */
                "line-width": 5 /* in pixels */ ,
                "marker": { /* Marker object */
                    "background-color": "#067dce",
                    /* hexadecimal or RGB value */
                    "size": 5,
                    /* in pixels */
                    "border-color": "#067dce",
                    /* hexadecimal or RBG value */
                }
            },
			{
				"values": no2_values_hourly,
				text: "NO2 minutely values in ppm",
      'legend-text': "NO2 level in ppm",
			},
			{
				"values": dust_values_hourly,
				text: "Dust minutely values in ppm",
      'legend-text': "Dust level in ug/cubicmeter",
			},
			
        ]
		
    };

    zingchart.render({
        id: 'hourly-status',
        data: myConfig1,
        height: "400px",
        width: "100%"
    });
}
}

function update_aqi_hourly(){
	
	if ($('#aqi-status').length) {
		
    var myConfig1 = {
        "type": "line",

        "scale-x": { //X-Axis
            "labels": aqi_time_stamp_hourly,  // label time array variable
            "label": {
                "font-size": 14,
                "offset-x": 0,
				"text":"Timestamp",
            },
            "item": { //Scale Items (scale values or labels)
                "font-size": 10,
            },
            "guide": { //Guides
                "visible": false,
                "line-style": "solid", //"solid", "dotted", "dashed", "dashdot"
                "alpha": 1
            }
        },
        "plot": { 'highlight-state': {
      'line-color': "black",
      'line-width': 3,
      'line-style': "dashdot"
    },
    'highlight-marker': {
      'type': "gear5",
      'size': 7,
      'background-color': "black white",
      'border-width': 3,
      'border-color': "black",
      shadow: false
    }},
		"legend": {
    'highlight-plot': true
  },
        "series": [
                
			{
				"values": aqi_hourly,
				text: "AQI Hourly values",
      'legend-text': "AQI level",
			},
			
        ]
		
    };

    zingchart.render({
        id: 'aqi-status',
        data: myConfig1,
        height: "400px",
        width: "100%"
    });
}
}

function update_history(hist_time,nh3,co,no2,dust,date){
	
	if ($('#prev-status').length) {
		//document.getElementById("test3").innerHTML =  hist_time;
    var myConfig2 = {
        "type": "line",
		"title": {
    "text": date
  },
        "scale-x": { //X-Axis
            "labels": hist_time,  // label time array variable
            "label": {
                "font-size": 14,
                "offset-x": 0,
				"text":"Timestamp",
            },
            "item": { //Scale Items (scale values or labels)
                "font-size": 10,
            },
            "guide": { //Guides
                "visible": false,
                "line-style": "solid", //"solid", "dotted", "dashed", "dashdot"
                "alpha": 1
            }
        },
        "plot": { 'highlight-state': {
      'line-color': "black",
      'line-width': 3,
      'line-style': "dashdot"
    },
    'highlight-marker': {
      'type': "gear5",
      'size': 7,
      'background-color': "black white",
      'border-width': 3,
      'border-color': "black",
      shadow: false
    }},
		"legend": {
    'highlight-plot': true
  },
        "series": [{
                "values": nh3,              // nh3
				text: "Nh3 minutely values",
      'legend-text': "NH3 level in ppm",
                "line-color": "#F0B41A",
                /* "dotted" | "dashed" */
                "line-width": 5 /* in pixels */ ,
                "marker": { /* Marker object */
                    "background-color": "#D79D3B",
                    /* hexadecimal or RGB value */
                    "size": 5,
                    /* in pixels */
                    "border-color": "#D79D3B",
                    /* hexadecimal or RBG value */
                }
            },
            {
                "values": co,
				text: "Co minutely values",
      'legend-text': "CO level in ppm",
                "line-color": "#0884D9",
                /* "dotted" | "dashed" */
                "line-width": 5 /* in pixels */ ,
                "marker": { /* Marker object */
                    "background-color": "#067dce",
                    /* hexadecimal or RGB value */
                    "size": 5,
                    /* in pixels */
                    "border-color": "#067dce",
                    /* hexadecimal or RBG value */
                }
            },
			{
				"values": no2,
				text: "NO2 minutely values",
      'legend-text': "NO2 level in ppm",
			},
			{
				"values": dust,
				text: "Dust minutely values",
      'legend-text': "Dust level ug/cubicmeter",
			},
			
        ]
		
    };

    zingchart.render({
        id: 'prev-status',
        data: myConfig2,
        height: "400px",
        width: "100%"
    });
}


}