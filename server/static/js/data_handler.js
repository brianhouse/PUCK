function poll () {
    $.ajax({
        type: "POST",
        url: "/" + puck_id,
        data: {'nop': null},
        cache: false,
        success: onData,
        error: onError,
    });
}

function onError (response) {
    $('#error_message').html("Error!");
    console.log(response);    
    poll();
}

function onData (data) {
    $('#error_message').html("");  
    data = jQuery.parseJSON(data);
    draw(data);
    setTimeout(poll, 100)
}


var ctx;
var flip = 0;

$(document).ready(function() {
    ctx = $('canvas#canvas')[0].getContext('2d');    
    poll();
});    

function draw (data) {
    
    // clear the context
    // ctx.clearRect(0, 0, canvas.width, canvas.height);         
    ctx.width = ctx.width;   
    
    // draw background
    // ctx.fillStyle = "rgb(225, 225, 225)";
    ctx.fillStyle = "rgb(250, 250, 250)";
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    ctx.strokeStyle = "lightgray";    
    ctx.strokeRect(0, 0, canvas.width, canvas.height);    
        
    var last_x = 0;
    var last_y = 0;
    for (var i=0; i<data.length; i++) {
        // d = data[i];
        // dn = data[i + 1];        

        x = i * (canvas.width / (data.length-1));

        y = data[i]['delta'] / 100.0
        if (i % 2 == flip) {
            y = 1.0 - y;
            y *= canvas.height / 2;
        } else {
            y *= canvas.height / 2;
            y += canvas.height / 2;
        }
        if (last_y == 0) last_y = y;
        line(last_x, last_y, x, y, "black");                
        last_x = x;
        last_y = y;

        // y1 = (1.0 - (d['delta'] / 100.0)) * canvas.height;
        // y2 = (1.0 - (dn['delta'] / 100.0)) * canvas.height;
        // line(x1, y1, x2, y2, "black");

    
        // y1 = (1.0 - (d['rssi'] / 100.0)) * canvas.height;
        // y2 = (1.0 - (dn['rssi'] / 100.0)) * canvas.height;
        // line(x1, y1, x2, y2, "red");
    
        // y1 = (1.0 - d['lowalpha']) * canvas.height;
        // y2 = (1.0 - dn['lowalpha']) * canvas.height;
        // line(x1, y1, x2, y2, "lightgray");
    
        // y1 = (1.0 - d['lowbeta']) * canvas.height;
        // y2 = (1.0 - dn['lowbeta']) * canvas.height;
        // line(x1, y1, x2, y2, "lightgray");
    
        // y1 = (1.0 - d['highbeta']) * canvas.height;
        // y2 = (1.0 - dn['highbeta']) * canvas.height;
        // line(x1, y1, x2, y2, "lightgray");
    
        // y1 = (1.0 - d['lowgamma']) * canvas.height;
        // y2 = (1.0 - dn['lowgamma']) * canvas.height;
        // line(x1, y1, x2, y2, "lightgray");
    
        // y1 = (1.0 - d['midgamma']) * canvas.height;
        // y2 = (1.0 - dn['midgamma']) * canvas.height;
        // line(x1, y1, x2, y2, "lightgray");
    
        // y1 = (1.0 - d['attention']) * canvas.height;
        // y2 = (1.0 - dn['attention']) * canvas.height;
        // line(x1, y1, x2, y2, "red");
    
        // y1 = (1.0 - d['meditation']) * canvas.height;
        // y2 = (1.0 - dn['meditation']) * canvas.height;
        // line(x1, y1, x2, y2, "blue");
    
        // y1 = (1.0 - d['signal']) * canvas.height;
        // y2 = (1.0 - dn['signal']) * canvas.height;
        // line(x1, y1, x2, y2, "lightgreen");
        
    }

    if (flip == 0) {
        flip = 1;
    } else {
        flip = 0;
    }
        
}

function dot (x, y) {
    ctx.fillRect(x, y, 2, 2);
}

function line (x1, y1, x2, y2, color) {
    ctx.beginPath();
    ctx.strokeStyle = color;
    ctx.moveTo(x1, y1);
    ctx.lineTo(x2, y2);
    ctx.stroke();       
}