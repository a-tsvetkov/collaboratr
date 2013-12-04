define 'models/Document',
    ["webjars!knockout.js", "jquery", "jsRoutes"],
    (ko, $, jsRoutes) ->
        class Document
            constructor: (data) ->
                @id = ko.observable(data.id)
                @title = ko.observable(data.title)
                @body = ko.observable(data.body)
                @owner_id = ko.observable(data.owner_id)
                @created_at = ko.observable(data.created_at)
                @updated_at = ko.observable(data.updated_at)

                @shortTitle = ko.computed () =>
                    size = 23
                    if @title().length > size then @title().substring(0, size).trim() + '...'  else @title

            delete: (callback) ->
                $.ajax jsRoutes.controllers.DocumentApi.delete(@id()).url,
                    type: 'DELETE'
                    dataType: 'json'
                    success: callback

            @getList: (callback) ->
                $.get jsRoutes.controllers.DocumentApi.list().url, callback

            @get: (id, callback) ->
                $.get jsRoutes.controllers.DocumentApi.item(id).url, callback
