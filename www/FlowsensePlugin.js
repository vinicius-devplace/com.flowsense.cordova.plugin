var exec = require('cordova/exec');

var Flowsense = function(partnerToken) {
    this.partnerToken = partnerToken;
    this._pushCallback = null;
    this._registerToken = null;

    var that = this;
    var success = function(result) {
        if (result && result.additionalData) {
            that.emit('notification', result);
        }
        else {
            that.emit('registration', result);
        };
    }

    var fail = function() {
        console.log('Error reading Push Notification');
    }

    setTimeout(function() {
        exec(success, fail, "FlowsensePlugin", "registerPush", []);
    }, 100);

    this.setSenderID = function(key) {
        cordova.exec(null,null, "FlowsensePlugin", "setSenderID", [key]);
    }
    this.startSDK = function(successCallback, errorCallback) {
        cordova.exec(
            function(){console.log("FlowsenseSDK is on!")},
            function(){console.log("FlowsenseSDK not working!")},
            "FlowsensePlugin", "startSDK_fs",  [partnerToken]);
    }
    this.updatePartnerUserId = function(PartnerUserId, successCallback, errorCallback) {
        cordova.exec(
            function(){console.log("UserID Updated!")},
            function(){console.log("UserID Not Updated")},
            "FlowsensePlugin", "updatePartnerUserId_fs", [PartnerUserId]);
    }
    this.startPushiOS = function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "FlowsensePlugin", "startPushiOS", []);
    }
    this.pushNotification = function(callback) {
        cordova.exec(
            callback,
            function(){console.log("Failed to Change Views")},
            "FlowsensePlugin", "listenPush", []);
    }
    this.setKeyValues = function(key, value, successCallback, errorCallback) {
        if (Object.prototype.toString.call(value) === '[object Date]'){
            cordova.exec(
                function(){console.log("Updated Key-Values")},
                function(){console.log("Failed to Update Key-Values")},
                "FlowsensePlugin", "updateKeyValuesDate", [key, value.getTime()]);
        }
        else {
            //Necessary for Objective-C
            if (Object.prototype.toString.call(value) === '[object Boolean]') {
                cordova.exec(
                    function(){console.log("Updated Key-Values")},
                    function(){console.log("Failed to Update Key-Values")},
                    "FlowsensePlugin", "updateKeyValueBoolean", [key, value]);
            }
            else cordova.exec(
                    function(){console.log("Updated Key-Values")},
                    function(){console.log("Failed to Update Key-Values")},
                    "FlowsensePlugin", "updateKeyValues", [key, value]);
        }
        
    }
    this.commitChanges = function(successCallback, errorCallback) {
        cordova.exec(
            function(){console.log("Committed Key-Values")},
            function(){console.log("Failed to commit Key-Values")},
            "FlowsensePlugin", "commitKeyValues", []);
    }
    this.inAppEvent = function(eventName, map, successCallback, errorCallback) {
        cordova.exec(
            function(){console.log("Sent inAppEvent")},
            function(){console.log("Failed to send inAppEvent")},
            "FlowsensePlugin", "inAppEvent", [eventName, map]);
    }

    //DELETAR FUNCAO E COLOCAR PONTO E VIRGULA ACIMA
    this.getInstantLocation = function() {
        cordova.exec(null,null,"FlowsensePlugin", "getInstantLocation", [])
    };
};

Flowsense.prototype.onNotification = function(callback) {
    this._pushCallback = callback;
};

Flowsense.prototype.onRegistration = function(callback) {
    this._registerToken = callback;
}

Flowsense.prototype.emit = function() {
    var args = Array.prototype.slice.call(arguments);
    var func = args[0];
    args.splice(0,1);
    var callback;
    if (func === 'notification') {
        callback = this._pushCallback;
    }
    else{
        callback = this._registerToken;
    }

    if (typeof callback === 'function') {
        callback.apply(undefined, args);
    } else {
        console.log('event handler: must be a function');
    }
    return true;
};

module.exports = Flowsense
