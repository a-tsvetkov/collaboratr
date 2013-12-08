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
                    size = 25
                    if @title().length > size then @title().substring(0, size).trim() + '...'  else @title

            @getList: (callback) ->
                $.ajax(jsRoutes.controllers.DocumentApi.list().url).done(callback)

            @get: (id, callback) ->
                $.ajax(jsRoutes.controllers.DocumentApi.item(id)).done(callback)

            @remove: (item, callback) ->
                $.ajax(jsRoutes.controllers.DocumentApi.delete(item.id())).done(callback)

            @create: (title, callback) ->
                r = jsRoutes.controllers.DocumentApi.create()
                $.ajax
                    url: r.url
                    type: r.type
                    contentType: 'application/json'
                    dataType: 'json'
                    data: JSON.stringify
                        title: title()
                    success: callback
