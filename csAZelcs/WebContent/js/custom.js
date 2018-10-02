$(document).ready(function() {
// Plugin initializations
	$('.datepicker').pickadate({
	    selectMonths: true,
	    selectYears: 5,
	    today: 'Today',
	    clear: 'Clear',
	    close: 'Ok',
	    closeOnSelect: true
	});
	$('.timepicker').pickatime({
		default: 'now',
		fromnow: 0,
		twelvehour: false,
		donetext: 'OK',
		cleartext: 'Clear',
		canceltext: 'Cancel',
		autoclose: false,
		ampmclickable: true,
		aftershow: function() {}
	});
	$('.modal').modal();
	
	
	
//<<<<<< Some Helpers <<<<<<
		
// Clear input and display elements
	var clear = {
		display: function () {
			$('#apContainer ul').empty();
			console.log("Appointment container cleared");
		},
		modalNew: function () {
			$('#apDescrNew').val('');
			$('#apDateNew').val('');
			$('#apTimeNew').val('');
			$('#apDuratNew').val('');
			console.log("New modal cleared");
		},
		modalView: function () {
			$('#apDescr').val('');
			$('#apDate').val('');
			$('#apTime').val('');
			$('#apDurat').val('');
			$('input[type="hidden"]').val('');
			console.log("View modal cleared");
		}
	};
	
/* Client's session store appointments data to later reuse in modals 
(to reduce a number of requests to api)*/
	var session = {	
		supported: typeof(Storage) !== "undefined" ? true : false,
		support: function () {
			if (this.supported) true;
			else console.log("Sorry! No Web Storage support...");
		},
		// Update after each show appointments request
		set: function (data) {
			if (this.support) {
				sessionStorage.appos = null;
				sessionStorage.appos = data;
			}
		},
		// Get data from session
		get: function () {
			if (sessionStorage.appos) {
			    return JSON.parse(sessionStorage.appos);
			} else console.log("No data in a session...");
		},
		// Get item by id from storage
		getById: function (id) {
			if (this.support) {
				var appos = this.get();
				for (var i=0; i<appos.length; i++) {
					if (appos[i].id == id) return appos[i];
				}
			} else console.log("Sorry! No Web Storage support...");
		}
	};

// Format time to correct representation (ex. 9:5 to 09:05 )
	function formatTime(hours, minutes) {
		var h = hours >= 0 && hours <= 9 ? "0" + hours : hours;
		var min = minutes >= 0 && minutes <= 9 ? "0" + minutes : minutes;
		return h + ":" + min;
	}
	
// Request appointment info by it's id and load data into a form
	function setEventHandlersToLi() { 
		$('#apContainer ul li').on('click', function(){
			var apId = $(this).find('span').attr('id');
			var data = session.getById(apId);		// Get appointment saved in session storage by its id
			// To make sure a new user cannot load appointments of other user in display before update
			if (data.username == $('#userName').val() && data.username != undefined) {
				$('#editApModal').modal('open');
				clear.modalView();		// Clear from the old inputs before adding new
				var dT = new Date(data.apDateTime);
				// For the same format as given by a framework's plugin
				var monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
				var date = dT.getDate() + " " + monthNames[dT.getMonth()] + ", " + dT.getFullYear();
				var time = formatTime(dT.getHours(), dT.getMinutes());
				$('#apDescr').val(data.description);
				$('#apDate').val(date);
				$('#apTime').val(time);
				$('#apDurat').val(data.duration);
				$('input[type="hidden"]').val(data.id);
				Materialize.updateTextFields();		// Solve framework's related issue with labels
			} else {
				Materialize.toast("Display updated", 2000);
				$('#showApTrigger').click();
			}
		});
	}
	
//<<<<<< End <<<<<<
	
	
	
// Open a modal with a form to create a new appointment
	$('#newApModalTrigger').on('click', function() {
		$('#newApModal').modal('open');
		clear.modalNew();		// Clear modal inputs in case any old entries were left
	});

// Show user appointments button trigger
	$('#showApTrigger').on('click', function() {
		clear.display();								// Clear the display container
		var username = $('#userName').val().trim();
		var from = new Date($('#datepickerFrom').val()).getTime();
		var to = new Date($('#datepickerTo').val()).getTime();
		
		var url;
		// All appointments
		if (isNaN(from) && isNaN(to)) url = 'http://localhost:8080/csAZelcs/api/appo/user/' + username;
		// Appointments after a provided date
		else if (isNaN(to)) url = 'http://localhost:8080/csAZelcs/api/appo/user/' + username + "/after/" + from;
		// Appointments before a provided date
		else if (isNaN(from)) url = 'http://localhost:8080/csAZelcs/api/appo/user/' + username + "/before/" + to;
		// Appointments from - to dates
		else url = 'http://localhost:8080/csAZelcs/api/appo/user/' + username + "/" + from + "/" + to;
        
		if (username == "" || username == null) Materialize.toast("Username is required", 3000);
		else if (from > to) Materialize.toast("From date cannot be before To date", 3000);
		else {		// Input is valid
			$.ajax({
	            url: url,
	            type: 'GET',
	            dataType: 'json',
	            success: function(data) {
	            	session.set(JSON.stringify(data));
                	if (data.length == 0) {		// No data for this user or user is not found in db
	            		$('#apContainer ul').append("<li class=\"collection-item avatar\">" +
	                		"<i class=\"large material-icons circle blue darken-3\">sentiment_dissatisfied</i>" +
		                    "<span class=\"title\">No data found for <b>" + username + "</b></span>" +
	                	"</li>");
                	} else {		// Display appointments of a user
		               for (var i=0; i<data.length; i++) {
		                	var date = new Date(data[i].apDateTime);		// Date and time represented in one numeric value
		                	var fDate = date.getDate() + "/" + (date.getMonth() + 1) + "/" + date.getFullYear() + 
		                		" <b>at</b> " + formatTime(date.getHours(), date.getMinutes());
		                	$('#apContainer ul').append("<li class=\"collection-item avatar\">" +
		                		"<i class=\"large material-icons circle blue darken-3\">assignment</i>" +
			                    "<span id=" + data[i].id + " class=\"title\"><b>" + data[i].description + "</b></span>" +
			                    "<p>" + fDate + "<br>duration: <b>" + data[i].duration + "</b> min" +"</p>" +
		                	"</li>");
		                }
                	}
                	setEventHandlersToLi();		// Add event handlers to newly added <li> elements
	            },
	            error: function(data) { console.log(JSON.stringify(data)); }
	        });
		}
	});
	
	
// Save a new appointment button trigger
	$('#saveNewBtn').on('click', function() {
		var description = $('#apDescrNew').val();
		var date = $('#apDateNew').val();
		var time = $('#apTimeNew').val();
		var apDateTime = new Date(date + " " + (time + ":00")).getTime() + "";		// Combine date and time picker values to apDateTime
		var duration = $('#apDuratNew').val();
		var username = $('#userName').val();
		var data = {		// Combine data in one object
				"description": description,
				"apDateTime":  apDateTime,
				"duration": duration,
				"username": username
			};
		if (data.description != "" &&
		date != "" &&		// As we combine that values into one before sending,
		time != "" &&		// ..we need to check their fields for completeness
		data.duration !== "") {
			
			$.ajax({	// Send a request to API to save a new appointment
	            url: 'http://localhost:8080/csAZelcs/api/appo/',
	            type: 'POST',
	            data: data,
	            success: function(data) {
	                Materialize.toast(data, 2000);
	                // Make another api call to update the display
	                $('#showApTrigger').click();	// Required - id from newly created record in db
	            },
	            error: function(data) { console.log('error' + JSON.stringify(data)); }
	        });
		} else Materialize.toast("All fields are required", 2000);
	});
	
// Update appointment button trigger
	$('#apUpdateBtn').on('click', function() {
		var id = $('input[type="hidden"]').val();		// id is not changing as it is hidden
		// New data from a form
		var description = $('#apDescr').val();
		var date = $('#apDate').val();
		var time = $('#apTime').val();
		var apDateTime = new Date(date + " " + (time + ":00")).getTime() + "";		// Combine date and time picker values to apDateTime
		var duration = $('#apDurat').val();
		var username = $('#userName').val();
		var data = {		// Combine data in one object
				"id": id,
				"description": description,
				"apDateTime":  apDateTime,
				"duration": duration,
				"username": username
			};
		var old = session.getById(id);		// Get old version of appointment's data
		if (old.description == data.description &&		// Compare for any changes before update
			old.apDateTime == data.apDateTime &&
			old.duration == data.duration) Materialize.toast("No new changes made", 2000);
		else {
			if (data.description != "" &&
			date != "" &&		// As we combine that values into one before sending,
			time != "" &&		// ..we need to check their fields for completeness
			data.duration !== "") {
				$.ajax({		// Send a request to API to save a new appointment
		            url: 'http://localhost:8080/csAZelcs/api/appo/',
		            type: 'PUT',
		            data: data,
		            success: function(data) {
		                Materialize.toast(data, 2000);		// Make another api call to update the display
		                $('#showApTrigger').click();
		            },
		            error: function(data) { console.log('error' + JSON.stringify(data)); }
		        });
			} else Materialize.toast("All fields are required", 2000);
		}
	});
	
// Delete appointment button trigger
	$('#apDeleteBtn').on('click', function() {
		var sureQm = confirm("Delete this appointment?");		// Confirmation
		if (sureQm) {
			var id = $('#editApModal input[type="hidden"]').val();		// Get id from the hidden input
			$.ajax({		// Send a request to delete by id
	            url: 'http://localhost:8080/csAZelcs/api/appo/',
	            type: 'DELETE',
	            data: { "id": id },
	            success: function(data) {
	                Materialize.toast(data, 2000);
	                // Check if <span> with the appointment id is existing in DOM
	                var exists = $('#' + id).length;		// To update the display (client only - avoiding another api call)
	                if (exists) $('#' + id).parent().remove();		// Go to it's parent element <li> and remove it
	            },
	            error: function(data) { console.log('error' + JSON.stringify(data)); }
	        });
		}
	});
	
});