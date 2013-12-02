require ["webjars!knockout.js", "jquery", "jsRoutes", "models/Document"], (ko, $, jsRoutes, Document) ->
    class MainViewModel
        constructor: () ->
            @documents = ko.observableArray
            @getDocuments()

        getDocuments: (owner_id) ->
            $.ajax jsRoutes.DocumentApi.list, (data) =>
                ko.observableArray(new Document(item)) for item in data.items
