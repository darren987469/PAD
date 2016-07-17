var App = function (name, package, count) {
  		this.name = name;
  		this.package = package;
  		this.count = count;
	};	

	// return today's time count
	App.prototype.getTimeCount = function(hello) {
		var today = new Date().toLocaleDateString();
		var latestRecord = this.count[0];
		var date = new Date(latestRecord.split(",")[0]).toLocaleDateString();
		if(date == today) {
			var timeCount = latestRecord.split(",")[1];
			return timeCount;
		} else {
			return 0;
		}
	};

	$(function() {
		$.getJSON("/data").done(function(data){
			//console.log("data:",data);
			var upload_time = new Date(data.upload).toString();
			$("#upload-time").text("更新時間: "+upload_time);
			//var appusage = data.appusage;
			var usecount = data.usecount;
			//console.log("usecount:",JSON.stringify(usecount));
			var today = new Date().toLocaleDateString();
			var totalTime = 0;
			var applist = [];
			data.appusage.forEach(function(d){
				var app = new App(d.name, d.package, d.count);
				console.log("app count:",app.getTimeCount());
				totalTime += parseInt(app.getTimeCount());
				applist.push(app);
			});
			applist.sort(function(app1, app2){
				return app1.getTimeCount() - app2.getTimeCount();
			});
			
			// caculate total to form HH:mm:ss
			var hours = parseInt(totalTime / 3600)%24;
			var minutes = parseInt(totalTime/60)%60;
			var seconds = totalTime % 60;
			var timestr = "今日使用時間: ";
			var timestr = "共 "+(hours == 0 ? "" : hours +" 時 ") + (minutes == 0 ? "" : minutes + " 分 ") + (seconds == 0 ? "" : seconds +" 秒"); 
			$("#total-time").text(timestr);
			console.log("total time:",totalTime);
			console.log("total time:",timestr);
			// pie chart data
			var appusage = [];
			// convert appusage data to pie chart data structure
			// appusage data structure: 
			// "{	"package": "com.android.calendar",
     		//	"count": ["2015-01-08,0"],
     		//	"name": "日曆" }"
			applist.forEach(function(app){
				if(app.getTimeCount() > 0) {
					appusage.push({ label: app.name, data: app.getTimeCount()});
				}
			});

			//console.log(JSON.stringify(appusage));
			var piechart = $("#piechart");
			$.plot(piechart, appusage, {
				series: {
					pie: {
						show:true,
						combine: {
							color: "#999",
							threshold: 0.05
						}
					}
				},
				legend: {
					show: false
				}
			});
			
			var rowchartData = [];
			var rowchartTicks = [];
			for(var i in applist) {
				//console.log("data.appusage["+i+"]:",data.appusage[i]);
				var app = applist[i];
				//console.log(d);
				if(app.getTimeCount() > 0) {
					rowchartData.push([app.getTimeCount(),i]);
					rowchartTicks.push([i,app.name]);	
				}
			}
			// filter app which use time is less than 5 % in total, those apps classify as 'others' app
			// TODO
			//console.log("rowchartData:",JSON.stringify(rowchartData));
			var rowchart = $("#rowchart");
			$.plot(rowchart, [{label: "appusage", data: rowchartData}], {
				series: {
					bars: {
						show: true
					}
				},
				bars: {
					horizontal: true,
					barWidth: 0.7,
					align: "center"
				},
				yaxis: {
					ticks: rowchartTicks
				},
				legend: {
					show: false
				}
			});
			console.log("usecount:",JSON.stringify(usecount));
			// barchart : show use cout
			var usecountData = [];
			usecount.forEach(function(d){
				var label = d.date.split("-")[2];
				usecountData.push([label, d.count]);
			});
			var barchart = $("#barchart");
			$.plot(barchart, [{
				data: usecountData,
				bars: {
					show: true
				}	
			}], {
				xaxis: {
					tickSize: 1
				},
				bars: {
					align:"center",
					barWidth:0.7
				}
			});
		});	
	});