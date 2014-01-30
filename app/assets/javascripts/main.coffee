"use strict"

requirejs.config
    shim:
        "jquery":
            exports: "$"
        "webjars!sammy.js":
            deps: ["jquery"]
            exports: "Sammy"
        "webjars!chosen.jquery.min.js": ["jquery"]

define("jquery", ["webjars!jquery.min.js"], () -> $)
define("jsRoutes", ["/routes.js"], () -> routes)

require ["webjars!knockout.js", "jquery", "viewmodels/MainViewModel", "webjars!bootstrap.min.js", "webjars!chosen.jquery.min.js"],
    (ko, $, MainViewModel) ->
        model = new MainViewModel()
        ko.applyBindings model
