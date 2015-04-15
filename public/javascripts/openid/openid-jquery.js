/*
Simple OpenID Plugin
http://code.google.com/p/openid-selector/

This code is licenced under the New BSD License.
*/

var providers;

var openid = {
	version: '1.2', // version constant
	demo: false,
	demo_text: 'demo text',
	cookie_expires: 30,	// 1 month.
	cookie_name: 'openid_provider',
	cookie_path: '/',
	
	img_path: '/public/images/openid/',
	lang: 'en', // language, is set in openid-jquery-<lang>.js
	signin_text: 'Valider', // text on submit button on the form
	input_id: null,
	provider_url: null,
	provider_id: null,
	all_small: false, // output large providers w/ small icons
	no_sprite: false, // don't use sprite image
	image_title: '{provider}', // for image title
	
    init: function(input_id) {
        providers = $.extend({}, providers_large, providers_small);
        
        var openid_btns = $('#openid_btns');
        
        this.input_id = input_id;
        
        $('#openid_choice').show();
        $('#openid_input_area').empty();
        
        var i = 1;
        // add box for each provider
        for (id in providers_large) {
        	if (this.all_small) {
        		openid_btns.append(this.getBoxHTML(id, providers_large[id], 'small', i++));	
        	} else
           	openid_btns.append(this.getBoxHTML(id, providers_large[id], 'large', i++));
        }
        if (providers_small) {
        	openid_btns.append('<br/>');
        	
	        for (id in providers_small) {
	        
	           	openid_btns.append(this.getBoxHTML(id, providers_small[id], 'small', i++));
	        }
        }
        
        $('#openid_form').submit(this.submit);
        
        var box_id = this.readCookie();
        if (box_id) {
        	this.signin(box_id, true);
        }  
    },
    getBoxHTML: function(box_id, provider, box_size, index) {
    	if (this.no_sprite) {
    	  var image_ext = box_size == 'small' ? '.ico.gif' : '.gif';
  	      return '<a title="'+this.image_title.replace('{provider}', provider["name"])+'" href="javascript:openid.signin(\''+ box_id +'\');"' +
    			' style="background: #FFF url(' + this.img_path + '../images.' + box_size + '/' + box_id + image_ext + ') no-repeat center center" ' + 
    			'class="' + box_id + ' openid_' + box_size + '_btn"></a>';    
    	}
   	  	var x = box_size == 'small' ? -index*24 : -index*100;
   	  	var y = box_size == 'small' ? -60 : 0;
        return '<a title="'+this.image_title.replace('{provider}', provider["name"])+'" href="javascript:openid.signin(\'' + box_id + '\');"' +
    			' style="background: #FFF url(' + this.img_path + 'openid-providers-' + this.lang + '.png); background-position: ' + x + 'px ' + y + 'px" ' +
    			'class="' + box_id + ' openid_' + box_size + '_btn"></a>';
    },
    /* Provider image click */
    signin: function(box_id, onload) {
    
    	var provider = providers[box_id];
  		if (! provider) {
  			return;
  		}
		
		this.highlight(box_id);
		this.setCookie(box_id);
		
		this.provider_id = box_id;
		this.provider_url = provider['url'];
		
		// prompt user for input?
		if (provider['label']) {
			this.useInputBox(provider);
		} else {
			$('#openid_input_area').empty();
			if (! onload) {
				$('#openid_form').submit();
			}
		}
    },
    /* Sign-in button click */
    submit: function() {
        
    	var url = openid.provider_url; 
    	if (url) {
    		url = url.replace('{username}', $('#openid_username').val());
    		openid.setOpenIdUrl(url);
    	}
    	if(openid.demo) {
    		alert(openid.demo_text + "\r\n" + document.getElementById(openid.input_id).value);
    		return false;
    	}
    	return true;
    },
    setOpenIdUrl: function (url) {
    
    	var hidden = document.getElementById(this.input_id);
    	if (hidden != null) {
    		hidden.value = url;
    	} else {
    		$('#openid_form').append('<input type="hidden" id="' + this.input_id + '" name="' + this.input_id + '" value="'+url+'"/>');
    	}
    },
    highlight: function (box_id) {
    	
    	// remove previous highlight.
    	var highlight = $('#openid_highlight');
    	if (highlight) {
    		highlight.replaceWith($('#openid_highlight a')[0]);
    	}
    	// add new highlight.
    	$('.'+box_id).wrap('<div id="openid_highlight"></div>');
    },
    setCookie: function (value) {
    
		var date = new Date();
		date.setTime(date.getTime()+(this.cookie_expires*24*60*60*1000));
		var expires = "; expires="+date.toGMTString();
		
		document.cookie = this.cookie_name+"="+value+expires+"; path=" + this.cookie_path;
    },
    readCookie: function () {
		var nameEQ = this.cookie_name + "=";
		var ca = document.cookie.split(';');
		for(var i=0;i < ca.length;i++) {
			var c = ca[i];
			while (c.charAt(0)==' ') c = c.substring(1,c.length);
			if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
		}
		return null;
    },
    useInputBox: function (provider) {
   	
		var input_area = $('#openid_input_area');
		
		var html = '';
		var id = 'openid_username';
		var value = '';
		var label = provider['label'];
		var style = '';
		
		if (label) {
			html = '<p>' + label + '</p>';
		}
		if (provider['name'] == 'OpenID') {
			id = this.input_id;
			value = 'http://';
			style = 'background:#FFF url('+this.img_path+'openid-inputicon.gif) no-repeat scroll 0 50%; padding-left:18px;';
		}
		html += '<input id="'+id+'" type="text" style="'+style+'" name="'+id+'" value="'+value+'" />' + 
					'<input id="openid_submit" type="submit" value="'+this.signin_text+'"/>';
		
		input_area.empty();
		input_area.append(html);

		$('#'+id).focus();
    },
    setDemoMode: function (demoMode) {
    	this.demo = demoMode;
    }
};
