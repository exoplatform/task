/*global requirejs */

// Ensure any request for this webjar brings in jQuery.
requirejs.config({
    shim: {
        select2: [ 'webjars!jquery.js' ]
    }
});
