var express = require('express');
var router = express.Router();

/* GET home page. */
router.get('/', function(req, res) {
  	res.sendfile('./views/index.html');
});

router.get('/data', function(req, res){
	var db = req.db;
	db.collection('userlist').findOne(function(err, result) {
		if (err) console.log(err);
		//console.log(result);
		//res.json(result);
		res.send(result);
		db.close();
	});
});

router.post('/adduser',function(req, res) {
	var db = req.db;
	db.collection('userlist').drop();
	req.body.upload = new Date();
	console.log(req.body);
	db.collection('userlist').insert(req.body, function(err, result){
        res.send(
            (err === null) ? "Data sent!" : { msg: err }
        );
    });
	console.log(req.body);
});

module.exports = router;
