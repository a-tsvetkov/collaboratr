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
                    size = 30
                    if @title().length > size then @title().substring(0, size).trim() + '...'  else @title

            @getList: () ->
                $.ajax(jsRoutes.controllers.DocumentApi.list())
                    .then((data) -> new Document(item) for item in data.items)
                    .promise()

            @get: (id, callback) ->
                $.ajax(jsRoutes.controllers.DocumentApi.item(id))
                    .then((data) -> new Document(data))
                    .promise()

            @remove: (item) ->
                $.ajax(jsRoutes.controllers.DocumentApi.delete(item.id())).promise()

            @create: (title) ->
                r = jsRoutes.controllers.DocumentApi.create()
                $.ajax(
                    url: r.url
                    type: r.type
                    contentType: 'application/json'
                    dataType: 'json'
                    data: JSON.stringify
                        title: title()
                ).then((data) -> new Document(data))
                .promise()
