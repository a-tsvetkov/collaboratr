require ["webjars!knockout.js", "webjars!jquery.js", "jsRoutes"], (ko, $, jsRoutes) ->
    class Document
        constructor: (data) ->
            @id = ko.observable(data.id)
            @title = ko.observable(data.title)
            @body = ko.bservable(data.body)
            @owner_id = ko.observable(data.owner_id)
            @created_at = ko.observable(data.created_at)
            @updated_at = ko.observable(data.updated_at)
