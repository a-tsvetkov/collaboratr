"use strict"

requirejs.config
    shim:
        "jquery":
            exports: "$"

define("jquery", ["webjars!jquery.js"], () -> $)
define("jsRoutes", ["/routes.js"], () -> routes)

require ["webjars!knockout.js", "jquery", "viewmodels/MainViewModel", "webjars!bootstrap.js",], (ko, $, MainViewModel) ->
    model = new MainViewModel()
    ko.applyBindings(model, $ ".row")
