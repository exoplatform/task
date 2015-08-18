/*global requirejs */

// Ensure any request for this webjar brings in jQuery.
requirejs.config({
    shim: {
        "bootstrap-editable": [ 'webjars!bootstrap.js' ]
    }
});
