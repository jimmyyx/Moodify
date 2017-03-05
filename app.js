var config = require('./config.js');
var express = require('express');
var path = require('path');
var favicon = require('serve-favicon');
var logger = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');
var multer = require('multer');
var upload = multer({dest: 'uploads/'});
var fs = require('fs');

var vision = require('@google-cloud/vision')({
  projectId: config.projectId,
  keyFilename: config.keyFilename
});

var colours = [
    {r: 255, g: 0, b: 0},
    {r: 0, g: 255, b: 0},
    {r: 0, g: 0, b: 255},
    {r: 255, g: 255, b: 0},
    {r: 0, g: 255, b: 255},
    {r: 255, g: 0, b: 255},
    {r: 255, g: 255, b: 255}
]

var app = express();

app.post('/upload', upload.single('image'), function(req, res, next){
    var options = {verbose: true};

	vision.detectProperties(req.file.path, options, function(err, props, apiResponse){
		fs.unlinkSync(req.file.path);
		if(err){
			res.send(err);
		} else {
		    //console.log(props.colors[0]);
            var dominant = colourDistance(colours, props.colors[0]);
            res.send(dominant);
		}
	})
});

function colourDistance(lib, colour){
    var min = 200000;
    var dominant;
    for(var i=0; i<lib.length; i++) {
        dist = Math.pow(lib[i].r - colour.red, 2) + Math.pow(lib[i].g - colour.green, 2) + Math.pow(lib[i].b - colour.blue, 2);
        console.log(dist);
        if(dist < min){
            min = dist;
            dominant = lib[i];
        }
    }
    return dominant;
}

app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(cookieParser());

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  var err = new Error('Not Found');
  err.status = 404;
  next(err);
});

// error handler
app.use(function(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});

module.exports = app;