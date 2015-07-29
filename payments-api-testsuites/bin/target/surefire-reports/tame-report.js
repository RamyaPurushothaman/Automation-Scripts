$(document).ready(function() {

	fixJenkinsHtmlReportIframeIssue();
	updateLoggerOutput();
	updateKnownFailureFlags();
	screenshotPreview();
	textfilePreview();
});

// Jenkins plugin for HTML Report uses iframe that confuses local links
// Need to reset the URL to fix the link problem
function fixJenkinsHtmlReportIframeIssue() {

	var current_url = $(location).attr('href');
	var parent_url = parent.window.location.href;

	if (parent_url.match('\\?$')) {
		parent.window.location.replace(current_url);
	}
}

function updateLoggerOutput() {
	console.log("updateLoggerOutput");
	var data = [];
	$('.reporter-method-div .reporter-method-name').each(
			function() {
				var testcase = $(this).html();
				// handle FireFox's extra html tags
				// var messages =
				// $(this).parent().find('.reporter-method-output-div');
				var messages = $(this).parents('.reporter-method-div').find(
						'.reporter-method-output-div');
				data[testcase] = messages;
			});

	$('div.method div.method-content').each(function() {
		var name = $(this).find('a')[0].name;
		if (name in data) {
			console.log(">> " + name);
			if ((typeof data[name]) == "object") {
				$(this).append(data[name].clone());
			}
		}
	});

}

function updateKnownFailureFlags() {
	var data = [];
	var failedTC = $("[class^='method-list-content failed']").find(
			"[class='method navigator-link']").each(function() {

		var testcase = $(this).attr('hash-for-method');

		console.log(">FAILED 1: " + $(this).html());
		console.log(">FAILED 2: " + $(this).parent().html());
		console.log(">FAILED 3: " + testcase);
		console.log(">>KF: " + $(this).nextAll('.tame').html());
		console.log(">>KFP: " + $(this).nextAll('.tame').parent().html());

		var failure = $(this).nextAll('.tame').clone();
		if (failure != null && failure.length > 0) {
			var text = $(failure).attr('title');
			$(failure).html("&nbsp;[" + text + "]");
			console.log(">new text: " + $(failure).html());
			console.log(">new text: " + $(failure) + ", " + failure);

			data[testcase] = failure;
		}

	});

	$("[class$='-failed']").find('.method .method-content').each(function() {
		var name = $(this).find('a').attr('name');
		if (name != null && name.length > 0 && name in data) {
			console.log("FF>> name: " + name);
			console.log("FF>> " + $(this).html());

			var testcase = $(this).find('span.method-name').append(data[name]);
			console.log("FF>>>> " + $(testcase).html());
		}
	});
}

function screenshotPreview() {
	/* CONFIG */

	xOffset = 10;
	yOffset = 30;

	// these 2 variable determine popup's distance from the cursor
	// you might want to adjust to get the right result

	/* END CONFIG */
	$("a.screenshot").hover(
			function(e) {
				this.t = this.title;
				this.title = "";
				var c = (this.t != "") ? "<br/>" + this.t : "";
				$("body").append(
						"<p id='screenshot'><img src='" + this.rel
								+ "' alt='url preview' />" + c + "</p>");
				$("#screenshot").css("top", (e.pageY - xOffset) + "px").css(
						"left", (e.pageX + yOffset) + "px").fadeIn("fast");
			}, function() {
				this.title = this.t;
				$("#screenshot").remove();
			});
	$("a.screenshot").mousemove(
			function(e) {
				$("#screenshot").css("top", (e.pageY - xOffset) + "px").css(
						"left", (e.pageX + yOffset) + "px");
			});
};

function textfilePreview() {
	var moveLeft = 5;
	var moveDown = 5;

	var $content = "";

	$('a.textfile').hover(
			function(e) {
				var href = this.href;
				$.get(href, function(data) {
					$content = $('<div id=pop-up>' + data + '</div>');
					$($content).appendTo('body').show();
				}, "html");
			}, function() {
				$($content).remove();
				$content = null;
			});

	 $('a.textfile').mousemove(
			function(e) {
				$($content).hide();
				$($content).css('top', e.pageY + moveDown).css('left',
						e.pageX + moveLeft).show();
			});
}


function dump(data) {
	for ( var testcase in data) {
		console.log(testcase);
		console.log($(data[testcase]).html());
	}
}

function display() {
	console.log($(this).html());
	console.log($(this).next('.reporter-method-output-div').html());

	var testcase = $(this).html();
	var messages = $(this).next('.reporter-method-output-div');
}